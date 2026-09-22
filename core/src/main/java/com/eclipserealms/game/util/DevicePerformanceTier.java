package com.eclipserealms.game.util;

/**
 * Central place that decides how "heavy" the game is allowed to be.
 *
 * The old approach (fixed high-quality settings for every device) is what
 * causes lag on a Snapdragon 680 phone or a 3rd-gen i3 laptop. Instead every
 * subsystem (rendering, particles, AI update rate, draw distance) reads its
 * budget from here, and this class adapts at runtime by watching real frame
 * times - so a weak device automatically settles into something smooth
 * instead of everyone being tuned for a flagship phone.
 */
public final class DevicePerformanceTier {

    public enum Tier { LOW, MEDIUM, HIGH }

    private static Tier currentTier = Tier.MEDIUM;
    private static boolean locked = false; // true once a human/manual override is set

    private static float smoothedFrameMs = 16.6f;
    private static final float SAMPLE_WEIGHT = 0.08f;
    private static float timeSinceLastCheck = 0f;
    private static final float CHECK_INTERVAL_SEC = 3f;

    private DevicePerformanceTier() {}

    public static void forceTier(Tier tier) {
        if (tier == null) return;
        currentTier = tier;
    }

    public static void lockTier(Tier tier) {
        forceTier(tier);
        locked = true;
    }

    public static Tier getTier() {
        return currentTier;
    }

    /** Call once per frame with the real (unclamped) delta time in seconds. */
    public static void update(float rawDeltaSeconds) {
        if (locked) return;

        float frameMs = rawDeltaSeconds * 1000f;
        smoothedFrameMs = smoothedFrameMs + (frameMs - smoothedFrameMs) * SAMPLE_WEIGHT;

        timeSinceLastCheck += rawDeltaSeconds;
        if (timeSinceLastCheck < CHECK_INTERVAL_SEC) return;
        timeSinceLastCheck = 0f;

        // Budgets: ~16.6ms = 60fps, ~22ms = 45fps, ~33ms = 30fps.
        if (smoothedFrameMs > 26f && currentTier != Tier.LOW) {
            currentTier = currentTier == Tier.HIGH ? Tier.MEDIUM : Tier.LOW;
        } else if (smoothedFrameMs < 14f && currentTier != Tier.HIGH) {
            currentTier = currentTier == Tier.LOW ? Tier.MEDIUM : Tier.HIGH;
        }
    }

    public static int maxVisibleEnemies() {
        switch (currentTier) {
            case LOW: return 6;
            case MEDIUM: return 10;
            default: return 16;
        }
    }

    public static int maxActiveParticles() {
        switch (currentTier) {
            case LOW: return 40;
            case MEDIUM: return 120;
            default: return 300;
        }
    }

    public static float drawDistance() {
        switch (currentTier) {
            case LOW: return 35f;
            case MEDIUM: return 55f;
            default: return 90f;
        }
    }

    public static boolean shadowsEnabled() {
        return currentTier == Tier.HIGH;
    }

    public static float resolutionScale() {
        switch (currentTier) {
            case LOW: return 0.70f;
            case MEDIUM: return 0.85f;
            default: return 1.0f;
        }
    }

    public static float logicTickHz() {
        switch (currentTier) {
            case LOW: return 12f;
            case MEDIUM: return 20f;
            default: return 30f;
        }
    }

    public static int targetFps() {
        switch (currentTier) {
            case LOW: return 30;
            case MEDIUM: return 45;
            default: return 60;
        }
    }
}
