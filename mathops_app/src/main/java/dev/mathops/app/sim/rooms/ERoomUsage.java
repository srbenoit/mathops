package dev.mathops.app.sim.rooms;

/**
 * Room usages.  A course can require multiple rooms (classroom, lab, recitation space, studio, etc.) each with its own
 * usage type, and defines a number of contact hours per week in each type of room.
 *
 * <p>
 * NOTE: the order of entries in this enumeration is significant.  Rooms will be allocated for each usage in turn,
 * so usages where room space is more constructed (for example, for labs that require several hours of contiguous time
 * in the same room) should be listed first, and usages with fewer restrictions (say, tutoring hours) should be listed
 * last.
 */
public enum ERoomUsage {

    /** A lab space. */
    LAB,

    /** A classroom. */
    CLASSROOM,

    /** A classroom. */
    RECITATION
}
