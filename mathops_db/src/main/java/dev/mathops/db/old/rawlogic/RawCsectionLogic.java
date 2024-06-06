package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.EProctoringOption;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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

    /** The base for keys for the results from "queryByTerm". */
    private static final String CSECTION_QUERY_BY_TERM = "csection:queryByTerm:";

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

        final String key = CSECTION_QUERY_BY_TERM + termKey;

        List<RawCsection> result = cache.getList(key, RawCsection.class);

        if (result == null) {
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

            cache.cloneAndStoreList(key, result);
        }

        return result;
    }

    /**
     * Retrieves all course sections for a specified course in a specified term.
     *
     * @param cache   the data cache
     * @param course the course ID
     * @param termKey the term key
     * @return the list of sections; {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCsection> queryByCourseTerm(final Cache cache, final String course, final TermKey termKey)
            throws SQLException {

        final List<RawCsection> forTerm = queryByTerm(cache, termKey);
        final List<RawCsection> result = new ArrayList<>(10);

        for (final RawCsection row : forTerm) {
            if (row.course.equals(course)) {
                result.add(row);
            }
        }

        return result;
    }

    /**
     * Retrieves a particular course section.
     *
     * @param cache   the data cache
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the corresponding course; {@code null} on any error or if no course exists with the specified ID
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCsection query(final Cache cache, final String course, final String sect,
                                    final TermKey termKey) throws SQLException {

        final List<RawCsection> list = queryByTerm(cache, termKey);

        RawCsection result = null;

        for (final RawCsection rec : list) {
            if (rec.course.equals(course) && rec.sect.equals(sect)) {
                result = rec;
                break;
            }
        }

        return result;
    }

    /**
     * Retrieves the instruction type for a particular course section.
     *
     * @param cache   the data cache
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the instruction type; {@code null} on any error or if no course exists with the specified ID
     * @throws SQLException if there is an error accessing the database
     */
    public static String getInstructionType(final Cache cache, final String course,
                                            final String sect, final TermKey termKey) throws SQLException {

        final RawCsection rec = query(cache, course, sect, termKey);

        return rec == null ? null : rec.instrnType;
    }

    /**
     * Retrieves the rule set ID for a particular course section.
     *
     * @param cache   the data cache
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the rule set ID; {@code null} on any error or if no course exists with the specified ID
     * @throws SQLException if there is an error accessing the database
     */
    public static String getRuleSetId(final Cache cache, final String course, final String sect,
                                      final TermKey termKey) throws SQLException {

        final RawCsection rec = query(cache, course, sect, termKey);

        return rec == null ? null : rec.pacingStructure;
    }

    /**
     * Retrieves the exam delete date for a particular course section.
     *
     * @param cache   the data cache
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the exam delete date; {@code null} if the exam delete date was null or if no course exists with the
     *         specified ID
     * @throws SQLException if there is an error accessing the database
     */
    public static LocalDate getExamDeleteDate(final Cache cache, final String course,
                                              final String sect, final TermKey termKey) throws SQLException {

        final RawCsection rec = query(cache, course, sect, termKey);

        return rec == null ? null : rec.examDeleteDt;
    }

    /**
     * Gets the topmatter associated with a course and section.
     *
     * @param courseId the course ID
     * @return the topmatter (null if none)
     */
    public static String getTopmatter(final String courseId) {

        String topmatter = null;

        if (RawRecordConstants.M125.equals(courseId) || RawRecordConstants.M126.equals(courseId)
                || RawRecordConstants.M1250.equals(courseId)
                || RawRecordConstants.M1260.equals(courseId)) {
            topmatter = "<strong class='red'>REMEMBER: when working with angles, always check the mode setting on "
                    + "your calculator.</strong>";
        }

        return topmatter;
    }

    /**
     * Gets the proctoring options available for a particular course section.
     *
     * @param csection the course section
     * @return the list of proctoring options; null if none
     */
    public static List<EProctoringOption> getProctoringOptions(final RawCsection csection) {

        final String course = csection.course;
        final String sect = csection.sect;

        // FIXME: For now, we base some data on first digit of section number!
        final char sectChar0 = sect == null || sect.isEmpty() ? 0 : sect.charAt(0);

        List<EProctoringOption> proctoringOptions = null;

        final boolean isPrecalcCourse = RawRecordConstants.M117.equals(course) || RawRecordConstants.M118.equals(course)
                || RawRecordConstants.M124.equals(course) || RawRecordConstants.M125.equals(course)
                || RawRecordConstants.M126.equals(course);

        if (sectChar0 == '8' || sectChar0 == '4') {
            if (isPrecalcCourse) {
                proctoringOptions = new ArrayList<>(6);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.DIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.HUMAN);
                proctoringOptions.add(EProctoringOption.HONORLOCK);
            }
        } else if (sectChar0 == '0') {
            if (isPrecalcCourse) {
                proctoringOptions = new ArrayList<>(6);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.DIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.HUMAN);
                proctoringOptions.add(EProctoringOption.RESPONDUS);
            }
        } else if (sectChar0 == '1') {
            if (RawRecordConstants.M100T.equals(course)) {
                // ELM Tutorial
                proctoringOptions = new ArrayList<>(5);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.PROCTOR_U_STUDENT);
                proctoringOptions.add(EProctoringOption.HUMAN);
            } else if (RawRecordConstants.M100P.equals(course)
                    || RawRecordConstants.M1170.equals(course)
                    || RawRecordConstants.M1180.equals(course)
                    || RawRecordConstants.M1240.equals(course)
                    || RawRecordConstants.M1250.equals(course)
                    || RawRecordConstants.M1260.equals(course)) {
                // Placement Tool or Precalculus Tutorial
                proctoringOptions = new ArrayList<>(5);
                proctoringOptions.add(EProctoringOption.DEPT_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.UNIV_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.ASSIST_TEST_CENTER);
                proctoringOptions.add(EProctoringOption.PROCTOR_U_STUDENT);
                proctoringOptions.add(EProctoringOption.HUMAN);
            }
        }

        return proctoringOptions;
    }
}
