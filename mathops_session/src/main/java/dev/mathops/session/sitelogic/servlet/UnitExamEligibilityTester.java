package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.LiveSessionInfo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tests for a student's eligibility to take a Unit exam in a course. The input is a student ID, "now" date/time, course
 * ID, and unit number. The output is an indication that the student is eligible for the exam, or a reason why the
 * student is not eligible.
 *
 * <p>
 * The following criteria are used:
 * <ol>
 * <li>There must be an active term.
 * <li>The course ID must correspond to a course in the database.
 * <li>The unit number must correspond to a unit in the database that has a unit exam (not a Skills
 * Review or Final Exam unit).
 * <li>The student must exist and must not have a fatal hold (except a hold 30, which still permits
 * testing in in-progress Incomplete courses).
 * <li>The student must not have an pending (ungraded) proctored exam in progress.
 * <li>If the rule set associated with the course section requires licensing, the student must be
 * marked as licensed.
 *
 * <li>If the student has the TUTOR or ADMINISTRATOR role:
 * <ol>
 * <li>The exam is available.
 * </ol>
 *
 * <li>If the student has an active (open or completed) registration in the course during the active
 * term:
 * <ol>
 * <li>The student's section must use Unit exams (rather than Midterms).
 * <li>The student must have passed the Unit Review Exam in the specified unit.
 * <li>If the student has not yet passed the Unit Exam, the student must not have submitted N
 * non-passing Unit Exam attempts since the most recent Unit Review Exin the specified unit.
 * <li>The student must not have a fatal hold.
 * <li>The course instruction type must not be "credit by exam".
 * </ol>
 *
 * <li>If the student has an in-progress incomplete (not counted in pace) in the course from a prior
 * term:
 * <ol>
 * <li>The "now" date/time must not be beyond the incomplete deadline date
 * <li>The student's section (from the incomplete term) must use Unit exams (rather than Midterms).
 * <li>The student must have passed the Unit Review Exam in the specified unit.
 * <li>If the student has not yet passed the Unit Exam, the student must not have submitted N
 * non-passing Unit Exam attempts since the most recent Unit Review Exam in the specified unit.
 * <li>The student must not have a fatal hold.
 * </ol>
 *
 * <li>If the student has an in-progress incomplete (counted in pace) in the course from a prior
 * term:
 * <ol>
 * <li>The student's section (from the current term) must use Unit exams.
 * <li>The student must have passed the Unit Review Exam in the specified unit.
 * <li>If the student has not yet passed the Unit Exam, the student must not have submitted N
 * non-passing Unit Exam attempts since the most recent Unit Review Exam in the specified unit.
 * <li>The student must not have a fatal hold.
 * </ol>
 *
 * </ol>
 */
public final class UnitExamEligibilityTester extends EligibilityTesterBase {

