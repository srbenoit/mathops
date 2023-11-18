package dev.mathops.core.parser.xml;

import dev.mathops.core.unicode.UnicodeCharacter;
import dev.mathops.core.unicode.UnicodeCharacterSet;

/**
 * Utilities for working with {@code Char}, as defined in XML 1.0 and XML 1.1.
 */
enum XmlChars {
    ;

    /** The ranges of characters valid as the first character of an encoding. */
    private static final int[][] ENCODING_1 = {{'A', 'Z'}, {'a', 'z'},};

    /** A Unicode code point. */
    private static final int SPACE = 0x20;

    /** A Unicode code point. */
    private static final int TAB = 0x9;

    /** A Unicode code point. */
    private static final int CR = 0xD;

    /** A Unicode code point. */
    private static final int LF = 0xA;

    /** A Unicode code point. */
    private static final int MIDDLE_DOT = 0xB7;

    /**
     * The set of ranges considered a {@code Char} in the XML 1.0 specification. Note that many of these characters are
     * not currently defined in Unicode. This list is a subset of those permitted by XML 1.1.
     */
    private static final int[][] CHAR_10_RANGES =
            {{0x9, 0xA}, {0xD}, {0x20, 0xD7FF}, {0xE000, 0xFFFD}, {0x10000, 0x10FFFF},};

    /**
     * The set of ranges considered a {@code Char} in the XML 1.1 specification that are not permitted by XML 1.0.
     */
    private static final int[][] EXTRA_CHAR_11_RANGES = {{0x1, 0x1F}};

    /**
     * Tests whether a code point is defined in the current Unicode standard and matches the {@code [2] Char} production
     * of XML 1.1.
     *
     * @param codePoint the code point to test
     * @return {@code EXmlVersion.BOTH} if code point is valid in any version of XML; {@code EXmlVersion.VER1_1} if code
     *         point is only valid in XML 1.1; {@code EXmlVersion.NONE} if code point is not valid in any version of
     *         XML
     */
    static boolean isChar(final int codePoint) {

        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(codePoint);
        final boolean ok;

        if (chr == null) {
            ok = false;
        } else if (isInRanges(codePoint, CHAR_10_RANGES)) {
            ok = true;
        } else {
            ok = isInRanges(codePoint, EXTRA_CHAR_11_RANGES);
        }

        return ok;
    }

    /**
     * Tests whether a Unicode code point matches the {@code [3] S} production of XML 1.1.
     *
     * @param codePoint the code point to test
     * @return {@code true} if code point matches {@code S}
     */
    static boolean isWhitespace(final int codePoint) {

        return codePoint == SPACE || codePoint == TAB || codePoint == CR || codePoint == LF;
    }

    /**
     * Tests whether a Unicode code point matches the {@code [4] NameStartChar} production of XML 1.1.
     *
     * @param codePoint the code point to test
     * @return {@code true} if code point matches {@code NameStartChar}
     */
    static boolean isNameStartChar(final int codePoint) {

        final int[][] ranges = {{'A', 'Z'}, {'a', 'z'}, {0xC0, 0xD6}, {0xD8, 0xF6}, {0xF8, 0x2FF},
                {0x370, 0x37D}, {0x37F, 0x1FFF}, {0x200C, 0x200D}, {0x2070, 0x218F}, {0x2C00, 0x2FEF},
                {0x3001, 0xD7FF}, {0xF900, 0xFDCF}, {0xFDF0, 0xFFFD}, {0x10000, 0xEFFFF},};

        return codePoint == ':' || codePoint == '_' || UnicodeCharacterSet.getInstance().getCharacter(codePoint) != null
                && isInRanges(codePoint, ranges);
    }

    /**
     * Tests whether a Unicode code point matches the {@code [4a] NameChar} production of XML 1.1.
     *
     * @param codePoint the code point to test
     * @return {@code true} if code point matches {@code NameChar}
     */
    static boolean isNameChar(final int codePoint) {

        final int[][] ranges = {{'0', '9'}, {0x300, 0x36f}, {0x203F, 0x2040}};

        return codePoint == '-' || codePoint == '.' || codePoint == MIDDLE_DOT || isNameStartChar(codePoint)
                || UnicodeCharacterSet.getInstance().getCharacter(codePoint) != null && isInRanges(codePoint, ranges);
    }

    /**
     * Tests whether a character is valid as the first character in an encoding.
     *
     * @param chr the character to test
     * @return {@code true} if the character is valid; {@code false} if not
     */
    static boolean isEncodingChar1(final char chr) {

        return isInRanges(chr, ENCODING_1);
    }

    /**
     * Tests whether a character is valid as the second or later character in an encoding.
     *
     * @param chr the character to test
     * @return {@code true} if the character is valid; {@code false} if not
     */
    static boolean isEncodingChar2(final char chr) {

        return isEncodingChar1(chr) || isDigit(chr) || "._-".indexOf(chr) != -1;
    }

    /**
     * Tests whether a Unicode code point is a one of the valid quote marks (an apostrophe or double quote mark).
     *
     * @param codePoint the code point to test
     * @return {@code true} if the character is a quote; {@code false} if not
     */
    static boolean isQuote(final int codePoint) {

        return codePoint == '\'' || codePoint == '\"';
    }

    /**
     * Tests whether a Unicode code point is a digit (0-9).
     *
     * @param codePoint the code point to test
     * @return {@code true} if the character is a digit; {@code false} if not
     */
    static boolean isDigit(final int codePoint) {

        final int[][] ranges = {{'0', '9'}};

        return isInRanges(codePoint, ranges);
    }

    /**
     * Tests whether a Unicode code point is a hexadecimal character (0-9 or a-f or A-F).
     *
     * @param codePoint the code point to test
     * @return {@code true} if the character is a digit; {@code false} if not
     */
    static boolean isHex(final int codePoint) {

        final int[][] ranges = {{'a', 'f'}, {'A', 'F'}};

        return isDigit(codePoint) || isInRanges(codePoint, ranges);
    }

    /**
     * Tests whether a Unicode code point falls within any of several ranges of values.
     *
     * @param codePoint the code point to test
     * @param ranges    an array of one- or two-integer arrays, each containing either a single code point value, or the
     *                  lower and upper limits of a range of code point values
     * @return {@code true} if the code point is included in any of the specifies ranges (including the endpoints)
     */
    private static boolean isInRanges(final int codePoint, final int[][] ranges) {

        boolean isInRanges = false;

        for (final int[] range : ranges) {
            if (range.length == 1) {
                if (codePoint == range[0]) {
                    isInRanges = true;
                    break;
                }
            } else if (codePoint >= range[0] && codePoint <= range[1]) {
                isInRanges = true;
                break;
            }
        }

        return isInRanges;
    }
}
