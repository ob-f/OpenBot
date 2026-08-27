package org.openbot.app.robot.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class LanguageOption {

  @Nullable private final String tag;
  private final String displayName;
  @DrawableRes private final int flagResId;

  public LanguageOption(@Nullable String tag, String displayName, @DrawableRes int flagResId) {
    this.tag = tag;
    this.displayName = displayName;
    this.flagResId = flagResId;
  }

  /** BCP-47 language tag, or {@code null} to represent "follow system language". */
  @Nullable
  public String getTag() {
    return tag;
  }

  /** Language name shown in its own language, independent of the current app locale. */
  public String getDisplayName() {
    return displayName;
  }

  @DrawableRes
  public int getFlagResId() {
    return flagResId;
  }
}
