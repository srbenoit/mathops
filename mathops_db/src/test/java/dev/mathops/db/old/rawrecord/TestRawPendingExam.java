package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawPendingExam} class.
 */
final class TestRawPendingExam {

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(123456L);

    /** A field name. */
    private static final String TEST_VERSION = "MPTTC";

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_EXAM_SCORE = Integer.valueOf(10);

    /** A field name. */
    private static final Integer TEST_START_TIME = Integer.valueOf(200);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(300);

    /** A field name. */
    private static final String TEST_TIME_OK = "Y";

    /** A field name. */
    private static final String TEST_PASSED = "N";

    /** A field name. */
    private static final Integer TEST_SEQ_NBR = Integer.valueOf(33);

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(5);

    /** A field name. */
    private static final String TEST_EXAM_TYPE = "FE";

    /** A field name. */
    private static final Float TEST_TIMELIMIT_FACTOR = Float.valueOf(1.5f);

    /** A field name. */
    private static final String TEST_STU_TYPE = "Great";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=123456",
            "version=MPTTC",
            "stu_id=888888888",
            "exam_dt=2023-01-02",
            "exam_score=10",
            "start_time=200",
            "finish_time=300",
            "time_ok=Y",
            "passed=N",
            "seq_nbr=33",
            "course=M 117",
            "unit=5",
            "exam_type=FE",
            "timelimit_factor=1.5",
            "stu_type=Great");

    /**
     * Constructs a new {@code IvtRawPendingExam}.
     */
    TestRawPendingExam() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawPendingExam obj = new RawPendingExam(TEST_SERIAL_NBR, TEST_VERSION, TEST_STU_ID,
                TEST_EXAM_DT, TEST_EXAM_SCORE, TEST_START_TIME, TEST_FINISH_TIME, TEST_TIME_OK,
                TEST_PASSED, TEST_SEQ_NBR, TEST_COURSE, TEST_UNIT, TEST_EXAM_TYPE,
                TEST_TIMELIMIT_FACTOR, TEST_STU_TYPE);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_EXAM_SCORE, obj.examScore, "Invalid exam_score value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_TIME_OK, obj.timeOk, "Invalid time_ok value after constructor");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after constructor");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_EXAM_TYPE, obj.examType, "Invalid exam_type value after constructor");
        assertEquals(TEST_TIMELIMIT_FACTOR, obj.timelimitFactor, "Invalid timelimit_factor value after constructor");
        assertEquals(TEST_STU_TYPE, obj.stuType, "Invalid stu_type value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawPendingExam obj = new RawPendingExam(TEST_SERIAL_NBR, TEST_VERSION, TEST_STU_ID,
                TEST_EXAM_DT, TEST_EXAM_SCORE, TEST_START_TIME, TEST_FINISH_TIME, TEST_TIME_OK,
                TEST_PASSED, TEST_SEQ_NBR, TEST_COURSE, TEST_UNIT, TEST_EXAM_TYPE,
                TEST_TIMELIMIT_FACTOR, TEST_STU_TYPE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
