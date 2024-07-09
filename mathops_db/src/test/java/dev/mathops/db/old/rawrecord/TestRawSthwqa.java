package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawSthwqa} class.
 */
final class TestRawSthwqa {

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(123456789L);

    /** A field name. */
    private static final Integer TEST_QUESTION_NBR = Integer.valueOf(1);

    /** A field name. */
    private static final Integer TEST_ANSWER_NBR = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_OBJECTIVE = "1.4";

    /** A field name. */
    private static final String TEST_STU_ANSWER = "A,B,C";

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_VERSION = "171HW";

    /** A field name. */
    private static final String TEST_ANS_CORRECT = "Y";

    /** A field name. */
    private static final LocalDate TEST_HW_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(123);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "serial_nbr=123456789",
            "question_nbr=1",
            "answer_nbr=2",
            "objective=1.4",
            "stu_answer=A,B,C",
            "stu_id=888888888",
            "version=171HW",
            "ans_correct=Y",
            "hw_dt=2023-01-02",
            "finish_time=123");

    /**
     * Constructs a new {@code IvtRawSthwqa}.
     */
    TestRawSthwqa() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawSthwqa obj = new RawSthwqa(TEST_SERIAL_NBR, TEST_QUESTION_NBR, TEST_ANSWER_NBR,
                TEST_OBJECTIVE, TEST_STU_ANSWER, TEST_STU_ID, TEST_VERSION, TEST_ANS_CORRECT,
                TEST_HW_DT, TEST_FINISH_TIME);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid question_nbr value after constructor");
        assertEquals(TEST_ANSWER_NBR, obj.answerNbr, "Invalid answer_nbr value after constructor");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after constructor");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_ANS_CORRECT, obj.ansCorrect, "Invalid ans_correct value after constructor");
        assertEquals(TEST_HW_DT, obj.hwDt, "Invalid hw_dt value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawSthwqa obj = new RawSthwqa(TEST_SERIAL_NBR, TEST_QUESTION_NBR, TEST_ANSWER_NBR,
                TEST_OBJECTIVE, TEST_STU_ANSWER, TEST_STU_ID, TEST_VERSION, TEST_ANS_CORRECT,
                TEST_HW_DT, TEST_FINISH_TIME);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
