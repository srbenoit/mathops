package dev.mathops.db.logic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawDisciplineLogic;
import dev.mathops.db.old.rawlogic.RawExceptStuLogic;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStcunitLogic;
import dev.mathops.db.old.rawlogic.RawStcuobjectiveLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStmsgLogic;
import dev.mathops.db.old.rawlogic.RawStresourceLogic;
import dev.mathops.db.old.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawlogic.RawStvisitLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawDiscipline;
import dev.mathops.db.old.rawrecord.RawExceptStu;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStcunit;
import dev.mathops.db.old.rawrecord.RawStcuobjective;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawStvisit;
import dev.mathops.db.old.rec.MasteryAttemptRec;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.rec.StudentCourseMasteryRec;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.rec.StudentUnitMasteryRec;
import dev.mathops.db.old.reclogic.MasteryAttemptLogic;
import dev.mathops.db.old.reclogic.StandardMilestoneLogic;
import dev.mathops.db.old.reclogic.StudentCourseMasteryLogic;
import dev.mathops.db.old.reclogic.StudentStandardMilestoneLogic;
import dev.mathops.db.old.reclogic.StudentUnitMasteryLogic;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * A data container for all data associated with a single student, with specific data loaded lazily as needed.
 */
public final class StudentData {

    /** The cache. */
    private final Cache cache;

    /** The student ID. */
    private final String stuId;

    /** The setting for doing live refreshes on queries. */
    private final ELiveRefreshes liveRefreshes;

    /** The student record. */
    private RawStudent studentRecord = null;

    /** The list of all holds on the student's account. */
    private List<RawAdminHold> holds = null;

    /** The list of all disciplinary actions on the student's account. */
    private List<RawDiscipline> disciplinaryActions = null;

    /** The list of all "visiting student" configurations for the student. */
    private List<RawExceptStu> visitingRegistrations = null;

    /** The list of all transfer credit records on the student account. */
    private List<RawFfrTrns> transferCredit = null;

    /** The list of all resources on loan to the student. */
    private List<RawStresource> resourcesOnLoan = null;

    /** The list of all placement attempts on record for this student. */
    private List<RawStmsg> messagesSent = null;

    /** The list of student visits to the center. */
    private List<RawStvisit> centerVisits = null;

    /** The list of all pending exams on record for this student. */
    private List<RawPendingExam> pendingExams = null;

    /** The list of all special student configurations for this student. */
    private List<RawSpecialStus> specialCategories = null;

    /** The list of all Math Plan responses on record for this student. */
    private List<RawStmathplan> mathPlanResponses = null;

    /** The list of all placement attempts on record for this student. */
    private List<RawStmpe> placementAttempts = null;

    /** The list of all placement credit on record for this student. */
    private List<RawMpeCredit> placementCredit = null;

    /** The list of all placement credit that was denied for this student. */
    private List<RawMpecrDenied> placementDenied = null;

    /** The list of challenge exams this student has taken. */
    private List<RawStchallenge> challengeExams = null;

    /** The list of all survey responses. */
    private List<RawStsurveyqa> surveyResponses = null;

    /** The list of all registrations for this student. */
    private List<RawStcourse> registrations = null;

    /** The student term configurations. */
    private List<RawStterm> studentTerm = null;

    /** The list of all exam attempts on record for this student. */
    private List<RawStexam> studentExams = null;

    /** The list of all homework attempts on record for this student. */
    private List<RawSthomework> studentHomeworks = null;

    /** The list of all course unit status objects  for this student. */
    private List<RawStcunit> studentCourseUnits = null;

    /** The list of all course unit objective status objects for this student. */
    private List<RawStcuobjective> courseUnitObjectives = null;

    /** The list of all mastery attempts on the student's record. */
    private List<MasteryAttemptRec> masteryAttempts = null;

    /** The list of student course mastery records for the student. */
    private List<StudentCourseMasteryRec> studentCourseMastery = null;

    /** The list of student unit mastery records for the student. */
    private List<StudentUnitMasteryRec> studentUnitMastery = null;

