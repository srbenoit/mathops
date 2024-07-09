package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawNewstu} class.
 */
final class TestRawNewstu {

    /** A field name. */
    private static final String TEST_STU_ID = "88888888";

    /** A field name. */
    private static final String TEST_ACAD_LEVEL = "UG";

    /** A field name. */
    private static final String TEST_REG_TYPE = "REG";

    /** A field name. */
    private static final String TEST_TERM = "202290";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=88888888",
            "acad_level=UG",
            "reg_type=REG",
            "term=202290");

    /**
     * Constructs a new {@code IvtRawNewstu}.
     */
    TestRawNewstu() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawNewstu obj = new RawNewstu(TEST_STU_ID, TEST_ACAD_LEVEL, TEST_REG_TYPE, TEST_TERM);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_ACAD_LEVEL, obj.acadLevel, "Invalid test_code value after constructor");
        assertEquals(TEST_REG_TYPE, obj.regType, "Invalid test_date value after constructor");
        assertEquals(TEST_TERM, obj.term, "Invalid test_score value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawNewstu obj = new RawNewstu(TEST_STU_ID, TEST_ACAD_LEVEL, TEST_REG_TYPE, TEST_TERM);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
