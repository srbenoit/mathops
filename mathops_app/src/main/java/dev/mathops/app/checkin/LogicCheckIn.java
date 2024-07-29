package dev.mathops.app.checkin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.logic.PlacementLogic;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class contains all logic to determine, based on a student ID, the list of proctored exams the student is
 * eligible to take, and the reasons why other exams are unavailable.
 */
public final class LogicCheckIn {

    /** A commonly used constant. */
    private static final Integer FOUR = Integer.valueOf(4);

    /** A commonly used string. */
    private static final String ERROR = "Error";

    /** A commonly used string. */
    private static final String NOT_IMPLEMENTED = "Not Implemented";

    /** A commonly used string. */
    private static final String SEND_TO_OFFICE = "Please send student to the office.";

    /** A commonly used string. */
    private static final String ACCOMPANY_TO_OFFICE = "Please accompany student to the office.";

    /** The context. */
    private final Cache cache;

    /** The current date. */
    private final ZonedDateTime curDate;

    /** The current day number. */
    private final LocalDate today;

    /** The currently active term. */
    private TermRec activeTerm = null;

    /**
     * Constructs a new {@code LogicCheckIn}.
     *
     * @param theCache the cache
     * @param now      the date/time to consider as "now"
     */
    public LogicCheckIn(final Cache theCache, final ZonedDateTime now) {

        this.cache = theCache;
        this.curDate = now;
        this.today = now.toLocalDate();
    }

    /**
     * Performs one-time initializations from the database. These do not need to be repeated for each student processed.
     * At the moment, this only consists of querying and caching the active term record and checking that the current
     * date falls within the active term.
     *
     * @return {@code true} if initialization succeeded; {@code false} otherwise.
     * @throws SQLException if there is an error accessing the database
     */
    public boolean isInitialized() throws SQLException {

        boolean ok = false;

        this.activeTerm = this.cache.getSystemData().getActiveTerm();

        if (this.activeTerm != null) {
            if (this.activeTerm.startDate == null) {
                final String msg = "Active term has no start date configured.";
                JOptionPane.showMessageDialog(null, msg, ERROR, JOptionPane.ERROR_MESSAGE);
                Log.warning(msg);
            } else if (this.activeTerm.endDate == null) {
                final String msg = "Active term has no end date configured.";
                JOptionPane.showMessageDialog(null, msg, ERROR, JOptionPane.ERROR_MESSAGE);
                Log.warning(msg);
            } else {
                final LocalDate start = this.activeTerm.startDate;

                if (this.today.isBefore(start)) {
                    final String msg = "Active term has not yet started.";
                    JOptionPane.showMessageDialog(null, msg, ERROR, JOptionPane.ERROR_MESSAGE);
                    Log.warning(msg);
                } else {
                    final LocalDate end = this.activeTerm.endDate;

                    if (this.today.isAfter(end)) {
                        final String msg = "Active term has ended.";
                        JOptionPane.showMessageDialog(null, msg, ERROR, JOptionPane.ERROR_MESSAGE);
                        Log.warning(msg);
                    } else {
                        ok = true;
                    }
                }
            }
        }

        return ok;
    }

    /**
     * Given a student ID, determine the list of proctored exams the student is eligible for, and return a data class
     * populated with the available exams list and other useful student information.
     *
     * @param studentId          the ID of the student for whom to gather information
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @return student information and the list of available proctored exams, encapsulated in a
     *         {@code StudentCheckInInfo} object, or {@code null} if an error in processing occurs
     * @throws SQLException if there is an error accessing the database
     */
    public DataCheckInAttempt performCheckInLogic(final String studentId, final boolean enforceEligibility)
            throws SQLException {

        DataCheckInAttempt info = null;

        if (this.activeTerm == null || isInitialized()) {
            info = processCheckIn(studentId, enforceEligibility);
        }

        return info;
    }

