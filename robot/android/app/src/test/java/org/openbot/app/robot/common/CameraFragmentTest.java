package org.openbot.app.robot.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import androidx.camera.core.ImageInfo;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.impl.TagBundle;
import androidx.camera.core.impl.utils.ExifData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.app.robot.utils.YuvToRgbConverter;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, shadows = CameraFragmentTest.FakeConverter.class)
public class CameraFragmentTest {
  private final List<String> events = new ArrayList<>();
  private TestCameraFragment fragment;
  private FakeConverter converter;

  @Before
  public void setUp() {
    fragment = new TestCameraFragment();
    fragment.events = events;
    YuvToRgbConverter realConverter = new YuvToRgbConverter(RuntimeEnvironment.getApplication());
    converter = Shadow.extract(realConverter);
    converter.events = events;
    ReflectionHelpers.setField(fragment, "converter", realConverter);
  }

  @Test
  public void processesConvertedFrameBeforeClosingItOnce() {
    FakeImage image = new FakeImage(events);

    analyze(image);

    assertEquals(Arrays.asList("convert", "process", "close"), events);
    assertEquals(1, fragment.processedFrames);
    assertSame(image, fragment.lastImage);
    assertEquals(Color.BLUE, fragment.lastBitmap.getPixel(0, 0));
    assertEquals(90, fragment.getRotationDegrees());
    assertClosedOnce(image);
  }

  @Test
  public void closesFrameWhenBitmapInitializationFails() {
    FakeImage image = new FakeImage(events);
    image.widthFailure = new IllegalStateException("Cannot read image width");

    assertAnalysisFailure(image, image.widthFailure);

    assertEquals(0, converter.calls);
    assertEquals(0, fragment.processedFrames);
    assertClosedOnce(image);
  }

  @Test
  public void closesFrameWhenRotationLookupFails() {
    FakeImage image = new FakeImage(events);
    image.rotationFailure = new IllegalStateException("Cannot read rotation");

    assertAnalysisFailure(image, image.rotationFailure);

    assertEquals(0, converter.calls);
    assertEquals(0, fragment.processedFrames);
    assertClosedOnce(image);
  }

  @Test
  public void closesFrameWithoutProcessingWhenConversionFails() {
    FakeImage image = new FakeImage(events);
    converter.failure = new IllegalArgumentException("Conversion failed");

    assertAnalysisFailure(image, converter.failure);

    assertEquals(Arrays.asList("convert", "close"), events);
    assertEquals(0, fragment.processedFrames);
    assertClosedOnce(image);
  }

  @Test
  public void closesFrameWhenProcessingThrows() {
    FakeImage image = new FakeImage(events);
    fragment.failure = new IllegalStateException("Processing failed");

    assertAnalysisFailure(image, fragment.failure);

    assertEquals(Arrays.asList("convert", "process", "close"), events);
    assertClosedOnce(image);
  }

  @Test
  public void closesFrameWhenProcessingReturnsEarly() {
    FakeImage image = new FakeImage(events);
    fragment.returnEarly = true;
    fragment.failure = new IllegalStateException("Must not run after early return");

    analyze(image);

    assertEquals(Arrays.asList("convert", "process", "close"), events);
    assertClosedOnce(image);
  }

  @Test
  public void canAnalyzeAnotherFrameAfterConversionFailure() {
    FakeImage failedImage = new FakeImage(events);
    converter.failure = new IllegalArgumentException("Conversion failed");
    assertAnalysisFailure(failedImage, converter.failure);
    converter.failure = null;
    FakeImage nextImage = new FakeImage(events);

    analyze(nextImage);

    assertEquals(2, converter.calls);
    assertEquals(1, fragment.processedFrames);
    assertSame(nextImage, fragment.lastImage);
    assertClosedOnce(failedImage);
    assertClosedOnce(nextImage);
  }

