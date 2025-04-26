package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardsCourseStandardRec} class.
 */
public final class TestStandardsCourseStandardRec {

    /** A field name. */
    private static final String TEST_COURSE_ID = "MATH 101";

    /** A field name. */
    private static final Integer TEST_MODULE_NBR = Integer.valueOf(8);

    /** A field name. */
    private static final Integer TEST_STANDARD_NBR = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_LEARNING_OBJECTIVE = "I can fly!";

    /** A field name. */
    private static final String TEST_IS_ESSENTIAL = "Y";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "course_id=MATH 101",
            "module_nbr=8",
            "standard_nbr=2",
            "learning_objective=I can fly!",
            "is_essential=Y");

    /**
     * Constructs a new {@code TestStandardsCourseStandardRec}.
     */
    TestStandardsCourseStandardRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardsCourseStandardRec obj = new StandardsCourseStandardRec(TEST_COURSE_ID, TEST_MODULE_NBR,
                TEST_STANDARD_NBR, TEST_LEARNING_OBJECTIVE, TEST_IS_ESSENTIAL);

        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course ID value after constructor");
        assertEquals(TEST_MODULE_NBR, obj.moduleNbr, "Invalid module number value after constructor");
        assertEquals(TEST_STANDARD_NBR, obj.standardNbr, "Invalid standard number value after constructor");
        assertEquals(TEST_LEARNING_OBJECTIVE, obj.learningObjective,
                "Invalid learning objective value after constructor");
        assertEquals(TEST_IS_ESSENTIAL, obj.isEssential, "Invalid essential flag value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardsCourseStandardRec obj = new StandardsCourseStandardRec(TEST_COURSE_ID, TEST_MODULE_NBR,
                TEST_STANDARD_NBR, TEST_LEARNING_OBJECTIVE, TEST_IS_ESSENTIAL);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}