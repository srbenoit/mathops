package dev.mathops.dbjobs.report.ods;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A class that queries and prints the ODS data dictionary.
 */
final class OdsDataDictionary {

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The ODS database context. */
    private final DbContext odsCtx;

    /**
     * Constructs a new {@code OdsDataDictionary}.
     */
    private OdsDataDictionary() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.odsCtx = this.dbProfile.getDbContext(ESchemaUse.ODS);
    }

    /**
     * Executes the job.
     *
     * @return a report
     */
    private String execute() {

        final Collection<String> report = new ArrayList<>(100);

        if (this.dbProfile == null) {
            report.add("Unable to create production context.");
        } else if (this.odsCtx == null) {
            report.add("Unable to create ODS database context.");
        } else {
            try {
                execute(report);
            } catch (final SQLException ex) {
                Log.warning(ex);
                report.add("Unable to obtain connection to ODS database");
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        htm.addln("<pre>");
        for (final String rep : report) {
            htm.addln(rep);
        }
        htm.addln("</pre>");

        return htm.toString();
    }

    /**
     * Executes the query against the ODS and loads data into the primary schema.
     *
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error querying the database
     */
    private void execute(final Collection<? super String> report) throws SQLException {

        final DbConnection odsConn = this.odsCtx.checkOutConnection();

        try {
            queryOdsFields(odsConn, report);
        } catch (final SQLException ex) {
            Log.warning(ex);
            report.add("Unable to perform query: " + ex.getMessage());
        } finally {
            this.odsCtx.checkInConnection(odsConn);
        }
    }

    /**
     * Queries the data dictionary tables and prints the contents.
     *
     * @param conn   the database connection
     * @param report a list to which to add report lines
     * @throws SQLException if there is an error performing the query
     */
    private static void queryOdsFields(final DbConnection conn, final Collection<? super String> report)
            throws SQLException {

        String curTable = CoreConstants.EMPTY;
        final Collection<String> tableNames = new ArrayList<>(100);

        try (final Statement stmt = conn.createStatement()) {
            final String sql2 = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, COLUMN_DESCRIPTION "
                    + "FROM CSU_BI_META.CSU_REPORTING_DATA_FIELDS "
                    + "WHERE OWNER='CSUBAN' ORDER BY TABLE_NAME, COLUMN_SEQ_NUM";

            try (final ResultSet rs = stmt.executeQuery(sql2)) {
                while (rs.next()) {

                    final String tableName = rs.getString("TABLE_NAME");
                    if (!tableName.equals(curTable)) {
                        report.add("TABLE: " + tableName);
                        curTable = tableName;
                        tableNames.add(tableName);
                    }

                    final String colName = rs.getString("COLUMN_NAME");
                    final String type = rs.getString("DATA_TYPE");
                    final String length = rs.getString("DATA_LENGTH");
                    final String descr = rs.getString("COLUMN_DESCRIPTION");

                    if (length == null || length.isBlank()) {
                        report.add(SimpleBuilder.concat("    ", colName, " [", type, "]"));
                    } else {
                        report.add(SimpleBuilder.concat("    ", colName, " [", type, "(", length, ")]"));
                    }

                    if (descr != null && !descr.isBlank()) {
                        report.add(SimpleBuilder.concat("      ", descr));
                    }
                }
            }

            for (final String tableName : tableNames) {

                final String sql3 = "SELECT COUNT(*) FROM CSUBAN." + tableName;

                try (final ResultSet rs = stmt.executeQuery(sql3)) {
                    if (rs.next()) {
                        final int count = rs.getInt(1);
                        report.add("TABLE: " + tableName + " has " + count + " records");
                    }
                } catch (final SQLException ex) {
                    Log.warning(sql3, ex);
                }
            }
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final OdsDataDictionary job = new OdsDataDictionary();

        Log.fine(job.execute());
    }
}
