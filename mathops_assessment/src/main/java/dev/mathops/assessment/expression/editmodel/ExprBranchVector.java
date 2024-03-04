package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a vector.
 */
public final class ExprBranchVector extends AbstractExprBranch {

    /** The maximum number of components supported. */
    private static final int MAX_COMPONENTS = 999;

    /** The type of brackets. */
    public final EVectorMatrixBrackets brackets;

    /** The components. */
    public final Expr[] components;

    /**
     * Constructs a new {@code ExprBranchVector}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param numComponents the number of components (can be changed)
     * @param theBrackets the type of brackets
     */
    public ExprBranchVector(final AbstractExprObject theParent, final EVectorMatrixBrackets theBrackets,
                            final int numComponents) {

        super(theParent);

        if (numComponents < 1 || numComponents > MAX_COMPONENTS) {
            throw new IllegalArgumentException("Invalid number of components");
        }

        this.brackets = theBrackets;
        this.components = new Expr[numComponents];

        for (int i = 0; i < numComponents; ++i) {
            final Expr expr = new Expr(this);
            this.components[i] = expr;
            expr.innerSetFirstCursorPosition(i + 1);
        }

        // Initially, there is one cursor position for the opening bracket, one cursor position for each transition
        // from one component to the next, and one for the closing bracket
        innerSetNumCursorPositions(numComponents + 1);
    }

    /**
     * Called when something potentially changes the number of cursor positions in a child.  This method recalculates
     * the starting cursor positions for each child of this object, and updates this objects total cursor position
     * count.  If this results in a change to this object's cursor position count, the call is propagated upward to the
     * parent (if any).
     */
    void recalculate(final int theFirstCursorPosition) {

        final int origCount = getNumCursorPositions();

        int pos = theFirstCursorPosition + 1;
        for (final Expr expr : this.components) {
            expr.innerSetFirstCursorPosition(pos);
            pos += expr.getNumCursorPositions() + 1;
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

        for (final Expr expr : this.components) {
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
