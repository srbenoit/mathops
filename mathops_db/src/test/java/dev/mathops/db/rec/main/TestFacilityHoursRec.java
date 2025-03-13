package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code FacilityHoursRec} class.
 */
final class TestFacilityHoursRec {

    /** A field name. */
    private static final String TEST_FACILITY_ID = "PRECALC LC";

    /** A field name. */
    private static final Integer TEST_DISPLAY_INDEX = Integer.valueOf(6);

    /** A field name. */
    private static final Integer TEST_WEEKDAYS = Integer.valueOf(0x3E);

    /** A field name. */
    private static final LocalDate TEST_START_DT = LocalDate.of(2025, 1, 21);

    /** A field name. */
    private static final LocalDate TEST_END_DT = LocalDate.of(2025, 5, 9);

    /** A field name. */
    private static final LocalTime TEST_OPEN_TIME_1 = LocalTime.of(10, 0);

    /** A field name. */
    private static final LocalTime TEST_CLOSE_TIME_1 = LocalTime.of(16, 0);

    /** A field name. */
    private static final LocalTime TEST_OPEN_TIME_2 = LocalTime.of(18, 30);

    /** A field name. */
    private static final LocalTime TEST_CLOSE_TIME_2 = LocalTime.of(20, 15);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "facility=PRECALC LC",
            "display_index=6",
            "weekdays=62",
            "start_dt=2025-01-21",
            "end_dt=2025-05-09",
            "open_time_1=10:00",
            "close_time_1=16:00",
            "open_time_2=18:30",
            "close_time_2=20:15");

    /**
     * Constructs a new {@code TestFacilityHoursRec}.
     */
    TestFacilityHoursRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final FacilityHoursRec obj = new FacilityHoursRec(TEST_FACILITY_ID, TEST_DISPLAY_INDEX, TEST_WEEKDAYS,
                TEST_START_DT, TEST_END_DT, TEST_OPEN_TIME_1, TEST_CLOSE_TIME_1, TEST_OPEN_TIME_2, TEST_CLOSE_TIME_2);

        assertEquals(TEST_FACILITY_ID, obj.facility, "Invalid facility value after constructor");
        assertEquals(TEST_DISPLAY_INDEX, obj.displayIndex, "Invalid display_index value after constructor");
        assertEquals(TEST_WEEKDAYS, obj.weekdays, "Invalid weekdays value after constructor");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after constructor");
        assertEquals(TEST_END_DT, obj.endDt, "Invalid end_dt value after constructor");
        assertEquals(TEST_OPEN_TIME_1, obj.openTime1, "Invalid open_time_1 value after constructor");
        assertEquals(TEST_CLOSE_TIME_1, obj.closeTime1, "Invalid close_time_1 value after constructor");
        assertEquals(TEST_OPEN_TIME_2, obj.openTime2, "Invalid open_time_2 value after constructor");
        assertEquals(TEST_CLOSE_TIME_2, obj.closeTime2, "Invalid close_time_2 value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final FacilityHoursRec obj = new FacilityHoursRec(TEST_FACILITY_ID, TEST_DISPLAY_INDEX, TEST_WEEKDAYS,
                TEST_START_DT, TEST_END_DT, TEST_OPEN_TIME_1, TEST_CLOSE_TIME_1, TEST_OPEN_TIME_2, TEST_CLOSE_TIME_2);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
