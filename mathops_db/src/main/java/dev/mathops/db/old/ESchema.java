package dev.mathops.db.old;

/**
 * The types of schemas in the PostgreSQL database.
 */
public enum ESchema {

    /** The main schema. */
    MAIN,

    /** The term schema (a separate schema exists for each term). */
    TERM,

    /** The analytics schema. */
    ANALYTICS
}
