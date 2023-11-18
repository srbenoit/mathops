package dev.mathops.core.parser;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources for the {@code edu.colostate.math.core.parser} package.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by HexEncoder

    /** A resource key. */
    static final String INVALID_HEX_LEN = key(index++);

    /** A resource key. */
    static final String INVALID_HEX = key(index++);

    // Used by Base64

    /** A resource key. */
    static final String B64_PAD_BEFORE_END = key(index++);

    /** A resource key. */
    static final String B64_BAD_SECOND = key(index++);

    /** A resource key. */
    static final String B64_BAD_THIRD = key(index++);

    /** A resource key. */
    static final String B64_OUT_OF_RANGE = key(index++);

    /** A resource key. */
    static final String B64_BAD_CHAR = key(index++);

    /** A resource key. */
    static final String B64_BYTE1_PAD = key(index++);

    /** A resource key. */
    static final String B64_BYTE2_PAD = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //
            {INVALID_HEX_LEN, "Invalid length for hex string {0}"},
            {INVALID_HEX, "Invalid hex string {0}"},

            {B64_PAD_BEFORE_END, "Base64 decode: Pad found before data end",},
            {B64_BAD_SECOND, "Base64 decode: Second character was invalid",},
            {B64_BAD_THIRD, "Base64 decode: Third character was invalid",},
            {B64_OUT_OF_RANGE, "Base64 decode: Character ''{0}'' out of range",},
            {B64_BAD_CHAR, "Base64 decode: Invalid character ''{0}''",},
            {B64_BYTE1_PAD, "Base64 decode: Pad character in first byte of block",},
            {B64_BYTE2_PAD, "Base64 decode: Pad character in second byte of block",},

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
