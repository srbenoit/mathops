package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStcuobjectiveLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawLesson;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.LiveSessionInfo;
import dev.mathops.session.sitelogic.bogus.ETextLogic;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gathers all information relating to a student's status in a course.
 * <p>
 * Students may access a course as a tutorial, or using an e-text purchased for an earlier course, and does not
 * necessarily need a current registration. In these cases, a simulated registration record is created with a default
 * section number.
 */
public final class StudentCourseStatus extends LogicBase {

    /** The streaming server. */
    private static final String STREAM = "https://nibbler.math.colostate.edu/media/";

    /** A zero-length array used when allocating other arrays. */
    private static final RawCuobjective[] ZERO_LEN_CUOBJ_ARR = new RawCuobjective[0];

    /** The active term record. */
    private TermRec activeTerm;

    /** The student record. */
    private RawStudent student;

    /** The student term record. */
    private RawStterm studentTerm;

    /** Flag indicating this is a visiting student. */
    private boolean studentVisiting;

    /** Flag indicating visiting student should see course in practice mode only. */
    private boolean visitingPracticeMode;

    /** Flag indicating student has open access to all of a course without limits. */
    private boolean openAccess;

    /** The student course record. */
    private RawStcourse studentCourse;

    /** The course record. */
    private RawCourse course;

    /** The course section record. */
    private RawCsection courseSection;

    /** The pacing structure associated with the course section. */
    private RawPacingStructure pacingStructure;

    /** The maximum unit number for the course. */
    private int maxUnit;

    /** A container for all scores in the course. */
    private StudentCourseScores scores;

    /** The course unit records (index is unit number). */
    private RawCunit[] courseUnits;

    /** The deadline dates for unit review exams. */
    private LocalDate[] unitReviewDeadlines;

    /** The deadline dates for unit exams. */
    private LocalDate[] unitExamDeadlines;

    /** The last-try deadline dates for unit exams. */
    private LocalDate[] unitExamLastTries;

    /** The last-try number of attempts for unit exams. */
    private Integer[] unitExamLastTryAttempts;

    /** The course section unit records (index is unit number). */
    private RawCusection[] courseSectionUnits;

    /**
     * The sequence of course-unit-objectives for each unit (index is unit number, value is array of records for the
     * unit).
     */
    private RawCuobjective[][] courseUnitObjectives;

    /**
     * The sequence of lessons for each unit (index is unit number, value is array of Lesson records for the unit).
     */
    private RawLesson[][] lessons;

    /** A map from category name to a map from label to URL of course media links. */
    private final Map<String, Map<String, String>> media;

    /** The review exams for each unit. */
    private RawExam[] reviewExams;

    /** The unit exams for each unit. */
    private RawExam[] unitExams;

    /** The list of all homework assignments for the course. */
    private List<AssignmentRec> homeworks;

    /** The number of review exams taken, indexed by unit. */
    private int[] unitTotalReviews;

    /** The number of review exams passed, indexed by unit. */
    private int[] unitPassedReviews;

    /** The earliest passing date for the review exam, indexed by unit. */
    private LocalDate[] earliestPassingReviews;

    /** The number of review exams passed, indexed by unit. */
    private boolean[] unitPassedReviewOnTime;

    /** The number of unit exams taken, indexed by unit. */
    private int[] unitTotalExams;

    /** The number of unit exams passed, indexed by unit. */
    private int[] unitPassedExams;

    /** The number of unit exams passed, indexed by unit. */
    private boolean[] unitPassedExamOnTime;

    /** The number of assignments completed, indexed by unit/objective. */
    private int[][] unitObjTotalHw;

    /** The number of assignments mastered, indexed by unit/objective. */
    private int[][] unitObjMasteredHw;

    /** Flag indicating whether homework is available. */
    private boolean[] homeworkAvailable;

    /** Status of each homework assignment ("May Move On", "Completed", null). */
    private String[] homeworkStatus;

    /** Reasons homework is not yet available. */
    private String[] homeworkReasons;

    /** Flag indicating whether unit review exam is available. */
    private boolean[] reviewAvailable;

    /** The status of unit review exam. */
    private String[] reviewStatus;

    /** Reason unit review exams is not currently available. */
    private String[] reviewReasons;

    /** Flag indicating proctored exam is available or not. */
    private boolean[] proctoredAvailable;

    /** The status of each proctored exam. */
    private String[] proctoredStatus;

    /** Reasons proctored exams are not yet available. */
    private String[] proctoredReasons;

    /** Message with date ranges for proctored exam. */
    private String[] proctoredRange;

    /** True if all available exam attempts have been used. */
    private boolean[] allAttemptsUsed;

    /** Number of proctored exams available. */
    private int[] earnedProctored;

    /** True if there is an incomplete in progress; false otherwise. */
    private boolean incompleteInProgress;

    /** The term in which an incomplete was earned. */
    private TermRec incompleteTerm;

    /** The deadline date to finish the incomplete. */
    private LocalDate incompleteDeadline;

    /** Flag indicating course exam delete date is in the past. */
    private boolean examDeleteDateIsPast;

    /** String describing the next term. */
    private final List<String> termStrings;

    /** Exam delete date for current course, keyed on term string. */
    private final Map<String, String> examDeleteDates;

    /**
     * Constructs a new {@code StudentCourseStatus}.
     *
     * @param theDbProfile the database context under which this site is accessed
     */
    public StudentCourseStatus(final DbProfile theDbProfile) {

        super(theDbProfile);

        this.media = new TreeMap<>();
        this.termStrings = new ArrayList<>(4);
        this.examDeleteDates = new HashMap<>(4);

        this.activeTerm = null;
        this.student = null;
        this.studentTerm = null;
        this.studentVisiting = false;
        this.visitingPracticeMode = false;
        this.studentCourse = null;
        this.course = null;
        this.courseSection = null;
        this.pacingStructure = null;
        this.courseUnits = null;
        this.unitReviewDeadlines = null;
        this.unitExamDeadlines = null;
        this.unitExamLastTries = null;
        this.unitExamLastTryAttempts = null;
        this.courseSectionUnits = null;
        this.courseUnitObjectives = null;
        this.lessons = null;
        this.homeworks = null;
        this.maxUnit = -1;
        this.scores = null;
        this.openAccess = false;
        this.unitTotalReviews = null;
        this.unitPassedReviews = null;
        this.earliestPassingReviews = null;
        this.unitPassedReviewOnTime = null;
        this.unitTotalExams = null;
        this.unitPassedExams = null;
        this.unitPassedExamOnTime = null;
        this.unitObjTotalHw = null;
        this.unitObjMasteredHw = null;
        this.homeworkAvailable = null;
        this.homeworkStatus = null;
        this.homeworkReasons = null;
        this.reviewExams = null;
        this.unitExams = null;
        this.reviewAvailable = null;
        this.reviewStatus = null;
        this.reviewReasons = null;
        this.proctoredAvailable = null;
        this.proctoredStatus = null;
        this.proctoredReasons = null;
        this.proctoredRange = null;
        this.allAttemptsUsed = null;
        this.earnedProctored = null;
        this.incompleteInProgress = false;
        this.incompleteTerm = null;
        this.incompleteDeadline = null;
        this.examDeleteDateIsPast = false;
    }

    /**
     * Gets the student record.
     *
     * @return the student
     */
    public RawStudent getStudent() {

        return this.student;
    }

    /**
     * Tests whether the student is a visiting student.
     *
     * @return {@code true} if this is a visiting student
     */
    public boolean isStudentVisiting() {

        return this.studentVisiting;
    }

    /**
     * Tests whether a visiting student should see the course in practice mode only.
     *
     * @return {@code true} this is a visiting student and the student should see the course in practice mode;
     *         {@code false} if this is not a visiting student, or the student should be able to take all exams and
     *         assignments normally
     */
    public boolean isVisitingPracticeMode() {

        return this.visitingPracticeMode;
    }

    /**
     * Determines whether the student is licensed.
     *
     * @return {@code true} if the student is licensed (or licensing is not needed), {@code false} otherwise
     */
    public boolean isStudentLicensed() {

        return "Y".equals(this.student.licensed)
                || "N".equals(this.pacingStructure.requireLicensed)
                || "Y".equals(this.course.isTutorial) || this.openAccess;
    }

    /**
     * Gets the student course record.
     *
     * @return the student course
     */
    public RawStcourse getStudentCourse() {

        return this.studentCourse;
    }

    /**
     * Gets the course.
     *
     * @return the course
     */
    public RawCourse getCourse() {

        return this.course;
    }

    /**
     * Gets the course section.
     *
     * @return the course section
     */
    public RawCsection getCourseSection() {

        return this.courseSection;
    }

    /**
     * Gets the pacing structure under which the student operates in the course.
     *
     * @return the pacing structure
     */
    public RawPacingStructure getPacingStructure() {

        return this.pacingStructure;
    }

    /**
     * Gets the unit number of the last unit.
     *
     * @return the maximum unit number
     */
    public int getMaxUnit() {

        return this.courseUnits.length - 1;
    }

    /**
     * Gets a course unit.
     *
     * @param unit the unit number
     * @return the course section
     */
    public RawCunit getCourseUnit(final int unit) {

        return this.courseUnits[unit];
    }

    /**
     * Gets the deadline date for the review exam in a unit.
     *
     * @param unit the unit number
     * @return the deadline date, {@code null} if none applies
     */
    public LocalDate getReviewExamDeadline(final int unit) {

        return this.unitReviewDeadlines[unit];
    }

    /**
     * Gets the deadline date for the unit exam in a unit.
     *
     * @param unit the unit number
     * @return the deadline date, {@code null} if none applies
     */
    public LocalDate getUnitExamDeadline(final int unit) {

        return this.unitExamDeadlines[unit];
    }

    /**
     * Gets the last-try deadline date for the unit exam in a unit.
     *
     * @param unit the unit number
     * @return the last-try date, {@code null} if none applies
     */
    public LocalDate getUnitExamLastTry(final int unit) {

        return this.unitExamLastTries[unit];
    }

    /**
     * Gets the last-try number of attempts for the unit exam in a unit.
     *
     * @param unit the unit number
     * @return the last-try number of attempts, {@code null} if none applies
     */
    public Integer getUnitExamLastTryAttempts(final int unit) {

        return this.unitExamLastTryAttempts[unit];
    }

    /**
     * Gets a course section unit.
     *
     * @param unit the unit number
     * @return the course section
     */
    public RawCusection getCourseSectionUnit(final int unit) {

        return this.courseSectionUnits[unit];
    }

//    /**
//     * Finds and returns the first unit of type FINAL found.
//     *
//     * @return the unit or {@code null} if there was not a unit of type FINAL in the course
//     */
//    private RawCunit getFinalUnit() {
//
//        RawCunit result = null;
//
//        for (final RawCunit test : this.courseUnits) {
//            if (test != null && "FIN".equals(test.unitType)) {
//                result = test;
//                break;
//            }
//        }
//
//        return result;
//    }

//    /**
//     * Finds and returns the first unit of type FINAL found.
//     *
//     * @return the unit, or {@code null} if there was not a unit of type FINAL in the course
//     */
//    public RawCusection getFinalSectionUnit() {
//
//        final RawCunit finUnit = getFinalUnit();
//        RawCusection result = null;
//
//        if (finUnit != null) {
//            final Integer number = finUnit.unit;
//
//            if (number != null) {
//                result = this.courseSectionUnits[number.intValue()];
//            }
//        }
//
//        return result;
//    }

    /**
     * Finds and returns the course section unit data for the first unit of type GATEWAY found.
     *
     * @return the unit, or {@code null} if there was not a unit of type GATEWAY in the course
     */
    public RawCusection getGatewaySectionUnit() {

        int index = -1;

        // Find the index of the gateway unit
        for (int i = 0; i <= this.maxUnit; ++i) {

            // FIXME: in the CSU schema, there is no CUNIT row for a gateway unit, but there is
            // a section unit row. This is bogus, but we need to override it here.

            if (this.courseUnits[i] == null) {
                if (this.courseSectionUnits[i] != null) {
                    index = i;
                    break;
                }
            } else if ("SR".equals(this.courseUnits[i].unitType)) {
                index = i;
                break;
            }
        }

        return index == -1 ? null : this.courseSectionUnits[index];
    }

