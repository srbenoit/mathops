package dev.mathops.app.adm.testing;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.TermKey;
import dev.mathops.db.logic.PlacementLogic;
import dev.mathops.db.logic.PlacementStatus;
import dev.mathops.db.logic.PrerequisiteLogic;
import dev.mathops.db.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.rawlogic.RawCsectionLogic;
import dev.mathops.db.rawlogic.RawCunitLogic;
import dev.mathops.db.rawlogic.RawCusectionLogic;
import dev.mathops.db.rawlogic.RawExamLogic;
import dev.mathops.db.rawlogic.RawMilestoneLogic;
import dev.mathops.db.rawlogic.RawPacingRulesLogic;
import dev.mathops.db.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.rawlogic.RawPendingExamLogic;
import dev.mathops.db.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.rawlogic.RawStchallengeLogic;
import dev.mathops.db.rawlogic.RawStcourseLogic;
import dev.mathops.db.rawlogic.RawStexamLogic;
import dev.mathops.db.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.rawlogic.RawSttermLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawAdminHold;
import dev.mathops.db.rawrecord.RawCsection;
import dev.mathops.db.rawrecord.RawCunit;
import dev.mathops.db.rawrecord.RawCusection;
import dev.mathops.db.rawrecord.RawExam;
import dev.mathops.db.rawrecord.RawMilestone;
import dev.mathops.db.rawrecord.RawPacingStructure;
import dev.mathops.db.rawrecord.RawPendingExam;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.db.rawrecord.RawSpecialStus;
import dev.mathops.db.rawrecord.RawStchallenge;
import dev.mathops.db.rawrecord.RawStcourse;
import dev.mathops.db.rawrecord.RawStexam;
import dev.mathops.db.rawrecord.RawStmilestone;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.db.svc.term.TermRec;

import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class contains all logic to determine, based on a student ID, the list of proctored exams the student is
 * eligible to take, and the reasons why other exams are unavailable.
 */
final class CheckinLogic {

    /** The context. */
    private final Cache cache;

    /** The current date. */
    private final ZonedDateTime curDate;

    /** The current day number. */
    private final LocalDate today;

    /** The currently active term. */
    private TermRec activeTerm;

    /** The unit of the tutorial the student is eligible for. */
    private Integer tutorialUnit;

    /**
     * Constructs a new {@code CheckinLogic}.
     *
     * @param theCache the data cache
     */
    CheckinLogic(final Cache theCache) {

        this.cache = theCache;
        this.curDate = ZonedDateTime.now();
        this.today = LocalDate.now();
    }

