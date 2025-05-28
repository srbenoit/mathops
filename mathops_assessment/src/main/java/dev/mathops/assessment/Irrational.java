package dev.mathops.assessment;

import dev.mathops.commons.log.Log;

import java.io.Serial;

// MOVE TO Mathemetric "math" library

/**
 * A constant irrational value consisting of a fraction (with integer numerator and denominator) and an irrational
 * factor which can be PI or E or the square root of a positive integer. No effort is made in the case of a square root
 * to ensure the root is actually irrational.
 */
public final class Irrational extends Number {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 4139505768172978960L;

    /** The factor. */
    public final EIrrationalFactor factor;

    /** The number whose root is to be taken. */
    public final long base;

    /** The coefficient numerator. */
    public final long numerator;

    /** The coefficient denominator. */
    public final long denominator;

    /**
     * Constructs a new {@code Irrational}.
     *
     * @param theFactor      the factor (may not be SQRT)
     * @param theNumerator   the numerator
     * @param theDenominator the denominator (must be positive, nonzero)
     */
    public Irrational(final EIrrationalFactor theFactor, final long theNumerator, final long theDenominator) {
        super();

        if (theFactor == null) {
            throw new IllegalArgumentException("Factor may not be null");
        }
        if (theFactor == EIrrationalFactor.SQRT) {
            throw new IllegalArgumentException("Square root factor must include base");
        }
        if (theDenominator <= 0L) {
            throw new IllegalArgumentException("Denominator must be positive, nonzero");
        }

        this.factor = theFactor;
        this.base = 0L;
        this.numerator = theNumerator;
        this.denominator = theDenominator;
    }

    /**
     * Constructs a new {@code Irrational}.
     *
     * @param theFactor      the factor (must be SQRT)
     * @param theBase        the base (must be positive, nonzero)
     * @param theNumerator   the numerator
     * @param theDenominator the denominator (must be positive, nonzero)
     */
    public Irrational(final EIrrationalFactor theFactor, final long theBase, final long theNumerator,
                      final long theDenominator) {
        super();

        if (theFactor == null) {
            throw new IllegalArgumentException("Factor may not be null");
        }
        if (theFactor != EIrrationalFactor.SQRT) {
            throw new IllegalArgumentException("Only square root factor may include base");
        }
        if (theDenominator <= 0L) {
            throw new IllegalArgumentException("Denominator must be positive, nonzero");
        }
        if (theBase <= 0L) {
            throw new IllegalArgumentException("Base must be positive, nonzero");
        }

        this.factor = theFactor;
        this.base = theBase;
        this.numerator = theNumerator;
        this.denominator = theDenominator;
    }

    /**
     * Attempts to parse an irrational from a string.
     *
     * <p>
     * Supported formats include:
     *
     * <pre>
     * 3PI/4  Any string with "PI" is treated as a fraction with PI in the numerator.  There can
     *        be an integer coefficient before PI, and optionally a denominator made up of a
     *        slash character followed by an integer.  No characters between the PI and slash are
     *        allowed.
     * 3E/4   Same format as above, but with the constant "E" rather than "PI".
     * 3R2/2  Any string with "R" is treated as a rational multiple of the square root of an
     *        integer.  The format has an optional integer coefficient before the R, a required
     *        integer after the R (whose square root is to be taken), and an optional denominator
     *        consisting of a slash character then an integer.
     * </pre>
     *
     * @param str the string
     * @return the parsed {@code Irrational}.
     * @throws NumberFormatException if the string is not in a valid format
     */
    public static Irrational valueOf(final String str) throws NumberFormatException {

        final Irrational result;

        final int piPos = str.indexOf("PI");
        if (piPos == -1) {
            final int ePos = str.indexOf('E');
            if (ePos == -1) {
                final int rPos = str.indexOf('R');
                if (rPos == -1) {
                    throw new NumberFormatException("Invalid irrational number: " + str);
                }
                result = valueOfSqrt(str, rPos);
            } else {
                result = valueOfE(str, ePos);
            }
        } else {
            result = valueOfPi(str, piPos);
        }

        return result;
    }

