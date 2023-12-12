package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawFfrTrns} class.
 */
final class TestRawFfrTrns {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_EXAM_PLACED = "P";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2022, 1, 2);

    /** A field name. */
    private static final LocalDate TEST_DT_CR_REFUSED = LocalDate.of(2023, 2, 3);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "course=M 117",
            "exam_placed=P",
            "exam_dt=2022-01-02",
            "dt_cr_refused=2023-02-03");

    /**
     * Constructs a new {@code IvtRawFfrTrns}.
     */
    TestRawFfrTrns() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawFfrTrns obj = new RawFfrTrns(TEST_STU_ID, TEST_COURSE, TEST_EXAM_PLACED,
                TEST_EXAM_DT, TEST_DT_CR_REFUSED);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_EXAM_PLACED, obj.examPlaced, "Invalid exam_placed value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_DT_CR_REFUSED, obj.dtCrRefused, "Invalid dt_cr_refused value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawFfrTrns obj = new RawFfrTrns(TEST_STU_ID, TEST_COURSE, TEST_EXAM_PLACED,
                TEST_EXAM_DT, TEST_DT_CR_REFUSED);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawFfrTrns obj = RawFfrTrns.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_EXAM_PLACED, obj.examPlaced, "Invalid exam_placed value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after deserialization");
        assertEquals(TEST_DT_CR_REFUSED, obj.dtCrRefused, "Invalid dt_cr_refused value after deserialization");
    }
}
