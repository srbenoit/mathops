package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardsCourseModuleRec} class.
 */
public final class TestStandardsCourseModuleRec {

    /** A field name. */
    private static final String TEST_COURSE_ID = "MATH 101";

    /** A field name. */
    private static final Integer TEST_MODULE_NBR = Integer.valueOf(8);

    /** A field name. */
    private static final Integer TEST_NBR_STANDARDS = Integer.valueOf(3);

    /** A field name. */
    private static final String TEST_MODULE_PATH = "01_f00/02_bar";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "course_id=MATH 101",
            "module_nbr=8",
            "nbr_standards=3",
            "module_path=01_f00/02_bar");

    /**
     * Constructs a new {@code TestStandardsCourseModuleRec}.
     */
    TestStandardsCourseModuleRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardsCourseModuleRec obj = new StandardsCourseModuleRec(TEST_COURSE_ID, TEST_MODULE_NBR,
                TEST_NBR_STANDARDS, TEST_MODULE_PATH);

        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course ID value after constructor");
        assertEquals(TEST_MODULE_NBR, obj.moduleNbr, "Invalid module number value after constructor");
        assertEquals(TEST_NBR_STANDARDS, obj.nbrStandards, "Invalid number of standards value after constructor");
        assertEquals(TEST_MODULE_PATH, obj.modulePath, "Invalid module path value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardsCourseModuleRec obj = new StandardsCourseModuleRec(TEST_COURSE_ID, TEST_MODULE_NBR,
                TEST_NBR_STANDARDS, TEST_MODULE_PATH);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}