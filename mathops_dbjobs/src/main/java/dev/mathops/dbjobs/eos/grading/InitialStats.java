package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A utility class to calculate and print initial statistics before a run of a grading process.
 */
enum InitialStats {
    ;

    /**
     * Prints initial statistics for Incompletes.
     *
     * @param cache      the data cache
     * @param activeTerm the active term
     */
    static void calculateForIncompletes(final Cache cache, final TermKey activeTerm) {

        Log.info("Starting statistics for Incompletes....");

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
            Log.info("  Number of INC students: ", numIncStudentsStr);

            final String numIncRegsStr = Integer.toString(numIncRegs);
            Log.info("  Number of INC registrations: ", numIncRegsStr);

            final String numCompletedStr = Integer.toString(numCompleted);
            Log.info("  Number with 'completed' flag set: ", numCompletedStr);

            final String numCountedStr = Integer.toString(numCounted);
            Log.info("  Number counted in pace: ", numCountedStr);
        } catch (final SQLException ex) {
            Log.warning("Failed to query for student course records for the active term.");
        }
    }

    /**
     * Prints initial statistics for current-term registrations.
     *
     * @param cache      the data cache
     * @param activeTerm the active term
     */
    static void calculateForFinalGrades(final Cache cache, final TermKey activeTerm) {

        Log.info("Starting statistics for Final Grades....");

        try {
            final Collection<String> nonMptStudentIds = new HashSet<>(1000);
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, activeTerm, false, false);

            int numNonMptRegs = 0;
            int numMptRegs = 0;
            int numNonMptCompleted = 0;
            int numMptCompleted = 0;
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

            final String numMptRegsStr = Integer.toString(numMptRegs);
            Log.info("  Number of MPT (550) registrations: ", numMptRegsStr);

            final String numCompletedStr = Integer.toString(numNonMptCompleted);
            Log.info("  Number of non-MPT with 'completed' flag set: ", numCompletedStr);

            final String numMptCompletedStr = Integer.toString(numMptCompleted);
            Log.info("  Number of MPT (550) with 'completed' flag set: ", numMptCompletedStr);
        } catch (final SQLException ex) {
            Log.warning("Failed to query for student course records for the active term.");
        }
    }
}
