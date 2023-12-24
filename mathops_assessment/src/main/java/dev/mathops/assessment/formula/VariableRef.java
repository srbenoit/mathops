package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FEVarRef;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.util.Objects;

/**
 * A reference to a parameter in a formula. The reference is simply the name of the parameter. The formula will maintain
 * a list of parameters and their values by name.
 */
public class VariableRef extends AbstractFormulaObject implements IEditableFormulaObject {

    /** The name of the parameter. */
    public String name;

    /** The index, for references to vector values. */
    public Integer index;

    /**
     * Construct a new {@code VariableRef}.
     *
     * @param theName the name of the parameter
     */
    public VariableRef(final String theName) {

        super();

        if (theName == null) {
            throw new IllegalArgumentException();
        }

        this.name = theName;
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public AbstractFormulaObject deepCopy() {

        final VariableRef copy = new VariableRef(this.name);
        copy.index = this.index;

        return copy;
    }

    /**
     * Generate the string representation of the object.
     *
     * @return the string representation of the object
     */
    @Override
    public String toString() {

        final HtmlBuilder str = new HtmlBuilder(50);

        str.add("{", this.name);

        if (this.index != null) {
            str.add("[", this.index, "]");
        }

        str.add('}');

        return str.toString();
    }

    /**
     * Gets the type this formula generates.
     *
     * @param context the context under which to evaluate the formula
     * @return the type; EType.ERROR if no type can be determined
     */
    @Override
    public EType getType(final EvalContext context) {

        final AbstractVariable var = context.getVariable(this.name);

        return var == null ? EType.ERROR : var.type;
    }

    /**
     * Tests whether this object is a simple constant value.
     *
     * @return true if a constant value (false for objects of this class)
     */
    @Override
    public boolean isConstant() {

        return false;
    }

    /**
     * Evaluates the object within the tree. Subclasses should override this to produce the correct value.
     *
     * @param context the context under which to evaluate the formula
     * @return an Long, Double, Boolean, or DocSimpleSpan value of the object, or a String with an error message if
     *         unable to compute
     */
    @Override
    public Object evaluate(final EvalContext context) {

        final AbstractVariable var = context.getVariable(this.name);
        Object result = var == null ? null : var.getValue();

        if (result == null) {
            result = new ErrorValue("Parameter {" + this.name
                    + "} cannot be evaluated.");
        } else if (result instanceof final RealVectorValue realVec && this.index != null) {
            result = Double.valueOf(realVec.getElement(this.index.intValue()));
        } else if (result instanceof final IntegerVectorValue intVec && this.index != null) {
            result = Long.valueOf(intVec.getElement(this.index.intValue()));
        } else if (this.index != null) {
            Log.warning("Unexpected result type with indexed parameter: ",
                    result.getClass().getName());
        }

        return result;
    }

    /**
     * Simplifies a formula by replacing all parameter references to constant values with the constant itself, and then
     * performing any constant-valued evaluations. For example, if a formula contained "3 * ({x} - 4)" and the parameter
     * {x} was a constant integer with value 7, this formula would be simplified to a single integer constant with value
     * 9.
     *
     * <p>
     * Parameters that refer to variables of an input type are never simplified away.
     *
     * @param context the context under which to evaluate the formula
     * @return the simplified version of this object (returns this object itself if already simplified)
     */
    @Override
    public AbstractFormulaObject simplify(final EvalContext context) {

        final AbstractFormulaObject result;

        final AbstractVariable var = context.getVariable(this.name);

        if (var == null) {
            result = new ErrorValue("Variable {" + this.name
                    + "} is not present in the evaluation context.");
        } else if (var.isInput()) {
            // We don't simplify out input variables
            result = this;
        } else {
            final Object value = var.getValue();

            if (value == null) {
                result = new ErrorValue("Variable {" + this.name
                        + "} cannot be evaluated.");
            } else if (value instanceof final ErrorValue error) {
                result = error;
            } else if ((value instanceof final IntegerFormulaVector vector) && this.index != null) {
                result = vector.getChild(this.index.intValue() - 1);
            } else if (value instanceof final Long valueLong) {
                result = new ConstIntegerValue(valueLong.longValue());
            } else if (value instanceof final Number valueNum) {
                result = new ConstRealValue(valueNum);
            } else if (value instanceof final Boolean valueBoolean) {
                result = new ConstBooleanValue(valueBoolean.booleanValue());
            } else if (value instanceof final String valueString) {
                result = new ConstStringValue(valueString);
            } else if (value instanceof final DocSimpleSpan valueSpan) {
                result = new ConstSpanValue(valueSpan);
            } else if (value instanceof final IntegerVectorValue valueIntegerVector) {
                final int len = valueIntegerVector.getNumElements();
                if (this.index == null) {
                    result = new ConstIntegerVector(valueIntegerVector);
                } else {
                    final int indexInt = this.index.intValue();
                    if (indexInt < 0 || indexInt >= len) {
                        result = new ErrorValue("Array index out of range.");
                    } else {
                        result = new ConstIntegerValue(valueIntegerVector.getElement(indexInt));
                    }
                }
            } else if (value instanceof final RealVectorValue valueRealVector) {
                final int len = valueRealVector.getNumElements();
                if (this.index == null) {
                    result = new ConstRealVector(valueRealVector);
                } else {
                    final int indexInt = this.index.intValue();
                    if (indexInt < 0 || indexInt >= len) {
                        result = new ErrorValue("Array index out of range.");
                    } else {
                        result = new ConstRealValue(valueRealVector.getElement(indexInt));
                    }
                }
            } else {
                result = this;
            }
        }

        return result;

    }

    /**
     * Generates an {@code AbstractFEObject} for this object.
     *
     * @param theFontSize the font size for the generated object
     * @return the generated {@code AbstractFEObject}
     */
    @Override
    public AbstractFEObject generateFEObject(final int theFontSize) {

        return new FEVarRef(theFontSize, this.name, this.index);
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.name) + Objects.hashCode(this.index);
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
        } else if (obj instanceof final VariableRef ref) {
            equal = Objects.equals(this.name, ref.name) && Objects.equals(this.index, ref.index);
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

        xml.add("<varref name='", this.name, "'");
        if (this.index != null) {
            xml.add(" index='", this.index, "'");
        }
        xml.add("/>");
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

        xml.add(ind, "Variable ref {", this.name);
        if (this.index != null) {
            xml.add('[');
            xml.add(this.index);
            xml.add(']');
        }
        xml.addln('}');
    }
}
