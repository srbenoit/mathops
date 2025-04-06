package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
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
 * CREATE TABLE IF NOT EXISTS main.facility (
 *   facility       CHAR(10)     NOT NULL,
 *   name           VARCHAR(100) NOT NULL,
 *   building       VARCHAR(40),
 *   room           VARCHAR(20),
 *   CONSTRAINT facility_pk PRIMARY KEY (facility)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class FacilityLogic implements IRecLogic<FacilityRec> {

    /** A single instance. */
    public static final FacilityLogic INSTANCE = new FacilityLogic();

    /** A field name. */
    private static final String FLD_FACILITY = "facility";

    /** A field name. */
    private static final String FLD_NAME = "name";

    /** A field name. */
    private static final String FLD_BUILDING = "building";

    /** A field name. */
    private static final String FLD_ROOM = "room";

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FacilityLogic() {

        super();
    }

    /**
     * Gets the instance of {@code FacilityLogic} appropriate to a cache. The result will depend on the database
     * installation type of the MAIN schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code FacilityLogic} object (null if none found)
     */
    public static FacilityLogic get(final Cache cache) {

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
    public boolean insert(final Cache cache, final FacilityRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".facility (facility,name,building,room) VALUES (",
                    sqlStringValue(record.facilityId), ",",
                    sqlStringValue(record.facilityName), ",",
                    sqlStringValue(record.buildingName), ",",
                    sqlStringValue(record.roomNbr), ")");

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
    public boolean delete(final Cache cache, final FacilityRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".facility WHERE facility=",
                    sqlStringValue(record.facilityId));

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
    public List<FacilityRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<FacilityRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility");

            result = doListQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for a facility by its ID.
     *
     * @param cache    the data cache
     * @param facility the facility ID for which to query
     * @return the facility; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public FacilityRec query(final Cache cache, final String facility) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final FacilityRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".facility WHERE facility=",
                    sqlStringValue(facility));

            return doSingleQuery(cache, sql);
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
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".facility SET name=",
                    sqlStringValue(record.facilityName), ",building=", sqlStringValue(record.buildingName), ",room=",
                    sqlStringValue(record.roomNbr), " WHERE facility=",
                    sqlStringValue(record.facilityId));

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
    public FacilityRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theFacility = getStringField(rs, FLD_FACILITY);
        final String theName = getStringField(rs, FLD_NAME);
        final String theBuilding = getStringField(rs, FLD_BUILDING);
        final String theRoom = getStringField(rs, FLD_ROOM);

        return new FacilityRec(theFacility, theName, theBuilding, theRoom);
    }
}
