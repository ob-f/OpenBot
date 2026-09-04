package org.openbot.vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reassembles newline-delimited serial messages across BLE/USB chunks. */
public final class SerialLineAccumulator {
  private static final int MAX_BUFFER_CHARS = 1024;
  private final StringBuilder pending = new StringBuilder();

  public synchronized List<String> accept(String chunk) {
    if (chunk == null || chunk.isEmpty()) return Collections.emptyList();
    pending.append(chunk);
    if (pending.length() > MAX_BUFFER_CHARS && pending.indexOf("\n") < 0) {
      pending.setLength(0);
      return Collections.emptyList();
    }
    List<String> lines = new ArrayList<>();
    int newline;
    while ((newline = pending.indexOf("\n")) >= 0) {
      String line = pending.substring(0, newline);
      pending.delete(0, newline + 1);
      if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
      if (!line.isEmpty()) lines.add(line);
    }
    return lines;
  }

  public synchronized void clear() {
    pending.setLength(0);
  }
}
