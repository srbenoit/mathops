package dev.mathops.web.host.precalc.course;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.impl.RawSthomeworkLogic;
import dev.mathops.db.schema.legacy.rec.RawStcourse;
import dev.mathops.db.schema.legacy.rec.RawSthomework;
import dev.mathops.db.schema.main.impl.MasteryAttemptLogic;
import dev.mathops.db.schema.main.impl.StuStandardMilestoneLogic;
import dev.mathops.db.schema.term.rec.MasteryAttemptRec;
import dev.mathops.db.schema.main.rec.MasteryExamRec;
import dev.mathops.db.schema.term.rec.StandardMilestoneRec;
import dev.mathops.db.schema.term.rec.StuStandardMilestoneRec;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

final class StdsMasteryStatus {

    /** The number of units. */
    private static final int NUM_UNITS = 8;

    /** The number of standards per unit. */
    private static final int STANDARDS_PER_UNIT = 3;

    /** The total number of standards. */
    private static final int NUM_STANDARDS = NUM_UNITS * STANDARDS_PER_UNIT;

    /** Number of minutes past midnight we treat as the day before for meeting due dates. */
    private static final int GRACE_PERIOD_MIN = 10;

    /** A possible value for "standardStatus" member. */
    private static final int MASTERY_NOT_ATTEMPTED = 0;

    /** A possible value for "standardStatus" member. */
    private static final int MASTERY_ATTEMPTED = 1;

    /** A possible value for "standardStatus" member. */
    private static final int MASTERED_LATE = 2;

    /** A possible value for "standardStatus" member. */
    private static final int MASTERED_ON_TIME = 3;

    /** The course data based on the student's registration. */
    final SiteDataCfgCourse courseData;

    /** True if the user should have tutor access to assignments. */
    final boolean tutor;

    /** Status in 8 Skills Reviews (0=not tried, 1=tried, 2=passed). */
    final int[] skillsReviewStatus;

    /** Status in 24 assignments (0=not tried, 1=tried, 2=passed). */
    final int[] assignmentStatus;

    /** Status in 24 standards (0=not tried, 1=tried, 2=mastered late, 3=mastered on time). */
    final int[] standardStatus;

    /** Dates when each standard was first mastered. */
    final LocalDate[] standardFirstMastered;

    /** Dates by which mastery of each standard is due. */
    final LocalDate[] masteryDeadlines;

    /** The number of standards that are "pending" (assignment passed but not mastered). */
    int numStandardsPending;

    /** The number of standards that are "pending" in the first half of the course. */
    int numStandardsPendingFirstHalf;

    /** The number of standards that are "pending" in the second half of the course. */
    int numStandardsPendingSecondHalf;

    /** The student's total score. */
    int score;

    /** The maximum unit number for which the student has work on record. */
    int maxUnit = 0;

