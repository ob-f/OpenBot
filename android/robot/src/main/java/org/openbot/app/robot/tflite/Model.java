package org.openbot.app.robot.tflite;

import android.util.Size;
import com.google.gson.annotations.SerializedName;

/** The model. */
public class Model {


  public Model(
      Integer id,
      CLASS classType,
      TYPE type,
      String name,
      PATH_TYPE pathType,
      String path,
      String inputSize) {
    this.id = id;
    this.classType = classType;
    this.type = type;
    this.name = name;
    this.pathType = pathType;
    this.path = path;
    this.inputSize = inputSize;
  }

  public enum CLASS {
    AUTOPILOT,
    MOBILENET,
    EFFICIENTDET,
    YOLOV4,
    YOLOV5,
    NAVIGATION
  }

  public enum TYPE {
    CMDNAV,
    DETECTOR,
    GOALNAV,
  }

  public enum PATH_TYPE {
    URL,
    ASSET,
    FILE
  }

  @SerializedName("class")
  public CLASS classType;

  public Integer id;

  public TYPE type;
  public String name;
  public PATH_TYPE pathType;
  public String path;
  private String inputSize;

  public String getName() {
    return name;
  }

  public Size getInputSize() {
    return Size.parseSize(inputSize);
  }

  public TYPE getType(){ return type; }
  public void setPath(String path) {
    this.path = path;
  }

  public void setPathType(PATH_TYPE pathType) {
    this.pathType = pathType;
  }

  public void setInputSize(String inputSize) {
    this.inputSize = inputSize;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setClassType(CLASS classType) {
    this.classType = classType;
  }

  public void setType(TYPE type) {
    this.type = type;
  }
}
