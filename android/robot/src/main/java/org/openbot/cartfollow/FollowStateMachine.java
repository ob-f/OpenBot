package org.openbot.cartfollow;

import android.graphics.Bitmap;
import android.graphics.RectF;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.openbot.tflite.Detector.Recognition;
import org.openbot.vehicle.Control;
import org.openbot.vehicle.RangeTelemetrySnapshot;

public class FollowStateMachine {

  public static final class InitializationObservation {
    public final Recognition candidate;
    public final int trackId;
    public final boolean uniqueHighConfidence;

    public InitializationObservation(
        Recognition candidate, int trackId, boolean uniqueHighConfidence) {
      this.candidate = candidate;
      this.trackId = trackId;
      this.uniqueHighConfidence = uniqueHighConfidence;
    }
  }

  public static class FrameResult {
    public final FollowState state;
    public final Control control;
    public final Recognition target;
    public final Recognition candidate;
    public final List<Recognition> persons;
    public final boolean matched;
    public final boolean tooClose;
    public final Bitmap snapshot;
    public final int countdownSec;
    public float matchScore;
    public ImageSetpointDistanceEstimator.DistanceEstimate distanceEstimate;
    public BehaviorDecisionResult behaviorDecision;
    public IdentityEvidence identityEvidence;
    public SteeringEvidence steeringEvidence;
    public GalleryUpdateStatus galleryUpdateStatus;
    public SimulatorAutoDriveController.Result simulatorDriveResult;
    public RealCartAutoDriveController.Result realDriveResult;
    public DetectionTierEvidence detectionTierEvidence;
    public IdentityCandidateSet identityCandidates;
    public DirectedReacquireEvidence directedReacquireEvidence;
    public TargetObservationEvidence targetObservation;
    public FrameTimingEvidence frameTiming;
    public TrackingDecision trackingDecision;
    public long frameSequence;
    public long sessionGeneration;
    public String distanceDiagnosticText;
    public GalleryCropGeometry galleryGeometry;
    public SimulatorIdentityGuard.Decision simulatorIdentity;
    public RecentGallery.Status recentGallery;
    public String deferredGalleryStatus;
    public boolean recentMatchingSupport;
    public int initializationSampleCount;
    public int initializationTrackId = -1;
    public String initializationDiscardReason = "";
    public int distanceCalibrationSampleCount;
    public long distanceCalibrationCompletedAtMs;
    public RangeTelemetrySnapshot rangeTelemetry;
    public boolean rangeFresh;
    public String rangeGateReason = "";

    public FrameResult(
        FollowState state,
        Control control,
        Recognition target,
        Recognition candidate,
        List<Recognition> persons,
        boolean matched,
        boolean tooClose,
        Bitmap snapshot,
        int countdownSec) {
      this.state = state;
      this.control = control;
      this.target = target;
      this.candidate = candidate;
      this.persons = persons;
      this.matched = matched;
      this.tooClose = tooClose;
      this.snapshot = snapshot;
      this.countdownSec = countdownSec;
      this.matchScore = 0f;
    }
  }

  public int CAPTURE_FRAMES = 15;
  public int REACQUIRE_MATCH_N = 8;
  public int FOLLOW_LOST_M = 10;
  public long LOST_TO_SEARCH_MS = 800;
  public long SEARCH_TIMEOUT_MS = 18000;
  public long IDENTITY_UNCERTAIN_TIMEOUT_MS = 18000;
  public long COUNTDOWN_MS = 3000;
  public int CAUTION_STABLE_FRAMES = 3;
  public int UNCERTAIN_FRAMES = 3;
  public int UNCERTAIN_RECOVER_FRAMES = 2;
  public int REACQUIRE_STRICT_FRAMES = 1;
  public int REACQUIRE_DEFAULT_FRAMES = 2;
  public int LOST_RECOVER_FRAMES = 3;
  public long CAPTURE_WINDOW_MS = 2000L;
  public long CAPTURE_GAP_RESET_MS = 500L;

  private final TargetMatcher matcher;
  private final ControlGenerator controlGenerator;
  private final TargetMemory memory = new TargetMemory();

