package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class GalleryCropGeometryTest {
  @Test
  public void allUprightBordersAreDiagnosticOnlyInEveryOrientation() {
    for (int rotation : new int[] {0, 90, 180, 270, -90, 450}) {
      GalleryCropGeometry clippedTop = geometry(new RectF(50, -20, 150, 250), rotation);
      assertTrue(clippedTop.topClipped);
      assertFalse(clippedTop.bottomClipped);
      assertFalse(clippedTop.lateralClipped);
      assertTrue(geometry(new RectF(50, 750, 150, 1020), rotation).bottomClipped);
      assertTrue(geometry(new RectF(0, 100, 100, 400), rotation).leftClipped);
      assertTrue(geometry(new RectF(400, 100, 500, 400), rotation).rightClipped);
      assertTrue(geometry(new RectF(50, -20, 150, 250), rotation).normalAllowed);
      assertTrue(geometry(new RectF(50, 750, 150, 1020), rotation).normalAllowed);
      assertTrue(geometry(new RectF(-50, 100, 100, 400), rotation).normalAllowed);
      assertTrue(geometry(new RectF(400, 100, 550, 400), rotation).quarantineAllowed);
      assertTrue(geometry(new RectF(1, 100, 101, 400), rotation).normalAllowed);
      assertTrue(geometry(new RectF(399, 100, 499, 400), rotation).normalAllowed);
      GalleryCropGeometry fullFrame = geometry(new RectF(-10, -10, 510, 1010), rotation);
      assertTrue(fullFrame.normalAllowed);
      assertTrue(fullFrame.leftClipped && fullFrame.rightClipped);
      assertTrue(fullFrame.topClipped && fullFrame.bottomClipped);
      assertEquals(500f, fullFrame.visibleWidthPx, 0f);
      assertEquals(1000f, fullFrame.visibleHeightPx, 0f);
      assertEquals(
          "bbox_exited_frame", geometry(new RectF(510, 100, 600, 400), rotation).normalReason);
    }
  }

  @Test
  public void narrowVisibleIntersectionIsRejectedRegardlessOfOriginalBoxSizeOrRotation() {
    for (int rotation : new int[] {0, 90, 180, 270, -90, 450}) {
      GalleryCropGeometry narrow = geometry(new RectF(-200, 100, 44, 400), rotation);
      assertEquals(44f, narrow.visibleWidthPx, 0f);
      assertEquals(300f, narrow.visibleHeightPx, 0f);
      assertTrue(narrow.leftClipped);
      assertFalse(narrow.normalAllowed);
      assertFalse(narrow.quarantineAllowed);
      assertEquals("visible_aspect_ratio_below_0.15", narrow.normalReason);
      assertTrue(geometry(new RectF(-200, 100, 45, 400), rotation).normalAllowed);
      assertTrue(geometry(new RectF(455, 100, 700, 400), rotation).normalAllowed);
      assertFalse(geometry(new RectF(456, 100, 700, 400), rotation).quarantineAllowed);
      assertEquals(
          "upright_width_below_32px",
          geometry(new RectF(-200, 100, 31, 400), rotation).normalReason);
      assertEquals("ok", geometry(new RectF(50, -500, 150, 119), rotation).quarantineReason);
    }
  }

  @Test
  public void pixelAndRelativeHeightLimitsUseVisibleUprightDimensions() {
    for (int rotation : new int[] {0, 90, 180, 270}) {
      assertTrue(geometry(new RectF(50, -100, 82, 180), rotation).normalAllowed);
      GalleryCropGeometry quarantine = geometry(new RectF(50, -100, 82, 120), rotation);
      assertTrue(quarantine.normalAllowed);
      assertTrue(quarantine.quarantineAllowed);
      assertEquals(.12f, quarantine.heightRatio, 0f);
    }
    GalleryCropGeometry minPixels =
        GalleryCropGeometry.evaluate(new RectF(-50, -50, 32, 64), 100, 200, 0);
    assertTrue(minPixels.normalAllowed);
    assertEquals(32f, minPixels.visibleWidthPx, 0f);
    assertEquals(64f, minPixels.visibleHeightPx, 0f);
  }

  @Test
  public void invalidAndFullyOffscreenBoxesCannotBeAdmitted() {
    assertEquals("invalid_bbox", GalleryCropGeometry.evaluate(null, 500, 1000, 0).normalReason);
    assertEquals("invalid_bbox", geometry(new RectF(Float.NaN, 0, 100, 300), 0).normalReason);
    assertEquals("invalid_bbox", geometry(new RectF(100, 0, 50, 300), 0).normalReason);
    for (RectF box :
        new RectF[] {
          new RectF(-100, 100, 0, 400), new RectF(500, 100, 600, 400),
          new RectF(50, -300, 150, 0), new RectF(50, 1000, 150, 1300)
        }) {
      for (int rotation : new int[] {0, 90, 180, 270}) {
        assertEquals("bbox_exited_frame", geometry(box, rotation).normalReason);
        assertFalse(geometry(box, rotation).quarantineAllowed);
      }
    }
  }

  @Test
  public void actualUprightPixelsRatherThanFrameHeightControlAdmission() {
    assertTrue(geometry(new RectF(50, 0, 82, 180), 0).normalAllowed);
    GalleryCropGeometry quarantine = geometry(new RectF(50, 0, 82, 120), 0);
    assertTrue(quarantine.normalAllowed);
    assertTrue(quarantine.quarantineAllowed);
    assertEquals("ok", geometry(new RectF(50, -100, 82, 119), 0).quarantineReason);
    assertEquals("upright_width_below_32px", geometry(new RectF(50, 0, 81, 300), 0).normalReason);
    assertEquals("upright_height_below_64px", geometry(new RectF(50, 0, 100, 63), 0).normalReason);
    assertEquals("unsupported_orientation", geometry(new RectF(50, 0, 100, 300), 45).normalReason);
  }

  private static GalleryCropGeometry geometry(RectF display, int rotation) {
    int r = ((rotation % 360) + 360) % 360;
    RectF raw = display;
    int width = 500, height = 1000;
    if (r == 90) {
      width = 1000;
      height = 500;
      raw = new RectF(display.top, 500 - display.right, display.bottom, 500 - display.left);
    } else if (r == 180) {
      raw =
          new RectF(
              500 - display.right, 1000 - display.bottom, 500 - display.left, 1000 - display.top);
    } else if (r == 270) {
      width = 1000;
      height = 500;
      raw = new RectF(1000 - display.bottom, display.left, 1000 - display.top, display.right);
    }
    return GalleryCropGeometry.evaluate(raw, width, height, rotation);
  }
}
