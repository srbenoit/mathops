package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStetext} class.
 */
final class TestRawStetext {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_ETEXT_ID = "PACe";

    /** A field name. */
    private static final LocalDate TEST_ACTIVE_DT = LocalDate.of(2020, 1, 2);

    /** A field name. */
    private static final String TEST_ETEXT_KEY = "ABC123456";

    /** A field name. */
    private static final LocalDate TEST_EXPIRATION_DT = LocalDate.of(2021, 3, 4);

    /** A field name. */
    private static final LocalDate TEST_REFUND_DEADLINE_DT = LocalDate.of(2022, 5, 6);

    /** A field name. */
    private static final LocalDate TEST_REFUND_DT = LocalDate.of(2023, 7, 8);

    /** A field name. */
    private static final String TEST_REFUND_REASON = "Not good enough.";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "etext_id=PACe",
            "active_dt=2020-01-02",
            "etext_key=ABC123456",
            "expiration_dt=2021-03-04",
            "refund_deadline_dt=2022-05-06",
            "refund_dt=2023-07-08",
            "refund_reason=Not good enough.");

    /**
     * Constructs a new {@code IvtRawStetext}.
     */
    TestRawStetext() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStetext obj =
                new RawStetext(TEST_STU_ID, TEST_ETEXT_ID, TEST_ACTIVE_DT, TEST_ETEXT_KEY,
                        TEST_EXPIRATION_DT, TEST_REFUND_DEADLINE_DT, TEST_REFUND_DT, TEST_REFUND_REASON);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_ETEXT_ID, obj.etextId, "Invalid etext_id value after constructor");
        assertEquals(TEST_ACTIVE_DT, obj.activeDt, "Invalid active_dt value after constructor");
        assertEquals(TEST_ETEXT_KEY, obj.etextKey, "Invalid etext_key value after constructor");
        assertEquals(TEST_EXPIRATION_DT, obj.expirationDt, "Invalid expiration_dt value after constructor");
        assertEquals(TEST_REFUND_DEADLINE_DT, obj.refundDeadlineDt,
                "Invalid refund_deadline_dt value after constructor");
        assertEquals(TEST_REFUND_DT, obj.refundDt, "Invalid refund_dt value after constructor");
        assertEquals(TEST_REFUND_REASON, obj.refundReason, "Invalid refund_reason value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStetext obj =
                new RawStetext(TEST_STU_ID, TEST_ETEXT_ID, TEST_ACTIVE_DT, TEST_ETEXT_KEY,
                        TEST_EXPIRATION_DT, TEST_REFUND_DEADLINE_DT, TEST_REFUND_DT, TEST_REFUND_REASON);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStetext obj = RawStetext.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_ETEXT_ID, obj.etextId, "Invalid etext_id value after deserialization");
        assertEquals(TEST_ACTIVE_DT, obj.activeDt, "Invalid active_dt value after deserialization");
        assertEquals(TEST_ETEXT_KEY, obj.etextKey, "Invalid etext_key value after deserialization");
        assertEquals(TEST_EXPIRATION_DT, obj.expirationDt, "Invalid expiration_dt value after deserialization");
        assertEquals(TEST_REFUND_DEADLINE_DT, obj.refundDeadlineDt,
                "Invalid refund_deadline_dt value after deserialization");
        assertEquals(TEST_REFUND_DT, obj.refundDt, "Invalid refund_dt value after deserialization");
        assertEquals(TEST_REFUND_REASON, obj.refundReason, "Invalid refund_reason value after deserialization");
    }
}
