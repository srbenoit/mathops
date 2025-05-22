package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.dbjobs.EDebugMode;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The final grading process.
 */
public class ProcessFinalGrades implements Runnable {

    /** Flag to run in "debug" mode which prints changes that would be performed rather than performing any changes. */
    private static final EDebugMode DEBUG_MODE = EDebugMode.DEBUG;

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code ProcessFinalGrades}.
     *
     * @param theCache the data cache
     */
    private ProcessFinalGrades(final Cache theCache) {

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
                InitialStats.calculateForFinalGrades(this.cache, active.term);

                final List<RawStcourse> regs = getNormalRegs(active);
                final int count = regs.size();

                if (count > 0) {
                    processRegs(active, regs);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query the active term.", ex);
        }
    }

    /**
     * Loads all registration records for the current term that are not "Incomplete" in a relevant course and not
     * dropped where the student is on the final class roll.
     *
     * @param active the active term record
     * @return the list of Incomplete records
     */
    private List<RawStcourse> getNormalRegs(final TermRec active) {

        // Find all registration records with:
        //  i_in_progress != 'Y'
        //  open_status is NULL or not 'D'
        //  final_class_roll = 'Y'
        //  in a 1-credit Precalculus course
        final List<RawStcourse> incRegs = new ArrayList<>(100);

        try {
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(this.cache, active.term, false, false);
            for (final RawStcourse reg : allRegs) {
                final String iInProgress = reg.iInProgress;
                final String openStatus = reg.openStatus;
                if ("Y".equals(iInProgress) || "D".equals(openStatus) || "G".equals(openStatus)) {
                    continue;
                }

                final String course = reg.course;
                final String finalClassRoll = reg.finalClassRoll;

                if (("M 117".equals(course) || "M 118".equals(course) || "M 124".equals(course)
                     || "M 125".equals(course) || "M 126".equals(course))
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
    private void processRegs(final TermRec active, final Iterable<RawStcourse> incRegs) {

        final LocalDate today = LocalDate.now();

        int numEligible = 0;

        for (final RawStcourse reg : incRegs) {
            if ("550".equals(reg.sect)) {
                final boolean eligible = processChallengeCreditReg(reg);
                if (eligible) {
                    ++numEligible;
                }
            } else if (StudentPaceSummary.createStudentPaceSummary(this.cache, DEBUG_MODE, active, reg)
                       == ESuccessFailure.SUCCESS) {
                final Integer examTotal = CalcExamTotal.calculate(this.cache, reg);
                final Integer reviewTotal = CalcStPaceTotal.calculate(this.cache, reg);

                if (examTotal != null && reviewTotal != null) {
                    final int totalPoints = examTotal.intValue() + reviewTotal.intValue();
                    final String grade = FindCourseGrade.calculate(this.cache, reg, totalPoints);
                    SetRegFlags.update(this.cache, DEBUG_MODE, reg, totalPoints, grade);
                }
            }
        }

        final String numEligibleStr = Integer.toString(numEligible);
        Log.info("Total of confirmed challenge credit registration rows: ", numEligibleStr);
    }

    /**
     * Processes a placement credit registration.
     *
     * @param reg the registration
     * @return true if the student was eligible for challenge credit; false if not
     */
    private boolean processChallengeCreditReg(final RawStcourse reg) {

        boolean eligible = false;

        try {
            final List<RawMpeCredit> mpeCredit = RawMpeCreditLogic.queryByStudent(this.cache, reg.stuId);
            int count = 0;
            for (final RawMpeCredit row : mpeCredit) {
                if (row.course.equals(reg.course) && "C".equals(row.examPlaced)) {
                    ++count;
                }
            }

            if (count > 0) {
                if (DEBUG_MODE == EDebugMode.DEBUG) {
                    Log.info("Updating student course row for ", reg.stuId, " in ", reg.course, " section ", reg.sect,
                            " to have open_status='N', completed='Y', grade='S', exam_placed='M'");
                } else {
                    // TODO: Make a new update call that aggregates these...
                    RawStcourseLogic.updateOpenStatusAndFinalClassRoll(this.cache, reg.stuId, reg.course, reg.sect,
                            reg.termKey, "N", reg.finalClassRoll, reg.lastClassRollDt);
                    RawStcourseLogic.updateCompletedScoreGrade(this.cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                            "Y", null, "S");
                    RawStcourseLogic.updateExamPlaced(this.cache, reg.stuId, reg.course, reg.sect, reg.termKey, "M");
                }
                eligible = true;
            } else {
                if (DEBUG_MODE == EDebugMode.DEBUG) {
                    Log.info("Updating student course row for ", reg.stuId, " in ", reg.course, " section ", reg.sect,
                            " to have open_status='N', completed='N', grade='U'");
                } else {
                    // TODO: Make a new update call that aggregates these...
                    RawStcourseLogic.updateOpenStatusAndFinalClassRoll(this.cache, reg.stuId, reg.course, reg.sect,
                            reg.termKey, "N", reg.finalClassRoll, reg.lastClassRollDt);
                    RawStcourseLogic.updateCompletedScoreGrade(this.cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                            "N", null, "U");
                }

                Log.warning("** WARNING: MPE credit not okay for ", reg.stuId, ", ", reg.course);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query for challenge credit rows.", ex);
        }

        return eligible;
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

        final Runnable obj = new ProcessFinalGrades(cache);
        obj.run();
    }
}