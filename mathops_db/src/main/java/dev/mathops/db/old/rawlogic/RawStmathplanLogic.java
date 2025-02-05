package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to work with "stmathplan" records.
 *
 * <pre>
 * Table:  'stmathplan'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no
 * pidm                 integer                   no
 * apln_term            char(4)                   yes
 * version              char(5)                   no
 * exam_dt              date                      no
 * survey_nbr           smallint                  no
 * stu_answer           char(50)                  yes
 * finish_time          integer                   no
 * session              bigint                    yes
 * </pre>
 */
public enum RawStmathplanLogic {
    ;

    /** An unmodifiable empty list that can be returned for. */
    private static final List<RawStmathplan> EMPTY = Collections.unmodifiableList(new ArrayList<>(0));

    /** Map from student ID to map from page ID to list of math plan records. */
    private static final Map<String, Map<String, List<RawStmathplan>>> DATA;

    static {
        final String fa19 = "FA19";
        final String sp19 = "SP19";

        DATA = new HashMap<>(100);

        makeStudentMap(999011111, fa19, 3010);

        makeStudentMap(999011121, fa19, 7131);
        makeStudentMap(999011122, fa19, 3020);
        makeStudentMap(999011123, fa19, 3010);
        makeStudentMap(999011124, fa19, 3041);
        makeStudentMap(999011125, fa19, 3051);
        makeStudentMap(999011126, fa19, 3052);

        makeStudentMap(999011131, fa19, 3053);
        makeStudentMap(999011132, fa19, 3060);
        makeStudentMap(999011133, fa19, 3070);

        makeStudentMap(999011141, fa19, 7061);
        makeStudentMap(999011142, fa19, 7062);

        makeStudentMap(999011211, sp19, 7064);
        makeStudentMap(999011221, sp19, 7064);
        makeStudentMap(999011231, sp19, 7065);
        makeStudentMap(999011241, sp19, 3080);

        makeStudentMap(999011311, sp19, 7061);
        makeStudentMap(999011321, sp19, 7061);
        makeStudentMap(999011331, sp19, 7065);
        makeStudentMap(999011341, sp19, 3080);

        makeStudentMap(999012111, fa19, 7065);
        makeStudentMap(999012121, fa19, 7065);
        makeStudentMap(999012122, fa19, 7065);
        makeStudentMap(999012123, fa19, 7065);
        makeStudentMap(999012124, fa19, 7065);
        makeStudentMap(999012125, fa19, 7065);
        makeStudentMap(999012126, fa19, 7065);
        makeStudentMap(999012131, fa19, 7065);
        makeStudentMap(999012132, fa19, 7065);
        makeStudentMap(999012133, fa19, 7065);
        makeStudentMap(999012141, fa19, 7065);
        makeStudentMap(999012142, fa19, 7065);
        makeStudentMap(999012211, sp19, 7065);
        makeStudentMap(999012221, sp19, 7065);
        makeStudentMap(999012231, sp19, 7065);
        makeStudentMap(999012241, sp19, 7065);

        makeStudentMap(999012311, sp19, 7065);
        makeStudentMap(999012321, sp19, 7065);
        makeStudentMap(999012331, sp19, 7065);
        makeStudentMap(999012341, sp19, 7065);

        makeStudentMap(999012411, fa19, 7051);
        makeStudentMap(999012421, fa19, 7052);
        makeStudentMap(999012422, fa19, 7053);
        makeStudentMap(999012423, fa19, 7054);
        makeStudentMap(999012424, fa19, 7051);
        makeStudentMap(999012425, fa19, 7051);
        makeStudentMap(999012426, fa19, 7051);
        makeStudentMap(999012431, fa19, 7051);
        makeStudentMap(999012432, fa19, 7051);
        makeStudentMap(999012433, fa19, 7051);
        makeStudentMap(999012441, fa19, 7051);
        makeStudentMap(999012442, fa19, 7051);
        makeStudentMap(999012451, sp19, 7051);
        makeStudentMap(999012461, sp19, 7051);
        makeStudentMap(999012471, sp19, 7051);
        makeStudentMap(999012481, sp19, 7051);

        makeStudentMap(999012511, sp19, 7065);
        makeStudentMap(999012521, sp19, 7065);
        makeStudentMap(999012531, sp19, 7065);
        makeStudentMap(999012541, sp19, 7065);

        makeStudentMap(999021111, fa19, 4060);
        makeStudentMap(999021121, fa19, 1080);
        makeStudentMap(999021131, fa19, 7095);
        makeStudentMap(999021141, fa19, 1061);
        makeStudentMap(999021151, fa19, 1043);

        makeStudentMap(999021211, fa19, 4080);
        makeStudentMap(999021221, fa19, 4050);
        makeStudentMap(999021231, fa19, 4051);
        makeStudentMap(999021241, fa19, 4052);

        makeStudentMap(999021251, fa19, 1011);
        makeStudentMap(999021261, fa19, 1050);
        makeStudentMap(999021271, fa19, 1050);
        makeStudentMap(999021281, fa19, 1050);

        makeStudentMap(999021311, fa19, 4100);
        makeStudentMap(999021411, fa19, 5001);

        makeStudentMap(999022111, sp19, 4060);
        makeStudentMap(999022121, sp19, 1080);
        makeStudentMap(999022131, sp19, 7095);
        makeStudentMap(999022141, sp19, 1061);
        makeStudentMap(999022151, sp19, 1043);

        makeStudentMap(999022211, sp19, 4080);
        makeStudentMap(999022221, sp19, 4050);
        makeStudentMap(999022231, sp19, 4051);
        makeStudentMap(999022241, sp19, 4052);

        makeStudentMap(999022251, sp19, 1011);
        makeStudentMap(999022261, sp19, 1050);
        makeStudentMap(999022271, sp19, 1050);
        makeStudentMap(999022281, sp19, 1050);

        makeStudentMap(999022311, sp19, 4100);
        makeStudentMap(999022411, sp19, 5001);
    }

