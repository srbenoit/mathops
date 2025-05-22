package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.rec.TermWeekRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A utility class to work with term week records.
 */
public abstract class TermWeekLogic implements IRecLogic<TermWeekRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private TermWeekLogic() {

        super();
    }

    /**
     * Gets the instance of {@code TermLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code TermLogic} object (null if none found)
     */
    public static TermWeekLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        TermWeekLogic result = null;
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
    public static final class Informix extends TermWeekLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** The table name. */
        public static final String TABLE_NAME = "semester_calendar";

        /** A field name. */
        private static final String FLD_WEEK_NBR = "week_nbr";

        /** A field name. */
        private static final String FLD_START_DT = "start_dt";

        /** A field name. */
        private static final String FLD_END_DT = "end_dt";

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
        public boolean insert(final Cache cache, final TermWeekRec record) throws SQLException {

            if (record.weekNbr == null || record.startDate == null || record.endDate == null) {
                throw new SQLException("Null value in required field.");
            }

            final SystemData systemData = cache.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();
            if (activeTerm == null) {
                throw new SQLException("No active term found.");
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO semester_calendar (term,term_yr,week_nbr,start_dt,end_dt) VALUES (",
                    sqlStringValue(activeTerm.term.termCode), ",",
                    sqlIntegerValue(activeTerm.term.shortYear), ",",
                    sqlIntegerValue(record.weekNbr), ",",
                    sqlDateValue(record.startDate), ",",
                    sqlDateValue(record.endDate), ")");

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
        public boolean delete(final Cache cache, final TermWeekRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM semester_calendar WHERE week_nbr=",
                    sqlIntegerValue(record.weekNbr));

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
        public List<TermWeekRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM semester_calendar");
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public TermWeekRec fromResultSet(final ResultSet rs) throws SQLException {

            final Integer weekNbr = getIntegerField(rs, FLD_WEEK_NBR);
            final LocalDate startDt = getDateField(rs, FLD_START_DT);
            final LocalDate endDt = getDateField(rs, FLD_END_DT);

            if (weekNbr == null || startDt == null || startDt == endDt) {
                throw new SQLException("TermWeek record found with null week number, start date, or end date");
            }

            return new TermWeekRec(weekNbr, startDt, endDt);
        }
    }

    /**
     * A subclass of {@code TermWeekLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends TermWeekLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** The table name. */
        public static final String TABLE_NAME = "term_week";

        /** A field name. */
        private static final String FLD_WEEK_NBR = "week_nbr";

        /** A field name. */
        private static final String FLD_START_DT = "start_dt";

        /** A field name. */
        private static final String FLD_END_DT = "end_dt";

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
        public boolean insert(final Cache cache, final TermWeekRec record) throws SQLException {

            if (record.weekNbr == null || record.startDate == null || record.endDate == null) {
                throw new SQLException("Null value in required field.");
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO term_week (week_nbr,start_dt,end_dt) VALUES (",
                    sqlIntegerValue(record.weekNbr), ",",
                    sqlDateValue(record.startDate), ",",
                    sqlDateValue(record.endDate), ")");

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
        public boolean delete(final Cache cache, final TermWeekRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM term_week WHERE week_nbr=",
                    sqlIntegerValue(record.weekNbr));

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
        public List<TermWeekRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM term_week");
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public TermWeekRec fromResultSet(final ResultSet rs) throws SQLException {

            final Integer weekNbr = getIntegerField(rs, FLD_WEEK_NBR);
            final LocalDate startDt = getDateField(rs, FLD_START_DT);
            final LocalDate endDt = getDateField(rs, FLD_END_DT);

            if (weekNbr == null || startDt == null || startDt == endDt) {
                throw new SQLException("TermWeek record found with null week number, start date, or end date");
            }

            return new TermWeekRec(weekNbr, startDt, endDt);
        }
    }
}
