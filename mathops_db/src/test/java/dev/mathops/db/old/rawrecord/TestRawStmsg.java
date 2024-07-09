package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStmsg} class.
 */
final class TestRawStmsg {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final LocalDate TEST_MSG_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_COURSE_INDEX = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_TOUCH_POINT = "FE";

    /** A field name. */
    private static final String TEST_MSG_CODE = "F1.2";

    /** A field name. */
    private static final String TEST_SENDER = "Steve Benoit";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "msg_dt=2023-01-02",
            "pace=5",
            "course_index=4",
            "touch_point=FE",
            "msg_code=F1.2",
            "sender=Steve Benoit");

    /**
     * Constructs a new {@code IvtRawStmsg}.
     */
    TestRawStmsg() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStmsg obj = new RawStmsg(TEST_STU_ID, TEST_MSG_DT, TEST_PACE, TEST_COURSE_INDEX,
                TEST_TOUCH_POINT, TEST_MSG_CODE, TEST_SENDER);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_MSG_DT, obj.msgDt, "Invalid msg_dt value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after constructor");
        assertEquals(TEST_COURSE_INDEX, obj.courseIndex, "Invalid course_index value after constructor");
        assertEquals(TEST_TOUCH_POINT, obj.touchPoint, "Invalid touch_point value after constructor");
        assertEquals(TEST_MSG_CODE, obj.msgCode, "Invalid msg_code value after constructor");
        assertEquals(TEST_SENDER, obj.sender, "Invalid sender value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStmsg obj = new RawStmsg(TEST_STU_ID, TEST_MSG_DT, TEST_PACE, TEST_COURSE_INDEX,
                TEST_TOUCH_POINT, TEST_MSG_CODE, TEST_SENDER);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
