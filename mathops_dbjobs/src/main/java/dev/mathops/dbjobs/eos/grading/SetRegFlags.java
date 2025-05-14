package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStpaceSummaryLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStpaceSummary;
import dev.mathops.dbjobs.EDebugMode;

import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to set the flags in the STCOURSE registration row to indicate grade and completion status.
 */
public enum SetRegFlags {
    ;

    /**
     * Finds the best passing scores for all proctored exams in a course and calculates their total.
     *
     * @param cache       the data cache
     * @param debugMode   the debug mode
     * @param reg         the course registration for which to construct the student pace summary
     * @param totalPoints the total points scored
     * @param grade       the grade earned
     * @return success or failure
     */
    static ESuccessFailure update(final Cache cache, final EDebugMode debugMode, final RawStcourse reg,
                                  final int totalPoints, final String grade) {

        String newOpenStatus = null;
        String newCompleted = null;
        String newGrade = grade;
        Integer newScore = null;
        boolean deleteStpace = false;

        if ("F".equals(grade)) {
            newOpenStatus = "N";
            newCompleted = "N";
            newScore = Integer.valueOf(totalPoints);
        } else if ("I".equals(grade)) {
            newOpenStatus = "Y";
            newCompleted = "N";
            deleteStpace = true;
        } else if ("A".equals(grade) || "B".equals(grade) || "C".equals(grade) || "D".equals(grade)) {
            newOpenStatus = "N";
            newCompleted = "Y";
            newScore = Integer.valueOf(totalPoints);
        } else if ("U".equals(grade)) {
            newOpenStatus = "N";
            newCompleted = "N";
            newScore = Integer.valueOf(totalPoints);
        } else {
            newOpenStatus = "N";
            newCompleted = "N";
            newGrade = null;
        }

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        if (debugMode == EDebugMode.DEBUG) {
            Log.fine("Updating student course row to set open_status=", newOpenStatus, ", completed=", newCompleted,
                    ", course_grade=", newGrade, ", score=", newScore);
        } else {
            try {
                RawStcourseLogic.updateCompletedScoreGrade(cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                        newCompleted, newScore, newGrade);
                RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                        newOpenStatus, reg.finalClassRoll, reg.lastClassRollDt);
            } catch (final SQLException ex) {
                Log.warning("Failed to update student course row to set open_status=", newOpenStatus, ", completed=",
                        newCompleted, ", course_grade=", newGrade, ", score=", newScore, ex);
                result = ESuccessFailure.FAILURE;
            }
        }

        if (deleteStpace) {
            deleteStPaceSummary(cache, debugMode, reg);
        }

        return result;
    }

    /**
     * Deletes all STPACE_SUMMARY records for a student in a course.
     *
     * @param cache     the data cache
     * @param debugMode the debug mode
     * @param reg       the course registration for which to construct the student pace summary
     * @return success or failure
     */
    static ESuccessFailure deleteStPaceSummary(final Cache cache, final EDebugMode debugMode, final RawStcourse reg) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        if (debugMode == EDebugMode.DEBUG) {
            Log.fine("Deleting STPACE_SUMMARY for ", reg.stuId, " for ", reg.course, " section ", reg.sect, " in ",
                    reg.termKey);
        } else {
            try {
                final List<RawStpaceSummary> rows = RawStpaceSummaryLogic.queryByStudentCourseSectionTerm(cache,
                        reg.stuId, reg.course, reg.sect, reg.termKey);
                for (final RawStpaceSummary row : rows) {
                    RawStpaceSummaryLogic.delete(cache, row);
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to delete STPACE_SUMMARY for ", reg.stuId, " for ", reg.course, " section ",
                        reg.sect, " in ", reg.termKey, ex);
                result = ESuccessFailure.FAILURE;
            }
        }

        return result;
    }
}