package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.TemporalUtils;
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
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.reclogic.AssignmentLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.LiveSessionInfo;
import dev.mathops.session.txn.messages.AvailableExam;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An object to check the eligibility of a student to take a particular unit review exam.
 */
public class ReviewExamEligibilityTester extends EligibilityTesterBase {

    /**
     * Create a new eligibility test class, which can be used to test unit review exams for eligibility.
     *
     * @param theStudentId the student ID for which eligibility is to be tested
     */
    public ReviewExamEligibilityTester(final String theStudentId) {

        super(theStudentId);
    }

    /**
     * Check a single exam for eligibility. This method does some initial tests on validity of inputs, then goes through
     * a multistage process to test the eligibility of the student to take the requested exam. Relevant status
     * information is accumulated, including holds on the student record, or the reason the exam cannot be taken at this
     * time.
     *
     * @param cache   the data cache
     * @param now     the date/time to consider as "now"
     * @param avail   the exam being tested
     * @param reasons a buffer to which to append the reason the exam is unavailable
     * @param holds   a list to which holds are added if present on the student account
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public boolean isExamEligible(final Cache cache, final ZonedDateTime now, final AvailableExam avail,
                                  final HtmlBuilder reasons, final Collection<? super RawAdminHold> holds)
            throws SQLException {

        // Verify that there is a term active and currently in progress
        boolean ok = validateStudent(cache, now, reasons, holds, true) && checkActiveTerm(cache, now, reasons);

        // See that the student is registered, and get some additional student information.
        if (ok) {
            final String course = avail.exam.course;
            Log.info("Review exam course is ", course);

            // Test the availability of this particular exam to the student
            if (!isSpecialStudentId() && !isSpecial()) {
                if (RawRecordConstants.M100T.equals(course)) {
                    ok = checkELMTutorialExamAvailability(cache, reasons, avail.exam.course, avail.exam.version);
                } else if (RawRecordConstants.M1170.equals(course)
                        || RawRecordConstants.M1180.equals(course)
                        || RawRecordConstants.M1240.equals(course)
                        || RawRecordConstants.M1250.equals(course)
                        || RawRecordConstants.M1260.equals(course)) {
                    ok = checkPrecalcTutorialExamAvailability(cache, reasons, avail.exam.course, avail.exam.version);
                } else {
                    ok = checkExamAvailability(cache, now, reasons, avail.exam.course, avail.exam.unit);
                }
            }
        }

        return ok;
    }

    /**
     * Check a single exam for eligibility. This method does some initial tests on validity of inputs, then goes through
     * a multistage process to test the eligibility of the student to take the requested exam. Relevant status
     * information is accumulated, including holds on the student record, or the reason the exam cannot be taken at this
     * time.
     *
     * @param cache   the data cache
     * @param now     the date/time to consider as "now"
     * @param course  the course
     * @param unit    the unit
     * @param version the version
     * @param reasons a buffer to which to append the reason the exam is unavailable
     * @param holds   a list to which holds are added if present on the student account
     * @return {@code true} if the request was handled successfully; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean isExamEligible(final Cache cache, final ZonedDateTime now, final String course,
                                   final Integer unit, final String version, final HtmlBuilder reasons,
                                   final Collection<? super RawAdminHold> holds) throws SQLException {

        // Verify that there is a term active and currently in progress
        boolean ok = validateStudent(cache, now, reasons, holds, true)
                && checkActiveTerm(cache, now, reasons);

        if (ok && !isSpecialStudentId() && !isSpecial()) {
            if (RawRecordConstants.M100T.equals(course)) {
                ok = checkELMTutorialExamAvailability(cache, reasons, course, version);
            } else if (RawRecordConstants.M1170.equals(course)
                    || RawRecordConstants.M1180.equals(course)
                    || RawRecordConstants.M1240.equals(course)
                    || RawRecordConstants.M1250.equals(course)
                    || RawRecordConstants.M1260.equals(course)) {
                ok = checkPrecalcTutorialExamAvailability(cache, reasons, course, version);
            } else {
                ok = checkExamAvailability(cache, now, reasons, course, unit);
            }
        }

        return ok;
    }

    /**
     * Get the mastery score for the review exam.
     *
     * @param examType the exam type
     * @return the mastery score
     */
    public final Integer getMasteryScore(final String examType) {

        Integer score = null;

        if (this.courseSectionUnit != null) {
            if ("R".equals(examType) || "Q".equals(examType)) {
                score = this.courseSectionUnit.reMasteryScore;
            } else {
                score = this.courseSectionUnit.ueMasteryScore;
            }
        }

        return score;
    }

