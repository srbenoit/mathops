package dev.mathops.db.rawrecord;

import dev.mathops.db.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawPacingRules} class.
 */
final class TestRawPacingRules {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_PACING_STRUCTURE = "I";

    /** A field name. */
    private static final String TEST_ACTIVITY_TYPE = "RE";

    /** A field name. */
    private static final String TEST_REQUIREMENT = "HW";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "pacing_structure=I",
            "activity_type=RE",
            "requirement=HW");

    /**
     * Constructs a new {@code IvtRawPacingRules}.
     */
    TestRawPacingRules() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawPacingRules obj = new RawPacingRules(TEST_TERM, TEST_PACING_STRUCTURE,
                TEST_ACTIVITY_TYPE, TEST_REQUIREMENT);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_PACING_STRUCTURE, obj.pacingStructure, "Invalid pacing_structure value after constructor");
        assertEquals(TEST_ACTIVITY_TYPE, obj.activityType, "Invalid activity_type value after constructor");
        assertEquals(TEST_REQUIREMENT, obj.requirement, "Invalid requirement value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawPacingRules obj = new RawPacingRules(TEST_TERM, TEST_PACING_STRUCTURE,
                TEST_ACTIVITY_TYPE, TEST_REQUIREMENT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawPacingRules obj = RawPacingRules.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_PACING_STRUCTURE, obj.pacingStructure,
                "Invalid pacing_structure value after deserialization");
        assertEquals(TEST_ACTIVITY_TYPE, obj.activityType, "Invalid activity_type value after deserialization");
        assertEquals(TEST_REQUIREMENT, obj.requirement, "Invalid requirement value after deserialization");
    }
}
