package dev.mathops.db.generalized.constraint;

import dev.mathops.commons.builder.SimpleBuilder;

/**
 * A field constraint for Float fields that specifies a minimum and maximum value.
 */
public final class FloatRangeConstraint extends AbstractFieldConstraint<Float> {

    /** The types of values allowed. */
    private final EFloatingPointAllow allowed;

    /** The minimum value allowed. */
    private final float minValue;

    /** The maximum value allowed. */
    private final float maxValue;

    /**
     * Constructs a new {@code FloatRangeConstraint}.
     *
     * @param theName the constraint name (must be unique within a field)
     * @param theAllowed  the types of values allowed
     * @param theMinValue the minimum value allowed
     * @param theMaxValue the maximum value allowed
     */
    public FloatRangeConstraint(final String theName, final EFloatingPointAllow theAllowed, final float theMinValue,
                                final float theMaxValue) {

        super(theName);

        if (theAllowed == null) {
            throw new IllegalArgumentException("Allowed values specification may not be null");
        }
        if (theMaxValue < theMinValue) {
            throw new IllegalArgumentException("Maximum value may not be less than minimum value");
        }

        this.allowed = theAllowed;
        this.minValue = theMinValue;
        this.maxValue = theMaxValue;
    }

    /**
     * Gets te types of values allowed.
     *
     * @return the types of values allowed
     */
    public EFloatingPointAllow getAllowed() {

        return this.allowed;
    }

    /**
     * Gets the minimum value allowed.
     *
     * @return the minimum value
     */
    public float getMinValue() {

        return this.minValue;
    }

    /**
     * Gets the maximum value allowed.
     *
     * @return the maximum value
     */
    public float getMaxValue() {

        return this.maxValue;
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValidValue(final Object value) {

        return (value instanceof final Float typed) && isValid(typed);
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValid(final Float value) {

        final float f = value.floatValue();

        boolean ok = true;

        switch (this.allowed) {
            case ALL_BUT_NAN -> ok = !Float.isNaN(f);
            case ALL_BUT_INFINITIES -> ok = !Float.isInfinite(f);
            case FINITE_ONLY -> ok = Float.isFinite(f);
        }

        return ok && f >= this.minValue && f <= this.maxValue;
    }

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String minValueStr = Float.toString(this.minValue);
        final String maxValueStr = Float.toString(this.maxValue);

        return SimpleBuilder.concat("FloatRangeConstraint{minValue=", minValueStr, ",maxValue=", maxValueStr, "}");
    }
}
