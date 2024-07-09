package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawAdminHold} class.
 */
final class TestRawAdminHold {

    /** A test field value. */
    private static final String TEST_STU_ID = "81818181";

    /** A test field value. */
    private static final String TEST_HOLD_ID = "45";

    /** A test field value. */
    private static final String TEST_SEV = "F";

    /** A test field value. */
    private static final Integer TEST_TIMES = Integer.valueOf(10);

    /** A test field value. */
    private static final LocalDate TEST_DATE = LocalDate.of(2023, 2, 3);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER1 = String.join(RecBase.DIVIDER,
            "stu_id=81818181",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER2 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            "hold_id=45",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER3 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "sev_admin_hold=F",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER4 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "times_display=10",
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER5 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "create_dt=2023-02-03");

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER6 = String.join(RecBase.DIVIDER,
            "stu_id=81818181",
            "hold_id=45",
            "sev_admin_hold=F",
            "times_display=10",
            "create_dt=2023-02-03");

    /**
     * Constructs a new {@code IvtRawAdminHold}.
     */
    TestRawAdminHold() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawAdminHold obj =
                new RawAdminHold(TEST_STU_ID, TEST_HOLD_ID, TEST_SEV, TEST_TIMES, TEST_DATE);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_HOLD_ID, obj.holdId, "Invalid hold_id value after constructor");
        assertEquals(TEST_SEV, obj.sevAdminHold, "Invalid sev_admin_hold value after constructor");
        assertEquals(TEST_TIMES, obj.timesDisplay, "Invalid times_display value after constructor");
        assertEquals(TEST_DATE, obj.createDt, "Invalid create_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0002() {

        final RawAdminHold obj = new RawAdminHold(TEST_STU_ID, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER1, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0004() {

        final RawAdminHold obj = new RawAdminHold(null, TEST_HOLD_ID, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER2, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0006() {

        final RawAdminHold obj = new RawAdminHold(null, null, TEST_SEV, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER3, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0008() {

        final RawAdminHold obj = new RawAdminHold(null, null, null, TEST_TIMES, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER4, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0010() {

        final RawAdminHold obj = new RawAdminHold(null, null, null, null, TEST_DATE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER5, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0012() {

        final RawAdminHold obj =
                new RawAdminHold(TEST_STU_ID, TEST_HOLD_ID, TEST_SEV, TEST_TIMES, TEST_DATE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER6, ser, "Invalid serialized string");
    }
}
