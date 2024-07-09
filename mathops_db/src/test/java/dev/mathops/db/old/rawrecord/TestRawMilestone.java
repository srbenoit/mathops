package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawMilestone} class.
 */
final class TestRawMilestone {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(3);

    /** A field name. */
    private static final String TEST_PACE_TRACK = "A";

    /** A field name. */
    private static final Integer TEST_MS_NBR = Integer.valueOf(331);

    /** A field name. */
    private static final String TEST_MS_TYPE = "R";

    /** A field name. */
    private static final LocalDate TEST_MS_DATE = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_NBR_ATMPTS_ALLOW = Integer.valueOf(2);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "pace=3",
            "pace_track=A",
            "ms_nbr=331",
            "ms_type=R",
            "ms_date=2023-01-02",
            "nbr_atmpts_allow=2");

    /**
     * Constructs a new {@code IvtRawMilestone}.
     */
    TestRawMilestone() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawMilestone obj = new RawMilestone(TEST_TERM, TEST_PACE, TEST_PACE_TRACK,
                TEST_MS_NBR, TEST_MS_TYPE, TEST_MS_DATE, TEST_NBR_ATMPTS_ALLOW);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace_track value after constructor");
        assertEquals(TEST_MS_NBR, obj.msNbr, "Invalid ms_nbr value after constructor");
        assertEquals(TEST_MS_TYPE, obj.msType, "Invalid ms_type value after constructor");
        assertEquals(TEST_MS_DATE, obj.msDate, "Invalid ms_date value after constructor");
        assertEquals(TEST_NBR_ATMPTS_ALLOW, obj.nbrAtmptsAllow, "Invalid nbr_atmps_allow value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawMilestone obj = new RawMilestone(TEST_TERM, TEST_PACE, TEST_PACE_TRACK,
                TEST_MS_NBR, TEST_MS_TYPE, TEST_MS_DATE, TEST_NBR_ATMPTS_ALLOW);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
