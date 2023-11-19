package dev.mathops.core.unicode;

import dev.mathops.core.CoreConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

/**
 * Tests for the UnicodeCharacterSet class.
 */
final class TestUnicodeCharacterSet {

    /** Test case. */
    @Test
    @DisplayName("isValid for valid character")
    void testIsValid1() {

        assertTrue(UnicodeCharacterSet.getInstance().isValid(0x30), "isValid 1");
    }

    /** Test case. */
    @Test
    @DisplayName("isValid for invalid character")
    void testIsValid2() {

        assertFalse(UnicodeCharacterSet.getInstance().isValid(-1), "isValid 2");
    }

    /** Test case. */
    @Test
    @DisplayName("iterator count")
    void testIterator() {

        final Iterator<UnicodeCharacter> iter = UnicodeCharacterSet.getInstance().iterator();

        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            ++count;
        }

        assertTrue(count > 0, "iterator");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter code point")
    void testGetCharacter1() {

        // 0073;LATIN SMALL LETTER S;Ll;0;L;;;;;N;;;0053;;0053
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x73);

        assertNotNull(chr, "getCharacter 1, char is null");
        assertEquals(0x73, chr.codePoint, "getCharacter 1");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter name")
    void testGetCharacter2() {

        // 0073;LATIN SMALL LETTER S;Ll;0;L;;;;;N;;;0053;;0053
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x73);

        assertNotNull(chr, "getCharacter 2, char is null");
        assertEquals("LATIN SMALL LETTER S", chr.name, "getCharacter 2");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter category")
    void testGetCharacter3() {

        // 0073;LATIN SMALL LETTER S;Ll;0;L;;;;;N;;;0053;;0053
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x73);

        assertNotNull(chr, "getCharacter 3, char is null");
        assertEquals("Ll", chr.category, "getCharacter 3");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter combining")
    void testGetCharacter4() {

        // 0073;LATIN SMALL LETTER S;Ll;0;L;;;;;N;;;0053;;0053
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x73);

        assertNotNull(chr, "getCharacter 4, char is null");
        assertEquals(Integer.valueOf(0), chr.combining, "getCharacter 4");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter BIDI category")
    void testGetCharacter5() {

        // 0073;LATIN SMALL LETTER S;Ll;0;L;;;;;N;;;0053;;0053
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x73);

