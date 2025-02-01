package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawZipCode} class.
 */
final class TestRawZipCode {

    /** A field name. */
    private static final String TEST_ZIP_CODE = "80535";

    /** A field name. */
    private static final String TEST_CITY = "Laporte";

    /** A field name. */
    private static final String TEST_STATE = "CO";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "zip_code=80535",
            "city=Laporte",
            "state=CO");

    /**
     * Constructs a new {@code IvtRawZipCode}.
     */
    TestRawZipCode() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawZipCode obj = new RawZipCode(TEST_ZIP_CODE, TEST_CITY, TEST_STATE);

        assertEquals(TEST_ZIP_CODE, obj.zipCode, "Invalid zip_code value after constructor");
        assertEquals(TEST_CITY, obj.city, "Invalid city value after constructor");
        assertEquals(TEST_STATE, obj.state, "Invalid state value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawZipCode obj = new RawZipCode(TEST_ZIP_CODE, TEST_CITY, TEST_STATE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
