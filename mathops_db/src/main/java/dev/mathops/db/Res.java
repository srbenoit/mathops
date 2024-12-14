package dev.mathops.db;

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

    /** A resource key. */
    static final String DB_CONN_REG_PG = key(index++);

    /** A resource key. */
    static final String DB_CONN_REG_PG_FAIL = key(index++);

    // Used by DbContext

    /** A resource key. */
    static final String DB_CTX_NULL_SCHEMA = key(index++);

    /** A resource key. */
    static final String DB_CTX_NULL_DRIVER = key(index++);

    /** A resource key. */
    static final String DB_CTX_MANY_CONNECTIONS = key(index++);

    /** A resource key. */
    static final String DB_CTX_NOT_CHECKED_IN = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {DB_CONN_REG_IFX, "Registering Informix JDBC driver"},
            {DB_CONN_REG_IFX_FAIL, "Failed to register Informix JDBC driver"},
            {DB_CONN_REG_ORA, "Registering Oracle JDBC driver"},
            {DB_CONN_REG_ORA_FAIL, "Failed to register Oracle JDBC driver"},
            {DB_CONN_REG_PG, "Registering PostgreSQL JDBC driver"},
            {DB_CONN_REG_PG_FAIL, "Failed to register PostgreSQL JDBC driver"},

            {DB_CTX_NULL_SCHEMA, "Null schema provided"},
            {DB_CTX_NULL_DRIVER, "Null driver configuration provided"},
            {DB_CTX_MANY_CONNECTIONS, "Pool for {0}/{1} has {2} connetions checked out"},
            {DB_CTX_NOT_CHECKED_IN, "Connection checked in that was not checked out!"},

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
