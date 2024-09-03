package dev.mathops.app.sim.registration;

/**
 * Possible days on which a Monday/Wednesday/Friday section will meet.
 */
enum EMeetingDaysMWF {

    /** The section will meet Monday, Wednesday, and Friday. */
    MWF,

    /** The section will meet Monday and Wednesday. */
    MW,

    /** The section will meet Monday and Friday. */
    MF,

    /** The section will meet Wednesday and Friday. */
    WF,

    /** The section will meet Monday. */
    M,

    /** The section will meet Wednesday. */
    W,

    /** The section will meet Friday. */
    F;

    /**
     * Tests if this set of meeting days includes Monday.
     *
     * @return true of the set of days includes Monday
     */
    boolean includesMonday() {

        return this == MWF || this == MW || this == MF || this == M;
    }

    /**
     * Tests if this set of meeting days includes Wednesday.
     *
     * @return true of the set of days includes Wednesday
     */
    boolean includesWednesday() {

        return this == MWF || this == MW || this == WF || this == W;
    }

    /**
     * Tests if this set of meeting days includes Friday.
     *
     * @return true of the set of days includes Friday
     */
    boolean includesFriday() {

        return this == MWF || this == MF || this == WF || this == F;
    }
}
