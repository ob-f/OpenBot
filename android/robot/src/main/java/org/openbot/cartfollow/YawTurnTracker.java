package org.openbot.cartfollow;

/** Integrates signed yaw around gravity; sensor health is independent of an active turn. */
public final class YawTurnTracker {
  private static final float RAD_TO_DEG = 57.29578f;
  private static final long FRESH_NS = 500_000_000L;

  public static final class Status {
    public final boolean sensorExists;
    public final boolean registered;
    public final boolean sampleFresh;
    public final boolean gravityAvailable;
    public final boolean available;
    public final long sampleAgeMs;

    private Status(boolean exists, boolean registered, boolean fresh, boolean gravity, long age) {
      this.sensorExists = exists;
      this.registered = registered;
      this.sampleFresh = fresh;
      this.gravityAvailable = gravity;
      this.available = exists && registered && fresh && gravity;
      this.sampleAgeMs = age;
    }
  }

  private float gravityX;
  private float gravityY;
  private float gravityZ;
  private boolean gravityAvailable;
  private long lastGravityReceiptNs = -1L;
  private long lastGravitySampleNs = -1L;
  private boolean sensorExists;
  private boolean registered;
  private long lastSampleNs = -1L;
  private long lastReceiptNs = -1L;
  private long integrationTimestampNs = -1L;
  private SteeringEvidence.Direction expectedDirection = SteeringEvidence.Direction.NONE;
  private float turnedDegrees;
  private boolean wrongDirection;

  public synchronized void setSensorStatus(boolean exists, boolean registered) {
    sensorExists = exists;
    this.registered = exists && registered;
    if (!this.registered) {
      lastSampleNs = -1L;
      lastReceiptNs = -1L;
      integrationTimestampNs = -1L;
    }
  }

  public synchronized void reset(SteeringEvidence.Direction direction) {
    expectedDirection = direction == null ? SteeringEvidence.Direction.NONE : direction;
    integrationTimestampNs = -1L;
    turnedDegrees = 0f;
    wrongDirection = false;
  }

  public synchronized void clear() {
    reset(SteeringEvidence.Direction.NONE);
    gravityAvailable = false;
    lastGravityReceiptNs = -1L;
    lastGravitySampleNs = -1L;
    setSensorStatus(false, false);
  }

  public synchronized void onGravity(float x, float y, float z) {
    onGravity(-1L, x, y, z);
  }

  public synchronized void onGravity(long timestampNs, float x, float y, float z) {
    float norm = (float) Math.sqrt(x * x + y * y + z * z);
    if (!Float.isFinite(norm) || norm < 0.1f) return;
    gravityX = x / norm;
    gravityY = y / norm;
    gravityZ = z / norm;
    gravityAvailable = true;
    lastGravityReceiptNs = System.nanoTime();
    lastGravitySampleNs = timestampNs;
  }

  public synchronized void onGyroscope(long timestampNs, float x, float y, float z) {
    if (!sensorExists
        || !registered
        || timestampNs < 0L
        || timestampNs <= lastSampleNs
        || !Float.isFinite(x)
        || !Float.isFinite(y)
        || !Float.isFinite(z)) return;
    lastSampleNs = timestampNs;
    lastReceiptNs = System.nanoTime();
    long previous = integrationTimestampNs;
    integrationTimestampNs = timestampNs;
    if (!gravityFresh() || expectedDirection == SteeringEvidence.Direction.NONE || previous < 0L)
      return;
    long deltaNs = timestampNs - previous;
    if (deltaNs <= 0L || deltaNs > FRESH_NS) return;
    float signedRate = x * gravityX + y * gravityY + z * gravityZ;
    if (Math.abs(signedRate) < 0.03f) {
      wrongDirection = false;
      return;
    }
    // Positive yaw projected onto Android's gravity axis is a left turn, not a right turn.
    float expectedSign = expectedDirection == SteeringEvidence.Direction.LEFT ? 1f : -1f;
    float directedRate = signedRate * expectedSign;
    turnedDegrees += directedRate * (deltaNs / 1_000_000_000f) * RAD_TO_DEG;
    wrongDirection = directedRate < 0f;
  }

  /** nowNs must use the SensorEvent timestamp time base (elapsedRealtimeNanos on Android). */
  public synchronized Status getStatus(long nowNs) {
    long age = lastSampleNs < 0L ? -1L : nowNs - lastSampleNs;
    boolean fresh = age >= 0L && age <= FRESH_NS;
    boolean gravityFresh =
        gravityFresh()
            && (lastGravitySampleNs < 0L
                || (nowNs >= lastGravitySampleNs && nowNs - lastGravitySampleNs <= FRESH_NS));
    return new Status(
        sensorExists, registered, fresh, gravityFresh, age < 0L ? -1L : age / 1_000_000L);
  }

  /** Receipt-clock freshness is usable by pure Java callers without Android clock dependencies. */
  public synchronized boolean isAvailable() {
    long age = lastReceiptNs < 0L ? -1L : System.nanoTime() - lastReceiptNs;
    return sensorExists && registered && gravityFresh() && age >= 0L && age <= FRESH_NS;
  }

  private boolean gravityFresh() {
    long age = lastGravityReceiptNs < 0L ? -1L : System.nanoTime() - lastGravityReceiptNs;
    return gravityAvailable && age >= 0L && age <= FRESH_NS;
  }

  public synchronized float getTurnedDegrees() {
    return turnedDegrees;
  }

  public synchronized boolean isWrongDirection() {
    return wrongDirection;
  }
}
