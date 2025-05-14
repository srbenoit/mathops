package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.dbjobs.EDebugMode;

import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to clear "passed" flags on student exam and homework rows that have already been applied to a final
 * grade to "remove" them from calculations for future grades.
 */
public enum ResetCoursework {
    ;

    /**
     * Finds the best passing scores for all proctored exams in a course and calculates their total.
     *
     * @param cache     the data cache
     * @param debugMode the debug mode
     * @param reg       the course registration for which to construct the student pace summary
     * @return success or failure
     */
    static ESuccessFailure update(final Cache cache, final EDebugMode debugMode, final RawStcourse reg) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        if (debugMode == EDebugMode.DEBUG) {
            Log.fine("Updating STEXAM to changed passed from 'Y' to 'P' and from 'N' to 'n' for ", reg.stuId, " in ",
                    reg.course);
            Log.fine("Updating STHOMEWORK to changed passed from 'Y' to 'P', 'G' to 'N', others to 'V' for ", reg.stuId,
                    " in ", reg.course);
        } else {
            try {
                final List<RawStexam> exams = RawStexamLogic.queryByStudentCourse(cache, reg.stuId, reg.course, true);

                for (final RawStexam exam : exams) {
                    final String oldPassed = exam.passed;
                    String newPassed = null;

                    if ("Y".equals(oldPassed)) {
                        newPassed = "P";
                    } else if ("N".equals(oldPassed)) {
                        newPassed = "n";
                    }

                    if (newPassed != null) {
                        RawStexamLogic.updatePassed(cache, exam, newPassed);
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to update STEXAM to change passed from 'Y' to 'P' and from 'N' to 'n' for ",
                        reg.stuId, " in ", reg.course);
                result = ESuccessFailure.FAILURE;
            }

            try {
                final List<RawSthomework> hws = RawSthomeworkLogic.queryByStudentCourse(cache, reg.stuId, reg.course,
                        true);

                for (final RawSthomework hw : hws) {
                    final String oldPassed = hw.passed;
                    final String newPassed;

                    if ("Y".equals(oldPassed)) {
                        newPassed = "P";
                    } else if ("N".equals(oldPassed)) {
                        newPassed = "G";
                    } else {
                        newPassed = "V";
                    }

                    RawSthomeworkLogic.updatePassed(cache, hw, newPassed);
                }
            } catch (final SQLException ex) {
                Log.warning(
                        "Failed to update STHOMEWORK to change passed from 'Y' to 'P', 'G' to 'N', others to 'V' for ",
                        reg.stuId, " in ", reg.course);
                result = ESuccessFailure.FAILURE;
            }
        }

        return result;
    }
}