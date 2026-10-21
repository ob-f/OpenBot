//
// Created by Nitish Yadav on 03/01/23.
//

import UIKit
import WebRTC

protocol WebRTCClientDelegate {
    func didGenerateCandidate(iceCandidate: RTCIceCandidate)
    func didIceConnectionStateChanged(iceConnectionState: RTCIceConnectionState)
    func didOpenDataChannel()
    func didReceiveData(data: Data)
    func didReceiveMessage(message: String)
    func didConnectWebRTC()
    func didDisconnectWebRTC()
}

class WebRTCClient: NSObject, RTCPeerConnectionDelegate {

    private var peerConnectionFactory: RTCPeerConnectionFactory!
    private var peerConnection: RTCPeerConnection?
    private var videoCapturer: RTCVideoCapturer!
    private var localVideoTrack: RTCVideoTrack!
    private var localAudioTrack: RTCAudioTrack!
    private var localRenderView: RTCEAGLVideoView?
    private var localView: UIView!
    private var remoteStream: RTCMediaStream?
    private var dataChannel: RTCDataChannel?
    private var localDataChannel: RTCDataChannel?
    private var remoteDataChannel: RTCDataChannel?
    private var channels: (video: Bool, audio: Bool, datachannel: Bool) = (false, false, false)
    private var customFrameCapturer: Bool = true
    private var cameraDevicePosition: AVCaptureDevice.Position = .front
    var delegate: WebRTCClientDelegate?
    public private(set) var isConnected: Bool = false

    func localVideoView() -> UIView {
        localView
    }


    override init() {
        super.init()
        peerConnection?.delegate = self
    }

    deinit {
        peerConnectionFactory = nil
        peerConnection = nil
    }

    // MARK: - Public functions
    func setup(videoTrack: Bool, audioTrack: Bool, dataChannel: Bool, customFrameCapturer: Bool) {
        channels.video = videoTrack
        channels.audio = audioTrack
        channels.datachannel = dataChannel
        self.customFrameCapturer = customFrameCapturer
        let videoEncoderFactory = RTCDefaultVideoEncoderFactory()
        let videoDecoderFactory = RTCDefaultVideoDecoderFactory()
        peerConnectionFactory = RTCPeerConnectionFactory(encoderFactory: videoEncoderFactory, decoderFactory: videoDecoderFactory)
        setupLocalTracks()
    }

    func captureCurrentFrame(sampleBuffer: CMSampleBuffer) {
        if let capturer = videoCapturer as? RTCCustomFrameCapturer {
            capturer.capture(sampleBuffer)
        }
    }

    func captureCurrentFrame(sampleBuffer: CVPixelBuffer) {
        if let capturer = videoCapturer as? RTCCustomFrameCapturer {
            capturer.capture(sampleBuffer)
        }
    }

    /// MARK: Connect with peer connection after successful data match
    func connect(onSuccess: @escaping (RTCSessionDescription) -> Void) {
        peerConnection = setupPeerConnection()
        peerConnection!.delegate = self

        if channels.video {
            peerConnection!.add(localVideoTrack, streamIds: ["stream0"])
        }
        if channels.audio {
            peerConnection!.add(localAudioTrack, streamIds: ["stream0"])
        }
        if channels.datachannel {
            dataChannel = setupDataChannel();
            dataChannel?.delegate = self
            localDataChannel = dataChannel;
            print("data channel is ======>",dataChannel);
        }
        makeOffer(onSuccess: onSuccess)
    }

    // MARK: HangUp
    func disconnect() {
        if peerConnection != nil {
            peerConnection!.close()
        }
    }

