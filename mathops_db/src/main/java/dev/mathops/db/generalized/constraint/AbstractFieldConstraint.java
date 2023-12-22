package dev.mathops.db.generalized.constraint;

import dev.mathops.db.generalized.Field;

/**
 * The base class for constraints that can be applied to a field.
 *
 * @param <T> the value type
 */
public abstract class AbstractFieldConstraint<T> {

    /** The field to which the constraint applies. */
    private final Field field;

    /**
     * Constructs a new {@code AbstractFieldConstraint}.
     *
     * @param theField the field to which the constraint applies
     */
    protected AbstractFieldConstraint(final Field theField) {

        if (theField == null) {
            throw new IllegalArgumentException("Field may not be null");
        }

        this.field = theField;
    }

    /**
     * Gets the field to which this constraint applies.
     *
     * @return the field
     */
    public final Field getField() {

        return this.field;
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public abstract boolean isValidValue(Object value);

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public abstract boolean isValid(T value);

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public abstract String toString();
}
