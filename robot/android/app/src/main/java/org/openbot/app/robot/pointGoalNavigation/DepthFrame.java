package org.openbot.app.robot.pointGoalNavigation;

import android.graphics.ImageFormat;
import android.media.Image;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DepthFrame {

  private int width;
  private int height;
  private int rowStride;
  private int pixelStride;
  private ByteBuffer data;

  public DepthFrame(final Image depthImage) {
    assert (depthImage.getFormat() == ImageFormat.DEPTH16);
    assert (depthImage.getPlanes().length == 1);

    width = depthImage.getWidth();
    height = depthImage.getHeight();

    rowStride = depthImage.getPlanes()[0].getRowStride();
    pixelStride = depthImage.getPlanes()[0].getPixelStride();

    ByteBuffer byteBuffer = depthImage.getPlanes()[0].getBuffer();
    data = ByteBuffer.allocate(byteBuffer.capacity());
    data.put(byteBuffer);
    data = data.order(ByteOrder.nativeOrder());
  }

  // INFO: This constructor is only used for testing. This is bad practice.
  // The problem is that an `Image` cannot be easily created/constructed. For the test, we need to
  // define the content of the test image.
  // Hence, we use this additional constructor as (non-optimal) remedy.
  public DepthFrame(short[][] inputData) {
    width = inputData[0].length;
    height = inputData.length;

    rowStride = 2 * width;
    pixelStride = 2;

    data = ByteBuffer.allocate(2 * width * height);
    ByteOrder byteOrder = ByteOrder.nativeOrder();
    data = data.order(byteOrder);

    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        data.putShort(inputData[row][col]);
      }
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public short getPixelValue(int row, int col) {
    int idx = col * pixelStride + row * rowStride;
    return data.getShort(idx);
  }
}
