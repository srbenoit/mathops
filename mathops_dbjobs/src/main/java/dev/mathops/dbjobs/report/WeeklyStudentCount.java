package dev.mathops.dbjobs.report;

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
 * Generates a report that shows the number of students in each section, and determines how many are in each pace (and
 * each pace track).
 */
final class WeeklyStudentCount implements Runnable {

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT =
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

    /**
     * Private constructor to prevent direct instantiation.
     */
    private WeeklyStudentCount() {

        this.sectionStudents = new TreeMap<>();
        this.regsByStudent = new HashMap<>(4000);
        this.studentPaces = new HashMap<>(4000);
        this.studentTracks = new HashMap<>(4000);
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

                Log.info("Generating weekly report of student counts by section and track.....please wait.");

                compute();
                generateReport(report);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }

        final LocalDateTime now = LocalDateTime.now();
        final File file = new File("/opt/zircon/reports/Weekly_Student_Count_" + DTTM_FMT.format(now) + ".txt");
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

        // Scan registrations, accumulating counts by section, and building sets of courses for each student
        for (final RawStcourse stc : this.registrations) {
            final String sect = stc.sect;
            final String stuId = stc.stuId;

            final Set<String> cur = this.sectionStudents.computeIfAbsent(sect, s -> new HashSet<>(1000));
            cur.add(stuId);

            final Map<String, RawStcourse> map = this.regsByStudent.computeIfAbsent(stuId, s -> new TreeMap<>());
            map.put(stc.course, stc);
        }

        // Now generate the pace and track for each student
        for (final Map.Entry<String, Map<String, RawStcourse>> e : this.regsByStudent.entrySet()) {
            final String stuId = e.getKey();
            final Iterable<RawStcourse> courses = new ArrayList<>(e.getValue().values());

            final int pace = PaceTrackLogic.determinePace(courses);
            final String track = PaceTrackLogic.determinePaceTrack(courses, pace);

            this.studentPaces.put(stuId, Integer.valueOf(pace));
            this.studentTracks.put(stuId, track);
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
                .addln("                    STUDENT COUNTS BY SECTION / PACE / TRACK ")
                .addln("                           REPORT DATE:  ", TemporalUtils.FMT_MDY.format(LocalDate.now()))
                .addln();

        // First, print counts by each section number
        report.addln("Number of students currently enrolled, by section").addln();
        int total = 0;
        for (final Map.Entry<String, Set<String>> e : this.sectionStudents.entrySet()) {
            report.addln("    SECT ", e.getKey(), ": ", Integer.valueOf(e.getValue().size()));
            total += e.getValue().size();
        }
        report.addln("    TOTAL: ", Integer.toString(total)).addln();

        // Maps from track to number in that track for each pace
        final Iterable<String> students = new ArrayList<>(this.regsByStudent.keySet());

        final Map<String, Integer> ones = new TreeMap<>();
        final Map<String, Integer> twos = new TreeMap<>();
        final Map<String, Integer> threes = new TreeMap<>();
        final Map<String, Integer> fours = new TreeMap<>();
        final Map<String, Integer> fives = new TreeMap<>();

        for (final String s : students) {
            final Integer pace = this.studentPaces.get(s);
            final String track = this.studentTracks.get(s);

            if (pace == null) {
                report.addln("ERROR: Student ", s, " has no pace");
            } else if (track == null) {
                report.addln("ERROR: Student ", s, " has no track");
            } else {
                switch (pace.intValue()) {
                    case 1:
                        final Integer cur1 = ones.get(track);
                        if (cur1 == null) {
                            ones.put(track, Integer.valueOf(1));
                        } else {
                            ones.put(track, Integer.valueOf(cur1.intValue() + 1));
                        }
                        break;
                    case 2:
                        final Integer cur2 = twos.get(track);
                        if (cur2 == null) {
                            twos.put(track, Integer.valueOf(1));
                        } else {
                            twos.put(track, Integer.valueOf(cur2.intValue() + 1));
                        }
                        break;
                    case 3:
                        final Integer cur3 = threes.get(track);
                        if (cur3 == null) {
                            threes.put(track, Integer.valueOf(1));
                        } else {
                            threes.put(track, Integer.valueOf(cur3.intValue() + 1));
                        }
                        break;
                    case 4:
                        final Integer cur4 = fours.get(track);
                        if (cur4 == null) {
                            fours.put(track, Integer.valueOf(1));
                        } else {
                            fours.put(track, Integer.valueOf(cur4.intValue() + 1));
                        }
                        break;
                    case 5:
                        final Integer cur5 = fives.get(track);
                        if (cur5 == null) {
                            fives.put(track, Integer.valueOf(1));
                        } else {
                            fives.put(track, Integer.valueOf(cur5.intValue() + 1));
                        }
                        break;
                    default:
                        report.addln("ERROR: Student ", s, " has pace = ", pace);
                        break;
                }
            }
        }

        // Print counts by each pace and track
        report.addln().addln("Number of students currently enrolled, by pace and track").addln();

        total = 0;
        report.addln().addln("    One course pace:");
        for (final Map.Entry<String, Integer> e1 : ones.entrySet()) {
            report.addln("        Track ", e1.getKey(), ": ", e1.getValue());
            total += e1.getValue().intValue();
        }
        report.addln("        TOTAL: ", Integer.toString(total));

        total = 0;
        report.addln().addln("    Two course pace:");
        for (final Map.Entry<String, Integer> e2 : twos.entrySet()) {
            report.addln("        Track ", e2.getKey(), ": ", e2.getValue());
            total += e2.getValue().intValue();
        }
        report.addln("        TOTAL: ", Integer.toString(total));

        total = 0;
        report.addln().addln("    Three course pace:");
        for (final Map.Entry<String, Integer> e3 : threes.entrySet()) {
            report.addln("        Track ", e3.getKey(), ": ", e3.getValue());
            total += e3.getValue().intValue();
        }
        report.addln("        TOTAL: ", Integer.toString(total));

        total = 0;
        report.addln().addln("    Four course pace:");
        for (final Map.Entry<String, Integer> e4 : fours.entrySet()) {
            report.addln("        Track ", e4.getKey(), ": ", e4.getValue());
            total += e4.getValue().intValue();
        }
        report.addln("        TOTAL: ", Integer.toString(total));

        total = 0;
        report.addln().addln("    Five course pace:");
        for (final Map.Entry<String, Integer> e5 : fives.entrySet()) {
            report.addln("        Track ", e5.getKey(), ": ", e5.getValue());
            total += e5.getValue().intValue();
        }
        report.addln("        TOTAL: ", Integer.toString(total));
    }

    /**
     * Main method to run the report.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        new WeeklyStudentCount().run();
    }
}
