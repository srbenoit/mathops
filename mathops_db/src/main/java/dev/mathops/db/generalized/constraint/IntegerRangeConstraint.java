package dev.mathops.db.generalized.constraint;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.generalized.Field;

/**
 * A field constraint for Integer fields that specifies a minimum and maximum value.
 */
public final class IntegerRangeConstraint extends AbstractFieldConstraint<Integer> {

    /** The minimum value allowed. */
    private final int minValue;

    /** The maximum value allowed. */
    private final int maxValue;

    /**
     * Constructs a new {@code IntegerRangeConstraint}.
     *
     * @param theName the constraint name (must be unique within a field)
     * @param theMinValue the minimum value allowed
     * @param theMaxValue the maximum value allowed
     */
    public IntegerRangeConstraint(final String theName, final int theMinValue, final int theMaxValue) {

        super(theName);

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
    public int getMinValue() {

        return this.minValue;
    }

    /**
     * Gets the maximum value allowed.
     *
     * @return the maximum value
     */
    public int getMaxValue() {

        return this.maxValue;
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValidValue(final Object value) {

        return (value instanceof final Integer typed) && isValid(typed);
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValid(final Integer value) {

        final int i = value.intValue();

        return i >= this.minValue && i <= this.maxValue;
    }

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String minValueStr = Integer.toString(this.minValue);
        final String maxValueStr = Integer.toString(this.maxValue);

        return SimpleBuilder.concat("IntegerRangeConstraint{minValue=", minValueStr, ",maxValue=", maxValueStr, "}");
    }
}