  private FollowState state = FollowState.IDLE;
  private int captureCount = 0;
  private final ArrayDeque<Long> captureSampleTimesMs = new ArrayDeque<>();
  private int captureTrackId = -1;
  private long lastCaptureSampleMs = -1L;
  private String initializationDiscardReason = "waiting_high_confidence";
  private int confirmedTrackId = -1;
  private int matchCount = 0;
  private int lostCount = 0;
  private int midDefaultStreak = 0;
  private int strongDefaultStreak = 0;
  private int strongStrictStreak = 0;
  private int unstableStreak = 0;
  private long stateEnterTime = 0L;
  private Bitmap snapshot = null;
  private boolean simulatorFastRecoveryEnabled;
  private boolean hasFollowedInSession;
  private int recoveryFreshReidCount;
  private long lastRecoveryObservationId = -1L;
  private int recoveryTrackId = -1;

  public FollowStateMachine(TargetMatcher matcher, ControlGenerator controlGenerator) {
    this.matcher = matcher;
    this.controlGenerator = controlGenerator;
  }

  public FollowState getState() {
    return state;
  }

  public TargetMemory getMemory() {
    return memory;
  }

  public void setMaximumDistanceMultiplier(float value) {
    controlGenerator.setMaximumDistanceMultiplier(value);
  }

  public float getMaximumDistanceMultiplier() {
    return controlGenerator.getMaximumDistanceMultiplier();
  }

  public void setSimulatorFastRecoveryEnabled(boolean enabled) {
    simulatorFastRecoveryEnabled = enabled;
  }

  public boolean hasFollowedInSession() {
    return hasFollowedInSession;
  }

  /** Only a completed simulator identity authorization may cross a missing spatial gate. */
  public void acceptSimulatorRecovery(SimulatorIdentityGuard.Decision permit, Recognition target) {
    if (!simulatorFastRecoveryEnabled
        || !hasFollowedInSession
        || permit == null
        || !permit.authorized
        || target == null
        || state == FollowState.IDLE
        || state == FollowState.STOP) return;
    memory.updateDynamic(target);
    state = FollowState.FOLLOW_CAUTION;
    stateEnterTime = System.currentTimeMillis();
    lostCount = 0;
    resetEvidenceCounters();
    resetRecoveryFreshEvidence();
  }

  /** Direction/geometry continuity may restore follow without requiring the original front view. */
  public void acceptDirectedContinuityRecovery(Recognition target) {
    if (!simulatorFastRecoveryEnabled
        || !hasFollowedInSession
        || target == null
        || state == FollowState.IDLE
        || state == FollowState.STOP) return;
    memory.updateDynamic(target);
    state = FollowState.FOLLOW_CAUTION;
    stateEnterTime = System.currentTimeMillis();
    lostCount = 0;
    resetEvidenceCounters();
    resetRecoveryFreshEvidence();
  }

  public void enterDirectedReacquire() {
    if (!simulatorFastRecoveryEnabled || !hasFollowedInSession) return;
    if (state == FollowState.STOP || state == FollowState.IDLE) return;
    state = FollowState.DIRECTED_REACQUIRE;
    stateEnterTime = System.currentTimeMillis();
    resetRecoveryFreshEvidence();
    resetEvidenceCounters();
  }

  public void startCapture() {
    if (state == FollowState.IDLE || state == FollowState.STOP) {
      memory.clear();
      snapshot = null;
      captureCount = 0;
      resetCaptureWindow("capture_started");
      confirmedTrackId = -1;
      hasFollowedInSession = false;
      resetRecoveryFreshEvidence();
      resetEvidenceCounters();
      state = FollowState.CAPTURE_TARGET;
    }
  }

  public void confirm() {
    confirm(-1);
  }

  public void confirm(int lockedTrackId) {
    if (state == FollowState.LOCKED_PENDING_CONFIRM) {
      confirmedTrackId = lockedTrackId;
      memory.resetDistanceCalibration();
      state = FollowState.DISTANCE_CALIBRATION;
      matchCount = 0;
    }
  }

  public void retake() {
    if (state == FollowState.LOCKED_PENDING_CONFIRM) {
      memory.clear();
      snapshot = null;
      captureCount = 0;
      resetCaptureWindow("retake");
      confirmedTrackId = -1;
      state = FollowState.CAPTURE_TARGET;
    }
  }

  public void cancel() {
    memory.clear();
    snapshot = null;
    captureCount = 0;
    resetCaptureWindow("cancelled");
    confirmedTrackId = -1;
    matchCount = 0;
    lostCount = 0;
    resetEvidenceCounters();
    hasFollowedInSession = false;
    resetRecoveryFreshEvidence();
    state = FollowState.IDLE;
  }

  public FrameResult onFrame(
      List<Recognition> persons, Bitmap frame, int frameW, int frameH, int sensorOrientation) {
    return onFrame(persons, frame, frameW, frameH, sensorOrientation, null);
  }

