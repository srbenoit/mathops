package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawUsers;
import dev.mathops.db.rec.MasteryAttemptRec;
import dev.mathops.db.reclogic.MasteryAttemptLogic;
import dev.mathops.text.builder.HtmlBuilder;
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
 * A utility class to work with "stexam" records.
 *
 * <pre>
 * Table:  'stexam'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * serial_nbr           integer                   no      PK
 * version              char(5)                   no
 * stu_id               char(9)                   no
 * exam_dt              date                      no
 * exam_score           smallint                  no
 * mastery_score        smallint                  yes
 * start_time           integer                   no
 * finish_time          integer                   no
 * time_ok              char(1)                   no
 * passed               char(1)                   no
 * seq_nbr              smallint                  yes
 * course               char(6)                   no
 * unit                 smallint                  no
 * exam_type            char(2)                   no
 * is_first_passed      char(1)                   yes
 * exam_source          char(2)                   yes
 * calc_nbr             char(7)                   yes
 * </pre>
 */
public enum RawStexamLogic {
    ;

    /** The types considered "unit" exams. */
    public static final String[] UNIT_EXAM_TYPES = {"U", "F"};

    /** The types considered "review" exams. */
    public static final String[] REVIEW_EXAM_TYPES = {"R"};

    /** All exam types considered. */
    public static final String[] ALL_EXAM_TYPES = {"U", "F", "R"};

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStexam record) throws SQLException {

        if (record.serialNbr == null || record.version == null || record.stuId == null
            || record.examDt == null || record.examScore == null || record.startTime == null
            || record.finishTime == null || record.timeOk == null || record.passed == null
            || record.course == null || record.unit == null || record.examType == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawStexam for test student:");
            Log.info("stu_id: ", record.stuId);
            result = false;
        } else {
            // Adjust serial number if needed to avoid collision with existing record
            Long ser = record.serialNbr;
            for (int i = 0; i < 1000; ++i) {
                final Integer existing = LogicUtils.executeSimpleIntQuery(cache,
                        "SELECT COUNT(*) FROM stexam WHERE serial_nbr=" + ser);

                if (existing == null || existing.longValue() == 0L) {
                    break;
                }
                ser = Long.valueOf(ser.longValue() + 1L);
            }

            // Exam date represents finish date; when the exam should be counted. If the exam was
            // submitted within 10 minutes after midnight, we want to allow it for the prior
            // day's deadlines, so we move the exam date back and adjust times to values beyond 1440
            // to show what adjustment was made.

            Integer start = record.startTime;
            Integer finish = record.finishTime;
            LocalDate examDate = record.examDt;

            if (start != null && finish != null && finish.intValue() <= 10) {
                if (start.intValue() > 10) {
                    // started one day, finished in first 10 minutes of next
                    examDate = examDate.minusDays(1L);
                } else {
                    // Started very early in day, finished within first 10 minutes of day
                    examDate = examDate.minusDays(1L);
                    start = Integer.valueOf(1440 + start.intValue());
                }
                finish = Integer.valueOf(1440 + finish.intValue());
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO stexam (serial_nbr,version,stu_id,exam_dt,exam_score,",
                    "mastery_score,start_time,finish_time,time_ok,passed,seq_nbr,course,",
                    "unit,exam_type,is_first_passed,exam_source,calc_nbr) VALUES (",
                    LogicUtils.sqlLongValue(ser), ",",
                    LogicUtils.sqlStringValue(record.version), ",",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlDateValue(examDate), ",",
                    LogicUtils.sqlIntegerValue(record.examScore), ",",
                    LogicUtils.sqlIntegerValue(record.masteryScore), ",",
                    LogicUtils.sqlIntegerValue(start), ",",
                    LogicUtils.sqlIntegerValue(finish), ",",
                    LogicUtils.sqlStringValue(record.timeOk), ",",
                    LogicUtils.sqlStringValue(record.passed), ",",
                    LogicUtils.sqlIntegerValue(record.seqNbr), ",",
                    LogicUtils.sqlStringValue(record.course), ",",
                    LogicUtils.sqlIntegerValue(record.unit), ",",
                    LogicUtils.sqlStringValue(record.examType), ",",
                    LogicUtils.sqlStringValue(record.isFirstPassed), ",",
                    LogicUtils.sqlStringValue(record.examSource), ",",
                    LogicUtils.sqlStringValue(record.calcNbr), ")");

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
        }

        return result;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawStexam record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stexam",
                " WHERE serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr),
                " AND version=", LogicUtils.sqlStringValue(record.version),
                " AND stu_id=", LogicUtils.sqlStringValue(record.stuId));

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
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM stexam");
    }

    /**
     * Queries for all exam records for a student. Results are sorted by exam date, then finish time.
     *
     * @param cache the data cache
     * @param stuId the student for which to query exams
     * @param all   {@code true} to include all exams, {@code false} to include only those with passing status of "Y" or
     *              "N"
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> queryByStudent(final Cache cache, final String stuId,
                                                 final boolean all) throws SQLException {

        final List<RawStexam> result;

        if (stuId.startsWith("99")) {
            result = queryTestDatByStudent(stuId);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM stexam ",
                    "WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                    (all ? CoreConstants.EMPTY : " AND (passed='Y' OR passed='N')"),
                    " ORDER BY exam_dt,finish_time");

            result = executeQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for all exam records for a student in a particular course. Results are sorted by exam date, then finish
     * time.
     *
     * @param cache  the data cache
     * @param stuId  the student for which to query exams
     * @param course the course for which to query exams
     * @param all    {@code true} to include all exams, {@code false} to include only those with passing status of "Y"
     *               or "N"
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> queryByStudentCourse(final Cache cache, final String stuId,
                                                       final String course, final boolean all) throws SQLException {

        final List<RawStexam> result;

        if (stuId.startsWith("99")) {
            result = queryByTestStudentCourse(stuId, course);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM stexam ",
                    " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                    "   AND course=", LogicUtils.sqlStringValue(course),
                    (all ? CoreConstants.EMPTY : " AND (passed='Y' OR passed='N')"),
                    " ORDER BY exam_dt,finish_time");

            result = executeQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for all exam records for a course. Results are sorted by exam date then finish time.
     *
     * @param cache      the data cache
     * @param courseId   the course for which to query exams
     * @param includeAll {@code true} to include all exams, {@code false} to include only those with passing status of
     *                   "Y" or "N"
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> queryByCourse(final Cache cache, final String courseId,
                                                final boolean includeAll) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stexam ",
                " WHERE course=", LogicUtils.sqlStringValue(courseId),
                (includeAll ? CoreConstants.EMPTY : " AND (passed='Y' OR passed='N')"),
                " ORDER BY exam_dt,finish_time");

        return executeQuery(cache, sql);
    }

    /**
     * Retrieves all {@code RawStexam} records created in the past N days (counting today) for a specified course,
     * organized into one list per day, sorted by activity date.
     *
     * @param cache   the data cache
     * @param history the list to which to add N lists of records
     * @param numDays the number of days (N)
     * @param today   today's date
     * @param courses the ID of the course for which to gather history
     * @throws SQLException if there is an error accessing the database
     */
    public static void getHistory(final Cache cache, final Collection<? super List<RawStexam>> history,
                                  final int numDays, final LocalDate today, final String... courses) throws SQLException {

        // Get the earliest date for which to return data...
        LocalDate earliest;
        if (numDays <= 1) {
            earliest = today;
        } else {
            earliest = today.minus(Period.ofDays(numDays - 1));
        }

        final HtmlBuilder sql = new HtmlBuilder(200);
        sql.add("SELECT * FROM stexam ");

        final int numCourses = courses.length;
        if (numCourses == 1) {
            sql.add(" WHERE course=", LogicUtils.sqlStringValue(courses[0]));
        } else {
            sql.add(" WHERE course IN (", LogicUtils.sqlStringValue(courses[0]));
            for (int i = 1; i < numCourses; ++i) {
                sql.add(CoreConstants.COMMA_CHAR).add(LogicUtils.sqlStringValue(courses[i]));
            }
            sql.add(')');
        }

        sql.add(" AND exam_dt>=", LogicUtils.sqlDateValue(earliest),
                " ORDER BY exam_dt,finish_time");

        final List<RawStexam> all = executeQuery(cache, sql.toString());
        all.sort(new RawStexam.FinishDateTimeComparator());

        // Log.info("Found " + all.size() + " records for " + courseId + " since " + earliest);

        int start = 0;
        int position = 0;
        final int size = all.size();
        for (int i = 0; i < numDays; ++i) {
            while (position < size && all.get(position).examDt.equals(earliest)) {
                ++position;
            }

            final List<RawStexam> daily = new ArrayList<>(position - start);
            for (int j = start; j < position; ++j) {
                daily.add(all.get(j));
            }
            history.add(daily);
            start = position;

            earliest = earliest.plusDays(1L);
        }
    }

    /**
     * Retrieves all exams within a certain course of specified types.
     *
     * @param cache      the data cache
     * @param course     the course ID
     * @param passedOnly {@code true} to return only exams with passed = 'Y'
     * @param examTypes  the types of exams for which to query
     * @return the list of matching exams
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> getExams(final Cache cache, final String course, final boolean passedOnly,
                                           final String... examTypes) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(200);

        sql.add("SELECT * FROM stexam WHERE course=", LogicUtils.sqlStringValue(course));

        if (passedOnly) {
            sql.add(" AND passed='Y'");
        } else {
            sql.add(" AND (passed='Y' OR passed='N')");
        }

        if (examTypes != null) {
            final int numExamTypes = examTypes.length;
            if (numExamTypes == 1) {
                sql.add(" AND exam_type=", LogicUtils.sqlStringValue(examTypes[0]));
            } else if (numExamTypes > 0) {
                sql.add(" AND exam_type IN (", LogicUtils.sqlStringValue(examTypes[0]));
                for (int i = 1; i < numExamTypes; ++i) {
                    sql.add(CoreConstants.COMMA_CHAR).add(LogicUtils.sqlStringValue(examTypes[i]));
                }
                sql.add(')');
            }
        }

        sql.add(" ORDER BY exam_dt,finish_time");

        return executeQuery(cache, sql.toString());
    }

    /**
     * Retrieves all exams for a student in a specified course of specified types.
     *
     * @param cache      the data cache
     * @param stuId      the student ID
     * @param course     the course ID
     * @param passedOnly {@code true} to return only exams with passed = 'Y'
     * @param examTypes  the types of exams for which to query
     * @return the list of matching exams
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> getExams(final Cache cache, final String stuId, final String course,
                                           final boolean passedOnly, final String... examTypes) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(200);

        sql.add("SELECT * FROM stexam ",
                " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                "   AND course=", LogicUtils.sqlStringValue(course));

        if (passedOnly) {
            sql.add(" AND passed='Y'");
        } else {
            sql.add(" AND (passed='Y' OR passed='N')");
        }

        if (examTypes != null) {
            final int numExamTypes = examTypes.length;
            if (numExamTypes == 1) {
                sql.add(" AND exam_type=", LogicUtils.sqlStringValue(examTypes[0]));
            } else if (numExamTypes > 0) {
                sql.add(" AND exam_type IN (", LogicUtils.sqlStringValue(examTypes[0]));
                for (int i = 1; i < numExamTypes; ++i) {
                    sql.add(CoreConstants.COMMA_CHAR).add(LogicUtils.sqlStringValue(examTypes[i]));
                }
                sql.add(')');
            }
        }

        sql.add(" ORDER BY exam_dt,finish_time");

        return executeQuery(cache, sql.toString());
    }

    /**
     * Retrieves all exams for a student in a specified course and unit of specified types.
     *
     * @param cache      the data cache
     * @param stuId      the student ID
     * @param course     the course ID
     * @param unit       the unit
     * @param passedOnly {@code true} to return only exams with passed = 'Y'
     * @param examTypes  the types of exams for which to query
     * @return the list of matching exams
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> getExams(final Cache cache, final String stuId, final String course,
                                           final Integer unit, final boolean passedOnly,
                                           final String... examTypes) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(200);

        sql.add("SELECT * FROM stexam ",
                " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                "   AND course=", LogicUtils.sqlStringValue(course),
                "   AND unit=", LogicUtils.sqlIntegerValue(unit));

        if (passedOnly) {
            sql.add(" AND passed='Y'");
        } else {
            sql.add(" AND (passed='Y' OR passed='N')");
        }

        if (examTypes != null) {
            final int numExamTypes = examTypes.length;
            if (numExamTypes == 1) {
                sql.add(" AND exam_type=", LogicUtils.sqlStringValue(examTypes[0]));
            } else if (numExamTypes > 0) {
                sql.add(" AND exam_type IN (", LogicUtils.sqlStringValue(examTypes[0]));
                for (int i = 1; i < numExamTypes; ++i) {
                    sql.add(CoreConstants.COMMA_CHAR).add(LogicUtils.sqlStringValue(examTypes[i]));
                }
                sql.add(')');
            }
        }

        sql.add(" ORDER BY exam_dt,finish_time");

        return executeQuery(cache, sql.toString());
    }

    /**
     * Retrieves all exams for a student for a specified exam version.
     *
     * @param cache      the data cache
     * @param stuId      the student ID
     * @param version    the exam version
     * @param passedOnly {@code true} to return only exams with passed = 'Y'
     * @return the list of matching exams
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStexam> getExamsByVersion(final Cache cache, final String stuId, final String version,
                                                    final boolean passedOnly) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(200);

        sql.add("SELECT * FROM stexam ",
                " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                "   AND version=", LogicUtils.sqlStringValue(version));

        if (passedOnly) {
            sql.add(" AND passed='Y'");
        } else {
            sql.add(" AND (passed='Y' OR passed='N')");
        }

        sql.add(" ORDER BY exam_dt,finish_time");

        return executeQuery(cache, sql.toString());
    }

    /**
     * Retrieves the first passing exam score achieved so far by a student in a course and unit on exams of a certain
     * type.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param course   the course
     * @param unit     the unit
     * @param examType the exam type
     * @return the maximum score found; null if no score has been recorded yet
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStexam getFirstPassing(final Cache cache, final String stuId, final String course,
                                            final Integer unit, final String examType) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stexam ",
                " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                " AND course=", LogicUtils.sqlStringValue(course),
                " AND unit=", LogicUtils.sqlIntegerValue(unit),
                " AND exam_type=", LogicUtils.sqlStringValue(examType),
                " AND is_first_passed='Y'");

        RawStexam result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawStexam.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Returns the number of unit exam attempts taken by a student in a course and unit since the last passing review
     * exam.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course
     * @param unit   the unit
     * @return the number of unit exam attempts that were found since the most recent passing review exam
     * @throws SQLException if there is an error accessing the database
     */
    public static int countUnitSinceLastPassedReview(final Cache cache, final String stuId, final String course,
                                                     final Integer unit) throws SQLException {

        // We rely on the following to generate a list sorted by finish date/time...
        final List<RawStexam> allExams = getExams(cache, stuId, course, unit, false, ALL_EXAM_TYPES);

        int tries = 0;

        for (int i = allExams.size() - 1; i >= 0; --i) {
            final RawStexam test = allExams.get(i);

            if ("U".equals(test.examType)) {
                ++tries;
            }
            if ("R".equals(test.examType) && "Y".equals(test.passed)) {
                break;
            }
        }

        return tries;
    }

    /**
     * Updates the "passed" field of a record. The fields in the record that constitute its primary key are used to
     * select the record to update. This method does not commit the update.
     *
     * @param cache     the data cache
     * @param rec       the object to update
     * @param newPassed the new "passed" value
     * @return true if successful; false if not
     * @throws SQLException if there is an error updating the record
     */
    public static boolean updatePassed(final Cache cache, final RawStexam rec,
                                       final String newPassed) throws SQLException {

        final boolean result;

        if (rec.stuId.startsWith("99")) {
            Log.info("Skipping update of StudentExam for test student:");
            Log.info("  Student ID: ", rec.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE stexam ",
                    " SET passed=", LogicUtils.sqlStringValue(newPassed),
                    " WHERE serial_nbr=", LogicUtils.sqlLongValue(rec.serialNbr),
                    " AND version=", LogicUtils.sqlStringValue(rec.version),
                    " AND stu_id=", LogicUtils.sqlStringValue(rec.stuId));

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
     * Updates the "examScore" and "passed" fields of a record. The fields in the record that constitute its primary key
     * are used to select the record to update.
     *
     * @param cache     the data cache
     * @param rec       the object to update
     * @param newScore  the new exam score
     * @param newPassed the new "passed" value
     * @return true if successful; false if not
     * @throws SQLException if there is an error updating the record
     */
    public static boolean updateScoreAndPassed(final Cache cache, final RawStexam rec, final Integer newScore,
                                               final String newPassed) throws SQLException {

        final boolean result;

        if (rec.stuId.startsWith("99")) {
            Log.info("Skipping update of StudentExam for test student:");
            Log.info("  Student ID: ", rec.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE stexam ",
                    " SET exam_score=", LogicUtils.sqlIntegerValue(newScore),
                    ", passed=", LogicUtils.sqlStringValue(newPassed),
                    " WHERE serial_nbr=", LogicUtils.sqlLongValue(rec.serialNbr),
                    " AND version=", LogicUtils.sqlStringValue(rec.version),
                    " AND stu_id=", LogicUtils.sqlStringValue(rec.stuId));

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
     * Updates the "masteryScore" field of a record. The fields in the record that constitute its primary key are used
     * to select the record to update.
     *
     * @param cache      the data cache
     * @param rec        the object to update
     * @param newMastery the new mastery score
     * @throws SQLException if there is an error updating the record
     */
    public static void updateMasteryScore(final Cache cache, final RawStexam rec, final Integer newMastery)
            throws SQLException {

        if (rec.stuId.startsWith("99")) {
            Log.info("Skipping update of StudentExam for test student:");
            Log.info("  Student ID: ", rec.stuId);
        } else {
            final String sql = SimpleBuilder.concat("UPDATE stexam ",
                    " SET mastery_score=", LogicUtils.sqlIntegerValue(newMastery),
                    " WHERE serial_nbr=", LogicUtils.sqlLongValue(rec.serialNbr),
                    " AND version=", LogicUtils.sqlStringValue(rec.version),
                    " AND stu_id=", LogicUtils.sqlStringValue(rec.stuId));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                if (stmt.executeUpdate(sql) > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }

    }

    /**
     * Updates the "exam_dt" and "finish_time" fields of an exam. This method does not commit the update.
     *
     * @param cache         the data cache
     * @param rec           the object to update
     * @param newExamDate   the new "exam_dt" value
     * @param newFinishTime the new "finish_time" value
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error updating the record
     */
    public static boolean updateWhenFinished(final Cache cache, final RawStexam rec, final LocalDate newExamDate,
                                             final Integer newFinishTime) throws SQLException {

        final boolean result;

        // TODO: If the date is changing, should the start time be adjusted to be a number relative to the new day?

        if (rec.stuId.startsWith("99")) {
            Log.info("Skipping update of StudentExam for test student:");
            Log.info("  Student ID: ", rec.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE stexam ",
                    " SET exam_dt=", LogicUtils.sqlDateValue(newExamDate),
                    ", finish_time=", LogicUtils.sqlIntegerValue(newFinishTime),
                    " WHERE serial_nbr=", LogicUtils.sqlLongValue(rec.serialNbr),
                    " AND version=", LogicUtils.sqlStringValue(rec.version),
                    " AND stu_id=", LogicUtils.sqlStringValue(rec.stuId));

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
     * Updates the "is_first_passed" field of an exam.
     *
     * @param cache          the data cache
     * @param rec            the object to update
     * @param newFirstPassed the new "is_first_passed" value
     * @throws SQLException if there is an error updating the record
     */
    private static void updateFirstPassed(final Cache cache, final RawStexam rec, final String newFirstPassed)
            throws SQLException {

        if (rec.stuId.startsWith("99")) {
            Log.info("Skipping update of StudentExam for test student:");
            Log.info("  Student ID: ", rec.stuId);
        } else {
            final String sql = SimpleBuilder.concat("UPDATE stexam ",
                    " SET is_first_passed=", LogicUtils.sqlStringValue(newFirstPassed),
                    " WHERE serial_nbr=", LogicUtils.sqlLongValue(rec.serialNbr),
                    " AND version=", LogicUtils.sqlStringValue(rec.version),
                    " AND stu_id=", LogicUtils.sqlStringValue(rec.stuId));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                if (stmt.executeUpdate(sql) > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }
    }

    /**
     * Updates the "calc_nbr" field of an exam.
     *
     * @param cache      the data cache
     * @param rec        the object to update
     * @param newCalcNbr the new "calc_nbr" value
     * @throws SQLException if there is an error updating the record
     */
    private static void updateCalcNbr(final Cache cache, final RawStexam rec, final String newCalcNbr)
            throws SQLException {

        if (rec.stuId.startsWith("99")) {
            Log.info("Skipping update of StudentExam for test student:");
            Log.info("  Student ID: ", rec.stuId);
        } else {
            final String sql = SimpleBuilder.concat("UPDATE stexam ",
                    " SET calc_nbr=", LogicUtils.sqlStringValue(newCalcNbr),
                    " WHERE serial_nbr=", LogicUtils.sqlLongValue(rec.serialNbr),
                    " AND version=", LogicUtils.sqlStringValue(rec.version),
                    " AND stu_id=", LogicUtils.sqlStringValue(rec.stuId));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                if (stmt.executeUpdate(sql) > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }
    }

    /**
     * Scans all exams of a specified type for a student in a course and unit, identifies the earliest that was passed,
     * and updates the "is_first_passed" fields in all records as needed. This method does not commit its updates.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param course   the course
     * @param unit     the unit
     * @param examType the exam type
     * @throws SQLException if there is an error querying or updating
     */
    public static void recalculateFirstPassed(final Cache cache, final String stuId, final String course,
                                              final Integer unit, final String examType) throws SQLException {

        final List<RawStexam> all = queryByStudentCourse(cache, stuId, course, false);
        final List<RawStexam> match = new ArrayList<>(all.size());

        for (final RawStexam test : all) {
            if (test.unit.equals(unit) && test.examType.equals(examType)) {
                match.add(test);
            }
        }

        match.sort(new RawStexam.FinishDateTimeComparator());

        boolean foundFirst = false;
        for (final RawStexam test : match) {
            if (!"Y".equals(test.passed) || foundFirst) {
                // This is NOT the first passing
                if ("Y".equals(test.isFirstPassed)) {
                    updateFirstPassed(cache, test, "N");
                }
            } else {
                foundFirst = true;
                // This IS the first passing
                if ("N".equals(test.isFirstPassed)) {
                    updateFirstPassed(cache, test, "Y");
                }
            }
        }
    }

    /**
     * Tests for the presence of an exam (course, placement, or licensing) for the student taken within the last 10
     * minutes.
     *
     * @param cache     the data cache
     * @param now       the current date/time
     * @param studentId the student ID
     * @return {@code true} if a recent exam was found; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean verifyRecentExam(final Cache cache, final LocalDateTime now,
                                           final String studentId) throws SQLException {

        final List<RawStexam> exams = queryByStudent(cache, studentId, false);
        final int minutes = TemporalUtils.minuteOfDay(now) - 10;

        boolean verified = lookForRecentStexam(exams, minutes);

        if (!verified) {
            final List<RawStmpe> placements = RawStmpeLogic.queryLegalByStudent(cache, studentId);
            verified = lookForRecentPlacementExam(placements, minutes);
        }

        if (!verified) {
            final List<RawStchallenge> challenges = RawStchallengeLogic.queryByStudent(cache, studentId);
            verified = lookForRecentChallengeExam(challenges, minutes);
        }

        if (!verified) {
            final List<RawUsers> licensings = RawUsersLogic.queryByStudent(cache, studentId);
            verified = lookForRecentLicensingExam(licensings);
        }

        if (!verified) {
            final List<MasteryAttemptRec> masteries = MasteryAttemptLogic.get(cache).queryByStudent(cache, studentId);
            verified = lookForRecentMasteryAttempt(masteries, minutes);
        }

        return verified;
    }

    /**
     * Looks for an exam in a list of student exams whose finish time is greater than some target value.
     *
     * @param list    the list to search
     * @param minutes the finish time
     * @return {@code true} if the list contained any exams with finish time greater than or equal to the target minute
     */
    private static boolean lookForRecentStexam(final Iterable<RawStexam> list, final int minutes) {

        boolean found = false;
        final LocalDate today = LocalDate.now();

        for (final RawStexam exam : list) {
            if (exam.examDt.isEqual(today) && exam.finishTime.intValue() >= minutes) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Looks for an exam in a list of student placement exams whose finish time is greater than some target value.
     *
     * @param list    the list to search
     * @param minutes the finish time
     * @return {@code true} if the list contained any exams with finish time greater than or equal to the target minute
     */
    private static boolean lookForRecentPlacementExam(final Iterable<RawStmpe> list, final int minutes) {

        boolean found = false;
        final LocalDate today = LocalDate.now();

        for (final RawStmpe exam : list) {
            if (exam.examDt.isEqual(today) && exam.finishTime.intValue() >= minutes) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Looks for an exam in a list of student challenge exams whose finish time is greater than some target value.
     *
     * @param list    the list to search
     * @param minutes the finish time
     * @return {@code true} if the list contained any exams with finish time greater than or equal to the target minute
     */
    private static boolean lookForRecentChallengeExam(final Iterable<RawStchallenge> list, final int minutes) {

        boolean found = false;
        final LocalDate today = LocalDate.now();

        for (final RawStchallenge exam : list) {
            if (exam.examDt.isEqual(today) && exam.finishTime.intValue() >= minutes) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Looks for an exam in a list of student exams whose finish time is greater than some target value.
     *
     * @param list the list to search
     * @return {@code true} if the list contained any exams with finish time greater than or equal to the target minute
     */
    private static boolean lookForRecentLicensingExam(final Iterable<RawUsers> list) {

        boolean found = false;
        final LocalDate today = LocalDate.now();

        for (final RawUsers exam : list) {
            if (exam.examDt.isEqual(today)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Looks for a mastery exam attempt in a list of student mastery attempts whose finish time is greater than some
     * target value.
     *
     * @param list    the list to search
     * @param minutes the finish time
     * @return {@code true} if the list contained any exams with finish time greater than or equal to the target minute
     */
    private static boolean lookForRecentMasteryAttempt(final Iterable<MasteryAttemptRec> list, final int minutes) {

        boolean found = false;
        final LocalDate today = LocalDate.now();

        for (final MasteryAttemptRec exam : list) {
            final LocalDateTime whenFinished = exam.whenFinished;

            if (whenFinished.toLocalDate().isEqual(today)) {
                final int finishMinute = TemporalUtils.minuteOfDay(whenFinished);

                if (finishMinute >= minutes) {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStexam> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStexam> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStexam.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries for all exam records for a test student. Results are sorted by exam date, then exam time.
     *
     * @param stuId the student for which to query exams
     * @return the list of records that matched the criteria, a zero-length array if none matched
     */
    private static List<RawStexam> queryTestDatByStudent(final String stuId) {

        final List<RawStexam> result = new ArrayList<>(4);

        if (stuId.length() == 9 && stuId.startsWith("99PL")) {
            final char ch9 = stuId.charAt(8);

            String course = null;
            String examPrefix = CoreConstants.EMPTY;
            final int[] scores = {10, 15, 20, 20};
            if (ch9 == '1') {
                course = RawRecordConstants.M100T;
                examPrefix = "MT";
            } else if (ch9 == '2') {
                course = RawRecordConstants.M1170;
                examPrefix = "7T";
            } else if (ch9 == '3') {
                course = RawRecordConstants.M1180;
                examPrefix = "8T";
            } else if (ch9 == '4') {
                course = RawRecordConstants.M1240;
                examPrefix = "4T";
            } else if (ch9 == '5') {
                course = RawRecordConstants.M1250;
                examPrefix = "5T";
            } else if (ch9 == '6') {
                course = RawRecordConstants.M1260;
                examPrefix = "6T";
            }

            if (course != null) {
                final Integer start = Integer.valueOf(10 * 60 + 11);
                final Integer end = Integer.valueOf(12 * 60 + 13);
                final LocalDate date = LocalDate.now().minusDays(5L);

                for (int unit = 1; unit <= 4; ++unit) {
                    result.add(new RawStexam(Long.valueOf(100000000L + (long) unit), examPrefix + unit + "RE", stuId,
                            date, Integer.valueOf(scores[unit - 1]), Integer.valueOf(8), start, end, "Y", "Y", null,
                            course, Integer.valueOf(unit), "R", "Y", null, null));
                }
            }
        }

        return result;
    }

    /**
     * Queries for all exam records for a student in a particular course. Results are sorted by exam date, then exam
     * time.
     *
     * @param stuId  the student for which to query exams
     * @param course the course for which to query exams
     * @return the list of records that matched the criteria, a zero-length array if none matched
     */
    private static List<RawStexam> queryByTestStudentCourse(final String stuId, final String course) {

        final List<RawStexam> result = new ArrayList<>(4);

        if (stuId.length() == 9 && stuId.startsWith("99PL")) {
            final char ch9 = stuId.charAt(8);

            String crs = null;
            String examPrefix = CoreConstants.EMPTY;
            final int[] scores = {10, 15, 20, 20};
            if (ch9 == '1') {
                crs = RawRecordConstants.M100T;
                examPrefix = "MT";
            } else if (ch9 == '2') {
                crs = RawRecordConstants.M1170;
                examPrefix = "7T";
            } else if (ch9 == '3') {
                crs = RawRecordConstants.M1180;
                examPrefix = "8T";
            } else if (ch9 == '4') {
                crs = RawRecordConstants.M1240;
                examPrefix = "4T";
            } else if (ch9 == '5') {
                crs = RawRecordConstants.M1250;
                examPrefix = "5T";
            } else if (ch9 == '6') {
                crs = RawRecordConstants.M1260;
                examPrefix = "6T";
            }

            if (crs != null && crs.equals(course)) {
                final Integer start = Integer.valueOf(10 * 60 + 11);
                final Integer end = Integer.valueOf(12 * 60 + 13);
                final LocalDate date = LocalDate.now().minusDays(5L);

                for (int unit = 1; unit <= 4; ++unit) {
                    result.add(new RawStexam(Long.valueOf(100000000L + (long) unit), examPrefix + unit + "RE", stuId,
                            date, Integer.valueOf(scores[unit - 1]), Integer.valueOf(8), start, end, "Y", "Y", null,
                            course, Integer.valueOf(unit), "R", "Y", null, null));
                }
            }
        }

        return result;
    }
}
