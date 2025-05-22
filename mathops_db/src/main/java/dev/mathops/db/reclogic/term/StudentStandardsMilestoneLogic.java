package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.StudentStandardsMilestoneRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "student standards milestone" records.
 *
 * <pre>
 * CREATE TABLE term_202510.student_standards_milestone (
 *     student_id               char(9)        NOT NULL,  -- The student ID
 *     pace_track               char(1)        NOT NULL,  -- The pace track
 *     pace                     smallint       NOT NULL,  -- The pace
 *     pace_index               smallint       NOT NULL,  -- The pace index
 *     module_nbr               smallint       NOT NULL,  -- The module number
 *     ms_type                  char(2)        NOT NULL,  -- The milestone type
 *     ms_date                  date           NOT NULL,  -- The new milestone date
 *     PRIMARY KEY (student_id, pace_track, pace, pace_index, module_nbr, ms_type)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StudentStandardsMilestoneLogic implements IRecLogic<StudentStandardsMilestoneRec> {

    /** A single instance. */
    public static final StudentStandardsMilestoneLogic INSTANCE = new StudentStandardsMilestoneLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentStandardsMilestoneLogic() {

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
    public boolean insert(final Cache cache, final StudentStandardsMilestoneRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".student_standards_milestone ",
                    "(student_id,pace_track,pace,pace_index,module_nbr,ms_type,ms_date) VALUES (",
                    sqlStringValue(record.studentId), ",",
                    sqlStringValue(record.paceTrack), ",",
                    sqlIntegerValue(record.pace), ",",
                    sqlIntegerValue(record.paceIndex), ",",
                    sqlIntegerValue(record.moduleNbr), ",",
                    sqlStringValue(record.msType), ",",
                    sqlDateValue(record.msDate), ")");

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
    public boolean delete(final Cache cache, final StudentStandardsMilestoneRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".student_standards_milestone WHERE student_id=", sqlStringValue(record.studentId),
                    " AND pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND module_nbr=", sqlIntegerValue(record.moduleNbr),
                    " AND ms_type=", sqlStringValue(record.msType));

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
    public List<StudentStandardsMilestoneRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StudentStandardsMilestoneRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".student_standards_milestone");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a standards milestone by its ID.
     *
     * @param cache     the data cache
     * @param studentId the student ID for which to query
     * @param paceTrack the pace track for which to query
     * @param pace      the pace for which to query
     * @param paceIndex the pace index for which to query
     * @param moduleNbr the module number for which to query
     * @param msType    the milestone type for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StudentStandardsMilestoneRec query(final Cache cache, final String studentId, final String paceTrack,
                                              final Integer pace, final Integer paceIndex, final Integer moduleNbr,
                                              final String msType)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StudentStandardsMilestoneRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".student_standards_milestone WHERE student_id=", sqlStringValue(studentId),
                    " AND pace_track=", sqlStringValue(paceTrack),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex),
                    " AND module_nbr=", sqlIntegerValue(moduleNbr),
                    " AND ms_type=", sqlStringValue(msType));

            result = doSingleQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries all standards milestone overrides records for a single student.
     *
     * @param cache     the data cache
     * @param studentId the student for which to query
     * @return the list of records for the specified student
     * @throws SQLException if there is an error performing the query
     */
    public List<StudentStandardsMilestoneRec> queryByStudent(final Cache cache, final String studentId)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StudentStandardsMilestoneRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".student_standards_milestone WHERE student_id=", sqlStringValue(studentId));

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
    public boolean update(final Cache cache, final StudentStandardsMilestoneRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".student_standards_milestone SET ms_date=", sqlDateValue(record.msDate),
                    " WHERE student_id=", sqlStringValue(record.studentId),
                    " AND pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND module_nbr=", sqlIntegerValue(record.moduleNbr),
                    " AND ms_type=", sqlStringValue(record.msType));

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
    public StudentStandardsMilestoneRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theStudentId = getStringField(rs, DataDict.FLD_STUDENT_ID);
        final String thePaceTrack = getStringField(rs, DataDict.FLD_PACE_TRACK);
        final Integer thePace = getIntegerField(rs, DataDict.FLD_PACE);
        final Integer thePaceIndex = getIntegerField(rs, DataDict.FLD_PACE_INDEX);
        final Integer theModuleNbr = getIntegerField(rs, DataDict.FLD_MODULE_NBR);
        final String theMsType = getStringField(rs, DataDict.FLD_MS_TYPE);
        final LocalDate theMsDate = getDateField(rs, DataDict.FLD_MS_DATE);

        return new StudentStandardsMilestoneRec(theStudentId, thePaceTrack, thePace, thePaceIndex, theModuleNbr,
                theMsType, theMsDate);
    }
}
