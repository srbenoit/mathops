package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawDontSubmitLogic;
import dev.mathops.db.old.rawlogic.RawParametersLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawDontSubmit;
import dev.mathops.db.old.rawrecord.RawParameters;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility to generate the file that gets uploaded to Banner to submit dates of last attendance for grades of U or F.
 *
 * <p>
 * NOTE: according to Student Financial Services (SFS), the User's Exam can be counted as an academic event, but ONLY
 * for the first course in which the student is enrolled.  Iif the student completed another course, it CANNOT be
 * counted for the F/U course.
 */
public class CreateLastAttendanceFile implements Runnable {

    /** The data cache. */
    private final Cache cache;

    /** The report path. */
    private final File reportPath;

    /**
     * Constructs a new {@code CreateLastAttendanceFile}.
     *
     * @param theCache      the data cache
     * @param theReportPath the report path
     */
    private CreateLastAttendanceFile(final Cache theCache, final File theReportPath) {

        this.cache = theCache;
        this.reportPath = theReportPath;
    }

    /**
     * Runs the process.
     */
    public void run() {

        final SystemData systemData = this.cache.getSystemData();

        try {
            final TermRec active = systemData.getActiveTerm();

            if (active == null) {
                Log.warning("ERROR: Unable to query the active term");
            } else {
                final RawParameters parameters = getParameters();
                if (parameters != null) {

                    // TODO:
                }
            }
        } catch (final SQLException ex) {
            Log.warning("ERROR: Unable to query the active term", ex);
        }
    }

    /**
     * Retrieves the "parameters" record with information for grade submission.
     *
     * @return the parameters record; null on failure
     */
    private RawParameters getParameters() {

        RawParameters result = null;

        try {
            final RawParameters row = RawParametersLogic.query(this.cache, "FINALGRADES");

            if (row.parm1 == null) {
                Log.warning("ERROR: Parameters row missing parameter 1.");
            } else if (row.parm2 == null) {
                Log.warning("ERROR: Parameters row missing parameter 2.");
            } else if (row.parm3 == null) {
                Log.warning("ERROR: Parameters row missing parameter 3.");
            } else {
                result = row;
            }
        } catch (final SQLException ex) {
            Log.warning("ERROR: Failed to query for parameters for FINALGRADES.", ex);
        }

        return result;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final File reportPath = PathList.getInstance().get(EPath.REPORT_PATH);
        if (reportPath == null) {
            Log.warning("ERROR: Unable to determine report path.");
        } else {

            DbConnection.registerDrivers();

            final DatabaseConfig config = DatabaseConfig.getDefault();
            final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
            final Cache cache = new Cache(profile);

            final Runnable obj = new CreateLastAttendanceFile(cache, reportPath);
            obj.run();
        }
    }
}