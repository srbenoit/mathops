package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility class to calculate the "STPACE_SUMMARY" record for a student.  This is used for incomplete processing and
 * for final grading.
 */
public enum StudentPaceSummary {
    ;

    /**
     * Calculates the number of points earned from passing Review Exams by specified deadline dates and populates
     * STPACE_SUMMARY table to give intermediate grading information and help with grade appeals.
     *
     * @param active the active term
     * @param reg    the course registration for which to construct the student pace summary
     * @param cache  the data cache
     * @return Success or failure
     */
    static ESuccessFailure createStudentPaceSummary(final TermRec active, final RawStcourse reg, final Cache cache) {

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
                    result = buildStudentPaceSummary(cache, reg);
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
     * @param cache the data cache
     * @param reg   the course registration
     * @return Success of failure
     */
    private static ESuccessFailure buildStudentPaceSummary(final Cache cache, final RawStcourse reg) {

        final List<RawStexam> firstPassedReviews = new ArrayList<>(5);
        ESuccessFailure result = gatherFirstPassedReviews(cache, reg, firstPassedReviews);

        if (result == ESuccessFailure.SUCCESS && !firstPassedReviews.isEmpty()) {

            for (final RawStexam exam : firstPassedReviews) {
                // For each unit, find point values for the RE exam and determine if it was completed on time
                if ("Y".equals(reg.iInProgress)) {

//        IF stc_rec.i_in_progress = "Y" THEN
//        LET countv = NULL
//        SELECT count(*) INTO countv FROM crsection
//        WHERE course = stc_rec.course
//        AND sect = stc_rec.sect
//        AND unit = stexamv.unit
//        AND term = stc_rec.i_term
//        AND term_yr = stc_rec.i_term_yr
//
//        IF countv = 0 THEN
//        CONTINUE FOREACH
//        END IF
//
//        SELECT re_points_ontime, re_points_late INTO pt_ontime,pt_late
//        FROM crsection
//        WHERE course = stc_rec.course
//        AND sect = stc_rec.sect
//        AND unit = stexamv.unit
//        AND term = stc_rec.i_term
//        AND term_yr = stc_rec.i_term_yr
//
//      # Calculate ms_nbrv from component parts
//        IF stc_rec.i_counted = "N" THEN
//        LET ipacev = NULL
//        SELECT pace INTO ipacev FROM stterm
//        WHERE stterm.stu_id = stc_rec.stu_id
//        AND stterm.term = stc_rec.i_term
//        AND stterm.term_yr = stc_rec.i_term_yr
//        LET ms_nbrv = ((ipacev * 100) + (orderv * 10) + stexamv.unit)
//        ELSE  { ie. when i_in_progress=Y AND i_counted=Y }
//        LET ms_nbrv = ((total_stc * 100) + (orderv * 10) + stexamv.unit)
//        END IF
//
//        IF stc_rec.i_counted = "N" THEN
//        SELECT ms_date INTO duev FROM milestone
//        WHERE pace = ipacev
//        AND term = stc_rec.i_term
//        AND term_yr = stc_rec.i_term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//        ELSE
//        SELECT ms_date INTO duev FROM milestone
//        WHERE pace = total_stc
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//        END IF
//
//      #determine if student was given an extension for this deadline
//        LET countv = NULL
//        IF stc_rec.i_counted = "N" THEN
//        SELECT count(*) INTO countv FROM stmilestone
//        WHERE stu_id = stc_rec.stu_id
//        AND term = stc_rec.i_term
//        AND term_yr = stc_rec.i_term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//
//        IF countv > 0 THEN
//        SELECT MAX(ms_date) INTO duev FROM stmilestone
//        WHERE stu_id = stc_rec.stu_id
//        AND term = stc_rec.i_term
//        AND term_yr = stc_rec.i_term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//        LET new_duev = "Y"
//        ELSE
//        LET new_duev = null
//        END IF
//        ELSE
//        SELECT count(*) INTO countv FROM stmilestone
//        WHERE stu_id = stc_rec.stu_id
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//
//        IF countv > 0 THEN
//        SELECT MAX(ms_date) INTO duev FROM stmilestone
//        WHERE stu_id = stc_rec.stu_id
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//        LET new_duev = "Y"
//        ELSE
//        LET new_duev = null
//        END IF
//        END IF
                } else {
//        LET countv = NULL
//        SELECT count(*) INTO countv FROM crsection
//        WHERE course = stc_rec.course
//        AND sect = stc_rec.sect
//        AND unit = stexamv.unit
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//
//        IF countv = 0 THEN
//        CONTINUE FOREACH
//        END IF
//
//        SELECT re_points_ontime, re_points_late INTO pt_ontime,pt_late
//        FROM crsection
//        WHERE course = stc_rec.course
//        AND sect = stc_rec.sect
//        AND unit = stexamv.unit
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//
//      # Calculate ms_nbrv from component parts
//        LET ms_nbrv = ((total_stc * 100) + (orderv * 10) + stexamv.unit)
//
//        SELECT ms_date INTO duev FROM milestone
//        WHERE pace = total_stc
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//
//      #determine if student was given an extension for this deadline
//        LET countv = NULL
//        SELECT count(*) INTO countv FROM stmilestone
//        WHERE stu_id = stc_rec.stu_id
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//
//        IF countv > 0 THEN
//        SELECT MAX(ms_date) INTO duev FROM stmilestone
//        WHERE stu_id = stc_rec.stu_id
//        AND term = stc_rec.term
//        AND term_yr = stc_rec.term_yr
//        AND pace_track = pace_trackv
//        AND ms_nbr = ms_nbrv
//        AND ms_type IN ("RE")
//        LET new_duev = "Y"
//        ELSE
//        LET new_duev = null
//        END IF
//        END IF { i_in_progress="Y" }
//
//        IF duev IS NULL THEN
//        LET rpt_line = "no due date for stu_id=", stc_rec.stu_id, " course=", stc_rec.course,
//                " term=", stc_rec.term, " ", stc_rec.term_yr, " track=", pace_trackv, " ms=",
//                ms_nbrv, "\n"
//        OUTPUT TO REPORT problems_rpt(rpt_line)
//
//                FINISH REPORT problems_rpt
//        FINISH REPORT incomp_rpt
//        FINISH REPORT final_grading_rpt
//        EXIT PROGRAM
//        END IF
//
//        IF duev >= stexamv.exam_dt THEN
//        INSERT INTO stpace_summary VALUES (stc_rec.stu_id,stc_rec.course,
//                stc_rec.sect, stc_rec.term,stc_rec.term_yr,stc_rec.i_in_progress,
//                total_stc,pace_trackv,orderv,ms_nbrv,stexamv.unit,duev,new_duev,
//                stexamv.exam_dt,pt_ontime)
//        ELSE
//        INSERT INTO stpace_summary VALUES (stc_rec.stu_id,stc_rec.course,
//                stc_rec.sect, stc_rec.term,stc_rec.term_yr,stc_rec.i_in_progress,
//                total_stc,pace_trackv,orderv,ms_nbrv,stexamv.unit,duev,new_duev,
//                stexamv.exam_dt,pt_late)
//        END IF
                }
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
}