    /**
     * Creates the math plan response maps for a single student.
     *
     * @param studentId    the student
     * @param appTerm      the student's application term
     * @param programCodes the list of program codes
     */
    private static void makeStudentMap(final int studentId, final String appTerm, final int... programCodes) {

        final Map<String, List<RawStmathplan>> map = new HashMap<>(1);

        map.put("WLCM1", makeWLCM1(studentId, appTerm, programCodes));
        map.put("WLCM3", makeWLCM3(studentId, appTerm));
        map.put("WLCM4", makeWLCM4(studentId, appTerm));
        map.put("WLCM5", makeWLCM5(studentId, appTerm));
        map.put("WLCM6", makeWLCM6(studentId, appTerm));

        DATA.put(Integer.toString(studentId), map);
    }

    /**
     * Creates a list of {@code RawStmathplan} records with "WLCM1" as their page ID and with a list of program codes.
     *
     * @param studentId    the student ID
     * @param appTerm      the student's application term
     * @param programCodes the list of program codes
     * @return the list of {@code RawStmathplan} records
     */
    private static List<RawStmathplan> makeWLCM1(final int studentId, final String appTerm, final int... programCodes) {

        final List<RawStmathplan> result = new ArrayList<>(programCodes.length);

        final LocalDateTime now = LocalDateTime.now();

        final String stuIdString = Integer.toString(studentId);
        for (final int pc : programCodes) {
            result.add(new RawStmathplan(stuIdString, Integer.valueOf(studentId), appTerm, "WLCM1", now.toLocalDate(),
                    Integer.valueOf(pc), "Y", Integer.valueOf(TemporalUtils.minuteOfDay(now)), null));
        }

        return result;

    }

    /**
     * Creates a list of {@code RawStmathplan} records with "WLCM3" as their page ID.
     *
     * @param studentId the student ID
     * @param appTerm   the student's application term
     * @return the list of {@code RawStmathplan} records
     */
    private static List<RawStmathplan> makeWLCM3(final int studentId, final String appTerm) {

        final List<RawStmathplan> result = new ArrayList<>(1);

        final LocalDateTime now = LocalDateTime.now();

        result.add(new RawStmathplan(Integer.toString(studentId), Integer.valueOf(studentId), appTerm, "WLCM3",
                now.toLocalDate(), Integer.valueOf(1), "Y", Integer.valueOf(TemporalUtils.minuteOfDay(now)), null));

        return result;
    }

    /**
     * Creates a list of {@code RawStmathplan} records with "WLCM4" as their page ID.
     *
     * @param studentId the student ID
     * @param appTerm   the student's application term
     * @return the list of {@code RawStmathplan} records
     */
    private static List<RawStmathplan> makeWLCM4(final int studentId, final String appTerm) {

        final List<RawStmathplan> result = new ArrayList<>(1);

        final LocalDateTime now = LocalDateTime.now();

        result.add(new RawStmathplan(Integer.toString(studentId), Integer.valueOf(studentId), appTerm, "WLCM4",
                now.toLocalDate(), Integer.valueOf(1), "Y", Integer.valueOf(TemporalUtils.minuteOfDay(now)), null));

        return result;
    }

