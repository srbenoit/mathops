package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
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
public final class RawCsectionLogic extends AbstractRawLogic<RawCsection> {

    /** A single instance. */
    public static final RawCsectionLogic INSTANCE = new RawCsectionLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawCsectionLogic() {

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
    public boolean insert(final Cache cache, final RawCsection record) throws SQLException {

        if (record.course == null || record.sect == null || record.termKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO csection (course,sect,term,term_yr,section_id,aries_start_dt,aries_end_dt,start_dt,",
                "exam_delete_dt,instrn_type,instructor,campus,pacing_structure,mtg_days,classroom_id,",
                "lst_stcrs_creat_dt,grading_std,a_min_score,b_min_score,c_min_score,d_min_score,survey_id,",
                "course_label_shown,display_score,display_grade_scale,count_in_max_courses,online,bogus,canvas_id,",
                "subterm) VALUES (",
                sqlStringValue(record.course), ",",
                sqlStringValue(record.sect), ",",
                sqlStringValue(record.termKey.termCode), ",",
                sqlIntegerValue(record.termKey.shortYear), ",",
                sqlStringValue(record.sectionId), ",",
                sqlDateValue(record.ariesStartDt), ",",
                sqlDateValue(record.ariesEndDt), ",",
                sqlDateValue(record.startDt), ",",
                sqlDateValue(record.examDeleteDt), ",",
                sqlStringValue(record.instrnType), ",",
                sqlStringValue(record.instructor), ",",
                sqlStringValue(record.campus), ",",
                sqlStringValue(record.pacingStructure), ",",
                sqlStringValue(record.mtgDays), ",",
                sqlStringValue(record.classroomId), ",",
                sqlDateValue(record.lstStcrsCreatDt), ",",
                sqlStringValue(record.gradingStd), ",",
                sqlIntegerValue(record.aMinScore), ",",
                sqlIntegerValue(record.bMinScore), ",",
                sqlIntegerValue(record.cMinScore), ",",
                sqlIntegerValue(record.dMinScore), ",",
                sqlStringValue(record.surveyId), ",",
                sqlStringValue(record.courseLabelShown), ",",
                sqlStringValue(record.displayScore), ",",
                sqlStringValue(record.displayGradeScale), ",",
                sqlStringValue(record.countInMaxCourses), ",",
                sqlStringValue(record.online), ",",
                sqlStringValue(record.bogus), ",",
                sqlStringValue(record.canvasId), ",",
                sqlStringValue(record.subterm), ")");

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
    public boolean delete(final Cache cache, final RawCsection record)
            throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM csection ",
                "WHERE course=", sqlStringValue(record.course),
                "  AND sect=", sqlStringValue(record.sect),
                "  AND term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear));

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
    public List<RawCsection> queryAll(final Cache cache) throws SQLException {

        final List<RawCsection> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM csection")) {

            while (rs.next()) {
                result.add(RawCsection.fromResultSet(rs));
            }
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

        List<RawCsection> result;

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM csection WHERE term='", termKey.termCode,
                "' AND term_yr=", termKey.shortYear);

        result = new ArrayList<>(100);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCsection.fromResultSet(rs));
            }
        }

        return result;
    }
}
