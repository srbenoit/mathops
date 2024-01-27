package dev.mathops.db.generalized.constraint;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A field constraint for String fields that specifies an enumeration of allowed values.  If a string field has no
 * length constraint but has this constraint, the length of the longest allowed value in this constraint can be used as
 * an upper bound on length when creating the database field.
 */
public final class StringEnumeratedConstraint extends AbstractFieldConstraint<String> {

    /** The set of allowed values. */
    private final String[] allowedValues;

    /**
     * Constructs a new {@code StringEnumeratedConstraint}.
     *
     * @param theName the constraint name (must be unique within a field)
     * @param theAllowedValues the allowed values
     */
    public StringEnumeratedConstraint(final String theName, final String... theAllowedValues) {

        super(theName);

        if (theAllowedValues == null || theAllowedValues.length == 0) {
            throw new IllegalArgumentException("Allowed values array may not be null or empty");
        }

        for (final String test : theAllowedValues) {
            if (test == null) {
                throw new IllegalArgumentException("Allowed values array may not include a null value");
            }
        }

        this.allowedValues = theAllowedValues.clone();
    }

    /**
     * Gets the number of allowed values
     *
     * @return the number of allowed values
     */
    public int getNumAllowedValues() {

        return this.allowedValues.length;
    }

    /**
     * Gets a particular allowed value
     *
     * @param index the zero-based index of the value to retrieve
     * @return the allowed value
     */
    public String getAllowedValue(final int index) {

        return this.allowedValues[index];
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValidValue(final Object value) {

        return (value instanceof final String typed) && isValid(typed);
    }

    /**
     * Tests whether a field value is valid according to this constraint.
     *
     * @param value the value
     * @return {@code true} if valid; {@code false} if not
     */
    public boolean isValid(final String value) {

        boolean valid = false;

        for (final String test : this.allowedValues) {
            if (test.equals(value)) {
                valid = true;
                break;
            }
        }

        return valid;
    }

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("StringEnumeratedConstraint{allowedValues=[", this.allowedValues[0]);
        final int count = this.allowedValues.length;
        for (int i = 1; i < count; ++i) {
            builder.add(CoreConstants.COMMA_CHAR);
            builder.add(this.allowedValues[i]);
        }
        builder.add('}');

        return builder.toString();
    }
}
