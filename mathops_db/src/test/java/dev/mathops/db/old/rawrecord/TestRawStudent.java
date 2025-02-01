package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStudent} class.
 */
final class TestRawStudent {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final Integer TEST_PIDM = Integer.valueOf(9876);

    /** A field name. */
    private static final String TEST_LAST_NAME = "Benoit";

    /** A field name. */
    private static final String TEST_FIRST_NAME = "Steve";

    /** A field name. */
    private static final String TEST_PREF_NAME = "S";

    /** A field name. */
    private static final String TEST_MIDDLE_INITIAL = "R";

    /** A field name. */
    private static final TermKey TEST_APLN_TERM = new TermKey(ETermName.FALL, 2002);

    /** A field name. */
    private static final String TEST_CLASS = "FR";

    /** A field name. */
    private static final String TEST_COLLEGE = "NS";

    /** A field name. */
    private static final String TEST_DEPT = "MATH";

    /** A field name. */
    private static final String TEST_PROGRAM_CODE = "MATHPHD";

    /** A field name. */
    private static final String TEST_MINOR = "PSYCH";

    /** A field name. */
    private static final TermKey TEST_EST_GRADUATION = new TermKey(ETermName.SPRING, 2012);

    /** A field name. */
    private static final String TEST_TR_CREDITS = "3";

    /** A field name. */
    private static final String TEST_HS_CODE = "H123";

    /** A field name. */
    private static final String TEST_HS_GPA = "3.9";

    /** A field name. */
    private static final Integer TEST_HS_CLASS_RANK = Integer.valueOf(19);

    /** A field name. */
    private static final Integer TEST_HS_SIZE_CLASS = Integer.valueOf(288);

    /** A field name. */
    private static final Integer TEST_ACT_SCORE = Integer.valueOf(25);

    /** A field name. */
    private static final Integer TEST_SAT_SCORE = Integer.valueOf(720);

    /** A field name. */
    private static final String TEST_AP_SCORE = "A";

    /** A field name. */
    private static final String TEST_RESIDENT = "B";

    /** A field name. */
    private static final LocalDate TEST_BIRTHDATE = LocalDate.of(2000, 12, 11);

    /** A field name. */
    private static final String TEST_ETHNICITY = "C";

    /** A field name. */
    private static final String TEST_GENDER = "D";

    /** A field name. */
    private static final String TEST_DISCIP_HISTORY = "E";

    /** A field name. */
    private static final String TEST_DISCIP_STATUS = "F";

    /** A field name. */
    private static final String TEST_SEV_ADMIN_HOLD = "G";

    /** A field name. */
    private static final Float TEST_TIMELIMIT_FACTOR = Float.valueOf(1.5f);

    /** A field name. */
    private static final String TEST_LICENSED = "H";

    /** A field name. */
    private static final String TEST_CAMPUS = "I";

    /** A field name. */
    private static final String TEST_STU_EMAIL = "j@k.com";

    /** A field name. */
    private static final String TEST_ADVISER_EMAIL = "l@m.com";

    /** A field name. */
    private static final String TEST_PASSWORD = "N";

    /** A field name. */
    private static final String TEST_ADMIT_TYPE = "O";

    /** A field name. */
    private static final String TEST_ORDER_ENFORCE = "P";

    /** A field name. */
    private static final String TEST_PACING_STRUCTURE = "Q";

