package org.openbot.app.robot.utils;

import java.util.ArrayList;
import java.util.List;

/*
AndGate will execute some action if all its input conditions are met.
This is useful if we do not know the order of the updates to the conditions.

We can add an arbitrary number of conditions, initially set to false. These conditions can
be updated in the future, and if all are true, the action will be executed.
 */
public class AndGate {
  private boolean isRunning = false;
  private List<Condition> conditions = new ArrayList<>();
  private Action startAction;
  private Action stopAction;

  public AndGate(Action startAction, Action stopAction) {
    this.conditions = conditions;
    this.startAction = startAction;
    this.stopAction = stopAction;
  }

  public void set(String name, boolean value) {
    for (Condition condition : conditions) {
      if (condition.name.equals(name)) {
        condition.value = value;
        break;
      }
    }
    // if all conditions match
    for (Condition condition : conditions) {
      if (!condition.value) {
        if (isRunning && stopAction != null) {
          isRunning = false;
          stopAction.runIt();
        }
        return;
      }
    }

    // and run the action
    if (!isRunning && startAction != null) {
      isRunning = true;
      startAction.runIt();
    }
  }

  private class Condition {
    String name;
    boolean value;

    public Condition(String name, boolean value) {
      this.name = name;
      this.value = value;
    }
  }

  public interface Action {
    public void runIt();
  }

  public void reset() {
    conditions.clear();
  }

  public void addCondition(String name) {
    conditions.add(new Condition(name, false));
  }
}
