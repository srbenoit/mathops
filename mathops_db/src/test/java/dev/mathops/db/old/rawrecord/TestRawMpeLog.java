package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawMpeLog} class.
 */
final class TestRawMpeLog {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_ACADEMIC_YR = "2021";

    /** A field name. */
    private static final String TEST_COURSE = "M 100P";

    /** A field name. */
    private static final String TEST_VERSION = "MPTTC";

    /** A field name. */
    private static final LocalDate TEST_START_DT = LocalDate.of(2021, 1, 2);

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2022, 2, 3);

    /** A field name. */
    private static final LocalDate TEST_RECOVER_DT = LocalDate.of(2023, 3, 4);

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(123456L);

    /** A field name. */
    private static final Integer TEST_START_TIME = Integer.valueOf(100);

    /** A field name. */
    private static final String TEST_CALC_NBR = "99";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "academic_yr=2021",
            "course=M 100P",
            "version=MPTTC",
            "start_dt=2021-01-02",
            "exam_dt=2022-02-03",
            "recover_dt=2023-03-04",
            "serial_nbr=123456",
            "start_time=100",
            "calc_nbr=99");

    /**
     * Constructs a new {@code IvtRawMpeLog}.
     */
    TestRawMpeLog() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawMpeLog obj =
                new RawMpeLog(TEST_STU_ID, TEST_ACADEMIC_YR, TEST_COURSE, TEST_VERSION, TEST_START_DT,
                        TEST_EXAM_DT, TEST_RECOVER_DT, TEST_SERIAL_NBR, TEST_START_TIME, TEST_CALC_NBR);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_ACADEMIC_YR, obj.academicYr, "Invalid academic_yr value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_RECOVER_DT, obj.recoverDt, "Invalid recover_dt value after constructor");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_CALC_NBR, obj.calcNbr, "Invalid calc_nbr value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawMpeLog obj =
                new RawMpeLog(TEST_STU_ID, TEST_ACADEMIC_YR, TEST_COURSE, TEST_VERSION, TEST_START_DT,
                        TEST_EXAM_DT, TEST_RECOVER_DT, TEST_SERIAL_NBR, TEST_START_TIME, TEST_CALC_NBR);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
