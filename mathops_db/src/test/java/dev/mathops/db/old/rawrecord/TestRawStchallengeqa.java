package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStchallengeqa} class.
 */
final class TestRawStchallengeqa {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_VERSION = ChallengeExamLogic.M117_CHALLENGE_EXAM_ID;

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(200);

    /** A field name. */
    private static final Integer TEST_QUESTION_NBR = Integer.valueOf(13);

    /** A field name. */
    private static final String TEST_STU_ANSWER = "123.45";

    /** A field name. */
    private static final String TEST_ANS_CORRECT = "N";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "course=M 117",
            "version=MC117",
            "exam_dt=2023-01-02",
            "finish_time=200",
            "question_nbr=13",
            "stu_answer=123.45",
            "ans_correct=N");

    /**
     * Constructs a new {@code IvtRawStchallengeqa}.
     */
    TestRawStchallengeqa() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStchallengeqa obj = new RawStchallengeqa(TEST_STU_ID, TEST_COURSE, TEST_VERSION,
                TEST_EXAM_DT, TEST_FINISH_TIME, TEST_QUESTION_NBR, TEST_STU_ANSWER, TEST_ANS_CORRECT);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid question_nbr value after constructor");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after constructor");
        assertEquals(TEST_ANS_CORRECT, obj.ansCorrect, "Invalid ans_correct value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStchallengeqa obj = new RawStchallengeqa(TEST_STU_ID, TEST_COURSE, TEST_VERSION,
                TEST_EXAM_DT, TEST_FINISH_TIME, TEST_QUESTION_NBR, TEST_STU_ANSWER, TEST_ANS_CORRECT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