    /**
     * Creates a new eligibility test class, which can be used to test several exams for eligibility.
     *
     * @param theStudentId the student ID for which eligibility is to be tested
     */
    public UnitExamEligibilityTester(final String theStudentId) {

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
     * @param cache   the data cache
     * @param session the login session
     * @param avail   the exam being tested
     * @param reasons a buffer to which to append the reason the exam is unavailable
     * @param holds   a list to which to add any holds found (null to skip gathering holds)
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public boolean isExamEligible(final Cache cache, final ImmutableSessionInfo session,
                                  final UnitExamAvailability avail, final HtmlBuilder reasons,
                                  final Collection<? super RawAdminHold> holds)
            throws SQLException {

        final ZonedDateTime now = session.getNow();

        boolean ok = checkActiveTerm(cache, now, reasons)
                && validateStudent(cache, now, reasons, holds, true)
                && gatherSectionInfo(cache, reasons, avail.course, avail.unit, true);

        if (ok) {
            if ("Y".equals(this.studentCourse.iInProgress)) {
                ok = checkIncompleteDeadline(session, reasons)
                        && checkPassedReview(cache, session, reasons, avail)
                        && checkNumAttempts(cache, session, reasons, avail)
                        && checkCourseRegistration(reasons);
            } else {
                ok = checkPassedReview(cache, session, reasons, avail)
                        && checkNumAttempts(cache, session, reasons, avail)
                        && checkCourseRegistration(reasons)
                        && checkUnitTestingDateRange(session, reasons)
                        && checkForCourseLockout(cache, session, reasons);
            }
        }

        return ok;
    }

    /**
     * Called only if the registration is for an incomplete; tests whether the Incomplete deadline date has already
     * passed.
     *
     * @param session the login session
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return true if student is eligible for the exam after this test; false otherwise
     */
    private boolean checkIncompleteDeadline(final ImmutableSessionInfo session,
                                            final HtmlBuilder reasons) {

        boolean ok = false;

        final LocalDate deadline = this.studentCourse.iDeadlineDt;

        if (deadline == null) {
            reasons.add("The course has no incomplete deadline date.");
        } else if (session.getNow().toLocalDate().isAfter(deadline)) {
            reasons.add("Past the deadline date for this incomplete.");
        } else {
            ok = true;
        }

        return ok;
    }

    /**
     * Checks that the student has passed the unit review exam.
     *
     * @param cache   the data cache
     * @param session the login session
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param avail   the exam to test
     * @return true if student is eligible for the exam after this test; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkPassedReview(final Cache cache, final ImmutableSessionInfo session,
                                      final HtmlBuilder reasons, final UnitExamAvailability avail) throws SQLException {

        final boolean ok;

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.INSTRUCTOR) || role.canActAs(ERole.TUTOR)) {
            ok = true;
        } else {
            // Check for passing review exam in the same unit
            final List<RawStexam> passedrev = RawStexamLogic.getExams(cache, this.student.stuId, avail.course,
                    avail.unit, true, "R");

            if (passedrev.isEmpty()) {
                reasons.add("Review exam not yet passed.");
                ok = false;
            } else {
                // See if the unit exam has been passed (so we don't test for N failures since last
                // rev)
                final List<RawStexam> passedunit = RawStexamLogic.getExams(cache,
                        this.student.stuId, avail.course, avail.unit, true, "U");

                if (!passedunit.isEmpty() || this.courseSectionUnit.atmptsPerReview == null) {
                    ok = true;
                } else {
                    RawStexam mostRecentPassedRev = passedrev.getFirst();
                    for (final RawStexam test : passedrev) {
                        if (test.getFinishDateTime() != null
                                && test.getFinishDateTime().isAfter(mostRecentPassedRev.getFinishDateTime())) {
                            mostRecentPassedRev = test;
                        }
                    }

                    // Count failed unit exams since most recent passed review exam
                    final List<RawStexam> allUnit = RawStexamLogic.getExams(cache, this.student.stuId, avail.course,
                            avail.unit, false, "U");

                    int count = 0;
                    for (final RawStexam test : allUnit) {
                        if ("Y".equals(test.passed)) {
                            continue;
                        }
                        if (test.getFinishDateTime() != null
                                && test.getFinishDateTime().isAfter(mostRecentPassedRev.getFinishDateTime())) {
                            ++count;
                        }
                    }

                    if (count >= this.courseSectionUnit.atmptsPerReview.intValue()) {
                        reasons.add("Must pass Unit Review Exam again to earn ",
                                this.courseSectionUnit.atmptsPerReview, " attempts on the Unit Exam.");
                        ok = false;
                    } else {
                        ok = true;
                    }
                }
            }
        }

        return ok;
    }

    /**
     * If the exam enforces a limit on the total number of attempts or number of attempts on a single version, test
     * those limits.
     *
     * @param cache   the data cache
     * @param session the login session
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param avail   the exam to test
     * @return true if student is eligible for the exam after this test; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkNumAttempts(final Cache cache, final ImmutableSessionInfo session,
                                     final HtmlBuilder reasons, final UnitExamAvailability avail) throws SQLException {

        boolean ok = true;

        final ERole role = session.getEffectiveRole();

        if (!(role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.INSTRUCTOR) || role.canActAs(ERole.TUTOR))) {

            int maxAttempts = 0;
            if (this.courseSectionUnit.nbrAtmptsAllow != null) {
                maxAttempts = this.courseSectionUnit.nbrAtmptsAllow.intValue();
            }

            if (maxAttempts > 0) {
                // There is a limit on total attempts, so determine total attempts so far, including
                // possible ungraded (batch) attempts, and test that value against the maximum
                // allowed attempts.

                final List<RawStexam> examList = RawStexamLogic.getExams(cache, this.studentId, avail.course,
                        avail.unit, false, "U");

                if (examList.size() >= maxAttempts) {
                    reasons.add("Maximum number of attempts exceeded.");
                    ok = false;
                }
            }
        }

        return ok;
    }

    /**
     * If the exam requires the student be registered in the exam's course, test that this is so.
     *
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return true if student is eligible for the exam after this test; false otherwise
     */
    private boolean checkCourseRegistration(final HtmlBuilder reasons) {

        boolean ok = true;

        if ("OT".equals(this.studentCourse.instrnType)) {
            // Not properly registered for the course
            reasons.add("Advance placement indicated in course.  Exams not available.");
            ok = false;
        }

        return ok;
    }

    /**
     * See whether the current time of day falls within the daily testing windows, and whether the selected exam has an
     * open testing window now.
     *
     * @param session the login session
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return true if student is eligible for the exam after this test; false otherwise
     */
    private boolean checkUnitTestingDateRange(final ImmutableSessionInfo session, final HtmlBuilder reasons) {

        final boolean ok;

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.INSTRUCTOR) || role.canActAs(ERole.TUTOR)) {
            ok = true;
        } else {
            final LocalDate today = session.getNow().toLocalDate();

            if (this.courseSectionUnit.firstTestDt != null
                    && this.courseSectionUnit.firstTestDt.isAfter(today)) {
                Log.info("Before start of testing window " + this.courseSectionUnit.firstTestDt);
                reasons.add("Outside testing window.");
                ok = false;
            } else if (this.courseSectionUnit.lastTestDt != null
                    && this.courseSectionUnit.lastTestDt.isBefore(today)) {
                Log.info("After end of testing window " + this.courseSectionUnit.lastTestDt);
                reasons.add("Outside testing window.");
                ok = false;
            } else {
                ok = true;
            }
        }

