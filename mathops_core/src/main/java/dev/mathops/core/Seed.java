package dev.mathops.core;

/**
 * A seed for the random number generator. A seed consists of 8 32-bit integers.
 */
final class Seed {

    /** Seed values. */
    private final int[] seedArray;

    /**
     * Constructs a new {@code Seed}, whose seed is {right1, left1, right2, left2, ~right1, ~left1, ~right2, ~left2},
     * where {@code left1} is the low 32 bits of {@code theSeed1}, and {@code right1} is the upper 32 bits, and
     * {@code left2} is the low 32 bits of {@code theSeed2}, and {@code right2} is the upper 32 bits.
     *
     * @param theSeed1 the first {@code long} seed value
     * @param theSeed2 the second {@code long} seed value
     */
    Seed(final long theSeed1, final long theSeed2) {

        final int left1 = (int) theSeed1;
        final int right1 = (int) (theSeed1 >> Integer.SIZE);
        final int left2 = (int) theSeed2;
        final int right2 = (int) (theSeed2 >> Integer.SIZE);

        this.seedArray = new int[]{right1, left1, right2, left2, ~right1, ~left1, ~right2, ~left2,};
    }

    /**
     * Retrieves the seed into an 8-integer array.
     *
     * @param target the target array to populate with the seed values
     */
    void getSeedArray(final int[] target) {

        System.arraycopy(this.seedArray, 0, target, 0, 8);
    }

    /**
     * Gets the seed integer value at a particular index.
     *
     * @param index the index (from 0 to 7, inclusive)
     * @return the seed integer value
     */
    int getSeed(final int index) {

        return this.seedArray[index];
    }
}
