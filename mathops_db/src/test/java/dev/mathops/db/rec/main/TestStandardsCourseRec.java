package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardsCourseRec} class.
 */
public final class TestStandardsCourseRec {

    /** A field name. */
    private static final String TEST_COURSE_ID = "MATH 101";

    /** A field name. */
    private static final String TEST_COURSE_TITLE = "Math in the Social Sciences";

    /** A field name. */
    private static final Integer TEST_NBR_MODULES = Integer.valueOf(8);

    /** A field name. */
    private static final Integer TEST_NBR_CREDITS = Integer.valueOf(12);

    /** A field name. */
    private static final Integer TEST_ALLOW_LEND = Integer.valueOf(5);

    /** A field name. */
    private static final String TEST_METADATA_PATH = "01_f00/02_bar";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "course_id=MATH 101",
            "course_title=Math in the Social Sciences",
            "nbr_modules=8",
            "nbr_credits=12",
            "allow_lend=5",
            "metadata_path=01_f00/02_bar");

    /**
     * Constructs a new {@code TestStandardsCourseRec}.
     */
    TestStandardsCourseRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardsCourseRec obj = new StandardsCourseRec(TEST_COURSE_ID, TEST_COURSE_TITLE, TEST_NBR_MODULES,
                TEST_NBR_CREDITS, TEST_ALLOW_LEND, TEST_METADATA_PATH);

        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course ID value after constructor");
        assertEquals(TEST_COURSE_TITLE, obj.courseTitle, "Invalid course title value after constructor");
        assertEquals(TEST_NBR_MODULES, obj.nbrModules, "Invalid number of modules value after constructor");
        assertEquals(TEST_NBR_CREDITS, obj.nbrCredits, "Invalid number of credits value after constructor");
        assertEquals(TEST_ALLOW_LEND, obj.allowLend, "Invalid allowed lends value after constructor");
        assertEquals(TEST_METADATA_PATH, obj.metadataPath, "Invalid metadata path value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardsCourseRec obj = new StandardsCourseRec(TEST_COURSE_ID, TEST_COURSE_TITLE, TEST_NBR_MODULES,
                TEST_NBR_CREDITS, TEST_ALLOW_LEND, TEST_METADATA_PATH);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}