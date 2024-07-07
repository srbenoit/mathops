package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FEBinaryOper;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.parser.xml.XmlEscaper;

/**
 * A binary operator. Operators that act on integers and reals to produce numeric results include * +, -, *,
 * (center-dot), /, ^ and %. Operators that act on integers and reals to produce boolean results include >, <, =, NE,
 * LE, and GE. Operators that act on booleans to produce booleans include | and &. Operators that act on Strings to
 * produce strings include +. When + is used with a String and some other type, that other type is converted to its
 * String representation and concatenated with the String. Note that +, - may be either unary or binary operators.
 */
public final class BinaryOper extends AbstractFormulaContainer implements IEditableFormulaObject {

    /** The operator. */
    public final EBinaryOp op;

    /**
     * Construct a new binary operator.
     *
     * @param theOp the operator
     * @throws IllegalArgumentException if the operator is {@code null}
     */
    public BinaryOper(final EBinaryOp theOp) {
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
    public BinaryOper deepCopy() {

        final BinaryOper copy = new BinaryOper(this.op);

        final int count = numChildren();
        for (int i = 0; i < count; ++i) {
            final AbstractFormulaObject child = getChild(i);
            if (child != null) {
                copy.addChild(child.deepCopy());
            }
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
        if (count == 0) {
            htm.add("null");
        } else {
            final AbstractFormulaObject child0 = getChild(0);
            htm.add(child0 == null ? "null" : child0.toString());
            for (int i = 1; i < count; ++i) {
                htm.add(this.op.op);
                final AbstractFormulaObject child = getChild(i);
                htm.add(child == null ? "null" : child.toString());
            }
        }

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

        if (numChildren() > 1) {
            final EType type1 = getChild(0).getType(context);
            final EType type2 = getChild(1).getType(context);

            result = switch (this.op) {
                case ADD, SUBTRACT, MULTIPLY, POWER ->
                        type1 == EType.INTEGER && type2 == EType.INTEGER ? EType.INTEGER : EType.REAL;
                case DIVIDE -> EType.REAL;
                case REMAINDER -> EType.INTEGER;
                case LT, GT, LE, GE, EQ, APPROX, NE, AND, OR -> EType.BOOLEAN;
            };
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
     * @return a {@code Long}, {@code Double}, {@code Boolean}, or {@code DocSimpleSpan} value of the object, or an {@code
     *         ErrorValue} if unable to compute
     */
    @Override
    public Object evaluate(final EvalContext context) {

        final Object result;

        if (this.op == EBinaryOp.AND) {
            result = doAnd(context);
        } else if (this.op == EBinaryOp.OR) {
            result = doOr(context);
        } else if (this.op == EBinaryOp.ADD) {
            result = doAdd(context);
        } else if (this.op == EBinaryOp.APPROX) {
            result = doApprox(context);
        } else {
            // Test that both children have been supplied.
            final int count = numChildren();
            if (count == 2) {

                final Object left = getChild(0).evaluate(context);
                if (left instanceof ErrorValue) {
                    result = left;
                } else {

                    final Object right = getChild(1).evaluate(context);
                    if (right instanceof ErrorValue) {
                        result = right;
                    } else {
                        result = switch (this.op) {
                            case SUBTRACT -> doSubtract(left, right);
                            case MULTIPLY -> doMultiply(left, right);
                            case DIVIDE -> doDivide(left, right);
                            case POWER -> doPower(left, right);
                            case REMAINDER -> doModulo(left, right);
                            case LT -> doLessThan(left, right);
                            case GT -> doGreaterThan(left, right);
                            case LE -> doLessThanOrEqual(left, right);
                            case GE -> doGreaterThanOrEqual(left, right);
                            case EQ -> doEqual(left, right);
                            case NE -> doNotEqual(left, right);
                            default -> new ErrorValue("Invalid operation: " + this.op);
                        };
                    }
                }
            } else {
                result = new ErrorValue("Binary operator cannot be evaluated without 2 children");
            }
        }

        return result;
    }

    /**
     * Determine the logical AND between any number of Boolean values. The result is a Boolean.
     *
     * @param context the evaluation context
     * @return {@code TRUE} if all terms are TRUE; {@code FALSE} otherwise, or an {@code ErrorValue} if either term is not
     *         a {@code Boolean}
     */
    private Object doAnd(final EvalContext context) {

        Object result = null;

        final int count = numChildren();
        if (count == 0) {
            result = new ErrorValue("AND operator cannot be evaluated without children");
        } else {
            boolean allTrue = true;

            for (int i = 0; i < count; ++i) {
                final Object child = getChild(i).evaluate(context);

                if (child instanceof final ErrorValue error) {
                    result = error;
                    break;
                }

                if (child instanceof final Boolean booleanChild) {
                    if (!booleanChild.booleanValue()) {
                        allTrue = false;
                    }
                } else {
                    result = new ErrorValue("Can only perform AND operation on boolean values.");
                    break;
                }
            }

            if (result == null) {
                result = Boolean.valueOf(allTrue);
            }
        }

        return result;
    }

    /**
     * Determine the logical OR between any number of Boolean values. The result is a Boolean.
     *
     * @param context the evaluation context
     * @return {@code TRUE} if at least one term is TRUE; {@code FALSE} otherwise, or an {@code ErrorValue} if either term
     *         is not a {@code Boolean}
     */
    private Object doOr(final EvalContext context) {

        Object result = null;

        final int count = numChildren();
        if (count == 0) {
            result = new ErrorValue("OR operator cannot be evaluated without children");
        } else {
            boolean anyTrue = false;

            for (int i = 0; i < count; ++i) {
                final Object child = getChild(i).evaluate(context);

                if (child instanceof final ErrorValue error) {
                    result = error;
                    break;
                }

                if (child instanceof final Boolean booleanChild) {
                    if (booleanChild.booleanValue()) {
                        anyTrue = true;
                    }
                } else {
                    result = new ErrorValue("Can only perform OR operation on boolean values.");
                    break;
                }
            }

            if (result == null) {
                result = Boolean.valueOf(anyTrue);
            }
        }

        return result;
    }

    /**
     * Calculates the sum of a list of numerical arguments.
     *
     * @param context the evaluation context
     * @return the sum - a {@code Long} if all arguments are integers, a {@code Double} if at least one is a real
     */
    private Object doAdd(final EvalContext context) {

        Object result = null;

        final int count = numChildren();
        if (count == 0) {
            result = new ErrorValue("+ operator cannot be evaluated without children");
        } else {
            long longValue = 0L;
            double doubleValue = 0.0;
            boolean allInteger = true;

            for (int i = 0; i < count; ++i) {
                final Object child = getChild(i).evaluate(context);

                if (child instanceof final ErrorValue error) {
                    result = error;
                    break;
                }

                if (child instanceof final Long longChild) {
                    if (allInteger) {
                        longValue += longChild.longValue();
                    }
                    doubleValue += longChild.doubleValue();
                } else if (child instanceof final Number numberChild) {
                    doubleValue += numberChild.doubleValue();
                    allInteger = false;
                } else {
                    result = new ErrorValue("Can only perform + operation on numeric values.");
                    break;
                }
            }

            if (result == null) {
                if (allInteger) {
                    result = Long.valueOf(longValue);
                } else {
                    result = Double.valueOf(doubleValue);
                }
            }
        }

        return result;
    }

    /**
     * Calculates the Boolean result of an "approximately equal to" operation. This operation takes three numeric
     * arguments, where the last is a tolerance value. If the absolute value of the difference between the first two
     * arguments is less than or equal to this tolerance, TRUE is returned; otherwise FALSE is returned. If there are
     * not three arguments, or if any returns a non-numeric result, an ErrorValue is returned.
     *
     * @param context the evaluation context
     * @return the result
     */
    private Object doApprox(final EvalContext context) {

        final Object result;

        final int count = numChildren();
        if (count == 3) {
            final Object child1 = getChild(0).evaluate(context);

            if (child1 instanceof ErrorValue) {
                result = child1;
            } else {
                final Object child2 = getChild(1).evaluate(context);
                if (child2 instanceof ErrorValue) {
                    result = child2;
                } else {
                    final Object child3 = getChild(2).evaluate(context);
                    if (child3 instanceof ErrorValue) {
                        result = child3;
                    } else if (child1 instanceof final Number num1
                            && child2 instanceof final Number num2
                            && child3 instanceof final Number num3) {

                        final double absDiff = Math.abs(num1.doubleValue() - num2.doubleValue());
                        final double tol = Math.abs(num3.doubleValue());

                        result = Boolean.valueOf(absDiff <= tol);
                    } else {
                        result = new ErrorValue("Can only perform approximately equal to operation on numeric values.");
                    }
                }
            }
        } else {
            result = new ErrorValue("approximately equal to operator requires exactly three children");
        }

        return result;
    }

    /**
     * Subtract one value from another. If both terms are integers, the result is an integer. Otherwise, the result is a
     * real.
     *
     * @param left  the left term
     * @param right the right term
     * @return the difference
     */
    private static Object doSubtract(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                final long diffInt = lLong.longValue() - rLong.longValue();
                result = Long.valueOf(diffInt);
            } else if (right instanceof final Number rNum) {
                final double diffReal = lLong.doubleValue() - rNum.doubleValue();
                result = Double.valueOf(diffReal);
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            final double diffReal = lNum.doubleValue() - rNum.doubleValue();
            result = Double.valueOf(diffReal);
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't subtract ", right.getClass().getSimpleName(), " from ",
                    left.getClass().getSimpleName(), " value"));
        }

        return result;
    }

    /**
     * Multiply two values. If both multiplicands are integers, the result is an integer. Otherwise, the result is a
     * real.
     *
     * @param left  the left multiplicand
     * @param right the right multiplicand
     * @return the product
     */
    private static Object doMultiply(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                final long prodInt = lLong.longValue() * rLong.longValue();
                result = Long.valueOf(prodInt);
            } else if (right instanceof final Number rNum) {
                final double prodReal = (double) lLong.longValue() * rNum.doubleValue();
                result = Double.valueOf(prodReal);
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            final double prodReal = lNum.doubleValue() * rNum.doubleValue();
            result = Double.valueOf(prodReal);
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't multiply ", left.getClass().getSimpleName(), " and ",
                    right.getClass().getSimpleName(), " value"));
        }

        return result;
    }

    /**
     * Divide two values. The result will be a real unless both the numerator and denominator are integers and the
     * denominator divides the numerator, in which case the result is an integer.
     *
     * @param left  the numerator
     * @param right the denominator
     * @return the quotient
     */
    private static Object doDivide(final Object left, final Object right) {

        Object result = null;

        if (right instanceof final Number rNum && rNum.doubleValue() == 0.0) {
            result = new ErrorValue("Divide by zero");
        } else if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                final long lint = lLong.longValue();
                final long rint = rLong.longValue();

                // See if denominator divides numerator.
                if (lint % rint == 0L) {
                    result = Long.valueOf(lint / rint);
                } else {
                    final double quot = lLong.doubleValue() / rLong.doubleValue();
                    result = Double.valueOf(quot);
                }
            } else if (right instanceof final Number rNum) {
                final double quot = lLong.doubleValue() / rNum.doubleValue();
                result = Double.valueOf(quot);
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            final double quot = lNum.doubleValue() / rNum.doubleValue();
            result = Double.valueOf(quot);
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't divide ", left.getClass().getSimpleName(), " by ",
                    right.getClass().getSimpleName(), " value"));
        }

        return result;
    }

    /**
     * Raise a value to a power. If both terms are integers, and the exponent is positive, the result is an integer.
     * Otherwise, the result is a real.
     *
     * @param left  the base
     * @param right the exponent
     * @return the result
     */
    private static Object doPower(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                final double powerReal = StrictMath.pow(lLong.doubleValue(), rLong.doubleValue());
                if (rLong.longValue() >= 0L) {
                    // For positive integer powers, result is an integer if possible
                    final long powerInt = (long) powerReal;
                    if ((double) powerInt == powerReal) {
                        result = Long.valueOf(powerInt);
                    } else {
                        result = Double.valueOf(powerReal);
                    }
                } else {
                    result = Double.isNaN(powerReal)
                            ? new ErrorValue("Unable to compute " + left + "^" + right + " (1)")
                            : Double.valueOf(powerReal);
                }
            } else if (right instanceof final Number rNum) {
                final double powerReal = StrictMath.pow(lLong.doubleValue(), rNum.doubleValue());
                result = Double.isNaN(powerReal)
                        ? new ErrorValue("Unable to compute " + left + "^" + right + " (2)")
                        : Double.valueOf(powerReal);
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            final double powerReal = StrictMath.pow(lNum.doubleValue(), rNum.doubleValue());
            result = Double.isNaN(powerReal) //
                    ? new ErrorValue("Unable to compute " + left + "^" + right + " (4)")
                    : Double.valueOf(powerReal);
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't raise " + left.getClass().getSimpleName(), " to ",
                    right.getClass().getSimpleName(), " power"));
        }

        return result;
    }

    /**
     * Compute a modulus. If both terms are integers, the result is an integer. Otherwise, the result is a real.
     *
     * @param left  the base
     * @param right the modulus
     * @return the base modulo the modulus
     */
    private static Object doModulo(final Object left, final Object right) {

        Object result = null;

        if (right instanceof final Number rNum && rNum.doubleValue() == 0.0) {
            result = new ErrorValue("Modulus zero");
        } else if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                final long modInt = lLong.longValue() % rLong.longValue();
                result = Long.valueOf(modInt);
            } else if (right instanceof final Number rNum) {
                final double modReal = (double) lLong.longValue() % rNum.doubleValue();
                result = Double.valueOf(modReal);
            }
        } else if (left instanceof final Number lNum) {
            if (right instanceof final Long rLong) {
                final double modReal = lNum.doubleValue() % (double) rLong.longValue();
                result = Double.valueOf(modReal);
            } else if (right instanceof final Number rNum) {
                final double modReal = lNum.doubleValue() % rNum.doubleValue();
                result = Double.valueOf(modReal);
            }
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't evaluate ", left.getClass().getSimpleName(),
                    " modulo ", right.getClass().getSimpleName()));
        }

        return result;
    }

    /**
     * Determine whether a number is less than another number. The result is a Boolean.
     *
     * @param left  the first term
     * @param right the second term
     * @return {@code TRUE} if the first term is less than the second term; {@code FALSE} if not, and an {@code ErrorValue}
     *         if either term is non-numeric
     */
    private static Object doLessThan(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                result = Boolean.valueOf(lLong.longValue() < rLong.longValue());
            } else if (right instanceof final Number rNum) {
                result = Boolean.valueOf(lLong.doubleValue() < rNum.doubleValue());
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            result = Boolean.valueOf(lNum.doubleValue() < rNum.doubleValue());
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't evaluate ", left.getClass().getSimpleName(),
                    " less than ", right.getClass().getSimpleName()));
        }

        return result;
    }

    /**
     * Determine whether a number is greater than another number. The result is a Boolean.
     *
     * @param left  the first term
     * @param right the second term
     * @return {@code TRUE} if the first term is greater than the second term; {@code FALSE} if not, and an {@code
     *         ErrorValue} if either term is non-numeric
     */
    private static Object doGreaterThan(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                result = Boolean.valueOf(lLong.longValue() > rLong.longValue());
            } else if (right instanceof final Number rNum) {
                result = Boolean.valueOf(lLong.doubleValue() > rNum.doubleValue());
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            result = Boolean.valueOf(lNum.doubleValue() > rNum.doubleValue());
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't evaluate ", left.getClass().getSimpleName(),
                    " greater than ", right.getClass().getSimpleName()));
        }

        return result;
    }

    /**
     * Determine whether a number is less than another number. The result is a Boolean.
     *
     * @param left  the first term
     * @param right the second term
     * @return {@code TRUE} if the first term is less than or equal to the second term; {@code FALSE} if not, and an {@code
     *         ErrorValue} if either term is non-numeric
     */
    private static Object doLessThanOrEqual(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                result = Boolean.valueOf(lLong.longValue() <= rLong.longValue());
            } else if (right instanceof final Number rNum) {
                result = Boolean.valueOf(lLong.doubleValue() <= rNum.doubleValue());
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            result = Boolean.valueOf(lNum.doubleValue() <= rNum.doubleValue());
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't evaluate ", left.getClass().getSimpleName(),
                    " less than or equal ", right.getClass().getSimpleName()));
        }

        return result;
    }

    /**
     * Determine whether a number is greater than another number. The result is a Boolean.
     *
     * @param left  the first term
     * @param right the second term
     * @return {@code TRUE} if the first term is greater than or equal to the second term; {@code FALSE} if not, and an
     *         {@code ErrorValue} if either term is non-numeric
     */
    private static Object doGreaterThanOrEqual(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                result = Boolean.valueOf(lLong.longValue() >= rLong.longValue());
            } else if (right instanceof final Number rNum) {
                result = Boolean.valueOf(lLong.doubleValue() >= rNum.doubleValue());
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            result = Boolean.valueOf(lNum.doubleValue() >= rNum.doubleValue());
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't evaluate ", left.getClass().getSimpleName(),
                    " greater than or equal ", right.getClass().getSimpleName()));
        }

        return result;
    }

    /**
     * Determine whether a number is equal to another number, or whether two Boolean values are the same. The result is
     * a Boolean. If an integer is being compared to a real value, equality is harder to test. In this case, we need
     * equality to within one bit's difference in the real value.
     *
     * @param left  the first term
     * @param right the second term
     * @return {@code TRUE} if the first term is equal to the second term; {@code FALSE} if not, and an {@code ErrorValue}
     *         if the two values cannot be compared
     */
    private static Object doEqual(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Boolean lBool && right instanceof final Boolean rBool) {
            result = Boolean.valueOf(lBool.booleanValue() == rBool.booleanValue());
        } else if (left instanceof String && right instanceof String) {
            result = Boolean.valueOf(right.equals(left));
        } else if (left instanceof final DocSimpleSpan lSpan && right instanceof final DocSimpleSpan rSpan) {
            final String lXml = lSpan.toXml(0);
            final String rXml = rSpan.toXml(0);
            result = Boolean.valueOf(lXml.equals(rXml));
        } else if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                result = Boolean.valueOf(lLong.longValue() == rLong.longValue());
            } else if (right instanceof final Number rNum) {
                result = Boolean.valueOf(lLong.doubleValue() == rNum.doubleValue());
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            result = Boolean.valueOf(lNum.doubleValue() == rNum.doubleValue());
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't test equality of ",
                    left.getClass().getSimpleName(), " and ", right.getClass().getSimpleName()));
        }

        return result;
    }

    /**
     * Determine whether a number is not equal to another number, or whether two Boolean values are not the same. The
     * result is a Boolean.
     *
     * @param left  the first term
     * @param right the second term
     * @return {@code TRUE} if the first term is not equal to the second term; {@code FALSE} if not, and an {@code
     *         ErrorValue} if the two values cannot be compared
     */
    private static Object doNotEqual(final Object left, final Object right) {

        Object result = null;

        if (left instanceof final Boolean lBool && right instanceof final Boolean rBool) {
            result = Boolean.valueOf(lBool.booleanValue() != rBool.booleanValue());
        } else if (left instanceof String && right instanceof String) {
            result = Boolean.valueOf(!left.equals(right));
        } else if (right instanceof final DocSimpleSpan lSpan && left instanceof final DocSimpleSpan rSpan) {
            final String lXml = lSpan.toXml(0);
            final String rXml = rSpan.toXml(0);
            result = Boolean.valueOf(!lXml.equals(rXml));
        } else if (left instanceof final Long lLong) {
            if (right instanceof final Long rLong) {
                result = Boolean.valueOf(lLong.longValue() != rLong.longValue());
            } else if (right instanceof final Number rNum) {
                result = Boolean.valueOf(lLong.doubleValue() != rNum.doubleValue());
            }
        } else if (left instanceof final Number lNum && right instanceof final Number rNum) {
            result = Boolean.valueOf(lNum.doubleValue() != rNum.doubleValue());
        }

        if (result == null) {
            result = new ErrorValue(SimpleBuilder.concat("Can't test inequality of ",
                    left.getClass().getSimpleName(), " and ", right.getClass().getSimpleName()));
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
     * @return the simplified version of this object (returns this object itself if already simplified)
     */
    @Override
    public AbstractFormulaObject simplify(final EvalContext context) {

        final AbstractFormulaObject result;

        boolean canEvaluate = false;

        AbstractFormulaObject child0 = getChild(0);
        AbstractFormulaObject child1 = getChild(1);
        if (child0 != null && child1 != null) {
            if (!child0.isConstant()) {
                final AbstractFormulaObject newChild0 = child0.simplify(context);
                if (newChild0 != child0) {
                    setChild(0, newChild0);
                }
                child0 = newChild0;
            }

            if (!child1.isConstant()) {
                final AbstractFormulaObject newChild1 = child1.simplify(context);
                if (newChild1 != child1) {
                    setChild(1, newChild1);
                }
                child1 = newChild1;
            }

            canEvaluate = child0.isConstant() && child1.isConstant();
        }

        if (canEvaluate) {
            final Object value = evaluate(context);

            result = switch (value) {
                case final Long longVal -> new ConstIntegerValue(longVal.longValue());
                case final Number numVal -> new ConstRealValue(numVal);
                case final Boolean booleanVal -> new ConstBooleanValue(booleanVal.booleanValue());
                case null, default -> this;
            };
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

        final FEBinaryOper result = new FEBinaryOper(theFontSize, this.op);

        if (numChildren() > 0) {
            final AbstractFormulaObject arg1 = getChild(0);
            if (arg1 instanceof final IEditableFormulaObject editable) {
                result.setArg1(editable.generateFEObject(theFontSize), false);
            }
        }

        if (numChildren() > 1) {
            final AbstractFormulaObject arg2 = getChild(1);
            if (arg2 instanceof final IEditableFormulaObject editable) {
                result.setArg2(editable.generateFEObject(theFontSize), false);
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
        } else if (obj instanceof final BinaryOper oper) {
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

        final String charString = Character.toString(this.op.op);
        final String escaped = XmlEscaper.escape(charString);

        xml.add("<binary op='", escaped, "'>");
        appendChildrenXml(xml);
        xml.add("</binary>");
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

        xml.addln(ind, "Binary Operator: ", this.op);
        printChildrenDiagnostics(xml, indent + 1);
    }
}
