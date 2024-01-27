package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawCourse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with course records.
 *
 * <pre>
 * Table:  'course'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * course               char(6)                   no      PK
 * nbr_units            smallint                  no
 * course_name          char(50)                  yes
 * nbr_credits          smallint                  no
 * calc_ok              char(1)                   yes
 * course_label         char(40)                  no
 * inline_prefix        char(20)                  yes
 * is_tutorial          char(1)                   no
 * require_etext        char(1)                   no
 * </pre>
 */
public final class RawCourseLogic extends AbstractRawLogic<RawCourse> {

    /** A single instance. */
    public static final RawCourseLogic INSTANCE = new RawCourseLogic();

    /** The base for keys for the results from "query". */
    private static final String COURSE_QUERY = "course:query:";

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawCourseLogic() {

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
    public boolean insert(final Cache cache, final RawCourse record) throws SQLException {

        if (record.course == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO course (course,nbr_units,course_name,nbr_credits,calc_ok,course_label,inline_prefix,",
                "is_tutorial,require_etext) VALUES (",
                "'", record.course, "',",
                sqlIntegerValue(record.nbrUnits), ",",
                sqlStringValue(record.courseName), ",",
                sqlIntegerValue(record.nbrCredits), ",",
                sqlStringValue(record.calcOk), ",",
                sqlStringValue(record.courseLabel), ",",
                sqlStringValue(record.inlinePrefix), ",",
                sqlStringValue(record.isTutorial), ",",
                sqlStringValue(record.requireEtext), ")");

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
    public boolean delete(final Cache cache, final RawCourse record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM course ",
                "WHERE course=", sqlStringValue(record.course));

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
    public List<RawCourse> queryAll(final Cache cache) throws SQLException {

        final List<RawCourse> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM course")) {

            while (rs.next()) {
                result.add(RawCourse.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Queries for a course by its ID.
     *
     * @param cache  the data cache
     * @param course the ID of the course to query
     * @return the testing center; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public static RawCourse query(final Cache cache, final String course) throws SQLException {

        final String key = COURSE_QUERY + course;

        RawCourse result = cache.getRecord(key, RawCourse.class);

        if (result == null) {
            final String sql = SimpleBuilder.concat(
                    "SELECT * FROM course WHERE course='", course, "'");

            try (final Statement stmt = cache.conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    result = RawCourse.fromResultSet(rs);
                    cache.storeRecord(key, result);
                }
            }
        }

        return result;
    }

    /**
     * Tests whether a course is a tutorial.
     *
     * @param cache  the data cache
     * @param course the ID of the course to retrieve
     * @return TRUE if the course was found an is marked as being a tutorial; FALSE if the course was found and is not
     *         marked as a tutorial; null if the course was not found
     * @throws SQLException if there is an error performing the query
     */
    public static Boolean isCourseTutorial(final Cache cache, final String course) throws SQLException {

        final RawCourse rec = query(cache, course);

        return rec == null ? null : Boolean.valueOf("Y".equals(rec.isTutorial));
    }

    /**
     * Tests whether a course requires an e-text.
     *
     * @param cache  the data cache
     * @param course the ID of the course to retrieve
     * @return TRUE if the course was found an is marked as being a tutorial; FALSE if the course was found as is not
     *         marked as a tutorial; null if the course was not found
     * @throws SQLException if there is an error performing the query
     */
    public static Boolean isEtextRequired(final Cache cache, final String course) throws SQLException {

        final RawCourse rec = query(cache, course);

        return rec == null ? null : Boolean.valueOf("Y".equals(rec.requireEtext));
    }

    /**
     * Gets the label for a course.
     *
     * @param cache  the data cache
     * @param course the ID of the course to query
     * @return the label; null if the course was not found or had no label
     * @throws SQLException if there is an error performing the query
     */
    public static String getCourseLabel(final Cache cache, final String course) throws SQLException {

        final RawCourse rec = query(cache, course);

        return rec == null ? null : rec.courseLabel;
    }
}
