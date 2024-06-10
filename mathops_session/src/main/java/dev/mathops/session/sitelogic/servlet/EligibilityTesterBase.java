package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collection;
import java.util.List;

/**
 * A base class with methods common to all eligibility testers.
 */
class EligibilityTesterBase {

    /** The student data object of the student for whom eligibility is being tested. */
    final StudentData studentData;

    /** The student registration record for the course. */
    RawStcourse studentCourse;

    /** The course section configuration. */
    RawCsection courseSection;

    /** The course unit section configuration. */
    RawCusection courseSectionUnit;

    /** The student's pacing structure (only non-null for a real registration). */
    RawPacingStructure pacingStructure;

    /** Flag indicating student may only test in incomplete courses. */
    boolean incompleteOnly;

    /**
     * Creates a new eligibility test class, which can be used to test several exams for eligibility.
     *
     * @param theStudentData the student data object of the student for whom eligibility is being tested
     */
    EligibilityTesterBase(final StudentData theStudentData) {

        this.studentData = theStudentData;
    }

    /**
     * Gets the student data object.
     *
     * @return the student data object
     */
    public final StudentData getStudentData() {

        return this.studentData;
    }

    /**
     * Verify the student submitting the request is really a student, has no fatal holds that would prevent accessing
     * assessments, and (if relevant) has no pending exams on record.
     *
     * @param now        the date/time to consider "now"
     * @param reasons    a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param holds      a list to which to add holds found for the student; null to skip gathering holds
     * @param checkHolds {@code true} to consider holds; false to bypass holds
     * @return {@code true} if request completed successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    final boolean validateStudent(final ChronoZonedDateTime<LocalDate> now, final HtmlBuilder reasons,
                                  final Collection<? super RawAdminHold> holds, final boolean checkHolds)
            throws SQLException {

        boolean ok = true;

        final RawStudent student = this.studentData.getStudentRecord();

        if (student.sevAdminHold != null) {
            ok = processHolds(reasons, holds, checkHolds);
        }

        // See if there are ungraded exams on record for the student
        if (ok) {
            final List<RawPendingExam> pendings = this.studentData.getPendingExams();

            if (!pendings.isEmpty()) {
                Log.info("Ungraded exams exist for student", student.stuId);
                reasons.add("Ungraded exams exist on the student record");
                ok = false;
            }
        }

        return ok;
    }

    /**
     * Read any holds the student has and add the reply. If any of the holds are fatal, return failure, which will
     * prevent further querying of exams.
     *
     * @param reasons    a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param holds      a list to which to add holds found for the student; null to skip gathering holds
     * @param checkHolds {@code true} to consider holds; false to bypass holds
     * @return true if request completed successfully; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean processHolds(final HtmlBuilder reasons, final Collection<? super RawAdminHold> holds,
                                 final boolean checkHolds) throws SQLException {

        final boolean ok;

        // This method is called only if STUDENT record has non-null hold severity.

        final List<RawAdminHold> stuHolds = this.studentData.getHolds();
        final String studentId = this.studentData.getStudentId();

        boolean fatal = false;
        if (stuHolds.isEmpty()) {
            Log.warning("Student record indicated holds for student ", studentId, " when none were present");
            ok = true;
        } else {
            if (holds != null) {
                holds.addAll(stuHolds);
            }
            for (final RawAdminHold hold : stuHolds) {
                if ("F".equals(hold.sevAdminHold)) {
                    fatal = true;
                    break;
                }
            }

            // If there are fatal holds, process them separately
            final RawStudent student = this.studentData.getStudentRecord();
            if ("F".equals(student.sevAdminHold)) {
                if (fatal) {
                    ok = processFatalHolds(stuHolds, reasons, checkHolds);
                } else {
                    Log.warning("Student record '", studentId, "' marked fatal, but no fatal holds exist.");
                    ok = true;
                }
            } else if (fatal) {
                Log.warning("Student record '", studentId, "' has fatal holds but is not marked fatal.");
                ok = processFatalHolds(stuHolds, reasons, checkHolds);
            } else {
                ok = true;
            }
        }

        return ok;
    }

    /**
     * This method is called only if there are fatal holds on the student's record. It checks for the presence of any
     * fatal hold other than a type 30 (an "Incomplete" hold), and if any non-30 holds exists, the student is barred
     * from testing at all. If there is only a hold 30, then the "incompleteOnly" member variable is set to indicate
     * that the student may test only in courses where there is an Incomplete in progress in the database.
     *
     * @param stuHolds   a {@code List} containing the {@code RawAdminHold} objects associated with the student (never
     *                   null)
     * @param reasons    a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param checkHolds {@code true} to consider holds; false to bypass holds
     * @return true if the client's connection should continue; false if it should be terminated
     */
    private static boolean processFatalHolds(final Iterable<RawAdminHold> stuHolds, final HtmlBuilder reasons,
                                             final boolean checkHolds) {

        boolean ok = true;

        // Student can practice any assessment, even with fatal holds
        if (checkHolds) {
            // See if there is a fatal hold with value other than 30, in which case the student may not take
            // assessments at all.
            for (final RawAdminHold hold : stuHolds) {
                if ("F".equals(hold.sevAdminHold) && !"30".equals(hold.holdId)) {
                    reasons.add("We currently have a problem with your precalculus course records that is ",
                            "preventing you from accessing your course assignments.  Please speak with a ",
                            "staff member in the Precalculus Center (Weber 137) as soon as possible.");
                    ok = false;
                    break;
                }
            }
        }

        return ok;
    }

