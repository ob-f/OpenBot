package org.openbot.cartfollow;

/** Requires repeated fresh strong ReID evidence before a remote track may replace a lost lock. */
final class GlobalReacquireGate {
  private static final int REQUIRED_MATCHES = 3;
  private int trackId = -1;
  private int matches;
  private long lastReidRunMs;

  void reset() {
    trackId = -1;
    matches = 0;
    lastReidRunMs = 0L;
  }

  boolean update(
      int candidateTrackId, boolean lockedVisible, ReIDMatchResult reid, long freshReidRunMs) {
    if (lockedVisible
        || candidateTrackId < 0
        || reid == null
        || !reid.reidAvailable
        || reid.bestScore < ReIDMatchResult.BEST_STRONG
        || reid.margin < 0.08f) {
      reset();
      return false;
    }
    if (freshReidRunMs <= 0L || freshReidRunMs == lastReidRunMs) return false;
    lastReidRunMs = freshReidRunMs;
    if (candidateTrackId != trackId) {
      trackId = candidateTrackId;
      matches = 1;
    } else {
      matches++;
    }
    return matches >= REQUIRED_MATCHES;
  }
}
