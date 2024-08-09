package dev.mathops.web.site.admin;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by AdminSite

    /** A resource key. */
    static final String SITE_TITLE = key(index++);

    /** A resource key. */
    static final String GET_BAD_PATH = key(index++);

    /** A resource key. */
    static final String POST_BAD_PATH = key(index++);

    /** A resource key. */
    static final String POST_NO_SESSION = key(index++);

    // Used by AdminPage

    /** A resource key. */
    static final String HOME_BTN_LBL = key(index++);

    /** A resource key. */
    static final String ROOT_BTN_LBL = key(index++);

    /** A resource key. */
    static final String LOGGED_IN_TO_AS = key(index++);

    // Used by PageLogin

    /** A resource key. */
    static final String LOGIN_PROMPT = key(index++);

    /** A resource key. */
    static final String LOGIN_BTN_LBL = key(index++);

    // Used by PageHome

    /** A resource key. */
    static final String GENADM_BTN_LBL = key(index++);

    /** A resource key. */
    static final String OFFICE_BTN_LBL = key(index++);

    /** A resource key. */
    static final String TESTING_BTN_LBL = key(index++);

    /** A resource key. */
    static final String PROCTOR_BTN_LBL = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {SITE_TITLE, "Precalculus System Administration"},
            {GET_BAD_PATH, "GET: Path {0} not found"},
            {POST_BAD_PATH, "POST: Path {0} not found"},
            {POST_NO_SESSION, "POST: Path {0} accessed with no logon session"},

            {HOME_BTN_LBL, "Home"},
            {ROOT_BTN_LBL, "Root"},
            {LOGGED_IN_TO_AS, "Logged in to {0} as <strong>{1}</strong>"},

            {LOGIN_PROMPT, "Use your Colorado State University <strong>eID</strong> to log in."},
            {LOGIN_BTN_LBL, "eID Login"},

            {GENADM_BTN_LBL, "General Administration"},
            {OFFICE_BTN_LBL, "Office Staff View"},
            {TESTING_BTN_LBL, "Testing Center View"},
            {PROCTOR_BTN_LBL, "Proctor View"},

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
