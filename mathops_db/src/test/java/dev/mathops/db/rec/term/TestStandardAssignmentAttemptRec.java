package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardAssignmentAttemptRec} class.
 */
public final class TestStandardAssignmentAttemptRec {

    /** A field name. */
    private static final Integer TEST_SERIAL_NBR = Integer.valueOf(12345);

    /** A field name. */
    private static final String TEST_STUDENT_ID = "876543210";

    /** A field name. */
    private static final String TEST_ASSIGNMENT_ID = "HW1.2.3";

    /** A field name. */
    private static final LocalDate TEST_ATTEMPT_DATE = LocalDate.of(2025, Month.APRIL, 10);

    /** A field name. */
    private static final Integer TEST_ATTEMPT_TIME_SEC = Integer.valueOf(86450);

    /** A field name. */
    private static final String TEST_COURSE_ID = "MATH 101";

    /** A field name. */
    private static final Integer TEST_MODULE_NBR = Integer.valueOf(6);

    /** A field name. */
    private static final Integer TEST_STANDARD_NBR = Integer.valueOf(2);

    /** A field name. */
    private static final Integer TEST_PTS_POSSIBLE = Integer.valueOf(100);

    /** A field name. */
    private static final Integer TEST_MIN_PASSING_SCORE = Integer.valueOf(80);

    /** A field name. */
    private static final Integer TEST_SCORE = Integer.valueOf(91);

    /** A field name. */
    private static final String TEST_PASSED = "Y";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=12345",
            "student_id=876543210",
            "assignment_id=HW1.2.3",
            "attempt_date=2025-04-10",
            "attempt_time_sec=86450",
            "course_id=MATH 101",
            "module_nbr=6",
            "standard_nbr=2",
            "pts_possible=100",
            "min_passing_score=80",
            "score=91",
            "passed=Y");

    /**
     * Constructs a new {@code TestStandardAssignmentAttemptRec}.
     */
    TestStandardAssignmentAttemptRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardAssignmentAttemptRec obj = new StandardAssignmentAttemptRec(TEST_SERIAL_NBR, TEST_STUDENT_ID,
                TEST_ASSIGNMENT_ID, TEST_ATTEMPT_DATE, TEST_ATTEMPT_TIME_SEC, TEST_COURSE_ID, TEST_MODULE_NBR,
                TEST_STANDARD_NBR, TEST_PTS_POSSIBLE, TEST_MIN_PASSING_SCORE, TEST_SCORE, TEST_PASSED);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial number value after constructor");
        assertEquals(TEST_STUDENT_ID, obj.studentId, "Invalid student ID value after constructor");
        assertEquals(TEST_ASSIGNMENT_ID, obj.assignmentId, "Invalid assignment ID value after constructor");
        assertEquals(TEST_ATTEMPT_DATE, obj.attemptDate, "Invalid attempt date value after constructor");
        assertEquals(TEST_ATTEMPT_TIME_SEC, obj.attemptTimeSec, "Invalid attempt time sec value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course ID value after constructor");
        assertEquals(TEST_MODULE_NBR, obj.moduleNbr, "Invalid module number value after constructor");
        assertEquals(TEST_STANDARD_NBR, obj.standardNbr, "Invalid standard number value after constructor");
        assertEquals(TEST_PTS_POSSIBLE, obj.ptsPossible, "Invalid points possible value after constructor");
        assertEquals(TEST_MIN_PASSING_SCORE, obj.minPassingScore, "Invalid min passing score value after constructor");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after constructor");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardAssignmentAttemptRec obj = new StandardAssignmentAttemptRec(TEST_SERIAL_NBR, TEST_STUDENT_ID,
                TEST_ASSIGNMENT_ID, TEST_ATTEMPT_DATE, TEST_ATTEMPT_TIME_SEC, TEST_COURSE_ID, TEST_MODULE_NBR,
                TEST_STANDARD_NBR, TEST_PTS_POSSIBLE, TEST_MIN_PASSING_SCORE, TEST_SCORE, TEST_PASSED);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}