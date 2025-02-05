package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates a report of all current-term registrations that have open_status NULL but have some work on record.
 *
 * <p>
 * Also generates a list of STCOURSE records that were added after 7/22/22, indicating they were "new" after a Banner
 * outage caused records to be marked as Dropped.
 */
public enum NotStartedButHasWork {
    ;

    /**
     * Runs the report, writing the result to a file.
     */
    private static void runReport() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.REPORT_PATH);
        final Cache cache = new Cache(profile);

        try {
            final Collection<String> report = new ArrayList<>(100);

            report.add(CoreConstants.EMPTY);
            report.add("STCOURSE rows with open_status = NULL but with work on record");
            report.add(CoreConstants.EMPTY);

            generate1(cache, report);

            report.add(CoreConstants.EMPTY);
            report.add(CoreConstants.EMPTY);
            report.add("STCOURSE rows with last_class_roll_dt after 7/22 with a dropped match");
            report.add(CoreConstants.EMPTY);

            generate2(cache, report);

            report.add(CoreConstants.EMPTY);
            report.add(CoreConstants.EMPTY);
            report.add("STCOURSE rows that are started but with no pace order");
            report.add(CoreConstants.EMPTY);

            generate3(cache, report);

            for (final String s : report) {
                Log.fine(s);
            }

            report.add(CoreConstants.EMPTY);
            report.add(CoreConstants.EMPTY);
            report.add("STCOURSE where order of exams does not match pace order");
            report.add(CoreConstants.EMPTY);

            generate4(cache, report);

            for (final String s : report) {
                Log.fine(s);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates the report and appends it to a list of strings. This can be called from external reports or batches to
     * embed this report in a larger report.
     *
     * @param cache  the data cache
     * @param report the list to which to append report lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void generate1(final Cache cache, final Collection<? super String> report) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        // Exclude OT and dropped for this part
        final List<RawStcourse> activeTermRegs = RawStcourseLogic.queryByTerm(cache, active.term, false, false);

        report.add("Student ID    Course    Sect");
        report.add("----------    ------    ----");

        for (final RawStcourse test : activeTermRegs) {

            if (test.openStatus == null) {

                final List<RawStexam> exams =
                        RawStexamLogic.getExams(cache, test.stuId, test.course, false, "R");

                if (!exams.isEmpty()) {
                    report.add(test.stuId + "     " + test.course + "     " + test.sect);

                    // RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, test.stuId,
                    // test.course, test.sect, active.termKey, "Y", test.finalClassRoll,
                    // test.lastClassRollDt);
                }
            }
        }
    }

    /**
     * Generates the report and appends it to a list of strings. This can be called from external reports or batches to
     * embed this report in a larger report.
     *
     * @param cache  the data cache
     * @param report the list to which to append report lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void generate2(final Cache cache, final Collection<? super String> report) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        final LocalDate cutoff = LocalDate.of(2022, 7, 22);

        // Exclude OT but include dropped for this part
        final List<RawStcourse> activeTermRegs =
                RawStcourseLogic.queryByTerm(cache, active.term, false, true);

        // Step 1: Sort into groups with last_class_roll after cutoff, organized by stu/course/sect
        final Map<String, List<RawStcourse>> found = new HashMap<>(100);
        for (final RawStcourse test : activeTermRegs) {

            if (test.lastClassRollDt != null && test.lastClassRollDt.isAfter(cutoff)) {
                final String key = test.stuId + test.course + test.sect;

                final List<RawStcourse> list = found.computeIfAbsent(key, s -> new ArrayList<>(5));
                list.add(test);
            }
        }

        // Step 2: Scan, and identify issues
        int count = 0;
        for (final Map.Entry<String, List<RawStcourse>> entry : found.entrySet()) {

            RawStcourse dropped = null;
            RawStcourse live = null;

            final List<RawStcourse> list = entry.getValue();
            for (final RawStcourse row : list) {
                if ("D".equals(row.openStatus)) {
                    dropped = row;
                } else {
                    live = row;
                }
            }

            if (dropped != null && live != null) {
                final RawStcourse sample = list.getFirst();
                report.add("Student " + sample.stuId + ", " + sample.course + "/" + sample.sect);
                ++count;

                // if ("D".equals(dropped.openStatus)) {
                // RawStcourseLogic.delete(cache, dropped);
                // }
            }
        }
        report.add(CoreConstants.EMPTY);
        report.add(count + " instances found");
    }

    /**
     * Generates the report and appends it to a list of strings. This can be called from external reports or batches to
     * embed this report in a larger report.
     *
     * @param cache  the data cache
     * @param report the list to which to append report lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void generate3(final Cache cache, final Collection<? super String> report) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        // Exclude OT and dropped for this part
        final List<RawStcourse> activeTermRegs =
                RawStcourseLogic.queryByTerm(cache, active.term, false, false);

        final Map<String, List<RawStcourse>> byStudent = new HashMap<>(10);
        for (final RawStcourse test : activeTermRegs) {
            if ("Y".equals(test.openStatus) && test.paceOrder == null) {
                final List<RawStcourse> list = byStudent.computeIfAbsent(test.stuId, s -> new ArrayList<>(5));
                list.add(test);
            }
        }

        report.add("Student ID");
        report.add("----------");

        int count = 0;
        // final Integer order1 = Integer.valueOf(1);
        for (final Map.Entry<String, List<RawStcourse>> entry : byStudent.entrySet()) {
            final List<RawStcourse> list = entry.getValue();
            if (list.size() == 1) {
                final RawStcourse row = list.getFirst();

//                if (row.paceOrder == null) {
//                     RawStcourseLogic.updatePaceOrder(cache, row.stuId, row.course, row.sect,
//                     active.termKey, order1);
//                }
            } else {
                final RawStcourse row = list.getFirst();
                report.add(row.stuId);
                ++count;
            }
        }

        report.add(CoreConstants.EMPTY);
        report.add(count + " instances found");
    }

    /**
     * Generates the report and appends it to a list of strings. This can be called from external reports or batches to
     * embed this report in a larger report.
     *
     * @param cache  the data cache
     * @param report the list to which to append report lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void generate4(final Cache cache, final Collection<? super String> report) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        // Exclude OT and dropped for this part
        final List<RawStcourse> activeTermRegs =
                RawStcourseLogic.queryByTerm(cache, active.term, false, false);

        // Step 1: Group by student
        final Map<String, List<RawStcourse>> found = new HashMap<>(100);
        for (final RawStcourse test : activeTermRegs) {
            if ("G".equals(test.openStatus)) {
                continue;
            }

            final List<RawStcourse> list = found.computeIfAbsent(test.stuId, s -> new ArrayList<>(5));
            list.add(test);
        }

        // Now, for each student, find all exams on record, and get the earliest exam date in each
        // course.
        for (final List<RawStcourse> list : found.values()) {
            final int count = list.size();
            final LocalDate[] earliest = new LocalDate[count];

            for (int i = 0; i < count; ++i) {
                final RawStcourse record = list.get(i);

                final List<RawStexam> exams = RawStexamLogic.getExams(cache, record.stuId, record.course, false, "R");
                for (final RawStexam exam : exams) {
                    if (earliest[i] == null || earliest[i].isAfter(exam.examDt)) {
                        earliest[i] = exam.examDt;
                    }
                }
            }

            // Order by first exam date
            final Map<LocalDate, RawStcourse> sorted = new TreeMap<>();
            for (int i = 0; i < count; ++i) {
                if (earliest[i] != null) {
                    sorted.put(earliest[i], list.get(i));
                }
            }

            // Now check that the pace_order agrees with work order
            int expect = 1;
            for (final RawStcourse test : sorted.values()) {

                if (test.openStatus == null) {
                    report.add("open_status null on STCOURSE with work for " + test.stuId + " in "
                               + test.course + "/" + test.sect);
                }

                if (test.paceOrder == null || test.paceOrder.intValue() != expect) {
                    report.add("Unexpected pace_order on STCOURSE for " + test.stuId + " in "
                               + test.course + "/" + test.sect + " (expected " + expect + ", but found "
                               + test.paceOrder);
                }
                ++expect;
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
        runReport();
    }
}
