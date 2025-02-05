package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawPacingStructure} class.
 */
final class TestRawPacingStructure {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_PACING_STRUCTURE = "I";

    /** A field name. */
    private static final String TEST_DEF_PACE_TRACK = "A";

    /** A field name. */
    private static final String TEST_REQUIRE_LICENSED = "a";

    /** A field name. */
    private static final String TEST_REQUIRE_PARTIC = "b";

    /** A field name. */
    private static final Integer TEST_MAX_PARTIC_MISSED = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_ALLOW_INC = "Y";

    /** A field name. */
    private static final Integer TEST_MAX_COURSES = Integer.valueOf(5);

    /** A field name. */
    private static final Integer TEST_NBR_OPEN_ALLOWED = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_REQUIRE_UNIT_EXAMS = "c";

    /** A field name. */
    private static final String TEST_USE_MIDTERMS = "d";

    /** A field name. */
    private static final String TEST_ALLOW_COUPONS = "e";

    /** A field name. */
    private static final String TEST_COUPONS_AFTER_WINDOW = "f";

    /** A field name. */
    private static final Integer TEST_USERS_PROGRESS_CR = Integer.valueOf(10);

    /** A field name. */
    private static final Integer TEST_HW_PROGRESS_CR = Integer.valueOf(11);

    /** A field name. */
    private static final Integer TEST_RE_PROGRESS_CR = Integer.valueOf(12);

    /** A field name. */
    private static final Integer TEST_UE_PROGRESS_CR = Integer.valueOf(13);

    /** A field name. */
    private static final Integer TEST_FIN_PROGRESS_CR = Integer.valueOf(14);

    /** A field name. */
    private static final String TEST_PACING_NAME = "Student Managed";

    /** A field name. */
    private static final String TEST_SCHEDULE_SOURCE = "Pace";

    /** A field name. */
    private static final String TEST_SR_DUE_DATE_ENFORCED = "g";

    /** A field name. */
    private static final String TEST_RE_DUE_DATE_ENFORCED = "h";

    /** A field name. */
    private static final String TEST_UE_DUE_DATE_ENFORCED = "i";

    /** A field name. */
    private static final String TEST_FE_DUE_DATE_ENFORCED = "j";

