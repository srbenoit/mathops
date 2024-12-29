package dev.mathops.dbjobs.report.datadict;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DatabaseMetaData;
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

        report.add("Querying ODS database metadata...");
        report.add(CoreConstants.EMPTY);

        final DatabaseMetaData metadata = conn.getConnection().getMetaData();

        try (final ResultSet tables = metadata.getTables(null, null, null, null)) {
            while (tables.next()) {
                final String schema = tables.getString("TABLE_SCHEM");
                final String name = tables.getString("TABLE_NAME");

                if (name.indexOf('/') > 0) {
                    Log.warning("Skipping table: ", name);
                    continue;
                }

                report.add(SimpleBuilder.concat("SCHEMA.TABLE: '", schema, ".", name, "'"));

                try (final ResultSet columns = metadata.getColumns(null, schema, name, null)) {
                    while (columns.next()) {
                        final String colname = columns.getString("COLUMN_NAME");
                        final String coltype = columns.getString("TYPE_NAME");
                        final String size = columns.getString("COLUMN_SIZE");
                        if (size == null) {
                            report.add(SimpleBuilder.concat("    ", colname, " (", coltype, ")"));
                        } else {
                            report.add(SimpleBuilder.concat("    ", colname, " (", coltype, "[", size, "])"));
                        }
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
                final String dataString = fileData;
                fw.write(dataString);
            } catch (final IOException ex) {
                Log.warning("Failed to write data dictionary JSON file.", ex);
            }
        }
    }
}
