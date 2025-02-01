package dev.mathops.db.rec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code MasteryExamRec} class.
 */
final class TestMasteryExamRec {

    /** A field name. */
    private static final String TEST_EXAM_ID = "ST41_2_M";

    /** A field name. */
    private static final String TEST_EXAM_TYPE = "ST";

    /** A field name. */
    private static final String TEST_COURSE_ID = "M 125";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(1);

    /** A field name. */
    private static final Integer TEST_OBJECTIVE = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_TREE_REF = "A.B.C.D";

    /** A field name. */
    private static final String TEST_TITLE = "Standard 41.2 Mastery";

    /** A field name. */
    private static final String TEST_BUTTON_LABEL = "Learning Target 1.2";

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_ACTIVE = LocalDateTime.of(2022, 1, 2, 3, 4, 5);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_PULLED = LocalDateTime.of(2023, 2, 3, 4, 5, 6);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "exam_id=ST41_2_M",
            "exam_type=ST",
            "course_id=M 125",
            "unit=1",
            "objective=2",
            "tree_ref=A.B.C.D",
            "title=Standard 41.2 Mastery",
            "button_label=Learning Target 1.2",
            "when_active=2022-01-02T03:04:05",
            "when_pulled=2023-02-03T04:05:06");

    /**
     * Constructs a new {@code TestMasteryExamRec}.
     */
    TestMasteryExamRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final MasteryExamRec obj = new MasteryExamRec(TEST_EXAM_ID, TEST_EXAM_TYPE, TEST_COURSE_ID,
                TEST_UNIT, TEST_OBJECTIVE, TEST_TREE_REF, TEST_TITLE, TEST_BUTTON_LABEL,
                TEST_WHEN_ACTIVE, TEST_WHEN_PULLED);

        assertEquals(TEST_EXAM_ID, obj.examId, "Invalid exam_id value after constructor");
        assertEquals(TEST_EXAM_TYPE, obj.examType, "Invalid exam_type value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after constructor");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after constructor");
        assertEquals(TEST_TITLE, obj.title, "Invalid title value after constructor");
        assertEquals(TEST_BUTTON_LABEL, obj.buttonLabel, "Invalid button_label value after constructor");
        assertEquals(TEST_WHEN_ACTIVE, obj.whenActive, "Invalid when_active value after constructor");
        assertEquals(TEST_WHEN_PULLED, obj.whenPulled, "Invalid when_pulled value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final MasteryExamRec obj = new MasteryExamRec(TEST_EXAM_ID, TEST_EXAM_TYPE, TEST_COURSE_ID, TEST_UNIT,
                TEST_OBJECTIVE, TEST_TREE_REF, TEST_TITLE, TEST_BUTTON_LABEL, TEST_WHEN_ACTIVE, TEST_WHEN_PULLED);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
