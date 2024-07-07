package dev.mathops.dbjobs.batch;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

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
    private final DbProfile dbProfile;

    /** The Primary database context. */
    private final DbContext primaryCtx;

    /** The live data database context. */
    private final DbContext liveCtx;

    /**
     * Constructs a new {@code DownloadBannerProgramCodes}.
     */
    public DownloadBannerProgramCodes() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        this.liveCtx = this.dbProfile.getDbContext(ESchemaUse.LIVE);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else if (this.primaryCtx == null) {
            Log.warning("Unable to create PRIMARY database context.");
        } else if (this.liveCtx == null) {
            Log.warning("Unable to create LIVE database context.");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);
                try {
                    execute(cache, report);
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
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

        final DbConnection conn = this.liveCtx.checkOutConnection();
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
            this.liveCtx.checkInConnection(conn);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final DownloadBannerProgramCodes job = new DownloadBannerProgramCodes();

        Log.fine(job.execute());
    }
}
