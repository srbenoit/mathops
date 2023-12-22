package dev.mathops.db.generalized.connection;

/**
 * Connection contexts, which can control which database or schema is used to locate tables.
 */
public enum EConnectionContext {

    /** Production. */
    PRODUCTION,

    /** Development. */
    DEVELOPMENT,

    /** Testing. */
    TESTING,
}