  public FrameResult onFrame(
      List<Recognition> persons,
      Bitmap frame,
      int frameW,
      int frameH,
      int sensorOrientation,
      IdentityEvidence externalIdentity) {
    return onFrame(persons, frame, frameW, frameH, sensorOrientation, externalIdentity, null, null);
  }

  FrameResult observationOnly(List<Recognition> persons, IdentityEvidence identity) {
    FrameResult result =
        new FrameResult(
            state,
            new Control(0f, 0f),
            null,
            identity == null ? null : identity.bestCandidate,
            persons,
            false,
            false,
            snapshot,
            state == FollowState.READY_TO_FOLLOW
                ? Math.max(
                    0,
                    (int)
                        Math.ceil(
                            (COUNTDOWN_MS - (System.currentTimeMillis() - stateEnterTime))
                                / 1000.0))
                : -1);
    if (identity != null) fillIdentity(result, identity);
    return result;
  }

  FrameResult continuityFrame(
      List<Recognition> persons,
      IdentityEvidence identity,
      SimulatorIdentityGuard.Decision permit,
      int frameW,
      int frameH,
      int orientation) {
    if (!simulatorFastRecoveryEnabled
        || permit == null
        || !permit.retainTarget
        || identity == null
        || identity.bestCandidate == null
        || !hasFollowedInSession
        || state == FollowState.STOP
        || state == FollowState.IDLE) return observationOnly(persons, identity);
    if (permit.tracking != null
        && permit.tracking.motionAllowed
        && permit.tracking.stableFrames >= 3) {
      state = FollowState.FOLLOW;
      lostCount = 0;
    }
    ControlGenerator.Result generated =
        controlGenerator.generateFromTarget(
            identity.bestCandidate, persons, frameW, frameH, orientation, memory);
    // Update geometry only. The user-confirmed appearance and distance setpoint remain untouched.
    memory.updateDynamic(identity.bestCandidate);
    FrameResult result =
        new FrameResult(
            state,
            new Control(0f, 0f),
            identity.bestCandidate,
            identity.bestCandidate,
            persons,
            false,
            generated.tooClose,
            snapshot,
            -1);
    result.distanceEstimate = generated.distanceEstimate;
    result.simulatorIdentity = permit;
    fillIdentity(result, identity);
    return result;
  }

  public FrameResult onFrame(
      List<Recognition> persons,
      Bitmap frame,
      int frameW,
      int frameH,
      int sensorOrientation,
      IdentityEvidence externalIdentity,
      TargetMatcher.MatchResult frameMatch) {
    return onFrame(
        persons, frame, frameW, frameH, sensorOrientation, externalIdentity, frameMatch, null);
  }

