package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FEConstantReal;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.number.Irrational;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * A constant real vector in a formula.
 */
public final class ConstRealValue extends AbstractFormulaObject implements IEditableFormulaObject {

    /** The constant value (Double or Irrational). */
    private final Number value;

    /**
     * Construct a new {@code ConstRealValue}.
     *
     * @param theValue the real value
     */
    public ConstRealValue(final double theValue) {

        super();

        this.value = Double.valueOf(theValue);
    }

    /**
     * Construct a new {@code ConstRealValue}.
     *
     * @param theValue the real value
     */
    public ConstRealValue(final Number theValue) {

        super();

        if (theValue == null) {
            throw new IllegalArgumentException("Real value may not be null");
        }

        if (theValue instanceof Double || theValue instanceof Irrational) {
            this.value = theValue;
        } else {
            this.value = Double.valueOf(theValue.doubleValue());
        }
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public ConstRealValue deepCopy() {

        return new ConstRealValue(this.value);
    }

    /**
     * Generate the string representation of the object.
     *
     * @return the string representation of the object
     */
    @Override
    public String toString() {

        return this.value.toString();
    }

    /**
     * Gets the type this formula generates.
     *
     * @param context the context under which to evaluate the formula
     * @return EType.REAL
     */
    @Override
    public EType getType(final EvalContext context) {

        return EType.REAL;
    }

    /**
     * Tests whether this object is a simple constant value.
     *
     * @return true if a constant value (true for objects of this class)
     */
    @Override
    public boolean isConstant() {

        return true;
    }

    /**
     * Evaluates the object within the tree. Subclasses should override this to produce the correct value.
     *
     * @param context the context under which to evaluate the formula
     * @return a Double or Irrational value of the object
     */
    @Override
    public Object evaluate(final EvalContext context) {

        return this.value;
    }

    /**
     * Simplifies a formula by replacing all parameter references to constant values with the constant itself, and then
     * performing any constant-valued evaluations. For example, if a formula contained "3 * ({x} - 4)" and the parameter
     * {x} was a constant integer with value 7, this formula would be simplified to a single integer constant with value
     * 9.
     *
     * @param context the context under which to evaluate the formula
     * @return the simplified version of this object (returns this object itself if already simplified)
     */
    @Override
    public AbstractFormulaObject simplify(final EvalContext context) {

        return this;
    }

    /**
     * Generates an {@code AbstractFEObject} for this object.
     *
     * @param theFontSize the font size for the generated object
     * @return the generated {@code AbstractFEObject}
     */
    @Override
    public AbstractFEObject generateFEObject(final int theFontSize) {

        return new FEConstantReal(theFontSize, this.value.doubleValue());
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return this.value.hashCode();
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final ConstRealValue val) {
            equal = this.value.equals(val.value);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Appends an XML representation of the formula to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    @Override
    public void appendXml(final HtmlBuilder xml) {

        xml.add("<real value='", this.value, "'/>");
    }

    /**
     * Appends a diagnostic representation of the formula.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param indent the indent level
     */
    @Override
    public void printDiagnostics(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent * 3);

        xml.addln(ind, "Real: ", this.value);
    }
}
