package org.openbot.cartfollow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openbot.tflite.Detector.Recognition;

/** Simulator-only evidence from the high/low confidence detector split. */
public final class DetectionTierEvidence {
  public final float highThreshold;
  public final float lowThreshold;
  public final List<Recognition> lowConfidencePersons;
  public final List<Recognition> continuedLowConfidencePersons;
  public final boolean selectedCandidateIsLowConfidence;

  public DetectionTierEvidence(
      float highThreshold,
      float lowThreshold,
      List<Recognition> lowConfidencePersons,
      List<Recognition> continuedLowConfidencePersons,
      boolean selectedCandidateIsLowConfidence) {
    this.highThreshold = highThreshold;
    this.lowThreshold = lowThreshold;
    this.lowConfidencePersons = immutableCopy(lowConfidencePersons);
    this.continuedLowConfidencePersons = immutableCopy(continuedLowConfidencePersons);
    this.selectedCandidateIsLowConfidence = selectedCandidateIsLowConfidence;
  }

  public static DetectionTierEvidence disabled(float highThreshold) {
    return new DetectionTierEvidence(
        highThreshold, highThreshold, Collections.emptyList(), Collections.emptyList(), false);
  }

  private static List<Recognition> immutableCopy(List<Recognition> source) {
    if (source == null || source.isEmpty()) return Collections.emptyList();
    return Collections.unmodifiableList(new ArrayList<>(source));
  }
}