  public FrameResult onFrame(
      List<Recognition> persons,
      Bitmap frame,
      int frameW,
      int frameH,
      int sensorOrientation,
      IdentityEvidence externalIdentity,
      TargetMatcher.MatchResult frameMatch,
      InitializationObservation initialization) {
    List<Recognition> safePersons = persons == null ? new ArrayList<>() : persons;
    long now = nowMs();

    switch (state) {
      case IDLE:
        return new FrameResult(
            FollowState.IDLE, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);

      case CAPTURE_TARGET:
        {
          Recognition cand =
              initialization != null ? initialization.candidate : selectLargest(safePersons);
          int trackId = initialization == null ? -1 : initialization.trackId;
          if (cand == null || cand.getLocation() == null || cand.getConfidence() == null) {
            if (lastCaptureSampleMs >= 0 && now - lastCaptureSampleMs > CAPTURE_GAP_RESET_MS)
              resetCaptureWindow("target_missing_over_500ms");
            else initializationDiscardReason = "waiting_high_confidence";
            FrameResult waiting =
                new FrameResult(
                    FollowState.CAPTURE_TARGET,
                    new Control(0f, 0f),
                    null,
                    null,
                    safePersons,
                    false,
                    false,
                    null,
                    -1);
            waiting.distanceDiagnosticText =
                "目标采集中 " + captureCount + "/" + CAPTURE_FRAMES + "，等待高置信人物";
            return waiting;
          }
          recordCaptureSample(trackId, now);
          if (captureCount >= CAPTURE_FRAMES) {
            memory.captureIdentityFromBitmap(frame, cand.getLocation());
            snapshot = cropSnapshot(frame, cand.getLocation(), sensorOrientation);
            state = FollowState.LOCKED_PENDING_CONFIRM;
            return new FrameResult(
                FollowState.LOCKED_PENDING_CONFIRM,
                new Control(0f, 0f),
                null,
                null,
                safePersons,
                false,
                false,
                snapshot,
                -1);
          }
          FrameResult collecting =
              new FrameResult(
                  FollowState.CAPTURE_TARGET,
                  new Control(0f, 0f),
                  null,
                  cand,
                  safePersons,
                  false,
                  false,
                  null,
                  -1);
          collecting.distanceDiagnosticText = "目标采集中 " + captureCount + "/" + CAPTURE_FRAMES;
          return collecting;
        }

      case LOCKED_PENDING_CONFIRM:
        return new FrameResult(
            FollowState.LOCKED_PENDING_CONFIRM,
            new Control(0f, 0f),
            null,
            null,
            safePersons,
            false,
            false,
            snapshot,
            -1);

      case DISTANCE_CALIBRATION:
        {
          Recognition cand =
              initialization != null ? initialization.candidate : selectLargest(safePersons);
          int trackId = initialization == null ? -1 : initialization.trackId;
          boolean matchingTrack =
              confirmedTrackId < 0 || (trackId >= 0 && trackId == confirmedTrackId);
          boolean unique =
              initialization == null
                  ? safePersons.size() == 1
                  : initialization.uniqueHighConfidence;
          String discard = null;
          if (cand == null || cand.getLocation() == null) discard = "waiting_locked_target";
          else if (!matchingTrack) discard = "different_track";
          else if (!unique) discard = "multiple_high_confidence_targets";
          boolean ready = false;
          if (discard == null) {
            ready =
                memory.offerDistanceCalibrationSample(
                    cand.getLocation(), frameW, frameH, sensorOrientation, now);
            if (!ready && memory.getDistanceCalibrationStatus().contains("裁切"))
              discard = "person_clipped";
          }
          initializationDiscardReason = discard == null ? "distance_sample_accepted" : discard;
          int samples = memory.getDistanceCalibrationSampleCount();
          if (ready && memory.completeDistanceCalibration(now)) {
            state = FollowState.CONFIRMED_ARMED;
            initializationDiscardReason = "distance_calibration_completed";
          }
          FrameResult result =
              new FrameResult(
                  state, new Control(0f, 0f), cand, cand, safePersons, false, false, snapshot, -1);
          result.distanceDiagnosticText =
              state == FollowState.CONFIRMED_ARMED
                  ? "视觉参考标定完成"
                  : discard == null
                      ? "视觉参考标定 " + samples + "/" + TargetMemory.DISTANCE_CALIBRATION_SAMPLES
                      : calibrationPrompt(discard, samples);
          return result;
        }

      case CONFIRMED_ARMED:
        if (!safePersons.isEmpty()) {
          state = FollowState.REACQUIRE_TARGET;
          matchCount = 0;
        }
        return new FrameResult(
            state, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);

      case REACQUIRE_TARGET:
        {
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          updateEvidenceCounters(id, m, safePersons);
          if (reacquireReady(id, m, safePersons)) matchCount++;
          else matchCount = 0;
          Recognition selected = selectedCandidate(id, m);
          boolean recovery = simulatorFastRecoveryEnabled && hasFollowedInSession;
          if (recovery) updateRecoveryFreshEvidence(id, selected, safePersons);
          boolean ordinaryReady =
              strongStrictStreak >= REACQUIRE_STRICT_FRAMES
                  || strongDefaultStreak >= REACQUIRE_DEFAULT_FRAMES
                  || matchCount >= REACQUIRE_MATCH_N;
          if (selected != null && ordinaryReady && (!recovery || recoveryFreshReidCount >= 3)) {
            state = recovery ? FollowState.FOLLOW_CAUTION : FollowState.READY_TO_FOLLOW;
            stateEnterTime = now;
            memory.updateDynamic(selected);
            resetEvidenceCounters();
            resetRecoveryFreshEvidence();
          }
          FrameResult fr =
              new FrameResult(
                  state,
                  new Control(0f, 0f),
                  selected,
                  null,
                  safePersons,
                  id.matched,
                  false,
                  null,
                  -1);
          fillIdentity(fr, id);
          return fr;
        }

      case READY_TO_FOLLOW:
        {
          int cd = (int) Math.ceil((COUNTDOWN_MS - (now - stateEnterTime)) / 1000.0);
          if (now - stateEnterTime >= COUNTDOWN_MS) {
            state = FollowState.FOLLOW;
            hasFollowedInSession = true;
            lostCount = 0;
            return new FrameResult(
                FollowState.FOLLOW,
                new Control(0f, 0f),
                null,
                null,
                safePersons,
                false,
                false,
                null,
                -1);
          }
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          Recognition selected = selectedCandidate(id, m);
          FrameResult fr =
              new FrameResult(
                  FollowState.READY_TO_FOLLOW,
                  new Control(0f, 0f),
                  selected,
                  null,
                  safePersons,
                  id.matched,
                  false,
                  null,
                  Math.max(0, cd));
          fillIdentity(fr, id);
          return fr;
        }

      case FOLLOW:
        {
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          updateEvidenceCounters(id, m, safePersons);
          Recognition selected = selectedCandidate(id, m);
          if (selected != null && followConfident(id, m, safePersons)) {
            hasFollowedInSession = true;
            memory.updateDynamic(selected);
            lostCount = 0;
            ControlGenerator.Result res =
                controlGenerator.generateFromTarget(
                    selected, safePersons, frameW, frameH, sensorOrientation, memory);
            FrameResult fr =
                new FrameResult(
                    FollowState.FOLLOW,
                    res.control,
                    selected,
                    null,
                    safePersons,
                    true,
                    res.tooClose,
                    null,
                    -1);
            fillIdentity(fr, id);
            fr.distanceEstimate = res.distanceEstimate;
            return fr;
          }
          if (selected != null && followCaution(id, m, safePersons)) {
            state = FollowState.FOLLOW_CAUTION;
            memory.updateDynamic(selected);
            ControlGenerator.Result res =
                controlGenerator.generateFromTarget(
                    selected, safePersons, frameW, frameH, sensorOrientation, memory);
            FrameResult fr =
                new FrameResult(
                    FollowState.FOLLOW_CAUTION,
                    res.control,
                    selected,
                    null,
                    safePersons,
                    true,
                    res.tooClose,
                    null,
                    -1);
            fillIdentity(fr, id);
            fr.distanceEstimate = res.distanceEstimate;
            return fr;
          }
          lostCount++;
          if (lostCount >= FOLLOW_LOST_M) {
            state = FollowState.IDENTITY_UNCERTAIN;
            stateEnterTime = now;
            resetEvidenceCounters();
          }
          FrameResult fr =
              new FrameResult(
                  state, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
          fillIdentity(fr, id);
          return fr;
        }

      case FOLLOW_CAUTION:
        {
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          updateEvidenceCounters(id, m, safePersons);
          Recognition selected = selectedCandidate(id, m);
          if (selected != null && midDefaultStreak >= CAUTION_STABLE_FRAMES) {
            state = FollowState.FOLLOW;
            hasFollowedInSession = true;
            memory.updateDynamic(selected);
            ControlGenerator.Result res =
                controlGenerator.generateFromTarget(
                    selected, safePersons, frameW, frameH, sensorOrientation, memory);
            FrameResult fr =
                new FrameResult(
                    FollowState.FOLLOW,
                    res.control,
                    selected,
                    null,
                    safePersons,
                    true,
                    res.tooClose,
                    null,
                    -1);
            fillIdentity(fr, id);
            fr.distanceEstimate = res.distanceEstimate;
            return fr;
          }
          if (selected != null && followCaution(id, m, safePersons)) {
            memory.updateDynamic(selected);
            ControlGenerator.Result res =
                controlGenerator.generateFromTarget(
                    selected, safePersons, frameW, frameH, sensorOrientation, memory);
            FrameResult fr =
                new FrameResult(
                    FollowState.FOLLOW_CAUTION,
                    res.control,
                    selected,
                    null,
                    safePersons,
                    true,
                    res.tooClose,
                    null,
                    -1);
            fillIdentity(fr, id);
            fr.distanceEstimate = res.distanceEstimate;
            return fr;
          }
          unstableStreak++;
          if (unstableStreak >= UNCERTAIN_FRAMES) {
            state = FollowState.IDENTITY_UNCERTAIN;
            stateEnterTime = now;
            resetEvidenceCounters();
          }
          FrameResult fr =
              new FrameResult(
                  state, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
          fillIdentity(fr, id);
          return fr;
        }

      case IDENTITY_UNCERTAIN:
        {
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          updateEvidenceCounters(id, m, safePersons);
          Recognition selected = selectedCandidate(id, m);
          if (selected != null
              && (identityAdmissionReady(id)
                  || strongStrictStreak >= UNCERTAIN_RECOVER_FRAMES
                  || midDefaultStreak >= UNCERTAIN_RECOVER_FRAMES)) {
            state = FollowState.REACQUIRE_TARGET;
            matchCount = 0;
            stateEnterTime = now;
            resetRecoveryFreshEvidence();
          } else if (now - stateEnterTime >= IDENTITY_UNCERTAIN_TIMEOUT_MS) {
            state = FollowState.STOP;
          }
          FrameResult fr =
              new FrameResult(
                  state, new Control(0f, 0f), selected, null, safePersons, false, false, null, -1);
          fillIdentity(fr, id);
          return fr;
        }

      case LOST:
        {
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          updateEvidenceCounters(id, m, safePersons);
          Recognition selected = selectedCandidate(id, m);
          if (selected != null && lostRecoverReady(id, m, safePersons)) {
            state = FollowState.REACQUIRE_TARGET;
            matchCount = 0;
            stateEnterTime = now;
            resetRecoveryFreshEvidence();
          }
          if (now - stateEnterTime >= LOST_TO_SEARCH_MS) {
            state = FollowState.SEARCH;
            stateEnterTime = now;
          }
          FrameResult fr =
              new FrameResult(
                  state, new Control(0f, 0f), selected, null, safePersons, false, false, null, -1);
          fillIdentity(fr, id);
          return fr;
        }

      case SEARCH:
        {
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          updateEvidenceCounters(id, m, safePersons);
          Recognition selected = selectedCandidate(id, m);
          if (selected != null && lostRecoverReady(id, m, safePersons)) {
            state = FollowState.REACQUIRE_TARGET;
            matchCount = 0;
            stateEnterTime = now;
            resetRecoveryFreshEvidence();
          }
          if (now - stateEnterTime >= SEARCH_TIMEOUT_MS) {
            state = FollowState.STOP;
          }
          FrameResult fr =
              new FrameResult(
                  state, new Control(0f, 0f), selected, null, safePersons, false, false, null, -1);
          fillIdentity(fr, id);
          return fr;
        }

      case DIRECTED_REACQUIRE:
        {
          TargetMatcher.MatchResult m =
              frameMatch != null
                  ? frameMatch
                  : matcher.match(safePersons, frame, memory, frameW, frameH);
          IdentityEvidence id = identityFrom(m, externalIdentity);
          updateEvidenceCounters(id, m, safePersons);
          Recognition selected = selectedCandidate(id, m);
          if (selected != null && lostRecoverReady(id, m, safePersons)) {
            state = FollowState.REACQUIRE_TARGET;
            matchCount = 0;
            stateEnterTime = now;
            resetRecoveryFreshEvidence();
          }
          FrameResult fr =
              new FrameResult(
                  state, new Control(0f, 0f), selected, null, safePersons, false, false, null, -1);
          fillIdentity(fr, id);
          return fr;
        }

      case STOP:
      default:
        return new FrameResult(
            FollowState.STOP, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
    }
  }

  private void recordCaptureSample(int trackId, long now) {
    if (captureTrackId != -1 && trackId != -1 && captureTrackId != trackId)
      resetCaptureWindow("capture_track_changed");
    if (lastCaptureSampleMs >= 0 && now - lastCaptureSampleMs > CAPTURE_GAP_RESET_MS)
      resetCaptureWindow("capture_gap_over_500ms");
    captureTrackId = trackId;
    lastCaptureSampleMs = now;
    captureSampleTimesMs.addLast(now);
    while (!captureSampleTimesMs.isEmpty()
        && now - captureSampleTimesMs.peekFirst() > CAPTURE_WINDOW_MS)
      captureSampleTimesMs.removeFirst();
    captureCount = captureSampleTimesMs.size();
    initializationDiscardReason = "capture_sample_accepted";
  }

  long nowMs() {
    return System.currentTimeMillis();
  }

  private void resetCaptureWindow(String reason) {
    captureSampleTimesMs.clear();
    captureCount = 0;
    captureTrackId = -1;
    lastCaptureSampleMs = -1L;
    initializationDiscardReason = reason;
  }

  private static String calibrationPrompt(String reason, int samples) {
    if ("person_clipped".equals(reason))
      return "视觉参考标定 " + samples + "/" + TargetMemory.DISTANCE_CALIBRATION_SAMPLES + "，请后退并保持完整人物入镜";
    if ("multiple_high_confidence_targets".equals(reason)) return "视觉参考标定暂停，请保持画面内只有已确认目标";
    return "视觉参考标定 " + samples + "/" + TargetMemory.DISTANCE_CALIBRATION_SAMPLES + "，等待已确认目标";
  }

  public int getInitializationSampleCount() {
    return captureCount;
  }

  public int getInitializationTrackId() {
    return captureTrackId;
  }

  public String getInitializationDiscardReason() {
    return initializationDiscardReason;
  }

  private void fillIdentity(FrameResult fr, IdentityEvidence id) {
    if (fr == null || id == null) return;
    fr.identityEvidence = id;
    fr.matchScore = id.confidence;
  }

  private IdentityEvidence identityFrom(TargetMatcher.MatchResult m, IdentityEvidence external) {
    if (external != null) return external;
    return new IdentityEvidence(
        m.matched ? m.score : 0f,
        m.score,
        m.matched,
        m.matched ? "legacy_matched" : "legacy_not_matched",
        ReIDMatchResult.unavailable("reid_not_connected", 0),
        BboxContinuityEvidence.from(
            m.best == null ? null : m.best.getLocation(),
            memory.getLastBbox(),
            memory.getPreviousBbox(),
            1,
            1),
        0,
        0,
        m.best);
  }

  private Recognition selectedCandidate(IdentityEvidence id, TargetMatcher.MatchResult m) {
    if (id != null && id.bestCandidate != null) return id.bestCandidate;
    return m == null ? null : m.best;
  }

  private boolean followConfident(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return id.beliefConfirmed() && id.beliefStableFrames >= 2 && motionGateOk(id);
    }
    if (id != null && id.reidAvailable()) {
      return id.weakOk() && id.bboxDefaultOk();
    }
    return m != null && m.matched && (persons == null || persons.size() <= 1 || id.bboxDefaultOk());
  }

  private boolean followCaution(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return id.beliefCaution() && motionGateOk(id);
    }
    if (id != null && id.reidAvailable()) {
      return id.bboxDefaultOk() && (id.weakOk() || id.midOk());
    }
    return m != null
        && m.matched
        && id != null
        && id.bboxDefaultOk()
        && persons != null
        && persons.size() <= 1;
  }

  private boolean reacquireReady(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return id.beliefConfirmed() && id.beliefStableFrames >= 3 && motionGateOk(id);
    }
    if (id != null && id.reidAvailable()) {
      return (id.strongOk() && id.bboxDefaultOk()) || (id.midOk() && id.bboxStrictOk());
    }
    return m != null
        && m.matched
        && id != null
        && id.bboxStrictOk()
        && persons != null
        && persons.size() <= 1;
  }

