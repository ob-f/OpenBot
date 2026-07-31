package org.openbot.app.robot.model;

public class SubCategory {

  public SubCategory(String title, int image, String backgroundColor) {
    this.title = title;
    this.backgroundColor = backgroundColor;
    this.image = image;
  }

  private String title;
  private String backgroundColor;
  private int image;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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
