package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStqa} class.
 */
final class TestRawStqa {

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(987654321L);

    /** A field name. */
    private static final Integer TEST_QUESTION_NBR = Integer.valueOf(1);

    /** A field name. */
    private static final Integer TEST_ANSWER_NBR = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_OBJECTIVE = "4.3";

    /** A field name. */
    private static final String TEST_STU_ANSWER = "A,B,C";

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_VERSION = "174UE";

    /** A field name. */
    private static final String TEST_ANS_CORRECT = "Y";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final String TEST_SUBTEST = "SCORE";

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(123);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=987654321",
            "question_nbr=1",
            "answer_nbr=2",
            "objective=4.3",
            "stu_answer=A,B,C",
            "stu_id=888888888",
            "version=174UE",
            "ans_correct=Y",
            "exam_dt=2023-01-02",
            "subtest=SCORE",
            "finish_time=123");

    /**
     * Constructs a new {@code IvtRawStqa}.
     */
    TestRawStqa() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStqa obj = new RawStqa(TEST_SERIAL_NBR, TEST_QUESTION_NBR, TEST_ANSWER_NBR,
                TEST_OBJECTIVE, TEST_STU_ANSWER, TEST_STU_ID, TEST_VERSION, TEST_ANS_CORRECT,
                TEST_EXAM_DT, TEST_SUBTEST, TEST_FINISH_TIME);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid question_nbr value after constructor");
        assertEquals(TEST_ANSWER_NBR, obj.answerNbr, "Invalid answer_nbr value after constructor");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after constructor");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_ANS_CORRECT, obj.ansCorrect, "Invalid ans_correct value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_SUBTEST, obj.subtest, "Invalid subtest value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStqa obj = new RawStqa(TEST_SERIAL_NBR, TEST_QUESTION_NBR, TEST_ANSWER_NBR,
                TEST_OBJECTIVE, TEST_STU_ANSWER, TEST_STU_ID, TEST_VERSION, TEST_ANS_CORRECT,
                TEST_EXAM_DT, TEST_SUBTEST, TEST_FINISH_TIME);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStqa obj = RawStqa.parse(EXPECT_SER99);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after deserialization");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid question_nbr value after deserialization");
        assertEquals(TEST_ANSWER_NBR, obj.answerNbr, "Invalid abswer_nbr value after deserialization");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after deserialization");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_ANS_CORRECT, obj.ansCorrect, "Invalid ans_correct value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after deserialization");
        assertEquals(TEST_SUBTEST, obj.subtest, "Invalid subtest value after deserialization");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after deserialization");
    }
}
