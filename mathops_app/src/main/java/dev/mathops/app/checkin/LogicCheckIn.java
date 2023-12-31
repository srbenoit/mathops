package dev.mathops.app.checkin;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.logic.PlacementLogic;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawCusectionLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
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
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class contains all logic to determine, based on a student ID, the list of proctored exams the student is
 * eligible to take, and the reasons why other exams are unavailable.
 */
final class LogicCheckIn {

    /** A commonly used constant. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** A commonly used constant. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A commonly used constant. */
    private static final Integer FOUR = Integer.valueOf(4);

    /** A commonly used constant. */
    private static final Long FOURLONG = Long.valueOf(4L);

    /** A commonly used string. */
    private static final String ERROR = "Error";

    /** The context. */
    private final DbProfile profile;

    /** The current date. */
    private final ZonedDateTime curDate;

    /** The current day number. */
    private final LocalDate today;

    /** The currently active term. */
    private TermRec activeTerm = null;

    /** The unit of the tutorial the student is eligible for. */
    private Integer tutorialUnit = null;

    /**
     * Constructs a new {@code LogicCheckIn}.
     *
     * @param theProfile the context
     * @param now        the date/time to consider as "now"
     */
    LogicCheckIn(final DbProfile theProfile, final ZonedDateTime now) {

        this.profile = theProfile;
        this.curDate = now;
        this.today = now.toLocalDate();
    }

