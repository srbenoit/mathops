package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawNewstuLogic;
import dev.mathops.db.old.rawrecord.RawNewstu;

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
    private final DbProfile dbProfile;

    /** The Primary database context. */
    private final DbContext primaryCtx;

    /** The ODS database context. */
    private final DbContext odsCtx;

    /**
     * Constructs a new {@code ImportOdsNewStus}.
     */
    public ImportOdsNewStus() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        this.odsCtx = this.dbProfile.getDbContext(ESchemaUse.ODS);
    }

    /**
     * Executes the job.
     *
     * @return a report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.dbProfile == null) {
            report.add("Unable to create production context.");
        } else if (this.primaryCtx == null) {
            report.add("Unable to create primary database context.");
        } else if (this.odsCtx == null) {
            report.add("Unable to create ODS database context.");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    execute(cache, report);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    report.add("Unable to perform query");
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
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
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error querying the database
     */
    private void execute(final Cache cache, final Collection<? super String> report) throws SQLException {

        final DbConnection odsConn = this.odsCtx.checkOutConnection();

        try {
            final Map<String, RawNewstu> newstus = queryOds(odsConn, report);

            report.add("Found " + newstus.size() + " new students.");
            processList(cache, newstus, report);

            report.add("Job completed");

        } catch (final SQLException ex) {
            Log.warning(ex);
            report.add("Unable to perform query: " + ex.getMessage());
        } finally {
            this.odsCtx.checkInConnection(odsConn);
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
            if (RawNewstuLogic.INSTANCE.insert(cache, stu)) {
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

        final ImportOdsNewStus job = new ImportOdsNewStus();

        Log.fine(job.execute());
    }
}
