package dev.mathops.core.parser;

import dev.mathops.core.CoreConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the HexEncoder class.
 */
final class TestHexEncoder {

    /** A test byte array. */
    private static final byte[] TEST_BYTES = {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7,
            (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 50,
            (byte) -1, (byte) -15, (byte) -16, (byte) -50, (byte) 127, (byte) -128};

    /** The lowercase hex for the test bytes. */
    private static final String TEST_LC_HEX = "0102030405060708090a0b0c0d0e0f1032fff1f0ce7f80";

    /** The uppercase hex for the test bytes. */
    private static final String TEST_UC_HEX = "0102030405060708090A0B0C0D0E0F1032FFF1F0CE7F80";

    /** A zero-length byte array. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * A test case.
     */
    @Test
    @DisplayName("Encode empty lowercase")
    void testEncodeLower1() {

        assertEquals(HexEncoder.encodeLowercase(ZERO_LEN_BYTE_ARR), CoreConstants.EMPTY,
                "Encode empty lowercase failed");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Encode lowercase")
    void testEncodeLower2() {

        assertEquals(HexEncoder.encodeLowercase(TEST_BYTES), TEST_LC_HEX, "Encode lowercase failed");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Encode empty uppercase")
    void testEncodeUpper1() {

        assertEquals(HexEncoder.encodeUppercase(ZERO_LEN_BYTE_ARR), CoreConstants.EMPTY,
                "Encode empty uppercase failed");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Encode uppercase")
    void testEncodeUpper2() {

        assertEquals(HexEncoder.encodeUppercase(TEST_BYTES), TEST_UC_HEX, "Encode uppercase failed");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode valid empty")
    void testDecode1() {

        assertArrayEquals(HexEncoder.decode(CoreConstants.EMPTY), ZERO_LEN_BYTE_ARR, "Decode valid empty failed");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode valid lowercase")
    void testDecode2() {

        assertArrayEquals(HexEncoder.decode(TEST_LC_HEX), TEST_BYTES, "Decode valid lowercase failed");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode valid uppercase")
    void testDecode3() {

        assertArrayEquals(HexEncoder.decode(TEST_UC_HEX), TEST_BYTES, "Decode valid uppercase failed");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode of invalid string (0).")
    void testDecode4() {

        assertThrows(IllegalArgumentException.class, ()->HexEncoder.decode(TEST_LC_HEX + "0"),
                "Decode of invalid string (0) did not throw exception");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode of invalid string (~Z).")
    void testDecode5() {

        assertThrows(IllegalArgumentException.class, ()->HexEncoder.decode(TEST_LC_HEX + "~Z"),
                "Decode of invalid string (~Z) did not throw exception");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode of invalid string (0:).")
    void testDecode6() {

        assertThrows(IllegalArgumentException.class, ()->HexEncoder.decode(TEST_LC_HEX + "0:"),
                "Decode of invalid string (0:) did not throw exception");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode of invalid string (^0).")
    void testDecode7() {

        assertThrows(IllegalArgumentException.class, ()->HexEncoder.decode(TEST_LC_HEX + "^0"),
                "Decode of invalid string (^0) did not throw exception");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Decode of invalid string (!0).")
    void testDecode8() {

        assertThrows(IllegalArgumentException.class, ()->HexEncoder.decode(TEST_LC_HEX + "!0"),
                "Decode of invalid string (!0) did not throw exception");
    }
}