    /**
     * Tests whether the student is in a "special" category that allows access to assessments without a registration.
     *
     * @return true if the student is "special"
     * @throws SQLException if there is an error accessing the database
     */
    final boolean isSpecial() throws SQLException {

        boolean isSpecial = false;

        final LocalDate today = LocalDate.now();
        final List<RawSpecialStus> specials = this.studentData.getActiveSpecialCategories(today);

        for (final RawSpecialStus test : specials) {
            final String type = test.stuType;

            if ("TUTOR".equals(type) || "M384".equals(type) || "ADMIN".equals(type) || "STEVE".equals(type)) {
                isSpecial = true;
                break;
            }
        }

        return isSpecial;
    }

    /**
     * Tests whether the student ID is a "special" ID that should always have access to homework.
     *
     * @return true if the student ID is a "special" ID
     */
    final boolean isSpecialStudentId() {

        final String studentId = this.studentData.getStudentId();

        return "GUEST".equals(studentId) || "AACTUTOR".equals(studentId) || "ETEXT".equals(studentId);
    }

    /**
     * Tests whether the student is in a "special" category that allows access to assessments outside the normal
     * start/end dates of a term.
     *
     * @return true if the student is "special"
     * @throws SQLException if there is an error accessing the database
     */
    final boolean canAccessOutsideTerm() throws SQLException {

        boolean canAccess = false;

        final LocalDate today = LocalDate.now();
        final List<RawSpecialStus> specials = this.studentData.getActiveSpecialCategories(today);

        for (final RawSpecialStus test : specials) {
            final String type = test.stuType;

            if ("TUTOR".equals(type) || "ADMIN".equals(type) || "STEVE".equals(type)) {
                canAccess = true;
                break;
            }
        }

        return canAccess;
    }

