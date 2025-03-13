package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code FacilityClosureRec} class.
 */
final class TestFacilityClosureRec {

    /** A field name. */
    private static final String TEST_FACILITY_ID = "PRECALC LC";

    /** A field name. */
    private static final LocalDate TEST_CLOSURE_DT = LocalDate.of(2025, 3, 17);

    /** A field name. */
    private static final String TEST_CLOSURE_TYPE = "SPBR";

    /** A field name. */
    private static final LocalTime TEST_START_TIME = LocalTime.of(10, 0);

    /** A field name. */
    private static final LocalTime TEST_END_TIME = LocalTime.of(16, 0);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "facility=PRECALC LC",
            "closure_dt=2025-03-17",
            "closure_type=SPBR",
            "start_time=10:00",
            "end_time=16:00");

    /**
     * Constructs a new {@code TestFacilityClosureRec}.
     */
    TestFacilityClosureRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final FacilityClosureRec obj = new FacilityClosureRec(TEST_FACILITY_ID, TEST_CLOSURE_DT, TEST_CLOSURE_TYPE,
                TEST_START_TIME, TEST_END_TIME);

        assertEquals(TEST_FACILITY_ID, obj.facility, "Invalid facility value after constructor");
        assertEquals(TEST_CLOSURE_DT, obj.closureDt, "Invalid closure_dt value after constructor");
        assertEquals(TEST_CLOSURE_TYPE, obj.closureType, "Invalid closure_type value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_END_TIME, obj.endTime, "Invalid end_time value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final FacilityClosureRec obj = new FacilityClosureRec(TEST_FACILITY_ID, TEST_CLOSURE_DT, TEST_CLOSURE_TYPE,
                TEST_START_TIME, TEST_END_TIME);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
