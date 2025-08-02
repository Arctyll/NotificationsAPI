package com.arctyll.notificationsapi.util;

public class TimeUtils {

    /**
     * Get progress of a notification (0 to 1) based on elapsed time.
     *
     * @param startTime When the notification was created (in ms).
     * @param duration  How long the notification should last (in ms).
     * @return A float between 0 and 1 representing life progress.
     */
    public static float getProgress(long startTime, long duration) {
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        return clamp((float) elapsed / duration, 0f, 1f);
    }

    /**
     * Applies a smooth in-out easing (sinusoidal).
     * Good for alpha/opacity or slide-in/out transitions.
     *
     * @param t Normalized time (0 to 1)
     * @return Eased value (0 to 1)
     */
    public static float easeInOut(float t) {
        return (float) (-0.5f * (Math.cos(Math.PI * t) - 1f));
    }

    /**
     * Fade in for the first X ms, fade out for the last X ms.
     *
     * @param startTime When the notification was created (in ms).
     * @param duration  Total duration of the notification (in ms).
     * @param fadeTime  Time for fade in/out (in ms).
     * @return Alpha (0 to 1) that can be used for opacity.
     */
    public static float getAlpha(long startTime, long duration, long fadeTime) {
        long now = System.currentTimeMillis();
        long timeLeft = (startTime + duration) - now;
        long timeElapsed = now - startTime;

        if (timeElapsed < fadeTime) {
            return clamp((float) timeElapsed / fadeTime, 0f, 1f);
        } else if (timeLeft < fadeTime) {
            return clamp((float) timeLeft / fadeTime, 0f, 1f);
        } else {
            return 1f;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