    /**
     * Creates a list of {@code RawStmathplan} records with "WLCM5" as their page ID.
     *
     * @param studentId the student ID
     * @param appTerm   the student's application term
     * @return the list of {@code RawStmathplan} records
     */
    private static List<RawStmathplan> makeWLCM5(final int studentId, final String appTerm) {

        final List<RawStmathplan> result = new ArrayList<>(2);

        final LocalDateTime now = LocalDateTime.now();

        result.add(new RawStmathplan(Integer.toString(studentId), Integer.valueOf(studentId), appTerm, "WLCM5",
                now.toLocalDate(), Integer.valueOf(1), studentId % 2 == 0 ? "N" : "Y",
                Integer.valueOf(TemporalUtils.minuteOfDay(now)), null));

        result.add(new RawStmathplan(Integer.toString(studentId), Integer.valueOf(studentId), appTerm, "WLCM5",
                now.toLocalDate(), Integer.valueOf(2), studentId / 2 % 2 == 0 ? "N" : "Y",
                Integer.valueOf(TemporalUtils.minuteOfDay(now)), null));

        return result;
    }

    /**
     * Creates a list of {@code RawStmathplan} records with "WLCM6" as their page ID.
     *
     * @param studentId the student ID
     * @param appTerm   the student's application term
     * @return the list of {@code RawStmathplan} records
     */
    private static List<RawStmathplan> makeWLCM6(final int studentId, final String appTerm) {

        final List<RawStmathplan> result = new ArrayList<>(1);

        final LocalDateTime now = LocalDateTime.now();

        result.add(new RawStmathplan(Integer.toString(studentId), Integer.valueOf(studentId), appTerm, "WLCM6",
                now.toLocalDate(), Integer.valueOf(1), "Y", Integer.valueOf(TemporalUtils.minuteOfDay(now)), null));

        return result;
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStmathplan record) throws SQLException {

        if (record.stuId == null || record.pidm == null || record.version == null
            || record.examDt == null || record.surveyNbr == null || record.finishTime == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO stmathplan (",
                "stu_id,pidm,apln_term,version,exam_dt,survey_nbr,stu_answer,",
                "finish_time,session) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlIntegerValue(record.pidm), ",",
                LogicUtils.sqlStringValue(record.aplnTerm), ",",
                LogicUtils.sqlStringValue(record.version), ",",
                LogicUtils.sqlDateValue(record.examDt), ",",
                LogicUtils.sqlIntegerValue(record.surveyNbr), ",",
                LogicUtils.sqlStringValue(record.stuAnswer), ",",
                LogicUtils.sqlIntegerValue(record.finishTime), ",",
                LogicUtils.sqlLongValue(record.session), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawStmathplan record)
            throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM stmathplan ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                " AND version=", LogicUtils.sqlStringValue(record.version),
                " AND exam_dt=", LogicUtils.sqlDateValue(record.examDt),
                " AND finish_time=", LogicUtils.sqlIntegerValue(record.finishTime),
                " AND survey_nbr=", LogicUtils.sqlIntegerValue(record.surveyNbr));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmathplan> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM stmathplan");
    }

