package dev.mathops.assessment.formula.edit;

/**
 * An interface for a formula component that can generate an edit object.
 */
@FunctionalInterface
public interface IEditableFormulaObject {

    /**
     * Generates an {@code AbstractFEObject} for this object.
     *
     * @param theFontSize the font size for the generated object
     * @return the generated {@code AbstractFEObject}
     */
    AbstractFEObject generateFEObject(final int theFontSize);
}
