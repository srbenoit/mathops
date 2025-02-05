package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "special_stus" records.
 *
 * <pre>
 * Table:  'special_stus'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * stu_type             char(7)                   no      PK
 * start_dt             date                      yes
 * end_dt               date                      yes
 * </pre>
 */
public enum RawSpecialStusLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawSpecialStus record) throws SQLException {

        if (record.stuId == null || record.stuType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO special_stus (stu_id,stu_type,start_dt,end_dt) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.stuType), ",",
                LogicUtils.sqlDateValue(record.startDt), ",",
                LogicUtils.sqlDateValue(record.endDt), ")");

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
    public static boolean delete(final Cache cache, final RawSpecialStus record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM special_stus ",
                " WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "   AND stu_type=", LogicUtils.sqlStringValue(record.stuType));

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
    public static List<RawSpecialStus> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM special_stus");
    }

    /**
     * Retrieves all records with a specified student ID.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of matching RawSpecialStus records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSpecialStus> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final List<RawSpecialStus> result;

        if (stuId.startsWith("99")) {
            result = queryByTestStudent(stuId);
        } else {
            final String sql = SimpleBuilder.concat(
                    "SELECT * FROM special_stus WHERE stu_id=",
                    LogicUtils.sqlStringValue(stuId));

            result = executeQuery(cache, sql);
        }

        return result;
    }

    /**
     * Retrieves the set of special student records for a student that are active as of a date.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param today the current date
     * @return the matching special student records; {@code null} if an error occurs
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSpecialStus> queryActiveByStudent(final Cache cache, final String stuId,
                                                            final ChronoLocalDate today) throws SQLException {

        final List<RawSpecialStus> list = queryByStudent(cache, stuId);

        final List<RawSpecialStus> result;

        if (list == null) {
            result = new ArrayList<>(0);
        } else {
            result = new ArrayList<>(10);
            for (final RawSpecialStus rawSpecialStus : list) {
                if (isSpecialTypeActive(rawSpecialStus, today)) {
                    result.add(rawSpecialStus);
                }
            }
        }

        return result;
    }

    /**
     * Retrieves all records with a specified student type.
     *
     * @param cache   the data cache
     * @param stuType the student type
     * @return the list of matching RawSpecialStus records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSpecialStus> queryByType(final Cache cache, final String stuType) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM special_stus WHERE stu_type=",
                LogicUtils.sqlStringValue(stuType));

        return executeQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache   the data cache
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawSpecialStus> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawSpecialStus> result = new ArrayList<>(10);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawSpecialStus.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Retrieves all special student records that are currently active and that have a specified type.
     *
     * @param cache the data cache
     * @param type  the type
     * @param today the current date
     * @return the list of records found
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSpecialStus> queryActiveByType(final Cache cache, final String type,
                                                         final ChronoLocalDate today) throws SQLException {

        final List<RawSpecialStus> list = queryByType(cache, type);

        final List<RawSpecialStus> result = new ArrayList<>(10);
        for (final RawSpecialStus rawSpecialStus : list) {
            if (isSpecialTypeActive(rawSpecialStus, today)) {
                result.add(rawSpecialStus);
            }
        }

        return result;
    }

    /**
     * Tests whether a student is a member of a special type.
     *
     * @param cache   the data cache
     * @param stuId   the student ID
     * @param today   the current date
     * @param stuType a list of one or more types for which to test
     * @return true if the student is a member of the specified type on the specified day
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean isSpecialType(final Cache cache, final String stuId, final ChronoLocalDate today,
                                        final String... stuType) throws SQLException {

        boolean result = false;

        final List<RawSpecialStus> list = queryByStudent(cache, stuId);

        if (list != null) {
            for (final RawSpecialStus test : list) {
                for (final String type : stuType) {
                    if (type.equals(test.stuType) && isSpecialTypeActive(test, today)) {
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Tests whether a special student record is active at a given date/time.
     *
     * @param special the special student record
     * @param today   the date to test
     * @return {@code true} if the type is active; {@code false} if not
     */
    private static boolean isSpecialTypeActive(final RawSpecialStus special, final ChronoLocalDate today) {

        final LocalDate start = special.startDt;
        final boolean started = start == null || !start.isAfter(today);
        final boolean isActive;

        if (started) {
            final LocalDate end = special.endDt;
            isActive = end == null || !end.isBefore(today);
        } else {
            isActive = false;
        }

        return isActive;
    }

    /**
     * Retrieves all special student records for a test student.
     *
     * @param studentId the student ID
     * @return the test student; an empty list if the student ID is not a valid test student ID
     */
    private static List<RawSpecialStus> queryByTestStudent(final String studentId) {

        final List<RawSpecialStus> result;

        if (studentId != null && studentId.length() == 9 //
            && studentId.startsWith("99")) {
            final char ch3 = studentId.charAt(2);
            final char ch4 = studentId.charAt(3);

            if (ch3 == 'P' && ch4 == 'L') {
                result = getPlacementTestStudent(studentId);
            } else if (ch3 == 'C' && ch4 == 'I') {
                result = getCheckinTestStudent(studentId);
            } else {
                Log.warning("Invalid test student ID: ", studentId);
                result = new ArrayList<>(0);
            }
        } else {
            Log.warning("Invalid test student ID: ", studentId);
            result = new ArrayList<>(0);
        }

        return result;
    }

    /**
     * Gets all special student records for a test student configured to test some aspect of the Math Plan.
     *
     * @param studentId the student ID
     * @return the special student records; an empty list if the student ID is not a valid test student ID
     */
    private static List<RawSpecialStus> getPlacementTestStudent(final String studentId) {

        final List<RawSpecialStus> result;

        if (RawStudentLogic.validate99PLStudentId(studentId)) {

            final LocalDate today = LocalDate.now();
            final LocalDate bef1 = today.minusDays(1L);
            final LocalDate bef2 = today.minusDays(2L);
            final LocalDate after1 = today.plusDays(1L);
            final LocalDate after2 = today.plusDays(2L);

            result = new ArrayList<>(1);

            final char c8 = studentId.charAt(7);

            if ("139BHJPR".indexOf(c8) >= 0) {
                result.add(new RawSpecialStus(studentId, "DCE", null, null));
            } else if ("57DFLN".indexOf(c8) >= 0) {
                result.add(new RawSpecialStus(studentId, "DCEN", null, null));
            }

            if ("048CGKO".indexOf(c8) >= 0) {
                // Preview row that ended yesterday
                result.add(new RawSpecialStus(studentId, "ORIENTN", bef2, bef1));
            } else if ("159DHLP".indexOf(c8) >= 0) {
                // Preview row that does not begin until tomorrow
                result.add(new RawSpecialStus(studentId, "ORIENTN", after1, after2));
            } else if ("23ABIJQR".indexOf(c8) >= 0) {
                // Preview row that begins today
                result.add(new RawSpecialStus(studentId, "ORIENTN", today, after1));
            } else if ("67EFMN".indexOf(c8) >= 0) {
                // Preview row that ends today
                result.add(new RawSpecialStus(studentId, "ORIENTN", bef1, today));
            }

        } else {
            Log.warning("Invalid test student ID: " + studentId);
            result = new ArrayList<>(0);
        }

        return result;
    }

    /**
     * Gets all special student records for a test student configured to test some aspect of Math Placement.
     *
     * @param studentId the student ID
     * @return the special student records; an empty list if the student ID is not a valid test student ID
     */
    private static List<RawSpecialStus> getCheckinTestStudent(final String studentId) {

        final List<RawSpecialStus> result;

        final char ch5 = studentId.charAt(4);
        final char ch6 = studentId.charAt(5);

        if (ch5 == 'M' && ch6 == 'P') {
            // Math Placement test cases

            // Last three digits are test case. 8-th digit governs attempts used.
            final char ch = studentId.charAt(8);

            if (ch == '2') {
                result = new ArrayList<>(1);
                result.add(new RawSpecialStus(studentId, "DCE", null, null));
            } else if (ch == '3') {
                result = new ArrayList<>(1);
                result.add(new RawSpecialStus(studentId, "DCEN", null, null));
            } else if (ch == '4') {
                result = new ArrayList<>(1);
                result.add(new RawSpecialStus(studentId, "ORIENTN", null, null));
            } else if (ch == '5') {
                result = new ArrayList<>(2);
                result.add(new RawSpecialStus(studentId, "DCE", null, null));
                result.add(new RawSpecialStus(studentId, "ORIENTN", null, null));
            } else if (ch == '6') {
                result = new ArrayList<>(2);
                result.add(new RawSpecialStus(studentId, "DCEN", null, null));
                result.add(new RawSpecialStus(studentId, "ORIENTN", null, null));
            } else {
                result = new ArrayList<>(0);
            }

        } else if (ch5 == 'C' && ch6 == 'H') {
            // Challenge Exam test cases

            final String id = studentId.substring(6);

            if ("011".equals(id)) {
                result = new ArrayList<>(1);
                result.add(new RawSpecialStus(studentId, "DCEN", null, null));
            } else {
                result = new ArrayList<>(0);
            }
        } else {
            Log.warning("Invalid test student ID: " + studentId);
            result = new ArrayList<>(0);
        }

        return result;
    }
}
