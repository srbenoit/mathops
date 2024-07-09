package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawStcunit} class.
 */
final class TestRawStcunit {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_REVIEW_STATUS = "Y";

    /** A field name. */
    private static final Integer TEST_REVIEW_SCORE = Integer.valueOf(10);

    /** A field name. */
    private static final Integer TEST_REVIEW_POINTS = Integer.valueOf(3);

    /** A field name. */
    private static final String TEST_PROCTORED_STATUS = "Z";

    /** A field name. */
    private static final Integer TEST_PROCTORED_SCORE = Integer.valueOf(9);

    /** A field name. */
    private static final Integer TEST_PROCTORED_POINTS = Integer.valueOf(8);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "course=M 117",
            "unit=4",
            "review_status=Y",
            "review_score=10",
            "review_points=3",
            "proctored_status=Z",
            "proctored_score=9",
            "proctored_points=8");

    /**
     * Constructs a new {@code IvtRawStcunit}.
     */
    TestRawStcunit() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStcunit obj = new RawStcunit(TEST_STU_ID, TEST_COURSE, TEST_UNIT,
                TEST_REVIEW_STATUS, TEST_REVIEW_SCORE, TEST_REVIEW_POINTS, TEST_PROCTORED_STATUS,
                TEST_PROCTORED_SCORE, TEST_PROCTORED_POINTS);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid version value after constructor");
        assertEquals(TEST_REVIEW_STATUS, obj.reviewStatus, "Invalid review_status value after constructor");
        assertEquals(TEST_REVIEW_SCORE, obj.reviewScore, "Invalid review_score value after constructor");
        assertEquals(TEST_REVIEW_POINTS, obj.reviewPoints, "Invalid review_points value after constructor");
        assertEquals(TEST_PROCTORED_STATUS, obj.proctoredStatus, "Invalid proctored_status value after constructor");
        assertEquals(TEST_PROCTORED_SCORE, obj.proctoredScore, "Invalid proctored_score value after constructor");
        assertEquals(TEST_PROCTORED_POINTS, obj.proctoredPoints, "Invalid proctored_points value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStcunit obj = new RawStcunit(TEST_STU_ID, TEST_COURSE, TEST_UNIT,
                TEST_REVIEW_STATUS, TEST_REVIEW_SCORE, TEST_REVIEW_POINTS, TEST_PROCTORED_STATUS,
                TEST_PROCTORED_SCORE, TEST_PROCTORED_POINTS);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