    /// MARK: Signaling Event; to receive offer from the connection and start sending audio and video tracks.
    func receiveOffer(offerSDP: RTCSessionDescription, onCreateAnswer: @escaping (RTCSessionDescription) -> Void) {
        if (peerConnection == nil) {
            peerConnection = setupPeerConnection()
            peerConnection!.delegate = self
            if channels.video {
                peerConnection!.add(localVideoTrack, streamIds: ["stream-0"])
            }
            if channels.audio {
                peerConnection!.add(localAudioTrack, streamIds: ["stream-0"])
            }
            if channels.datachannel {
                dataChannel = setupDataChannel()
                dataChannel?.delegate = self
                localDataChannel = dataChannel;
            }

        }
        peerConnection!.setRemoteDescription(offerSDP) { (err) in
            if let error = err {
                print("failed to set remote offer SDP")
                print(error)
                return
            }
            self.makeAnswer(onCreateAnswer: onCreateAnswer)
        }
    }

    /// function to receive the answer to create the descriptor and set description.
    func receiveAnswer(answerSDP: RTCSessionDescription) {
        peerConnection!.setRemoteDescription(answerSDP) { (err) in
            if let error = err {
                print("failed to set remote answer SDP")
                print(error)
                return
            }
        }
    }

    /// function to receive the candidate and add it to the connection.
    func receiveCandidate(candidate: RTCIceCandidate) {
        peerConnection!.add(candidate)
    }

    /// function to create the connection
    private func setupPeerConnection() -> RTCPeerConnection {
        let rtcConf = RTCConfiguration()
        rtcConf.iceServers = [RTCIceServer(urlStrings: ["stun:stun.l.google.com:19302",
                                                        "stun:stun1.l.google.com:19302",
                                                        "stun:stun2.l.google.com:19302",
                                                        "stun:stun3.l.google.com:19302",
                                                        "stun:stun4.l.google.com:19302"])]
        let mediaConstraints = RTCMediaConstraints.init(mandatoryConstraints: nil, optionalConstraints: nil)
        let pc = peerConnectionFactory.peerConnection(with: rtcConf, constraints: mediaConstraints, delegate: nil)
        return pc
    }

    //MARK: - Local Media
    private func setupLocalTracks() {
        if channels.video == true {
            localVideoTrack = createVideoTrack()
        }
        if channels.audio == true {
            localAudioTrack = createAudioTrack()
        }
    }

    /// function to create the video track for the video.
    private func createAudioTrack() -> RTCAudioTrack {
        let audioConstrains = RTCMediaConstraints(mandatoryConstraints: nil, optionalConstraints: nil)
        let audioSource = peerConnectionFactory.audioSource(with: audioConstrains)
        let audioTrack = peerConnectionFactory.audioTrack(with: audioSource, trackId: "audio0")
        audioTrack.source.volume = 10.0
        return audioTrack
    }

    /// function to create the video track to send to the controller device
    private func createVideoTrack() -> RTCVideoTrack {
        let videoSource = peerConnectionFactory.videoSource()
        if customFrameCapturer {
            videoCapturer = RTCCustomFrameCapturer(delegate: videoSource)
        }
        let videoTrack = peerConnectionFactory.videoTrack(with: videoSource, trackId: "video0")
        return videoTrack
    }

    /// MARK: - Local Data
    private func setupDataChannel() -> RTCDataChannel {
        let dataChannelConfig = RTCDataChannelConfiguration()
        dataChannelConfig.channelId = 0
        let _dataChannel = peerConnection?.dataChannel(forLabel: "dataChannel", configuration: dataChannelConfig)
        return _dataChannel!
    }

