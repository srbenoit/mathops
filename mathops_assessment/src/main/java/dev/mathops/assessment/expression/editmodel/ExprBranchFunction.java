package dev.mathops.assessment.expression.editmodel;

import dev.mathops.assessment.formula.EFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * A glyph that represents a function name and its arguments.
 */
public final class ExprBranchFunction extends AbstractExprBranch {

    /** The function. */
    public final EFunction function;

    /** The arguments. */
    public final List<Expr> arguments;

    /**
     * Constructs a new {@code ExprBranchFunction}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theFunction the function
     */
    public ExprBranchFunction(final AbstractExprObject theParent, final EFunction theFunction) {

        super(theParent);

        this.function = theFunction;
        this.arguments = new ArrayList<>(10);


        // Initially, there is one cursor position for the step into the argument list and one for the step out of the
        // argument list
        innerSetNumCursorPositions(2);
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

        int pos = theFirstCursorPosition + 1;
        for (final Expr arg : this.arguments) {
            arg.innerSetFirstCursorPosition(pos);
            pos += arg.getNumCursorPositions();
        }

        ++pos;

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

        for (final Expr expr : this.arguments) {
            final int start = expr.getFirstCursorPosition();
            final int count = expr.getNumCursorPositions();
            final int end = start + count;
            if (cursorPosition >= start && cursorPosition < end) {
                expr.processAction(action, cursorPosition);
                break;
            }
        }
    }
}

