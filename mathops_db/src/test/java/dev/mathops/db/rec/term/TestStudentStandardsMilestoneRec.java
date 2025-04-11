package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StudentStandardsMilestoneRec} class.
 */
public final class TestStudentStandardsMilestoneRec {

    /** A field name. */
    private static final String TEST_STUDENT_ID = "876543210";

    /** A field name. */
    private static final String TEST_PACE_TRACK = "A";

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(3);

    /** A field name. */
    private static final Integer TEST_PACE_INDEX = Integer.valueOf(2);

    /** A field name. */
    private static final Integer TEST_MODULE_NBR = Integer.valueOf(5);

    /** A field name. */
    private static final String TEST_MS_TYPE = "HW";

    /** A field name. */
    private static final LocalDate TEST_MS_DATE = LocalDate.of(2025, Month.APRIL, 10);

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "student_id=876543210",
            "pace_track=A",
            "pace=3",
            "pace_index=2",
            "module_nbr=5",
            "ms_type=HW",
            "ms_date=2025-04-10");

    /**
     * Constructs a new {@code TestStudentStandardsMilestoneRec}.
     */
    TestStudentStandardsMilestoneRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StudentStandardsMilestoneRec obj = new StudentStandardsMilestoneRec(TEST_STUDENT_ID, TEST_PACE_TRACK,
                TEST_PACE, TEST_PACE_INDEX, TEST_MODULE_NBR, TEST_MS_TYPE, TEST_MS_DATE);

        assertEquals(TEST_STUDENT_ID, obj.studentId, "Invalid student ID value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace track value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after constructor");
        assertEquals(TEST_PACE_INDEX, obj.paceIndex, "Invalid pace index value after constructor");
        assertEquals(TEST_MODULE_NBR, obj.moduleNbr, "Invalid module number value after constructor");
        assertEquals(TEST_MS_TYPE, obj.msType, "Invalid milestone date value after constructor");
        assertEquals(TEST_MS_DATE, obj.msDate, "Invalid milestone type value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StudentStandardsMilestoneRec obj = new StudentStandardsMilestoneRec(TEST_STUDENT_ID, TEST_PACE_TRACK,
                TEST_PACE, TEST_PACE_INDEX, TEST_MODULE_NBR, TEST_MS_TYPE, TEST_MS_DATE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}