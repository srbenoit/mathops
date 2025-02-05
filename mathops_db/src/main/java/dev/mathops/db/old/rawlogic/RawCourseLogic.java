package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.text.builder.SimpleBuilder;

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
public enum RawCourseLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawCourse record) throws SQLException {

        if (record.course == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO course (course,nbr_units,course_name,nbr_credits,calc_ok,course_label,inline_prefix,",
                "is_tutorial,require_etext) VALUES (",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlIntegerValue(record.nbrUnits), ",",
                LogicUtils.sqlStringValue(record.courseName), ",",
                LogicUtils.sqlIntegerValue(record.nbrCredits), ",",
                LogicUtils.sqlStringValue(record.calcOk), ",",
                LogicUtils.sqlStringValue(record.courseLabel), ",",
                LogicUtils.sqlStringValue(record.inlinePrefix), ",",
                LogicUtils.sqlStringValue(record.isTutorial), ",",
                LogicUtils.sqlStringValue(record.requireEtext), ")");

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
    public static boolean delete(final Cache cache, final RawCourse record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM course ",
                "WHERE course=", LogicUtils.sqlStringValue(record.course));

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
    public static List<RawCourse> queryAll(final Cache cache) throws SQLException {

        final List<RawCourse> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM course")) {

            while (rs.next()) {
                result.add(RawCourse.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
