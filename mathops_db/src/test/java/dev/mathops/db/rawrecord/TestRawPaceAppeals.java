package dev.mathops.db.rawrecord;

import dev.mathops.db.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawPaceAppeals} class.
 */
final class TestRawPaceAppeals {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_STU_ID = "88888888";

    /** A field name. */
    private static final LocalDate TEST_APPEAL_DT = LocalDate.of(2021, 1, 2);

    /** A field name. */
    private static final String TEST_RELIEF_GIVEN = "Y";

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(3);

    /** A field name. */
    private static final String TEST_PACE_TRACK = "A";

    /** A field name. */
    private static final Integer TEST_MS_NBR = Integer.valueOf(332);

    /** A field name. */
    private static final String TEST_MS_TYPE = "R";

    /** A field name. */
    private static final LocalDate TEST_MS_DATE = LocalDate.of(2022, 3, 4);

    /** A field name. */
    private static final LocalDate TEST_NEW_DEADLINE_DT = LocalDate.of(2023, 4, 5);

    /** A field name. */
    private static final Integer TEST_NBR_ATMPTS_ALLOW = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_CIRCUMSTANCES = "Circumstances";

    /** A field name. */
    private static final String TEST_COMMENT = "Comment";

    /** A field name. */
    private static final String TEST_INTERVIEWER = "Interviewer";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "stu_id=88888888",
            "appeal_dt=2021-01-02",
            "relief_given=Y",
            "pace=3",
            "pace_track=A",
            "ms_nbr=332",
            "ms_type=R",
            "ms_date=2022-03-04",
            "new_deadline_dt=2023-04-05",
            "nbr_atmpts_allow=4",
            "circumstances=Circumstances",
            "comment=Comment",
            "interviewer=Interviewer");

    /**
     * Constructs a new {@code IvtRawPaceAppeals}.
     */
    TestRawPaceAppeals() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawPaceAppeals obj =
                new RawPaceAppeals(TEST_TERM, TEST_STU_ID, TEST_APPEAL_DT, TEST_RELIEF_GIVEN, TEST_PACE,
                        TEST_PACE_TRACK, TEST_MS_NBR, TEST_MS_TYPE, TEST_MS_DATE, TEST_NEW_DEADLINE_DT,
                        TEST_NBR_ATMPTS_ALLOW, TEST_CIRCUMSTANCES, TEST_COMMENT, TEST_INTERVIEWER);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_APPEAL_DT, obj.appealDt, "Invalid test_code value after constructor");
        assertEquals(TEST_RELIEF_GIVEN, obj.reliefGiven, "Invalid test_date value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid test_code value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid test_date value after constructor");
        assertEquals(TEST_MS_NBR, obj.msNbr, "Invalid test_code value after constructor");
        assertEquals(TEST_MS_TYPE, obj.msType, "Invalid test_date value after constructor");
        assertEquals(TEST_MS_DATE, obj.msDate, "Invalid test_code value after constructor");
        assertEquals(TEST_NEW_DEADLINE_DT, obj.newDeadlineDt, "Invalid test_date value after constructor");
        assertEquals(TEST_NBR_ATMPTS_ALLOW, obj.nbrAtmptsAllow, "Invalid test_code value after constructor");
        assertEquals(TEST_CIRCUMSTANCES, obj.circumstances, "Invalid test_date value after constructor");
        assertEquals(TEST_COMMENT, obj.comment, "Invalid test_code value after constructor");
        assertEquals(TEST_INTERVIEWER, obj.interviewer, "Invalid test_date value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawPaceAppeals obj =
                new RawPaceAppeals(TEST_TERM, TEST_STU_ID, TEST_APPEAL_DT, TEST_RELIEF_GIVEN, TEST_PACE,
                        TEST_PACE_TRACK, TEST_MS_NBR, TEST_MS_TYPE, TEST_MS_DATE, TEST_NEW_DEADLINE_DT,
                        TEST_NBR_ATMPTS_ALLOW, TEST_CIRCUMSTANCES, TEST_COMMENT, TEST_INTERVIEWER);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawPaceAppeals obj = RawPaceAppeals.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_APPEAL_DT, obj.appealDt, "Invalid test_code value after deserialization");
        assertEquals(TEST_RELIEF_GIVEN, obj.reliefGiven, "Invalid test_date value after deserialization");
        assertEquals(TEST_PACE, obj.pace, "Invalid test_code value after deserialization");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid test_date value after deserialization");
        assertEquals(TEST_MS_NBR, obj.msNbr, "Invalid test_code value after deserialization");
        assertEquals(TEST_MS_TYPE, obj.msType, "Invalid test_date value after deserialization");
        assertEquals(TEST_MS_DATE, obj.msDate, "Invalid test_code value after deserialization");
        assertEquals(TEST_NEW_DEADLINE_DT, obj.newDeadlineDt, "Invalid test_date value after deserialization");
        assertEquals(TEST_NBR_ATMPTS_ALLOW, obj.nbrAtmptsAllow, "Invalid test_code value after deserialization");
        assertEquals(TEST_CIRCUMSTANCES, obj.circumstances, "Invalid test_date value after deserialization");
        assertEquals(TEST_COMMENT, obj.comment, "Invalid test_code value after deserialization");
        assertEquals(TEST_INTERVIEWER, obj.interviewer, "Invalid test_date value after deserialization");
    }
}
