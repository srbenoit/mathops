package dev.mathops.app.sim.registration;

/**
 * An available classroom.
 *
 * @param id          the classroom ID
 * @param hoursPerDay the number of hours per day the classroom is open
 * @param capacity    the capacity
 */
record AvailableClassroom(int id, int hoursPerDay, int capacity) {
}