        return ok;
    }

    /**
     * Tests whether current date is beyond the final exam last try deadline, and the student has not yet passed the
     * final. If today is the last try deadline, the student must not have a final exam attempt yet today (if this
     * method is called, the student is eligible for the final - we assume they were eligible early enough to qualify
     * for the last try).
     *
     * @param cache   the data cache
     * @param session the login session
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return true if student is eligible for the exam after this test; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkForCourseLockout(final Cache cache, final ImmutableSessionInfo session,
                                          final HtmlBuilder reasons) throws SQLException {

        boolean ok = true;

        // If the student has passed the Final, we're done
        boolean finalNotYetPassed = true;

        final List<RawStexam> passedFinals = RawStexamLogic.getExams(cache, this.studentId,
                this.studentCourse.course, true, "F");

        for (final RawStexam test : passedFinals) {
            Log.info(test);

            if (test.course.equals(this.studentCourse.course)) {
                finalNotYetPassed = false;
                break;
            }
        }

        if (finalNotYetPassed && this.studentCourse.paceOrder != null) {
            final RawStterm stterm = RawSttermLogic.query(cache, this.activeTerm.term, this.studentId);

            if (stterm == null || stterm.pace == null) {
                reasons.add("Unable to determine your course pace.");
                ok = false;
            } else {
                final List<RawMilestone> allMs = RawMilestoneLogic.getAllMilestones(cache,
                        this.activeTerm.term, stterm.pace.intValue(), stterm.paceTrack);

                final List<RawStmilestone> stuMs = RawStmilestoneLogic.getStudentMilestones(
                        cache, this.activeTerm.term, stterm.paceTrack, this.studentId);
                stuMs.sort(null);

                // There may not be a "last try", so start with the final deadline
                LocalDate deadline = null;
                for (final RawMilestone ms : allMs) {

                    final int unit = ms.getUnit();
                    final int index = ms.getIndex();

                    if (unit == 5 && "FE".equals(ms.msType)
                            && Integer.valueOf(index).equals(this.studentCourse.paceOrder)) {
                        deadline = ms.msDate;
                        break;
                    }
                }
                if (deadline != null) {
                    for (final RawStmilestone ms : stuMs) {

                        final int unit = ms.getUnit();
                        final int index = ms.getIndex();

                        if (unit == 5 && index == this.studentCourse.paceOrder.intValue()
                                && "FE".equals(ms.msType)) {
                            deadline = ms.msDate;
                            // Don't break - student milestones are sorted by due date, and if there are multiple
                            // matching rows, we want the latest date
                        }
                    }

                    for (final RawMilestone ms : allMs) {

                        final int unit = ms.getUnit();
                        final int index = ms.getIndex();

                        if (unit == 5 && index == this.studentCourse.paceOrder.intValue()
                                && "F1".equals(ms.msType)
                                && ms.msDate.isAfter(deadline)) {
                            deadline = ms.msDate;
                            break;
                        }
                    }
                    for (final RawStmilestone ms : stuMs) {

                        final int unit = ms.getUnit();
                        final int index = ms.getIndex();

                        if (unit == 5 && index == this.studentCourse.paceOrder.intValue()
                                && "F1".equals(ms.msType)
                                && ms.msDate.isAfter(deadline)) {
                            deadline = ms.msDate;
                            // Don't break - student milestones are sorted by due date, and if there are multiple
                            // matching rows, we want the latest date
                        }
                    }
                }

                if (deadline != null && session.getNow().toLocalDate().isAfter(deadline)) {
                    reasons.add("Course deadline has passed.");
                    ok = false;
                }
            }
        }

        return ok;
    }

    /**
     * Main method to test this class.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final DbProfile dbProfile = ContextMap.getDefaultInstance().getCodeProfile(//
                "checkin");

        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final LiveSessionInfo live = new LiveSessionInfo("abcdef",
                        "Local", ERole.STUDENT);

                live.setUserInfo("831599651", "Test",
                        "Student", "Test Student");

                final ImmutableSessionInfo session = new ImmutableSessionInfo(live);

                final UnitExamAvailability avail =
                        new UnitExamAvailability(RawRecordConstants.M125, Integer.valueOf(4));

                final UnitExamEligibilityTester tester = new UnitExamEligibilityTester(session.userId);

                final Collection<RawAdminHold> holds = new ArrayList<>(2);
                final HtmlBuilder reason = new HtmlBuilder(100);
                final boolean ok = tester.isExamEligible(cache, session, avail, reason, holds);

                Log.info("Student  : ", live.getUserId());
                Log.info("Exam     : ", avail.course, " Unit ", avail.unit);
                Log.info("Eligible : ", Boolean.toString(ok));
                if (!ok) {
                    Log.info("Reason   : ", reason.toString());
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
