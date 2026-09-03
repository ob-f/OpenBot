package org.openbot.cartfollow;

import static org.junit.Assert.*;

import org.junit.Test;

public class RecentGalleryTest {
  @Test
  public void redundantSamplesAreRetainedButQueryRequiresThreeOlderSamples() {
    RecentGallery gallery = new RecentGallery();
    float[] f = {1f, 0f};
    for (int i = 0; i < 3; i++) {
      assertEquals(0f, gallery.score(1, f, i * 300), 0f);
      assertTrue(gallery.append(1, i + 1, i * 300, i * 300, f));
    }
    assertEquals(1f, gallery.score(1, f, 601), .0001f);
    assertEquals(3, gallery.size(601));
    f[0] = 0f;
    assertEquals(1f, gallery.score(1, new float[] {1, 0}, 601), .0001f);
  }

  @Test
  public void capacityAgeTrackAndSampleIntervalAreBounded() {
    RecentGallery gallery = new RecentGallery();
    for (int i = 0; i < 20; i++) gallery.append(1, i + 1, i * 300, i * 300, new float[] {1});
    assertEquals(16, gallery.size(5700));
    assertFalse(gallery.append(1, 21, 5750, 5750, new float[] {1}));
    assertFalse(gallery.append(1, 20, 6000, 6000, new float[] {1}));
    assertEquals(0f, gallery.score(2, new float[] {1}, 6000), 0f);
    assertTrue(gallery.append(2, 21, 6000, 6000, new float[] {1}));
    assertEquals(1, gallery.size(6000));
    assertEquals(0, gallery.size(11000));
  }

  @Test
  public void usesTopThreeMeanAndRejectsStaleWrites() {
    RecentGallery gallery = new RecentGallery();
    for (int i = 0; i < 4; i++)
      gallery.append(1, i + 1, i * 300, i * 300, new float[] {.6f + i * .1f});
    assertEquals(.8f, gallery.score(1, new float[] {1}, 950), .0001f);
    assertFalse(gallery.append(1, 5, 1200, 1701, new float[] {1}));
    gallery.clear();
    assertEquals(0, gallery.size(2000));
  }

  @Test
  public void lateInsertionUsesOriginalTimeAndDoesNotMoveRegularWatermarks() {
    RecentGallery gallery = new RecentGallery();
    float[] feature = {1f};
    assertTrue(gallery.append(1, 10, 1800, 1800, feature));
    assertTrue(gallery.appendRetrospective(1, 8, 1200, 2000, feature));
    assertTrue(gallery.appendRetrospective(1, 9, 1500, 2000, feature));
    assertEquals(1f, gallery.score(1, feature, 2000), 0f);
    assertFalse(gallery.append(1, 9, 2100, 2100, feature));
    assertTrue(gallery.append(1, 11, 2100, 2100, feature));
    assertEquals(4, gallery.size(6199));
    assertEquals(3, gallery.size(6200));
    assertEquals(2, gallery.size(6500));
    assertEquals(1, gallery.size(6800));
    assertEquals(0, gallery.size(7100));
  }

  @Test
  public void chronologicalIntervalChecksBothNeighborsIncludingRegularEntries() {
    RecentGallery gallery = new RecentGallery();
    float[] f = {1};
    assertTrue(gallery.append(1, 10, 1000, 1000, f));
    assertTrue(gallery.append(1, 20, 1600, 1600, f));
    assertFalse(gallery.appendRetrospective(1, 11, 1299, 2000, f));
    assertFalse(gallery.appendRetrospective(1, 12, 1301, 2000, f));
    assertTrue(gallery.appendRetrospective(1, 13, 1300, 2000, f));
    assertFalse(gallery.appendRetrospective(1, 14, 701, 2000, f));
    assertTrue(gallery.appendRetrospective(1, 15, 700, 2000, f));
    assertTrue(gallery.appendRetrospective(1, 21, 2200, 2400, f));
    assertFalse(gallery.append(1, 22, 2000, 2400, f));
    assertTrue(gallery.append(1, 22, 1900, 2400, f));
    assertFalse(gallery.append(1, 23, 2400, 2400, f));
    assertTrue(gallery.append(1, 23, 2500, 2500, f));
  }

