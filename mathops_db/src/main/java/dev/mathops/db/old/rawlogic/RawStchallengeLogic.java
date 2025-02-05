package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility class to work with "stchallenge" and "stchallengeqa" records.
 *
 * <pre>
 * Table:  'stchallenge'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * version              char(5)                   no
 * academic_yr          char(4)                   no
 * exam_dt              date                      no      PK
 * start_time           integer                   yes
 * finish_time          integer                   no      PK
 * last_name            char(30)                  yes
 * first_name           char(30)                  yes
 * middle_initial       char(1)                   yes
 * seq_nbr              smallint                  yes
 * serial_nbr           integer                   yes
 * score                smallint                  yes
 * passed               char(1)                   no
 * how_validated        char(1)                   yes
 *
 *
 * Table:  'stchallengeqa'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * version              char(5)                   no
 * exam_dt              date                      no      PK
 * finish_time          integer                   no      PK
 * question_nbr         smallint                  no      PK
 * stu_answer           char(5)                   yes
 * ans_correct          char(1)                   yes
 * </pre>
 */
public enum RawStchallengeLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStchallenge record) throws SQLException {

        if (record.stuId == null || record.course == null || record.version == null || record.academicYr == null
                || record.examDt == null || record.finishTime == null || record.passed == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO stchallenge (stu_id,course,version,academic_yr,exam_dt,",
                "start_time,finish_time,last_name,first_name,middle_initial,seq_nbr,",
                "serial_nbr,score,passed,how_validated) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlStringValue(record.version), ",",
                LogicUtils.sqlStringValue(record.academicYr), ",",
                LogicUtils.sqlDateValue(record.examDt), ",",
                LogicUtils.sqlIntegerValue(record.startTime), ",",
                LogicUtils.sqlIntegerValue(record.finishTime), ",",
                LogicUtils.sqlStringValue(record.lastName), ",",
                LogicUtils.sqlStringValue(record.firstName), ",",
                LogicUtils.sqlStringValue(record.middleInitial), ",",
                LogicUtils.sqlIntegerValue(record.seqNbr), ",",
                LogicUtils.sqlLongValue(record.serialNbr), ",",
                LogicUtils.sqlIntegerValue(record.score), ",",
                LogicUtils.sqlStringValue(record.passed), ",",
                LogicUtils.sqlStringValue(record.howValidated), ")");

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
    public static boolean delete(final Cache cache, final RawStchallenge record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stchallenge ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND exam_dt=", LogicUtils.sqlDateValue(record.examDt),
                "  AND finish_time=", LogicUtils.sqlIntegerValue(record.finishTime));

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
     * Deletes a record and the related stchallengeqa records.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @throws SQLException if there is an error accessing the database
     */
    public static void deleteAttemptAndAnswers(final Cache cache, final RawStchallenge record) throws SQLException {

        RawStchallengeqaLogic.deleteAllForAttempt(cache, record);

        delete(cache, record);
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStchallenge> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM stchallenge");
    }

    /**
     * Retrieves all Challenge exams for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of matching RawStchallenge records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawStchallenge> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final List<RawStchallenge> result = new ArrayList<>(5);

        if (stuId.startsWith("99")) {
            if (stuId.length() == 9) {
                final char ch3 = stuId.charAt(2);
                final char ch4 = stuId.charAt(3);

                if (ch3 == 'C' && ch4 == 'I') {
                    getCheckinTestStudent(stuId, result);
                }
            } else {
                Log.warning("Invalid test student ID: " + stuId);
            }
        } else {
            final String sql = "SELECT * FROM stchallenge WHERE stu_id=" + LogicUtils.sqlStringValue(stuId);

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    result.add(RawStchallenge.fromResultSet(rs));
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }

        return result;
    }

    /**
     * Retrieves all challenge exam attempts for a student in a course.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course
     * @return the list of matching RawStchallenge records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawStchallenge> queryByStudentCourse(final Cache cache, final String stuId,
                                                            final String course) throws SQLException {

        final List<RawStchallenge> result = new ArrayList<>(2);

        if (stuId.startsWith("99")) {

            if (stuId.length() == 9) {
                final char ch3 = stuId.charAt(2);
                final char ch4 = stuId.charAt(3);

                if (ch3 == 'C' && ch4 == 'I') {
                    getCheckinTestStudent(stuId, course, result);
                }
            } else {
                Log.warning("Invalid test student ID: " + stuId);
            }

        } else {
            final String sql = "SELECT * FROM stchallenge WHERE stu_id="
                    + LogicUtils.sqlStringValue(stuId) + " AND course=" + LogicUtils.sqlStringValue(course);

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    result.add(RawStchallenge.fromResultSet(rs));
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }
        return result;
    }

    /**
     * Queries for the students with test data relating to a checkin (IDs beginning with "99CI").
     *
     * @param studentId the ID of the student to query
     * @param result    the list to which to add results
     */
    private static void getCheckinTestStudent(final String studentId,
                                              final Collection<? super RawStchallenge> result) {

        final char ch5 = studentId.charAt(4);
        final char ch6 = studentId.charAt(5);

        if (ch5 == 'C' && ch6 == 'H') {
            // Math Challenge test cases

            final String test = studentId.substring(6);

            switch (test) {
                case "019" -> result.add(makeRecord(studentId, RawRecordConstants.M117,
                        ChallengeExamLogic.M117_CHALLENGE_EXAM_ID));
                case "026" -> result.add(makeRecord(studentId, RawRecordConstants.M118,
                        ChallengeExamLogic.M118_CHALLENGE_EXAM_ID));
                case "036" -> result.add(makeRecord(studentId, RawRecordConstants.M124,
                        ChallengeExamLogic.M124_CHALLENGE_EXAM_ID));
                case "046" -> result.add(makeRecord(studentId, RawRecordConstants.M125,
                        ChallengeExamLogic.M125_CHALLENGE_EXAM_ID));
                case "056" -> result.add(makeRecord(studentId, RawRecordConstants.M126,
                        ChallengeExamLogic.M126_CHALLENGE_EXAM_ID));
            }
        }
    }

    /**
     * Queries for the students with test data relating to a checkin (IDs beginning with "99CI").
     *
     * @param stuId  the ID of the student to query
     * @param course the ID of the course to query
     * @param result the list to which to add results
     */
    private static void getCheckinTestStudent(final String stuId, final String course,
                                              final Collection<? super RawStchallenge> result) {

        final char ch5 = stuId.charAt(4);
        final char ch6 = stuId.charAt(5);

        if (ch5 == 'C' && ch6 == 'H') {
            // Math Challenge test cases

            final String test = stuId.substring(6);

            if ("019".equals(test)) {
                if (RawRecordConstants.M117.equals(course)) {
                    result.add(makeRecord(stuId, RawRecordConstants.M117, ChallengeExamLogic.M117_CHALLENGE_EXAM_ID));
                }
            } else if ("026".equals(test)) {
                if (RawRecordConstants.M118.equals(course)) {
                    result.add(makeRecord(stuId, RawRecordConstants.M118, ChallengeExamLogic.M118_CHALLENGE_EXAM_ID));
                }
            } else if ("036".equals(test)) {
                if (RawRecordConstants.M124.equals(course)) {
                    result.add(makeRecord(stuId, RawRecordConstants.M124, ChallengeExamLogic.M124_CHALLENGE_EXAM_ID));
                }
            } else if ("046".equals(test)) {
                if (RawRecordConstants.M125.equals(course)) {
                    result.add(makeRecord(stuId, RawRecordConstants.M125, ChallengeExamLogic.M125_CHALLENGE_EXAM_ID));
                }
            } else if ("056".equals(test) && RawRecordConstants.M126.equals(course)) {
                result.add(makeRecord(stuId, RawRecordConstants.M126, ChallengeExamLogic.M126_CHALLENGE_EXAM_ID));
            }
        }
    }

    /**
     * Makes a challenge attempt record.
     *
     * @param stuId    the student ID
     * @param courseId the course ID
     * @param examId   the exam ID
     * @return the record
     */
    private static RawStchallenge makeRecord(final String stuId, final String courseId, final String examId) {

        final LocalDateTime now = LocalDateTime.now();
        final String[] firstLast = RawStudentLogic.genStudentName(stuId);
        final int finish = -now.getHour() * 60 + now.getMinute();

        return new RawStchallenge(stuId, courseId, examId, "1718", now.toLocalDate(), Integer.valueOf(finish - 60),
                Integer.valueOf(finish), firstLast[1], firstLast[0], null, null,
                Long.valueOf(Integer.MAX_VALUE + stuId.hashCode()), Integer.valueOf(0), "N", "P");
    }

    /**
     * Queries for all legal Challenge attempts for the student for a particular exam ID.
     *
     * @param cache  the data cache
     * @param stuId  the ID of the student to query
     * @param course the course ID for which to query
     * @return the number of legal attempts
     * @throws SQLException if there is an error performing the query
     */
    public static int countLegalAttempts(final Cache cache, final String stuId, final String course)
            throws SQLException {

        final int result;

        if (stuId.startsWith("99")) {

            final char ch5 = stuId.charAt(4);
            final char ch6 = stuId.charAt(5);

            if (ch5 == 'C' && ch6 == 'H') {
                final String test = stuId.substring(6);

                result = switch (test) {
                    case "019" -> RawRecordConstants.M117.equals(course) ? 1 : 0;
                    case "026" -> RawRecordConstants.M118.equals(course) ? 1 : 0;
                    case "036" -> RawRecordConstants.M124.equals(course) ? 1 : 0;
                    case "046" -> RawRecordConstants.M125.equals(course) ? 1 : 0;
                    case "056" -> RawRecordConstants.M126.equals(course) ? 1 : 0;
                    default -> 0;
                };
            } else {
                result = 0;
            }

        } else {
            // Count legal attempts
            final String sql1 = "SELECT COUNT(*) FROM stchallenge WHERE stu_id=" + LogicUtils.sqlStringValue(stuId)
                    + " AND course=" + LogicUtils.sqlStringValue(course) + " AND (passed='Y' OR passed='N')";

            result = LogicUtils.executeSimpleIntQuery(cache, sql1).intValue();
        }

        return result;
    }

    /**
     * Retrieves all {@code StudentChallengeAttempt} records created in the past N days (counting today), organized into
     * one list per day, sorted by activity date.
     *
     * @param cache   the data cache
     * @param history the list to which to add N lists of records
     * @param numDays the number of days (N)
     * @param today   today's date
     * @throws SQLException if there is an error performing the query
     */
    public static void getHistory(final Cache cache, final Collection<? super List<RawStchallenge>> history,
                                  final int numDays, final LocalDate today) throws SQLException {

        history.clear();

        // Get the earliest date for which to return data...
        LocalDate earliest;
        if (numDays <= 1) {
            earliest = today;
        } else {
            earliest = today.minus(Period.ofDays(numDays - 1));
        }

        final List<RawStchallenge> all = queryOnOrAfter(cache, earliest);

        // Organize by day

        all.sort(new RawStchallenge.FinishDateTimeComparator());

        int start = 0;
        int position = 0;
        final int size = all.size();
        for (int i = 0; i < numDays; ++i) {

            while (position < size && all.get(position).examDt.equals(earliest)) {
                ++position;
            }

            final List<RawStchallenge> daily = new ArrayList<>(position - start);
            for (int j = start; j < position; ++j) {
                daily.add(all.get(j));
            }
            history.add(daily);

            start = position;

            earliest = earliest.plusDays(1L);
        }
    }

    /**
     * Retrieves all records that occurred on or after a specified date.
     *
     * @param cache    the data cache
     * @param earliest the earliest date to return
     * @return the list of matching records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawStchallenge> queryOnOrAfter(final Cache cache, final LocalDate earliest) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stchallenge WHERE exam_dt>=",
                LogicUtils.sqlDateValue(earliest));

        return executeQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache    the data cache
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStchallenge> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStchallenge> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStchallenge.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

}
