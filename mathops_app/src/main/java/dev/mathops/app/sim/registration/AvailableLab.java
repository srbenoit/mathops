package dev.mathops.app.sim.registration;

/**
 * An available lab.
 *
 * @param id          the ab ID
 * @param hoursPerDay the number of hours per day the lab is open
 * @param capacity    the capacity
 */
record AvailableLab(int id, int hoursPerDay, int capacity) {
}
