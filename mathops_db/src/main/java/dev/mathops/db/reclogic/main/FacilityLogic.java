package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.FacilityRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "facility" records.
 *
 * <pre>
 * CREATE TABLE main.facility (
 *     facility_id              char(10)        NOT NULL,  -- A unique ID for each facility (not visible)
 *     facility_name            varchar(100)    NOT NULL,  -- The facility name (visible)
 *     building_name            varchar(40),               -- Building name, null if virtual
 *     room_nbr                 varchar(20),               -- Room number, null if virtual
 *     PRIMARY KEY (facility_id)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class FacilityLogic implements IRecLogic<FacilityRec> {

    /** A single instance. */
    public static final FacilityLogic INSTANCE = new FacilityLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FacilityLogic() {

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
    public boolean insert(final Cache cache, final FacilityRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".facility (facility_id,facility_name,building_name,room_nbr) VALUES (",
                    sqlStringValue(record.facilityId), ",",
                    sqlStringValue(record.facilityName), ",",
                    sqlStringValue(record.buildingName), ",",
                    sqlStringValue(record.roomNbr), ")");

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
    public boolean delete(final Cache cache, final FacilityRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".facility WHERE facility_id=",
                    sqlStringValue(record.facilityId));

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
    public List<FacilityRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<FacilityRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for a facility by its ID.
     *
     * @param cache    the data cache
     * @param facility the facility ID for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public FacilityRec query(final Cache cache, final String facility) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final FacilityRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility WHERE facility_id=",
                    sqlStringValue(facility));

            result = doSingleQuery(cache, ESchema.MAIN, sql);
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
    public boolean update(final Cache cache, final FacilityRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".facility SET facility_name=",
                    sqlStringValue(record.facilityName), ",building_name=", sqlStringValue(record.buildingName),
                    ",room_nbr=", sqlStringValue(record.roomNbr), " WHERE facility_id=",
                    sqlStringValue(record.facilityId));

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
    public FacilityRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theFacilityId = getStringField(rs, DataDict.FLD_FACILITY_ID);
        final String theFacilityName = getStringField(rs, DataDict.FLD_FACILITY_NAME);
        final String theBuildingName = getStringField(rs, DataDict.FLD_BUILDING_NAME);
        final String theRoomNbr = getStringField(rs, DataDict.FLD_ROOM_NBR);

        return new FacilityRec(theFacilityId, theFacilityName, theBuildingName, theRoomNbr);
    }
}
