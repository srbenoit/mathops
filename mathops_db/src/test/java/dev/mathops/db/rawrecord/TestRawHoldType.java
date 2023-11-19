package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawHoldType} class.
 */
final class TestRawHoldType {

    /** A field name. */
    private static final String TEST_HOLD_ID = "10";

    /** A field name. */
    private static final String TEST_SEV_ADMIN_HOLD = "F";

    /** A field name. */
    private static final String TEST_HOLD_TYPE = "Reg";

    /** A field name. */
    private static final String TEST_ADD_HOLD = "Y";

    /** A field name. */
    private static final String TEST_DELETE_HOLD = "N";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "hold_id=10",
            "sev_admin_hold=F",
            "hold_type=Reg",
            "add_hold=Y",
            "delete_hold=N");

    /**
     * Constructs a new {@code IvtRawHoldType}.
     */
    TestRawHoldType() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawHoldType obj = new RawHoldType(TEST_HOLD_ID, TEST_SEV_ADMIN_HOLD, TEST_HOLD_TYPE,
                TEST_ADD_HOLD, TEST_DELETE_HOLD);

        assertEquals(TEST_HOLD_ID, obj.holdId, "Invalid hold_id value after constructor");
        assertEquals(TEST_SEV_ADMIN_HOLD, obj.sevAdminHold, "Invalid sev_admin_hold value after constructor");
        assertEquals(TEST_HOLD_TYPE, obj.holdType, "Invalid hold_type value after constructor");
        assertEquals(TEST_ADD_HOLD, obj.addHold, "Invalid add_hold value after constructor");
        assertEquals(TEST_DELETE_HOLD, obj.deleteHold, "Invalid delete_hold value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawHoldType obj = new RawHoldType(TEST_HOLD_ID, TEST_SEV_ADMIN_HOLD, TEST_HOLD_TYPE,
                TEST_ADD_HOLD, TEST_DELETE_HOLD);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawHoldType obj = RawHoldType.parse(EXPECT_SER99);

        assertEquals(TEST_HOLD_ID, obj.holdId, "Invalid hold_id value after deserialization");
        assertEquals(TEST_SEV_ADMIN_HOLD, obj.sevAdminHold, "Invalid sev_admin_hold value after deserialization");
        assertEquals(TEST_HOLD_TYPE, obj.holdType, "Invalid hold_type value after deserialization");
        assertEquals(TEST_ADD_HOLD, obj.addHold, "Invalid add_hold value after deserialization");
        assertEquals(TEST_DELETE_HOLD, obj.deleteHold, "Invalid delete_hold value after deserialization");
    }
}
