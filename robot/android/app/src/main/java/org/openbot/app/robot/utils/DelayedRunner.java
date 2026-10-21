package org.openbot.app.robot.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayedRunner {
  private static final ScheduledExecutorService worker =
      Executors.newSingleThreadScheduledExecutor();

  public void runAfter(Runnable action, Long delay, TimeUnit timeUnit) {
    worker.schedule(action, delay, timeUnit);
  }
}
