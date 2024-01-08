package dev.mathops.db.generalized.constraint;

/**
 * The base class for constraints that can be applied to a field.
 *
 * @param <T> the value type
 */
public abstract class AbstractFieldConstraint<T> implements Comparable<AbstractFieldConstraint<?>> {

    /** The constraint name (must be unique within a field). */
    private final String name;

    /**
     * Constructs a new {@code AbstractFieldConstraint}.
     *
     * @param theName the constraint name (must be unique within a field)
     */
    protected AbstractFieldConstraint(final String theName) {

        if (theName == null) {
            throw new IllegalArgumentException("Constraint name may not be null");
        }

        this.name = theName;
    }

    /**
     * Gets the constraint name.
     *
     * @return the constraint name
     */
    public final String getName() {

        return this.name;
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

    /**
     * Generates a hash code for the object, which is based only on the constraint name.  It is intended that only
     * constraints within a single field would be compared for equality or sorted.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode() {

        return this.name.hashCode();
    }

    /**
     * Tests whether this object is equal to another.  To be equal, the other object must be an
     * {@code AbstractFieldConstraint} with the same name as this constraint.
     *
     * @return the hash code
     */
    @Override
    public final boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final AbstractFieldConstraint<?> objConstraint) {
            final String objName = objConstraint.getName();
            equal = this.name.equals(objName);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object
     */
    @Override
    public final int compareTo(final AbstractFieldConstraint<?> o) {

        final String oName = o.getName();
        return this.name.compareTo(oName);
    }
}
