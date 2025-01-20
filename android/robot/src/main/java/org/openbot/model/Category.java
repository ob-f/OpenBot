package org.openbot.model;

import android.content.Context;
import java.util.List;

public class Category {

  private int titleResId;  // Resource ID for the title
  private List<SubCategory> subCategories;

  // Default constructor
  public Category() {}

  // Constructor accepting a resource ID for the title
  public Category(int titleResId, List<SubCategory> subCategories) {
    this.titleResId = titleResId;
    this.subCategories = subCategories;
  }

  // Getter method to retrieve the title as a string
  public String getTitle(Context context) {
    return context.getString(titleResId);  // Fetches string resource
  }

  // Getter and setter for subCategories
  public List<SubCategory> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<SubCategory> subCategories) {
    this.subCategories = subCategories;
  }

  // Getter and setter for titleResId
  public int getTitleResId() {
    return titleResId;
  }

  public void setTitleResId(int titleResId) {
    this.titleResId = titleResId;
  }
}
