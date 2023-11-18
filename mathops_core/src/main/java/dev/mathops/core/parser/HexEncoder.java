package dev.mathops.core.parser;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * Utility class to encode and decode hexadecimal strings.
 */
public enum HexEncoder {
    ;

    /** Hex characters with uppercase. */
    private static final char[] UC_HEX = "0123456789ABCDEF".toCharArray();

    /** Hex characters with lowercase. */
    private static final char[] LC_HEX = "0123456789abcdef".toCharArray();

    /**
     * Decodes a string of hex characters into a byte array.
     *
     * @param hex the hex characters
     * @return the decoded byte array
     * @throws IllegalArgumentException if the hex string is invalid (has odd length or contains a character that is not
     *                                  in a range '0'-'9', 'a'-'f', or 'A'-'F'. Leading "0x" or trailing 'h' characters
     *                                  are not permitted)
     */
    public static byte[] decode(final String hex) throws IllegalArgumentException {

        final int numBytes = hex.length() / 2;
        if (hex.length() != numBytes << 1) {
            throw new IllegalArgumentException(Res.fmt(Res.INVALID_HEX_LEN, hex));
        }

        final byte[] bytes = new byte[numBytes];

        for (int i = 0; i < numBytes; ++i) {
            final int lo = decodeNibble(hex.charAt((i << 1) + 1));
            if (lo < 0) {
                throw new IllegalArgumentException(Res.fmt(Res.INVALID_HEX, hex));
            }
            final int hi = decodeNibble(hex.charAt(i << 1));
            if (hi < 0) {
                throw new IllegalArgumentException(Res.fmt(Res.INVALID_HEX, hex));
            }
            bytes[i] = (byte) ((hi << 4) + lo);
        }

        return bytes;
    }

    /**
     * Attempts to decode a nibble from a character, which must be in the range '0'-'9', 'a'-'f', or 'A'-'F'.
     *
     * @param chr the character
     * @return the decoded nibble, -1 if invalid
     */
    private static int decodeNibble(final char chr) {

        final int nibble;

        if (chr >= '0' && chr <= '9') {
            nibble = chr - '0';
        } else if (chr >= 'a' && chr <= 'f') {
            nibble = chr - 'a' + 10;
        } else if (chr >= 'A' && chr <= 'F') {
            nibble = chr - 'A' + 10;
        } else {
            nibble = -1;
        }

        return nibble;
    }

    /**
     * Encodes an array of bytes as hexadecimal, using uppercase letters 'A' through 'F'.
     *
     * @param bytes the byte array
     * @return the encoded hex string
     */
    public static String encodeUppercase(final byte[] bytes) {

        final HtmlBuilder htm = new HtmlBuilder(bytes.length << 1);

        for (final byte aByte : bytes) {
            htm.add(UC_HEX[((int) aByte >> 4) & 0x0F]);
            htm.add(UC_HEX[(int) aByte & 0x0F]);
        }

        return htm.toString();
    }

    /**
     * Encodes a nibble as a single hexadecimal character, using uppercase letters 'A' through 'F'.
     *
     * @param nibble the nibble
     * @return the encoded hex character
     */
    public static char encodeNibble(final int nibble) {

        return UC_HEX[nibble & 0x0F];
    }

    /**
     * Encodes an array of bytes as hexadecimal, using lowercase letters 'A' through 'F'.
     *
     * @param bytes the byte array
     * @return the encoded hex string
     */
    public static String encodeLowercase(final byte[] bytes) {

        final HtmlBuilder htm = new HtmlBuilder(bytes.length << 1);

        for (final byte aByte : bytes) {
            htm.add(LC_HEX[((int) aByte >> 4) & 0x0F]);
            htm.add(LC_HEX[(int) aByte & 0x0F]);
        }

        return htm.toString();
    }
}
