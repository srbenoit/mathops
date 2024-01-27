package dev.mathops.app.canvas;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by multiple classes

    /** A resource key. */
    static final String TITLE = key(index++);

    // Used by LoginWindow

    /** A resource key. */
    static final String LOGIN_TITLE = key(index++);

    /** A resource key. */
    static final String LOGIN_HOST_FIELD_LBL = key(index++);

    /** A resource key. */
    static final String LOGIN_TOKEN_FIELD_LBL = key(index++);

    /** A resource key. */
    static final String LOGIN_LOGIN_BTN = key(index++);

    /** A resource key. */
    static final String LOGIN_CANCEL_BTN = key(index++);

    /** A resource key. */
    static final String LOGIN_NO_HOST_ERR = key(index++);

    /** A resource key. */
    static final String LOGIN_NO_TOKEN_ERR = key(index++);

    /** A resource key. */
    static final String LOGIN_FAILED = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {//

            {TITLE, "Canvas API Exerciser"},

            {LOGIN_TITLE, "Connect to Canvas"},
            {LOGIN_HOST_FIELD_LBL, "Canvas Host URL:"},
            {LOGIN_TOKEN_FIELD_LBL, "Access Token:"},
            {LOGIN_LOGIN_BTN, "Login"},
            {LOGIN_CANCEL_BTN, "Cancel"},
            {LOGIN_NO_HOST_ERR, "No host URL entered."},
            {LOGIN_NO_TOKEN_ERR, "No access token entered."},
            {LOGIN_FAILED, "Unable to log in to Canvas.  Check credentials."},

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
//     * @return the best-matching message, an empty string if none is registered (never {@code null})
//     */
//     static String get(final String key, final Locale locale) {
//
//     return instance.getMsg(key, locale);
//     }

//    /**
//     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format
//     * that message pattern with a collection of arguments.
//     *
//     * @param key the message key
//     * @param arguments the arguments, as for {@code MessageFormat}
//     * @return the formatted string (never {@code null})
//     */
//     static String fmt(final String key, final Object... arguments) {
//
//     return instance.formatMsg(key, arguments);
//     }

//    /**
//     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format
//     * that message pattern with a collection of arguments.
//     *
//     * @param key the message key
//     * @param locale the desired {@code Locale}
//     * @param arguments the arguments, as for {@code MessageFormat}
//     * @return the formatted string (never {@code null})
//     */
//     static String fmt(final String key, final Locale locale, final Object... arguments) {
//
//     return instance.formatMsg(key, locale, arguments);
//     }
}
