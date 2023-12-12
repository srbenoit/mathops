package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawResource} class.
 */
final class TestRawResource {

    /** A field name. */
    private static final String TEST_RESOURCE_ID = "C99";

    /** A field name. */
    private static final String TEST_RESOURCE_TYPE = "CA";

    /** A field name. */
    private static final String TEST_RESOURCE_DESC = "Calculator";

    /** A field name. */
    private static final Integer TEST_DAYS_ALLOWED = Integer.valueOf(2);

    /** A field name. */
    private static final Integer TEST_HOLDS_ALLOWED = Integer.valueOf(3);

    /** A field name. */
    private static final String TEST_HOLD_ID = "45";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "resource_id=C99",
            "resource_type=CA",
            "resource_desc=Calculator",
            "days_allowed=2",
            "holds_allowed=3",
            "hold_id=45");

    /**
     * Constructs a new {@code IvtRawResource}.
     */
    TestRawResource() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawResource obj = new RawResource(TEST_RESOURCE_ID, TEST_RESOURCE_TYPE,
                TEST_RESOURCE_DESC, TEST_DAYS_ALLOWED, TEST_HOLDS_ALLOWED, TEST_HOLD_ID);

        assertEquals(TEST_RESOURCE_ID, obj.resourceId, "Invalid term value after constructor");
        assertEquals(TEST_RESOURCE_TYPE, obj.resourceType, "Invalid apln_term value after constructor");
        assertEquals(TEST_RESOURCE_DESC, obj.resourceDesc, "Invalid course value after constructor");
        assertEquals(TEST_DAYS_ALLOWED, obj.daysAllowed, "Invalid start_dt value after constructor");
        assertEquals(TEST_HOLDS_ALLOWED, obj.holdsAllowed, "Invalid end_dt value after constructor");
        assertEquals(TEST_HOLD_ID, obj.holdId, "Invalid end_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawResource obj = new RawResource(TEST_RESOURCE_ID, TEST_RESOURCE_TYPE,
                TEST_RESOURCE_DESC, TEST_DAYS_ALLOWED, TEST_HOLDS_ALLOWED, TEST_HOLD_ID);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawResource obj = RawResource.parse(EXPECT_SER99);

        assertEquals(TEST_RESOURCE_ID, obj.resourceId, "Invalid term value after deserialization");
        assertEquals(TEST_RESOURCE_TYPE, obj.resourceType, "Invalid apln_term value after deserialization");
        assertEquals(TEST_RESOURCE_DESC, obj.resourceDesc, "Invalid course value after deserialization");
        assertEquals(TEST_DAYS_ALLOWED, obj.daysAllowed, "Invalid start_dt value after deserialization");
        assertEquals(TEST_HOLDS_ALLOWED, obj.holdsAllowed, "Invalid end_dt value after deserialization");
        assertEquals(TEST_HOLD_ID, obj.holdId, "Invalid end_dt value after deserialization");
    }
}
