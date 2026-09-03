package org.openbot.cartfollow;

/** Hardware admission around the shared exit detector. No BLE calls. */
public final class RealCartSearchController {
  public static final long BRAKE_MS = 300L;

  public static final class Result {
    public final DirectedReacquireEvidence evidence;
    public final boolean pivotAllowed;
    public final boolean braking;
    public final boolean lockout;
    public final String reason;
    public final long generation;
    public final long frameSequence;

    private Result(
        DirectedReacquireEvidence evidence,
        boolean allowed,
        boolean braking,
        boolean lockout,
        String reason,
        long generation,
        long sequence) {
      this.evidence = evidence;
      this.pivotAllowed = allowed;
      this.braking = braking;
      this.lockout = lockout;
      this.reason = reason;
      this.generation = generation;
      this.frameSequence = sequence;
    }

    public int left() {
      return pivotAllowed ? evidence.left() : 0;
    }

    public int right() {
      return pivotAllowed ? evidence.right() : 0;
    }

    public boolean overridesFollow() {
      return lockout
          || (evidence.phase != DirectedReacquireEvidence.Phase.IDLE
              && evidence.phase != DirectedReacquireEvidence.Phase.COMPLETE);
    }
  }

  private final DirectedReacquireController detector = new DirectedReacquireController();
  private boolean enabled;
  private boolean startedPivot;
  private boolean pendingYawStart;
  private boolean enterRequested;
  private long generation = -1;
  private long sequence = -1;
  private long brakeIssuedAt = -1;
  private long sourceAt = -1;
  private DirectedReacquireEvidence evidence = DirectedReacquireEvidence.idle("search_disabled");
  private String fault;
  private boolean parkedWithoutSensor;

  public RealCartSearchController() {
    detector.requireSubmittedRotation();
  }

  public synchronized void configure(boolean enabled, int speed, float angle, long timeout) {
    reset();
    this.enabled = enabled;
    detector.configure(speed, angle, timeout);
  }

  public synchronized void reset() {
    detector.reset();
    startedPivot = pendingYawStart = parkedWithoutSensor = enterRequested = false;
    generation = sequence = sourceAt = brakeIssuedAt = -1;
    fault = null;
    evidence = DirectedReacquireEvidence.idle("idle");
  }

  public synchronized Result update(
      FollowStateMachine.FrameResult frame, long now, YawTurnTracker yaw) {
    if (frame == null) return result(false, false, "frame_missing");
    if (generation != frame.sessionGeneration) {
      reset();
      generation = frame.sessionGeneration;
    }
    if (frame.frameSequence <= sequence) return poll(now, yaw);
    sequence = frame.frameSequence;
    sourceAt = frame.frameTiming == null ? -1 : frame.frameTiming.receivedAtMs;
    if (parkedWithoutSensor
        && frame.simulatorIdentity != null
        && frame.simulatorIdentity.authorized
        && frame.simulatorIdentity.state == SimulatorIdentityGuard.State.VERIFIED
        && (frame.state == FollowState.FOLLOW || frame.state == FollowState.FOLLOW_CAUTION)) {
      parkedWithoutSensor = false;
      detector.reset();
      evidence = DirectedReacquireEvidence.idle("new_verified_follow");
    }
    if (parkedWithoutSensor || fault != null) return poll(now, yaw);
    evidence = detector.update(frame, now, yaw);
    if (detector.consumeEnterRequest()) {
      brakeIssuedAt = -1;
      pendingYawStart = true;
      startedPivot = false;
      enterRequested = true;
      if (!enabled || !yaw.getStatus(sensorNow(now)).available) {
        enterRequested = false;
        parkedWithoutSensor = true;
        evidence = parked(enabled ? "gyro_not_ready" : "search_disabled");
        detector.reset();
      }
    }
    return poll(now, yaw);
  }

  public synchronized Result poll(long now, YawTurnTracker yaw) {
    if (fault != null) return result(false, false, fault);
    if (parkedWithoutSensor) return result(false, false, evidence.reason);
    boolean episode = detector.isActive();
    if (episode && startedPivot) {
      if (!yaw.getStatus(sensorNow(now)).available) fault = "search_sensor_lost";
      else if (yaw.getTurnedDegrees() <= -5f) fault = "search_wrong_direction";
      if (fault != null) return result(false, false, fault);
    }
    DirectedReacquireEvidence deadline = detector.pollDeadline(now, yaw);
    if (deadline != null) evidence = deadline;
    if (evidence.lockout) {
      fault = evidence.reason;
      return result(false, false, fault);
    }
    if (!enabled || evidence.phase != DirectedReacquireEvidence.Phase.TURNING)
      return result(false, false, evidence.reason);
    if (brakeIssuedAt < 0 || now - brakeIssuedAt < BRAKE_MS)
      return result(false, true, "search_brake_wait");
    if (sourceAt < 0 || now < sourceAt || now - sourceAt > 400L)
      return result(false, false, "search_frame_stale");
    if (!yaw.getStatus(sensorNow(now)).available) {
      parkedWithoutSensor = true;
      evidence = parked("gyro_not_ready");
      detector.reset();
      return result(false, false, evidence.reason);
    }
    return result(true, false, "directed_search");
  }

  /** Timestamp command submission, not a GATT acknowledgement or physical completion. */
  public synchronized void noteCommand(int left, int right, long now, YawTurnTracker yaw) {
    if (!detector.isActive() || fault != null) return;
    if (left == 0 && right == 0 && brakeIssuedAt < 0) brakeIssuedAt = now;
    if (pendingYawStart
        && left != 0
        && left == -right
        && evidence.phase == DirectedReacquireEvidence.Phase.TURNING
        && left == evidence.left()
        && right == evidence.right()) {
      detector.onRotationSubmitted(yaw);
      pendingYawStart = false;
      startedPivot = true;
    }
  }

  public synchronized boolean learningRisk(long now) {
    return detector.isActive() || detector.hasRecentExitEvidence(now);
  }

  public synchronized boolean consumeEnterRequest() {
    boolean entered = enterRequested;
    enterRequested = false;
    return entered;
  }
  // Millisecond clocks are truncated; use the end of that millisecond for SensorEvent timestamps.
  private static long sensorNow(long nowMs) {
    return nowMs * 1_000_000L + 999_999L;
  }

  private DirectedReacquireEvidence parked(String reason) {
    return new DirectedReacquireEvidence(
        DirectedReacquireEvidence.Phase.PARKED_WAIT,
        evidence.direction,
        evidence.speed,
        evidence.turnedDegrees,
        evidence.targetDegrees,
        evidence.elapsedMs,
        evidence.timeoutMs,
        evidence.gyroAvailable,
        false,
        false,
        reason);
  }

  private Result result(boolean allowed, boolean braking, String reason) {
    return new Result(evidence, allowed, braking, fault != null, reason, generation, sequence);
  }
}
