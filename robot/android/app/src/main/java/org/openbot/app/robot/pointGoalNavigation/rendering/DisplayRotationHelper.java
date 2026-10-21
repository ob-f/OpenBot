package org.openbot.app.robot.pointGoalNavigation.rendering;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.view.Display;
import android.view.WindowManager;
import com.google.ar.core.Session;

/**
 * Helper to track the display rotations. In particular, the 180 degree rotations are not notified
 * by the onSurfaceChanged() callback, and thus they require listening to the android display
 * events. * From:
 * https://chromium.googlesource.com/external/github.com/google-ar/arcore-android-sdk/+/refs/tags/v1.2.0/samples/cloud_anchor_java/app/src/main/java/com/google/ar/core/examples/java/common/helpers/DisplayRotationHelper.java
 */
public final class DisplayRotationHelper implements DisplayListener {
  private final Display display;
  private final DisplayManager displayManager;
  private boolean viewportChanged;
  private int viewportWidth;
  private int viewportHeight;

  /**
   * Constructs the DisplayRotationHelper but does not register the listener yet.
   *
   * @param context the Android {@link Context}.
   */
  public DisplayRotationHelper(Context context) {
    displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    display = windowManager.getDefaultDisplay();
  }

  /**
   * Registers the display listener. Should be called from {@link DisplayRotationHelper#onResume()}.
   */
  public void onResume() {
    displayManager.registerDisplayListener(this, null);
  }

  /**
   * Unregisters the display listener. Should be called from {@link
   * DisplayRotationHelper#onPause()}.
   */
  public void onPause() {
    displayManager.unregisterDisplayListener(this);
  }

  /**
   * Records a change in surface dimensions. This will be later used by {@link
   * #updateSessionIfNeeded(Session)}. Should be called from {@link
   * android.opengl.GLSurfaceView.Renderer
   * #onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)}.
   *
   * @param width the updated width of the surface.
   * @param height the updated height of the surface.
   */
  public void onSurfaceChanged(int width, int height) {
    viewportWidth = width;
    viewportHeight = height;
    viewportChanged = true;
  }

  /**
   * Updates the session display geometry if a change was posted either by {@link
   * #onSurfaceChanged(int, int)} call or by {@link #onDisplayChanged(int)} system callback. This
   * function should be called explicitly before each call to {@link Session#update()}. This
   * function will also clear the 'pending update' (viewportChanged) flag.
   *
   * @param session the {@link Session} object to update if display geometry changed.
   */
  public void updateSessionIfNeeded(Session session) {
    if (viewportChanged) {
      int displayRotation = display.getRotation();
      session.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight);
      viewportChanged = false;
    }
  }

  @Override
  public void onDisplayAdded(int displayId) {}

  @Override
  public void onDisplayRemoved(int displayId) {}

  @Override
  public void onDisplayChanged(int displayId) {
    viewportChanged = true;
  }
}
