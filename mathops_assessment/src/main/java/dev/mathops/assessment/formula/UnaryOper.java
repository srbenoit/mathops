package dev.mathops.assessment.formula;

import dev.mathops.assessment.EIrrationalFactor;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.Irrational;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FEUnaryOper;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.XmlEscaper;

/**
 * A unary operator. This is a special case of the '-' and '+' operators when they occur as the first token in a
 * formula, or immediately following another operator. In these cases, they are treated as the "negative" or "positive"
 * operators rather than the "subtraction" and "addition" operators.
 */
public class UnaryOper extends AbstractFormulaContainer implements IEditableFormulaObject {

    /** The operator. */
    private final EUnaryOp op;

    /**
     * Construct a new {@code UnaryOperator}.
     *
     * @param theOp the operator
     * @throws IllegalArgumentException if the operator is {@code null}
     */
    public UnaryOper(final EUnaryOp theOp) {
        super();

        if (theOp == null) {
            throw new IllegalArgumentException("Operator may not be null");
        }

        this.op = theOp;
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public UnaryOper deepCopy() {

        final UnaryOper copy;

        copy = new UnaryOper(this.op);

        final int count = numChildren();
        if (count > 0) {
            copy.addChild(getChild(0).deepCopy());
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

        final HtmlBuilder str = new HtmlBuilder(50);

        str.add(this.op.op);
        str.add(getChild(0) != null ? getChild(0).toString() : "null");

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

        final EType result;

        if (numChildren() > 0) {
            // These operators never change the type of their operand
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
     * @return an Long, Double, Boolean, or DocSimpleSpan value of the object, or a String with an error message if
     *         unable to compute
     */
    @Override
    public Object evaluate(final EvalContext context) {

        final long ival;
        final double rval;

        if (getChild(0) == null) {
            return new ErrorValue("Unary operator evaluated with no argument.");
        }

        Object result = getChild(0).evaluate(context);

        if (!(result instanceof ErrorValue)) {
            if (this.op == EUnaryOp.PLUS) {
                if (!(result instanceof Number)) {
                    return new ErrorValue("Unary '+' cannot be applied to "
                            + result.getClass().getSimpleName());
                }
            } else if (this.op == EUnaryOp.MINUS) { // Change the sign on a Real or Integer

                if (result instanceof final Long l) {
                    ival = l.longValue();
                    result = Long.valueOf(-ival);
                } else if (result instanceof final Irrational i) {
                    if (i.factor == EIrrationalFactor.SQRT) {
                        result = new Irrational(i.factor, i.base, -i.numerator, i.denominator);
                    } else {
                        result = new Irrational(i.factor, -i.numerator, i.denominator);
                    }
                } else if (result instanceof final Number d) {
                    rval = d.doubleValue();
                    result = Double.valueOf(-rval);
                } else {
                    return new ErrorValue("Unary '-' cannot be applied to "
                            + result.getClass().getSimpleName());
                }
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
            } else if (value instanceof final Number num) {
                result = new ConstRealValue(num);
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

        final FEUnaryOper result = new FEUnaryOper(theFontSize, this.op);

        if (numChildren() > 0) {
            final AbstractFormulaObject arg = getChild(0);
            if (arg instanceof final IEditableFormulaObject editable) {
                result.setArg1(editable.generateFEObject(theFontSize), false);
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

        return innerHashCode() + this.op.hashCode();
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
        } else if (obj instanceof final UnaryOper oper) {
            equal = innerEquals(oper) && this.op == oper.op;
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

        xml.add("<unary op='" + XmlEscaper.escape(Character.toString(this.op.op)) + "'>");
        appendChildrenXml(xml);
        xml.add("</unary>");
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

        xml.addln(ind, "Unary Operator: ", this.op);
        printChildrenDiagnostics(xml, indent + 1);
    }
}
