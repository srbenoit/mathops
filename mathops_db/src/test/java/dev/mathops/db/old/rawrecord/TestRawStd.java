package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawStd} class.
 */
final class TestRawStd {

    /** A field name. */
    private static final String TEST_STD_ID = "S1.2.3";

    /** A field name. */
    private static final Integer TEST_MASTERY_GROUPS = Integer.valueOf(17);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "std_id=S1.2.3",
            "mastery_groups=17");

    /**
     * Constructs a new {@code IvtRawStd}.
     */
    TestRawStd() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStd obj = new RawStd(TEST_STD_ID, TEST_MASTERY_GROUPS);

        assertEquals(TEST_STD_ID, obj.stdId, "Invalid std_id value after constructor");
        assertEquals(TEST_MASTERY_GROUPS, obj.masteryGroups, "Invalid mastery_groups value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStd obj = new RawStd(TEST_STD_ID, TEST_MASTERY_GROUPS);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStd obj = RawStd.parse(EXPECT_SER99);

        assertEquals(TEST_STD_ID, obj.stdId, "Invalid std_id value after deserialization");
        assertEquals(TEST_MASTERY_GROUPS, obj.masteryGroups, "Invalid mastery_groups value after deserialization");
    }
}
