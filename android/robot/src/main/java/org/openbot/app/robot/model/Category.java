package org.openbot.app.robot.model;

import android.content.Context;
import androidx.annotation.StringRes;
import java.util.List;

public class Category {

  public Category() {}

  public Category(String title, List<SubCategory> subCategories) {
    this.title = title;
    this.subCategories = subCategories;
  }

  public Category(String title, @StringRes int titleResId, List<SubCategory> subCategories) {
    this.title = title;
    this.titleResId = titleResId;
    this.subCategories = subCategories;
  }

  private String title;
  @StringRes private int titleResId;
  private List<SubCategory> subCategories;

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

  public List<SubCategory> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<SubCategory> subCategories) {
    this.subCategories = subCategories;
  }
}
