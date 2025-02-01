package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawMsg} class.
 */
final class TestRawMsg {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_TOUCH_POINT = "1RE";

    /** A field name. */
    private static final String TEST_MSG_CODE = "1RE_OK";

    /** A field name. */
    private static final String TEST_SUBJECT = "You are OK.";

    /** A field name. */
    private static final String TEST_TEMPLATE = "However, keep working!";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "touch_point=1RE",
            "msg_code=1RE_OK",
            "subject=You are OK.",
            "template=However, keep working!");

    /**
     * Constructs a new {@code IvtRawMsg}.
     */
    TestRawMsg() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawMsg obj = new RawMsg(TEST_TERM, TEST_TOUCH_POINT, TEST_MSG_CODE, TEST_SUBJECT, TEST_TEMPLATE);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_TOUCH_POINT, obj.touchPoint, "Invalid pace value after constructor");
        assertEquals(TEST_MSG_CODE, obj.msgCode, "Invalid pace_track value after constructor");
        assertEquals(TEST_SUBJECT, obj.subject, "Invalid ms_nbr value after constructor");
        assertEquals(TEST_TEMPLATE, obj.template, "Invalid ms_type value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawMsg obj = new RawMsg(TEST_TERM, TEST_TOUCH_POINT, TEST_MSG_CODE, TEST_SUBJECT, TEST_TEMPLATE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
