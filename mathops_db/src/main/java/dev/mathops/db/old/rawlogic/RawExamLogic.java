package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawExam;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "exam" records.
 *
 * <pre>
 * Table:  'exam'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * version              char(5)                   no      PK
 * course               char(6)                   no
 * unit                 smallint                  no
 * vsn_explt            char(7)                   yes
 * title                char(30)                  yes
 * tree_ref             char(40)                  yes
 * exam_type            char(2)                   no
 * active_dt            date                      no
 * pull_dt              date                      yes
 * button_label         char(50)                  yes
 * </pre>
 */
public final class RawExamLogic extends AbstractRawLogic<RawExam> {

    /** A single instance. */
    public static final RawExamLogic INSTANCE = new RawExamLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawExamLogic() {

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
    public boolean insert(final Cache cache, final RawExam record) throws SQLException {

        if (record.version == null || record.course == null || record.unit == null
                || record.examType == null || record.activeDt == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO exam (",
                "version,course,unit,vsn_explt,title,tree_ref,exam_type,active_dt,",
                "pull_dt,button_label) VALUES (",
                sqlStringValue(record.version), ",",
                sqlStringValue(record.course), ",",
                sqlIntegerValue(record.unit), ",",
                sqlStringValue(record.vsnExplt), ",",
                sqlStringValue(record.title), ",",
                sqlStringValue(record.treeRef), ",",
                sqlStringValue(record.examType), ",",
                sqlDateValue(record.activeDt), ",",
                sqlDateValue(record.pullDt), ",",
                sqlStringValue(record.buttonLabel), ")");

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
    public boolean delete(final Cache cache, final RawExam record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM exam ",
                "WHERE version=", sqlStringValue(record.version));

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
    public List<RawExam> queryAll(final Cache cache) throws SQLException {

        return doListQuery(cache, "SELECT * FROM exam");
    }

    /**
     * Queries for all active (having null pull date) exams in a course.
     *
     * @param cache  the data cache
     * @param course the course for which to query
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawExam> queryActiveByCourse(final Cache cache, final String course)
            throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM exam WHERE course=", sqlStringValue(course),
                " AND pull_dt IS NULL");

        return doListQuery(cache, sql);
    }

    /**
     * Queries for all active (having null pull date) exams in a course unit.
     *
     * @param cache  the data cache
     * @param course the course for which to query
     * @param unit   the unit for which to query
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawExam> queryActiveByCourseUnit(final Cache cache, final String course,
                                                        final Integer unit) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM exam WHERE course=", sqlStringValue(course),
                " AND unit=", sqlIntegerValue(unit),
                " AND pull_dt IS NULL");

        return doListQuery(cache, sql);
    }

    /**
     * Queries for an active (having null pull date) exam by its course, unit, and type (which should produce a unique
     * result or nothing).
     *
     * @param cache    the data cache
     * @param course   the course for which to query
     * @param unit     the unit for which to query
     * @param examType the exam type for which to query
     * @return the exam; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public static RawExam queryActiveByCourseUnitType(final Cache cache, final String course,
                                                      final Integer unit, final String examType) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM exam WHERE course=", sqlStringValue(course),
                " AND unit=", sqlIntegerValue(unit),
                " AND exam_type=", sqlStringValue(examType),
                " AND pull_dt IS NULL");

        return doSingleQuery(cache, sql);
    }

    /**
     * Queries for an exam by its version.
     *
     * @param cache   the data cache
     * @param version the version of the exam to query
     * @return the exam; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public static RawExam query(final Cache cache, final String version) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM exam WHERE version=", sqlStringValue(version));

        return doSingleQuery(cache, sql);
    }

    /**
     * Performs a query that returns single record.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return the record; null if none returned
     * @throws SQLException if there is an error performing the query
     */
    private static RawExam doSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawExam result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawExam.fromResultSet(rs);
            }
        }

        return result;
    }

    /**
     * Performs a query that returns list of records.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    private static List<RawExam> doListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawExam> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawExam.fromResultSet(rs));
            }
        }

        return result;
    }
}
