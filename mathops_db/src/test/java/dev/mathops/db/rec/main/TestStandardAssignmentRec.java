package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardAssignmentRec} class.
 */
public final class TestStandardAssignmentRec {

    /** A field name. */
    private static final String TEST_ASSIGNMENT_ID = "HW1.2.3";

    /** A field name. */
    private static final String TEST_ASSIGNMENT_TYPE = "HW";

    /** A field name. */
    private static final String TEST_COURSE_ID = "MATH 101";

    /** A field name. */
    private static final Integer TEST_MODULE_NBR = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_STANDARD_NBR = Integer.valueOf(2);

    /** A field name. */
    private static final Integer TEST_PTS_POSSIBLE = Integer.valueOf(100);

    /** A field name. */
    private static final Integer TEST_MIN_PASSING_SCORE = Integer.valueOf(80);

    /** A field name. */
    private static final String TEST_TREE_REF = "a.b.c.d";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "assignment_id=HW1.2.3",
            "assignment_type=HW",
            "course_id=MATH 101",
            "module_nbr=5",
            "standard_nbr=2",
            "pts_possible=100",
            "min_passing_score=80",
            "tree_ref=a.b.c.d");

    /**
     * Constructs a new {@code TestStandardAssignmentRec}.
     */
    TestStandardAssignmentRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardAssignmentRec obj = new StandardAssignmentRec(TEST_ASSIGNMENT_ID, TEST_ASSIGNMENT_TYPE,
                TEST_COURSE_ID, TEST_MODULE_NBR, TEST_STANDARD_NBR, TEST_PTS_POSSIBLE, TEST_MIN_PASSING_SCORE,
                TEST_TREE_REF);

        assertEquals(TEST_ASSIGNMENT_ID, obj.assignmentId, "Invalid assignment ID value after constructor");
        assertEquals(TEST_ASSIGNMENT_TYPE, obj.assignmentType, "Invalid assignment type value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course ID value after constructor");
        assertEquals(TEST_MODULE_NBR, obj.moduleNbr, "Invalid module number value after constructor");
        assertEquals(TEST_STANDARD_NBR, obj.standardNbr, "Invalid standard number value after constructor");
        assertEquals(TEST_PTS_POSSIBLE, obj.ptsPossible, "Invalid points possible value after constructor");
        assertEquals(TEST_MIN_PASSING_SCORE, obj.minPassingScore, "Invalid min passing score value after constructor");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree ref value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardAssignmentRec obj = new StandardAssignmentRec(TEST_ASSIGNMENT_ID, TEST_ASSIGNMENT_TYPE,
                TEST_COURSE_ID, TEST_MODULE_NBR, TEST_STANDARD_NBR, TEST_PTS_POSSIBLE, TEST_MIN_PASSING_SCORE,
                TEST_TREE_REF);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}