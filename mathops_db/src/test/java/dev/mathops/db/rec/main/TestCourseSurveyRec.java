package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code CourseSurveyRec} class.
 */
public final class TestCourseSurveyRec {

    /** A field name. */
    private static final String TEST_SURVEY_ID = "SURV001";

    /** A field name. */
    private static final Integer TEST_OPEN_WEEK = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_OPEN_DAY = Integer.valueOf(2);

    /** A field name. */
    private static final Integer TEST_CLOSE_WEEK = Integer.valueOf(12);

    /** A field name. */
    private static final Integer TEST_CLOSE_DAY = Integer.valueOf(5);

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "survey_id=SURV001",
            "open_week=4",
            "open_day=2",
            "close_week=12",
            "close_day=5");

    /**
     * Constructs a new {@code TestCourseSurveyRec}.
     */
    TestCourseSurveyRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final CourseSurveyRec obj = new CourseSurveyRec(TEST_SURVEY_ID, TEST_OPEN_WEEK, TEST_OPEN_DAY, TEST_CLOSE_WEEK,
                TEST_CLOSE_DAY);

        assertEquals(TEST_SURVEY_ID, obj.surveyId, "Invalid survey ID value after constructor");
        assertEquals(TEST_OPEN_WEEK, obj.openWeek, "Invalid open week value after constructor");
        assertEquals(TEST_OPEN_DAY, obj.openDay, "Invalid open day value after constructor");
        assertEquals(TEST_CLOSE_WEEK, obj.closeWeek, "Invalid close week value after constructor");
        assertEquals(TEST_CLOSE_DAY, obj.closeDay, "Invalid close day value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CourseSurveyRec obj = new CourseSurveyRec(TEST_SURVEY_ID, TEST_OPEN_WEEK, TEST_OPEN_DAY, TEST_CLOSE_WEEK,
                TEST_CLOSE_DAY);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}