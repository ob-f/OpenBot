package org.openbot.app.robot.pointGoalNavigation;

import android.graphics.ImageFormat;
import android.media.Image;
import java.nio.ByteBuffer;

/* fillBytes() taken From: https://github.com/wangjiangyong/tflite_android_facedemo/blob/master/app/src/main/java/org/tensorflow/demo/CameraActivity.java */

public class ImageFrame {

  private byte[][] yuvBytes = new byte[3][];
  int width;
  int height;
  private int yRowStride;
  private int uvRowStride;
  private int uvPixelStride;

  public ImageFrame(final Image image) {
    assert (image.getFormat() == ImageFormat.YUV_420_888);
    assert (image.getPlanes().length == 3);

    Image.Plane[] planes = image.getPlanes();

    fillBytes(planes, yuvBytes);

    width = image.getWidth();
    height = image.getHeight();

    yRowStride = planes[0].getRowStride();
    uvRowStride = planes[1].getRowStride();
    uvPixelStride = planes[1].getPixelStride();
  }

  public byte[][] getYuvBytes() {
    return yuvBytes;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getYRowStride() {
    return yRowStride;
  }

  public int getUvRowStride() {
    return uvRowStride;
  }

  public int getUvPixelStride() {
    return uvPixelStride;
  }

  protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }
}
