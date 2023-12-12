package dev.mathops.db.old.rec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

/**
 * Tests for the {@code MasteryAttemptRec} class.
 */
final class TestMasteryAttemptRec {

    /** A field name. */
    private static final Integer TEST_SERIAL_NBR = Integer.valueOf(11111);

    /** A field name. */
    private static final String TEST_EXAM_ID = "ST41_2_M";

    /** A field name. */
    private static final String TEST_STU_ID = "888777666";

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_STARTED = LocalDateTime.of(2022, 1, 2, 3, 4, 5);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_FINISHED = LocalDateTime.of(2023, 2, 3, 4, 5, 6);

    /** A field name. */
    private static final Integer TEST_EXAM_SCORE = Integer.valueOf(10);

    /** A field name. */
    private static final Integer TEST_MASTERY_SCORE = Integer.valueOf(8);

    /** A field name. */
    private static final String TEST_PASSED = "Y";

    /** A field name. */
    private static final String TEST_IS_FIRST_PASSED = "N";

    /** A field name. */
    private static final String TEST_EXAM_SOURCE = "TC";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=11111",
            "exam_id=ST41_2_M",
            "stu_id=888777666",
            "when_started=2022-01-02T03:04:05",
            "when_finished=2023-02-03T04:05:06",
            "exam_score=10",
            "mastery_score=8",
            "passed=Y",
            "is_first_passed=N",
            "exam_source=TC");

    /**
     * Constructs a new {@code IvtMasteryAttemptRec}.
     */
    TestMasteryAttemptRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final MasteryAttemptRec obj = new MasteryAttemptRec(TEST_SERIAL_NBR, TEST_EXAM_ID,
                TEST_STU_ID, TEST_WHEN_STARTED, TEST_WHEN_FINISHED, TEST_EXAM_SCORE, TEST_MASTERY_SCORE,
                TEST_PASSED, TEST_IS_FIRST_PASSED, TEST_EXAM_SOURCE);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_EXAM_ID, obj.examId, "Invalid exam_id value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_WHEN_STARTED, obj.whenStarted, "Invalid when_started value after constructor");
        assertEquals(TEST_WHEN_FINISHED, obj.whenFinished, "Invalid when_finished value after constructor");
        assertEquals(TEST_EXAM_SCORE, obj.examScore, "Invalid exam_score value after constructor");
        assertEquals(TEST_MASTERY_SCORE, obj.masteryScore, "Invalid mastery_score value after constructor");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after constructor");
        assertEquals(TEST_IS_FIRST_PASSED, obj.isFirstPassed, "Invalid is_first_passed value after constructor");
        assertEquals(TEST_EXAM_SOURCE, obj.examSource, "Invalid exam_source value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final MasteryAttemptRec obj = new MasteryAttemptRec(TEST_SERIAL_NBR, TEST_EXAM_ID, TEST_STU_ID,
                TEST_WHEN_STARTED, TEST_WHEN_FINISHED, TEST_EXAM_SCORE, TEST_MASTERY_SCORE, TEST_PASSED,
                TEST_IS_FIRST_PASSED, TEST_EXAM_SOURCE);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final MasteryAttemptRec obj = MasteryAttemptRec.parse(EXPECT_SER99);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after deserialization");
        assertEquals(TEST_EXAM_ID, obj.examId, "Invalid exam_id value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_WHEN_STARTED, obj.whenStarted, "Invalid when_started value after deserialization");
        assertEquals(TEST_WHEN_FINISHED, obj.whenFinished, "Invalid when_finished value after deserialization");
        assertEquals(TEST_EXAM_SCORE, obj.examScore, "Invalid exam_score value after deserialization");
        assertEquals(TEST_MASTERY_SCORE, obj.masteryScore, "Invalid mastery_score value after deserialization");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after deserialization");
        assertEquals(TEST_IS_FIRST_PASSED, obj.isFirstPassed, "Invalid is_first_passed value after deserialization");
        assertEquals(TEST_EXAM_SOURCE, obj.examSource, "Invalid exam_source value after deserialization");
    }
}
