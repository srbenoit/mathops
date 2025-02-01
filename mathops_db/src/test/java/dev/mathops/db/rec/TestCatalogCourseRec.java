package dev.mathops.db.rec;

import dev.mathops.db.enums.EGradeMode;
import dev.mathops.db.enums.EOfferingTermName;
import dev.mathops.db.type.CatalogCourseNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code CatalogCourse} class.
 */
final class TestCatalogCourseRec {

    /** A test field value. */
    private static final CatalogCourseNumber TEST_COURSE_NUMBER = new CatalogCourseNumber("MATH", "118");

    /** A test field value. */
    private static final String TEST_TITLE = "College Algebra II";

    /** A test field value. */
    private static final String TEST_DESCRIPTION =
            "Polynomials, Rational Functions, Roots and Powers, and Systems of Equations.";

    /** A test field value. */
    private static final String TEST_PREREQUISITE = "MATH 117 or MATH 120";

    /** A test field value. */
    private static final String TEST_REGISTRATION_INFO = "Credit not allowed for both MATH 118 and MATH 120";

    /** A test field value. */
    private static final String TEST_RESTRICTION = "Undergraduate only";

    /** A test field value. */
    private static final EnumSet<EOfferingTermName> TEST_TERMS_OFFERED = EnumSet.of(EOfferingTermName.EVERY_SPRING,
            EOfferingTermName.EVERY_SUMMER, EOfferingTermName.EVERY_FALL);

    /** A test field value. */
    private static final EGradeMode TEST_GRADE_MODE = EGradeMode.TRADITIONAL;

    /** A test field value. */
    private static final String TEST_SPECIAL_COURSE_FEE = "None";

    /** A test field value. */
    private static final String TEST_ADDITIONAL_INFO = "Challenge Exam Available";

    /** A test field value. */
    private static final String TEST_GT_CODE = "MA1";

    /** A test field value. */
    private static final Integer TEST_MIN_CREDITS = Integer.valueOf(1);

    /** A test field value. */
    private static final Integer TEST_MAX_CREDITS = Integer.valueOf(3);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "course_number=MATH 118",
            "title=College Algebra II",
            "description=Polynomials, Rational Functions, Roots and Powers, and Systems of Equations.",
            "prerequisite=MATH 117 or MATH 120",
            "registration_info=Credit not allowed for both MATH 118 and MATH 120",
            "restriction=Undergraduate only",
            "terms_offered=Spring,Summer,Fall",
            "grade_mode=TRADITIONAL",
            "special_course_fee=None",
            "additional_info=Challenge Exam Available",
            "gt_code=MA1",
            "min_credits=1",
            "max_credits=3");

    /**
     * Constructs a new {@code TestCatalogCourseRec}.
     */
    TestCatalogCourseRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final CatalogCourseRec obj = new CatalogCourseRec(TEST_COURSE_NUMBER, TEST_TITLE, TEST_DESCRIPTION,
                TEST_PREREQUISITE, TEST_REGISTRATION_INFO, TEST_RESTRICTION, TEST_TERMS_OFFERED, TEST_GRADE_MODE,
                TEST_SPECIAL_COURSE_FEE, TEST_ADDITIONAL_INFO, TEST_GT_CODE, TEST_MIN_CREDITS, TEST_MAX_CREDITS);

        assertEquals(TEST_COURSE_NUMBER, obj.courseNumber, "Invalid course_number value after constructor");
        assertEquals(TEST_TITLE, obj.title, "Invalid title value after constructor");
        assertEquals(TEST_DESCRIPTION, obj.description, "Invalid description value after constructor");
        assertEquals(TEST_PREREQUISITE, obj.prerequisite, "Invalid prerequisite value after constructor");
        assertEquals(TEST_REGISTRATION_INFO, obj.registrationInfo, "Invalid registration_info value after constructor");
        assertEquals(TEST_RESTRICTION, obj.restriction, "Invalid restriction value after constructor");
        assertEquals(TEST_TERMS_OFFERED, obj.termsOffered, "Invalid terms_offered value after constructor");
        assertEquals(TEST_GRADE_MODE, obj.gradeMode, "Invalid grade_mode value after constructor");
        assertEquals(TEST_SPECIAL_COURSE_FEE, obj.specialCourseFee,
                "Invalid special_course_fee value after constructor");
        assertEquals(TEST_ADDITIONAL_INFO, obj.additionalInfo, "Invalid additional_info value after constructor");
        assertEquals(TEST_GT_CODE, obj.gtCode, "Invalid gt_code value after constructor");
        assertEquals(TEST_MIN_CREDITS, obj.minCredits, "Invalid min_credits value after constructor");
        assertEquals(TEST_MAX_CREDITS, obj.maxCredits, "Invalid max_credits value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CatalogCourseRec obj = new CatalogCourseRec(TEST_COURSE_NUMBER, TEST_TITLE, TEST_DESCRIPTION,
                TEST_PREREQUISITE, TEST_REGISTRATION_INFO, TEST_RESTRICTION, TEST_TERMS_OFFERED, TEST_GRADE_MODE,
                TEST_SPECIAL_COURSE_FEE, TEST_ADDITIONAL_INFO, TEST_GT_CODE, TEST_MIN_CREDITS, TEST_MAX_CREDITS);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
