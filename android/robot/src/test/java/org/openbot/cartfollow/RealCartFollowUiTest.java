package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.R;
import org.openbot.databinding.FragmentHumanCartSimulatorBinding;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class RealCartFollowUiTest {
  public static class Screen extends RealCartFollowFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
      binding = FragmentHumanCartSimulatorBinding.inflate(inflater, parent, false);
      return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle state) {}
  }

  private Screen screen() {
    FragmentActivity activity =
        Robolectric.buildActivity(FragmentActivity.class).create().start().get();
    activity.setTheme(R.style.AppTheme);
    Screen f = new Screen();
    activity
        .getSupportFragmentManager()
        .beginTransaction()
        .add(android.R.id.content, f)
        .commitNow();
    f.installFollowExperiments();
    return f;
  }

  @Test
  public void firstEntryUsesHighCapButSearchIsOff() {
    Screen f = screen();
    assertEquals(R.id.auto_gear_21, f.binding.autoGearGroup.getCheckedButtonId());
    assertFalse(f.binding.realSearchEnabled.isChecked());
    assertEquals(5, f.binding.searchSpeedSlider.getValue(), 0);
    assertFalse(f.binding.searchSpeedSlider.isEnabled());
    assertTrue(f.binding.recentGallerySwitch.isChecked());
    assertEquals(View.GONE, f.binding.recoveryTimeoutGroup.getVisibility());
    assertSame(f.binding.simulatorExperimentPanel, f.binding.steeringStrengthPanel.getParent());
  }

  @Test
  public void startLocksRiskControlsWithoutLockingSteeringStrength() {
    Screen f = screen();
    f.binding.realSearchEnabled.setChecked(true);
    f.binding.startSwitch.setChecked(true);
    f.setExperimentControlsEnabled(false);
    assertFalse(f.binding.realSearchEnabled.isEnabled());
    assertFalse(f.binding.searchSpeedSlider.isEnabled());
    assertFalse(f.binding.autoGear21.isEnabled());
    assertFalse(f.binding.galleryStatic.isEnabled());
    assertFalse(f.binding.recentGallerySwitch.isEnabled());
    assertTrue(f.binding.steeringStrengthSlider.isEnabled());
    f.binding.startSwitch.setChecked(false);
    f.setExperimentControlsEnabled(true);
    assertTrue(f.binding.searchSpeedSlider.isEnabled());
    assertTrue(f.binding.autoGear14.isEnabled());
  }

  @Test
  public void settingsPersistButRotationPermissionDoesNot() {
    Screen f = screen();
    f.binding.autoGearGroup.check(R.id.auto_gear_18);
    f.binding.realSearchEnabled.setChecked(true);
    f.binding.searchSpeedSlider.setValue(9);
    f.binding.searchAngleSlider.setValue(120);
    f.binding.searchTimeoutSlider.setValue(8);
    Screen next = screen();
    assertEquals(R.id.auto_gear_18, next.binding.autoGearGroup.getCheckedButtonId());
    assertEquals(9, next.binding.searchSpeedSlider.getValue(), 0);
    assertEquals(120, next.binding.searchAngleSlider.getValue(), 0);
    assertFalse(next.binding.realSearchEnabled.isChecked());
  }

  @Test
  public void malformedStoredLimitsClampAndStaticModeLocksRecent() {
    Screen f = screen();
    android.content.SharedPreferences p =
        f.requireContext().getSharedPreferences(RealFollowSettings.PREFS, 0);
    p.edit()
        .putInt("maximum_gear", 100)
        .putInt("search_speed", -1)
        .putInt("search_angle", 999)
        .putLong("search_timeout", 999999)
        .apply();
    RealFollowSettings s = RealFollowSettings.load(p);
    assertEquals(21, s.maximumGear);
    assertEquals(5, s.searchSpeed);
    assertEquals(180, s.searchAngle);
    assertEquals(10000, s.searchTimeoutMs);
    f.binding.galleryModeGroup.check(R.id.gallery_static);
    assertFalse(f.binding.recentGallerySwitch.isEnabled());
  }

  @Test
  public void gearButtonsStayInsideNarrowAndLandscapeLayouts() {
    Screen f = screen();
    f.binding.simulatorExperimentPanel.setVisibility(View.VISIBLE);
    for (int width : new int[] {320, 640}) {
      View group = f.binding.autoGearGroup;
      group.measure(
          View.MeasureSpec.makeMeasureSpec(width - 40, View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.AT_MOST));
      group.layout(0, 0, width - 40, group.getMeasuredHeight());
      assertTrue(f.binding.autoGear14.getWidth() > 0);
      assertTrue(f.binding.autoGear21.getRight() <= group.getWidth());
    }
  }

  @Test
  public void realAutomaticLayoutKeepsSettingsAndEmergencyAccessible() {
    Screen f = screen();
    f.binding.realControlPanel.setVisibility(View.VISIBLE);
    f.binding.manualDriveControls.setVisibility(View.GONE);
    f.binding.manualSpeedPanel.setVisibility(View.GONE);
    f.binding.unlockAuto.setVisibility(View.VISIBLE);
    f.binding.realSafetyNotice.setVisibility(View.VISIBLE);
    f.binding.steeringPanel.setVisibility(View.VISIBLE);
    f.binding.predictionHorizonGroup.setVisibility(View.GONE);
    f.binding.simulatorExperimentScroll.setVisibility(View.VISIBLE);
    f.binding.simulatorExperimentPanel.setVisibility(View.VISIBLE);
    f.binding.steeringStrengthPanel.setVisibility(View.VISIBLE);
    for (int[] size : new int[][] {{360, 720}, {720, 360}}) {
      f.configureResponsiveLayout(size[0], size[1]);
      View root = f.binding.getRoot();
      root.measure(
          View.MeasureSpec.makeMeasureSpec(size[0], View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(size[1], View.MeasureSpec.EXACTLY));
      root.layout(0, 0, size[0], size[1]);
      assertTrue(
          "settings viewport "
              + size[0]
              + " scroll="
              + f.binding.simulatorExperimentScroll.getTop()
              + ":"
              + f.binding.simulatorExperimentScroll.getBottom()
              + " control="
              + f.binding.realControlPanel.getTop()
              + ":"
              + f.binding.realControlPanel.getBottom()
              + " bottom="
              + f.binding.bottomPanel.getTop()
              + " density="
              + f.getResources().getDisplayMetrics().density,
          f.binding.simulatorExperimentScroll.getHeight() >= 48);
      assertTrue("controls in viewport", f.binding.realControlPanel.getTop() >= 0);
      assertTrue(f.binding.realControlPanel.getBottom() <= f.binding.bottomPanel.getTop());
    }
    f.configureResponsiveLayout(360, 720);
    assertSame(f.binding.getRoot(), f.binding.commandText.getParent());
    assertSame(f.binding.getRoot(), f.binding.confirmPanel.getParent());
    assertSame(f.binding.bottomPanel, ((View) f.binding.modelSpinner.getParent()).getParent());
  }
}
