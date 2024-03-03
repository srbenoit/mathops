package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a fraction with subexpression numerator and subexpression denominator.
 */
public final class ExprBranchFraction extends AbstractExprBranch {

    /** The shape. */
    private EFractionShape shape;

    /** The numerator. */
    public final Expr numerator;

    /** The denominator. */
    public final Expr denominator;

    /**
     * Constructs a new {@code ExprGlyphFraction}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theShape the shape
     */
    public ExprBranchFraction(final AbstractExprObject theParent, final EFractionShape theShape) {

            super(theParent);

            this.shape = theShape;
            this.numerator = new Expr();
            this.denominator = new Expr();

            this.numerator.innerSetFirstCursorPosition(1);
            this.denominator.innerSetFirstCursorPosition(2);

            // Initially, there is one cursor position for the step into the numerator, one for the step from numerator
            // to denominator, and one for the step out of the denominator
            innerSetNumCursorPositions(3);
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

            this.numerator.innerSetFirstCursorPosition(theFirstCursorPosition + 1);

            int pos = 2 + this.numerator.getNumCursorPositions();
            this.denominator.innerSetFirstCursorPosition(pos);

            pos += 1 + this.denominator.getNumCursorPositions();
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

            final int rootStart = this.numerator.getFirstCursorPosition();
            final int rootCount = this.numerator.getNumCursorPositions();
            final int rootEnd = rootStart + rootCount;
            if (cursorPosition >= rootStart && cursorPosition < rootEnd) {
                this.numerator.processAction(action, cursorPosition);
            } else {
                final int baseStart = this.denominator.getFirstCursorPosition();
                final int baseCount = this.denominator.getNumCursorPositions();
                final int baseEnd = baseStart + baseCount;
                if (cursorPosition >= baseStart && cursorPosition < baseEnd) {
                    this.denominator.processAction(action, cursorPosition);
                }
            }
        }
    }

