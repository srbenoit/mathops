package dev.mathops.db.generalized.constraint;

import dev.mathops.db.generalized.EFieldType;

/**
 * The base class for constraints that can be applied to a field.
 *
 * @param <T> the value type
 */
public abstract class AbstractFieldConstraint<T> {

    /** The field type. */
    private final EFieldType fieldType;

    /**
     * Constructs a new {@code AbstractFieldConstraint}.
     *
     * @param theFieldType the field type
     */
    protected AbstractFieldConstraint(final EFieldType theFieldType) {

        this.fieldType = theFieldType;
    }

    /**
     * Gets the field type to which this constraint applies.
     * @return the field type
     */
    public final EFieldType getFieldType() {

        return this.fieldType;
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
}
