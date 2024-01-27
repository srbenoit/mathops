package dev.mathops.db.old;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by DbConnection

    /** A resource key. */
    static final String DB_CONN_REG_IFX = key(index++);

    /** A resource key. */
    static final String DB_CONN_REG_IFX_FAIL = key(index++);

    /** A resource key. */
    static final String DB_CONN_REG_ORA = key(index++);

    /** A resource key. */
    static final String DB_CONN_REG_ORA_FAIL = key(index++);

    // Used by DbContext

    /** A resource key. */
    static final String DB_CTX_NULL_SCHEMA = key(index++);

    /** A resource key. */
    static final String DB_CTX_NULL_DRIVER = key(index++);

    /** A resource key. */
    static final String DB_CTX_MANY_CONNECTIONS = key(index++);

    /** A resource key. */
    static final String DB_CTX_NOT_CHECKED_IN = key(index++);

    // Used by SchemaBuilder

    /** A resource key. */
    static final String SCH_BLD_NO_CONSTRUCTOR = key(index++);

    /** A resource key. */
    static final String SCH_BLD_CANT_CONSTRUCT = key(index++);

    // Used by TermKey

    /** A resource key. */
    static final String BAD_SHORT_TERM = key(index++);

    /** A resource key. */
    static final String NULL_SHORT_TERM = key(index++);

    /** A resource key. */
    static final String BAD_LONG_TERM = key(index++);

    /** A resource key. */
    static final String NULL_LONG_TERM = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {DB_CONN_REG_IFX, "Registering Informix JDBC driver"},
            {DB_CONN_REG_IFX_FAIL, "Failed to register Informix JDBC driver"},
            {DB_CONN_REG_ORA, "Registering Oracle JDBC driver"},
            {DB_CONN_REG_ORA_FAIL, "Failed to register Oracle JDBC driver"},

            {DB_CTX_NULL_SCHEMA, "Null schema provided"},
            {DB_CTX_NULL_DRIVER, "Null driver configuration provided"},
            {DB_CTX_MANY_CONNECTIONS, "Pool for {0}/{1} has {2} connetions checked out"},
            {DB_CTX_NOT_CHECKED_IN, "Connection checked in that was not checked out!"},

            {SCH_BLD_NO_CONSTRUCTOR, "Missing required constructor in {0}"},
            {SCH_BLD_CANT_CONSTRUCT, "Failed to construct {0}"},

            {BAD_SHORT_TERM, "Invalid short term string: {0}"},
            {NULL_SHORT_TERM, "Short term string may not be null"},
            {BAD_LONG_TERM, "Invalid long term string: {0}"},
            {NULL_LONG_TERM, "Long term string may not be null"},

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
