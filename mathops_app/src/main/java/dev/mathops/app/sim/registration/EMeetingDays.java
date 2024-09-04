package dev.mathops.app.sim.registration;

/**
 * Possible days on which an offered section will meet.
 */
enum EMeetingDays {

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
}
