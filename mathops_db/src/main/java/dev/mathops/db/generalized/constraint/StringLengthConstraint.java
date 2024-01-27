package dev.mathops.db.generalized.constraint;

import dev.mathops.commons.builder.SimpleBuilder;

/**
 * A field constraint for String fields that specifies a minimum and maximum length, in characters.
 */
public final class StringLengthConstraint extends AbstractFieldConstraint<String> {

    /** The minimum length allowed. */
    private final int minLength;

    /** The maximum length allowed. */
    private final int maxLength;

    /**
     * Constructs a new {@code StringLengthConstraint}.
     *
     * @param theName the constraint name (must be unique within a field)
     * @param theMinLength the minimum length allowed
     * @param theMaxLength the maximum length allowed
     */
    public StringLengthConstraint(final String theName, final int theMinLength, final int theMaxLength) {

        super(theName);

        if (theMinLength < 0) {
            throw new IllegalArgumentException("Minimum length may not be negative");
        }
        if (theMaxLength < theMinLength) {
            throw new IllegalArgumentException("Maximum length may not be less than minimum length");
        }

        this.minLength = theMinLength;
        this.maxLength = theMaxLength;
    }

    /**
     * Gets the minimum length allowed.
     *
     * @return the minimum length
     */
    public int getMinLength() {

        return this.minLength;
    }

    /**
     * Gets the maximum length allowed.
     *
     * @return the maximum length
     */
    public int getMaxLength() {

        return this.maxLength;
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

        final int len = value.length();

        return len >= this.minLength && len <= this.maxLength;
    }

    /**
     * Generates a diagnostic string representation of the constraint.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String minLengthStr = Integer.toString(this.minLength);
        final String maxLengthStr = Integer.toString(this.maxLength);

        return SimpleBuilder.concat("StringLengthConstraint{minLength=", minLengthStr,
                ",maxLength=", maxLengthStr, "}");
    }
}
