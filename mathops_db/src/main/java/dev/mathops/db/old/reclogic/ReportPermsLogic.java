package dev.mathops.db.old.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.rec.ReportPermsRec;
import dev.mathops.db.old.reclogic.iface.IInformixRecLogic;
import dev.mathops.db.old.reclogic.query.IntegerCriteria;
import dev.mathops.db.old.reclogic.query.StringCriteria;
import dev.mathops.db.old.reclogic.iface.IPostgresRecLogic;
import dev.mathops.db.old.reclogic.iface.IRecLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with report_perms records.
 */
public abstract class ReportPermsLogic implements IRecLogic<ReportPermsRec> {

    /** A single instance. */
    public static final Informix INFORMIX = new Informix();

    /** A single instance. */
    public static final Postgres POSTGRES = new Postgres();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ReportPermsLogic() {

        super();
    }

    /**
     * Gets the instance of {@code MasteryExamLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code MasteryExamLogic} object (null if none found)
     */
    public static ReportPermsLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache);

        ReportPermsLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = INFORMIX;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = POSTGRES;
        }

        return result;
    }

    /**
     * Queries for all report permissions for a single student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<ReportPermsRec> queryByStuId(Cache cache, String stuId) throws SQLException;

    /**
     * Queries for all report permissions for a single report.
     *
     * @param cache the data cache
     * @param rptId the report ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<ReportPermsRec> queryByRptId(Cache cache, String rptId) throws SQLException;

    /**
     * Queries for a single report permission record.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param rptId the report ID
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract ReportPermsRec query(Cache cache, String stuId, String rptId) throws SQLException;

    /**
     * Queries for all records matching given criteria.
     *
     * @param cache         the data cache
     * @param queryCriteria the general query criteria
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<ReportPermsRec> generalQuery(Cache cache, Criteria queryCriteria) throws SQLException;

    /**
     * Updates the permission level in a record.
     *
     * @param cache  the data cache
     * @param record the record to update
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public abstract boolean updatePermLevel(Cache cache, ReportPermsRec record) throws SQLException;

    /**
     * An "assignment" criteria record used to perform arbitrary queries.
     */
    public static final class Criteria {

        /** The criteria for the 'stu_id' field. */
        StringCriteria stuId;

        /** The criteria for the 'rpt_id' field. */
        StringCriteria rptId;

        /** The criteria for the 'perm_level' field. */
        IntegerCriteria permLevel;

        /**
         * Constructs a new {@code Criteria}.
         */
        public Criteria() {

            // No action
        }
    }

    /**
     * A subclass of {@code MasteryExamLogic} designed for the Informix schema.
     */
    public static final class Informix extends ReportPermsLogic
            implements IInformixRecLogic<ReportPermsRec> {

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_RPT_ID = "rpt_id";

        /** A field name. */
        private static final String FLD_PERM_LEVEL = "perm_level";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final ReportPermsRec record) throws SQLException {

            if (record.stuId == null || record.rptId == null || record.permLevel == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO report_perms (stu_id,rpt_id,perm_level) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.rptId), ",",
                    sqlIntegerValue(record.permLevel), ")");

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the permission level in a record.
         *
         * @param cache  the data cache
         * @param record the record to update
         * @return {@code true} if successful; {@code false} otherwise
         * @throws SQLException if there is an error accessing the database
         */
        public boolean updatePermLevel(Cache cache, ReportPermsRec record) throws SQLException {

            if (record.stuId == null || record.rptId == null || record.permLevel == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("UPDATE report_perms set perm_level=",
                    sqlIntegerValue(record.permLevel),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND rpt_id=", sqlStringValue(record.rptId));

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
        public boolean delete(final Cache cache, final ReportPermsRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM report_perms WHERE stu_id=",
                    sqlStringValue(record.stuId), " AND rpt_id=",
                    sqlStringValue(record.rptId));

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
        public List<ReportPermsRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, "SELECT * FROM report_perms");
        }

        /**
         * Queries for all report permissions for a single student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByStuId(Cache cache, String stuId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM report_perms ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all report permissions for a single report.
         *
         * @param cache the data cache
         * @param rptId the report ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByRptId(Cache cache, String rptId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM report_perms ",
                    "WHERE rpt_id=", sqlStringValue(rptId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for a single report permission record.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @param rptId the report ID
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        public ReportPermsRec query(Cache cache, String stuId, String rptId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM report_perms ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND rpt_id=", sqlStringValue(rptId));

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for all records matching given criteria.
         *
         * @param cache         the data cache
         * @param queryCriteria the general query criteria
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<ReportPermsRec> generalQuery(final Cache cache, final Criteria queryCriteria) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM report_perms");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "rpt_id", queryCriteria.rptId);
            integerWhere(sql, w, "perm_level", queryCriteria.permLevel);

            return doListQuery(cache, sql.toString());
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public ReportPermsRec fromResultSet(final ResultSet rs) throws SQLException {

            final ReportPermsRec result = new ReportPermsRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.rptId = getStringField(rs, FLD_RPT_ID);
            result.permLevel = getIntegerField(rs, FLD_PERM_LEVEL);

            return result;
        }
    }

    /**
     * A subclass of {@code MasteryExamLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends ReportPermsLogic
            implements IPostgresRecLogic<ReportPermsRec> {

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_RPT_ID = "rpt_id";

        /** A field name. */
        private static final String FLD_PERM_LEVEL = "perm_level";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final ReportPermsRec record) throws SQLException {

            if (record.stuId == null || record.rptId == null || record.permLevel == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    cache.mainSchemaName, ".report_perms (stu_id,rpt_id,perm_level) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.rptId), ",",
                    sqlIntegerValue(record.permLevel), ")");

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the permission level in a record.
         *
         * @param cache  the data cache
         * @param record the record to update
         * @return {@code true} if successful; {@code false} otherwise
         * @throws SQLException if there is an error accessing the database
         */
        public boolean updatePermLevel(Cache cache, ReportPermsRec record) throws SQLException {

            if (record.stuId == null || record.rptId == null || record.permLevel == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("UPDATE ", cache.mainSchemaName, ".report_perms set perm_level=",
                    sqlIntegerValue(record.permLevel),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND rpt_id=", sqlStringValue(record.rptId));

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
        public boolean delete(final Cache cache, final ReportPermsRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM ", cache.mainSchemaName, ".report_perms WHERE stu_id=",
                    sqlStringValue(record.stuId), " AND rpt_id=",
                    sqlStringValue(record.rptId));

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
        public List<ReportPermsRec> queryAll(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".report_perms");

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all report permissions for a single student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByStuId(Cache cache, String stuId) throws SQLException {

                final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.mainSchemaName,
                        ".report_perms WHERE stu_id=", sqlStringValue(stuId));

                return doListQuery(cache, sql);
        }

        /**
         * Queries for all report permissions for a single report.
         *
         * @param cache the data cache
         * @param rptId the report ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByRptId(Cache cache, String rptId) throws SQLException {

                final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.mainSchemaName,
                        ".report_perms WHERE rpt_id=", sqlStringValue(rptId));

                return doListQuery(cache, sql);
        }

        /**
         * Queries for a single report permission record.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @param rptId the report ID
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        public ReportPermsRec query(Cache cache, String stuId, String rptId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.mainSchemaName,
                    ".report_perms WHERE stu_id=", sqlStringValue(stuId),
                    " AND rpt_id=", sqlStringValue(rptId));

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for all records matching given criteria.
         *
         * @param cache         the data cache
         * @param queryCriteria the general query criteria
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<ReportPermsRec> generalQuery(final Cache cache, final Criteria queryCriteria) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM  ", cache.mainSchemaName, ".report_perms");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "rpt_id", queryCriteria.rptId);
            integerWhere(sql, w, "perm_level", queryCriteria.permLevel);

            return doListQuery(cache, sql.toString());
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public ReportPermsRec fromResultSet(final ResultSet rs) throws SQLException {

            final ReportPermsRec result = new ReportPermsRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.rptId = getStringField(rs, FLD_RPT_ID);
            result.permLevel = getIntegerField(rs, FLD_PERM_LEVEL);

            return result;
        }
    }
}