    /**
     * Determine whether an online exam may be taken by the student. This involves exam-specific logic, based on
     * parameters stored in the exam, course, and course-unit objects. ALL exam-specific test logic is database-driven,
     * so this code will not need to change as new exams are defined or exam versions are added.
     *
     * @param cache   the data cache
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param course  the course
     * @param version the exam version
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkELMTutorialExamAvailability(final Cache cache, final HtmlBuilder reasons,
                                                     final String course, final String version) throws SQLException {

        final List<RawStexam> passedReviews = RawStexamLogic.getExams(cache, this.studentId, course, true, "R");

        boolean ok = false;

        if ("MT1RE".equals(version)) {
            ok = true;
        } else if ("MT2RE".equals(version)) {
            if (hasPassedReview(passedReviews, 1)) {
                ok = true;
            } else {
                reasons.add("Unit 1 Review Exam not yet passed.");
            }
        } else if ("MT3RE".equals(version)) {
            if (hasPassedReview(passedReviews, 2)) {
                ok = true;
            } else {
                reasons.add("Unit 2 Review Exam not yet passed.");
            }
        } else if ("MT4RE".equals(version)) {
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
     * Determine whether an online exam may be taken by the student. This involves exam-specific logic, based on
     * parameters stored in the exam, course, and course-unit objects. ALL exam-specific test logic is database-driven,
     * so this code will not need to change as new exams are defined or exam versions are added.
     *
     * @param cache   the data cache
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param course  the course
     * @param version the exam version
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkPrecalcTutorialExamAvailability(final Cache cache,
                                                         final HtmlBuilder reasons, final String course,
                                                         final String version) throws SQLException {

        final List<RawStexam> passedReviews =
                RawStexamLogic.getExams(cache, this.studentId, course, true, "R");

        boolean ok = false;

        if ("7TGAT".equals(version) || "7TELM".equals(version) || "8TGAT".equals(version) ||
                "4TGAT".equals(version) || "5TGAT".equals(version) || "6TGAT".equals(version)) {
            ok = true;
        } else if ("7T1RE".equals(version) || "8T1RE".equals(version) || "4T1RE".equals(version)
                || "5T1RE".equals(version) || "6T1RE".equals(version)) {

            if (hasPassedReview(passedReviews, 0)) {
                ok = true;
            } else {
                reasons.add("Skills Review Exam not yet passed.");
            }
        } else if ("7T2RE".equals(version) || "8T2RE".equals(version) || "4T2RE".equals(version)
                || "5T2RE".equals(version) || "6T2RE".equals(version)) {

            if (hasPassedReview(passedReviews, 1)) {
                ok = true;
            } else {
                reasons.add("Unit 1 Review Exam not yet passed.");
            }
        } else if ("7T3RE".equals(version) || "8T3RE".equals(version)
                || "4T3RE".equals(version) || "5T3RE".equals(version)
                || "6T3RE".equals(version)) {

            if (hasPassedReview(passedReviews, 2)) {
                ok = true;
            } else {
                reasons.add("Unit 2 Review Exam not yet passed.");
            }
        } else if ("7T4RE".equals(version) || "8T4RE".equals(version) || "4T4RE".equals(version)
                || "5T4RE".equals(version) || "6T4RE".equals(version)) {

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
     * Determine whether an online exam may be taken by the student. This involves exam-specific logic, based on
     * parameters stored in the exam, course, and course-unit objects. ALL exam-specific test logic is database-driven,
     * so this code will not need to change as new exams are defined or exam versions are added.
     *
     * @param cache   the data cache
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param course  the course
     * @param unit    the unit
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkExamAvailability(final Cache cache, final ZonedDateTime now, final HtmlBuilder reasons,
                                          final String course, final Integer unit) throws SQLException {

        // Next, gather the information from STCOURSE and CUSECTION, since it is used repeatedly in
        // the checks that follow.
        boolean ok = gatherSectionInfo(cache, reasons, course, unit, true) && checkSectionStartDate(now, reasons);

        if ("Y".equals(this.studentCourse.iInProgress)) {
            ok = ok && (checkIncompleteDeadline(now, reasons) && checkCourseRegistration(reasons)
                    && checkTimeWindows(now, reasons) && checkCourseEligibility(reasons)
                    && checkUnitEligibility(cache, reasons, course, unit));
        } else {
            ok = ok && (checkCourseRegistration(reasons) && checkTimeWindows(now, reasons)
                    && checkCourseEligibility(reasons) && checkUnitEligibility(cache, reasons, course, unit)
                    && checkForCourseLockout(cache, now, reasons));
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

        final LocalDate start;
        final boolean ok;

        if (canAccessOutsideTerm() || this.courseSection.startDt == null) {
            ok = true;
        } else {
            start = this.courseSection.startDt;
            final LocalDate today = now.toLocalDate();

            if (start.isAfter(today)) {
                Log.info("Course ", this.studentCourse.course, " section ", this.studentCourse.sect,
                        " has not yet started.");

                reasons.add("This course begins on ", TemporalUtils.FMT_MD.format(this.courseSection.startDt),
                        ".  Exams are not available until that date.");

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
    private boolean checkIncompleteDeadline(final ZonedDateTime now, final HtmlBuilder reasons) {

        final boolean ok;

        if ("Y".equals(this.studentCourse.iInProgress)) {
            final LocalDate deadline = this.studentCourse.iDeadlineDt;

            if (deadline != null) {
                final LocalDate today = now.toLocalDate();

                if (deadline.isBefore(today)) {
                    Log.info("Past the deadline date for incompletes.");
                    reasons.add("Past the deadline date for incompletes.");
                    ok = false;
                } else {
                    ok = true;
                }
            } else {
                Log.info("The course has no incomplete deadline date.");
                reasons.add("The course has no incomplete deadline date.");
                ok = false;
            }
        } else {
            Log.info("You are no longer eligible to work on this course.");
            reasons.add("You are no longer eligible to work on this course.");
            ok = false;
        }

        return ok;
    }

    /**
     * If the exam requires the student be registered in the exam's course, test that this is so.
     *
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return {@code true} if request completed successfully; {@code false} otherwise
     */
    private boolean checkCourseRegistration(final HtmlBuilder reasons) {

        if ("OT".equals(this.studentCourse.instrnType)) {

            // Not properly registered for the course
            Log.info("Advance placement indicated in course, cannot test.");
            reasons.add("Advance placement indicated in course.  Exams not available.");
            return false;
        }

        return true;
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

        boolean success = true;

        if (!"Y".equals(this.studentCourse.iInProgress) && this.courseSectionUnit != null &&
                this.courseSectionUnit.lastTestDt != null) {

            final LocalDate day = this.courseSectionUnit.lastTestDt;
            final LocalDate today = now.toLocalDate();

            // If the last coupon date is in the past, the course is unavailable.
            if (day.isBefore(today)) {
                reasons.add("Course not currently available.");
                Log.info("Course not currently available.");
                success = false;
            }
        }

        return success;
    }

