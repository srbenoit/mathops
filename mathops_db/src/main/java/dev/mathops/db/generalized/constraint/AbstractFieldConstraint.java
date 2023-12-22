package dev.mathops.db.generalized.constraint;

/**
 * The base class for constraints that can be applied to a field.
 *
 * @param <T> the value type
 */
public abstract class AbstractFieldConstraint<T> {

    /**
     * Constructs a new {@code AbstractFieldConstraint}.
     */
    protected AbstractFieldConstraint() {

        // No action
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
