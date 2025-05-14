package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawCusectionLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStpaceSummaryLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStpaceSummary;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.dbjobs.EDebugMode;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to calculate and insert the "STPACE_SUMMARY" record for a student.  This is used for incomplete
 * processing and for final grading.
 */
public enum StudentPaceSummary {
    ;

    /**
     * Calculates the number of points earned from passing Review Exams by specified deadline dates and populates
     * STPACE_SUMMARY table to give intermediate grading information and help with grade appeals.
     *
     * @param cache     the data cache
     * @param debugMode the debug mode
     * @param active    the active term
     * @param reg       the course registration for which to construct the student pace summary
     * @return Success or failure
     */
    static ESuccessFailure createStudentPaceSummary(final Cache cache, final EDebugMode debugMode,
                                                    final TermRec active, final RawStcourse reg) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        final String paceTrack = determinePaceTrack(cache, reg);

        if (paceTrack == null) {
            result = ESuccessFailure.FAILURE;
        } else {
            // The following returns -1 if an error occurs, 0 if "reg" is not counted toward pace, or the pace.
            final int pace = determinePace(cache, active.term, reg);
            if (pace < 0) {
                result = ESuccessFailure.FAILURE;
            } else if (pace > 0) {
                final Integer paceOrder = reg.paceOrder;

                if (paceOrder != null) {
                    result = buildStudentPaceSummary(cache, debugMode, reg, pace, paceTrack);
                }
            }
        }