    /** A field name. */
    private static final String TEST_FIRST_OBJ_AVAIL = "k";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "pacing_structure=I",
            "def_pace_track=A",
            "require_licensed=a",
            "require_partic=b",
            "max_partic_missed=4",
            "allow_inc=Y",
            "max_courses=5",
            "nbr_open_allowed=1",
            "require_unit_exams=c",
            "use_midterms=d",
            "allow_coupons=e",
            "coupons_after_window=f",
            "users_progress_cr=10",
            "hw_progress_cr=11",
            "re_progress_cr=12",
            "ue_progress_cr=13",
            "fin_progress_cr=14",
            "pacing_name=Student Managed",
            "schedule_source=Pace",
            "sr_due_date_enforced=g",
            "re_due_date_enforced=h",
            "ue_due_date_enforced=i",
            "fe_due_date_enforced=j",
            "first_obj_avail=k","");

    /**
     * Constructs a new {@code IvtRawPacingStructure}.
     */
    TestRawPacingStructure() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawPacingStructure obj = new RawPacingStructure(TEST_TERM, TEST_PACING_STRUCTURE,
                TEST_DEF_PACE_TRACK, TEST_REQUIRE_LICENSED, TEST_REQUIRE_PARTIC, TEST_MAX_PARTIC_MISSED,
                TEST_ALLOW_INC, TEST_MAX_COURSES, TEST_NBR_OPEN_ALLOWED, TEST_REQUIRE_UNIT_EXAMS,
                TEST_USE_MIDTERMS, TEST_ALLOW_COUPONS, TEST_COUPONS_AFTER_WINDOW,
                TEST_USERS_PROGRESS_CR, TEST_HW_PROGRESS_CR, TEST_RE_PROGRESS_CR, TEST_UE_PROGRESS_CR,
                TEST_FIN_PROGRESS_CR, TEST_PACING_NAME, TEST_SCHEDULE_SOURCE, TEST_SR_DUE_DATE_ENFORCED,
                TEST_RE_DUE_DATE_ENFORCED, TEST_UE_DUE_DATE_ENFORCED, TEST_FE_DUE_DATE_ENFORCED,
                TEST_FIRST_OBJ_AVAIL, null);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_PACING_STRUCTURE, obj.pacingStructure, "Invalid pacing_structure value after constructor");
        assertEquals(TEST_DEF_PACE_TRACK, obj.defPaceTrack, "Invalid def_pace_track value after constructor");
        assertEquals(TEST_REQUIRE_LICENSED, obj.requireLicensed, "Invalid require_licensed value after constructor");
        assertEquals(TEST_REQUIRE_PARTIC, obj.requirePartic, "Invalid require_partic value after constructor");
        assertEquals(TEST_MAX_PARTIC_MISSED, obj.maxParticMissed, "Invalid max_partic_missed value after constructor");
        assertEquals(TEST_ALLOW_INC, obj.allowInc, "Invalid allow_inc value after constructor");
        assertEquals(TEST_MAX_COURSES, obj.maxCourses, "Invalid max_courses value after constructor");
        assertEquals(TEST_NBR_OPEN_ALLOWED, obj.nbrOpenAllowed, "Invalid nbr_open_allowed value after constructor");
        assertEquals(TEST_REQUIRE_UNIT_EXAMS, obj.requireUnitExams,
                "Invalid require_unit_exams value after constructor");
        assertEquals(TEST_USE_MIDTERMS, obj.useMidterms, "Invalid use_midterm value after constructor");
        assertEquals(TEST_ALLOW_COUPONS, obj.allowCoupons, "Invalid allow_coupons value after constructor");
        assertEquals(TEST_COUPONS_AFTER_WINDOW, obj.couponsAfterWindow,
                "Invalid coupons_after_window value after constructor");
        assertEquals(TEST_USERS_PROGRESS_CR, obj.usersProgressCr, "Invalid users_progress_cr value after constructor");
        assertEquals(TEST_HW_PROGRESS_CR, obj.hwProgressCr, "Invalid hw_progress_cr value after constructor");
        assertEquals(TEST_RE_PROGRESS_CR, obj.reProgressCr, "Invalid re_progress_cr value after constructor");
        assertEquals(TEST_UE_PROGRESS_CR, obj.ueProgressCr, "Invalid ue_progress_cr value after constructor");
        assertEquals(TEST_FIN_PROGRESS_CR, obj.finProgressCr, "Invalid fin_progress_Cr value after constructor");
        assertEquals(TEST_PACING_NAME, obj.pacingName, "Invalid pacing_name value after constructor");
        assertEquals(TEST_SCHEDULE_SOURCE, obj.scheduleSource, "Invalid schedule_source value after constructor");
        assertEquals(TEST_SR_DUE_DATE_ENFORCED, obj.srDueDateEnforced,
                "Invalid sr_due_date_enforced value after constructor");
        assertEquals(TEST_RE_DUE_DATE_ENFORCED, obj.reDueDateEnforced,
                "Invalid re_due_date_enforced value after constructor");
        assertEquals(TEST_UE_DUE_DATE_ENFORCED, obj.ueDueDateEnforced,
                "Invalid ue_due_date_enforced value after constructor");
        assertEquals(TEST_FE_DUE_DATE_ENFORCED, obj.feDueDateEnforced,
                "Invalid fe_due_date_enforced value after constructor");
        assertEquals(TEST_FIRST_OBJ_AVAIL, obj.firstObjAvail, "Invalid first_obj_avail value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawPacingStructure obj = new RawPacingStructure(TEST_TERM, TEST_PACING_STRUCTURE,
                TEST_DEF_PACE_TRACK, TEST_REQUIRE_LICENSED, TEST_REQUIRE_PARTIC, TEST_MAX_PARTIC_MISSED,
                TEST_ALLOW_INC, TEST_MAX_COURSES, TEST_NBR_OPEN_ALLOWED, TEST_REQUIRE_UNIT_EXAMS,
                TEST_USE_MIDTERMS, TEST_ALLOW_COUPONS, TEST_COUPONS_AFTER_WINDOW,
                TEST_USERS_PROGRESS_CR, TEST_HW_PROGRESS_CR, TEST_RE_PROGRESS_CR, TEST_UE_PROGRESS_CR,
                TEST_FIN_PROGRESS_CR, TEST_PACING_NAME, TEST_SCHEDULE_SOURCE, TEST_SR_DUE_DATE_ENFORCED,
                TEST_RE_DUE_DATE_ENFORCED, TEST_UE_DUE_DATE_ENFORCED, TEST_FE_DUE_DATE_ENFORCED,
                TEST_FIRST_OBJ_AVAIL, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
