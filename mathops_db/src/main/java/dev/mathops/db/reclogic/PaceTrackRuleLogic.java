package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.rec.PaceTrackRuleRec;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with pace track rule records.
 */
public abstract class PaceTrackRuleLogic implements IRecLogic<PaceTrackRuleRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private PaceTrackRuleLogic() {

        super();
    }

    /**
     * Gets the instance of {@code TermLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code TermLogic} object (null if none found)
     */
    public static PaceTrackRuleLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        PaceTrackRuleLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
        }

        return result;
    }

    /**
     * A subclass of {@code TermWeekLogic} designed for the Informix schema.
     */
    public static final class Informix extends PaceTrackRuleLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** The table name. */
        public static final String TABLE_NAME = "pace_track_rule";

        /** A field name. */
        private static final String FLD_SUBTERM = "subterm";

        /** A field name. */
        private static final String FLD_PACE = "pace";

        /** A field name. */
        private static final String FLD_PACE_TRACK = "pace_track";

        /** A field name. */
        private static final String FLD_CRITERIA = "criteria";

        /**
         * Constructs a new {@code Informix}.
         */
        Informix() {

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
        public boolean insert(final Cache cache, final PaceTrackRuleRec record) throws SQLException {

            if (record.subterm == null || record.pace == null || record.paceTrack == null || record.criteria == null) {
                throw new SQLException("Null value in required field.");
            }

            final SystemData systemData = cache.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();
            if (activeTerm == null) {
                throw new SQLException("No active term found.");
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO pace_track_rule (term,term_yr,subterm,pace,pace_track,criteria) VALUES (",
                    sqlStringValue(activeTerm.term.termCode), ",",
                    sqlIntegerValue(activeTerm.term.shortYear), ",",
                    sqlStringValue(record.subterm), ",",
                    sqlIntegerValue(record.pace), ",",
                    sqlStringValue(record.paceTrack), ",",
                    sqlStringValue(record.criteria), ")");

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
        public boolean delete(final Cache cache, final PaceTrackRuleRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM pace_track_rule ",
                    "WHERE subterm=", sqlStringValue(record.subterm),
                    "  AND pace=", sqlIntegerValue(record.pace),
                    "  AND pace_track=", sqlStringValue(record.paceTrack));

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
        public List<PaceTrackRuleRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM pace_track_rule");
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public PaceTrackRuleRec fromResultSet(final ResultSet rs) throws SQLException {

            final String subterm = getStringField(rs, FLD_SUBTERM);
            final Integer pace = getIntegerField(rs, FLD_PACE);
            final String paceTrack = getStringField(rs, FLD_PACE_TRACK);
            final String criteria = getStringField(rs, FLD_CRITERIA);

            if (subterm == null || pace == null || paceTrack == null || criteria == null) {
                throw new SQLException("PaceTrackRule record found with null subterm, pace, pace track, or criteria");
            }

            return new PaceTrackRuleRec(subterm, pace, paceTrack, criteria);
        }
    }

    /**
     * A subclass of {@code TermWeekLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends PaceTrackRuleLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** The table name. */
        public static final String TABLE_NAME = "pace_track_rule";

        /** A field name. */
        private static final String FLD_SUBTERM = "subterm";

        /** A field name. */
        private static final String FLD_PACE = "pace";

        /** A field name. */
        private static final String FLD_PACE_TRACK = "pace_track";

        /** A field name. */
        private static final String FLD_CRITERIA = "criteria";

        /**
         * Constructs a new {@code Postgres}.
         */
        Postgres() {

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
        public boolean insert(final Cache cache, final PaceTrackRuleRec record) throws SQLException {

            if (record.subterm == null || record.pace == null || record.paceTrack == null || record.criteria == null) {
                throw new SQLException("Null value in required field.");
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO pace_track_rule (subterm,pace,pace_track,criteria) VALUES (",
                    sqlStringValue(record.subterm), ",",
                    sqlIntegerValue(record.pace), ",",
                    sqlStringValue(record.paceTrack), ",",
                    sqlStringValue(record.criteria), ")");

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
        public boolean delete(final Cache cache, final PaceTrackRuleRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM pace_track_rule ",
                    "WHERE subterm=", sqlStringValue(record.subterm),
                    "  AND pace=", sqlIntegerValue(record.pace),
                    "  AND pace_track=", sqlStringValue(record.paceTrack));

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
        public List<PaceTrackRuleRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM pace_track_rule");
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public PaceTrackRuleRec fromResultSet(final ResultSet rs) throws SQLException {

            final String subterm = getStringField(rs, FLD_SUBTERM);
            final Integer pace = getIntegerField(rs, FLD_PACE);
            final String paceTrack = getStringField(rs, FLD_PACE_TRACK);
            final String criteria = getStringField(rs, FLD_CRITERIA);

            if (subterm == null || pace == null || paceTrack == null || criteria == null) {
                throw new SQLException("PaceTrackRule record found with null subterm, pace, pace track, or criteria");
            }

            return new PaceTrackRuleRec(subterm, pace, paceTrack, criteria);
        }
    }
}