  @Test
  public void canAnalyzeAnotherFrameAfterProcessingFailure() {
    FakeImage failedImage = new FakeImage(events);
    fragment.failure = new IllegalStateException("Processing failed");
    assertAnalysisFailure(failedImage, fragment.failure);
    Bitmap previousBuffer = fragment.lastBitmap;
    fragment.failure = null;
    FakeImage nextImage = new FakeImage(events);

    analyze(nextImage);

    assertEquals(2, fragment.processedFrames);
    assertSame(nextImage, fragment.lastImage);
    assertSame(previousBuffer, fragment.lastBitmap);
    assertClosedOnce(failedImage);
    assertClosedOnce(nextImage);
  }

  private void analyze(ImageProxy image) {
    ReflectionHelpers.callInstanceMethod(
        fragment, "analyzeFrame", ClassParameter.from(ImageProxy.class, image));
  }

  private void assertAnalysisFailure(FakeImage image, RuntimeException expected) {
    Throwable thrown = assertThrows(RuntimeException.class, () -> analyze(image));
    // ReflectionHelpers may wrap the exception thrown by the private analyzer method.
    while (thrown.getCause() != null) thrown = thrown.getCause();
    assertSame(expected, thrown);
  }

  private static void assertClosedOnce(FakeImage image) {
    assertTrue(image.closed);
    assertEquals(1, image.closeCount);
  }

  private static class TestCameraFragment extends CameraFragment {
    List<String> events;
    int processedFrames;
    Bitmap lastBitmap;
    ImageProxy lastImage;
    RuntimeException failure;
    boolean returnEarly;

    @Override
    protected void processFrame(Bitmap bitmap, ImageProxy image) {
      assertFalse(((FakeImage) image).closed);
      assertEquals(Color.BLUE, bitmap.getPixel(0, 0));
      events.add("process");
      processedFrames++;
      lastBitmap = bitmap;
      lastImage = image;
      if (returnEarly) return;
      if (failure != null) throw failure;
    }

    @Override
    protected void processControllerKeyData(String command) {}

    @Override
    protected void processUSBData(String data) {}
  }

  @Implements(value = YuvToRgbConverter.class, isInAndroidSdk = false)
  public static class FakeConverter {
    List<String> events;
    int calls;
    RuntimeException failure;

    @Implementation
    protected void __constructor__(Context context) {}

    @Implementation
    protected void yuvToRgb(Image image, Bitmap output) {
      calls++;
      events.add("convert");
      if (failure != null) throw failure;
      output.eraseColor(Color.BLUE);
    }
  }

  private static class FakeImage implements ImageProxy {
    final List<String> events;
    boolean closed;
    int closeCount;
    RuntimeException widthFailure;
    RuntimeException rotationFailure;
    Rect cropRect = new Rect(0, 0, 4, 3);

    FakeImage(List<String> events) {
      this.events = events;
    }

    private void checkOpen() {
      if (closed) throw new IllegalStateException("Image already closed");
    }

    @Override
    public void close() {
      closeCount++;
      closed = true;
      events.add("close");
    }

    @Override
    public Rect getCropRect() {
      checkOpen();
      return cropRect;
    }

    @Override
    public void setCropRect(Rect rect) {
      checkOpen();
      cropRect = rect;
    }

    @Override
    public int getFormat() {
      checkOpen();
      return ImageFormat.YUV_420_888;
    }

    @Override
    public int getWidth() {
      checkOpen();
      if (widthFailure != null) throw widthFailure;
      return 4;
    }

    @Override
    public int getHeight() {
      checkOpen();
      return 3;
    }

    @Override
    public PlaneProxy[] getPlanes() {
      checkOpen();
      return new PlaneProxy[0];
    }

    @Override
    public Image getImage() {
      checkOpen();
      // The converter shadow does not require a platform Image or RenderScript.
      return null;
    }

    @Override
    public ImageInfo getImageInfo() {
      checkOpen();
      return new ImageInfo() {
        @Override
        public TagBundle getTagBundle() {
          return TagBundle.emptyBundle();
        }

        @Override
        public long getTimestamp() {
          return 0;
        }

        @Override
        public int getRotationDegrees() {
          checkOpen();
          if (rotationFailure != null) throw rotationFailure;
          return 90;
        }

        @Override
        public void populateExifData(ExifData.Builder builder) {}
      };
    }
  }
}
