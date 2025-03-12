package dev.mathops.db.reclogic.main;

import dev.mathops.db.Cache;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.FacilityHoursRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * A utility class to work with "facility_hours" records.
 *
 * <pre>
 * CREATE TABLE IF NOT EXISTS main.facility_hours (
 *   facility       CHAR(10)  NOT NULL,
 *   display_index  SMALLINT  NOT NULL,
 *   weekdays       SMALLINT  NOT NULL,
 *   start_dt       DATE      NOT NULL,
 *   end_dt         DATE      NOT NULL,
 *   open_time_1    TIME      NOT NULL,
 *   close_time_1   TIME      NOT NULL,
 *   open_time_2    TIME,
 *   close_time_2   TIME,
 *   CONSTRAINT facility_hours_pk PRIMARY KEY (facility, display_index)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class FacilityHoursLogic implements IRecLogic<FacilityHoursRec> {

    /** A single instance. */
    public static final FacilityHoursLogic INSTANCE = new FacilityHoursLogic();

    /** A field name. */
    private static final String FLD_FACILITY = "facility";

    /** A field name. */
    private static final String FLD_DISPLAY_INDEX = "display_index";

    /** A field name. */
    private static final String FLD_WEEKDAYS = "weekdays";

    /** A field name. */
    private static final String FLD_START_DT = "start_dt";

    /** A field name. */
    private static final String FLD_END_DT = "end_dt";

    /** A field name. */
    private static final String FLD_OPEN_TIME_1 = "open_time_1";

    /** A field name. */
    private static final String FLD_CLOSE_TIME_1 = "close_time_1";

    /** A field name. */
    private static final String FLD_OPEN_TIME_2 = "open_time_2";

    /** A field name. */
    private static final String FLD_CLOSE_TIME_2 = "close_time_2";

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FacilityHoursLogic() {

        super();
    }

    /**
     * Gets the instance of {@code FacilityHoursLogic} appropriate to a cache. The result will depend on the database
     * installation type of the MAIN schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code FacilityLogic} object (null if none found)
     */
    public static FacilityHoursLogic get(final Cache cache) {

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
    public boolean insert(final Cache cache, final FacilityHoursRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".facility_hours (facility,",
                "display_index,weekdays,start_dt,end_dt,open_time_1,close_time_1,open_time_2,close_time_2) VALUES (",
                sqlStringValue(record.facility), ",",
                sqlIntegerValue(record.displayIndex), ",",
                sqlIntegerValue(record.weekdays), ",",
                sqlDateValue(record.startDt), ",",
                sqlDateValue(record.endDt), ",",
                sqlTimeValue(record.openTime1), ",",
                sqlTimeValue(record.closeTime1), ",",
                sqlTimeValue(record.openTime2), ",",
                sqlTimeValue(record.closeTime2), ")");

        return doUpdateOneRow(cache, sql);
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

        final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".facility_hours WHERE facility=",
                sqlStringValue(record.facility), " AND display_index=",
                sqlIntegerValue(record.displayIndex));

        return doUpdateOneRow(cache, sql);
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

        final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility_hours");

        return doListQuery(cache, sql);
    }

    /**
     * Queries for all facility hours records for a single facility.
     *
     * @param cache    the data cache
     * @param facility the facility ID for which to query
     * @return the facility; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public List<FacilityHoursRec> queryByFacility(final Cache cache, final String facility) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility_hours WHERE facility=",
                sqlStringValue(facility));

        return doListQuery(cache, sql);
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

        final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".facility_hours SET weekdays=",
                sqlIntegerValue(record.weekdays), ",start_dt=",
                sqlDateValue(record.startDt), ",end_dt=",
                sqlDateValue(record.endDt), ",open_time_1=",
                sqlTimeValue(record.openTime1), ",close_time_1=",
                sqlTimeValue(record.closeTime1), ",open_time_2=",
                sqlTimeValue(record.openTime2), ",close_Time_2=",
                sqlTimeValue(record.closeTime2), " WHERE facility=",
                sqlStringValue(record.facility), " AND display_index=",
                sqlIntegerValue(record.displayIndex));

        return doUpdateOneRow(cache, sql);
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

        final String theFacility = getStringField(rs, FLD_FACILITY);
        final Integer theDisplayIndex = getIntegerField(rs, FLD_DISPLAY_INDEX);
        final Integer theWeekdays = getIntegerField(rs, FLD_WEEKDAYS);
        final LocalDate theStartDt = getDateField(rs, FLD_START_DT);
        final LocalDate theEndDt = getDateField(rs, FLD_END_DT);
        final LocalTime theOpenTime1 = getTimeField(rs, FLD_OPEN_TIME_1);
        final LocalTime theCloseTime1 = getTimeField(rs, FLD_CLOSE_TIME_1);
        final LocalTime theOpenTime2 = getTimeField(rs, FLD_OPEN_TIME_2);
        final LocalTime theCloseTime2 = getTimeField(rs, FLD_CLOSE_TIME_2);

        return new FacilityHoursRec(theFacility, theDisplayIndex, theWeekdays, theStartDt, theEndDt, theOpenTime1,
                theCloseTime1, theOpenTime2, theCloseTime2);
    }
}