    /**
     * Processes the check-in request.
     *
     * @param studentId          the student ID
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @return student information and the list of available proctored exams, encapsulated in a
     *         {@code StudentCheckInInfo} object, or {@code null} if an error in processing occurs
     * @throws SQLException if there is an error accessing the database
     */
    private DataCheckInAttempt processCheckIn(final String studentId, final boolean enforceEligibility)
            throws SQLException {

        final String[] error = new String[2];
        final DataStudent studentData = loadDataOnStudent(studentId, error);

        final DataCheckInAttempt info = new DataCheckInAttempt(studentData);

        final boolean ok = error[0] == null;
        if (error[0] != null) {
            info.error = error;
        }

        // Verify there are no outstanding exams in progress for the student. If there are, we may offer to let the
        // student exchange calculators. NOTE: We now do this before the holds test to prevent a student who is
        // exchanging calculator from being shown the same hold messages as were shown at initial check-in.
        if (ok && isNoExamInProgress(info)) {

            final LogicCheckInCourseExams courseExamsLogic = new LogicCheckInCourseExams(this.today, this.activeTerm,
                    info);
            courseExamsLogic.execute(this.cache, enforceEligibility);

            // Now we determine which exams the student is eligible to take. The logic is split into evaluating
            // non-course exams (placement exams, paper exams, etc.) and exams within courses.
            determineAvailableNonCourseExams(info, enforceEligibility);
        }

        return info;
    }

