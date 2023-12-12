package dev.mathops.db.old.rec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StudentUnitMasteryRec} class.
 */
final class TestStudentUnitMasteryRec {

    /** A field name. */
    private static final String TEST_STU_ID = "888777666";

    /** A field name. */
    private static final String TEST_COURSE_ID = "M 125";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(2);

    /** A field name. */
    private static final Integer TEST_SCORE = Integer.valueOf(9);

    /** A field name. */
    private static final String TEST_SR_STATUS = "P";

    /** A field name. */
    private static final String TEST_S1_STATUS = "ML";

    /** A field name. */
    private static final String TEST_S2_STATUS = "M";

    /** A field name. */
    private static final String TEST_S3_STATUS = "A21";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888777666",
            "course_id=M 125",
            "unit=2",
            "score=9",
            "sr_status=P",
            "s1_status=ML",
            "s2_status=M",
            "s3_status=A21");

    /**
     * Constructs a new {@code IvtStudentUnitMasteryRec}.
     */
    TestStudentUnitMasteryRec() {

        // No action
    }

    /** A test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StudentUnitMasteryRec obj = new StudentUnitMasteryRec(TEST_STU_ID, TEST_COURSE_ID,
                TEST_UNIT, TEST_SCORE, TEST_SR_STATUS, TEST_S1_STATUS, TEST_S2_STATUS, TEST_S3_STATUS);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after constructor");
        assertEquals(TEST_SR_STATUS, obj.srStatus, "Invalid sr_status value after constructor");
        assertEquals(TEST_S1_STATUS, obj.s1Status, "Invalid s1_status value after constructor");
        assertEquals(TEST_S2_STATUS, obj.s2Status, "Invalid s2_status value after constructor");
        assertEquals(TEST_S3_STATUS, obj.s3Status, "Invalid s3_status value after constructor");
    }

    /** A test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StudentUnitMasteryRec obj = new StudentUnitMasteryRec(TEST_STU_ID, TEST_COURSE_ID,
                TEST_UNIT, TEST_SCORE, TEST_SR_STATUS, TEST_S1_STATUS, TEST_S2_STATUS, TEST_S3_STATUS);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** A test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final StudentUnitMasteryRec obj = StudentUnitMasteryRec.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_COURSE_ID, obj.courseId, "Invalid course_id value after deserialization");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after deserialization");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after deserialization");
        assertEquals(TEST_SR_STATUS, obj.srStatus, "Invalid sr_status value after deserialization");
        assertEquals(TEST_S1_STATUS, obj.s1Status, "Invalid s1_status value after deserialization");
        assertEquals(TEST_S2_STATUS, obj.s2Status, "Invalid s2_status value after deserialization");
        assertEquals(TEST_S3_STATUS, obj.s3Status, "Invalid s3_status value after deserialization");
    }
}
