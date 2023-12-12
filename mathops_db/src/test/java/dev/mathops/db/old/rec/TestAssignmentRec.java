package dev.mathops.db.old.rec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

/**
 * Tests for the {@code AssignmentRec} class.
 */
final class TestAssignmentRec {

    /** A field name. */
    private static final String TEST_ASSIGNMENT_ID = "7H412";

    /** A field name. */
    private static final String TEST_ASSIGNMENT_TYPE = "HW";

    /** A field name. */
    private static final String TEST_COURSE_ID = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_OBJECTIVE = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_TREE_REF = "A.B.C.D";

    /** A field name. */
    private static final String TEST_TITLE = "Homework 4.1";

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_ACTIVE = LocalDateTime.of(2022, 1, 2, 3, 4, 5);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_PULLED = LocalDateTime.of(2023, 2, 3, 4, 5, 6);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "assignment_id=7H412",
            "assignment_type=HW",
            "course_id=M 117",
            "unit=4",
            "objective=1",
            "tree_ref=A.B.C.D",
            "title=Homework 4.1",
            "when_active=2022-01-02T03:04:05",
            "when_pulled=2023-02-03T04:05:06");

    /**
     * Constructs a new {@code IvtAssignmentRec}.
     */
    TestAssignmentRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final AssignmentRec obj =
                new AssignmentRec(TEST_ASSIGNMENT_ID, TEST_ASSIGNMENT_TYPE, TEST_COURSE_ID, TEST_UNIT,
                        TEST_OBJECTIVE, TEST_TREE_REF, TEST_TITLE, TEST_WHEN_ACTIVE, TEST_WHEN_PULLED);

        assertEquals(TEST_ASSIGNMENT_ID, obj.assignmentId, "Invalid assignment_id value after constructor");
        assertEquals(TEST_ASSIGNMENT_TYPE, obj.assignmentType, "Invalid assignment_type value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after constructor");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after constructor");
        assertEquals(TEST_TITLE, obj.title, "Invalid title value after constructor");
        assertEquals(TEST_WHEN_ACTIVE, obj.whenActive, "Invalid when_active value after constructor");
        assertEquals(TEST_WHEN_PULLED, obj.whenPulled, "Invalid when_pulled value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final AssignmentRec obj = new AssignmentRec(TEST_ASSIGNMENT_ID, TEST_ASSIGNMENT_TYPE, TEST_COURSE_ID,
                TEST_UNIT, TEST_OBJECTIVE, TEST_TREE_REF, TEST_TITLE, TEST_WHEN_ACTIVE, TEST_WHEN_PULLED);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final AssignmentRec obj = AssignmentRec.parse(EXPECT_SER99);

        assertEquals(TEST_ASSIGNMENT_ID, obj.assignmentId, "Invalid version value after deserialization");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course value after deserialization");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after deserialization");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after deserialization");
        assertEquals(TEST_TITLE, obj.title, "Invalid title value after deserialization");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after deserialization");
        assertEquals(TEST_ASSIGNMENT_TYPE, obj.assignmentType, "Invalid hw_type value after deserialization");
        assertEquals(TEST_WHEN_ACTIVE, obj.whenActive, "Invalid active_dt value after deserialization");
        assertEquals(TEST_WHEN_PULLED, obj.whenPulled, "Invalid pull_dt value after deserialization");
    }
}
