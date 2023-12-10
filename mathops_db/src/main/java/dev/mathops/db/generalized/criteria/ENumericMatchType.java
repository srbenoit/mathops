package dev.mathops.db.generalized.criteria;

/**
 * Types of numeric matching.
 */
public enum ENumericMatchType {

    /** Match null values. */
    IS_NULL,

    /** Match non-null values. */
    IS_NOT_NULL,

    /** Match integers that are exactly equal to any match string.  */
    EXACT_IN,

    /** Match integers that are not exactly equal to any match string.  */
    EXACT_NOT_IN,

    /** Match integers that are greater than a given lower bound.  */
    GREATER_THAN,

    /** Match integers that are greater than or equal to a given lower bound.  */
    GREATER_THAN_OR_EQUAL,

    /** Match integers that are less than a given lower bound.  */
    LESS_THAN,

    /** Match integers that are less than or equal to a given lower bound.  */
    LESS_THAN_OR_EQUAL,

    /** Match integers that are between two bounds, excluding the bounds.  */
    BETWEEN_EXCLUDE_BOUNDS,

    /** Match integers that are between two bounds, excluding the lower bound.  */
    BETWEEN_EXCLUDE_LOWER_BOUND,

    /** Match integers that are between two bounds, excluding the upper bound.  */
    BETWEEN_EXCLUDE_UPPER_BOUND,

    /** Match integers that are between two bounds, including the bounds.  */
    BETWEEN_INCLUDE_BOUNDS,
}
