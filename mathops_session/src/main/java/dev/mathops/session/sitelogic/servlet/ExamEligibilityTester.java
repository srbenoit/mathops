package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.session.txn.messages.AvailableExam;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * An object to check the eligibility of a student to take a particular exam. This is intended for use for Unit, Review,
 * Pretest and Final exams. It should not be used for Placement exams or homework assignments.
 */
public final class ExamEligibilityTester extends EligibilityTesterBase {

    /**
     * Creates a new eligibility test class, which can be used to test several exams for eligibility.
     *
     * @param theStudentId the student ID for which eligibility is to be tested
     */
    public ExamEligibilityTester(final String theStudentId) {

        super(theStudentId);
    }

    /**
     * Gets the course/section for which the student is registered.
     *
     * @return the course section
     */
    public RawCsection getCourseSection() {

        return this.courseSection;
    }

    /**
     * Check a single exam for eligibility. This method does some initial tests on validity of inputs, then goes through
     * a multistage process to test the eligibility of the student to take the requested exam. Relevant status
     * information is accumulated, including holds on the student record, or the reason the exam cannot be taken at this
     * time.
     *
     * @param cache            the data cache
     * @param now              the date/time to consider as "now"
     * @param avail            the exam being tested
     * @param reasons          a buffer to which to append the reason the exam is unavailable
     * @param holds            a list to which holds are added if present on the student account
     * @param checkEligibility {@code true} if student eligibility to take the exam should be enforced, {@code false} if
     *                         not
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public boolean isExamEligible(final Cache cache, final ZonedDateTime now, final AvailableExam avail,
                                  final HtmlBuilder reasons, final Collection<? super RawAdminHold> holds,
                                  final boolean checkEligibility) throws SQLException {

        boolean ok = validateStudent(cache, now, reasons, holds, checkEligibility)
                && checkActiveTerm(cache, now, reasons);

        if (ok) {
            avail.timelimitFactor = this.student.timelimitFactor;
            ok = checkExamAvailability(cache, now, reasons, avail, checkEligibility);
        }

        return ok;
    }

    // FIXME: Look for all "LocalDate".toString(), and replace with formatter output

    /**
     * Determine whether an online exam may be taken by the student. This involves exam-specific logic, based on
     * parameters stored in the exam, course, and course-unit objects. ALL exam-specific test logic is database-driven,
     * so this code will not need to change as new exams are defined or exam versions are added.
     *
     * @param cache            the data cache
     * @param now              the date/time to consider as "now"
     * @param reasons          a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param avail            the exam to test (on entry, only version is set)
     * @param checkEligibility true if student eligibility to take the exam should be enforced, false if not
     * @return true if request completed successfully; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkExamAvailability(final Cache cache, final ZonedDateTime now, final HtmlBuilder reasons,
                                          final AvailableExam avail, final boolean checkEligibility)
            throws SQLException {

        boolean ok;

        // Gather the information from STCOURSE and CUSECTION, since it is used repeatedly in the checks that follow.
        ok = gatherSectionInfo(cache, reasons, avail.exam.course, avail.exam.unit, checkEligibility);

        // If student is finishing an incomplete, they may only test in the course that is incomplete, or courses where
        // this restriction is not enforced.
        if (ok && this.incompleteOnly && checkEligibility) {
            ok = checkIncomplete(now, reasons);
        }

        // If the exam enforces limited number of attempts, test this limit.
        if (ok && checkEligibility) {
            ok = checkNumAttempts(cache, reasons, avail);
        }

        // See if the student is registered for the exam's course, or is in an exception category and allowed to take
        // the exam
        if (ok) {
            ok = checkCourseRegistration(reasons);
        }

        if (ok) {
            final String course = this.courseSection.course;
            final String section = this.studentCourse.sect;

            final boolean isELM = RawRecordConstants.M100T.equals(course);
            final boolean isDistance = section != null && (section.charAt(0) == '8' || section.charAt(0) == '4');
            final boolean isPuAllowed = RawSpecialStusLogic.isSpecialType(cache, this.studentId, now.toLocalDate(),
                    RawSpecialStus.RIUSEPU);
            final boolean isIncomplete = "Y".equals(this.studentCourse.iInProgress);

            if (isELM || isDistance || isPuAllowed || isIncomplete) {
                // No time-window checks
                ok = true;
            } else {
                // Check time of day and testing window requirements
                ok = checkTimeWindows(now, reasons, checkEligibility);
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
     * @return true if request completed successfully; false otherwise
     */
    private boolean checkIncomplete(final ChronoZonedDateTime<LocalDate> now, final HtmlBuilder reasons) {

        final LocalDate deadline;
        boolean ok = false;

        if ("Y".equals(this.studentCourse.iInProgress)) {
            deadline = this.studentCourse.iDeadlineDt;

            if (deadline != null) {

                if (now.toLocalDate().isAfter(deadline)) {
                    reasons.add("Past the deadline date for incompletes.");
                } else {
                    ok = true;
                }
            } else {
                reasons.add("The course has no incomplete deadline date.");
            }
        } else {
            reasons.add("You are no longer eligible to work on this course.");
        }

        return ok;
    }

