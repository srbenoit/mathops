package dev.mathops.db.generalized.criteria;

/**
 * Types of Boolean matching.
 */
public enum EBooleanMatchType {

    /** Match null values. */
    IS_NULL,

    /** Match non-null values. */
    IS_NOT_NULL,

    /** Match TRUE values.  */
    IS_TRUE,

    /** Match FALSE values.  */
    IS_FALSE
}
