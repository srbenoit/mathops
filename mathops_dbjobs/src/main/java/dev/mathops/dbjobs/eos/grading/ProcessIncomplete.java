package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.dbjobs.EDebugMode;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes grades of "Incomplete" as part of the final grading process.
 */
public final class ProcessIncomplete implements Runnable {

    /** Flag to run in "debug" mode which prints changes that would be performed rather than performing any changes. */
    private static final EDebugMode DEBUG_MODE = EDebugMode.DEBUG;

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code ProcessIncomplete}.
     *
     * @param theCache the data cache
     */
    private ProcessIncomplete(final Cache theCache) {

        this.cache = theCache;
    }

    /**
     * Runs the process.
     */
    public void run() {

        try {
            final TermRec active = TermLogic.get(this.cache).queryActive(this.cache);

            if (active == null) {
                Log.warning("Unable to query the active term");
            } else {
                // Display initial statistics
                InitialStats.calculateForIncompletes(this.cache, active.term);

                final List<RawStcourse> incRegs = getIncompleteRegs(active);
                final int count = incRegs.size();

                if (count > 0) {
                    processIncompleteRows(active, incRegs);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query the active term.", ex);
        }
    }

    /**
     * Loads all registration records for the current term that are "Incomplete" in a relevant course and not dropped
     * where the student is on the final class roll.
     *
     * @param active the active term record
     * @return the list of Incomplete records
     */
    private List<RawStcourse> getIncompleteRegs(final TermRec active) {

        // Find all registration records with:
        //  i_in_progress = 'Y'
        //  open_status is NULL or not 'D'
        //  final_class_roll = 'Y'
        //  in a 1-credit Precalculus course
        final List<RawStcourse> incRegs = new ArrayList<>(100);

        try {
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(this.cache, active.term, false, false);
            for (final RawStcourse reg : allRegs) {
                final String course = reg.course;
                final String openStatus = reg.openStatus;
                final String finalClassRoll = reg.finalClassRoll;
                final String iInProgress = reg.iInProgress;

                if ("Y".equals(iInProgress)
                    && ("M 117".equals(course) || "M 118".equals(course) || "M 124".equals(course)
                        || "M 125".equals(course) || "M 126".equals(course))
                    && (!"D".equals(openStatus))
                    && "Y".equals(finalClassRoll)) {
                    incRegs.add(reg);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to scan for Incomplete course registrations.", ex);
        }

        final int count = incRegs.size();
        final String countStr = Integer.toString(count);
        Log.info("Found ", countStr, " Incomplete registrations to process...");

        return incRegs;
    }

    /**
     * Does something impactful.
     *
     * @param active  the active term record
     * @param incRegs the list of "Incomplete" registration records to process
     */
    private void processIncompleteRows(final TermRec active, final Iterable<RawStcourse> incRegs) {

        final LocalDate today = LocalDate.now();

        for (final RawStcourse reg : incRegs) {
            ESuccessFailure result = StudentPaceSummary.createStudentPaceSummary(this.cache, DEBUG_MODE, active, reg);

            if (result == ESuccessFailure.SUCCESS) {
                final Integer examTotal = CalcExamTotal.calculate(this.cache, reg);
                final Integer reviewTotal = CalcStPaceTotal.calculate(this.cache, reg);
                if (examTotal != null && reviewTotal != null) {
                    final int totalPoints = examTotal.intValue() + reviewTotal.intValue();
                    final String grade = FindCourseGrade.calculate(this.cache, reg, totalPoints);

                    if ("A".equals(grade)) {
                        // Student earned an A grade - close out the Incomplete
                        SetRegFlags.update(this.cache, DEBUG_MODE, reg, totalPoints, grade);
                        ResetCoursework.update(this.cache, DEBUG_MODE, reg);

                        try {
                            final RawStudent stu = RawStudentLogic.query(this.cache, reg.stuId, false);
                            final String name = stu.getScreenName();
                            Log.info("Incomplete processed for ", reg.stuId, "  ", name, ":");
                            Log.info("  Course=", reg.course, ", sect=", reg.sect, ", iTerm=", reg.iTermKey,
                                    " grade=", reg.courseGrade, ", points=", reg.score);
                        } catch (final SQLException ex) {
                            Log.warning("Failed to query student record for ", reg.stuId, ex);
                        }
                    } else if (reg.iDeadlineDt == null) {
                        Log.warning("Incomplete in ", reg.course, " for ", reg.stuId, " with no Inc deadline date.");
                    } else if (reg.iDeadlineDt.isBefore(today)) {
                        // "A" grade was not earned but deadline has passed so close out the Incomplete
                        SetRegFlags.update(this.cache, DEBUG_MODE, reg, totalPoints, grade);
                        ResetCoursework.update(this.cache, DEBUG_MODE, reg);

                        try {
                            final RawStudent stu = RawStudentLogic.query(this.cache, reg.stuId, false);
                            final String name = stu.getScreenName();
                            Log.info("Incomplete processed for ", reg.stuId, "  ", name, ":");
                            Log.info("  Course=", reg.course, ", sect=", reg.sect, ", iTerm=", reg.iTermKey,
                                    " grade=", reg.courseGrade, ", points=", reg.score);
                        } catch (final SQLException ex) {
                            Log.warning("Failed to query student record for ", reg.stuId, ex);
                        }
                    } else {
                        SetRegFlags.deleteStPaceSummary(this.cache, DEBUG_MODE, reg);
                    }

                    FinalStats.calculateForIncompletes(this.cache, active.term);
                }
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

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        final Runnable obj = new ProcessIncomplete(cache);
        obj.run();
    }
}
