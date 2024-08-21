package dev.mathops.web.site;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    // Used by WebMidController

    /** A resource key. */
    static final String INFO = key(15);

    /** A resource key. */
    static final String CANT_CREATE_DIR = key(16);

    /** A resource key. */
    static final String BAD_DIR = key(17);

    /** A resource key. */
    static final String STARTING = key(18);

    /** A resource key. */
    static final String STARTED = key(19);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {
            {INFO, "Zircon Web Mid Controller 1.0"},
            {CANT_CREATE_DIR, "Unable to create ''{0}'' directory"},
            {STARTING, "{0} starting with base directory ''{1}''"},
            {STARTED, "{0} initialized"},
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
