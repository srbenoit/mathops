package dev.mathops.assessment;

import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.log.Log;

/**
 * A container for a Number and a Formula, only one of which should be present. This supports a common idiom in which
 * either a constant or formula is provided for some real-valued parameter.
 */
public final class NumberOrFormula {

    /** The number. */
    private final Number number;

    /** The formula. */
    private final Formula formula;

    /**
     * Constructs a new {@code NumberOrFormula} with a number.
     *
     * @param theNumber the number
     */
    public NumberOrFormula(final Number theNumber) {

        if (theNumber == null) {
            throw new IllegalArgumentException("Number may not be null");
        }

        this.number = theNumber;
        this.formula = null;
    }

    /**
     * Constructs a new {@code NumberOrFormula} with a formula.
     *
     * @param theFormula the formula
     */
    public NumberOrFormula(final Formula theFormula) {

        if (theFormula == null) {
            throw new IllegalArgumentException("Formula may not be null");
        }

        this.number = null;
        this.formula = theFormula;
    }

    /**
     * Returns a deep copy of this object.
     *
     * @return the copy
     */
    public NumberOrFormula deepCopy() {

        final NumberOrFormula result;

        if (this.number == null) {
            result = new NumberOrFormula(this.formula.deepCopy());
        } else {
            result = new NumberOrFormula(this.number);
        }

        return result;
    }

    /**
     * Gets the number.
     *
     * @return the number (null if this object contains a formula)
     */
    public Number getNumber() {

        return this.number;
    }

    /**
     * Gets the formula.
     *
     * @return the formula (null if this object contains a number)
     */
    public Formula getFormula() {

        return this.formula;
    }

    /**
     * Evaluates the object. If this object contains a number, that number is returned. If it contains a formula, the
     * formula is evaluated and the result is returned.
     *
     * @param evalContext the evaluation context
     * @return the result
     */
    public Object evaluate(final EvalContext evalContext) {

        return this.number == null ? this.formula.evaluate(evalContext) : this.number;
    }

    /**
     * Tests whether this object's value is constant (does not depend on variables).
     *
     * @return true if this object's value is constant
     */
    public boolean isConstant() {

        return this.number != null || this.formula.isConstant();
    }

    /**
     * Simplifies the object. If this object contains a number, that number is returned. If it contains a formula, the
     * formula is simplified and the result is returned.
     *
     * @param evalContext the evaluation context
     * @return the result
     */
    public NumberOrFormula simplify(final EvalContext evalContext) {

        final NumberOrFormula result;

        if (this.number == null && !this.formula.isConstant()) {
            final AbstractFormulaObject simple = this.formula.simplify(evalContext);

            if (simple instanceof final Formula simpleFormula) {
                result = new NumberOrFormula(simpleFormula);
            } else if (simple.isConstant()) {
                final Object value = simple.evaluate(evalContext);
                if (value instanceof final Number numberValue) {
                    result = new NumberOrFormula(numberValue);
                } else {
                    Log.warning("NumberOrFormula simplified to a non-number: ",
                            simple.getClass().getSimpleName());
                    return this;
                }
            } else {
                Log.warning("NumberOrFormula simplified to a non-constant: ",
                        simple.getClass().getSimpleName());
                return this;
            }
        } else {
            result = this;
        }

        return result;
    }

    /**
     * Gets the hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.number == null ? this.formula.hashCode() : this.number.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object against which to test
     * @return true if objects are equal
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final NumberOrFormula other) {
            if (this.number == null) {
                equal = this.formula.equals(other.formula);
            } else {
                equal = this.number.equals(other.number);
            }
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates the string representation of the value.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.number == null ? this.formula.toString() : this.number.toString();
    }
}
