package dev.mathops.db.old.reclogic.query;

/**
 * Comparison relations.
 */
public enum EComparison {

    /** Must match a value exactly. */
    EQUAL,

    /** Must not match a value. */
    UNEQUAL,

    /** Must be less than a value. */
    LESS_THAN,

    /** Must be less than or equal to a value. */
    LESS_THAN_OR_EQUAL,

    /** Must be greater than a value. */
    GREATER_THAN,

    /** Must be greater than or equal to a value. */
    GREATER_THAN_OR_EQUAL
}
