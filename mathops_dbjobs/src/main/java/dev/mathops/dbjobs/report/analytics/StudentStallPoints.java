package dev.mathops.dbjobs.report.analytics;

import dev.mathops.commons.IProgressListener;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This program queries all students with "open" but not "completed" registrations in the current term, and within each,
 * looks at what assignments the student has completed, how long since the last completion of an assignment, and how
 * many tries have been made on an assignment that is not yet completed.
 */
final class StudentStallPoints {

    /** The data cache. */
    private final Cache cache;

    /** An optional progress listener. */
    private final IProgressListener listener;

    /** Flag that can cancel an in-progress scan. */
    private final AtomicBoolean canceled;

    /** All open, non-completed registrations in the term. */
    private List<RawStcourse> registrations;

    /**
     * Constructs a new {@code StudentStallPoints}.
     *
     * @param theCache    the data cache
     * @param theListener an optional progress listener
     */
    private StudentStallPoints(final Cache theCache, final IProgressListener theListener) {

        this.cache = theCache;
        this.listener = theListener;
        this.canceled = new AtomicBoolean(false);
    }

    /**
     * Cancels the operations.
     */
    public void cancel() {

        this.canceled.set(true);
    }

    /**
     * Tests whether the scan was canceled.
     *
     * @return true if canceled
     */
    public boolean wasCanceled() {

        return this.canceled.get();
    }

