package dev.mathops.app.sim.campus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * A semester schedule, which defines some number of weeks of class sessions (15, 12, 8, 5, etc.), and defines how each
 * day is divided (into 50-minute blocks with 10-minute passing time, or 75-minute blocks with 15-minute passing times,
 * for example), as well as the hours of operations.
 */
public final class SemesterSchedule {

    /** The unique semester schedule ID. */
    private final String id;

    /** The number of weeks of class (this controls the number of contact hours per week each credit-hour requires). */
    private final int weeksOfClass;

    /** The start time for classes each weekday. */
    private final Map<DayOfWeek, LocalTime> startTimes;

    /** The start time for classes each weekday. */
    private final Map<DayOfWeek, LocalTime> endTimes;

    /**
     * The type of schedule each day provides (this is the "campus schedule", but individual classrooms or labs could
     * override this).
     */
    private final Map<DayOfWeek, EDailyScheduleType> dailyScheduleTypes;

    /**
     * Constructs a new {@code SemesterSchedule}.
     */
    SemesterSchedule(final String theId, final int theWeeksOfClass) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Schedule ID may not be null or blank");
        }
        if (theWeeksOfClass < 1) {
            throw new IllegalArgumentException("Number of weeks of class may not be less than 1");
        }

        this.id = theId;
        this.weeksOfClass = theWeeksOfClass;

        this.startTimes = new EnumMap<>(DayOfWeek.class);
        this.endTimes = new EnumMap<>(DayOfWeek.class);
        this.dailyScheduleTypes = new EnumMap<>(DayOfWeek.class);
    }

    /**
     * Gets the schedule ID.
     *
     * @return the schedule ID
     */
    public String getId() {

        return this.id;
    }

    /**
     * Gets the number of weeks of class.
     *
     * @return the number of weeks of class
     */
    public int getWeeksOfClass() {

        return this.weeksOfClass;
    }

    /**
     * Adds a configuration for a day of the week.
     *
     * @param day          the weekday
     * @param startTime    the start time for classes that day
     * @param endTime      the end time for classes that day
     * @param scheduleType the schedule type to use that day
     */
    public void addDayOfWeek(final DayOfWeek day, final LocalTime startTime, final LocalTime endTime,
                             final EDailyScheduleType scheduleType) {

        if (day == null) {
            throw new IllegalArgumentException("Day of week cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time may not be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time may not be null");
        }
        if (endTime.isAfter(startTime)) {
            if (scheduleType == null) {
                throw new IllegalArgumentException("Schedule type may not be null");
            }

            this.startTimes.put(day, startTime);
            this.endTimes.put(day, endTime);
            this.dailyScheduleTypes.put(day, scheduleType);
        } else {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    /**
     * Delete the configuration for a day of the week.
     *
     * @param day the weekday
     */
    public void deleteDayOfWeek(final DayOfWeek day) {

        if (day == null) {
            throw new IllegalArgumentException("Day of week cannot be null");
        }

        this.startTimes.remove(day);
        this.endTimes.remove(day);
        this.dailyScheduleTypes.remove(day);
    }
}
