package dev.mathops.dbjobs.batch;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple class to fetch all program codes to which a student can be admitted from Banner.
 */
public final class DownloadBannerProgramCodes {

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code DownloadBannerProgramCodes}.
     */
    public DownloadBannerProgramCodes() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                execute(cache, report);
            } catch (final SQLException ex) {
                report.add("EXCEPTION: " + ex.getMessage());
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
     * @throws SQLException if there is an error accessing the database
     */
    private void execute(final Cache cache, final Collection<? super String> report) throws SQLException {

        final String sql = SimpleBuilder.concat( //
                "SELECT DISTINCT n.sgbstdn_program_1,n.sgbstdn_coll_code_1,n.sgbstdn_majr_code_1,n.sgbstdn_degc_code_1",
                "  FROM sgbstdn n",
                " WHERE n.sgbstdn_program_1 IS NOT NULL",
                "   AND n.sgbstdn_stst_code = 'AS'",
                "   AND n.sgbstdn_levl_code = 'UG'",
                "   AND n.sgbstdn_styp_code in ('N','T')",
                " ORDER BY n.sgbstdn_program_1");

        final Login login = this.profile.getLogin(ESchema.LIVE);

        final DbConnection conn = login.checkOutConnection();
        try {
            final Connection jdbc = conn.getConnection();

            try (final Statement stmt = jdbc.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    final String program = rs.getString(1);
                    final String college = rs.getString(2);
                    final String major = rs.getString(3);
                    final String degree = rs.getString(4);

                    Log.info(program, "/", college, "/", major, "/", degree);
                }
            }
        } finally {
            login.checkInConnection(conn);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final DownloadBannerProgramCodes job = new DownloadBannerProgramCodes();

        Log.fine(job.execute());
    }
}
