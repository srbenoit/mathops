package dev.mathops.db.old.rec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code MasteryAttemptQaRec} class.
 */
final class TestMasteryAttemptQaRec {

    /** A field name. */
    private static final Integer TEST_SERIAL_NBR = Integer.valueOf(11111);

    /** A field name. */
    private static final String TEST_EXAM_ID = "ST41_2_M";

    /** A field name. */
    private static final Integer TEST_QUESTION_NBR = Integer.valueOf(3);

    /** A field name. */
    private static final String TEST_CORRECT = "N";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=11111",
            "exam_id=ST41_2_M",
            "question_nbr=3",
            "correct=N");

    /**
     * Constructs a new {@code IvtMasteryAttemptQaRec}.
     */
    TestMasteryAttemptQaRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final MasteryAttemptQaRec obj =
                new MasteryAttemptQaRec(TEST_SERIAL_NBR, TEST_EXAM_ID, TEST_QUESTION_NBR, TEST_CORRECT);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_EXAM_ID, obj.examId, "Invalid exam_id value after constructor");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid exam_score value after constructor");
        assertEquals(TEST_CORRECT, obj.correct, "Invalid passed value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final MasteryAttemptQaRec obj = new MasteryAttemptQaRec(TEST_SERIAL_NBR, TEST_EXAM_ID, TEST_QUESTION_NBR,
                TEST_CORRECT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
