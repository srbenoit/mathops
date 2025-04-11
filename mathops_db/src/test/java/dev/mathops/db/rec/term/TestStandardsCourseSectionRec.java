package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardsCourseSectionRec} class.
 */
public final class TestStandardsCourseSectionRec {

    /** A field name. */
    private static final String TEST_COURSE_ID = "MATH 101";

    /** A field name. */
    private static final String TEST_SECTION_NBR = "001";

    /** A field name. */
    private static final String TEST_CRN = "123654";

    /** A field name. */
    private static final LocalDate TEST_ARIES_START_DATE = LocalDate.of(2025, 1, 19);

    /** A field name. */
    private static final LocalDate TEST_ARIES_END_DATE = LocalDate.of(2025, 5, 11);

    /** A field name. */
    private static final LocalDate TEST_FIRST_CLASS_DATE = LocalDate.of(2025, 1, 21);

    /** A field name. */
    private static final LocalDate TEST_LAST_CLASS_DATE = LocalDate.of(2025, 5, 9);

    /** A field name. */
    private static final String TEST_SUBTERM = "FULL";

    /** A field name. */
    private static final String TEST_GRADING_SYSTEM_ID = "G1";

    /** A field name. */
    private static final String TEST_CAMPUS = "FC";

    /** A field name. */
    private static final String TEST_CANVAS_ID = "CAN123";

    /** A field name. */
    private static final String TEST_INSTRUCTOR = "John Doe";

    /** A field name. */
    private static final String TEST_BUILDING_NAME = "Weber";

    /** A field name. */
    private static final String TEST_ROOM_NBR = "321A";

    /** A field name. */
    private static final Integer TEST_WEEKDAYS = Integer.valueOf(13);

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "course_id=MATH 101",
            "section_nbr=001",
            "crn=123654",
            "aries_start_date=2025-01-19",
            "aries_end_date=2025-05-11",
            "first_class_date=2025-01-21",
            "last_class_date=2025-05-09",
            "subterm=FULL",
            "grading_system_id=G1",
            "campus=FC",
            "canvas_id=CAN123",
            "instructor=John Doe",
            "building_name=Weber",
            "room_nbr=321A",
            "weekdays=13");

    /**
     * Constructs a new {@code TestStandardsCourseSectionRec}.
     */
    TestStandardsCourseSectionRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardsCourseSectionRec obj = new StandardsCourseSectionRec(TEST_COURSE_ID,
                TEST_SECTION_NBR, TEST_CRN, TEST_ARIES_START_DATE, TEST_ARIES_END_DATE,
                TEST_FIRST_CLASS_DATE, TEST_LAST_CLASS_DATE, TEST_SUBTERM, TEST_GRADING_SYSTEM_ID, TEST_CAMPUS,
                TEST_CANVAS_ID, TEST_INSTRUCTOR, TEST_BUILDING_NAME, TEST_ROOM_NBR, TEST_WEEKDAYS);

        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course ID value after constructor");
        assertEquals(TEST_SECTION_NBR, obj.sectionNbr, "Invalid section number value after constructor");
        assertEquals(TEST_CRN, obj.crn, "Invalid CRN value after constructor");
        assertEquals(TEST_ARIES_START_DATE, obj.ariesStartDate, "Invalid Aries start date value after constructor");
        assertEquals(TEST_ARIES_END_DATE, obj.ariesEndDate, "Invalid Aries end date value after constructor");
        assertEquals(TEST_FIRST_CLASS_DATE, obj.firstClassDate, "Invalid first class date value after constructor");
        assertEquals(TEST_LAST_CLASS_DATE, obj.lastClassDate, "Invalid last class date value after constructor");
        assertEquals(TEST_SUBTERM, obj.subterm, "Invalid subterm value after constructor");
        assertEquals(TEST_GRADING_SYSTEM_ID, obj.gradingSystemId, "Invalid grading system ID value after constructor");
        assertEquals(TEST_CAMPUS, obj.campus, "Invalid campus value after constructor");
        assertEquals(TEST_CANVAS_ID, obj.canvasId, "Invalid canvas ID after constructor");
        assertEquals(TEST_INSTRUCTOR, obj.instructor, "Invalid instructor after constructor");
        assertEquals(TEST_BUILDING_NAME, obj.buildingName, "Invalid building name after constructor");
        assertEquals(TEST_ROOM_NBR, obj.roomNbr, "Invalid room number after constructor");
        assertEquals(TEST_WEEKDAYS, obj.weekdays, "Invalid weekdays after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardsCourseSectionRec obj = new StandardsCourseSectionRec(TEST_COURSE_ID,
                TEST_SECTION_NBR, TEST_CRN, TEST_ARIES_START_DATE, TEST_ARIES_END_DATE,
                TEST_FIRST_CLASS_DATE, TEST_LAST_CLASS_DATE, TEST_SUBTERM, TEST_GRADING_SYSTEM_ID, TEST_CAMPUS,
                TEST_CANVAS_ID, TEST_INSTRUCTOR, TEST_BUILDING_NAME, TEST_ROOM_NBR, TEST_WEEKDAYS);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}