    /**
     * Query the active Term from the database (there should be only one record in the Term table with its 'active' flag
     * set), then test the beginning and end dates of the active term. Exams may not be given except during a term. If
     * there is no active term, or if the active term has not yet started or is over, then a non-success result code is
     * set in the reply object. Otherwise, the active term object is stored in the {@code mActiveTerm} member variable.
     *
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return true if request completed successfully; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    final boolean checkActiveTerm(final ZonedDateTime now, final HtmlBuilder reasons) throws SQLException {

        boolean ok = false;

        final LocalDate today = now.toLocalDate();

        final SystemData systemData = this.studentData.getSystemData();
        final TermRec activeTerm = systemData.getActiveTerm();

        if (activeTerm == null) {
            reasons.add("Unable to query the active term.");
        } else if (canAccessOutsideTerm()) {
            ok = true;
        } else if (activeTerm.startDate == null || activeTerm.endDate == null) {
            reasons.add("A system error has occurred: Invalid term specification in database");
        } else {
            final LocalDate start = activeTerm.startDate;

            if (today.isBefore(start)) {
                reasons.add("The term has not yet started. It will begin ", activeTerm.startDate.toString());
            } else {
                final LocalDate end = activeTerm.endDate;

                if (today.isAfter(end)) {
                    reasons.add("The term ended ", activeTerm.endDate.toString(), ".  Testing will resume next term.");
                } else {
                    ok = true;
                }
            }
        }

        return ok;
    }

    /**
     * Query the database for the STCOURSE and CUSECTION data corresponding to the course/unit of the exam, and the
     * section the student is enrolled in (if so).
     *
     * <p>
     * This method populates the "studentCourse", "courseSection", and "courseSectionUnit" fields.
     *
     * @param reasons       a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param course        the course
     * @param unit          the unit
     * @param checkLicensed true if student's "licensed" flag should be checked and used to deny eligibility if the
     *                      course/section indicates licensing is required
     * @return true if information was successfully found; false if an error occurred or the data was not found
     * @throws SQLException if there is an error accessing the database
     */
    final boolean gatherSectionInfo(final HtmlBuilder reasons, final String course, final Integer unit,
                                    final boolean checkLicensed) throws SQLException {

        boolean ok = true;

        final SystemData systemData = this.studentData.getSystemData();

        final RawCourse courseRecord = systemData.getCourse(course);
        final Boolean isTut = courseRecord == null ? null : Boolean.valueOf("Y".equals(courseRecord.isTutorial));

        if (isTut == null) {
            reasons.add("Unable to query for this course");
            ok = false;
        } else if (isTut.booleanValue()) {
            gatherTutorialSectionInfo(course);
        } else {
            ok = gatherCourseSectionInfo(reasons, course);
        }

        if (isTut != null && ok) {
            final TermRec activeTerm = systemData.getActiveTerm();
            final TermKey term = "Y".equals(this.studentCourse.iInProgress) ? this.studentCourse.iTermKey
                    : activeTerm.term;

            this.courseSection = systemData.getCourseSection(course, this.studentCourse.sect, term);

            if (this.courseSection == null) {
                reasons.add("Unable to query course section information");
                ok = false;
            } else {
                final List<RawCusection> cusections = systemData.getCourseUnitSections(course, this.studentCourse.sect,
                        term);
                for (final RawCusection cusect : cusections) {
                    if (cusect.unit.equals(unit)) {
                        this.courseSectionUnit = cusect;
                        break;
                    }
                }

                if (this.courseSectionUnit == null) {
                    reasons.add("No data found for your section of the course.");
                    ok = false;
                } else {
                    String pacingId = null;
                    final RawStudent student = this.studentData.getStudentRecord();

                    if (this.courseSection != null) {
                        pacingId = this.courseSection.pacingStructure;

                        if (!isTut.booleanValue() && "N".equals(this.studentCourse.iInProgress)
                                && student.pacingStructure == null && pacingId != null) {

                            // Set the student's rule set if not yet known and this is a real course
                            Log.info("Setting student pacing to ", pacingId, " as part of testing exam eligibility");
                            final Cache cache = this.studentData.getCache();
                            RawStudentLogic.updatePacingStructure(cache, student.stuId, pacingId);
                            student.pacingStructure = pacingId;
                        }
                    }

                    if (pacingId == null) {
                        reasons.add("Unable to determine pacing structure.");
                        return false;
                    }

                    this.pacingStructure = systemData.getPacingStructure(pacingId, activeTerm.term);
                    if (this.pacingStructure == null) {
                        reasons.add("Unable to look up pacing structure.");
                        return false;
                    }

                    if (checkLicensed && "Y".equals(this.pacingStructure.requireLicensed)
                            && "N".equals(student.licensed)) {
                        reasons.add("You must complete the User's Exam before you can access this assignment.");
                        return false;
                    }
                }
            }
        }

        return ok;
    }

