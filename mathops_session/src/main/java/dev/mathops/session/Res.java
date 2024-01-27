package dev.mathops.session;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by LiveRegCache

    /** A resource key. */
    static final String BAD_TERM_NAME = key(index++);

    /** A resource key. */
    static final String LIVE_REG_QUERY_TIMING = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {//

            {BAD_TERM_NAME, "Invalid term name: {0}"},
            {LIVE_REG_QUERY_TIMING, "Live registration query took {0} ms."},
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
