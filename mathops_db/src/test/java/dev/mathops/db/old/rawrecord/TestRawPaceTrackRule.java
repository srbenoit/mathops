package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawPaceTrackRule} class.
 */
final class TestRawPaceTrackRule {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_SUBTERM = "FULL";

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_PACE_TRACK = "B";

    /** A field name. */
    private static final String TEST_CRITERIA = "DEFAULT";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "subterm=FULL",
            "pace=2",
            "pace_track=B",
            "criteria=DEFAULT");

    /**
     * Constructs a new {@code IvtRawPaceTrackRule}.
     */
    TestRawPaceTrackRule() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawPaceTrackRule obj = new RawPaceTrackRule(TEST_TERM, TEST_SUBTERM, TEST_PACE,
                TEST_PACE_TRACK, TEST_CRITERIA);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_SUBTERM, obj.subterm, "Invalid subterm value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid paceTrack value after constructor");
        assertEquals(TEST_CRITERIA, obj.criteria, "Invalid criteria value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawPaceTrackRule obj = new RawPaceTrackRule(TEST_TERM, TEST_SUBTERM, TEST_PACE,
                TEST_PACE_TRACK, TEST_CRITERIA);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawPaceTrackRule obj = RawPaceTrackRule.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_SUBTERM, obj.subterm, "Invalid subterm value after deserialization");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after deserialization");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid paceTrack value after deserialization");
        assertEquals(TEST_CRITERIA, obj.criteria, "Invalid criteria value after deserialization");
    }
}
