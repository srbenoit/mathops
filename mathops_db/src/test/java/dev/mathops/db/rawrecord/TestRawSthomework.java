package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawSthomework} class.
 */
final class TestRawSthomework {

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(123456789L);

    /** A field name. */
    private static final String TEST_VERSION = "171H1";

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final LocalDate TEST_HW_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_HW_SCORE = Integer.valueOf(100);

    /** A field name. */
    private static final Integer TEST_START_TIME = Integer.valueOf(3000);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(4000);

    /** A field name. */
    private static final String TEST_TIME_OK = "Y";

    /** A field name. */
    private static final String TEST_PASSED = "N";

    /** A field name. */
    private static final String TEST_HW_TYPE = "LB";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_SECT = "001";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(1);

    /** A field name. */
    private static final Integer TEST_OBJECTIVE = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_HW_COUPON = "Z";

    /** A field name. */
    private static final LocalDate TEST_USED_DT = LocalDate.of(2022, 3, 4);

    /** A field name. */
    private static final Long TEST_USED_SERIAL_NBR = Long.valueOf(999L);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=123456789",
            "version=171H1",
            "stu_id=888888888",
            "hw_dt=2023-01-02",
            "hw_score=100",
            "start_time=3000",
            "finish_time=4000",
            "time_ok=Y",
            "passed=N",
            "hw_type=LB",
            "course=M 117",
            "sect=001",
            "unit=1",
            "objective=2",
            "hw_coupon=Z",
            "used_dt=2022-03-04",
            "used_serial_nbr=999");

    /**
     * Constructs a new {@code IvtRawSthomework}.
     */
    TestRawSthomework() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawSthomework obj = new RawSthomework(TEST_SERIAL_NBR, TEST_VERSION, TEST_STU_ID,
                TEST_HW_DT, TEST_HW_SCORE, TEST_START_TIME, TEST_FINISH_TIME, TEST_TIME_OK, TEST_PASSED,
                TEST_HW_TYPE, TEST_COURSE, TEST_SECT, TEST_UNIT, TEST_OBJECTIVE, TEST_HW_COUPON,
                TEST_USED_DT, TEST_USED_SERIAL_NBR);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_HW_DT, obj.hwDt, "Invalid hw_dt value after constructor");
        assertEquals(TEST_HW_SCORE, obj.hwScore, "Invalid hw_score value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_TIME_OK, obj.timeOk, "Invalid time_ok value after constructor");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after constructor");
        assertEquals(TEST_HW_TYPE, obj.hwType, "Invalid hw_type value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after constructor");
        assertEquals(TEST_HW_COUPON, obj.hwCoupon, "Invalid hw_coupon value after constructor");
        assertEquals(TEST_USED_DT, obj.usedDt, "Invalid used_dt value after constructor");
        assertEquals(TEST_USED_SERIAL_NBR, obj.usedSerialNbr, "Invalid used_serial_nbr value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawSthomework obj = new RawSthomework(TEST_SERIAL_NBR, TEST_VERSION, TEST_STU_ID,
                TEST_HW_DT, TEST_HW_SCORE, TEST_START_TIME, TEST_FINISH_TIME, TEST_TIME_OK, TEST_PASSED,
                TEST_HW_TYPE, TEST_COURSE, TEST_SECT, TEST_UNIT, TEST_OBJECTIVE, TEST_HW_COUPON,
                TEST_USED_DT, TEST_USED_SERIAL_NBR);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawSthomework obj = RawSthomework.parse(EXPECT_SER99);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_HW_DT, obj.hwDt, "Invalid hw_dt value after deserialization");
        assertEquals(TEST_HW_SCORE, obj.hwScore, "Invalid hw_score value after deserialization");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after deserialization");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after deserialization");
        assertEquals(TEST_TIME_OK, obj.timeOk, "Invalid time_ok value after deserialization");
        assertEquals(TEST_PASSED, obj.passed, "Invalid passed value after deserialization");
        assertEquals(TEST_HW_TYPE, obj.hwType, "Invalid hw_type value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after deserialization");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after deserialization");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after deserialization");
        assertEquals(TEST_HW_COUPON, obj.hwCoupon, "Invalid hw_coupon value after deserialization");
        assertEquals(TEST_USED_DT, obj.usedDt, "Invalid used_dt value after deserialization");
        assertEquals(TEST_USED_SERIAL_NBR, obj.usedSerialNbr, "Invalid used_serial_nbr value after deserialization");
    }
}
