package dev.mathops.app;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by multiple classes

    /** A resource key. */
    static final String NOT_AWT_THREAD = key(index++);

    /** A resource key. */
    static final String FILE_LOAD_FAIL = key(index++);

    /** A resource key. */
    static final String FILE_NOT_FOUND = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {NOT_AWT_THREAD, "NOT THE AWT EVENT THREAD!",},
            {FILE_LOAD_FAIL, "FileLoader failed to read file {0}"},
            {FILE_NOT_FOUND, "File not found: {0} - {1}"},

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
