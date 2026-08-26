package org.openbot.app.robot.pointGoalNavigation;

import android.graphics.PointF;
import android.util.Size;

public class CameraIntrinsics {

  private Size size;
  private PointF focalLength; // in pixels
  private PointF principalPoint;

  public CameraIntrinsics(
      int width,
      int height,
      float principalPointX,
      float principalPointY,
      float focalLengthX,
      float focalLengthY) {
    size = new Size(width, height);
    focalLength = new PointF(focalLengthX, focalLengthY);
    principalPoint = new PointF(principalPointX, principalPointY);
  }

  public CameraIntrinsics(com.google.ar.core.CameraIntrinsics arCoreCameraIntrinsics) {
    int[] dims = arCoreCameraIntrinsics.getImageDimensions();
    float[] focalLength = arCoreCameraIntrinsics.getFocalLength();
    float[] principalPoint = arCoreCameraIntrinsics.getPrincipalPoint();

    this.size = new Size(dims[0], dims[1]);
    this.focalLength = new PointF(focalLength[0], focalLength[1]);
    this.principalPoint = new PointF(principalPoint[0], principalPoint[1]);
  }

  public CameraIntrinsics(
      com.google.ar.core.CameraIntrinsics arCoreCameraIntrinsics, float resizeFactor) {
    this(arCoreCameraIntrinsics);
    resize(resizeFactor);
  }

  public void resize(float resizeFactor) {
    size =
        new Size((int) (resizeFactor * size.getWidth()), (int) (resizeFactor * size.getHeight()));

    focalLength.x = resizeFactor * focalLength.x;
    focalLength.y = resizeFactor * focalLength.y;

    principalPoint.x = resizeFactor * principalPoint.x;
    principalPoint.y = resizeFactor * principalPoint.y;
  }

  public Size getSize() {
    return size;
  }

  public PointF getFocalLength() {
    return focalLength;
  }

  public PointF getPrincipalPoint() {
    return principalPoint;
  }
}
