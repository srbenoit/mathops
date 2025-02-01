package dev.mathops.db.rec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code PaceTrackRule} class.
 */
final class TestPaceTrackRuleRec {

    /** A field name. */
    private static final String TEST_SUBTERM = "A";

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_PACE_TRACK = "B";

    /** A field name. */
    private static final String TEST_CRITERIA = "HAS(M 125) | HAS(M 126)";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "subterm=A",
            "pace=2",
            "pace_track=B",
            "criteria=HAS(M 125) | HAS(M 126)");

    /**
     * Constructs a new {@code TestPaceTrackRuleRec}.
     */
    TestPaceTrackRuleRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final PaceTrackRuleRec obj = new PaceTrackRuleRec(TEST_SUBTERM, TEST_PACE, TEST_PACE_TRACK, TEST_CRITERIA);

        assertEquals(TEST_SUBTERM, obj.subterm, "Invalid subterm value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace_track value after constructor");
        assertEquals(TEST_CRITERIA, obj.criteria, "Invalid unit criteria after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final PaceTrackRuleRec obj = new PaceTrackRuleRec(TEST_SUBTERM, TEST_PACE, TEST_PACE_TRACK, TEST_CRITERIA);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
