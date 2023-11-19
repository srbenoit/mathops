package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawSpecialStus} class.
 */
final class TestRawSpecialStus {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_STU_TYPE = "ADM";

    /** A field name. */
    private static final LocalDate TEST_START_DT = LocalDate.of(2022, 1, 2);

    /** A field name. */
    private static final LocalDate TEST_END_DT = LocalDate.of(2023, 2, 3);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "stu_type=ADM",
            "start_dt=2022-01-02",
            "end_dt=2023-02-03");

    /**
     * Constructs a new {@code IvtRawSpecialStus}.
     */
    TestRawSpecialStus() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawSpecialStus obj = new RawSpecialStus(TEST_STU_ID, TEST_STU_TYPE, TEST_START_DT, TEST_END_DT);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_STU_TYPE, obj.stuType, "Invalid stu_type value after constructor");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after constructor");
        assertEquals(TEST_END_DT, obj.endDt, "Invalid end_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawSpecialStus obj =
                new RawSpecialStus(TEST_STU_ID, TEST_STU_TYPE, TEST_START_DT, TEST_END_DT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawSpecialStus obj = RawSpecialStus.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_STU_TYPE, obj.stuType, "Invalid stu_type value after deserialization");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after deserialization");
        assertEquals(TEST_END_DT, obj.endDt, "Invalid end_dt value after deserialization");
    }
}
