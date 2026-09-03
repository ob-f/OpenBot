package org.openbot.cartfollow;

/** App-side monotonic timing. Sensor timestamps are recorded, not mixed with this clock. */
public final class FrameTimingEvidence {
  public final long receivedAtMs;
  public final long sensorTimestampNs;
  public final long detectorMs;
  public final long reidMs;
  public final long pipelineMs;
  public final long sourceAgeMs;
  public final float completedFps;
  public final long droppedFrames;
  public final long copyMs,
      matchMs,
      initializationMs,
      decisionMs,
      logSubmitMs,
      completedAtMs,
      uiWaitMs;

  public FrameTimingEvidence(
      long receivedAtMs,
      long sensorTimestampNs,
      long detectorMs,
      long reidMs,
      long pipelineMs,
      long sourceAgeMs,
      float completedFps,
      long droppedFrames) {
    this(
        receivedAtMs,
        sensorTimestampNs,
        detectorMs,
        reidMs,
        pipelineMs,
        sourceAgeMs,
        completedFps,
        droppedFrames,
        0L,
        0L,
        0L,
        0L,
        -1L,
        -1L,
        -1L);
  }

  private FrameTimingEvidence(
      long receivedAtMs,
      long sensorTimestampNs,
      long detectorMs,
      long reidMs,
      long pipelineMs,
      long sourceAgeMs,
      float completedFps,
      long droppedFrames,
      long copyMs,
      long matchMs,
      long initializationMs,
      long decisionMs,
      long logSubmitMs,
      long completedAtMs,
      long uiWaitMs) {
    this.receivedAtMs = receivedAtMs;
    this.sensorTimestampNs = sensorTimestampNs;
    this.detectorMs = detectorMs;
    this.reidMs = reidMs;
    this.pipelineMs = pipelineMs;
    this.sourceAgeMs = sourceAgeMs;
    this.completedFps = completedFps;
    this.droppedFrames = droppedFrames;
    this.copyMs = copyMs;
    this.matchMs = matchMs;
    this.initializationMs = initializationMs;
    this.decisionMs = decisionMs;
    this.logSubmitMs = logSubmitMs;
    this.completedAtMs = completedAtMs;
    this.uiWaitMs = uiWaitMs;
  }

  public FrameTimingEvidence withStages(
      long copy, long match, long initialization, long decision, long logSubmit, long completedAt) {
    return new FrameTimingEvidence(
        receivedAtMs,
        sensorTimestampNs,
        detectorMs,
        reidMs,
        pipelineMs,
        sourceAgeMs,
        completedFps,
        droppedFrames,
        copy,
        match,
        initialization,
        decision,
        logSubmit,
        completedAt,
        uiWaitMs);
  }

  public FrameTimingEvidence presentedAt(long now) {
    return new FrameTimingEvidence(
        receivedAtMs,
        sensorTimestampNs,
        detectorMs,
        reidMs,
        pipelineMs,
        now - receivedAtMs,
        completedFps,
        droppedFrames,
        copyMs,
        matchMs,
        initializationMs,
        decisionMs,
        logSubmitMs,
        completedAtMs,
        completedAtMs < 0L ? -1L : now - completedAtMs);
  }
}
