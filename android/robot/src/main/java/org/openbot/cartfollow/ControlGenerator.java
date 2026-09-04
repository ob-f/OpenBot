package org.openbot.cartfollow;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import org.openbot.tflite.Detector.Recognition;
import org.openbot.vehicle.Control;

public class ControlGenerator {

  public static class Result {
    public final Control control;
    public final Recognition target;
    public final List<Recognition> persons;
    public final boolean tooClose;
    public final ImageSetpointDistanceEstimator.DistanceEstimate distanceEstimate;

    public Result(
        Control control,
        Recognition target,
        List<Recognition> persons,
        boolean tooClose,
        ImageSetpointDistanceEstimator.DistanceEstimate distanceEstimate) {
      this.control = control;
      this.target = target;
      this.persons = persons;
      this.tooClose = tooClose;
      this.distanceEstimate = distanceEstimate;
    }
  }

  public float K_TURN = 1.5f;
  public float MAX_FORWARD = 0.6f;
  public float MIN_CONFIDENCE = 0.5f;
  public boolean FLIP_TURN = true;

  public final ImageSetpointDistanceEstimator distanceEstimator =
      new ImageSetpointDistanceEstimator();

  public void setMaximumDistanceMultiplier(float value) {
    distanceEstimator.setMaximumDistanceMultiplier(value);
  }

  public float getMaximumDistanceMultiplier() {
    return distanceEstimator.getMaximumDistanceMultiplier();
  }

  public Result generate(List<Recognition> results, int frameW, int frameH, int sensorOrientation) {
    List<Recognition> persons = new ArrayList<>();
    if (results != null) {
      for (Recognition r : results) {
        if (r == null || r.getLocation() == null) continue;
        if (r.getConfidence() == null || r.getConfidence() < MIN_CONFIDENCE) continue;
        if (!"person".equals(r.getTitle())) continue;
        persons.add(r);
      }
    }

    if (persons.isEmpty() || frameW <= 0 || frameH <= 0) {
      return new Result(new Control(0f, 0f), null, persons, false, null);
    }

    Recognition target = null;
    float maxArea = -1f;
    for (Recognition r : persons) {
      RectF loc = r.getLocation();
      float area = loc.width() * loc.height();
      if (area > maxArea) {
        maxArea = area;
        target = r;
      }
    }

    return generateFromTarget(target, persons, frameW, frameH, sensorOrientation, null);
  }

  public Result generateFromTarget(
      Recognition target,
      List<Recognition> persons,
      int frameW,
      int frameH,
      int sensorOrientation,
      TargetMemory memory) {
    if (target == null || target.getLocation() == null || frameW <= 0 || frameH <= 0) {
      return new Result(
          new Control(0f, 0f), null, persons == null ? new ArrayList<>() : persons, false, null);
    }

    RectF loc = target.getLocation();
    boolean rotated = sensorOrientation % 180 == 90;
    float imgWidth = rotated ? frameH : frameW;
    float imgHeight = rotated ? frameW : frameH;
    float centerX = rotated ? loc.centerY() : loc.centerX();
    centerX = Math.max(0f, Math.min(centerX, imgWidth));

    float xError = centerX / imgWidth - 0.5f;

    ImageSetpointDistanceEstimator.Setpoint setpoint =
        memory == null ? null : memory.getDistanceSetpoint();
    ImageSetpointDistanceEstimator.DistanceEstimate est =
        distanceEstimator.estimate(target, frameW, frameH, sensorOrientation, setpoint);

    float forward;
    boolean tooClose;
    switch (est.state) {
      case TOO_FAR:
        forward = MAX_FORWARD;
        tooClose = false;
        break;
      case TOO_CLOSE:
        forward = 0f;
        tooClose = true;
        break;
      case OK:
        forward = 0f;
        tooClose = false;
        break;
      case UNKNOWN:
      default:
        forward = 0f;
        tooClose = false;
        break;
    }

    float turn = K_TURN * xError;
    if (FLIP_TURN) turn = -turn;

    float left = forward - turn;
    float right = forward + turn;
    return new Result(new Control(left, right), target, persons, tooClose, est);
  }
}
