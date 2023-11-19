package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawMpe} class.
 */
final class TestRawMpe {

    /** A field name. */
    private static final String TEST_VERSION = "MPTTC";

    /** A field name. */
    private static final Integer TEST_MAX_ONLINE_ATMPTS = Integer.valueOf(1);

    /** A field name. */
    private static final Integer TEST_MAX_PROCTORED_ATMPTS = Integer.valueOf(2);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "version=MPTTC",
            "max_online_atmpts=1",
            "max_proctored_atmpts=2");

    /**
     * Constructs a new {@code IvtRawMpe}.
     */
    TestRawMpe() {

        // No action
    }


    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawMpe obj = new RawMpe(TEST_VERSION, TEST_MAX_ONLINE_ATMPTS, TEST_MAX_PROCTORED_ATMPTS);

        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_MAX_ONLINE_ATMPTS, obj.maxOnlineAtmpts, "Invalid max_online_atmpts value after constructor");
        assertEquals(TEST_MAX_PROCTORED_ATMPTS, obj.maxProctoredAtmpts,
                "Invalid max_proctored_atmpts value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawMpe obj = new RawMpe(TEST_VERSION, TEST_MAX_ONLINE_ATMPTS, TEST_MAX_PROCTORED_ATMPTS);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawMpe obj = RawMpe.parse(EXPECT_SER99);

        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_MAX_ONLINE_ATMPTS, obj.maxOnlineAtmpts,
                "Invalid max_online_atmpts value after deserialization");
        assertEquals(TEST_MAX_PROCTORED_ATMPTS, obj.maxProctoredAtmpts,
                "Invalid max_proctored_atmpts value after deserialization");
    }
}
