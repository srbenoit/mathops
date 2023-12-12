package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawEText} class.
 */
final class TestRawEtext {

    /** A field name. */
    private static final String TEST_ETEXT_ID = "PACe";

    /** A field name. */
    private static final String TEST_RETENTION = "C";

    /** A field name. */
    private static final String TEST_PURCHASE_URL = "http://example.com/purchase";

    /** A field name. */
    private static final Integer TEST_REFUND_PERIOD = Integer.valueOf(100);

    /** A field name. */
    private static final String TEST_KEY_ENTRY = "Y";

    /** A field name. */
    private static final String TEST_ACTIVE = "N";

    /** A field name. */
    private static final String TEST_BUTTON_LABEL = "Old E-text";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "etext_id=PACe",
            "retention=C",
            "purchase_url=http://example.com/purchase",
            "refund_period=100",
            "key_entry=Y",
            "active=N",
            "button_label=Old E-text");

    /**
     * Constructs a new {@code IvtRawEText}.
     */
    TestRawEtext() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawEtext obj = new RawEtext(TEST_ETEXT_ID, TEST_RETENTION, TEST_PURCHASE_URL,
                TEST_REFUND_PERIOD, TEST_KEY_ENTRY, TEST_ACTIVE, TEST_BUTTON_LABEL);

        assertEquals(TEST_ETEXT_ID, obj.etextId, "Invalid etext_id value after constructor");
        assertEquals(TEST_RETENTION, obj.retention, "Invalid retention value after constructor");
        assertEquals(TEST_PURCHASE_URL, obj.purchaseUrl, "Invalid purchase_url value after constructor");
        assertEquals(TEST_REFUND_PERIOD, obj.refundPeriod, "Invalid refund_period value after constructor");
        assertEquals(TEST_KEY_ENTRY, obj.keyEntry, "Invalid key_entry value after constructor");
        assertEquals(TEST_ACTIVE, obj.active, "Invalid active value after constructor");
        assertEquals(TEST_BUTTON_LABEL, obj.buttonLabel, "Invalid button_label value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawEtext obj = new RawEtext(TEST_ETEXT_ID, TEST_RETENTION, TEST_PURCHASE_URL,
                TEST_REFUND_PERIOD, TEST_KEY_ENTRY, TEST_ACTIVE, TEST_BUTTON_LABEL);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawEtext obj = RawEtext.parse(EXPECT_SER99);

        assertEquals(TEST_ETEXT_ID, obj.etextId, "Invalid etext_id value after deserialization");
        assertEquals(TEST_RETENTION, obj.retention, "Invalid retention value after deserialization");
        assertEquals(TEST_PURCHASE_URL, obj.purchaseUrl, "Invalid purchase_url value after deserialization");
        assertEquals(TEST_REFUND_PERIOD, obj.refundPeriod, "Invalid refund_period value after deserialization");
        assertEquals(TEST_KEY_ENTRY, obj.keyEntry, "Invalid key_entry value after deserialization");
        assertEquals(TEST_ACTIVE, obj.active, "Invalid active value after deserialization");
        assertEquals(TEST_BUTTON_LABEL, obj.buttonLabel, "Invalid button_label value after deserialization");
    }
}
