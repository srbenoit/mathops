package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardsCourseGradingSystemRec} class.
 */
public final class TestStandardsCourseGradingSystemRec {

    /** A field name. */
    private static final String TEST_GRADING_SYSTEM_ID = "G1";

    /** A field name. */
    private static final Integer TEST_NBR_STANDARDS = Integer.valueOf(24);

    /** A field name. */
    private static final Integer TEST_MIN_STANDARDS = Integer.valueOf(18);

    /** A field name. */
    private static final Integer TEST_NBR_ESSENTIAL_STANDARDS = Integer.valueOf(6);

    /** A field name. */
    private static final Integer TEST_MIN_ESSENTIAL_STANDARDS = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_HOMEWORK_PTS = Integer.valueOf(0);

    /** A field name. */
    private static final Integer TEST_ON_TIME_MASTERY_PTS = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_LATE_MASTERY_PTS = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_A_MIN_SCORE = Integer.valueOf(108);

    /** A field name. */
    private static final Integer TEST_B_MIN_SCORE = Integer.valueOf(96);

    /** A field name. */
    private static final Integer TEST_C_MIN_SCORE = Integer.valueOf(84);

    /** A field name. */
    private static final Integer TEST_D_MIN_SCORE = Integer.valueOf(72);

    /** A field name. */
    private static final Integer TEST_U_MIN_SCORE = Integer.valueOf(12);

    /** A field name. */
    private static final Integer TEST_MIN_STANDARDS_FOR_INC = Integer.valueOf(13);

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "grading_system_id=G1",
            "nbr_standards=24",
            "min_standards=18",
            "nbr_essential_standards=6",
            "min_essential_standards=5",
            "homework_pts=0",
            "on_time_mastery_pts=5",
            "late_mastery_pts=4",
            "a_min_score=108",
            "b_min_score=96",
            "c_min_score=84",
            "d_min_score=72",
            "u_min_score=12",
            "min_standards_for_inc=13");

    /**
     * Constructs a new {@code TestStandardsCourseGradingSystemRec}.
     */
    TestStandardsCourseGradingSystemRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardsCourseGradingSystemRec obj = new StandardsCourseGradingSystemRec(TEST_GRADING_SYSTEM_ID,
                TEST_NBR_STANDARDS, TEST_MIN_STANDARDS, TEST_NBR_ESSENTIAL_STANDARDS, TEST_MIN_ESSENTIAL_STANDARDS,
                TEST_HOMEWORK_PTS, TEST_ON_TIME_MASTERY_PTS, TEST_LATE_MASTERY_PTS, TEST_A_MIN_SCORE, TEST_B_MIN_SCORE,
                TEST_C_MIN_SCORE, TEST_D_MIN_SCORE, TEST_U_MIN_SCORE, TEST_MIN_STANDARDS_FOR_INC);

        assertEquals(TEST_GRADING_SYSTEM_ID, obj.gradingSystemId, "Invalid grading system ID value after constructor");
        assertEquals(TEST_NBR_STANDARDS, obj.nbrStandards, "Invalid number of standards value after constructor");
        assertEquals(TEST_MIN_STANDARDS, obj.minStandards, "Invalid min standards value after constructor");
        assertEquals(TEST_NBR_ESSENTIAL_STANDARDS, obj.nbrEssentialStandards,
                "Invalid number of essential standards value after constructor");
        assertEquals(TEST_MIN_ESSENTIAL_STANDARDS, obj.minEssentialStandards,
                "Invalid min essential standards value after constructor");
        assertEquals(TEST_HOMEWORK_PTS, obj.homeworkPts, "Invalid homework pts value after constructor");
        assertEquals(TEST_ON_TIME_MASTERY_PTS, obj.onTimeMasteryPts,
                "Invalid on-time mastery pts value after constructor");
        assertEquals(TEST_LATE_MASTERY_PTS, obj.lateMasteryPts, "Invalid late mastery pts value after constructor");
        assertEquals(TEST_A_MIN_SCORE, obj.aMinScore, "Invalid A min score value after constructor");
        assertEquals(TEST_B_MIN_SCORE, obj.bMinScore, "Invalid B min score value after constructor");
        assertEquals(TEST_C_MIN_SCORE, obj.cMinScore, "Invalid C min score after constructor");
        assertEquals(TEST_D_MIN_SCORE, obj.dMinScore, "Invalid D min score after constructor");
        assertEquals(TEST_U_MIN_SCORE, obj.uMinScore, "Invalid U min score after constructor");
        assertEquals(TEST_MIN_STANDARDS_FOR_INC, obj.minStandardsForInc,
                "Invalid min standards for inc after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardsCourseGradingSystemRec obj = new StandardsCourseGradingSystemRec(TEST_GRADING_SYSTEM_ID,
                TEST_NBR_STANDARDS, TEST_MIN_STANDARDS, TEST_NBR_ESSENTIAL_STANDARDS, TEST_MIN_ESSENTIAL_STANDARDS,
                TEST_HOMEWORK_PTS, TEST_ON_TIME_MASTERY_PTS, TEST_LATE_MASTERY_PTS, TEST_A_MIN_SCORE, TEST_B_MIN_SCORE,
                TEST_C_MIN_SCORE, TEST_D_MIN_SCORE, TEST_U_MIN_SCORE, TEST_MIN_STANDARDS_FOR_INC);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}