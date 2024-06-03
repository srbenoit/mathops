package dev.mathops.db.logic;

import dev.mathops.db.old.Cache;
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

import java.util.List;

/**
 * A data container for all data associated with a single student, with specific data loaded lazily as needed.
 */
public class StudentData {

    /** The cache. */
    private final Cache cache;

    /** The student ID. */
    private final String stuId;

    /** The student record. */
    private RawStudent studentRecord = null;

    /** The list of all holds on the student's account. */
    private List<RawAdminHold> holds = null;

    /** The list of all disciplinary actions on the student's account. */
    private List<RawDiscipline> discipline = null;

    /** The list of all "visiting student" configurations for the student. */
    private List<RawExceptStu> visiting = null;

    /** The list of all transfer credit records on the student account. */
    private List<RawFfrTrns> transfer = null;

    /** The list of all mastery attempts on the student's record. */
    private List<MasteryAttemptRec> masteryAttempts = null;

    /** The list of all milestones that apply to the student this term. */
    private List<RawMilestone> milestones = null;

    /** The list of all milestone overrides that apply to the student this term. */
    private List<RawStmilestone> studentMilestones = null;

    /** The list of all standards-based milestones that apply to this student this term. */
    private List<StandardMilestoneRec> standardMilestones = null;

    /** The list of all overrides to standards-based milestones for this student this term. */
    private List<StudentStandardMilestoneRec> studentStandardMilestones = null;

    /** The list of all placement credit on record for this student. */
    private List<RawMpeCredit> mpeCredit = null;

    /** The list of all placement credit that was denied for this student. */
    private List<RawMpecrDenied> mpeCreditDenied = null;

    /** The list of all deadline appeals on record for this student. */
    private List<RawPaceAppeals> paceAppeals = null;

    /** The list of all pending exams on record for this student. */
    private List<RawPendingExam> pendingExams = null;

    /** The list of all special student configurations for this student. */
    private List<RawSpecialStus> specialStus = null;

    /** The list of challenge exams this student has taken. */
    private List<RawStchallenge> challengeExams = null;

    /** The list of all registrations for this student. */
    private List<RawStcourse> registrations = null;

    /** The list of all course unit status objects  for this student. */
    private List<RawStcunit> courseUnits = null;

    /** The list of all course unit objective status objects for this student. */
    private List<RawStcuobjective> courseUnitObjectives = null;

    /** The list of all exam attempts on record for this student. */
    private List<RawStexam> studentExams = null;

    /** The list of all homework attempts on record for this student. */
    private List<RawStexam> studentHomeworks = null;

    /** The list of all Math Plan responses on record for this student. */
    private List<RawStmathplan> mathPlanResponses = null;

    /** The list of all placement attempts on record for this student. */
    private List<RawStmpe> placementAttempts = null;

    /** The list of all placement attempts on record for this student. */
    private List<RawStmsg> messagesSent = null;

    /** The list of all resources on loan to the student. */
    private List<RawStresource> resources = null;

    /** The list of all survey responses. */
    private List<RawStsurveyqa> surveyResponses = null;

    /** The student term configuration. */
    private RawStterm studentTerm = null;

    /** The list of student course mastery records for the student. */
    private List<StudentCourseMasteryRec> studentCourseMastery = null;

    /** The list of student unit mastery records for the student. */
    private List<StudentUnitMasteryRec> studentUnitMastery = null;

    /** The list of student visits to the testing center. */
    private List<RawStvisit> visits = null;

    /**
     * Constructs a new {@code StudentData}.
     *
     * @param theCache the cache
     * @param theStuId the student ID
     */
    public StudentData(final Cache theCache, final String theStuId) {

        this.cache = theCache;
        this.stuId = theStuId;
    }



}
