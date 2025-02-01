package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawWhichDb} class.
 */
final class TestRawWhichDb {

    /** A field name. */
    private static final String TEST_DESCR = "TEST";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, "descr=TEST");

    /**
     * Constructs a new {@code IvtRawWhichDb}.
     */
    TestRawWhichDb() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawWhichDb obj = new RawWhichDb(TEST_DESCR);

        assertEquals(TEST_DESCR, obj.descr, "Invalid descr value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawWhichDb obj = new RawWhichDb(TEST_DESCR);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