    /**
     * If the exam enforces a limit on the total number of attempts or number of attempts on a single version, test
     * those limits.
     *
     * @param cache   the data cache
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param avail   the exam to test
     * @return true if request completed successfully; false otherwise
     * @throws SQLException of there is an error accessing the database
     */
    private boolean checkNumAttempts(final Cache cache, final HtmlBuilder reasons,
                                     final AvailableExam avail) throws SQLException {

        int maxAttempts = 0;
        final int graded;

        // Attempt limits only apply to UNIT, MIDTERM, or FINAL exams.
        final String type = avail.exam.examType;
        if (!"U".equals(type) && !"F".equals(type)) {
            return true;
        }

        if (this.courseSectionUnit.nbrAtmptsAllow != null) {
            maxAttempts = this.courseSectionUnit.nbrAtmptsAllow.intValue();
        }

        if (maxAttempts == 0) {
            // no limit
            return true;
        }

        // There is a limit on total attempts, so determine total attempts so far, including
        // possible ungraded (batch) attempts, and test that value against the maximum allowed
        // attempts.

        // Count the number of graded attempts on this exam
        final List<RawStexam> examList = RawStexamLogic.getExams(cache, this.studentId,
                avail.exam.course, avail.exam.unit, false, type);
        graded = examList.size();

        if (graded >= maxAttempts) {
            reasons.add("Maximum number of attempts exceeded.");
            return false;
        }

        return true;
    }

    /**
     * If the exam requires the student be registered in the exam's course, test that this is so.
     *
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return true if request completed successfully; false otherwise
     */
    private boolean checkCourseRegistration(final HtmlBuilder reasons) {

        if ("OT".equals(this.studentCourse.instrnType)) {
            // Not properly registered for the course
            reasons.add("Advance placement indicated in course.  Exams not available.");
            return false;
        }

        return true;
    }

    /**
     * See whether the current time of day falls within the daily testing windows, and whether the selected exam has an
     * open testing window now.
     *
     * @param now              the date/time to consider as "now"
     * @param reasons          a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param checkEligibility true if student eligibility to take the exam should be enforced, false if not
     * @return true if request completed successfully; false otherwise
     */
    private boolean checkTimeWindows(final ZonedDateTime now, final HtmlBuilder reasons,
                                     final boolean checkEligibility) {

        // Compute the day ranges for the testing window.
        final LocalDate today = now.toLocalDate();

        final LocalDate day1 = Objects.requireNonNullElseGet(this.courseSectionUnit.firstTestDt,
                () -> today.minusDays(1L));
        final LocalDate day2 = Objects.requireNonNullElseGet(this.courseSectionUnit.lastTestDt,
                () -> today.plusDays(1L));
        final LocalDate day3 = today.plusDays(1L);

        // If the last coupon date is in the past, the course is unavailable.
        if (day3.isBefore(today) && checkEligibility) {
            reasons.add("Course not currently available.");
            return false;
        }

        // If we are past the last testing date for the unit, but still within the coupon test
        // date range, see if the student has enough coupons.
        if (day1.isAfter(today)) {
            Log.info("Before start of testing window " + this.courseSectionUnit.firstTestDt.toString());
            reasons.add("Outside testing window.");
        } else if (day2.isBefore(today)) {
            Log.info("After end of testing window " + this.courseSectionUnit.lastTestDt.toString());
            reasons.add("Outside testing window.");
        }

        return true;
    }
}
