package dev.mathops.assessment.expression.editmodel;

/**
 * Function names.
 */
public enum EFunctionName {

    /** Absolute value. */
    ABS,

    /** Cosine (Maps Integer or Real to Real). */
    COS,

    /** Sine (Maps Integer or Real to Real). */
    SIN,

    /** Tangent (Maps Integer or Real to Real). */
    TAN,

    /** Secant (Maps Integer or Real to Real). */
    SEC,

    /** Cosecant (Maps Integer or Real to Real). */
    CSC,

    /** Cotangent (Maps Integer or Real to Real). */
    COT,

    /** Arccosine (Maps Integer or Real to Real). */
    ACOS,

    /** Arcsine (Maps Integer or Real to Real). */
    ASIN,

    /** Arctangent (Maps Integer or Real to Real). */
    ATAN,

    /** Natural exponential (Maps Integer or Real to Real). */
    EXP,

    /** Natural logarithm (Maps Integer or Real to Real). */
    LOG,

    /** Ceiling (Maps Integer or Real to Integer). */
    CEIL,

    /** Floor (Maps Integer or Real to Integer). */
    FLOOR,

    /** Round (Maps Integer or Real to Integer). */
    ROUND,

    /** Square root (Maps Integer or Real to Real). */
    SQRT,

    /** Cube root (Maps Integer or Real to Real). */
    CBRT,

    /** Radians to Degrees (Maps Integer or Real to Real). */
    TO_DEG,

    /** Degrees to radians (Maps Integer or Real to Real). */
    TO_RAD,

    /** Logical not (Maps Boolean to Boolean). */
    NOT,

    /** Greatest common divisor (Maps Integer Vector to Integer). */
    GCD,

    /** Least common multiple (Maps Integer Vector to Integer). */
    LCM,

    /** Greatest factor of number that is a perfect square (Maps Integer to Integer). */
    SRAD2,

    /** Greatest factor of number that is a perfect cube (Maps Integer to Integer). */
    SRAD3,

    /** Converts string to lowercase. */
    LCASE,

    /** Converts string to uppercase. */
    UCASE,

    /** Degrees to numerator of fraction in radian representation (Maps Integer to Integer). */
    RAD_NUM,

    /** Degrees to denominator of fraction in radian representation (Maps Integer to Integer). */
    RAD_DEN,

    /** Approximately equal (3-argument). */
    APPROX_EQUAL;
}
