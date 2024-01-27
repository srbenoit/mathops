package dev.mathops.app.ops;

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
    static final String LOGIN_DB_FIELD_LBL = key(index++);

    /** A resource key. */
    static final String LOGIN_USER_FIELD_LBL = key(index++);

    /** A resource key. */
    static final String LOGIN_PWD_FIELD_LBL = key(index++);

    /** A resource key. */
    static final String LOGIN_LOGIN_BTN = key(index++);

    /** A resource key. */
    static final String LOGIN_CANCEL_BTN = key(index++);

    /** A resource key. */
    static final String LOGIN_NO_PWD_ERR = key(index++);

    /** A resource key. */
    static final String LOGIN_NO_USER_ERR = key(index++);

    /** A resource key. */
    static final String LOGIN_NO_DB_ERR = key(index++);

    /** A resource key. */
    static final String LOGIN_NO_SCHEMA_ERR = key(index++);

    /** A resource key. */
    static final String LOGIN_BAD_LOGIN_ERR = key(index++);

    /** A resource key. */
    static final String LOGIN_CANT_CREATE_SCHEMA_ERR = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {//

            {TITLE, "Precalculus Program Operations"},

            {LOGIN_DB_FIELD_LBL, "Database:"},
            {LOGIN_USER_FIELD_LBL, "Username:"},
            {LOGIN_PWD_FIELD_LBL, "Password:"},
            {LOGIN_LOGIN_BTN, "Login"},
            {LOGIN_CANCEL_BTN, "Cancel"},
            {LOGIN_NO_PWD_ERR, "No password entered."},
            {LOGIN_NO_USER_ERR, "No username entered."},
            {LOGIN_NO_DB_ERR, "No database selected."},
            {LOGIN_NO_SCHEMA_ERR, "Selected database has no primary schema."},
            {LOGIN_BAD_LOGIN_ERR, "Invalid login."},
            {LOGIN_CANT_CREATE_SCHEMA_ERR, "Unable to create schema object."},

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
