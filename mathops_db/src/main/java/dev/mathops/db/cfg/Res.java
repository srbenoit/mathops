package dev.mathops.db.cfg;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    // Used by DatabaseConfigXml

    /** A resource key. */
    static final String XML_CANT_READ_FILE = key(1);

    /** A resource key. */
    static final String XML_CANT_FIND_TOP_ELEM = key(2);

    /** A resource key. */
    static final String XML_TOP_ELEM_IS_EMPTY = key(3);

    /** A resource key. */
    static final String XML_TOP_INCORRECT = key(4);

    /** A resource key. */
    static final String XML_UNEXPECTED_ELEM = key(5);

    /** A resource key. */
    static final String XML_BAD_ATTR = key(6);

    // Used by Server

    /** A resource key. */
    static final String SERVER_NULL_TYPE_HOST = key(10);

    // Used by Database

    /** A resource key. */
    static final String DATABASE_NULL_SERVER_ID = key(20);

    /** A resource key. */
    static final String DATABASE_CONNECTED_TO = key(21);

    /** A resource key. */
    static final String DATABASE_CONNECTED_TO_NO_INST = key(22);

    /** A resource key. */
    static final String DATABASE_CANT_CONNECT = key(23);

    // Used by Login

    /** A resource key. */
    static final String LOGIN_NULL_DB_ID_USER = key(30);

    /** A resource key. */
    static final String LOGIN_MANY_CONNECTIONS = key(31);

    /** A resource key. */
    static final String LOGIN_NOT_CHECKED_IN = key(32);

    // Used by Data

    /** A resource key. */
    static final String DATA_NULL_DB_ID_SCHEMA_USE = key(40);

    /** A resource key. */
    static final String DATA_PREFIX_BLANK = key(41);

    /** A resource key. */
    static final String DATA_BAD_PREFIX = key(42);

    // Used by Profile

    /** A resource key. */
    static final String PROFILE_NULL_ID = key(50);

    /** A resource key. */
    static final String PROFILE_MISSING_START = key(51);

    /** A resource key. */
    static final String PROFILE_DUP_START = key(52);

    /** A resource key. */
    static final String PROFILE_MSG_END = key(53);

    // Used by Schema

    /** A resource key. */
    static final String SCHEMA_NULL_DATA_LOGIN = key(60);

    // Used by WebContext

    /** A resource key. */
    static final String WEB_CONTEXT_NULL_HOST = key(70);

    // Used by Site

    /** A resource key. */
    static final String SITE_NULL_OWNER_PATH_PROFILE = key(80);

    // Used by CodeContext

    /** A resource key. */
    static final String CODE_CONTEXT_NULL_ID_PROFILE = key(90);

    // Used by DatabaseConfig

    /** A resource key. */
    static final String DATABASE_CFG_LOADED_FROM = key(100);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {XML_CANT_READ_FILE, "Unable to read file: {0}"},
            {XML_CANT_FIND_TOP_ELEM, "Unable to find top-level element: {0}"},
            {XML_TOP_ELEM_IS_EMPTY, "Top-level element is empty: {0}"},
            {XML_TOP_INCORRECT, "Top-level element was not ''{0}''"},
            {XML_UNEXPECTED_ELEM, "Unexpected element ({0}) within ''{1}''"},
            {XML_BAD_ATTR, "Invalid ''{0}'' attribute ({1}) in ''{2}'' element"},

            {SERVER_NULL_TYPE_HOST, "Server type and host may not be null"},

            {DATABASE_NULL_SERVER_ID, "Database server and ID may not be null"},
            {DATABASE_CONNECTED_TO, "Connected to {0} {1} ({2}) as {3}"},
            {DATABASE_CONNECTED_TO_NO_INST, "Connected to {0} ({1}) as {2}"},
            {DATABASE_CANT_CONNECT, "Failed to connect to server {0}.{1} ({2}:{3})"},

            {LOGIN_NULL_DB_ID_USER, "Login database, ID, and username may not be null"},
            {LOGIN_MANY_CONNECTIONS, "Pool for login {0} has {1} connections checked out"},
            {LOGIN_NOT_CHECKED_IN, "Connection checked in that was not checked out"},

            {DATA_NULL_DB_ID_SCHEMA_USE, "Data database, ID, schema, and use may not be null"},
            {DATA_PREFIX_BLANK, "Data prefix may not be blank"},
            {DATA_BAD_PREFIX, "Invalid prefix - must be simple alphanumeric string"},

            {PROFILE_NULL_ID, "Profile ID may not be null"},
            {PROFILE_MISSING_START, "Profile does not include {"},
            {PROFILE_DUP_START, "Profile includes duplicate {"},
            {PROFILE_MSG_END, "} schema"},

            {SCHEMA_NULL_DATA_LOGIN, "Schema data and login may not be null"},

            {WEB_CONTEXT_NULL_HOST, "Web context host may not be null"},

            {SITE_NULL_OWNER_PATH_PROFILE, "Site owner, path, and profile may not be null"},

            {CODE_CONTEXT_NULL_ID_PROFILE, "Code context ID and profile may not be null"},

            {DATABASE_CFG_LOADED_FROM, "Database configuration loaded from {0}"},

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
