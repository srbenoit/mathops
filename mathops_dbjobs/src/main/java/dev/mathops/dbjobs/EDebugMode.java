package dev.mathops.dbjobs;

/**
 * A debug mode for batch jobs.
 */
public enum EDebugMode {

    /** Normal mode - all data updates performed. */
    NORMAL,

    /** Debug mode - data updates printed but no changes made to database. */
    DEBUG;
}
