package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawCunit} class.
 */
final class TestRawCunit {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(3);

    /** A field name. */
    private static final Float TEST_UNIT_EXAM_WGT = Float.valueOf(1.5f);

    /** A field name. */
    private static final String TEST_UNIT_DESC = "Unit 3";

    /** A field name. */
    private static final Integer TEST_UNIT_TIMELIMIT = Integer.valueOf(60);

    /** A field name. */
    private static final Integer TEST_POSSIBLE_SCORE = Integer.valueOf(15);

    /** A field name. */
    private static final Integer TEST_NBR_QUESTIONS = Integer.valueOf(10);

    /** A field name. */
    private static final String TEST_UNIT_TYPE = "IN";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "course=M 117",
            "unit=3",
            "unit_exam_wgt=1.5",
            "unit_desc=Unit 3",
            "unit_timelimit=60",
            "possible_score=15",
            "nbr_questions=10",
            "unit_type=IN");

    /**
     * Constructs a new {@code IvtRawCunit}.
     */
    TestRawCunit() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawCunit obj =
                new RawCunit(TEST_TERM, TEST_COURSE, TEST_UNIT, TEST_UNIT_EXAM_WGT, TEST_UNIT_DESC,
                        TEST_UNIT_TIMELIMIT, TEST_POSSIBLE_SCORE, TEST_NBR_QUESTIONS, TEST_UNIT_TYPE);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_UNIT_EXAM_WGT, obj.unitExamWgt, "Invalid unit_exam_wgt value after constructor");
        assertEquals(TEST_UNIT_DESC, obj.unitDesc, "Invalid unit_desc value after constructor");
        assertEquals(TEST_UNIT_TIMELIMIT, obj.unitTimelimit, "Invalid unit_timelimit value after constructor");
        assertEquals(TEST_POSSIBLE_SCORE, obj.possibleScore, "Invalid possible_score value after constructor");
        assertEquals(TEST_NBR_QUESTIONS, obj.nbrQuestions, "Invalid nbr_questions value after constructor");
        assertEquals(TEST_UNIT_TYPE, obj.unitType, "Invalid unit_type value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawCunit obj =
                new RawCunit(TEST_TERM, TEST_COURSE, TEST_UNIT, TEST_UNIT_EXAM_WGT, TEST_UNIT_DESC,
                        TEST_UNIT_TIMELIMIT, TEST_POSSIBLE_SCORE, TEST_NBR_QUESTIONS, TEST_UNIT_TYPE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
