package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.StandardMilestoneRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A utility class to work with std_milestone records.
 */
public abstract class StandardMilestoneLogic implements IRecLogic<StandardMilestoneRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardMilestoneLogic() {

        super();
    }

    /**
     * Gets the instance of {@code StandardMilestoneLogic} appropriate to a cache. The result will depend on the
     * database installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code StandardMilestoneLogic} object (null if none found)
     */
    public static StandardMilestoneLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        StandardMilestoneLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
        }

        return result;
    }

    /**
     * Updates the milestone date on a standard milestone record.
     *
     * @param cache   the data cache
     * @param record  the record to be updated
     * @param newDate the new date
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateDate(Cache cache, StandardMilestoneRec record, LocalDate newDate)
            throws SQLException;

    /**
     * Queries for all standard milestones for a specified pace track and pace. This will include milestones for all
     * pace indexes in that pace.
     *
     * @param cache the data cache
     * @param track the pace track
     * @param pace  the pace
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StandardMilestoneRec> queryByPaceTrackPace(Cache cache, final String track,
                                                                    final Integer pace) throws SQLException;

    /**
     * Queries for all standard milestones for a specified pace track, pace, and pace index.
     *
     * @param cache     the data cache
     * @param track     the pace track
     * @param pace      the pace
     * @param paceIndex the pace index
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StandardMilestoneRec> queryByPaceTrackPaceIndex(Cache cache,
                                                                         final String track, final Integer pace,
                                                                         final Integer paceIndex) throws SQLException;

    /**
     * Queries for a single standard milestone pace track, pace, pace index, unit, objective, and milestone type.
     *
     * @param cache     the data cache
     * @param track     the pace track
     * @param pace      the pace
     * @param paceIndex the pace index
     * @param unit      the unit
     * @param objective the objective
     * @param msType    the milestone type
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract StandardMilestoneRec query(Cache cache, String track, Integer pace, Integer paceIndex,
                                               Integer unit, Integer objective, String msType) throws SQLException;

    /**
     * A subclass of {@code StandardMilestoneLogic} designed for the Informix schema.
     */
    public static final class Informix extends StandardMilestoneLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** A field name. */
        private static final String FLD_PACE_TRACK = "pace_track";

        /** A field name. */
        private static final String FLD_PACE = "pace";

        /** A field name. */
        private static final String FLD_PACE_INDEX = "pace_index";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_OBJECTIVE = "objective";

        /** A field name. */
        private static final String FLD_MS_TYPE = "ms_type";

        /** A field name. */
        private static final String FLD_MS_DATE = "ms_date";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StandardMilestoneRec record)
                throws SQLException {

            if (record.paceTrack == null || record.pace == null || record.paceIndex == null
                || record.unit == null || record.objective == null || record.msType == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO std_milestone (pace_track,pace,pace_index,unit,objective,ms_type,ms_date) VALUES (",
                    sqlStringValue(record.paceTrack), ",",
                    sqlIntegerValue(record.pace), ",",
                    sqlIntegerValue(record.paceIndex), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.msType), ",",
                    sqlDateValue(record.msDate), ")");

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean delete(final Cache cache, final StandardMilestoneRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat(
                    "DELETE FROM std_milestone ",
                    "WHERE pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StandardMilestoneRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM std_milestone");
        }

        /**
         * Updates the milestone date on a standard milestone record.
         *
         * @param cache   the data cache
         * @param record  the record to be updated
         * @param newDate the new date
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateDate(final Cache cache, final StandardMilestoneRec record,
                                  final LocalDate newDate) throws SQLException {

            final String sql = SimpleBuilder.concat(
                    "UPDATE std_milestone ",
                    "SET ms_date=", sqlDateValue(newDate),
                    " WHERE pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track and pace. This will include milestones for all
         * pace indexes in that pace.
         *
         * @param cache the data cache
         * @param track the pace track
         * @param pace  the pace
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StandardMilestoneRec> queryByPaceTrackPace(final Cache cache, final String track,
                                                               final Integer pace) throws SQLException {

            final String sql = SimpleBuilder.concat(
                    "SELECT * FROM std_milestone ",
                    "WHERE pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track, pace, and pace index.
         *
         * @param cache     the data cache
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StandardMilestoneRec> queryByPaceTrackPaceIndex(final Cache cache, final String track,
                                                                    final Integer pace, final Integer paceIndex)
                throws SQLException {

            final String sql = SimpleBuilder.concat(
                    "SELECT * FROM std_milestone ",
                    "WHERE pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a single standard milestone pace track, pace, pace index, unit, objective, and milestone type.
         *
         * @param cache     the data cache
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @param unit      the unit
         * @param objective the objective
         * @param msType    the milestone type
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StandardMilestoneRec query(final Cache cache, final String track, final Integer pace,
                                          final Integer paceIndex, final Integer unit, final Integer objective,
                                          final String msType) throws SQLException {

            final String sql = SimpleBuilder.concat(
                    "SELECT * FROM std_milestone ",
                    "WHERE pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND ms_type=", sqlStringValue(msType));

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public StandardMilestoneRec fromResultSet(final ResultSet rs) throws SQLException {

            final StandardMilestoneRec result = new StandardMilestoneRec();

            result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
            result.pace = getIntegerField(rs, FLD_PACE);
            result.paceIndex = getIntegerField(rs, FLD_PACE_INDEX);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.msType = getStringField(rs, FLD_MS_TYPE);
            result.msDate = getDateField(rs, FLD_MS_DATE);

            return result;
        }
    }

    /**
     * A subclass of {@code StandardMilestoneLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends StandardMilestoneLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** A field name. */
        private static final String FLD_PACE_TRACK = "pace_track";

        /** A field name. */
        private static final String FLD_PACE = "pace";

        /** A field name. */
        private static final String FLD_PACE_INDEX = "pace_index";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_OBJECTIVE = "objective";

        /** A field name. */
        private static final String FLD_MS_TYPE = "ms_type";

        /** A field name. */
        private static final String FLD_MS_DATE = "ms_date";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StandardMilestoneRec record)
                throws SQLException {

            if (record.paceTrack == null || record.pace == null || record.paceIndex == null
                || record.unit == null || record.objective == null || record.msType == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    schemaPrefix, ".std_milestone (pace_track,pace,pace_index,",
                    "unit,objective,ms_type,ms_date) VALUES (",
                    sqlStringValue(record.paceTrack), ",",
                    sqlIntegerValue(record.pace), ",",
                    sqlIntegerValue(record.paceIndex), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.msType), ",",
                    sqlDateValue(record.msDate), ")");

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean delete(final Cache cache, final StandardMilestoneRec record)
                throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("DELETE FROM ",
                    schemaPrefix, ".std_milestone ",
                    "WHERE pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StandardMilestoneRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".std_milestone");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates the milestone date on a standard milestone record.
         *
         * @param cache   the data cache
         * @param record  the record to be updated
         * @param newDate the new date
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateDate(final Cache cache, final StandardMilestoneRec record,
                                  final LocalDate newDate) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ",
                    schemaPrefix, ".std_milestone ",
                    "SET ms_date=", sqlDateValue(newDate),
                    " WHERE pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track and pace. This will include milestones for all
         * pace indexes in that pace.
         *
         * @param cache the data cache
         * @param track the pace track
         * @param pace  the pace
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StandardMilestoneRec> queryByPaceTrackPace(final Cache cache, final String track,
                                                               final Integer pace) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".std_milestone ",
                    "WHERE pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track, pace, and pace index.
         *
         * @param cache     the data cache
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StandardMilestoneRec> queryByPaceTrackPaceIndex(final Cache cache, final String track,
                                                                    final Integer pace, final Integer paceIndex)
                throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".std_milestone ",
                    "WHERE pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a single standard milestone pace track, pace, pace index, unit, objective, and milestone type.
         *
         * @param cache     the data cache
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @param unit      the unit
         * @param objective the objective
         * @param msType    the milestone type
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StandardMilestoneRec query(final Cache cache, final String track, final Integer pace,
                                          final Integer paceIndex, final Integer unit, final Integer objective,
                                          final String msType) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".std_milestone ",
                    "WHERE pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND ms_type=", sqlStringValue(msType));

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public StandardMilestoneRec fromResultSet(final ResultSet rs) throws SQLException {

            final StandardMilestoneRec result = new StandardMilestoneRec();

            result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
            result.pace = getIntegerField(rs, FLD_PACE);
            result.paceIndex = getIntegerField(rs, FLD_PACE_INDEX);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.msType = getStringField(rs, FLD_MS_TYPE);
            result.msDate = getDateField(rs, FLD_MS_DATE);

            return result;
        }
    }
}
