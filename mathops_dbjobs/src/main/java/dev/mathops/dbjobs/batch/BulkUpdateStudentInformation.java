package dev.mathops.dbjobs.batch;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.DbUtils;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class that scans the student table, and for each student found, queries the ODS for the student's name,
 * preferred name, college, department, and program of study. If found, the name is updated to the mixed-case name from
 * ODS rather than the all-caps name we have had historically, and other fields are updated accordingly.
 */
final class BulkUpdateStudentInformation {

    /** When true, does not update database - just logs what would be updated. */
    private static final boolean DEBUG = true;

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The primary database context. */
    private final DbContext primaryCtx;

    /** The ODS data database context. */
    private final DbContext odsCtx;

    /**
     * Constructs a new {@code StudentNamesToMixedCase}.
     */
    private BulkUpdateStudentInformation() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        this.odsCtx = this.dbProfile.getDbContext(ESchemaUse.ODS);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        if (this.dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else if (this.primaryCtx == null) {
            Log.warning("Unable to create PRIMARY database context.");
        } else if (this.odsCtx == null) {
            Log.warning("Unable to create LIVE database context.");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    final DbConnection odsConn = this.odsCtx.checkOutConnection();
                    try {
                        exec(cache, odsConn);
                    } finally {
                        this.odsCtx.checkInConnection(odsConn);
                    }

                } catch (final SQLException ex) {
                    Log.warning("Failed to connect to LIVE database.", ex);
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to connect to PRIMARY database.", ex);
            }
        }
    }

    /**
     * Executes logic once database connections have been established.
     *
     * @param cache   the data cache
     * @param odsConn the connection to the ODS database
     * @throws SQLException if there is an error accessing either database
     */
    private static void exec(final Cache cache, final DbConnection odsConn) throws SQLException {

        final List<RawStudent> allStudents = RawStudentLogic.INSTANCE.queryAll(cache);
        final int numStudents = allStudents.size();

        if (numStudents > 0) {
            Log.info("Loaded " + numStudents + " students");

            final Map<String, OdsPersonData> personData = queryAllPersons(odsConn);

//            int onStudent = 0;
//            for (final RawStudent student : allStudents) {
//                ++onStudent;
//                if ((onStudent % 100) == 0) {
//                    Log.fine("-> Processing student " + onStudent + " out of " + numStudents);
//                }
//
//                if ("888888888".equals(student.stuId)) {
//                    continue;
//                }
//
//                try {
//                    processStudent(cache, odsConn, student);
//                } catch (final SQLException ex) {
//                    Log.warning("Failed to query student from LIVE database.", ex);
//                }
//            }
        }
    }

    /**
     * Queries for all person records in the ODS.
     *
     * @param odsConn the connection to the ODS database
     * @return a map from CSU ID number to the person data record
     * @throws SQLException if there is an error accessing either database
     */
    private static Map<String, OdsPersonData> queryAllPersons(final DbConnection odsConn) throws SQLException {

//        Table CSUBAN.CSUG_GP_ADMISSIONS:
//        CSU_ID                        : VARCHAR2(63)
//        PIDM                          : NUMBER
//        LEGAL_NAME                    : VARCHAR2(500)
//        UPPER_NAME                    : VARCHAR2(60)
//        FIRST_NAME                    : VARCHAR2(63)
//        MIDDLE_NAME                   : VARCHAR2(63)
//        LAST_NAME                     : VARCHAR2(63)
//        NAME_SUFFIX                   : VARCHAR2(20)
//        PREFERRED_FIRST_NAME          : VARCHAR2(63)
//        PREFERRED_LAST_NAME           : VARCHAR2(60)
//        EMAIL                         : VARCHAR2(255)
//        ADDR_1                        : VARCHAR2(255)
//        ADDR_2                        : VARCHAR2(255)
//        ADDR_3                        : VARCHAR2(255)
//        BIRTH_DATE                    : DATE
//        AGE                           : NUMBER
//        CITY                          : VARCHAR2(63)
//        COUNTY                        : VARCHAR2(63)
//        COUNTY_DESC                   : VARCHAR2(255)
//        STATE                         : VARCHAR2(63)
//        STATE_DESC                    : VARCHAR2(255)
//        NATION                        : VARCHAR2(63)
//        NATION_DESC                   : VARCHAR2(255)
//        ZIP                           : VARCHAR2(63)
//        TELEPHONE                     : VARCHAR2(63)
//        CONFIDENTIALITY_IND           : VARCHAR2(1)
//        GENDER                        : VARCHAR2(63)
//        ETHNIC                        : VARCHAR2(63)
//        ETHNIC_DESC                   : VARCHAR2(255)
//        LOST_ID_DIGIT                 : VARCHAR2(300)
//        TRAN_LAST_COLL_ADM            : VARCHAR2(4000)
//        TRAN_LAST_COLL_DESC_ADM       : VARCHAR2(255)
//        TRAN_NUM_PRIOR_ADM            : NUMBER
//        TRAN_CUMCRDT_ADM              : NUMBER
//        TRAN_CUMGPA_ADM               : NUMBER
//        TRAN_INPROG_CREDIT_ADM        : NUMBER
//        SATR_READ                     : NUMBER
//        SATR_MATH                     : NUMBER
//        SATR_COMB                     : NUMBER
//        SATR_ESSAY                    : NUMBER
//        ACT_ENG                       : NUMBER
//        ACT_MATH                      : NUMBER
//        ACT_READ                      : NUMBER
//        ACT_SCI                       : NUMBER
//        ACT_COMP                      : NUMBER
//        ACT_WRIT                      : NUMBER
//        SAT_READ                      : NUMBER
//        SAT_MATH                      : NUMBER
//        SAT_COMB                      : NUMBER
//        SAT_WRIT                      : NUMBER
//        GED_SCORE                     : NUMBER
//        ILTS_SCORE                    : NUMBER
//        TOFL_PAPER                    : NUMBER
//        TOFL_COMPUTER                 : NUMBER
//        TOFL_INTERNET                 : NUMBER
//        GRE_TEST_DATE                 : DATE
//        GRE_VERB_SCORE                : NUMBER
//        GRE_VERB_PCT                  : NUMBER
//        GRE_QUAN_SCORE                : NUMBER
//        GRE_QUAN_PCT                  : NUMBER
//        GRE_ANALYTICAL_SCORE          : NUMBER
//        GRE_ANALYTICAL_PCT            : NUMBER
//        GRE_WRITING_SCORE             : NUMBER
//        GRE_WRITING_PCT               : NUMBER
//        GRE_ANALY_WRIT_SCORE          : NUMBER
//        HS_GPA                        : NUMBER
//        HS_CLASS_RANK                 : NUMBER
//        HS_CLASS_SIZE                 : NUMBER
//        HS_RANK_PCTILE                : NUMBER
//        HS_CODE                       : VARCHAR2(6)
//        HS_DESC                       : VARCHAR2(30)
//        HS_GRAD_DATE                  : DATE
//        HS_TRAN_RECV_DATE             : DATE
//        HS_DIPLOMA                    : VARCHAR2(2)
//        RESIDENCY_COUNTY              : VARCHAR2(5)
//        RESIDENCY_STATE               : VARCHAR2(3)
//        RESIDENCY_NATION              : VARCHAR2(5)
//        INTERNATIONAL_FLAG            : VARCHAR2(4000)
//        CITIZENSHIP_COUNTRY_CODE      : VARCHAR2(4000)
//        CITIZENSHIP_COUNTRY_DESC      : VARCHAR2(4000)
//        HISPANIC_LATINO_ETHNICITY_IND : VARCHAR2(1)
//        AMERICAN_INDIAN_RACE_IND      : VARCHAR2(1)
//        ASIAN_RACE_IND                : VARCHAR2(1)
//        BLACK_RACE_IND                : VARCHAR2(1)
//        HAWAIIAN_RACE_IND             : VARCHAR2(1)
//        WHITE_RACE_IND                : VARCHAR2(1)
//        MULTI_RACE_IND                : VARCHAR2(1)
//        MULTI_SOURCE                  : VARCHAR2(6)
//        MULTI_SOURCE_DESC             : VARCHAR2(30)

        final Map<String, OdsPersonData> result = new HashMap<>(1_800_000);

        final String sql = SimpleBuilder.concat(
                "SELECT CSU_ID, PIDM, FIRST_NAME, MIDDLE_NAME, LAST_NAME, PREFERRED_FIRST_NAME, EMAIL, BIRTH_DATE, ",
                "SATR_MATH, SAT_MATH, ACT_MATH, HS_GPA hs_gpa, HS_CODE, HS_CLASS_SIZE, HS_CLASS_RANK ",
                "FROM CSUBAN.CSUG_GP_ADMISSIONS");

        int count = 0;
        try (final Statement stmt = odsConn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String csuId = rs.getString("CSU_ID");
                    final Integer pidm = DbUtils.getInteger(rs, "PIDM");
                    final String firstName = rs.getString("FIRST_NAME");
                    final String middleName = rs.getString("MIDDLE_NAME");
                    final String lastName = rs.getString("LAST_NAME");
                    final String prefName = rs.getString("PREFERRED_FIRST_NAME");
                    final String email = rs.getString("EMAIL");
                    final String birthDate = rs.getString("BIRTH_DATE");
                    final Integer satr = DbUtils.getInteger(rs, "SATR_MATH");
                    final Integer sat = DbUtils.getInteger(rs, "SAT_MATH");
                    final Integer act = DbUtils.getInteger(rs, "ACT_MATH");
                    final String hsGpa = rs.getString("HS_GPA");
                    final String hsCode = rs.getString("HS_CODE");
                    final Integer hsClassSize = DbUtils.getInteger(rs, "HS_CLASS_SIZE");
                    final Integer hsClassRank = DbUtils.getInteger(rs, "HS_CLASS_RANK");

                    try {
                        // Birth data format: 1990-06-29 00:00:00
                        LocalDate bdate = null;
                        if (birthDate != null && birthDate.length() >= 10) {
                            final String yearStr = birthDate.substring(0, 4);
                            final String monthStr = birthDate.substring(5, 7);
                            final String dayStr = birthDate.substring(8, 10);

                            final int year = Integer.parseInt(yearStr);
                            final int month = Integer.parseInt(monthStr);
                            final int day = Integer.parseInt(dayStr);
                            bdate = LocalDate.of(year, month, day);
                        }

                        final String middleInitial = middleName == null || middleName.isBlank() ? null :
                                middleName.trim().substring(0, 1);

                        final OdsPersonData rec = new OdsPersonData(csuId, pidm, firstName, middleInitial, lastName,
                                prefName, email, bdate, satr, sat, act, hsGpa, hsCode, hsClassSize, hsClassRank);

                        if (result.containsKey(csuId)) {
                            Log.warning(" *** Duplicate ODS record for student ", csuId);
                        }
                        result.put(csuId, rec);
                        ++count;

                    } catch (final NumberFormatException | DateTimeException ex) {
                        Log.warning("Unable to parse fields from ODS for student ", csuId, ex);
                    }
                }
            }
        }

        Log.info("Loaded " + count + " ODS students");

        return result;
    }

    /**
     * Processes a single student.
     *
     * @param cache   the data cache
     * @param odsConn the connection to the ODS database
     * @param student the student record
     * @throws SQLException if there is an error accessing either database
     */
    private static void processStudent(final Cache cache, final DbConnection odsConn, final RawStudent student)
            throws SQLException {

//        Table CSUBAN.CSUG_GP_DEMO:
//        CSU_ID                        : VARCHAR2(63)
//        PIDM                          : NUMBER
//        LEGAL_NAME                    : VARCHAR2(500)
//        UPPER_NAME                    : VARCHAR2(255)
//        FIRST_NAME                    : VARCHAR2(63)
//        MIDDLE_NAME                   : VARCHAR2(63)
//        LAST_NAME                     : VARCHAR2(63)
//        NAME_SUFFIX                   : VARCHAR2(20)
//        PREFERRED_FIRST_NAME          : VARCHAR2(63)
//        PREFERRED_LAST_NAME           : VARCHAR2(60)
//        EMAIL                         : VARCHAR2(90)
//        ADDR_1                        : VARCHAR2(255)
//        ADDR_2                        : VARCHAR2(255)
//        ADDR_3                        : VARCHAR2(255)
//        BIRTH_DATE                    : DATE
//        AGE                           : NUMBER
//        CITY                          : VARCHAR2(63)
//        COUNTY                        : VARCHAR2(63)
//        COUNTY_DESC                   : VARCHAR2(255)
//        STATE                         : VARCHAR2(63)
//        STATE_DESC                    : VARCHAR2(255)
//        NATION                        : VARCHAR2(63)
//        NATION_DESC                   : VARCHAR2(255)
//        ZIP                           : VARCHAR2(30)
//        TELEPHONE                     : VARCHAR2(30)
//        CONFIDENTIALITY_IND           : VARCHAR2(1)
//        GENDER                        : VARCHAR2(63)
//        ETHNIC                        : VARCHAR2(63)
//        ETHNIC_DESC                   : VARCHAR2(255)
//        LOST_ID_DIGIT                 : VARCHAR2(300)
//        INTERNATIONAL_FLAG            : VARCHAR2(4000)
//        CITIZENSHIP_COUNTRY_CODE      : VARCHAR2(4000)
//        CITIZENSHIP_COUNTRY_DESC      : VARCHAR2(4000)
//        HISPANIC_LATINO_ETHNICITY_IND : VARCHAR2(1)
//        AMERICAN_INDIAN_RACE_IND      : VARCHAR2(1)
//        ASIAN_RACE_IND                : VARCHAR2(1)
//        BLACK_RACE_IND                : VARCHAR2(1)
//        HAWAIIAN_RACE_IND             : VARCHAR2(1)
//        WHITE_RACE_IND                : VARCHAR2(1)
//        MULTI_RACE_IND                : VARCHAR2(1)
//        MULTI_SOURCE                  : VARCHAR2(6)
//        MULTI_SOURCE_DESC             : VARCHAR2(30)

        String odsPidm = null;
        String odsFirstName = null;
        String odsLastName = null;
        String odsMiddleName = null;
        String odsPrefName = null;
        String odsEmail = null;
        String odsBirthDate = null;

        final String sql1 = SimpleBuilder.concat(
                "SELECT A.CSU_ID csuid, A.PIDM pidm, A.FIRST_NAME first, A.MIDDLE_NAME middle, A.LAST_NAME last, ",
                "A.PREFERRED_FIRST_NAME pref, A.EMAIL email, A.BIRTH_DATE birth ",
                "FROM CSUBAN.CSUG_GP_DEMO A WHERE A.CSU_ID = '", student.stuId, "'");

        try (final Statement stmt = odsConn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql1)) {
                while (rs.next()) {
                    odsPidm = rs.getString("pidm");
                    odsFirstName = rs.getString("first");
                    odsMiddleName = rs.getString("middle");
                    odsLastName = rs.getString("last");
                    odsPrefName = rs.getString("pref");
                    odsEmail = rs.getString("email");
                    odsBirthDate = rs.getString("birth");
                }
            }
        }