    /**
     * Queries every record in the database.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the complete set of records in the database
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmathplan> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stmathplan ",
                " WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        return executeQuery(cache, sql);
    }

    /**
     * Queries for the most recent set of survey responses submitted by a student for a particular survey version.
     * Responses are ordered by question number.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student whose responses to retrieve
     * @param pageId    the ID of the page whose responses to retrieve
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmathplan> queryLatestByStudentPage(final Cache cache, final String studentId,
                                                               final String pageId) throws SQLException {

        final List<RawStmathplan> result;

        if (studentId.startsWith("99")) {
            result = queryLatestByTestStudentPage(studentId, pageId);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM stmathplan ",
                    " WHERE stu_id=", LogicUtils.sqlStringValue(studentId),
                    "   AND version=", LogicUtils.sqlStringValue(pageId),
                    "  AND exam_dt IN ",
                    "  (SELECT MAX(exam_dt) ",
                    "    FROM stmathplan WHERE stu_id=", LogicUtils.sqlStringValue(studentId),
                    "    AND version=", LogicUtils.sqlStringValue(pageId), ") ",
                    "  AND finish_time IN ",
                    "  (SELECT MAX(finish_time) FROM stmathplan WHERE exam_dt IN ",
                    "   (SELECT MAX(exam_dt) FROM stmathplan ",
                    "      WHERE stu_id=", LogicUtils.sqlStringValue(studentId),
                    "        AND version=", LogicUtils.sqlStringValue(pageId), ") ",
                    "   AND stu_id=", LogicUtils.sqlStringValue(studentId),
                    "   AND version=", LogicUtils.sqlStringValue(pageId),
                    "  ) ORDER BY survey_nbr");

            result = executeQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for the most recent set of survey responses submitted by a student for a particular survey version.
     * Responses are ordered by question number.
     *
     * @param cache  the data cache
     * @param pidm   the PIDM of the student whose responses to retrieve
     * @param pageId the ID of the page whose responses to retrieve
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmathplan> queryLatestByStudentPage(final Cache cache, final Integer pidm,
                                                               final String pageId) throws SQLException {

        final List<RawStmathplan> result;

        if (pidm.intValue() >= 990000000) {
            result = queryLatestByTestStudentPage(pidm, pageId);
        } else {

            final String sql = SimpleBuilder.concat("SELECT * FROM stmathplan ",
                    " WHERE pidm=", LogicUtils.sqlIntegerValue(pidm),
                    "   AND version=", LogicUtils.sqlStringValue(pageId),
                    "  AND exam_dt IN ",
                    "  (SELECT MAX(exam_dt) ",
                    "    FROM stmathplan WHERE pidm=", LogicUtils.sqlIntegerValue(pidm),
                    "    AND version=", LogicUtils.sqlStringValue(pageId), ") ",
                    "  AND finish_time IN ",
                    "  (SELECT MAX(finish_time) FROM stmathplan WHERE exam_dt IN ",
                    "   (SELECT MAX(exam_dt) FROM stmathplan ",
                    "      WHERE pidm=", LogicUtils.sqlIntegerValue(pidm),
                    "        AND version=", LogicUtils.sqlStringValue(pageId), ") ",
                    "   AND pidm=", LogicUtils.sqlIntegerValue(pidm),
                    "   AND version=", LogicUtils.sqlStringValue(pageId),
                    "  ) ORDER BY survey_nbr");

            result = executeQuery(cache, sql);
        }

        return result;
    }

    /**
     * Deletes all responses for a student relating to a page.
     *
     * @param cache  the data cache
     * @param stuId  the studentId whose responses to delete
     * @param pageId the page ID of the responses to delete
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean deleteAllForPage(final Cache cache, final String stuId,
                                           final String pageId) throws SQLException {

        final boolean result;

        if (stuId.startsWith("99")) {
            // Don't delete data for test IDs
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM stmathplan ",
                    "WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                    "  AND version=", LogicUtils.sqlStringValue(pageId));

            Log.info(sql);

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                result = stmt.executeUpdate(sql) > 0;

                if (result) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }

        return result;
    }

    /**
     * Retrieves all {@code StudentMathPlan} records created in the past N days (counting today), organized into one
     * list per day, sorted by activity date.
     *
     * @param cache   the data cache
     * @param history the list to which to add N lists of records
     * @param numDays the number of days (N)
     * @param today   today's date
     * @throws SQLException if there is an error accessing the database
     */
    public static void getHistory(final Cache cache, final Collection<? super List<RawStmathplan>> history,
                                  final int numDays, final LocalDate today) throws SQLException {

        // Get the earliest date for which to return data...
        LocalDate earliest;
        if (numDays <= 1) {
            earliest = today;
        } else {
            earliest = today.minus(Period.ofDays(numDays - 1));
        }

        final String sql = SimpleBuilder.concat("SELECT * FROM stmathplan WHERE exam_dt>=",
                LogicUtils.sqlDateValue(earliest));

        final List<RawStmathplan> all = executeQuery(cache, sql);
        Collections.sort(all);

        int start = 0;
        int position = 0;
        final int size = all.size();
        for (int i = 0; i < numDays; ++i) {

            while (position < size && all.get(position).examDt.equals(earliest)) {
                ++position;
            }

            final List<RawStmathplan> daily = new ArrayList<>(position - start);
            for (int j = start; j < position; ++j) {
                daily.add(all.get(j));
            }
            history.add(daily);
            start = position;

            earliest = earliest.plusDays(1L);
        }
    }

    /**
     * Queries for the most recent set of survey responses submitted by a student for a particular survey version.
     * Responses are ordered by question number.
     *
     * @param studentId the ID of the student whose responses to retrieve
     * @param pageId    the ID of the page whose responses to retrieve
     * @return the list of records that matched the criteria, a zero-length array if none matched
     */
    private static List<RawStmathplan> queryLatestByTestStudentPage(final String studentId, final String pageId) {

        List<RawStmathplan> result;

        final Map<String, List<RawStmathplan>> map = DATA.get(studentId);
        if (map == null) {
            result = EMPTY;
        } else {
            result = map.get(pageId);
            if (result == null) {
                result = EMPTY;
            }
        }

        return result;
    }

    /**
     * Queries for the most recent set of survey responses submitted by a student for a particular survey version.
     * Responses are ordered by question number.
     *
     * @param pidm   the PIDM of the student whose responses to retrieve
     * @param pageId the ID of the page whose responses to retrieve
     * @return the list of records that matched the criteria, a zero-length array if none matched
     */
    private static List<RawStmathplan> queryLatestByTestStudentPage(final Integer pidm, final String pageId) {

        List<RawStmathplan> result;

        final Map<String, List<RawStmathplan>> map = DATA.get(pidm.toString());
        if (map == null) {
            result = EMPTY;
        } else {
            result = map.get(pageId);
            if (result == null) {
                result = EMPTY;
            }
        }

        return result;
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStmathplan> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStmathplan> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStmathplan.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
