package dev.mathops.assessment.formula.edit;

/**
 * A container for Undo/Redo state.
 */
class UndoRedoState {

    /** The formula. */
    public final FEFormula formula;

    /** The cursor. */
    public final FECursor cursor;

    /**
     * Constructs a new {@code UndoRedoState}.
     *
     * @param theFormula the formula
     * @param theCursor  the cursor
     */
    UndoRedoState(final FEFormula theFormula, final FECursor theCursor) {

        this.formula = theFormula.duplicate();
        this.cursor = theCursor.duplicate();
    }
}
