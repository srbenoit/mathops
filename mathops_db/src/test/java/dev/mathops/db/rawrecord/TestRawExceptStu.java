package dev.mathops.db.rawrecord;

import dev.mathops.db.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawExceptStu} class.
 */
final class TestRawExceptStu {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_COURSE_ENROLL = "M 384";

    /** A field name. */
    private static final String TEST_HWORK_STATUS = "Y";

    /** A field name. */
    private static final String TEST_SECT = "001";

    /** A field name. */
    private static final String TEST_SECT_ENROLL = "1";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "stu_id=888888888",
            "course=M 117",
            "unit=2",
            "course_enroll=M 384",
            "hwork_status=Y",
            "sect=001",
            "sect_enroll=1");

    /**
     * Constructs a new {@code IvtRawExceptStu}.
     */
    TestRawExceptStu() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawExceptStu obj = new RawExceptStu(TEST_TERM, TEST_STU_ID, TEST_COURSE, TEST_UNIT,
                TEST_COURSE_ENROLL, TEST_HWORK_STATUS, TEST_SECT, TEST_SECT_ENROLL);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_COURSE_ENROLL, obj.courseEnroll, "Invalid course_enroll value after constructor");
        assertEquals(TEST_HWORK_STATUS, obj.hworkStatus, "Invalid hwork_status value after constructor");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after constructor");
        assertEquals(TEST_SECT_ENROLL, obj.sectEnroll, "Invalid sect_enroll value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawExceptStu obj = new RawExceptStu(TEST_TERM, TEST_STU_ID, TEST_COURSE, TEST_UNIT,
                TEST_COURSE_ENROLL, TEST_HWORK_STATUS, TEST_SECT, TEST_SECT_ENROLL);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawExceptStu obj = RawExceptStu.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after deserialization");
        assertEquals(TEST_COURSE_ENROLL, obj.courseEnroll, "Invalid course_enroll value after deserialization");
        assertEquals(TEST_HWORK_STATUS, obj.hworkStatus, "Invalid hwork_status value after deserialization");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after deserialization");
        assertEquals(TEST_SECT_ENROLL, obj.sectEnroll, "Invalid sect_enroll value after deserialization");
    }
}