  private boolean lostRecoverReady(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return identityAdmissionReady(id);
    }
    if (id != null && id.reidAvailable()) {
      return strongStrictStreak >= LOST_RECOVER_FRAMES
          || strongDefaultStreak >= LOST_RECOVER_FRAMES
          || midDefaultStreak >= LOST_RECOVER_FRAMES;
    }
    return m != null
        && m.matched
        && id != null
        && id.bboxStrictOk()
        && persons != null
        && persons.size() <= 1
        && strongStrictStreak >= LOST_RECOVER_FRAMES;
  }

  private void updateEvidenceCounters(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      boolean safeBbox = motionGateOk(id);
      boolean midDefault = id.targetBelief >= IdentityBelief.BELIEF_CAUTION && safeBbox;
      boolean strongDefault = id.targetBelief >= IdentityBelief.BELIEF_CONFIRM && safeBbox;
      boolean strongStrict =
          id.targetBelief >= IdentityBelief.BELIEF_CONFIRM
              && (id.bboxStrictOk() || id.predictionOk() || id.trackId == id.lockedTrackId);
      midDefaultStreak = midDefault ? midDefaultStreak + 1 : 0;
      strongDefaultStreak = strongDefault ? strongDefaultStreak + 1 : 0;
      strongStrictStreak = strongStrict ? strongStrictStreak + 1 : 0;
      if (midDefault || strongDefault || strongStrict) unstableStreak = 0;
      return;
    }
    boolean reidAvailable = id != null && id.reidAvailable();
    boolean midDefault;
    boolean strongDefault;
    boolean strongStrict;
    if (reidAvailable) {
      midDefault = id.midOk() && id.bboxDefaultOk();
      strongDefault = id.strongOk() && id.bboxDefaultOk();
      strongStrict = id.strongOk() && id.bboxStrictOk();
    } else {
      boolean legacySafe =
          m != null && m.matched && id != null && persons != null && persons.size() <= 1;
      midDefault = legacySafe && id.bboxDefaultOk();
      strongDefault = legacySafe && id.bboxDefaultOk();
      strongStrict = legacySafe && id.bboxStrictOk();
    }
    midDefaultStreak = midDefault ? midDefaultStreak + 1 : 0;
    strongDefaultStreak = strongDefault ? strongDefaultStreak + 1 : 0;
    strongStrictStreak = strongStrict ? strongStrictStreak + 1 : 0;
    if (midDefault || strongDefault || strongStrict) unstableStreak = 0;
  }

  private boolean identityAdmissionReady(IdentityEvidence id) {
    if (id == null || !id.hasBelief()) return false;
    boolean admission =
        id.looseAdmissionOk()
            || id.bboxDefaultOk()
            || id.predictionOk()
            || id.trackId == id.lockedTrackId;
    return id.beliefCaution() && id.beliefStableFrames >= UNCERTAIN_RECOVER_FRAMES && admission;
  }

  private boolean motionGateOk(IdentityEvidence id) {
    if (id == null) return false;
    return id.bboxDefaultOk() || id.predictionOk() || id.trackId == id.lockedTrackId;
  }

  private void resetEvidenceCounters() {
    midDefaultStreak = 0;
    strongDefaultStreak = 0;
    strongStrictStreak = 0;
    unstableStreak = 0;
  }

  private void updateRecoveryFreshEvidence(
      IdentityEvidence id, Recognition selected, List<Recognition> persons) {
    if (id == null
        || selected == null
        || !persons.contains(selected)
        || id.trackId < 0
        || id.lockedTrackId < 0
        || id.trackId != id.lockedTrackId
        || id.missedFrames != 0
        || id.reidMatch == null
        || !id.reidMatch.reidAvailable
        || id.reidMatch.observationId <= 0L
        || id.reidMatch.bestScore < 0.85f
        || id.reidMatch.margin < 0.08f
        || !motionGateOk(id)) {
      recoveryFreshReidCount = 0;
      recoveryTrackId = -1;
      return;
    }
    if (recoveryTrackId != id.trackId) {
      recoveryFreshReidCount = 0;
      recoveryTrackId = id.trackId;
    }
    long observationId = id.reidMatch.observationId;
    if (observationId <= lastRecoveryObservationId) return;
    lastRecoveryObservationId = observationId;
    recoveryFreshReidCount++;
  }

  private void resetRecoveryFreshEvidence() {
    recoveryFreshReidCount = 0;
    lastRecoveryObservationId = -1L;
    recoveryTrackId = -1;
  }

  private static Recognition selectLargest(List<Recognition> persons) {
    Recognition target = null;
    float maxArea = -1f;
    for (Recognition r : persons) {
      if (r == null || r.getLocation() == null) continue;
      RectF loc = r.getLocation();
      float area = loc.width() * loc.height();
      if (area > maxArea) {
        maxArea = area;
        target = r;
      }
    }
    return target;
  }

  private static Bitmap cropSnapshot(Bitmap frame, RectF bbox, int sensorOrientation) {
    if (frame == null || bbox == null) return null;
    int fw = frame.getWidth();
    int fh = frame.getHeight();
    int l = clamp((int) bbox.left, 0, fw - 1);
    int t = clamp((int) bbox.top, 0, fh - 1);
    int r = clamp((int) bbox.right, 1, fw);
    int b = clamp((int) bbox.bottom, 1, fh);
    int w = r - l;
    int h = b - t;
    if (w <= 0 || h <= 0) return null;
    try {
      Bitmap raw = Bitmap.createBitmap(frame, l, t, w, h);
      // Full-frame createBitmap may alias the inference frame, which its owner recycles.
      if (raw == frame) raw = frame.copy(Bitmap.Config.ARGB_8888, false);
      if (raw == null) return null;
      int rotation = ((sensorOrientation % 360) + 360) % 360;
      if (rotation == 0) return raw;
      android.graphics.Matrix matrix = new android.graphics.Matrix();
      matrix.postRotate(rotation);
      Bitmap upright =
          Bitmap.createBitmap(raw, 0, 0, raw.getWidth(), raw.getHeight(), matrix, true);
      if (upright != raw) raw.recycle();
      return upright;
    } catch (Exception e) {
      return null;
    }
  }

  private static int clamp(int v, int lo, int hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }
}
