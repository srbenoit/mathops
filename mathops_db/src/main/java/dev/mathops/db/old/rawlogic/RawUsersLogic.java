package dev.mathops.db.old.rawlogic;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.rawrecord.RawUsers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "users" records.
 *
 * <pre>
 * Table:  'users'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * term                 char(2)                   no
 * term_yr              smallint                  no
 * serial_nbr           integer                   yes      PK
 * version              char(5)                   no
 * exam_dt              date                      yes
 * exam_score           smallint                  yes
 * calc_course          char(2)                   no
 * passed               char(1)                   yes
 * </pre>
 */
public final class RawUsersLogic extends AbstractRawLogic<RawUsers> {

    /** A single instance. */
    public static final RawUsersLogic INSTANCE = new RawUsersLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawUsersLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawUsers record) throws SQLException {

        if (record.stuId == null || record.termKey == null || record.serialNbr == null
                || record.version == null || record.calcCourse == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO users ",
                "(stu_id,term,term_yr,serial_nbr,version,exam_dt,exam_score,",
                "calc_course,passed) VALUES (",
                sqlStringValue(record.stuId), ",",
                sqlStringValue(record.termKey.termCode), ",",
                sqlIntegerValue(record.termKey.shortYear), ",",
                sqlLongValue(record.serialNbr), ",",
                sqlStringValue(record.version), ",",
                sqlDateValue(record.examDt), ",",
                sqlIntegerValue(record.examScore), ",",
                sqlStringValue(record.calcCourse), ",",
                sqlStringValue(record.passed), ")");

        try (final Statement s = cache.conn.createStatement()) {
            final boolean result = s.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
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
    @Override
    public boolean delete(final Cache cache, final RawUsers record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM users ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND serial_nbr=", sqlLongValue(record.serialNbr),
                "  AND version=", sqlStringValue(record.version));

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
    @Override
    public List<RawUsers> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache.conn, "SELECT * FROM users");
    }

    /**
     * Retrieves all records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawUsers> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM users ",
                "WHERE stu_id=", sqlStringValue(stuId));

        return executeQuery(cache.conn, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawUsers> executeQuery(final DbConnection conn, final String sql) throws SQLException {

        final List<RawUsers> result = new ArrayList<>(50);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawUsers.fromResultSet(rs));
            }
        }

        return result;
    }
}
