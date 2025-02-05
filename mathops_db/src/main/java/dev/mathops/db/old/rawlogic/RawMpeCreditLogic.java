package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A utility class to work with mpe_credit records.
 *
 * <pre>
 * Table:  'mpe_credit'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * stu_id               char(9)           no      PK
 * course               char(6)           no      PK
 * exam_placed          char(1)           no
 * exam_dt              date              no
 * dt_cr_refused        date              yes
 * serial_nbr           integer           yes     PK
 * version              char(5)           yes
 * exam_source          char(2)           yes
 * </pre>
 */
public enum RawMpeCreditLogic {
    ;

    /** Field name. */
    private static final String M_100M = "M 100M";

    /** A commonly used string. */
    private static final String POOOO = "POOOO";

    /** A commonly used string. */
    private static final String PPPPP = "PPPPP";

    /** A commonly used string. */
    private static final String MPTUN = "MPTUN";

    /** A commonly used string. */
    private static final String P = "P";

    /** A commonly used string. */
    private static final String C = "C";

    /** Test student IDs placed into MATH 1105. */
    private static final List<String> placedInto105OnMCE;

    /** Test student IDs placed into MATH 117. */
    private static final List<String> placedInto117OnMCE;

    /** Test student IDs placed out of MATH 117. */
    private static final List<String> placedOutOf117OnMCE;

    /** Test student IDs placed out of MATH 117-124 on an MPE. */
    private static final List<String> placedOutOf117to124OnMPE;

    /** Test student IDs placed out of MATH 117-118 on an MCE. */
    private static final List<String> placedOutOf117to118OnMCE;

    /** Test student IDs that placed out of MATH 117-125 on MPE. */
    private static final List<String> placedOutOf117to125OnMPE;

    /** Test student IDs placed out of MATH 117 on an MPE, no attempts remaining. */
    private static final List<String> placedOutOf117OnMPE;

