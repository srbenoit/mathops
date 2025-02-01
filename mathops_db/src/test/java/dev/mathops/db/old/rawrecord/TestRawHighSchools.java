package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawHighSchools} class.
 */
final class TestRawHighSchools {

    /** A field name. */
    private static final String TEST_HS_CODE = "1010";

    /** A field name. */
    private static final String TEST_HS_NAME = "Foo County High School";

    /** A field name. */
    private static final String TEST_ADDRES_1 = "100 Learning Way";

    /** A field name. */
    private static final String TEST_CITY = "Knowledgeton";

    /** A field name. */
    private static final String TEST_STATE = "CO";

    /** A field name. */
    private static final String TEST_ZIP_CODE = "80999";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "hs_code=1010",
            "hs_name=Foo County High School",
            "addres_1=100 Learning Way",
            "city=Knowledgeton",
            "state=CO",
            "zip_code=80999");

    /**
     * Constructs a new {@code IvtRawHighSchools}.
     */
    TestRawHighSchools() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawHighSchools obj = new RawHighSchools(TEST_HS_CODE, TEST_HS_NAME, TEST_ADDRES_1,
                TEST_CITY, TEST_STATE, TEST_ZIP_CODE);

        assertEquals(TEST_HS_CODE, obj.hsCode, "Invalid hs_code value after constructor");
        assertEquals(TEST_HS_NAME, obj.hsName, "Invalid hs_name value after constructor");
        assertEquals(TEST_ADDRES_1, obj.addres1, "Invalid addres_1 value after constructor");
        assertEquals(TEST_CITY, obj.city, "Invalid city value after constructor");
        assertEquals(TEST_STATE, obj.state, "Invalid state value after constructor");
        assertEquals(TEST_ZIP_CODE, obj.zipCode, "Invalid zip_code value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawHighSchools obj = new RawHighSchools(TEST_HS_CODE, TEST_HS_NAME, TEST_ADDRES_1,
                TEST_CITY, TEST_STATE, TEST_ZIP_CODE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
