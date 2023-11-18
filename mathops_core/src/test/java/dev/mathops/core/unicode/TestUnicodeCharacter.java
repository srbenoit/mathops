package dev.mathops.core.unicode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

/**
 * Tests for the UnicodeCharacter class.
 */
final class TestUnicodeCharacter {

    /** Test code point string. */
    private static final String TEST_CODE_POINT = "2345";

    /** Test code point string. */
    private static final String TEST_NAME = "SMALL FOO";

    /** Test code point string. */
    private static final String TEST_CATEGORY = "Fo";

    /** Test code point string. */
    private static final String TEST_COMBINING = "2";

    /** Test code point string. */
    private static final String TEST_BIDI_CAT = "BIDI";

    /** Test code point string. */
    private static final String TEST_DECOMP = "<medial> 0123";

    /** Test code point string. */
    private static final String TEST_DECIMAL = "9";

    /** Test code point string. */
    private static final String TEST_DIGIT = "8";

    /** Test code point string. */
    private static final String TEST_NUMERIC = "8/3";

    /** Test code point string. */
    private static final String TEST_MIRRORED = "Y";

    /** Test code point string. */
    private static final String TEST_OLD_NAME = "LITTLE FOO";

    /** Test code point string. */
    private static final String TEST_COMMENT = "JUST A FOO";

    /** Test code point string. */
    private static final String TEST_UPPERCASE = "2346";

    /** Test code point string. */
    private static final String TEST_LOWERCASE = "2347";

    /** Test code point string. */
    private static final String TEST_TITLECASE = "2348";

    /** Test case. */
    @Test
    @DisplayName("Constructor: code point")
    void testGetCodePoint() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(0x2345, chr.codePoint, "GetCodePoint");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: name")
    void testGetName() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(TEST_NAME, chr.name, "GetName");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: category")
    void testGetCategory() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(TEST_CATEGORY, chr.category, "GetCategory");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: combining")
    void testGetCombining() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Integer.valueOf(2), chr.combining, "GetCombining");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: BIDI category")
    void testGetBidiCategory() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(TEST_BIDI_CAT, chr.bidiCategory, "GetBidiCategory");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: decomp mapping")
    void testGetDecompMapping() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(EDecompMapping.MEDIAL, chr.decompMapping, "GetDecompMapping");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: decomp mapping code point")
    void testGetDecompMappingCodePoint() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertArrayEquals(chr.getDecompMappingCodePoints(), new Integer[]{Integer.valueOf(0x0123)},
                "GetDecompMappingCodePoint");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: decimal")
    void testGetDecimal() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Integer.valueOf(9), chr.decimal, "GetDecimal");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: digit")
    void testGetDigit() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Integer.valueOf(8), chr.digit, "GetDigit");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: numeric")
    void testGetNumeric() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Double.valueOf(8.0 / 3.0), chr.numeric, "GetNumeric");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: mirrored")
    void testIsMirrored() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Boolean.TRUE, chr.mirrored, "IsMirrored 1");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: old name")
    void testGetOldName() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(TEST_OLD_NAME, chr.oldName, "GetOldName");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: comment")
    void testGetComment1() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(TEST_COMMENT, chr.comment, "GetComment");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: uppercase")
    void testGetUppercase() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Integer.valueOf(0x2346), chr.uppercase, "GetUppercase");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: lowercase")
    void testGetLowercase() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Integer.valueOf(0x2347), chr.lowercase, "GetLowercase");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor: titlecase")
    void testGetTitlecase() {

        final UnicodeCharacter chr = new UnicodeCharacter(
                TEST_CODE_POINT, TEST_NAME, TEST_CATEGORY, TEST_COMBINING, TEST_BIDI_CAT,
                TEST_DECOMP, TEST_DECIMAL, TEST_DIGIT, TEST_NUMERIC, TEST_MIRRORED, TEST_OLD_NAME,
                TEST_COMMENT, TEST_UPPERCASE, TEST_LOWERCASE, TEST_TITLECASE);

        assertEquals(Integer.valueOf(0x2348), chr.titlecase, "GetTitlecase");
    }

    /** Test case. */
    @Test
    @DisplayName("stringValue")
    void testStringValue() {

        final Iterator<UnicodeCharacter> iter = UnicodeCharacterSet.getInstance().iterator();

        while (iter.hasNext()) {
            final UnicodeCharacter chr = iter.next();
            assertEquals(chr.stringValue(), new String(new int[]{chr.codePoint}, 0, 1), "StringValue");
        }
    }
}