    /**
     * Performs one-time initializations from the database. These do not need to be repeated for each student processed.
     * At the moment, this only consists of querying and caching the active term record and checking that the current
     * date falls within the active term.
     *
     * @param cache the data cache
     * @return {@code true} if initialization succeeded; {@code false} otherwise.
     * @throws SQLException if there is an error accessing the database
     */
    boolean init(final Cache cache) throws SQLException {

        boolean ok = false;

        this.activeTerm = TermLogic.get(cache).queryActive(cache);
        if (this.activeTerm != null) {
            if (this.activeTerm.startDate == null) {
                JOptionPane.showMessageDialog(null, "Invalid term specification in database.", ERROR,
                        JOptionPane.ERROR_MESSAGE);
                Log.warning("Active term has no start date");
            } else if (this.activeTerm.endDate == null) {
                JOptionPane.showMessageDialog(null, "Invalid term specification in database.", ERROR,
                        JOptionPane.ERROR_MESSAGE);
                Log.warning("Active term has no end date");
            } else {
                final LocalDate start = this.activeTerm.startDate;

                if (this.today.isBefore(start)) {
                    JOptionPane.showMessageDialog(null, "Active term has not yet started.", ERROR,
                            JOptionPane.ERROR_MESSAGE);
                    Log.warning("Active term has not yet started.");
                } else {
                    final LocalDate end = this.activeTerm.endDate;

                    if (this.today.isAfter(end)) {
                        JOptionPane.showMessageDialog(null, "Active term has ended.", ERROR,
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
     * @param studentId the ID of the student for whom to gather information
     * @return student information and the list of available proctored exams, encapsulated in a
     *         {@code StudentCheckInInfo} object, or {@code null} if an error in processing occurs
     */
    DataOnCheckInAttempt performCheckInLogic(final String studentId) {

        DataOnCheckInAttempt info = null;

        final DbContext ctx = this.profile.getDbContext(ESchemaUse.PRIMARY);
        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(this.profile, conn);

            try {
                if (this.activeTerm == null || init(cache)) {
                    info = processCheckIn(cache, studentId);
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        return info;
    }

    /**
     * Processes the check-in request.
     *
     * @param cache the data cache
     * @param studentId the student ID
     * @return student information and the list of available proctored exams, encapsulated in a
     *         {@code StudentCheckInInfo} object, or {@code null} if an error in processing occurs
     * @throws SQLException if there is an error accessing the database
     */
    private DataOnCheckInAttempt processCheckIn(final Cache cache, final String studentId) throws SQLException {

        final String[] error = new String[2];
        final DataOnStudent studentData = loadDataOnStudent(cache, studentId, error);

        final DataOnCheckInAttempt info = new DataOnCheckInAttempt(studentData);

        final boolean ok = error[0] == null;
        if (error[0] != null) {
            info.error = error;
        }

        // Verify there are no outstanding exams in progress for the student. If there are, we may offer to let the
        // student exchange calculators. NOTE: We now do this before the holds test to prevent a student who is
        // exchanging calculator from being shown the same hold messages as were shown at initial check-in.
        if (ok && isNoExamInProgress(cache, info)) {

            final LogicCheckInCourseExams courseExamsLogic = new LogicCheckInCourseExams(this.today, this.activeTerm,
                    info);
            courseExamsLogic.execute(cache);

            // Now we determine which exams the student is eligible to take. The logic is split into evaluating
            // non-course exams (placement exams, paper exams, etc.) and exams within courses.
            determineAvailableNonCourseExams(cache, info);
        }

        return info;
    }

    /**
     * Gets the unit of the tutorial the student is eligible for.
     *
     * @return the unit
     */
    Integer getTutorialUnit() {

        return this.tutorialUnit;
    }

    /**
     * Loads the student data.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param error an 2-string array to populate with any error encountered.
     * @return the loaded student data; {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private DataOnStudent loadDataOnStudent(final Cache cache, final String stuId, final String[] error)
            throws SQLException {

        DataOnStudent data = null;

        final RawStudent stu = RawStudentLogic.query(cache, stuId, false);

        if (stu == null) {
            error[0] = "STUDENT record not found.";
            error[1] = "Please send student to the office...";
        } else {
            RawStterm stterm = RawSttermLogic.query(cache, this.activeTerm.term, stuId);
            if (stterm == null) {
                // TODO: Attempt to construct a proper STTERM record here...

                final List<RawStcourse> allRegs = RawStcourseLogic.getActiveForStudent(cache, stuId,
                        this.activeTerm.term);
                final int pace = PaceTrackLogic.determinePace(allRegs);
                final String paceTrack = PaceTrackLogic.determinePaceTrack(allRegs, pace);
                final String first = PaceTrackLogic.determineFirstCourse(allRegs);

                final Integer paceObj = Integer.valueOf(pace);
                stterm = new RawStterm(this.activeTerm.term, stuId, paceObj, paceTrack, first, null, null, null);
                try {
                    RawSttermLogic.INSTANCE.insert(cache, stterm);
                } catch (final SQLException ex) {
                    // Even if this insert fails, we can continue with the STTERM row we have created
                    Log.warning(ex);
                }
            }

            final List<RawAdminHold> holds = RawAdminHoldLogic.queryByStudent(cache, stuId);
            final int numHolds = holds.size();
            final List<String> holdsToShow = new ArrayList<>(numHolds);

            for (final RawAdminHold hold : holds) {
                if ("F".equalsIgnoreCase(hold.sevAdminHold)) {
                    error[0] = "Student has an administrative hold.";
                    error[1] = "Please send student to the office...";
                } else {
                    final String msg = RawAdminHoldLogic.getStaffMessage(hold.holdId);
                    if (msg != null) {
                        holdsToShow.add(msg);
                    }
                }
            }

            if (error[0] == null) {
                final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByStudent(cache, stuId, this.today);
                final int numSpecials = specials.size();
                final List<String> specialTypes = new ArrayList<>(numSpecials);
                for (final RawSpecialStus spec : specials) {
                    specialTypes.add(spec.stuType);
                }

                data = new DataOnStudent(stu, stterm, holdsToShow, specialTypes);
            }
        }

        return data;
    }

    /**
     * Tests whether there are existing in-progress exam records for the student.
     *
     * @param cache the data cache
     * @param info  the data object to populate with pending exam information
     * @return {@code true} if there are no exams pending for the student; {@code false} if there are pending exams
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean isNoExamInProgress(final Cache cache, final DataOnCheckInAttempt info) throws SQLException {

        final boolean result;

        final List<RawPendingExam> open = RawPendingExamLogic.queryByStudent(cache, info.studentData.stuId);

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
     * Determines the list of non-course exams the student is eligible to take, based on their course registrations,
     * licensed status, placement history, prerequisites, incomplete status, and so on. The list of exams the student is
     * eligible for is compiled into the {@code availableExams} field in the {@code StudentCheckInInfo} object.
     *
     * @param cache the data cache
     * @param info  the data object to populate with available exams
     * @throws SQLException if there is an error accessing the database
     */
    private void determineAvailableNonCourseExams(final Cache cache, final DataOnCheckInAttempt info)
            throws SQLException {


        // See if there is an active user's exam
        boolean searchingForUsersExam = true;
        final List<RawExam> exams = RawExamLogic.queryActiveByCourse(cache, RawRecordConstants.M100U);
        for (final RawExam exam : exams) {
            if ("Q".equals(exam.examType)) {
                addAvailableExam(info, RawRecordConstants.M100U, exam.unit, null);
                searchingForUsersExam = false;
                break;
            }
        }
        if (searchingForUsersExam) {
            addUnavailableExam(info, RawRecordConstants.M100U, ZERO, "Not Implemented");
        }

        // See if there is an active tutorial exam
        testElmTutorialAvaialability(cache, info);
        testPrecalcTutorialAvailability(cache, info);

        // Paper exams - unimplemented at this time.
        addUnavailableExam(info, "Paper", ZERO, "Not Implemented");

        // Math Placement Tool
        final PlacementLogic logic = new PlacementLogic(cache, info.studentData.stuId,
                info.studentData.student.aplnTerm, this.curDate);
        final PlacementStatus status = logic.status;

        if (status.availableLocalProctoredIds.contains("MPTTC")) {
            addAvailableExam(info, RawRecordConstants.M100P, ONE, null);
        } else {
            final String msg = Objects.requireNonNullElse(status.shortWhyProctoredUnavailable, "No attempts left");
            addUnavailableExam(info, RawRecordConstants.M100P, ONE, msg);
        }
    }

    /**
     * Checks the student's eligibility for any ELM Tutorial exams, and if so, add the appropriate exam to the eligible
     * list. The logic assumes that a student may be eligible for only ONE tutorial exam at a time; the exam selected is
     * the highest unit for which the student qualifies.
     *
     * @param cache the data cache
     * @param info  the data object to populate with available exams
     * @throws SQLException if there is an error accessing the database
     */
    private void testElmTutorialAvaialability(final Cache cache, final DataOnCheckInAttempt info) throws SQLException {

        boolean ineligible = true;

        final List<RawExam> exams = RawExamLogic.queryActiveByCourse(cache, RawRecordConstants.M100T);

        // Sort the exams in descending order by unit, and filter out all but unit exams.
        final SortedMap<Integer, RawExam> map = new TreeMap<>();
        for (final RawExam exam : exams) {
            if ("U".equals(exam.examType) && exam.unit != null) {
                map.put(exam.unit, exam);
            }
        }

        while (!map.isEmpty()) {
            final Integer unit = map.lastKey();

            // If there exist any passed unit exams in this unit, the unit is henceforth available for unit exams.
            final List<RawStexam> stexams = RawStexamLogic.getExams(cache, info.studentData.stuId,
                    RawRecordConstants.M100T, unit, true, RawStexamLogic.UNIT_EXAM_TYPES);

            if (!stexams.isEmpty()) {
                this.tutorialUnit = unit;
                addAvailableExam(info, RawRecordConstants.M100T, unit, null);
                ineligible = false;
                break;
            }

            // If the student has a passing review exam on record, make the exam available, subject to the limits on
            // the number of attempts per passing review exam.
            final List<RawStexam> stReviewExams = RawStexamLogic.getExams(cache, info.studentData.stuId,
                    RawRecordConstants.M100T, unit, true, RawStexamLogic.REVIEW_EXAM_TYPES);

            if (stReviewExams.isEmpty()) {
                Log.info("Bypassing M 100T unit " + unit + " (no passing review exam)");
            } else {
                // Get the unit configuration
                final RawCusection unitData = RawCusectionLogic.query(cache, RawRecordConstants.M100T, "1", unit,
                        this.activeTerm.term);
                if (unitData == null) {
                    Log.info("No Course-Unit-Section data for M 100T section 1, unit ", unit);
                    map.remove(unit);
                    continue;
                }

                if (unitData.atmptsPerReview != null) {

                    // Value of zero indicates unlimited attempts.
                    if (unitData.atmptsPerReview.intValue() == 0) {
                        this.tutorialUnit = unit;
                        addAvailableExam(info, RawRecordConstants.M100T, unit, null);
                        ineligible = false;

                        break;
                    }

                    final int count = RawStexamLogic.countUnitSinceLastPassedReview(cache, info.studentData.stuId,
                            RawRecordConstants.M100T, unit);

                    if (count >= unitData.atmptsPerReview.intValue()) {
                        Log.info("Removing M 100T unit ", unit, " (no attempts remaining)");
                        break; // Don't fall through to next unit
                    }

                    this.tutorialUnit = unit;
                    addAvailableExam(info, RawRecordConstants.M100T, unit, null);
                    ineligible = false;
                    break;
                }
            }

            map.remove(unit);
        }

        if (ineligible) {
            addUnavailableExam(info, RawRecordConstants.M100T, ZERO, "Not Eligible");
        }
    }

    /**
     * Checks the student's eligibility for any Precalculus Tutorial exams, and if so, add the appropriate exam to the
     * eligible list. The logic assumes that a student may be eligible for only ONE tutorial exam at a time; the exam
     * selected is the highest unit for which the student qualifies.
     *
     * @param cache the data cache
     * @param info  the data object to populate with available exams
     * @throws SQLException if there is an error accessing the database
     */
    private void testPrecalcTutorialAvailability(final Cache cache, final DataOnCheckInAttempt info)
            throws SQLException {

        final String[] courses = {RawRecordConstants.M1260, RawRecordConstants.M1250, RawRecordConstants.M1240,
                RawRecordConstants.M1180, RawRecordConstants.M1170};

        for (final String course : courses) {

            boolean passedReview = false;
            boolean needsToPassUnit = true;

            // See if there is a passing unit 4 review exam but no passing unit exam
            final List<RawStexam> stexams = RawStexamLogic.getExams(cache, info.studentData.stuId, course, FOUR, true,
                    "R", "U");

            for (final RawStexam exam : stexams) {
                if ("R".equals(exam.examType)) {
                    passedReview = true;
                } else if ("U".equals(exam.examType)) {
                    needsToPassUnit = false;
                }
            }

            if (passedReview && needsToPassUnit) {
                // Get the unit configuration
                final RawCusection unitData = RawCusectionLogic.query(cache, course, "1", FOUR, this.activeTerm.term);

                if (unitData.lastTestDt.isEqual(this.today) || unitData.lastTestDt.isAfter(this.today)) {

                    Log.info("Adding available ", course);
                    addAvailableExam(info, course, FOURLONG, null);
                }
                // Working from the top down, so break when we find one...
                break;
            }

            Log.info("Adding unavailable ", course);
            addUnavailableExam(info, course, FOUR, "Not Eligible");
        }
    }

    /**
     * Adds a record of an available exam to the list of exams in the student information object.
     *
     * @param info     the student information object
     * @param course   the exam course
     * @param unit     the exam unit
     * @param newLabel the new button label for the exam
     */
    private static void addAvailableExam(final DataOnCheckInAttempt info, final String course, final Number unit,
                                         final String newLabel) {

        final String key = SimpleBuilder.concat(course, CoreConstants.DASH, unit);

        if (!info.availableExams.containsKey(key)) {
            // Log.info("Adding available ", key);

            final int unitInt = unit.intValue();
            final ExamStatus avail = new ExamStatus(course, unitInt);
            avail.newLabel = newLabel;
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
    private static void addUnavailableExam(final DataOnCheckInAttempt info, final String course, final Integer unit,
                                           final String whyNot) {

        final String key = SimpleBuilder.concat(course, CoreConstants.DASH, unit);

        if (!info.availableExams.containsKey(key)) {
            // Log.info("Adding unavailable ", key);

            final int unitInt = unit.intValue();
            final ExamStatus avail = new ExamStatus(course, unitInt);
            avail.available = false;
            avail.whyNot = whyNot;
            info.availableExams.put(key, avail);
        }
    }
}
