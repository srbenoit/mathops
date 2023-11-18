package dev.mathops.core.res;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    /** A resource key. */
    static final String NULL_LOCALE = key(index++);

    /** A resource key. */
    static final String BAD_ARRAY = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //
            {NULL_LOCALE, "Locale may not be null"},
            {BAD_ARRAY, "Invalid message array"},
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
