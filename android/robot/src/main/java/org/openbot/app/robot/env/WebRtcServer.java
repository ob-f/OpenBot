package org.openbot.app.robot.env;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceView;
import android.view.TextureView;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;
import com.pedro.rtplibrary.view.OpenGlView;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.openbot.app.robot.utils.AndGate;
import org.openbot.app.robot.utils.Constants;
import org.openbot.app.robot.utils.ConnectionUtils;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.CapturerObserver;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.JavaI420Buffer;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import timber.log.Timber;

/*
This class initiates a WebRTC call to the controller, by sending an WebRTC "offer"
to the controller, providing its A/V capabilities. It then waits for an "answer" with
controller's capabilities. The two sides then exchange ICE candidates until a suitable
common capabilities are found, and then media is streamed from this class to the controller.

Note that the media is streamed only one way from this class to the controller.

WebRTC does not specify signaling protocol. Usually, a separate signaling server is used
witch mediates between the two WebRTC peers, and communication from and to this server is
carried over WebSocket. However, we already have a communication channel between the peers
(NetworkServiceConnection) so we are using it instead. No separate signaling server is required.

It is possible in the future to factor out signaling into a separate class and provide
various signalling types, such as to separate signalling server.
 */
public class WebRtcServer implements IVideoServer {
  private final String TAG = "WebRtcPeer";
  private SurfaceViewRenderer view;
  private Size resolution = new Size(640, 360);

  public static final String VIDEO_TRACK_ID = "ARDAMSv0";
  public static final int VIDEO_RESOLUTION_WIDTH = 640;
  public static final int VIDEO_RESOLUTION_HEIGHT = 360;
  public static final int FPS = 30;

  // WebRTC-specific
  private EglBase rootEglBase;
  private PeerConnectionFactory factory;
  private VideoTrack videoTrackFromCamera;
  MediaConstraints audioConstraints;
  AudioSource audioSource;
  AudioTrack localAudioTrack;
  SurfaceTextureHelper surfaceTextureHelper;
  private PeerConnection peerConnection;
  MediaStream mediaStream;
  private RtpSender videoSender;
  private RtpSender audioSender;

  private AndGate andGate;
  private Context context;
  private VideoCapturer videoCapturer;

  private final SignalingHandler signalingHandler = new SignalingHandler();

  // The robot preview (CameraFragment) and WebRTC cannot both open the camera. While a preview
  // is on screen, WebRTC streams the preview's frames instead of opening the camera itself.
  private static final Object frameLock = new Object();
  private static WeakReference<WebRtcServer> activeServer = new WeakReference<>(null);
  private static Object localPreviewOwner;
  private static CapturerObserver sharedFrameObserver;

  private VideoSource videoSource;
  private boolean cameraCapturing = false;
  private boolean streaming = false;

  public WebRtcServer() {}

  // IVideoServer Interface
  @Override
  public void init(Context context) {
    this.context = context;
    activeServer = new WeakReference<>(this);

    andGate = new AndGate(() -> startServer(), () -> stopServer());
    andGate.addCondition("connected");
    andGate.addCondition("view set");
    andGate.addCondition("camera permission");
    andGate.addCondition("resolution set");
    andGate.addCondition("can start");

    int camera = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA);
    andGate.set("camera permission", camera == PackageManager.PERMISSION_GRANTED);

    rootEglBase = EglBase.create();

    signalingHandler.handleControllerWebRtcEvents();

