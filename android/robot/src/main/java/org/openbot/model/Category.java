package org.openbot.model;

import android.content.Context;
import java.util.List;

public class Category {

  public Category() {}

  public Category(int titleResId, List<SubCategory> subCategories) {
    this.titleResId = titleResId;
    this.subCategories = subCategories;
  }

  // Store the resource ID for the title
  private int titleResId;
  private List<SubCategory> subCategories;

  // Getter method for the title using context to get the localized string
  public String getTitle(Context context) {
    return context.getString(titleResId);  // Retrieve the localized string using the resource ID
  }

  // Setter for the title (although it's now resource-based, this may not be used)
  public void setTitle(int titleResId) {
    this.titleResId = titleResId;
  }

  public List<SubCategory> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<SubCategory> subCategories) {
    this.subCategories = subCategories;
  }
}