    /** A field name. */
    private static final LocalDate TEST_CREATE_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_EXTENSION_DAYS = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_CANVAS_ID = "ABC123";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "stu_id=888888888",
            "pidm=9876",
            "last_name=Benoit",
            "first_name=Steve",
            "pref_name=S",
            "middle_initial=R",
            "apln_term=Fall, 2002",
            "class=FR",
            "college=NS",
            "dept=MATH",
            "program_code=MATHPHD",
            "minor=PSYCH",
            "est_graduation=Spring, 2012",
            "tr_credits=3",
            "hs_code=H123",
            "hs_gpa=3.9",
            "hs_class_rank=19",
            "hs_size_class=288",
            "act_score=25",
            "sat_score=720",
            "ap_score=A",
            "resident=B",
            "birthdate=2000-12-11",
            "ethnicity=C",
            "gender=D",
            "discip_history=E",
            "discip_status=F",
            "sev_admin_hold=G",
            "timelimit_factor=1.5",
            "licensed=H",
            "campus=I",
            "stu_email=j@k.com",
            "adviser_email=l@m.com",
            "password=N",
            "admit_type=O",
            "order_enforce=P",
            "pacing_structure=Q",
            "create_dt=2023-01-02",
            "extension_days=2",
            "canvas_id=ABC123");

    /**
     * Constructs a new {@code IvtRawStudent}.
     */
    TestRawStudent() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStudent obj = new RawStudent(TEST_STU_ID, TEST_PIDM, TEST_LAST_NAME,
                TEST_FIRST_NAME, TEST_PREF_NAME, TEST_MIDDLE_INITIAL, TEST_APLN_TERM, TEST_CLASS,
                TEST_COLLEGE, TEST_DEPT, TEST_PROGRAM_CODE, TEST_MINOR, TEST_EST_GRADUATION,
                TEST_TR_CREDITS, TEST_HS_CODE, TEST_HS_GPA, TEST_HS_CLASS_RANK, TEST_HS_SIZE_CLASS,
                TEST_ACT_SCORE, TEST_SAT_SCORE, TEST_AP_SCORE, TEST_RESIDENT, TEST_BIRTHDATE,
                TEST_ETHNICITY, TEST_GENDER, TEST_DISCIP_HISTORY, TEST_DISCIP_STATUS,
                TEST_SEV_ADMIN_HOLD, TEST_TIMELIMIT_FACTOR, TEST_LICENSED, TEST_CAMPUS, TEST_STU_EMAIL,
                TEST_ADVISER_EMAIL, TEST_PASSWORD, TEST_ADMIT_TYPE, TEST_ORDER_ENFORCE,
                TEST_PACING_STRUCTURE, TEST_CREATE_DT, TEST_EXTENSION_DAYS, TEST_CANVAS_ID);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_PIDM, obj.pidm, "Invalid pidm value after constructor");
        assertEquals(TEST_LAST_NAME, obj.lastName, "Invalid last_name value after constructor");
        assertEquals(TEST_FIRST_NAME, obj.firstName, "Invalid first_name value after constructor");
        assertEquals(TEST_PREF_NAME, obj.prefName, "Invalid pref_name value after constructor");
        assertEquals(TEST_MIDDLE_INITIAL, obj.middleInitial, "Invalid middle_initial value after constructor");
        assertEquals(TEST_APLN_TERM, obj.aplnTerm, "Invalid apln_term value after constructor");
        assertEquals(TEST_CLASS, obj.clazz, "Invalid clazz value after constructor");
        assertEquals(TEST_COLLEGE, obj.college, "Invalid college value after constructor");
        assertEquals(TEST_DEPT, obj.dept, "Invalid dept value after constructor");
        assertEquals(TEST_PROGRAM_CODE, obj.programCode, "Invalid program_code value after constructor");
        assertEquals(TEST_MINOR, obj.minor, "Invalid minor value after constructor");
        assertEquals(TEST_EST_GRADUATION, obj.estGraduation, "Invalid est_graduation value after constructor");
        assertEquals(TEST_TR_CREDITS, obj.trCredits, "Invalid tr_credits value after constructor");
        assertEquals(TEST_HS_CODE, obj.hsCode, "Invalid hs_code value after constructor");
        assertEquals(TEST_HS_GPA, obj.hsGpa, "Invalid hs_gpa value after constructor");
        assertEquals(TEST_HS_CLASS_RANK, obj.hsClassRank, "Invalid hs_class_rank value after constructor");
        assertEquals(TEST_HS_SIZE_CLASS, obj.hsSizeClass, "Invalid hs_size_class value after constructor");
        assertEquals(TEST_ACT_SCORE, obj.actScore, "Invalid act_score value after constructor");
        assertEquals(TEST_SAT_SCORE, obj.satScore, "Invalid sat_score value after constructor");
        assertEquals(TEST_AP_SCORE, obj.apScore, "Invalid ap_score value after constructor");
        assertEquals(TEST_RESIDENT, obj.resident, "Invalid resident value after constructor");
        assertEquals(TEST_BIRTHDATE, obj.birthdate, "Invalid birthdate value after constructor");
        assertEquals(TEST_ETHNICITY, obj.ethnicity, "Invalid ethnicity value after constructor");
        assertEquals(TEST_GENDER, obj.gender, "Invalid gender value after constructor");
        assertEquals(TEST_DISCIP_HISTORY, obj.discipHistory, "Invalid discip_history value after constructor");
        assertEquals(TEST_DISCIP_STATUS, obj.discipStatus, "Invalid discip_status value after constructor");
        assertEquals(TEST_SEV_ADMIN_HOLD, obj.sevAdminHold, "Invalid sev_admin_hold value after constructor");
        assertEquals(TEST_TIMELIMIT_FACTOR, obj.timelimitFactor, "Invalid timelimit_factor value after constructor");
        assertEquals(TEST_LICENSED, obj.licensed, "Invalid licensed value after constructor");
        assertEquals(TEST_CAMPUS, obj.campus, "Invalid campus value after constructor");
        assertEquals(TEST_STU_EMAIL, obj.stuEmail, "Invalid stu_email value after constructor");
        assertEquals(TEST_ADVISER_EMAIL, obj.adviserEmail, "Invalid adviser_email value after constructor");
        assertEquals(TEST_PASSWORD, obj.password, "Invalid password value after constructor");
        assertEquals(TEST_ADMIT_TYPE, obj.admitType, "Invalid admit_type value after constructor");
        assertEquals(TEST_ORDER_ENFORCE, obj.orderEnforce, "Invalid order_enforce value after constructor");
        assertEquals(TEST_PACING_STRUCTURE, obj.pacingStructure, "Invalid pacing_structure value after constructor");
        assertEquals(TEST_CREATE_DT, obj.createDt, "Invalid create_dt value after constructor");
        assertEquals(TEST_EXTENSION_DAYS, obj.extensionDays, "Invalid extension_days value after constructor");
        assertEquals(TEST_CANVAS_ID, obj.canvasId, "Invalid canvas_id value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStudent obj = new RawStudent(TEST_STU_ID, TEST_PIDM, TEST_LAST_NAME,
                TEST_FIRST_NAME, TEST_PREF_NAME, TEST_MIDDLE_INITIAL, TEST_APLN_TERM, TEST_CLASS,
                TEST_COLLEGE, TEST_DEPT, TEST_PROGRAM_CODE, TEST_MINOR, TEST_EST_GRADUATION,
                TEST_TR_CREDITS, TEST_HS_CODE, TEST_HS_GPA, TEST_HS_CLASS_RANK, TEST_HS_SIZE_CLASS,
                TEST_ACT_SCORE, TEST_SAT_SCORE, TEST_AP_SCORE, TEST_RESIDENT, TEST_BIRTHDATE,
                TEST_ETHNICITY, TEST_GENDER, TEST_DISCIP_HISTORY, TEST_DISCIP_STATUS,
                TEST_SEV_ADMIN_HOLD, TEST_TIMELIMIT_FACTOR, TEST_LICENSED, TEST_CAMPUS, TEST_STU_EMAIL,
                TEST_ADVISER_EMAIL, TEST_PASSWORD, TEST_ADMIT_TYPE, TEST_ORDER_ENFORCE,
                TEST_PACING_STRUCTURE, TEST_CREATE_DT, TEST_EXTENSION_DAYS, TEST_CANVAS_ID);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
