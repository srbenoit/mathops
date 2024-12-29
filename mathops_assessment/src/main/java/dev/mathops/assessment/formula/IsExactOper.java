package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.edit.FEIsExact;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An operation to test whether a real value is "exact" when printed to a specified number of decimal places. For
 * example, "1.66" is exact to 3 places, but "1.666666" is not. The first child is a real-valued value to test, the
 * second is the number of decimal places. The test works by scaling the real value by 10 to the power of the number of
 * decimal places, subtracting this value from the same value rounded to an integer, and comparing the result to a very
 * small value (this allows for small differences caused by round-off or floating point arithmetic).
 */
public final class IsExactOper extends AbstractFormulaContainer implements IEditableFormulaObject {

    /** Small value used to test for exactness. */
    private static final double EPSILON = 0.00001;

    /**
     * Construct a new {@code IsExactOper}.
     */
    public IsExactOper() {

        super();
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public IsExactOper deepCopy() {

        final IsExactOper copy = new IsExactOper();

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

        final int count = numChildren();
        htm.add("isExact(");
        htm.add(count > 0 ? getChild(0).toString() : "null");
        htm.add(CoreConstants.COMMA_CHAR);
        htm.add(count > 1 ? getChild(1).toString() : "null");
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

        return EType.BOOLEAN;
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

        final int count = numChildren();
        if (count == 2) {
            final Object child0 = getChild(0).evaluate(context);
            if (child0 instanceof Long) {
                // Integer values are always "exact"
                result = Boolean.TRUE;
            } else if (child0 instanceof final Number num) {
                final Object child1 = getChild(1).evaluate(context);

                if (child1 instanceof final Long lng) {
                    final int numPlaces = lng.intValue();

                    if (numPlaces >= 0 && numPlaces <= 10) {
                        double numValue = num.doubleValue();
                        for (int i = 0; i < numPlaces; ++i) {
                            numValue *= 10.0;
                        }

                        final double rounded = (double) Math.round(numValue);
                        final double diff = Math.abs(rounded - numValue);

                        result = Boolean.valueOf(diff < EPSILON);
                    } else {
                        result = new ErrorValue(
                                "Number of places to test in IsExact must be from 0 to 10, inclusive.");
                    }
                } else {
                    result =
                            new ErrorValue("Second child of IsExact did not evaluate to an integer.");
                }
            } else {
                result = new ErrorValue("First child of IsExact did not evaluate to a number.");
            }
        } else {
            result = new ErrorValue("IsExact operator requires two children");
        }

        return result;
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

        final AbstractFormulaObject result;

        boolean canEvaluate = false;

        final AbstractFormulaObject child0 = getChild(0);
        final AbstractFormulaObject child1 = getChild(1);
        if (child0 != null && child1 != null) {
            if (child0.isConstant() && child1.isConstant()) {
                canEvaluate = true;
            } else {
                final AbstractFormulaObject newChild0 = child0.simplify(context);
                final AbstractFormulaObject newChild1 = child1.simplify(context);

                if (newChild0 != child0) {
                    setChild(0, newChild0);
                }
                if (newChild1 != child1) {
                    setChild(1, newChild1);
                }

                if (newChild0.isConstant() && newChild1.isConstant()) {
                    canEvaluate = true;
                }
            }
        }

        if (canEvaluate) {
            final Object value = evaluate(context);

            if (value instanceof final Boolean booleanVal) {
                result = new ConstBooleanValue(booleanVal.booleanValue());
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
    public FEIsExact generateFEObject(final int theFontSize) {

        final FEIsExact result = new FEIsExact(theFontSize);

        if (numChildren() > 0) {
            final AbstractFormulaObject arg1 = getChild(0);
            if (arg1 instanceof final IEditableFormulaObject editable) {
                result.setValueToTest(editable.generateFEObject(theFontSize), false);
            }
        }
        if (numChildren() > 1) {
            final AbstractFormulaObject arg2 = getChild(1);
            if (arg2 instanceof final IEditableFormulaObject editable) {
                result.setNumPlaces(editable.generateFEObject(theFontSize), false);
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
        } else if (obj instanceof final IsExactOper oper) {
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

        xml.add("<is-exact>");
        appendChildrenXml(xml);
        xml.add("</is-exact>");
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

        xml.addln(ind, "Exactness Test:");
        printChildrenDiagnostics(xml, indent + 1);
    }
}
