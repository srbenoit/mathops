package dev.mathops.assessment.formula;

/**
 * Functions.
 */
public enum EFunction {

    /** Absolute value (Maps Integer to Integer, Real to Real). */
    ABS("abs", '\u2720'),

    /** Cosine (Maps Integer or Real to Real). */
    COS("cos", '\u2721'),

    /** Sine (Maps Integer or Real to Real). */
    SIN("sin", '\u2722'),

    /** Tangent (Maps Integer or Real to Real). */
    TAN("tan", '\u2723'),

    /** Secant (Maps Integer or Real to Real). */
    SEC("sec", '\u2738'),

    /** Cosecant (Maps Integer or Real to Real). */
    CSC("csc", '\u2739'),

    /** Cotangent (Maps Integer or Real to Real). */
    COT("cot", '\u273A'),

    /** Arccosine (Maps Integer or Real to Real). */
    ACOS("acos", '\u2724'),

    /** Arcsine (Maps Integer or Real to Real). */
    ASIN("asin", '\u2725'),

    /** Arctangent (Maps Integer or Real to Real). */
    ATAN("atan", '\u2726'),

    /** Natural exponential (Maps Integer or Real to Real). */
    EXP("exp", '\u2727'),

    /** Natural logarithm (Maps Integer or Real to Real). */
    LOG("log", '\u2728'),

    /** Ceiling (Maps Integer or Real to Integer). */
    CEIL("ceil", '\u2729'),

    /** Floor (Maps Integer or Real to Integer). */
    FLOOR("floor", '\u272A'),

    /** Round (Maps Integer or Real to Integer). */
    ROUND("round", '\u272B'),

    /** Square root (Maps Integer or Real to Real). */
    SQRT("sqrt", '\u272C'),

    /** Cube root (Maps Integer or Real to Real). */
    CBRT("cbrt", '\u272D'),

    /** Radians to Degrees (Maps Integer or Real to Real). */
    TO_DEG("toDeg", '\u272E'),

    /** Degrees to radians (Maps Integer or Real to Real). */
    TO_RAD("toRad", '\u272F'),

    /** Logical not (Maps Boolean to Boolean). */
    NOT("not", '\u2730'),

    /** Greatest common divisor (Maps Integer Vector to Integer). */
    GCD("gcd", '\u2731'),

    /** Greatest factor of number that is a perfect square (Maps Integer to Integer). */
    SRAD2("srad2", '\u2732'),

    /** Greatest factor of number that is a perfect cube (Maps Integer to Integer). */
    SRAD3("srad3", '\u2733'),

    /** Converts string to lowercase. */
    LCASE("lcase", '\u2734'),

    /** Converts string to uppercase. */
    UCASE("ucase", '\u2735'),

    /** Degrees to numerator of fraction in radian representation (Maps Integer to Integer). */
    RAD_NUM("radNum", '\u2736'),

    /** Degrees to denominator of fraction in radian representation (Maps Integer to Integer). */
    RAD_DEN("radDen", '\u2737'),

    /** Least common multiple (Maps Integer Vector to Integer). */
    LCM("lcm", '\u2738'),

    /** Approximately equal to (Maps Number, Number, Number to Boolean). */
    APPROX("approx", '\u2739');

    /** The function name. */
    public final String name;

    /**
     * A character that can represent this function in a String (these are selected from the Unicode "Dingbats" range to
     * avoid potential conflicts with other characters in a string that represents a mathematical expression).
     */
    private final char ch;

    /**
     * Constructs a new {@code EFunction}.
     *
     * @param theName the function name
     * @param theCh   a character that can represent this function in a String
     */
    EFunction(final String theName, final char theCh) {

        this.name = theName;
        this.ch = theCh;
    }

    /**
     * Retrieves the {@code EFunction} that has a specified name.
     *
     * @param theName the function name
     * @return the corresponding {@code EFunction}; {@code null} if none matches
     */
    public static EFunction forName(final String theName) {

        EFunction result = null;

        for (final EFunction test : values()) {
            if (test.name.equals(theName)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Retrieves the {@code EFunction} that has a specified character.
     *
     * @param theCh the character
     * @return the corresponding {@code EFunction}; {@code null} if none matches
     */
    public static EFunction forChar(final char theCh) {

        EFunction result = null;

        for (final EFunction test : values()) {
            if (test.ch == theCh) {
                result = test;
                break;
            }
        }

        return result;
    }
}
