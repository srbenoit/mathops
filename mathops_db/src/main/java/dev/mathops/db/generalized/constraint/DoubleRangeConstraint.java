package dev.mathops.db.generalized.constraint;

import dev.mathops.core.builder.SimpleBuilder;

/**
 * A field constraint for Double fields that specifies a minimum and maximum value.
 */
public final class DoubleRangeConstraint extends AbstractFieldConstraint<Double> {

    /** The types of values allowed. */
    private final EFloatingPointAllow allowed;

    /** The minimum value allowed. */
    private final double minValue;

    /** The maximum value allowed. */
    private final double maxValue;

    /**
     * Constructs a new {@code FloatRangeConstraint}.
     *
     * @param theAllowed  the types of values allowed
     * @param theMinValue the minimum value allowed
     * @param theMaxValue the maximum value allowed
     */
    public DoubleRangeConstraint(final EFloatingPointAllow theAllowed, final double theMinValue,
                                 final double theMaxValue) {

        super();

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
    public double getMinValue() {

        return this.minValue;
    }

    /**
     * Gets the maximum value allowed.
     *
     * @return the maximum value
     */
    public double getMaxValue() {

        return this.maxValue;
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValidValue(final Object value) {

        return (value instanceof final Double typed) && isValid(typed);
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValid(final Double value) {

        final double d = value.doubleValue();

        boolean ok = true;

        switch (this.allowed) {
            case ALL_BUT_NAN -> ok = !Double.isNaN(d);
            case ALL_BUT_INFINITIES -> ok = !Double.isInfinite(d);
            case FINITE_ONLY -> ok = Double.isFinite(d);
        }

        return ok && d >= this.minValue && d <= this.maxValue;
    }

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String minValueStr = Double.toString(this.minValue);
        final String maxValueStr = Double.toString(this.maxValue);

        return SimpleBuilder.concat("DoubleRangeConstraint{minValue=", minValueStr, ",maxValue=", maxValueStr, "}");
    }
}