    /** The list of all milestones that apply to the student this term. */
    private List<RawMilestone> milestones = null;

    /** The list of all milestone overrides that apply to the student this term. */
    private List<RawStmilestone> studentMilestones = null;

    /** The list of all standards-based milestones that apply to this student this term. */
    private List<StandardMilestoneRec> standardMilestones = null;

    /** The list of all overrides to standards-based milestones for this student this term. */
    private List<StudentStandardMilestoneRec> studentStandardMilestones = null;

    /** The list of all deadline appeals on record for this student. */
    private List<RawPaceAppeals> deadlineAppeals = null;

    /**
     * Constructs a new {@code StudentData}.
     *
     * @param theCache         the cache
     * @param theStuId         the student ID
     * @param theLiveRefreshes true if live student data should be queried;
     */
    public StudentData(final Cache theCache, final String theStuId, final ELiveRefreshes theLiveRefreshes) {

        if (theCache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (theStuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theLiveRefreshes == null) {
            throw new IllegalArgumentException("Live refreshes setting may not be null");
        }

        this.cache = theCache;
        this.stuId = theStuId;
        this.liveRefreshes = theLiveRefreshes;
    }

    /**
     * Gets the student record, querying for it if it has not already been loaded.
     *
     * @return the student record; null only if the student record does not exist
     * @throws SQLException if there is an error accessing the database
     */
    private RawStudent getStudentRecord() throws SQLException {

        if (this.studentRecord == null) {
            if (this.liveRefreshes == ELiveRefreshes.ALL) {
                this.studentRecord = RawStudentLogic.query(this.cache, this.stuId, true);
            } else {
                this.studentRecord = RawStudentLogic.query(this.cache, this.stuId, false);

                if (this.liveRefreshes == ELiveRefreshes.IF_MISSING) {
                    this.studentRecord = RawStudentLogic.query(this.cache, this.stuId, true);
                }
            }
        }

        return this.studentRecord;
    }

    /**
     * Gets the list of all holds on the student's account.
     *
     * @return the list of holds
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawAdminHold> getHolds() throws SQLException {

        if (this.holds == null) {
            this.holds = RawAdminHoldLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.holds;
    }

    /**
     * Gets the list of all disciplinary actions on the student's account.
     *
     * @return the list of disciplinary actions
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawDiscipline> getDisciplinaryActions() throws SQLException {

        if (this.disciplinaryActions == null) {
            this.disciplinaryActions = RawDisciplineLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.disciplinaryActions;
    }

    /**
     * Gets the list of all visiting student registrations for the student.
     *
     * @return the list of visiting student registrations
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawExceptStu> getVisitingRegistrations() throws SQLException {

        if (this.visitingRegistrations == null) {
            this.visitingRegistrations = RawExceptStuLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.visitingRegistrations;
    }

    /**
     * Gets the list of all transfer credit for the student.
     *
     * @return the list of transfer credit
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawFfrTrns> getTransferCredit() throws SQLException {

        if (this.transferCredit == null) {
            this.transferCredit = RawFfrTrnsLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.transferCredit;
    }

    /**
     * Gets the list of all resources on loan to the student.
     *
     * @return the list of resources on loan
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStresource> getResourcesOnLoan() throws SQLException {

        if (this.resourcesOnLoan == null) {
            this.resourcesOnLoan = RawStresourceLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.resourcesOnLoan;
    }

    /**
     * Gets the list of all messages sent to the student.
     *
     * @return the list of messages sent
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStmsg> getMessagesSent() throws SQLException {

        if (this.messagesSent == null) {
            this.messagesSent = RawStmsgLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.messagesSent;
    }

    /**
     * Gets the list of all center visits by the student.
     *
     * @return the list of center visits
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStvisit> getCenterVisits() throws SQLException {

        if (this.centerVisits == null) {
            this.centerVisits = RawStvisitLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.centerVisits;
    }

    /**
     * Gets the list of all pending exams on record for this student.
     *
     * @return the list of pending exams
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawPendingExam> getPendingExams() throws SQLException {

        if (this.pendingExams == null) {
            this.pendingExams = RawPendingExamLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.pendingExams;
    }

    /**
     * Gets the list of all special student categories to which the student belongs.
     *
     * @return the list of special student categories
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawSpecialStus> getSpecialCategories() throws SQLException {

        if (this.specialCategories == null) {
            this.specialCategories = RawSpecialStusLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.specialCategories;
    }

    /**
     * Gets the list of all Math Plan responses on record for the student.
     *
     * @return the list of Math Plan responses
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStmathplan> getMathPlanResponses() throws SQLException {

        if (this.mathPlanResponses == null) {
            this.mathPlanResponses = RawStmathplanLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.mathPlanResponses;
    }

    /**
     * Gets the list of all placement attempts on record for the student.
     *
     * @return the list of placement attempts
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStmpe> getPlacementAttempts() throws SQLException {

        if (this.placementAttempts == null) {
            this.placementAttempts = RawStmpeLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.placementAttempts;
    }

    /**
     * Gets the list of all placement credit earned by the student.
     *
     * @return the list of placement credit
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawMpeCredit> getPlacementCredit() throws SQLException {

        if (this.placementCredit == null) {
            this.placementCredit = RawMpeCreditLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.placementCredit;
    }

    /**
     * Gets the list of all placement that was denied to the student.
     *
     * @return the list of placement denied
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawMpecrDenied> getPlacementDenied() throws SQLException {

        if (this.placementDenied == null) {
            this.placementDenied = RawMpecrDeniedLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.placementDenied;
    }

    /**
     * Gets the list of all challenge exams taken by the student.
     *
     * @return the list of challenge exams
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStchallenge> getChallengeExams() throws SQLException {

        if (this.challengeExams == null) {
            this.challengeExams = RawStchallengeLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.challengeExams;
    }

    /**
     * Gets the list of all survey responses the student provided on the placement tool.
     *
     * @return the list of survey responses
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStsurveyqa> getSurveyResponses() throws SQLException {

        if (this.surveyResponses == null) {
            this.surveyResponses = RawStsurveyqaLogic.queryLatestByStudent(this.cache, this.stuId);
        }

        return this.surveyResponses;
    }

    /**
     * Gets the list of all registrations for this student.
     *
     * @return the list of registrations
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStcourse> getRegistrations() throws SQLException {

        if (this.registrations == null) {
            this.registrations = RawStcourseLogic.queryByStudent(this.cache, this.stuId, true, true);
        }

        return this.registrations;
    }

    /**
     * Gets the student term configurations.
     *
     * @return the student term configurations
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStterm> getStudentTerm() throws SQLException {

        if (this.studentTerm == null) {
            this.studentTerm = RawSttermLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.studentTerm;
    }

    /**
     * Gets the list of all records of exams taken by the student.
     *
     * @return the student exams
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStexam> getStudentExams() throws SQLException {

        if (this.studentExams == null) {
            this.studentExams = RawStexamLogic.queryByStudent(this.cache, this.stuId, true);
        }

        return this.studentExams;
    }

    /**
     * Gets the list of all records of homeworks taken by the student.
     *
     * @return the student homeworks
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawSthomework> getStudentHomework() throws SQLException {

        if (this.studentHomeworks == null) {
            this.studentHomeworks = RawSthomeworkLogic.queryByStudent(this.cache, this.stuId, true);
        }

        return this.studentHomeworks;
    }

    /**
     * Gets the list of all student course unit status objects.
     *
     * @return the student course unit status objects
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStcunit> getStudentCourseUnits() throws SQLException {

        if (this.studentCourseUnits == null) {
            this.studentCourseUnits = RawStcunitLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.studentCourseUnits;
    }

    /**
     * Gets the list of all student course unit objective status objects.
     *
     * @return the student course unit objective status objects
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStcuobjective> getStudentCourseUnitObjectives() throws SQLException {

        if (this.courseUnitObjectives == null) {
            this.courseUnitObjectives = RawStcuobjectiveLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.courseUnitObjectives;
    }

    /**
     * Gets the list of all mastery attempts by the student.
     *
     * @return the mastery attempts
     * @throws SQLException if there is an error accessing the database
     */
    private List<MasteryAttemptRec> getMasteryAttempts() throws SQLException {

        if (this.masteryAttempts == null) {
            this.masteryAttempts = MasteryAttemptLogic.get(this.cache).queryByStudent(this.cache, this.stuId);
        }

        return this.masteryAttempts;
    }

    /**
     * Gets the list of all course mastery status objects for the student.
     *
     * @return the course mastery status objects
     * @throws SQLException if there is an error accessing the database
     */
    private List<StudentCourseMasteryRec> getStudentCourseMastery() throws SQLException {

        if (this.studentCourseMastery == null) {
            this.studentCourseMastery = StudentCourseMasteryLogic.get(this.cache).queryByStudent(this.cache,
                    this.stuId);
        }

        return this.studentCourseMastery;
    }

    /**
     * Gets the list of all unit mastery status objects for the student.
     *
     * @return the unit mastery status objects
     * @throws SQLException if there is an error accessing the database
     */
    private List<StudentUnitMasteryRec> getStudentUnitMastery() throws SQLException {

        if (this.studentUnitMastery == null) {
            this.studentUnitMastery = StudentUnitMasteryLogic.get(this.cache).queryByStudent(this.cache, this.stuId);
        }

        return this.studentUnitMastery;
    }

    /**
     * Gets the list of all course milestones in the current term.
     *
     * @return the course milestones
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawMilestone> getMilestones() throws SQLException {

        if (this.milestones == null) {
            this.milestones = RawMilestoneLogic.INSTANCE.queryAll(this.cache);
        }

        return this.milestones;
    }

    /**
     * Gets the list of all student course milestone overrides in the current term.
     *
     * @return the student course milestone overrides
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStmilestone> getStudentMilestones() throws SQLException {

        if (this.studentMilestones == null) {
            this.studentMilestones = RawStmilestoneLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.studentMilestones;
    }

    /**
     * Gets the list of all standard milestones in the current term.
     *
     * @return the standard milestones
     * @throws SQLException if there is an error accessing the database
     */
    private List<StandardMilestoneRec> getStandardMilestones() throws SQLException {

        if (this.standardMilestones == null) {
            this.standardMilestones = StandardMilestoneLogic.get(this.cache).queryAll(this.cache);
        }

        return this.standardMilestones;
    }

    /**
     * Gets the list of all student standard milestone overrides in the current term.
     *
     * @return the student standard milestone overrides
     * @throws SQLException if there is an error accessing the database
     */
    private List<StudentStandardMilestoneRec> getStudentStandardMilestones() throws SQLException {

        if (this.studentStandardMilestones == null) {
            this.studentStandardMilestones = StudentStandardMilestoneLogic.get(this.cache).queryByStudent(this.cache,
                    this.stuId);
        }

        return this.studentStandardMilestones;
    }

    /**
     * Gets the list of all deadline appeals by the student.
     *
     * @return the deadline appeals
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawPaceAppeals> getDeadlineAppeals() throws SQLException {

        if (this.deadlineAppeals == null) {
            this.deadlineAppeals = RawPaceAppealsLogic.queryByStudent(this.cache, this.stuId);
        }

        return this.deadlineAppeals;
    }

    /**
     * Main method to exercise this class.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final DbProfile dbProfile =map.getCodeProfile(Contexts.BATCH_PATH);
        if (dbProfile == null) {
            Log.warning("Code profile not found");
        } else {
            final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

            final ZonedDateTime now = ZonedDateTime.now();

            try {
                final DbConnection conn = ctx.checkOutConnection();

                try {
                    final Cache cache = new Cache(dbProfile, conn);
                    final StudentData data = new StudentData(cache, "823251213", ELiveRefreshes.NONE);
                    runTests(data);
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Runs tests to exercise the class.
     *
     * @param data the student data object
     */
    private static void runTests(final StudentData data) {

    }
}
