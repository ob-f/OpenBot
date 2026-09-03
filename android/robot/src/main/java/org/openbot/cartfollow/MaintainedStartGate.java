package org.openbot.cartfollow;

/** Only independent identity observations can restart a continuously maintained target. */
final class MaintainedStartGate {
  private long lastObservation = -1L;
  private long lastTime = -1L;
  private int count;

  void reset() {
    lastObservation = lastTime = -1L;
    count = 0;
  }

  boolean ready(SimulatorIdentityGuard.Decision permit, long now) {
    if (permit != null && permit.tracking != null) {
      count = permit.tracking.stableFrames;
      return permit.tracking.allowsMotion(now, 500L) && count >= 3;
    }
    if (permit == null || (!permit.isContinuous() && permit.identityEvidenceTimeMs < 0)) {
      reset();
      return true;
    }
    if (!permit.retainTarget
        || permit.identityEvidenceTimeMs < 0
        || now < permit.identityEvidenceTimeMs
        || now - permit.identityEvidenceTimeMs > 500L) {
      reset();
      return false;
    }
    if (lastTime >= 0 && permit.identityEvidenceTimeMs - lastTime > 500L) reset();
    if (permit.identityObservationId > lastObservation
        && permit.identityEvidenceTimeMs > lastTime) {
      lastObservation = permit.identityObservationId;
      lastTime = permit.identityEvidenceTimeMs;
      count = Math.min(3, count + 1);
    }
    return count >= 3;
  }

  int observations() {
    return count;
  }

  void prime(FollowStateMachine.FrameResult frame, long now, long maxAge) {
    if (canObserve(frame, now, maxAge)) ready(frame.simulatorIdentity, now);
    else reset();
  }

  static boolean canObserve(FollowStateMachine.FrameResult frame, long now, long maxAge) {
    if (frame == null
        || frame.frameTiming == null
        || frame.simulatorIdentity == null
        || frame.behaviorDecision == null
        || frame.distanceEstimate == null
        || frame.steeringEvidence == null
        || !frame.steeringEvidence.valid) return false;
    BehaviorAction action = frame.behaviorDecision.selectedAction;
    return (frame.state == FollowState.FOLLOW || frame.state == FollowState.FOLLOW_CAUTION)
        && frame.distanceEstimate.state == DistanceState.TOO_FAR
        && Float.isFinite(frame.distanceEstimate.heightScale)
        && frame.distanceEstimate.heightScale <= .80f
        && now >= frame.frameTiming.receivedAtMs
        && now - frame.frameTiming.receivedAtMs <= maxAge
        && frame.simulatorIdentity.retainTarget
        && (frame.simulatorIdentity.tracking != null
            || frame.simulatorIdentity.identityEvidenceTimeMs >= 0)
        && (action == BehaviorAction.FOLLOW_SLOW
            || action == BehaviorAction.FOLLOW_CAUTION
            || (action == BehaviorAction.MOTION_STOP
                && "identity_unmatched".equals(frame.behaviorDecision.actionReason)));
  }
}
