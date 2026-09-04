package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.R;
import org.openbot.databinding.FragmentHumanCartSimulatorBinding;
import org.openbot.vehicle.Control;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class HumanCartSimulatorUiTest {
  @Test
  public void galleryGeometryShowsThresholdsAndConcreteReason() {
    String text =
        HumanCartSimulatorFragment.galleryGeometryText(GalleryCropGeometry.evaluate(null, 0, 0, 0));
    assertTrue(text.contains("32×64px"));
    assertTrue(text.contains("18%"));
    assertTrue(text.contains("12%"));
    assertTrue(text.contains("宽高比>=0.15"));
    assertTrue(text.contains("仅诊断"));
    assertTrue(text.contains("目标框无效"));
    assertFalse(text.contains("质量不足"));
  }

  @Test
  public void presentationRequiresFreshTimingEvidence() {
    assertFalse(HumanCartSimulatorFragment.isFrameFresh(null));
    assertTrue(
        HumanCartSimulatorFragment.isFrameFresh(
            new FrameTimingEvidence(0, 0, 0, 0, 0, 500, 10f, 0)));
    assertFalse(
        HumanCartSimulatorFragment.isFrameFresh(
            new FrameTimingEvidence(0, 0, 0, 0, 0, 501, 10f, 0)));
  }

  @Test
  public void sensorStatusDistinguishesPresenceRegistrationAndFirstEvent() {
    assertTrue(HumanCartSimulatorFragment.sensorState(false, false, -1, 0).contains("不存在"));
    assertTrue(HumanCartSimulatorFragment.sensorState(true, false, -1, 0).contains("未注册"));
    assertTrue(HumanCartSimulatorFragment.sensorState(true, true, -1, 0).contains("等待事件"));
  }

  @Test
  public void sensorFreshnessExpiresAfter500Milliseconds() {
    assertTrue(
        HumanCartSimulatorFragment.sensorState(true, true, 1_000_000L, 501_000_000L)
            .contains("新鲜 500ms"));
    assertTrue(
        HumanCartSimulatorFragment.sensorState(true, true, 1_000_000L, 502_000_000L)
            .contains("过期 501ms"));
    assertTrue(
        HumanCartSimulatorFragment.sensorState(true, true, 1_000_000L, 501_000_001L)
            .contains("过期"));
    assertTrue(
        HumanCartSimulatorFragment.sensorState(true, true, 2_000_000L, 1_000_000L)
            .contains("时间戳异常"));
  }

  @Test
  public void wrongDirectionTextReportsActualSearchOutput() {
    for (SteeringEvidence.Direction direction :
        new SteeringEvidence.Direction[] {
          SteeringEvidence.Direction.LEFT, SteeringEvidence.Direction.RIGHT
        }) {
      DirectedReacquireEvidence search =
          new DirectedReacquireEvidence(
              DirectedReacquireEvidence.Phase.TURNING,
              direction,
              18,
              12f,
              90f,
              100L,
              5000L,
              true,
              true,
              false,
              "turning");
      String text = HumanCartSimulatorFragment.directedTurningText(search);
      assertTrue(text.contains("旋转方向错误"));
      assertTrue(text.contains("c" + search.left() + "," + search.right()));
      if (search.left() != 0 || search.right() != 0) assertFalse(text.contains("c0,0"));
    }
  }

  @Test
  public void autoVerificationUsesRecoveryTypeAndRequiredFreshMatches() {
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    SimulatorIdentityGuard.Decision local = verification(false);
    assertTrue(HumanCartSimulatorFragment.identityVerificationText(local).contains("局部身份复核 1/3"));
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.simulatorIdentity = verification(true);
    frame.simulatorDriveResult =
        drive(
            SimulatorAutoDriveController.Phase.RECOVERY_STOP,
            0,
            0,
            "global_fresh_reid_verification");
    String text = fragment.commandForFrame(frame, "");
    assertTrue(text.contains("全局身份复核 1/5"));
    assertTrue(text.contains("0.0/1.2s"));
    assertTrue(text.contains("c0,0"));
    assertFalse(text.contains("重新确认"));
    assertFalse(text.contains("/3"));
  }

  @Test
  public void initialPendingConfirmationRemainsYellowEvenWhenFrameIsMatched() throws Exception {
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    org.openbot.tflite.Detector.Recognition person =
        new org.openbot.tflite.Detector.Recognition(
            "1", "person", .95f, new android.graphics.RectF(0, 0, 64, 128), 0);
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.LOCKED_PENDING_CONFIRM,
            new Control(0, 0),
            person,
            person,
            Collections.singletonList(person),
            true,
            false,
            null,
            -1);
    java.lang.reflect.Method build =
        BaseCartFollowFragment.class.getDeclaredMethod(
            "buildDrawBoxes",
            FollowStateMachine.FrameResult.class,
            int.class,
            int.class,
            int.class);
    build.setAccessible(true);
    java.util.List<?> boxes = (java.util.List<?>) build.invoke(fragment, frame, 64, 128, 0);
    assertEquals(1, boxes.size());
    java.lang.reflect.Field color = boxes.get(0).getClass().getDeclaredField("colorType");
    color.setAccessible(true);
    assertEquals(1, color.getInt(boxes.get(0)));
    assertTrue(BaseCartFollowFragment.shouldShowConfirmation(true, frame.state));
  }

  @Test
  public void forcedReconfirmStaysHiddenWithoutRemovingInitialConfirmationOrRetake() {
    HumanCartSimulatorFragment fragment = boundFragment();
    fragment.binding.confirmPanel.setVisibility(View.VISIBLE);
    boolean[] retaken = {false};
    fragment.binding.btnRetake.setOnClickListener(v -> retaken[0] = true);
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.simulatorIdentity = new SimulatorIdentityGuard.Decision(false, true, 1, 0, "test");
    fragment.binding.simulatorReconfirm.setVisibility(View.VISIBLE);
    fragment.onFrameUiApplied(frame);
    assertEquals(View.GONE, fragment.binding.simulatorReconfirm.getVisibility());
    assertEquals(View.VISIBLE, fragment.binding.confirmPanel.getVisibility());
    fragment.binding.btnRetake.performClick();
    assertTrue(retaken[0]);
    assertTrue(
        BaseCartFollowFragment.shouldShowConfirmation(true, FollowState.LOCKED_PENDING_CONFIRM));
    assertTrue(fragment.commandForFrame(frame, "").contains("自动身份复核"));
    assertFalse(fragment.commandForFrame(frame, "").contains("重新确认"));
    assertFalse(fragment.commandForFrame(frame, "").contains("0/0"));
  }

  @Test
  public void actualDriveOutputWinsOverSearchSpeedAndStationaryIdentityText() {
    HumanCartSimulatorFragment fragment = boundFragment();
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.simulatorIdentity = verification(true);
    frame.directedReacquireEvidence = search(DirectedReacquireEvidence.Phase.TURNING);
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.RECOVERY_STOP, -12, 12, "turning");
    String command = fragment.commandForFrame(frame, "");
    assertTrue(command.contains("c-12,12"));
    assertFalse(command.contains("c-18,18"));
    assertFalse(command.contains("模拟停车"));
    fragment.onFrameUiApplied(frame);
    String status = fragment.binding.simulatorStatus.getText().toString();
    assertTrue(status.contains("模拟输出来源=定向搜索 c-12,12"));
    assertFalse(status.contains("c-18,18"));
  }

  @Test
  public void stoppedDriveAndStaleFramesNeverBorrowTurningCommands() {
    HumanCartSimulatorFragment fragment = boundFragment();
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.directedReacquireEvidence = search(DirectedReacquireEvidence.Phase.TURNING);
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.RECOVERY_STOP, 0, 0, "continuity_deadline");
    assertZeroCommand(fragment.commandForFrame(frame, ""));
    fragment.onFrameUiApplied(frame);
    assertTrue(fragment.binding.simulatorStatus.getText().toString().contains("模拟输出来源=停车 c0,0"));
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.RECOVERY_STOP, -18, 18, "turning");
    frame.frameTiming = new FrameTimingEvidence(1000, 0, 0, 0, 0, 501, 0, 0);
    assertZeroCommand(fragment.commandForFrame(frame, ""));
    fragment.onFrameUiApplied(frame);
    assertFalse(fragment.binding.simulatorStatus.getText().toString().contains("c-18,18"));
    frame.frameTiming = null;
    assertZeroCommand(fragment.commandForFrame(frame, ""));
  }

  @Test
  public void completeReportsActualStopOrMotionInsteadOfPromisingLowGearFollow() {
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.directedReacquireEvidence = search(DirectedReacquireEvidence.Phase.COMPLETE);
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.FOLLOW, 0, 0, "distance_ok");
    String stopped = fragment.commandForFrame(frame, "");
    assertTrue(stopped.contains("恢复完成"));
    assertTrue(stopped.contains("已达到跟随距离"));
    assertZeroCommand(stopped);
    assertFalse(stopped.contains("低档跟随"));
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.FOLLOW, 14, 14, "follow_straight");
    String moving = fragment.commandForFrame(frame, "");
    assertTrue(moving.contains("恢复完成"));
    assertTrue(moving.contains("c14,14"));
    assertFalse(moving.contains("c0,0"));
    frame.simulatorDriveResult = null;
    assertZeroCommand(fragment.commandForFrame(frame, ""));
  }

  @Test
  public void missingTargetDoesNotClaimOcclusion() {
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.RECOVERY_STOP, 0, 0, "target_not_visible");
    String text = fragment.commandForFrame(frame, "");
    assertTrue(text.contains("目标暂不可见"));
    assertFalse(text.contains("遮挡"));
    assertZeroCommand(text);
  }

  @Test
  public void gallerySeparatesAllFourTiersAndIncludesDeferredAdmissionAndRecentSupport() {
    HumanCartSimulatorFragment fragment = boundFragment();
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.galleryUpdateStatus =
        new GalleryUpdateStatus(
            GalleryUpdateStatus.Mode.ADAPTIVE, 8, 2, 1, 2, 3, 1, .9f, .8f, .1f, "pending", "test");
    frame.recentGallery = new RecentGallery.Status(true, 4, .9f, "recent_support");
    frame.deferredGalleryStatus = "segment_approved_2";
    frame.recentMatchingSupport = true;
    fragment.onFrameUiApplied(frame);
    String text = fragment.binding.simulatorStatus.getText().toString();
    assertTrue(text.contains("Anchor（初始身份）=8"));
    assertTrue(text.contains("Adaptive（已批准外观）=2"));
    assertTrue(text.contains("Recent（短期记忆）=4/16 开启"));
    assertTrue(text.contains("Quarantine（待验证隔离）=1"));
    assertTrue(text.contains("延迟入库=segment_approved_2"));
    assertTrue(text.contains("Recent 匹配支持=有"));
    assertTrue(text.contains("不等于身份授权"));
    frame.recentMatchingSupport = false;
    assertTrue(HumanCartSimulatorFragment.deferredGalleryText(frame).contains("匹配支持=无"));
  }

  @Test
  public void parkedDeadlineImmediatelyZerosUiWithoutResetAndRejectsQueuedOldOutput() {
    HumanCartSimulatorFragment fragment = boundFragment();
    fragment.binding.startSwitch.setChecked(true);
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.directedReacquireEvidence = search(DirectedReacquireEvidence.Phase.TURNING);
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.RECOVERY_STOP, -18, 18, "turning");
    fragment.onFrameUiApplied(frame);
    fragment.applyDirectedDeadline(search(DirectedReacquireEvidence.Phase.PARKED_WAIT), 1500);
    assertTrue(fragment.binding.startSwitch.isChecked());
    assertTrue(fragment.binding.commandText.getText().toString().contains("搜索已结束"));
    assertTrue(
        fragment.binding.simulatorStatus.getText().toString().contains("PARKED_WAIT 停车 c0,0"));
    assertTrue(fragment.binding.commandText.getText().toString().contains("目标身份已保留"));
    fragment.applyDirectedDeadline(search(DirectedReacquireEvidence.Phase.PARKED_WAIT), 60000);
    assertTrue(fragment.binding.startSwitch.isChecked());
    assertTrue(fragment.binding.simulatorStatus.getText().toString().contains("5.0/5.0s"));
    assertFalse(fragment.binding.commandText.getText().toString().contains("本轮模拟已结束"));
    assertZeroCommand(fragment.commandForFrame(frame, ""));
    fragment.onFrameUiApplied(frame);
    assertFalse(fragment.binding.simulatorStatus.getText().toString().contains("c-18,18"));
    frame.frameTiming = new FrameTimingEvidence(1000, 0, 0, 0, 0, 501, 0, 0);
    fragment.onFrameUiApplied(frame);
    assertTrue(fragment.binding.commandText.getText().toString().contains("搜索已结束"));
    assertTrue(fragment.binding.commandText.getText().toString().contains("等待新鲜画面"));
    FollowStateMachine.FrameResult recovered = frame(2000);
    recovered.directedReacquireEvidence = search(DirectedReacquireEvidence.Phase.COMPLETE);
    recovered.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.FOLLOW, 14, 14, "follow_straight");
    assertTrue(fragment.commandForFrame(recovered, "").contains("c14,14"));
    fragment.onFrameUiApplied(recovered);
    assertTrue(fragment.binding.startSwitch.isChecked());
    assertFalse(fragment.binding.simulatorStatus.getText().toString().contains("PARKED_WAIT 停车"));
  }

  @Test
  public void ordinaryParkedWaitHasZeroOutputAndSafetyLockoutTakesPriority() {
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    FollowStateMachine.FrameResult frame = frame(1000);
    frame.simulatorDriveResult =
        drive(SimulatorAutoDriveController.Phase.PARKED_WAIT, 0, 0, "target_missing_wait");
    frame.directedReacquireEvidence = search(DirectedReacquireEvidence.Phase.TURNING);
    String text = fragment.commandForFrame(frame, "");
    assertTrue(text.contains("目标身份已保留"));
    assertFalse(text.contains("本轮模拟已结束"));
    assertZeroCommand(text);
    frame.simulatorDriveResult =
        new SimulatorAutoDriveController.Result(
            SimulatorAutoDriveController.Phase.ENDED, 0, 0, 0, "safety_stop", true, 0, 2000);
    frame.directedReacquireEvidence = search(DirectedReacquireEvidence.Phase.PARKED_WAIT);
    assertTrue(fragment.commandForFrame(frame, "").contains("安全停止"));
    assertFalse(fragment.commandForFrame(frame, "").contains("目标身份已保留"));
  }

  private static void assertZeroCommand(String text) {
    assertTrue(text, text.contains("c0,0"));
    assertFalse(text, text.contains("c-18,18"));
    assertFalse(text, text.contains("c14,14"));
  }

  @Test
  public void prepareLearningExposesSameFrameSideExitBeforeFollowOutput() {
    for (boolean leftExit : new boolean[] {false, true}) {
      LearningHookFragment fragment = new LearningHookFragment();
      long firstAt = android.os.SystemClock.elapsedRealtime();
      FollowStateMachine.FrameResult first =
          learningObservation(firstAt, 1, leftExit ? 0f : .8f, leftExit ? .2f : 1f, false);
      fragment.prepareSimulatorLearningFrame(first, firstAt);
      assertFalse(fragment.simulatorExitLearningRisk(firstAt));
      fragment.onFollowFrame(first);

      org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMillis(100));
      long exitAt = android.os.SystemClock.elapsedRealtime();
      FollowStateMachine.FrameResult edge =
          learningObservation(exitAt, 2, leftExit ? 0f : .9f, leftExit ? .1f : 1f, true);
      assertFalse(fragment.simulatorExitLearningRisk(exitAt));
      assertNull(edge.directedReacquireEvidence);
      fragment.prepareSimulatorLearningFrame(edge, exitAt);

      // This is the pre-Gallery boundary: drive output has not been computed for this frame yet.
      assertTrue(fragment.simulatorExitLearningRisk(exitAt));
      assertNull(edge.simulatorDriveResult);
      DirectedReacquireEvidence prepared = edge.directedReacquireEvidence;
      assertEquals(DirectedReacquireEvidence.Phase.IDLE, prepared.phase);
      assertEquals("exit_outward_verified", prepared.reason);
      fragment.onFollowFrame(edge);
      assertSame(prepared, edge.directedReacquireEvidence);
      assertEquals(2, fragment.preparedFrames);
      assertTrue(fragment.simulatorExitLearningRisk(exitAt));
    }
  }

  @Test
  public void onFollowFrameReusesPreparedMissingObservationWithoutAdvancingDebounce() {
    LearningHookFragment fragment = new LearningHookFragment();
    long now = android.os.SystemClock.elapsedRealtime();
    FollowStateMachine.FrameResult first = learningObservation(now, 1, .7f, .9f, false);
    fragment.prepareSimulatorLearningFrame(first, now);
    fragment.onFollowFrame(first);
    org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMillis(100));
    now = android.os.SystemClock.elapsedRealtime();
    FollowStateMachine.FrameResult edge = learningObservation(now, 2, .8f, 1f, true);
    fragment.prepareSimulatorLearningFrame(edge, now);
    fragment.onFollowFrame(edge);

    org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMillis(100));
    now = android.os.SystemClock.elapsedRealtime();
    FollowStateMachine.FrameResult missing = frame(now);
    missing.frameSequence = 3;
    fragment.prepareSimulatorLearningFrame(missing, now);
    DirectedReacquireEvidence prepared = missing.directedReacquireEvidence;
    assertEquals(DirectedReacquireEvidence.Phase.IDLE, prepared.phase);
    assertEquals("target_missing_debounce", prepared.reason);
    assertEquals(3, fragment.preparedFrames);

    // Enough wall time has passed, but the post-Gallery callback is still the same observation.
    org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMillis(100));
    fragment.onFollowFrame(missing);
    fragment.onFollowFrame(missing);
    assertEquals(3, fragment.preparedFrames);
    assertSame(prepared, missing.directedReacquireEvidence);
    assertEquals(0, missing.simulatorDriveResult.left);
    assertEquals(0, missing.simulatorDriveResult.right);

    now = android.os.SystemClock.elapsedRealtime();
    FollowStateMachine.FrameResult nextMissing = frame(now);
    nextMissing.frameSequence = 4;
    fragment.prepareSimulatorLearningFrame(nextMissing, now);
    DirectedReacquireEvidence turning = nextMissing.directedReacquireEvidence;
    assertEquals(DirectedReacquireEvidence.Phase.TURNING, turning.phase);
    assertEquals(SteeringEvidence.Direction.RIGHT, turning.direction);
    fragment.onFollowFrame(nextMissing);
    assertEquals(4, fragment.preparedFrames);
    assertSame(turning, nextMissing.directedReacquireEvidence);
    assertEquals(5, nextMissing.simulatorDriveResult.left);
    assertEquals(-5, nextMissing.simulatorDriveResult.right);
  }

  private static final class LearningHookFragment extends HumanCartSimulatorFragment {
    int preparedFrames;

    @Override
    protected void prepareSimulatorLearningFrame(FollowStateMachine.FrameResult frame, long now) {
      preparedFrames++;
      super.prepareSimulatorLearningFrame(frame, now);
    }
  }

  private static FollowStateMachine.FrameResult learningObservation(
      long time, long sequence, float left, float right, boolean low) {
    org.openbot.tflite.Detector.Recognition person =
        new org.openbot.tflite.Detector.Recognition(
            "1", "person", low ? .3f : .95f, new android.graphics.RectF(left, 0, right, 1), 0);
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.FOLLOW,
            new Control(0, 0),
            null,
            null,
            low ? Collections.emptyList() : Collections.singletonList(person),
            false,
            false,
            null,
            -1);
    frame.frameSequence = sequence;
    frame.frameTiming = new FrameTimingEvidence(time, 0, 0, 0, 0, 0, 10, 0);
    if (low) {
      frame.detectionTierEvidence =
          new DetectionTierEvidence(
              .5f,
              .25f,
              Collections.singletonList(person),
              Collections.singletonList(person),
              true);
    }
    frame.targetObservation =
        new TargetObservationEvidence(
            new android.graphics.RectF(left, 0, right, 1), 1, time, .9f, low, true, 1, "locked");
    ReIDMatchResult match =
        new ReIDMatchResult(.92f, .75f, 0, 8, true, 1, "test", .92f, 0, sequence)
            .withBinding(1, time, sequence, true, 0, false);
    frame.identityEvidence =
        new IdentityEvidence(
            .9f, .9f, true, "test", match, null, 3, 0, person, 1, 1, -1, 1, 5, 0, .9f, 0, 0, 0, 0,
            3, 0, "test");
    return frame;
  }

  private static HumanCartSimulatorFragment boundFragment() {
    Context context =
        new ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppTheme);
    HumanCartSimulatorFragment fragment = new HumanCartSimulatorFragment();
    fragment.binding = FragmentHumanCartSimulatorBinding.inflate(LayoutInflater.from(context));
    return fragment;
  }

  private static FollowStateMachine.FrameResult frame(long receivedAt) {
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
    frame.frameTiming = new FrameTimingEvidence(receivedAt, 0, 0, 0, 0, 0, 10, 0);
    return frame;
  }

  private static SimulatorAutoDriveController.Result drive(
      SimulatorAutoDriveController.Phase phase, int left, int right, String reason) {
    return new SimulatorAutoDriveController.Result(
        phase,
        phase == SimulatorAutoDriveController.Phase.FOLLOW && left > 0 ? 14 : 0,
        left,
        right,
        reason,
        false,
        500,
        2000);
  }

  private static DirectedReacquireEvidence search(DirectedReacquireEvidence.Phase phase) {
    return new DirectedReacquireEvidence(
        phase,
        SteeringEvidence.Direction.LEFT,
        18,
        45,
        90,
        5000,
        5000,
        true,
        false,
        false,
        phase == DirectedReacquireEvidence.Phase.PARKED_WAIT ? "search_timeout" : "search");
  }

  private static SimulatorIdentityGuard.Decision verification(boolean global) {
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    int track = global ? 2 : 1;
    ReIDMatchResult match =
        new ReIDMatchResult(.97f, .1f, 0, 8, true, 30, "fresh", .97f, 0f, 1)
            .withBinding(track, 1000, 1, true, 0, false);
    return guard.update(
        1,
        1,
        1000,
        1000,
        track,
        1,
        true,
        !global,
        match,
        1,
        false,
        false,
        global ? match : null,
        null,
        false);
  }
}
