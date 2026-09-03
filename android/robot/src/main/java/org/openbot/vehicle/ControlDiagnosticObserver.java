package org.openbot.vehicle;

/** Optional, non-blocking observer of transport events; never participates in scheduling. */
public interface ControlDiagnosticObserver {
  void onEvent(String event, String details);
}
