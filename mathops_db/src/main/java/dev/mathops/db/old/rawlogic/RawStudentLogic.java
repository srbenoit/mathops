package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.ifaces.ILiveStudent;
import dev.mathops.db.old.ifaces.ILiveTransferCredit;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.LiveReg;
import dev.mathops.db.old.rec.LiveStudent;
import dev.mathops.db.old.rec.LiveTransferCredit;
import dev.mathops.db.old.schema.csubanner.ImplLiveStudent;
import dev.mathops.db.old.schema.csubanner.ImplLiveTransferCredit;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

/**
 * A utility class to work with student records.
 *
 * <pre>
 * Table:  'student'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * pidm                 integer                   yes
 * last_name            char(30)                  yes
 * first_name           char(30)                  yes
 * pref_name            char(30)                  yes
 * middle_initial       char(1)                   yes
 * apln_term            char(4)                   yes
 * class                char(2)                   yes
 * college              char(2)                   yes
 * dept                 char(4)                   yes
 * program_code         char(14)                  yes
 * minor                char(6)                   yes
 * est_graduation       char(4)                   yes
 * tr_credits           char(5)                   yes
 * hs_code              char(6)                   yes
 * hs_gpa               char(4)                   yes
 * hs_class_rank        smallint                  yes
 * hs_size_class        smallint                  yes
 * act_score            smallint                  yes
 * sat_score            smallint                  yes
 * ap_score             char(3)                   yes
 * resident             char(4)                   yes
 * birthdate            date                      yes
 * ethnicity            char(2)                   yes
 * gender               char(1)                   yes
 * discip_history       char(1)                   no
 * discip_status        char(2)                   yes
 * sev_admin_hold       char(1)                   yes
 * timelimit_factor     decimal(3,2)              yes
 * licensed             char(1)                   no
 * campus               char(20)                  yes
 * stu_email            char(60)                  yes
 * adviser_email        char(60)                  yes
 * password             char(3)                   yes
 * admit_type           char(2)                   yes
 * order_enforce        char(2)                   no
 * pacing_structure     char(1)                   yes
 * create_dt            date                      yes
 * </pre>
 */
public enum RawStudentLogic {
    ;

    /** A commonly used string. */
    private static final String SKIPPING_UPDATE = "Skipping update of Student for test student:";

    /** A commonly used string. */
    private static final String STU_ID = "  Student ID: ";

    /** Shortest time between subsequent live queries for a single student (ms). */
    private static final long TIMEOUT = 30L * 60L * 1000L; // 30 minutes

    /** The set of students IDs for which a live query has been done, and the time it was done. */
    private static final Map<String, Long> LIVE_QUERIED_STUDENTS = new ConcurrentHashMap<>(1000);

    /** Test student IDs admitted for upcoming Fall term. */
    private static final List<String> UPCOMING_FALL_ADMITS;

    /** Test student IDs admitted for upcoming Spring term. */
    private static final List<String> UPCOMING_SPRING_ADMITS;

    /** Program codes for each test student. */
    private static final Map<String, String> PROGRAM_CODES;

    /** A commonly used string. */
    private static final String AGED_AGLZ_BS = "AGED-AGLZ-BS";

    /** A commonly used string. */
    private static final String EQSC_BS = "EQSC-BS";

    /** A commonly used string. */
    private static final String DSCI_CSCZ_BS = "DSCI-CSCZ-BS";

    /** A commonly used string. */
    private static final String MATH_MTEZ_BS = "MATH-MTEZ-BS";

    /** Consonants. */
    private static final char[] CONSONANTS = "bcdgklmnprstvw".toCharArray();

    /** Vowels. */
    private static final char[] VOWELS = "aeiou".toCharArray();

    /** Doubles. */
    private static final char[] DOUBLES =
            "bbffllnnssblbrchcrdrdsflfrgrglkrplprscspsttrwr".toCharArray();

