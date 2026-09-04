package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.SystemClock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector.Recognition;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SimulatorAutomaticRecoveryIntegrationTest {
  @Test
  public void longLostNewTrackResumesFromParkedWaitWithoutAnotherCountdown() {
    try (Flow flow = new Flow()) {
      flow.startFollowing();
      int oldTrack = flow.tracks.getLockedTrackId();
      flow.parkAfterLongLoss();
      assertNull(flow.tracks.getTrackById(oldTrack));

      long started = SystemClock.elapsedRealtime();
      for (int i = 1; i <= 5; i++) {
        Recognition returned = remote("returned");
        Step step = flow.step(300, returned);
        assertNotEquals(oldTrack, step.identity.trackId);
        assertEquals(SimulatorIdentityGuard.RecoveryType.GLOBAL, step.permit.recoveryType);
        assertEquals(i, step.permit.freshMatches);
        assertFalse(step.permit.needsConfirmation);
        assertEquals(i == 5, step.permit.authorized);
        assertNotEquals(FollowState.READY_TO_FOLLOW, step.frame.state);
        assertNotEquals(SimulatorAutoDriveController.Phase.COUNTDOWN, step.drive.phase);
        assertEquals(-1, step.frame.countdownSec);
        if (i < 5) {
          assertEquals(oldTrack, flow.tracks.getLockedTrackId());
          assertStopped(step);
        } else {
          assertEquals(1200L, step.permit.recoverySpanMs);
          assertEquals(step.identity.trackId, flow.tracks.getLockedTrackId());
          assertEquals(FollowState.FOLLOW_CAUTION, step.frame.state);
          assertSame(returned, step.frame.target);
          assertPivoting(step);
        }
      }
      assertTrue(SystemClock.elapsedRealtime() - started < 3000L);
      Step next = flow.step(300, remote("returned"));
      assertPivoting(next);
      assertEquals(SimulatorIdentityGuard.RecoveryType.LOCAL, next.permit.recoveryType);
      assertEquals(3000L, flow.machine.COUNTDOWN_MS);
    }
  }

  @Test
  public void knownDistractorNeverAuthorizesEvenWithFreshPerfectGlobalScores() {
    try (Flow flow = new Flow()) {
      flow.startFollowing();
      int original = flow.tracks.getLockedTrackId();
      Recognition distractor = remote("distractor");
      flow.colors.put("distractor", Color.BLUE);
      Step crowd = null;
      for (int i = 0; i < 3; i++) crowd = flow.step(300, movingTarget(), distractor);
      int excluded = flow.tracks.getTrackForRecognition(distractor).trackId;
      assertTrue(crowd.permit.motionAllowed);
      assertTrue(flow.guard.isDistractor(excluded));

      // A deliberately adversarial extractor match must not defeat the remembered exclusion.
      flow.colors.put("distractor", Color.RED);
      RectF lastAuthorizedBox = flow.machine.getMemory().getLastBbox();
      for (int i = 0; i < 24; i++) {
        Step step = flow.step(300, remote("distractor"));
        assertEquals(excluded, step.identity.trackId);
        assertNotNull(step.globalScore);
        assertTrue(step.globalScore.fresh);
        assertEquals(1f, step.globalScore.bestScore, .00001f);
        assertEquals("known_distractor", step.permit.reason);
        assertFalse(step.permit.authorized);
        assertEquals(0, step.permit.freshMatches);
        assertEquals(original, flow.tracks.getLockedTrackId());
        assertEquals(lastAuthorizedBox, flow.machine.getMemory().getLastBbox());
        assertStopped(step);
      }
      assertNull(flow.tracks.getTrackById(original));
    }
  }

  @Test
  public void detectorIdAliasCannotReuseAnOldScoredTrackOrOverwriteTheCurrentCandidate() {
    try (Flow flow = new Flow()) {
      flow.startFollowing();
      int original = flow.tracks.getLockedTrackId();
      Step previous = flow.step(300, movingTarget());
      ReIDMatchResult oldScore = previous.globalScore;
      Recognition oldRecognition = previous.identity.bestCandidate;

      // The detector's id is not a track identity. Move the same id outside association gates.
      Recognition alias = remote(oldRecognition.getId());
      Step current = flow.step(100, alias);
      int replacement = flow.tracks.getTrackForRecognition(alias).trackId;
      assertNotEquals(original, replacement);
      assertNull(flow.tracks.getTrackForRecognition(oldRecognition));
      assertFalse(flow.tracks.getTrackById(original).isVisible());
      assertSame(alias, current.raw.bestCandidate);
      assertSame(alias, current.identity.bestCandidate);
      assertSame(alias, flow.coordinator.getLastBestCandidate());
      assertTrue(current.globalScore.fresh);
      assertTrue(current.globalScore.observationId > oldScore.observationId);
      assertTrue(current.globalScore.isBoundToTrack(replacement));
      assertEquals(1, current.permit.freshMatches);
      assertStopped(current);

      IdentityEvidence wrongBinding =
          current.belief.forSimulatorCandidate(
              flow.tracks.getTrackById(replacement),
              original,
              oldScore,
              flow.coordinator.getLastBboxEvidence(),
              current.belief.targetBelief);
      assertFalse(wrongBinding.matched);
      assertEquals(0f, wrongBinding.score, 0f);
      assertSame(alias, wrongBinding.bestCandidate);
      assertSame(alias, current.identity.bestCandidate);
      assertTrue(current.identity.reidMatch.isBoundToTrack(replacement));

      Step cached = flow.step(100, remote(oldRecognition.getId()));
      assertFalse(cached.globalScore.fresh);
      assertEquals(current.globalScore.observationId, cached.globalScore.observationId);
      assertSame(cached.raw.bestCandidate, cached.identity.bestCandidate);
      assertNotSame(alias, cached.identity.bestCandidate);
      assertEquals(1, cached.permit.freshMatches);
      assertStopped(cached);
    }
  }

  @Test
  public void visibleLockedCandidateRetainsItsOwnLowScoreInsteadOfBorrowingAnotherPersonsScore() {
    try (Flow flow = new Flow()) {
      flow.startFollowing();
      int locked = flow.tracks.getLockedTrackId();
      Recognition lockedBox = movingTarget();
      Recognition scoredBox = remote("scored");
      flow.colors.put(lockedBox.getId(), Color.BLUE);
      Step step = flow.step(300, lockedBox, scoredBox);
      int otherTrack = flow.tracks.getTrackForRecognition(scoredBox).trackId;
      assertSame(lockedBox, step.raw.bestCandidate);
      assertEquals(0f, step.raw.reidMatch.bestScore, .00001f);
      assertEquals(1f, flow.coordinator.getGlobalScoredTrack(otherTrack).bestScore, .00001f);
      assertSame(lockedBox, step.belief.bestCandidate);
      assertEquals(locked, step.belief.trackId);
      assertTrue(step.belief.reidMatch.isBoundToTrack(locked));
      assertSame(lockedBox, step.identity.bestCandidate);
      assertEquals(locked, step.identity.trackId);
      assertTrue(step.identity.reidMatch.isBoundToTrack(step.identity.trackId));
      assertEquals(0f, step.identity.score, .00001f);
      assertFalse(step.identity.matched);
      assertFalse(step.permit.authorized);
      assertEquals("multi_person_check", step.permit.reason);
      assertTrue(step.drive.left > 0 && step.drive.right > 0);
      assertFalse(step.permit.samplingAllowed);
      Step conflict = flow.step(600, movingTarget(), remote("scored"));
      assertEquals("identity_conflict", conflict.permit.reason);
      assertStopped(conflict);
      assertEquals(locked, flow.tracks.getLockedTrackId());
      assertSame(lockedBox, step.belief.bestCandidate);
    }
  }

  private static void assertStopped(Step step) {
    assertEquals(0, step.drive.left);
    assertEquals(0, step.drive.right);
    assertFalse(step.drive.lockout);
  }

  private static void assertMoving(Step step) {
    assertTrue(step.permit.motionAllowed);
    assertEquals(DistanceState.TOO_FAR, step.frame.distanceEstimate.state);
    assertEquals(SimulatorAutoDriveController.Phase.FOLLOW, step.drive.phase);
    assertTrue(step.drive.left > 0 && step.drive.right > 0);
    assertFalse(step.drive.lockout);
  }

  private static void assertPivoting(Step step) {
    assertTrue(step.permit.motionAllowed);
    assertEquals(SimulatorAutoDriveController.Phase.PIVOT, step.drive.phase);
    assertTrue(step.drive.left != 0 || step.drive.right != 0);
    assertEquals(-step.drive.left, step.drive.right);
    assertFalse(step.drive.lockout);
  }

  private static Recognition initialTarget() {
    return person("target", new RectF(160, 80, 240, 320));
  }

  static Recognition movingTarget() {
    return person("target", new RectF(168, 104, 232, 296));
  }

  static Recognition remote(String id) {
    return person(id, new RectF(340, 320, 396, 388));
  }

  private static Recognition person(String id, RectF box) {
    return new Recognition(id, "person", .95f, box, 0);
  }

  static final class Step {
    final IdentityEvidence raw;
    final IdentityEvidence belief;
    final IdentityEvidence identity;
    final ReIDMatchResult globalScore;
    final SimulatorIdentityGuard.Decision permit;
    final FollowStateMachine.FrameResult frame;
    final SimulatorAutoDriveController.Result drive;

    Step(
        IdentityEvidence raw,
        IdentityEvidence belief,
        IdentityEvidence identity,
        ReIDMatchResult globalScore,
        SimulatorIdentityGuard.Decision permit,
        FollowStateMachine.FrameResult frame,
        SimulatorAutoDriveController.Result drive) {
      this.raw = raw;
      this.belief = belief;
      this.identity = identity;
      this.globalScore = globalScore;
      this.permit = permit;
      this.frame = frame;
      this.drive = drive;
    }
  }

  /** Mirrors Base's synchronous decision flow; only model inference is deterministic test input. */
  static final class Flow implements AutoCloseable {
    static final int W = 400;
    static final int H = 400;
    Bitmap image = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
    final Map<String, Integer> colors = new HashMap<>();
    float targetSimilarity = 1f;
    final ReIDCoordinator coordinator =
        new ReIDCoordinator(
            crop -> {
              int pixel = crop.getPixel(crop.getWidth() / 2, crop.getHeight() / 2);
              return Color.red(pixel) > Color.blue(pixel)
                  ? new float[] {
                    targetSimilarity, (float) Math.sqrt(1f - targetSimilarity * targetSimilarity)
                  }
                  : new float[] {0f, 1f};
            });
    final TargetTrackManager tracks = new TargetTrackManager();
    final IdentityBeliefAccumulator beliefs = new IdentityBeliefAccumulator();
    final TargetMatcher matcher = new TargetMatcher();
    final FollowStateMachine machine = new FollowStateMachine(matcher, new ControlGenerator());
    final SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    final SimulatorContinuityTracker continuity = new SimulatorContinuityTracker();
    final SimulatorAutoDriveController driver = new SimulatorAutoDriveController();
    final SteeringDemandEstimator steering = new SteeringDemandEstimator();
    final ActionArbitrator arbitrator = new ActionArbitrator();
    final Bitmap confirmedSnapshot;
    final int[] snapshotPixels;
    final ImageSetpointDistanceEstimator.Setpoint baseline;
    final float confirmedArea;
    final float[] upperHist;
    final float[] lowerHist;
    long sequence;

    Flow() {
      this(false);
    }

    Flow(boolean dynamic) {
      ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
      tracks.setGlobalAssociationEnabled(true);
      beliefs.setStrictReidProvenance(true);
      coordinator.setEnhancedRecovery(true);
      coordinator.setGalleryMode(
          dynamic ? GalleryUpdateStatus.Mode.ADAPTIVE : GalleryUpdateStatus.Mode.STATIC);
      coordinator.setRecentEnabled(dynamic);
      machine.setSimulatorFastRecoveryEnabled(true);
      machine.CAPTURE_FRAMES = 1;
      machine.REACQUIRE_MATCH_N = 1;
      guard.begin(1);
      Recognition target = initialTarget();
      paint(Collections.singletonList(target));
      tracks.update(Collections.singletonList(target), W, H, SystemClock.elapsedRealtime());
      int locked = tracks.getTrackForRecognition(target).trackId;
      assertTrue(tracks.lockTrack(locked, "user_confirmed"));
      beliefs.lockTrack(locked);
      for (int i = 0; i < 12; i++) coordinator.collectInitializationCandidate(image, target, 0);
      coordinator.confirmGallery();
      assertEquals(8, coordinator.getGallerySize());
      machine.startCapture();
      FollowStateMachine.FrameResult captured =
          machine.onFrame(Collections.singletonList(target), image, W, H, 0);
      assertEquals(FollowState.LOCKED_PENDING_CONFIRM, captured.state);
      confirmedSnapshot = captured.snapshot;
      assertNotSame(image, confirmedSnapshot);
      snapshotPixels = pixels(confirmedSnapshot);
      confirmedArea = machine.getMemory().getConfirmedArea();
      upperHist = machine.getMemory().getUpperColorHist().clone();
      lowerHist = machine.getMemory().getLowerColorHist().clone();
      machine.confirm();
      for (int i = 0; i < 15; i++)
        machine.getMemory().offerDistanceCalibrationSample(target.getLocation(), W, H, 0, i * 50L);
      assertEquals(
          FollowState.CONFIRMED_ARMED,
          machine.onFrame(Collections.singletonList(target), image, W, H, 0).state);
      baseline = machine.getMemory().getDistanceSetpoint();
    }

    void startFollowing() {
      long countdownStarted = -1L;
      boolean followed = false;
      for (int i = 0; i < 24; i++) {
        Step step = step(300, initialTarget());
        if (step.frame.state == FollowState.READY_TO_FOLLOW && countdownStarted < 0L) {
          countdownStarted = SystemClock.elapsedRealtime();
          // The production countdown uses wall time, while frame provenance uses elapsed time.
          ShadowSystemClock.advanceBy(Duration.ofMillis(3000));
          ReflectionHelpers.setField(machine, "stateEnterTime", System.currentTimeMillis() - 3000L);
        }
        if (step.frame.state == FollowState.FOLLOW) {
          followed = true;
          break;
        }
      }
      assertTrue("first start still requires the real countdown", countdownStarted >= 0L);
      assertTrue(followed && machine.hasFollowedInSession());
      assertTrue(SystemClock.elapsedRealtime() - countdownStarted >= 3000L);
      assertMoving(step(300, movingTarget()));
    }

    void parkAfterLongLoss() {
      Step step = null;
      for (int i = 0; i < 24; i++) {
        step = step(300);
        assertFalse(step.permit.authorized);
        assertStopped(step);
      }
      assertEquals(SimulatorAutoDriveController.Phase.PARKED_WAIT, step.drive.phase);
      assertTrue(step.drive.recoveryElapsedMs > 6000L);
      assertTrue(machine.hasFollowedInSession());
    }

    Step step(long elapsed, Recognition... detections) {
      ShadowSystemClock.advanceBy(Duration.ofMillis(elapsed));
      long now = SystemClock.elapsedRealtime();
      List<Recognition> persons = Arrays.asList(detections);
      paint(persons);
      List<Recognition> high = new java.util.ArrayList<>(), low = new java.util.ArrayList<>();
      for (Recognition person : persons) {
        if (person.getConfidence() >= .5f) high.add(person);
        else if (person.getConfidence() >= .25f) low.add(person);
      }
      TargetTrackManager.TwoStageUpdateResult tiers =
          tracks.updateWithLowConfidence(high, low, W, H, now);
      coordinator.setFrameContext(tracks, now, ++sequence);
      TargetMatcher.MatchResult legacy = matcher.match(persons, image, machine.getMemory(), W, H);
      FollowState before = machine.getState();
      IdentityEvidence raw =
          coordinator.evaluate(
              persons,
              image,
              machine.getMemory(),
              before,
              W,
              H,
              0,
              legacy.score,
              legacy.matched,
              legacy.best);
      IdentityEvidence belief =
          beliefs.update(
              raw,
              tracks,
              tracks.getTrackForRecognition(raw.bestCandidate),
              machine.getMemory(),
              W,
              H);
      IdentityEvidence identity = belief;
      TargetTrack locked = tracks.getLockedTrack();
      TargetTrack scored =
          locked != null && locked.isVisible() && guard.prefersContinuity(locked.trackId, now)
              ? locked
              : tracks.getTrackForRecognition(coordinator.getLastBestCandidate());
      if (scored != null) {
        identity =
            belief.forSimulatorCandidate(
                scored,
                tracks.getLockedTrackId(),
                coordinator.getScoredTrack(scored.trackId),
                coordinator.getLastBboxEvidence(),
                beliefs.getBeliefForTrack(scored));
      }
      TargetTrack candidate = tracks.getTrackForRecognition(identity.bestCandidate);
      boolean highTarget = high.contains(identity.bestCandidate);
      boolean local =
          SimulatorContinuityTracker.hasHistoricalLocalSupport(
              identity.bboxDefaultOk(),
              identity.predictionOk(),
              identity.trackId,
              tracks.getLockedTrackId(),
              tracks.isNearLockedGhost(candidate, W, H, now));
      SimulatorContinuityTracker.Evidence geometry =
          continuity.observe(
              1,
              identity.trackId,
              identity.bestCandidate == null ? null : identity.bestCandidate.getLocation(),
              sequence,
              now,
              now,
              W,
              H,
              candidate != null
                  && candidate.missedFrames == 0
                  && (highTarget || tiers.continuedLowConfidence.contains(candidate.recognition)),
              tracks.isLockedAssociationCompeting());
      if (candidate != null && geometry.observedGeometry != null) {
        local = geometry.reliable || "continuity_warming".equals(geometry.reason);
        identity =
            identity.forSimulatorCandidate(
                candidate,
                tracks.getLockedTrackId(),
                identity.reidMatch,
                geometry.observedGeometry,
                identity.targetBelief);
      }
      ReIDMatchResult global = coordinator.getGlobalScoredTrack(identity.trackId);
      guard.inspectCandidates(
          coordinator.getGlobalScores(), persons.size(), tracks.getLockedTrackId(), sequence, now);
      SimulatorIdentityGuard.Decision permit =
          guard.update(
              1,
              sequence,
              now,
              now,
              identity.trackId,
              tracks.getLockedTrackId(),
              highTarget,
              local,
              identity.reidMatch,
              persons.size(),
              tracks.isLockedAssociationCompeting(),
              candidate == null || candidate.missedFrames > 0,
              global,
              geometry,
              machine.hasFollowedInSession());
      coordinator.setAutomaticVerification(!permit.authorized && !permit.isContinuous());
      if (permit.motionAllowed
          && identity.reidMatch != null
          && identity.reidMatch.fresh
          && identity.reidMatch.bestScore >= .85f
          && identity.reidMatch.margin >= .08f) {
        for (Recognition other : persons) {
          TargetTrack otherTrack = tracks.getTrackForRecognition(other);
          if (otherTrack.trackId != permit.trackId
              && BaseCartFollowFragment.separatePersonBoxes(
                  identity.bestCandidate.getLocation(), other.getLocation())) {
            guard.rememberDistractor(otherTrack.trackId);
          }
        }
      }
      if (permit.authorized && permit.trackId != tracks.getLockedTrackId()) {
        assertTrue(tracks.lockTrack(permit.trackId, "simulator_fresh_authorization"));
        beliefs.lockTrack(permit.trackId);
        identity =
            identity.forSimulatorCandidate(
                candidate,
                permit.trackId,
                global,
                identity.bboxContinuity,
                beliefs.getBeliefForTrack(candidate));
        machine.acceptSimulatorRecovery(permit, identity.bestCandidate);
      }
      if (permit.authorized && (!local || before == FollowState.DIRECTED_REACQUIRE)) {
        machine.acceptSimulatorRecovery(permit, identity.bestCandidate);
      }
      FollowStateMachine.FrameResult frame =
          permit.authorized
              ? machine.onFrame(persons, image, W, H, 0, identity, legacy)
              : permit.retainTarget
                  ? machine.continuityFrame(persons, identity, permit, W, H, 0)
                  : machine.observationOnly(persons, identity);
      frame.simulatorIdentity = permit;
      frame.trackingDecision = permit.tracking;
      frame.detectionTierEvidence =
          new DetectionTierEvidence(
              .5f, .25f, low, tiers.continuedLowConfidence, low.contains(identity.bestCandidate));
      frame.frameSequence = sequence;
      frame.sessionGeneration = 1;
      frame.frameTiming = new FrameTimingEvidence(now, 0, 0, 0, 0, 0, 0, 0);
      if (frame.target != null) {
        frame.steeringEvidence =
            steering.update(frame.target.getLocation(), W, H, 0, identity.trackId, now, 400);
      }
      ImageSetpointDistanceEstimator.DistanceEstimate distance = frame.distanceEstimate;
      frame.behaviorDecision =
          arbitrator.decide(
              frame.state,
              identity,
              new DistanceEvidence(
                  distance == null ? DistanceState.UNKNOWN : distance.state,
                  distance == null ? 0f : distance.confidence,
                  "image_setpoint"),
              new TraversabilityEvidence(1f, 1f, 1f, false, "clear"),
              new SystemSafetyEvidence(false, true, true, "ready"),
              machine.getMemory(),
              W,
              permit);
      frame.galleryUpdateStatus =
          coordinator.updateSimulatorGallery(
              identity.bestCandidate,
              frame.state,
              frame.behaviorDecision,
              identity,
              persons.size(),
              W,
              H,
              0,
              now,
              permit,
              false,
              geometry.reliable);
      frame.recentGallery = coordinator.getRecentStatus(now);
      SimulatorAutoDriveController.Result drive = driver.update(frame, now);
      assertBaselineUnchanged();
      return new Step(raw, belief, identity, global, permit, frame, drive);
    }

    void assertBaselineUnchanged() {
      TargetMemory memory = machine.getMemory();
      assertFalse(memory.isEmpty());
      ImageSetpointDistanceEstimator.Setpoint current = memory.getDistanceSetpoint();
      assertNotNull(current);
      assertEquals(baseline.desiredHeightRatio, current.desiredHeightRatio, 0f);
      assertEquals(baseline.desiredAreaRatio, current.desiredAreaRatio, 0f);
      assertEquals(baseline.desiredBottomRatio, current.desiredBottomRatio, 0f);
      assertEquals(confirmedArea, memory.getConfirmedArea(), 0f);
      assertArrayEquals(upperHist, memory.getUpperColorHist(), 0f);
      assertArrayEquals(lowerHist, memory.getLowerColorHist(), 0f);
      Bitmap retained = machine.observationOnly(Collections.emptyList(), null).snapshot;
      assertSame(confirmedSnapshot, retained);
      assertFalse(retained.isRecycled());
      assertArrayEquals(snapshotPixels, pixels(retained));
    }

    void paint(List<Recognition> persons) {
      image.recycle();
      image = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
      image.eraseColor(Color.BLACK);
      // Legacy Robolectric Canvas draws are not rasterized into the source bitmap.
      for (Recognition person : persons) {
        RectF box = person.getLocation();
        int width = (int) box.width();
        int height = (int) box.height();
        int[] pixels = new int[width * height];
        Arrays.fill(pixels, colors.getOrDefault(person.getId(), Color.RED));
        image.setPixels(pixels, 0, width, (int) box.left, (int) box.top, width, height);
      }
    }

    @Override
    public void close() {
      coordinator.reset();
      image.recycle();
      assertBaselineUnchanged();
      confirmedSnapshot.recycle();
    }

    private static int[] pixels(Bitmap bitmap) {
      int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
      bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
      return pixels;
    }
  }
}
