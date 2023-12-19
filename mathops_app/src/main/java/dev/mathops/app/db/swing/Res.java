package dev.mathops.app.db.swing;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by MainWindow

    /** A resource key. */
    static final String MAIN_WINDOW_TITLE = key(index++);

    /** A resource key. */
    static final String DATA_TAB_TITLE = key(index++);

    /** A resource key. */
    static final String ANALYTICS_TAB_TITLE = key(index++);

    /** A resource key. */
    static final String DBA_TAB_TITLE = key(index++);

    /** A resource key. */
    static final String CONFIGURATION_TAB_TITLE = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {MAIN_WINDOW_TITLE, "Database Management",},

            {DATA_TAB_TITLE, "Data"},
            {ANALYTICS_TAB_TITLE, "Analytics"},
            {DBA_TAB_TITLE, "Administration"},
            {CONFIGURATION_TAB_TITLE, "Configuration"},
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
