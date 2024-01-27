package dev.mathops.db.type;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by TermKey

    /** A resource key. */
    static final String BAD_SHORT_TERM = key(index++);

    /** A resource key. */
    static final String NULL_SHORT_TERM = key(index++);

    /** A resource key. */
    static final String BAD_LONG_TERM = key(index++);

    /** A resource key. */
    static final String NULL_LONG_TERM = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {BAD_SHORT_TERM, "Invalid short term string: {0}"},
            {NULL_SHORT_TERM, "Short term string may not be null"},
            {BAD_LONG_TERM, "Invalid long term string: {0}"},
            {NULL_LONG_TERM, "Long term string may not be null"},

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
