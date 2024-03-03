package dev.mathops.assessment.expression.editmodel;

/**
 * The base class for expression objects that are "leaf" nodes in an expression model tree.
 */
abstract class AbstractExprLeaf extends AbstractExprObject {

    /**
     * Constructs a new {@code AbstractExprLeaf}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    AbstractExprLeaf(final AbstractExprObject theParent) {

        super(theParent);
    }

    /**
     * Sets the first cursor position that is considered part of this object.
     *
     * @param theFirstCursorPosition the new first cursor position
     */
    final void setFirstCursorPosition(final int theFirstCursorPosition) {

        innerSetFirstCursorPosition(theFirstCursorPosition);
    }
}
