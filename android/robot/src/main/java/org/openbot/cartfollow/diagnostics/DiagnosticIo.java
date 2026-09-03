package org.openbot.cartfollow.diagnostics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded queues; all disk access, flushing and finalization happen away from control callbacks.
 */
public final class DiagnosticIo {
  private final ArrayBlockingQueue<Runnable> text = new ArrayBlockingQueue<>(2048);
  private final ThreadPoolExecutor images =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(8),
          r -> new Thread(r, "cart-log-images"));
  private final ScheduledExecutorService writer =
      Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "cart-log-text"));
  private final Map<File, BufferedWriter> streams = new HashMap<>();
  private final CountDownLatch initialized = new CountDownLatch(1);
  private final CountDownLatch closed = new CountDownLatch(1);
  public final AtomicLong droppedImages = new AtomicLong(), droppedText = new AtomicLong();
  public volatile String error = "";
  private volatile boolean accepting = true;
  private volatile Runnable finalizer;
  private long lastFlush;

  public DiagnosticIo(Runnable initialize) {
    text.add(
        () -> {
          try {
            initialize.run();
          } finally {
            initialized.countDown();
          }
        });
  }

  public void start() {
    writer.scheduleWithFixedDelay(this::drain, 0, 100, TimeUnit.MILLISECONDS);
  }

  public synchronized boolean submit(Runnable task) {
    if (!accepting) return false;
    if (text.offer(task)) return true;
    droppedText.incrementAndGet();
    error = "文字队列已满，部分记录丢失";
    return false;
  }

  public boolean imageCapacity() {
    return accepting && images.getQueue().remainingCapacity() > 0;
  }

  public boolean image(Runnable task, Runnable discarded) {
    synchronized (this) {
      if (accepting)
        try {
          images.execute(
              () -> {
                try {
                  initialized.await();
                  task.run();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  discarded.run();
                } catch (RuntimeException e) {
                  fail(e);
                }
              });
          return true;
        } catch (RejectedExecutionException e) {
          droppedImages.incrementAndGet();
        }
    }
    discarded.run();
    return false;
  }

  /** Called only by the text worker, or by the finalizer on that same worker. */
  public void append(File file, String row) {
    try {
      BufferedWriter out = streams.get(file);
      if (out == null) {
        out =
            new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
        streams.put(file, out);
      }
      out.write(row);
    } catch (IOException e) {
      fail(e);
    }
  }

  public void fail(Exception e) {
    error = e.getClass().getSimpleName() + ": " + e.getMessage();
  }

  public synchronized void finish(Runnable summary) {
    if (!accepting) return;
    accepting = false;
    finalizer = summary;
    images.shutdown();
  }

  public boolean isClosed() {
    return closed.getCount() == 0;
  }

  public boolean awaitClosed(long millis) throws InterruptedException {
    return closed.await(millis, TimeUnit.MILLISECONDS);
  }

  private void drain() {
    try {
      for (int n = 0; n < 512; n++) {
        Runnable task = text.poll();
        if (task == null) break;
        try {
          task.run();
        } catch (RuntimeException e) {
          fail(e);
        }
      }
      long now = System.nanoTime() / 1000000;
      if (now - lastFlush >= 1000 || !accepting) {
        for (BufferedWriter out : streams.values())
          try {
            out.flush();
          } catch (IOException e) {
            fail(e);
          }
        lastFlush = now;
      }
      if (!accepting && text.isEmpty() && images.isTerminated()) {
        for (BufferedWriter out : streams.values())
          try {
            out.close();
          } catch (IOException e) {
            fail(e);
          }
        streams.clear();
        try {
          if (finalizer != null) finalizer.run();
        } catch (RuntimeException e) {
          fail(e);
        } finally {
          closed.countDown();
          writer.shutdown();
        }
      }
    } catch (RuntimeException e) {
      fail(e);
    }
  }
}
