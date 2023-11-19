package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStexam} class.
 */
final class TestRawStexam {

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(123456789L);

    /** A field name. */
    private static final String TEST_VERSION = "171UE";

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_EXAM_SCORE = Integer.valueOf(100);

    /** A field name. */
    private static final Integer TEST_MASTERY_SCORE = Integer.valueOf(200);

    /** A field name. */
    private static final Integer TEST_START_TIME = Integer.valueOf(3000);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(4000);

    /** A field name. */
    private static final String TEST_TIME_OK = "Y";

    /** A field name. */
    private static final String TEST_PASSED = "N";

    /** A field name. */
    private static final Integer TEST_SEQ_NBR = Integer.valueOf(500);

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_EXAM_TYPE = "R";

    /** A field name. */
    private static final String TEST_IS_FIRST_PASSED = "X";

    /** A field name. */
    private static final String TEST_EXAM_SOURCE = "Z";

    /** A field name. */
    private static final String TEST_CALC_NBR = "999";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=123456789",
            "version=171UE",
            "stu_id=888888888",
            "exam_dt=2023-01-02",
            "exam_score=100",
            "mastery_score=200",
            "start_time=3000",
            "finish_time=4000",
            "time_ok=Y",
            "passed=N",
            "seq_nbr=500",
            "course=M 117",
            "unit=1",
            "exam_type=R",
            "is_first_passed=X",
            "exam_source=Z",
            "calc_nbr=999");

    /**
     * Constructs a new {@code IvtRawStexam}.
     */
    TestRawStexam() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStexam obj = new RawStexam(TEST_SERIAL_NBR, TEST_VERSION, TEST_STU_ID,
                TEST_EXAM_DT, TEST_EXAM_SCORE, TEST_MASTERY_SCORE, TEST_START_TIME, TEST_FINISH_TIME,
                TEST_TIME_OK, TEST_PASSED, TEST_SEQ_NBR, TEST_COURSE, TEST_UNIT, TEST_EXAM_TYPE,
                TEST_IS_FIRST_PASSED, TEST_EXAM_SOURCE, TEST_CALC_NBR);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_EXAM_SCORE, obj.examScore, "Invalid exam_score value after constructor");
        assertEquals(TEST_MASTERY_SCORE, obj.masteryScore, "Invalid masery_score value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_TIME_OK, obj.timeOk, "Invalid time_ok value after constructor");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after constructor");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_EXAM_TYPE, obj.examType, "Invalid exam_type value after constructor");
        assertEquals(TEST_IS_FIRST_PASSED, obj.isFirstPassed, "Invalid is_first_passed value after constructor");
        assertEquals(TEST_EXAM_SOURCE, obj.examSource, "Invalid exam_source value after constructor");
        assertEquals(TEST_CALC_NBR, obj.calcNbr, "Invalid calc_nbr value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStexam obj = new RawStexam(TEST_SERIAL_NBR, TEST_VERSION, TEST_STU_ID,
                TEST_EXAM_DT, TEST_EXAM_SCORE, TEST_MASTERY_SCORE, TEST_START_TIME, TEST_FINISH_TIME,
                TEST_TIME_OK, TEST_PASSED, TEST_SEQ_NBR, TEST_COURSE, TEST_UNIT, TEST_EXAM_TYPE,
                TEST_IS_FIRST_PASSED, TEST_EXAM_SOURCE, TEST_CALC_NBR);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStexam obj = RawStexam.parse(EXPECT_SER99);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after deserialization");
        assertEquals(TEST_EXAM_SCORE, obj.examScore, "Invalid exam_score value after deserialization");
        assertEquals(TEST_MASTERY_SCORE, obj.masteryScore, "Invalid masery_score value after deserialization");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after deserialization");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after deserialization");
        assertEquals(TEST_TIME_OK, obj.timeOk, "Invalid time_ok value after deserialization");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after deserialization");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after deserialization");
        assertEquals(TEST_EXAM_TYPE, obj.examType, "Invalid exam_type value after deserialization");
        assertEquals(TEST_IS_FIRST_PASSED, obj.isFirstPassed, "Invalid is_first_passed value after deserialization");
        assertEquals(TEST_EXAM_SOURCE, obj.examSource, "Invalid exam_source value after deserialization");
        assertEquals(TEST_CALC_NBR, obj.calcNbr, "Invalid calc_nbr value after deserialization");
    }
}
