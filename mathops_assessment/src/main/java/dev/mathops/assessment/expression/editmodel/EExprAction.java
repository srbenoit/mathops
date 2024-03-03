package dev.mathops.assessment.expression.editmodel;

/**
 * Enumerated expression actions.
 *
 * <p>
 * These are enumerated actions in addition to actions indicated by UTF-16 character values:
 * <ul>
 *     <li>All characters, when used in a string, insert that character.</li>
 *     <li>'0' through '9' insert decimal digits</li>
 *     <li>'.' inserts a decimal point</li>
 *     <li>'(' inserts a matched pair of parentheses containing a sub-expression</li>
 *     <li>'[' inserts a matched pair of square brackets containing a sub-expression</li>
 *     <li>'"' inserts a matched pair of double-quotes containing a string literal</li>
 *     <li>'+', '-', '*', '/', '%', '|', '&', '=', '<', '>', or '!' character inserts an operator</li>
 *     <li>The U+03C0 character inserts a PI constant</li>
 *     <li>The U+2147 character inserts an E (base of natural logarithm) constant</li>
 *     <li>The U+2148 character inserts an I (imaginary unit) constant</li>
 *     <li>ASCII and Greek letters interpreted as variable names, where '_' indicates the start of a subscript.</li>
 *     <lI></lI>
 * </ul>
 */
public enum EExprAction {

    /** Insert Boolean TRUE. */
    INSERT_BOOLEAN_TRUE(0x00010001),

    /** Insert Boolean FALSE. */
    INSERT_BOOLEAN_FALSE(0x00010002),

    /** Insert Engineering "E". */
    INSERT_ENGINEERING_E(0x00010003),

    /** Insert a vector with parentheses. */
    INSERT_VECTOR_PARENS(0x00010004),

    /** Insert a vector with BRACKETS. */
    INSERT_VECTOR_BRACKETS(0x00010005),

    /** Insert a matrix with parentheses. */
    INSERT_MATRIX_PARENS(0x00010006),

    /** Insert a matrix with BRACKETS. */
    INSERT_MATRIX_BRACKETS(0x00010007),

    /** Insert a fraction with vertical orientation. */
    INSERT_FRACTION_VERTICAL(0x00010008),

    /** Insert a fraction with slant orientation. */
    INSERT_FRACTION_SLANT(0x00010009),

    /** Insert a fraction with horizontal orientation. */
    INSERT_FRACTION_HORIZONTAL(0x0001000A),

    /** Insert a radical with no root. */
    INSERT_RADICAL(0x0001000B),

    /** Insert a radical with root. */
    INSERT_RADICAL_WITH_ROOT(0x0001000C),

    /** Insert an IF-THEN construction. */
    INSERT_IF_THEN(0x0001000D),

    /** Insert an IF-THEN-ELSE construction. */
    INSERT_IF_THEN_ELSE(0x0001000E),

    /** Insert a SWITCH construction. */
    INSERT_SWITCH(0x0001000F),

    /** Inserts an Absolute value function. */
    INSERT_FXN_ABS(0x00020000),

    /** Inserts a Cosine function (Maps Integer or Real to Real). */
    INSERT_FXN_COS(0x00020001),

    /** Inserts a Sine function (Maps Integer or Real to Real). */
    INSERT_FXN_SIN(0x00020002),

    /** Inserts a Tangent function (Maps Integer or Real to Real). */
    INSERT_FXN_TAN(0x00020003),

    /** Inserts a Secant function (Maps Integer or Real to Real). */
    INSERT_FXN_SEC(0x00020004),

    /** Inserts a Cosecant function (Maps Integer or Real to Real). */
    INSERT_FXN_CSC(0x00020005),

    /** Inserts a Cotangent function (Maps Integer or Real to Real). */
    INSERT_FXN_COT(0x00020006),

    /** Inserts an Arccosine function (Maps Integer or Real to Real). */
    INSERT_FXN_ACOS(0x00020007),

    /** Inserts an Arcsine function (Maps Integer or Real to Real). */
    INSERT_FXN_ASIN(0x00020008),

    /** Inserts an Arctangent function (Maps Integer or Real to Real). */
    INSERT_FXN_ATAN(0x00020009),

    /** Inserts a Natural exponential function (Maps Integer or Real to Real). */
    INSERT_FXN_EXP(0x0002000A),

    /** Inserts a Natural logarithm function (Maps Integer or Real to Real). */
    INSERT_FXN_LOG(0x0002000B),

    /** Inserts a Ceiling function (Maps Integer or Real to Integer). */
    INSERT_FXN_CEIL(0x0002000C),

    /** Inserts a Floor function (Maps Integer or Real to Integer). */
    INSERT_FXN_FLOOR(0x0002000D),

    /** Inserts a Round function (Maps Integer or Real to Integer). */
    INSERT_FXN_ROUND(0x0002000E),

    /** Inserts a Square root function (Maps Integer or Real to Real). */
    INSERT_FXN_SQRT(0x0002000F),

    /** Inserts a Cube root function (Maps Integer or Real to Real). */
    INSERT_FXN_CBRT(0x00020010),

    /** Inserts a Radians to Degrees function (Maps Integer or Real to Real). */
    INSERT_FXN_TO_DEG(0x00020011),

    /** Inserts a Degrees to radians function (Maps Integer or Real to Real). */
    INSERT_FXN_TO_RAD(0x00020012),

    /** Inserts a Logical not function (Maps Boolean to Boolean). */
    INSERT_FXN_NOT(0x00020013),

    /** Inserts a Greatest common divisor function (Maps Integer Vector to Integer). */
    INSERT_FXN_GCD(0x00020014),

    /** Inserts a Least common multiple function (Maps Integer Vector to Integer). */
    INSERT_FXN_LCM(0x00020015),

    /** Inserts a Greatest factor of number that is a perfect square function (Maps Integer to Integer). */
    INSERT_FXN_SRAD2(0x00020016),

    /** Inserts a Greatest factor of number that is a perfect cube function (Maps Integer to Integer). */
    INSERT_FXN_SRAD3(0x00020017),

    /** Inserts a Converts string to lowercase function. */
    INSERT_FXN_LCASE(0x00020018),

    /** Inserts a Converts string to uppercase function. */
    INSERT_FXN_UCASE(0x00020019),

    /** Inserts a Degrees to numerator of fraction in radian representation function (Maps Integer to Integer). */
    INSERT_FXN_RAD_NUM(0x0002001A),

    /** Inserts a Degrees to denominator of fraction in radian representation function (Maps Integer to Integer). */
    INSERT_FXN_RAD_DEN(0x0002001B),

    /** Inserts an Approximately equal (3-argument) function. */
    INSERT_FXN_APPROX_EQUAL(0x0002001C);

    ;

    /** The integer value (must be greater than 0xFFFF). */
    int value;

    /**
     * Constructs a new {@code EExprAction}.
     *
     * @param theValue the integer value (must be greater than 0xFFFF)
     */
    EExprAction(final int theValue) {

        this.value = theValue;
    }
}
