package dev.mathops.app.sim.semester;

/**
 * Daily schedule types.  For a semester schedule configuration, each day of the week would adopt one of these schedule
 * types, and all classrooms would operate on that schedule type.  This would not prevent, say, a 75-minute class from
 * occupying two consecutive 50-minute blocks on a day with that schedule type, or a 50-minute class from occupying a
 * 75-minute block if necessary.
 */
public enum EDailyScheduleType {

    /** Classes meet 50 minutes with 10-minute passing time between classes. */
    CLASS_50_PASSING_10,

    /** Classes meet 75 minutes with 15-minute passing time between classes. */
    CLASS_75_PASSING_15
}