    /**
     * Attempts to parse an irrational from a string that contains "PI".
     *
     * @param str   the string
     * @param piPos the index of the "PI" substring
     * @return the parsed {@code Irrational}.
     * @throws NumberFormatException if the string is not in a valid format
     */
    private static Irrational valueOfPi(final String str, final int piPos) {

        final Irrational result;

        final int len = str.length();

        if (piPos == len - 2) {
            if (piPos == 0) {
                // Format: "PI"
                result = new Irrational(EIrrationalFactor.PI, 1L, 1L);
            } else {
                // Format: "10PI"
                final String numStr = str.substring(0, piPos);
                final long numer = "-".equals(numStr) ? -1L : Long.parseLong(numStr);
                result = new Irrational(EIrrationalFactor.PI, numer, 1L);
            }
        } else if ((int) str.charAt(piPos + 2) == (int) '/') {
            final long denom = Long.parseLong(str.substring(piPos + 3));
            if (piPos == 0) {
                // Format: "PI/4"
                result = new Irrational(EIrrationalFactor.PI, 1L, denom);
            } else {
                // Format: "5PI/4"
                final String numStr = str.substring(0, piPos);
                final long numer = "-".equals(numStr) ? -1L : Long.parseLong(numStr);
                result = new Irrational(EIrrationalFactor.PI, numer, denom);
            }
        } else {
            throw new NumberFormatException("Invalid irrational number: " + str);
        }

        return result;
    }

    /**
     * Attempts to parse an irrational from a string that contains "E".
     *
     * @param str  the string
     * @param ePos the index of the "E" substring
     * @return the parsed {@code Irrational}.
     * @throws NumberFormatException if the string is not in a valid format
     */
    private static Irrational valueOfE(final String str, final int ePos)
            throws NumberFormatException {

        final Irrational result;

        final int len = str.length();

        if (ePos == len - 1) {
            if (ePos == 0) {
                // Format: "E"
                result = new Irrational(EIrrationalFactor.E, 1L, 1L);
            } else {
                // Format: "10E"
                final String numStr = str.substring(0, ePos);
                final long numer = "-".equals(numStr) ? -1L : Long.parseLong(numStr);
                result = new Irrational(EIrrationalFactor.E, numer, 1L);
            }
        } else if (str.charAt(ePos + 1) == '/') {
            final long denom = Long.parseLong(str.substring(ePos + 2));
            if (ePos == 0) {
                // Format: "E/4"
                result = new Irrational(EIrrationalFactor.E, 1L, denom);
            } else {
                // Format: "5E/4"
                final String numStr = str.substring(0, ePos);
                final long numer = "-".equals(numStr) ? -1L : Long.parseLong(numStr);
                result = new Irrational(EIrrationalFactor.E, numer, denom);
            }
        } else {
            throw new NumberFormatException("Invalid irrational number: " + str);
        }

        return result;
    }

    /**
     * Attempts to parse an irrational from a string that contains "R".
     *
     * @param str  the string
     * @param rPos the index of the "R" substring
     * @return the parsed {@code Irrational}.
     * @throws NumberFormatException if the string is not in a valid format
     */
    private static Irrational valueOfSqrt(final String str, final int rPos)
            throws NumberFormatException {

        final Irrational result;

        final int slashPos = str.indexOf('/');

        if (slashPos == -1) {
            final long base = Long.parseLong(str.substring(rPos + 1));

            if (rPos == 0) {
                // Format: R3
                result = new Irrational(EIrrationalFactor.SQRT, base, 1L, 1L);
            } else {
                // Format: 2R3
                final String numStr = str.substring(0, rPos);
                final long numer = "-".equals(numStr) ? -1L : Long.parseLong(numStr);
                result = new Irrational(EIrrationalFactor.SQRT, base, numer, 1L);
            }
        } else if (slashPos > rPos) {
            final long base = Long.parseLong(str.substring(rPos + 1, slashPos));
            final long denom = Long.parseLong(str.substring(slashPos + 1));

            if (rPos == 0) {
                // Format: R3/4
                result = new Irrational(EIrrationalFactor.SQRT, base, 1L, denom);
            } else {
                // Format: 2R3/4
                final String numStr = str.substring(0, rPos);
                final long numer = "-".equals(numStr) ? -1L : Long.parseLong(numStr);
                result = new Irrational(EIrrationalFactor.SQRT, base, numer, denom);
            }

        } else {
            throw new NumberFormatException("Invalid irrational number: " + str);
        }

        return result;
    }

