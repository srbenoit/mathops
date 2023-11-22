package dev.mathops.web.site.help.student;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by various pages

    /** A resource key. */
    static final String SITE_TITLE = key(index++);

    // Used by PageLiveHelp

    /** A resource key. */
    static final String LIVE_HELP_HEADING = key(index++);

    /** A resource key. */
    static final String MISSING_HW_ASSIGN = key(index++);

    /** A resource key. */
    static final String MISSING_EX_XML = key(index++);

    /** A resource key. */
    static final String MISSING_LE_COURSE = key(index++);

    /** A resource key. */
    static final String MISSING_LE_UNIT = key(index++);

    /** A resource key. */
    static final String MISSING_LE_LESSON = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {SITE_TITLE, "Help Queue"},

            {LIVE_HELP_HEADING, "Online Math Course Assistants"},

            {MISSING_HW_ASSIGN, "Missing 'assign' parameter in HW help request"},
            {MISSING_EX_XML, "Missing 'xml' parameter in EX help request"},
            {MISSING_LE_COURSE, "Missing 'course' parameter in LE help request"},
            {MISSING_LE_UNIT, "Missing 'unit' parameter in LE help request"},
            {MISSING_LE_LESSON, "Missing 'lesson' parameter in LE help request"},

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
    static String get(final String key) {

        return instance.getMsg(key);
    }
}
