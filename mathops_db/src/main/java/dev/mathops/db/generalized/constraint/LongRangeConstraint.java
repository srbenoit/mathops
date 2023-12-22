package dev.mathops.db.generalized.constraint;

import dev.mathops.core.builder.SimpleBuilder;

/**
 * A field constraint for Long fields that specifies a minimum and maximum value.
 */
public final class LongRangeConstraint extends AbstractFieldConstraint<Long> {

    /** The minimum value allowed. */
    private final long minValue;

    /** The maximum value allowed. */
    private final long maxValue;

    /**
     * Constructs a new {@code LongRangeConstraint}.
     *
     * @param theMinValue the minimum value allowed
     * @param theMaxValue the maximum value allowed
     */
    public LongRangeConstraint(final long theMinValue, final long theMaxValue) {

        super();

        if (theMaxValue < theMinValue) {
            throw new IllegalArgumentException("Maximum value may not be less than minimum value");
        }

        this.minValue = theMinValue;
        this.maxValue = theMaxValue;
    }

    /**
     * Gets the minimum value allowed.
     *
     * @return the minimum value
     */
    public long getMinValue() {

        return this.minValue;
    }

    /**
     * Gets the maximum value allowed.
     *
     * @return the maximum value
     */
    public long getMaxValue() {

        return this.maxValue;
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValidValue(final Object value) {

        return (value instanceof final Long typed) && isValid(typed);
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValid(final Long value) {

        final long l = value.longValue();

        return l >= this.minValue && l <= this.maxValue;
    }

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String minValueStr = Long.toString(this.minValue);
        final String maxValueStr = Long.toString(this.maxValue);

        return SimpleBuilder.concat("LongRangeConstraint{minValue=", minValueStr, ",maxValue=", maxValueStr, "}");
    }
}
