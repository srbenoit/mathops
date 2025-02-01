package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

/**
 * An object to check the eligibility of a student to take a particular homework assignment.
 */
public class HomeworkEligibilityTester extends EligibilityTesterBase {

    /**
     * Creates a new eligibility test class, which can be used to test several exams for eligibility.
     *
     * @param theStudentId the student ID for which eligibility is to be tested
     */
    public HomeworkEligibilityTester(final String theStudentId) {

        super(theStudentId);
    }

    /**
     * Get the minimum score required to move on past the homework assignment to the next instructional item in the
     * course.
     *
     * @return the minimum move on score, or {@code null} if none is configured
     */
    public final Integer getMinMoveOnScore() {

        if (this.courseSectionUnit == null) {
            return null;
        }

        return this.courseSectionUnit.hwMoveonScore;
    }

    /**
     * Get the minimum score required to master the homework assignment and get a homework credit.
     *
     * @return the minimum mastery score, or {@code null} if none is configured.
     */
    public final Integer getMinMasteryScore() {

        if (this.courseSectionUnit == null) {
            return null;
        }

        return this.courseSectionUnit.hwMasteryScore;
    }

    /**
     * Check a single homework for eligibility. This method does some initial tests on validity of inputs, then goes
     * through a multistage process to test the eligibility of the student to access the requested homework. Relevant
     * status information is accumulated, including holds on the student record, or the reason the homework cannot be
     * taken at this time.
     *
     * @param cache    the data cache
     * @param now      the date/time to consider as "now"
     * @param hw       the homework being tested
     * @param reasons  a buffer to which to append the reason the exam is unavailable
     * @param holds    a list to which holds are added if present on the student account
     * @param practice {@code true} if the request is for a practice assignment
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public final boolean isHomeworkEligible(final Cache cache, final ZonedDateTime now, final AssignmentRec hw,
                                            final HtmlBuilder reasons, final Collection<? super RawAdminHold> holds,
                                            final boolean practice) throws SQLException {

        final boolean ok;

        // Homeworks in the "MPE Review" course are always available
        if ("M 100R".equals(hw.courseId)) {
            ok = true;
        } else if (validateStudent(cache, now, reasons, holds, !practice) && checkActiveTerm(cache, now, reasons)) {
            final String course = hw.courseId;

            if (practice || isSpecialStudentId() || isSpecial()) {
                ok = true;
            } else if (RawRecordConstants.M1170.equals(course) || RawRecordConstants.M1180.equals(course) ||
                    RawRecordConstants.M1240.equals(course) || RawRecordConstants.M1250.equals(course) ||
                    RawRecordConstants.M1260.equals(course)) {
                ok = checkPrecalcTutorialHWAvailability(cache, reasons, hw);
            } else {
                ok = checkHWAvailability(cache, now, reasons, hw);
            }
        } else {
            ok = false;
        }

        return ok;
    }

    /**
     * Determine whether an online exam may be taken by the student. This involves exam-specific logic, based on
     * parameters stored in the exam, course, and course-unit objects. ALL exam-specific test logic is database-driven,
     * so this code will not need to change as new exams are defined or exam versions are added.
     *
     * @param cache   the data cache
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param avail   the exam to test (on entry, only version is set)
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkPrecalcTutorialHWAvailability(final Cache cache, final HtmlBuilder reasons,
                                                       final AssignmentRec avail) throws SQLException {

        final String course = avail.courseId;

        boolean ok = false;

        final List<RawStexam> passedReviews = RawStexamLogic.getExams(cache, this.studentId, course, true, "R");

        // Student is eligible for the tutorial - now check for the requested exam
        final int unit = avail.unit.intValue();

        if (unit == 1) {
            if (hasPassedReview(passedReviews, 0)) {
                ok = true;
            } else {
                reasons.add("Skills Review Exam not yet passed.");
            }
        } else if (unit == 2) {
            if (hasPassedReview(passedReviews, 1)) {
                ok = true;
            } else {
                reasons.add("Unit 1 Review Exam not yet passed.");
            }
        } else if (unit == 3) {
            if (hasPassedReview(passedReviews, 2)) {
                ok = true;
            } else {
                reasons.add("Unit 2 Review Exam not yet passed.");
            }
        } else if (unit == 4) {
            if (hasPassedReview(passedReviews, 3)) {
                ok = true;
            } else {
                reasons.add("Unit 3 Review Exam not yet passed.");
            }
        } else {
            reasons.add("You are not eligible for this Tutorial.");
        }

        return ok;
    }

    /**
     * Scans a list of passing review exams to see if one exists for a specified unit.
     *
     * @param passedReviews the list of all passed reviews in the course
     * @param unit          the unit for which to test
     * @return true if a passing review exam was found for the specified unit
     */
    private static boolean hasPassedReview(final Iterable<RawStexam> passedReviews, final int unit) {

        boolean ok = false;

        for (final RawStexam test : passedReviews) {
            if (test.unit != null && test.unit.intValue() == unit) {
                ok = true;
                break;
            }
        }

        return ok;
    }

