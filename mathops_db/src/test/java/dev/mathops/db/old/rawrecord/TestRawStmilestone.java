package dev.mathops.db.old.rawrecord;

import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;
import dev.mathops.db.type.TermKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawStmilestone} class.
 */
final class TestRawStmilestone {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_PACE_TRACK = "A";

    /** A field name. */
    private static final Integer TEST_MS_NBR = Integer.valueOf(111);

    /** A field name. */
    private static final String TEST_MS_TYPE = "RE";

    /** A field name. */
    private static final LocalDate TEST_MS_DATE = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_NBR_ATMPTS_ALLOW = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_EXT_TYPE = "ACC";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "stu_id=888888888",
            "pace_track=A",
            "ms_nbr=111",
            "ms_type=RE",
            "ms_date=2023-01-02",
            "nbr_atmpts_allow=1");

    /**
     * Constructs a new {@code IvtRawStmilestone}.
     */
    TestRawStmilestone() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStmilestone obj = new RawStmilestone(TEST_TERM, TEST_STU_ID, TEST_PACE_TRACK,
                TEST_MS_NBR, TEST_MS_TYPE, TEST_MS_DATE, TEST_NBR_ATMPTS_ALLOW);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace_track value after constructor");
        assertEquals(TEST_MS_NBR, obj.msNbr, "Invalid ms_nbr value after constructor");
        assertEquals(TEST_MS_TYPE, obj.msType, "Invalid ms_type value after constructor");
        assertEquals(TEST_MS_DATE, obj.msDate, "Invalid ms_date value after constructor");
        assertEquals(TEST_NBR_ATMPTS_ALLOW, obj.nbrAtmptsAllow, "Invalid nbr_atmpts_allow value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStmilestone obj = new RawStmilestone(TEST_TERM, TEST_STU_ID, TEST_PACE_TRACK,
                TEST_MS_NBR, TEST_MS_TYPE, TEST_MS_DATE, TEST_NBR_ATMPTS_ALLOW);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
