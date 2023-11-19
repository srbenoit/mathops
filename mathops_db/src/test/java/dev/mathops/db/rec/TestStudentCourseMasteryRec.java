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

    /** A field name. */
    private static final String TEST_EXPLOR_1_STATUS = "M";

    /** A field name. */
    private static final String TEST_EXPLOR_2_STATUS = "AL";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888777666",
            "course_id=M 125",
            "score=80",
            "nbr_mastered_h1=10",
            "nbr_mastered_h2=5",
            "nbr_eligible=3",
            "explor_1_status=M",
            "explor_2_status=AL");

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
                TEST_NBR_MASTERED_H1, TEST_NBR_MASTERED_H2, TEST_NBR_ELIGIBLE, TEST_EXPLOR_1_STATUS,
                TEST_EXPLOR_2_STATUS);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after constructor");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after constructor");
        assertEquals(TEST_NBR_MASTERED_H1, obj.nbrMasteredH1, "Invalid nbr_mastered_h1 value after constructor");
        assertEquals(TEST_NBR_MASTERED_H2, obj.nbrMasteredH2, "Invalid nbr_mastered_h2 value after constructor");
        assertEquals(TEST_NBR_ELIGIBLE, obj.nbrEligible, "Invalid nbr_eligible value after constructor");
        assertEquals(TEST_EXPLOR_1_STATUS, obj.explor1Status, "Invalid explor_1_status value after constructor");
        assertEquals(TEST_EXPLOR_2_STATUS, obj.explor2Status, "Invalid explor_2_status value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StudentCourseMasteryRec obj = new StudentCourseMasteryRec(TEST_STU_ID, TEST_COURSE_ID, TEST_SCORE,
                TEST_NBR_MASTERED_H1, TEST_NBR_MASTERED_H2, TEST_NBR_ELIGIBLE, TEST_EXPLOR_1_STATUS,
                TEST_EXPLOR_2_STATUS);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final StudentCourseMasteryRec obj = StudentCourseMasteryRec.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after deserialization");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after deserialization");
        assertEquals(TEST_NBR_MASTERED_H1, obj.nbrMasteredH1, "Invalid nbr_mastered_h1 value after deserialization");
        assertEquals(TEST_NBR_MASTERED_H2, obj.nbrMasteredH2, "Invalid nbr_mastered_h2 value after deserialization");
        assertEquals(TEST_NBR_ELIGIBLE, obj.nbrEligible, "Invalid nbr_eligible value after deserialization");
        assertEquals(TEST_EXPLOR_1_STATUS, obj.explor1Status, "Invalid explor_1_status value after deserialization");
        assertEquals(TEST_EXPLOR_2_STATUS, obj.explor2Status, "Invalid explor_2_status value after deserialization");
    }
}
