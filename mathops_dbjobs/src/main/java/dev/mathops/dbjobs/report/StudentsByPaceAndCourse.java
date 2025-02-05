package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Generates a report that the number of students taking each course in each position of a pace under each track.
 */
final class StudentsByPaceAndCourse implements Runnable {

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT = //
            DateTimeFormatter.ofPattern("yyyy'_'MM'_'dd'-'hh'_'mm'_'ss", Locale.US);

    /** The list of registrations. */
    private List<RawStcourse> registrations;

    /** Set of students in each section number. */
    private final Map<String, Set<String>> sectionStudents;

    /** Map from student ID to set of courses for which the student is registered. */
    private final Map<String, Map<String, RawStcourse>> regsByStudent;

    /** Pace for each student. */
    private final Map<String, Integer> studentPaces;

    /** Pace track for each student. */
    private final Map<String, String> studentTracks;

    /** Map: CourseId -> Pace -> Order -> Track -> Integer count */
    private final Map<String, Map<Integer, Map<Integer, Map<String, Integer>>>> counts;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentsByPaceAndCourse() {

        this.sectionStudents = new TreeMap<>();
        this.regsByStudent = new HashMap<>(4000);
        this.studentPaces = new HashMap<>(4000);
        this.studentTracks = new HashMap<>(4000);
        this.counts = new HashMap<>(5);
    }

    /**
     * Runs the report.
     */
    @Override
    public void run() {

        final HtmlBuilder report = new HtmlBuilder(10000);

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.REPORT_PATH);

        if (profile == null) {
            report.addln("*** ERROR: There is no database code profile named 'report'.");
        } else {
            Log.info("Using ", profile.id, " profile");
            final Cache cache = new Cache(profile);

            try {
                this.registrations = RawStcourseLogic.queryActiveForActiveTerm(cache);

                Log.info("Generating report of enrollments in courses at each position in each track...");

                compute();
                generateReport(report);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }

        final LocalDateTime now = LocalDateTime.now();
        final File file = new File("/opt/zircon/reports/CountByPaceOrderAndCourse_" + DTTM_FMT.format(now) + ".txt");
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

        // Scan registrations building sets of courses for each student
        for (final RawStcourse stc : this.registrations) {
            final String sect = stc.sect;
            final String stuId = stc.stuId;

            final Set<String> cur = this.sectionStudents.computeIfAbsent(sect, s -> new HashSet<>(1000));
            cur.add(stuId);

            final Map<String, RawStcourse> map = this.regsByStudent.computeIfAbsent(stuId, k -> new TreeMap<>());
            map.put(stc.course, stc);
        }

        // Now generate the pace and track for each student
        for (final Map.Entry<String, Map<String, RawStcourse>> e : this.regsByStudent.entrySet()) {
            final String stuId = e.getKey();
            final Iterable<RawStcourse> courses = new ArrayList<>(e.getValue().values());

            final int pace = PaceTrackLogic.determinePace(courses);
            final String track = PaceTrackLogic.determinePaceTrack(courses, pace);

            final Integer paceInteger = Integer.valueOf(pace);
            this.studentPaces.put(stuId, paceInteger);
            this.studentTracks.put(stuId, track);

            // Accumulate counts for each course
            // (counts is map: CourseId -> Pace -> Order -> Track -> Integer count)

            int order = 1;
            for (final RawStcourse reg : courses) {
                final String courseId = reg.course;

                final Map<Integer, Map<Integer, Map<String, Integer>>> inner1 = this.counts.computeIfAbsent(courseId,
                        s -> new HashMap<>(5));
                // 5 courses

                final Map<Integer, Map<String, Integer>> inner2 = inner1.computeIfAbsent(paceInteger,
                        k -> new HashMap<>(5));
                // 5 possible paces

                final Integer orderInteger = Integer.valueOf(order);
                final Map<String, Integer> inner3 = inner2.computeIfAbsent(orderInteger, i -> new HashMap<>(pace));

                final Integer cur = inner3.get(track);
                if (cur == null) {
                    inner3.put(track, Integer.valueOf(1));
                } else {
                    inner3.put(track, Integer.valueOf(cur.intValue() + 1));
                }

                ++order;
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
                .addln("                   COLORADO STATE UNIVERSITY MATHEMATICS DEPT")
                .addln("                    STUDENT COUNTS BY PACE / ORDER / TRACK ")
                .addln("                           REPORT DATE:  ",
                        TemporalUtils.FMT_MDY.format(LocalDate.now()))
                .addln();

        // First, print counts by each section number
        report.addln("Number of enrollments in each course by pace, order, and track").addln();

        // Counts is map: CourseId -> Pace -> Order -> Track -> Integer count
        for (final Map.Entry<String, Map<Integer, Map<Integer, Map<String, Integer>>>> byCourse :
                this.counts.entrySet()) {
            report.addln(byCourse.getKey(), CoreConstants.COLON);

            for (final Map.Entry<Integer, Map<Integer, Map<String, Integer>>> byPace : byCourse.getValue().entrySet()) {
                report.addln("    ", byPace.getKey(), " course pace:");

                for (final Map.Entry<Integer, Map<String, Integer>> byOrder : byPace.getValue().entrySet()) {
                    report.addln("        Course ", byOrder.getKey(), CoreConstants.COLON);

                    for (final Map.Entry<String, Integer> byTrack : byOrder.getValue().entrySet()) {
                        report.addln("            Track ", byTrack.getKey(), ": ", byTrack.getValue());
                    }
                }
            }

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
        new StudentsByPaceAndCourse().run();
    }
}
