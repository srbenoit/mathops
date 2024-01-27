package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FEGrouping;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

/**
 * A grouping operation to control order of evaluation. Everything inside a grouping operation (which can be either a
 * matched pair of parentheses, or a matched pair of square brackets) is treated as a self-contained formula.
 */
@Deprecated
public final class GroupingOper extends AbstractFormulaContainer implements IEditableFormulaObject {

    /**
     * Construct a new {@code GroupingOper}.
     */
    public GroupingOper() {

        super();
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public GroupingOper deepCopy() {

        final GroupingOper copy = new GroupingOper();

        final int count = numChildren();
        for (int i = 0; i < count; ++i) {
            copy.addChild(getChild(i).deepCopy());
        }

        return copy;
    }

    /**
     * Generate the string representation of the object.
     *
     * @return the string representation of the object
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(50);

        htm.add('(');
        htm.add(getChild(0) != null ? getChild(0).toString() : "null");
        htm.add(')');

        return htm.toString();
    }

    /**
     * Gets the type this formula generates.
     *
     * @param context the context under which to evaluate the formula
     * @return the type; {@code EType.ERROR} if no type can be determined
     */
    @Override
    public EType getType(final EvalContext context) {

        final EType result;

        if (numChildren() > 0) {
            result = getChild(0).getType(context);
        } else {
            result = EType.ERROR;
        }

        return result;
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
     * @return a Long, Double, Boolean, or DocSimpleSpan value of the object, or a String with an error message if
     *         unable to compute
     */
    @Override
    public Object evaluate(final EvalContext context) {

        final Object result;

        if (numChildren() == 0) {
            result = new ErrorValue("Grouping operator cannot be evaluated without children");
        } else {
            result = getChild(0).evaluate(context);
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

        boolean canEvaluate = false;

        final AbstractFormulaObject child = getChild(0);
        if (child != null) {
            if (child.isConstant()) {
                canEvaluate = true;
            } else {
                final AbstractFormulaObject newChild = child.simplify(context);
                if (newChild != child) {
                    if (newChild.isConstant()) {
                        canEvaluate = true;
                    } else {
                        setChild(0, newChild);
                    }
                }
            }
        }

        if (canEvaluate) {
            final Object value = evaluate(context);

            if (value instanceof final Long longVal) {
                result = new ConstIntegerValue(longVal.longValue());
            } else if (value instanceof final Number numVal) {
                result = new ConstRealValue(numVal);
            } else if (value instanceof final Boolean booleanVal) {
                result = new ConstBooleanValue(booleanVal.booleanValue());
            } else if (value instanceof final String stringVal) {
                result = new ConstStringValue(stringVal);
            } else if (value instanceof final DocSimpleSpan spanVal) {
                result = new ConstSpanValue(spanVal);
            } else if (value instanceof final IntegerVectorValue intVecVal) {
                result = new ConstIntegerVector(intVecVal);
            } else if (value instanceof final RealVectorValue realVecVal) {
                result = new ConstRealVector(realVecVal);
            } else {
                result = this;
            }
        } else {
            result = this;
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

        final FEGrouping result = new FEGrouping(theFontSize);

        if (numChildren() > 0) {
            final AbstractFormulaObject arg1 = getChild(0);
            if (arg1 instanceof final IEditableFormulaObject editable) {
                result.setArg(editable.generateFEObject(theFontSize), false);
            }
        }

        return result;
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return innerHashCode();
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
        } else if (obj instanceof final GroupingOper oper) {
            equal = innerEquals(oper);
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

        xml.add("<grouping>");
        appendChildrenXml(xml);
        xml.add("</grouping>");
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

        xml.addln(ind, "Grouping:");
        printChildrenDiagnostics(xml, indent + 1);
    }
}
