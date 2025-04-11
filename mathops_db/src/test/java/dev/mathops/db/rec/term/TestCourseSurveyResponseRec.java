package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code CourseSurveyResponseRec} class.
 */
public final class TestCourseSurveyResponseRec {

    /** A field name. */
    private static final Integer TEST_SERIAL_NBR = Integer.valueOf(12345);

    /** A field name. */
    private static final String TEST_SURVEY_ID = "SURV001";

    /** A field name. */
    private static final String TEST_STUDENT_ID = "876543210";

    /** A field name. */
    private static final LocalDate TEST_RESPONSE_DATE = LocalDate.of(2025, Month.APRIL, 10);

    /** A field name. */
    private static final LocalTime TEST_RESPONSE_TIME = LocalTime.of(22, 35, 10);

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=12345",
            "survey_id=SURV001",
            "student_id=876543210",
            "response_date=2025-04-10",
            "response_time=22:35:10");

    /**
     * Constructs a new {@code TestCourseSurveyResponseRec}.
     */
    TestCourseSurveyResponseRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final CourseSurveyResponseRec obj = new CourseSurveyResponseRec(TEST_SERIAL_NBR, TEST_SURVEY_ID,
                TEST_STUDENT_ID, TEST_RESPONSE_DATE, TEST_RESPONSE_TIME);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial number ID value after constructor");
        assertEquals(TEST_SURVEY_ID, obj.surveyId, "Invalid survey ID value after constructor");
        assertEquals(TEST_STUDENT_ID, obj.studentId, "Invalid student ID value after constructor");
        assertEquals(TEST_RESPONSE_DATE, obj.responseDate, "Invalid response date value after constructor");
        assertEquals(TEST_RESPONSE_TIME, obj.responseTime, "Invalid response time value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CourseSurveyResponseRec obj = new CourseSurveyResponseRec(TEST_SERIAL_NBR, TEST_SURVEY_ID,
                TEST_STUDENT_ID, TEST_RESPONSE_DATE, TEST_RESPONSE_TIME);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}