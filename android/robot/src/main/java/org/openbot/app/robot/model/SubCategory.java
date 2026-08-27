package org.openbot.app.robot.model;

import android.content.Context;
import androidx.annotation.StringRes;

public class SubCategory {

  public SubCategory(String title, int image, String backgroundColor) {
    this.title = title;
    this.backgroundColor = backgroundColor;
    this.image = image;
  }

  public SubCategory(String title, @StringRes int titleResId, int image, String backgroundColor) {
    this.title = title;
    this.titleResId = titleResId;
    this.backgroundColor = backgroundColor;
    this.image = image;
  }

  private String title;
  @StringRes private int titleResId;
  private String backgroundColor;
  private int image;

  /** Internal identity key, used to match against {@code FeatureList} constants for navigation. */
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /** Localized text to show in the UI; falls back to the raw key if no string resource is set. */
  public String getDisplayTitle(Context context) {
    return titleResId != 0 ? context.getString(titleResId) : title;
  }

  public int getImage() {
    return image;
  }

  public void setImage(int image) {
    this.image = image;
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }
}