    /**
     * See whether the student is authorized to take any exams in the requested course, based on satisfaction of
     * prerequisites, course open status, and course pacing structure.
     *
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return {@code true} if request completed successfully; {@code false} otherwise
     */
    private boolean checkCourseEligibility(final HtmlBuilder reasons) {

        final boolean ok = "Y".equals(this.studentCourse.openStatus);

        if (!ok) {
            final String msg = this.courseSection.course + " section " + this.courseSection.sect
                    + " is not currently in progress.";
            Log.info(msg);
            reasons.add(msg);
        }

        return ok;
    }

    /**
     * See whether the student is authorized to take any exams in the requested course, based on satisfaction of
     * prerequisites, course open status, and course pacing structure.
     *
     * @param cache   the data cache
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param course  the course
     * @param unit    the unit
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkUnitEligibility(final Cache cache, final HtmlBuilder reasons,
                                         final String course, final Integer unit) throws SQLException {

        final List<RawSthomework> sthw;
        boolean ok = true;

        if (unit == null) {
            Log.info("Exam has no unit number associated with it.");
            reasons.add("Exam has no unit number associated with it.");
            ok = false;
        } else if (!isSpecial()) {
            // Make sure the prerequisite homework assignments have been done.
            final List<AssignmentRec> hws = AssignmentLogic.get(cache).queryActiveByCourseUnit(cache, course, unit,
                    "HW");

            if (hws == null) {
                reasons.add("Unable to query homeworks for this unit.");
                ok = false;
            } else if (!hws.isEmpty()) {
                final AssignmentRec hw = hws.get(hws.size() - 1);

                // See if there is a record of a passed homework submission
                sthw = RawSthomeworkLogic.getHomeworks(cache, this.studentId, course, unit, true, "HW");

                boolean passedLast = false;
                for (final RawSthomework test : sthw) {
                    if ("Y".equals(test.passed) && test.objective.equals(hw.objective)) {
                        passedLast = true;
                        break;
                    }
                }

                if (sthw.isEmpty() || !passedLast) {
                    Log.info("Not enough homeworks submitted to take review exam.");
                    reasons.add("You must complete all homework assignments in the unit before taking the review exam");
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

        final DbProfile dbProfile = ContextMap.getDefaultInstance().getCodeProfile("checkin");

        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final LiveSessionInfo live = new LiveSessionInfo("abcdef", "Local", ERole.STUDENT);

                live.setUserInfo("833291747", "Test", "Student", "Test Student");

                final ImmutableSessionInfo session = new ImmutableSessionInfo(live);

                final ReviewExamEligibilityTester tester = new ReviewExamEligibilityTester(session.userId);

                final Collection<RawAdminHold> holds = new ArrayList<>(10);

                final HtmlBuilder reason = new HtmlBuilder(100);
                final ZonedDateTime now = session.getNow();
                final boolean ok = tester.isExamEligible(cache, now, RawRecordConstants.M125, Integer.valueOf(3),
                        "253RE", reason, holds);

                Log.info("Student  : ", live.getUserId());
                Log.info("Exam     : M 125 unit 3");
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
