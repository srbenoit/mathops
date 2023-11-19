package dev.mathops.db.rawrecord;

import dev.mathops.db.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawUsers} class.
 */
final class TestRawUsers {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(987654321L);

    /** A field name. */
    private static final String TEST_VERSION = "UPPPP";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_EXAM_SCORE = Integer.valueOf(17);

    /** A field name. */
    private static final String TEST_CALC_COURSE = "M 160";

    /** A field name. */
    private static final String TEST_PASSED = "Y";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "stu_id=888888888",
            "serial_nbr=987654321",
            "version=UPPPP",
            "exam_dt=2023-01-02",
            "exam_score=17",
            "calc_course=M 160",
            "passed=Y");

    /**
     * Constructs a new {@code IvtRawUsers}.
     */
    TestRawUsers() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawUsers obj = new RawUsers(TEST_TERM, TEST_STU_ID, TEST_SERIAL_NBR, TEST_VERSION,
                TEST_EXAM_DT, TEST_EXAM_SCORE, TEST_CALC_COURSE, TEST_PASSED);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_EXAM_SCORE, obj.examScore, "Invalid exam_score value after constructor");
        assertEquals(TEST_CALC_COURSE, obj.calcCourse, "Invalid calc_course value after constructor");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawUsers obj = new RawUsers(TEST_TERM, TEST_STU_ID, TEST_SERIAL_NBR, TEST_VERSION,
                TEST_EXAM_DT, TEST_EXAM_SCORE, TEST_CALC_COURSE, TEST_PASSED);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawUsers obj = RawUsers.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after deserialization");
        assertEquals(TEST_EXAM_SCORE, obj.examScore, "Invalid exam_score value after deserialization");
        assertEquals(TEST_CALC_COURSE, obj.calcCourse, "Invalid calc_course value after deserialization");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after deserialization");
    }
}
