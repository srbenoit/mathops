package dev.mathops.db.old.logic;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class TestRes extends ResBundle {

    // Used by multiple classes

    /** A resource key. */
    private static final String ERR_NO_TEST_PROFILE = key(1);

    /** A resource key. */
    private static final String ERR_NO_PRIMARY_CONTEXT = key(2);

    // Used by IvtPlacementLogic

    /** A resource key. */
    static final String PLC_ERR_NO_CHECKIN_PROFILE = key(3);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {ERR_NO_TEST_PROFILE, "No 'test' code profile configured."},
            {ERR_NO_PRIMARY_CONTEXT, "No primary context in the 'test' code profile."},

            {PLC_ERR_NO_CHECKIN_PROFILE, "No 'checkin' code profile configured."},

            //
    };

    /** The singleton instance. */
    private static final TestRes instance = new TestRes();

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

        return instance.getMsg(key);
    }
}
