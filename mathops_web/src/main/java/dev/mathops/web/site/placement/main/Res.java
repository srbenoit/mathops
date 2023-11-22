package dev.mathops.web.site.placement.main;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by Site

    /** A resource key. */
    static final String UNRECOGNIZED_PATH = key(index++);

    // Used by Page

    /** A resource key. */
    static final String ERR_NO_STUDENT = key(index++);

    /** A resource key. */
    static final String ERR_SEND_EMAIL_PRE = key(index++);

    /** A resource key. */
    static final String ERR_SEND_EMAIL_POST = key(index++);

    /** A resource key. */
    static final String LOGGED_IN_AS = key(index++);

    /** A resource key. */
    static final String BACK_TO_HOME = key(index++);

    /** A resource key. */
    static final String BACK_TO_SECURE = key(index++);

    /** A resource key. */
    static final String BACK_TO_TOOL = key(index++);

    // Used by PageExploing

    /** A resource key. */
    static final String EXPLORE_MAJORS_BTN = key(index++);

    // Used by PageSecure

    /** A resource key. */
    static final String SECURE_NEXT_STEP_BTN = key(index++);

    /** A resource key. */
    static final String SECURE_SUBMIT_MAJORS_BTN = key(index++);

    /** A resource key. */
    static final String SECURE_UPDATE_MAJORS_BTN = key(index++);

    /** A resource key. */
    static final String SECURE_CUR_MAJOR = key(index++);

    // Used by PageHistory

    /** A resource key. */
    static final String HISTORY_XFER_CREDIT_SUB = key(index++);

    /** A resource key. */
    static final String HISTORY_NONE_ON_FILE = key(index++);

    /** A resource key. */
    static final String HISTORY_ANY_COURSES_MISSING = key(index++);

    // Used by PagePlan

    /** A resource key. */
    static final String PLAN_M101_TITLE = key(index++);

    /** A resource key. */
    static final String PLAN_M101_LABEL = key(index++);

    /** A resource key. */
    static final String PLAN_YOUR_FIRST_COURSE = key(index++);

    /** A resource key. */
    static final String PLAN_YOUR_FIRST_COURSES = key(index++);

    /** A resource key. */
    static final String PLAN_THESE_COURSES = key(index++);

    /** A resource key. */
    static final String PLAN_IDEALLY_SINGULAR = key(index++);

    /** A resource key. */
    static final String PLAN_IDEALLY_PLURAL = key(index++);

    /** A resource key. */
    static final String PLAN_1A_SINGULAR_1 = key(index++);

    /** A resource key. */
    static final String PLAN_1A_SINGULAR_2 = key(index++);

    /** A resource key. */
    static final String PLAN_1A_PLURAL_1 = key(index++);

    /** A resource key. */
    static final String PLAN_1A_PLURAL_2 = key(index++);

    /** A resource key. */
    static final String RAMREADY_SVC = key(index++);

    /** A resource key. */
    static final String MISSING_XFER_HEADING = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {UNRECOGNIZED_PATH, "Unrecognized path: {0}"},

            {ERR_NO_STUDENT, "It seems that your student record is not yet available. "
                    + "&nbsp;Please try this site again later."},
            {ERR_SEND_EMAIL_PRE, "If you see this message more than 24 hours after "
                    + "creating your eID, please send an e-mail to "},
            {ERR_SEND_EMAIL_POST, " to let us know. &nbsp;Please include your CSU ID number in your e-mail."},
            {LOGGED_IN_AS, "Logged in as <strong>{0}</strong>"},
            {BACK_TO_HOME, "Back to <span class='hideabove300'><br></span>Home Page"},
            {BACK_TO_SECURE, "Back to <span class='hideabove300'><br></span>Placement Process"},
            {BACK_TO_TOOL, "Back to Math <span class='hideabove300'><br></span>Placement Tool"},

            {EXPLORE_MAJORS_BTN, "Math requirements for specific majors..."},

            {SECURE_NEXT_STEP_BTN, "Go to the next step..."},
            {SECURE_SUBMIT_MAJORS_BTN, "Submit my <span class='hidebelow500'>possible</span> majors list"},
            {SECURE_UPDATE_MAJORS_BTN, "Update my <span class='hidebelow500'>possible</span> majors list"},
            {SECURE_CUR_MAJOR, "This is currently your declared major."},

            {HISTORY_XFER_CREDIT_SUB, "(Includes exam credit: AP/IB/CLEP)"},
            {HISTORY_NONE_ON_FILE, "None on file"},
            {HISTORY_ANY_COURSES_MISSING, "Click if not up to date..."},

            {PLAN_M101_TITLE, "MATH 101: Math in the Social Sciences"},
            {PLAN_M101_LABEL, "MATH 101"},
            {PLAN_YOUR_FIRST_COURSE, "your first semester course"},
            {PLAN_YOUR_FIRST_COURSES, "your first semester courses"},
            {PLAN_THESE_COURSES, "those courses"},
            {PLAN_IDEALLY_SINGULAR, "Ideally, you will register for this course during Orientation."},
            {PLAN_IDEALLY_PLURAL, "Ideally, you will register for those courses during Orientation."},
            {PLAN_1A_SINGULAR_1, "It is important to satisfy the prerequisite for {0} "
                    + "so you can take it during {1}."},
            {PLAN_1A_SINGULAR_2, "It is important to satisfy the prerequisites for {0} "
                    + "so you can take it during {1}."},
            {PLAN_1A_PLURAL_1, "It is important to satisfy the prerequisite for {0} "
                    + "so you can take those courses during {1}."},
            {PLAN_1A_PLURAL_2, "It is important to satisfy the prerequisites for {0} "
                    + "so you can take those courses during {1}."},

            {RAMREADY_SVC, "RamReady Mathematics Plan and Placement Status Service"},

            {MISSING_XFER_HEADING,
                    "If my <span class='hidebelow500'>Mathematics</span> Transfer Credit is not up to date:"},

            //
    };

    /** The singleton instance. */
    private static final Res instance = new Res();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Res() {

        super(Locale.US, EN_US);
    }

    /**
     * Gets the message with a specified key using the current locale.
     *
     * @param key the message key
     * @return the best-matching message, an empty string if none is registered (never {@code null})
     */
    public static String get(final String key) {

        return instance.getMsg(key);
    }

    /**
     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format that message pattern with
     * a collection of arguments.
     *
     * @param key       the message key
     * @param arguments the arguments, as for {@code MessageFormat}
     * @return the formatted string (never {@code null})
     */
    static String fmt(final String key, final Object... arguments) {

        return instance.formatMsg(key, arguments);
    }
}
