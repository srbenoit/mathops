package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the {@code RawCourse} class.
 */
final class TestRawCourse {

    /** A test field value. */
    private static final String TEST_COURSE = "M 117";

    /** A test field value. */
    private static final Integer TEST_NBR_UNITS = Integer.valueOf(4);

    /** A test field value. */
    private static final String TEST_COURSE_NAME = "College Algebra I";

    /** A test field value. */
    private static final Integer TEST_NBR_CREDIT = Integer.valueOf(1);

    /** A test field value. */
    private static final String TEST_CALC_OK = "A";

    /** A test field value. */
    private static final String TEST_COURSE_LABEL = "MATH 117";

    /** A test field value. */
    private static final String TEST_INLINE_PREFIX = "The ";

    /** A test field value. */
    private static final String TEST_IS_TUTORIAL = "N";

    /** A test field value. */
    private static final String TEST_REQUIRE_ETEXT = "Y";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER1 = String.join(RecBase.DIVIDER,
            "course=M 117",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER2 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            "nbr_units=4",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER3 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "course_name=College Algebra I",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER4 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "nbr_credits=1",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER5 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "calc_ok=A",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER6 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "course_label=MATH 117",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER7 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "inline_prefix=The ",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER8 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "is_tutorial=N",
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER9 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "require_etext=Y");

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER10 = String.join(RecBase.DIVIDER,
            "course=M 117",
            "nbr_units=4",
            "course_name=College Algebra I",
            "nbr_credits=1",
            "calc_ok=A",
            "course_label=MATH 117",
            "inline_prefix=The ",
            "is_tutorial=N",
            "require_etext=Y");

    /**
     * Constructs a new {@code IvtRawCourse}.
     */
    TestRawCourse() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawCourse obj = new RawCourse(TEST_COURSE, TEST_NBR_UNITS, TEST_COURSE_NAME,
                TEST_NBR_CREDIT, TEST_CALC_OK, TEST_COURSE_LABEL, TEST_INLINE_PREFIX, TEST_IS_TUTORIAL,
                TEST_REQUIRE_ETEXT);

        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_NBR_UNITS, obj.nbrUnits, "Invalid nbr_units value after constructor");
        assertEquals(TEST_COURSE_NAME, obj.courseName, "Invalid course_name value after constructor");
        assertEquals(TEST_NBR_CREDIT, obj.nbrCredits, "Invalid nbr_credits value after constructor");
        assertEquals(TEST_CALC_OK, obj.calcOk, "Invalid calc_ok value after constructor");
        assertEquals(TEST_COURSE_LABEL, obj.courseLabel, "Invalid course_label value after constructor");
        assertEquals(TEST_INLINE_PREFIX, obj.inlinePrefix, "Invalid inline_prefix value after constructor");
        assertEquals(TEST_IS_TUTORIAL, obj.isTutorial, "Invalid is_tutorial value after constructor");
        assertEquals(TEST_REQUIRE_ETEXT, obj.requireEtext, "Invalid require_etext value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0002() {

        final RawCourse obj =
                new RawCourse(TEST_COURSE, null, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER1, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0003() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER1);

        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0004() {

        final RawCourse obj =
                new RawCourse(null, TEST_NBR_UNITS, null, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER2, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0005() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER2);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_NBR_UNITS, obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0006() {

        final RawCourse obj = new RawCourse(null, null, TEST_COURSE_NAME, null, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER3, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0007() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER3);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertEquals(TEST_COURSE_NAME, obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0008() {

        final RawCourse obj = new RawCourse(null, null, null, TEST_NBR_CREDIT, null, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER4, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0009() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER4);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertEquals(TEST_NBR_CREDIT, obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0010() {

        final RawCourse obj =
                new RawCourse(null, null, null, null, TEST_CALC_OK, null, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER5, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0011() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER5);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertEquals(TEST_CALC_OK, obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0012() {

        final RawCourse obj =
                new RawCourse(null, null, null, null, null, TEST_COURSE_LABEL, null, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER6, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0013() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER6);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertEquals(TEST_COURSE_LABEL, obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0014() {

        final RawCourse obj = new RawCourse(null, null, null, null, null, null, TEST_INLINE_PREFIX, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER7, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0015() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER7);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertEquals(TEST_INLINE_PREFIX, obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0016() {

        final RawCourse obj = new RawCourse(null, null, null, null, null, null, null, TEST_IS_TUTORIAL, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER8, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0017() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER8);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertEquals(TEST_IS_TUTORIAL, obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertNull(obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0018() {

        final RawCourse obj = new RawCourse(null, null, null, null, null, null, null, null, TEST_REQUIRE_ETEXT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER9, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0019() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER9);

        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertNull(obj.courseName, "Invalid course_name value after deserialization");
        assertNull(obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertNull(obj.calcOk, "Invalid calc_ok value after deserialization");
        assertNull(obj.courseLabel, "Invalid course_label value after deserialization");
        assertNull(obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertNull(obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertEquals(TEST_REQUIRE_ETEXT, obj.requireEtext, "Invalid require_etext value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0020() {

        final RawCourse obj = new RawCourse(TEST_COURSE, TEST_NBR_UNITS, TEST_COURSE_NAME,
                TEST_NBR_CREDIT, TEST_CALC_OK, TEST_COURSE_LABEL, TEST_INLINE_PREFIX, TEST_IS_TUTORIAL,
                TEST_REQUIRE_ETEXT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER10, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0021() {

        final RawCourse obj = RawCourse.parse(EXPECT_SER10);

        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_NBR_UNITS, obj.nbrUnits, "Invalid nbr_units value after deserialization");
        assertEquals(TEST_COURSE_NAME, obj.courseName, "Invalid course_name value after deserialization");
        assertEquals(TEST_NBR_CREDIT, obj.nbrCredits, "Invalid nbr_credits value after deserialization");
        assertEquals(TEST_CALC_OK, obj.calcOk, "Invalid calc_ok value after deserialization");
        assertEquals(TEST_COURSE_LABEL, obj.courseLabel, "Invalid course_label value after deserialization");
        assertEquals(TEST_INLINE_PREFIX, obj.inlinePrefix, "Invalid inline_prefix value after deserialization");
        assertEquals(TEST_IS_TUTORIAL, obj.isTutorial, "Invalid is_tutorial value after deserialization");
        assertEquals(TEST_REQUIRE_ETEXT, obj.requireEtext, "Invalid require_etext value after deserialization");
    }
}
