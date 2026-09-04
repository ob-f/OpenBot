package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector.Recognition;
import org.openbot.vehicle.Control;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SimulatorFrameProcessingTest {
  private static final class TestClockMachine extends FollowStateMachine {
    long now;

    TestClockMachine() {
      super(new TargetMatcher(), new ControlGenerator());
    }

    @Override
    long nowMs() {
      return now;
    }
  }

  @Test
  public void recentControlLocksWithStartAndRetainedIdentityHasSeparateMotionText() {
    android.content.Context context =
        new android.view.ContextThemeWrapper(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            org.openbot.R.style.AppTheme);
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    fragment.binding =
        org.openbot.databinding.FragmentHumanCartSimulatorBinding.inflate(
            android.view.LayoutInflater.from(context));
    assertTrue(fragment.binding.recentGallerySwitch.isChecked());
    fragment.onFollowEnabledChanged(true);
    assertFalse(fragment.binding.recentGallerySwitch.isEnabled());
    fragment.onFollowEnabledChanged(false);
    assertTrue(fragment.binding.recentGallerySwitch.isEnabled());
    assertEquals(android.view.View.GONE, fragment.binding.simulatorExperimentPanel.getVisibility());
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    for (int i = 1; i <= 3; i++) SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .99f);
    FollowStateMachine.FrameResult frame =
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
    frame.simulatorIdentity = SimulatorIdentityGuardTest.continuous(guard, 4, 1200, .82f);
    assertTrue(fragment.commandForFrame(frame, "").contains("连续跟踪"));
    SimulatorIdentityGuardTest.continuous(guard, 5, 1500, .69f);
    SimulatorIdentityGuardTest.continuous(guard, 6, 1800, .69f);
    SimulatorIdentityGuardTest.continuous(guard, 7, 2100, .69f);
    frame.simulatorIdentity = SimulatorIdentityGuardTest.continuous(guard, 8, 2400, .69f);
    assertTrue(fragment.commandForFrame(frame, "").contains("连续跟踪"));
  }

  @Test
  public void explicitContinuityPermitDoesNotFabricateReidOrBypassSafety() {
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    for (int i = 1; i <= 3; i++) SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .99f);
    SimulatorIdentityGuard.Decision permit =
        SimulatorIdentityGuardTest.continuous(guard, 4, 1200, .82f);
    IdentityEvidence identity =
        new IdentityEvidence(
            .75f,
            .75f,
            false,
            "weak_pose",
            null,
            null,
            0,
            0,
            null,
            1,
            1,
            -1,
            1,
            10,
            0,
            .8f,
            0f,
            0f,
            0f,
            0f,
            0,
            0,
            "test");
    ActionArbitrator arbitrator = new ActionArbitrator();
    for (boolean blocked : new boolean[] {false, true}) {
      BehaviorDecisionResult result =
          arbitrator.decide(
              FollowState.FOLLOW,
              identity,
              new DistanceEvidence(DistanceState.TOO_FAR, 1f, "test"),
              new TraversabilityEvidence(1, 1, 1, blocked, "test"),
              new SystemSafetyEvidence(false, true, true, "test"),
              null,
              400,
              permit);
      assertEquals(
          blocked ? BehaviorAction.BLOCKED_WAIT : BehaviorAction.FOLLOW_SLOW,
          result.selectedAction);
    }
    for (DistanceState distance :
        new DistanceState[] {DistanceState.UNKNOWN, DistanceState.TOO_CLOSE})
      assertEquals(
          BehaviorAction.MOTION_STOP,
          arbitrator.decide(
                  FollowState.FOLLOW,
                  identity,
                  new DistanceEvidence(distance, 1f, "test"),
                  null,
                  new SystemSafetyEvidence(false, true, true, "test"),
                  null,
                  400,
                  permit)
              .selectedAction);
    assertEquals(
        BehaviorAction.EMERGENCY_STOP,
        arbitrator.decide(
                FollowState.FOLLOW,
                identity,
                new DistanceEvidence(DistanceState.TOO_FAR, 1f, "test"),
                null,
                new SystemSafetyEvidence(true, true, true, "test"),
                null,
                400,
                permit)
            .selectedAction);
    assertFalse(identity.matched);
    assertEquals(.75f, identity.score, 0f);
    assertEquals(
        BehaviorAction.MOTION_STOP,
        arbitrator.decide(
                FollowState.FOLLOW,
                identity,
                new DistanceEvidence(DistanceState.TOO_FAR, 1f, "test"),
                null,
                new SystemSafetyEvidence(false, true, true, "test"),
                null,
                400)
            .selectedAction);
  }

  @Test
  public void samplingIsBoundedAndRetainsHistogramAndInitializationSemantics() {
    assertEquals(4096, TargetMemory.colorSampleCount(1280, 720));
    assertEquals(6, TargetMemory.colorSampleCount(2, 3));
    assertEquals(0, TargetMemory.colorSampleCount(0, 100));
    Bitmap frame = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888);
    frame.eraseColor(Color.RED);
    RectF box = new RectF(0, 0, 720, 1280);
    assertArrayEquals(
        TargetMemory.computeHsvHist(frame, box),
        TargetMemory.computeSampledHsvHist(frame, box),
        .00001f);
    TargetMemory memory = new TargetMemory();
    memory.setBoundedColorSampling(true);
    memory.captureFromBitmap(frame, box);
    assertEquals(1f, memory.colorScore(frame, box), .00001f);
    frame.eraseColor(Color.BLUE);
    assertEquals(0f, memory.colorScore(frame, box), .00001f);
    frame.recycle();
  }

  @Test
  public void distanceCalibrationRequiresTemporalWindowAndUsesMedianHeight() {
    TargetMemory memory = new TargetMemory();
    Bitmap frame = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    RectF ordinary = new RectF(25, 30, 75, 130);
    for (int i = 0; i < 16; i++) {
      RectF sample = i == 7 ? new RectF(25, 30, 75, 170) : ordinary;
      assertFalse(memory.offerDistanceCalibrationSample(sample, 100, 200, 0, i * 33L));
    }
    assertTrue(memory.offerDistanceCalibrationSample(ordinary, 100, 200, 0, 528L));
    memory.captureFromBitmap(frame, ordinary, 100, 200, 0);
    assertEquals(.50f, memory.getDistanceSetpoint().desiredHeightRatio, .0001f);
    frame.recycle();
  }

  @Test
  public void clippedDistanceCalibrationRequestsNewStandingPosition() {
    TargetMemory memory = new TargetMemory();
    assertFalse(memory.offerDistanceCalibrationSample(new RectF(20, 20, 80, 150), 100, 200, 0, 0));
    assertFalse(memory.offerDistanceCalibrationSample(new RectF(20, 0, 80, 150), 100, 200, 0, 0));
    assertTrue(memory.getDistanceCalibrationStatus().contains("裁切"));
    assertEquals(1, memory.getDistanceCalibrationSampleCount());
  }

  @Test
  public void captureUsesTrackBoundSlidingWindowAndToleratesShortDetectionGap() {
    TestClockMachine machine = new TestClockMachine();
    machine.CAPTURE_FRAMES = 3;
    Bitmap frame = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    Recognition person = new Recognition("1", "person", .90f, new RectF(20, 0, 80, 200), 0);
    FollowStateMachine.InitializationObservation trackOne =
        new FollowStateMachine.InitializationObservation(person, 1, true);
    machine.startCapture();
    machine.now = 0;
    machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, null, null, trackOne);
    machine.now = 400;
    machine.onFrame(
        Collections.emptyList(),
        frame,
        100,
        200,
        0,
        null,
        null,
        new FollowStateMachine.InitializationObservation(null, -1, false));
    assertEquals(1, machine.getInitializationSampleCount());
    machine.now = 450;
    machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, null, null, trackOne);
    machine.now = 500;
    FollowStateMachine.FrameResult captured =
        machine.onFrame(
            Collections.singletonList(person), frame, 100, 200, 0, null, null, trackOne);
    assertEquals(FollowState.LOCKED_PENDING_CONFIRM, captured.state);
    assertFalse(machine.getMemory().hasDistanceSetpoint());
    assertNotNull(captured.snapshot);
    captured.snapshot.recycle();
    frame.recycle();
  }

  @Test
  public void captureTrackChangeAndLongGapRestartProgress() {
    TestClockMachine machine = new TestClockMachine();
    machine.CAPTURE_FRAMES = 3;
    Bitmap frame = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    Recognition one = new Recognition("1", "person", .9f, new RectF(20, 20, 80, 170), 0);
    Recognition two = new Recognition("2", "person", .9f, new RectF(20, 20, 80, 170), 0);
    machine.startCapture();
    machine.now = 0;
    machine.onFrame(
        Collections.singletonList(one),
        frame,
        100,
        200,
        0,
        null,
        null,
        new FollowStateMachine.InitializationObservation(one, 1, true));
    machine.now = 100;
    machine.onFrame(
        Collections.singletonList(two),
        frame,
        100,
        200,
        0,
        null,
        null,
        new FollowStateMachine.InitializationObservation(two, 2, true));
    assertEquals(1, machine.getInitializationSampleCount());
    machine.now = 700;
    machine.onFrame(
        Collections.emptyList(),
        frame,
        100,
        200,
        0,
        null,
        null,
        new FollowStateMachine.InitializationObservation(null, -1, false));
    assertEquals(0, machine.getInitializationSampleCount());
    frame.recycle();
  }

  @Test
  public void confirmedTargetStaysStoppedUntilTrackBoundDistanceCalibrationCompletes() {
    TestClockMachine machine = new TestClockMachine();
    machine.CAPTURE_FRAMES = 1;
    Bitmap frame = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    Recognition person = new Recognition("1", "person", .9f, new RectF(20, 20, 80, 170), 0);
    FollowStateMachine.InitializationObservation locked =
        new FollowStateMachine.InitializationObservation(person, 7, true);
    machine.startCapture();
    machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, null, null, locked);
    machine.confirm(7);
    assertEquals(FollowState.DISTANCE_CALIBRATION, machine.getState());

    machine.now = 0;
    FollowStateMachine.FrameResult result =
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, null, null, locked);
    assertEquals(0f, result.control.getLeft(), 0f);
    assertEquals(1, machine.getMemory().getDistanceCalibrationSampleCount());
    Recognition clipped = new Recognition("1", "person", .9f, new RectF(20, 0, 80, 200), 0);
    machine.now = 100;
    result =
        machine.onFrame(
            Collections.singletonList(clipped),
            frame,
            100,
            200,
            0,
            null,
            null,
            new FollowStateMachine.InitializationObservation(clipped, 7, true));
    assertEquals(FollowState.DISTANCE_CALIBRATION, result.state);
    assertEquals(1, machine.getMemory().getDistanceCalibrationSampleCount());

    for (int i = 0; i < 14; i++) {
      machine.now = 150 + i * 50L;
      result =
          machine.onFrame(
              Collections.singletonList(person), frame, 100, 200, 0, null, null, locked);
    }
    assertEquals(FollowState.CONFIRMED_ARMED, result.state);
    assertTrue(machine.getMemory().hasDistanceSetpoint());
    assertTrue(machine.getMemory().getDistanceCalibrationCompletedAtMs() > 0);
    assertEquals(0f, result.control.getLeft(), 0f);
    frame.recycle();
  }

  @Test
  public void bothDriveControllersStopDuringDistanceCalibration() {
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.DISTANCE_CALIBRATION,
            new Control(1f, 1f),
            null,
            null,
            Collections.emptyList(),
            false,
            false,
            null,
            -1);
    frame.behaviorDecision =
        new ActionArbitrator()
            .decide(
                FollowState.DISTANCE_CALIBRATION,
                null,
                null,
                null,
                new SystemSafetyEvidence(false, true, true, "ok"),
                null,
                100);
    frame.frameTiming = new FrameTimingEvidence(1000, 0, 1, 0, 1, 1, 30, 0);
    SimulatorAutoDriveController.Result simulator =
        new SimulatorAutoDriveController().update(frame, 1001);
    RealCartAutoDriveController.Result real = new RealCartAutoDriveController().update(frame, 1001);
    assertEquals(0, simulator.left);
    assertEquals(0, simulator.right);
    assertTrue(real.isStop());
    assertEquals(BehaviorAction.MOTION_STOP, frame.behaviorDecision.selectedAction);
  }

  @Test
  public void configuredHighConfidenceCandidateBelowPointSevenFiveCanBeCaptured() {
    FollowStateMachine machine =
        new FollowStateMachine(new TargetMatcher(), new ControlGenerator());
    machine.CAPTURE_FRAMES = 1;
    Bitmap frame = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    Recognition person = new Recognition("1", "person", .60f, new RectF(20, 20, 80, 170), 0);
    machine.startCapture();
    FollowStateMachine.FrameResult result =
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0);
    assertEquals(FollowState.LOCKED_PENDING_CONFIRM, result.state);
    assertNotNull(result.snapshot);
    result.snapshot.recycle();
    frame.recycle();
  }

  @Test
  public void suppliedPerFrameMatchIsNotRecomputedByStateMachine() {
    int[] calls = {0};
    TargetMatcher matcher =
        new TargetMatcher() {
          @Override
          public MatchResult match(List<Recognition> p, Bitmap b, TargetMemory m, int w, int h) {
            calls[0]++;
            return super.match(p, b, m, w, h);
          }
        };
    FollowStateMachine machine = new FollowStateMachine(matcher, new ControlGenerator());
    machine.CAPTURE_FRAMES = 1;
    Bitmap frame = Bitmap.createBitmap(64, 128, Bitmap.Config.ARGB_8888);
    Recognition person = new Recognition("1", "person", .95f, new RectF(0, 0, 64, 128), 0);
    List<Recognition> persons = Collections.singletonList(person);
    machine.startCapture();
    machine.onFrame(persons, frame, 64, 128, 0);
    machine.confirm();
    for (int i = 0; i < 15; i++)
      machine
          .getMemory()
          .offerDistanceCalibrationSample(new RectF(8, 8, 56, 120), 64, 128, 0, i * 50L);
    machine.onFrame(
        Collections.singletonList(
            new Recognition("1", "person", .95f, new RectF(8, 8, 56, 120), 0)),
        frame,
        64,
        128,
        0);
    for (int i = 0; i < 5; i++) {
      TargetMatcher.MatchResult result =
          matcher.match(persons, frame, machine.getMemory(), 64, 128);
      machine.onFrame(persons, frame, 64, 128, 0, null, result);
    }
    assertEquals(5, calls[0]);
    frame.recycle();
  }

  @Test
  public void expiredCountdownPreservesStageAndNeverOutputsMotion() {
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.READY_TO_FOLLOW,
            new Control(1, 1),
            null,
            null,
            Collections.emptyList(),
            false,
            false,
            null,
            2);
    frame.frameTiming = new FrameTimingEvidence(100, 0, 30, 30, 800, 800, 1, 0);
    SimulatorAutoDriveController.Result result =
        new SimulatorAutoDriveController().update(frame, 900);
    assertEquals(SimulatorAutoDriveController.Phase.COUNTDOWN, result.phase);
    assertFalse(result.lockout);
    assertEquals(0, result.left);
    assertTrue(HumanCartSimulatorFragment.staleCommand(frame.state).contains("倒计时"));
    assertFalse(HumanCartSimulatorFragment.isFrameFresh(frame.frameTiming));
    frame.frameTiming = new FrameTimingEvidence(1000, 0, 30, 30, 80, 80, 10, 0);
    assertTrue(HumanCartSimulatorFragment.isFrameFresh(frame.frameTiming));
  }

  @Test
  public void identityHoldDoesNotAdvanceMemoryAndAlwaysStops() {
    FollowStateMachine machine =
        new FollowStateMachine(new TargetMatcher(), new ControlGenerator());
    machine.startCapture();
    FollowStateMachine.FrameResult frame = machine.observationOnly(Collections.emptyList(), null);
    assertEquals(FollowState.CAPTURE_TARGET, frame.state);
    assertTrue(machine.getMemory().isEmpty());
    frame.simulatorIdentity =
        new SimulatorIdentityGuard.Decision(false, true, 2, 0, "known_distractor");
    SimulatorAutoDriveController.Result result =
        new SimulatorAutoDriveController().update(frame, 100);
    assertEquals(0, result.left);
    assertEquals(0, result.right);
  }

  @Test
  public void frameTimingRetainsStagesAndSourceTimestampThroughPresentation() {
    FrameTimingEvidence timing =
        new FrameTimingEvidence(100, 99, 30, 35, 80, 80, 12, 3)
            .withStages(2, 5, 0, 8, 3, 183)
            .presentedAt(200);
    assertEquals(100, timing.receivedAtMs);
    assertEquals(100, timing.sourceAgeMs);
    assertEquals(17, timing.uiWaitMs);
    assertEquals(5, timing.matchMs);
  }

  @Test
  public void overlappingDuplicateBoxesAreNotMarkedAsSeparatePeople() {
    RectF a = new RectF(0, 0, 100, 100);
    assertFalse(BaseCartFollowFragment.separatePersonBoxes(a, new RectF(5, 5, 100, 100)));
    assertTrue(BaseCartFollowFragment.separatePersonBoxes(a, new RectF(110, 0, 200, 100)));
  }

  @Test
  public void unauthorizedCandidateIsYellowEvenWhenTrackBeliefWasGreen() throws Exception {
    BaseCartFollowFragment fragment = new HumanCartSimulatorFragment();
    Recognition person = new Recognition("2", "person", .95f, new RectF(0, 0, 64, 128), 0);
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.FOLLOW,
            new Control(1, 1),
            person,
            person,
            Collections.singletonList(person),
            true,
            false,
            null,
            -1);
    frame.simulatorIdentity =
        new SimulatorIdentityGuard.Decision(false, true, 2, 0, "known_distractor");
    java.lang.reflect.Method build =
        BaseCartFollowFragment.class.getDeclaredMethod(
            "buildDrawBoxes",
            FollowStateMachine.FrameResult.class,
            int.class,
            int.class,
            int.class);
    build.setAccessible(true);
    List<?> boxes = (List<?>) build.invoke(fragment, frame, 64, 128, 0);
    java.lang.reflect.Field color = boxes.get(0).getClass().getDeclaredField("colorType");
    color.setAccessible(true);
    assertEquals(1, color.getInt(boxes.get(0)));
  }

  @Test
  public void actualSimulatorUiKeepsStartAndConfirmationVisibleAcrossStaleThenFreshFrames() {
    android.content.Context context =
        new android.view.ContextThemeWrapper(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            org.openbot.R.style.AppTheme);
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    fragment.binding =
        org.openbot.databinding.FragmentHumanCartSimulatorBinding.inflate(
            android.view.LayoutInflater.from(context));
    fragment.binding.startSwitch.setChecked(true);
    fragment.binding.confirmPanel.setVisibility(android.view.View.VISIBLE);
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.LOCKED_PENDING_CONFIRM,
            new Control(0, 0),
            null,
            null,
            Collections.emptyList(),
            false,
            false,
            null,
            -1);
    frame.frameTiming = new FrameTimingEvidence(100, 0, 30, 30, 800, 800, 1, 0);
    fragment.onFrameUiApplied(frame);
    assertTrue(fragment.binding.startSwitch.isChecked());
    assertEquals(android.view.View.VISIBLE, fragment.binding.confirmPanel.getVisibility());
    assertTrue(fragment.binding.commandText.getText().toString().contains("请确认目标"));
    frame.frameTiming = new FrameTimingEvidence(1000, 0, 30, 30, 80, 80, 10, 0);
    fragment.onFrameUiApplied(frame);
    assertTrue(fragment.binding.simulatorFrameHealth.getText().toString().contains("画面有效"));
  }
}
