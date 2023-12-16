package dev.mathops.app.db.ui.configuration;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by MainConfigurationPanel

    /** A resource key. */
    static final String SERVERS_BTN_LABEL = key(index++);

    /** A resource key. */
    static final String PROFILES_BTN_LABEL = key(index++);

    /** A resource key. */
    static final String CODE_BTN_LABEL = key(index++);

    /** A resource key. */
    static final String WEB_BTN_LABEL = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {SERVERS_BTN_LABEL, "Database Servers",},
            {PROFILES_BTN_LABEL, "Data Profiles",},
            {CODE_BTN_LABEL, "Code Contexts",},
            {WEB_BTN_LABEL, "Website Contexts",},
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
