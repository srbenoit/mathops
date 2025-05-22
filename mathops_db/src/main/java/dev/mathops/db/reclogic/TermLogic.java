package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A utility class to work with term records.
 */
public abstract class TermLogic implements IRecLogic<TermRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private TermLogic() {

        super();
    }

    /**
     * Gets the instance of {@code TermLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code TermLogic} object (null if none found)
     */
    public static TermLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        TermLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
        }

        return result;
    }

    /**
     * Queries for the term with a specified active index, where 0 represents the active term, +1 the next term, +2 the
     * term after that, and so forth, and -1 represents the prior term, -2 the term before that, and so on.
     *
     * @param cache     the data cache
     * @param termIndex the term index
     * @return the matching record; null if none found
     * @throws SQLException if there is an error performing the query
     */
    public abstract TermRec queryByIndex(Cache cache, int termIndex) throws SQLException;

    /**
     * Queries for all future terms. Results are in term order.
     *
     * @param cache the data cache
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<TermRec> getFutureTerms(Cache cache) throws SQLException;

    /**
     * Queries for the active term.
     *
     * @param cache the data cache
     * @return the active term; null if none found
     * @throws SQLException if there is an error performing the query
     */
    public abstract TermRec queryActive(Cache cache) throws SQLException;

    /**
     * Queries for the next term (that with a term index of 1).
     *
     * @param cache the data cache
     * @return the next term; null if none found
     * @throws SQLException if there is an error performing the query
     */
    public abstract TermRec queryNext(Cache cache) throws SQLException;

    /**
     * Queries for the prior term (that with a term index of -1).
     *
     * @param cache the data cache
     * @return the prior term; null if none found
     * @throws SQLException if there is an error performing the query
     */
    public abstract TermRec queryPrior(Cache cache) throws SQLException;

    /**
     * Queries for a term by its key.
     *
     * @param cache   the data cache
     * @param termKey the key of the term to query
     * @return the term; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract TermRec query(Cache cache, TermKey termKey) throws SQLException;

    /**
     * A subclass of {@code TermLogic} designed for the Informix schema.
     */
    public static final class Informix extends TermLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** A field name. */
        private static final String FLD_TERM = "term";

        /** A field name. */
        private static final String FLD_TERM_YR = "term_yr";

        /** A field name. */
        private static final String FLD_START_DT = "start_dt";

        /** A field name. */
        private static final String FLD_END_DT = "end_dt";

        /** A field name. */
        private static final String FLD_ACADEMIC_YR = "academic_yr";

        /** A field name. */
        private static final String FLD_ACTIVE_INDEX = "active_index";

        /** A field name. */
        private static final String FLD_I_DEADLINE_DT = "i_deadline_dt";

        /** A field name. */
        private static final String FLD_W_DROP_DT = "w_drop_dt";

        /** A field name. */
        private static final String FLD_DROP_DT = "drop_dt";

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
        public boolean insert(final Cache cache, final TermRec record) throws SQLException {

            if (record.term == null || record.startDate == null || record.endDate == null
                    || record.academicYear == null || record.activeIndex == null) {
                throw new SQLException("Null value in required field.");
            }

            final String active;
            final int index = record.activeIndex.intValue();
            if (index == 0) {
                active = "Y";
            } else if (index == 1) {
                active = "X";
            } else if (index == 2) {
                active = "2";
            } else if (index == 3) {
                active = "3";
            } else if (index == -1) {
                active = "P";
            } else {
                active = "N";
            }

            final String sql = SimpleBuilder.concat("INSERT INTO term (term,term_yr,start_dt,end_dt,academic_yr,",
                    "ctrl_enforce,active,active_index,drop_dt,w_drop_dt,i_deadline_dt) VALUES (",
                    sqlStringValue(record.term.termCode), ",",
                    sqlIntegerValue(record.term.shortYear), ",",
                    sqlDateValue(record.startDate), ",",
                    sqlDateValue(record.endDate), ",",
                    sqlStringValue(record.academicYear), ",'N',",
                    sqlStringValue(active), ",",
                    sqlIntegerValue(record.activeIndex), ",",
                    sqlDateValue(record.dropDeadline), ",",
                    sqlDateValue(record.withdrawDeadline), ",",
                    sqlDateValue(record.incDeadline), ")");

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
        public boolean delete(final Cache cache, final TermRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM term WHERE term=",
                    sqlStringValue(record.term.termCode), " AND term_yr=",
                    sqlIntegerValue(record.term.shortYear));

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
        public List<TermRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM term");
        }

        /**
         * Queries for the term with a specified active index, where 0 represents the active term, +1 the next term, +2
         * the term after that, and so forth, and -1 represents the prior term, -2 the term before that, and so on.
         *
         * @param cache     the data cache
         * @param termIndex the term index
         * @return the matching record; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryByIndex(final Cache cache, final int termIndex) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM term WHERE active_index=",
                    sqlIntegerValue(termIndex));

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all future terms. Results are in term order.
         *
         * @param cache the data cache
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<TermRec> getFutureTerms(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM term WHERE active_index>0");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for the active term.
         *
         * @param cache the data cache
         * @return the active term; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryActive(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM term WHERE active_index=0");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for the next term (that with a term index of 1).
         *
         * @param cache the data cache
         * @return the next term; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryNext(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM term WHERE active_index=1");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for the prior term (that with a term index of -1).
         *
         * @param cache the data cache
         * @return the prior term; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryPrior(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM term WHERE active_index=-1");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a term by its key.
         *
         * @param cache   the data cache
         * @param termKey the key of the term to query
         * @return the term; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec query(final Cache cache, final TermKey termKey) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM term WHERE term=", sqlStringValue(termKey.termCode),
                    " AND term_yr=", sqlIntegerValue(termKey.shortYear));

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
        public TermRec fromResultSet(final ResultSet rs) throws SQLException {

            final String term = getStringField(rs, FLD_TERM);
            final Integer termYr = getIntegerField(rs, FLD_TERM_YR);

            if (term == null || termYr == null) {
                throw new SQLException("Term record found with null term or term year");
            }

            final ETermName termName = ETermName.forName(term);
            if (termName == null) {
                throw new SQLException("Term record found with invalid term: " + term);
            }

            final TermKey theTermKey = new TermKey(termName, 2000 + termYr.intValue());
            final LocalDate theStartDate = getDateField(rs, FLD_START_DT);
            final LocalDate theEndDate = getDateField(rs, FLD_END_DT);
            final String theAcademicYear = getStringField(rs, FLD_ACADEMIC_YR);
            final Integer theActiveIndex = getIntegerField(rs, FLD_ACTIVE_INDEX);
            final LocalDate theDropDeadline = getDateField(rs, FLD_DROP_DT);
            final LocalDate theWithdrawDeadline = getDateField(rs, FLD_W_DROP_DT);
            final LocalDate theIncDeadline = getDateField(rs, FLD_I_DEADLINE_DT);

            return new TermRec(theTermKey, theStartDate, theEndDate, theAcademicYear, theActiveIndex, theDropDeadline,
                    theWithdrawDeadline, theIncDeadline);
        }
    }

    /**
     * A subclass of {@code TermLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends TermLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** A field name. */
        private static final String FLD_TERM = "term";

        /** A field name. */
        private static final String FLD_START_DATE = "start_date";

        /** A field name. */
        private static final String FLD_END_DATE = "end_date";

        /** A field name. */
        private static final String FLD_ACADEMIC_YEAR = "academic_year";

        /** A field name. */
        private static final String FLD_ACTIVE_INDEX = "active_index";

        /** A field name. */
        private static final String FLD_DROP_DEADLINE = "drop_deadline";

        /** A field name. */
        private static final String FLD_WITHDRAW_DEADLINE = "withdraw_deadline";

        /** A field name. */
        private static final String FLD_INC_DEADLINE = "inc_deadline";

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
        public boolean insert(final Cache cache, final TermRec record) throws SQLException {

            if (record.term == null || record.startDate == null || record.endDate == null
                    || record.academicYear == null || record.activeIndex == null) {
                throw new SQLException("Null value in required field.");
            }

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".term ",
                    "(term,start_date,end_date,academic_year,active_index,",
                    "drop_deadline,withdraw_deadline,inc_deadline) VALUES (",
                    sqlIntegerValue(record.term.toNumeric()), ",",
                    sqlDateValue(record.startDate), ",",
                    sqlDateValue(record.endDate), ",",
                    sqlStringValue(record.academicYear), ",",
                    sqlIntegerValue(record.activeIndex), ",",
                    sqlDateValue(record.dropDeadline), ",",
                    sqlDateValue(record.withdrawDeadline), ",",
                    sqlDateValue(record.incDeadline), ")");

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
        public boolean delete(final Cache cache, final TermRec record) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".term WHERE term=",
                    sqlIntegerValue(record.term.toNumeric()));

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
        public List<TermRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".term");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for the term with a specified active index, where 0 represents the active term, +1 the next term, +2
         * the term after that, and so forth, and -1 represents the prior term, -2 the term before that, and so on.
         *
         * @param cache     the data cache
         * @param termIndex the term index
         * @return the matching record; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryByIndex(final Cache cache, final int termIndex) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".term WHERE active_index=", sqlIntegerValue(termIndex));

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all future terms. Results are in term order.
         *
         * @param cache the data cache
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<TermRec> getFutureTerms(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".term WHERE active_index>0");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for the active term.
         *
         * @param cache the data cache
         * @return the active term; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryActive(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".term WHERE active_index=0");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for the next term (that with a term index of 1).
         *
         * @param cache the data cache
         * @return the next term; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryNext(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".term WHERE active_index=1");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for the prior term (that with a term index of -1).
         *
         * @param cache the data cache
         * @return the prior term; null if none found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec queryPrior(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".term WHERE active_index=-1");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a term by its key.
         *
         * @param cache   the data cache
         * @param termKey the key of the term to query
         * @return the term; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public TermRec query(final Cache cache, final TermKey termKey) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".term WHERE term=",
                    sqlIntegerValue(termKey.toNumeric()));

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
        public TermRec fromResultSet(final ResultSet rs) throws SQLException {

            final TermKey theTermKey = new TermKey(getIntegerField(rs, FLD_TERM));
            final LocalDate theStartDate = getDateField(rs, FLD_START_DATE);
            final LocalDate theEndDate = getDateField(rs, FLD_END_DATE);
            final String theAcademicYear = getStringField(rs, FLD_ACADEMIC_YEAR);
            final Integer theActiveIndex = getIntegerField(rs, FLD_ACTIVE_INDEX);
            final LocalDate theDropDeadline = getDateField(rs, FLD_DROP_DEADLINE);
            final LocalDate theWithdrawDeadline = getDateField(rs, FLD_WITHDRAW_DEADLINE);
            final LocalDate theIcnDeadline = getDateField(rs, FLD_INC_DEADLINE);

            return new TermRec(theTermKey, theStartDate, theEndDate, theAcademicYear, theActiveIndex, theDropDeadline,
                    theWithdrawDeadline, theIcnDeadline);
        }
    }
}