    // The controller socket really closed: end the call so the next controller gets a fresh one.
    ControllerToBotEventBus.subscribe(
        "WebRtcServer.disconnect",
        event -> mainHandler.post(this::closeCall),
        error -> Log.d(TAG, "Error occurred in disconnect monitor: " + error),
        event -> event.has("command") && "DISCONNECTED".equals(event.getString("command")));
  }

  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public void setCanStart(boolean canStart) {
    andGate.set("can start", canStart);
  }

  @Override
  public void startClient() {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_PROTOCOL", "WEBRTC"));
    sendServerUrl();
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_COMMAND", "START"));
  }

  @Override
  public void sendServerUrl() {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_SERVER_URL", ""));
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("FRAGMENT_TYPE", ""));
  }

  @Override
  public void sendVideoStoppedStatus() {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_COMMAND", "STOP"));
  }

  @Override
  public void setView(SurfaceView view) {}

  @Override
  public void setView(TextureView view) {}

  @Override
  public void setView(SurfaceViewRenderer view) {
    this.view = view;
    this.view.setEnabled(false);
    andGate.set("view set", true);
  }

  @Override
  public void setView(OpenGlView view) {}

  @Override
  public void setConnected(boolean connected) {
    andGate.set("connected", connected);

    int camera = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA);
    andGate.set("camera permission", camera == PackageManager.PERMISSION_GRANTED);
  }

  @Override
  public void setResolution(int w, int h) {
    resolution = new Size(w, h);
    andGate.set("resolution set", true);
  }
  // end Interface

  // Shared camera frames from CameraFragment

  /** Called on the main thread when a CameraFragment binds or releases the camera preview. */
  public static void setLocalPreviewActive(Object owner, boolean active) {
    if (active) {
      localPreviewOwner = owner;
    } else if (localPreviewOwner == owner) {
      localPreviewOwner = null;
    } else {
      return; // a newer preview already owns the camera
    }
    WebRtcServer server = activeServer.get();
    if (server != null) {
      server.applyVideoSource();
    }
  }

  /** Called on the camera analysis thread for every preview frame. */
  public static void onLocalPreviewFrame(ImageProxy image) {
    if (image.getFormat() != ImageFormat.YUV_420_888) {
      return;
    }
    synchronized (frameLock) {
      if (sharedFrameObserver == null) {
        return;
      }
      VideoFrame frame =
          new VideoFrame(
              toI420Buffer(image), image.getImageInfo().getRotationDegrees(), System.nanoTime());
      sharedFrameObserver.onFrameCaptured(frame);
      frame.release();
    }
  }

  private static JavaI420Buffer toI420Buffer(ImageProxy image) {
    int width = image.getWidth();
    int height = image.getHeight();
    int chromaWidth = (width + 1) / 2;
    int chromaHeight = (height + 1) / 2;
    JavaI420Buffer buffer = JavaI420Buffer.allocate(width, height);
    ImageProxy.PlaneProxy[] planes = image.getPlanes();
    copyPlane(planes[0], width, height, buffer.getDataY(), buffer.getStrideY());
    copyPlane(planes[1], chromaWidth, chromaHeight, buffer.getDataU(), buffer.getStrideU());
    copyPlane(planes[2], chromaWidth, chromaHeight, buffer.getDataV(), buffer.getStrideV());
    return buffer;
  }

  private static void copyPlane(
      ImageProxy.PlaneProxy plane, int width, int height, ByteBuffer dst, int dstStride) {
    ByteBuffer src = plane.getBuffer().duplicate();
    int rowStride = plane.getRowStride();
    int pixelStride = plane.getPixelStride();
    int srcLimit = src.limit();
    for (int y = 0; y < height; y++) {
      int srcRow = y * rowStride;
      int dstRow = y * dstStride;
      if (pixelStride == 1) {
        src.limit(srcRow + width).position(srcRow);
        dst.position(dstRow);
        dst.put(src);
        src.limit(srcLimit);
      } else {
        for (int x = 0; x < width; x++) {
          dst.put(dstRow + x, src.get(srcRow + x * pixelStride));
        }
      }
    }
    dst.rewind();
  }

  // Chooses where streamed frames come from: nothing (paused), the preview's frames, or WebRTC's
  // own camera capturer. The camera is always released before the preview needs it.
  private void applyVideoSource() {
    if (videoSource == null) {
      return;
    }
    boolean usePreviewFrames = streaming && localPreviewOwner != null;
    if (usePreviewFrames || !streaming) {
      stopCameraCapture();
    }
    synchronized (frameLock) {
      sharedFrameObserver = usePreviewFrames ? videoSource.getCapturerObserver() : null;
    }
    if (usePreviewFrames) {
      videoSource.getCapturerObserver().onCapturerStarted(true);
    } else if (streaming) {
      startCameraCapture();
    }
  }

  private void startCameraCapture() {
    if (videoCapturer != null && !cameraCapturing) {
      videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
      cameraCapturing = true;
    }
  }

  private void stopCameraCapture() {
    if (videoCapturer != null && cameraCapturing) {
      try {
        videoCapturer.stopCapture();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      cameraCapturing = false;
    }
  }

  // local methods
  private void startServer() {
    streaming = true;
    if (peerConnection != null) {
      // The Flutter controller keeps one peer connection per session and cannot accept a new
      // offer on it, so reuse the existing call and just resume the video.
      applyVideoSource();
      startClient();
      return;
    }

    initializeSurfaceViews();
    initializePeerConnectionFactory();
    createVideoTrackFromCameraAndShowIt();
    if (videoTrackFromCamera == null) {
      Log.e(TAG, "startServer: no camera available, video not started");
      closeCall();
      return;
    }
    initializePeerConnections();

    startStreamingVideo();
    doCall();
    startClient();
    monitorCameraControlEvents();
    applyVideoSource();
  }

  // Delay to let the local camera actually finish releasing before we switch.
  private static final long LOCAL_CAMERA_RELEASE_DELAY_MS = 300;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private void monitorCameraControlEvents() {
    ControllerToBotEventBus.subscribe(
        this.getClass().getSimpleName(),
        event -> {
          switch (event.getString("command")) {
            case "SWITCH_CAMERA":
              Log.d(TAG, "Received SWITCH_CAMERA command");
              if (localPreviewOwner != null) {
                // The robot preview owns the camera; WebRTC is only forwarding its frames.
                emitSwitchCameraStatus("ERROR:LOCAL_PREVIEW_ACTIVE");
                break;
              }
              if (!(videoCapturer instanceof CameraVideoCapturer)) {
                Log.e(TAG, "Cannot switch camera: capturer is not ready");
                emitSwitchCameraStatus("ERROR:CAPTURER_NOT_READY");
                break;
              }
              switchCameraWithoutConflictingWithLocalPreview();
              break;
          }
        },
        error -> {
          Log.d(null, "Error occurred in monitorCameraControlEvents: " + error);
        },
        event ->
            event.has("command")
                && ("SWITCH_CAMERA".equals(event.getString("command")))
        );
  }

  // Pauses the local preview camera first so we don't open two cameras at once.
  private void switchCameraWithoutConflictingWithLocalPreview() {
    emitLocalCameraCommand(Constants.CMD_PAUSE_LOCAL_CAMERA);

    mainHandler.postDelayed(
        () ->
            ((CameraVideoCapturer) videoCapturer)
                .switchCamera(
                    new CameraVideoCapturer.CameraSwitchHandler() {
                      @Override
                      public void onCameraSwitchDone(boolean isFrontCamera) {
                        String cameraName = isFrontCamera ? "FRONT" : "BACK";
                        Log.d(TAG, "Camera switched successfully to " + cameraName);
                        emitSwitchCameraStatus(cameraName);
                        resumeLocalCamera();
                      }

                      @Override
                      public void onCameraSwitchError(String errorDescription) {
                        Log.e(TAG, "Camera switch failed: " + errorDescription);
                        emitSwitchCameraStatus("ERROR:" + errorDescription);
                        resumeLocalCamera();
                      }
                    }),
        LOCAL_CAMERA_RELEASE_DELAY_MS);
  }

  private void resumeLocalCamera() {
    emitLocalCameraCommand(Constants.CMD_RESUME_LOCAL_CAMERA);
  }

  private void emitLocalCameraCommand(String command) {
    try {
      ControllerToBotEventBus.emitEvent(new JSONObject().put("command", command).toString());
    } catch (JSONException e) {
      Timber.e(e, "Failed to emit local camera lifecycle command: %s", command);
    }
  }

  private void emitSwitchCameraStatus(String value) {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("SWITCH_CAMERA", value));
  }

  private void doAnswer() {
    peerConnection.createAnswer(
        new SimpleSdpObserver() {
          @Override
          public void onCreateSuccess(SessionDescription sessionDescription) {
            peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
            JSONObject message = new JSONObject();
            try {
              message.put("type", "answer");
              message.put("sdp", sessionDescription.description);
              sendMessage(message);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        },
        new MediaConstraints());
  }

  // Logs whether the negotiated SDP actually contains a video m-line, so a
  // logcat pull tells us immediately whether the break is in track
  // negotiation (this log) vs. transport/rendering (later logs).
  private void logSdp(String label, SessionDescription sdp) {
    boolean hasVideo = sdp.description.contains("m=video");
    boolean hasAudio = sdp.description.contains("m=audio");
    Log.d(TAG, "SDP [" + label + "]: m=video present=" + hasVideo + " m=audio present=" + hasAudio);
  }

  private void startStreamingVideo() {
    mediaStream = factory.createLocalMediaStream("ARDAMS");
    mediaStream.addTrack(videoTrackFromCamera);
    mediaStream.addTrack(localAudioTrack);
    // peerConnection.addStream() is a Plan-B-only API and is a no-op under the
    // Unified Plan SDP semantics that the current WebRTC build defaults to, so
    // tracks must be attached with addTrack() instead.
    List<String> streamIds = Collections.singletonList(mediaStream.getId());
    videoSender = peerConnection.addTrack(videoTrackFromCamera, streamIds);
    audioSender = peerConnection.addTrack(localAudioTrack, streamIds);
    Log.d(
        TAG,
        "startStreamingVideo: videoSender="
            + (videoSender != null)
            + " audioSender="
            + (audioSender != null)
            + " videoTrack.state="
            + videoTrackFromCamera.state());
  }

  private void stopStreamingVideo() {
    if (videoSender != null) {
      peerConnection.removeTrack(videoSender);
    }
    if (audioSender != null) {
      peerConnection.removeTrack(audioSender);
    }
  }

  // Pause only: release the camera and stop sending frames, but keep the call alive.
  private void stopServer() {
    streaming = false;
    applyVideoSource();
    stopClient();
  }

  // Ends the call and frees all WebRTC resources. Only used when the controller disconnects.
  private void closeCall() {
    if (factory == null) {
      return; // nothing was started
    }
    streaming = false;
    applyVideoSource();
    if (peerConnection != null) {
      peerConnection.dispose();
      peerConnection = null;
    }
    mediaStream = null;
    videoSender = null;
    audioSender = null;
    videoTrackFromCamera = null;
    localAudioTrack = null;
    if (videoCapturer != null) {
      videoCapturer.dispose();
      videoCapturer = null;
    }
    if (videoSource != null) {
      videoSource.dispose();
      videoSource = null;
    }
    if (audioSource != null) {
      audioSource.dispose();
      audioSource = null;
    }
    if (surfaceTextureHelper != null) {
      surfaceTextureHelper.dispose();
      surfaceTextureHelper = null;
    }
    if (factory != null) {
      factory.dispose();
      factory = null;
    }
    view.release();
  }

  private void stopClient() {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_COMMAND", "STOP"));
  }

  private void doCall() {
    MediaConstraints sdpMediaConstraints = new MediaConstraints();

    sdpMediaConstraints.mandatory.add(
        new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
    sdpMediaConstraints.mandatory.add(
        new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));

    peerConnection.createOffer(
        new SimpleSdpObserver() {
          @Override
          public void onCreateSuccess(SessionDescription sessionDescription) {
            peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
            JSONObject message = new JSONObject();
            try {
              message.put("type", "offer");
              message.put("sdp", sessionDescription.description);

              sendMessage(message);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        },
        sdpMediaConstraints);
  }

  private void initializePeerConnections() {
    peerConnection = createPeerConnection(factory);
  }

  private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
    ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();

    PeerConnection.IceServer stunServer =
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
    iceServers.add(stunServer);

    PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
    rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
    MediaConstraints pcConstraints = new MediaConstraints();

    PeerConnection.Observer pcObserver =
        new PeerConnection.Observer() {
          @Override
          public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d(TAG, "onSignalingChange: ");
          }

          @Override
          public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "onIceConnectionChange: ");
          }

          @Override
          public void onStandardizedIceConnectionChange(
              PeerConnection.IceConnectionState newState) {}

          @Override
          public void onConnectionChange(PeerConnection.PeerConnectionState newState) {}

          @Override
          public void onIceConnectionReceivingChange(boolean b) {
            Log.d(TAG, "onIceConnectionReceivingChange: ");
          }

          @Override
          public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.d(TAG, "onIceGatheringChange: ");
          }

          @Override
          public void onIceCandidate(IceCandidate iceCandidate) {
            Log.d(TAG, "onIceCandidate: ");
            JSONObject message = new JSONObject();

            try {
              message.put("type", "candidate");
              message.put("label", iceCandidate.sdpMLineIndex);
              message.put("id", iceCandidate.sdpMid);
              message.put("candidate", iceCandidate.sdp);

              Log.d(TAG, "onIceCandidate: sending candidate " + message);
              sendMessage(message);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            Log.d(TAG, "onIceCandidatesRemoved: ");
          }

          @Override
          public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {}

          @Override
          public void onAddStream(MediaStream mediaStream) {
            Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
            VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
            AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
            remoteAudioTrack.setEnabled(true);
            remoteVideoTrack.setEnabled(true);
            remoteVideoTrack.addSink(view);
          }

          @Override
          public void onRemoveStream(MediaStream mediaStream) {
            Log.d(TAG, "onRemoveStream: ");
          }

          @Override
          public void onDataChannel(DataChannel dataChannel) {
            Log.d(TAG, "onDataChannel: ");
          }

          @Override
          public void onRenegotiationNeeded() {
            Log.d(TAG, "onRenegotiationNeeded: ");
          }

          @Override
          public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}

          @Override
          public void onTrack(RtpTransceiver transceiver) {}
        };

    return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
  }
  private void sendMessage(JSONObject message) {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("WEB_RTC_EVENT", message));
  }

  private void createVideoTrackFromCameraAndShowIt() {
    audioConstraints = new MediaConstraints();
    videoCapturer = createVideoCapturer();
    if (videoCapturer == null) {
      Log.e(TAG, "createVideoTrackFromCameraAndShowIt: no back-facing camera capturer found");
      return;
    }
    videoSource = factory.createVideoSource(videoCapturer.isScreencast());
    videoSource.adaptOutputFormat(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

    surfaceTextureHelper =
        SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
    videoCapturer.initialize(
        surfaceTextureHelper,
        context /*getApplicationContext()*/,
        videoSource.getCapturerObserver());

    // Capture (or using the preview's frames) is started in applyVideoSource().

    videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
    videoTrackFromCamera.setEnabled(true);
    videoTrackFromCamera.addSink(view);

    // create an AudioSource instance
    audioSource = factory.createAudioSource(audioConstraints);
    localAudioTrack = factory.createAudioTrack("101", audioSource);
  }

  private void initializePeerConnectionFactory() {
    // PeerConnectionFactory.initialize() loads the native WebRTC library; it must
    // run before any class that calls into native code (e.g. DefaultVideoEncoderFactory's
    // SoftwareVideoEncoderFactory) is constructed, or it fails with UnsatisfiedLinkError.
    PeerConnectionFactory.InitializationOptions initializationOptions =
            PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions();
    PeerConnectionFactory.initialize(initializationOptions);

    VideoEncoderFactory encoderFactory =
        new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(), true, true);
    VideoDecoderFactory decoderFactory =
        new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

    PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
    options.networkIgnoreMask = 16;
    options.disableEncryption = false;
    options.disableNetworkMonitor = true;

    factory =
        PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setOptions(options)
            .createPeerConnectionFactory();
  }

  private void initializeSurfaceViews() {
    view.init(rootEglBase.getEglBaseContext(), null);
    view.setEnableHardwareScaler(true);
  }

  private VideoCapturer createVideoCapturer() {
    VideoCapturer videoCapturer;
    if (useCamera2()) {
      videoCapturer = createCameraCapturer(new Camera2Enumerator(context));
    } else {
      videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
    }
    return videoCapturer;
  }

  private boolean useCamera2() {
    return Camera2Enumerator.isSupported(context);
  }

  private final CameraVideoCapturer.CameraEventsHandler cameraEventsHandler =
      new CameraVideoCapturer.CameraEventsHandler() {
        @Override
        public void onCameraError(String errorDescription) {
          Log.e(TAG, "Camera onCameraError: " + errorDescription);
        }

        @Override
        public void onCameraDisconnected() {
          Log.e(TAG, "Camera onCameraDisconnected");
        }

        @Override
        public void onCameraFreezed(String errorDescription) {
          Log.e(TAG, "Camera onCameraFreezed: " + errorDescription);
        }

        @Override
        public void onCameraOpening(String cameraName) {
          Log.d(TAG, "Camera onCameraOpening: " + cameraName);
        }

        @Override
        public void onFirstFrameAvailable() {
          Log.d(TAG, "Camera onFirstFrameAvailable");
        }

        @Override
        public void onCameraClosed() {
          Log.d(TAG, "Camera onCameraClosed");
        }
      };

  private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
    final String[] deviceNames = enumerator.getDeviceNames();

    for (String deviceName : deviceNames) {
      if (enumerator.isBackFacing(deviceName)) {
        VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, cameraEventsHandler);
        if (videoCapturer != null) {
          return videoCapturer;
        }
      }
    }

    return null;
  }

  // Utils
  private void beep() {
    final ToneGenerator tg = new ToneGenerator(6, 100);
    tg.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE);
  }

  class SignalingHandler {
    void handleControllerWebRtcEvents() {
      ControllerToBotEventBus.subscribe(
          "WEB_RTC_COMMANDS",
          event -> {
            if (peerConnection == null) {
              // Late message after the call closed. Throwing here would end this subscription.
              Log.d(TAG, "Ignoring WebRTC event: no active call");
              return;
            }
            String commandType = "";
            // The controller sometimes sends webrtc_event as a JSON string instead of an object.
            Object rawEvent = event.get("webrtc_event");
            JSONObject webRtcEvent =
                rawEvent instanceof JSONObject
                    ? (JSONObject) rawEvent
                    : new JSONObject(rawEvent.toString());
            String type = webRtcEvent.getString("type");
            switch (type) {
              case "offer":
                Timber.d("connectToSignallingServer: received an offer $isInitiator $isStarted");
                peerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(
                        SessionDescription.Type.OFFER, webRtcEvent.getString("sdp")));
                doAnswer();
                break;

              case "answer":
                String remoteDescr = webRtcEvent.getString("sdp");
                Timber.i("Got remote description %s", remoteDescr);
                peerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(SessionDescription.Type.ANSWER, remoteDescr));
                break;

              case "candidate":
                IceCandidate candidate =
                    new IceCandidate(
                        webRtcEvent.getString("id"),
                        webRtcEvent.getInt("label"),
                        webRtcEvent.getString("candidate"));
                peerConnection.addIceCandidate(candidate);
                break;
            }
          },
          error -> Log.d(TAG, "Error occurred in handleControllerWebRtcEvents: %s", error),
          commandJsn ->
              commandJsn.has("webrtc_event") // filter out all non "webrtc_event" messages.
          );
    }

    public void shutDown() {
      // Not used
      ControllerToBotEventBus.unsubscribe("WEB_RTC_COMMANDS");
    }
  }
}
