package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStsurveyqa} class.
 */
final class TestRawStsurveyqa {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_VERSION = "UOOOO";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_SURVEY_NBR = Integer.valueOf(10);

    /** A field name. */
    private static final String TEST_STU_ANSWER = "Y";

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(2000);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "version=UOOOO",
            "exam_dt=2023-01-02",
            "survey_nbr=10",
            "stu_answer=Y",
            "finish_time=2000");

    /**
     * Constructs a new {@code IvtRawStsurveyqa}.
     */
    TestRawStsurveyqa() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStsurveyqa obj = new RawStsurveyqa(TEST_STU_ID, TEST_VERSION, TEST_EXAM_DT,
                TEST_SURVEY_NBR, TEST_STU_ANSWER, TEST_FINISH_TIME);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_SURVEY_NBR, obj.surveyNbr, "Invalid survey_nbr value after constructor");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStsurveyqa obj = new RawStsurveyqa(TEST_STU_ID, TEST_VERSION, TEST_EXAM_DT,
                TEST_SURVEY_NBR, TEST_STU_ANSWER, TEST_FINISH_TIME);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
