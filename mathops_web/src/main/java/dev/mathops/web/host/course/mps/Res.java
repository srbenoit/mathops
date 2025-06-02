package dev.mathops.web.host.course.mps;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by Proctoring site

    /** A resource key. */
    static final String UNRECOGNIZED_PATH = key(index++);

    // Used by various pages

    /** A resource key. */
    static final String SITE_TITLE = key(index++);

    // Used by PageLanding

    /** A resource key. */
    static final String LANDING_HEADING = key(index++);

    // Used by PageHome

    /** A resource key. */
    static final String HOME_HEADING = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {UNRECOGNIZED_PATH, "Unrecognized path: {0}"},

            {SITE_TITLE, "Mathematics Proctoring System"},

            {LANDING_HEADING, "Welcome to the Mathematics Proctoring System"},

            {HOME_HEADING, "Mathematics Proctoring System"},

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
