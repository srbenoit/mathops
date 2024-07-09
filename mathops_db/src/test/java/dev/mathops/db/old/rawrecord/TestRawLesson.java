package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawLesson} class.
 */
final class TestRawLesson {

    /** A field name. */
    private static final String TEST_LESSON_ID = "L1.2.3";

    /** A field name. */
    private static final String TEST_LESSON_TYPE = "A";

    /** A field name. */
    private static final String TEST_DESCR = "Some Lesson";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "lesson_id=L1.2.3",
            "lesson_type=A",
            "descr=Some Lesson");

    /**
     * Constructs a new {@code IvtRawLesson}.
     */
    TestRawLesson() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawLesson obj = new RawLesson(TEST_LESSON_ID, TEST_LESSON_TYPE, TEST_DESCR);

        assertEquals(TEST_LESSON_ID, obj.lessonId, "Invalid lesson_id value after constructor");
        assertEquals(TEST_LESSON_TYPE, obj.lessonType, "Invalid lesson_type value after constructor");
        assertEquals(TEST_DESCR, obj.descr, "Invalid descr value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawLesson obj = new RawLesson(TEST_LESSON_ID, TEST_LESSON_TYPE, TEST_DESCR);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
