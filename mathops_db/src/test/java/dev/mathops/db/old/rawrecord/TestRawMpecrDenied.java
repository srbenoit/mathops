package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawMpecrDenied} class.
 */
final class TestRawMpecrDenied {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_EXAM_PLACED = "P";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final String TEST_WHY_DENIED = "V";

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(123456L);

    /** A field name. */
    private static final String TEST_VERSION = "MPTTC";

    /** A field name. */
    private static final String TEST_EXAM_SOURCE = "SY";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "course=M 117",
            "exam_placed=P",
            "exam_dt=2023-01-02",
            "why_denied=V",
            "serial_nbr=123456",
            "version=MPTTC",
            "exam_source=SY");

    /**
     * Constructs a new {@code IvtRawMpecrDenied}.
     */
    TestRawMpecrDenied() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawMpecrDenied obj = new RawMpecrDenied(TEST_STU_ID, TEST_COURSE, TEST_EXAM_PLACED,
                TEST_EXAM_DT, TEST_WHY_DENIED, TEST_SERIAL_NBR, TEST_VERSION, TEST_EXAM_SOURCE);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_EXAM_PLACED, obj.examPlaced, "Invalid exam_placed value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_WHY_DENIED, obj.whyDenied, "Invalid why_denied value after constructor");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_EXAM_SOURCE, obj.examSource, "Invalid exam_source value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawMpecrDenied obj = new RawMpecrDenied(TEST_STU_ID, TEST_COURSE, TEST_EXAM_PLACED,
                TEST_EXAM_DT, TEST_WHY_DENIED, TEST_SERIAL_NBR, TEST_VERSION, TEST_EXAM_SOURCE);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawMpecrDenied obj = RawMpecrDenied.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_EXAM_PLACED, obj.examPlaced, "Invalid exam_placed value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after deserialization");
        assertEquals(TEST_WHY_DENIED, obj.whyDenied, "Invalid why_denied value after deserialization");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_EXAM_SOURCE, obj.examSource, "Invalid exam_source value after deserialization");
    }
}
