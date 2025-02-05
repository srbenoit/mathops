package dev.mathops.dbjobs.batch;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.DbUtils;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.SimpleBuilder;

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
public final class BulkUpdateStudentInformation {

    /** When true, does not update database - just logs what would be updated. */
    private static final boolean DEBUG = false;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code BulkUpdateStudentInformation}.
     */
    public BulkUpdateStudentInformation() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     */
    public void execute() {

        if (this.profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(this.profile);
            final Login login = this.profile.getLogin(ESchema.ODS);

            try {
                final DbConnection odsConn = login.checkOutConnection();
                try {
                    exec(cache, odsConn);
                } finally {
                    login.checkInConnection(odsConn);
                }

            } catch (final SQLException ex) {
                Log.warning("Failed to connect to LIVE database.", ex);
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

        final List<RawStudent> allStudents = RawStudentLogic.queryAll(cache);
        final int numStudents = allStudents.size();

        if (numStudents > 0) {
            Log.info("Loaded " + numStudents + " students");

            final Map<String, OdsPersonData> personData = queryAllPersons(odsConn);
            final Map<String, OdsTermData> termData = queryAllTerms(odsConn);

            for (final RawStudent student : allStudents) {
                final String stuId = student.stuId;
                if ("888888888".equals(stuId)) {
                    continue;
                }

                try {
                    processStudent(cache, student, personData.get(stuId), termData.get(stuId));
                } catch (final SQLException ex) {
                    Log.warning("Failed to query student from LIVE database.", ex);
                }
            }
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
                "SATR_MATH, SAT_MATH, ACT_MATH, HS_GPA, HS_CODE, HS_CLASS_SIZE, HS_CLASS_RANK ",
                "FROM CSUBAN.CSUG_GP_ADMISSIONS WHERE MULTI_SOURCE='CSU'");

        try (final Statement stmt = odsConn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String csuId = rs.getString("CSU_ID");
                    final Integer pidm = DbUtils.getInteger(rs, "PIDM");
                    final String firstName = prune(rs.getString("FIRST_NAME"), 30);
                    final String middleName = prune(rs.getString("MIDDLE_NAME"), 1);
                    final String lastName = prune(rs.getString("LAST_NAME"), 30);
                    final String prefName = prune(rs.getString("PREFERRED_FIRST_NAME"), 30);
                    final String email = prune(rs.getString("EMAIL"), 60);
                    final String birthDate = rs.getString("BIRTH_DATE");
                    final Integer satr = DbUtils.getInteger(rs, "SATR_MATH");
                    final Integer sat = DbUtils.getInteger(rs, "SAT_MATH");
                    final Integer act = DbUtils.getInteger(rs, "ACT_MATH");
                    final String hsGpa = prune(rs.getString("HS_GPA"), 4);
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
                            Log.fine(result.get(csuId));
                            Log.fine(rec);
                        }
                        result.put(csuId, rec);

                    } catch (final NumberFormatException | DateTimeException ex) {
                        Log.warning("Unable to parse fields from ODS for student ", csuId, ex);
                    }
                }
            }
        }

        Log.info("Loaded " + result.size() + " ODS students");

        return result;
    }

    /**
     * Queries for all the most recent term record for all students in the ODS.
     *
     * @param odsConn the connection to the ODS database
     * @return a map from CSU ID number to the person data record
     * @throws SQLException if there is an error accessing either database
     */
    private static Map<String, OdsTermData> queryAllTerms(final DbConnection odsConn) throws SQLException {

//        Table CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH:
//         PIDM                          : NUMBER
//         CSU_ID                        : VARCHAR2(63)
//         NAME                          : VARCHAR2(255)
//         TERM                          : VARCHAR2(63)
//         TERM_DESC                     : VARCHAR2(255)
//         YEAR                          : VARCHAR2(63)
//         ACADEMIC_STANDING             : VARCHAR2(63)
//         ACADEMIC_STANDING_DESC        : VARCHAR2(255)
//         ADM_INDEX                     : NUMBER
//         ANTICIPATED_GRAD_ACAD_YR      : VARCHAR2(63)
//         ANTICIPATED_GRAD_ACAD_YR_DESC : VARCHAR2(255)
//         ANTICIPATED_GRAD_DATE         : DATE
//         ANTICIPATED_GRAD_TERM         : VARCHAR2(63)
//         CONTINUOUS_REG                : VARCHAR2(1)
//         CREDITS_CE                    : NUMBER
//         CREDITS_RI                    : NUMBER
//         CREDITS_NON_CSU               : NUMBER
//         CREDITS_SI                    : NUMBER
//         CREDITS_OTHER                 : NUMBER
//         CREDITS_TOTAL                 : NUMBER
//         GPA                           : NUMBER
//         GPA_ATTEMPTED_CREDITS         : NUMBER
//         GPA_CREDITS                   : NUMBER
//         GPA_EARNED_CREDITS            : NUMBER
//         GPA_PASSED_CREDITS            : NUMBER
//         DEANS_LIST                    : VARCHAR2(63)
//         DEANS_LIST_DESC               : VARCHAR2(255)
//         CREDIT_LOAD                   : VARCHAR2(63)
//         CAMPUS                        : VARCHAR2(63)
//         CAMPUS_DESC                   : VARCHAR2(255)
//         PRIMARY_COLLEGE               : VARCHAR2(63)
//         PRIMARY_COLLEGE_DESC          : VARCHAR2(255)
//         PRIMARY_DEPARTMENT            : VARCHAR2(63)
//         PRIMARY_DEPARTMENT_DESC       : VARCHAR2(255)
//         PRIMARY_MAJOR                 : VARCHAR2(63)
//         PRIMARY_MAJOR_DESC            : VARCHAR2(255)
//         PROGRAM_OF_STUDY              : VARCHAR2(63)
//         PROGRAM_OF_STUDY_DESC         : VARCHAR2(255)
//         RESIDENCY                     : VARCHAR2(63)
//         RESIDENCY_DESC                : VARCHAR2(255)
//         RESIDENCY_INDICATOR           : VARCHAR2(1)
//         STUDENT_CLASS                 : VARCHAR2(63)
//         STUDENT_CLASS_DESC            : VARCHAR2(255)
//         STUDENT_LEVEL                 : VARCHAR2(63)
//         STUDENT_LEVEL_DESC            : VARCHAR2(255)
//         STUDENT_TYPE                  : VARCHAR2(63)
//         STUDENT_TYPE_DESC             : VARCHAR2(255)
//         TERM_DEGREE                   : VARCHAR2(63)
//         TERM_DEGREE_DESC              : VARCHAR2(255)
//         ADMISSIONS_POPULATION         : VARCHAR2(63)
//         ADMISSIONS_POPULATION_DESC    : VARCHAR2(255)
//         SITE                          : VARCHAR2(63)
//         SITE_DESC                     : VARCHAR2(255)
//         PROGRAM_SITE                  : VARCHAR2(127)
//         MULTI_SOURCE                  : VARCHAR2(6)
//         MULTI_SOURCE_DESC             : VARCHAR2(30)
//         EXTRACT_DATE                  : DATE

        final Map<String, OdsTermData> result = new HashMap<>(600_000);

        final String sql = SimpleBuilder.concat(
                "SELECT CSU_ID, TERM, ANTICIPATED_GRAD_TERM, CAMPUS, PRIMARY_COLLEGE, PRIMARY_DEPARTMENT, ",
                "PROGRAM_OF_STUDY, RESIDENCY, STUDENT_CLASS ",
                "FROM CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH");

        try (final Statement stmt = odsConn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String csuId = rs.getString("CSU_ID");
                    final Integer term = DbUtils.getInteger(rs, "TERM");

                    if (term == null) {
                        Log.warning("Null term field in ODS for student ", csuId);
                    } else {
                        final Integer gradTerm = DbUtils.getInteger(rs, "ANTICIPATED_GRAD_TERM");
                        final String campus = rs.getString("CAMPUS");
                        final String college = rs.getString("PRIMARY_COLLEGE");
                        final String dept = rs.getString("PRIMARY_DEPARTMENT");
                        final String program = rs.getString("PROGRAM_OF_STUDY");
                        final String residency = rs.getString("RESIDENCY");
                        final String studentClass = rs.getString("STUDENT_CLASS");

                        try {
                            final OdsTermData rec = new OdsTermData(csuId, term, gradTerm, campus, college, dept,
                                    program,
                                    residency, studentClass);

                            final OdsTermData existing = result.get(csuId);
                            if (existing == null || existing.term.intValue() < term.intValue()) {
                                result.put(csuId, rec);
                            }
                        } catch (final NumberFormatException | DateTimeException ex) {
                            Log.warning("Unable to parse fields from ODS for student ", csuId, ex);
                        }
                    }
                }
            }
        }

        Log.info("Loaded " + result.size() + " ODS term records");

        return result;
    }

    /**
     * Trims and prunes a string to a maximum length.
     *
     * @param raw       the raw string
     * @param maxLength the maximum length
     * @return the trimmed and pruned string
     */
    private static String prune(final String raw, final int maxLength) {

        String pruned = null;

        if (raw != null) {
            final String trimmed = raw.trim();
            if (!trimmed.isEmpty()) {
                pruned = trimmed.length() > maxLength ? trimmed.substring(0, maxLength).trim() : trimmed;

                if (pruned.indexOf('\u2019') > -1) {
                    pruned = pruned.replace('\u2019', '\'');
                }
                if (pruned.indexOf('\u00B4') > -1) {
                    pruned = pruned.replace('\u00B4', '\'');
                }
            }
        }

        return pruned;
    }

    /**
     * Processes a single student.
     *
     * @param cache      the data cache
     * @param student    the student record
     * @param personData the ODS person data, if found
     * @param termData   the ODS term data, if found
     * @throws SQLException if there is an error accessing either database
     */
    private static void processStudent(final Cache cache, final RawStudent student, final OdsPersonData personData,
                                       final OdsTermData termData) throws SQLException {

        if (personData == null) {
            if (termData == null) {
                Log.warning("*** No [person] or [term] data for student ", student.stuId);
            } else {
                Log.warning("*** No [person] data for student ", student.stuId);
                updateTermData(cache, student, termData);
            }
        } else {
            updatePersonData(cache, student, personData);
            if (termData != null) {
                updateTermData(cache, student, termData);
            }
        }
    }

    /**
     * Updates person data on a single student.
     *
     * @param cache      the data cache
     * @param student    the student record
     * @param personData the ODS person data
     * @throws SQLException if there is an error accessing either database
     */
    private static void updatePersonData(final Cache cache, final RawStudent student, final OdsPersonData personData)
            throws SQLException {

        final Integer pidm = personData.pidm();
        final String firstName = personData.firstName();
        final String middleInitial = personData.middleInitial();
        final String lastName = personData.lastName();
        final String prefName = personData.prefName();
        final String email = personData.email();
        final LocalDate birthDate = personData.birthDate();
        final Integer satR = personData.satR();
        final Integer sat = personData.sat();
        final Integer act = personData.act();
        final String hsGpa = personData.hsGpa();
        final String hsCode = personData.hsCode();
        final Integer hsClassSize = personData.hsClassSize();
        final Integer hsClassRank = personData.hsClassRank();

        final Integer effectiveSat = satR == null ? sat : satR;

        final boolean matchPidm = Objects.equals(student.pidm, pidm);
        final boolean matchFirstName = Objects.equals(student.firstName, firstName);
        final boolean matchMiddleInitial = Objects.equals(student.middleInitial, middleInitial);
        final boolean matchLastName = Objects.equals(student.lastName, lastName);
        final boolean matchPrefName = Objects.equals(student.prefName, prefName);
        final boolean matchEmail = Objects.equals(student.stuEmail, email);
        final boolean matchBirthDate = Objects.equals(student.birthdate, birthDate);
        final boolean matchAct = Objects.equals(student.actScore, act);
        final boolean matchSat = Objects.equals(student.satScore, effectiveSat);
        final boolean matchHsGpa = Objects.equals(student.hsGpa, hsGpa);
        final boolean matchHsCode = Objects.equals(student.hsCode, hsCode);
        final boolean matchHsClassSize = Objects.equals(student.hsSizeClass, hsClassSize);
        final boolean matchHsClassRank = Objects.equals(student.hsClassRank, hsClassRank);

        final boolean changed = !(matchPidm && matchFirstName && matchMiddleInitial && matchLastName && matchPrefName
                                  && matchEmail && matchBirthDate && matchAct && matchSat && matchHsGpa && matchHsCode && matchHsClassSize
                                  && matchHsClassRank);

        if (changed) {
            final String stuId = student.stuId;

            if (DEBUG) {
                Log.fine("PERSON data for student ", stuId, " needs to be updated:");

                if (!matchPidm) {
                    Log.fine("    PIDM           : [", student.pidm, "] -> [", pidm, "]");
                }
                if (!matchFirstName) {
                    Log.fine("    FIRST NAME     : [", student.firstName, "] -> [", firstName, "]");
                }
                if (!matchMiddleInitial) {
                    Log.fine("    MIDDLE INITIAL : [", student.middleInitial, "] -> [", middleInitial, "]");
                }
                if (!matchLastName) {
                    Log.fine("    LAST NAME      : [", student.lastName, "] -> [", lastName, "]");
                }
                if (!matchPrefName) {
                    Log.fine("    PREF NAME      : [", student.prefName, "] -> [", prefName, "]");
                }
                if (!matchEmail) {
                    Log.fine("    EMAIL          : [", student.stuEmail, "] -> [", email, "]");
                }
                if (!matchBirthDate) {
                    Log.fine("    BIRTH DATE     : [", student.birthdate, "] -> [", birthDate, "]");
                }
                if (!matchAct) {
                    Log.fine("    ACT SCORE      : [", student.actScore, "] -> [", act, "]");
                }
                if (!matchSat) {
                    Log.fine("    SAT SCORE      : [", student.satScore, "] -> [", effectiveSat, "]");
                }
                if (!matchHsGpa) {
                    Log.fine("    HS GPA         : [", student.hsGpa, "] -> [", hsGpa, "]");
                }
                if (!matchHsCode) {
                    Log.fine("    HS CODE        : [", student.hsCode, "] -> [", hsCode, "]");
                }
                if (!matchHsClassSize) {
                    Log.fine("    HS CLASS SIZE  : [", student.hsSizeClass, "] -> [", hsClassSize, "]");
                }
                if (!matchHsClassRank) {
                    Log.fine("    HS CLASS RANK  : [", student.hsClassRank, "] -> [", hsClassRank, "]");
                }
            } else {
                if (!matchPidm) {
                    RawStudentLogic.updateInternalId(cache, stuId, pidm);
                }
                if (!(matchFirstName && matchLastName && matchMiddleInitial && matchPrefName)) {
                    RawStudentLogic.updateName(cache, stuId, lastName, firstName, prefName, middleInitial);
                }
                if (!matchEmail) {
                    RawStudentLogic.updateEmail(cache, stuId, email, student.adviserEmail);
                }
                if (!matchBirthDate) {
                    RawStudentLogic.updateBirthDate(cache, stuId, birthDate);
                }
                if (!(matchAct && matchSat)) {
                    RawStudentLogic.updateTestScores(cache, stuId, act, effectiveSat, student.apScore);
                }
                if (!(matchHsGpa && matchHsCode && matchHsClassSize && matchHsClassRank)) {
                    RawStudentLogic.updateHighSchool(cache, stuId, hsCode, hsGpa, hsClassRank, hsClassSize);
                }
            }
        }
    }

    /**
     * Updates person data on a single student.
     *
     * @param cache    the data cache
     * @param student  the student record
     * @param termData the ODS term data
     * @throws SQLException if there is an error accessing either database
     */
    private static void updateTermData(final Cache cache, final RawStudent student, final OdsTermData termData)
            throws SQLException {

        final Integer expectGradTerm = termData.expectGradTerm();
        // Format of term: "202410", "202460", "202490"
        TermKey effectiveTerm = null;
        if (expectGradTerm != null) {
            final int value = expectGradTerm.intValue();
            if (value > 100000) {
                final int which = value % 100;
                final int year = value / 100;
                if (which == 10) {
                    effectiveTerm = new TermKey(ETermName.SPRING, year);
                } else if (which == 60) {
                    effectiveTerm = new TermKey(ETermName.SUMMER, year);
                } else if (which == 90) {
                    effectiveTerm = new TermKey(ETermName.FALL, year);
                }
            }
        }

        final String campus = termData.campus();
        final String college = termData.college();
        final String dept = termData.dept();
        final String program = termData.program();
        final String residency = termData.residency();
        final String studentClass = termData.studentClass();

        final boolean matchGradTerm = Objects.equals(student.estGraduation, effectiveTerm);
        final boolean matchCampus = Objects.equals(student.campus, campus);
        final boolean matchCollege = Objects.equals(student.college, college);
        final boolean matchDept = Objects.equals(student.dept, dept);
        final boolean matchProgram = Objects.equals(student.programCode, program);
        final boolean matchResidency = Objects.equals(student.resident, residency);
        final boolean matchStudentClass = Objects.equals(student.clazz, studentClass);

        final boolean changed = !(matchGradTerm && matchCampus && matchCollege && matchDept && matchProgram
                                  && matchResidency && matchStudentClass);

        if (changed) {
            final String stuId = student.stuId;
            if (DEBUG) {
                Log.fine("TERM data for student ", student.stuId, " needs to be updated:");

                if (!matchGradTerm) {
                    Log.fine("    EST GRAD TERM  : [", student.estGraduation, "] -> [", effectiveTerm, "]");
                }
                if (!matchCampus) {
                    Log.fine("    CAMPUS         : [", student.campus, "] -> [", campus, "]");
                }
                if (!matchCollege) {
                    Log.fine("    COLLEGE        : [", student.college, "] -> [", college, "]");
                }
                if (!matchDept) {
                    Log.fine("    DEPARTMENT     : [", student.dept, "] -> [", dept, "]");
                }
                if (!matchProgram) {
                    Log.fine("    PROGRAM        : [", student.programCode, "] -> [", program, "]");
                }
                if (!matchResidency) {
                    Log.fine("    RESIDENCY      : [", student.resident, "] -> [", residency, "]");
                }
                if (!matchStudentClass) {
                    Log.fine("    STUDENT CLASS  : [", student.clazz, "] -> [", studentClass, "]");
                }
            } else {
                if (!matchGradTerm) {
                    RawStudentLogic.updateAnticGradTerm(cache, stuId, effectiveTerm);
                }
                if (!matchCampus) {
                    RawStudentLogic.updateCampus(cache, stuId, campus);
                }
                if (!(matchCollege && matchDept && matchProgram)) {
                    RawStudentLogic.updateProgram(cache, stuId, college, dept, program, student.minor);
                }
                if (!matchResidency) {
                    RawStudentLogic.updateResidency(cache, stuId, residency);
                }
                if (!matchStudentClass) {
                    RawStudentLogic.updateClassLevel(cache, stuId, studentClass);
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

        DbConnection.registerDrivers();
        final BulkUpdateStudentInformation job = new BulkUpdateStudentInformation();

        job.execute();
    }

    /**
     * A container for data from the ODS about a single person
     */
    private record OdsPersonData(String csuId, Integer pidm, String firstName, String middleInitial, String lastName,
                                 String prefName, String email, LocalDate birthDate, Integer satR, Integer sat,
                                 Integer act, String hsGpa, String hsCode, Integer hsClassSize,
                                 Integer hsClassRank) {
    }

    /**
     * A container for data from the ODS about a person in a single term.
     */
    private record OdsTermData(String csuId, Integer term, Integer expectGradTerm, String campus, String college,
                               String dept, String program, String residency, String studentClass) {
    }
}
