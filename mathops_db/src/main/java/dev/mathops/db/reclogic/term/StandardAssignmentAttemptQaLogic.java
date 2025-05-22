package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.StandardAssignmentAttemptQaRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standard assignment attempt qa" records.
 *
 * <pre>
 * CREATE TABLE term_202510.standard_assignment_attempt_qa (
 *     serial_nbr                integer         NOT NULL,  -- The serial number (ref. standard_assignment_attempt)
 *     question_nbr              smallint        NOT NULL,  -- The question number
 *     points                    smallint        NOT NULL,  -- Points earned
 *     item_id                   varchar(20),               -- The item ID
 *     PRIMARY KEY (serial_nbr, question_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardAssignmentAttemptQaLogic implements IRecLogic<StandardAssignmentAttemptQaRec> {

    /** A single instance. */
    public static final StandardAssignmentAttemptQaLogic INSTANCE = new StandardAssignmentAttemptQaLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardAssignmentAttemptQaLogic() {

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
    public boolean insert(final Cache cache, final StandardAssignmentAttemptQaRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".standard_assignment_attempt_qa (serial_nbr,question_nbr,points,item_id) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlIntegerValue(record.questionNbr), ",",
                    sqlIntegerValue(record.points), ",",
                    sqlStringValue(record.itemId), ")");

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
    public boolean delete(final Cache cache, final StandardAssignmentAttemptQaRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standard_assignment_attempt_qa WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND question_nbr=", sqlIntegerValue(record.questionNbr));

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
    public List<StandardAssignmentAttemptQaRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StandardAssignmentAttemptQaRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standard_assignment_attempt_qa");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a standard assignment attempt answer by its serial number and question number.
     *
     * @param cache       the data cache
     * @param serialNbr   the serial number for which to query
     * @param questionNbr the question number for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardAssignmentAttemptQaRec query(final Cache cache, final Integer serialNbr, final Integer questionNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StandardAssignmentAttemptQaRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standard_assignment_attempt_qa WHERE serial_nbr=", sqlIntegerValue(serialNbr),
                    " AND question_nbr=", sqlIntegerValue(questionNbr));

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
    public boolean update(final Cache cache, final StandardAssignmentAttemptQaRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standard_assignment_attempt_qa SET points=", sqlIntegerValue(record.points),
                    ",item_id=", sqlStringValue(record.itemId),
                    " WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND question_nbr=", sqlIntegerValue(record.questionNbr));

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
    public StandardAssignmentAttemptQaRec fromResultSet(final ResultSet rs) throws SQLException {

        final Integer theSerialNbr = getIntegerField(rs, DataDict.FLD_SERIAL_NBR);
        final Integer theQuestionNbr = getIntegerField(rs, DataDict.FLD_QUESTION_NBR);
        final Integer thePoints = getIntegerField(rs, DataDict.FLD_POINTS);
        final String theItemId = getStringField(rs, DataDict.FLD_ITEM_ID);

        return new StandardAssignmentAttemptQaRec(theSerialNbr, theQuestionNbr, thePoints, theItemId);
    }
}
