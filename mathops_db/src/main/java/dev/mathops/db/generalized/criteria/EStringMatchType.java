package dev.mathops.db.generalized.criteria;

/**
 * Types of String matching.
 */
public enum EStringMatchType {

    /** Match null values. */
    IS_NULL,

    /** Match non-null values. */
    IS_NOT_NULL,

    /** Match strings that are exactly equal to any match string.  */
    EXACT_IN,

    /** Match strings that are not exactly equal to any match string.  */
    EXACT_NOT_IN,

    /** Match strings that are case-insensitive matches to any match string.  */
    CASE_INSENSITIVE_IN,

    /** Match strings that are not case-insensitive matches to any match string.  */
    CASE_INSENSITIVE_NOT_IN,

    /** Match strings that start with a part exactly equal to any match string.  */
    EXACT_STARTS_WITH,

    /** Match strings that start with a part not exactly equal to any match string.  */
    EXACT_NOT_STARTS_WITH,

    /** Match strings that start with a part case-insensitive matches to any match string.  */
    CASE_INSENSITIVE_STARTS_WITH,

    /** Match strings that start with a part not case-insensitive matches to any match string.  */
    CASE_INSENSITIVE_NOT_STARTS_WITH,
}
