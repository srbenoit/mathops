package dev.mathops.assessment;

import java.util.Random;

/**
 * A shared random number generation class that can be called statically by all classes. This avoids the overhead of
 * each class constructing its own random number generator.
 */
public enum Randomizer {
    ;

    /** A pseudo-random number generator. */
    private static final Random random;

    static {
        random = new Random(System.currentTimeMillis());
    }

    /**
     * Generates a random integer between 0 and n-1, inclusive.
     *
     * @param max a value that the number must fall below
     * @return the random value
     */
    public static int nextInt(final int max) {

        // Note: "Random" class is thread-safe.
        return random.nextInt(max);
    }
}