    static {

        UPCOMING_FALL_ADMITS = Arrays.asList("999011111", "999011121",
                "999011122", "999011123", "999011124",
                "999011125", "999011126", "999011131",
                "999011132", "999011133", "999011141",
                "999011142", "999012111", "999012121",
                "999012122", "999012123", "999012124",
                "999012125", "999012126", "999012131",
                "999012132", "999012133", "999012141",
                "999012142", "999012411", "999012421",
                "999012422", "999012423", "999012424",
                "999012425", "999012426", "999012431",
                "999012432", "999012433", "999012441",
                "999012442", "999021111", "999021121",
                "999021131", "999021141", "999021151",
                "999021211", "999021221", "999021231",
                "999021241", "999021251", "999021261",
                "999021271", "999021281", "999021311",
                "999021411", "999031011", "999031021",
                "999032011", "999033111", "999033121",
                "999033211", "999033221", "999033231",
                "999034011");

        UPCOMING_SPRING_ADMITS = Arrays.asList("999011211", "999011221",
                "999011231", "999011241", "999012211",
                "999012221", "999012231", "999012241",
                "999012451", "999012461", "999012471",
                "999012481");

        PROGRAM_CODES = new HashMap<>(100);
        PROGRAM_CODES.put("999011111", "CBEG-BS");
        PROGRAM_CODES.put("999011121", "CHEM-ACSZ-BS");
        PROGRAM_CODES.put("999011122", "CIVE-BS");
        PROGRAM_CODES.put("999011123", "CPEG-BS");
        PROGRAM_CODES.put("999011124", "ELEG-ELEZ-BS");
        PROGRAM_CODES.put("999011125", "EGSC-EGPZ-BS");
        PROGRAM_CODES.put("999011126", "EGSC-SPEZ-BS");
        PROGRAM_CODES.put("999011131", "EGSC-TCEZ-BS");
        PROGRAM_CODES.put("999011132", "EGIS-DUAL");
        PROGRAM_CODES.put("999011133", "ENVE-BS");
        PROGRAM_CODES.put("999011141", "MATH-ALSZ-BS");
        PROGRAM_CODES.put("999011142", "MATH-AMTZ-BS");
        PROGRAM_CODES.put("999011211", "MATH-GNMZ-BS");
        PROGRAM_CODES.put("999011221", "MATH-GNMZ-BS");
        PROGRAM_CODES.put("999011231", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999011241", "MECH-BS");
        PROGRAM_CODES.put("999011311", "MATH-ALSZ-BS");
        PROGRAM_CODES.put("999011321", "MATH-ALSZ-BS");
        PROGRAM_CODES.put("999011331", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999011341", "MECH-BS");
        PROGRAM_CODES.put("999012111", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012121", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012122", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012123", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012124", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012125", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012126", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012131", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012132", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012133", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012141", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012142", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012211", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012221", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012231", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012241", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012311", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012321", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012331", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012341", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012411", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012421", "DSCI-ECNZ-BS");
        PROGRAM_CODES.put("999012422", "DSCI-MATZ-BS");
        PROGRAM_CODES.put("999012423", "DSCI-STSZ-BS");
        PROGRAM_CODES.put("999012424", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012425", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012426", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012431", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012432", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012433", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012441", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012442", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012451", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012461", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012471", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012481", DSCI_CSCZ_BS);
        PROGRAM_CODES.put("999012511", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012521", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012531", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999012541", MATH_MTEZ_BS);
        PROGRAM_CODES.put("999021111", "HSMG-BS");
        PROGRAM_CODES.put("999021121", "SOCR-APMZ-BS");
        PROGRAM_CODES.put("999021131", "PSYC-MBBZ-BS");
        PROGRAM_CODES.put("999021141", "HORT-FLOZ-BS");
        PROGRAM_CODES.put("999021151", "ENHR-NALZ-BS");
        PROGRAM_CODES.put("999021211", "INTD-BS");
        PROGRAM_CODES.put("999021221", "HAES-BS");
        PROGRAM_CODES.put("999021231", "HAES-HPRZ-BS");
        PROGRAM_CODES.put("999021241", "HAES-SPMZ-BS");
        PROGRAM_CODES.put("999021251", AGED_AGLZ_BS);
        PROGRAM_CODES.put("999021261", EQSC_BS);
        PROGRAM_CODES.put("999021271", EQSC_BS);
        PROGRAM_CODES.put("999021281", EQSC_BS);
        PROGRAM_CODES.put("999021311", "SOWK-BSW");
        PROGRAM_CODES.put("999021411", "ANTH-ARCZ-BA");
        PROGRAM_CODES.put("999022111", "HSMG-BS");
        PROGRAM_CODES.put("999022121", "SOCR-APMZ-BS");
        PROGRAM_CODES.put("999022131", "PSYC-MBBZ-BS");
        PROGRAM_CODES.put("999022141", "HORT-FLOZ-BS");
        PROGRAM_CODES.put("999022151", "ENHR-NALZ-BS");
        PROGRAM_CODES.put("999022211", "INTD-BS");
        PROGRAM_CODES.put("999022221", "HAES-BS");
        PROGRAM_CODES.put("999022231", "HAES-HPRZ-BS");
        PROGRAM_CODES.put("999022241", "HAES-SPMZ-BS");
        PROGRAM_CODES.put("999022251", AGED_AGLZ_BS);
        PROGRAM_CODES.put("999022261", EQSC_BS);
        PROGRAM_CODES.put("999022271", EQSC_BS);
        PROGRAM_CODES.put("999022281", EQSC_BS);
        PROGRAM_CODES.put("999022311", "SOWK-BSW");
        PROGRAM_CODES.put("999022411", "ANTH-ARCZ-BA");
        PROGRAM_CODES.put("999031011", "LLAC-LFRZ-BA");
        PROGRAM_CODES.put("999031021", "LLAC-LGEZ-BA");
        PROGRAM_CODES.put("999032011", "ETST-WSTZ-BA");
        PROGRAM_CODES.put("999033111", AGED_AGLZ_BS);
        PROGRAM_CODES.put("999033121", AGED_AGLZ_BS);
        PROGRAM_CODES.put("999033211", "CTMG-BS");
        PROGRAM_CODES.put("999033221", "EVHL-BS");
        PROGRAM_CODES.put("999033231", "ENRE-BS");
        PROGRAM_CODES.put("999034011", "ECSS-BA");
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStudent record) throws SQLException {

        if (record.stuId == null || record.discipHistory == null || record.licensed == null
                || record.orderEnforce == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO student ",
                "(stu_id,pidm,last_name,first_name,pref_name,middle_initial,apln_term,class,college,dept,program_code,",
                "minor,est_graduation,tr_credits,hs_code,hs_gpa,hs_class_rank,hs_size_class,act_score,sat_score,",
                "ap_score,resident,birthdate,ethnicity,gender,discip_history,discip_status,sev_admin_hold,",
                "timelimit_factor,licensed,campus,stu_email,adviser_email,password,admit_type,order_enforce,",
                "pacing_structure,create_dt) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlIntegerValue(record.pidm), ",",
                LogicUtils.sqlStringValue(record.lastName), ",",
                LogicUtils.sqlStringValue(record.firstName), ",",
                LogicUtils.sqlStringValue(record.prefName), ",",
                LogicUtils.sqlStringValue(record.middleInitial), ",",
                LogicUtils.sqlTermValue(record.aplnTerm), ",",
                LogicUtils.sqlStringValue(record.clazz), ",",
                LogicUtils.sqlStringValue(record.college), ",",
                LogicUtils.sqlStringValue(record.dept), ",",
                LogicUtils.sqlStringValue(record.programCode), ",",
                LogicUtils.sqlStringValue(record.minor), ",",
                LogicUtils.sqlTermValue(record.estGraduation), ",",
                LogicUtils.sqlStringValue(record.trCredits), ",",
                LogicUtils.sqlStringValue(record.hsCode), ",",
                LogicUtils.sqlStringValue(record.hsGpa), ",",
                LogicUtils.sqlIntegerValue(record.hsClassRank), ",",
                LogicUtils.sqlIntegerValue(record.hsSizeClass), ",",
                LogicUtils.sqlIntegerValue(record.actScore), ",",
                LogicUtils.sqlIntegerValue(record.satScore), ",",
                LogicUtils.sqlStringValue(record.apScore), ",",
                LogicUtils.sqlStringValue(record.resident), ",",
                LogicUtils.sqlDateValue(record.birthdate), ",",
                LogicUtils.sqlStringValue(record.ethnicity), ",",
                LogicUtils.sqlStringValue(record.gender), ",",
                LogicUtils.sqlStringValue(record.discipHistory), ",",
                LogicUtils.sqlStringValue(record.discipStatus), ",",
                LogicUtils.sqlStringValue(record.sevAdminHold), ",",
                LogicUtils.sqlFloatValue(record.timelimitFactor), ",",
                LogicUtils.sqlStringValue(record.licensed), ",",
                LogicUtils.sqlStringValue(record.campus), ",",
                LogicUtils.sqlStringValue(record.stuEmail), ",",
                LogicUtils.sqlStringValue(record.adviserEmail), ",",
                LogicUtils.sqlStringValue(record.password), ",",
                LogicUtils.sqlStringValue(record.admitType), ",",
                LogicUtils.sqlStringValue(record.orderEnforce), ",",
                LogicUtils.sqlStringValue(record.pacingStructure), ",",
                LogicUtils.sqlDateValue(record.createDt), ")");

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Inserts a new student based on values from a live registration record.
     *
     * @param cache   the data cache
     * @param liveReg a model of type {@code CLiveReg}
     * @throws SQLException if there is an error accessing the database
     */
    public static void insertFromLive(final Cache cache, final LiveReg liveReg) throws SQLException {

        final Integer sat = liveReg.satrScore == null ? liveReg.satScore : liveReg.satrScore;

        final LocalDate now = LocalDate.now();

        final RawStudent record = new RawStudent(liveReg.studentId, liveReg.internalId, liveReg.lastName,
                liveReg.firstName, null, null, null, liveReg.classLevel, liveReg.college, liveReg.department,
                liveReg.major1, null, liveReg.anticGradTerm, liveReg.numTransferCredits, liveReg.highSchoolCode,
                liveReg.highSchoolGpa, liveReg.highSchoolClassRank, liveReg.highSchoolClassSize, liveReg.actScore, sat,
                liveReg.apScore, liveReg.residency, liveReg.birthDate, null, liveReg.gender, "N", null, null, null, "N",
                liveReg.campus, liveReg.email, liveReg.adviserEmail, null, liveReg.admitType, "N", null, now, null,
                null);

        insert(cache, record);
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawStudent record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM student WHERE stu_id=",
                LogicUtils.sqlStringValue(record.stuId));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStudent> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM student");
    }

    /**
     * Gets the record with a specified student ID.
     *
     * @param cache         the data cache
     * @param stuId         the student ID
     * @param liveRefreshes true to query Banner for student information; false to skip
     * @return the student record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStudent query(final Cache cache, final String stuId,
                                   final boolean liveRefreshes) throws SQLException {

        RawStudent result = null;

        if (stuId.startsWith("99")) {
            result = getTestStudent(cache, stuId);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM student WHERE stu_id=",
                    LogicUtils.sqlStringValue(stuId));

            try (final Statement stmt = cache.conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    result = RawStudent.fromResultSet(rs);
                }
            }
        }

        if (liveRefreshes && !LogicUtils.isBannerDown() && stuId.length() == 9
            && stuId.charAt(0) == '8' && !"888888888".equals(stuId)) {

            final Long when = LIVE_QUERIED_STUDENTS.get(stuId);
            final long now = System.currentTimeMillis();

            if (when == null || now - when.longValue() > TIMEOUT) {
                LIVE_QUERIED_STUDENTS.put(stuId, Long.valueOf(System.currentTimeMillis()));

                if (result == null) {
                    Log.warning("Student ", stuId, " was not found - doing live query");

                    result = liveQueryStudent(cache, stuId);
                } else {
                    liveRefreshStudent(cache, result);
                }
            }
        }

        return result;
    }

    /**
     * Gets the record with a specified PIDM.
     *
     * @param cache the data cache
     * @param pidm  the PIDM
     * @return if the PIDM uniquely determined a student, that student record; if there are no students with the
     *         specified PIDM, or if there are multiple students with the PIDM, {@code null}
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStudent queryByInternalId(final Cache cache, final Integer pidm) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM student WHERE pidm=", pidm);

        return executeSingleQuery(cache, sql);
    }

    /**
     * Gets the record with a specified last name.
     *
     * @param cache    the data cache
     * @param lastName the last name
     * @return if the last name uniquely determined a student, that student record; if there are no students with the
     *         specified last name, or if there are multiple students with the last name, {@code null}
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStudent queryByLastName(final Cache cache, final String lastName)
            throws SQLException {

        final String upper = lastName.toUpperCase(Locale.US);

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM student WHERE UPPER(last_name)=", LogicUtils.sqlStringValue(upper));

        final List<RawStudent> list = executeListQuery(cache, sql);

        return list.size() == 1 ? list.getFirst() : null;
    }

    /**
     * Gets the record with a specified first and last name.
     *
     * @param cache     the data cache
     * @param firstName the first name
     * @param lastName  the last name
     * @return if the first and last name uniquely determined a student, that student record; if there are no students
     *         with the specified name, or if there are multiple students with the name, {@code null}
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStudent queryByName(final Cache cache, final String firstName,
                                         final String lastName) throws SQLException {

        final String upperLast = lastName.toUpperCase(Locale.US);
        final String upperFirst = firstName.toUpperCase(Locale.US);

        final String sql = SimpleBuilder.concat("SELECT * FROM student ",
                "WHERE UPPER(last_name)=", LogicUtils.sqlStringValue(upperLast),
                "  AND (UPPER(first_name)=", LogicUtils.sqlStringValue(upperFirst),
                " OR UPPER(pref_name)=", LogicUtils.sqlStringValue(upperFirst),
                ")");

        return executeSingleQuery(cache, sql);
    }

    /**
     * Gets all students matching a provided first and last name (which may contain '%' wild-cards).
     *
     * @param cache     the data cache
     * @param firstName the first name (must not be null, could be '%')
     * @param lastName  the last name (must not be null, could be '%')
     * @return the list of matching students; empty if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStudent> queryAllByName(final Cache cache, final String firstName,
                                                  final String lastName) throws SQLException {

        final String upperLast = lastName.toUpperCase(Locale.US);
        final String upperFirst = firstName.toUpperCase(Locale.US);

        final String sql = SimpleBuilder.concat("SELECT * FROM student ",
                "WHERE UPPER(last_name) LIKE ", LogicUtils.sqlStringValue(upperLast),
                " AND (UPPER(first_name) LIKE ", LogicUtils.sqlStringValue(upperFirst),
                " OR UPPER(pref_name) LIKE ", LogicUtils.sqlStringValue(upperFirst),
                ")");

        return executeListQuery(cache, sql);
    }

    /**
     * Updates a student's internal ID.
     *
     * @param cache      the data cache
     * @param studentId  the ID of the student whose internal ID to update
     * @param internalId the new internal ID
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateInternalId(final Cache cache, final String studentId,
                                           final Integer internalId) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = "UPDATE student SET pidm=" + internalId
                    + " WHERE stu_id='" + studentId + "'";

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's name fields.
     *
     * @param cache         the data cache
     * @param studentId     the ID of the student whose name to update
     * @param lastName      the new last name
     * @param firstName     the new first name
     * @param prefName      the new preferred first name
     * @param middleInitial the new middle initial
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateName(final Cache cache, final String studentId,
                                     final String lastName, final String firstName, final String prefName,
                                     final String middleInitial) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            check(lastName);
            check(firstName);
            check(prefName);
            check(middleInitial);

            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET last_name=", LogicUtils.sqlStringValue(lastName),
                    ", first_name=", LogicUtils.sqlStringValue(firstName),
                    ", pref_name=", LogicUtils.sqlStringValue(prefName),
                    ", middle_initial=", LogicUtils.sqlStringValue(middleInitial),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's application term.
     *
     * @param cache              the data cache
     * @param studentId          the ID of the student whose application term to update
     * @param newApplicationTerm the new application term
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateApplicationTerm(final Cache cache, final String studentId,
                                                final TermKey newApplicationTerm) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET apln_term=", LogicUtils.sqlTermValue(newApplicationTerm),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's class level.
     *
     * @param cache         the data cache
     * @param studentId     the ID of the student whose class level to update
     * @param newClassLevel the new class level
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateClassLevel(final Cache cache, final String studentId,
                                           final String newClassLevel) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET class=", LogicUtils.sqlStringValue(newClassLevel),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's program (college, department, program code, and minor).
     *
     * @param cache          the data cache
     * @param studentId      the ID of the student whose program to update
     * @param newCollege     the new college
     * @param newDepartment  the new department
     * @param newProgramCode the new program code
     * @param newMinor       the new minor
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateProgram(final Cache cache, final String studentId, final String newCollege,
                                        final String newDepartment, final String newProgramCode,
                                        final String newMinor) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET college=", LogicUtils.sqlStringValue(newCollege),
                    ", dept=", LogicUtils.sqlStringValue(newDepartment),
                    ", program_code=", LogicUtils.sqlStringValue(newProgramCode),
                    ", minor=", LogicUtils.sqlStringValue(newMinor),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's anticipated graduation term.
     *
     * @param cache             the data cache
     * @param studentId         the ID of the student whose anticipated graduation term to update
     * @param newGraduationTerm the new anticipated graduation term
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateAnticGradTerm(final Cache cache, final String studentId,
                                              final TermKey newGraduationTerm) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET est_graduation=", LogicUtils.sqlTermValue(newGraduationTerm),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's number of transfer credits.
     *
     * @param cache             the data cache
     * @param studentId         the ID of the student whose number of transfer credits to update
     * @param newNumXferCredits the new number of transfer credits
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateNumTransferCredits(final Cache cache, final String studentId,
                                                   final String newNumXferCredits) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET tr_credits=", LogicUtils.sqlStringValue(newNumXferCredits),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's high school information.
     *
     * @param cache             the data cache
     * @param studentId         the ID of the student whose high school information to update
     * @param newHighSchoolCode the new high school code
     * @param newHichSchoolGpa  the new high school GPA
     * @param newHSClassRank    the new high school class rank
     * @param newHSClassSize    the new high school class size
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateHighSchool(final Cache cache, final String studentId, final String newHighSchoolCode,
                                           final String newHichSchoolGpa, final Integer newHSClassRank,
                                           final Integer newHSClassSize) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET hs_code=", LogicUtils.sqlStringValue(newHighSchoolCode),
                    ", hs_gpa=", LogicUtils.sqlStringValue(newHichSchoolGpa),
                    ", hs_class_rank=", LogicUtils.sqlIntegerValue(newHSClassRank),
                    ", hs_size_class=", LogicUtils.sqlIntegerValue(newHSClassSize),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's test scores.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student whose test scores to update
     * @param newAct    the new ACT score
     * @param newSat    the new SAT score
     * @param newAp     the new AP score
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateTestScores(final Cache cache, final String studentId, final Integer newAct,
                                           final Integer newSat, final String newAp) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET act_score=", LogicUtils.sqlIntegerValue(newAct),
                    ", sat_score=", LogicUtils.sqlIntegerValue(newSat),
                    ", ap_score=", LogicUtils.sqlStringValue(newAp),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's residency.
     *
     * @param cache        the data cache
     * @param studentId    the ID of the student whose residency to update
     * @param newResidency the new residency
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateResidency(final Cache cache, final String studentId,
                                          final String newResidency) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET resident=", LogicUtils.sqlStringValue(newResidency),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's birthdate.
     *
     * @param cache        the data cache
     * @param studentId    the ID of the student whose birthdate to update
     * @param newBirthDate the new birthdate
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateBirthDate(final Cache cache, final String studentId,
                                          final LocalDate newBirthDate) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET birthdate=", LogicUtils.sqlDateValue(newBirthDate),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's gender.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student whose gender to update
     * @param newGender the new gender
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateGender(final Cache cache, final String studentId,
                                       final String newGender) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET gender=", LogicUtils.sqlStringValue(newGender),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's hold severity.
     *
     * @param cache           the data cache
     * @param studentId       the ID of the student whose hold severity to update
     * @param newHoldSeverity the new hold severity
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateHoldSeverity(final Cache cache, final String studentId,
                                             final String newHoldSeverity) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET sev_admin_hold=", LogicUtils.sqlStringValue(newHoldSeverity),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's time limit factor.
     *
     * @param cache              the data cache
     * @param studentId          the ID of the student whose time limit factor to update
     * @param newTimeLimitFactor the new time limit factor
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateTimeLimitFactor(final Cache cache, final String studentId,
                                                final Float newTimeLimitFactor) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = "UPDATE student SET timelimit_factor="
                    + newTimeLimitFactor + " WHERE stu_id='" + studentId
                    + "'";

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's number of extension days.
     *
     * @param cache            the data cache
     * @param studentId        the ID of the student whose time limit factor to update
     * @param newExtensionDays the new number of extension days
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateExtensionDays(final Cache cache, final String studentId,
                                              final Integer newExtensionDays) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = "UPDATE student SET extension_days="
                    + newExtensionDays + " WHERE stu_id='" + studentId
                    + "'";

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's licensed status.
     *
     * @param cache       the data cache
     * @param studentId   the ID of the student whose licensed status to update
     * @param newLicensed the new licensed status ("Y" or "N")
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateLicensed(final Cache cache, final String studentId,
                                         final String newLicensed) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET licensed=", LogicUtils.sqlStringValue(newLicensed),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's campus.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student whose campus to update
     * @param newCampus the new campus
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateCampus(final Cache cache, final String studentId,
                                       final String newCampus) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET campus=", LogicUtils.sqlStringValue(newCampus),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's email information.
     *
     * @param cache           the data cache
     * @param studentId       the ID of the student whose email information to update
     * @param newStudentEmail the new student email address
     * @param newAdviserEmail the new adviser email address
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateEmail(final Cache cache, final String studentId, final String newStudentEmail,
                                      final String newAdviserEmail) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            check(newStudentEmail);
            check(newAdviserEmail);

            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET stu_email=", LogicUtils.sqlStringValue(newStudentEmail),
                    ", adviser_email=", LogicUtils.sqlStringValue(newAdviserEmail),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's admission information.
     *
     * @param cache        the data cache
     * @param studentId    the ID of the student whose admission information to update
     * @param newAdmitType the new admit type
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateAdmission(final Cache cache, final String studentId, final String newAdmitType)
            throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET admit_type=", LogicUtils.sqlStringValue(newAdmitType),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's optimal course order.
     *
     * @param cache          the data cache
     * @param studentId      the ID of the student whose optimal course order to update
     * @param newCourseOrder the new optimal course order
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateCourseOrder(final Cache cache, final String studentId,
                                            final String newCourseOrder) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET order_enforce=", LogicUtils.sqlStringValue(newCourseOrder),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's rule set ID.
     *
     * @param cache              the data cache
     * @param studentId          the ID of the student whose rule set ID to update
     * @param newPacingStructure the new pacing structure ID
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updatePacingStructure(final Cache cache, final String studentId,
                                                final String newPacingStructure) throws SQLException {

        final boolean result;

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET pacing_structure=", LogicUtils.sqlStringValue(newPacingStructure),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            result = executeSimpleUpdate(cache, sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Updates a student's canvas ID.
     *
     * @param cache       the data cache
     * @param studentId   the ID of the student whose Canvas ID to update
     * @param newCanvasId the new Canvas ID
     * @throws SQLException if there is an error accessing the database
     */
    public static void updateCanvasId(final Cache cache, final String studentId,
                                      final String newCanvasId) throws SQLException {

        if (studentId.startsWith("99")) {
            Log.info(SKIPPING_UPDATE);
            Log.info(STU_ID, studentId);
        } else {
            final String sql = SimpleBuilder.concat("UPDATE student ",
                    "SET canvas_id=", LogicUtils.sqlStringValue(newCanvasId),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId));

            if (executeSimpleUpdate(cache, sql) == 1) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStudent> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStudent> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStudent.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Executes a query that returns a single records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the record found; null if none returned
     * @throws SQLException if there is an error accessing the database
     */
    private static RawStudent executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawStudent result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawStudent.fromResultSet(rs);
            }
        }

        return result;
    }

    /**
     * Executes an SQL update.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the number of rows updated
     * @throws SQLException if there is an error accessing the database
     */
    private static int executeSimpleUpdate(final Cache cache, final String sql)
            throws SQLException {

        try (final Statement stmt = cache.conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Checks a string for non-printable characters.
     *
     * @param str the string
     */
    private static void check(final String str) {

        if (str != null) {
            for (final char ch : str.toCharArray()) {
                if (ch > '\u007f') {
                    Log.warning("String '", str, "' has character ", Integer.toHexString(ch));
                }
            }
        }
    }

    /**
     * Called when a student ID does not exist in the local database. Attempts to query the live database for a student.
     * If found, a student record is created for the student.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return the student record; {@code null} if not found or an error occurs
     */
    private static RawStudent liveQueryStudent(final Cache cache, final String studentId) {

        RawStudent result = null;

        try {
            final DbContext banner = cache.dbProfile.getDbContext(ESchemaUse.LIVE);
            final DbConnection bannerConn = banner.checkOutConnection();

            try {
                final ILiveStudent impl1 = ImplLiveStudent.INSTANCE;
                final ILiveTransferCredit impl2 = ImplLiveTransferCredit.INSTANCE;

                result = fetchLiveStudent(cache, bannerConn, studentId, impl1, impl2);
            } finally {
                banner.checkInConnection(bannerConn);
            }
        } catch (final SQLException ex) {
            LogicUtils.indicateBannerDown();
            Log.warning(ex);
        }

        return result;
    }

    /**
     * Performs a query against live data to create a student record, and if successful, inserts the student in the
     * local database.
     *
     * @param cache      the data cache
     * @param bannerConn the banner database connection
     * @param studentId  the student ID
     * @param impl1      the {@code ILiveStudent} implementation
     * @param impl2      the {@code ILiveTransferCredit} implementation
     * @return the student record; {@code null} if not found or an error occurs
     * @throws SQLException if there is an error accessing the database
     */
    private static RawStudent fetchLiveStudent(final Cache cache, final DbConnection bannerConn,
                                               final String studentId, final ILiveStudent impl1,
                                               final ILiveTransferCredit impl2) throws SQLException {

        RawStudent result = null;

        final List<LiveStudent> list1 = impl1.query(bannerConn, studentId);

        if (list1.isEmpty()) {
            Log.warning("No live student record for ", studentId);
        } else {
            final LiveStudent liveStudent = list1.getFirst();

            final List<LiveTransferCredit> liveTransfer = impl2.query(bannerConn, studentId);
            // Count transfer credits
            double totalTransfer = 0.0;
            for (final LiveTransferCredit rec : liveTransfer) {
                if (rec.credits != null) {
                    totalTransfer += rec.credits.doubleValue();
                }
            }

            TermKey anticGradTerm = null;

            final LocalDate gradDate = liveStudent.estGradDate;
            if (gradDate != null) {
                // Spring ends in May (5), Fall ends in December (12)
                final int year = gradDate.getYear();
                final int month = gradDate.getMonthValue();

                if (month < 3) {
                    // Jan/Feb - treat as Fall of the year before
                    anticGradTerm = new TermKey(ETermName.FALL, year - 1);
                } else if (month < 9) {
                    // March to August - treat as Spring of same year
                    anticGradTerm = new TermKey(ETermName.SPRING, year);
                } else {
                    // September to December - treat as Fall of same year
                    anticGradTerm = new TermKey(ETermName.FALL, year);
                }
            }

            String transferStr = Double.toString(totalTransfer);
            if (transferStr.endsWith(".0")) {
                transferStr = transferStr.substring(0, transferStr.length() - 2);
            }

            final Integer sat = liveStudent.satrScore == null ? liveStudent.satScore : liveStudent.satrScore;

            final String res = liveStudent.residency;
            final String residency;
            if ("C".equals(res)) {
                residency = "CO";
            } else if ("N".equals(res)) {
                residency = liveStudent.state;
            } else {
                residency = res;
            }

            result = new RawStudent(liveStudent.studentId, liveStudent.internalId, liveStudent.lastName,
                    liveStudent.firstName, liveStudent.prefFirstName, liveStudent.middleInitial, liveStudent.admitTerm,
                    null, liveStudent.collegeCode, liveStudent.departmentCode, liveStudent.programCode,
                    liveStudent.minorCode, anticGradTerm, transferStr, liveStudent.highSchoolCode,
                    liveStudent.highSchoolGpa, liveStudent.highSchoolClassRank, liveStudent.highSchoolClassSize,
                    liveStudent.actScore, sat, null, residency, liveStudent.birthDate, null, liveStudent.gender, "N",
                    null, "N", null, "N", liveStudent.campus, liveStudent.email, liveStudent.adviserEmail, null,
                    liveStudent.admitType, "N", null, LocalDate.now(), null, null);

            RawStudentLogic.insert(cache, result);

            // Scan FFR_TRNS table and add any MATH transfer credits not in that table already.

            final List<RawFfrTrns> existing = RawFfrTrnsLogic.queryByStudent(cache, studentId);

            final LocalDate today = LocalDate.now();
            for (final LiveTransferCredit live : liveTransfer) {

                boolean searching = true;
                for (final RawFfrTrns exist : existing) {
                    if (exist.course.equals(live.courseId)) {
                        searching = false;
                        break;
                    }
                }

                if (searching) {
                    Log.info("Adding ", live.courseId, " transfer credit for student ", studentId);

                    final RawFfrTrns toAdd = new RawFfrTrns(live.studentId, live.courseId, "T", today, null);

                    RawFfrTrnsLogic.insert(cache, toAdd);
                }
            }
        }

        return result;
    }

    /**
     * Called when a student exists in the local database but has not been recently verified. Attempts to query the live
     * database for a student and update any mismatched fields in the local record.
     *
     * @param cache    the data cache
     * @param existing the existing student record (updated in place if changes are found)
     */
    private static void liveRefreshStudent(final Cache cache, final RawStudent existing) {

        final String stuId = existing.stuId;

        try {
            final DbContext banner = cache.dbProfile.getDbContext(ESchemaUse.LIVE);
            final DbConnection bannerConn = banner.checkOutConnection();

            try {
                final ILiveStudent impl1 = ImplLiveStudent.INSTANCE;

                final List<LiveStudent> liveList = impl1.query(bannerConn, stuId);

                if (liveList.isEmpty()) {
                    Log.warning("No live student record for ", stuId);
                } else {
                    final LiveStudent liveStudent = liveList.getFirst();

                    if (liveStudent.admitTerm != null && isDifferent(existing.aplnTerm, liveStudent.admitTerm)) {
                        updateApplicationTerm(cache, stuId, liveStudent.admitTerm);
                        existing.aplnTerm = liveStudent.admitTerm;
                    }

                    boolean programUpdate = liveStudent.collegeCode != null
                            && isDifferent(existing.college, liveStudent.collegeCode);

                    if (liveStudent.departmentCode != null && isDifferent(existing.dept, liveStudent.departmentCode)) {
                        programUpdate = true;
                    }
                    if (liveStudent.programCode != null && isDifferent(existing.programCode, liveStudent.programCode)) {
                        programUpdate = true;
                    }
                    if (liveStudent.minorCode != null
                            && isDifferent(existing.minor, liveStudent.minorCode)) {
                        programUpdate = true;
                    }

                    if (programUpdate) {
                        updateProgram(cache, stuId, liveStudent.collegeCode, liveStudent.departmentCode,
                                liveStudent.programCode, liveStudent.minorCode);

                        existing.college = liveStudent.collegeCode;
                        existing.dept = liveStudent.departmentCode;
                        existing.programCode = liveStudent.programCode;
                        existing.minor = liveStudent.minorCode;
                    }

                    boolean testScoresUpdate = false;

                    final Integer sat = liveStudent.satrScore == null ? liveStudent.satScore : liveStudent.satrScore;

                    if (liveStudent.actScore != null && isDifferent(existing.actScore, liveStudent.actScore)) {
                        testScoresUpdate = true;
                    }
                    if (sat != null && isDifferent(existing.satScore, sat)) {
                        testScoresUpdate = true;
                    }

                    if (testScoresUpdate) {
                        updateTestScores(cache, stuId, liveStudent.actScore, sat, existing.apScore);
                        existing.actScore = liveStudent.actScore;
                        existing.satScore = sat;
                    }

                    boolean emailUpdate = liveStudent.email != null
                            && isDifferent(existing.stuEmail, liveStudent.email);

                    if (liveStudent.adviserEmail != null
                            && isDifferent(existing.adviserEmail, liveStudent.adviserEmail)) {
                        emailUpdate = true;
                    }

                    if (emailUpdate) {
                        updateEmail(cache, stuId, liveStudent.email, liveStudent.adviserEmail);
                        existing.stuEmail = liveStudent.email;
                        existing.adviserEmail = liveStudent.adviserEmail;
                    }
                }
            } finally {
                banner.checkInConnection(bannerConn);
            }
        } catch (final SQLException ex) {
            LogicUtils.indicateBannerDown();
            Log.warning(ex);
        }
    }

    /**
     * Tests whether two objects are different, where either may be {@code null}, and where Integer and Long values are
     * considered equal if they have the same numeric value.
     *
     * @param obj1 the first object
     * @param obj2 the second object
     * @return {@code true} if the objects are different
     */
    private static boolean isDifferent(final Object obj1, final Object obj2) {

        final boolean different;

        if (obj1 == null) {
            different = obj2 != null;
        } else if (obj1 instanceof Long && obj2 instanceof Integer) {
            different = ((Long) obj1).intValue() != ((Integer) obj2).intValue();
        } else {
            different = !obj1.equals(obj2);
        }

        return different;
    }

    /**
     * Updates a student model to include values from a live registration record.
     *
     * @param cache   the data cache
     * @param record  the model to be updated
     * @param liveReg a model of type {@code CLiveReg}
     * @throws SQLException if there is an error accessing the database
     */
    public static void updateFromLive(final Cache cache, final RawStudent record,
                                      final LiveReg liveReg) throws SQLException {

        boolean result = true;

        if (mismatch(liveReg.classLevel, record.clazz)) {
            result = updateClassLevel(cache, record.stuId, liveReg.classLevel);
        }

        if (mismatch(liveReg.college, record.college)
                || mismatch(liveReg.department, record.dept)) {
            result = result && updateProgram(cache, record.stuId, liveReg.college,
                    liveReg.department, record.programCode, record.minor);
        }

        if (mismatch(liveReg.anticGradTerm, record.estGraduation)) {
            result = result && updateAnticGradTerm(cache, record.stuId, liveReg.anticGradTerm);
        }

        if (mismatch(liveReg.numTransferCredits, record.trCredits)) {
            result = result && updateNumTransferCredits(cache, record.stuId, liveReg.numTransferCredits);
        }

        String newGpa = liveReg.highSchoolGpa;
        while (newGpa != null && newGpa.endsWith("0")) {
            newGpa = newGpa.substring(0, newGpa.length() - 1);
        }
        while (newGpa != null && newGpa.endsWith(".")) {
            newGpa = newGpa.substring(0, newGpa.length() - 1);
        }

        if (mismatch(liveReg.highSchoolCode, record.hsCode) || mismatch(newGpa, record.hsGpa)
                || mismatch(liveReg.highSchoolClassRank, record.hsClassRank)
                || mismatch(liveReg.highSchoolClassSize, record.hsSizeClass)) {

            result = result && updateHighSchool(cache, record.stuId, liveReg.highSchoolCode,
                    newGpa, liveReg.highSchoolClassRank, liveReg.highSchoolClassSize);
        }

        final Integer sat = liveReg.satrScore == null ? liveReg.satScore : liveReg.satrScore;

        if (mismatch(liveReg.actScore, record.actScore) || mismatch(sat, record.satScore)
                || mismatch(liveReg.apScore, record.apScore)) {
            result = result && updateTestScores(cache, record.stuId, liveReg.actScore, sat, liveReg.apScore);
        }

        if (mismatch(liveReg.residency, record.resident)) {
            result = result && updateResidency(cache, record.stuId, liveReg.residency);
        }

        if (mismatch(liveReg.birthDate, record.birthdate)) {
            result = result && updateBirthDate(cache, record.stuId, liveReg.birthDate);
        }

        if (mismatch(liveReg.gender, record.gender)) {
            result = result && updateGender(cache, record.stuId, liveReg.gender);
        }

        if (mismatch(liveReg.email, record.stuEmail)
                || mismatch(liveReg.adviserEmail, record.adviserEmail)) {
            result = result && updateEmail(cache, record.stuId, liveReg.email, liveReg.adviserEmail);
        }

        if (mismatch(liveReg.campus, record.campus)) {
            result = result && updateCampus(cache, record.stuId, liveReg.campus);
        }

        if (mismatch(liveReg.admitType, record.admitType)) {
            if (result) {
                // TODO: Get a real "admitted" value here, and add to RawStudent
                updateAdmission(cache, record.stuId, liveReg.admitType);
            }
        }
    }

    /**
     * Tests for a mismatch where the live data object has a non-null value that differs from the local data object.
     *
     * @param live  the live object value
     * @param local the local object value
     * @return true if the live object value is not null and differs from the local object value
     */
    private static boolean mismatch(final Object live, final Object local) {

        return live != null && !live.equals(local);
    }

    /**
     * Retrieves a test student.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return the test student; {@code null} if the student ID is not a valid test student ID
     * @throws SQLException if there is an error accessing the database
     */
    private static RawStudent getTestStudent(final Cache cache, final String studentId)
            throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        RawStudent result = null;

        if (active != null && studentId != null && studentId.length() == 9 && studentId.startsWith("99")) {

            final char ch3 = studentId.charAt(2);
            final char ch4 = studentId.charAt(3);

            if (ch3 == 'P' && ch4 == 'L') {
                result = getPlaceentTestStudent(active, studentId);
            } else if (ch3 == 'C' && ch4 == 'I') {
                result = getCheckinTestStudent(active, studentId);
            } else {
                result = queryTestStudent(cache, studentId);
            }
        } else {
            Log.warning("Invalid test student ID: ", studentId);
        }

        return result;
    }

    /**
     * Gets a test student configured to test some aspect of Math Placement.
     *
     * @param active    the active term
     * @param studentId the student ID
     * @return the test student; {@code null} if the student ID is not a valid test student ID
     */
    private static RawStudent getPlaceentTestStudent(final TermRec active, final String studentId) {

        RawStudent result = null;

        // 99PL - the "Student" record does not encode number of attempts on record
        // or placement outcomes, so we simply test those digits for range, but it does contain
        // application term, so we look at digit 9 to drive data.

        if (validate99PLStudentId(studentId)) {
            final char ch8 = studentId.charAt(7);

            final TermKey aplnTerm;
            if (ch8 >= '0' && ch8 <= '3') {
                aplnTerm = active.term;
            } else if (ch8 >= '4' && ch8 <= '7') {
                aplnTerm = active.term.add(1);
            } else if (ch8 >= '8' && ch8 <= '9' || ch8 >= 'A' && ch8 <= 'B') {
                aplnTerm = active.term.add(2);
            } else if (ch8 >= 'C' && ch8 <= 'F') {
                aplnTerm = active.term.add(3);
            } else if (ch8 >= 'G' && ch8 <= 'J') {
                aplnTerm = active.term.add(4);
            } else if (ch8 >= 'K' && ch8 <= 'N') {
                aplnTerm = active.term.add(5);
            } else {
                aplnTerm = active.term.add(6);
            }

            result = makeTestStudent(studentId, aplnTerm, null, null);
        } else {
            Log.warning("Invalid test student ID: ", studentId);
        }

        return result;
    }

    /**
     * Validates a test student ID that begins with "99PL". There are only certain combinations of remaining digits that
     * constitute a valid ID.
     *
     * @param studentId the student ID
     * @return {@code true} if the ID is valid; {@code false} if not
     */
    static boolean validate99PLStudentId(final CharSequence studentId) {

        final char c5 = studentId.charAt(4);
        final char c6 = studentId.charAt(5);
        final char c7 = studentId.charAt(6);
        final char c9 = studentId.charAt(8);

        boolean valid = false;

        if (c5 == '0') {
            valid = c6 == '0' && c7 == '0' && c9 == '0';
        } else if (c5 == '3') {
            valid = c6 >= '0' && c6 <= '8' && c7 == '0';
        } else if (c5 >= '1' && c5 <= '5') {
            if (c6 == '0' || c6 == '1') {
                valid = c7 == '0';
            } else if (c6 == '2') {
                valid = c7 == '0' || c7 == '2';
            } else if (c6 == '3') {
                valid = c7 == '0' || c7 == '2' || c7 == '3';
            } else if (c6 == '4') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '4';
            } else if (c6 == '5') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '5';
            } else if (c6 == '6') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '4' || c7 == '5' || c7 == '6';
            } else if (c6 == '7') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '5' || c7 == '7';
            } else if (c6 == '8') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '4' || c7 == '5' || c7 == '6' || c7 == '7'
                        || c7 == '8';
            }
        }

