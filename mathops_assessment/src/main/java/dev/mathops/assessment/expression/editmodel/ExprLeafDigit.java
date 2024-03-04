package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.log.Log;

/**
 * An expression object that represents a single decimal digit, from 0 to 9.
 */
public final class ExprLeafDigit extends AbstractExprLeaf {

    /** The digit, from 0 to 9. */
    public final int digit;

    /**
     * Constructs a new {@code ExprLeafDigit}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theDigit the digit
     */
    public ExprLeafDigit(final AbstractExprObject theParent, final int theDigit) {

        super(theParent);

        if (theDigit < 0 || theDigit > 9) {
            throw new IllegalArgumentException("Invalid digit");
        }

        this.digit = theDigit;
    }
}
