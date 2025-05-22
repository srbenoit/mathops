package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.StudentPreferenceRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "student preference" records.
 *
 * <pre>
 * CREATE TABLE term_202510.student_preference (
 *     student_id               char(9)        NOT NULL,  -- The student ID
 *     pref_key                 char(4)        NOT NULL,  -- A key that identifies a preference ('MESG')
 *     pref_value               smallint       NOT NULL,  -- The student's preference setting
 *     PRIMARY KEY (student_id, pref_key)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StudentPreferenceLogic implements IRecLogic<StudentPreferenceRec> {

    /** A single instance. */
    public static final StudentPreferenceLogic INSTANCE = new StudentPreferenceLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentPreferenceLogic() {

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
    public boolean insert(final Cache cache, final StudentPreferenceRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".student_preference (student_id,pref_key,pref_value) VALUES (",
                    sqlStringValue(record.studentId), ",",
                    sqlStringValue(record.prefKey), ",",
                    sqlIntegerValue(record.prefValue), ")");

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
    public boolean delete(final Cache cache, final StudentPreferenceRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".student_preference WHERE student_id=", sqlStringValue(record.studentId),
                    " AND pref_key=", sqlStringValue(record.prefKey));

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
    public List<StudentPreferenceRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StudentPreferenceRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".student_preference");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries all preference records for a single student.
     *
     * @param cache     the data cache
     * @param studentId the student for which to query
     * @return the list of records for the specified student
     * @throws SQLException if there is an error performing the query
     */
    public StudentPreferenceRec query(final Cache cache, final String studentId, final String prefKey)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StudentPreferenceRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".student_preference WHERE student_id=", sqlStringValue(studentId),
                    " AND pref_key=", sqlStringValue(prefKey));

            result = doSingleQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries all preference records for a single student.
     *
     * @param cache     the data cache
     * @param studentId the student for which to query
     * @return the list of records for the specified student
     * @throws SQLException if there is an error performing the query
     */
    public List<StudentPreferenceRec> queryByStudent(final Cache cache, final String studentId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StudentPreferenceRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".student_preference WHERE student_id=", sqlStringValue(studentId));

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
    public boolean update(final Cache cache, final StudentPreferenceRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".student_preference SET pref_value=", sqlIntegerValue(record.prefValue),
                    " WHERE student_id=", sqlStringValue(record.studentId),
                    " AND pref_key=", sqlStringValue(record.prefKey));

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
    public StudentPreferenceRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theStudentId = getStringField(rs, DataDict.FLD_STUDENT_ID);
        final String thePrefKey = getStringField(rs, DataDict.FLD_PREF_KEY);
        final Integer thePrefValue = getIntegerField(rs, DataDict.FLD_PREF_VALUE);

        return new StudentPreferenceRec(theStudentId, thePrefKey, thePrefValue);
    }
}
