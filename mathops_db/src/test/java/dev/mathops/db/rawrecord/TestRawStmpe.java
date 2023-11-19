package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStmpe} class.
 */
final class TestRawStmpe {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_VERSION = "MPTTC";

    /** A field name. */
    private static final String TEST_ACADEMIC_YR = "2021";

    /** A field name. */
    private static final LocalDate TEST_EXAM_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_START_TIME = Integer.valueOf(1000);

    /** A field name. */
    private static final Integer TEST_FINISH_TIME = Integer.valueOf(2000);

    /** A field name. */
    private static final String TEST_LAST_NAME = "Benoit";

    /** A field name. */
    private static final String TEST_FIRST_NAME = "Steve";

    /** A field name. */
    private static final String TEST_MIDDLE_INITIAL = "R";

    /** A field name. */
    private static final Integer TEST_SEQ_NBR = Integer.valueOf(999);

    /** A field name. */
    private static final Long TEST_SERIAL_NBR = Long.valueOf(987654321L);

    /** A field name. */
    private static final Integer TEST_STS_A = Integer.valueOf(9);

    /** A field name. */
    private static final Integer TEST_STS_117 = Integer.valueOf(8);

    /** A field name. */
    private static final Integer TEST_STS_118 = Integer.valueOf(7);

    /** A field name. */
    private static final Integer TEST_STS_124 = Integer.valueOf(6);

    /** A field name. */
    private static final Integer TEST_STS_125 = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_STS_126 = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_PLACED = "Y";

    /** A field name. */
    private static final String TEST_HOW_VALIDATED = "P";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "stu_id=888888888",
            "version=MPTTC",
            "academic_yr=2021",
            "exam_dt=2023-01-02",
            "start_time=1000",
            "finish_time=2000",
            "last_name=Benoit",
            "first_name=Steve",
            "middle_initial=R",
            "seq_nbr=999",
            "serial_nbr=987654321",
            "sts_a=9",
            "sts_117=8",
            "sts_118=7",
            "sts_124=6",
            "sts_125=5",
            "sts_126=4",
            "placed=Y",
            "how_validated=P");

    /**
     * Constructs a new {@code IvtRawStmpe}.
     */
    TestRawStmpe() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStmpe obj = new RawStmpe(TEST_STU_ID, TEST_VERSION, TEST_ACADEMIC_YR, TEST_EXAM_DT,
                TEST_START_TIME, TEST_FINISH_TIME, TEST_LAST_NAME, TEST_FIRST_NAME, TEST_MIDDLE_INITIAL,
                TEST_SEQ_NBR, TEST_SERIAL_NBR, TEST_STS_A, TEST_STS_117, TEST_STS_118, TEST_STS_124,
                TEST_STS_125, TEST_STS_126, TEST_PLACED, TEST_HOW_VALIDATED);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_ACADEMIC_YR, obj.academicYr, "Invalid academic_yr value after constructor");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after constructor");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after constructor");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after constructor");
        assertEquals(TEST_LAST_NAME, obj.lastName, "Invalid last_name value after constructor");
        assertEquals(TEST_FIRST_NAME, obj.firstName, "Invalid first_name value after constructor");
        assertEquals(TEST_MIDDLE_INITIAL, obj.middleInitial, "Invalid middle_initial value after constructor");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after constructor");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after constructor");
        assertEquals(TEST_STS_A, obj.stsA, "Invalid sts_a value after constructor");
        assertEquals(TEST_STS_117, obj.sts117, "Invalid sts_117 value after constructor");
        assertEquals(TEST_STS_118, obj.sts118, "Invalid sts_118 value after constructor");
        assertEquals(TEST_STS_124, obj.sts124, "Invalid sts_124 value after constructor");
        assertEquals(TEST_STS_125, obj.sts125, "Invalid sts_125 value after constructor");
        assertEquals(TEST_STS_126, obj.sts126, "Invalid sts_126 value after constructor");
        assertEquals(TEST_PLACED, obj.placed, "Invalid placed value after constructor");
        assertEquals(TEST_HOW_VALIDATED, obj.howValidated, "Invalid how_validated value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStmpe obj = new RawStmpe(TEST_STU_ID, TEST_VERSION, TEST_ACADEMIC_YR, TEST_EXAM_DT,
                TEST_START_TIME, TEST_FINISH_TIME, TEST_LAST_NAME, TEST_FIRST_NAME, TEST_MIDDLE_INITIAL,
                TEST_SEQ_NBR, TEST_SERIAL_NBR, TEST_STS_A, TEST_STS_117, TEST_STS_118, TEST_STS_124,
                TEST_STS_125, TEST_STS_126, TEST_PLACED, TEST_HOW_VALIDATED);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStmpe obj = RawStmpe.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_ACADEMIC_YR, obj.academicYr, "Invalid academic_yr value after deserialization");
        assertEquals(TEST_EXAM_DT, obj.examDt, "Invalid exam_dt value after deserialization");
        assertEquals(TEST_START_TIME, obj.startTime, "Invalid start_time value after deserialization");
        assertEquals(TEST_FINISH_TIME, obj.finishTime, "Invalid finish_time value after deserialization");
        assertEquals(TEST_LAST_NAME, obj.lastName, "Invalid last_name value after deserialization");
        assertEquals(TEST_FIRST_NAME, obj.firstName, "Invalid first_name value after deserialization");
        assertEquals(TEST_MIDDLE_INITIAL, obj.middleInitial, "Invalid middle_initial value after deserialization");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after deserialization");
        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial_nbr value after deserialization");
        assertEquals(TEST_STS_A, obj.stsA, "Invalid sts_a value after deserialization");
        assertEquals(TEST_STS_117, obj.sts117, "Invalid sts_117 value after deserialization");
        assertEquals(TEST_STS_118, obj.sts118, "Invalid sts_118 value after deserialization");
        assertEquals(TEST_STS_124, obj.sts124, "Invalid sts_124 value after deserialization");
        assertEquals(TEST_STS_125, obj.sts125, "Invalid sts_125 value after deserialization");
        assertEquals(TEST_STS_126, obj.sts126, "Invalid sts_126 value after deserialization");
        assertEquals(TEST_PLACED, obj.placed, "Invalid placed value after deserialization");
        assertEquals(TEST_HOW_VALIDATED, obj.howValidated, "Invalid how_validated value after deserialization");
    }
}
