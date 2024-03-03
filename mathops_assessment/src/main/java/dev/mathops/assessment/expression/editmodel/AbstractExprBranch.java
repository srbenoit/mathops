package dev.mathops.assessment.expression.editmodel;

/**
 * The base class for expression objects that are "branch" nodes in an expression model tree.
 */
abstract class AbstractExprBranch extends AbstractExprObject {

    /**
     * Constructs a new {@code AbstractExprBranch}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    AbstractExprBranch(final AbstractExprObject theParent) {

        super(theParent);
    }

    /**
     * Called when something potentially changes the number of cursor positions in a child.  This method recalculates
     * the starting cursor positions for each child of this object, and if this results in a change to this object's
     * cursor position count, the call is  propagated upward to the parent (if any).
     *
     * @param theFirstCursorPosition the new first cursor position
     */
    abstract void recalculuate(int theFirstCursorPosition);

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
    abstract void processAction(int action, int cursorPosition);
}
