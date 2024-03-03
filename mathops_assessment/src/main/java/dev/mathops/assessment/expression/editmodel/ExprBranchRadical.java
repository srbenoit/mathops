package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a radical with a subexpression under the radical but no root.
 */
public final class ExprBranchRadical extends AbstractExprBranch {

    /** The base. */
    public final Expr base;

    /**
     * Constructs a new {@code ExprBranchRadical}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprBranchRadical(final AbstractExprObject theParent) {

        super(theParent);

        this.base = new Expr();

        this.base.innerSetFirstCursorPosition(1);

        // Initially, there is one cursor position for the step into the base and one for the step out of the base
        innerSetNumCursorPositions(2);
    }

    /**
     * Called when something potentially changes the number of cursor positions in a child.  This method recalculates
     * the starting cursor positions for each child of this object, and if this results in a change to this object's
     * cursor position count, the call is  propagated upward to the parent (if any).
     *
     * @param theFirstCursorPosition the new first cursor position
     */
    void recalculuate(final int theFirstCursorPosition) {

        innerSetFirstCursorPosition(theFirstCursorPosition);

        this.base.innerSetFirstCursorPosition(theFirstCursorPosition + 1);

        final int pos = 1 + this.base.getNumCursorPositions();
        innerSetNumCursorPositions(pos);
    }

    /**
     * Processes an action represented by an integer.  If the action code is 0xFFFF or smaller, it is interpreted
     * as a Unicode character;  otherwise, it is interpreted as an enumerated code.
     *
     * <p>
     * If there is a selection and an action is performed that would result in the deletion of that selection, the
     * deletion is done before this method is called.  Actions on objects are called only when there is no selection
     * region.  Actions like CUT/COPY/PASTE/DELETE (and undo/redo) are handled by other mechanisms.
     *
     * @param action the action code
     * @param cursorPosition the cursor position
     */
    void processAction(final int action, final int cursorPosition) {

        final int baseStart = this.base.getFirstCursorPosition();
        final int baseCount = this.base.getNumCursorPositions();
        final int baseEnd = baseStart + baseCount;
        if (cursorPosition >= baseStart && cursorPosition < baseEnd) {
            this.base.processAction(action, cursorPosition);
        }
    }
}

