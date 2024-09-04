package dev.mathops.app.sim.schedule;

/**
 * Possible days on which a Monday/Wednesday/Friday section will meet.
 */
public enum EMeetingDaysTR {

    /** The section will meet Tuesday and Thursday. */
    TR,

    /** The section will meet Tuesday. */
    T,

    /** The section will meet Thursday. */
    R;

    /**
     * Tests if this set of meeting days includes Tuesday.
     *
     * @return true of the set of days includes Tuesday
     */
    public boolean includesTuesday() {

        return this == TR || this == T;
    }

    /**
     * Tests if this set of meeting days includes Thursday.
     *
     * @return true of the set of days includes Thursday
     */
    public boolean includesThursday() {

        return this == TR || this == R;
    }
}
