package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.StandardAssignmentAttemptRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standard assignment attempt" records.
 *
 * <pre>
 * CREATE TABLE term_202510.standard_assignment_attempt (
 *     serial_nbr                integer         NOT NULL,  -- A unique serial number for the attempt
 *     student_id                char(9)         NOT NULL,  -- The student ID who submitted the attempt
 *     assignment_id             varchar(20)     NOT NULL,  -- The ID of the assignment (references standard_assignment)
 *     attempt_date              date            NOT NULL,  -- The date when the attempt is to be counted (the
 *                                                          --     submission date or the day before submission for
 *                                                          --     some assignments accepted early the following day)
 *     attempt_time_sec          integer         NOT NULL,  -- The time of day when the assignment was submitted, in
 *                                                          --     seconds (typically 0 to 86,399, but 86,400 or greater
 *                                                          --     if assignment accepted early the following day and
 *                                                          --     counted as submitted in prior day)
 *     course_id                 char(10)        NOT NULL,  -- The course ID (from standard_assignment)
 *     module_nbr                smallint        NOT NULL,  -- The module number (from standard_assignment)
 *     standard_nbr              smallint        NOT NULL,  -- The standard number (from standard_assignment)
 *     pts_possible              smallint,                  -- The number of points possible (from standard_assignment)
 *     min_passing_score         smallint,                  -- The minimum passing score (from standard_assignment)
 *     score                     smallint        NOT NULL,  -- The earned score
 *     passed                    char(1)         NOT NULL,  -- "Y"=passed, "N"=not passed, "G"=ignore, "P"=passed but
 *                                                          --     invalidated (say, for academic misconduct)
 *     PRIMARY KEY (serial_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardAssignmentAttemptLogic implements IRecLogic<StandardAssignmentAttemptRec> {

    /** A single instance. */
    public static final StandardAssignmentAttemptLogic INSTANCE = new StandardAssignmentAttemptLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardAssignmentAttemptLogic() {

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
    public boolean insert(final Cache cache, final StandardAssignmentAttemptRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".standard_assignment_attempt (serial_nbr,student_id,assignment_id,attempt_date,attempt_time_sec,",
                    "course_id,module_nbr,standard_nbr,pts_possible,min_passing_score,score,passed) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlStringValue(record.studentId), ",",
                    sqlStringValue(record.assignmentId), ",",
                    sqlDateValue(record.attemptDate), ",",
                    sqlIntegerValue(record.attemptTimeSec), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.moduleNbr), ",",
                    sqlIntegerValue(record.standardNbr), ",",
                    sqlIntegerValue(record.ptsPossible), ",",
                    sqlIntegerValue(record.minPassingScore), ",",
                    sqlIntegerValue(record.score), ",",
                    sqlStringValue(record.passed), ")");

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
    public boolean delete(final Cache cache, final StandardAssignmentAttemptRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standard_assignment_attempt WHERE serial_nbr=", sqlIntegerValue(record.serialNbr));

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
    public List<StandardAssignmentAttemptRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StandardAssignmentAttemptRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standard_assignment_attempt");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a standard assignment attempt by its serial number.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardAssignmentAttemptRec query(final Cache cache, final Integer serialNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StandardAssignmentAttemptRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standard_assignment_attempt WHERE serial_nbr=", sqlIntegerValue(serialNbr));

            result = doSingleQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for all standard assignment attempts by a student.
     *
     * @param cache     the data cache
     * @param studentId the student ID whose attempts to retrieve
     * @return the matching records
     * @throws SQLException if there is an error performing the query
     */
    public List<StandardAssignmentAttemptRec> queryByStudent(final Cache cache, final String studentId)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StandardAssignmentAttemptRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standard_assignment_attempt WHERE student_id=", sqlStringValue(studentId));

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for all standard assignment attempts by a student in a course.
     *
     * @param cache     the data cache
     * @param studentId the student ID whose attempts to retrieve
     * @param courseId  the course ID for which to query
     * @return the matching records
     * @throws SQLException if there is an error performing the query
     */
    public List<StandardAssignmentAttemptRec> queryByStudentCourse(final Cache cache, final String studentId,
                                                                   final String courseId)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StandardAssignmentAttemptRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standard_assignment_attempt WHERE student_id=", sqlStringValue(studentId),
                    " AND course_id=", sqlStringValue(courseId));

            result = doListQuery(cache, ESchema.TERM, sql);
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
    public boolean update(final Cache cache, final StandardAssignmentAttemptRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standard_assignment_attempt SET student_id=", sqlStringValue(record.studentId),
                    ",assignment_id=", sqlStringValue(record.assignmentId),
                    ",attempt_date=", sqlDateValue(record.attemptDate),
                    ",attempt_time_sec=", sqlIntegerValue(record.attemptTimeSec),
                    ",course_id=", sqlStringValue(record.courseId),
                    ",module_nbr=", sqlIntegerValue(record.moduleNbr),
                    ",standard_nbr=", sqlIntegerValue(record.standardNbr),
                    ",pts_possible=", sqlIntegerValue(record.ptsPossible),
                    ",min_passing_score=", sqlIntegerValue(record.minPassingScore),
                    ",score=", sqlIntegerValue(record.score),
                    ",passed=", sqlStringValue(record.passed),
                    " WHERE serial_nbr=", sqlIntegerValue(record.serialNbr));

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
    public StandardAssignmentAttemptRec fromResultSet(final ResultSet rs) throws SQLException {

        final Integer theSerialNbr = getIntegerField(rs, DataDict.FLD_SERIAL_NBR);
        final String theStudentId = getStringField(rs, DataDict.FLD_STUDENT_ID);
        final String theAssignmentId = getStringField(rs, DataDict.FLD_ASSIGNMENT_ID);
        final LocalDate theAttemptDate = getDateField(rs, DataDict.FLD_ATTEMPT_DATE);
        final Integer theAttemptTimeSec = getIntegerField(rs, DataDict.FLD_ATTEMPT_TIME_SEC);
        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final Integer theModuleNbr = getIntegerField(rs, DataDict.FLD_MODULE_NBR);
        final Integer theStandardNbr = getIntegerField(rs, DataDict.FLD_STANDARD_NBR);
        final Integer thePtsPossible = getIntegerField(rs, DataDict.FLD_PTS_POSSIBLE);
        final Integer theMinPassingScore = getIntegerField(rs, DataDict.FLD_MIN_PASSING_SCORE);
        final Integer theScore = getIntegerField(rs, DataDict.FLD_SCORE);
        final String thePassed = getStringField(rs, DataDict.FLD_PASSED);

        return new StandardAssignmentAttemptRec(theSerialNbr, theStudentId, theAssignmentId, theAttemptDate,
                theAttemptTimeSec, theCourseId, theModuleNbr, theStandardNbr, thePtsPossible, theMinPassingScore,
                theScore, thePassed);
    }
}
