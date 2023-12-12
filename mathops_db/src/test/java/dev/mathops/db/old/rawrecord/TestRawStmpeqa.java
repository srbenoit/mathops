package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStmpeqa} class.
 */
final class TestRawStmpeqa {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_VERSION = "MPTTC";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(123);

    /** A field name. */
    private static final Integer TEST_QUESTION_NBR = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_STU_ANSWER = "A,B,C";

    /** A field name. */
    private static final String TEST_ANS_CORRECT = "Y";

    /** A field name. */
    private static final String TEST_SUBTEST = "125";

    /** A field name. */
    private static final String TEST_TREE_REF = "a.b.c.d.e";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "version=MPTTC",
            "exam_dt=2023-01-02",
            "finish_time=123",
            "question_nbr=1",
            "stu_answer=A,B,C",
            "ans_correct=Y",
            "subtest=125",
            "tree_ref=a.b.c.d.e");

    /**
     * Constructs a new {@code IvtRawStmpeqa}.
     */
    TestRawStmpeqa() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStmpeqa obj =
                new RawStmpeqa(TEST_STU_ID, TEST_VERSION, TEST_EXAM_DT, TEST_FINISH_TIME,
                        TEST_QUESTION_NBR, TEST_STU_ANSWER, TEST_ANS_CORRECT, TEST_SUBTEST, TEST_TREE_REF);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid question_nbr value after constructor");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after constructor");
        assertEquals(TEST_ANS_CORRECT, obj.ansCorrect, "Invalid ans_correct value after constructor");
        assertEquals(TEST_SUBTEST, obj.subtest, "Invalid subtest value after constructor");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStmpeqa obj =
                new RawStmpeqa(TEST_STU_ID, TEST_VERSION, TEST_EXAM_DT, TEST_FINISH_TIME,
                        TEST_QUESTION_NBR, TEST_STU_ANSWER, TEST_ANS_CORRECT, TEST_SUBTEST, TEST_TREE_REF);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStmpeqa obj = RawStmpeqa.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after deserialization");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after deserialization");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid question_nbr value after deserialization");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after deserialization");
        assertEquals(TEST_ANS_CORRECT, obj.ansCorrect, "Invalid ans_correct value after deserialization");
        assertEquals(TEST_SUBTEST, obj.subtest, "Invalid subtest value after deserialization");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after deserialization");
    }
}