    /**
     * Gathers section info when the course number maps to a Tutorial.
     *
     * <p>
     * This method populates the "studentCourse", "courseSection", and "courseSectionUnit" fields.
     *
     * @param course the course
     * @throws SQLException if there is an error accessing the database
     */
    private void gatherTutorialSectionInfo(final String course) throws SQLException {

        final RawStudent student = this.studentData.getStudentRecord();
        final SystemData systemData = this.studentData.getSystemData();
        final TermRec activeTerm = systemData.getActiveTerm();

        // Make a fake STCOURSE record
        this.studentCourse = new RawStcourse(activeTerm.term, // termKey
                student.stuId, // stuId
                course, // course
                "1", // section
                Integer.valueOf(1), // paceOrder
                "Y", // openStatus
                null, // gradingOption
                "N", // completed
                null, // score
                null, // courseGrade
                "Y", // prereqSaatis
                "N", // initClassRoll
                "N", // stuProvided
                "N", // finalClassRoll
                null, // examPlaced
                null, // zeroUnit
                null, // timeoutFactor
                null, // forfeitI
                "N", // iInProgress
                null, // iCounted
                "N", // ctrlTest
                null, // deferredFDt
                Integer.valueOf(0), // bypassTimeout
                null, // instrnType
                null, // registrationStatus
                null, // lastClassRollDt
                null, // iTermKey
                null); // iDeadlineDt

        this.studentCourse.synthetic = true;
    }

