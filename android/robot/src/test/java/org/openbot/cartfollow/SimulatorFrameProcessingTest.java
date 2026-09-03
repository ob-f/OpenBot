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
