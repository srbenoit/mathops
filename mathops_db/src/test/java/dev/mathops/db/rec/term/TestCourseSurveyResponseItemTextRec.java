package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code CourseSurveyResponseItemTextRec} class.
 */
public final class TestCourseSurveyResponseItemTextRec {

    /** A field name. */
    private static final Integer TEST_SERIAL_NBR = Integer.valueOf(12345);

    /** A field name. */
    private static final Integer TEST_ITEM_NBR = Integer.valueOf(5);

    /** A field name. */
    private static final String TEST_RESPONSE_TEXT = "Spectacular";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=12345",
            "item_nbr=5",
            "response_text=Spectacular");

    /**
     * Constructs a new {@code TesCourseSurveyResponseItemTextRec}.
     */
    TestCourseSurveyResponseItemTextRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final CourseSurveyResponseItemTextRec obj = new CourseSurveyResponseItemTextRec(TEST_SERIAL_NBR,
                TEST_ITEM_NBR, TEST_RESPONSE_TEXT);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial number ID value after constructor");
        assertEquals(TEST_ITEM_NBR, obj.itemNbr, "Invalid item number value after constructor");
        assertEquals(TEST_RESPONSE_TEXT, obj.responseText, "Invalid response text value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CourseSurveyResponseItemTextRec obj = new CourseSurveyResponseItemTextRec(TEST_SERIAL_NBR,
                TEST_ITEM_NBR, TEST_RESPONSE_TEXT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}