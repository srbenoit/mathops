package dev.mathops.web.host.course.video;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    // Used by VideoSite

    /** A resource key. */
    static final String UNRECOGNIZED_PATH = key(1);

    /** A resource key. */
    static final String BAD_PARAMETERS = key(2);

    /** A resource key. */
    static final String VIDEO_NOT_SUPP = key(3);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {
            {UNRECOGNIZED_PATH, "Unrecognized path: {0}"},
            {BAD_PARAMETERS, "Invalid request parameters - possible attack:"},
            {VIDEO_NOT_SUPP, "Your browser does not support inline video."},
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
