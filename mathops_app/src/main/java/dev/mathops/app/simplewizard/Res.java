package dev.mathops.app.simplewizard;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    /** A resource key. */
    static final String BACK_TEXT = key(index++);

    /** A resource key. */
    static final String NEXT_TEXT = key(index++);

    /** A resource key. */
    static final String CANCEL_TEXT = key(index++);

    /** A resource key. */
    static final String FINISH_TEXT = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {BACK_TEXT, "Back",},
            {NEXT_TEXT, "Next",},
            {CANCEL_TEXT, "Cancel",},
            {FINISH_TEXT, "Finish",},

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