    /**
     * Determine whether a homework may be accessed by the student.
     *
     * @param cache   the data cache
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param hw      the homework to test
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkHWAvailability(final Cache cache, final ZonedDateTime now,
                                        final HtmlBuilder reasons, final AssignmentRec hw) throws SQLException {

        final boolean ok;

        // Guests and tutors can access all homework at any time
        if (isSpecialStudentId() || isSpecial()) {
            ok = gatherSectionInfo(cache, reasons, hw.courseId, hw.unit, false);
        } else if (gatherSectionInfo(cache, reasons, hw.courseId, hw.unit, true)) {
            if ("Y".equals(this.studentCourse.iInProgress)) {
                ok = checkSectionStartDate(now, reasons) && checkIncomplete(now, reasons)
                        && checkObjectiveEligibility(cache, reasons, hw)
                        && checkForCourseLockout(cache, now, reasons);
            } else {
                ok = checkSectionStartDate(now, reasons) && checkIncomplete(now, reasons)
                        && checkTimeWindows(now, reasons) && checkObjectiveEligibility(cache, reasons, hw)
                        && checkForCourseLockout(cache, now, reasons);
            }
        } else {
            ok = false;
        }

        return ok;
    }

    /**
     * If the section record includes a start date, make sure we are beyond the start date. Otherwise, no work may be
     * performed in the section.
     *
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return {@code true} if information was successfully found; {@code false} if an error occurred or the data was
     *         not found
     */
    private boolean checkSectionStartDate(final ZonedDateTime now, final HtmlBuilder reasons) {

        final boolean ok;

        if (canAccessOutsideTerm() || this.courseSection.startDt == null) {
            ok = true;
        } else {
            final LocalDate start = this.courseSection.startDt;
            final LocalDate today = now.toLocalDate();

            if (start.isAfter(today)) {
                reasons.add("This course begins on ", TemporalUtils.FMT_MDY.format(start),
                        ".  Assignments are not available until that date.");
                ok = false;
            } else {
                ok = true;
            }
        }

        return ok;
    }

    /**
     * If the student has an incomplete in progress, he/she may only test in the course that is incomplete, or in
     * courses that do not enforce this restriction.
     *
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return {@code true} if request completed successfully; {@code false} otherwise
     */
    private boolean checkIncomplete(final ZonedDateTime now, final HtmlBuilder reasons) {

        final boolean ok;

        if (this.incompleteOnly) {
            if ("Y".equals(this.studentCourse.iInProgress)) {

                final LocalDate deadline = this.studentCourse.iDeadlineDt;

                if (deadline == null) {
                    reasons.add("Invalid incomplete deadline date.");
                    ok = false;
                } else {
                    final LocalDate today = now.toLocalDate();

                    if (today.isAfter(deadline)) {
                        reasons.add("Past the deadline date for this incomplete.");
                        ok = false;
                    } else {
                        ok = true;
                    }
                }
            } else {
                reasons.add("Must finish incomplete courses first");
                ok = false;
            }
        } else {
            ok = true;
        }

        return ok;
    }

    /**
     * See whether the current time of day falls within the daily testing windows, and whether the selected exam has an
     * open testing window now.
     *
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return {@code true} if request completed successfully; {@code false} otherwise
     */
    private boolean checkTimeWindows(final ZonedDateTime now, final HtmlBuilder reasons) {

        final LocalDate today = now.toLocalDate();

        final LocalDate day;
        if (this.courseSectionUnit.lastTestDt != null) {
            day = this.courseSectionUnit.lastTestDt;
        } else {
            day = today;
        }

        // If the last test date is in the past, the course is unavailable.
        if (day.isBefore(today)) {
            reasons.add("Course not currently available.");
            return false;
        }

        return true;
    }

    /**
     * To do homework in a unit, (assuming the unit is available), the objective must either be 1, or there must be a
     * homework on record for the prior objective (not necessarily passed).
     *
     * @param cache   the data cache
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param hw      the homework to test
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkObjectiveEligibility(final Cache cache, final HtmlBuilder reasons,
                                              final AssignmentRec hw) throws SQLException {

        boolean ok = true;

        // Pacing structure will be null if we are under ADMIN/TUTOR access or similar, which does
        // not enforce objective eligibility rules.

        if (!isSpecial()) {
            if (this.pacingStructure != null) {
                final String pacing = this.pacingStructure.pacingStructure;

                final SystemData systemData = cache.getSystemData();

                final boolean reqHw = systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.HW_PASS);
                final boolean reqRe = systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.UR_MSTR);
                final boolean reqUe = systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.UE_MSTR);

                final String courseId = hw.courseId;
                final int unit = hw.unit == null ? -1 : hw.unit.intValue();
                final int obj = hw.objective == null ? -1 : hw.objective.intValue();

                if (reqHw && obj > 1) {
                    final List<RawSthomework> hws = RawSthomeworkLogic.getHomeworks(cache, this.studentId, courseId,
                            hw.unit, true, "HW");

                    ok = false;
                    for (final RawSthomework homework : hws) {
                        final Integer done = homework.objective;

                        if (done != null && done.intValue() == obj - 1) {
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        final String priorObjString = Integer.toString(obj - 1);
                        reasons.add("Assignment for objective ", priorObjString, " has not yet been completed.");
                    }
                }

                // See if the prior unit's review exam was mastered
                final Integer priorUnit = Integer.valueOf(unit - 1);
                final String priorUnitString = Integer.toString(unit - 1);

                if (ok && reqRe && unit != 1
                        && RawStexamLogic.getExams(cache, this.studentId, courseId, priorUnit, true, "R").isEmpty()) {

                    reasons.add("Review exam for unit ", priorUnitString, " has not yet been passed.");
                    ok = false;
                }

                // See if the prior unit's unit exam was mastered
                if (ok && reqUe && unit > 1
                        && RawStexamLogic.getExams(cache, this.studentId, courseId, priorUnit, true, "U").isEmpty()) {

                    reasons.add("Unit exam for unit ", priorUnitString, " has not yet been passed.");
                    ok = false;
                }
            }
        }

        return ok;
    }
}
