package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStmathplan} class.
 */
final class TestRawStmathplan {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final Integer TEST_PIDM = Integer.valueOf(999);

    /** A field name. */
    private static final String TEST_APLN_TERM = "FA21";

    /** A field name. */
    private static final String TEST_VERSION = "ABCDE";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_SURVEY_NBR = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_STU_ANSWER = "A";

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(100);

    /** A field name. */
    private static final Long TEST_SESSION = Long.valueOf(987654321L);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "pidm=999",
            "apln_term=FA21",
            "version=ABCDE",
            "exam_dt=2023-01-02",
            "survey_nbr=4",
            "stu_answer=A",
            "finish_time=100",
            "session=987654321");

    /**
     * Constructs a new {@code IvtRawStmathplan}.
     */
    TestRawStmathplan() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStmathplan obj =
                new RawStmathplan(TEST_STU_ID, TEST_PIDM, TEST_APLN_TERM, TEST_VERSION, TEST_EXAM_DT,
                        TEST_SURVEY_NBR, TEST_STU_ANSWER, TEST_FINISH_TIME, TEST_SESSION);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_PIDM, obj.pidm, "Invalid pidm value after constructor");
        assertEquals(TEST_APLN_TERM, obj.aplnTerm, "Invalid apln_term value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_SURVEY_NBR, obj.surveyNbr, "Invalid survey_nbr value after constructor");
        assertEquals(TEST_STU_ANSWER, obj.stuAnswer, "Invalid stu_answer value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_SESSION, obj.session, "Invalid session value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStmathplan obj =
                new RawStmathplan(TEST_STU_ID, TEST_PIDM, TEST_APLN_TERM, TEST_VERSION, TEST_EXAM_DT,
                        TEST_SURVEY_NBR, TEST_STU_ANSWER, TEST_FINISH_TIME, TEST_SESSION);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
