package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code CourseSurveyItemRec} class.
 */
public final class TestCourseSurveyItemRec {

    /** A field name. */
    private static final String TEST_SURVEY_ID = "SURV001";

    /** A field name. */
    private static final Integer TEST_ITEM_NBR = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_ITEM_TYPE = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_PROMPT_HTML = "What is your <b>favorite</b> color?";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "survey_id=SURV001",
            "item_nbr=4",
            "item_type=2",
            "prompt_html=What is your <b>favorite</b> color?");

    /**
     * Constructs a new {@code TestCourseSurveyItemRec}.
     */
    TestCourseSurveyItemRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final CourseSurveyItemRec obj = new CourseSurveyItemRec(TEST_SURVEY_ID, TEST_ITEM_NBR, TEST_ITEM_TYPE,
                TEST_PROMPT_HTML);

        assertEquals(TEST_SURVEY_ID, obj.surveyId, "Invalid survey ID value after constructor");
        assertEquals(TEST_ITEM_NBR, obj.itemNbr, "Invalid item number value after constructor");
        assertEquals(TEST_ITEM_TYPE, obj.itemType, "Invalid item type value after constructor");
        assertEquals(TEST_PROMPT_HTML, obj.promptHtml, "Invalid prompt HTML value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CourseSurveyItemRec obj = new CourseSurveyItemRec(TEST_SURVEY_ID, TEST_ITEM_NBR, TEST_ITEM_TYPE,
                TEST_PROMPT_HTML);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}