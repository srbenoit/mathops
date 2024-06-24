package dev.mathops.db.old.logic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.svc.term.TermLogic;
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
     * @param cache    the data cache
     * @param stcourse the student course record
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    public static String checkForComplete(final Cache cache, final RawStcourse stcourse) throws SQLException {

        final String stuId = stcourse.stuId;
        final String course = stcourse.course;
        String error = null;

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        if (active == null) {
            error = "Unable to query active term";
        } else {
            final List<RawStexam> passedExams = RawStexamLogic.getExams(cache, stuId, course, true, "U", "F");

            int maxUnit1 = -1;
            int maxUnit2 = -1;
            int maxUnit3 = -1;
            int maxUnit4 = -1;
            int maxFinal = -1;
            for (final RawStexam exam : passedExams) {
                if (exam.unit != null && exam.examScore != null) {
                    final int unit = exam.unit.intValue();

                    if (unit == 1) {
                        maxUnit1 = Math.max(maxUnit1, exam.examScore.intValue());
                    } else if (unit == 2) {
                        maxUnit2 = Math.max(maxUnit2, exam.examScore.intValue());
                    } else if (unit == 3) {
                        maxUnit3 = Math.max(maxUnit3, exam.examScore.intValue());
                    } else if (unit == 4) {
                        maxUnit4 = Math.max(maxUnit4, exam.examScore.intValue());
                    } else if (unit == 5) {
                        maxFinal = Math.max(maxFinal, exam.examScore.intValue());
                    }
                }
            }

            if (maxUnit1 >= 0 && maxUnit2 >= 0 && maxUnit3 >= 0 && maxUnit4 >= 0 && maxFinal >= 0
                    && !stcourse.synthetic) {

                // Check for on-time review exams to add in those points
                final RawStterm stterm = RawSttermLogic.query(cache, active.term, stuId);

                if (stterm == null || stterm.pace == null || stcourse.paceOrder == null) {
                    Log.warning("Unable to locate milestone records for ", stuId);
                } else {
                    final int pace = stterm.pace.intValue();
                    final int order = stcourse.paceOrder.intValue();

                    final List<RawMilestone> milestones = RawMilestoneLogic.getAllMilestones(cache,
                            active.term, pace, stterm.paceTrack);

                    final List<RawStmilestone> stmilestones = RawStmilestoneLogic
                            .getStudentMilestones(cache, active.term, stterm.paceTrack, stuId);

                    LocalDate unit1 = null;
                    LocalDate unit2 = null;
                    LocalDate unit3 = null;
                    LocalDate unit4 = null;
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
                    final List<RawStexam> passedReviews = RawStexamLogic.getExams(cache, stuId, stcourse.course,
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

                    if (totalScore >= 54) {

                        final String grade;
                        if (totalScore >= 65) {
                            grade = "A";
                        } else if (totalScore >= 62) {
                            grade = "B";
                        } else {
                            grade = "C";
                        }

                        if (RawStcourseLogic.updateCompletedScoreGrade(cache, stuId, stcourse.course, stcourse.sect,
                                stcourse.termKey, "Y", newScore, grade)) {

                            stcourse.completed = "Y";
                            stcourse.score = newScore;

                            Log.info("Marked ", course, " as completed for ", stuId, " with score ", newScore);
                        } else {
                            error = "Unable to mark course as Completed";
                        }
                    } else if ("Y".equals(stcourse.completed)) {

                        if (RawStcourseLogic.updateCompletedScoreGrade(cache, stuId, stcourse.course, stcourse.sect,
                                stcourse.termKey, "N", newScore, "U")) {

                            stcourse.completed = "N";
                            stcourse.score = null;
                            stcourse.courseGrade = null;

                            Log.info("Marked ", course, " as not completed for ", stuId);
                        } else {
                            error = "Unable to mark course as Completed";
                        }
                    }
                }
            }
        }

        return error;
    }

    /**
     * Generates the student's status in a current in-progress course.  This includes proper status information for
     * incompletes, and for both traditional and standards-based courses.
     *
     * @param cache the cache
     * @param reg   the registration record
     * @return the course status object
     * @throws SQLException if there is an error accessing the database
     */
    public static CourseStatus computeStatus(final Cache cache, final RawStcourse reg) throws SQLException {

        final CourseStatus status;

        if ("Y".equals(reg.iInProgress)) {
            final RawCsection csection = getCourseSection(cache, reg);
            status = new CourseStatus(reg, csection, null);

            if ("Y".equals(reg.iCounted)) {
                // Incomplete, counted in pace

                final List<RawStcourse> paced = RawStcourseLogic.getPaced(cache, reg.stuId);
            } else {
                // Incomplete, not counted in pace
            }
        } else {
            final List<RawStcourse> paced = RawStcourseLogic.getPaced(cache, reg.stuId);

            // Current-term registration
            status = computeCurrentTermStatus(cache, reg, paced);
        }

        return status;
    }

    /**
     * Generates status for a current-term course.
     *
     * @param cache the cache
     * @param reg   the registration record
     * @param paced the set of registrations that determine pace
     * @return the course status object
     * @throws SQLException if there is an error accessing the database
     */
    private static CourseStatus computeCurrentTermStatus(final Cache cache, final RawStcourse reg,
                                                         final List<RawStcourse> paced) throws SQLException {
        final CourseStatus status;

        final RawCsection csection = getCourseSection(cache, reg);

        if ("MAS".equals(csection.gradingStd)) {
            // This is a mastery-based course
            status = computeCurrentTermStandardsBasedStatus(cache, reg, paced, csection);
        } else {
            status = computeCurrentTermLegacyStatus(cache, reg, paced, csection);
        }

        return status;
    }

    /**
     * Generates status for a current-term standards-based course.
     *
     * @param cache    the cache
     * @param reg      the registration record
     * @param paced    the set of registrations that determine pace
     * @param csection the course section object
     * @return the course status object
     * @throws SQLException if there is an error accessing the database
     */
    private static CourseStatus computeCurrentTermStandardsBasedStatus(final Cache cache, final RawStcourse reg,
                                                                       final List<RawStcourse> paced,
                                                                       final RawCsection csection) throws SQLException {

        return null;
    }

    /**
     * Generates status for a current-term legacy course.
     *
     * @param cache    the cache
     * @param reg      the registration record
     * @param paced    the set of registrations that determine pace
     * @param csection the course section object
     * @return the course status object
     * @throws SQLException if there is an error accessing the database
     */
    private static CourseStatus computeCurrentTermLegacyStatus(final Cache cache, final RawStcourse reg,
                                                               final List<RawStcourse> paced,
                                                               final RawCsection csection) throws SQLException {

        final int pace = PaceTrackLogic.determinePace(paced);
        final String track = PaceTrackLogic.determinePaceTrack(paced, pace);

        final List<RawMilestone> milestones = RawMilestoneLogic.getAllMilestones(cache, reg.termKey, pace, track);
        final List<RawStmilestone> stmilestones = RawStmilestoneLogic.getStudentMilestones(cache, reg.termKey,
                track, reg.stuId);

        int index = 1;
        if (reg.paceOrder == null) {
            final int count = paced.size();
            for (int i = 0; i < count; ++i) {
                final RawStcourse test = paced.get(i);
                if (test.course.equals(reg.course)) {
                    index = i + 1;
                    break;
                }
            }
        } else {
            index = reg.paceOrder.intValue();
        }

        LocalDate re1 = null;
        LocalDate re2 = null;
        LocalDate re3 = null;
        LocalDate re4 = null;
        LocalDate fe = null;

        for (final RawMilestone test : milestones) {
            if (test.getIndex() == index) {
                if ("RE".equals(test.msType)) {
                    final int unit = test.getUnit();
                    if (unit == 1) {
                        re1 = test.msDate;
                        for (final RawStmilestone stms : stmilestones) {
                            if ("RE".equals(stms.msType) && test.msNbr.equals(stms.msNbr)) {
                                re1 = stms.msDate;
                                break;
                            }
                        }
                    } else if (unit == 2) {
                        re2 = test.msDate;
                        for (final RawStmilestone stms : stmilestones) {
                            if ("RE".equals(stms.msType) && test.msNbr.equals(stms.msNbr)) {
                                re2 = stms.msDate;
                                break;
                            }
                        }
                    } else if (unit == 3) {
                        re3 = test.msDate;
                        for (final RawStmilestone stms : stmilestones) {
                            if ("RE".equals(stms.msType) && test.msNbr.equals(stms.msNbr)) {
                                re3 = stms.msDate;
                                break;
                            }
                        }
                    } else if (unit == 4) {
                        re4 = test.msDate;
                        for (final RawStmilestone stms : stmilestones) {
                            if ("RE".equals(stms.msType) && test.msNbr.equals(stms.msNbr)) {
                                re4 = stms.msDate;
                                break;
                            }
                        }
                    }
                } else if ("FE".equals(test.msType)) {
                    final int unit = test.getUnit();
                    if (unit == 5) {
                        fe = test.msDate;
                        for (final RawStmilestone stms : stmilestones) {
                            if ("FE".equals(stms.msType) && test.msNbr.equals(stms.msNbr)) {
                                fe = stms.msDate;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (re1 == null || re2 == null || re3 == null || re4 == null) {
            final String paceStr = Integer.toString(pace);
            final String msg = SimpleBuilder.concat("Failed to query milestones for pace ", paceStr, " track ",
                    track, " in ", reg.termKey.shortString);
            throw new SQLException(msg);
        }

        final List<RawStexam> stexams = RawStexamLogic.getExams(cache, reg.stuId, reg.course, false,
                RawStexam.REVIEW_EXAM, RawStexam.UNIT_EXAM, RawStexam.FINAL_EXAM);

        LocalDate re1PassDate = null;
        LocalDate re2PassDate = null;
        LocalDate re3PassDate = null;
        LocalDate re4PassDate = null;
        int bestPassingU1 = 0;
        int bestPassingU2 = 0;
        int bestPassingU3 = 0;
        int bestPassingU4 = 0;
        int bestPassingFE = 0;
        int bestFailedU1 = 0;
        int bestFailedU2 = 0;
        int bestFailedU3 = 0;
        int bestFailedU4 = 0;
        int bestFailedFE = 0;
        int numU1 = 0;
        int numU2 = 0;
        int numU3 = 0;
        int numU4 = 0;
        int numFE = 0;

        for (final RawStexam exam : stexams) {
            if (RawStexam.REVIEW_EXAM.equals(exam.examType) && "Y".equals(exam.passed)) {
                final int unit = exam.unit.intValue();

                if (unit == 1) {
                    if (re1PassDate == null || re1PassDate.isAfter(exam.examDt)) {
                        re1PassDate = exam.examDt;
                    }
                } else if (unit == 2) {
                    if (re2PassDate == null || re2PassDate.isAfter(exam.examDt)) {
                        re2PassDate = exam.examDt;
                    }
                } else if (unit == 3) {
                    if (re3PassDate == null || re3PassDate.isAfter(exam.examDt)) {
                        re3PassDate = exam.examDt;
                    }
                } else if (unit == 4) {
                    if (re4PassDate == null || re4PassDate.isAfter(exam.examDt)) {
                        re4PassDate = exam.examDt;
                    }
                }
            } else if (RawStexam.UNIT_EXAM.equals(exam.examType) && exam.examScore != null) {
                final int unit = exam.unit.intValue();
                final boolean passed = "Y".equals(exam.passed);
                final int score = exam.examScore.intValue();

                if (unit == 1) {
                    if (passed) {
                        bestPassingU1 = Math.max(bestPassingU1, score);
                    } else {
                        bestFailedU1 = Math.max(bestFailedU1, score);
                    }
                    ++numU1;
                } else if (unit == 2) {
                    if (passed) {
                        bestPassingU2 = Math.max(bestPassingU2, score);
                    } else {
                        bestFailedU2 = Math.max(bestFailedU2, score);
                    }
                    ++numU2;
                } else if (unit == 3) {
                    if (passed) {
                        bestPassingU3 = Math.max(bestPassingU3, score);
                    } else {
                        bestFailedU3 = Math.max(bestFailedU3, score);
                    }
                    ++numU3;
                } else if (unit == 4) {
                    if (passed) {
                        bestPassingU4 = Math.max(bestPassingU4, score);
                    } else {
                        bestFailedU4 = Math.max(bestFailedU4, score);
                    }
                    ++numU4;
                }
            } else if (RawStexam.FINAL_EXAM.equals(exam.examType) && exam.examScore != null) {
                final boolean passed = "Y".equals(exam.passed);
                final int score = exam.examScore.intValue();

                if (passed) {
                    bestPassingFE = Math.max(bestPassingFE, score);
                } else {
                    bestFailedFE = Math.max(bestFailedFE, score);
                }
                ++numFE;
            }
        }

        final boolean re1OnTime = re1PassDate != null && !re1PassDate.isAfter(re1);
        final boolean re2OnTime = re2PassDate != null && !re2PassDate.isAfter(re2);
        final boolean re3OnTime = re3PassDate != null && !re3PassDate.isAfter(re3);
        final boolean re4OnTime = re4PassDate != null && !re4PassDate.isAfter(re4);

        // Compute total score
        int totalScore = bestPassingU1 + bestPassingU2 + bestPassingU3 + bestPassingU4 + bestPassingFE;
        if (re1OnTime) {
            totalScore += 3;
        }
        if (re2OnTime) {
            totalScore += 3;
        }
        if (re3OnTime) {
            totalScore += 3;
        }
        if (re4OnTime) {
            totalScore += 3;
        }

        final CourseStatus.LegacyCourseStatus legacy = new CourseStatus.LegacyCourseStatus(re1, re2, re3, re4, fe,
                re1OnTime, re2OnTime, re3OnTime, re4OnTime, bestPassingU1,
                bestPassingU2, bestPassingU3, bestPassingU4, bestPassingFE, bestFailedU1, bestFailedU2,
                bestFailedU3, bestFailedU4, bestFailedFE, totalScore, numU1, numU2, numU3, numU4, numFE);

        return new CourseStatus(reg, csection, legacy);
    }

    /**
     * Attempts to query the course section object for a registration.
     *
     * @param cache the cache
     * @param reg   the registration
     * @return the course section record
     * @throws SQLException if there is an error accessing the database or the course section cannot be found
     */
    public static RawCsection getCourseSection(final Cache cache, final RawStcourse reg) throws SQLException {

        RawCsection csection = null;

        if ("Y".equals(reg.iInProgress)) {
            csection = RawCsectionLogic.query(cache, reg.course, reg.sect, reg.iTermKey);
        }

        if (csection == null) {
            csection = RawCsectionLogic.query(cache, reg.course, reg.sect, reg.termKey);
        }

        if (csection == null) {
            final List<RawCsection> all = RawCsectionLogic.queryByTerm(cache, reg.termKey);
            final boolean isDistance = reg.sect.startsWith("8") || reg.sect.startsWith("4");

            for (final RawCsection test : all) {
                final boolean testIsDistance = test.sect.startsWith("8") || test.sect.startsWith("4");
                if (isDistance == testIsDistance && test.course.equals(reg.course)) {
                    csection = test;
                    break;
                }
            }
        }

        if (csection == null) {
            final String msg = SimpleBuilder.concat("Failed to query course section for ", reg.course, " sect ",
                    reg.sect, " in ", reg.termKey.shortString);
            throw new SQLException(msg);
        }

        return csection;
    }
}
