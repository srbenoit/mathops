package dev.mathops.db.rec;

import dev.mathops.db.enums.ECatalogCoursePrereqType;
import dev.mathops.db.type.CatalogCourseNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code CatalogCoursePrereq} class.
 */
final class TestCatalogCoursePrereqRec {

    /** A test field value. */
    private static final String TEST_PREREQ_ID = "MATH_125_1";

    /** A test field value. */
    private static final String TEST_PARENT_PREREQ_ID = "MATH_125_1.1";

    /** A test field value. */
    private static final ECatalogCoursePrereqType TEST_PREREQ_TYPE = ECatalogCoursePrereqType.SIMPLE;

    /** A test field value. */
    private static final CatalogCourseNumber TEST_COURSE_ID = new CatalogCourseNumber("MATH", "118");

    /** A test field value. */
    private static final String TEST_MIN_GRADE = "C-";

    /** A test field value. */
    private static final String TEST_CONCURRENT = "Y";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "prereq_id=MATH_125_1",
            "parent_prereq_id=MATH_125_1.1",
            "prereq_type=S",
            "course_id=MATH 118",
            "min_grade=C-",
            "concurrent=Y");

    /**
     * Constructs a new {@code TestCatalogCoursePrereqRec}.
     */
    TestCatalogCoursePrereqRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final CatalogCoursePrereqRec obj =
                new CatalogCoursePrereqRec(TEST_PREREQ_ID, TEST_PARENT_PREREQ_ID, TEST_PREREQ_TYPE, TEST_COURSE_ID,
                        TEST_MIN_GRADE, TEST_CONCURRENT);

        assertEquals(TEST_PREREQ_ID, obj.prereqId, "Invalid prereq_id value after constructor");
        assertEquals(TEST_PARENT_PREREQ_ID, obj.parentPrereqId, "Invalid parent_prereq_id value after constructor");
        assertEquals(TEST_PREREQ_TYPE, obj.prereqType, "Invalid prereq_type value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after constructor");
        assertEquals(TEST_MIN_GRADE, obj.minGrade, "Invalid min_grade value after constructor");
        assertEquals(TEST_CONCURRENT, obj.concurrent, "Invalid concurrent value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final CatalogCoursePrereqRec obj = new CatalogCoursePrereqRec(TEST_PREREQ_ID, TEST_PARENT_PREREQ_ID,
                TEST_PREREQ_TYPE, TEST_COURSE_ID,
                TEST_MIN_GRADE, TEST_CONCURRENT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
