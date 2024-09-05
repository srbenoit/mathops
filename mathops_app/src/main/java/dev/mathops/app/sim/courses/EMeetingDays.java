package dev.mathops.app.sim.courses;

/**
 * Possible days on which an offered section will meet.
 */
public enum EMeetingDays {

    /** The section will meet Monday, Wednesday, and Friday. */
    MWF,

    /** The section will meet Monday and Wednesday. */
    MW,

    /** The section will meet Monday and Friday. */
    MF,

    /** The section will meet Wednesday and Friday. */
    WF,

    /** The section will meet Tuesday and Thursday. */
    TR,

    /** The section will meet Monday. */
    M,

    /** The section will meet Tuesday. */
    T,

    /** The section will meet Wednesday. */
    W,

    /** The section will meet Thursday. */
    R,

    /** The section will meet Friday. */
    F;

    /**
     * Tests whether a set of meeting days includes Monday.
     *
     * @return true if Monday is included
     */
    public boolean includesMonday() {

        return this == MWF || this == MW || this == MF || this == M;
    }

    /**
     * Tests whether a set of meeting days includes Tuesday.
     *
     * @return true if Tuesday is included
     */
    public boolean includesTuesday() {

        return this == TR || this == T;
    }

    /**
     * Tests whether a set of meeting days includes Wednesday.
     *
     * @return true if Wednesday is included
     */
    public boolean includesWednesday() {

        return this == MWF || this == MW || this == WF || this == W;
    }

    /**
     * Tests whether a set of meeting days includes Thursday.
     *
     * @return true if Thursday is included
     */
    public boolean includesThursday() {

        return this == TR || this == R;
    }

    /**
     * Tests whether a set of meeting days includes Friday.
     *
     * @return true if Friday is included
     */
    public boolean includesFriday() {

        return this == MWF || this == MF || this == WF || this == F;
    }
}
