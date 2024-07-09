package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStchallenge} class.
 */
final class TestRawStchallenge {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_VERSION = ChallengeExamLogic.M117_CHALLENGE_EXAM_ID;

    /** A field name. */
    private static final String TEST_ACADEMIC_YR = "2021";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_START_TIME = Integer.valueOf(100);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(200);

    /** A field name. */
    private static final String TEST_LAST_NAME = "Benoit";

    /** A field name. */
    private static final String TEST_FIRST_NAME = "Steve";

    /** A field name. */
    private static final String TEST_MIDDLE_INITIAL = "R";

    /** A field name. */
    private static final Integer TEST_SEQ_NBR = Integer.valueOf(123456);

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(987654321L);

    /** A field name. */
    private static final Integer TEST_SCORE = Integer.valueOf(17);

    /** A field name. */
    private static final String TEST_PASSED = "Y";

    /** A field name. */
    private static final String TEST_HOW_VALIDATED = "S";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "stu_id=888888888",
            "course=M 117",
            "version=MC117",
            "academic_yr=2021",
            "exam_dt=2023-01-02",
            "start_time=100",
            "finish_time=200",
            "last_name=Benoit",
            "first_name=Steve",
            "middle_initial=R",
            "seq_nbr=123456",
            "serial_nbr=987654321",
            "score=17",
            "passed=Y",
            "how_validated=S");

    /**
     * Constructs a new {@code IvtRawStchallenge}.
     */
    TestRawStchallenge() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStchallenge obj = new RawStchallenge(TEST_STU_ID, TEST_COURSE, TEST_VERSION,
                TEST_ACADEMIC_YR, TEST_EXAM_DT, TEST_START_TIME, TEST_FINISH_TIME, TEST_LAST_NAME,
                TEST_FIRST_NAME, TEST_MIDDLE_INITIAL, TEST_SEQ_NBR, TEST_SERIAL_NBR, TEST_SCORE,
                TEST_PASSED, TEST_HOW_VALIDATED);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_ACADEMIC_YR, obj.academicYr, "Invalid academic_yr value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_LAST_NAME, obj.lastName, "Invalid last_name value after constructor");
        assertEquals(TEST_FIRST_NAME, obj.firstName, "Invalid first_name value after constructor");
        assertEquals(TEST_MIDDLE_INITIAL, obj.middleInitial, "Invalid middle_initial value after constructor");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after constructor");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after constructor");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after constructor");
        assertEquals(TEST_HOW_VALIDATED, obj.howValidated, "Invalid how_validated value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStchallenge obj = new RawStchallenge(TEST_STU_ID, TEST_COURSE, TEST_VERSION,
                TEST_ACADEMIC_YR, TEST_EXAM_DT, TEST_START_TIME, TEST_FINISH_TIME, TEST_LAST_NAME,
                TEST_FIRST_NAME, TEST_MIDDLE_INITIAL, TEST_SEQ_NBR, TEST_SERIAL_NBR, TEST_SCORE,
                TEST_PASSED, TEST_HOW_VALIDATED);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
