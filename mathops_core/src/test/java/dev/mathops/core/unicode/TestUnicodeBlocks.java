package dev.mathops.core.unicode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the UnicodeBlocks class.
 */
final class TestUnicodeBlocks {

    /** Commonly used string. */
    private static final String TEST_STRING_1 = "FooBarBaz";

    /** Commonly used string. */
    private static final String TEST_STRING_2 = "foo_bar_baz";

    /** Commonly used string. */
    private static final String TEST_STRING_3 = "foo-bar-baz";

    /** Commonly used string. */
    private static final String TEST_STRING_4 = "foo bar\tbaz";

    /** Commonly used string. */
    private static final String TEST_STRING_NORM = "foobarbaz";

    /** Commonly used string. */
    private static final String BASIC_LATIN = "Basic Latin";

    /** Commonly used string. */
    private static final String BASIC_LATIN_NOSP = "BasicLatin";

    /** Commonly used string. */
    private static final String LATIN_1_SUPPLEMENT = "Latin-1 Supplement";

    /** Commonly used string. */
    private static final String PRIVATE = "Supplementary Private Use Area-B";

    /** Test case. */
    @Test
    @DisplayName("normalizeBlockName with simple string")
    void testNormalize1() {

        final String norm = UnicodeBlocks.normalizeBlockName(TEST_STRING_1);

        assertEquals(norm, TEST_STRING_NORM, "NormalizeBlockName 1");
    }

    /** Test case. */
    @Test
    @DisplayName("normalizeBlockName with string with underscores")
    void testNormalize2() {

        final String norm = UnicodeBlocks.normalizeBlockName(TEST_STRING_2);

        assertEquals(norm, TEST_STRING_NORM, "NormalizeBlockName 2");
    }

    /** Test case. */
    @Test
    @DisplayName("normalizeBlockName with string with dashes")
    void testNormalize3() {

        final String norm = UnicodeBlocks.normalizeBlockName(TEST_STRING_3);

        assertEquals(norm, TEST_STRING_NORM, "NormalizeBlockName 3");
    }

    /** Test case. */
    @Test
    @DisplayName("normalizeBlockName with string with space, tab")
    void testNormalize4() {

        final String norm = UnicodeBlocks.normalizeBlockName(TEST_STRING_4);

        assertEquals(norm, TEST_STRING_NORM, "NormalizeBlockName 4");
    }

    /** Test case. */
    @Test
    @DisplayName("StripSpaces with simple string")
    void testStripSpaces1() {

        final String norm = UnicodeBlocks.stripSpaces(TEST_STRING_1);

        assertEquals(norm, TEST_STRING_1, "StripSpaces 1");
    }

    /** Test case. */
    @Test
    @DisplayName("StripSpaces with string with underscores")
    void testStripSpaces2() {

        final String norm = UnicodeBlocks.stripSpaces(TEST_STRING_2);

        assertEquals(norm, TEST_STRING_2, "StripSpaces 2");
    }

    /** Test case. */
    @Test
    @DisplayName("StripSpaces with string with dashes")
    void testStripSpaces3() {

        final String norm = UnicodeBlocks.stripSpaces(TEST_STRING_3);

        assertEquals(norm, TEST_STRING_3, "StripSpaces 3");
    }

    /** Test case. */
    @Test
    @DisplayName("StripSpaces with string with space, tab")
    void testStripSpaces4() {

        final String norm = UnicodeBlocks.stripSpaces(TEST_STRING_4);

        assertEquals(norm, TEST_STRING_NORM, "StripSpaces 4");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: Basic Latin character in 'basiclatin'")
    void testIsInBlock1() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x7F,
                UnicodeBlocks.normalizeBlockName(BASIC_LATIN));

