package org.openbot.cartfollow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openbot.tflite.Detector.Recognition;

/** The measured, track-bound people that are eligible for identity reasoning in one frame. */
public final class IdentityCandidateSet {
  public static final int MAX_CANDIDATES = 5;

  public final List<Recognition> candidates;
  public final Set<Integer> trackIds;
  private final Map<Recognition, Integer> trackByRecognition;

  private IdentityCandidateSet(
      List<Recognition> candidates,
      Set<Integer> trackIds,
      Map<Recognition, Integer> trackByRecognition) {
    this.candidates = Collections.unmodifiableList(candidates);
    this.trackIds = Collections.unmodifiableSet(trackIds);
    this.trackByRecognition = Collections.unmodifiableMap(trackByRecognition);
  }

  public static IdentityCandidateSet from(
      List<Recognition> highConfidence,
      List<Recognition> continuedLowConfidence,
      TargetTrackManager tracks) {
    List<Recognition> result = new ArrayList<>();
    Set<Integer> ids = new LinkedHashSet<>();
    Map<Recognition, Integer> bindings = new IdentityHashMap<>();
    addTrackBound(highConfidence, tracks, result, ids, bindings);
    addTrackBound(continuedLowConfidence, tracks, result, ids, bindings);
    return new IdentityCandidateSet(result, ids, bindings);
  }

  public static IdentityCandidateSet empty() {
    return new IdentityCandidateSet(
        new ArrayList<>(), new LinkedHashSet<>(), new IdentityHashMap<>());
  }

  private static void addTrackBound(
      List<Recognition> source,
      TargetTrackManager tracks,
      List<Recognition> result,
      Set<Integer> ids,
      Map<Recognition, Integer> bindings) {
    if (source == null || tracks == null) return;
    for (Recognition recognition : source) {
      TargetTrack track = tracks.getTrackForRecognition(recognition);
      if (track == null || !track.isVisible()) continue;
      bindings.put(recognition, track.trackId);
      if (ids.add(track.trackId)) result.add(recognition);
    }
  }

  public int size() {
    return candidates.size();
  }

  public boolean isMultiPerson() {
    return size() > 1;
  }

  public boolean exceedsBudget() {
    return size() > MAX_CANDIDATES;
  }

  public boolean contains(Recognition recognition) {
    return trackByRecognition.containsKey(recognition);
  }

  public int trackId(Recognition recognition) {
    Integer id = trackByRecognition.get(recognition);
    return id == null ? -1 : id;
  }
}
