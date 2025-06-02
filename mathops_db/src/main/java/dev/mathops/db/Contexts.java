package dev.mathops.db;

import dev.mathops.commons.CoreConstants;

/**
 * A container for the context definitions in the active system.
 */
public enum Contexts {
    ;

    /** The default domain. */
    public static final String DOMAIN = "math.colostate.edu";

    /** A server host. */
    public static final String PRECALC_HOST = "precalc." + DOMAIN;

    /** A server host. */
    public static final String PLACEMENT_HOST = "placement." + DOMAIN;

    /** A server host. */
    public static final String COURSE_HOST = "course." + DOMAIN;

    /** A server host. */
    public static final String TESTING_HOST = "testing." + DOMAIN;

    /** A server host. */
    public static final String ONLINE_HOST = "online." + DOMAIN;

    /** A server host. */
    public static final String NIBBLER_HOST = "nibbler." + DOMAIN;

    /** A server host. */
    public static final String PRECALCDEV_HOST = "precalcdev." + DOMAIN;

    /** A server host. */
    public static final String PLACEMENTDEV_HOST = "placementdev." + DOMAIN;

    /** A server host. */
    public static final String COURSEDEV_HOST = "coursedev." + DOMAIN;

    /** A server host. */
    public static final String TESTINGDEV_HOST = "testingdev." + DOMAIN;

    //
    //

    /** A common path. */
    public static final String ROOT_PATH = CoreConstants.SLASH;

    /** A common path. */
    public static final String TXN_PATH = "/txn";

    /** A common path. */
    public static final String WEBSVC_PATH = "/websvc";

    /** A common path. */
    public static final String INSTRUCTION_PATH = "/instruction";

    /** A common path. */
    public static final String CANVAS_PATH = "/canvas";

    /** A common path. */
    public static final String TESTING_CENTER_PATH = "/testing-center";

    /** A common path. */
    public static final String ELM_TUTORIAL_PATH = "/elm-tutorial";

    /** A common path. */
    public static final String PRECALC_TUTORIAL_PATH = "/precalc-tutorial";

    /** A common path. */
    public static final String WELCOME_PATH = "/welcome";

    /** A common path. */
    public static final String LTI_PATH = "/lti";

    /** A common path. */
    public static final String CSU_MATH_COURSE_MGR_PATH = "/csu_math_course_mgr";

    /** A common path. */
    public static final String VIDEO_PATH = "/video";

    /** A common path. */
    public static final String CFM_PATH = "/cfm";

    /** A common path. */
    public static final String ADMINSYS_PATH = "/adminsys";

    /** A common path. */
    public static final String MPS_PATH = "/mps";

    /** A common path. */
    public static final String MPSMEDIA_PATH = "/mps-media";

    /** A common path. */
    public static final String HELP_PATH = "/help";

    /** A common path. */
    public static final String RAMWORK_PATH = "/ramwork";

    /** A common path. */
    public static final String REPORTING_PATH = "/reporting";

    /** A common path. */
    public static final String SCHEDULING_PATH = "/scheduling";

    /** A common path. */
    public static final String CHECKIN_PATH = "checkin";

    /** A common path. */
    public static final String CHECKOUT_PATH = "checkout";

    /** A common path. */
    public static final String INFORMIX_TEST_PATH = "ifxtest";

    /** A common path. */
    public static final String POSTGRES_TEST_PATH = "pgtest";

    /** A common path. */
    public static final String BATCH_PATH = "batch";

    /** A common path. */
    public static final String REPORT_PATH = "report";

    /** A common path. */
    public static final String ADMPROD_PATH = "admprod";

    /** A common path. */
    public static final String ADMDEV_PATH = "admdev";

    /** A common path. */
    public static final String FA21 = "fa21";

    /** A common path. */
    public static final String POSTGRES = "postgres";

}
