package dev.mathops.app.scheduling;

/**
 * A container for student preferences for office hours.
 */
public final class StudentPreferences {

    /**
     * Preferences where [A][B] means weekday A, time slot B, and the value is 0 if no preference, -1 if "unable to make
     * office hours in that slot", and +1 if "able to make office hours in that slot".
     */
    private final int[][] preferences;

    /**
     * Constructs a new {@code StudentPreferences}.
     *
     * @param numTimeslots the number of timeslots
     */
    StudentPreferences(final int numTimeslots) {

        this.preferences = new int[5][numTimeslots];
    }

    /**
     * Gets the preference for a weekday and timeslot.
     *
     * @param weekdayIndex  the weekday index
     * @param timeslotIndex the timeslot index
     * @return 0 if no preference, -1 if "unable to make office hours in that slot", and +1 if "able to make office
     *         hours in that slot".
     */
    public int get(final int weekdayIndex, final int timeslotIndex) {

        return this.preferences[weekdayIndex][timeslotIndex];
    }

    /**
     * Sets the preference for a weekday and timeslot.
     *
     * @param weekdayIndex  the weekday index
     * @param timeslotIndex the timeslot index
     * @param pref          0 if no preference, -1 if "unable to make office hours in that slot", and +1 if "able to
     *                      make office hours in that slot".
     */
    public void set(final int weekdayIndex, final int timeslotIndex, final int pref) {

        this.preferences[weekdayIndex][timeslotIndex] = pref;
    }
}