        assertTrue(inBlock, "IsInBlock 1");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: Basic Latin character in 'BasicLatin'")
    void testIsInBlock2() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x7F, UnicodeBlocks.stripSpaces(BASIC_LATIN));

        assertTrue(inBlock, "IsInBlock 2");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: Supplemental Latin character in 'basiclatin'")
    void testIsInBlock3() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x80,
                UnicodeBlocks.normalizeBlockName(BASIC_LATIN));

        assertFalse(inBlock, "IsInBlock 3");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: Supplemental Latin character in 'BasicLatin'")
    void testIsInBlock4() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x80, UnicodeBlocks.stripSpaces(BASIC_LATIN));

        assertFalse(inBlock, "IsInBlock 4");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: least Supplemental Latin character in 'latin1supplement'")
    void testIsInBlock5() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x80,
                UnicodeBlocks.normalizeBlockName(LATIN_1_SUPPLEMENT));

        assertTrue(inBlock, "IsInBlock 5");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: least Supplemental Latin character in 'Latin-1Supplement'")
    void testIsInBlock6() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x80,
                UnicodeBlocks.stripSpaces(LATIN_1_SUPPLEMENT));

        assertTrue(inBlock, "IsInBlock 6");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: greatest Supplemental Latin character in 'latin1supplement'")
    void testIsInBlock7() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0xFF,
                UnicodeBlocks.normalizeBlockName(LATIN_1_SUPPLEMENT));

        assertTrue(inBlock, "IsInBlock 7");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: greatest Supplemental Latin character in 'Latin-1Supplement'")
    void testIsInBlock8() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0xFF,
                UnicodeBlocks.stripSpaces(LATIN_1_SUPPLEMENT));

        assertTrue(inBlock, "IsInBlock 8");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: beyond Supplemental Latin character in 'latin1supplement'")
    void testIsInBlock9() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x100,
                UnicodeBlocks.normalizeBlockName(LATIN_1_SUPPLEMENT));

        assertFalse(inBlock, "IsInBlock 9");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: beyond Supplemental Latin character in 'Latin-1Supplement'")
    void testIsInBlock10() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x100,
                UnicodeBlocks.stripSpaces(LATIN_1_SUPPLEMENT));

        assertFalse(inBlock, "IsInBlock 10");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: least private character in 'supplementaryprivateuseareab'")
    void testIsInBlock11() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x100000,
                UnicodeBlocks.normalizeBlockName(PRIVATE));

        assertTrue(inBlock, "IsInBlock 11");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: least private character in 'SupplementaryPrivateUseAreaB'")
    void testIsInBlock12() {

        final boolean inBlock =
                UnicodeBlocks.getInstance().isInBlock(0x100000, UnicodeBlocks.stripSpaces(PRIVATE));

        assertTrue(inBlock, "IsInBlock 12");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: greatest private character in 'supplementaryprivateuseareab'")
    void testIsInBlock13() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x10FFFF,
                UnicodeBlocks.normalizeBlockName(PRIVATE));

        assertTrue(inBlock, "IsInBlock 13");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: greatest private character in 'SupplementaryPrivateUseAreaB'")
    void testIsInBlock14() {

        final boolean inBlock =
                UnicodeBlocks.getInstance().isInBlock(0x10FFFF, UnicodeBlocks.stripSpaces(PRIVATE));

        assertTrue(inBlock, "IsInBlock 14");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: beyond private character in 'supplementaryprivateuseareab'")
    void testIsInBlock15() {

        final boolean inBlock = UnicodeBlocks.getInstance().isInBlock(0x110000,
                UnicodeBlocks.normalizeBlockName(PRIVATE));

        assertFalse(inBlock, "IsInBlock 15");
    }

    /** Test case. */
    @Test
    @DisplayName("isInBlock: beyond private character in 'SupplementaryPrivateUseAreaB'")
    void testIsInBlock16() {

        final boolean inBlock =
                UnicodeBlocks.getInstance().isInBlock(0x110000, UnicodeBlocks.stripSpaces(PRIVATE));

        assertFalse(inBlock, "IsInBlock 16");
    }

    /** Test case. */
    @Test
    @DisplayName("isValidNoSpaceName: invalid name")
    void testIsValidNoSpaceName1() {

        final UnicodeBlocks instance = UnicodeBlocks.getInstance();
        final boolean valid = instance.isValidNoSpaceName(BASIC_LATIN);

        assertFalse(valid, "IsValidNoSpaceName 1");
    }

    /** Test case. */
    @Test
    @DisplayName("isValidNoSpaceName: valid name")
    void testIsValidNoSpaceName2() {

        final UnicodeBlocks instance = UnicodeBlocks.getInstance();
        final boolean valid = instance.isValidNoSpaceName(BASIC_LATIN_NOSP);

        assertTrue(valid, "IsValidNoSpaceName 2");
    }
}