    static {
        placedInto105OnMCE = Arrays.asList("999011121", "999011125");

        placedInto117OnMCE = Arrays.asList("999011122", "999011124", "999012122", "999012124",
                "999012422", "999012424", "999031011", "999033111", "999033121");

        placedOutOf117OnMCE = Arrays.asList("999011123", "999012123", "999012423");

        placedOutOf117to124OnMPE = Arrays.asList("999011126", "999012126", "999012426", "999011142",
                "999012142", "999012442");

        placedOutOf117to118OnMCE = Arrays.asList("999011132", "999012132", "999012432", "999033221");

        placedOutOf117to125OnMPE = Arrays.asList("999011133", "999011221", "999011321", "999012133",
                "999012221", "999012321", "999012433", "999012461", "999012521", "999034011");

        placedOutOf117OnMPE = Arrays.asList("999011241", "999011341", "999012241", "999012341",
                "999012481", "999012541", "999033231");
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawMpeCredit record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO mpe_credit (stu_id,course,exam_placed,exam_dt,",
                    "dt_cr_refused,serial_nbr,version,exam_source) VALUES (",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlStringValue(record.course), ",",
                    LogicUtils.sqlStringValue(record.examPlaced), ",",
                    LogicUtils.sqlDateValue(record.examDt), ",",
                    LogicUtils.sqlDateValue(record.dtCrRefused), ",",
                    LogicUtils.sqlLongValue(record.serialNbr), ",",
                    LogicUtils.sqlStringValue(record.version), ",",
                    LogicUtils.sqlStringValue(record.examSource), ")");

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
    public static boolean delete(final Cache cache, final RawMpeCredit record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat(
                "DELETE FROM mpe_credit WHERE stu_id=",
                LogicUtils.sqlStringValue(record.stuId), " AND course=",
                LogicUtils.sqlStringValue(record.course), "AND exam_dt=",
                LogicUtils.sqlDateValue(record.examDt), " AND serial_nbr=",
                LogicUtils.sqlLongValue(record.serialNbr), " AND version=",
                LogicUtils.sqlStringValue(record.version));

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
    public static List<RawMpeCredit> queryAll(final Cache cache) throws SQLException {

        return executeSimpleQuery(cache, "SELECT * FROM mpe_credit");
    }

    /**
     * Queries for all records for a student.
     *
     * @param cache the data cache
     * @param stuId the ID of the student to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpeCredit> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final List<RawMpeCredit> result;

        if (stuId.startsWith("99")) {
            result = queryByTestStudent(stuId);
        } else {
            final String sql = SimpleBuilder.concat(
                    "SELECT * FROM mpe_credit",
                    " WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

            result = executeSimpleQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for all records for a single exam.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number of the exam to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpeCredit> queryByExam(final Cache cache, final Long serialNbr)
            throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM mpe_credit",
                " WHERE serial_nbr=", LogicUtils.sqlLongValue(serialNbr));

        return executeSimpleQuery(cache, sql);
    }

    /**
     * Queries for the set of placement credits on record for a course.
     *
     * @param cache    the data cache
     * @param courseId the ID of the course to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpeCredit> queryByCourse(final Cache cache, final String courseId)
            throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM mpe_credit",
                " WHERE course=", LogicUtils.sqlStringValue(courseId));

        return executeSimpleQuery(cache, sql);
    }

    /**
     * Applies a placement result to a student's record. Since there is only one row for a course for a student in the
     * placement results table, this may either update the exam date or placement result on an existing row, or may
     * insert a new row. This call performs a commit after successful completion.
     *
     * @param cache  the data cache
     * @param credit the placement credit to apply
     * @throws SQLException if there is an error accessing the database
     */
    public static void apply(final Cache cache, final RawMpeCredit credit) throws SQLException {

        if (!credit.stuId.startsWith("99")) {

            final String course = credit.course;

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try {
                // Hardcodes to handle M 100 weirdness
                switch (course) {
                    case RawRecordConstants.M100T -> applyM100T(cache, credit);
                    case M_100M -> applyM100M(cache, credit);
                    case RawRecordConstants.M100C -> {
                        final String student = credit.stuId;

                        final String sql1 = SimpleBuilder.concat("DELETE FROM mpe_credit",
                                " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                                " AND course MATCHES 'M 100*'");

                        try (final Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate(sql1);
                            conn.commit();
                        }

                        insert(cache, credit);
                    }
                    case null, default -> {
                        final String student = credit.stuId;

                        final String sql2 = SimpleBuilder.concat(
                                "SELECT exam_placed FROM mpe_credit",
                                " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                                " AND course=", LogicUtils.sqlStringValue(course));

                        final boolean found;
                        String orig = null;
                        try (final Statement stmt = conn.createStatement();
                             final ResultSet rs = stmt.executeQuery(sql2)) {

                            found = rs.next();
                            if (found) {
                                orig = rs.getString(1);
                            }
                        }

                        if (found) {
                            updateExistingCredit(cache, course, credit, orig, student);
                        } else {
                            insert(cache, credit);
                        }
                    }
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }
    }

    /**
     * Applies an M 100T placement result to a student's record. This is a hardcode but is the simplest way to handle
     * the logic required for this case. This call does not commit any updates.
     *
     * @param cache  the data cache
     * @param credit the placement credit to apply
     * @throws SQLException if there is an error accessing the database
     */
    private static void applyM100T(final Cache cache, final RawMpeCredit credit) throws SQLException {

        final String student = credit.stuId;

        // If there exists an M 100C or M 100M row, do nothing
        final String sql1 = SimpleBuilder.concat("SELECT * FROM mpe_credit",
                " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                "   AND (course='M 100C' OR course='M 100M')");

        final List<RawMpeCredit> m100cm = executeSimpleQuery(cache, sql1);

        if (m100cm.isEmpty()) {
            // If there exists an M 100T row, update its date; otherwise, insert a new M 100T row

            final String sql2 = SimpleBuilder.concat("SELECT * FROM mpe_credit",
                    " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                    "   AND course='M 100T'");

            final List<RawMpeCredit> m100t = executeSimpleQuery(cache, sql2);

            if (m100t.isEmpty()) {
                insert(cache, credit);
            } else {
                final String sql3 = SimpleBuilder.concat("UPDATE mpe_credit SET ",
                        "exam_dt=", LogicUtils.sqlDateValue(credit.examDt),
                        ",serial_nbr=", LogicUtils.sqlLongValue(credit.serialNbr),
                        ",version=", LogicUtils.sqlStringValue(credit.version),
                        ",exam_source=", LogicUtils.sqlStringValue(credit.examSource),
                        " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                        " AND course='M 100T'");

                final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql3);
                    conn.commit();
                } finally {
                    Cache.checkInConnection(conn);
                }
            }
        }
    }

    /**
     * Applies an M 100M placement result to a student's record. This is a hardcode but is the simplest way to handle
     * the logic required for this case. This call does not commit any updates.
     *
     * @param cache  the data cache
     * @param credit the placement credit to apply
     * @throws SQLException if there is an error accessing the database
     */
    private static void applyM100M(final Cache cache, final RawMpeCredit credit) throws SQLException {

        final String student = credit.stuId;

        // If there exists an M 100C row, we do nothing
        final String sql2 = SimpleBuilder.concat("SELECT * FROM mpe_credit",
                " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                "   AND course='M 100C'");

        final List<RawMpeCredit> m100c = executeSimpleQuery(cache, sql2);

        if (m100c.isEmpty()) {
            // If there exists an M 100T row, we delete it.
            final String sql3 = SimpleBuilder.concat("DELETE FROM mpe_credit",
                    " WHERE stu_id='", student, "'",
                    " AND course='M 100T'");

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement statement = conn.createStatement()) {
                statement.executeUpdate(sql3);
                conn.commit();
            } finally {
                Cache.checkInConnection(conn);
            }

            // If there exists an M 100M row, we update placed and date

            final String sql4 = SimpleBuilder.concat("SELECT * FROM mpe_credit",
                    " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                    "   AND course='M 100M'");

            final List<RawMpeCredit> m100m = executeSimpleQuery(cache, sql4);

            if (m100m.isEmpty()) {
                insert(cache, credit);
            } else {
                // Result has improved, so update placed & exam date.
                final String sql5 = SimpleBuilder.concat("UPDATE mpe_credit",
                        " SET exam_placed=", LogicUtils.sqlStringValue(credit.examPlaced),
                        ",exam_dt=", LogicUtils.sqlDateValue(credit.examDt),
                        ",serial_nbr=", LogicUtils.sqlLongValue(credit.serialNbr),
                        ",version=", LogicUtils.sqlStringValue(credit.version),
                        ",exam_source=", LogicUtils.sqlStringValue(credit.examSource),
                        " WHERE stu_id=", LogicUtils.sqlStringValue(credit.stuId),
                        " AND course='M 100M'");

                final DbConnection conn2 = cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn2.createStatement()) {
                    stmt.executeUpdate(sql5);
                    conn2.commit();
                } finally {
                    Cache.checkInConnection(conn2);
                }
            }
        }
    }

    /**
     * Updates an existing credit record. This call does not commit any updates.
     *
     * @param cache   the data cache
     * @param course  the course ID
     * @param credit  the credit record being applied
     * @param orig    the original result
     * @param student the student ID
     * @throws SQLException if there is an error accessing the database
     */
    private static void updateExistingCredit(final Cache cache, final String course, final RawMpeCredit credit,
                                             final String orig, final String student) throws SQLException {

        // Record already exists - if this placement result is stronger, use this one; if the result is the same,
        // simply update the exam date; if the existing record is stronger, leave it.

        if (credit.examPlaced.equals(orig)) {
            // Result is unchanged, so update existing record

            final String sql = SimpleBuilder.concat("UPDATE mpe_credit",
                    " SET exam_dt=", LogicUtils.sqlDateValue(credit.examDt),
                    ",serial_nbr=", LogicUtils.sqlLongValue(credit.serialNbr),
                    ",version=", LogicUtils.sqlStringValue(credit.version),
                    ",exam_source=", LogicUtils.sqlStringValue(credit.examSource),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                    " AND course=", LogicUtils.sqlStringValue(course));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                conn.commit();
            } finally {
                Cache.checkInConnection(conn);
            }

        } else if ("P".equals(orig) && "C".equals(credit.examPlaced)) {

            // Result has improved, so update placed & exam date.

            final String sql = SimpleBuilder.concat("UPDATE mpe_credit",
                    " SET exam_placed='C',exam_dt=", LogicUtils.sqlDateValue(credit.examDt),
                    ",serial_nbr=", LogicUtils.sqlLongValue(credit.serialNbr),
                    ",version=", LogicUtils.sqlStringValue(credit.version),
                    ",exam_source=", LogicUtils.sqlStringValue(credit.examSource),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                    " AND course=", LogicUtils.sqlStringValue(course));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                conn.commit();
            } finally {
                Cache.checkInConnection(conn);
            }

        } else if ("C".equals(orig) && "P".equals(credit.examPlaced)) {
            // Result has worsened, so take no action
        } else {
            // New P or C status, so update row

            final String sql = SimpleBuilder.concat("UPDATE mpe_credit",
                    " SET exam_placed=", LogicUtils.sqlStringValue(credit.examPlaced),
                    ",exam_dt=", LogicUtils.sqlDateValue(credit.examDt),
                    ",serial_nbr=", LogicUtils.sqlLongValue(credit.serialNbr),
                    ",version=", LogicUtils.sqlStringValue(credit.version),
                    ",exam_source=", LogicUtils.sqlStringValue(credit.examSource),
                    " WHERE stu_id=", LogicUtils.sqlStringValue(student),
                    " AND course=", LogicUtils.sqlStringValue(course));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                conn.commit();
            } finally {
                Cache.checkInConnection(conn);
            }
        }
    }

    /**
     * Executes a simple query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL statement to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawMpeCredit> executeSimpleQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawMpeCredit> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpeCredit.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries for the set of placement credits on record for a student.
     *
     * @param studentId the ID of the student to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     */
    private static List<RawMpeCredit> queryByTestStudent(final String studentId) {

        final List<RawMpeCredit> result = new ArrayList<>(3);

        if (studentId != null && studentId.length() == 9 && studentId.startsWith("99")) {
            final char ch3 = studentId.charAt(2);
            final char ch4 = studentId.charAt(3);

            if (ch3 == 'P' && ch4 == 'L') {
                getPlacementTestStudent(studentId, result);
            } else if (ch3 == '9') {
                getMathPlanTestStudent(studentId, result);
            } else if (ch3 == 'C' && ch4 == 'I') {
                getCheckinTestStudent(studentId, result);
            } else {
                Log.warning("Invalid test student ID: " + studentId);
            }
        } else {
            Log.warning("Invalid test student ID: " + studentId);
        }

        return result;
    }

    /**
     * Queries for the students with test data relating to math placement.
     *
     * @param studentId the ID of the student to query
     * @param result    the list to which to add results
     */
    private static void getPlacementTestStudent(final String studentId, final Collection<? super RawMpeCredit> result) {

        if (LogicUtils.validate99PLStudentId(studentId)) {

            final char ch5 = studentId.charAt(4);
            final char ch6 = studentId.charAt(5);
            if (ch5 != '0' && ch6 != '0') {

                final String examId = ch5 == '3' ? POOOO : PPPPP;
                final int index = ch5 <= '3' ? 0 : 1;

                final char ch7 = studentId.charAt(6);

                if (ch7 == '8') {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M117, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M118, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M124, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M125, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M126, C, examId));
                } else if (ch7 == '7') {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M117, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M118, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M125, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M126, C, examId));
                    if (ch6 == '8') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                    }
                } else if (ch7 == '6') {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M117, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M118, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M124, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M125, C, examId));
                    if (ch6 == '8') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                    }
                } else if (ch7 == '5') {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M117, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M118, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M125, C, examId));
                    if (ch6 == '8') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                        result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                    } else if (ch6 == '7') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                    } else if (ch6 == '6') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                    }
                } else if (ch7 == '4') {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M117, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M118, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M124, C, examId));
                    if (ch6 == '8') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                        result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                    } else if (ch6 == '6') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                    }
                } else if (ch7 == '3') {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M117, C, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M118, C, examId));
                    if (ch6 == '8') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                        result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                        result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                    } else if (ch6 == '7') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                        result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                    } else if (ch6 == '6') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                        result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                    } else if (ch6 == '5') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                    } else if (ch6 == '4') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                    }
                } else if (ch7 == '2') {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    result.add(makeRecord(studentId, index, RawRecordConstants.M117, C, examId));

                    if (ch6 >= '3') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M118, P, examId));
                        if (ch6 == '8') {
                            result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                            result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                            result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                        } else if (ch6 == '7') {
                            result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                            result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                        } else if (ch6 == '6') {
                            result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                            result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                        } else if (ch6 == '5') {
                            result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                        } else if (ch6 == '4') {
                            result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                        }
                    }
                } else {
                    result.add(makeRecord(studentId, index, RawRecordConstants.M100C, P, examId));
                    if (ch6 >= '2') {
                        result.add(makeRecord(studentId, index, RawRecordConstants.M117, P, examId));

                        if (ch6 >= '3') {
                            result.add(makeRecord(studentId, index, RawRecordConstants.M118, P, examId));

                            if (ch6 == '8') {
                                result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                                result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                                result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                            } else if (ch6 == '7') {
                                result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                                result.add(makeRecord(studentId, index, RawRecordConstants.M126, P, examId));
                            } else if (ch6 == '6') {
                                result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                                result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                            } else if (ch6 == '5') {
                                result.add(makeRecord(studentId, index, RawRecordConstants.M118, P, examId));
                                result.add(makeRecord(studentId, index, RawRecordConstants.M125, P, examId));
                            } else if (ch6 == '4') {
                                result.add(makeRecord(studentId, index, RawRecordConstants.M124, P, examId));
                            }
                        }
                    }
                }
            }
        } else {
            Log.warning("Invalid test student ID: " + studentId);
        }
    }

    /**
     * Queries for the students with test data relating to math plan.
     *
     * @param studentId the ID of the student to query
     * @param result    the list to which to add results
     */
    private static void getMathPlanTestStudent(final String studentId, final Collection<? super RawMpeCredit> result) {

        if (placedInto105OnMCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, "M 100M", P,
                    PPPPP));
        } else if (placedInto117OnMCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, PPPPP));
        } else if (placedOutOf117OnMCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, PPPPP));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M117, P, PPPPP));
        } else if (placedOutOf117to124OnMPE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M117, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M118, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M124, P, POOOO));
        } else if (placedOutOf117to118OnMCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, PPPPP));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M117, P, PPPPP));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M118, P, PPPPP));
        } else if (placedOutOf117to125OnMPE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M117, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M118, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M124, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M125, P, POOOO));
        } else if (placedOutOf117OnMPE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, POOOO));
            result.add(makeRecord(studentId, 0, RawRecordConstants.M117, P, POOOO));
        }
    }

    /**
     * Queries for the students with test data related to the checkin.
     *
     * @param studentId the ID of the student to query
     * @param result    the list to which to add results
     */
    private static void getCheckinTestStudent(final String studentId, final Collection<? super RawMpeCredit> result) {

        final char ch5 = studentId.charAt(4);
        final char ch6 = studentId.charAt(5);

        if (ch5 == 'M' && ch6 == 'P') {
            // Math Placement test cases - no data
        } else if (ch5 == 'C' && ch6 == 'H') {
            // Challenge Exam test cases
            final String id = studentId.substring(6);

            switch (id) {
                case "001" -> {
                    result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, MPTUN));
                    result.add(makeRecord(studentId, 0, RawRecordConstants.M117, P, MPTUN));
                    result.add(makeRecord(studentId, 0, RawRecordConstants.M118, P, MPTUN));
                    result.add(makeRecord(studentId, 0, RawRecordConstants.M124, P, MPTUN));
                    result.add(makeRecord(studentId, 0, RawRecordConstants.M125, P, MPTUN));
                    result.add(makeRecord(studentId, 0, RawRecordConstants.M126, P, MPTUN));
                }
                case "011", "018", "019" -> result.add(makeRecord(studentId, 0, RawRecordConstants.M100C, P, MPTUN));
                case "020", "025", "026" -> result.add(makeRecord(studentId, 0, RawRecordConstants.M117, P, MPTUN));
                case "030", "035", "036", "040", "045", "046" ->
                        result.add(makeRecord(studentId, 0, RawRecordConstants.M118, P, MPTUN));
                case "050", "055", "056" -> result.add(makeRecord(studentId, 0, RawRecordConstants.M125, P, MPTUN));
            }
        }
    }

    /**
     * Makes a placement credit record.
     *
     * @param studentId       the student ID
     * @param index           the index of the attempt
     * @param courseId        the course ID
     * @param placementResult the placement result ("P" or "C")
     * @param examId          the exam ID
     * @return the record
     */
    private static RawMpeCredit makeRecord(final String studentId, final int index, final String courseId,
                                           final String placementResult, final String examId) {

        return new RawMpeCredit(studentId, courseId, placementResult, LocalDate.now(), null,
                Long.valueOf(Integer.MAX_VALUE + studentId.hashCode() + index), examId, "TC");
    }
}
