package dev.mathops.web.site.admin.bookstore;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by Page

    /** A resource key. */
    static final String DEPARTMENT_TITLE = key(index++);

    /** A resource key. */
    static final String NO_ROLE_TARGET = key(index++);

    /** A resource key. */
    static final String POSSIBLE_ATTACK = key(index++);

    /** A resource key. */
    static final String ATTACK_PARAM = key(index++);

    /** A resource key. */
    static final String ACTING_AS = key(index++);

    /** A resource key. */
    static final String BECOMING = key(index++);

    /** A resource key. */
    static final String CHECK_STATUS_PROMPT = key(index++);

    /** A resource key. */
    static final String CHECK_BTN_LBL = key(index++);

    // Used by LoginPage

    /** A resource key. */
    static final String SITE_TITLE = key(index++);

    // Used by CheckKeyPage

    /** A resource key. */
    static final String KEY_NOT_FOUND = key(index++);

    /** A resource key. */
    static final String KEY_NOT_ACTIVE = key(index++);

    /** A resource key. */
    static final String KEY_ACTIVE_NO_USER = key(index++);

    /** A resource key. */
    static final String KEY_ACTIVE_NO_STU = key(index++);

    /** A resource key. */
    static final String KEY_ACTIVE_STU = key(index++);

    /** A resource key. */
    static final String DEACTIVATE_PROMPT = key(index++);

    /** A resource key. */
    static final String DEACTIVATE_BTN_LBL = key(index++);

    // Used by DeactivateKeyPage

    /** A resource key. */
    static final String DEACTIVATE_CONFIRM = key(index++);

    /** A resource key. */
    static final String KEY_DEACTIVATED = key(index++);

    /** A resource key. */
    static final String DEACTIVATION_ERROR = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {DEPARTMENT_TITLE, "Department&nbsp;of Mathematics"},
            {NO_ROLE_TARGET, "No target provided with role control"},
            {POSSIBLE_ATTACK, "Invalid request parameters - possible attack:"},
            {ATTACK_PARAM, "  {0} = ''{1}''"},
            {ACTING_AS, "Acting as student {0}"},
            {BECOMING, "Becoming student {0}"},

            {CHECK_STATUS_PROMPT, "To check the status of a Precalculus course Access "
                    + "Code, enter the code below:"},
            {CHECK_BTN_LBL, "Check Key Status..."},

            {SITE_TITLE, "Math Bookstore Administration"},

            {KEY_NOT_FOUND, "Key not found"},
            {KEY_NOT_ACTIVE, "Key has not been activated."},
            {KEY_ACTIVE_NO_USER, "Key was activated {0}, but user who activated key is "
                    + "no longer in records."},
            {KEY_ACTIVE_NO_STU, "Key was activated {0} by student with CSU ID {0}"},
            {KEY_ACTIVE_STU, "Key was activated {0} by {1} {2} (CSU ID {3})."},
            {DEACTIVATE_PROMPT, "To DEACTIVATE the key (which will remove the key from "
                    + "the student's account and return it to the pool of available keys), "
                    + "click the button below:"},
            {DEACTIVATE_BTN_LBL, "Deactivate Key..."},

            {DEACTIVATE_CONFIRM, "CONFIRM YOU WISH TO DEACTIVATE THIS KEY AND RETURN IT "
                    + "TO THE POOL OF AVAILABLE KEYS"},
            {KEY_DEACTIVATED, "Key has been deactivated."},
            {DEACTIVATION_ERROR, "There was an error deactivating key!"},

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