    /**
     * Loads the student data.
     *
     * @param stuId the student ID
     * @param error an 2-string array to populate with any error encountered.
     * @return the loaded student data; {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private DataStudent loadDataOnStudent(final String stuId, final String[] error) throws SQLException {

        DataStudent data = null;

        final RawStudent stu = RawStudentLogic.query(this.cache, stuId, false);

        if (stu == null) {
            error[0] = "STUDENT record not found.";
            error[1] = SEND_TO_OFFICE;
        } else {
            RawStterm stterm = RawSttermLogic.query(this.cache, this.activeTerm.term, stuId);
            if (stterm == null) {
                // Attempt to construct an accurate STTERM record
                final List<RawStcourse> allRegs = RawStcourseLogic.getActiveForStudent(this.cache, stuId,
                        this.activeTerm.term);
                final int pace = PaceTrackLogic.determinePace(allRegs);
                if (pace > 0) {
                    final String paceTrack = PaceTrackLogic.determinePaceTrack(allRegs, pace);
                    final String first = PaceTrackLogic.determineFirstCourse(allRegs);

                    final Integer paceObj = Integer.valueOf(pace);
                    stterm = new RawStterm(this.activeTerm.term, stuId, paceObj, paceTrack, first, null, null, null);
                    try {
                        RawSttermLogic.INSTANCE.insert(this.cache, stterm);
                    } catch (final SQLException ex) {
                        // Even if this insert fails, we can continue with the STTERM row we have created
                        Log.warning(ex);
                    }
                }
            }

            final List<RawAdminHold> holds = RawAdminHoldLogic.queryByStudent(this.cache, stuId);
            final int numHolds = holds.size();
            final List<String> holdsToShow = new ArrayList<>(numHolds);

            for (final RawAdminHold hold : holds) {
                if ("F".equalsIgnoreCase(hold.sevAdminHold)) {
                    error[0] = "Student has an administrative hold.";
                    error[1] = SEND_TO_OFFICE;
                } else {
                    final String msg = RawAdminHoldLogic.getStaffMessage(hold.holdId);
                    if (msg != null) {
                        holdsToShow.add(msg);
                    }
                }
            }

            if (error[0] == null) {
                final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByStudent(this.cache, stuId,
                        this.today);
                final int numSpecials = specials.size();
                final List<String> specialTypes = new ArrayList<>(numSpecials);
                for (final RawSpecialStus spec : specials) {
                    specialTypes.add(spec.stuType);
                }

                data = new DataStudent(stu, stterm, holdsToShow, specialTypes);
            }
        }

        return data;
    }

    /**
     * Tests whether there are existing in-progress exam records for the student.
     *
     * @param info the data object to populate with pending exam information
     * @return {@code true} if there are no exams pending for the student; {@code false} if there are pending exams
     * @throws SQLException if there is an error accessing the database
     */
    private boolean isNoExamInProgress(final DataCheckInAttempt info) throws SQLException {

        final boolean result;

        final List<RawPendingExam> open = RawPendingExamLogic.queryByStudent(this.cache, info.studentData.stuId);

        if (open.size() > 1) {
            info.error = new String[]{"The student is currently taking multiple exams.", ACCOMPANY_TO_OFFICE};
            result = false;
        } else if (open.size() == 1) {
            info.error = new String[]{"The student is currently taking an exam.", ACCOMPANY_TO_OFFICE};
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    /**
     * Determines the list of non-course exams the student is eligible to take, based on their course registrations,
     * licensed status, placement history, prerequisites, incomplete status, and so on. The list of exams the student is
     * eligible for is compiled into the {@code availableExams} field in the {@code StudentCheckInInfo} object.
     *
     * @param info               the data object to populate with available exams
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    private void determineAvailableNonCourseExams(final DataCheckInAttempt info, final boolean enforceEligibility)
            throws SQLException {

        final SystemData systemData = this.cache.getSystemData();

        // See if there is an active user's exam
        boolean searchingForUsersExam = true;
        final List<RawExam> exams = systemData.getActiveExams(RawRecordConstants.M100U);
        for (final RawExam exam : exams) {
            if ("Q".equals(exam.examType)) {
                searchingForUsersExam = false;
                break;
            }
        }
        if (searchingForUsersExam) {
            info.nonCourseExams.usersExam = DataExamStatus.unavailable(RawRecordConstants.M100U, 0,
                    NOT_IMPLEMENTED);
        } else {
            info.nonCourseExams.usersExam = DataExamStatus.available(RawRecordConstants.M100U, 0);
        }

        // See if there is an active tutorial exam
        testElmExamAvailability(info, enforceEligibility);
        testPrecalcTutorialAvailability(info, enforceEligibility);

        // Math Placement Tool
        final PlacementLogic logic = new PlacementLogic(this.cache, info.studentData.stuId,
                info.studentData.student.aplnTerm, this.curDate);
        final PlacementStatus status = logic.status;

        if (status.availableLocalProctoredIds.contains("MPTTC")) {
            info.nonCourseExams.placement = DataExamStatus.available(RawRecordConstants.M100P, 1);
        } else if (enforceEligibility) {
            final String msg = Objects.requireNonNullElse(status.shortWhyProctoredUnavailable, "No attempts left");
            info.nonCourseExams.placement = DataExamStatus.unavailable(RawRecordConstants.M100P, 1, msg);
        } else {
            info.nonCourseExams.placement = DataExamStatus.available(RawRecordConstants.M100P, 1);
            info.nonCourseExams.placement.eligibilityOverrides.add("All attempts on MPT have been used.");
        }

        if (!enforceEligibility) {
            info.nonCourseExams.elmExam.annotateIneligible();
            info.nonCourseExams.precalc117.annotateIneligible();
            info.nonCourseExams.precalc118.annotateIneligible();
            info.nonCourseExams.precalc124.annotateIneligible();
            info.nonCourseExams.precalc125.annotateIneligible();
            info.nonCourseExams.precalc126.annotateIneligible();
            info.nonCourseExams.usersExam.annotateIneligible();
            info.nonCourseExams.placement.annotateIneligible();
        }
    }

    /**
     * Checks the student's eligibility for the ELM Exam.
     *
     * @param info               the data object to populate with available exams
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    private void testElmExamAvailability(final DataCheckInAttempt info, final boolean enforceEligibility)
            throws SQLException {

        final DataExamStatus data = DataExamStatus.available(RawRecordConstants.M100T, 0);
        info.nonCourseExams.elmExam = data;

        // If there exist any passed unit exams in this unit, the unit is henceforth available for unit exams.
        final List<RawStexam> stexams = RawStexamLogic.getExams(this.cache, info.studentData.stuId,
                RawRecordConstants.M100T, FOUR, true, RawStexamLogic.UNIT_EXAM_TYPES);

        if (stexams.isEmpty()) {
            // If the student has a passing review exam on record, make the exam available, subject to the limits on
            // the number of attempts per passing review exam.
            final List<RawStexam> stReviewExams = RawStexamLogic.getExams(this.cache, info.studentData.stuId,
                    RawRecordConstants.M100T, FOUR, true, RawStexamLogic.REVIEW_EXAM_TYPES);

            if (stReviewExams.isEmpty()) {
                Log.info("Removing M 100T unit 4 (no passing review exam)");
                if (enforceEligibility) {
                    data.indicateUnavailable("Must pass review");
                } else {
                    data.eligibilityOverrides.add("Unit 4 review exam not passed");
                }
            }

            final SystemData systemData = this.cache.getSystemData();

            // Get the unit configuration
            final RawCusection unitData = systemData.getCourseUnitSection(RawRecordConstants.M100T, "1", FOUR,
                    this.activeTerm.term);

            if (unitData == null) {
                Log.warning("No Course-Unit-Section data for M 100T section 1, unit 4");
                data.indicateUnavailable("No CUSECTION");
            } else if (Objects.nonNull(unitData.atmptsPerReview) && unitData.atmptsPerReview.intValue() > 0) {

                final int count = RawStexamLogic.countUnitSinceLastPassedReview(this.cache, info.studentData.stuId,
                        RawRecordConstants.M100T, FOUR);

                if (count >= unitData.atmptsPerReview.intValue()) {
                    Log.info("Removing M 100T unit 4 (no attempts remaining)");
                    if (enforceEligibility) {
                        if (data.available) {
                            data.indicateUnavailable("Must repass review");
                        }
                    } else {
                        data.eligibilityOverrides.add("Needs to re-pass Unit 4 Review exam");
                    }
                }

                if (unitData.lastTestDt.isBefore(this.today)) {
                    data.indicateUnavailable("Past last test dt");
                }
            }
        } else {
            // Student has already passed ELM exam - it is available (but useless)
            data.note = "(already passed)";
        }
    }

    /**
     * Checks the student's eligibility for any Precalculus Tutorial exams, and if so, add the appropriate exam to the
     * eligible list. The logic assumes that a student may be eligible for only ONE tutorial exam at a time; the exam
     * selected is the highest unit for which the student qualifies.
     *
     * @param info               the data object to populate with available exams
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    private void testPrecalcTutorialAvailability(final DataCheckInAttempt info, final boolean enforceEligibility)
            throws SQLException {

        final SystemData systemData = this.cache.getSystemData();

        for (final CourseNumbers numbers : CourseNumbers.COURSES) {

            final String course = numbers.tutorialId();
            boolean passedReview = false;
            boolean passedUnit = false;

            // See if there is a passing unit 4 review exam but no passing unit exam
            final List<RawStexam> stexams = RawStexamLogic.getExams(this.cache, info.studentData.stuId, course, FOUR,
                    true, "R", "U");

            for (final RawStexam exam : stexams) {
                if ("R".equals(exam.examType)) {
                    passedReview = true;
                } else if ("U".equals(exam.examType)) {
                    passedUnit = true;
                }
            }

            final DataExamStatus status = DataExamStatus.available(course, 4);

            final RawCusection unitData = systemData.getCourseUnitSection(course, "1", FOUR, this.activeTerm.term);

            if (unitData == null) {
                Log.warning("No Course-Unit-Section data for ", course, " section 1, unit 4");
                status.indicateUnavailable("No CUSECTION");
            } else {
                if (passedUnit) {
                    if (enforceEligibility) {
                        if (status.available) {
                            status.indicateUnavailable("Already passed");
                        }
                    } else {
                        status.eligibilityOverrides.add("Already passed Tutorial exam");
                        if (!passedReview) {
                            status.eligibilityOverrides.add("Needs to pass Review exam.");
                        }
                    }
                } else if (passedReview) {
                    if (Objects.nonNull(unitData.atmptsPerReview) && unitData.atmptsPerReview.intValue() > 0) {
                        final int count = RawStexamLogic.countUnitSinceLastPassedReview(this.cache,
                                info.studentData.stuId, course, FOUR);

                        if (count >= unitData.atmptsPerReview.intValue()) {
                            Log.info("Removing ", course, ", unit 4 (no attempts remain)");
                            if (enforceEligibility) {
                                if (status.available) {
                                    status.indicateUnavailable("Must repass review");
                                }
                            } else {
                                status.eligibilityOverrides.add("Needs to re-pass Unit 4 Review exam");
                            }
                        }
                    }
                } else if (enforceEligibility) {
                    if (status.available) {
                        status.indicateUnavailable("Must pass review");
                    }
                } else {
                    status.eligibilityOverrides.add("Needs to pass Review exam.");
                }

                if (unitData.lastTestDt.isBefore(this.today)) {
                    if (enforceEligibility) {
                        if (status.available) {
                            status.indicateUnavailable("Past last test dt");
                        }
                    } else {
                        status.eligibilityOverrides.add("Past last test date");
                    }
                }
            }

            final String tutorialId = numbers.tutorialId();

            if (RawRecordConstants.M1170.equals(tutorialId)) {
                info.nonCourseExams.precalc117 = status;
            } else if (RawRecordConstants.M1180.equals(tutorialId)) {
                info.nonCourseExams.precalc118 = status;
            } else if (RawRecordConstants.M1240.equals(tutorialId)) {
                info.nonCourseExams.precalc124 = status;
            } else if (RawRecordConstants.M1250.equals(tutorialId)) {
                info.nonCourseExams.precalc125 = status;
            } else if (RawRecordConstants.M1260.equals(tutorialId)) {
                info.nonCourseExams.precalc126 = status;
            }
        }
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("LogicCheckIn{cache=", this.cache, ", curDate=", this.curDate, ", today=",
                this.today, ", activeTerm=", this.activeTerm, "}");
    }
}
