package dev.mathops.db.config;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by DatabaseConfig

    /** A resource key. */
    static final String DB_CFG_LOADING = key(index++);

    /** A resource key. */
    static final String DB_CFG_CANT_LOAD = key(index++);

    /** A resource key. */
    static final String DB_CFG_DIR_NONEXIST = key(index++);

    /** A resource key. */
    static final String DB_CFG_FILE_NONEXIST = key(index++);

    /** A resource key. */
    static final String DB_CFG_CANT_OPEN_SRC = key(index++);

    /** A resource key. */
    static final String DB_CFG_NO_TOPLEVEL = key(index++);

    /** A resource key. */
    static final String DB_CFG_BAD_TOPLEVEL = key(index++);

    /** A resource key. */
    static final String DB_CFG_BAD_CODE_PROFILE = key(index++);

    /** A resource key. */
    static final String DB_CFG_BAD_SITE_PROFILE = key(index++);

    /** A resource key. */
    static final String DB_CFG_DUP_HOST = key(index++);

    /** A resource key. */
    static final String DB_CFG_DUP_PATH = key(index++);

    /** A resource key. */
    static final String DB_CFG_DUP_CODE = key(index++);

    /** A resource key. */
    static final String DB_CFG_BAD_SITE_TAG = key(index++);

    // Used by ServerConfig

    /** A resource key. */
    static final String SRV_CFG_BAD_ELEM_TAG = key(index++);

    /** A resource key. */
    static final String SRV_CFG_BAD_TYPE = key(index++);

    /** A resource key. */
    static final String SRV_CFG_BAD_SCHEMA = key(index++);

    /** A resource key. */
    static final String SRV_CFG_CANT_CONNECT = key(index++);

    /** A resource key. */
    static final String SRV_CFG_DUP_LOGIN_ID = key(index++);

    // Used by LoginConfig

    /** A resource key. */
    static final String LOGIN_CFG_BAD_ELEM_TAG = key(index++);

    // Used by DbaLoginConfig

    /** A resource key. */
    static final String DBA_LOGIN_CFG_BAD_ELEM_TAG = key(index++);

    // Used by ProfileConfig

    /** A resource key. */
    static final String PROF_CFG_BAD_ELEM_TAG = key(index++);

    /** A resource key. */
    static final String PROF_CFG_BAD_CHILD_ELEM_TAG = key(index++);

    /** A resource key. */
    static final String PROF_CFG_BAD_CHILD_SCHEMA = key(index++);

    /** A resource key. */
    static final String PROF_CFG_BAD_CHILD_LOGIN = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {DB_CFG_LOADING, "DatabaseConfig loading database mappings from {0}"},
            {DB_CFG_CANT_LOAD, "Unable to load database config instance"},
            {DB_CFG_DIR_NONEXIST, "Directory {0} does not exist"},
            {DB_CFG_FILE_NONEXIST, "{0} not found - installing defaults"},
            {DB_CFG_CANT_OPEN_SRC, "Failed to open source file {0}"},
            {DB_CFG_NO_TOPLEVEL, "Missing top-level 'database-config' element in XML"},
            {DB_CFG_BAD_TOPLEVEL, "Unable to identify top-level 'database-config' element in XML"},
            {DB_CFG_BAD_CODE_PROFILE, "Unrecognized profile ID ''{0}'' in code context in context map"},
            {DB_CFG_BAD_SITE_PROFILE, "Unrecognized profile ID ''{0}'' in site context in context map"},
            {DB_CFG_DUP_HOST, "Multiple 'web' tags with host ''{0}'' in context map"},
            {DB_CFG_DUP_PATH, "Multiple 'site' tags with path ''{0}'' in context map"},
            {DB_CFG_DUP_CODE, "Multiple 'code' tags with context ''{0}'' in context map"},
            {DB_CFG_BAD_SITE_TAG, "Child of 'web' element was ''{0}'' rather than 'site' in context map"},

            {SRV_CFG_BAD_ELEM_TAG, "A server configuration must be in an element with tag 'server'"},
            {SRV_CFG_BAD_TYPE, "Invalid type in 'server' tag: {0}"},
            {SRV_CFG_BAD_SCHEMA, "Invalid schema in 'server' tag: {0}"},
            {SRV_CFG_CANT_CONNECT, "Failed to connect to server {0} ({1}:{2})"},
            {SRV_CFG_DUP_LOGIN_ID, "Duplicated login ID {0}"},

            {LOGIN_CFG_BAD_ELEM_TAG, "A login configuration must be in an element with tag 'login'"},

            {DBA_LOGIN_CFG_BAD_ELEM_TAG, "A DBA login configuration must be in an element with tag 'dbalogin'"},

            {PROF_CFG_BAD_ELEM_TAG, "A profile configuration must be in an element with tag 'profile'"},
            {PROF_CFG_BAD_CHILD_ELEM_TAG,
                    "A child of a profile configuration must be an element with tag 'schema-login'"},
            {PROF_CFG_BAD_CHILD_SCHEMA, "Unrecognized schema in schema-login: {0}"},
            {PROF_CFG_BAD_CHILD_LOGIN, "Unrecognized login in schema-login: {0}"},

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
