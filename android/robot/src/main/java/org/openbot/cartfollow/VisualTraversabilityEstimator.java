package org.openbot.cartfollow;

import android.graphics.RectF;
import org.openbot.tflite.Detector.Recognition;

/** Image-only person corridor check in the same upright coordinates as target steering. */
public final class VisualTraversabilityEstimator {
  private VisualTraversabilityEstimator() {}

  public static TraversabilityEvidence estimate(
      FollowStateMachine.FrameResult frame, int width, int height, int orientation) {
    boolean blocked = false;
    if (frame != null && frame.persons != null && width > 0 && height > 0) {
      int targetTrack = frame.targetObservation != null && frame.targetObservation.current
          ? frame.targetObservation.trackId : -1;
      for (Recognition person : frame.persons) {
        if (person == null || person.getLocation() == null) continue;
        int track = frame.identityCandidates == null ? -1 : frame.identityCandidates.trackId(person);
        if (track >= 0 && track == targetTrack) continue;
        // Legacy frames have no track bindings: match the selected detection by id AND geometry.
        if (track < 0 && sameDetection(person, frame.target)) continue;
        RectF b = TargetObservationEvidence.toScreen(person.getLocation(), width, height, orientation);
        if (!Float.isFinite(b.left) || !Float.isFinite(b.top)
            || !Float.isFinite(b.right) || !Float.isFinite(b.bottom)) continue;
        b.left = Math.max(0f, Math.min(1f, b.left));
        b.top = Math.max(0f, Math.min(1f, b.top));
        b.right = Math.max(0f, Math.min(1f, b.right));
        b.bottom = Math.max(0f, Math.min(1f, b.bottom));
        if (b.width() > 0f && b.height() > 0f && b.centerX() >= .33f
            && b.centerX() <= .67f && b.bottom >= .55f && b.width() * b.height() >= .03f)
          blocked = true;
      }
    }
    return new TraversabilityEvidence(1f, blocked ? .2f : 1f, 1f, blocked,
        blocked ? "non_target_in_center_corridor" : "default_clear");
  }

  static boolean sameDetection(Recognition a, Recognition b) {
    return a != null && b != null && a.getId() != null && a.getId().equals(b.getId())
        && a.getLocation() != null && a.getLocation().equals(b.getLocation());
  }
}
