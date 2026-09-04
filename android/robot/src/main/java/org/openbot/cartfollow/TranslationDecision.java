package org.openbot.cartfollow;

/** Frame-local permission for forward translation, independent of camera aiming. */
public final class TranslationDecision {
  public final boolean allowed;
  public final int maximumGear;
  public final String reason;

  private TranslationDecision(boolean allowed, int maximumGear, String reason) {
    this.allowed = allowed;
    this.maximumGear = allowed ? AutoGearSelector.cap(maximumGear) : 0;
    this.reason = reason;
  }

  public static TranslationDecision allow(int maximumGear, String reason) {
    return new TranslationDecision(true, maximumGear, reason);
  }

  public static TranslationDecision block(String reason) {
    return new TranslationDecision(false, 0, reason);
  }
}