        if (valid) {
            final char c8 = studentId.charAt(7);
            valid = c8 >= '0' && c8 <= '9' || c8 >= 'A' && c8 <= 'R';

            if (c6 == '0') {
                if (c9 != '0' && c9 != '1') {
                    valid = false;
                }
            } else if (c6 == '1') {
                if (c9 != '0' && c9 != '2') {
                    valid = false;
                }
            } else if (c6 == '2') {
                if (c9 != '0' && c9 != '3') {
                    valid = false;
                }
            } else if (c6 == '3' || c6 == '5' || c6 == '7') {
                if (c9 != '0' && c9 != '4') {
                    valid = false;
                }
            } else if (c6 == '4') {
                if (c9 != '0' && c9 != '5') {
                    valid = false;
                }
            } else if (c6 == '6') {
                if (c9 != '0' && c9 != '6') {
                    valid = false;
                }
            } else if (c9 != '0') {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Gets a test student for Checkin testing (IDs beginning with "99CI").
     *
     * @param active    the active term
     * @param studentId the student ID
     * @return the test student; {@code null} if the student ID is not a valid test student ID
     */
    private static RawStudent getCheckinTestStudent(final TermRec active, final String studentId) {

        TermKey upcomingFall = null;

        if (active != null) {
            // Determine the "upcoming Fall" and "upcoming Spring" terms
            final int upcomingFallYear;
            if (active.term.name == ETermName.FALL) {
                upcomingFallYear = active.term.year.intValue() + 1;
            } else {
                upcomingFallYear = active.term.year.intValue();
            }

            upcomingFall = new TermKey(ETermName.FALL, upcomingFallYear);
        }

        RawStudent result = null;

        final char ch5 = studentId.charAt(4);
        final char ch6 = studentId.charAt(5);

        if (ch5 == 'M' && ch6 == 'P') {
            // Math Placement test cases

            // Last three digits are test case. Last digit dictates content of the Student record.
            final char ch9 = studentId.charAt(8);

            if (ch9 == '0' || ch9 == '2' || ch9 == '3' || ch9 == '5' || ch9 == '6') {
                result = makeTestStudent(studentId, new TermKey(ETermName.FALL, 2000), null, null);
            } else if (ch9 == '1' || ch9 == '4') {
                result = makeTestStudent(studentId, upcomingFall, null, null);
            } else if (ch9 == '7') {
                result = makeTestStudent(studentId, upcomingFall, "F", null);
            } else if (ch9 == '8') {
                result = makeTestStudent(studentId, upcomingFall, null, Float.valueOf(2.0f));
            } else {
                Log.warning("Invalid test student ID: ", studentId);
            }
        } else if (ch5 == 'C' && ch6 == 'H') {
            // Challenge Exam test cases

            // Only '001' has a 2x time limit factor - all others have same student data
            if (studentId.endsWith("001")) {
                result = makeTestStudent(studentId, new TermKey(ETermName.FALL, 2000), null, Float.valueOf(2.0f));
            } else {
                result = makeTestStudent(studentId, new TermKey(ETermName.FALL, 2000), null, null);
            }
        } else {
            Log.warning("Invalid test student ID: ", studentId);
        }

        return result;
    }

    /**
     * Makes a single test student record.
     *
     * @param studentId       the student ID
     * @param applicationTerm the student's application term
     * @param holdSeverity    the severity of the most severe hold on the student account ("F" or "N" if a hold exists,
     *                        null if not)
     * @param timelimitFactor the student's exam time-limit factor
     * @return the record
     */
    private static RawStudent makeTestStudent(final String studentId, final TermKey applicationTerm,
                                              final String holdSeverity, final Float timelimitFactor) {

        final String[] firstAndLast = genStudentName(studentId);

        int internal = studentId.hashCode() & 0x7FFFFFFF;
        if (internal < 100000000) {
            internal += 100000000;
        }

        return new RawStudent(studentId, Integer.valueOf(internal), firstAndLast[1], firstAndLast[0], null, null,
                applicationTerm, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, "N", null, holdSeverity, timelimitFactor, "N", null, null, null, null, null,
                "N", null, LocalDate.now(), null, null);
    }

    /**
     * Queries a pre-defined test student.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    private static RawStudent queryTestStudent(final Cache cache, final String studentId) throws SQLException {

        RawStudent result = null;

        final TermRec active = cache.getSystemData().getActiveTerm();
        TermKey upcomingFall = null;
        TermKey upcomingSpring = null;

        if (active != null) {
            // Determine the "upcoming Fall" and "upcoming Spring" terms
            final int upcomingFallYear;
            if (active.term.name == ETermName.FALL) {
                upcomingFallYear = active.term.year.intValue() + 1;
            } else {
                upcomingFallYear = active.term.year.intValue();
            }
            final int upcomingSpringYear = active.term.year.intValue() + 1;

            upcomingFall = new TermKey(ETermName.FALL, upcomingFallYear);
            upcomingSpring = new TermKey(ETermName.SPRING, upcomingSpringYear);
        }

        if (active != null) {
            final String lastName = "Student " + studentId.substring(3);

            final TermKey applicationTerm;
            final TermKey gradTerm;

            if (UPCOMING_FALL_ADMITS.contains(studentId)) {
                applicationTerm = upcomingFall;
                gradTerm = new TermKey(ETermName.SPRING, active.term.year.intValue() + 4);
            } else if (UPCOMING_SPRING_ADMITS.contains(studentId)) {
                applicationTerm = upcomingSpring;
                gradTerm = new TermKey(ETermName.FALL, active.term.year.intValue() + 3);
            } else if (active.term.name == ETermName.SPRING) {
                applicationTerm = active.term;
                gradTerm = new TermKey(ETermName.FALL, active.term.year.intValue() + 3);
            } else {
                applicationTerm = active.term;
                gradTerm = new TermKey(ETermName.SPRING, active.term.year.intValue() + 4);
            }

            result = new RawStudent(studentId, null, lastName, "Sample", null, null, applicationTerm, null, null, null,
                    PROGRAM_CODES.get(studentId), null, gradTerm, null, null, null, null, null, null, null, null, null,
                    null, null, null, "N", null, null, null, "Y", null, null, "my_adviser@fake.colostate.edu", null,
                    null, "N", null, LocalDate.now(), null, null);
        }

        return result;
    }

    /**
     * Generates a random (repeatable) student name based on the student ID.
     *
     * @param studentId the student ID
     * @return the student name (a two-String array with first and last name)
     */
    static String[] genStudentName(final String studentId) {

        // Fixed seed for repeatable sequence of names
        final RandomGenerator rnd = new Random(studentId.hashCode());
        final HtmlBuilder htm = new HtmlBuilder(21);

        final String[] firstAndLast = new String[2];

        // Generate first and last name
        int size = 5 + rnd.nextInt(4);
        int skip = rnd.nextInt(2);
        for (int i = 0; i < size; i += 2) {
            if (i > size - 3 || rnd.nextBoolean()) {
                htm.add(CONSONANTS[rnd.nextInt(CONSONANTS.length)]);
            } else {
                final int doubleIndex = rnd.nextInt(DOUBLES.length / 2);
                htm.add(DOUBLES[(doubleIndex << 1)]);
                htm.add(DOUBLES[(doubleIndex << 1) + 1]);
            }
            htm.add(VOWELS[rnd.nextInt(VOWELS.length)]);
        }
        firstAndLast[0] = Character.toUpperCase(htm.charAt(skip)) + htm.toString().substring(skip + 1, size);

        htm.reset();
        size = 4 + rnd.nextInt(7);
        skip = rnd.nextInt(3);
        for (int i = 0; i < size; i += 2) {
            htm.add(CONSONANTS[rnd.nextInt(CONSONANTS.length)]);
            htm.add(VOWELS[rnd.nextInt(VOWELS.length)]);
        }
        firstAndLast[1] = Character.toUpperCase(htm.charAt(skip)) + htm.toString().substring(skip + 1, size);

        return firstAndLast;
    }

    /**
     * Generates a "fake" student record with minimal fields.
     *
     * @param studentId the student ID
     * @param lastName  the last name
     * @param firstName the first name
     * @return the student record
     */
    public static RawStudent makeFakeStudent(final String studentId, final String lastName,
                                             final String firstName) {

        final RawStudent result = new RawStudent(studentId, null, lastName, firstName, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, "N", null, null, null, "Y",
                null, null, null, null, null, "N",
                RawPacingStructure.GUEST_PACING_STRUCTURE, LocalDate.now(), null, null);

        result.synthetic = true;

        return result;
    }
}
