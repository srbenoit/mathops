package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
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
 * CREATE TABLE IF NOT EXISTS main.facility_closure (
 *   facility       CHAR(10)  NOT NULL,
 *   closure_dt     DATE      NOT NULL,
 *   closure_type   CHAR(4)   NOT NULL,
 *   start_time     TIME,
 *   end_time       TIME,
 *   CONSTRAINT facility_closures_pk PRIMARY KEY (facility,closure_dt)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class FacilityClosureLogic implements IRecLogic<FacilityClosureRec> {

    /** A single instance. */
    public static final FacilityClosureLogic INSTANCE = new FacilityClosureLogic();

    /** A field name. */
    private static final String FLD_FACILITY = "facility";

    /** A field name. */
    private static final String FLD_CLOSURE_DT = "closure_dt";

    /** A field name. */
    private static final String FLD_CLOSURE_TYPE = "closure_type";

    /** A field name. */
    private static final String FLD_START_TIME = "start_time";

    /** A field name. */
    private static final String FLD_END_TIME = "end_time";

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FacilityClosureLogic() {

        super();
    }

    /**
     * Gets the instance of {@code FacilityHoursLogic} appropriate to a cache. The result will depend on the database
     * installation type of the MAIN schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code FacilityLogic} object (null if none found)
     */
    public static FacilityClosureLogic get(final Cache cache) {

        return INSTANCE;
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
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".facility_closure (facility,",
                    "closure_dt,closure_type,start_time,end_time) VALUES (",
                    sqlStringValue(record.facility), ",",
                    sqlDateValue(record.closureDt), ",",
                    sqlStringValue(record.closureType), ",",
                    sqlTimeValue(record.startTime), ",",
                    sqlTimeValue(record.endTime), ")");

            result = doUpdateOneRow(cache, sql);
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
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".facility_closure WHERE facility=",
                    sqlStringValue(record.facility), " AND closure_dt=",
                    sqlDateValue(record.closureDt));

            result = doUpdateOneRow(cache, sql);
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

            result = doListQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for all facility closure records for a single facility closure.
     *
     * @param cache     the data cache
     * @param facility  the facility ID for which to query
     * @param closureDt the closure date
     * @return the facility; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public FacilityClosureRec query(final Cache cache, final String facility, final LocalDate closureDt) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final FacilityClosureRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility_closure WHERE facility=",
                    sqlStringValue(facility), " AND closure_dt=", sqlDateValue(closureDt));

            result = doSingleQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for all facility hours records for a single facility.
     *
     * @param cache    the data cache
     * @param facility the facility ID for which to query
     * @return the facility; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public List<FacilityClosureRec> queryByFacility(final Cache cache, final String facility) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<FacilityClosureRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility_closure WHERE facility=",
                    sqlStringValue(facility));

            result = doListQuery(cache, sql);
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
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".facility_closure SET closure_type=",
                    sqlStringValue(record.closureType), ",start_time=",
                    sqlTimeValue(record.startTime), ",end_time=",
                    sqlTimeValue(record.endTime), " WHERE facility=",
                    sqlStringValue(record.facility), " AND closure_dt=",
                    sqlDateValue(record.closureDt));

            result = doUpdateOneRow(cache, sql);
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

        final String theFacility = getStringField(rs, FLD_FACILITY);
        final LocalDate theClosureDt = getDateField(rs, FLD_CLOSURE_DT);
        final String theClosureType = getStringField(rs, FLD_CLOSURE_TYPE);
        final LocalTime theStartTime = getTimeField(rs, FLD_START_TIME);
        final LocalTime theEndTime = getTimeField(rs, FLD_END_TIME);

        return new FacilityClosureRec(theFacility, theClosureDt, theClosureType, theStartTime, theEndTime);
    }
}
