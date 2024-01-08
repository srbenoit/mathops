package dev.mathops.db.generalized.criteria;

/**
 * Types of temporal matching.
 */
public enum ETemporalMatchType {

    /** Match null values. */
    IS_NULL,

    /** Match non-null values. */
    IS_NOT_NULL,

    /** Match values that are exactly equal to any match values.  */
    EXACT_IN,

    /** Match values that are not exactly equal to any match values.  */
    EXACT_NOT_IN,

    /** Match values that are greater than a given lower bound.  */
    GREATER_THAN,

    /** Match values that are greater than or equal to a given lower bound.  */
    GREATER_THAN_OR_EQUAL,

    /** Match values that are less than a given lower bound.  */
    LESS_THAN,

    /** Match values that are less than or equal to a given lower bound.  */
    LESS_THAN_OR_EQUAL,

    /** Match values that are between two bounds, excluding the bounds.  */
    BETWEEN_EXCLUDE_BOUNDS,

    /** Match values that are between two bounds, excluding the lower bound.  */
    BETWEEN_EXCLUDE_LOWER_BOUND,

    /** Match values that are between two bounds, excluding the upper bound.  */
    BETWEEN_EXCLUDE_UPPER_BOUND,

    /** Match values that are between two bounds, including the bounds.  */
    BETWEEN_INCLUDE_BOUNDS,
}
