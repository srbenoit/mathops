package dev.mathops.app.sim.registration;

/**
 * An assignment that occupies some block(s) of time in a {@code Room}.
 */
record RoomAssignment(Room room, int id, int num25MinBlocks, EAssignmentType type) {
}