    /**
     * Executes the job.
     *
     * @param includeSections map from course ID to a list of section numbers to include in the scan
     */
    private void calculate(final Map<String, ? extends List<String>> includeSections) {

        // In terms of progress, the first 1% will be loading data, then next 99 will be scanning students.

        final File desktop = new File("/Users/benoit.MATH4/OneDrive - Colostate/Desktop");

        // Map from course to a list of report rows
        final Map<String, List<String>> outputs = new TreeMap<>();

        this.canceled.set(false);

        try {
            gatherOneTimeInformation();

            final int total = this.registrations.size() + 2;
            int current = 2;
            for (final RawStcourse reg : this.registrations) {

                final List<String> sects = includeSections.get(reg.course);
                if (sects != null && sects.contains(reg.sect)) {

                    final List<String> output = outputs.computeIfAbsent(reg.course, s -> new ArrayList<>(500));

                    if ("Y".equals(reg.openStatus) && !"Y".equals(reg.completed)) {
                        processRegistration(reg, output);
                    }
                    fireProgress("Scanned registration", current, total);
                    ++current;
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        final HtmlBuilder htm = new HtmlBuilder(10000);
        for (final Map.Entry<String, List<String>> entry : outputs.entrySet()) {
            htm.addln(entry.getKey(), ":");

            for (final String s : entry.getValue()) {
                htm.addln("  ", s);
            }
        }

        writeReport(new File(desktop, "Student Stall Point Results.txt"), htm.toString());
    }

    /**
     * Writes a report file.
     *
     * @param target  the target file
     * @param content the report content
     */
    private static void writeReport(final File target, final String content) {

        try (final FileWriter w = new FileWriter(target, StandardCharsets.UTF_8)) {
            w.write(content);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Collects "one-time" information, including all student registrations, all milestones, and the active term row for
     * each term database.
     *
     * @throws SQLException if there is an error accessing the database
     */
    private void gatherOneTimeInformation() throws SQLException {

        fireProgress("Querying active term", 1, 200);
        final TermRec active = this.cache.getSystemData().getActiveTerm();
        Log.info("    Active term is ", active.term);

        fireProgress("Querying registrations", 2, 200);
        final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(this.cache, active.term, false, false);
        Log.info("    Queried ", Integer.toString(allRegs.size()), " registrations");

        this.registrations = new ArrayList<>(8000);
        for (final RawStcourse reg : allRegs) {
            if ("Y".equals(reg.openStatus) && "N".equals(reg.completed)) {
                this.registrations.add(reg);
            }
        }
    }

    /**
     * Processes a single registration. This may or may not generates a report row with a stall point for that student.
     *
     * @param reg    the registration
     * @param output a list to which to add output lines
     * @throws SQLException if there is an error accessing the database
     */
    private void processRegistration(final RawStcourse reg, final Collection<? super String> output)
            throws SQLException {

        final List<RawStexam> stexams = RawStexamLogic.queryByStudentCourse(this.cache, reg.stuId, reg.course, true);
        final List<RawSthomework> sthomeworks = RawSthomeworkLogic.queryByStudentCourse(this.cache, reg.stuId,
                reg.course, true);

        if (stexams.isEmpty() && sthomeworks.isEmpty()) {
            output.add("Student " + reg.stuId + " has not yet done work in " + reg.course);
        } else {
            // Find the most recent exam and homework completed, and the latest work date
            RawStexam latestExam = null;
            RawSthomework latestHomework = null;
            LocalDate latestWorkDate = null;
            int latestWorkTime = 0;

            for (final RawStexam stexam : stexams) {
                if (latestExam == null || latestExam.examDt.isBefore(stexam.examDt)
                    || (latestExam.examDt.equals(stexam.examDt)
                        && latestExam.finishTime.intValue() < stexam.finishTime.intValue())) {
                    latestExam = stexam;
                    latestWorkDate = stexam.examDt;
                    latestWorkTime = stexam.finishTime.intValue();
                }
            }

            for (final RawSthomework sthomework : sthomeworks) {

                if (latestWorkDate == null) {
                } else if (latestWorkDate.isAfter(sthomework.hwDt) || (latestWorkDate.equals(sthomework.hwDt)
                                                                       && latestWorkTime >= sthomework.finishTime.intValue())) {
                    continue;
                }

                // The homework falls after the latest exam that was found and any later homeworks
                // that were found after that.
                latestExam = null;
                latestHomework = sthomework;
                latestWorkDate = sthomework.hwDt;
                latestWorkTime = sthomework.finishTime.intValue();
            }

            if (latestWorkDate != null) {
                final long days = LocalDate.now().toEpochDay() - latestWorkDate.toEpochDay();

                if (days > 3L) {
                    if (latestExam != null && !"Y".equals(latestExam.passed)) {
                        output.add("Student " + reg.stuId + " stalled on exam " + latestExam.version);
                    } else if (latestHomework != null && !"Y".equals(latestHomework.passed)) {
                        output.add("Student " + reg.stuId + " stalled on HW " + latestHomework.version);
                    }
                }
            }
        }
    }

    /**
     * Fires a progress notification if there is a listener installed.
     *
     * @param description a description of the current step
     * @param done        the number of steps done
     * @param total       the number of total steps
     */
    private void fireProgress(final String description, final int done, final int total) {

        // Log.info(description);
        if (this.listener != null) {
            this.listener.progress(description, done, total);
        }
    }

    /**
     * Executes the analysis for a specified term.
     *
     * @param profile         the profile
     * @param includeSections map from course ID to a list of section numbers to include in the scan
     */
    private static void executeWithProfile(final Profile profile,
                                           final Map<String, ? extends List<String>> includeSections) {

        if (profile == null) {
            Log.warning("Unable to create production context.");
        } else {
            Log.info("Using profile" + profile.id);

            final Cache cache = new Cache(profile);

            final StudentStallPoints obj = new StudentStallPoints(cache, null);
            obj.calculate(includeSections);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final Map<String, List<String>> includeSections = new HashMap<>(10);

        final String[] sections = {"001", "002", "401", "801", "802", "809"};
        final List<String> sect117 = Arrays.asList(sections);
        final List<String> sect118 = Arrays.asList(sections);
        final List<String> sect124 = Arrays.asList(sections);
        final List<String> sect125 = Arrays.asList(sections);
        final List<String> sect126 = Arrays.asList(sections);

        includeSections.put(RawRecordConstants.M117, sect117);
        includeSections.put(RawRecordConstants.M118, sect118);
        includeSections.put(RawRecordConstants.M124, sect124);
        includeSections.put(RawRecordConstants.M125, sect125);
        includeSections.put(RawRecordConstants.M126, sect126);

        final DatabaseConfig databaseConfig = DatabaseConfig.getDefault();

        executeWithProfile(databaseConfig.getCodeProfile("batch"), includeSections);
    }
}