    /**
     * Gets the number of lessons in a unit.
     *
     * @param unit the unit
     * @return the number of lessons
     */
    public int getNumLessons(final int unit) {

        final RawCuobjective[] less = this.courseUnitObjectives[unit];

        return less == null ? 0 : less.length;
    }

    /**
     * Gets a course unit objective.
     *
     * @param unit      the unit
     * @param objective the objective to retrieve
     * @return the objective (a model of class CCourseUnitObjective)
     */
    public RawCuobjective getCourseUnitObjective(final int unit, final int objective) {

        return this.courseUnitObjectives[unit][objective];
    }

    /**
     * Gets a lesson.
     *
     * @param unit      the unit
     * @param objective the objective to retrieve
     * @return the lesson (a model of class CLesson)
     */
    public RawLesson getLesson(final int unit, final int objective) {

        return this.lessons[unit][objective];
    }

    /**
     * Gets the map from media category to a map from label to URL of the links to media associated with the course.
     *
     * @return the media map
     */
    public Map<String, Map<String, String>> getMedia() {

        return this.media;
    }

    /**
     * Tests whether the user has attempted the course gateway exam.
     *
     * @return {@code true} if the course gateway exam has been attempted; {@code false} if not, or if there is no
     *         gateway exam
     */
    public boolean isCourseGatewayAttempted() {

        final RawCusection cusect = getGatewaySectionUnit();
        final boolean result;

        if (cusect != null) {
            result = this.unitTotalReviews[cusect.unit.intValue()] > 0;
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Tests whether the user has passed the course gateway exam. Note that the exam may be marked as passed without
     * being attempted, in the case where the requirement for taking the gateway exam has been met some other way.
     *
     * @return {@code true} if the course gateway exam has been passed or if there is no gateway exam in the course;
     *         {@code false} otherwise
     */
    public boolean isCourseGatewayPassed() {

        final RawCusection model = getGatewaySectionUnit();
        final boolean result;
        final int gwUnit;

        if (this.openAccess) {
            result = true;
        } else if (model != null) {
            gwUnit = model.unit.intValue();
            result = this.unitPassedReviews[gwUnit] > 0;
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Determines whether a homework is configured for a unit and objective.
     *
     * @param unit      the unit to test
     * @param objective the objective to test
     * @return {@code true} if the object is available; {@code false} otherwise
     */
    public boolean hasHomework(final int unit, final int objective) {

        return findHomeworkIndex(unit, objective) != -1;
    }

    /**
     * Determines whether a homework is available. This test also can be used to indicate whether instructional material
     * is to be made available for a particular objective.
     *
     * @param unit      the unit to test
     * @param objective the objective to test
     * @return {@code true} if the object is available; {@code false} otherwise
     */
    public boolean isHomeworkAvailable(final int unit, final int objective) {

        if (this.homeworks == null || this.homeworkAvailable == null) {
            return false;
        }

        final int index = findHomeworkIndex(unit, objective);

        return index != -1 && this.homeworkAvailable[index];
    }

    /**
     * For a homework assignment that is not available, retrieves the reason.
     *
     * @param unit      the unit to test
     * @param objective the objective to test
     * @return the reason the homework is unavailable
     */
    public String getHomeworkReason(final int unit, final int objective) {

        if (this.homeworks == null || this.homeworkReasons == null) {
            return null;
        }

        final int index = findHomeworkIndex(unit, objective);

        return index == -1 ? null : this.homeworkReasons[index];
    }

    /**
     * Gets a homework assignment status.
     *
     * @param unit      the unit to test
     * @param objective the objective to test
     * @return the status ("May Move On", "Completed" or {@code null}) of the assignment
     */
    public String getHomeworkStatus(final int unit, final int objective) {

        if (this.homeworks == null || this.homeworkStatus == null) {
            return null;
        }

        final int index = findHomeworkIndex(unit, objective);

        return index == -1 ? null : this.homeworkStatus[index];
    }

    /**
     * Determines whether a review exam is available.
     *
     * @param unit the unit to test
     * @return {@code true} if the object is available; {@code false} otherwise
     */
    public boolean isReviewExamAvailable(final int unit) {

        if (this.reviewAvailable == null || this.reviewAvailable.length < unit) {
            return false;
        }

        return this.reviewAvailable[unit];
    }

    /**
     * For a review exam that is unavailable, determines the reason.
     *
     * @param unit the unit to test
     * @return the reason the exam is unavailable
     */
    public String getReviewReason(final int unit) {

        if (this.reviewReasons == null || this.reviewReasons.length < unit) {
            return null;
        }

        return this.reviewReasons[unit];
    }

    /**
     * Gets the status of a review exam.
     *
     * @param unit the unit to test
     * @return the exam status ("Passed" or "Not Yet Passed" or {@code null})
     */
    public String getReviewStatus(final int unit) {

        if (this.reviewStatus == null || this.reviewStatus.length < unit) {
            return null;
        }

        return this.reviewStatus[unit];
    }

    /**
     * Tests whether the user has passed the unit exam in a unit.
     *
     * @param unit the unit
     * @return {@code true} if the unit review exam has been passed; {@code false} if not
     */
    public boolean isReviewPassed(final int unit) {

        return this.unitPassedReviews[unit] > 0;
    }

    /**
     * Gets the date on which the review exam was first passed.
     *
     * @param unit the unit
     * @return the first passing review exam date; null if the review exam has not yet been passed
     */
    public LocalDate getEarliestPassingReview(final int unit) {

        return this.earliestPassingReviews[unit];
    }

    /**
     * Tests whether the user has passed the unit review exam in a unit on time.
     *
     * @param unit the unit
     * @return {@code true} if the unit review exam was passed on time; {@code false} if not
     */
    public boolean isReviewPassedOnTime(final int unit) {

        return this.unitPassedReviewOnTime[unit];
    }

    /**
     * Gets the date ranges for the proctored exam.
     *
     * @param unit the unit to test
     * @return the date ranges
     */
    public String getProctoredRange(final int unit) {

        if (this.proctoredRange == null || this.proctoredRange.length < unit) {
            return null;
        }

        return this.proctoredRange[unit];
    }

    /**
     * Determines whether a proctored exam is available.
     *
     * @param unit the unit to test
     * @return {@code true} if the object is available; {@code false} otherwise
     */
    public boolean isProctoredExamAvailable(final int unit) {

        if (this.proctoredAvailable == null || this.proctoredAvailable.length < unit) {
            return false;
        }

        return this.proctoredAvailable[unit];
    }

    /**
     * For a proctored exam that is unavailable, determines the reason.
     *
     * @param unit the unit to test
     * @return the reason the exam is unavailable
     */
    public String getProctoredReason(final int unit) {

        if (this.proctoredReasons == null || this.proctoredReasons.length < unit) {
            return null;
        }

        return this.proctoredReasons[unit];
    }

    /**
     * Gets the status of a proctored exam.
     *
     * @param unit The unit to test.
     * @return the exam status ("Passed" or null)
     */
    public String getProctoredStatus(final int unit) {

        if (this.proctoredStatus == null || this.proctoredStatus.length < unit) {
            return null;
        }

        return this.proctoredStatus[unit];
    }

    /**
     * Gets the number of times the user has taken the Unit Exam in the current unit.
     *
     * @param unit the unit
     * @return the number of times the Unit exam has been taken
     */
    public int getProctoredTimesTaken(final int unit) {

        return this.unitTotalExams[unit];
    }

    /**
     * Tests whether the user has passed the unit exam in the current unit.
     *
     * @param unit the unit
     * @return {@code true} if the proctored unit exam has been passed; {@code false} if not
     */
    public boolean isProctoredPassed(final int unit) {

        return this.unitPassedExams[unit] > 0;
    }

    /**
     * Tests whether the user has passed the unit exam in a unit on time.
     *
     * @param unit the unit
     * @return {@code true} if the unit exam was passed on time; {@code false} if not
     */
    public boolean isProctoredPassedOnTime(final int unit) {

        return this.unitPassedExamOnTime[unit];
    }

    /**
     * Gets the number of times the user has taken the Unit Exam in the current unit.
     *
     * @param unit the unit
     * @return the number of times the Unit exam has been taken
     */
    public int getProctoredAttemptsAvailable(final int unit) {

        return this.earnedProctored[unit];
    }

    /**
     * Gets the user's current scores in the course.
     *
     * @return the container with course scores
     */
    public StudentCourseScores getScores() {

        return this.scores;
    }

    /**
     * Gets the highest possible score on the Unit exam in the current unit.
     *
     * @param unit the unit
     * @return the highest possible score
     */
    public int getPerfectScore(final int unit) {

        final RawCunit cunit = this.courseUnits[unit];
        int score = 0;

        if (cunit != null && cunit.possibleScore != null) {
            score = cunit.possibleScore.intValue();
        }

        return score;
    }

    /**
     * Gets the highest possible score on the Unit exam in the current unit.
     *
     * @param unit the unit
     * @return the highest possible score
     */
    public boolean isPassing(final int unit) {

        return this.unitPassedExams[unit] > 0;
    }

    /**
     * Determine if this is an incomplete that is in progress.
     *
     * @return {@code true} if an incomplete
     */
    public boolean isIncompleteInProgress() {

        return this.incompleteInProgress;
    }

//    /**
//     * Get the term of the incomplete.
//     *
//     * @return the incomplete term
//     */
//    public TermRec getIncompleteTerm() {
//
//        return this.incompleteTerm;
//    }

//    /**
//     * Get the deadline date for finishing the incomplete.
//     *
//     * @return the incomplete deadline date
//     */
//    public LocalDate getIncompleteDeadline() {
//
//        return this.incompleteDeadline;
//    }

//    /**
//     * See whether the exam delete date for the current term is in the past.
//     *
//     * @return {@code true} if the deadline date is in the past
//     */
//    public boolean isExamDeleteDateInPast() {
//
//        return this.examDeleteDateIsPast;
//    }

//    /**
//     * Get the String representing the current and future terms, such as "Spring, 2006".
//     *
//     * @return the strings representing the current and future terms
//     */
//    public List<String> getTermStrings() {
//
//        return this.termStrings;
//    }

//    /**
//     * Get the String representing the first current term.
//     *
//     * @return the strings representing the first current term
//     */
//    public String getFirstTermString() {
//
//        return this.termStrings.get(0);
//    }

    /**
     * Clears all stored data.
     */
    private void reset() {

        this.scores = null;
        this.activeTerm = null;
        this.student = null;
        this.studentTerm = null;
        this.studentVisiting = false;
        this.visitingPracticeMode = false;
        this.studentCourse = null;
        this.course = null;
        this.courseSection = null;
        this.pacingStructure = null;
        this.courseUnits = null;
        this.unitReviewDeadlines = null;
        this.unitExamDeadlines = null;
        this.unitExamLastTries = null;
        this.unitExamLastTryAttempts = null;
        this.courseSectionUnits = null;
        this.courseUnitObjectives = null;
        this.lessons = null;
        this.homeworks = null;
        this.maxUnit = -1;
        this.openAccess = false;
        this.unitTotalReviews = null;
        this.unitPassedReviews = null;
        this.unitPassedReviewOnTime = null;
        this.unitTotalExams = null;
        this.unitPassedExams = null;
        this.unitPassedExamOnTime = null;
        this.unitObjTotalHw = null;
        this.unitObjMasteredHw = null;
        this.homeworkAvailable = null;
        this.homeworkStatus = null;
        this.homeworkReasons = null;
        this.reviewExams = null;
        this.unitExams = null;
        this.reviewAvailable = null;
        this.reviewStatus = null;
        this.reviewReasons = null;
        this.proctoredAvailable = null;
        this.proctoredStatus = null;
        this.proctoredReasons = null;
        this.proctoredRange = null;
        this.allAttemptsUsed = null;
        this.earnedProctored = null;
        this.incompleteInProgress = false;
        this.incompleteTerm = null;
        this.incompleteDeadline = null;
        this.examDeleteDateIsPast = false;

        this.media.clear();
        this.termStrings.clear();
        this.examDeleteDates.clear();
    }

    /**
     * Gathers the data for a particular course, with content tailored to a particular student.
     *
     * @param cache          the data cache
     * @param session        the login session
     * @param theStudentId   the ID of the student
     * @param theCourseId    the ID of the course whose data to gather
     * @param isSkillsReview {@code true} if the outline is being presented as a skills review
     * @param isPractice     {@code true} if the outline is being presented in practice mode
     * @return {@code true} if data was gathered successfully; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public boolean gatherData(final Cache cache, final ImmutableSessionInfo session, final String theStudentId,
                              final String theCourseId, final boolean isSkillsReview, final boolean isPractice)
            throws SQLException {

        final boolean result;

        reset();

        final ZonedDateTime now = session.getNow();
        final ERole role = session.getEffectiveRole();

        if (queryActiveTerm(cache) && queryCourse(cache, theCourseId)
                && queryCourseUnits(cache, theCourseId) && queryStudent(cache, theStudentId)
                && queryStudentCourse(cache, now, role, theStudentId, theCourseId, isSkillsReview, isPractice)) {

            // Allocate overall unit status variables
            this.unitExams = new RawExam[this.maxUnit + 1];
            this.unitTotalReviews = new int[this.maxUnit + 1];
            this.unitPassedReviews = new int[this.maxUnit + 1];
            this.earliestPassingReviews = new LocalDate[this.maxUnit + 1];
            this.unitPassedReviewOnTime = new boolean[this.maxUnit + 1];
            this.unitTotalExams = new int[this.maxUnit + 1];
            this.unitPassedExams = new int[this.maxUnit + 1];
            this.unitPassedExamOnTime = new boolean[this.maxUnit + 1];

            // Allocate unit review exam status variables
            this.reviewExams = new RawExam[this.maxUnit + 1];
            this.reviewAvailable = new boolean[this.maxUnit + 1];
            this.reviewStatus = new String[this.maxUnit + 1];
            this.reviewReasons = new String[this.maxUnit + 1];
            this.unitReviewDeadlines = new LocalDate[this.maxUnit + 1];

            // Allocate unit exam status variables
            this.proctoredRange = new String[this.maxUnit + 1];
            this.proctoredAvailable = new boolean[this.maxUnit + 1];
            this.proctoredStatus = new String[this.maxUnit + 1];
            this.proctoredReasons = new String[this.maxUnit + 1];
            this.earnedProctored = new int[this.maxUnit + 1];
            this.allAttemptsUsed = new boolean[this.maxUnit + 1];
            this.unitExamDeadlines = new LocalDate[this.maxUnit + 1];
            this.unitExamLastTries = new LocalDate[this.maxUnit + 1];
            this.unitExamLastTryAttempts = new Integer[this.maxUnit + 1];

            this.scores = new StudentCourseScores(this.maxUnit);

            if (queryCourseSection(cache) && queryCourseSectionUnits(cache, now)
                    && queryCourseUnitObjectives(cache)) {

                queryExams(cache);

                this.homeworks = cache.getSystemData().getActiveAssignmentsByCourseType(this.studentCourse.course,
                        "HW");

                // Allocate homework status variables
                this.homeworkAvailable = new boolean[this.homeworks.size()];
                this.homeworkStatus = new String[this.homeworks.size()];
                this.homeworkReasons = new String[this.homeworks.size()];
                this.unitObjMasteredHw = new int[this.maxUnit + 1][];
                this.unitObjTotalHw = new int[this.maxUnit + 1][];

                loadExamDeadlines(cache);
                loadStudentHistory(cache);
                buildExamStatus();
                checkHomeworkAvailability(cache, now);
                checkExamAvailability(cache, now);
                calculateScore(cache);

                result = true;

                // FIXME: HARDCODES - MOVE INTO DATA (new course-media table?)

                if (RawRecordConstants.M117.equals(theCourseId)) {
                    final Map<String, String> map = new TreeMap<>();
                    map.put("Course Outline", STREAM + "M117/117TOC.pdf");
                    this.media.put("Course Overview", map);
                } else if (RawRecordConstants.M118.equals(theCourseId)) {
                    final Map<String, String> map = new TreeMap<>();
                    map.put("Course Outline", STREAM + "M118/118TOC.pdf");
                    this.media.put("Course Overview", map);
                } else if (RawRecordConstants.M124.equals(theCourseId)) {
                    final Map<String, String> map = new TreeMap<>();
                    map.put("Course Outline", STREAM + "M124/124TOC.pdf");
                    this.media.put("Course Overview", map);
                } else if (RawRecordConstants.M125.equals(theCourseId)) {
                    final Map<String, String> map = new TreeMap<>();
                    map.put("Course Outline", STREAM + "M125/125TOC.pdf");
                    this.media.put("Course Overview", map);
                } else if (RawRecordConstants.M126.equals(theCourseId)) {
                    final Map<String, String> map = new TreeMap<>();
                    map.put("Course Outline", STREAM + "M126/126TOC.pdf");
                    this.media.put("Course Overview", map);

                    final Map<String, String> map2 = new TreeMap<>();
                    map2.put("Identities", STREAM + "M126/126Identities.pdf");
                    this.media.put("References", map2);
                } else if (RawRecordConstants.M100T.equals(theCourseId)) {
                    final Map<String, String> map = new TreeMap<>();
                    map.put("Instructions", "/www/media/ELM_information.pdf");
                    map.put("Tutorial Outline", STREAM + "M100T/100TTOC.pdf");
                    this.media.put("Tutorial Overview", map);
                } else if (RawRecordConstants.M1170.equals(theCourseId)
                        || RawRecordConstants.M1180.equals(theCourseId)
                        || RawRecordConstants.M1240.equals(theCourseId)
                        || RawRecordConstants.M1250.equals(theCourseId)
                        || RawRecordConstants.M1260.equals(theCourseId)) {
                    final Map<String, String> map = new TreeMap<>();
                    map.put("Instructions", "/www/media/precalc_tutorial.pdf");
                    this.media.put("Tutorial Overview", map);
                }
            } else {
                reset();
                result = false;
            }
        } else {
            reset();
            result = false;
        }

        return result;
    }

    /**
     * Queries the active term.
     *
     * @param cache the data cache
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryActiveTerm(final Cache cache) throws SQLException {

        this.activeTerm = cache.getSystemData().getActiveTerm();

        final boolean result;
        if (this.activeTerm == null) {
            setErrorText("Unable to look up the active term.");
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    /**
     * Queries the course.
     *
     * @param cache       the data cache
     * @param theCourseId the course ID
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryCourse(final Cache cache, final String theCourseId) throws SQLException {

        final boolean result;

        this.course = cache.getSystemData().getCourse(theCourseId);
        if (this.course == null) {
            setErrorText("Unable to look up the " + theCourseId + " course.");
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    /**
     * Queries the course units.
     *
     * @param cache       the data cache
     * @param theCourseId the course ID
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryCourseUnits(final Cache cache, final String theCourseId) throws SQLException {

        final List<RawCunit> list = cache.getSystemData().getCourseUnits(theCourseId, this.activeTerm.term);

        // store the course units in an array indexed by unit number
        int max = -1;

        for (final RawCunit model : list) {
            max = Math.max(max, model.unit.intValue());
        }

        this.maxUnit = max;
        this.courseUnits = new RawCunit[max + 1];

        for (final RawCunit model : list) {
            final int index = model.unit.intValue();
            this.courseUnits[index] = model;
        }

        return true;
    }

    /**
     * Loads the student record (if the student ID is a valid ID), or attempt to build a student record if the ID is
     * that of a pseudo-student, like 'GUEST'.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryStudent(final Cache cache, final String stuId) throws SQLException {

        boolean result = true;

        // For "special-case" student IDs, generate appropriate data
        // FIXME: new table "guest-users" table with student ID, name, open access?)

        if ("GUEST".equalsIgnoreCase(stuId.trim())) {
            this.student = RawStudentLogic.makeFakeStudent("GUEST", CoreConstants.EMPTY, "Guest");
            this.studentVisiting = true;
            this.visitingPracticeMode = true;
        } else if ("AACTUTOR".equals(stuId.trim())) {
            this.student = RawStudentLogic.makeFakeStudent("AACTUTOR", CoreConstants.EMPTY, "Tutor");
            this.studentVisiting = true;
            this.visitingPracticeMode = true;
            this.openAccess = true;
        } else if ("ETEXT".equals(stuId.trim())) {
            this.student = RawStudentLogic.makeFakeStudent("ETEXT", CoreConstants.EMPTY, "Tutor");
            this.studentVisiting = true;
            this.visitingPracticeMode = true;
            this.openAccess = true;
        } else {
            this.student = RawStudentLogic.query(cache, stuId, false);
            if (this.student == null) {
                setErrorText("Student " + stuId + " not found");
                result = false;
            } else {
                this.studentTerm = RawSttermLogic.query(cache, this.activeTerm.term, stuId);
            }
        }

        return result;
    }

    /**
     * Queries the student course registration record, or builds simulated records for special case students.
     *
     * @param cache          the data cache
     * @param now            the date/time to consider "now"
     * @param role           the role
     * @param theStudentId   the student ID
     * @param theCourseId    the course ID
     * @param isSkillsReview {@code true} if the outline is being presented as a skills review
     * @param isPractice     {@code true} if the outline is being presented in practice mode
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryStudentCourse(final Cache cache, final ChronoZonedDateTime<LocalDate> now, final ERole role,
                                       final String theStudentId, final String theCourseId,
                                       final boolean isSkillsReview, final boolean isPractice)
            throws SQLException {

        boolean result = true;

        String defaultSect;
        if ("Y".equals(this.course.isTutorial)) {
            defaultSect = "1";
        } else {
            defaultSect = "001";
            final List<RawCsection> csections = cache.getSystemData().getCourseSections(this.activeTerm.term);
            csections.sort(null);

            for (final RawCsection test : csections) {
                if (test.course.equals(theCourseId)) {
                    defaultSect = test.sect;
                    break;
                }
            }
        }

        if (isSkillsReview) {
            this.studentCourse = makeStudentCourse(theStudentId, theCourseId, defaultSect);
        } else if (isPractice) {
            this.studentCourse = makeStudentCourse(theStudentId, theCourseId, defaultSect);
            this.openAccess = true;
        } else if ("Y".equals(this.course.isTutorial)) {
            this.studentCourse = makeStudentCourse(theStudentId, theCourseId, defaultSect);

            if (RawSpecialStusLogic.isSpecialType(cache, theStudentId, now.toLocalDate(), "ADMIN", "M384", "TUTOR")) {
                this.openAccess = true;
            }

        } else {
            // Query for an actual student course registration
            this.studentCourse = RawStcourseLogic.getRegistration(cache, theStudentId, theCourseId);

            if (this.studentCourse != null) {
                // We have an actual student-course record, see if it is an incomplete
                this.incompleteDeadline = this.studentCourse.iDeadlineDt;
                if (this.incompleteDeadline == null) {
                    this.incompleteInProgress = false;
                    this.incompleteTerm = null;
                } else {
                    this.incompleteInProgress = "Y".equals(this.studentCourse.iInProgress);
                    this.incompleteTerm = this.studentCourse.iTermKey == null ? null
                            : cache.getSystemData().getTerm(this.studentCourse.iTermKey);

                    if (this.incompleteTerm == null) {
                        setErrorText("Unable to look up the incomplete term.");
                        result = false;
                    }
                }
            }

            if (this.studentCourse == null) {
                // No actual or visiting record; some special student types get simulated record

                if (("AACTUTOR".equals(theStudentId)
                        || RawSpecialStusLogic.isSpecialType(cache, theStudentId, now.toLocalDate(),
                        "ADMIN", "M384", "TUTOR"))
                        || role.canActAs(ERole.ADMINISTRATOR)) {

                    // FIXME: Why is this not using SiteDataRegistration?

                    this.studentCourse = makeStudentCourse(theStudentId, theCourseId, defaultSect);
                    this.openAccess = true;
                } else {
                    setErrorText("Unable to find student " + theStudentId + " course data for " + theCourseId
                            + " (not a recognized special ID)");
                    result = false;
                }
            }
        }

        return result;
    }

    /**
     * Builds a simulated student course registration record with pace order 1.
     *
     * @param theStudentId the student ID
     * @param theCourseId  the course ID
     * @param theSection   the section
     * @return the generated student course registration record
     */
    private RawStcourse makeStudentCourse(final String theStudentId, final String theCourseId,
                                          final String theSection) {

        final RawStcourse result = new RawStcourse(this.activeTerm.term, // termKey
                theStudentId, // stuId
                theCourseId, // course
                theSection, // section
                Integer.valueOf(1), // paceOrder
                null, // openStatus
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

        result.synthetic = true;

        return result;
    }

    /**
     * Queries the course section and associated pacing structure based on the student course record.
     *
     * @param cache the data cache
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there was an error accessing the database
     */
    private boolean queryCourseSection(final Cache cache) throws SQLException {

        final String crs = this.studentCourse.course;
        final String sect = this.studentCourse.sect;

        final boolean result;

        final SystemData systemData = cache.getSystemData();

        if ("Y".equals(this.studentCourse.iInProgress)) {
            this.courseSection = systemData.getCourseSection(crs, sect, this.studentCourse.iTermKey);
        } else {
            this.courseSection = systemData.getCourseSection(crs, sect, this.activeTerm.term);
        }

        if (this.courseSection == null) {
            setErrorText("Unable to look up information on " + crs + " section " + sect);
            result = false;
        } else {
            result = true;
            String ruleSetId = this.courseSection.pacingStructure;

            if (ruleSetId == null) {
                ruleSetId = RawPacingStructure.DEF_PACING_STRUCTURE;
            }

            this.pacingStructure = systemData.getPacingStructure(ruleSetId, this.activeTerm.term);
        }

        return result;
    }

    /**
     * Queries the course section units.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there was an error accessing the database
     */
    private boolean queryCourseSectionUnits(final Cache cache, final ChronoZonedDateTime<LocalDate> now) throws SQLException {

        final String crs = this.studentCourse.course;
        final String sect = this.studentCourse.sect;

        final TermKey term = "Y".equals(this.studentCourse.iInProgress) ? this.studentCourse.iTermKey
                : this.studentCourse.termKey;

        final List<RawCusection> list = cache.getSystemData().getCourseUnitSections(crs, sect, term);

        boolean result = true;
        this.courseSectionUnits = new RawCusection[this.maxUnit + 1];

        for (final RawCusection test : list) {
            final int index = test.unit.intValue();

            if (index > this.maxUnit) {
                Log.warning("Course section unit data for course ", crs, " section ", sect,
                        " existed with unit number above max unit in course unit");
                setErrorText("Unable to look up the student course section units.");
                result = false;

                break;
            }
            this.courseSectionUnits[index] = test;
        }

        if (result) {
            queryDeadlineDates(cache, now);
        }

        return result;
    }

    /**
     * Gathers the registration deadline dates for the current term and all future terms.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @throws SQLException if there was an error accessing the database
     */
    private void queryDeadlineDates(final Cache cache, final ChronoZonedDateTime<LocalDate> now)
            throws SQLException {

        // The exam delete date represents the add deadline for the course. After this time,
        // exam data from the prior term is deleted, and students would have to start over
        // (only applies to tutorials). If the deadline date is in the past, we show no
        // warnings - if in the future, we warn the student
        final LocalDate deadline = this.courseSection.examDeleteDt;

        if (deadline != null) {
            this.examDeleteDateIsPast = now.toLocalDate().isAfter(deadline);

            if (!this.examDeleteDateIsPast) {
                this.termStrings.add(this.activeTerm.term.longString);

                this.examDeleteDates.put(this.activeTerm.term.longString,
                        TemporalUtils.FMT_MDY.format(this.courseSection.examDeleteDt));
            }
        } else {
            this.termStrings.add(this.activeTerm.term.longString);
            this.examDeleteDates.put(this.activeTerm.term.longString, "(No deadline listed)");
        }

        if ("Y".equals(this.course.isTutorial)) {

            final List<TermRec> futureTerms = cache.getSystemData().getFutureTerms();

            final String courseId = this.course.course;

            for (final TermRec fut : futureTerms) {

                // FIXME: Drive this behavior into data on TERM object
                if (this.activeTerm.term.name == ETermName.SUMMER) {
                    // We skip deadline dates in the summer term
                    continue;
                }

                this.termStrings.add(fut.term.longString);

                final LocalDate examDeleteDt = cache.getSystemData().getExamDeleteDate(courseId,
                        this.studentCourse.sect, fut.term);

                if (examDeleteDt == null) {
                    this.examDeleteDates.put(fut.term.longString, "the course add deadline for that term");
                } else {
                    this.examDeleteDates.put(fut.term.longString, TemporalUtils.FMT_MDY.format(examDeleteDt));
                }
            }
        } else {
            this.termStrings.add("the following term");
            this.examDeleteDates.put("the following term", "the course add deadline for that term");
        }
    }

    /**
     * Queries the course unit objective and lesson data for all units in the course.
     *
     * @param cache the data cache
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean queryCourseUnitObjectives(final Cache cache) throws SQLException {

        final String crs = this.studentCourse.course;
        boolean result = true;

        this.courseUnitObjectives = new RawCuobjective[this.maxUnit + 1][];
        this.lessons = new RawLesson[this.maxUnit + 1][];

        final SystemData systemData = cache.getSystemData();
        for (int i = 0; i <= this.maxUnit; ++i) {

            final List<RawCuobjective> all = systemData.getCourseUnitObjectives(crs, Integer.valueOf(i),
                    this.activeTerm.term);

            this.courseUnitObjectives[i] = all.toArray(ZERO_LEN_CUOBJ_ARR);
            final int count = this.courseUnitObjectives[i].length;

            this.lessons[i] = new RawLesson[count];

            for (int j = 0; j < count; ++j) {
                final RawCuobjective culess = this.courseUnitObjectives[i][j];
                final String lessonId = culess.lessonId;

                this.lessons[i][j] = systemData.getLesson(lessonId);
                if (this.lessons[i][j] == null) {
                    setErrorText("Unable to look up lesson " + lessonId);
                    result = false;
                }
            }
        }

        return result;
    }

    /**
     * Queries all homework and exam records associated with the course.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    private void queryExams(final Cache cache) throws SQLException {

        final String crs = this.studentCourse.course;

        final SystemData systemData = cache.getSystemData();

        final List<RawExam> exams = systemData.getActiveExams(crs);
        final int numUnits = this.courseUnits.length;
        for (int i = 0; i < numUnits; ++i) {
            for (final RawExam exam : exams) {
                if (Integer.valueOf(i).equals(exam.unit)) {
                    final String type = exam.examType;

                    if ("R".equals(type)) {
                        this.reviewExams[i] = exam;
                    } else if ("U".equals(type) || "F".equals(type)) {
                        this.unitExams[i] = exam;
                    }
                }
            }
        }
    }

    /**
     * Loads the review and unit exam deadlines.
     *
     * @param cache the data cache
     * @throws SQLException if there is error accessing the database
     */
    private void loadExamDeadlines(final Cache cache) throws SQLException {

        final String studentId = this.student.stuId;
        final String courseId = this.course.course;
        final String sect = this.courseSection.sect;

        final List<RawStcourse> paced = RawStcourseLogic.getPaced(cache, studentId);

        // See if the student has any paced courses, and if so, load the pace milestones
        List<RawMilestone> allMilestones = null;
        List<RawStmilestone> stMilestones = null;
        int pace = paced.size();

        TermRec term = this.activeTerm;
        RawStterm stterm = this.studentTerm;

        final SystemData systemData = cache.getSystemData();

        // Determine the term to use for deadlines
        if ("Y".equals(this.studentCourse.iInProgress) && "N".equals(this.studentCourse.iCounted)) {

            term = systemData.getTerm(this.studentCourse.iTermKey);

            Log.info("Term to use for milestones: ", term.term);

            // To get the right milestone, pace needs to be that from the "stterm" record from the incomplete term
            stterm = RawSttermLogic.query(cache, term.term, this.student.stuId);
            if (stterm != null) {
                pace = stterm.pace.intValue();

                allMilestones = systemData.getMilestones(term.term, stterm.pace, stterm.paceTrack);

                stMilestones = RawStmilestoneLogic.getStudentMilestones(cache, term.term,
                        stterm.paceTrack.substring(0, 1), studentId);
                stMilestones.sort(null);

                Log.info("Found " + allMilestones.size() + " milestones and " + stMilestones.size()
                        + " student milestones");
            }
        }

        for (final RawStcourse model : paced) {
            if (stterm != null && courseId.equals(model.course) && sect.equals(model.sect)) {

                allMilestones = systemData.getMilestones(term.term, stterm.pace, stterm.paceTrack);

                stMilestones = RawStmilestoneLogic.getStudentMilestones(cache, term.term,
                        stterm.paceTrack.substring(0, 1), studentId);
                stMilestones.sort(null);
                break;
            }
        }

        final LocalDate today = LocalDate.now();

        for (int unit = 0; unit <= this.maxUnit; ++unit) {

            this.unitReviewDeadlines[unit] = null;
            this.unitExamDeadlines[unit] = null;
            this.unitExamLastTries[unit] = null;
            this.unitExamLastTryAttempts[unit] = null;

            final Integer paceOrder = this.studentCourse.paceOrder;

            if (paceOrder == null || allMilestones == null || stMilestones == null) {
                continue;
            }

            final int order = paceOrder.intValue();
            final String type;
            if (this.courseUnits[unit] == null) {
                type = "SR";
            } else {
                type = this.courseUnits[unit].unitType;
            }

            String reviewImp = RawMilestone.UNIT_REVIEW_EXAM;
            String unitImp = RawMilestone.UNIT_EXAM;

            final boolean isReviewFieldTrue;
            final boolean isUnitFieldTrue;

            if ("SR".equals(type)) {
                isReviewFieldTrue = false;
                isUnitFieldTrue = false;
                reviewImp = RawMilestone.SKILLS_REVIEW;
            } else if ("FIN".equals(type)) {
                isReviewFieldTrue = true;
                isUnitFieldTrue = true;
                unitImp = RawMilestone.FINAL_EXAM;
            } else {
                isReviewFieldTrue = true;
                isUnitFieldTrue = false;
            }

            // See if there is a deadline date for review exams
            if (isReviewFieldTrue) {
                Integer reIndex = null;
                for (final RawMilestone ms : allMilestones) {
                    final int msNum = ms.msNbr.intValue();

                    if (msNum / 10 % 10 == order && msNum % 10 == unit && reviewImp.equals(ms.msType)) {
                        this.unitReviewDeadlines[unit] = ms.msDate;
                        reIndex = ms.msNbr;
                        break;
                    }
                }
                if (reIndex != null) {
                    for (final RawStmilestone stms : stMilestones) {
                        if (reIndex.equals(stms.msNbr) && reviewImp.equals(stms.msType)) {
                            this.unitReviewDeadlines[unit] = stms.msDate;
                            // Don't break - student milestones are sorted by due date, and if there are multiple
                            // matching rows, we want the latest date
                        }
                    }
                }
            }

            // See if there is a deadline date for unit exams
            if (isUnitFieldTrue) {

                Integer ueIndex = null;
                for (final RawMilestone ms : allMilestones) {
                    final int msNum = ms.msNbr.intValue();

                    if (msNum / 10 % 10 == order && msNum % 10 == unit
                            && unitImp.equals(ms.msType)) {

                        this.unitExamDeadlines[unit] = ms.msDate;
                        ueIndex = ms.msNbr;
                        break;
                    }
                }
                if (ueIndex != null) {
                    for (final RawStmilestone stms : stMilestones) {
                        if (ueIndex.equals(stms.msNbr) && unitImp.equals(stms.msType)) {
                            this.unitExamDeadlines[unit] = stms.msDate;
                            // Don't break - student milestones are sorted by due date, and if there are multiple
                            // matching rows, we want the latest date
                        }
                    }
                }
            }

            // If this is a final exam unit, and if there is a "F1" deadline date and the
            // student was eligible for the final by its deadline date, in which case they
            // have a
            // last try on the F1 date.
            if ("FIN".equals(type) && (this.unitExamDeadlines[unit] != null
                    && this.unitExamDeadlines[unit].isBefore(today))) {

                // FIXME: HARDCODES for what makes a student eligible: Passing unit 4 exam
                final RawStexam firstPassing = RawStexamLogic.getFirstPassing(cache, this.student.stuId, courseId,
                        Integer.valueOf(4), "U");

                if ((firstPassing != null)
                        && !firstPassing.examDt.isAfter(this.unitExamDeadlines[unit])) {

                    Integer ltIndex = null;

                    // Student is eligible for the last try date, if available
                    for (final RawMilestone ms : allMilestones) {
                        final int msNum = ms.msNbr.intValue();

                        if (msNum / 10 % 10 == order && msNum % 10 == unit
                                && RawMilestone.FINAL_LAST_TRY.equals(ms.msType)) {

                            this.unitExamLastTries[unit] = ms.msDate;
                            final Integer att = ms.nbrAtmptsAllow;
                            this.unitExamLastTryAttempts[unit] = att;
                            ltIndex = ms.msNbr;
                            break;
                        }
                    }

                    if (ltIndex != null) {
                        for (final RawStmilestone stms : stMilestones) {
                            if (ltIndex.equals(stms.msNbr) && RawMilestone.FINAL_LAST_TRY.equals(stms.msType)) {
                                this.unitExamLastTries[unit] = stms.msDate;
                                this.unitExamLastTryAttempts[unit] = stms.nbrAtmptsAllow;
                                // Don't break - student milestones are sorted by due date, and if there are multiple
                                // matching rows, we want the latest date
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Loads the set of homework and exams the student has submitted in the course, and populates the raw unit exam
     * scores in the score object, as well as the points for completion of unit and review exams on time or late.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    private void loadStudentHistory(final Cache cache) throws SQLException {

        final String studentId = this.student.stuId;
        final String courseId = this.course.course;

        // See how many objectives each unit has, and allocate storage for the objective-specific
        // homework statistics
        int numObj;

        for (int i = 0; i <= this.maxUnit; ++i) {
            numObj = countObjectives(i);

            if (numObj > 0) {
                this.unitObjMasteredHw[i] = new int[numObj];
                this.unitObjTotalHw[i] = new int[numObj];
            }
        }

        // Load review exam history (count total number of review exams taken, and number passed)
        final List<RawStexam> stuReviews = RawStexamLogic.getExams(cache, studentId, courseId,
                false, RawStexamLogic.REVIEW_EXAM_TYPES);

        for (final RawStexam stuReview : stuReviews) {

            if (stuReview.unit == null) {
                continue;
            }

            final int unit = stuReview.unit.intValue();

            if (unit < 0 || unit > this.maxUnit) {
                continue;
            }

            ++this.unitTotalReviews[unit];

            if ("Y".equals(stuReview.passed)) {
                ++this.unitPassedReviews[unit];

                if (this.earliestPassingReviews[unit] == null
                        || this.earliestPassingReviews[unit].isAfter(stuReview.examDt)) {
                    this.earliestPassingReviews[unit] = stuReview.examDt;
                }

                if (!this.unitPassedReviewOnTime[unit] && ((this.unitReviewDeadlines[unit] == null)
                        || !stuReview.examDt.isAfter(this.unitReviewDeadlines[unit]))) {
                    this.unitPassedReviewOnTime[unit] = true;
                }
            }
        }

        // Load unit exam history (count the total number of unit exams taken, the number
        // passed, and keep track of the highest passing score)
        final List<RawStexam> stuUnits = RawStexamLogic.getExams(cache, studentId, courseId, false,
                RawStexamLogic.UNIT_EXAM_TYPES);

        for (final RawStexam stuUnit : stuUnits) {

            if (stuUnit.unit == null) {
                continue;
            }

            final int unit = stuUnit.unit.intValue();

            if (unit < 0 || unit > this.maxUnit) {
                continue;
            }

            ++this.unitTotalExams[unit];

            if ("Y".equals(stuUnit.passed)) {
                ++this.unitPassedExams[unit];

                if (!this.unitPassedExamOnTime[unit] && ((this.unitExamDeadlines[unit] == null)
                        || !stuUnit.examDt.isAfter(this.unitExamDeadlines[unit]))) {
                    this.unitPassedExamOnTime[unit] = true;
                }

                final int max = this.scores.getPassingUnitExamScore(unit);
                if (stuUnit.examScore != null && stuUnit.examScore.intValue() > max) {
                    this.scores.setPassingUnitExamScore(unit, stuUnit.examScore.intValue());
                }
            }

            final int max = this.scores.getRawUnitExamScore(unit);
            if (stuUnit.examScore != null && stuUnit.examScore.intValue() > max) {
                this.scores.setRawUnitExamScore(unit, stuUnit.examScore.intValue());
            }
        }

        // Accumulate points somewhere for review exams passed on time
        for (int unit = 0; unit <= this.maxUnit; ++unit) {

            if (this.courseSectionUnits[unit] == null) {
                this.scores.setOntimeReviewExamScore(unit, 0);
                this.scores.setLateReviewExamScore(unit, 0);
                this.scores.setOntimeUnitExamScore(unit, 0);
                this.scores.setLateUnitExamScore(unit, 0);
            } else {
                // FIXME: For incompletes from an earlier term, we should use the points per
                // on-time and late exam and grading scale from the term in which the incomplete
                // was earned, rather than current term, which this does now.

                final Integer reOntime = this.courseSectionUnits[unit].rePointsOntime;

                if (((this.unitPassedReviews[unit] > 0) && this.unitPassedReviewOnTime[unit])
                        && (reOntime != null)) {
                    this.scores.setOntimeReviewExamScore(unit, reOntime.intValue());
                }
            }
        }

        // Load homework assignment history
        final List<RawSthomework> stuHomeworks = RawSthomeworkLogic.getHomeworks(cache, studentId, courseId, false,
                "HW");

        for (final RawSthomework stuHw : stuHomeworks) {

            final int unit = stuHw.unit.intValue();
            final int obj = stuHw.objective.intValue();

            if (unit < 0 || unit > this.maxUnit || obj < 1 || obj > this.unitObjMasteredHw[unit].length) {
                continue;
            }

            if ("Y".equals(stuHw.passed)) {
                this.unitObjMasteredHw[unit][obj - 1]++;
                this.unitObjTotalHw[unit][obj - 1]++;
            }
        }
    }

    /**
     * Populates the student course scores object and calculates the total course score.
     * <p>
     * It is assumed that the scores object has its raw unit exam scores and the points for on-time and late unit and
     * review exams. This method calculates the counted unit exam scores, stores the unit exam weights, and the coupon
     * point values.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    private void calculateScore(final Cache cache) throws SQLException {

        for (int unit = 0; unit <= this.maxUnit; ++unit) {

            // If the unit has not been passed, take no action
            // If the unit is not defined (not unusual for unit 0), take no action
            if ((this.unitPassedExams[unit] == 0) || (this.courseSectionUnits[unit] == null)) {
                continue;
            }

            // Determine the counted points from unit exam score
            int counted = this.scores.getPassingUnitExamScore(unit);
            final Integer mastery = this.courseSectionUnits[unit].ueMasteryScore;

            // If a zero unit is assigned, which takes away unit exam points
            if (this.studentCourse.zeroUnit != null && this.studentCourse.zeroUnit.intValue() == unit) {

                // For a unit exam, zero unit means subtract the mastery score. For a final exam
                // unit, zero means half the student's score
                if ((this.courseUnits[unit] == null) || (!"FIN".equals(this.courseUnits[unit].unitType))) {
                    // Gateway - probably have no mastery score, but just in case
                    counted = Math.max(0, counted - (mastery == null ? 0 : mastery.intValue()));
                } else {
                    counted /= 2;
                }
            }

            // Store the counted point value
            this.scores.setCountedUnitExamScore(unit, counted);
        }

        this.scores.calculateTotalScore();

        // If the course is currently open but not marked as completed, but the student's score is
        // sufficient to complete the course, mark it as completed
        if ("Y".equals(this.studentCourse.openStatus) && !"Y".equals(this.studentCourse.completed)) {

            Integer minPassing = this.courseSection.dMinScore;
            if (minPassing == null) {
                minPassing = this.courseSection.cMinScore;
                if (minPassing == null) {
                    minPassing = this.courseSection.bMinScore;
                    if (minPassing == null) {
                        minPassing = this.courseSection.aMinScore;
                    }
                }
            }

            if (minPassing != null && this.scores.getTotalScore() >= minPassing.intValue()) {

                final Integer score = Integer.valueOf(this.scores.getTotalScore());

                final String grade;
                if (score.intValue() >= 65) {
                    grade = "A";
                } else if (score.intValue() >= 62) {
                    grade = "B";
                } else if (score.intValue() >= 54) {
                    grade = "C";
                } else {
                    grade = "U";
                }

                if (RawStcourseLogic.updateCompletedScoreGrade(cache, this.studentCourse.stuId,
                        this.studentCourse.course, this.studentCourse.sect, this.studentCourse.termKey, "Y", score,
                        grade)) {

                    this.studentCourse.completed = "Y";
                    this.studentCourse.score = score;
                }

                Log.info("Marking course ", this.studentCourse.course, " as completed with score ",
                        this.studentCourse.score);

                // Convert any one-term etext's purchased into permanent-access records
                ETextLogic.courseCompleted(cache, this.studentCourse.stuId, this.studentCourse.course,
                        ZonedDateTime.now());
            }
        }
    }

    /**
     * Scans the homework for this course and set the availability of each based on pacing structure settings and
     * historical data.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @throws SQLException if there is an error accessing the database
     */
    private void checkHomeworkAvailability(final Cache cache, final ChronoZonedDateTime<LocalDate> now)
            throws SQLException {

        final String studentId = this.student.stuId;
        final String pacing = this.pacingStructure.pacingStructure;

        final SystemData systemData = cache.getSystemData();

        // determine availability of each homework assignment
        final int numHw = this.homeworks.size();
        for (int i = 0; i < numHw; ++i) {

            if (this.openAccess) {
                this.homeworkAvailable[i] = true;
                continue;
            }

            this.homeworkAvailable[i] = false;
            this.homeworkStatus[i] = "Not Yet Completed";

            final Integer unitInt = this.homeworks.get(i).unit;
            final Integer objInt = this.homeworks.get(i).objective;

            final int unit = unitInt.intValue();
            final int obj = objInt.intValue();

            // Sample data for guest access: Unit 1 homework have been done, with objective 1
            // mastered and objective 2 completed. Unit 1 HW and Unit 2 HW 1, 2, 3 are available.
            if ("GUEST".equalsIgnoreCase(studentId)) {

                if (unit == 1) {
                    this.homeworkAvailable[i] = true;
                    this.homeworkStatus[i] = "Completed";
                } else if (unit == 2) {

                    if (obj == 1) {
                        this.homeworkAvailable[i] = true;
                        this.homeworkStatus[i] = "Completed";
                    } else if (obj == 2) {
                        this.homeworkAvailable[i] = true;
                        this.homeworkStatus[i] = "May Move On";
                    } else if (obj == 3) {
                        this.homeworkAvailable[i] = true;
                    } else {
                        this.homeworkAvailable[i] = false;
                        this.homeworkReasons[i] = "Prior assignment not yet completed.";
                    }
                } else {
                    final String examName = this.reviewExams[unit].buttonLabel;
                    this.homeworkReasons[i] = examName + " not yet passed.";
                }

                continue;
            }

            // Special-case student IDs have all assignments available with no status listed
            if ("AACTUTOR".equals(studentId) || "ETEXT".equals(studentId) || "ADMIN".equals(studentId)) {

                this.homeworkStatus[i] = CoreConstants.EMPTY;
                this.homeworkAvailable[i] = true;
                continue;
            }

            // Make all homework available to visiting students
            // In a tutorial, all homework is always available
            if (this.studentVisiting || "Y".equals(this.course.isTutorial)) {

                this.homeworkAvailable[i] = true;
                this.homeworkStatus[i] = CoreConstants.EMPTY;
                continue;
            }

            this.homeworkAvailable[i] = true;

            // Indicate "Completed" or "May move on" based on existing work
            if (obj > 0) {
                if (this.unitObjMasteredHw[unit] != null && this.unitObjMasteredHw[unit][obj - 1] > 0) {

                    this.homeworkStatus[i] = "Completed";
                } else if (this.unitObjTotalHw[unit] != null && this.unitObjTotalHw[unit][obj - 1] > 0) {

                    this.homeworkStatus[i] = "May Move On";
                }
            }

            final boolean skipUnitExams =
                    RawSpecialStusLogic.isSpecialType(cache, studentId, now.toLocalDate(), "SKIP-UE")
                            || !"Y".equals(this.pacingStructure.requireUnitExams);

            // If homework is only available if the prior unit exam has been done, test for the required unit exam, and
            // if the homework is only available if the prior review exam has been done, test for the required review
            // exam. However, if a student is authorized to skip unit exams based on a special student entry, override
            // this condition.
            if (unit == 1) {
                final RawCusection gwSecUnit = getGatewaySectionUnit();

                if (gwSecUnit != null) {
                    final int gwUnit = gwSecUnit.unit.intValue();

                    if (this.unitPassedReviews[gwUnit] == 0) {
                        this.homeworkAvailable[i] = false;

                        final String lbl = this.reviewExams[0].buttonLabel;
                        this.homeworkReasons[i] = lbl + " not yet passed.";
                    }
                }
            } else if (unit > 1 && !skipUnitExams) {

                if (this.unitExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.UE_MSTR)
                        && this.unitPassedExams[unit - 1] == 0) {

                    this.homeworkAvailable[i] = false;
                    this.homeworkReasons[i] = this.unitExams[unit - 1].buttonLabel + " not yet passed.";
                    break;
                } else if (this.unitExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.UE_PASS)
                        && this.unitTotalExams[unit - 1] == 0) {

                    this.homeworkAvailable[i] = false;
                    this.homeworkReasons[i] = this.unitExams[unit - 1].buttonLabel + " not yet completed.";
                    break;
                } else if (this.reviewExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.UR_MSTR)
                        && this.unitPassedReviews[unit - 1] == 0) {

                    this.homeworkAvailable[i] = false;
                    this.homeworkReasons[i] = this.reviewExams[unit - 1].buttonLabel + " not yet passed.";
                    break;
                } else if (this.reviewExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.UR_PASS)
                        && this.unitTotalReviews[unit - 1] == 0) {

                    this.homeworkAvailable[i] = false;
                    this.homeworkReasons[i] = this.reviewExams[unit - 1].buttonLabel + " not yet completed.";
                    break;
                }
            }

            if (!this.homeworkAvailable[i]) {
                continue;
            }

            // Determine the unit and objective numbers of the immediately preceding homework
            // assignment (use 0 and 0 if there is none).
            int priorUnit = 0;
            int priorObj = 0;

            if (obj == 1 && unit > 1) {
                priorUnit = unit - 1;

                for (final AssignmentRec hw : this.homeworks) {

                    if (hw.unit != null && hw.unit.intValue() == priorUnit && hw.objective != null) {
                        final int tstObj = hw.objective.intValue();

                        if (tstObj > priorObj) {
                            priorObj = tstObj;
                        }
                    }
                }
            } else if (obj > 1) {
                priorUnit = unit;
                priorObj = obj - 1;
            }

            // If homework is only available if the prior homework has been done, test for the
            // required homework.

            if (priorObj > 0) {
                if (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.HW_MSTR)) {

                    if (this.unitObjMasteredHw[priorUnit] != null
                            && this.unitObjMasteredHw[priorUnit][priorObj - 1] > 0) {
                        this.homeworkAvailable[i] = true;
                    } else {
                        this.homeworkAvailable[i] = false;
                        this.homeworkReasons[i] = "Prior assignment not yet mastered.";
                    }
                } else if (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.HW_PASS)) {

                    if (this.unitObjTotalHw[unit] != null && this.unitObjTotalHw[priorUnit][priorObj - 1] > 0) {
                        this.homeworkAvailable[i] = true;
                    } else {
                        this.homeworkAvailable[i] = false;
                        this.homeworkReasons[i] = "Prior assignment not yet completed.";
                    }
                }
            }

            // If this is the first objective of a unit, and the pacing structure indicates the
            // first objective should be available regardless of prior homeworks, then indicate this
            if ("Y".equals(this.pacingStructure.firstObjAvail) && obj == 1) {
                this.homeworkAvailable[i] = true;
            }

            // If the pacing structure requires student to view lecture before accessing HW, check
            // that

            if ((this.homeworkAvailable[i] && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                    RawPacingRules.ACTIVITY_HOMEWORK, RawPacingRules.LECT_VIEWED))
                    && !RawStcuobjectiveLogic.hasLectureBeenViewed(cache, studentId,
                    this.homeworks.get(i).courseId, unitInt, objInt)) {
                this.homeworkAvailable[i] = false;
                this.homeworkReasons[i] = "Instructor Lecture not yet viewed.";
            }
        }
    }

    /**
     * Populates the exam status fields based on historical work.
     */
    private void buildExamStatus() {

        // Determine the review and unit status based on historical work
        for (int unit = 0; unit <= this.maxUnit; ++unit) {

            if (this.unitPassedReviews[unit] > 0) {
                if (this.unitPassedReviewOnTime[unit]) {
                    this.reviewStatus[unit] = "Passed";
                } else {
                    this.reviewStatus[unit] = "Passed (Late)";
                }
            } else if (this.unitTotalReviews[unit] > 0) {
                this.reviewStatus[unit] = "Not Yet Passed";
            } else {
                this.reviewStatus[unit] = "Not Yet Attempted";
            }

            if (this.unitPassedExams[unit] > 0) {
                if (this.unitPassedExamOnTime[unit]) {
                    this.proctoredStatus[unit] = "Passed";
                } else {
                    this.proctoredStatus[unit] = "Passed (Late)";
                }
            } else if (this.unitTotalExams[unit] > 0) {
                this.proctoredStatus[unit] = "Not Yet Passed";
            } else {
                this.proctoredStatus[unit] = "Not Yet Attempted";
            }
        }
    }

    /**
     * Scans the exams for this course and set the availability of each based on pacing structure settings and
     * historical data.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @throws SQLException if there is an error accessing the database
     */
    private void checkExamAvailability(final Cache cache, final ZonedDateTime now)
            throws SQLException {

        final String studentId = this.student.stuId;

        if ("GUEST".equalsIgnoreCase(studentId)) {

            // Sample data for guest access: Unit 1 homework have been done, with objective 1
            // mastered and objective 2 completed. Unit 1 HW and Unit 2 HW 1, 2, 3 are available.
            for (int unit = 0; unit <= this.maxUnit; ++unit) {
                this.reviewStatus[unit] = "Not Yet Attempted";
                this.proctoredStatus[unit] = "Not Yet Attempted";

                if (unit == 1) {
                    this.reviewAvailable[1] = true;
                } else {
                    this.reviewAvailable[unit] = false;
                    this.reviewReasons[unit] = "Unit assignments not yet completed.";
                    this.proctoredReasons[unit] = "Unit assignments not yet completed.";
                }

                this.proctoredAvailable[unit] = false;
            }
        } else if ("AACTUTOR".equals(studentId) || "ETEXT".equals(studentId)) {

            // Special-case student IDs have all exams available with no status listed
            for (int unit = 0; unit <= this.maxUnit; ++unit) {
                this.reviewStatus[unit] = CoreConstants.EMPTY;
                this.reviewAvailable[unit] = true;
                this.proctoredStatus[unit] = CoreConstants.EMPTY;
                this.proctoredAvailable[unit] = false;
            }
        } else if (this.studentVisiting) {
            // Make all exams available to visiting students

            for (int unit = 0; unit <= this.maxUnit; ++unit) {

                if (this.courseSectionUnits[unit] != null) {
                    final boolean onlineUnit =
                            "Y".equals(this.courseSectionUnits[unit].unproctoredExam);
                    this.reviewAvailable[unit] = true;
                    this.reviewStatus[unit] = CoreConstants.EMPTY;
                    this.proctoredAvailable[unit] = onlineUnit;
                }
            }
        } else if ("Y".equals(this.course.isTutorial)) {
            checkTutorialExamAvailability(cache, now);
        } else {
            checkReviewExamAvailability(cache, now);
            checkProctoredExamAvailability(cache, now);
        }
    }

    /**
     * Scans the exams for this course and set the availability of each based on pacing structure settings and
     * historical data, assuming the user is not a guest, a special user ID, a visiting user, and the course is a
     * tutorial.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @throws SQLException if there is an error accessing the database
     */
    private void checkTutorialExamAvailability(final Cache cache, final ZonedDateTime now)
            throws SQLException {

        // Determine review exam availability
        for (int unit = 0; unit <= this.maxUnit; ++unit) {

            if (this.openAccess) {
                this.reviewAvailable[unit] = true;

                continue;
            }

            // Since tutorials don't require homework, we must force their review exams to be
            // taken in order by looking at completed review exams.
            if (unit > 1 && this.reviewExams[unit - 1] != null
                    && !"Passed".equals(this.reviewStatus[unit - 1])) {

                this.reviewAvailable[unit] = false;

                final String lbl = this.reviewExams[unit - 1].buttonLabel;
                this.reviewReasons[unit] = lbl + " not yet passed.";
            } else {
                this.reviewAvailable[unit] = true;
            }
        }

        // Determine proctored exam availability
        for (int unit = 1; unit <= this.maxUnit; ++unit) {

            if (this.reviewExams[unit] != null) {
                this.proctoredAvailable[unit] = false;
                final String lbl = this.reviewExams[unit].buttonLabel;
                this.proctoredReasons[unit] = lbl + " not yet passed.";

                // Proctored exam only available when review exam has been passed
                if (this.unitExams[unit] != null && "Passed".equals(this.reviewStatus[unit])) {

                    this.proctoredAvailable[unit] = true;
                }
            }
        }

        // Get the first and last dates in the term where students may work
        final LocalDate termFirst = cache.getSystemData().getFirstClassDay();
        final LocalDate termLast = cache.getSystemData().getLastClassDay();

        // Determine availability of unit/proctored exams in each unit
        for (int unit = 0; unit <= this.maxUnit; ++unit) {
            final RawCusection sectionUnit = this.courseSectionUnits[unit];

            if (sectionUnit == null) {
                if (unit > 0) {
                    Log.warning("No course section unit for ", this.course.course, " section ", this.courseSection.sect,
                            " unit " + unit);
                }
                continue;
            }
            if (this.unitExams[unit] == null) {
                continue;
            }

            // Date-based constraints
            final LocalDate firstTest = sectionUnit.firstTestDt;
            final LocalDate lastTest = sectionUnit.lastTestDt;
            final String examLbl = this.unitExams[unit].buttonLabel;

            if ("Y".equals(sectionUnit.showTestWindow) && firstTest != null && lastTest != null) {
                describeTestingWindow(unit, termFirst, termLast, firstTest, lastTest, now, examLbl);
            }

            final LocalDate today = now.toLocalDate();

            // See if date makes the exam unavailable
            if (firstTest != null && firstTest.isAfter(today)) {
                this.proctoredAvailable[unit] = false;
                this.proctoredReasons[unit] = examLbl + " not yet available";
            } else if (lastTest != null && today.isAfter(lastTest)) {
                this.proctoredAvailable[unit] = false;
                this.proctoredReasons[unit] = examLbl + " no longer available";
            }

            if (this.proctoredAvailable[unit]) {
                // See if constraints on the number of attempts per passing review make the exam
                // unavailable

                final Integer perReview = sectionUnit.atmptsPerReview;
                Integer total = sectionUnit.nbrAtmptsAllow;

                if (total != null && total.intValue() >= 90) {

                    // A value >= 90 means there is no limit on the number of attempts, which is
                    // how we interpret null values
                    total = null;
                }

                if ((this.unitPassedExams[unit] > 0) || (perReview == null && total == null)) {

                    // Unit exam passed - no more review-based limits
                    this.allAttemptsUsed[unit] = false;
                    this.earnedProctored[unit] = Integer.MAX_VALUE;
                } else {
                    final String studentId = this.student.stuId;
                    final String courseId = this.course.course;

                    final List<RawStexam> examsTaken = RawStexamLogic.getExams(cache, studentId,
                            courseId, Integer.valueOf(unit), false, RawStexamLogic.UNIT_EXAM_TYPES);
                    final int taken = examsTaken.size();

                    this.allAttemptsUsed[unit] = total != null && taken >= total.intValue();

                    if (perReview == null) {
                        this.earnedProctored[unit] = Math.max(0, total.intValue() - taken);
                    } else {
                        final int count = RawStexamLogic.countUnitSinceLastPassedReview(cache, studentId,
                                courseId, Integer.valueOf(unit));
                        this.earnedProctored[unit] = Math.max(0, perReview.intValue() - count);
                    }

                    // If total limit is closer than per-review limit, use the smaller limit.
                    if (total != null && this.earnedProctored[unit] > total.intValue() - taken) {
                        this.earnedProctored[unit] = total.intValue() - taken;
                    }

                    if (this.allAttemptsUsed[unit]) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "No attempts remaining";
                    } else if (this.earnedProctored[unit] == 0) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "Must retake and pass " + this.reviewExams[unit].buttonLabel;
                    }
                }
            }
        }
    }

    /**
     * Scans the exams for this course and set the availability of each based on pacing structure settings and
     * historical data, assuming the user is not a guest, a special user ID, a visiting user, and the course is not a
     * tutorial.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @throws SQLException if there is an error accessing the database
     */
    private void checkReviewExamAvailability(final Cache cache, final ZonedDateTime now) throws SQLException {

        final String pacing = this.pacingStructure.pacingStructure;
        final boolean isIncomplete = "Y".equals(this.studentCourse.iInProgress);

        final SystemData systemData = cache.getSystemData();

        // Determine availability of review exams in each unit
        for (int unit = 0; unit <= this.maxUnit; ++unit) {
            final RawCusection sectionUnit = this.courseSectionUnits[unit];

            if (sectionUnit == null) {
                if (unit > 0 && unit < 5) {
                    Log.warning("No course section unit for ", this.course.course, " unit " + unit);
                }
                continue;
            }

            final LocalDate today = now.toLocalDate();

            if (this.studentVisiting || this.openAccess) {
                this.reviewAvailable[unit] = true;
                continue;
            }

            final LocalDate firstTest = sectionUnit.firstTestDt;
            final LocalDate lastTest = sectionUnit.lastTestDt;

            if (this.reviewExams[unit] != null) {
                // We don't use section test dates for incompletes...
                if (!isIncomplete) {
                    final String examLbl = this.reviewExams[unit].buttonLabel;

                    // Use the start and ends dates to test availability of the review exam.
                    if (firstTest != null && firstTest.isAfter(today)) {
                        this.reviewAvailable[unit] = false;
                        this.reviewReasons[unit] = "The first date to take " + examLbl + " is "
                                + TemporalUtils.FMT_MDY.format(firstTest);

                        continue;
                    }

                    if (lastTest != null && today.isAfter(lastTest)) {
                        this.reviewAvailable[unit] = false;
                        this.reviewReasons[unit] = "The last date to take " + examLbl + " was "
                                + TemporalUtils.FMT_MDY.format(lastTest);

                        continue;
                    }
                }

                // The review exam is available, now see if it can be taken for credit. If there
                // are no start or end dates for the credit period, it is assumed that the review
                // exam does not count for credit.
                this.reviewAvailable[unit] = true;
            }

            // Now we test whether the pacing structure constraints prevent the review exam from
            // being available

            if (unit > 1 && unit < this.unitExams.length) {

                if (this.unitExams[unit - 1] != null
                        && (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM, RawPacingRules.UE_MSTR)
                        || systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM,
                        RawPacingRules.TE_MSTR))
                        && this.unitPassedExams[unit - 1] == 0) {

                    this.reviewAvailable[unit] = false;
                    this.reviewReasons[unit] = this.unitExams[unit - 1].buttonLabel + " not yet completed.";
                } else if (this.unitExams[unit - 1] != null
                        && (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM, RawPacingRules.UE_PASS)
                        || systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM,
                        RawPacingRules.TE_PASS))
                        && this.unitTotalExams[unit - 1] == 0) {

                    this.reviewAvailable[unit] = false;
                    this.reviewReasons[unit] = this.unitExams[unit - 1].buttonLabel + " not yet completed.";
                } else if (this.reviewExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM, RawPacingRules.UR_MSTR)
                        && this.unitPassedReviews[unit - 1] == 0) {

                    this.reviewAvailable[unit] = false;
                    this.reviewReasons[unit] = this.reviewExams[unit - 1].buttonLabel + " not yet mastered.";
                } else if (this.reviewExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM, RawPacingRules.UR_PASS)
                        && this.unitTotalReviews[unit - 1] == 0) {

                    this.reviewAvailable[unit] = false;
                    this.reviewReasons[unit] = this.reviewExams[unit - 1].buttonLabel + " not yet completed.";
                }
            }

            if (unit < this.unitExams.length && this.reviewAvailable[unit]) {

                if (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM, RawPacingRules.HW_MSTR)) {

                    final int numObj = this.unitObjMasteredHw[unit] == null ? 0
                            : this.unitObjMasteredHw[unit].length;

                    if (numObj > 0 && this.unitObjMasteredHw[unit][numObj - 1] == 0) {
                        this.reviewAvailable[unit] = false;
                        this.reviewReasons[unit] = "Unit required assignments not yet mastered.";
                    }
                } else if (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_REV_EXAM, RawPacingRules.HW_PASS)) {

                    final int numObj = this.unitObjTotalHw[unit] == null ? 0 : this.unitObjTotalHw[unit].length;

                    if (numObj > 0 && this.unitObjTotalHw[unit][numObj - 1] == 0) {
                        this.reviewAvailable[unit] = false;
                        this.reviewReasons[unit] = "Unit required assignments not yet completed.";
                    }
                }
            }
        }
    }

    /**
     * Scans the exams for this course and set the availability of each based on pacing structure settings and
     * historical data, assuming the user is not a guest, a special user ID, a visiting user, and the course is not a
     * tutorial.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @throws SQLException if there is an error accessing the database
     */
    private void checkProctoredExamAvailability(final Cache cache, final ZonedDateTime now) throws SQLException {

        final String pacing = this.pacingStructure.pacingStructure;
        final boolean isIncomplete = "Y".equals(this.studentCourse.iInProgress);
        final boolean isUncounted = "N".equals(this.studentCourse.iCounted);

        final SystemData systemData = cache.getSystemData();

        // Get the first and last dates in the term where students may work
        final LocalDate termFirst = systemData.getFirstClassDay();
        final LocalDate termLast = systemData.getLastClassDay();
        final LocalDate today = now.toLocalDate();

        // Determine availability of unit/proctored exams in each unit
        for (int unit = 0; unit <= this.maxUnit; ++unit) {

            final RawCusection sectionUnit = this.courseSectionUnits[unit];

            if (sectionUnit == null) {
                if (unit > 0 && unit < 5) {
                    Log.warning("No course section unit for unit " + unit);
                }

                continue;
            }

            // Date-based constraints
            if (isIncomplete) {
                if (isUncounted) {
                    // A non-counted Incomplete - only date constraint is incomplete deadline
                    final LocalDate deadline = this.studentCourse.iDeadlineDt;
                    if (deadline != null && deadline.isBefore(today)) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "Deadline to finish Incomplete has passed";
                    } else {
                        this.proctoredAvailable[unit] = true;
                    }
                }
            } else {
                // Only for non-Incompletes, since test dates would be from earlier term
                final LocalDate firstTest = sectionUnit.firstTestDt;
                final LocalDate lastTest = sectionUnit.lastTestDt;
                final String examLbl;
                if (this.unitExams[unit] == null) {
                    examLbl = "(no exam defined)";
                } else {
                    examLbl = this.unitExams[unit].buttonLabel;
                }

                if ("Y".equals(sectionUnit.showTestWindow) && firstTest != null && lastTest != null) {
                    describeTestingWindow(unit, termFirst, termLast, firstTest, lastTest, now, examLbl);
                }

                this.proctoredAvailable[unit] = true;

                if (firstTest != null && firstTest.isAfter(today)) {
                    this.proctoredAvailable[unit] = false;
                    this.proctoredReasons[unit] = examLbl + " not yet available";
                } else if (lastTest != null && today.isAfter(lastTest)) {
                    this.proctoredAvailable[unit] = false;
                    this.proctoredReasons[unit] = examLbl + " no longer available";
                }
            }

            if (this.proctoredAvailable[unit]) {
                final boolean isFinal = this.courseUnits[unit] != null && "FIN".equals(this.courseUnits[unit].unitType);

                // See if pacing structure constraints make the exam unavailable

                if (isFinal) {

                    if (this.unitExams[unit - 1] != null
                            && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                            RawPacingRules.ACTIVITY_FINAL_EXAM, RawPacingRules.UE_MSTR)
                            && this.unitPassedExams[unit - 1] == 0) {

                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = this.unitExams[unit - 1].buttonLabel + " not yet passed.";
                    } else if (this.unitExams[unit - 1] != null
                            && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                            RawPacingRules.ACTIVITY_FINAL_EXAM, RawPacingRules.UE_PASS)
                            && this.unitTotalExams[unit - 1] == 0) {

                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = this.unitExams[unit - 1].buttonLabel + " not yet completed.";
                    } else if (this.reviewExams[unit] != null
                            && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                            RawPacingRules.ACTIVITY_FINAL_EXAM, RawPacingRules.UR_MSTR)
                            && this.unitPassedReviews[unit] == 0) {

                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = this.reviewExams[unit].buttonLabel + " not yet passed.";
                    } else if (this.reviewExams[unit] != null
                            && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                            RawPacingRules.ACTIVITY_FINAL_EXAM, RawPacingRules.UR_PASS)
                            && this.unitTotalReviews[unit] == 0) {

                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = this.reviewExams[unit].buttonLabel + " not yet completed.";
                    }
                } else if (unit > 0 && this.unitExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UE_MSTR)
                        && this.unitPassedExams[unit - 1] == 0) {

                    this.proctoredAvailable[unit] = false;
                    this.proctoredReasons[unit] = this.unitExams[unit - 1].buttonLabel + " not yet passed.";
                } else if (unit > 0 && this.unitExams[unit - 1] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UE_PASS)
                        && this.unitTotalExams[unit - 1] == 0) {

                    this.proctoredAvailable[unit] = false;
                    this.proctoredReasons[unit] = this.unitExams[unit - 1].buttonLabel + " not yet completed.";
                } else if (this.reviewExams[unit] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UR_MSTR)
                        && this.unitPassedReviews[unit] == 0) {

                    this.proctoredAvailable[unit] = false;
                    this.proctoredReasons[unit] = this.reviewExams[unit].buttonLabel + " not yet passed.";
                } else if (this.reviewExams[unit] != null
                        && systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UR_PASS)
                        && this.unitTotalReviews[unit] == 0) {

                    this.proctoredAvailable[unit] = false;
                    this.proctoredReasons[unit] = this.reviewExams[unit].buttonLabel + " not yet completed.";
                } else if (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.HW_MSTR)) {

                    final int numObj = this.unitObjMasteredHw[unit] == null ? 0 : this.unitObjMasteredHw[unit].length;

                    if (numObj > 0 && this.unitObjMasteredHw[unit][numObj - 1] == 0) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "Unit required assignments not yet mastered.";
                    }
                } else if (systemData.isRequiredByPacingRules(this.activeTerm.term, pacing,
                        RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.HW_PASS)) {

                    final int numObj = this.unitObjTotalHw[unit] == null ? 0 : this.unitObjTotalHw[unit].length;

                    if (numObj > 0 && this.unitObjTotalHw[unit][numObj - 1] == 0) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "Unit required assignments not yet completed.";
                    }
                }
            }

            if (this.proctoredAvailable[unit]) {
                // See if constraints on the number of attempts per passing review make the exam unavailable

                final Integer perReview = sectionUnit.atmptsPerReview;
                Integer total = sectionUnit.nbrAtmptsAllow;

                if (total != null && total.intValue() >= 90) {

                    // A value >= 90 means there is no limit on the number of attempts, which is
                    // how we interpret null values
                    total = null;
                }

                if ((this.unitPassedExams[unit] > 0) || (perReview == null && total == null)) {
                    // Unit exam passed - no more review-based limits
                    this.allAttemptsUsed[unit] = false;
                    this.earnedProctored[unit] = Integer.MAX_VALUE;
                } else {
                    final String studentId = this.student.stuId;
                    final String courseId = this.course.course;

                    final List<RawStexam> examsTaken = RawStexamLogic.getExams(cache, studentId,
                            courseId, Integer.valueOf(unit), false, RawStexamLogic.UNIT_EXAM_TYPES);
                    final int taken = examsTaken.size();

                    this.allAttemptsUsed[unit] = total != null && taken >= total.intValue();

                    if (perReview == null) {
                        this.earnedProctored[unit] = Math.max(0, total.intValue() - taken);
                    } else {
                        final int count = RawStexamLogic.countUnitSinceLastPassedReview(cache,
                                studentId, courseId, Integer.valueOf(unit));
                        this.earnedProctored[unit] = Math.max(0, perReview.intValue() - count);
                    }

                    // If total limit is closer than per-review limit, use the smaller limit.
                    if (total != null && this.earnedProctored[unit] > total.intValue() - taken) {
                        this.earnedProctored[unit] = total.intValue() - taken;
                    }

                    if (this.allAttemptsUsed[unit]) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "No attempts remaining";
                    } else if (this.reviewExams[unit] == null) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "No review exam configured";
                    } else if (this.earnedProctored[unit] == 0) {
                        this.proctoredAvailable[unit] = false;
                        this.proctoredReasons[unit] = "Must retake and pass " + this.reviewExams[unit].buttonLabel;
                    }
                }
            }
        }

        // If the student has not passed the final, see if the course has ended
        if (!isProctoredPassed(5)) {
            boolean courseEnded = false;

            if (isIncomplete && isUncounted) {
                // Incomplete - use inc deadline
                final LocalDate deadline = this.studentCourse.iDeadlineDt;
                courseEnded = deadline.isBefore(today);
            } else // Not incomplete (or counted in deadlines) - use milestones
                if (this.maxUnit >= 5 && this.unitExamDeadlines[5] != null
                        && this.unitExamDeadlines[5].isBefore(today)) {

                    // Presence of non-null this.unitExamLastTries[5] indicates the student is
                    // eligible for a last-try attempt
                    if ((this.unitExamLastTries[5] == null) || this.unitExamLastTries[5].isBefore(today)) {
                        // Not eligible for last try
                        courseEnded = true;
                    }
                }

            if (courseEnded) {
                for (int unit = 0; unit < this.maxUnit; ++unit) {
                    this.proctoredAvailable[unit] = false;
                    this.proctoredReasons[unit] = "Course has ended";
                }
            }
        }
    }

    /**
     * Constructs the description of the testing window.
     *
     * @param unit      the unit
     * @param termFirst the first official date of the term in which students can work
     * @param termLast  the last official date of the term in which students can work
     * @param firstTest the first test date (could be before {@code termFirst})
     * @param lastTest  the last test date
     * @param now       the current date/time
     * @param examLbl   the label for the exam
     */
    private void describeTestingWindow(final int unit, final ChronoLocalDate termFirst, final ChronoLocalDate termLast,
                                       final ChronoLocalDate firstTest, final ChronoLocalDate lastTest,
                                       final ZonedDateTime now, final String examLbl) {

        // Build the description of the testing window
        if (!firstTest.isAfter(termFirst) && !termLast.isAfter(lastTest)) {

            // Testing window encompasses whole term
            this.proctoredRange[unit] = null;
        } else {
            final LocalDate today = now.toLocalDate();

            if (firstTest.isAfter(today)) {
                this.proctoredRange[unit] = "The testing window for the " + examLbl
                        + " will be open from " + TemporalUtils.FMT_MDY.format(firstTest) + " through "
                        + TemporalUtils.FMT_MDY.format(lastTest);
            } else if (today.isAfter(lastTest)) {
                this.proctoredRange[unit] = "The testing window for the " + examLbl
                        + " was open from " + TemporalUtils.FMT_MDY.format(firstTest) + " through "
                        + TemporalUtils.FMT_MDY.format(lastTest);
            } else {
                this.proctoredRange[unit] = "The testing window for the " + examLbl
                        + " is open from " + TemporalUtils.FMT_MDY.format(firstTest) + " through "
                        + TemporalUtils.FMT_MDY.format(lastTest);
            }
        }
    }

    /**
     * Counts the number of objectives in a unit.
     *
     * @param unit the unit number
     * @return the number of objectives
     */
    private int countObjectives(final int unit) {

        int maxObj = 0;

        for (final AssignmentRec hw : this.homeworks) {

            if (hw.unit == null || hw.unit.intValue() != unit || hw.objective == null) {
                continue;
            }

            final int obj = hw.objective.intValue();

            if (obj > maxObj) {
                maxObj = obj;
            }
        }

        return maxObj;
    }

    /**
     * Given a unit and objective, determines the index of the homework that matches those values.
     *
     * @param unit      the unit to search for
     * @param objective the objective to search for
     * @return the index of the matching homework, or -1 if not found
     */
    private int findHomeworkIndex(final int unit, final int objective) {

        final int numHw = this.homeworks.size();
        for (int i = 0; i < numHw; ++i) {
            final AssignmentRec hw = this.homeworks.get(i);

            if (hw.unit == null || hw.objective == null || (hw.unit.intValue() != unit)) {
                continue;
            }

            if (hw.objective.intValue() == objective) {
                return i;
            }
        }

        Log.warning("No homework for ", this.course.course, " unit " + unit + " obj " + objective);

        return -1;
    }

    /**
     * Main method to exercise the methods of the class.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final WebSiteProfile siteProfile = map.getWebSiteProfile(Contexts.PRECALC_HOST, Contexts.INSTRUCTION_PATH);
        if (siteProfile == null) {
            Log.warning("Site profile not found");
        } else {
            final StudentCourseStatus status = new StudentCourseStatus(siteProfile.dbProfile);

            final String sessionId = CoreConstants.newId(ISessionManager.SESSION_ID_LEN);

            final LiveSessionInfo live = new LiveSessionInfo(sessionId, "None", ERole.STUDENT);
            live.setUserInfo("888888888", "Test", "Student", "Test Student");

            final DbContext ctx = siteProfile.dbProfile.getDbContext(ESchemaUse.PRIMARY);
            try {
                final DbConnection conn = ctx.checkOutConnection();
                final Cache cache = new Cache(siteProfile.dbProfile, conn);

                try {
                    final ImmutableSessionInfo session = new ImmutableSessionInfo(live);

                    if (status.gatherData(cache, session, "888888888", RawRecordConstants.M117, false, false)) {

                        final SystemData systemData = cache.getSystemData();
                        final TermRec active = systemData.getActiveTerm();
                        final RawPacingStructure pacing = status.pacingStructure;

                        Log.info("Pacing structure: ", pacing.pacingStructure);

                        Log.info("Prior UE mastery required for Unit exam: "
                                + systemData.isRequiredByPacingRules(active.term, pacing.pacingStructure,
                                RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UE_MSTR));

                        Log.info("Prior UE passed required for Unit exam: "
                                + systemData.isRequiredByPacingRules(active.term, pacing.pacingStructure,
                                RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UE_PASS));

                        Log.info("UR mastery required for Unit exam: "
                                + systemData.isRequiredByPacingRules(active.term, pacing.pacingStructure,
                                RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UR_MSTR));

                        Log.info("UR passed required for Unit exam: "
                                + systemData.isRequiredByPacingRules(active.term, pacing.pacingStructure,
                                RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.UR_PASS));

                        Log.info("Homework mastery required for Unit exam: "
                                + systemData.isRequiredByPacingRules(active.term, pacing.pacingStructure,
                                RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.HW_MSTR));

                        Log.info("Homework passed required for Unit exam: "
                                + systemData.isRequiredByPacingRules(active.term, pacing.pacingStructure,
                                RawPacingRules.ACTIVITY_UNIT_EXAM, RawPacingRules.HW_PASS));

                        final StudentCourseScores scores = status.scores;

                        final int total = scores.getTotalScore();
                        Log.fine("Total Score: " + total);
                        Log.fine("Prereq: " + status.studentCourse.prereqSatis);

                        final int max = status.getMaxUnit();

                        for (int i = 0; i <= max; ++i) {

                            Log.info(scores.getPassingUnitExamScore(i) + " points from passing unit " + i
                                    + " exam with raw score " + scores.getRawUnitExamScore(i));

                            final int count = status.getNumLessons(i);
                            for (int j = 1; j <= count; j++) {
                                if (status.hasHomework(i, j)) {
                                    if (status.isHomeworkAvailable(i, j)) {
                                        Log.info(" Homework " + i + CoreConstants.DOT + j + " available ("
                                                + status.getHomeworkStatus(i, j) + ")");
                                    } else {
                                        Log.info(" Homework " + i + CoreConstants.DOT + j + " unavailable ("
                                                + status.getHomeworkReason(i, j) + ")");
                                    }
                                } else {
                                    Log.info(" No Homework " + i + CoreConstants.DOT + j);
                                }
                            }

                            if (status.isReviewExamAvailable(i)) {
                                Log.info(" Review Exam " + i + " available (" + status.getReviewStatus(i) + ") " +
                                        "On-time: "
                                        + status.isReviewPassedOnTime(i));
                            } else {
                                Log.info(" Review Exam " + i + " unavailable (" + status.getReviewReason(i) + ")");
                            }

                            if (status.isProctoredPassed(i)) {
                                if (status.isProctoredExamAvailable(i)) {
                                    Log.info(" Proctored Exam ", Integer.toString(i), " passed and available (",
                                            status.getProctoredStatus(i), ") score=",
                                            Integer.toString(status.scores.getRawUnitExamScore(i)), ", ",
                                            Integer.toString(status.getProctoredAttemptsAvailable(i)),
                                            " attempts available");
                                } else {
                                    Log.info(" Proctored Exam ", Integer.toString(i), " passed but unavailable (",
                                            status.getProctoredStatus(i), ") score=",
                                            Integer.toString(status.scores.getRawUnitExamScore(i)), ", ",
                                            Integer.toString(status.getProctoredAttemptsAvailable(i)),
                                            " attempts available");
                                }
                            } else if (status.isProctoredExamAvailable(i)) {
                                Log.info(" Proctored Exam " + i + " available (" + status.getProctoredStatus(i) + ") "
                                        + status.getProctoredAttemptsAvailable(i) + " attempts available");
                                Log.info(" Proctored Exam " + i + " deadline: " + status.getUnitExamDeadline(i)
                                        + " last try: " + status.getUnitExamLastTry(i));
                            } else {
                                Log.info(" Proctored Exam " + i + " unavailable (" + status.getProctoredReason(i) +
                                        ")");
                            }
                        }
                    } else {
                        Log.warning(status.getErrorText());
                    }
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }
}
