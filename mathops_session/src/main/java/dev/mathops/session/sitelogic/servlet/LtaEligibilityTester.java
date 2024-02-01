package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rec.AssignmentRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

/**
 * An object to check the eligibility of a student to take a particular learning target assignment.
 */
public class LtaEligibilityTester extends EligibilityTesterBase {

    /**
     * Creates a new eligibility test class, which can be used to test several exams for eligibility.
     *
     * @param theStudentId the student ID for which eligibility is to be tested
     */
    public LtaEligibilityTester(final String theStudentId) {

        super(theStudentId);
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
     * @param cache      the data cache
     * @param now        the date/time to consider as "now"
     * @param assignment the assignment being tested
     * @param reasons    a buffer to which to append the reason the exam is unavailable
     * @param holds      a list to which holds are added if present on the student account
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public final boolean isLtaEligible(final Cache cache, final ZonedDateTime now, final AssignmentRec assignment,
                                       final HtmlBuilder reasons, final Collection<? super RawAdminHold> holds)
            throws SQLException {

        final boolean ok;

        if (validateStudent(cache, now, reasons, holds, true) && checkActiveTerm(cache, now, reasons)) {
            if (isSpecialStudentId() || isSpecial()) {
                ok = true;
            } else {
                ok = checkAssignmentAvailability(cache, now, reasons, assignment);
            }
        } else {
            ok = false;
        }

        return ok;
    }

    /**
     * Determine whether a learning target assignment may be accessed by the student.
     *
     * @param cache   the data cache
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param hw      the homework to test
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkAssignmentAvailability(final Cache cache, final ZonedDateTime now, final HtmlBuilder reasons,
                                                final AssignmentRec hw) throws SQLException {

        final boolean ok;

        if (isSpecialStudentId() || isSpecial()) {
            ok = gatherSectionInfo(cache, reasons, hw.courseId, hw.unit, false);
        } else if (gatherSectionInfo(cache, reasons, hw.courseId, hw.unit, true)) {
            ok = checkSectionStartDate(now, reasons) && checkIncomplete(now, reasons)
                    && checkTimeWindows(now, reasons) && checkObjectiveEligibility(cache, reasons, hw)
                    && checkForCourseLockout(cache, now, reasons);
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
                final String startStr = TemporalUtils.FMT_MDY.format(start);
                reasons.add("This course begins on ", startStr, ".  Assignments are not available until that date.");
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
                reasons.add("Must finish incomplete course first");
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

        boolean ok = true;

        // If the last test date is in the past, the course is unavailable.
        if (day.isBefore(today)) {
            reasons.add("Course is no longer available.");
            ok = false;
        }

        return ok;
    }

    /**
     * The "objective 0" assignment is always available in a unit.  And objectives greater than 0 are available if the
     * unit 0 assignment has been passed.
     *
     * @param cache      the data cache
     * @param reasons    a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param assignment the assignment to test
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkObjectiveEligibility(final Cache cache, final HtmlBuilder reasons,
                                              final AssignmentRec assignment) throws SQLException {

        boolean ok = true;

        // Pacing structure will be null if we are under ADMIN/TUTOR access or similar, which does
        // not enforce objective eligibility rules.

        if (!isSpecial()) {
            final String courseId = assignment.courseId;
            final int unit = assignment.unit == null ? -1 : assignment.unit.intValue();
            final int obj = assignment.objective == null ? -1 : assignment.objective.intValue();

            if (obj > 1) {
                final List<RawSthomework> hws = RawSthomeworkLogic.getHomeworks(cache, this.studentId, courseId,
                        assignment.unit, true, "ST");

                ok = false;
                for (final RawSthomework homework : hws) {
                    if (homework.objective != null && homework.objective.intValue() == 0
                            && "Y".equals(homework.passed)) {
                        ok = true;
                        break;
                    }
                }

                if (!ok) {
                    reasons.add("Skills Review assignment has not yet been completed.");
                }
            }
        }

        return ok;
    }
}
