package dev.mathops.db.old.logic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Logic around courses
 */
public enum CourseLogic {
    ;

    /**
     * Test whether the exam should cause the course to be marked as "complete".
     *
     * @param studentData the student data object
     * @param stcourse    the student course record
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    public static String checkForComplete(final StudentData studentData, final RawStcourse stcourse)
            throws SQLException {

        final String stuId = stcourse.stuId;
        final String course = stcourse.course;
        String error = null;

        final SystemData systemData = studentData.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        if (active == null) {
            error = "Unable to query active term";
        } else {
            final List<RawStexam> passedExams = studentData.getStudentExamsByCourseType(course, true, "U", "F");

            int maxUnit1 = -1;
            int maxUnit2 = -1;
            int maxUnit3 = -1;
            int maxUnit4 = -1;
            int maxFinal = -1;
            for (final RawStexam exam : passedExams) {
                if (exam.unit != null && exam.examScore != null) {
                    final int unit = exam.unit.intValue();
                    final int scoreValue = exam.examScore.intValue();

                    if (unit == 1) {
                        maxUnit1 = Math.max(maxUnit1, scoreValue);
                    } else if (unit == 2) {
                        maxUnit2 = Math.max(maxUnit2, scoreValue);
                    } else if (unit == 3) {
                        maxUnit3 = Math.max(maxUnit3, scoreValue);
                    } else if (unit == 4) {
                        maxUnit4 = Math.max(maxUnit4, scoreValue);
                    } else if (unit == 5) {
                        maxFinal = Math.max(maxFinal, scoreValue);
                    }
                }
            }

            if (maxUnit1 >= 0 && maxUnit2 >= 0 && maxUnit3 >= 0 && maxUnit4 >= 0 && maxFinal >= 0
                    && !stcourse.synthetic) {

                // Check for on-time review exams to add in those points
                final RawStterm stterm = studentData.getStudentTerm(active.term);

                if (stterm == null || stterm.pace == null || stcourse.paceOrder == null) {
                    Log.warning("Unable to locate milestone records for ", stuId);
                } else {
                    final int pace = stterm.pace.intValue();
                    final int order = stcourse.paceOrder.intValue();

                    LocalDate unit1 = null;
                    LocalDate unit2 = null;
                    LocalDate unit3 = null;
                    LocalDate unit4 = null;

                    final List<RawMilestone> milestones = systemData.getMilestones(active.term, stterm.pace,
                            stterm.paceTrack);
                    for (final RawMilestone test : milestones) {
                        if ("RE".equals(test.msType)) {
                            if (test.msNbr.intValue() == pace * 100 + order * 10 + 1) {
                                unit1 = test.msDate;
                            } else if (test.msNbr.intValue() == pace * 100 + order * 10 + 2) {
                                unit2 = test.msDate;
                            } else if (test.msNbr.intValue() == pace * 100 + order * 10 + 3) {
                                unit3 = test.msDate;
                            } else if (test.msNbr.intValue() == pace * 100 + order * 10 + 4) {
                                unit4 = test.msDate;
                            }
                        }
                    }

                    final List<RawStmilestone> stmilestones = studentData.getStudentMilestones(active.term,
                            stterm.paceTrack);
                    for (final RawStmilestone test : stmilestones) {
                        if ("RE".equals(test.msType)) {
                            if (unit1 != null && test.msNbr.intValue() == pace * 100 + order * 10 + 1) {
                                unit1 = test.msDate;
                            } else if (unit2 != null && test.msNbr.intValue() == pace * 100 + order * 10 + 2) {
                                unit2 = test.msDate;
                            } else if (unit3 != null && test.msNbr.intValue() == pace * 100 + order * 10 + 3) {
                                unit3 = test.msDate;
                            } else if (unit4 != null && test.msNbr.intValue() == pace * 100 + order * 10 + 4) {
                                unit4 = test.msDate;
                            }
                        }
                    }

                    // Log.info("Unit 1 deadline is ", unit1);
                    // Log.info("Unit 2 deadline is ", unit2);
                    // Log.info("Unit 3 deadline is ", unit3);
                    // Log.info("Unit 4 deadline is ", unit4);

                    int ontime1 = 0;
                    int ontime2 = 0;
                    int ontime3 = 0;
                    int ontime4 = 0;
                    final List<RawStexam> passedReviews = studentData.getStudentExamsByCourseType(stcourse.course,
                            true, "R");
                    for (final RawStexam rev : passedReviews) {
                        final int unit = rev.unit.intValue();

                        if (unit1 != null && unit == 1 && !rev.examDt.isAfter(unit1)) {
                            ontime1 = 3;
                        } else if (unit2 != null && unit == 2 && !rev.examDt.isAfter(unit2)) {
                            ontime2 = 3;
                        } else if (unit3 != null && unit == 3 && !rev.examDt.isAfter(unit3)) {
                            ontime3 = 3;
                        } else if (unit4 != null && unit == 4 && !rev.examDt.isAfter(unit4)) {
                            ontime4 = 3;
                        }
                    }

                    final int totalScore = maxUnit1 + maxUnit2 + maxUnit3 + maxUnit4 + maxFinal + ontime1 + ontime2
                            + ontime3 + ontime4;
                    final Integer newScore = Integer.valueOf(totalScore);

                    // FIXME: Get these scores from data
                    if (totalScore >= 54) {

                        final String grade;
                        if (totalScore >= 65) {
                            grade = "A";
                        } else if (totalScore >= 62) {
                            grade = "B";
                        } else {
                            grade = "C";
                        }

                        final Cache cache = studentData.getCache();
                        if (RawStcourseLogic.updateCompletedScoreGrade(cache, stuId, stcourse.course, stcourse.sect,
                                stcourse.termKey, "Y", newScore, grade)) {

                            stcourse.completed = "Y";
                            stcourse.score = newScore;

                            Log.info("Marked ", course, " as completed for ", stuId, " with score ", newScore);
                            studentData.forgetRegistrations();
                        } else {
                            error = "Unable to mark course as Completed";
                        }
                    } else if ("Y".equals(stcourse.completed)) {

                        final Cache cache = studentData.getCache();
                        if (RawStcourseLogic.updateCompletedScoreGrade(cache, stuId, stcourse.course, stcourse.sect,
                                stcourse.termKey, "N", newScore, "U")) {

                            stcourse.completed = "N";
                            stcourse.score = null;
                            stcourse.courseGrade = null;

                            Log.info("Marked ", course, " as not completed for ", stuId);
                            studentData.forgetRegistrations();
                        } else {
                            error = "Unable to mark course as Completed";
                        }
                    }
                }
            }
        }

        return error;
    }
}
