package dev.mathops.db.generalized.constraint;

import dev.mathops.commons.builder.SimpleBuilder;

/**
 * A field constraint for Byte fields that specifies a minimum and maximum value.
 */
public final class ByteRangeConstraint extends AbstractFieldConstraint<Byte> {

    /** The minimum value allowed. */
    private final byte minValue;

    /** The maximum value allowed. */
    private final byte maxValue;

    /**
     * Constructs a new {@code ByteRangeConstraint}.
     *
     * @param theName the constraint name (must be unique within a field)
     * @param theMinValue the minimum value allowed
     * @param theMaxValue the maximum value allowed
     */
    public ByteRangeConstraint(final String theName, final byte theMinValue, final byte theMaxValue) {

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
    public byte getMinValue() {

        return this.minValue;
    }

    /**
     * Gets the maximum value allowed.
     *
     * @return the maximum value
     */
    public byte getMaxValue() {

        return this.maxValue;
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValidValue(final Object value) {

        return (value instanceof final Byte typed) && isValid(typed);
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValid(final Byte value) {

        final byte b = value.byteValue();

        return b >= this.minValue && b <= this.maxValue;
    }

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String minValueStr = Byte.toString(this.minValue);
        final String maxValueStr = Byte.toString(this.maxValue);

        return SimpleBuilder.concat("ByteRangeConstraint{minValue=", minValueStr, ",maxValue=", maxValueStr, "}");
    }
}