//        Table CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH:
//        PIDM                          : NUMBER
//        CSU_ID                        : VARCHAR2(63)
//        NAME                          : VARCHAR2(255)
//        TERM                          : VARCHAR2(63)
//        TERM_DESC                     : VARCHAR2(255)
//        YEAR                          : VARCHAR2(63)
//        ACADEMIC_STANDING             : VARCHAR2(63)
//        ACADEMIC_STANDING_DESC        : VARCHAR2(255)
//        ADM_INDEX                     : NUMBER
//        ANTICIPATED_GRAD_ACAD_YR      : VARCHAR2(63)
//        ANTICIPATED_GRAD_ACAD_YR_DESC : VARCHAR2(255)
//        ANTICIPATED_GRAD_DATE         : DATE
//        ANTICIPATED_GRAD_TERM         : VARCHAR2(63)
//        CONTINUOUS_REG                : VARCHAR2(1)
//        CREDITS_CE                    : NUMBER
//        CREDITS_RI                    : NUMBER
//        CREDITS_NON_CSU               : NUMBER
//        CREDITS_SI                    : NUMBER
//        CREDITS_OTHER                 : NUMBER
//        CREDITS_TOTAL                 : NUMBER
//        GPA                           : NUMBER
//        GPA_ATTEMPTED_CREDITS         : NUMBER
//        GPA_CREDITS                   : NUMBER
//        GPA_EARNED_CREDITS            : NUMBER
//        GPA_PASSED_CREDITS            : NUMBER
//        DEANS_LIST                    : VARCHAR2(63)
//        DEANS_LIST_DESC               : VARCHAR2(255)
//        CREDIT_LOAD                   : VARCHAR2(63)
//        CAMPUS                        : VARCHAR2(63)
//        CAMPUS_DESC                   : VARCHAR2(255)
//        PRIMARY_COLLEGE               : VARCHAR2(63)
//        PRIMARY_COLLEGE_DESC          : VARCHAR2(255)
//        PRIMARY_DEPARTMENT            : VARCHAR2(63)
//        PRIMARY_DEPARTMENT_DESC       : VARCHAR2(255)
//        PRIMARY_MAJOR                 : VARCHAR2(63)
//        PRIMARY_MAJOR_DESC            : VARCHAR2(255)
//        PROGRAM_OF_STUDY              : VARCHAR2(63)
//        PROGRAM_OF_STUDY_DESC         : VARCHAR2(255)
//        RESIDENCY                     : VARCHAR2(63)
//        RESIDENCY_DESC                : VARCHAR2(255)
//        RESIDENCY_INDICATOR           : VARCHAR2(1)
//        STUDENT_CLASS                 : VARCHAR2(63)
//        STUDENT_CLASS_DESC            : VARCHAR2(255)
//        STUDENT_LEVEL                 : VARCHAR2(63)
//        STUDENT_LEVEL_DESC            : VARCHAR2(255)
//        STUDENT_TYPE                  : VARCHAR2(63)
//        STUDENT_TYPE_DESC             : VARCHAR2(255)
//        TERM_DEGREE                   : VARCHAR2(63)
//        TERM_DEGREE_DESC              : VARCHAR2(255)
//        ADMISSIONS_POPULATION         : VARCHAR2(63)
//        ADMISSIONS_POPULATION_DESC    : VARCHAR2(255)
//        SITE                          : VARCHAR2(63)
//        SITE_DESC                     : VARCHAR2(255)
//        PROGRAM_SITE                  : VARCHAR2(127)
//        MULTI_SOURCE                  : VARCHAR2(6)
//        MULTI_SOURCE_DESC             : VARCHAR2(30)
//        EXTRACT_DATE                  : DATE

        int maxTerm = 0;
        String odsCollege = null;
        String odsDept = null;
        String odsProgram = null;
        String odsClass = null;
        String odsGradTerm = null;
        String odsResidency = null;
        String odsCampus = null;

        final String sql2 = SimpleBuilder.concat("SELECT A.TERM term, A.PRIMARY_COLLEGE college, ",
                "A.PRIMARY_DEPARTMENT dept, A.PROGRAM_OF_STUDY program, A.STUDENT_CLASS cls, ",
                "A.ANTICIPATED_GRAD_TERM gradTerm, A.RESIDENCY residency, A.CAMPUS campus ",
                "FROM CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH A WHERE A.CSU_ID = '", student.stuId, "'");

        try (final Statement stmt = odsConn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql2)) {
                while (rs.next()) {
                    final String termStr = rs.getString("term");

                    if (termStr != null) {
                        try {
                            int termValue = Integer.parseInt(termStr);

                            if (termValue > maxTerm) {
                                maxTerm = termValue;
                                odsCollege = rs.getString("college");
                                odsDept = rs.getString("dept");
                                odsProgram = rs.getString("program");
                                odsClass = rs.getString("cls");
                                odsGradTerm = rs.getString("gradTerm");
                                odsResidency = rs.getString("residency");
                                odsCampus = rs.getString("campus");
                            }
                        } catch (final NumberFormatException ex) {
                            Log.warning("Unable to parse term string [", termStr, "]", ex);
                        }
                    }
                }
            }
        }

        if (maxTerm > 0) {

            if (odsPidm == null) {
                Log.warning("PIDM from ODS was null for student ", student.stuId);
            } else if (odsFirstName == null) {
                Log.warning("First name from ODS was null for student ", student.stuId);
            } else if (odsLastName == null) {
                Log.warning("Last name from ODS was null for student ", student.stuId);
            } else {
                try {
                    final Integer pidmValue = Integer.valueOf(odsPidm);
                    final String initial = odsMiddleName == null || odsMiddleName.isBlank() ? null :
                            odsMiddleName.trim().substring(0, 1);

                    final boolean match = pidmValue.equals(student.pidm)
                            && odsFirstName.equals(student.firstName)
                            && odsLastName.equals(student.lastName)
                            && Objects.equals(initial, student.middleInitial)
                            && Objects.equals(odsPrefName, student.prefName)
                            && Objects.equals(odsEmail, student.stuEmail)
                            && Objects.equals(odsCollege, student.college)
                            && Objects.equals(odsDept, student.dept)
                            && Objects.equals(odsProgram, student.programCode);

                    if (!match) {
                        Log.info("Need to update ", student.stuId);
                    }
                } catch (final NumberFormatException ex) {
                    Log.warning("Unable to parse PIDM from ODS [", odsPidm, "] for student ", student.stuId, ex);
                }
            }
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final BulkUpdateStudentInformation job = new BulkUpdateStudentInformation();

        job.execute();
    }

    /**
     * A container for data from the ODS about a single person
     */
    private record OdsPersonData(String csuId, Integer pidm, String firstName, String middleInitial, String lastName,
                                 String prefName, String email, LocalDate birthDate, Integer satR, Integer sat,
                                 Integer act, String hsGpa, String hsCode, Integer hsClassSize, Integer hsClassRank) {
    }

    /**
     * A container for data from the ODS about a single person
     */
    private record OdsTermData(String csuId, Integer term, Integer expectGradTerm,


                               Integer pidm, String firstName, String middleInitial, String lastName,
                                 String prefName, String email, LocalDate birthDate, Integer satR, Integer sat,
                                 Integer act, String hsGpa, String hsCode, Integer hsClassSize, Integer hsClassRank) {
    }
}
