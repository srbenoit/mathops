package dev.mathops.app.sim.registration.rooms;

/**
 * An assignment that occupies some block(s) of time in a {@code Room}.
 */
public record RoomAssignment(int id, int num25MinBlocks, boolean contiguous) {
}