    /**
     * Gathers section info when the course number maps to a Course.
     *
     * <p>
     * This method populates the "studentCourse", "courseSection", and "courseSectionUnit" fields.
     *
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @param course  the course
     * @return true if information was successfully found; false if an error occurred or the data was not found
     * @throws SQLException if there is an error accessing the database
     */
    private boolean gatherCourseSectionInfo(final HtmlBuilder reasons, final String course) throws SQLException {


        boolean ok = true;
        this.studentCourse = this.studentData.getActiveRegistration(course);

        if (this.studentCourse == null) {

            final RawStudent student = this.studentData.getStudentRecord();
            final SystemData systemData = this.studentData.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();

            // Some special student types automatically have access to all precalculus courses without having an
            // actual registration record
            boolean isSpecial = false;
            if (RawRecordConstants.M117.equals(course) || RawRecordConstants.M118.equals(course)
                    || RawRecordConstants.M124.equals(course) || RawRecordConstants.M125.equals(course)
                    || RawRecordConstants.M126.equals(course)
                    || RawRecordConstants.MATH117.equals(course)
                    || RawRecordConstants.MATH118.equals(course)
                    || RawRecordConstants.MATH124.equals(course)
                    || RawRecordConstants.MATH125.equals(course)
                    || RawRecordConstants.MATH126.equals(course)) {

                isSpecial = isSpecial();
            }

            if (isSpecial) {
                // Make a fake STCOURSE record
                String sect = "001";
                final List<RawCsection> csections = systemData.getCourseSectionsByCourse(course, activeTerm.term);
                if (!csections.isEmpty()) {
                    csections.sort(null);
                    sect = csections.getFirst().sect;
                }

                this.studentCourse = new RawStcourse(activeTerm.term, // termKey
                        student.stuId, // stuId
                        course, // course
                        sect, // section
                        Integer.valueOf(1), // paceOrder
                        "Y", // openStatus
                        null, // gradingOption
                        "N", // completed
                        null, // score
                        null, // courseGrade
                        "Y", // prereqSaatis
                        "N", // initClassRoll
                        "N", // stuProvided
                        "N", // finalClassRoll
                        null, // examPlaced
                        null, // zeroUnit
                        null, // timeoutFactor
                        null, // forfeitI
                        "N", // iInProgress
                        null, // iCounted
                        "N", // ctrlTest
                        null, // deferredFDt
                        Integer.valueOf(0), // bypassTimeout
                        null, // instrnType
                        null, // registrationStatus
                        null, // lastClassRollDt
                        null, // iTermKey
                        null); // iDeadlineDt

                this.studentCourse.synthetic = true;
            } else {
                reasons.add("You are not registered in this course.");
                ok = false;
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
     * @param now     the date/time to consider as "now"
     * @param reasons a buffer to populate with the reason the exam is unavailable, to be shown to the student
     * @return true if student is eligible for the exam after this test; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    final boolean checkForCourseLockout(final ChronoZonedDateTime<LocalDate> now, final HtmlBuilder reasons)
            throws SQLException {

        boolean ok = true;

        // If the student has passed the Final, we're done
        boolean stillNeedsFinal = true;

        final List<RawStexam> passedFinals = this.studentData.getStudentExamsByCourseType(this.studentCourse.course,
                true, "F");
        for (final RawStexam test : passedFinals) {
            if (test.course.equals(this.studentCourse.course)) {
                stillNeedsFinal = false;
                break;
            }
        }

        if (stillNeedsFinal) {
            if ("Y".equals(this.studentCourse.iInProgress) && "N".equals(this.studentCourse.iCounted)) {
                final LocalDate deadline = this.studentCourse.iDeadlineDt;
                if (now.toLocalDate().isAfter(deadline)) {
                    reasons.add("Deadline to finish Incomplete has passed.");
                    ok = false;
                }
            } else if (this.studentCourse.paceOrder != null) {
                final SystemData systemData = this.studentData.getSystemData();
                final TermRec activeTerm = systemData.getActiveTerm();

                final RawStterm stterm = this.studentData.getStudentTerm(activeTerm.term);

                if (stterm == null || stterm.pace == null) {
                    reasons.add("Unable to determine your course pace.");
                    ok = false;
                } else {
                    final List<RawMilestone> allMs = systemData.getMilestones(activeTerm.term, stterm.pace,
                            stterm.paceTrack);
                    final List<RawStmilestone> stuMs = this.studentData.getStudentMilestones(activeTerm.term,
                            stterm.paceTrack);

                    // There may not be a "last try", so start with the final deadline
                    LocalDate deadline = null;
                    for (final RawMilestone ms : allMs) {
                        if (ms.getUnit() == 5 && ms.getIndex() == this.studentCourse.paceOrder.intValue()
                                && "FE".equals(ms.msType)) {
                            deadline = ms.msDate;
                            break;
                        }
                    }
                    if (deadline != null) {
                        final int pace = stterm.pace.intValue();

                        for (final RawStmilestone ms : stuMs) {
                            if (ms.getPace() == pace && ms.getUnit() == 5
                                    && ms.getIndex() == this.studentCourse.paceOrder.intValue()
                                    && "FE".equals(ms.msType)) {
                                deadline = ms.msDate;
                            }
                        }
                        for (final RawMilestone ms : allMs) {
                            if (ms.getUnit() == 5 && ms.getIndex() == this.studentCourse.paceOrder.intValue()
                                    && "F1".equals(ms.msType) && ms.msDate.isAfter(deadline)) {
                                deadline = ms.msDate;
                                break;
                            }
                        }
                        for (final RawStmilestone ms : stuMs) {
                            if (ms.getPace() == pace && ms.getUnit() == 5 &&
                                    ms.getIndex() == this.studentCourse.paceOrder.intValue()
                                    && "F1".equals(ms.msType) && ms.msDate.isAfter(deadline)) {
                                deadline = ms.msDate;
                            }
                        }
                    }

                    if (now.toLocalDate().isAfter(deadline)) {
                        reasons.add("Course deadline has passed.");
                        ok = false;
                    }
                }
            }
        }

        return ok;
    }
}
