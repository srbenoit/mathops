package dev.mathops.db.reclogic.query;

/**
 * Comparison relations.
 */
public enum EStringComparison {

    /** Must match the string exactly. */
    EQUAL,

    /** Matches, ignoring case. */
    EQUAL_IGNORE_CASE,

    /** Matches, using wildcard '%'. */
    LIKE,

    /** Matches, ignoring case, using wildcard '%'. */
    LIKE_IGNORE_CASE
}
