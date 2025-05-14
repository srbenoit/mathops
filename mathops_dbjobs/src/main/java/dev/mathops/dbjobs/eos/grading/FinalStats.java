package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.type.TermKey;
import dev.mathops.dbjobs.EDebugMode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A utility class to calculate and print final statistics after a run of a grading process.
 */
public enum FinalStats {
    ;

    /**
     * Prints final statistics for Incompletes.
     *
     * @param cache      the data cache
     * @param activeTerm the active term
     */
    static void calculateForIncompletes(final Cache cache, final TermKey activeTerm) {

        Log.info("Ending statistics for Incompletes....");

        try {
            final Collection<String> studentIds = new HashSet<>(1000);
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, activeTerm, false, false);

            int numIncRegs = 0;
            int numCompleted = 0;
            int numCounted = 0;
            for (final RawStcourse reg : allRegs) {
                if ("D".equals(reg.openStatus)) {
                    continue;
                }
                if ("Y".equals(reg.iInProgress) && "Y".equals(reg.finalClassRoll)) {
                    final String course = reg.course;
                    if ("M 117".equals(course) || "M 118".equals(course) || "M 124".equals(course)
                        || "M 125".equals(course) || "M 126".equals(course)) {
                        studentIds.add(reg.stuId);
                        ++numIncRegs;

                        if ("Y".equals(reg.completed)) {
                            ++numCompleted;
                        }
                        if ("Y".equals(reg.iCounted)) {
                            ++numCounted;
                        }
                    }
                }
            }

            final int numIncStudents = studentIds.size();
            final String numIncStudentsStr = Integer.toString(numIncStudents);
            Log.info("  Number of INC students remaining: ", numIncStudentsStr);

            final String numIncRegsStr = Integer.toString(numIncRegs);
            Log.info("  Number of INC registrations remaining: ", numIncRegsStr);

            final String numCompletedStr = Integer.toString(numCompleted);
            Log.info("  Number remaining INC registrations with 'completed' flag set: ", numCompletedStr);

            final String numCountedStr = Integer.toString(numCounted);
            Log.info("  Number remaining INC registrations with counted in pace: ", numCountedStr);
        } catch (final SQLException ex) {
            Log.warning("Failed to query for student course records for the active term.");
        }
    }

    /**
     * Prints final statistics for current-term registrations.  This method also updates any rows found with
     * "open_status = 'G'" to have open_status = 'N', completed = 'N', course_grade = 'U' and score = NULL.
     *
     * @param cache      the data cache
     * @param debugMode  the debug mode
     * @param activeTerm the active term
     * @return success or failure
     */
    static ESuccessFailure calculateForFinalGrades(final Cache cache, final EDebugMode debugMode,
                                                   final TermKey activeTerm) {

        ESuccessFailure result;

        Log.info("Ending statistics for Final Grades....");

        try {
            final Collection<String> nonMptStudentIds = new HashSet<>(1000);
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, activeTerm, false, false);
            final List<RawStcourse> gRegs = new ArrayList<>(50);

            int numNonMptRegs = 0;
            int numMptRegs = 0;
            int numNonMptCompleted = 0;
            int numMptCompleted = 0;
            int numIgnored = 0;
            for (final RawStcourse reg : allRegs) {
                if ("D".equals(reg.openStatus) || "Y".equals(reg.iInProgress)) {
                    continue;
                }

                if ("Y".equals(reg.finalClassRoll)) {
                    final String course = reg.course;
                    if ("M 117".equals(course) || "M 118".equals(course) || "M 124".equals(course)
                        || "M 125".equals(course) || "M 126".equals(course)) {

                        if ("550".equals(reg.sect)) {
                            ++numMptRegs;
                            if ("Y".equals(reg.completed)) {
                                ++numMptCompleted;
                            }
                        } else if ("G".equals(reg.openStatus)) {
                            gRegs.add(reg);
                            ++numIgnored;
                        } else {
                            nonMptStudentIds.add(reg.stuId);
                            ++numNonMptRegs;
                            if ("Y".equals(reg.completed)) {
                                ++numNonMptCompleted;
                            }
                        }
                    }
                }
            }

            final int numNonMptStudents = nonMptStudentIds.size();
            final String numNonMptStudentsStr = Integer.toString(numNonMptStudents);
            Log.info("  Number of non-INC, non-MPT students: ", numNonMptStudentsStr);

            final String numNonMptRegsStr = Integer.toString(numNonMptRegs);
            Log.info("  Number of non-INC, non-MPT registrations: ", numNonMptRegsStr);

            final String numIgnoredStr = Integer.toString(numIgnored);
            Log.info("  Number of non-INC, non-MPT 'ignored' registrations: ", numIgnoredStr);

            final String numMptRegsStr = Integer.toString(numMptRegs);
            Log.info("  Number of MPT (550) registrations: ", numMptRegsStr);

            final String numCompletedStr = Integer.toString(numNonMptCompleted);
            Log.info("  Number of non-MPT with 'completed' flag set: ", numCompletedStr);

            final String numMptCompletedStr = Integer.toString(numMptCompleted);
            Log.info("  Number of MPT (550) with 'completed' flag set: ", numMptCompletedStr);

            result = setGFlags(cache, debugMode, gRegs, activeTerm);
        } catch (final SQLException ex) {
            Log.warning("Failed to query for student course records for the active term.");
            result = ESuccessFailure.FAILURE;
        }

        return result;
    }

    /**
     * Updates registration records that have "open_status = 'G'" to have open_status = 'N', completed = 'N',
     * course_grade = 'U' and score = NULL.
     *
     * @param cache      the data cache
     * @param debugMode  the debug mode
     * @param gRegs      the list of all registrations found with open_status = 'G'
     * @param activeTerm the active term key
     * @return success or failure
     */
    private static ESuccessFailure setGFlags(final Cache cache, final EDebugMode debugMode,
                                             final Iterable<RawStcourse> gRegs, final TermKey activeTerm) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        try {
            for (final RawStcourse reg : gRegs) {
                if (debugMode == EDebugMode.DEBUG) {
                    Log.fine("Updating registration for ", reg.stuId, " in ", reg.course, ", section ", reg.sect,
                            " from open_status='G' to open_status='N', completed='N', course_grade='U', score=NULL");
                } else {
                    RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, reg.stuId, reg.course,
                            reg.sect, reg.termKey, "N", "Y", reg.lastClassRollDt);
                    RawStcourseLogic.updateCompletedScoreGrade(cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                            "N", null, "U");
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to update registration rows with open_status = 'G'.", ex);
            result = ESuccessFailure.FAILURE;
        }

        try {
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, activeTerm, false, false);

            int numIgnored = 0;
            for (final RawStcourse reg : allRegs) {
                if ("D".equals(reg.openStatus) || "Y".equals(reg.iInProgress) || "550".equals(reg.sect)) {
                    continue;
                }

                if ("Y".equals(reg.finalClassRoll)) {
                    final String course = reg.course;
                    if ("M 117".equals(course) || "M 118".equals(course) || "M 124".equals(course)
                        || "M 125".equals(course) || "M 126".equals(course)) {

                        if ("G".equals(reg.openStatus)) {
                            ++numIgnored;
                        }
                    }
                }
            }

            final String numIgnoredStr = Integer.toString(numIgnored);
            Log.info("  Recount of non-INC, non-MPT 'ignored' registrations: ", numIgnoredStr);
        } catch (final SQLException ex) {
            Log.warning("Failed to requery for student course records for the active term.");
            result = ESuccessFailure.FAILURE;
        }

        return result;
    }
}
