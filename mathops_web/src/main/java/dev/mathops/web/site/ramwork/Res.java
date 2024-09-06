package dev.mathops.web.site.ramwork;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by RamWorkSite

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

    // Used by PageAuthoring

    /** A resource key. */
    static final String AUTHORING_HEADING = key(index++);

    // Used by PageGraphEditor

    /** A resource key. */
    static final String GRAPHEDIT_HEADING = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {
            {UNRECOGNIZED_PATH, "Unrecognized path: {0}"},
            {SITE_TITLE, "RAMwork"},
            {LANDING_HEADING, "Welcome to RAMwork"},
            {HOME_HEADING, "RAMwork Online Homework System"},
            {AUTHORING_HEADING, "Problem Authoring Resources"},
            {GRAPHEDIT_HEADING, "Graph Editor"},
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

//    /**
//     * Gets the message with a specified key using the current locale.
//     *
//     * @param key the message key
//     * @param locale the desired {@code Locale}
//     * @return the best-matching message, an empty string if none is registered (never
//     *         {@code null})
//     */
//     public static String get(final String key, final Locale locale) {
//
//     return instance.getMsg(key, locale);
//     }

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

//    /**
//     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format
//     * that message pattern with a collection of arguments.
//     *
//     * @param key the message key
//     * @param locale the desired {@code Locale}
//     * @param arguments the arguments, as for {@code MessageFormat}
//     * @return the formatted string (never {@code null})
//     */
//     public static String fmt(final String key, final Locale locale, final Object... arguments) {
//
//     return instance.formatMsg(key, locale, arguments);
//     }
}
