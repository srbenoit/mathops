package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawStterm} class.
 */
final class TestRawStterm {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022); // $NON-NLS-1$

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final Integer TEST_PACE = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_PACE_TRACK = "A";

    /** A field name. */
    private static final String TEST_FIRST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_COHORT = "C1.2.3";

    /** A field name. */
    private static final Integer TEST_URGENCY = Integer.valueOf(27);

    /** A field name. */
    private static final String TEST_DO_NOT_DISTURB = "N";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "stu_id=888888888",
            "pace=4",
            "pace_track=A",
            "first_course=M 117",
            "cohort=C1.2.3",
            "urgency=27",
            "do_not_disturb=N");

    /**
     * Constructs a new {@code IvtRawStterm}.
     */
    TestRawStterm() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStterm obj = new RawStterm(TEST_TERM, TEST_STU_ID, TEST_PACE, TEST_PACE_TRACK,
                TEST_FIRST_COURSE, TEST_COHORT, TEST_URGENCY, TEST_DO_NOT_DISTURB);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after constructor");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace_track value after constructor");
        assertEquals(TEST_FIRST_COURSE, obj.firstCourse, "Invalid first_course value after constructor");
        assertEquals(TEST_COHORT, obj.cohort, "Invalid cohort value after constructor");
        assertEquals(TEST_URGENCY, obj.urgency, "Invalid urgency value after constructor");
        assertEquals(TEST_DO_NOT_DISTURB, obj.doNotDisturb, "Invalid do_not_disturb value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStterm obj = new RawStterm(TEST_TERM, TEST_STU_ID, TEST_PACE, TEST_PACE_TRACK,
                TEST_FIRST_COURSE, TEST_COHORT, TEST_URGENCY, TEST_DO_NOT_DISTURB);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStterm obj = RawStterm.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after deserialization");
        assertEquals(TEST_PACE, obj.pace, "Invalid pace value after deserialization");
        assertEquals(TEST_PACE_TRACK, obj.paceTrack, "Invalid pace_track value after deserialization");
        assertEquals(TEST_FIRST_COURSE, obj.firstCourse, "Invalid first_course value after deserialization");
        assertEquals(TEST_COHORT, obj.cohort, "Invalid cohort value after deserialization");
        assertEquals(TEST_URGENCY, obj.urgency, "Invalid urgency value after deserialization");
        assertEquals(TEST_DO_NOT_DISTURB, obj.doNotDisturb, "Invalid do_not_disturb value after deserialization");
    }
}
