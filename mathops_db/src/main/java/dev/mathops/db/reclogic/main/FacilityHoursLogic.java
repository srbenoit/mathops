package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.FacilityHoursRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "facility_hours" records.
 *
 * <pre>
 * CREATE TABLE main.facility_hours (
 *     facility_id              char(10)        NOT NULL,  -- The facility ID (references facility table)
 *     display_index            smallint        NOT NULL,  -- The display index (rows display in order)
 *     weekdays                 smallint        NOT NULL,  -- Weekday (logical OR of 1=Sun, 2=Mon, 4=Tue,
 *                                                         --  8=Wed, 16=Thu, 32=Fri, 64=Sat)
 *     start_date               date            NOT NULL,  -- The first date the facility is open
 *     end_date                 date            NOT NULL,  -- The last date the facility is open
 *     open_time_1              time            NOT NULL,  -- The time the facility opens
 *     close_time_1             time            NOT NULL,  -- The time the facility closes
 *     open_time_2              time,                      -- The time the facility re-opens
 *     close_time_2             time,                      -- The time the facility closes after re-opening
 *     PRIMARY KEY (facility_id, display_index)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class FacilityHoursLogic implements IRecLogic<FacilityHoursRec> {

    /** A single instance. */
    public static final FacilityHoursLogic INSTANCE = new FacilityHoursLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FacilityHoursLogic() {

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
    public boolean insert(final Cache cache, final FacilityHoursRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".facility_hours (facility_id,display_index,weekdays,start_date,end_date,open_time_1,close_time_1,",
                    "open_time_2,close_time_2) VALUES (",
                    sqlStringValue(record.facilityId), ",",
                    sqlIntegerValue(record.displayIndex), ",",
                    sqlIntegerValue(record.weekdays), ",",
                    sqlDateValue(record.startDate), ",",
                    sqlDateValue(record.endDate), ",",
                    sqlTimeValue(record.openTime1), ",",
                    sqlTimeValue(record.closeTime1), ",",
                    sqlTimeValue(record.openTime2), ",",
                    sqlTimeValue(record.closeTime2), ")");

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
    public boolean delete(final Cache cache, final FacilityHoursRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".facility_hours WHERE facility_id=",
                    sqlStringValue(record.facilityId), " AND display_index=",
                    sqlIntegerValue(record.displayIndex));

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
    public List<FacilityHoursRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<FacilityHoursRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility_hours");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for all facility hours records for a single facility.
     *
     * @param cache        the data cache
     * @param facilityId   the facility ID for which to query
     * @param displayIndex the display index for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public FacilityHoursRec query(final Cache cache, final String facilityId, final Integer displayIndex)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final FacilityHoursRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".facility_hours WHERE facility_id=", sqlStringValue(facilityId), " AND display_index=",
                    sqlIntegerValue(displayIndex));

            result = doSingleQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for all facility hours records for a single facility.
     *
     * @param cache      the data cache
     * @param facilityId the facility ID for which to query
     * @return the list of facility hours records (could be empty)
     * @throws SQLException if there is an error performing the query
     */
    public List<FacilityHoursRec> queryByFacility(final Cache cache, final String facilityId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<FacilityHoursRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".facility_hours WHERE facility_id=", sqlStringValue(facilityId));

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
    public boolean update(final Cache cache, final FacilityHoursRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".facility_hours SET weekdays=",
                    sqlIntegerValue(record.weekdays), ",start_date=",
                    sqlDateValue(record.startDate), ",end_date=",
                    sqlDateValue(record.endDate), ",open_time_1=",
                    sqlTimeValue(record.openTime1), ",close_time_1=",
                    sqlTimeValue(record.closeTime1), ",open_time_2=",
                    sqlTimeValue(record.openTime2), ",close_Time_2=",
                    sqlTimeValue(record.closeTime2), " WHERE facility_id=",
                    sqlStringValue(record.facilityId), " AND display_index=",
                    sqlIntegerValue(record.displayIndex));

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
    public FacilityHoursRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theFacilityId = getStringField(rs, DataDict.FLD_FACILITY_ID);
        final Integer theDisplayIndex = getIntegerField(rs, DataDict.FLD_DISPLAY_INDEX);
        final Integer theWeekdays = getIntegerField(rs, DataDict.FLD_WEEKDAYS);
        final LocalDate theStartDate = getDateField(rs, DataDict.FLD_START_DATE);
        final LocalDate theEndDate = getDateField(rs, DataDict.FLD_END_DATE);
        final LocalTime theOpenTime1 = getTimeField(rs, DataDict.FLD_OPEN_TIME_1);
        final LocalTime theCloseTime1 = getTimeField(rs, DataDict.FLD_CLOSE_TIME_1);
        final LocalTime theOpenTime2 = getTimeField(rs, DataDict.FLD_OPEN_TIME_2);
        final LocalTime theCloseTime2 = getTimeField(rs, DataDict.FLD_CLOSE_TIME_2);

        return new FacilityHoursRec(theFacilityId, theDisplayIndex, theWeekdays, theStartDate, theEndDate, theOpenTime1,
                theCloseTime1, theOpenTime2, theCloseTime2);
    }
}
