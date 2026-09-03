package org.openbot.vehicle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/** Serializes BLE UART writes and keeps stale drive commands out of the GATT pipeline. */
final class BleSerialWriteQueue {
  enum Type {
    EMERGENCY,
    STOP,
    MOTION,
    HEARTBEAT,
    QUERY
  }

  interface Sender {
    void send(String payload);
  }

  interface CriticalFailureListener {
    void onCriticalWriteFailure(String payload);
  }

  interface EventListener {
    void onQueueEvent(String event, Type type, String payload, long generation, int pendingCount);
  }

  private static final class Entry {
    final Type type;
    final String payload;
    final long generation;
    int retries;

    Entry(Type type, String payload, long generation) {
      this.type = type;
      this.payload = payload;
      this.generation = generation;
    }

    boolean isCritical() {
      return type == Type.EMERGENCY || type == Type.STOP;
    }
  }

  private final Deque<Entry> pending = new ArrayDeque<>();
  private final Sender sender;
  private final CriticalFailureListener failureListener;
  private final EventListener eventListener;
  private Entry inFlight;
  private String status = "idle";

  BleSerialWriteQueue(Sender sender, CriticalFailureListener failureListener) {
    this(sender, failureListener, (event, type, payload, generation, pendingCount) -> {});
  }

  BleSerialWriteQueue(
      Sender sender, CriticalFailureListener failureListener, EventListener eventListener) {
    this.sender = sender;
    this.failureListener = failureListener;
    this.eventListener = eventListener;
  }

  synchronized void enqueue(Type type, String payload, long generation) {
    if (type == Type.EMERGENCY) {
      pending.clear();
      pending.addFirst(new Entry(type, payload, generation));
    } else if (type == Type.STOP) {
      removePendingDriveCommands();
      pending.addFirst(new Entry(type, payload, generation));
    } else if (type == Type.MOTION) {
      removePending(Type.MOTION);
      addMotionAfterPendingStop(new Entry(type, payload, generation));
    } else {
      pending.addLast(new Entry(type, payload, generation));
    }
    eventListener.onQueueEvent("enqueue", type, payload, generation, pending.size());
    dispatchNext();
  }

  synchronized void enqueueTransition(String stopPayload, String motionPayload, long generation) {
    removePendingDriveCommands();
    // addFirst in reverse order keeps the pair contiguous ahead of heartbeat/query traffic.
    pending.addFirst(new Entry(Type.MOTION, motionPayload, generation));
    pending.addFirst(new Entry(Type.STOP, stopPayload, generation));
    eventListener.onQueueEvent(
        "transition", Type.MOTION, motionPayload, generation, pending.size());
    dispatchNext();
  }

  synchronized void onWriteComplete(boolean success) {
    if (inFlight == null) return;
    Entry completed = inFlight;
    if (!success && completed.isCritical() && completed.retries == 0) {
      completed.retries++;
      status = "retry:" + summarize(completed.payload);
      eventListener.onQueueEvent(
          "retry", completed.type, completed.payload, completed.generation, pending.size());
      sender.send(completed.payload);
      return;
    }

    inFlight = null;
    status = (success ? "ok:" : "failed:") + summarize(completed.payload);
    eventListener.onQueueEvent(
        success ? "success" : "failure",
        completed.type,
        completed.payload,
        completed.generation,
        pending.size());
    if (!success && completed.isCritical()) {
      pending.clear();
      failureListener.onCriticalWriteFailure(completed.payload);
      return;
    }
    dispatchNext();
  }

  synchronized void clear() {
    pending.clear();
    inFlight = null;
    status = "cleared";
    eventListener.onQueueEvent("clear", null, "", 0L, 0);
  }

  synchronized String getStatus() {
    return status;
  }

  synchronized int getPendingCount() {
    return pending.size();
  }

  synchronized boolean hasInFlight() {
    return inFlight != null;
  }

  private void dispatchNext() {
    if (inFlight != null || pending.isEmpty()) return;
    inFlight = pending.removeFirst();
    status = "writing:" + summarize(inFlight.payload);
    eventListener.onQueueEvent(
        "dispatch", inFlight.type, inFlight.payload, inFlight.generation, pending.size());
    sender.send(inFlight.payload);
  }

  private void removePendingDriveCommands() {
    removePending(Type.STOP);
    removePending(Type.MOTION);
  }

  private void addMotionAfterPendingStop(Entry motion) {
    if (pending.isEmpty()) {
      pending.addLast(motion);
      return;
    }
    Deque<Entry> reordered = new ArrayDeque<>();
    boolean inserted = false;
    while (!pending.isEmpty()) {
      Entry entry = pending.removeFirst();
      reordered.addLast(entry);
      if (!inserted && entry.type == Type.STOP) {
        reordered.addLast(motion);
        inserted = true;
      }
    }
    if (!inserted) reordered.addLast(motion);
    pending.addAll(reordered);
  }

  private void removePending(Type type) {
    Iterator<Entry> iterator = pending.iterator();
    while (iterator.hasNext()) {
      Entry removed = iterator.next();
      if (removed.type == type) {
        iterator.remove();
        eventListener.onQueueEvent(
            "replaced", removed.type, removed.payload, removed.generation, pending.size());
      }
    }
  }

  private static String summarize(String payload) {
    return payload == null ? "null" : payload.trim();
  }
}
