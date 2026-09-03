package org.openbot.cartfollow;

import android.graphics.RectF;

/**
 * Gallery quality checks on the actual frame intersection in upright/display coordinates, before
 * padded extraction. Border flags are diagnostic only, not gallery admission vetoes.
 */
public final class GalleryCropGeometry {
  // The subject can legitimately fill a vertically mounted phone view at the intended distance.
  // Pixel dimensions, not image-height percentage, are the sampling quality boundary.
  public static final float NORMAL_MIN_HEIGHT = 0f;
  public static final float QUARANTINE_MIN_HEIGHT = 0f;
  public static final float MIN_UPRIGHT_WIDTH = 32f;
  public static final float MIN_UPRIGHT_HEIGHT = 64f;
  public static final float MIN_VISIBLE_ASPECT_RATIO = 0.15f;
  public static final float LATERAL_EDGE_MARGIN_PX = 2f;

  public final boolean normalAllowed;
  public final boolean quarantineAllowed;
  public final String normalReason;
  public final String quarantineReason;
  public final float visibleWidthPx;
  public final float visibleHeightPx;
  public final float heightRatio;
  public final boolean leftClipped;
  public final boolean rightClipped;
  public final boolean lateralClipped;
  public final boolean topClipped;
  public final boolean bottomClipped;

  private GalleryCropGeometry(float width, float height, float ratio, String rejection) {
    this(width, height, ratio, rejection, false, false, false, false);
  }

  private GalleryCropGeometry(
      float width,
      float height,
      float ratio,
      String rejection,
      boolean leftClipped,
      boolean rightClipped,
      boolean topClipped,
      boolean bottomClipped) {
    visibleWidthPx = width;
    visibleHeightPx = height;
    heightRatio = ratio;
    this.leftClipped = leftClipped;
    this.rightClipped = rightClipped;
    this.lateralClipped = leftClipped || rightClipped;
    this.topClipped = topClipped;
    this.bottomClipped = bottomClipped;
    normalReason =
        rejection != null
            ? rejection
            : ratio < NORMAL_MIN_HEIGHT
                ? "height_below_" + Math.round(NORMAL_MIN_HEIGHT * 100f) + "_percent"
                : "ok";
    quarantineReason =
        rejection != null
            ? rejection
            : ratio < QUARANTINE_MIN_HEIGHT
                ? "height_below_" + Math.round(QUARANTINE_MIN_HEIGHT * 100f) + "_percent"
                : "ok";
    normalAllowed = "ok".equals(normalReason);
    quarantineAllowed = "ok".equals(quarantineReason);
  }

  public static GalleryCropGeometry evaluate(RectF box, int width, int height, int orientation) {
    if (box == null
        || width <= 0
        || height <= 0
        || !Float.isFinite(box.left)
        || !Float.isFinite(box.top)
        || !Float.isFinite(box.right)
        || !Float.isFinite(box.bottom)
        || box.width() <= 0
        || box.height() <= 0) {
      return new GalleryCropGeometry(0, 0, 0, "invalid_bbox");
    }
    int rotation = ((orientation % 360) + 360) % 360;
    float left, right, top, bottom, displayWidth, displayHeight;
    switch (rotation) {
      case 0:
        left = box.left;
        right = box.right;
        top = box.top;
        bottom = box.bottom;
        displayWidth = width;
        displayHeight = height;
        break;
      case 90:
        left = height - box.bottom;
        right = height - box.top;
        top = box.left;
        bottom = box.right;
        displayWidth = height;
        displayHeight = width;
        break;
      case 180:
        left = width - box.right;
        right = width - box.left;
        top = height - box.bottom;
        bottom = height - box.top;
        displayWidth = width;
        displayHeight = height;
        break;
      case 270:
        left = box.top;
        right = box.bottom;
        top = width - box.right;
        bottom = width - box.left;
        displayWidth = height;
        displayHeight = width;
        break;
      default:
        return new GalleryCropGeometry(0, 0, 0, "unsupported_orientation");
    }
    float visibleWidth = Math.max(0, Math.min(displayWidth, right) - Math.max(0, left));
    float visibleHeight = Math.max(0, Math.min(displayHeight, bottom) - Math.max(0, top));
    String reason = null;
    if (right <= 0 || left >= displayWidth || bottom <= 0 || top >= displayHeight)
      reason = "bbox_exited_frame";
    else if (visibleWidth < MIN_UPRIGHT_WIDTH)
      reason = "upright_width_below_" + Math.round(MIN_UPRIGHT_WIDTH) + "px";
    else if (visibleHeight < MIN_UPRIGHT_HEIGHT)
      reason = "upright_height_below_" + Math.round(MIN_UPRIGHT_HEIGHT) + "px";
    else if (visibleWidth / visibleHeight < MIN_VISIBLE_ASPECT_RATIO)
      reason = "visible_aspect_ratio_below_" + MIN_VISIBLE_ASPECT_RATIO;
    return new GalleryCropGeometry(
        visibleWidth,
        visibleHeight,
        visibleHeight / displayHeight,
        reason,
        left <= LATERAL_EDGE_MARGIN_PX,
        right >= displayWidth - LATERAL_EDGE_MARGIN_PX,
        top <= 0f,
        bottom >= displayHeight);
  }
}
