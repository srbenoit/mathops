package dev.mathops.assessment.formula;

import dev.mathops.assessment.EIrrationalFactor;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.Irrational;
import dev.mathops.assessment.formula.edit.AbstractFEObject;
import dev.mathops.assessment.formula.edit.FEFunction;
import dev.mathops.assessment.formula.edit.IEditableFormulaObject;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;

/**
 * A built-in function.
 */
public final class Function extends AbstractFormulaContainer implements IEditableFormulaObject {

    /** The name of the function. */
    private final EFunction function;

    /**
     * Construct a new {@code Function}.
     *
     * @param theFunction the name of the function
     * @throws IllegalArgumentException if the function name is invalid
     */
    public Function(final EFunction theFunction) {

        super();

        if (theFunction == null) {
            throw new IllegalArgumentException("Function type may not be null");
        }

        this.function = theFunction;
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public Function deepCopy() {

        final Function copy = new Function(this.function);

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

        final HtmlBuilder builder = new HtmlBuilder(50);

        builder.add(this.function.name);

        if (numChildren() == 0 || getChild(0) == null) {
            builder.add("(null)");
        } else if (getChild(0) instanceof GroupingOper) {
            builder.add(getChild(0).toString());
        } else {
            builder.add("(", getChild(0).toString(), ")");
        }

        return builder.toString();
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

        switch (this.function) {
            case ABS:
                EType argType = null;
                if (numChildren() > 0) {
                    argType = getChild(0).getType(context);
                }
                result = argType == EType.INTEGER ? EType.INTEGER : EType.REAL;
                break;

            case COS:
            case SIN:
            case TAN:
            case SEC:
            case CSC:
            case COT:
            case ACOS:
            case ASIN:
            case ATAN:
            case EXP:
            case LOG:
            case SQRT:
            case CBRT:
            case TO_DEG:
            case TO_RAD:
                result = EType.REAL;
                break;

            case CEIL:
            case FLOOR:
            case ROUND:
            case GCD:
            case LCM:
            case SRAD2:
            case SRAD3:
            case RAD_NUM:
            case RAD_DEN:
                result = EType.INTEGER;
                break;

            case LCASE:
            case UCASE:
                result = EType.STRING;
                break;

            case NOT:
                result = EType.BOOLEAN;
                break;

            default:
                result = EType.ERROR;
                break;
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

        if (getChild(0) == null) {
            return new ErrorValue(//
                    "Function cannot be evaluated without an argument.");
        }

        final Object arg = getChild(0).evaluate(context);

        final Object result;
        if (arg instanceof ErrorValue) {
            result = arg;
        } else {
            result = switch (this.function) {
                case ABS -> abs(arg);
                case ACOS -> acos(arg);
                case ASIN -> asin(arg);
                case ATAN -> atan(arg);
                case CBRT -> cbrt(arg);
                case CEIL -> ceil(arg);
                case COS -> cos(arg);
                case COT -> cot(arg);
                case CSC -> csc(arg);
                case EXP -> exp(arg);
                case FLOOR -> floor(arg);
                case GCD -> gcd(arg);
                case LCM -> lcm(arg);
                case LOG -> log(arg);
                case NOT -> not(arg);
                case ROUND -> round(arg);
                case SEC -> sec(arg);
                case SIN -> sin(arg);
                case SQRT -> sqrt(arg);
                case SRAD2 -> srad2(arg);
                case SRAD3 -> srad3(arg);
                case TAN -> tan(arg);
                case TO_DEG -> toDeg(arg);
                case TO_RAD -> toRad(arg);
                case LCASE -> lcase(arg);
                case UCASE -> ucase(arg);
                case RAD_NUM -> radNum(arg);
                case RAD_DEN -> radDen(arg);
                default -> new ErrorValue("Unsupported function");
            };
        }

        return result;
    }

    /**
     * Compute the absolute value of an argument.
     *
     * @param arg the argument of the function
     * @return a Long or Double to provide the integer or real value of the object, or an {@code ErrorValue} with an
     *         error message if unable to compute
     */
    private static Object abs(final Object arg) {

        final Object result;

        if (arg instanceof final Long argLong) {
            result = Long.valueOf(Math.abs(argLong.longValue()));
        } else if (arg instanceof final Irrational argIrr) {
            final EIrrationalFactor fac = argIrr.factor;
            if (fac == EIrrationalFactor.SQRT) {
                result = new Irrational(EIrrationalFactor.SQRT, argIrr.base, Math.abs(argIrr.numerator),
                        argIrr.denominator);
            } else {
                result = new Irrational(fac, Math.abs(argIrr.numerator), argIrr.denominator);
            }
        } else if (arg instanceof final Number argNum) {
            result = Double.valueOf(Math.abs(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'abs' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the cosine of an argument (which must be in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object cos(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.cos(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'cos' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the sine of an argument (which must be in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object sin(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.sin(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'sin' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the tangent of an argument (which must be in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object tan(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.tan(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'tan' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the secant of an argument (which must be in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object sec(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(1.0 / StrictMath.cos(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'sec' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the cosecant of an argument (which must be in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object csc(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(1.0 / StrictMath.sin(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'csc' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the cotangent of an argument (which must be in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object cot(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(1.0 / StrictMath.tan(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'cot' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the value of E raised to an exponent.
     *
     * @param arg the exponent to which to raise E
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object exp(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.exp(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'exp' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the logarithm (base E) of an argument.
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object log(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.log(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'log' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the boolean complement of an argument.
     *
     * @param arg the argument of the function
     * @return the Boolean complement of the object's Boolean value, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object not(final Object arg) {

        if (arg instanceof final Boolean argBool) {
            return Boolean.valueOf(!argBool.booleanValue());
        }

        return new ErrorValue("Argument to 'not' was invalid type.");
    }

    /**
     * Compute the GCD of a vector argument.
     *
     * @param arg the argument of the function
     * @return the GCD of the elements of the vector, which must have elements that all evaluate to integers; otherwise,
     *         an {@code ErrorValue} with an error message
     */
    private static Object gcd(final Object arg) {

        final Object result;

        if (arg instanceof final IntegerVectorValue vec) {
            // Get the first object from the vector, make sure it's an integer
            long gcd = vec.getElement(0);

            // Now loop through each subsequent element, accumulating it into the GCD calculation
            final int count = vec.getNumElements();

            for (int i = 1; i < count; ++i) {
                final long elem = vec.getElement(i);

                final BigInteger newgcd = new BigInteger(Long.toString(gcd))
                        .gcd(new BigInteger(Long.toString(Math.round((double)elem))));
                gcd = Long.parseLong(newgcd.toString());
            }

            result = Long.valueOf(gcd);
        } else {
            result = new ErrorValue("Argument to 'gcd' was not integer vector.");
        }

        return result;
    }

    /**
     * Compute the LCM of a vector argument.
     *
     * @param arg the argument of the function
     * @return the LCM of the elements of the vector, which must have elements that all evaluate to integers; otherwise,
     *         an {@code ErrorValue} with an error message
     */
    private static Object lcm(final Object arg) {

        final Object result;

        if (arg instanceof final IntegerVectorValue vec) {
            // Get the first object from the vector, make sure it's an integer
            long lcm = vec.getElement(0);

            // Now loop through each subsequent element, accumulating it into the GCD calculation
            final int count = vec.getNumElements();

            for (int i = 1; i < count; ++i) {
                final long elem = vec.getElement(i);

                final BigInteger newgcd = new BigInteger(Long.toString(lcm))
                        .gcd(new BigInteger(Long.toString(Math.round((double)elem))));
                lcm = lcm * elem / Long.parseLong(newgcd.toString());
            }

            result = Long.valueOf(lcm);
        } else {
            result = new ErrorValue("Argument to 'lcm' was not integer vector.");
        }

        return result;
    }

    /**
     * Compute the arc-cosine of an argument (in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object acos(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.acos(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'acos' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the arc-sine of an argument (in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object asin(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.asin(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'asin' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the arc-tangent of an argument (in radians).
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object atan(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(StrictMath.atan(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'atan' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the ceiling of an argument. That is, the next larger (closer to positive infinity) integer to the
     * argument.
     *
     * @param arg the argument of the function
     * @return a Long to provide the integer value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object ceil(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Long.valueOf((long) Math.ceil(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'ceil' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the square root of an argument.
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object sqrt(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            final double squareRoot = Math.sqrt(argNum.doubleValue());
            if (Double.isNaN(squareRoot)) {
                result = new ErrorValue("Unable to compute square root.");
            } else {
                result = Double.valueOf(squareRoot);
            }
        } else {
            result = new ErrorValue("Argument to 'sqrt' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the cube root of an argument.
     *
     * @param arg the argument of the function
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object cbrt(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            final double squareRoot = StrictMath.cbrt(argNum.doubleValue());
            if (Double.isNaN(squareRoot)) {
                result = new ErrorValue("Unable to compute cube root.");
            } else {
                result = Double.valueOf(squareRoot);
            }
        } else {
            result = new ErrorValue("Argument to 'cbrt' was invalid type.");
        }

        return result;
    }

    /**
     * Compute the floor of an argument. That is, the next smaller (closer to negative infinity) integer to the
     * argument.
     *
     * @param arg the argument of the function
     * @return a Long to provide the integer value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object floor(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Long.valueOf((long) Math.floor(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'floor' was invalid type.");
        }

        return result;
    }

    /**
     * Round an argument to the nearest integer.
     *
     * @param arg the argument of the function
     * @return a Long to provide the integer value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object round(final Object arg) {

        final Object result;

        if (arg instanceof Long) {
            result = arg; // Already an integer
        } else if (arg instanceof final Number argNum) {
            result = Long.valueOf(Math.round(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'round' was invalid type.");
        }

        return result;
    }

    /**
     * Convert a given value in radians to degrees.
     *
     * @param arg the radian measure
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object toDeg(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(Math.toDegrees(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'toDeg' was invalid type.");
        }

        return result;
    }

    /**
     * Convert a given value in degrees to radians.
     *
     * @param arg the degree measure
     * @return a Double to provide the real value of the object, or an {@code ErrorValue} with an error message if
     *         unable to compute
     */
    private static Object toRad(final Object arg) {

        final Object result;

        if (arg instanceof final Number argNum) {
            result = Double.valueOf(Math.toRadians(argNum.doubleValue()));
        } else {
            result = new ErrorValue("Argument to 'toRad' was invalid type.");
        }

        return result;
    }

    /**
     * Given an integer degree measure, generates the numerator in the fraction part of the corresponding radian
     * measure. For example, given 150 degrees, which is 5 PI / 6 radians, this returns 5.
     *
     * @param arg the degree measure
     * @return a Long with the numerator
     */
    private static Object radNum(final Object arg) {

        final Object result;

        if (arg instanceof final Long argLong) {

            // Fraction is "d/180" - find GCD of d and 180, then divide d by this...
            final BigInteger biggcd = new BigInteger(argLong.toString()).gcd(new BigInteger("180"));
            final long numer = argLong.longValue() / Long.parseLong(biggcd.toString());
            result = Long.valueOf(numer);
        } else {
            result = new ErrorValue("Argument to 'radNum' was invalid type.");
        }

        return result;
    }

    /**
     * Given an integer degree measure, generates the denominator in the fraction part of the corresponding radian
     * measure. For example, given 150 degrees, which is 5 PI / 6 radians, this returns 6.
     *
     * @param arg the degree measure
     * @return a Long with the denominator
     */
    private static Object radDen(final Object arg) {

        final Object result;

        if (arg instanceof final Long argLong) {

            // Fraction is "d/180" - find GCD of d and 180, then divide 180 by this...
            final BigInteger biggcd = new BigInteger(argLong.toString()).gcd(new BigInteger("180"));
            final long denom = 180L / Long.parseLong(biggcd.toString());
            result = Long.valueOf(denom);
        } else {
            result = new ErrorValue("Argument to 'radDen' was invalid type.");
        }

        return result;
    }

    /**
     * Determine the largest integer that can be factored out of a square root. For example, if the argument is 20,
     * which is 2 sqrt(5), this function returns 2. Users can then divide the initial argument by the square of the
     * result to get what remains under the radical.
     *
     * @param arg the integer value under the radical
     * @return the integer that can be factored out of the radical
     */
    private static Object srad2(final Object arg) {

        final Object result;

        if (arg instanceof final Long argLong) {
            long val = argLong.longValue();
            if (val == 0L) {
                result = argLong;
            } else {
                if (val < 0L) {
                    val = -val;
                }

                // Now we loop to find all perfect squares that are factors
                long test = 2L;
                long square = test * test;

                long factor = 1L;
                while (square <= val) {

                    while (square * (val / square) == val) {
                        factor *= test;
                        val /= square;
                    }

                    test++;
                    square = test * test;
                }

                result = Long.valueOf(factor);
            }
        } else {
            result = new ErrorValue("Argument to 'srad2' was invalid type.");
        }

        return result;
    }

    /**
     * Determine the largest integer that can be factored out of a cube root. For example, if the argument is 40, which
     * is 2 cuberoot(5), this function returns 2. Users can then divide the initial argument by the cube of the result
     * to get what remains under the radical.
     *
     * @param arg the integer value under the radical
     * @return the integer that can be factored out of the radical
     */
    private static Object srad3(final Object arg) {

        final Object result;

        if (arg instanceof final Long argLong) {
            long val = argLong.longValue();
            if (val == 0L) {
                result = argLong;
            } else {
                if (val < 0L) {
                    val = -val;
                }

                // Now we loop to find all perfect squares that are factors
                long test = 2L;
                long cube = test * test * test;

                long factor = 1L;
                while (cube <= val) {

                    while (cube * (val / cube) == val) {
                        factor *= test;
                        val /= cube;
                    }

                    test++;
                    cube = test * test * test;
                }

                result = Long.valueOf(factor);
            }
        } else {
            result = new ErrorValue("Argument to 'srad3' was invalid type.");
        }

        return result;
    }

    /**
     * Convert a string to lowercase.
     *
     * @param arg the string
     * @return the lowercase string, an ErrorValue if the argument is not a string
     */
    private static Object lcase(final Object arg) {

        final Object result;

        if (arg instanceof final String stringArg) {
            result = stringArg.toLowerCase(Locale.US);
        } else {
            result = new ErrorValue("Argument to 'lcase' was invalid type.");
        }

        return result;
    }

    /**
     * Convert a string to uppercase.
     *
     * @param arg the string
     * @return the lowercase string, an ErrorValue if the argument is not a string
     */
    private static Object ucase(final Object arg) {

        final Object result;

        if (arg instanceof final String stringArg) {
            result = stringArg.toUpperCase(Locale.US);
        } else {
            result = new ErrorValue("Argument to 'ucase' was invalid type.");
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

        final FEFunction result = new FEFunction(theFontSize, this.function);

        if (numChildren() > 0) {
            final AbstractFormulaObject arg = getChild(0);
            if (arg instanceof final IEditableFormulaObject editable) {
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

        return innerHashCode() + Objects.hashCode(this.function);
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
        } else if (obj instanceof final Function fxn) {
            equal = innerEquals(fxn) && Objects.equals(this.function, fxn.function);
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

        xml.add("<function name='", this.function.name, "'>");
        appendChildrenXml(xml);
        xml.add("</function>");
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

        xml.addln(ind, "Function: ", this.function);
        printChildrenDiagnostics(xml, indent + 1);
    }
}