  @Test
  public void duplicatesAcrossBothAppendPathsCannotRefreshCaptureTime() {
    RecentGallery gallery = new RecentGallery();
    float[] f = {1};
    assertTrue(gallery.append(1, 10, 1000, 1000, f));
    assertTrue(gallery.appendRetrospective(1, 20, 1300, 2000, f));
    assertFalse(gallery.appendRetrospective(1, 10, 1600, 2000, f));
    assertFalse(gallery.appendRetrospective(1, 20, 1600, 2000, f));
    assertFalse(gallery.append(1, 20, 2000, 2000, f));
    assertEquals(2, gallery.size(5999));
    assertEquals(1, gallery.size(6000));
    assertEquals(0, gallery.size(6300));
  }

  @Test
  public void retrospectiveTtlIsCaptureBasedAndRegularFreshnessRemainsUnchanged() {
    RecentGallery gallery = new RecentGallery();
    float[] f = {1};
    assertFalse(gallery.append(1, 1, 1000, 1501, f));
    assertFalse(gallery.appendRetrospective(1, 1, 1000, 6000, f));
    assertFalse(gallery.appendRetrospective(1, 1, 6001, 6000, f));
    assertTrue(gallery.appendRetrospective(1, 1, 1000, 5999, f));
    assertEquals(1, gallery.size(5999));
    assertEquals(0, gallery.size(6000));
    assertFalse(gallery.appendRetrospective(1, 1, 1000, 6001, f));
    assertTrue(gallery.append(1, 2, 6000, 6500, f));
  }

  @Test
  public void capacityEvictsOldestCaptureNotOldestAdmission() {
    RecentGallery gallery = new RecentGallery();
    long now = 5800;
    for (int i = 0; i < RecentGallery.CAPACITY; i++) {
      long capturedAt = 5500 - i * 300;
      assertTrue(gallery.appendRetrospective(1, 100 - i, capturedAt, now, new float[] {1}));
    }
    assertEquals(16, gallery.size(now));
    assertFalse(gallery.appendRetrospective(1, 1, 850, now, new float[] {1}));
    assertTrue(gallery.append(1, 101, now, now, new float[] {1}));
    assertEquals(16, gallery.size(6000));
    assertEquals(15, gallery.size(6300));
    assertEquals(0, gallery.size(10800));
  }

  @Test
  public void capacityEvictionDoesNotPermitDuplicateReadmission() {
    RecentGallery gallery = new RecentGallery();
    for (int i = 0; i < 17; i++) {
      assertTrue(gallery.append(1, i + 1, i * 300, i * 300, new float[] {1}));
    }
    assertFalse(gallery.appendRetrospective(1, 1, 0, 4900, new float[] {1}));
    assertFalse(gallery.appendRetrospective(1, 1, 0, 5100, new float[] {1}));
  }

  @Test
  public void retrospectiveFeaturesAreCopiedAndTrackSwitchAndClearResetHistory() {
    RecentGallery gallery = new RecentGallery();
    float[] feature = {1};
    assertFalse(gallery.appendRetrospective(1, -1, 1000, 2000, feature));
    assertFalse(gallery.appendRetrospective(1, 1, 1000, 2000, new float[] {Float.NaN}));
    assertTrue(gallery.appendRetrospective(1, 1, 1000, 2000, feature));
    assertTrue(gallery.appendRetrospective(1, 2, 1300, 2000, feature));
    assertTrue(gallery.appendRetrospective(1, 3, 1600, 2000, feature));
    feature[0] = 0;
    assertEquals(1f, gallery.score(1, new float[] {1}, 2000), 0f);
    assertTrue(gallery.appendRetrospective(2, 1, 1000, 2000, feature));
    assertEquals(1, gallery.size(2000));
    assertEquals(0f, gallery.score(1, new float[] {1}, 2000), 0f);
    gallery.clear();
    assertTrue(gallery.appendRetrospective(2, 1, 1000, 2000, feature));
  }
}
