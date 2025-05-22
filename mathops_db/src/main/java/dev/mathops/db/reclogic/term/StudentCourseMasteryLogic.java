package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.rec.main.StandardAssignmentRec;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardAssignmentAttemptRec;
import dev.mathops.db.rec.term.StudentCourseMasteryRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "student course mastery" records.
 *
 * <pre>
 * CREATE TABLE IF NOT EXISTS term_202510.student_course_mastery (
 *     student_id               char(9)        NOT NULL,  -- The student ID
 *     course_id                char(10)       NOT NULL,  -- The course ID
 *     course_structure         varchar(200)   NOT NULL,  -- The course structure in a format like:
 *                                                        --     "aAabbbCcc...zzZ", where each letter (a-z) represents a
 *                                                        --     module and each repetition of that letter represents a
 *                                                        --     standard, lowercase=non-essential, uppercase=essential
 *     homework_status          varchar(200)   NOT NULL,  -- Student status on standard homeworks for each standard
 *                                                        --     in a format like "YN---", the same length as the course
 *                                                        --     structure, Y=passed, N=attempted, -=not attempted
 *     mastery_status           varchar(200)   NOT NULL,  -- Student mastery status for each standard, in a format
 *                                                        --     like "Yyn---", the same length as the course structure,
 *                                                        --     Y=mastered on time, y=mastered late, N=attempted,
 *                                                        --     -=not attempted
 *     nbr_completed_hw         smallint       NOT NULL,  -- The number of completed homework sets (of 24)
 *     nbr_mastered_standards   smallint       NOT NULL,  -- The number of mastered standards (out of 24)
 *     completed                char(1)        NOT NULL,  -- "Y" if course is completed, "N" if not.
 *     score                    smallint       NOT NULL,  -- The current score
 *     PRIMARY KEY (student_id, course_id)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StudentCourseMasteryLogic implements IRecLogic<StudentCourseMasteryRec> {

    /** A single instance. */
    public static final StudentCourseMasteryLogic INSTANCE = new StudentCourseMasteryLogic();

    /** Characters used to indicate modules within a {@code StudentCourseMasteryRec}. */
    private static final String STRUCTURE_CHARS = "abcdefghijklmnopqrestuvwxyz";

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentCourseMasteryLogic() {

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
    public boolean insert(final Cache cache, final StudentCourseMasteryRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".student_course_mastery ",
                    "(student_id,course_id,course_structure,homework_status,mastery_status,completed,score) VALUES (",
                    sqlStringValue(record.studentId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlStringValue(record.courseStructure), ",",
                    sqlStringValue(record.homeworkStatus), ",",
                    sqlStringValue(record.masteryStatus), ",",
                    sqlStringValue(record.completed), ",",
                    sqlIntegerValue(record.score), ")");

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
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
    @Override
    public boolean delete(final Cache cache, final StudentCourseMasteryRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".student_course_mastery WHERE student_id=", sqlStringValue(record.studentId),
                    " AND course_id=", sqlStringValue(record.courseId));

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries every record in the database.
     *
     * @param cache the data cache
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public List<StudentCourseMasteryRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StudentCourseMasteryRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".student_course_mastery");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for all student course mastery records for a student.
     *
     * @param cache     the data cache
     * @param studentId the student ID for which to query
     * @return the list of matching records
     * @throws SQLException if there is an error performing the query
     */
    public List<StudentCourseMasteryRec> queryByStudent(final Cache cache, final String studentId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StudentCourseMasteryRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".student_course_mastery WHERE student_id=", sqlStringValue(studentId));

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a student course mastery by student and course ID.
     *
     * @param cache     the data cache
     * @param studentId the student ID for which to query
     * @param courseId  the course ID for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StudentCourseMasteryRec query(final Cache cache, final String studentId,
                                         final String courseId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StudentCourseMasteryRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".student_course_mastery WHERE student_id=", sqlStringValue(studentId),
                    " AND course_id=", sqlStringValue(courseId));

            result = doSingleQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Updates a record.
     *
     * @param cache  the data cache
     * @param record the record to update
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public boolean update(final Cache cache, final StudentCourseMasteryRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".student_course_mastery SET course_structure=", sqlStringValue(record.courseStructure),
                    ",homework_status=", sqlStringValue(record.homeworkStatus),
                    ",mastery_status=", sqlStringValue(record.masteryStatus),
                    ",completed=", sqlStringValue(record.completed),
                    ",score=", sqlIntegerValue(record.score),
                    " WHERE student_id=", sqlStringValue(record.studentId),
                    " AND course_id=", sqlStringValue(record.courseId));

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Extracts a record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public StudentCourseMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theStudentId = getStringField(rs, DataDict.FLD_STUDENT_ID);
        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final String theCourseStructure = getStringField(rs, DataDict.FLD_COURSE_STRUCTURE);
        final String theHomeworkStatus = getStringField(rs, DataDict.FLD_HOMEWORK_STATUS);
        final String theMasteryStatus = getStringField(rs, DataDict.FLD_MASTERY_STATUS);
        final String theCompleted = getStringField(rs, DataDict.FLD_COMPLETED);
        final Integer theScore = getIntegerField(rs, DataDict.FLD_SCORE);

        return new StudentCourseMasteryRec(theStudentId, theCourseId, theCourseStructure, theHomeworkStatus,
                theMasteryStatus, theCompleted, theScore);
    }

    /**
     * Constructs the course structure, homework status, and mastery status for a student in a course and either updates
     * the existing {@code StudentCourseMasteryRec} or inserts a new record if none exists.  Any time new work is
     * submitted or a deadline is changed, this can be called to ensure the database has accurate status.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return the constructed {@code StudentCourseMasteryRec}; {@code null} if the object could not be constructed
     * @throws SQLException if there is an error accessing the database
     */
    public static StudentCourseMasteryRec buildCourseMastery(final Cache cache, final String studentId,
                                                             final String courseId) throws SQLException {

        StudentCourseMasteryRec result = null;

        final TermData termData = cache.getTermData();
        final MainData mainData = cache.getMainData();

        final StandardsCourseRec course = mainData.getStandardsCourse(courseId);
        boolean ok = true;
        if (course != null) {
            final HtmlBuilder structure = new HtmlBuilder(50);
            final HtmlBuilder homeworkStatus = new HtmlBuilder(50);
            final HtmlBuilder masteryStatus = new HtmlBuilder(50);

            final List<StandardsCourseModuleRec> modules = mainData.getStandardsCourseModules(courseId);
            final List<StandardAssignmentRec> assignments = mainData.getStandardAssignments(courseId);
            final List<StandardAssignmentAttemptRec> attempts = termData.getStandardAssignmentAttempts(studentId);

            final int nbrModules = course.nbrModules.intValue();

            for (int moduleNbr = 1; moduleNbr <= nbrModules; ++moduleNbr) {

                final char structureChar = STRUCTURE_CHARS.charAt(moduleNbr - 1);
                StandardsCourseModuleRec courseModule = null;
                for (final StandardsCourseModuleRec rec : modules) {
                    if (rec.moduleNbr.intValue() == moduleNbr) {
                        courseModule = rec;

                        break;
                    }
                }
                if (courseModule == null) {
                    Log.warning("There was no StandardsCourseModuleRec for ", courseId, " module ", moduleNbr);
                    ok = false;
                    break;
                }
//                if (metaCourse.modules.size() < moduleNbr) {
//                    Log.warning("There was no MetadataCourseModule for ", courseId, " module ", moduleNbr);
//                    ok = false;
//                    break;
//                }

                final int nbrStandards = courseModule.nbrStandards.intValue();

                for (int standardNbr = 1; standardNbr <= nbrStandards; ++standardNbr) {

                    StandardAssignmentRec homework = null;
                    StandardAssignmentRec mastery = null;
                    boolean homeworkAttempted = false;
                    boolean homeworkPassed = false;
                    boolean masteryAttempted = false;
                    boolean masteredOnTime = false;
                    boolean masteredLate = false;

                    for (final StandardAssignmentRec rec : assignments) {
                        if (rec.moduleNbr.intValue() == moduleNbr && rec.standardNbr.intValue() == standardNbr) {
                            if ("HW".equals(rec.assignmentType)) {
                                homework = rec;
                            } else if ("MA".equals(rec.assignmentType)) {
                                mastery = rec;
                            }
                        }
                    }

                    if (homework != null) {
                        for (final StandardAssignmentAttemptRec attempt : attempts) {
                            if (attempt.assignmentId.equals(homework.assignmentId)) {
                                homeworkAttempted = true;
                                if ("Y".equals(attempt.passed)) {
                                    homeworkPassed = true;
                                }
                            }
                        }
                    }

                    if (mastery != null) {
                        for (final StandardAssignmentAttemptRec attempt : attempts) {
                            if (attempt.assignmentId.equals(mastery.assignmentId)) {
                                masteryAttempted = true;
                                if ("Y".equals(attempt.passed)) {
                                    // Need to see if it was passed on time.
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
