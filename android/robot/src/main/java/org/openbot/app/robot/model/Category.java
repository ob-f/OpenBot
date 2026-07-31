package org.openbot.app.robot.model;

import java.util.List;

public class Category {

  public Category() {}

  public Category(String title, List<SubCategory> subCategories) {
    this.title = title;
    this.subCategories = subCategories;
  }

  private String title;
  private List<SubCategory> subCategories;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<SubCategory> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<SubCategory> subCategories) {
    this.subCategories = subCategories;
  }
}
