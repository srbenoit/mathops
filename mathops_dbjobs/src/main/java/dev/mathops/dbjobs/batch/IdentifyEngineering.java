package dev.mathops.dbjobs.batch;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawlogic.RawApplicantLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawrecord.RawApplicant;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This class scans the APPLICANTS table for applicants with programs of study that fall in a list that are identified
 * as "Engineering", and then adds them as "ENGRPLC" special student records.
 */
public final class IdentifyEngineering {

    /** The special category. */
    public static final String SPECIAL_CATEGORY = "ENGRPLC";

    /** Debug flag - set to 'true' to print changes rather than performing them. */
    private static final boolean DEBUG = false;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /** The list of "engineering" program codes. */
    private static final List<String> ENGINEERING_PROGRAM_CODES = Arrays.asList("CBEG-AVMZ-BS", "CBEG-BIMS-BS",
            "CBEG-BMEC-BS", "CBEG-BS", "CBEG-MLMZ-BS", "CBEG-SSEZ-BS", "CIVE-BS", "CPEG-AESZ-BS", "CPEG-BMEP-BS",
            "CPEG-BS", "CPEG-EISZ-BS", "CPEG-NDTZ-BS", "ELEG-ASPZ-BS", "ELEG-BMEE-BS", "ELEG-BMEL-BS", "ELEG-ELEZ-BS",
            "ELEG-LOEZ-BS", "ENVE-BS", "MECH-ACEZ-BS", "MECH-ADMZ-BS", "MECH-ASU-BS", "MECH-BMEM-BS", "MECH-BS");

    /**
     * Constructs a new {@code IdentifyEngineering}.
     */
    private IdentifyEngineering() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @param applicationTerm the application term for which to scan
     * @return a report
     */
    public String execute(final TermKey applicationTerm) {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            report.add("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                executeInTerm(cache, applicationTerm, report);
            } catch (final SQLException ex) {
                Log.warning(ex);
                report.add("Unable to perform query");
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
     * Executes the query against the ODS term table matching the active term and loads data into the primary schema.
     *
     * @param cache           the data cache
     * @param applicationTerm the application term for which to scan
     * @param report          a list of strings to which to add report output lines
     * @throws SQLException if there is an error querying the database
     */
    private void executeInTerm(final Cache cache, final TermKey applicationTerm,
                               final Collection<? super String> report) throws SQLException {

        final LocalDate today = LocalDate.now();
        final List<RawSpecialStus> existingSpecials = RawSpecialStusLogic.queryActiveByType(cache, SPECIAL_CATEGORY,
                today);
        final int numExisting = existingSpecials.size();

        report.add("Found " + numExisting + " existing " + SPECIAL_CATEGORY + " records in SPECIAL_STUS");

        final List<RawApplicant> applicants = RawApplicantLogic.queryAll(cache);
        final List<RawApplicant> engineering = new ArrayList<>(1000);

        int total = 0;

        for (final RawApplicant row : applicants) {
            if (applicationTerm.equals(row.aplnTerm)) {
                ++total;

                if (row.progStudy != null && ENGINEERING_PROGRAM_CODES.contains(row.progStudy)) {
                    engineering.add(row);
                }
            }
        }

        report.add("Found " + total + " applicants for " + applicationTerm.longString);

        final int count = engineering.size();
        report.add("Found " + count + " Engineering applicants for " + applicationTerm.longString);

        processList(cache, engineering, existingSpecials, report);
    }

    /**
     * Processes the list of Engineering applicants.
     *
     * @param cache                 the data cache
     * @param engineeringApplicants the list of Engineering applicants
     * @param existingSpecials      the list of existing special student records
     * @param report                a list of strings to which to add report output lines
     * @throws SQLException if there is an error querying the database
     */
    private static void processList(final Cache cache, final List<RawApplicant> engineeringApplicants,
                                    final Collection<RawSpecialStus> existingSpecials,
                                    final Collection<? super String> report) throws SQLException {

        final int numExisting = existingSpecials.size();
        final Collection<String> existingStuIds = new HashSet<>(numExisting);
        for (final RawSpecialStus row : existingSpecials) {
            existingStuIds.add(row.stuId);
        }

        for (final RawApplicant applicant : engineeringApplicants) {
            if (existingStuIds.contains(applicant.stuId)) {
                continue;
            }

            report.add("  Adding SPECIAL_STUS row for " + applicant.stuId);

            if (!DEBUG) {
                final RawSpecialStus newRow = new RawSpecialStus(applicant.stuId, SPECIAL_CATEGORY, null, null);
                RawSpecialStusLogic.insert(cache, newRow);
            }
        }

    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final IdentifyEngineering job = new IdentifyEngineering();

        final TermKey applicationTerm = new TermKey(ETermName.FALL, 2026);

        Log.fine(job.execute(applicationTerm));
    }
}
