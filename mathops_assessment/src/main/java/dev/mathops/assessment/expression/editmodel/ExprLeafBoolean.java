package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.log.Log;

/**
 * An expression object that represents a Boolean constant.
 */
public final class ExprLeafBoolean extends AbstractExprLeaf {

    /** The boolean value. */
    public final boolean value;

    /**
     * Constructs a new {@code ExprLeafBoolean}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theValue the boolean value
     */
    public ExprLeafBoolean(final AbstractExprObject theParent, final boolean theValue) {

        super(theParent);

        this.value = theValue;
        innerSetNumCursorPositions(1);
    }
}
