package dev.mathops.app.scheduling;

/**
 * An available lab.
 *
 * @param id          the ab ID
 * @param hoursPerDay the number of hours per day the lab is open
 * @param capacity    the capacity
 */
record AvailableLab(int id, int hoursPerDay, int capacity) {
}
