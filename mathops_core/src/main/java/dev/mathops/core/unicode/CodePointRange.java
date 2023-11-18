package dev.mathops.core.unicode;

/**
 * An inclusive range of code points.
 */
public final class CodePointRange {

    /** The minimum code point included in the range. */
    public final int min;

    /** The maximum code point included in the range. */
    public final int max;

    /**
     * Constructs a new {@code CodePointRange}.
     *
     * @param theMin the minimum code point included in the range
     * @param theMax the maximum code point included in the range
     */
    public CodePointRange(final int theMin, final int theMax) {

        this.min = theMin;
        this.max = theMax;
    }

    /**
     * Tests whether a code point falls within the range.
     *
     * @param codePoint the code point
     * @return {@code true} if the code points falls within the range
     */
    public boolean isInRange(final int codePoint) {

        return codePoint >= this.min && codePoint <= this.max;
    }
}
