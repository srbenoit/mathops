package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawCustd} class.
 */
final class TestRawCustd {

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_INDEX = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_STD_ID = "S1.2";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "course=M 117",
            "unit=4",
            "index=1",
            "std_id=S1.2");

    /**
     * Constructs a new {@code IvtRawCustd}.
     */
    TestRawCustd() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawCustd obj = new RawCustd(TEST_COURSE, TEST_UNIT, TEST_INDEX, TEST_STD_ID);

        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_INDEX, obj.index, "Invalid index value after constructor");
        assertEquals(TEST_STD_ID, obj.stdId, "Invalid std_id value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawCustd obj = new RawCustd(TEST_COURSE, TEST_UNIT, TEST_INDEX, TEST_STD_ID);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
