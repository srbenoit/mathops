package dev.mathops.db.reclogic.term;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class TestRes extends ResBundle {

    // Used by multiple classes

    /** A resource key. */
    static final String ERR_NO_PGTEST_PROFILE = key(1);

    /** A resource key. */
    static final String ERR_NO_PGPRIMARY_CONTEXT = key(2);

    /** A resource key. */
    static final String ERR_CANT_QUERY_WHICH_DB = key(3);

    /** A resource key. */
    static final String ERR_NOT_CONNECTED_TO_TEST = key(4);

    /** A resource key. */
    static final String ERR_NO_TERM_PREFIX = key(5);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {ERR_NO_PGTEST_PROFILE, "No 'pgtest' code profile configured."},
            {ERR_NO_PGPRIMARY_CONTEXT, "No primary context in the 'pgtest' code profile."},
            {ERR_CANT_QUERY_WHICH_DB, "Can't query which_db"},
            {ERR_NOT_CONNECTED_TO_TEST, "Connected to {0} rather than TEST database"},
            {ERR_NO_TERM_PREFIX, "No prefix configured for TERM schema"},

            //
    };

    /** The singleton instance. */
    private static final TestRes INSTANCE = new TestRes();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private TestRes() {

        super(Locale.US, EN_US);
    }

    /**
     * Gets the message with a specified key using the current locale.
     *
     * @param key the message key
     * @return the best-matching message, an empty string if none is registered (never {@code null})
     */
    static String get(final String key) {

        return INSTANCE.getMsg(key);
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

        return INSTANCE.formatMsg(key, arguments);
    }
}
