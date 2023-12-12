package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStpaceSummary} class.
 */
final class TestRawStpaceSummary {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022); // $NON-NLS-1$

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_SECT = "001";

    /** A field name. */
    private static final String TEST_I_IN_PROGRESS = "N";

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(5);

    /** A field name. */
    private static final String TEST_PACE_TRACK = "A";

    /** A field name. */
    private static final Integer TEST_PACE_ORDER = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_MS_NBR = Integer.valueOf(543);

    /** A field name. */
    private static final Integer TEST_MS_UNIT = Integer.valueOf(3);

    /** A field name. */
    private static final LocalDate TEST_MS_DATE = LocalDate.of(2022, 1, 2);

    /** A field name. */
    private static final String TEST_NEW_MS_DATE = "Y";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 3, 4);

    /** A field name. */
    private static final Integer TEST_RE_POINTS = Integer.valueOf(2);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "term=Fall, 2022",
            "stu_id=888888888",
            "course=M 117",
            "sect=001",
            "i_in_progress=N",
            "pace=5",
            "pace_track=A",
            "pace_order=4",
            "ms_nbr=543",
            "ms_unit=3",
            "ms_date=2022-01-02",
            "new_ms_date=Y",
            "exam_dt=2023-03-04",
            "re_points=2");

    /**
     * Constructs a new {@code IvtRawStpaceSummary}.
     */
    TestRawStpaceSummary() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStpaceSummary obj = new RawStpaceSummary(TEST_TERM, TEST_STU_ID, TEST_COURSE,
                TEST_SECT, TEST_I_IN_PROGRESS, TEST_PACE, TEST_PACE_TRACK, TEST_PACE_ORDER, TEST_MS_NBR,
                TEST_MS_UNIT, TEST_MS_DATE, TEST_NEW_MS_DATE, TEST_EXAM_DT, TEST_RE_POINTS);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after constructor");
        assertEquals(TEST_I_IN_PROGRESS, obj.iInProgress, "Invalid i_in_progress value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace_track value after constructor");
        assertEquals(TEST_PACE_ORDER, obj.paceOrder, "Invalid pace_order value after constructor");
        assertEquals(TEST_MS_NBR, obj.msNbr, "Invalid ms_nbr value after constructor");
        assertEquals(TEST_MS_UNIT, obj.msUnit, "Invalid ms_unit value after constructor");
        assertEquals(TEST_MS_DATE, obj.msDate, "Invalid ms_date value after constructor");
        assertEquals(TEST_NEW_MS_DATE, obj.newMsDate, "Invalid new_ms_date value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_date value after constructor");
        assertEquals(TEST_RE_POINTS, obj.rePoints, "Invalid re_points value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStpaceSummary obj = new RawStpaceSummary(TEST_TERM, TEST_STU_ID, TEST_COURSE,
                TEST_SECT, TEST_I_IN_PROGRESS, TEST_PACE, TEST_PACE_TRACK, TEST_PACE_ORDER, TEST_MS_NBR,
                TEST_MS_UNIT, TEST_MS_DATE, TEST_NEW_MS_DATE, TEST_EXAM_DT, TEST_RE_POINTS);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStpaceSummary obj = RawStpaceSummary.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid term value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after deserialization");
        assertEquals(TEST_I_IN_PROGRESS, obj.iInProgress, "Invalid i_in_progress value after deserialization");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after deserialization");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace_track value after deserialization");
        assertEquals(TEST_PACE_ORDER, obj.paceOrder, "Invalid pace_order value after deserialization");
        assertEquals(TEST_MS_NBR, obj.msNbr, "Invalid ms_nbr value after deserialization");
        assertEquals(TEST_MS_UNIT, obj.msUnit, "Invalid ms_unit value after deserialization");
        assertEquals(TEST_MS_DATE, obj.msDate, "Invalid ms_date value after deserialization");
        assertEquals(TEST_NEW_MS_DATE, obj.newMsDate, "Invalid new_ms_date value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_date value after deserialization");
        assertEquals(TEST_RE_POINTS, obj.rePoints, "Invalid re_points value after deserialization");
    }
}
