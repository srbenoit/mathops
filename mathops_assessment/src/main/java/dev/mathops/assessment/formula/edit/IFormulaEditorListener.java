package dev.mathops.assessment.formula.edit;

/**
 * A listener for a formula editor that can be notified of each edit.
 */
@FunctionalInterface
public interface IFormulaEditorListener {

    /**
     * Called each time the formula is edited.
     *
     * @param formula the edited formula
     */
    void formulaEdited(FEFormula formula);
}
