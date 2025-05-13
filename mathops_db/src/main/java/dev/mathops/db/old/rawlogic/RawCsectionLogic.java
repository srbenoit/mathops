package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with csection records.
 *
 * <pre>
 * Table:  'csection'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * course               char(6)                   no      PK
 * sect                 char(4)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * section_id           char(6)                   yes
 * aries_start_dt       date                      yes
 * aries_end_dt         date                      yes
 * start_dt             date                      yes
 * exam_delete_dt       date                      yes
 * instrn_type          char(2)                   yes
 * instructor           char(30)                  yes
 * campus               char(2)                   no
 * pacing_structure     char(1)                   yes
 * mtg_days             char(5)                   yes
 * classroom_id         char(14)                  yes
 * lst_stcrs_creat_dt   date                      yes
 * grading_std          char(3)                   yes
 * a_min_score          smallint                  yes
 * b_min_score          smallint                  yes
 * c_min_score          smallint                  yes
 * d_min_score          smallint                  yes
 * survey_id            char(5)                   yes
 * course_label_shown   char(1)                   yes
 * display_score        char(1)                   yes
 * display_grade_sca+   char(1)                   yes
 * count_in_max_cour+   char(1)                   yes
 * online               char(1)                   no
 * bogus                char(1)                   no
 * canvas_id            char(30)                  yes
 * subterm              char(4)                   yes
 * </pre>
 */
public enum RawCsectionLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawCsection record) throws SQLException {

        if (record.course == null || record.sect == null || record.termKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO csection (course,sect,term,term_yr,section_id,aries_start_dt,aries_end_dt,start_dt,",
                "exam_delete_dt,instrn_type,instructor,campus,pacing_structure,mtg_days,classroom_id,",
                "lst_stcrs_creat_dt,grading_std,a_min_score,b_min_score,c_min_score,d_min_score,survey_id,",
                "course_label_shown,display_score,display_grade_scale,count_in_max_courses,online,bogus,canvas_id,",
                "subterm) VALUES (",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlStringValue(record.sect), ",",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlStringValue(record.sectionId), ",",
                LogicUtils.sqlDateValue(record.ariesStartDt), ",",
                LogicUtils.sqlDateValue(record.ariesEndDt), ",",
                LogicUtils.sqlDateValue(record.startDt), ",",
                LogicUtils.sqlDateValue(record.examDeleteDt), ",",
                LogicUtils.sqlStringValue(record.instrnType), ",",
                LogicUtils.sqlStringValue(record.instructor), ",",
                LogicUtils.sqlStringValue(record.campus), ",",
                LogicUtils.sqlStringValue(record.pacingStructure), ",",
                LogicUtils.sqlStringValue(record.mtgDays), ",",
                LogicUtils.sqlStringValue(record.classroomId), ",",
                LogicUtils.sqlDateValue(record.lstStcrsCreatDt), ",",
                LogicUtils.sqlStringValue(record.gradingStd), ",",
                LogicUtils.sqlIntegerValue(record.aMinScore), ",",
                LogicUtils.sqlIntegerValue(record.bMinScore), ",",
                LogicUtils.sqlIntegerValue(record.cMinScore), ",",
                LogicUtils.sqlIntegerValue(record.dMinScore), ",",
                LogicUtils.sqlStringValue(record.surveyId), ",",
                LogicUtils.sqlStringValue(record.courseLabelShown), ",",
                LogicUtils.sqlStringValue(record.displayScore), ",",
                LogicUtils.sqlStringValue(record.displayGradeScale), ",",
                LogicUtils.sqlStringValue(record.countInMaxCourses), ",",
                LogicUtils.sqlStringValue(record.online), ",",
                LogicUtils.sqlStringValue(record.bogus), ",",
                LogicUtils.sqlStringValue(record.canvasId), ",",
                LogicUtils.sqlStringValue(record.subterm), ")");

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
    public static boolean delete(final Cache cache, final RawCsection record)
            throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM csection ",
                "WHERE course=", LogicUtils.sqlStringValue(record.course),
                "  AND sect=", LogicUtils.sqlStringValue(record.sect),
                "  AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear));

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
    public static List<RawCsection> queryAll(final Cache cache) throws SQLException {

        final List<RawCsection> result = new ArrayList<>(500);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM csection")) {

            while (rs.next()) {
                result.add(RawCsection.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Retrieves all course sections for a specified term.
     *
     * @param cache   the data cache
     * @param termKey the term key
     * @return the list of sections; {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCsection> queryByTerm(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM csection WHERE term='", termKey.termCode,
                "' AND term_yr=", termKey.shortYear);

        final List<RawCsection> result = new ArrayList<>(100);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCsection.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Retrieves a single course section record.
     *
     * @param cache   the data cache
     * @param termKey the term key
     * @param course  the course ID
     * @param sect    the section
     * @return the matching sections; {@code null} if not found
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCsection query(final Cache cache, final TermKey termKey, final String course, final String sect)
            throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM csection WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                " AND term_yr=", LogicUtils.sqlIntegerValue(termKey.shortYear),
                " AND course=", LogicUtils.sqlStringValue(course),
                " AND sect=", LogicUtils.sqlStringValue(sect));

        return executeSingleQuery(cache, sql);
    }
}