        assertNotNull(chr, "getCharacter 5, char is null");
        assertEquals("L", chr.bidiCategory, "getCharacter 5");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter decomp mapping")
    void testGetCharacter6() {

        // 00A8;DIAERESIS;Sk;0;ON;<compat> 0020 0308;;;;N;SPACING DIAERESIS;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0xA8);

        assertNotNull(chr, "getCharacter 6, char is null");
        assertEquals(EDecompMapping.COMPAT, chr.decompMapping, "getCharacter 6");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter decomp mapping code points")
    void testGetCharacter7() {

        // 00A8;DIAERESIS;Sk;0;ON;<compat> 0020 0308;;;;N;SPACING DIAERESIS;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0xA8);

        assertNotNull(chr, "getCharacter 7, char is null");
        assertArrayEquals(new Integer[]{Integer.valueOf(0x0020), Integer.valueOf(0x0308)},
                chr.getDecompMappingCodePoints(), "getCharacter 7");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter decimal")
    void testGetCharacter8() {

        // 0031;DIGIT ONE;Nd;0;EN;;1;1;1;N;;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x0031);

        assertNotNull(chr, "getCharacter 8, char is null");
        assertEquals(Integer.valueOf(1), chr.decimal, "getCharacter 8");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter digit")
    void testGetCharacter9() {

        // 00B2;SUPERSCRIPT TWO;No;0;EN;<super> 0032;;2;2;N;SUPERSCRIPT DIGIT TWO;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x00B2);

        assertNotNull(chr, "getCharacter 9, char is null");
        assertEquals(Integer.valueOf(2), chr.digit, "getCharacter 9");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter numeric")
    void testGetCharacter10() {

        // 2150;VULGAR FRACTION ONE SEVENTH;No;0;ON;<fraction> 0031 2044 0037;;;1/7;N;;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x2150);

        assertNotNull(chr, "getCharacter 10, char is null");
        assertEquals(Double.valueOf(1.0 / 7.0), chr.numeric, "getCharacter 10");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter mirrored")
    void testGetCharacter11() {

        // 2019;RIGHT SINGLE QUOTATION MARK;Pf;0;ON;;;;;N;SINGLE COMMA QUOTATION MARK;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x2019);

        assertNotNull(chr, "getCharacter 11, char is null");
        assertEquals(Boolean.FALSE, chr.mirrored, "getCharacter 11");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter old name")
    void testGetCharacter12() {

        // 2019;RIGHT SINGLE QUOTATION MARK;Pf;0;ON;;;;;N;SINGLE COMMA QUOTATION MARK;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x2019);

        assertNotNull(chr, "getCharacter 12, char is null");
        assertEquals("SINGLE COMMA QUOTATION MARK", chr.oldName, "getCharacter 12");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter comment")
    void testGetCharacter13() {

        // 2019;RIGHT SINGLE QUOTATION MARK;Pf;0;ON;;;;;N;SINGLE COMMA QUOTATION MARK;;;;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x2019);

        assertNotNull(chr, "getCharacter 13, char is null");
        assertEquals(CoreConstants.EMPTY, chr.comment, "getCharacter 13");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter uppercase")
    void testGetCharacter14() {

        // 006E;LATIN SMALL LETTER N;Ll;0;L;;;;;N;;;004E;;004E
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x6E);

        assertNotNull(chr, "getCharacter 14, char is null");
        assertEquals(Integer.valueOf(0x4E), chr.uppercase, "getCharacter 14");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter lowercase")
    void testGetCharacter15() {

        // 0052;LATIN CAPITAL LETTER R;Lu;0;L;;;;;N;;;;0072;
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x52);

        assertNotNull(chr, "getCharacter 15, char is null");
        assertEquals(Integer.valueOf(0x72), chr.lowercase, "getCharacter 15");
    }

    /** Test case. */
    @Test
    @DisplayName("getCharacter title case")
    void testGetCharacter16() {

        // 006E;LATIN SMALL LETTER N;Ll;0;L;;;;;N;;;004E;;004E
        final UnicodeCharacter chr = UnicodeCharacterSet.getInstance().getCharacter(0x6E);

        assertNotNull(chr, "getCharacter 16, char is null");
        assertEquals(Integer.valueOf(0x4E), chr.titlecase, "getCharacter 16");
    }

    /** Test case. */
    @Test
    @DisplayName("toUppercase from lowercase")
    void testToUppercase1() {

        final int cp = UnicodeCharacterSet.getInstance().toUppercase('n');

        assertEquals('N', cp, "toUppercase 1");
    }

    /** Test case. */
    @Test
    @DisplayName("toUppercase from uppercase")
    void testToUppercase2() {

        final int cp = UnicodeCharacterSet.getInstance().toUppercase('N');

        assertEquals('N', cp, "toUppercase 2");
    }

    /** Test case. */
    @Test
    @DisplayName("toUppercase from digit")
    void testToUppercase3() {

        final int cp = UnicodeCharacterSet.getInstance().toUppercase('1');

        assertEquals('1', cp, "toUppercase 3");
    }

    /** Test case. */
    @Test
    @DisplayName("toUppercase from negative integer")
    void testToUppercase4() {

        final int cp = UnicodeCharacterSet.getInstance().toUppercase(-1);

        assertEquals(-1, cp, "toUppercase 4");
    }

    /** Test case. */
    @Test
    @DisplayName("toLowercase from uppercase")
    void testToLowercase1() {

        final int cp = UnicodeCharacterSet.getInstance().toLowercase('N');

        assertEquals('n', cp, "toLowercase 1");
    }

    /** Test case. */
    @Test
    @DisplayName("toLowercase from lowercase")
    void testToLowercase2() {

        final int cp = UnicodeCharacterSet.getInstance().toLowercase('n');

        assertEquals('n', cp, "toLowercase 2");
    }

    /** Test case. */
    @Test
    @DisplayName("toLowercase from digit")
    void testToLowercase3() {

        final int cp = UnicodeCharacterSet.getInstance().toLowercase('1');

        assertEquals('1', cp, "toLowercase 3");
    }

    /** Test case. */
    @Test
    @DisplayName("toLowercase from negative integer")
    void testToLowercase4() {

        final int cp = UnicodeCharacterSet.getInstance().toLowercase(-1);

        assertEquals(-1, cp, "toLowercase 4");
    }

    /** Test case. */
    @Test
    @DisplayName("toTitlecase from title case")
    void testToTitlecase1() {

        final int cp = UnicodeCharacterSet.getInstance().toTitlecase(0x01C7);

        assertEquals(0x01C8, cp, "toTitlecase 1");
    }

    /** Test case. */
    @Test
    @DisplayName("toTitlecase from lowercase")
    void testToTitlecase2() {

        final int cp = UnicodeCharacterSet.getInstance().toTitlecase(0x01C8);

        assertEquals(0x01C8, cp, "toTitlecase 2");
    }

    /** Test case. */
    @Test
    @DisplayName("toTitlecase from uppercase")
    void testToTitlecase3() {

        final int cp = UnicodeCharacterSet.getInstance().toTitlecase('N');

        assertEquals('N', cp, "toTitlecase 3");
    }

    /** Test case. */
    @Test
    @DisplayName("toTitlecase from negative integer")
    void testToTitlecase4() {

        final int cp = UnicodeCharacterSet.getInstance().toTitlecase(-1);

        assertEquals(-1, cp, "toTitlecase 4");
    }
}
