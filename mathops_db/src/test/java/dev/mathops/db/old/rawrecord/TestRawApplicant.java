package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawApplicant} class.
 */
final class TestRawApplicant {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_FIRST_NAME = "Steve";

    /** A field name. */
    private static final String TEST_LAST_NAME = "Benoit";

    /** A field name. */
    private static final LocalDate TEST_BIRTHDATE = LocalDate.of(2000, 12, 11);

    /** A field name. */
    private static final String TEST_ETHNICITY = "C";

    /** A field name. */
    private static final String TEST_GENDER = "D";

    /** A field name. */
    private static final String TEST_COLLEGE = "NS";

    /** A field name. */
    private static final String TEST_PROG_STUDY = "MATHPHD";

    /** A field name. */
    private static final String TEST_HS_CODE = "H123";

    /** A field name. */
    private static final String TEST_TR_CREDITS = "3";

    /** A field name. */
    private static final String TEST_RESIDENT = "B";

    /** A field name. */
    private static final String TEST_RESIDENT_STATE = "CO";

    /** A field name. */
    private static final String TEST_RESIDENT_COUNTY = "Larimer";

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
    private static final Integer TEST_PIDM = Integer.valueOf(9876);

    /** A field name. */
    private static final TermKey TEST_APLN_TERM = new TermKey(ETermName.FALL, 2002);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER1 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER2 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            "first_name=Steve",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER3 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "last_name=Benoit",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER4 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "birthdate=2000-12-11",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER5 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "ethnicity=C",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER6 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "gender=D",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER7 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "college=NS",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER8 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "prog_study=MATHPHD",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER9 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "hs_code=H123",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER10 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "tr_credits=3",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER11 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "resident=B",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER12 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "resident_state=CO",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER13 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "resident_county=Larimer",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER14 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "hs_gpa=3.9",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER15 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "hs_class_rank=19",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER16 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "hs_size_class=288",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER17 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "act_score=25",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER18 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "sat_score=720",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER19 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "pidm=9876",
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER20 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "apln_term=Fall, 2002");

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "first_name=Steve",
            "last_name=Benoit",
            "birthdate=2000-12-11",
            "ethnicity=C",
            "gender=D",
            "college=NS",
            "prog_study=MATHPHD",
            "hs_code=H123",
            "tr_credits=3",
            "resident=B",
            "resident_state=CO",
            "resident_county=Larimer",
            "hs_gpa=3.9",
            "hs_class_rank=19",
            "hs_size_class=288",
            "act_score=25",
            "sat_score=720",
            "pidm=9876",
            "apln_term=Fall, 2002");

    /**
     * Constructs a new {@code IvtRawApplicant}.
     */
    TestRawApplicant() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawApplicant obj = new RawApplicant(TEST_STU_ID, TEST_FIRST_NAME, TEST_LAST_NAME,
                TEST_BIRTHDATE, TEST_ETHNICITY, TEST_GENDER, TEST_COLLEGE, TEST_PROG_STUDY,
                TEST_HS_CODE, TEST_TR_CREDITS, TEST_RESIDENT, TEST_RESIDENT_STATE, TEST_RESIDENT_COUNTY,
                TEST_HS_GPA, TEST_HS_CLASS_RANK, TEST_HS_SIZE_CLASS, TEST_ACT_SCORE, TEST_SAT_SCORE,
                TEST_PIDM, TEST_APLN_TERM);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid std_id value after constructor");
        assertEquals(TEST_FIRST_NAME, obj.firstName, "Invalid first_name value after constructor");
        assertEquals(TEST_LAST_NAME, obj.lastName, "Invalid last_name value after constructor");
        assertEquals(TEST_BIRTHDATE, obj.birthdate, "Invalid birthdate value after constructor");
        assertEquals(TEST_ETHNICITY, obj.ethnicity, "Invalid ethnicity value after constructor");
        assertEquals(TEST_GENDER, obj.gender, "Invalid gender value after constructor");
        assertEquals(TEST_COLLEGE, obj.college, "Invalid college value after constructor");
        assertEquals(TEST_PROG_STUDY, obj.progStudy, "Invalid program_code value after constructor");
        assertEquals(TEST_HS_CODE, obj.hsCode, "Invalid hs_code value after constructor");
        assertEquals(TEST_TR_CREDITS, obj.trCredits, "Invalid tr_credits value after constructor");
        assertEquals(TEST_RESIDENT, obj.resident, "Invalid resident value after constructor");
        assertEquals(TEST_RESIDENT_STATE, obj.residentState, "Invalid resident_state value after constructor");
        assertEquals(TEST_RESIDENT_COUNTY, obj.residentCounty, "Invalid resident_county value after constructor");
        assertEquals(TEST_HS_GPA, obj.hsGpa, "Invalid hs_gpa value after constructor");
        assertEquals(TEST_HS_CLASS_RANK, obj.hsClassRank, "Invalid hs_class_rank value after constructor");
        assertEquals(TEST_HS_SIZE_CLASS, obj.hsSizeClass, "Invalid hs_size_class value after constructor");
        assertEquals(TEST_ACT_SCORE, obj.actScore, "Invalid act_score value after constructor");
        assertEquals(TEST_SAT_SCORE, obj.satScore, "Invalid sat_score value after constructor");
        assertEquals(TEST_PIDM, obj.pidm, "Invalid pidm value after constructor");
        assertEquals(TEST_APLN_TERM, obj.aplnTerm, "Invalid apln_term value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0002() {

        final RawApplicant obj = new RawApplicant(TEST_STU_ID, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER1, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0004() {

        final RawApplicant obj = new RawApplicant(null, TEST_FIRST_NAME, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER2, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0006() {

        final RawApplicant obj = new RawApplicant(null, null, TEST_LAST_NAME, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER3, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0008() {

        final RawApplicant obj = new RawApplicant(null, null, null, TEST_BIRTHDATE, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER4, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0010() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, TEST_ETHNICITY, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER5, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0012() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, TEST_GENDER, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER6, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0014() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, TEST_COLLEGE,
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER7, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0016() {

        final RawApplicant obj =
                new RawApplicant(null, null, null, null, null, null, null, TEST_PROG_STUDY, null, null,
                        null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER8, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0018() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                TEST_HS_CODE, null, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER9, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0020() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, TEST_TR_CREDITS, null, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER10, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0022() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, TEST_RESIDENT, null, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER11, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0024() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, TEST_RESIDENT_STATE, null, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER12, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0026() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, TEST_RESIDENT_COUNTY, null, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER13, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0028() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, null, TEST_HS_GPA, null, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER14, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0030() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, TEST_HS_CLASS_RANK, null, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER15, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0032() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, TEST_HS_SIZE_CLASS, null, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER16, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0034() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, TEST_ACT_SCORE, null, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER17, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0036() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, TEST_SAT_SCORE, null, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER18, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0038() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, TEST_PIDM, null);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER19, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0040() {

        final RawApplicant obj = new RawApplicant(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, TEST_APLN_TERM);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER20, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawApplicant obj = new RawApplicant(TEST_STU_ID, TEST_FIRST_NAME, TEST_LAST_NAME,
                TEST_BIRTHDATE, TEST_ETHNICITY, TEST_GENDER, TEST_COLLEGE, TEST_PROG_STUDY,
                TEST_HS_CODE, TEST_TR_CREDITS, TEST_RESIDENT, TEST_RESIDENT_STATE, TEST_RESIDENT_COUNTY,
                TEST_HS_GPA, TEST_HS_CLASS_RANK, TEST_HS_SIZE_CLASS, TEST_ACT_SCORE, TEST_SAT_SCORE,
                TEST_PIDM, TEST_APLN_TERM);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