        return result;
    }

    /**
     * Determines the student's pace track.
     *
     * @param cache the data cache
     * @param reg   the student registration
     * @return the pace track
     */
    private static String determinePaceTrack(final Cache cache, final RawStcourse reg) {

        String paceTrack = null;

        try {
            if ("Y".equals(reg.iInProgress)) {
                final RawStterm studentTerm = RawSttermLogic.query(cache, reg.iTermKey, reg.stuId);
                if (studentTerm != null) {
                    paceTrack = studentTerm.paceTrack;
                }
            } else {
                final RawStterm studentTerm = RawSttermLogic.query(cache, reg.termKey, reg.stuId);
                if (studentTerm != null) {
                    paceTrack = studentTerm.paceTrack;
                }

                if (paceTrack == null) {
                    final RawStudent student = RawStudentLogic.query(cache, reg.stuId, false);
                    if (student != null) {
                        final RawPacingStructure pacing = RawPacingStructureLogic.query(cache, reg.termKey,
                                student.pacingStructure);
                        paceTrack = pacing.defPaceTrack;
                    }
                }
            }

            if (paceTrack == null) {
                Log.warning("Unable to determine pace track for ", reg.stuId, " for ", reg.course);
            } else if (paceTrack.length() > 1) {
                paceTrack = paceTrack.substring(0, 1);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to create STPACE_SUMMARY for student ", reg.stuId, " for course ", reg.course, ex);
        }

        return paceTrack;
    }

    /**
     * Determines the student's pace based on the number of registrations.
     *
     * @param cache  the data cache
     * @param active the active term key
     * @param reg    the student registration
     * @return the pace, -1 if an error occurred, 0 if the registration row provided is NOT in a section that enforces
     *         review exam deadlines
     */
    private static int determinePace(final Cache cache, final TermKey active, final RawStcourse reg) {

        boolean error = false;

        // Identify all pacing structures that have due dates on Review exams (this job will only consider registrations
        // in sections that have one of these pacing structures)

        final List<String> structuresThatCountReviewExams = new ArrayList<>(3);

        try {
            final List<RawPacingStructure> termPacing = RawPacingStructureLogic.queryByTerm(cache, reg.termKey);
            for (final RawPacingStructure pacing : termPacing) {
                if ("Y".equals(pacing.reDueDateEnforced)) {
                    structuresThatCountReviewExams.add(pacing.pacingStructure);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to identify pacing structures that enforce review exam deadlines", ex);
            error = true;
        }

        // Gather the list of course sections whose pacing structure is one of those identified above
        final List<RawCsection> reviewEnforcingSections = new ArrayList<>(10);
        try {
            final List<RawCsection> sections = RawCsectionLogic.queryByTerm(cache, active);
            for (final RawCsection section : sections) {
                if (structuresThatCountReviewExams.contains(section.pacingStructure)) {
                    reviewEnforcingSections.add(section);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to identify sections that enforce review exam deadlines", ex);
            error = true;
        }

        // Count current-term registrations in any sections that have one of these pacing structures, and incomplete
        // prior-term registrations that are "counted in pace"
        final List<RawStcourse> currentTermRegs = new ArrayList<>(5);
        final List<RawStcourse> priorTermRegs = new ArrayList<>(5);
        final int totalCourses;

        boolean regIsEnforced = false;

        for (final RawCsection section : reviewEnforcingSections) {
            if (reg.course.equals(section.course) && reg.sect.equals(section.sect)) {
                regIsEnforced = true;
                break;
            }
        }

        if (regIsEnforced) {
            try {
                final List<RawStcourse> stuRegs = RawStcourseLogic.getActiveForStudent(cache, reg.stuId, active);

                for (final RawStcourse test : stuRegs) {
                    final String openStatus = test.openStatus;
                    // Skip dropped and ignored registrations, and OT courses
                    if ("G".equals(openStatus) || "D".equals(openStatus) || "OT".equals(test.instrnType)) {
                        continue;
                    }

                    if (test.iTermKey == null) {
                        // Not an Incomplete - count as a "current term" reg. if in a relevant section
                        for (final RawCsection section : reviewEnforcingSections) {
                            if (test.course.equals(section.course) && test.sect.equals(section.sect)) {
                                currentTermRegs.add(test);
                                break;
                            }
                        }
                    } else if ("Y".equals(test.iCounted)) {
                        // An Incomplete from a prior term; count as a "prior term" reg. if in a relevant section
                        // NOTE: "counted" incompleted use active-term settings from CSECTION, not settings from the
                        //       term when the incomplete was started.
                        for (final RawCsection section : reviewEnforcingSections) {
                            if (test.course.equals(section.course) && test.sect.equals(section.sect)) {
                                priorTermRegs.add(test);
                                break;
                            }
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to count current-term registration for student ", reg.stuId, ex);
                error = true;
            }
            totalCourses = currentTermRegs.size() + priorTermRegs.size();

            try {
                final RawStterm studentTerm = RawSttermLogic.query(cache, active, reg.stuId);
                if (studentTerm != null) {
                    final Integer pace = studentTerm.pace;
                    if (pace != null && totalCourses != pace.intValue()) {
                        final String totalStr = Integer.toString(totalCourses);
                        Log.warning("Pace mismatch for student ", reg.stuId, ": calculated pace: ", totalStr,
                                ", STTERM value: ", pace);
                        error = true;
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to count current-term registration for student ", reg.stuId, ex);
                error = true;
            }
        } else {
            totalCourses = -1;
        }

        return error ? -1 : totalCourses;
    }

    /**
     * Computes and inserts the student pace summary record for a student.
     *
     * @param cache     the data cache
     * @param debugMode the debug mode
     * @param reg       the course registration
     * @param pace      the total number of in-pace registrations
     * @param paceTrack the student's pace track
     * @return Success of failure
     */
    private static ESuccessFailure buildStudentPaceSummary(final Cache cache, final EDebugMode debugMode,
                                                           final RawStcourse reg, final int pace,
                                                           final String paceTrack) {

        final List<RawStexam> firstPassedReviews = new ArrayList<>(5);
        ESuccessFailure result = gatherFirstPassedReviews(cache, reg, firstPassedReviews);

        if (result == ESuccessFailure.SUCCESS && !firstPassedReviews.isEmpty()) {

            for (final RawStexam exam : firstPassedReviews) {
                // For each unit, find point values for the RE exam and determine if it was completed on time
                final Integer rePointsOnTime;
                if ("Y".equals(reg.iInProgress) && reg.iTermKey != null) {
                    rePointsOnTime = getReviewExamPointsOnTime(cache, reg, exam, reg.iTermKey);
                } else {
                    rePointsOnTime = getReviewExamPointsOnTime(cache, reg, exam, reg.termKey);
                }
                result = buildAndInsertStPaceSummary(cache, debugMode, reg, exam, pace, paceTrack, rePointsOnTime);
            }
        }

        return result;
    }

    /**
     * Collects all review exams in a course that are marked as "the first passed".
     *
     * @param cache  the data cache
     * @param reg    the registration
     * @param target the collection to which to add all matching exams found (at the conclusion of this method, this
     *               list is sorted by course, then unit, then exam date/time
     * @return the list of review exams with the "first passed" flag set
     */
    private static ESuccessFailure gatherFirstPassedReviews(final Cache cache, final RawStcourse reg,
                                                            final List<RawStexam> target) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        try {
            final List<RawStexam> allExams = RawStexamLogic.queryByStudentCourse(cache, reg.stuId, reg.course, false);

            for (final RawStexam exam : allExams) {
                if ("Y".equals(exam.passed) && "Y".equals(exam.isFirstPassed) && exam.version.endsWith("RE")) {
                    final String type = exam.examType;
                    if ("R".equals(type) || "RE".equals(type) || "UR".equals(type) || "MR".equals(type)
                        || "FR".equals(type) || "SR".equals(type)) {
                        target.add(exam);
                    }
                }
            }

            target.sort(new RawStexam.CourseUnitComparator());
        } catch (final SQLException ex) {
            Log.warning("Failed to count first-passing review exams for ", reg.stuId, " in ", reg.course, ex);
            result = ESuccessFailure.FAILURE;
        }

        return result;
    }

    /**
     * Attempts to query for the number of points awarded for an on-time review exam.
     *
     * @param cache the data cache
     * @param reg   the student registration
     * @param exam  the student exam
     * @return the number of points; null if the value could not be determined
     */
    private static Integer getReviewExamPointsOnTime(final Cache cache, final RawStcourse reg, final RawStexam exam,
                                                     final TermKey term) {

        Integer result = null;

        try {
            final List<RawCusection> cusections = RawCusectionLogic.queryByTerm(cache, term);

            for (final RawCusection cusection : cusections) {
                if (cusection.course.equals(reg.course) && cusection.sect.equals(reg.sect)
                    && cusection.unit.equals(exam.unit)) {
                    result = cusection.rePointsOntime;
                    break;
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to determine points for on-time review exam for ", reg.stuId, " in ", reg.course, ex);
        }

        return result;
    }

    /**
     * Builds and inserts the STPACE_SUMMARY record.  This computes the milestone number for the exam, then looks up the
     * original due date and then checks for an override due date for the student.
     *
     * @param cache     the data cache
     * @param debugMode the debug mode
     * @param reg       the registration
     * @param exam      the student exam
     * @param pace      the total number of in-pace registrations
     * @param paceTrack the student's pace track
     * @return success of failure
     */
    private static ESuccessFailure buildAndInsertStPaceSummary(final Cache cache, final EDebugMode debugMode,
                                                               final RawStcourse reg, final RawStexam exam,
                                                               final int pace, final String paceTrack,
                                                               final Integer rePointsOnTime) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;
        int milestoneNumber = -1;

        final int paceOrder = reg.paceOrder.intValue();
        final int unit = exam.unit.intValue();
        int effectivePace = pace;
        String effectivePaceTrack = paceTrack;

        if ("Y".equals(reg.iInProgress) && reg.iTermKey != null) {
            if ("N".equals(reg.iCounted)) {
                // Use the pace from the term when the student started the course
                try {
                    final RawStterm studentTerm = RawSttermLogic.query(cache, reg.iTermKey, reg.stuId);
                    if (studentTerm == null || studentTerm.pace == null) {
                        Log.warning("No student term record found for ", reg.stuId, " in ", reg.iTermKey);
                        result = ESuccessFailure.FAILURE;
                    } else {
                        effectivePace = studentTerm.pace.intValue();
                        effectivePaceTrack = studentTerm.paceTrack;
                        milestoneNumber = (effectivePace * 100) + (paceOrder * 10) + unit;
                    }
                } catch (final SQLException ex) {
                    Log.warning("Failed to query student term record for ", reg.stuId, " in ", reg.iTermKey, ex);
                    result = ESuccessFailure.FAILURE;
                }
            } else {
                // "Counted" incomplete, so use the pace from the current term
                milestoneNumber = (effectivePace * 100) + (paceOrder * 10) + unit;
            }
        } else {
            milestoneNumber = (effectivePace * 100) + (paceOrder * 10) + unit;
        }

        LocalDate dueDate = null;
        String newDueDate = null;

        if (milestoneNumber > 0) {

            final TermKey effectiveTerm;
            if ("Y".equals(reg.iInProgress) && reg.iTermKey != null) {
                if ("N".equals(reg.iCounted)) {
                    effectiveTerm = reg.iTermKey;
                } else {
                    effectiveTerm = reg.termKey;
                }
            } else {
                effectiveTerm = reg.termKey;
            }

            try {
                final List<RawMilestone> allMilestones = RawMilestoneLogic.getAllMilestones(cache, effectiveTerm,
                        effectivePace, effectivePaceTrack);

                for (final RawMilestone ms : allMilestones) {
                    if (ms.msNbr.intValue() == milestoneNumber && "RE".equals(ms.msType)) {
                        dueDate = ms.msDate;
                        break;
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query original milestones for pace ", effectivePace, "track ",
                        effectivePaceTrack, " in ", effectiveTerm, ex);
                result = ESuccessFailure.FAILURE;
            }

            if (dueDate != null) {
                // Check for a due date override
                try {
                    final List<RawStmilestone> allStMilestones = RawStmilestoneLogic.getStudentMilestones(cache,
                            effectiveTerm, effectivePaceTrack, reg.stuId);

                    for (final RawStmilestone stms : allStMilestones) {
                        if (stms.msNbr.intValue() == milestoneNumber && "RE".equals(stms.msType)) {
                            if (stms.msDate.isAfter(dueDate)) {
                                dueDate = stms.msDate;
                                newDueDate = "Y";
                                // Don't break - take the greatest override date in case there are multiple
                            }
                        }
                    }
                } catch (final SQLException ex) {
                    Log.warning("Failed to query student milestones for student ", reg.stuId, " in pace ",
                            effectivePace, "track ", effectivePaceTrack, " in ", effectiveTerm, ex);
                    result = ESuccessFailure.FAILURE;
                }
            }

            if (dueDate == null) {
                Log.warning("Unable to determine due date for student ", reg.stuId, " in pace ", effectivePace,
                        "track ", effectivePaceTrack, " in ", effectiveTerm);
                result = ESuccessFailure.FAILURE;
            } else {
                final Integer rePoints;
                if (exam.examDt.isAfter(dueDate)) {
                    rePoints = Integer.valueOf(0);
                } else {
                    rePoints = rePointsOnTime;
                }

                final RawStpaceSummary newRec = new RawStpaceSummary(reg.termKey, reg.stuId, reg.course, reg.sect,
                        reg.iInProgress, effectivePace, effectivePaceTrack, paceOrder, milestoneNumber, exam.unit,
                        dueDate, newDueDate, exam.examDt, rePoints);

                if (debugMode == EDebugMode.DEBUG) {
                    Log.fine("Inserting: ", newRec);
                } else {
                    try {
                        RawStpaceSummaryLogic.delete(cache, newRec);
                        RawStpaceSummaryLogic.insert(cache, newRec);
                    } catch (final SQLException ex) {
                        Log.warning("Failed to insert STPACE_SUMMARY for student ", reg.stuId, " in pace ",
                                effectivePace, "track ", effectivePaceTrack, " in ", effectiveTerm, ex);
                        result = ESuccessFailure.FAILURE;
                    }
                }
            }
        }

        return result;
    }
}