    /// MARK: - Signaling Offer/Answer
    private func makeOffer(onSuccess: @escaping (RTCSessionDescription) -> Void) {
        peerConnection?.offer(for: RTCMediaConstraints.init(mandatoryConstraints: nil, optionalConstraints: nil)) { (sdp, err) in
            if let error = err {("error with make offer")
                print(error)
                return
            }

            if let offerSDP = sdp {
                self.peerConnection!.setLocalDescription(offerSDP, completionHandler: { (err) in
                    if let error = err {
                        print("error with set local offer sdp")
                        print(error)
                        return
                    }
                    onSuccess(offerSDP)
                })
            }

        }
    }
    /// function to create answer for the controller
    private func makeAnswer(onCreateAnswer: @escaping (RTCSessionDescription) -> Void) {
        peerConnection!.answer(for: RTCMediaConstraints(mandatoryConstraints: nil, optionalConstraints: nil), completionHandler: { (answerSessionDescription, err) in
            if let error = err {
                print("failed to create local answer SDP")
                print(error)
                return
            }
            if let answerSDP = answerSessionDescription {
                self.peerConnection!.setLocalDescription(answerSDP, completionHandler: { (err) in
                    if let error = err {
                        print("failed to set local answer SDP")
                        print(error)
                        return
                    }
                    print("succeed to set local answer SDP")
                    onCreateAnswer(answerSDP)
                })
            }
        })
    }

    // MARK: - Connection Events
    private func onConnected() {
        isConnected = true
        DispatchQueue.main.async {
            self.delegate?.didConnectWebRTC()
        }
    }

    private func onDisConnected() {
        isConnected = false
        DispatchQueue.main.async {
            self.peerConnection!.close()
            self.peerConnection = nil
            self.dataChannel = nil
            self.delegate?.didDisconnectWebRTC()
        }
    }
}

// MARK: - PeerConnection Delegates
extension WebRTCClient {
    func peerConnection(_ peerConnection: RTCPeerConnection, didChange stateChanged: RTCSignalingState) {
    }

    func peerConnection(_ peerConnection: RTCPeerConnection, didChange newState: RTCIceConnectionState) {
        switch newState {
        case .connected, .completed:
            if !isConnected {
                onConnected()
            }
        default:
            if isConnected {
                onDisConnected()
            }
        }

        DispatchQueue.main.async {
            self.delegate?.didIceConnectionStateChanged(iceConnectionState: newState)
        }
    }

    func peerConnection(_ peerConnection: RTCPeerConnection, didAdd stream: RTCMediaStream) {
        remoteStream = stream
        if stream.videoTracks.first != nil {
            print("video track found")
        }

        if let audioTrack = stream.audioTracks.first {
            print("audio track found")
            audioTrack.source.volume = 8
        }
    }

    func peerConnection(_ peerConnection: RTCPeerConnection, didGenerate candidate: RTCIceCandidate) {
        delegate?.didGenerateCandidate(iceCandidate: candidate)
    }

    func peerConnection(_ peerConnection: RTCPeerConnection, didRemove stream: RTCMediaStream) {
    }

    func peerConnection(_ peerConnection: RTCPeerConnection, didOpen dataChannel: RTCDataChannel) {
        remoteDataChannel = dataChannel
        delegate?.didOpenDataChannel()
        dataChannel.delegate = self
    }




    func peerConnection(_ peerConnection: RTCPeerConnection, didRemove candidates: [RTCIceCandidate]) {
    }

    func peerConnectionShouldNegotiate(_ peerConnection: RTCPeerConnection) {
    }

    func peerConnection(_ peerConnection: RTCPeerConnection, didChange newState: RTCIceGatheringState) {
    }

    // Assume you have a function to send messages on the other end
    func sendMessage(message: String) {
        if let dataChannel = webRTCClient.remoteDataChannel {
            let data = Data(message.utf8)
            let buffer = RTCDataBuffer(data: data, isBinary: false)
            dataChannel.sendData(buffer)
        }
    }

}

extension WebRTCClient : RTCDataChannelDelegate {
    func dataChannelDidChangeState(_ dataChannel: RTCDataChannel) {
        debugPrint("dataChannel did change state: \(dataChannel.readyState)")
    }

    func dataChannel(_ dataChannel: RTCDataChannel, didReceiveMessageWith buffer: RTCDataBuffer) {
        delegate?.didReceiveData(data: buffer.data);
    }

     func dataChannel(_ dataChannel: RTCDataChannel, didChangeBufferedAmount amount: UInt64) {
        print("did changed buffer amount");
    }


}

