package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FEVector;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

/**
 * An ordered list of integer-valued formula objects.
 */
public final class IntegerFormulaVector extends AbstractFormulaContainer implements IEditableFormulaObject {

    /**
     * Construct a new {@code IntegerFormulaVector}.
     */
    public IntegerFormulaVector() {

        super();
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public IntegerFormulaVector deepCopy() {

        final IntegerFormulaVector copy = new IntegerFormulaVector();

        final int count = numChildren();
        for (int i = 0; i < count; i++) {
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

        htm.add('[');

        final int count = numChildren();
        for (int i = 0; i < count; i++) {
            htm.add(getChild(i).toString(), CoreConstants.COMMA);
        }

        htm.add(']');

        return htm.toString();
    }

    /**
     * Gets the type this formula generates.
     *
     * @param context the context under which to evaluate the formula
     * @return EType.VECTOR
     */
    @Override
    public EType getType(final EvalContext context) {

        return EType.INTEGER_VECTOR;
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

        Object result = null;

        final int count = numChildren();
        if (count > 0) {
            final long[] elements = new long[count];
            for (int i = 0; i < count; ++i) {

                final Object obj = getChild(i).evaluate(context);

                if (obj instanceof ErrorValue) {
                    result = obj;
                    break;
                } else if (obj instanceof final Long l) {
                    elements[i] = l.longValue();
                } else {
                    result = new ErrorValue("Vector element is not integer");
                    break;
                }
            }

            if (result == null) {
                result = new IntegerVectorValue(elements);
            }
        } else {
            result = new ErrorValue("Vector must have at least one child");
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

        boolean allConstant = true;

        ErrorValue error = null;

        final int count = numChildren();
        final long[] elements = new long[count];

        for (int i = 0; i < count; ++i) {
            final AbstractFormulaObject child = getChild(i);
            Object value = null;
            if (child == null) {
                allConstant = false;
            } else if (child.isConstant()) {
                value = child.evaluate(context);
            } else {
                final AbstractFormulaObject newChild = child.simplify(context);
                if (newChild != child) {
                    setChild(i, newChild);
                }
                if (newChild.isConstant()) {
                    value = child.evaluate(context);
                } else {
                    allConstant = false;
                }
            }

            if (value instanceof final Long longVal) {
                elements[i] = longVal.longValue();
            } else if (value instanceof final ErrorValue errorVal) {
                error = errorVal;
            } else {
                error = new ErrorValue("Integer formula vector element evaluated to non-integer");
            }
        }

        final AbstractFormulaObject result;

        if (error == null) {
            if (allConstant) {
                result = new ConstIntegerVector(new IntegerVectorValue(elements));
            } else {
                result = this;
            }
        } else {
            result = error;
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

        final FEVector result = new FEVector(theFontSize);
        result.getAllowedTypes().remove(EType.REAL_VECTOR);

        final int count = numChildren();
        result.setNumEntries(count, false);

        for (int i = 0; i < count; ++i) {
            final AbstractFormulaObject arg = getChild(i);
            if (arg instanceof final IEditableFormulaObject editable) {
                result.setEntry(i, editable.generateFEObject(theFontSize), false);
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
        } else if (obj instanceof final IntegerFormulaVector vec) {
            equal = innerEquals(vec);
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

        xml.add("<int-vector>");
        appendChildrenXml(xml);
        xml.add("</int-vector>");
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

        xml.addln(ind, "Integer Formula Vector:");
        printChildrenDiagnostics(xml, indent + 1);
    }
}