    /**
     * Returns the value of the specified number as an {@code int}.
     *
     * @return the numeric value represented by this object after conversion to type {@code int}.
     */
    @Override
    public int intValue() {

        return (int) Math.round(doubleValue());
    }

    /**
     * Returns the value of the specified number as a {@code long}.
     *
     * @return the numeric value represented by this object after conversion to type {@code long}.
     */
    @Override
    public long longValue() {

        return Math.round(doubleValue());
    }

    /**
     * Returns the value of the specified number as a {@code float}.
     *
     * @return the numeric value represented by this object after conversion to type {@code float}.
     */
    @Override
    public float floatValue() {

        return (float) doubleValue();
    }

    /**
     * Returns the value of the specified number as a {@code double}.
     *
     * @return the numeric value represented by this object after conversion to type {@code double}.
     */
    @Override
    public double doubleValue() {

        return switch (this.factor) {
            case PI -> Math.PI * (double) this.numerator / (double) this.denominator;
            case E -> Math.E * (double) this.numerator / (double) this.denominator;
            case SQRT -> Math.sqrt((double) this.base) * (double) this.numerator / (double) this.denominator;
        };
    }

    /**
     * Gets the numeric value of the factor.
     *
     * @return the numeric value
     */
    public double getFactorValue() {

        return switch (this.factor) {
            case PI -> Math.PI;
            case E -> Math.E;
            case SQRT -> Math.sqrt((double) this.base);
        };
    }

    /**
     * Gets the string representation of the factor.
     *
     * @return the string
     */
    public String getFactorString() {

        return switch (this.factor) {
            case PI -> "\u03c0";
            case E -> "e";
            case SQRT -> "\u221A" + this.base;
        };
    }

    /**
     * Generates a hash code for this value.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        final long value = ((long) this.factor.hashCode() << 24) + (this.base << 16) + (this.numerator << 8)
                + this.denominator;

        return Long.hashCode(value);
    }

    /**
     * Tests whether this value is equal to another. To be equal, the other value must be an {@code Irrational} that
     * evaluates to the same double value.
     *
     * @param obj the other value
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final Irrational irrat) {
            equal = this.doubleValue() == irrat.doubleValue();
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates the string representation of the number.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final StringBuilder result = new StringBuilder(16);

        switch (this.factor) {
            case PI:
                if (this.numerator != 1L) {
                    result.append(this.numerator);
                }
                result.append("PI");
                if (this.denominator != 1L) {
                    result.append('/');
                    result.append(this.denominator);
                }
                break;

            case E:
                if (this.numerator != 1L) {
                    result.append(this.numerator);
                }
                result.append("E");
                if (this.denominator != 1L) {
                    result.append('/');
                    result.append(this.denominator);
                }
                break;

            case SQRT:
                if (this.numerator != 1L) {
                    result.append(this.numerator);
                }
                result.append("R");
                result.append(this.base);
                if (this.denominator != 1L) {
                    result.append('/');
                    result.append(this.denominator);
                }
                break;

            default:
                Log.warning("Unsuported Irrational factor");
                break;
        }

        return result.toString();
    }
}
