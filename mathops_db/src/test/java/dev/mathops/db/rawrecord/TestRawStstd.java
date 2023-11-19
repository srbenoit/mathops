package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStstd} class.
 */
final class TestRawStstd {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_STD_ID = "A1.2";

    /** A field name. */
    private static final LocalDate TEST_WHEN_PLACED_OUT = LocalDate.of(2022, 1, 2);

    /** A field name. */
    private static final LocalDate TEST_WHEN_MASTERED = LocalDate.of(2023, 2, 3);

    /** A field name. */
    private static final Integer TEST_MASTERED_GROUPS = Integer.valueOf(18);

    /** A field name. */
    private static final Integer TEST_COMPLETED_STEPS = Integer.valueOf(7);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "std_id=A1.2",
            "when_placed_out=2022-01-02",
            "when_mastered=2023-02-03",
            "mastered_groups=18",
            "completed_steps=7");

    /**
     * Constructs a new {@code IvtRawStstd}.
     */
    TestRawStstd() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStstd obj = new RawStstd(TEST_STU_ID, TEST_STD_ID, TEST_WHEN_PLACED_OUT,
                TEST_WHEN_MASTERED, TEST_MASTERED_GROUPS, TEST_COMPLETED_STEPS);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_STD_ID, obj.stdId, "Invalid std_id value after constructor");
        assertEquals(TEST_WHEN_PLACED_OUT, obj.whenPlacedOut, "Invalid when_placed_out value after constructor");
        assertEquals(TEST_WHEN_MASTERED, obj.whenMastered, "Invalid when_mastered value after constructor");
        assertEquals(TEST_MASTERED_GROUPS, obj.masteredGroups, "Invalid mastered_groups value after constructor");
        assertEquals(TEST_COMPLETED_STEPS, obj.completedSteps, "Invalid completed_steps value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStstd obj = new RawStstd(TEST_STU_ID, TEST_STD_ID, TEST_WHEN_PLACED_OUT,
                TEST_WHEN_MASTERED, TEST_MASTERED_GROUPS, TEST_COMPLETED_STEPS);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStstd obj = RawStstd.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_STD_ID, obj.stdId, "Invalid std_id value after deserialization");
        assertEquals(TEST_WHEN_PLACED_OUT, obj.whenPlacedOut, "Invalid when_placed_out value after deserialization");
        assertEquals(TEST_WHEN_MASTERED, obj.whenMastered, "Invalid when_mastered value after deserialization");
        assertEquals(TEST_MASTERED_GROUPS, obj.masteredGroups, "Invalid mastered_groups value after deserialization");
        assertEquals(TEST_COMPLETED_STEPS, obj.completedSteps, "Invalid completed_steps value after deserialization");
    }
}
