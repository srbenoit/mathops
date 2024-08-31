package dev.mathops.app.sim.registration;

/**
 * An assignment that occupies some block(s) of time in a {@code Room}.
 */
record RoomAssignment(int id, Room room, Course course, int numSeats, ERoomUsage usage, int num25MinBlocks,
                      EAssignmentType type) {
}
