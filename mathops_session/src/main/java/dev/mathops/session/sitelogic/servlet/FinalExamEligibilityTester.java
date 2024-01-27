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
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.LiveSessionInfo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tests for a student's eligibility to take a Final exam in a course. The input is a student ID, "now" date/time,
 * course ID, and unit number. The output is an indication that the student is eligible for the exam, or a reason why
 * the student is not eligible.
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
public class FinalExamEligibilityTester extends EligibilityTesterBase {

    /**
     * Creates a new eligibility test class, which can be used to test several exams for eligibility.
     *
     * @param theStudentId the student ID for which eligibility is to be tested
     */
    public FinalExamEligibilityTester(final String theStudentId) {

        super(theStudentId);
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
     * @param holds   a list to which to add holds on the student account
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public final boolean isExamEligible(final Cache cache, final ImmutableSessionInfo session,
                                        final FinalExamAvailability avail, final HtmlBuilder reasons,
                                        final Collection<? super RawAdminHold> holds) throws SQLException {

        final ZonedDateTime now = session.getNow();

        boolean ok = validateStudent(cache, now, reasons, holds, true)
                && checkActiveTerm(cache, now, reasons)
                && gatherSectionInfo(cache, reasons, avail.course, avail.unit, true);

        if (ok) {
            if ("Y".equals(this.studentCourse.iInProgress)) {
                ok = checkIncompleteDeadline(session, reasons) //
                        && checkPassedUnit(cache, session, reasons, avail) //
                        && checkNumAttempts(cache, session, reasons, avail) //
                        && checkCourseRegistration(reasons) //
                        && checkUnitTestingDateRange(session, reasons); //
            } else {
                ok = checkPassedUnit(cache, session, reasons, avail) //
                        && checkNumAttempts(cache, session, reasons, avail) //
                        && checkCourseRegistration(reasons) //
                        && checkUnitTestingDateRange(session, reasons) //
                        && checkForCourseLockout(cache, session.getNow(), reasons); //
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
     * Checks that the student has passed the unit exam in the unit before the final.
     *
     * @param cache   the data cache
     * @param session the login session
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param avail   the exam to test
     * @return true if student is eligible for the exam after this test; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkPassedUnit(final Cache cache, final ImmutableSessionInfo session,
                                    final HtmlBuilder reasons, final FinalExamAvailability avail) throws SQLException {

        final boolean ok;

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.INSTRUCTOR)
                || role.canActAs(ERole.TUTOR)) {
            ok = true;
        } else {
            // Check for passing unit exam in the prior unit
            final List<RawStexam> passedrev = RawStexamLogic.getExams(cache, this.student.stuId,
                    avail.course, Integer.valueOf(avail.unit.intValue() - 1), true, //
                    "U");

            if (passedrev.isEmpty()) {
                reasons.add("Unit " + (avail.unit.intValue() - 1) + " exam not yet passed.");
                ok = false;
            } else {
                ok = true;
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
                                     final HtmlBuilder reasons, final FinalExamAvailability avail) throws SQLException {

        boolean ok = true;

        final ERole role = session.getEffectiveRole();

        if (!(role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.INSTRUCTOR)
                || role.canActAs(ERole.TUTOR))) {
            int maxAttempts = 0;
            if (this.courseSectionUnit.nbrAtmptsAllow != null) {
                maxAttempts = this.courseSectionUnit.nbrAtmptsAllow.intValue();
            }

            if (maxAttempts > 0) {
                // There is a limit on total attempts, so determine total attempts so far, including
                // possible ungraded (batch) attempts, and test that value against the maximum
                // allowed attempts.

                final List<RawStexam> examList = RawStexamLogic.getExams(cache, this.studentId,
                        avail.course, avail.unit, false, "U");

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
    private boolean checkUnitTestingDateRange(final ImmutableSessionInfo session,
                                              final HtmlBuilder reasons) {

        final boolean ok;

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.INSTRUCTOR)
                || role.canActAs(ERole.TUTOR)) {
            ok = true;
        } else {
            final LocalDate today = session.getNow().toLocalDate();

            if (this.courseSectionUnit.firstTestDt != null
                    && this.courseSectionUnit.firstTestDt.isAfter(today)) {
                Log.info("Before start of testing window "
                        + this.courseSectionUnit.firstTestDt);
                reasons.add("Outside testing window.");
                ok = false;
            } else if (this.courseSectionUnit.lastTestDt != null
                    && this.courseSectionUnit.lastTestDt.isBefore(today)) {
                Log.info("After end of testing window "
                        + this.courseSectionUnit.lastTestDt);
                reasons.add("Outside testing window.");
                ok = false;
            } else {
                ok = true;
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

        final DbProfile dbProfile = ContextMap.getDefaultInstance().getCodeProfile("checkin");
        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final LiveSessionInfo live = new LiveSessionInfo("abcdef", "Local", ERole.STUDENT);

                live.setUserInfo("833155611", "Test", "Student", "Test Student");

                final ImmutableSessionInfo session = new ImmutableSessionInfo(live);

                final FinalExamAvailability avail = new FinalExamAvailability(RawRecordConstants.M117,
                        Integer.valueOf(5));

                final FinalExamEligibilityTester tester = new FinalExamEligibilityTester(session.userId);

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
