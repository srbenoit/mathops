package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

/**
 * Tests for the {@code RawMpscorequeue} class.
 */
final class TestRawMpscorequeue {

    /** A field name. */
    private static final Integer TEST_PIDM = Integer.valueOf(999888);

    /** A field name. */
    private static final String TEST_TEST_CODE = "PL";

    /** A field name. */
    private static final LocalDateTime TEST_TEST_DATE = LocalDateTime.of(2023, 1, 2, 3, 4, 5);

    /** A field name. */
    private static final String TEST_TEST_SCORE = "5";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "pidm=999888",
            "test_code=PL",
            "test_date=2023-01-02T03:04:05",
            "test_score=5");

    /**
     * Constructs a new {@code IvtRawMpscorequeue}.
     */
    TestRawMpscorequeue() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawMpscorequeue obj = new RawMpscorequeue(TEST_PIDM, TEST_TEST_CODE, TEST_TEST_DATE, TEST_TEST_SCORE);

        assertEquals(TEST_PIDM, obj.pidm, "Invalid stu_id value after constructor");
        assertEquals(TEST_TEST_CODE, obj.testCode, "Invalid test_code value after constructor");
        assertEquals(TEST_TEST_DATE, obj.testDate, "Invalid test_date value after constructor");
        assertEquals(TEST_TEST_SCORE, obj.testScore, "Invalid test_score value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawMpscorequeue obj =
                new RawMpscorequeue(TEST_PIDM, TEST_TEST_CODE, TEST_TEST_DATE, TEST_TEST_SCORE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
