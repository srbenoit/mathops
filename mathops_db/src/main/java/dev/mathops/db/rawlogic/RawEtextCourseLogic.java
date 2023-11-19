package dev.mathops.db.rawlogic;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawEtextCourse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with etext_course records.
 *
 * <pre>
 * Table:  'etext_course'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * etext_id             char(6)                   no      PK
 * course               char(6)                   no      PK
 * </pre>
 */
public final class RawEtextCourseLogic extends AbstractRawLogic<RawEtextCourse> {

    /** A single instance. */
    public static final RawEtextCourseLogic INSTANCE = new RawEtextCourseLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawEtextCourseLogic() {

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
    public boolean insert(final Cache cache, final RawEtextCourse record) throws SQLException {

        if (record.etextId == null || record.course == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO etext_course (etext_id,course) VALUES (",
                sqlStringValue(record.etextId), ",",
                sqlStringValue(record.course), ")");

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
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean delete(final Cache cache, final RawEtextCourse record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM etext_course ",
                "WHERE etext_id=", sqlStringValue(record.etextId),
                "  AND course=", sqlStringValue(record.course));

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
    public List<RawEtextCourse> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM etext_course");
    }

    /**
     * Queries for e-text courses provided by a particular e-text.
     *
     * @param cache   the data cache
     * @param etextId the ID of the e-text course to query
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawEtextCourse> queryByEtext(final Cache cache, final String etextId) throws SQLException {

        return executeQuery(cache, SimpleBuilder.concat(//
                "SELECT * FROM etext_course WHERE etext_id=", sqlStringValue(etextId)));
    }

    /**
     * Queries for all e-text courses that provide access to a particular course.
     *
     * @param cache    the data cache
     * @param courseId the ID of the e-text course to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawEtextCourse> queryByCourse(final Cache cache, final String courseId)
            throws SQLException {

        return executeQuery(cache, SimpleBuilder.concat(
                "SELECT * FROM etext_course WHERE course=", sqlStringValue(courseId)));
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawEtextCourse> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawEtextCourse> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawEtextCourse.fromResultSet(rs));
            }
        }

        return result;
    }
}
