package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.StandardsMilestoneRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standards milestone" records.
 *
 * <pre>
 * CREATE TABLE term_202510.standards_milestone (
 *     pace_track               char(1)        NOT NULL,  -- The pace track
 *     pace                     smallint       NOT NULL,  -- The pace
 *     pace_index               smallint       NOT NULL,  -- The pace index
 *     module_nbr               smallint       NOT NULL,  -- The module number
 *     ms_type                  char(2)        NOT NULL,  -- The milestone type
 *     ms_date                  date           NOT NULL,  -- The milestone date
 *     PRIMARY KEY (pace_track, pace, pace_index, module_nbr, ms_type)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardsMilestoneLogic implements IRecLogic<StandardsMilestoneRec> {

    /** A single instance. */
    public static final StandardsMilestoneLogic INSTANCE = new StandardsMilestoneLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardsMilestoneLogic() {

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
    public boolean insert(final Cache cache, final StandardsMilestoneRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".standards_milestone (pace_track,pace,pace_index,module_nbr,ms_type,ms_date) VALUES (",
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
    public boolean delete(final Cache cache, final StandardsMilestoneRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standards_milestone WHERE pace_track=", sqlStringValue(record.paceTrack),
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
    public List<StandardsMilestoneRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StandardsMilestoneRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standards_milestone");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a standards milestone by its ID.
     *
     * @param cache     the data cache
     * @param paceTrack the pace track for which to query
     * @param pace      the pace for which to query
     * @param paceIndex the pace index for which to query
     * @param moduleNbr the module number for which to query
     * @param msType    the milestone type for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardsMilestoneRec query(final Cache cache, final String paceTrack, final Integer pace,
                                       final Integer paceIndex, final Integer moduleNbr, final String msType)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StandardsMilestoneRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_milestone WHERE pace_track=", sqlStringValue(paceTrack),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex),
                    " AND module_nbr=", sqlIntegerValue(moduleNbr),
                    " AND ms_type=", sqlStringValue(msType));
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
    public boolean update(final Cache cache, final StandardsMilestoneRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standards_milestone SET ms_date=", sqlDateValue(record.msDate),
                    " WHERE pace_track=", sqlStringValue(record.paceTrack),
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
    public StandardsMilestoneRec fromResultSet(final ResultSet rs) throws SQLException {

        final String thePaceTrack = getStringField(rs, DataDict.FLD_PACE_TRACK);
        final Integer thePace = getIntegerField(rs, DataDict.FLD_PACE);
        final Integer thePaceIndex = getIntegerField(rs, DataDict.FLD_PACE_INDEX);
        final Integer theModuleNbr = getIntegerField(rs, DataDict.FLD_MODULE_NBR);
        final String theMsType = getStringField(rs, DataDict.FLD_MS_TYPE);
        final LocalDate theMsDate = getDateField(rs, DataDict.FLD_MS_DATE);

        return new StandardsMilestoneRec(thePaceTrack, thePace, thePaceIndex, theModuleNbr, theMsType, theMsDate);
    }
}
