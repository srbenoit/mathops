package dev.mathops.dbjobs.report.analytics.longitudinal.datacollection;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class retrieves data from the ODS and stores it in a local file so we can load and process it without having to
 * do a lengthy ODS query each time.
 */
public enum RetrieveLongitudinalData {
    ;

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final File dir = new File("C:\\opt\\zircon\\data");

        final int start = 201000; // 201000
        final int end = 202480;   // 202480 - Excludes FALL 2024

        if (dir.exists() || dir.mkdirs()) {

            final ContextMap contextMap = ContextMap.getDefaultInstance();
            final DbProfile dbProfile = contextMap.getCodeProfile(Contexts.BATCH_PATH);
            final DbContext odsCtx = dbProfile.getDbContext(ESchemaUse.ODS);

            if (odsCtx == null) {
                Log.warning("Unable to create ODS database context.");
            } else {
                final DbConnection odsConn = odsCtx.checkOutConnection();

                try {
                    final File majorsProgramsFile = new File(dir, "majors_programs.json");
                    FetchMajorAndProgramData.gatherMajorAndProgramData(odsConn, start, end, majorsProgramsFile);

                    final File registrationStatusFile = new File(dir, "registration_statuses.json");
                    FetchRegistrationStatusData.gatherMajorAndProgramData(odsConn, start, end, registrationStatusFile);

                    // A map from student ID to the list of academic period values in which the student was enrolled.
                    final Map<String, List<Integer>> map = new HashMap<>(100000);

                    // Gather all student enrollments in the period of interest (this should include several years
                    // prior to the reporting start date since we look back to see how students cleared prerequisites
                    // for courses in the reporting period.

                    final File enrollmentsFile = new File(dir, "enrollments.json");
                    FetchEnrollmentData.gatherEnrollmentData(odsConn, start, end, enrollmentsFile, map);

                    // For all terms in which a student was enrolled, we want to know the student's college, department,
                    // major, and program code

                    final File studentTermsFile = new File(dir, "student_terms.json");
                    FetchStudentTermData.gatherStudentTermData(odsConn, start, end, studentTermsFile, map);

                    // Finally, we want student demographic information like gender and ethnicity (and cohort data
                    // when we can get it)

                    final File studentFile = new File(dir, "students.json");
                    final Set<String> studentIdSet = map.keySet();
                    FetchStudentData.gatherStudentData(odsConn, studentIdSet, studentFile);

                } catch (final SQLException ex) {
                    Log.warning("Failed to query ODS.", ex);
                } finally {
                    odsCtx.checkInConnection(odsConn);
                }
            }
        }
    }
}

