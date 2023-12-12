package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

/**
 * Tests for the {@code RawStlessonAssign} class.
 */
final class TestRawStlessonAssign {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE_ID = "M 117";

    /** A field name. */
    private static final String TEST_LESSON_ID = "L1.2,3";

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_SHOWN = LocalDateTime.of(2000, 1, 2, 3, 4, 5);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_OPEN = LocalDateTime.of(2001, 2, 3, 4, 5, 6);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_CLOSED = LocalDateTime.of(2002, 3, 4, 5, 6, 7);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_HIDDEN = LocalDateTime.of(2003, 4, 5, 6, 7, 8);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_STARTED = LocalDateTime.of(2004, 5, 6, 7, 8, 9);

    /** A field name. */
    private static final LocalDateTime TEST_WHEN_FINISHED = LocalDateTime.of(2005, 6, 7, 8, 9, 10);

    /** A field name. */
    private static final Integer TEST_SCORE_TENTHS = Integer.valueOf(31);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "course=M 117",
            "lesson_id=L1.2,3",
            "when_shown=2000-01-02T03:04:05",
            "when_open=2001-02-03T04:05:06",
            "when_closed=2002-03-04T05:06:07",
            "when_hidden=2003-04-05T06:07:08",
            "when_started=2004-05-06T07:08:09",
            "when_finished=2005-06-07T08:09:10",
            "score_tenths=31");

    /**
     * Constructs a new {@code IvtRawStlessonAssign}.
     */
    TestRawStlessonAssign() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStlessonAssign obj = new RawStlessonAssign(TEST_STU_ID, TEST_COURSE_ID,
                TEST_LESSON_ID, TEST_WHEN_SHOWN, TEST_WHEN_OPEN, TEST_WHEN_CLOSED, TEST_WHEN_HIDDEN,
                TEST_WHEN_STARTED, TEST_WHEN_FINISHED, TEST_SCORE_TENTHS);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_COURSE_ID, obj.course, "Invalid course_id value after constructor");
        assertEquals(TEST_LESSON_ID, obj.lessonId, "Invalid lesson_id value after constructor");
        assertEquals(TEST_WHEN_SHOWN, obj.whenShown, "Invalid when_shown value after constructor");
        assertEquals(TEST_WHEN_OPEN, obj.whenOpen, "Invalid when_open value after constructor");
        assertEquals(TEST_WHEN_CLOSED, obj.whenClosed, "Invalid when_closed value after constructor");
        assertEquals(TEST_WHEN_HIDDEN, obj.whenHidden, "Invalid when_hidden value after constructor");
        assertEquals(TEST_WHEN_STARTED, obj.whenStarted, "Invalid when_started value after constructor");
        assertEquals(TEST_WHEN_FINISHED, obj.whenFinished, "Invalid when_finished value after constructor");
        assertEquals(TEST_SCORE_TENTHS, obj.scoreTenths, "Invalid score_tenths value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStlessonAssign obj = new RawStlessonAssign(TEST_STU_ID, TEST_COURSE_ID,
                TEST_LESSON_ID, TEST_WHEN_SHOWN, TEST_WHEN_OPEN, TEST_WHEN_CLOSED, TEST_WHEN_HIDDEN,
                TEST_WHEN_STARTED, TEST_WHEN_FINISHED, TEST_SCORE_TENTHS);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStlessonAssign obj = RawStlessonAssign.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_COURSE_ID, obj.course, "Invalid course_id value after deserialization");
        assertEquals(TEST_LESSON_ID, obj.lessonId, "Invalid lesson_id value after deserialization");
        assertEquals(TEST_WHEN_SHOWN, obj.whenShown, "Invalid when_shown value after deserialization");
        assertEquals(TEST_WHEN_OPEN, obj.whenOpen, "Invalid when_open value after deserialization");
        assertEquals(TEST_WHEN_CLOSED, obj.whenClosed, "Invalid when_closed value after deserialization");
        assertEquals(TEST_WHEN_HIDDEN, obj.whenHidden, "Invalid when_hidden value after deserialization");
        assertEquals(TEST_WHEN_STARTED, obj.whenStarted, "Invalid when_started value after deserialization");
        assertEquals(TEST_WHEN_FINISHED, obj.whenFinished, "Invalid when_finished value after deserialization");
        assertEquals(TEST_SCORE_TENTHS, obj.scoreTenths, "Invalid score_tenths value after deserialization");
    }
}
