package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector;
import org.openbot.vehicle.Control;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DirectedReacquireControllerTest {
  private final DirectedReacquireController controller = new DirectedReacquireController();
  private final YawTurnTracker yaw = new YawTurnTracker();

  @Test
  public void independentOfMotionGateRequiresTwoOutwardAndTimedMissingFrames() {
    armRight();
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1100).phase);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1199).phase);
    DirectedReacquireEvidence result = update(missing(), 1200);
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, result.phase);
    assertEquals(18, result.left());
    assertEquals(-18, result.right());
    assertTrue(controller.consumeEnterRequest());
    assertFalse(controller.consumeEnterRequest());
  }

  @Test
  public void deadlineExpiresWithoutAnotherInferenceResultAndRetainsSession() {
    armRight();
    update(missing(), 1100);
    update(missing(), 1200);
    assertNull(controller.pollDeadline(6199, yaw));
    DirectedReacquireEvidence parked = controller.pollDeadline(6200, yaw);
    assertParked(parked, "search_timeout");
    FollowStateMachine.FrameResult stale = missing();
    stale.frameTiming = new FrameTimingEvidence(1200, 0, 30, 30, 5000, 5000, 0, 0);
    assertSame(parked, update(stale, 6200));
    assertSame(parked, controller.pollDeadline(20000, yaw));
  }

  @Test
  public void shrinkingClippedLeftBoxesAreOutward() {
    update(observation(900, 0f, .20f, false, 1, 0), 900);
    update(observation(1000, 0f, .10f, true, 1, 0), 1000);
    update(missing(), 1100);
    DirectedReacquireEvidence result = update(missing(), 1200);
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, result.phase);
    assertEquals(-18, result.left());
    assertEquals(18, result.right());
  }

  @Test
  public void initialConfirmationReacquireAndCountdownCannotArm() {
    FollowState[] states = {
      FollowState.CONFIRMED_ARMED, FollowState.REACQUIRE_TARGET, FollowState.READY_TO_FOLLOW
    };
    for (FollowState state : states) {
      controller.reset();
      update(inState(observation(900, .70f, .90f, false, 1, 0), state), 900);
      update(inState(observation(1000, .80f, 1f, true, 1, 0), state), 1000);
      update(missing(), 1100);
      assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
      assertFalse(controller.consumeEnterRequest());
    }
  }

  @Test
  public void exactlyTwoPositionsWithFallingBeliefSuffice() {
    update(observation(900, .70f, .90f, false, 1, 0), 900);
    FollowStateMachine.FrameResult edge = observation(1000, .80f, 1f, true, 1, 0);
    edge.targetObservation =
        new TargetObservationEvidence(
            new RectF(.80f, 0, 1, 1), 1, 1000, .55f, true, true, 1, "locked");
    update(edge, 1000);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(missing(), 1200).phase);
  }

  @Test
  public void stationaryJitterAndInwardReturnCannotArm() {
    update(observation(900, .80f, 1f, false, 1, 0), 900);
    update(observation(1000, .805f, 1f, true, 1, 0), 1000);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
    controller.reset();
    armRight();
    update(observation(1050, .70f, .90f, false, 1, 0), 1050);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
  }

  @Test
  public void staleOrAmbiguousOrUnreliableHistoryCannotArm() {
    armRight();
    update(missing(), 1800);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1900).phase);
    controller.reset();
    armRight();
    FollowStateMachine.FrameResult ambiguous = observation(1050, .85f, 1f, false, 1, 0);
    ambiguous.targetObservation =
        new TargetObservationEvidence(
            new RectF(.85f, 0, 1, 1), 1, 1050, .9f, false, true, 2, "test");
    update(ambiguous, 1050);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
    controller.reset();
    FollowStateMachine.FrameResult weak = observation(1050, .85f, 1f, false, 1, 0);
    weak.targetObservation =
        new TargetObservationEvidence(
            new RectF(.85f, 0, 1, 1), 1, 1050, .74f, false, true, 1, "test");
    update(weak, 1050);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
  }

  @Test
  public void duplicateFramesCannotSatisfyCandidateFreeTrigger() {
    armRight();
    FollowStateMachine.FrameResult frame = missing();
    frame.frameSequence = 10;
    update(frame, 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(frame, 1200).phase);
    frame.frameSequence = 11;
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(frame, 1201).phase);
  }

  @Test
  public void staleFollowIsNotRecovery() {
    start();
    FollowStateMachine.FrameResult stale =
        new FollowStateMachine.FrameResult(
            FollowState.FOLLOW,
            new Control(0, 0),
            null,
            null,
            Collections.emptyList(),
            false,
            false,
            null,
            -1);
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(stale, 1300).phase);
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(stale, 1700).phase);
  }

  @Test
  public void staleFrameStopsActiveSearchAndLatchesFailure() {
    start();
    FollowStateMachine.FrameResult frame = missing();
    frame.frameTiming = new FrameTimingEvidence(0, 0, 0, 0, 501, 501, 0, 0);
    DirectedReacquireEvidence result = update(frame, 1300);
    assertEquals("frame_stale", result.reason);
    assertEquals(0, result.left());
    assertTrue(result.lockout);
    assertSame(result, update(missing(), 1400));
  }

  @Test
  public void persistentRejectedCandidateStaysStoppedAndTimesOutIncludingVerification() {
    controller.configure(18, 90, 1000);
    start();
    for (long time = 1300; time < 2200; time += 100) {
      DirectedReacquireEvidence result = update(observation(time, .4f, .6f, true, 1, 0), time);
      assertEquals(DirectedReacquireEvidence.Phase.VERIFYING, result.phase);
      assertEquals(0, result.left());
      assertEquals(0, result.right());
    }
    DirectedReacquireEvidence failed = update(observation(2200, .4f, .6f, true, 1, 0), 2200);
    assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, failed.phase);
    assertEquals("search_timeout", failed.reason);
    assertFalse(failed.lockout);
    assertSame(failed, update(missing(), 2300));
  }

  @Test
  public void requiresThreeFreshResultsForSameCurrentHighTrackThenSettlesOnce() {
    start();
    assertEquals(
        DirectedReacquireEvidence.Phase.VERIFYING,
        update(observation(1300, .4f, .6f, false, 1, 10), 1300).phase);
    assertEquals(
        DirectedReacquireEvidence.Phase.VERIFYING,
        update(observation(1400, .4f, .6f, false, 1, 10), 1400).phase);
    assertEquals(
        DirectedReacquireEvidence.Phase.VERIFYING,
        update(observation(1500, .4f, .6f, false, 1, 11), 1500).phase);
    assertEquals(
        DirectedReacquireEvidence.Phase.SETTLING,
        update(observation(1600, .4f, .6f, false, 1, 12), 1600).phase);
    assertEquals(
        DirectedReacquireEvidence.Phase.SETTLING,
        update(observation(1899, .4f, .6f, false, 1, 12), 1899).phase);
    assertEquals(
        DirectedReacquireEvidence.Phase.COMPLETE,
        update(observation(1900, .4f, .6f, false, 1, 12), 1900).phase);
    assertEquals(
        DirectedReacquireEvidence.Phase.IDLE,
        update(observation(2000, .4f, .6f, false, 1, 13), 2000).phase);
  }

  @Test
  public void trackSwitchAndLowConfidenceResetRecoveryEvidence() {
    start();
    update(observation(1300, .4f, .6f, false, 1, 10), 1300);
    update(observation(1400, .4f, .6f, false, 1, 11), 1400);
    assertEquals(
        DirectedReacquireEvidence.Phase.VERIFYING,
        update(observation(1500, .4f, .6f, false, 2, 12), 1500).phase);
    update(observation(1600, .4f, .6f, true, 2, 13), 1600);
    assertEquals(
        DirectedReacquireEvidence.Phase.VERIFYING,
        update(observation(1700, .4f, .6f, false, 2, 14), 1700).phase);
  }

  @Test
  public void deadlineAlsoBoundsSettling() {
    controller.configure(18, 90, 1000);
    start();
    update(observation(1900, .4f, .6f, false, 1, 10), 1900);
    update(observation(2000, .4f, .6f, false, 1, 11), 2000);
    assertEquals(
        DirectedReacquireEvidence.Phase.SETTLING,
        update(observation(2100, .4f, .6f, false, 1, 12), 2100).phase);
    assertEquals(
        DirectedReacquireEvidence.Phase.PARKED_WAIT,
        update(observation(2200, .4f, .6f, false, 1, 13), 2200).phase);
  }

  @Test
  public void angleLimitAndNewSessionReset() {
    yaw.setSensorStatus(true, true);
    yaw.onGravity(0, 0, 9.81f);
    controller.configure(18, 30, 5000);
    start();
    yaw.onGyroscope(1_200_000_000L, 0, 0, -2);
    yaw.onGyroscope(1_500_000_000L, 0, 0, -2);
    assertParked(update(missing(), 1500), "search_angle_limit");
    FollowStateMachine.FrameResult freshSession = missing();
    freshSession.sessionGeneration = 1;
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(freshSession, 1600).phase);
  }

  @Test
  public void wideBoxesTouchingBothEdgeZonesUseMotionForDirectionAndMirror() {
    for (boolean mirror : new boolean[] {false, true}) {
      controller.reset();
      update(mirrored(900, .04f, .90f, mirror), 900);
      update(mirrored(1000, .09f, .95f, mirror), 1000);
      update(missing(), 1100);
      DirectedReacquireEvidence result = update(missing(), 1200);
      assertEquals(DirectedReacquireEvidence.Phase.TURNING, result.phase);
      assertEquals(mirror ? -18 : 18, result.left());
      assertEquals(-result.left(), result.right());
    }
  }

  @Test
  public void cumulativeSmallOutwardStepsReachTwoPercentInBothDirections() {
    for (boolean mirror : new boolean[] {false, true}) {
      controller.reset();
      update(mirrored(800, .70f, .90f, mirror), 800);
      update(mirrored(850, .708f, .908f, mirror), 850);
      update(mirrored(900, .716f, .916f, mirror), 900);
      update(mirrored(1000, .724f, .924f, mirror), 1000);
      update(missing(), 1100);
      assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(missing(), 1200).phase);
    }
  }

  @Test
  public void subThresholdDisplacementAndNonEdgeMotionCannotArm() {
    update(observation(900, .70f, .90f, false, 1, 0), 900);
    assertEquals(
        "exit_displacement_insufficient",
        update(observation(1000, .719f, .919f, false, 1, 0), 1000).reason);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
    controller.reset();
    update(observation(900, .30f, .50f, false, 1, 0), 900);
    assertEquals(
        "exit_not_at_edge", update(observation(1000, .40f, .60f, false, 1, 0), 1000).reason);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
  }

  @Test
  public void oneNeutralJitterPreservesExitButTwoOrMarkedReverseClearIt() {
    for (boolean mirror : new boolean[] {false, true}) {
      controller.reset();
      update(mirrored(800, .65f, .85f, mirror), 800);
      update(mirrored(900, .75f, .95f, mirror), 900);
      assertEquals(
          "exit_outward_jitter_tolerated",
          update(mirrored(1000, .745f, .945f, mirror), 1000).reason);
      update(missing(), 1100);
      assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(missing(), 1200).phase);

      controller.reset();
      update(mirrored(800, .65f, .85f, mirror), 800);
      update(mirrored(900, .75f, .95f, mirror), 900);
      update(mirrored(950, .745f, .945f, mirror), 950);
      assertEquals("exit_repeated_jitter", update(mirrored(1000, .74f, .94f, mirror), 1000).reason);
      update(missing(), 1100);
      assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);

      controller.reset();
      update(mirrored(800, .65f, .85f, mirror), 800);
      update(mirrored(900, .75f, .95f, mirror), 900);
      assertEquals("exit_marked_reverse", update(mirrored(1000, .73f, .93f, mirror), 1000).reason);
      update(missing(), 1100);
      assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
    }
  }

  @Test
  public void expansionAcrossBothEdgesIsAmbiguousNotAnExit() {
    update(observation(900, .10f, .90f, false, 1, 0), 900);
    assertEquals(
        "exit_geometry_ambiguous", update(observation(1000, 0f, .96f, false, 1, 0), 1000).reason);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
  }

  @Test
  public void rollingHistoryDoesNotIncludeExpiredPositions() {
    update(observation(100, .50f, .70f, false, 1, 0), 100);
    update(observation(900, .70f, .90f, false, 1, 0), 900);
    update(observation(1000, .705f, .905f, false, 1, 0), 1000);
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
  }

  @Test
  public void strongIdentityMayBeOneSecondOldButCachedOrContinuityCannotRefreshIt() {
    for (long missingAt : new long[] {1200, 1201}) {
      controller.reset();
      FollowStateMachine.FrameResult strong = observation(200, .40f, .60f, false, 1, 0);
      update(strong, 200);
      FollowStateMachine.FrameResult first = observation(900, .70f, .90f, false, 1, 0);
      first.identityEvidence = strong.identityEvidence;
      update(first, 900);
      FollowStateMachine.FrameResult edge = observation(1000, .80f, 1f, true, 1, 0);
      edge.identityEvidence = strong.identityEvidence;
      update(edge, 1000);
      update(missing(), 1100);
      DirectedReacquireEvidence result = update(missing(), missingAt);
      assertEquals(
          missingAt == 1200
              ? DirectedReacquireEvidence.Phase.TURNING
              : DirectedReacquireEvidence.Phase.IDLE,
          result.phase);
      if (missingAt > 1200) assertEquals("exit_evidence_expired", result.reason);
    }
  }

  @Test
  public void reliableContinuityPreservesExitWithoutRenewingStrongIdentity() {
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(0);
    SimulatorContinuityTracker.Evidence continuity =
        new SimulatorContinuityTracker.Evidence(
            true,
            "continuous_observations",
            3,
            null,
            new android.graphics.RectF(100, 100, 180, 280));
    for (int i = 1; i <= 3; i++) {
      long time = 100L * i;
      ReIDMatchResult match =
          new ReIDMatchResult(.92f, .70f, 0, 8, true, 1, "fresh", .92f, 0, i)
              .withBinding(1, time, i, true, 0, false);
      FollowStateMachine.FrameResult frame = observation(time, .40f, .60f, false, 1, i);
      frame.simulatorIdentity =
          guard.update(0, i, time, time, 1, 1, true, true, match, false, continuity, true);
      update(frame, time);
    }
    for (int i = 4; i <= 7; i++) {
      long time = i == 4 ? 600 : i == 5 ? 900 : i == 6 ? 1000 : 1100;
      ReIDMatchResult match =
          new ReIDMatchResult(.82f, .60f, 0, 8, true, 1, "fresh", .72f, 0, i)
              .withBinding(1, time, i, true, 0, false);
      FollowStateMachine.FrameResult frame =
          observation(
              time,
              i <= 5 ? .40f : i == 6 ? .70f : .80f,
              i <= 5 ? .60f : i == 6 ? .90f : 1f,
              false,
              1,
              i);
      frame.simulatorIdentity =
          guard.update(0, i, time, time, 1, 1, true, true, match, false, continuity, true);
      assertEquals(SimulatorIdentityGuard.State.TRACK_STABLE, frame.simulatorIdentity.state);
      update(frame, time);
    }
    update(missing(), 1200);
    assertEquals("strong_identity_expired_or_unreliable", update(missing(), 1301).reason);
    assertFalse(controller.isActive());
  }

  @Test
  public void otherCandidatesPreventRotationBeforeAndDuringSearch() {
    armRight();
    FollowStateMachine.FrameResult other = observation(1100, .4f, .6f, true, 2, 0);
    other.targetObservation = null;
    assertEquals("target_missing_other_candidates", update(other, 1100).reason);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1200).phase);
    controller.reset();
    start();
    other = observation(1300, .4f, .6f, true, 2, 0);
    other.targetObservation = null;
    DirectedReacquireEvidence result = update(other, 1300);
    assertEquals(DirectedReacquireEvidence.Phase.VERIFYING, result.phase);
    assertEquals("target_missing_other_candidates", result.reason);
    assertEquals(0, result.left());
    assertEquals(0, result.right());
  }

  @Test
  public void longTimeoutParksOnlyAtConfiguredDeadlineEvenAfterExitHistoryExpires() {
    controller.configure(18, 90, 10000);
    start();
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(missing(), 11199).phase);
    assertNull(controller.pollDeadline(11199, yaw));
    DirectedReacquireEvidence result = update(missing(), 11200);
    assertParked(result, "search_timeout");
    assertSame(result, controller.pollDeadline(11200, yaw));
    assertSame(result, update(missing(), 100000));
  }

  @Test
  public void parkedNeedsNewVerifiedFollowAndNewExitBeforeAnotherTurn() {
    controller.configure(18, 90, 1000);
    start();
    DirectedReacquireEvidence parked = update(missing(), 2200);
    assertParked(parked, "search_timeout");
    assertSame(
        parked,
        update(
            inState(observation(2300, .70f, .90f, false, 1, 0), FollowState.IDENTITY_UNCERTAIN),
            2300));
    assertSame(parked, update(observation(2400, .80f, 1f, true, 1, 0), 2400));
    assertEquals(
        DirectedReacquireEvidence.Phase.IDLE,
        update(observation(2500, .70f, .90f, false, 1, 0), 2500).phase);
    update(missing(), 2600);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 2700).phase);
    update(observation(2800, .72f, .92f, false, 1, 0), 2800);
    update(observation(2900, .82f, 1f, true, 1, 0), 2900);
    update(missing(), 3000);
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(missing(), 3100).phase);
    assertTrue(controller.consumeEnterRequest());
  }

  @Test
  public void pollAndFrameAngleLimitReturnSameParkedEvidence() {
    yaw.setSensorStatus(true, true);
    yaw.onGravity(0, 0, 9.81f);
    controller.configure(18, 30, 10000);
    start();
    yaw.onGyroscope(1_200_000_000L, 0, 0, -2);
    yaw.onGyroscope(1_500_000_000L, 0, 0, -2);
    DirectedReacquireEvidence result = controller.pollDeadline(1500, yaw);
    assertParked(result, "search_angle_limit");
    assertSame(result, update(missing(), 1500));
  }

  @Test
  public void parkedReleasesForAuthorizedCachedRecoveryButDoesNotRenewExitTrust() {
    controller.configure(18, 90, 1000);
    start();
    assertParked(controller.pollDeadline(2200, yaw), "search_timeout");
    FollowStateMachine.FrameResult recovery = observation(2300, .70f, .90f, false, 1, 10);
    recovery.identityEvidence = observation(2100, .70f, .90f, false, 1, 10).identityEvidence;
    recovery.simulatorIdentity = new SimulatorIdentityGuard.Decision(true, false, 1, 3, "verified");
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(recovery, 2300).phase);
    assertNull(controller.pollDeadline(2300, yaw));
    update(observation(2400, .82f, 1f, true, 1, 0), 2400);
    update(missing(), 2500);
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 2600).phase);
  }

  @Test
  public void staleFrameAtDeadlineUsesSameParkingTransitionAsPoll() {
    controller.configure(18, 90, 1000);
    start();
    FollowStateMachine.FrameResult stale = missing();
    stale.frameTiming = new FrameTimingEvidence(1200, 0, 0, 0, 1000, 1000, 0, 0);
    DirectedReacquireEvidence result = update(stale, 2200);
    assertParked(result, "search_timeout");
    assertSame(result, controller.pollDeadline(2200, yaw));
  }

  @Test
  public void sideExitLearningGateExpiresAndClearsOnAmbiguity() {
    armRight();
    assertTrue(controller.hasRecentExitEvidence(1000));
    assertFalse(controller.hasRecentExitEvidence(1801));
    controller.reset();
    armRight();
    FollowStateMachine.FrameResult ambiguous = observation(1050, .85f, 1f, false, 1, 0);
    ambiguous.targetObservation =
        new TargetObservationEvidence(
            new RectF(.85f, 0, 1, 1), 1, 1050, .9f, false, true, 2, "test");
    update(ambiguous, 1050);
    assertFalse(controller.hasRecentExitEvidence(1050));
  }

  @Test
  public void freshFlagWithoutCurrentBindingCannotRefreshStrongTimestamp() {
    for (boolean wrongSequence : new boolean[] {false, true}) {
      controller.reset();
      update(observation(200, .40f, .60f, false, 1, 0), 200);
      FollowStateMachine.FrameResult first = observation(900, .70f, .90f, false, 1, 10);
      ReIDMatchResult match = first.identityEvidence.reidMatch;
      first.frameSequence = 100;
      first.identityEvidence =
          withReid(
              first.identityEvidence,
              match.withBinding(1, 900, wrongSequence ? 99 : 100, wrongSequence, 0, false));
      update(first, 900);
      update(observation(1000, .80f, 1f, true, 1, 0), 1000);
      update(missing(), 1100);
      assertEquals(DirectedReacquireEvidence.Phase.IDLE, update(missing(), 1201).phase);
    }
  }

  private static IdentityEvidence withReid(IdentityEvidence original, ReIDMatchResult reid) {
    return new IdentityEvidence(
        .9f,
        .9f,
        true,
        "test",
        reid,
        null,
        3,
        0,
        null,
        original.trackId,
        original.lockedTrackId,
        -1,
        1,
        5,
        0,
        .9f,
        0,
        0,
        0,
        0,
        3,
        0,
        "test");
  }

  private void assertParked(DirectedReacquireEvidence result, String reason) {
    assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, result.phase);
    assertEquals(reason, result.reason);
    assertFalse(result.lockout);
    assertFalse(controller.isActive());
    assertFalse(controller.consumeEnterRequest());
    assertEquals(0, result.left());
    assertEquals(0, result.right());
  }

  private static FollowStateMachine.FrameResult mirrored(
      long time, float left, float right, boolean mirror) {
    return observation(time, mirror ? 1f - right : left, mirror ? 1f - left : right, false, 1, 0);
  }

  private void armRight() {
    update(observation(800, .65f, .85f, false, 1, 0), 800);
    update(inState(observation(900, .75f, .95f, false, 1, 0), FollowState.IDENTITY_UNCERTAIN), 900);
    update(inState(observation(1000, .82f, 1f, true, 1, 0), FollowState.IDENTITY_UNCERTAIN), 1000);
  }

  private void start() {
    armRight();
    update(missing(), 1100);
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, update(missing(), 1200).phase);
  }

  private DirectedReacquireEvidence update(FollowStateMachine.FrameResult frame, long now) {
    return controller.update(frame, now, yaw);
  }

  private static FollowStateMachine.FrameResult observation(
      long time, float left, float right, boolean low, int track, long reidId) {
    Detector.Recognition person =
        new Detector.Recognition("1", "person", low ? .3f : .95f, new RectF(left, 0, right, 1), 0);
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            reidId > 0 ? FollowState.FOLLOW_CAUTION : FollowState.FOLLOW,
            new Control(0, 0),
            null,
            null,
            low ? Collections.emptyList() : Collections.singletonList(person),
            false,
            false,
            null,
            -1);
    if (low)
      frame.detectionTierEvidence =
          new DetectionTierEvidence(
              .5f,
              .25f,
              Collections.singletonList(person),
              Collections.singletonList(person),
              true);
    frame.targetObservation =
        new TargetObservationEvidence(
            new RectF(left, 0, right, 1), track, time, .9f, low, true, 1, "locked");
    ReIDMatchResult reid =
        new ReIDMatchResult(.92f, .75f, 0, 8, true, 1, "test", .92f, 0, reidId)
            .withBinding(track, time, time, true, 0, false);
    frame.identityEvidence =
        new IdentityEvidence(
            .9f, .9f, true, "test", reid, null, 3, 0, person, track, track, -1, 1, 5, 0, .9f, 0, 0,
            0, 0, 3, 0, "test");
    return frame;
  }

  private static FollowStateMachine.FrameResult missing() {
    return new FollowStateMachine.FrameResult(
        FollowState.IDENTITY_UNCERTAIN,
        new Control(0, 0),
        null,
        null,
        Collections.emptyList(),
        false,
        false,
        null,
        -1);
  }

  private static FollowStateMachine.FrameResult inState(
      FollowStateMachine.FrameResult source, FollowState state) {
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            state,
            source.control,
            source.target,
            source.candidate,
            source.persons,
            source.matched,
            source.tooClose,
            source.snapshot,
            source.countdownSec);
    frame.targetObservation = source.targetObservation;
    frame.identityEvidence = source.identityEvidence;
    frame.detectionTierEvidence = source.detectionTierEvidence;
    return frame;
  }
}
