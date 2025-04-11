package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code CourseSurveyItemChoiceRec} class.
 */
public final class TestCourseSurveyItemChoiceRec {

    /** A field name. */
    private static final String TEST_SURVEY_ID = "SURV001";

    /** A field name. */
    private static final Integer TEST_ITEM_NBR = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_CHOICE_NBR = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_CHOICE_HTML = "I am <strong>exceptionally</strong> happy with the course!";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "survey_id=SURV001",
            "item_nbr=4",
            "choice_nbr=2",
            "choice_html=I am <strong>exceptionally</strong> happy with the course!");

    /**
     * Constructs a new {@code TestCourseSurveyItemChoiceRec}.
     */
    TestCourseSurveyItemChoiceRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final CourseSurveyItemChoiceRec obj = new CourseSurveyItemChoiceRec(TEST_SURVEY_ID, TEST_ITEM_NBR,
                TEST_CHOICE_NBR, TEST_CHOICE_HTML);

        assertEquals(TEST_SURVEY_ID, obj.surveyId, "Invalid survey ID value after constructor");
        assertEquals(TEST_ITEM_NBR, obj.itemNbr, "Invalid item number value after constructor");
        assertEquals(TEST_CHOICE_NBR, obj.choiceNbr, "Invalidc choice number value after constructor");
        assertEquals(TEST_CHOICE_HTML, obj.choiceHtml, "Invalid choice HTML value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CourseSurveyItemChoiceRec obj = new CourseSurveyItemChoiceRec(TEST_SURVEY_ID, TEST_ITEM_NBR,
                TEST_CHOICE_NBR, TEST_CHOICE_HTML);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}