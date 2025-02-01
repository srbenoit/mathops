package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

/**
 * Tests for the {@code RawEtextKey} class.
 */
final class TestRawEtextKey {

    /** A field name. */
    private static final String TEST_ETEXT_ID = "PACe";

    /** A field name. */
    private static final String TEST_ETEXT_KEY = "123ABC";

    /** A field name. */
    private static final LocalDateTime TEST_ACTIVE_DT = LocalDateTime.of(2023, 1, 2, 3, 4, 5);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "etext_id=PACe",
            "etext_key=123ABC",
            "active_dt=2023-01-02T03:04:05");

    /**
     * Constructs a new {@code IvtRawEtextKey}.
     */
    TestRawEtextKey() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawEtextKey obj = new RawEtextKey(TEST_ETEXT_ID, TEST_ETEXT_KEY, TEST_ACTIVE_DT);

        assertEquals(TEST_ETEXT_ID, obj.etextId, "Invalid etext_id value after constructor");
        assertEquals(TEST_ETEXT_KEY, obj.etextKey, "Invalid etext_key value after constructor");
        assertEquals(TEST_ACTIVE_DT, obj.activeDt, "Invalid active_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawEtextKey obj = new RawEtextKey(TEST_ETEXT_ID, TEST_ETEXT_KEY, TEST_ACTIVE_DT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
