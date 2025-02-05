package dev.mathops.dbjobs.report;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates a report that the number of students with each unique combination of courses.
 */
final class StudentsByUniqueCourseCombination implements Runnable {

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT = //
            DateTimeFormatter.ofPattern("yyyy'_'MM'_'dd'-'hh'_'mm'_'ss", Locale.US);

    /** The list of registrations. */
    private List<RawStcourse> registrations;

    /**
     * Map: Combination -> Integer count, where combination is a string like "78456" with the trailing digit of each
     * course, in course-number order.
     */
    private final Map<String, Integer> counts;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentsByUniqueCourseCombination() {

        this.counts = new TreeMap<>();
    }

    /**
     * Runs the report.
     */
    @Override
    public void run() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.REPORT_PATH);

        final HtmlBuilder report = new HtmlBuilder(10000);

        if (profile == null) {
            report.addln("*** ERROR: There is no database code profile named 'report'.");
        } else {
            Log.info("Using ", profile.id, " profile");
            final Cache cache = new Cache(profile);

            try {
                this.registrations = RawStcourseLogic.queryActiveForActiveTerm(cache);

                Log.info("Generating report of counts for each unique course combination... please wait.");

                compute();
                generateReport(report);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }

        final LocalDateTime now = LocalDateTime.now();
        final File file = new File("/opt/zircon/reports/CountByUniqueCourseCombination_" + DTTM_FMT.format(now)
                                   + ".txt");
        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(report.toString());
            Log.info("Report complete, written to: ", file.getAbsolutePath());
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Computes summary data based on the registrations and exams queried.
     */
    private void compute() {

        // Assemble the set of distinct students
        final Collection<String> studentIds = new HashSet<>(3000);
        for (final RawStcourse stc : this.registrations) {
            studentIds.add(stc.stuId);
        }

        // For each student, compute the collection of courses and increment the count for that'
        // combination
        final StringBuilder key = new StringBuilder(5);
        for (final String studentId : studentIds) {
            boolean has117 = false;
            boolean has118 = false;
            boolean has124 = false;
            boolean has125 = false;
            boolean has126 = false;

            for (final RawStcourse stc : this.registrations) {
                if (studentId.equals(stc.stuId)) {
                    if (RawRecordConstants.M117.equals(stc.course)
                        || RawRecordConstants.MATH117.equals(stc.course)) {
                        has117 = true;
                    } else if (RawRecordConstants.M118.equals(stc.course)
                               || RawRecordConstants.MATH118.equals(stc.course)) {
                        has118 = true;
                    } else if (RawRecordConstants.M124.equals(stc.course)
                               || RawRecordConstants.MATH124.equals(stc.course)) {
                        has124 = true;
                    } else if (RawRecordConstants.M125.equals(stc.course)
                               || RawRecordConstants.MATH125.equals(stc.course)) {
                        has125 = true;
                    } else if (RawRecordConstants.M126.equals(stc.course)
                               || RawRecordConstants.MATH126.equals(stc.course)) {
                        has126 = true;
                    }
                }
            }

            key.setLength(0);
            key.append(has117 ? '7' : ' ');
            key.append(has118 ? '8' : ' ');
            key.append(has124 ? '4' : ' ');
            key.append(has125 ? '5' : ' ');
            key.append(has126 ? '6' : ' ');

            final String keyStr = key.toString();

            final Integer cur = this.counts.get(keyStr);
            if (cur == null) {
                this.counts.put(keyStr, Integer.valueOf(1));
            } else {
                this.counts.put(keyStr, Integer.valueOf(cur.intValue() + 1));
            }
        }
    }

    /**
     * Generates the report.
     *
     * @param report the {@code HtmlBuilder} to which to write error messages
     */
    private void generateReport(final HtmlBuilder report) {

        report.addln("                              *** CONFIDENTIAL ***")
                .addln("                   COLORADO STATE UNIVERSITY MATHEMATICS DEPT.")
                .addln("                   STUDENT COUNTS BY UNIQUE COURSE COMBINATION")
                .addln("                           REPORT DATE:  ", TemporalUtils.FMT_MDY.format(LocalDate.now()))
                .addln();

        // First, print counts by each section number
        report.addln("Number of students with each unique combination of courses").addln();

        // Counts is map: CourseId -> Pace -> Order -> Track -> Integer count
        for (final Map.Entry<String, Integer> entry : this.counts.entrySet()) {
            report.addln(entry.getKey(), ": ", entry.getValue());
            report.addln();
        }
    }

    /**
     * Main method to run the report.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        new StudentsByUniqueCourseCombination().run();
    }
}
