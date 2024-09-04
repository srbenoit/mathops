package dev.mathops.app.sim.schedule;

/**
 * Types of assignment of a section to a room.
 */
public enum EAssignmentType {

    /** The assignment needs to be a set of contiguous blocks on a single weekday. */
    CONTIGUOUS,

    /** The assignment prefers to be scheduled in 50-minute blocks, split across multiple days as needed. */
    BLOCKS_OF_50,

    /** The assignment prefers to be scheduled in 75-minute blocks, split across multiple days as needed. */
    BLOCKS_OF_75,

    /**
     * The assignment prefers to be scheduled in either 50-minute blocks or 75-minute blocks, split across multiple days
     * as needed.
     */
    BLOCKS_OF_50_OR_75
}
