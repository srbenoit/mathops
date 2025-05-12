package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes grades of "Incomplete" as part of the final grading process.
 */
public final class ProcessIncomplete implements Runnable {

    /** Flag to run in "debug" mode which prints changes that would be performed rather than performing any changes. */
    private static final boolean DEBUG = false;

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

            createStpaceSummary(reg);
            final int examTotal = calcExamTotal(reg);
            final int stpaceTotal = calcStpaceTotal(reg);
            final int totalPoints = examTotal + stpaceTotal;
            final String grade = findCourseGrade(reg, examTotal, stpaceTotal);

            if ("A".equals(grade)) {
//            CALL set_stc_flags(total_pts,course_grv)
//            CALL reset_coursework()
//
//            SELECT last_name,first_name INTO lname,fname FROM student
//               WHERE student.stu_id = stc_rec.stu_id
//            LET rpt_line = "INC processed for:   ",stc_rec.stu_id,"     ",
//              lname CLIPPED,", ",fname CLIPPED
//            OUTPUT TO REPORT incomp_rpt(rpt_line)
//            LET rpt_line= stc_rec.stu_id,"   ",stc_rec.course,
//               "   section: ",stc_rec.sect," i_term: ",stc_rec.i_term CLIPPED,
//               " ", stc_rec.i_term_yr USING "<<","   GRADE: ",course_grv,
//               " POINTS: ", total_pts USING "<<<","\n\n"
//            OUTPUT TO REPORT incomp_rpt(rpt_line)
            } else if (reg.iDeadlineDt == null) {
                Log.warning("Incomplete in ", reg.course, " for ", reg.stuId, " with no Inc deadline date.");
            } else if (reg.iDeadlineDt.isBefore(today)) {
//            CALL set_stc_flags(total_pts,course_grv)
//            CALL reset_coursework()
//
//            SELECT last_name,first_name INTO lname,fname FROM student
//               WHERE student.stu_id = stc_rec.stu_id
//            LET rpt_line = "INC processed for:   ",stc_rec.stu_id,"     ",
//              lname CLIPPED,", ",fname CLIPPED
//            OUTPUT TO REPORT incomp_rpt(rpt_line)
//            LET rpt_line= stc_rec.stu_id,"   ",stc_rec.course,
//               "   section: ",stc_rec.sect," i_term: ",stc_rec.i_term CLIPPED,
//               " ", stc_rec.i_term_yr USING "<<","   GRADE: ",course_grv,
//               " POINTS: ", total_pts USING "<<<","\n\n"
//            OUTPUT TO REPORT incomp_rpt(rpt_line)
            } else {
//            DELETE FROM stpace_summary
//               WHERE stpace_summary.stu_id = stc_rec.stu_id
//                 AND stpace_summary.course = stc_rec.course
//                 AND stpace_summary.sect = stc_rec.sect
//                 AND stpace_summary.term = stc_rec.term
//                 AND stpace_summary.term_yr = stc_rec.term_yr
//                 AND stpace_summary.i_in_progress = "Y"
            }

//            CALL final_stats()
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
