package org.openbot.cartfollow;

/** Explicit perception and motion capabilities for each follow screen. */
public final class FollowPolicy {
  public final boolean enhancedIdentity;
  public final GalleryUpdateStatus.Mode galleryMode;
  public final boolean continuityMotion;
  public final boolean directedSearch;

  public FollowPolicy(
      boolean enhancedIdentity,
      GalleryUpdateStatus.Mode galleryMode,
      boolean continuityMotion,
      boolean directedSearch) {
    this.enhancedIdentity = enhancedIdentity;
    this.galleryMode = galleryMode;
    this.continuityMotion = continuityMotion;
    this.directedSearch = directedSearch;
  }
}
