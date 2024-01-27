package dev.mathops.app.passwordhash;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    /** A resource key. */
    static final String FRAME_TITLE = key(index++);

    /** A resource key. */
    static final String PWD_PROMPT = key(index++);

    /** A resource key. */
    static final String BUTTON_LABEL = key(index++);

    /** A resource key. */
    static final String SALT_LABEL = key(index++);

    /** A resource key. */
    static final String SALTED_HASH_LABEL = key(index++);

    /** A resource key. */
    static final String EXPLAIN = key(index++);

    /** A resource key. */
    static final String ERROR_MSG = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {FRAME_TITLE, "Password Hash Generator",},
            {PWD_PROMPT, "Enter Password:",},
            {BUTTON_LABEL, "Generate Salt and Hashes...",},
            {SALT_LABEL, "Salt:",},
            {SALTED_HASH_LABEL, "Salted Hash:",},
            {EXPLAIN, "Hash=SHA-256(Password),  Salted hash = SHA-256(Salt + Hash)",},
            {ERROR_MSG, "ERROR: SHA-256 algorithm not supported",},
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