    /**
     * Performs one-time initializations from the database. These do not need to be repeated for each student processed.
     * At the moment, this only consists of querying and caching the active term record.
     *
     * @return {@code true} if initialization succeeded; {@code false} otherwise.
     * @throws SQLException if there is an error accessing the database
     */
    private boolean init() throws SQLException {

        boolean ok = false;

        this.activeTerm = TermLogic.get(this.cache).queryActive(this.cache);
        if (this.activeTerm != null) {

            if (this.activeTerm.startDate == null) {
                JOptionPane.showMessageDialog(null, "Invalid term specification in database.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                Log.warning("Active term has no start date");
            } else if (this.activeTerm.endDate == null) {
                JOptionPane.showMessageDialog(null, "Invalid term specification in database.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                Log.warning("Active term has no end date");
            } else {
                final LocalDate start = this.activeTerm.startDate;

                if (this.today.isBefore(start)) {
                    JOptionPane.showMessageDialog(null, "Active term has not yet started.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    Log.warning("Active term has not yet started.");
                } else {
                    final LocalDate end = this.activeTerm.endDate;

                    if (this.today.isAfter(end)) {
                        JOptionPane.showMessageDialog(null, "Active term has ended.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        Log.warning("Active term has ended.");
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
     * @param studentId        the ID of the student for whom to gather information
     * @param checkEligibility true to enforce all eligibility requirements; false to relax some
     * @return student information and the list of available proctored exams, encapsulated in a
     *         {@code StudentCheckinInfo} object, or {@code null} if an error in processing occurs
     */
    StudentCheckinInfo performCheckinLogic(final String studentId, final boolean checkEligibility) {

        StudentCheckinInfo info = null;

        try {
            // Make sure initialization has been run.
            if (this.activeTerm == null) {
                init();
            }

            // Test whether initialization succeeded, and if not, abort
            if (this.activeTerm != null) {
                // Create the student information structure to be populated.
                info = new StudentCheckinInfo(studentId);

                // Retrieve the student record if possible, and store it in the
                // <code>StudentCheckinInfo</code> object. If not found, log an error and abort
                // the checkin attempt.
                boolean ok = queryStudentRecord(info);

                // Verify there are no outstanding exams in progress for the student. If there
                // are, we may offer to let the student exchange calculators. NOTE: We now do
                // this before the holds test to prevent a student who is exchanging calculator
                // from being shown the same hold messages as were shown at initial checkin.
                if (ok) {
                    ok = checkForPendingExams(info);
                    if (!ok) {
                        // Go no further - there is an exam in progress, and the only valid
                        // action is to trade calculators
                        return info;
                    }
                }

                if (ok) {
                    // We now query all special categories the student is a member of.
                    queryStudentSpecials(info);

                    // We now query all holds that apply to the student, and if there are fatal
                    // holds, do not allow testing.
                    ok = queryStudentHolds(info, checkEligibility);

                    // Now we determine which exams the student is eligible to take. The logic
                    // is split into evaluating non-unit exams (placement exams, paper exams,
                    // etc.) and unit exams within courses.
                    if (ok) {
                        determineAvailableNonunitExams(info);
                        determineAvailableCourseExams(info, checkEligibility);
                        determineAvailableChallengeExams(info, checkEligibility);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        return info;
    }

//    /**
//     * Gets the unit of the tutorial the student is eligible for.
//     *
//     * @return the unit
//     */
//    public Integer getTutorialUnit() {
//
//        return this.tutorialUnit;
//    }

    /**
     * Loads the student record into the {@code StudentCheckinInfo} object.
     *
     * @param info the data object to populate with student information
     * @return {@code true} if successful; {@code false} if an error occurred or the student was not found
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryStudentRecord(final StudentCheckinInfo info) throws SQLException {

        final boolean ok;

        info.studentRecord = RawStudentLogic.query(this.cache, info.studentId, false);

        if (info.studentRecord == null) {
            info.error = new String[]{"STUDENT record not found.", "Please send student to the office..."};
            ok = false;
        } else {
            info.studentTermRecord = RawSttermLogic.query(this.cache, this.activeTerm.term, info.studentId);
            ok = true;
        }

        return ok;
    }

    /**
     * Tests whether there are existing in-progress exam records for the student.
     *
     * @param info the data object to populate with pending exam information
     * @return {@code true} if there are no exams pending for the student; {@code false} if there are pending exams
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkForPendingExams(final StudentCheckinInfo info) throws SQLException {

        final boolean result;

        final List<RawPendingExam> open = RawPendingExamLogic.queryByStudent(this.cache, info.studentId);

        if (open.size() > 1) {
            info.error = new String[]{"The student is currently taking multiple exams.",
                    "Please accompany student to the office."};
            result = false;
        } else if (open.size() == 1) {
            info.error = new String[]{"The student is currently taking an exam.",
                    "Please accompany student to the office."};
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    /**
     * Look up the set of special categories the student is a member of.
     *
     * @param info The data object to populate with specials information
     * @throws SQLException if there is an error accessing the database
     */
    private void queryStudentSpecials(final StudentCheckinInfo info) throws SQLException {

        // Load the list of special categories the student is a member of (this query
        // automatically filters on those active right now)
        final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByStudent(this.cache, info.studentId,
                this.curDate.toLocalDate());

        // Scan for fatal holds
        info.specials = new ArrayList<>(specials.size());
        for (final RawSpecialStus spec : specials) {
            info.specials.add(spec.stuType);
        }
    }

    /**
     * Tests whether there are holds that will prevent the student from testing.
     *
     * @param info             the data object to populate with holds information
     * @param checkEligibility true to enforce all eligibility requirements; false to relax some
     * @return {@code true} if successful; {@code false} if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryStudentHolds(final StudentCheckinInfo info, final boolean checkEligibility)
            throws SQLException {

        // Load the list of holds on the student record
        final List<RawAdminHold> holds = RawAdminHoldLogic.queryByStudent(this.cache, info.studentId);

        // Scan for fatal holds
        for (final RawAdminHold hold : holds) {

            if ("F".equalsIgnoreCase(hold.sevAdminHold)) {

                if ("30".equals(hold.holdId)) {

                    // If the student has a hold 30, the student may only test in a course that
                    // has "incomplete in progress" flag set.
                    info.incompleteOnly = true;
                } else if (checkEligibility) {
                    // If there is a fatal hold other than "30", the student may not test
                    info.error = new String[]{"Student has an administrative hold.",
                            "Please send student to the office..."};

                    return false;
                }
            }
        }

        // Accumulate non-fatal holds to be disclosed to the student.
        for (final RawAdminHold hold : holds) {

            if (!"F".equalsIgnoreCase(hold.sevAdminHold)) {

                if (info.holdsToShow == null) {
                    info.holdsToShow = new ArrayList<>(1);
                }

                final String msg = RawAdminHoldLogic.getStaffMessage(hold.holdId);
                info.holdsToShow.add(msg);
            }
        }

        return true;
    }

    /**
     * Determines the list of non-unit exams the student is eligible to take, based on their course registrations,
     * placement history, prerequisites, incomplete status, and so on. The list of exams the student is eligible for is
     * compiled into the {@code availableExams} field in the {@code StudentCheckinInfo} object.
     *
     * @param info the data object to populate with available exams
     * @throws SQLException if there is an error accessing the database
     */
    private void determineAvailableNonunitExams(final StudentCheckinInfo info) throws SQLException {

        boolean fail = true;

        // See if there is an active user's exam of type "
        final List<RawExam> exams = RawExamLogic.queryActiveByCourse(this.cache, RawRecordConstants.M100U);
        for (final RawExam exam : exams) {
            if ("Q".equals(exam.examType)) {
                addAvailableExam(info, RawRecordConstants.M100U, exam.unit, null);
                fail = false;
                break;
            }
        }

        if (fail) {
            addUnavailableExam(info, RawRecordConstants.M100U, Integer.valueOf(0), "Not Implemented");
        }

        // See if there is an active tutorial exam
        checkElmTutorialAvailability(info);
        checkPrecalcTutorialAvailability(info);

        // Math Placement Tool
        final PlacementLogic logic = new PlacementLogic(this.cache, info.studentId, info.studentRecord.aplnTerm,
                this.curDate);
        final PlacementStatus status = logic.status;

        if (status.availableLocalProctoredIds.contains("MPTTC")) {
            addAvailableExam(info, RawRecordConstants.M100P, Integer.valueOf(1), null);
        } else {
            final String msg = Objects.requireNonNullElse(status.shortWhyProctoredUnavailable, "No attempts left");
            addUnavailableExam(info, RawRecordConstants.M100P, Integer.valueOf(1), msg);
        }
    }

    /**
     * Checks the student's eligibility for any ELM Tutorial exams, and if so, add the appropriate exam to the eligible
     * list. The logic assumes that a student may be eligible for only ONE tutorial exam at a time; the exam selected is
     * the highest unit for which the student qualifies.
     *
     * @param info the data object to populate with available exams
     * @throws SQLException if there is an error accessing the database
     */
    private void checkElmTutorialAvailability(final StudentCheckinInfo info) throws SQLException {

        boolean fail = true;

        final List<RawExam> exams = RawExamLogic.queryActiveByCourse(this.cache, RawRecordConstants.M100T);

        // Sort the exams in descending order by unit, and filter out all but unit exams.
        final SortedMap<Integer, RawExam> map = new TreeMap<>();
        for (final RawExam exam : exams) {
            if (RawStexam.UNIT_EXAM.equals(exam.examType) && exam.unit != null) {
                map.put(exam.unit, exam);
            }
        }

        while (!map.isEmpty()) {
            final Integer unit = map.lastKey();

            // If there exist any passed unit exams in this unit, the unit is henceforth
            // available for unit exams.
            final List<RawStexam> stexams = RawStexamLogic.getExams(this.cache, info.studentId, //
                    RawRecordConstants.M100T, unit, true, RawStexamLogic.UNIT_EXAM_TYPES);

            if (!stexams.isEmpty()) {
                this.tutorialUnit = unit;
                addAvailableExam(info, RawRecordConstants.M100T, unit, null);
                fail = false;
                break;
            }

            // If the student has a passing review exam on record, make the exam available,
            // subject to the limits on the number of attempts per passing review exam.
            final List<RawStexam> streviews = RawStexamLogic.getExams(this.cache, info.studentId,
                    RawRecordConstants.M100T, unit, true, RawStexamLogic.REVIEW_EXAM_TYPES);

            if (!streviews.isEmpty()) {
                // Get the unit configuration
                final RawCusection unitData = RawCusectionLogic.query(this.cache, RawRecordConstants.M100T, "1", unit,
                        this.activeTerm.term);
                if (unitData == null) {
                    Log.warning("No cusection data for M 100T section 1, unit " + unit);
                    map.remove(unit);
                    continue;
                }

                if (unitData.atmptsPerReview != null) {
                    // Value of zero indicates unlimited attempts.
                    if (unitData.atmptsPerReview.intValue() == 0) {
                        this.tutorialUnit = unit;
                        addAvailableExam(info, RawRecordConstants.M100T, unit, null);
                        fail = false;

                        break;
                    }

                    final int count = RawStexamLogic.countUnitSinceLastPassedReview(this.cache, info.studentId,
                            RawRecordConstants.M100T, unit);

                    if (count >= unitData.atmptsPerReview.intValue()) {
                        break; // Don't fall through to next unit
                    }

                    this.tutorialUnit = unit;
                    addAvailableExam(info, RawRecordConstants.M100T, unit, null);
                    fail = false;
                    break;
                }
            }

            map.remove(unit);
        }

        if (fail) {
            addUnavailableExam(info, RawRecordConstants.M100T, Integer.valueOf(0), "Not Eligible");
        }
    }

    /**
     * Checks the student's eligibility for any Precalc Tutorial exams, and if so, add the appropriate exam to the
     * eligible list. The logic assumes that a student may be eligible for only ONE tutorial exam at a time; the exam
     * selected is the highest unit for which the student qualifies.
     *
     * @param info the data object to populate with available exams
     * @throws SQLException if there is an error accessing the database
     */
    private void checkPrecalcTutorialAvailability(final StudentCheckinInfo info) throws SQLException {

        final String[] courses = {RawRecordConstants.M1260, RawRecordConstants.M1250, RawRecordConstants.M1240,
                RawRecordConstants.M1180, RawRecordConstants.M1170};

        for (final String course : courses) {

            boolean passedReview = false;
            boolean passedUnit = false;

            // See if there is a passing unit 4 review exam but no passing unit exam
            final List<RawStexam> stexams = RawStexamLogic.getExams(this.cache, info.studentId, course,
                    Integer.valueOf(4), true, RawStexam.REVIEW_EXAM, RawStexam.UNIT_EXAM);

            for (final RawStexam exam : stexams) {
                if (RawStexam.REVIEW_EXAM.equals(exam.examType)) {
                    passedReview = true;
                } else if (RawStexam.UNIT_EXAM.equals(exam.examType)) {
                    passedUnit = true;
                }
            }

            if (passedReview && !passedUnit) {
                // Get the unit configuration
                final RawCusection unitData = RawCusectionLogic.query(this.cache, course, "1", Integer.valueOf(4),
                        this.activeTerm.term);

                if (unitData.lastTestDt.isEqual(this.today) || unitData.lastTestDt.isAfter(this.today)) {

                    addAvailableExam(info, course, Long.valueOf(4L));
                }
                // Working from the top down, so break when we find one...
                break;
            }

            addUnavailableExam(info, course, Integer.valueOf(4), "Not Eligible");
        }
    }

    /**
     * Determines the list of course exams the student is eligible to take, based on their registrations, licensed
     * status, placement history, prerequisites, incomplete status, and so on. The list of exams the student is eligible
     * for is compiled into the {@code mAvailableExams} field in the {@code StudentCheckinInfo} object.
     *
     * @param info             the data object to populate with available exams
     * @param checkEligibility true to enforce all eligibility requirements; false to relax some
     * @throws SQLException if there is an error accessing the database
     */
    private void determineAvailableCourseExams(final StudentCheckinInfo info,
                                               final boolean checkEligibility) throws SQLException {

        final String[] courses = {RawRecordConstants.M117, RawRecordConstants.M118,
                RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126};

        // Get exception rows for the student, which can be used in lieu of registration to permit
        // testing in courses, and add those units to the list of exams the student is potentially
        // eligible for.
        addExceptionStudentExams(info);

        final List<RawStcourse> currentTermRegs = RawStcourseLogic.queryByStudent(this.cache,
                info.studentId, this.activeTerm.term, false, false);

        // Eliminate any "forfeit" courses, and update provisional prereqs at the same time
        final Iterator<RawStcourse> iterator = currentTermRegs.iterator();
        while (iterator.hasNext()) {
            final RawStcourse stc = iterator.next();
            if ("G".equals(stc.openStatus)) {
                iterator.remove();
            }
            RawStcourseLogic.testProvisionalPrereqSatisfied(stc);
        }

        // For each course, load the section information for the student.
        final Map<RawStcourse, RawCsection> sections = new HashMap<>(currentTermRegs.size());
        final Map<RawStcourse, RawPacingStructure> pacingStructures = new HashMap<>(currentTermRegs.size());
        loadSectionData(info, currentTermRegs, sections, pacingStructures);

        // For courses that are not open, mark as unavailable
        boolean searching;

        for (final String course : courses) {
            searching = true;

            final Iterator<RawStcourse> iterator2 = currentTermRegs.iterator();
            while (iterator2.hasNext()) {
                final RawStcourse stc = iterator2.next();

                if (course.equals(stc.course)) {

                    if (stc.iDeadlineDt == null) {

                        if (stc.openStatus == null) {
                            for (int k = 1; k <= 5; ++k) {
                                addUnavailableExam(info, course, Integer.valueOf(k), "Not Yet Started");
                            }

                            iterator2.remove();
                        } else if (!"Y".equals(stc.openStatus)) {
                            for (int k = 1; k <= 5; ++k) {
                                addUnavailableExam(info, course, Integer.valueOf(k), "Course Not Open");
                            }

                            iterator2.remove();
                        }
                    }

                    searching = false;
                    break;
                }
            }

            if (searching) {
                for (int j = 1; j <= 5; ++j) {
                    addUnavailableExam(info, course, Integer.valueOf(j), "Not Registered");
                }
            }
        }

        // If there is an incomplete in-progress, student may only take courses that are marked
        // as incomplete in progress.
        processIncomplete(info, currentTermRegs);

        // Remove courses whose prerequisites have not been met.
        testPrerequisites(currentTermRegs, sections, info);

        // Allocate storage for unit information for the remaining courses and get the list of
        // units in each section for which the testing window is currently open.

        // Map from StudentCourse to corresponding course section
        final Map<RawStcourse, RawCsection> csects = new HashMap<>(currentTermRegs.size());

        // Map from StudentCourse to corresponding map from unit # to unit model
        final Map<RawStcourse, Map<Integer, RawCunit>> units = new HashMap<>(currentTermRegs.size());

        // Map from StudentCourse to corresponding map from unit # to course unit section model
        final Map<RawStcourse, Map<Integer, RawCusection>> cusects = new HashMap<>(currentTermRegs.size());

        final TermKey key = this.activeTerm.term;

        for (final RawStcourse stc : currentTermRegs) {
            if (stc == null) {
                continue;
            }

            csects.put(stc, RawCsectionLogic.query(this.cache, stc.course, stc.sect, key));

            final List<RawCusection> cusectList = RawCusectionLogic.queryByCourseSection(this.cache, stc.course,
                    stc.sect, this.activeTerm.term);

            final Map<Integer, RawCusection> cusectMap = new HashMap<>(cusectList.size());
            cusects.put(stc, cusectMap);
            for (final RawCusection cusect : cusectList) {
                cusect.showTestWindow = null;
                cusectMap.put(Integer.valueOf(cusect.unit.intValue()), cusect);
            }

            final Map<Integer, RawCunit> unitMap = new HashMap<>(cusects.size());
            units.put(stc, unitMap);
            for (final RawCusection cusect : cusectList) {
                unitMap.put(Integer.valueOf(cusect.unit.intValue()),
                        RawCunitLogic.query(this.cache, stc.course, cusect.unit, this.activeTerm.term));
            }
        }

        // Check testing windows for each unit in each course
        if (checkEligibility) {
            testWindows(info, currentTermRegs, cusects);
        }

        // Mark unit exams as unavailable if prior unit exam not completed and pacing structure
        // requires they be completed.
        enforcePassedUnits(info, currentTermRegs, units, cusects, pacingStructures);

        // Handle required passage of review test, and maximum number of proctored attempts per
        // successful review.
        enforcePassedReviewTests(this.cache, info, currentTermRegs, units, cusects);

        // Test max attempts per unit
        checkMaxAttempts(info, currentTermRegs, cusects, checkEligibility);

        // See if there are unit/midterm exam deadlines that are in the past
        // checkExamDeadlines(info, currentTermRegs, cusects, checkEligibility);

        // If the final exam has not been passed and the final exam deadline is in the past, then
        // see if the student is locked out of the course. No exams other than the final may be
        // taken, and the final exam may be taken if (1) it is within one working day of the final
        // exam deadline, (2) the student is eligible for the final and (3) the student has not
        // attempted the final since the course deadline.
        checkFinalExamDeadline(info, currentTermRegs, cusects, checkEligibility);

        // Now, add the available unit exams to the list, or deny if unlicensed
        for (final RawStcourse stc : currentTermRegs) {

            for (final RawCusection cusect : cusects.get(stc).values()) {

                if ("Y".equals(pacingStructures.get(stc).requireLicensed) && "N".equals(info.studentRecord.licensed)) {
                    addUnavailableExam(info, stc.course, Integer.valueOf(cusect.unit.intValue()),
                            "Need User's Exam");
                } else {
                    addAvailableExam(info, stc.course, cusect.unit, null);
                }
            }
        }

    }

    /**
     * Determines the list of challenge exams the student is eligible to take. A student can take a challenge exam if
     * <ul>
     * <li>they are not currently registered in the course (even if open status is "forfeit")
     * <li>they do not have an open Incomplete in the course from a prior term
     * <li>they do not have placement (OT) credit for the course already
     * <li>they have not already taken and completed the course and do not have transfer credit for
     * the course on their record
     * <li>they have not already challenged the course with the Challenge exam (earlier attempts on
     * the combined Math Challenge Exam would not affect eligibility)
     * <li>they have met the prerequisites for the course
     * <li>they do not have a fatal hold other than hold 30 (tested before this method)
     * </ul>
     *
     * @param info             the data object to populate with available exams
     * @param checkEligibility true to enforce all eligibility requirements; false to relax some
     * @throws SQLException if there is an error accessing the database
     */
    private void determineAvailableChallengeExams(final StudentCheckinInfo info,
                                                  final boolean checkEligibility) throws SQLException {

        final String[] courses = {RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                RawRecordConstants.M125, RawRecordConstants.M126};
        final String[] examIds = {"MC117", "MC118", "MC124", "MC125", "MC126"};

        final PrerequisiteLogic prereq = new PrerequisiteLogic(this.cache, info.studentId);
        String note = null;

        final int numCourses = courses.length;
        for (int i = 0; i < numCourses; ++i) {
            String reason = null;

            final String course = courses[i];

            // The following does not include Dropped courses. This is OK - one can drop and then
            // challenge.

            final List<RawStcourse> active = RawStcourseLogic.getActiveForStudent(this.cache, info.studentId,
                    this.activeTerm.term);

            for (final RawStcourse test : active) {
                if (course.equals(test.course)) {
                    if ("OT".equals(test.instrnType)) {
                        reason = "Has Placement Credit";
                    } else {
                        reason = "Currently Enrolled";
                    }
                    break;
                }
            }

            if (reason == null) {
                // No enrollments in the current term (including Incompletes) - see if the course
                // was completed (or placement credit earned) in a prior term

                final List<RawStcourse> prior = RawStcourseLogic.getAllPrior(this.cache, info.studentId,
                        this.activeTerm.term);

                for (final RawStcourse test : prior) {
                    if (course.equals(test.course) && ("OT".equals(test.instrnType))) {
                        reason = "Has Placement Credit";
                        break;
                    }
                }
            }

            if (reason == null) {
                // No course or transfer credit - see if already challenged
                final List<RawStchallenge> att = RawStchallengeLogic.queryByStudent(this.cache, info.studentId);

                for (final RawStchallenge test : att) {
                    if (course.equals(test.course) && examIds[i].equals(test.version)) {
                        if (checkEligibility) {
                            reason = "Attempt Used";
                        } else {
                            note = "Attempt Used";
                        }
                        break;
                    }
                }

                if ((reason == null) && !prereq.hasSatisfiedPrereqsFor(course)) {
                    reason = "Needs Prereq.";
                }
            }

            if (reason == null) {
                addAvailableExam(info, examIds[i], Integer.valueOf(0), note);
            } else {
                addUnavailableExam(info, examIds[i], Integer.valueOf(0), reason);
            }
        }
    }

    /**
     * Get exception rows for the student, which can be used in lieu of registration to permit testing in courses, and
     * add those units to the list of exams the student is potentially eligible for.
     *
     * @param info the data object to populate with available exams
     * @throws SQLException if there is an error accessing the database
     */
    private void addExceptionStudentExams(final StudentCheckinInfo info) throws SQLException {

        final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByStudent(this.cache, info.studentId,
                this.curDate.toLocalDate());

        // TODO: Eliminate hard-coded numbers of units below...

        for (final RawSpecialStus special : specials) {

            final String type = special.stuType;

            if ("TUTOR".equals(type) || "M384".equals(type) || "ADMIN".equals(type)) {

                for (long unit = 1L; unit <= 5L; unit++) {
                    addAvailableExam(info, RawRecordConstants.M117, Long.valueOf(unit));
                    addAvailableExam(info, RawRecordConstants.M118, Long.valueOf(unit));
                    addAvailableExam(info, RawRecordConstants.M124, Long.valueOf(unit));
                    addAvailableExam(info, RawRecordConstants.M125, Long.valueOf(unit));
                    addAvailableExam(info, RawRecordConstants.M126, Long.valueOf(unit));
                }
            }
        }
    }

    /**
     * If there is an incomplete in progress, delete (set to null) all courses that are not an incomplete in progress.
     *
     * @param info     the data object to populate with available exams
     * @param stcourse the list of courses to test
     */
    private void processIncomplete(final StudentCheckinInfo info, final Iterable<RawStcourse> stcourse) {

        boolean incompletes;

        if (info.incompleteOnly) {

            // See if there are any incomplete courses.
            incompletes = false;

            for (final RawStcourse stc : stcourse) {
                if (stc.iDeadlineDt != null) {
                    incompletes = true;
                    break;
                }
            }

            // Remove any course in the list that is not an incomplete in progress
            final Iterator<RawStcourse> iterator = stcourse.iterator();
            while (iterator.hasNext()) {
                final RawStcourse stc = iterator.next();

                final LocalDate dline = stc.iDeadlineDt;

                if (dline == null) {
                    for (int j = 1; j <= 5; j++) {
                        if (incompletes) {
                            addUnavailableExam(info, stc.course, Integer.valueOf(j), "Do Incomplete First");
                        } else {
                            addUnavailableExam(info, stc.course, Integer.valueOf(j), "Missed Progress");
                        }
                    }

                    iterator.remove();
                } else if (this.today.isAfter(dline)) {
                    for (int j = 1; j <= 5; j++) {
                        addUnavailableExam(info, stc.course, Integer.valueOf(j), "Past Inc. Deadline");
                    }

                    iterator.remove();
                }
            }
        } else {
            // For any course that's got an incomplete deadline date, but the incomplete is no
            // longer in progress, or the deadline date is in the past, disable access

            final Iterator<RawStcourse> iterator = stcourse.iterator();
            while (iterator.hasNext()) {
                final RawStcourse stc = iterator.next();

                final LocalDate dline = stc.iDeadlineDt;

                if (dline != null) {
                    if ("Y".equals(stc.iInProgress)) {

                        if (this.today.isAfter(dline)) {
                            for (int j = 1; j <= 5; j++) {
                                addUnavailableExam(info, stc.course, Integer.valueOf(j), "Past Inc. Deadline");
                            }

                            iterator.remove();
                        }
                    } else {
                        for (int j = 1; j <= 5; j++) {
                            addUnavailableExam(info, stc.course, Integer.valueOf(j), "Incomplete Processed");
                        }
                    }
                }
            }
        }
    }

    /**
     * Given a list of courses, loads the section information for the current student's registration in the course,
     * marking exams as unavailable if there is no section data.
     *
     * @param info     the data object to populate with available exams
     * @param stcourse the (possibly sparse) list of courses
     * @param sections the resulting list of sections
     * @param ruleSets the corresponding list of rule sets
     * @throws SQLException if there was an error accessing the database
     */
    private void loadSectionData(final StudentCheckinInfo info, final Iterable<RawStcourse> stcourse,
                                 final Map<? super RawStcourse, ? super RawCsection> sections,
                                 final Map<? super RawStcourse, ? super RawPacingStructure> ruleSets)
            throws SQLException {

        // TODO: Use the rule set from the term when an incomplete was earned rather than the
        // active term.

        final List<RawPacingStructure> allRuleSets =
                RawPacingStructureLogic.queryByTerm(this.cache, this.activeTerm.term);

        final Iterator<RawStcourse> iterator = stcourse.iterator();
        while (iterator.hasNext()) {
            final RawStcourse stc = iterator.next();

            TermRec effTerm = this.activeTerm;
            if (!effTerm.term.equals(stc.termKey)) {
                final TermRec incTerm = TermLogic.get(this.cache).query(this.cache, stc.termKey);
                if (incTerm != null) {
                    effTerm = incTerm;
                }
            }

            final RawCsection sect =
                    RawCsectionLogic.query(this.cache, stc.course, stc.sect, effTerm.term);

            if (sect == null) {
                for (int j = 1; j <= 5; j++) {
                    addUnavailableExam(info, stc.course, Integer.valueOf(j), "No Section Data");
                }
            } else {
                sections.put(stc, sect);

                if ("Y".equals(sect.bogus)) {

                    if (sect.lstStcrsCreatDt == null) {
                        Log.warning("Invalid section data for " + stc.course + ", section " + stc.sect);

                        for (int j = 1; j <= 5; j++) {
                            addUnavailableExam(info, stc.course, Integer.valueOf(j), "Invalid Section");
                        }

                        sections.remove(stc);
                        iterator.remove();
                    } else {
                        final LocalDate lastAdd = sect.lstStcrsCreatDt;

                        if (this.today.isAfter(lastAdd)) {
                            Log.warning("Bogus section data for " + stc.course + ", section " + stc.sect);

                            for (int j = 1; j <= 5; j++) {
                                addUnavailableExam(info, stc.course, Integer.valueOf(j), "Bogus Section");
                            }

                            sections.remove(stc);
                            iterator.remove();
                        }

                        // Store the rule set for the section
                        for (final RawPacingStructure test : allRuleSets) {
                            if (test.pacingStructure.equals(sect.pacingStructure)) {
                                ruleSets.put(stc, test);
                            }
                        }
                    }
                } else if ("OT".equals(sect.instrnType)) {

                    // Regardless of student's "exam-placed" value, we prevent taking exams in placement sections.
                    for (int j = 1; j <= 5; j++) {
                        addUnavailableExam(info, stc.course, Integer.valueOf(j), "Placement Credit");
                    }

                    sections.remove(stc);
                    iterator.remove();
                } else {

                    // Store the pacing structure for the section
                    for (final RawPacingStructure test : allRuleSets) {
                        if (test.pacingStructure.equals(sect.pacingStructure)) {
                            ruleSets.put(stc, test);
                        }
                    }

                    if (!ruleSets.containsKey(stc)) {
                        for (int j = 1; j <= 5; j++) {
                            addUnavailableExam(info, stc.course, Integer.valueOf(j), "Pacing Not Found");
                        }
                    }

                    // FIXME: Should we do this: sections.remove(stc); iterator.remove();
                }
            }
        }
    }

    /**
     * Reexamines the prerequisite information for a course and determine if the prerequisite has been satisfied. This
     * is done only if the student-course record does not yet indicate that the prerequisite has been satisfied.
     *
     * @param stcourse the student-course data to test
     * @param sections the list of sections corresponding to the courses
     * @param info     the data object to populate with available exams
     */
    private static void testPrerequisites(final Iterable<RawStcourse> stcourse,
                                          final Map<RawStcourse, RawCsection> sections, final StudentCheckinInfo info) {

        final Iterator<RawStcourse> iter = stcourse.iterator();
        while (iter.hasNext()) {
            final RawStcourse stc = iter.next();

            // Test for prerequisites
            final String prereq = stc.prereqSatis;

            if ((prereq != null) && (!"Y".equals(prereq) && !"P".equals(prereq))) {

                for (int j = 1; j <= 5; ++j) {
                    addUnavailableExam(info, stc.course, Integer.valueOf(j), "Need Prerequisites");
                }

                sections.remove(stc);
                iter.remove();
            }
        }
    }

    /**
     * Examines the testing windows for the various units and determine if the exam is unavailable. If there are no
     * units available for a course, the course is removed from the list.
     *
     * @param info     the data object to populate with available exams
     * @param stcourse the list of courses for which the student may take exams
     * @param cusects  the list of units in each course
     */
    private void testWindows(final StudentCheckinInfo info, final Iterable<RawStcourse> stcourse,
                             final Map<RawStcourse, ? extends Map<Integer, RawCusection>> cusects) {

        final Iterator<RawStcourse> iterator = stcourse.iterator();
        while (iterator.hasNext()) {
            final RawStcourse stc = iterator.next();

            // Indicate no units yet found within this course
            boolean searching = true;
            final Map<Integer, RawCusection> stcCusects = cusects.get(stc);

            final Iterator<Map.Entry<Integer, RawCusection>> iterator2 = stcCusects.entrySet().iterator();
            while (iterator2.hasNext()) {
                final Map.Entry<Integer, RawCusection> entry = iterator2.next();
                final RawCusection stcCusect = entry.getValue();

                final LocalDate firstTest = stcCusect.firstTestDt;
                if (firstTest != null && this.today.isBefore(firstTest)) {

                    addUnavailableExam(info, stc.course, Integer.valueOf(stcCusect.unit.intValue()),
                            "Window Not Open Yet");

                    iterator2.remove();
                    continue;
                }

                final LocalDate lastTest = stcCusect.lastTestDt;
                if (lastTest != null && this.today.isAfter(lastTest)) {

                    addUnavailableExam(info, stc.course, Integer.valueOf(stcCusect.unit.intValue()),
                            "Window Has Closed");

                    iterator2.remove();
                    continue;
                }

                searching = false;
            }

            if (searching) {
                // No units within this course are available, so delete the course.
                iterator.remove();
                cusects.remove(stc);
            }
        }
    }

    /**
     * Ensures that all prior units are passed in order to take a unit exam.
     *
     * @param info     the data object to populate with available exams
     * @param stcourse the list of courses for which the student may take exams
     * @param units    the list of units
     * @param cusects  the list of units in each course
     * @param ruleSets the rule sets associated with the courses, if known
     * @throws SQLException if there is an error accessing the database
     */
    private void enforcePassedUnits(final StudentCheckinInfo info, final Iterable<RawStcourse> stcourse,
                                    final Map<RawStcourse, ? extends Map<Integer, RawCunit>> units,
                                    final Map<RawStcourse, ? extends Map<Integer, RawCusection>> cusects,
                                    final Map<RawStcourse, RawPacingStructure> ruleSets) throws SQLException {

        for (final RawStcourse stc : stcourse) {
            // If pacing structure requires passed unit exams (or if pacing structure is unknown),
            // allow unit exams only if prior units have been passed.
            if (!"Y".equals(ruleSets.get(stc).requireUnitExams)) {
                continue;
            }

            // Determine the maximum unit that has been passed
            final Integer maxPassed = RawStexamLogic.maxPassedUnit(this.cache, info.studentId, stc.course);

            final String pacingStructure = ruleSets.get(stc).pacingStructure;

            final Map<Integer, RawCusection> cusectlist = cusects.get(stc);
            final Iterator<Map.Entry<Integer, RawCusection>> iterator2 = cusectlist.entrySet().iterator();
            while (iterator2.hasNext()) {
                final Map.Entry<Integer, RawCusection> entry = iterator2.next();
                final RawCusection cusect = entry.getValue();

                final int max = maxPassed == null ? -1 : maxPassed.intValue();

                if (cusect.unit.intValue() > max + 1) {

                    final String unitType = units.get(stc).get(entry.getKey()).unitType;

                    if ("INST".equals(unitType)) {

                        if (RawPacingRulesLogic.isRequired(this.cache, this.activeTerm.term, pacingStructure,
                                RawPacingRulesLogic.ACTIVITY_UNIT_EXAM, RawPacingRulesLogic.UE_MSTR)) {

                            // Remove all units beyond one greater than the highest passed unit
                            addUnavailableExam(info, stc.course, Integer.valueOf(cusect.unit.intValue()),
                                    "Must Pass Unit " + (cusect.unit.intValue() - 1));

                            iterator2.remove();
                        }
                    } else if ("FIN".equals(unitType) &&
                            RawPacingRulesLogic.isRequired(this.cache, this.activeTerm.term, pacingStructure,
                                    RawPacingRulesLogic.ACTIVITY_FINAL_EXAM, RawPacingRulesLogic.UE_MSTR)) {

                        // Remove all units beyond one greater than the highest passed unit.
                        addUnavailableExam(info, stc.course, Integer.valueOf(cusect.unit.intValue()),
                                "Must Pass Unit " + (cusect.unit.intValue() - 1));

                        iterator2.remove();
                    }
                }
            }
        }
    }

    /**
     * Ensures that the student has passed the review test enough times to warrant taking the proctored unit exam, and
     * enforce limits on the number of times the student may take the proctored exam after each successful attempt on
     * the review exam.
     *
     * @param cache    the data cache
     * @param info     the data object to populate with available exams
     * @param stcourse the list of courses for which the student may take exams
     * @param units    the list of units
     * @param cusects  the list of units in each course
     * @throws SQLException if there is an error accessing the database
     */
    private static void enforcePassedReviewTests(final Cache cache, final StudentCheckinInfo info,
                                                 final Iterable<RawStcourse> stcourse,
                                                 final Map<RawStcourse, ? extends Map<Integer, RawCunit>> units,
                                                 final Map<RawStcourse, ? extends Map<Integer, RawCusection>> cusects)
            throws SQLException {

        for (final RawStcourse stc : stcourse) {

            final String courseId = stc.course;

            final Iterator<Map.Entry<Integer, RawCusection>> iter2 = cusects.get(stc).entrySet().iterator();
            while (iter2.hasNext()) {
                final Map.Entry<Integer, RawCusection> entry = iter2.next();
                final Integer unit = entry.getKey();

                final RawCunit cunit = units.get(stc).get(unit);
                if (cunit == null) {
                    continue;
                }
                final String type = cunit.unitType;

                // Final exam units have no review exams of their own
                if ("FIN".equals(type)) {
                    continue;
                }

                // If there exist any passed unit exams in this unit, the unit is henceforth
                // available for unit exams.
                List<RawStexam> stexams = RawStexamLogic.getExams(cache, info.studentId, courseId, unit, true,
                        RawStexamLogic.UNIT_EXAM_TYPES);

                if (!stexams.isEmpty()) {
                    continue;
                }

                // See if there exists a passed review exam for this unit.
                stexams = RawStexamLogic.getExams(cache, info.studentId, courseId, unit, true,
                        RawStexamLogic.REVIEW_EXAM_TYPES);

                if (stexams.isEmpty()) {
                    addUnavailableExam(info, courseId, unit, "Must Pass Review");

                    iter2.remove();
                } else if (cusects.get(stc).get(unit).atmptsPerReview != null) {

                    final int count = RawStexamLogic.countUnitSinceLastPassedReview(cache, info.studentId, stc.course,
                            unit);

                    if (count >= cusects.get(stc).get(unit).atmptsPerReview.intValue()) {
                        addUnavailableExam(info, courseId, unit, "Must Retake Review");

                        iter2.remove();
                    }
                }
            }
        }
    }

    /**
     * Verifies that the maximum number of attempts for a unit exam has not been reached.
     *
     * @param info             the data object to populate with available exams
     * @param stcourse         the list of courses for which the student may take exams
     * @param cusects          the list of units in each course
     * @param checkEligibility true to enforce all eligibility requirements; false to relax some
     * @throws SQLException if there is an error accessing the database
     */
    private void checkMaxAttempts(final StudentCheckinInfo info, final Iterable<RawStcourse> stcourse,
                                  final Map<RawStcourse, ? extends Map<Integer, RawCusection>> cusects,
                                  final boolean checkEligibility)
            throws SQLException {

        for (final RawStcourse stc : stcourse) {

            final Iterator<Map.Entry<Integer, RawCusection>> iter2 = cusects.get(stc).entrySet().iterator();
            while (iter2.hasNext()) {
                final Map.Entry<Integer, RawCusection> entry = iter2.next();
                final RawCusection cusect = entry.getValue();

                if (cusect.nbrAtmptsAllow == null) {
                    continue;
                }

                // If there exist any passed unit exams in this unit, the unit is henceforth
                // available for unit exams.
                final List<RawStexam> utries = RawStexamLogic.getExams(this.cache, info.studentId, stc.course,
                        cusect.unit, false, RawStexam.UNIT_EXAM, RawStexam.FINAL_EXAM);
                final int attempts = utries.size();

                if (checkEligibility && attempts >= cusect.nbrAtmptsAllow.intValue()) {
                    addUnavailableExam(info, stc.course, Integer.valueOf(cusect.unit.intValue()),
                            "All attempts used");

                    iter2.remove();
                }
            }
        }
    }

    /**
     * If the final exam has not been passed and the final exam deadline is in the past, then see if the student is
     * locked out of the course. No exams other than the final may be taken, and the final exam may be taken if (1) it
     * is within one working day of the final exam deadline, (2) the student is eligible for the final and (3) the
     * student has not attempted the final since the course deadline.
     *
     * @param info             the data object to populate with available exams
     * @param stcourse         the list of courses for which the student may take exams
     * @param cusects          the list of units in each course
     * @param checkEligibility true to enforce all eligibility requirements; false to relax some
     * @throws SQLException if there is an error accessing the database
     */
    private void checkFinalExamDeadline(final StudentCheckinInfo info, final Iterable<RawStcourse> stcourse,
                                        final Map<RawStcourse, ? extends Map<Integer, RawCusection>> cusects,
                                        final boolean checkEligibility) throws SQLException {

        final int numPaced = info.studentTermRecord == null ? 0
                : RawStcourseLogic.getPaced(this.cache, info.studentId).size();

        if (numPaced > 0) {
            final String paceTrack = info.studentTermRecord.paceTrack.substring(0, 1);

            final List<RawMilestone> allMilestones = RawMilestoneLogic.getAllMilestones(this.cache,
                    this.activeTerm.term, numPaced, paceTrack);

            final List<RawStmilestone> stuMilestones = RawStmilestoneLogic
                    .getStudentMilestones(this.cache, this.activeTerm.term, paceTrack, info.studentId);

            for (final RawStcourse stc : stcourse) {
                final String courseId = stc.course;

                // See if the course has a final exam unit
                final RawCunit finalUnit = RawCunitLogic.getFinalUnit(this.cache, courseId, this.activeTerm.term);
                // If not final unit (or course was killed earlier), take no action
                if (finalUnit == null || finalUnit.unit == null) {
                    continue;
                }

                // If no pace order, then pace-based deadlines do not apply
                final Integer paceOrder = stc.paceOrder;
                if (paceOrder == null) {
                    continue;
                }

                final Integer finUnit = finalUnit.unit;
                final int finUnitNum = finUnit.intValue();

                // If the final exam has been passed, take no action
                final Integer highestPassing = RawStexamLogic.getHighestScore(this.cache, info.studentId, courseId,
                        Integer.valueOf(finUnitNum), true, "F");
                if (highestPassing != null) {
                    continue;
                }

                // Get the final exam deadline
                LocalDate deadline = null;
                int foundMs = -1;
                for (final RawMilestone test : allMilestones) {
                    final int msNbr = test.msNbr.intValue();

                    if (msNbr / 10 % 10 == paceOrder.intValue() && msNbr % 10 == finUnitNum
                            && "FE".equals(test.msType)) {

                        deadline = test.msDate;
                        foundMs = msNbr;
                    }
                }

                if (foundMs != -1) {
                    for (final RawStmilestone test : stuMilestones) {
                        if ((foundMs == test.msNbr.intValue() && "FE".equals(test.msType))) {
                            deadline = test.msDate;
                        }
                    }
                }

                // If there is no final exam deadline, or it is not in the past, take no action
                if (deadline == null || !this.today.isAfter(deadline)) {
                    continue;
                }

                // If we get here, the final exam deadline has passed and the final is not passed.
                // We automatically make unit exams unavailable at this point...
                for (int j = 0; j < finUnitNum; ++j) {
                    final Integer unit = Integer.valueOf(j);
                    if (cusects.get(stc).get(unit) != null && checkEligibility) {
                        addUnavailableExam(info, stc.course,
                                Integer.valueOf(cusects.get(stc).get(unit).unit.intValue()),
                                "Past Final deadline");

                        cusects.get(stc).remove(unit);
                    }
                }

                // If there is a final unit, check it.
                if (cusects.get(stc).get(Integer.valueOf(finUnitNum)) != null) {

                    // See if there is a "one more try" deadline configured
                    LocalDate extension = null;
                    Integer numExtension = null;

                    foundMs = -1;
                    for (final RawMilestone test : allMilestones) {
                        final int msNbr = test.msNbr.intValue();

                        if (msNbr / 10 % 10 == paceOrder.intValue() && msNbr % 10 == finUnitNum //
                                && "F1".equals(test.msType)) {

                            extension = test.msDate;
                            numExtension = test.nbrAtmptsAllow;
                            foundMs = msNbr;
                        }
                    }
                    if (foundMs != -1) {
                        for (final RawStmilestone test : stuMilestones) {
                            if (foundMs == test.msNbr.intValue() && "F1".equals(test.msType)) {

                                extension = test.msDate;
                                numExtension = test.nbrAtmptsAllow;
                            }
                        }
                    }

                    // If no last-try date is defined, disable the final exam
                    if (extension == null && checkEligibility) {
                        addUnavailableExam(info, stc.course, finUnit, "Past Final deadline");

                        cusects.get(stc).remove(finUnit);
                        continue;
                    }

                    // See if the final has been attempted the number of allowed times since the
                    // deadline
                    int attemptsSince = 0;
                    final List<RawStexam> tries = RawStexamLogic.getExams(this.cache,
                            info.studentId, courseId, Integer.valueOf(finUnitNum), false, "F");

                    for (final RawStexam tri : tries) {
                        if (tri.examDt.isAfter(deadline)) {
                            ++attemptsSince;
                        }
                    }

                    // If there has been an attempt on the final since the deadline, disable the
                    // final
                    if (numExtension == null) {
                        numExtension = Integer.valueOf(1);
                    }

                    // If an extension is earned, and this is the first course (pace order = 1)
                    // and the student has a "UBONUS" special type, award an additional attempt
                    if (Integer.valueOf(1).equals(stc.paceOrder)) {
                        final boolean earnedUBonus = RawSpecialStusLogic.isSpecialType(this.cache,
                                info.studentId, this.curDate.toLocalDate(), "UBONUS");

                        if (earnedUBonus) {
                            numExtension = Integer.valueOf(numExtension.intValue() + 1);
                        }
                    }

                    if (attemptsSince >= numExtension.intValue()) {
                        if (cusects.get(stc).get(finUnit) != null && checkEligibility) {
                            addUnavailableExam(info, stc.course, finUnit, "Past Final deadline");

                            cusects.get(stc).remove(finUnit);
                        }
                    } else if (this.today.isAfter(extension) && checkEligibility) {
                        // We are past the extension date, so disable the final exam
                        addUnavailableExam(info, stc.course, finUnit, "Past Final deadline");

                        cusects.get(stc).remove(finUnit);
                    }
                }
            }
        }

    }

    /**
     * Adds a record of an available exam to the list of exams in the student information object.
     *
     * @param info   the student information object
     * @param course the exam course
     * @param unit   the exam unit
     */
    private static void addAvailableExam(final StudentCheckinInfo info, final String course, final Long unit) {

        final String key = course + CoreConstants.DASH + unit.toString();

        if (!info.availableExams.containsKey(key)) {

            final AvailableExam avail = new AvailableExam(course, unit.intValue());
            avail.newLabel = null;
            avail.note = null;
            info.availableExams.put(key, avail);
        }
    }

    /**
     * Adds a record of an available exam to the list of exams in the student information object.
     *
     * @param info   the student information object
     * @param course the exam course
     * @param unit   the exam unit
     * @param note   an optional note to attach to the available exam
     */
    private static void addAvailableExam(final StudentCheckinInfo info, final String course, final Integer unit,
                                         final String note) {

        final String key = course + CoreConstants.DASH + unit.toString();

        if (!info.availableExams.containsKey(key)) {

            final AvailableExam avail = new AvailableExam(course, unit.intValue());
            avail.newLabel = null;
            avail.note = note;
            info.availableExams.put(key, avail);
        }
    }

    /**
     * Adds a record of an unavailable exam to the list of exams in the student information object.
     *
     * @param info   the student information object
     * @param course the exam course
     * @param unit   the exam unit
     * @param whyNot the reason the exam is not available
     */
    private static void addUnavailableExam(final StudentCheckinInfo info, final String course, final Integer unit,
                                           final String whyNot) {

        final String key = course + CoreConstants.DASH + unit.toString();

        if (!info.availableExams.containsKey(key)) {

            final AvailableExam avail = new AvailableExam(course, unit.intValue());
            avail.available = false;
            avail.newLabel = null;
            avail.whyNot = whyNot;
            info.availableExams.put(key, avail);
        }
    }
}
