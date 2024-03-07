package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An expression object that represents a single decimal digit, from 0 to 9.
 */
public final class ExprLeafDigit extends ExprObjectLeaf {

    /** The digit, from 0 to 9. */
    private final int digit;

    /**
     * Constructs a new {@code ExprLeafDigit}.
     *
     * @param theDigit the digit
     */
    public ExprLeafDigit(final int theDigit) {

        super();

        if (theDigit < 0 || theDigit > 9) {
            throw new IllegalArgumentException("Invalid digit");
        }

        this.digit = theDigit;
    }

    /**
     * Gets the digit.
     *
     * @return the digit (from 0 to 9)
     */
    public int getDigit() {

        return this.digit;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprLeafDigit{digit='");
        htm.add(this.digit);
        htm.add("}");

        return htm.toString();
    }
}