    /**
     * Constructs a new {@code StdsMasteryStatus}.
     *
     * @param cache         the data cache
     * @param theCourseData the course data based on the student's registration
     * @param pace          the student's pace
     * @param paceTrack     the student's pace track
     * @param reg           the course registration for which to generate status
     * @param isTutor       true if the user should have TUTOR access to assignments
     */
    StdsMasteryStatus(final Cache cache, final SiteDataCfgCourse theCourseData, final int pace, final String paceTrack,
                      final RawStcourse reg, final boolean isTutor) {

        this.courseData = theCourseData;
        this.tutor = isTutor;

        this.skillsReviewStatus = new int[NUM_UNITS];
        this.assignmentStatus = new int[NUM_STANDARDS];
        this.standardStatus = new int[NUM_STANDARDS];
        this.standardFirstMastered = new LocalDate[NUM_STANDARDS];
        this.masteryDeadlines = new LocalDate[NUM_STANDARDS];

        if ("Y".equals(reg.iInProgress) && !("Y".equals(reg.iCounted))) {

            // TODO: Incomplete that is not counted in current pace. Due dates from the I term should govern for all
            //  work completed in the I term, but current-term dates should govern new work. How to do?

        } else if (reg.paceOrder != null) {
            // Current term "in pace" registration

            final Integer paceObj = Integer.valueOf(pace);
            final int order = reg.paceOrder.intValue();

            final SystemData systemData = cache.getSystemData();

            try {
                final List<StandardMilestoneRec> standardMilestones =
                        systemData.getStandardMilestonesForPaceTrack(paceTrack, paceObj);
                final List<StuStandardMilestoneRec> studentMilestones = StuStandardMilestoneLogic
                        .INSTANCE.queryByStuPaceTrackPace(cache, reg.stuId, paceTrack, paceObj);
                final List<MasteryExamRec> masteryExams = systemData.getActiveMasteryExamsByCourse(reg.course);
                final List<MasteryAttemptRec> masteryAttempts = MasteryAttemptLogic.INSTANCE
                        .queryByStudent(cache, reg.stuId);
                final List<RawSthomework> sthomeworks = RawSthomeworkLogic.queryByStudent(cache, reg.stuId, false);

                Log.info("Found " + masteryAttempts.size() + " mastery attempts");

                for (int unitIdx = 0; unitIdx < NUM_UNITS; ++unitIdx) {
                    final int unit = unitIdx + 1;
                    final Integer unitObj = Integer.valueOf(unit);

                    // See if the Skills Review for this unit has been attempted / passed
                    for (final RawSthomework sthomework : sthomeworks) {
                        if (sthomework.unit == null || sthomework.objective == null) {
                            continue;
                        }
                        final int hwUnit = sthomework.unit.intValue();
                        final int hwObj = sthomework.objective.intValue();

                        if (hwUnit == unit && hwObj == 0) {
                            final int value = "Y".equals(sthomework.passed) ? 2 : 1;
                            this.skillsReviewStatus[unitIdx] = Math.max(this.skillsReviewStatus[unitIdx], value);
                            this.maxUnit = unit;
                        }
                    }

                    for (int objIdx = 0; objIdx < STANDARDS_PER_UNIT; ++objIdx) {
                        final int obj = objIdx + 1;
                        final Integer objObj = Integer.valueOf(obj);
                        final int arrayIndex = unitIdx * 3 + objIdx;

                        // See if the Standard Assignment has been attempted / passed
                        for (final RawSthomework sthomework : sthomeworks) {
                            if (sthomework.unit == null || sthomework.objective == null) {
                                continue;
                            }

                            final int hwUnit = sthomework.unit.intValue();
                            final int hwObj = sthomework.objective.intValue();

                            if (hwUnit == unit && hwObj == obj) {
                                final int value = "Y".equals(sthomework.passed) ? 2 : 1;
                                this.assignmentStatus[arrayIndex] = Math.max(this.assignmentStatus[arrayIndex], value);
                            }
                        }

                        // Get the mastery due date for this standard
                        LocalDate onTime = null;
                        for (final StandardMilestoneRec ms : standardMilestones) {
                            if (ms.paceIndex.intValue() == order && ms.unit.intValue() == unit
                                && ms.objective.intValue() == obj
                                && StandardMilestoneRec.MS_TYPE_STD_MASTERY.equals(ms.msType)) {
                                onTime = ms.msDate;
                                break;
                            }
                        }
                        for (final StuStandardMilestoneRec stms : studentMilestones) {
                            if (stms.paceIndex.intValue() == order && stms.unit.intValue() == unit
                                && stms.objective.intValue() == obj
                                && StandardMilestoneRec.MS_TYPE_STD_MASTERY.equals(stms.msType)) {
                                onTime = stms.msDate;
                                break;
                            }
                        }

                        this.masteryDeadlines[arrayIndex] = onTime;

                        // Find the mastery exam for this unit/objective
                        String examId = null;
                        for (final MasteryExamRec exam : masteryExams) {
                            if (exam.unit == null || exam.objective == null) {
                                continue;
                            }
                            final int exUnit = exam.unit.intValue();
                            final int exObj = exam.objective.intValue();

                            if (exUnit == unit && exObj == obj) {
                                examId = exam.examId;
                                break;
                            }
                        }

                        if (examId == null) {
                            Log.warning("Unable to find Mastery exam for ", reg.course, " Unit ", unitObj,
                                    " Objective ", objObj);
                        } else {
                            // See if the Standard Mastery exam has been attempted / mastered (late or on-time)
                            int value = MASTERY_NOT_ATTEMPTED;
                            for (final MasteryAttemptRec attempt : masteryAttempts) {
                                if (examId.equals(attempt.examId)) {
                                    value = Math.max(value, MASTERY_ATTEMPTED);
                                    if ("Y".equals(attempt.passed)) {
                                        final LocalDateTime whenFinished = attempt.whenFinished;
                                        final LocalDate finishDate = whenFinished.toLocalDate();
                                        final LocalTime finishTime = whenFinished.toLocalTime();
                                        final LocalDate examDate;
                                        if (TemporalUtils.minuteOfDay(finishTime) < GRACE_PERIOD_MIN) {
                                            examDate = finishDate.minusDays(1L);
                                        } else {
                                            examDate = finishDate;
                                        }

                                        if (onTime == null || examDate.isAfter(onTime)) {
                                            value = Math.max(value, MASTERED_LATE);
                                        } else {
                                            value = MASTERED_ON_TIME;
                                            if (this.standardFirstMastered[arrayIndex] == null ||
                                                this.standardFirstMastered[arrayIndex].isAfter(examDate)) {
                                                this.standardFirstMastered[arrayIndex] = examDate;
                                            }
                                        }
                                    }
                                }
                            }

                            this.standardStatus[arrayIndex] = Math.max(this.standardStatus[arrayIndex], value);

                            if (value == MASTERED_LATE) {
                                this.score += 4;
                            } else if (value == MASTERED_ON_TIME) {
                                this.score += 5;
                            }
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Error querying milestones, mastery exams, oir mastery attempts.", ex);
            }
        }

        // Count the number of standards "pending" (assignment passed, standard not yet mastered)
        final int max = NUM_STANDARDS >> 1;
        final int count = this.standardStatus.length;
        for (int i = 0; i < count; ++i) {
            if (this.assignmentStatus[i] == 2 && this.standardStatus[i] < 2) {
                ++this.numStandardsPending;
                if (i < max) {
                    ++this.numStandardsPendingFirstHalf;
                } else {
                    ++this.numStandardsPendingSecondHalf;
                }
            }
        }
    }

    /**
     * Gets the number of standards the student has mastered in the first half of the course.
     *
     * @return the number mastered in the first half
     */
    int getNbrMasteredInFirstHalf() {

        int count = 0;

        final int max = NUM_STANDARDS >> 1;
        for (int i = 0; i < max; ++i) {
            if (this.standardStatus[i] > 1) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Gets the number of standards the student has mastered in the second half of the course.
     *
     * @return the number mastered in the second half
     */
    int getNbrMasteredInSecondHalf() {

        int count = 0;

        final int min = NUM_STANDARDS >> 1;
        for (int i = min; i < NUM_STANDARDS; ++i) {
            if (this.standardStatus[i] > 1) {
                ++count;
            }
        }

        return count;
    }
}
