package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a subexpression (the base) raised to a subexpression (the exponent).
 *
 * <p>
 * When evaluated, if the base and exponent evaluate to numbers, the result is the number found by raising the base
 * to the exponent.  If the base is a square matrix, and the exponent is zero or a (small) positive integer, the result
 * is the matrix raised to the specified power.
 */
public final class ExprBranchBaseWithExponent extends AbstractExprBranch {

    /** The base. */
    public final Expr base;

    /** The exponent. */
    public final Expr exponent;

    /**
     * Constructs a new {@code ExprBranchBaseWithExponent}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprBranchBaseWithExponent(final AbstractExprObject theParent) {

        super(theParent);

        this.base = new Expr(this);
        this.exponent = new Expr(this);

        this.base.innerSetFirstCursorPosition(1);
        this.exponent.innerSetFirstCursorPosition(2);

        // Initially, there is one cursor position for the step into the base, one for the step from base to exponent,
        // and one for the step out of the exponent
        innerSetNumCursorPositions(3);
    }

    /**
     * Called when something potentially changes the number of cursor positions in a child.  This method recalculates
     * the starting cursor positions for each child of this object, and if this results in a change to this object's
     * cursor position count, the call is  propagated upward to the parent (if any).
     *
     * @param theFirstCursorPosition the new first cursor position
     */
    void recalculate(final int theFirstCursorPosition) {

        final int origCount = getNumCursorPositions();

        innerSetFirstCursorPosition(theFirstCursorPosition);

        this.base.innerSetFirstCursorPosition(theFirstCursorPosition + 1);

        int pos = 2 + this.base.getNumCursorPositions();
        this.exponent.innerSetFirstCursorPosition(pos);

        pos += 1 + this.exponent.getNumCursorPositions();

        if (pos != origCount) {
            innerSetNumCursorPositions(pos);
            if (getParent() instanceof final AbstractExprBranch parentBranch) {
                final int parentFirst = parentBranch.getFirstCursorPosition();
                parentBranch.recalculate(parentFirst);
            }
        }
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
        } else {
            final int expStart = this.exponent.getFirstCursorPosition();
            final int expCount = this.exponent.getNumCursorPositions();
            final int expEnd = expStart + expCount;
            if (cursorPosition >= expStart && cursorPosition < expEnd) {
                this.exponent.processAction(action, cursorPosition);
            }
        }
    }
}

