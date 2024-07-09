package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawItem} class.
 */
final class TestRawItem {

    /** A field name. */
    private static final String TEST_ITEM_ID = "ABC";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, "item_id=ABC");

    /**
     * Constructs a new {@code IvtRawItem}.
     */
    TestRawItem() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawItem obj = new RawItem(TEST_ITEM_ID);

        assertEquals(TEST_ITEM_ID, obj.itemId, "Invalid item_id value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawItem obj = new RawItem(TEST_ITEM_ID);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
