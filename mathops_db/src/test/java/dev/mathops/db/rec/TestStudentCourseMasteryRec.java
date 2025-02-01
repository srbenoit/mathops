package dev.mathops.db.rec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StudentCourseMasteryRec} class.
 */
final class TestStudentCourseMasteryRec {

    /** A field name. */
    private static final String TEST_STU_ID = "888777666";

    /** A field name. */
    private static final String TEST_COURSE_ID = "M 125";

    /** A field name. */
    private static final Integer TEST_SCORE = Integer.valueOf(80);

    /** A field name. */
    private static final Integer TEST_NBR_MASTERED_H1 = Integer.valueOf(10);

    /** A field name. */
    private static final Integer TEST_NBR_MASTERED_H2 = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_NBR_ELIGIBLE = Integer.valueOf(3);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888777666",
            "course_id=M 125",
            "score=80",
            "nbr_mastered_h1=10",
            "nbr_mastered_h2=5",
            "nbr_eligible=3");

    /**
     * Constructs a new {@code IvtStudentCourseMasteryRec}.
     */
    TestStudentCourseMasteryRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StudentCourseMasteryRec obj = new StudentCourseMasteryRec(TEST_STU_ID, TEST_COURSE_ID, TEST_SCORE,
                TEST_NBR_MASTERED_H1, TEST_NBR_MASTERED_H2, TEST_NBR_ELIGIBLE);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after constructor");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after constructor");
        assertEquals(TEST_NBR_MASTERED_H1, obj.nbrMasteredH1, "Invalid nbr_mastered_h1 value after constructor");
        assertEquals(TEST_NBR_MASTERED_H2, obj.nbrMasteredH2, "Invalid nbr_mastered_h2 value after constructor");
        assertEquals(TEST_NBR_ELIGIBLE, obj.nbrEligible, "Invalid nbr_eligible value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StudentCourseMasteryRec obj = new StudentCourseMasteryRec(TEST_STU_ID, TEST_COURSE_ID, TEST_SCORE,
                TEST_NBR_MASTERED_H1, TEST_NBR_MASTERED_H2, TEST_NBR_ELIGIBLE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
