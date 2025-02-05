package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawNewstuLogic;
import dev.mathops.db.old.rawrecord.RawNewstu;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that performs an import of new student data from the ODS and stores the data in the 'newstu' table.
 */
public final class ImportOdsNewStus {

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code ImportOdsNewStus}.
     */
    public ImportOdsNewStus() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @return a report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            report.add("Unable to create production context.");
        } else {
            final Cache cache = new Cache(this.profile);
            execute(cache, report);
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
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     */
    private void execute(final Cache cache, final Collection<? super String> report) {

        final Login odsLogin = this.profile.getLogin(ESchema.ODS);

        final DbConnection odsConn = odsLogin.checkOutConnection();

        try {
            final Map<String, RawNewstu> newstus = queryOds(odsConn, report);

            report.add("Found " + newstus.size() + " new students.");
            processList(cache, newstus, report);

            report.add("Job completed");

        } catch (final SQLException ex) {
            Log.warning(ex);
            report.add("Unable to perform query: " + ex.getMessage());
        } finally {
            odsLogin.checkInConnection(odsConn);
        }
    }

    /**
     * Queries applicant records from the ODS given the name of the term info table.
     *
     * @param conn   the database connection
     * @param report a list to which to add report lines
     * @return a map from CSU ID to new student record
     * @throws SQLException if there is an error performing the query
     */
    private static Map<String, RawNewstu> queryOds(final DbConnection conn,
                                                   final Collection<? super String> report) throws SQLException {

        final Map<String, RawNewstu> result = new HashMap<>(5000);

        try (final Statement stmt = conn.createStatement()) {

            final String sql = "SELECT CSU_ID, TERM FROM CSUBAN.CSUS_TERM_INFO_FAL "
                               + "WHERE (STUDENT_LEVEL = 'UG') AND (STUDENT_TYPE = 'N')";

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {

                    final String csuId = rs.getString("CSU_ID");
                    final String term = rs.getString("TERM");

                    if (csuId == null) {
                        report.add("ODS record had null CSU ID");
                    } else if (term == null) {
                        report.add("ODS record had null term");
                    } else if (csuId.length() == 9) {
                        final RawNewstu newRec = new RawNewstu(csuId, "UG", "N", term);
                        result.putIfAbsent(csuId, newRec);
                    } else {
                        report.add("ODS record had bad student ID: '" + csuId + "'");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Processes a list of new student records.
     *
     * @param cache   the data cache
     * @param newStus the list of new student from the ODS
     * @param report  a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void processList(final Cache cache, final Map<String, RawNewstu> newStus,
                                    final Collection<? super String> report) throws SQLException {

        RawNewstuLogic.deleteAll(cache);

        int numSuccess = 0;
        int numFail = 0;

        for (final RawNewstu stu : newStus.values()) {
            if (RawNewstuLogic.insert(cache, stu)) {
                ++numSuccess;
            } else {
                ++numFail;
            }
        }

        report.add("  Records successfully loaded: " + numSuccess);
        report.add("  Records that failed to load: " + numFail);
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final ImportOdsNewStus job = new ImportOdsNewStus();

        Log.fine(job.execute());
    }
}
