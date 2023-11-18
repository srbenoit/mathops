package dev.mathops.core;

/**
 * Pseudo-random number generator taking an 8-integer seed and generating a series of integer values.
 */
final class Rnd {

    /** The initial value of the carry field. */
    private static final int INITIAL_CARRY = 123456;

    /** Multiplier to apply on each step. */
    private static final long MULTIPLIER = 987651386L;

    /** Number of bits in an integer. */
    private static final int BITS_PER_INT = 32;

    /** All bits in an int except the sign bit. */
    private static final int INT_NONSIGN_BITS = 0x7FFFFFFF;

    /** Intermediate value 1. */
    private int val1;

    /** Intermediate value 2. */
    private int val2;

    /** Intermediate value 3. */
    private int val3;

    /** Intermediate value 4. */
    private int val4;

    /** Intermediate value 5. */
    private int val5;

    /** Intermediate value 6. */
    private int val6;

    /** Intermediate value 7. */
    private int val7;

    /** Intermediate value 8. */
    private int val8;

    /** Intermediate carry value. */
    private int carry;

    /** Storage of the original seed values. */
    private final int[] origSeed;

    /**
     * Constructs a new {@code Rnd} with a given seed.
     *
     * @param seed the seed
     */
    Rnd(final Seed seed) {

        this.origSeed = new int[8];
        seed.getSeedArray(this.origSeed);
        reset();
    }

    /**
     * Seeds the pseudo-random number generator with a given seed. This does not alter the original seed stored in the
     * generator. A {@code reset} call will restore the original seed.
     *
     * @param seed the seed
     */
    public void seed(final Seed seed) {

        this.val1 = seed.getSeed(1);
        this.val2 = seed.getSeed(2);
        this.val3 = seed.getSeed(3);
        this.val4 = seed.getSeed(4);
        this.val5 = seed.getSeed(5);
        this.val6 = seed.getSeed(6);
        this.val7 = seed.getSeed(7);
        this.val8 = seed.getSeed(8);
        this.carry = INITIAL_CARRY;
    }

    /**
     * Resets the generator based on its original seed values. After a reset, calling {@code next} any number of times
     * will generate the same pseudo-random sequence as when the object was initially created.
     */
    private void reset() {

        this.val1 = this.origSeed[0];
        this.val2 = this.origSeed[1];
        this.val3 = this.origSeed[2];
        this.val4 = this.origSeed[3];
        this.val5 = this.origSeed[4];
        this.val6 = this.origSeed[5];
        this.val7 = this.origSeed[6];
        this.val8 = this.origSeed[7];
        this.carry = INITIAL_CARRY;
    }

    /**
     * Generates the next pseudo-random integer value.
     *
     * @return the value
     */
    private int nextInt() {

        final long temp = MULTIPLIER * (long) this.val8 + (long) this.carry;

        this.val8 = this.val7;
        this.val7 = this.val6;
        this.val6 = this.val5;
        this.val5 = this.val4;
        this.val4 = this.val3;
        this.val3 = this.val2;
        this.val2 = this.val1;
        this.val1 = -(2 + (int) temp);
        this.carry = (int) (temp >> BITS_PER_INT);

        return this.val1;
    }

    /**
     * Generates the next pseudo-random integer value between 0 and some upper limit (may include 0 but not the upper
     * limit).
     *
     * @param upperLimit the upper limit
     * @return the value
     */
    public int nextInt(final int upperLimit) {

        if (upperLimit <= 0) {
            throw new IllegalArgumentException(Res.get(Res.RND_UPPER_LIMIT_NOT_POS));
        }

        return (nextInt() & INT_NONSIGN_BITS) % upperLimit;
    }
}
