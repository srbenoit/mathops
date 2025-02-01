package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStresource} class.
 */
final class TestRawStresource {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_RESOURCE_ID = "C99";

    /** A field name. */
    private static final LocalDate TEST_LOAN_DT = LocalDate.of(2020, 1, 2);

    /** A field name. */
    private static final Integer TEST_START_TIME = Integer.valueOf(1000);

    /** A field name. */
    private static final LocalDate TEST_DUE_DT = LocalDate.of(2021, 2, 3);

    /** A field name. */
    private static final LocalDate TEST_RETURN_DT = LocalDate.of(2022, 3, 4);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(2000);

    /** A field name. */
    private static final Integer TEST_TIMES_DISPLAY = Integer.valueOf(12);

    /** A field name. */
    private static final LocalDate TEST_CREATE_DT = LocalDate.of(2023, 4, 5);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "resource_id=C99",
            "loan_dt=2020-01-02",
            "start_time=1000",
            "due_dt=2021-02-03",
            "return_dt=2022-03-04",
            "finish_time=2000",
            "times_display=12",
            "create_dt=2023-04-05");

    /**
     * Constructs a new {@code IvtRawStresource}.
     */
    TestRawStresource() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStresource obj =
                new RawStresource(TEST_STU_ID, TEST_RESOURCE_ID, TEST_LOAN_DT, TEST_START_TIME,
                        TEST_DUE_DT, TEST_RETURN_DT, TEST_FINISH_TIME, TEST_TIMES_DISPLAY, TEST_CREATE_DT);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_RESOURCE_ID, obj.resourceId, "Invalid resource_id value after constructor");
        assertEquals(TEST_LOAN_DT, obj.loanDt, "Invalid loan_dt value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_DUE_DT, obj.dueDt, "Invalid due_dt value after constructor");
        assertEquals(TEST_RETURN_DT, obj.returnDt, "Invalid return_dt value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_TIMES_DISPLAY, obj.timesDisplay, "Invalid times_display value after constructor");
        assertEquals(TEST_CREATE_DT, obj.createDt, "Invalid create_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStresource obj =
                new RawStresource(TEST_STU_ID, TEST_RESOURCE_ID, TEST_LOAN_DT, TEST_START_TIME,
                        TEST_DUE_DT, TEST_RETURN_DT, TEST_FINISH_TIME, TEST_TIMES_DISPLAY, TEST_CREATE_DT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
