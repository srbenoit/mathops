package dev.mathops.app.sim.registration;

/**
 * Types of room assignment.
 */
public enum EAssignmentType {

    /** The assignment needs to be a set of contiguous blocks on a single weekday. */
    CONTIGUOUS,

    /**
     * The assignment prefers groups of 2 blocks each, split across multiple days as needed.
     */
    GROUPS_OF_2,

    /** The assignment prefers groups of 3 blocks each, split across multiple days as needed. */
    GROUPS_OF_3,

    /**
     * The assignment prefers either groups of 2 blocks each or groups of 3 blocks each, split across multiple days as
     * needed.
     */
    GROUPS_OF_2_OR_3
}
