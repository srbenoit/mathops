package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FETest;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

/**
 * A test operation that evaluates a boolean condition, and depending on the result, returns the evaluated value of one
 * of two subexpressions. This can be interpreted logically is "if X is true, then A, otherwise B".
 */
public class TestOper extends AbstractFormulaContainer implements IEditableFormulaObject {

    /**
     * Construct a new {@code TestOper}.
     */
    public TestOper() {

        super();
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public TestOper deepCopy() {

        final TestOper copy = new TestOper();

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
        htm.add("test(");
        htm.add(count > 0 ? getChild(0).toString() : "null");
        htm.add('?');
        htm.add(count > 1 ? getChild(1).toString() : "null");
        htm.add(':');
        htm.add(count > 2 ? getChild(2).toString() : "null");
        htm.add(')');

        return htm.toString();
    }

    /**
     * Gets the type this formula generates.
     *
     * @param context the context under which to evaluate the formula
     * @return the type; EType.ERROR if no type can be determined
     */
    @Override
    public EType getType(final EvalContext context) {

        final EType result;

        if (numChildren() > 2) {
            final EType type1 = getChild(1).getType(context);
            final EType type2 = getChild(2).getType(context);

            if (type1 == type2) {
                result = type1;
            } else if ((type1 == EType.INTEGER && type2 == EType.REAL)
                    || (type1 == EType.REAL && type2 == EType.INTEGER)) {
                result = EType.REAL;
            } else if (type1 == EType.SPAN || type2 == EType.SPAN) {
                result = EType.SPAN;
            } else {
                result = EType.ERROR;
            }
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
     * @return an Long, Double, Boolean, or DocSimpleSpan value of the object, or a String with an error message if
     *         unable to compute
     */
    @Override
    public Object evaluate(final EvalContext context) {

        Object result;

        final int count = numChildren();
        if (count != 3) {
            result = new ErrorValue("Test operator cannot be evaluated without all clauses");
        } else {
            final Object decision = getChild(0).evaluate(context);

            if (decision instanceof Boolean) {
                if (((Boolean) decision).booleanValue()) {
                    result = getChild(1).evaluate(context);
                } else {
                    result = getChild(2).evaluate(context);
                }

                if (result != null) {
                    final EType type = getType(context);

                    if (type == EType.SPAN && (result instanceof Number || result instanceof Boolean
                            || result instanceof String)) {
                        final DocSimpleSpan span = new DocSimpleSpan();
                        span.add(new DocText(result.toString()));
                        result = span;
                    }
                }
            } else if (decision instanceof ErrorValue) {
                result = decision;
            } else {
                return new ErrorValue("IF clause in a test did not evaluate to a boolean.");
            }
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

        final Object value = evaluate(context);

        result = switch (value) {
            case final Long longValue -> new ConstIntegerValue(longValue.longValue());
            case final Number numValue -> new ConstRealValue(numValue);
            case final Boolean booleanValue -> new ConstBooleanValue(booleanValue.booleanValue());
            case final String stringValue -> new ConstStringValue(stringValue);
            case final DocSimpleSpan spanValue -> new ConstSpanValue(spanValue);
            case final IntegerVectorValue vecValue -> new ConstIntegerVector(vecValue);
            case final RealVectorValue vecValue -> new ConstRealVector(vecValue);
            case null, default -> this;
        };

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

        final FETest result = new FETest(theFontSize);

        if (numChildren() > 0) {
            final AbstractFormulaObject arg1 = getChild(0);
            if (arg1 instanceof final IEditableFormulaObject editable) {
                result.setCondition(editable.generateFEObject(theFontSize), false);
            }
        }
        if (numChildren() > 1) {
            final AbstractFormulaObject arg2 = getChild(1);
            if (arg2 instanceof final IEditableFormulaObject editable) {
                result.setThenClause(editable.generateFEObject(theFontSize), false);
            }
        }
        if (numChildren() > 2) {
            final AbstractFormulaObject arg3 = getChild(2);
            if (arg3 instanceof final IEditableFormulaObject editable) {
                result.setElseClause(editable.generateFEObject(theFontSize), false);
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
        } else if (obj instanceof final TestOper oper) {
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

        xml.add("<test>");
        appendChildrenXml(xml);
        xml.add("</test>");
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

        xml.addln(ind, "Test Operation:");
        printChildrenDiagnostics(xml, indent + 1);
    }
}
