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
    private static final String TEST_TITLE = "Survey Title";

    /** A field name. */
    private static final String TEST_PROMPT = "Survey Prompt <b>HTML</b>.";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "survey_id=SURV001",
            "survey_title=Survey Title",
            "prompt_html=Survey Prompt <b>HTML</b>.");

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

        final CourseSurveyRec obj = new CourseSurveyRec(TEST_SURVEY_ID, TEST_TITLE, TEST_PROMPT);

        assertEquals(TEST_SURVEY_ID, obj.surveyId, "Invalid survey ID value after constructor");
        assertEquals(TEST_TITLE, obj.surveyTitle, "Invalid survey title value after constructor");
        assertEquals(TEST_PROMPT, obj.promptHtml, "Invalid prompt HTML value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CourseSurveyRec obj = new CourseSurveyRec(TEST_SURVEY_ID, TEST_TITLE, TEST_PROMPT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}