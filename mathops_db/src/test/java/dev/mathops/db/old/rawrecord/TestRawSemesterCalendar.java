package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawSemesterCalendar} class.
 */
final class TestRawSemesterCalendar {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final Integer TEST_WEEK_NBR = Integer.valueOf(1);

    /** A field name. */
    private static final LocalDate TEST_START_DT = LocalDate.of(2022, 12, 28);

    /** A field name. */
    private static final LocalDate TEST_END_DT = LocalDate.of(2023, 1, 4);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "week_nbr=1",
            "start_dt=2022-12-28",
            "end_dt=2023-01-04");

    /**
     * Constructs a new {@code IvtRawSemesterCalendar}.
     */
    TestRawSemesterCalendar() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawSemesterCalendar obj =
                new RawSemesterCalendar(TEST_TERM, TEST_WEEK_NBR, TEST_START_DT, TEST_END_DT);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_WEEK_NBR, obj.weekNbr, "Invalid apln_term value after constructor");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid course value after constructor");
        assertEquals(TEST_END_DT, obj.endDt, "Invalid start_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawSemesterCalendar obj = new RawSemesterCalendar(TEST_TERM, TEST_WEEK_NBR, TEST_START_DT, TEST_END_DT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawSemesterCalendar obj = RawSemesterCalendar.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_WEEK_NBR, obj.weekNbr, "Invalid apln_term value after deserialization");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid course value after deserialization");
        assertEquals(TEST_END_DT, obj.endDt, "Invalid start_dt value after deserialization");
    }
}
