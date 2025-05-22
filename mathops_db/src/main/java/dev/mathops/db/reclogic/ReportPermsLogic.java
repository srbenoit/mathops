package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.ReportPermsRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with report_perms records.
 */
public abstract class ReportPermsLogic implements IRecLogic<ReportPermsRec> {

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

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        ReportPermsLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
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
     * Updates the permission level in a record.
     *
     * @param cache  the data cache
     * @param record the record to update
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public abstract boolean updatePermLevel(Cache cache, ReportPermsRec record) throws SQLException;

    /**
     * A subclass of {@code MasteryExamLogic} designed for the Informix schema.
     */
    public static final class Informix extends ReportPermsLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

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

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates the permission level in a record.
         *
         * @param cache  the data cache
         * @param record the record to update
         * @return {@code true} if successful; {@code false} otherwise
         * @throws SQLException if there is an error accessing the database
         */
        public boolean updatePermLevel(final Cache cache, final ReportPermsRec record) throws SQLException {

            if (record.stuId == null || record.rptId == null || record.permLevel == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("UPDATE report_perms set perm_level=",
                    sqlIntegerValue(record.permLevel),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND rpt_id=", sqlStringValue(record.rptId));

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
        public boolean delete(final Cache cache, final ReportPermsRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM report_perms WHERE stu_id=",
                    sqlStringValue(record.stuId), " AND rpt_id=",
                    sqlStringValue(record.rptId));

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
        public List<ReportPermsRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM report_perms");
        }

        /**
         * Queries for all report permissions for a single student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByStuId(final Cache cache, final String stuId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM report_perms ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all report permissions for a single report.
         *
         * @param cache the data cache
         * @param rptId the report ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByRptId(final Cache cache, final String rptId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM report_perms ",
                    "WHERE rpt_id=", sqlStringValue(rptId));

            return doListQuery(cache, ESchema.LEGACY, sql);
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
        public ReportPermsRec query(final Cache cache, final String stuId, final String rptId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM report_perms ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND rpt_id=", sqlStringValue(rptId));

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
    public static final class Postgres extends ReportPermsLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

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

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    schemaPrefix, ".report_perms (stu_id,rpt_id,perm_level) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.rptId), ",",
                    sqlIntegerValue(record.permLevel), ")");

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates the permission level in a record.
         *
         * @param cache  the data cache
         * @param record the record to update
         * @return {@code true} if successful; {@code false} otherwise
         * @throws SQLException if there is an error accessing the database
         */
        public boolean updatePermLevel(final Cache cache, final ReportPermsRec record) throws SQLException {

            if (record.stuId == null || record.rptId == null || record.permLevel == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".report_perms set perm_level=",
                    sqlIntegerValue(record.permLevel),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND rpt_id=", sqlStringValue(record.rptId));

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
        public boolean delete(final Cache cache, final ReportPermsRec record) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".report_perms WHERE stu_id=",
                    sqlStringValue(record.stuId), " AND rpt_id=",
                    sqlStringValue(record.rptId));

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
        public List<ReportPermsRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".report_perms");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all report permissions for a single student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByStuId(final Cache cache, final String stuId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".report_perms WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all report permissions for a single report.
         *
         * @param cache the data cache
         * @param rptId the report ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        public List<ReportPermsRec> queryByRptId(final Cache cache, final String rptId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".report_perms WHERE rpt_id=", sqlStringValue(rptId));

            return doListQuery(cache, ESchema.LEGACY, sql);
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
        public ReportPermsRec query(final Cache cache, final String stuId, final String rptId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".report_perms WHERE stu_id=", sqlStringValue(stuId),
                    " AND rpt_id=", sqlStringValue(rptId));

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
        public ReportPermsRec fromResultSet(final ResultSet rs) throws SQLException {

            final ReportPermsRec result = new ReportPermsRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.rptId = getStringField(rs, FLD_RPT_ID);
            result.permLevel = getIntegerField(rs, FLD_PERM_LEVEL);

            return result;
        }
    }
}
