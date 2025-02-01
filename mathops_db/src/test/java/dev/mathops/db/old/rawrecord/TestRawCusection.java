package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawCusection} class.
 */
final class TestRawCusection {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_SECT = "001";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(4);

    /** A field name. */
    private static final Integer TEST_TIMEOUT = Integer.valueOf(4800);

    /** A field name. */
    private static final Integer TEST_RE_MASTERY_SCORE = Integer.valueOf(8);

    /** A field name. */
    private static final Integer TEST_UE_MASTERY_SCORE = Integer.valueOf(9);

    /** A field name. */
    private static final Integer TEST_HW_MASTERY_SCORE = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_HW_MOVEON_SCORE = Integer.valueOf(2);

    /** A field name. */
    private static final Integer TEST_NBR_ATMPTS_ALLOW = Integer.valueOf(99);

    /** A field name. */
    private static final Integer TEST_ATMPTS_PER_REVIEW = Integer.valueOf(3);

    /** A field name. */
    private static final LocalDate TEST_FIRST_TEST_DT = LocalDate.of(2021, 1, 2);

    /** A field name. */
    private static final LocalDate TEST_LAST_TEST_DT = LocalDate.of(2022, 2, 3);

    /** A field name. */
    private static final Integer TEST_BEGIN_TEST_PERIOD = Integer.valueOf(120);

    /** A field name. */
    private static final Integer TEST_END_TEST_PERIOD = Integer.valueOf(86400);

    /** A field name. */
    private static final Integer TEST_COUPON_COST = Integer.valueOf(1);

    /** A field name. */
    private static final LocalDate TEST_LAST_COUPON_DT = LocalDate.of(2023, 3, 4);

    /** A field name. */
    private static final String TEST_SHOW_TEST_WINDOW = "N";

    /** A field name. */
    private static final String TEST_UNPROCTORED_EXAM = "Y";

    /** A field name. */
    private static final Integer TEST_RE_POINTS_ONTIME = Integer.valueOf(6);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "course=M 117",
            "sect=001",
            "unit=4",
            "timeout=4800",
            "re_mastery_score=8",
            "ue_mastery_score=9",
            "hw_mastery_score=5",
            "hw_moveon_score=2",
            "nbr_atmpts_allow=99",
            "atmpts_per_review=3",
            "first_test_dt=2021-01-02",
            "last_test_dt=2022-02-03",
            "begin_test_period=120",
            "end_test_period=86400",
            "coupon_cost=1",
            "last_coupon_dt=2023-03-04",
            "show_test_window=N",
            "unproctored_exam=Y",
            "re_points_ontime=6");

    /**
     * Constructs a new {@code IvtRawCusection}.
     */
    TestRawCusection() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawCusection obj = new RawCusection(TEST_TERM, TEST_COURSE, TEST_SECT, TEST_UNIT,
                TEST_TIMEOUT, TEST_RE_MASTERY_SCORE, TEST_UE_MASTERY_SCORE, TEST_HW_MASTERY_SCORE,
                TEST_HW_MOVEON_SCORE, TEST_NBR_ATMPTS_ALLOW, TEST_ATMPTS_PER_REVIEW, TEST_FIRST_TEST_DT,
                TEST_LAST_TEST_DT, TEST_BEGIN_TEST_PERIOD, TEST_END_TEST_PERIOD, TEST_COUPON_COST,
                TEST_LAST_COUPON_DT, TEST_SHOW_TEST_WINDOW, TEST_UNPROCTORED_EXAM,
                TEST_RE_POINTS_ONTIME);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_TIMEOUT, obj.timeout, "Invalid timeout value after constructor");
        assertEquals(TEST_RE_MASTERY_SCORE, obj.reMasteryScore, "Invalid re_mastery_score value after constructor");
        assertEquals(TEST_UE_MASTERY_SCORE, obj.ueMasteryScore, "Invalid ue_mastery_score value after constructor");
        assertEquals(TEST_HW_MASTERY_SCORE, obj.hwMasteryScore, "Invalid hw_mastery_score value after constructor");
        assertEquals(TEST_HW_MOVEON_SCORE, obj.hwMoveonScore, "Invalid hw_moveon_score value after constructor");
        assertEquals(TEST_NBR_ATMPTS_ALLOW, obj.nbrAtmptsAllow, "Invalid nbr_atmpts_allow value after constructor");
        assertEquals(TEST_ATMPTS_PER_REVIEW, obj.atmptsPerReview, "Invalid atmpts_per_review value after constructor");
        assertEquals(TEST_FIRST_TEST_DT, obj.firstTestDt, "Invalid first_test_dt value after constructor");
        assertEquals(TEST_LAST_TEST_DT, obj.lastTestDt, "Invalid last_test_dt value after constructor");
        assertEquals(TEST_BEGIN_TEST_PERIOD, obj.beginTestPeriod, "Invalid begin_test_period value after constructor");
        assertEquals(TEST_END_TEST_PERIOD, obj.endTestPeriod, "Invalid end_test_period value after constructor");
        assertEquals(TEST_COUPON_COST, obj.couponCost, "Invalid coupon_cost value after constructor");
        assertEquals(TEST_LAST_COUPON_DT, obj.lastCouponDt, "Invalid last_coupon_dt value after constructor");
        assertEquals(TEST_SHOW_TEST_WINDOW, obj.showTestWindow, "Invalid show_test_window value after constructor");
        assertEquals(TEST_UNPROCTORED_EXAM, obj.unproctoredExam, "Invalid unproctored_exam value after constructor");
        assertEquals(TEST_RE_POINTS_ONTIME, obj.rePointsOntime, "Invalid re_points_ontime value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawCusection obj = new RawCusection(TEST_TERM, TEST_COURSE, TEST_SECT, TEST_UNIT,
                TEST_TIMEOUT, TEST_RE_MASTERY_SCORE, TEST_UE_MASTERY_SCORE, TEST_HW_MASTERY_SCORE,
                TEST_HW_MOVEON_SCORE, TEST_NBR_ATMPTS_ALLOW, TEST_ATMPTS_PER_REVIEW, TEST_FIRST_TEST_DT,
                TEST_LAST_TEST_DT, TEST_BEGIN_TEST_PERIOD, TEST_END_TEST_PERIOD, TEST_COUPON_COST,
                TEST_LAST_COUPON_DT, TEST_SHOW_TEST_WINDOW, TEST_UNPROCTORED_EXAM,
                TEST_RE_POINTS_ONTIME);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
