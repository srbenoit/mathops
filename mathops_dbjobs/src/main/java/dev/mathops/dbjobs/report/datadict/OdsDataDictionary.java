package dev.mathops.dbjobs.report.datadict;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            execute(report);
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        for (final String rep : report) {
            htm.addln(rep);
        }

        return htm.toString();
    }

    /**
     * Executes the query against the ODS and loads data into the primary schema.
     *
     * @param report a list of strings to which to add report output lines
     */
    private void execute(final Collection<? super String> report) {

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
//        final Collection<String> tableNames = new ArrayList<>(100);

        try (final Statement stmt = conn.createStatement()) {
            final String sql2 = "SELECT OWNER, TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, COLUMN_DESCRIPTION "
                    + "FROM CSU_BI_META.CSU_REPORTING_DATA_FIELDS "
                    + "ORDER BY OWNER, TABLE_NAME, COLUMN_SEQ_NUM";

            try (final ResultSet rs = stmt.executeQuery(sql2)) {
                while (rs.next()) {
                    final String tableName = rs.getString("TABLE_NAME");
                    if (!tableName.equals(curTable)) {
                        final String owner = rs.getString("OWNER");
                        report.add(SimpleBuilder.concat("TABLE: ", owner, ".", tableName));
                        curTable = tableName;
//                        tableNames.add(tableName);
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
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final OdsDataDictionary job = new OdsDataDictionary();

        final File dir = new File("C:\\opt\\zircon\\data");

        if (dir.exists() || dir.mkdirs()) {
            final File dataDictFile = new File(dir, "ods_data_dict.txt");
            final String fileData = job.execute();

            try (final FileWriter fw = new FileWriter(dataDictFile)) {
                final String dataString = fileData.toString();
                fw.write(dataString);
            } catch (final IOException ex) {
                Log.warning("Failed to write data dictionary JSON file.", ex);
            }
        }
    }
}
