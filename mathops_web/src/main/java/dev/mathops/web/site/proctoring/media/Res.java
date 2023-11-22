package dev.mathops.web.site.proctoring.media;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by ProctoringMediaSite

    /** A resource key. */
    static final String SITE_TITLE = key(index++);

    /** A resource key. */
    static final String UNRECOGNIZED_PATH = key(index++);

    /** A resource key. */
    static final String ERR_NO_ROLE_TARGET = key(index++);

    // Used by PageLanding

    /** A resource key. */
    static final String LANDING_HEADING = key(index++);

    // Used by PageHome

    /** A resource key. */
    static final String HOME_HEADING = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {SITE_TITLE, "Proctoring Media Management"},
            {UNRECOGNIZED_PATH, "Unrecognized path: {0}"},
            {ERR_NO_ROLE_TARGET, "No target provided with role control"},

            {LANDING_HEADING, "Welcome to Proctoring Media Management"},

            {HOME_HEADING, "Proctoring Media Management"},

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
