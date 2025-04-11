package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StudentCourseMasteryRec} class.
 */
public final class TestStudentCourseMasteryRec {

    /** A field name. */
    private static final String TEST_STUDENT_ID = "876543210";

    /** A field name. */
    private static final String TEST_COURSE_ID = "MATH 101";

    /** A field name. */
    private static final String TEST_COURSE_STRUCTURE = "aaAbbBccCddDeeEffFggGhhH";

    /** A field name. */
    private static final String TEST_HOMEWORK_STATUS = "YYYYYNNNN---------------";

    /** A field name. */
    private static final String TEST_MASTERY_STATUS = "YYyNN-------------------";

    /** A field name. */
    private static final String TEST_COMPLETED = "N";

    /** A field name. */
    private static final Integer TEST_SCORE = Integer.valueOf(14);

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "student_id=876543210",
            "course_id=MATH 101",
            "course_structure=aaAbbBccCddDeeEffFggGhhH",
            "homework_status=YYYYYNNNN---------------",
            "mastery_status=YYyNN-------------------",
            "completed=N",
            "score=14");

    /**
     * Constructs a new {@code TestStudentCourseMasteryRec}.
     */
    TestStudentCourseMasteryRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StudentCourseMasteryRec obj = new StudentCourseMasteryRec(TEST_STUDENT_ID, TEST_COURSE_ID,
                TEST_COURSE_STRUCTURE, TEST_HOMEWORK_STATUS, TEST_MASTERY_STATUS, TEST_COMPLETED, TEST_SCORE);

        assertEquals(TEST_STUDENT_ID, obj.studentId, "Invalid student ID value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course ID value after constructor");
        assertEquals(TEST_COURSE_STRUCTURE, obj.courseStructure, "Invalid course structure value after constructor");
        assertEquals(TEST_HOMEWORK_STATUS, obj.homeworkStatus, "Invalid homework status value after constructor");
        assertEquals(TEST_MASTERY_STATUS, obj.masteryStatus, "Invalid mastery status value after constructor");
        assertEquals(TEST_COMPLETED, obj.completed, "Invalid completed value after constructor");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StudentCourseMasteryRec obj = new StudentCourseMasteryRec(TEST_STUDENT_ID, TEST_COURSE_ID,
                TEST_COURSE_STRUCTURE, TEST_HOMEWORK_STATUS, TEST_MASTERY_STATUS, TEST_COMPLETED, TEST_SCORE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}