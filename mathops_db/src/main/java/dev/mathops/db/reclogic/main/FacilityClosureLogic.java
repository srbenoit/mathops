package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.FacilityClosureRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "facility_closure" records.
 *
 * <pre>
 * CREATE TABLE main.facility_closure (
 *     facility_id              char(10)        NOT NULL,  -- The facility ID (references facility table)
 *     closure_date             date            NOT NULL,  -- The date of the closure
 *     closure_type             char(10)        NOT NULL,  -- The type of closure ('HOLIDAY, 'SP_BREAK', 'FA_BREAK',
 *                                                         --  'WEATHER', 'EMERGENCY', 'MAINT', 'EVENT')
 *     start_time               time,                      -- Start time, or null if all day
 *     end_time                 time,                      -- End time, or null if all day
 *     PRIMARY KEY (facility_id, closure_date)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class FacilityClosureLogic implements IRecLogic<FacilityClosureRec> {

    /** A single instance. */
    public static final FacilityClosureLogic INSTANCE = new FacilityClosureLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FacilityClosureLogic() {

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
    public boolean insert(final Cache cache, final FacilityClosureRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".facility_closure (facility_id,start_date,end_date,closure_type,start_time,end_time) VALUES (",
                    sqlStringValue(record.facilityId), ",",
                    sqlDateValue(record.startDate), ",",
                    sqlDateValue(record.endDate), ",",
                    sqlStringValue(record.closureType), ",",
                    sqlTimeValue(record.startTime), ",",
                    sqlTimeValue(record.endTime), ")");

            result = doUpdateOneRow(cache, ESchema.MAIN, sql);
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
    public boolean delete(final Cache cache, final FacilityClosureRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".facility_closure WHERE facility_id=", sqlStringValue(record.facilityId), " AND start_date=",
                    sqlDateValue(record.startDate));

            result = doUpdateOneRow(cache, ESchema.MAIN, sql);
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
    public List<FacilityClosureRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<FacilityClosureRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility_closure");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for all facility closure records for a single facility closure.
     *
     * @param cache      the data cache
     * @param facilityId the facility ID for which to query
     * @param startDate  the start date
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public FacilityClosureRec query(final Cache cache, final String facilityId, final LocalDate startDate)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final FacilityClosureRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".facility_closure WHERE facility_id=", sqlStringValue(facilityId), " AND start_date=",
                    sqlDateValue(startDate));

            result = doSingleQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for all facility hours records for a single facility.
     *
     * @param cache      the data cache
     * @param facilityId the facility ID for which to query
     * @return the facility; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public List<FacilityClosureRec> queryByFacility(final Cache cache, final String facilityId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<FacilityClosureRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".facility_closure WHERE facility_id=", sqlStringValue(facilityId));

            result = doListQuery(cache, ESchema.MAIN, sql);
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
    public boolean update(final Cache cache, final FacilityClosureRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".facility_closure SET end_date=", sqlDateValue(record.endDate),
                    ", closure_type=", sqlStringValue(record.closureType),
                    ",start_time=", sqlTimeValue(record.startTime),
                    ",end_time=", sqlTimeValue(record.endTime),
                    " WHERE facility_id=", sqlStringValue(record.facilityId),
                    " AND start_date=", sqlDateValue(record.startDate));

            result = doUpdateOneRow(cache, ESchema.MAIN, sql);
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
    public FacilityClosureRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theFacility = getStringField(rs, DataDict.FLD_FACILITY_ID);
        final LocalDate theStartDate = getDateField(rs, DataDict.FLD_START_DATE);
        final LocalDate theEndDate = getDateField(rs, DataDict.FLD_END_DATE);
        final String theClosureType = getStringField(rs, DataDict.FLD_CLOSURE_TYPE);
        final LocalTime theStartTime = getTimeField(rs, DataDict.FLD_START_TIME);
        final LocalTime theEndTime = getTimeField(rs, DataDict.FLD_END_TIME);

        return new FacilityClosureRec(theFacility, theStartDate, theEndDate, theClosureType, theStartTime, theEndTime);
    }
}
