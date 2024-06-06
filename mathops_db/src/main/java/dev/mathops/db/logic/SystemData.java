package dev.mathops.db.logic;

import dev.mathops.db.old.rawlogic.RawCampusCalendarLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawEtextCourseLogic;
import dev.mathops.db.old.rawlogic.RawEtextLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawExceptStuLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawRemoteMpeLogic;
import dev.mathops.db.old.rawlogic.RawWhichDbLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawEtext;
import dev.mathops.db.old.rawrecord.RawEtextCourse;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRemoteMpe;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.rec.MasteryExamRec;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.reclogic.AssignmentLogic;
import dev.mathops.db.old.reclogic.MasteryExamLogic;
import dev.mathops.db.old.reclogic.StandardMilestoneLogic;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data container for system data (not related to individual students) used in a single webpage generation or business
 * process.  Data is loaded lazily when queried, and can be "forgotten" to trigger a re-query if underlying data is
 * changed.
 */
public final class SystemData {

    /** The cache. */
    private final Cache cache;

    /** The database to which the cache is connected. */
    private RawWhichDb whichDb = null;

    /** The active term. */
    private TermRec activeTerm = null;

    /** The prior term. */
    private TermRec priorTerm = null;

    /** The next term. */
    private TermRec nextTerm = null;

    /** The list of all future terms. */
    private List<TermRec> futureTerms = null;

    /** The list of all course milestones. */
    private List<RawMilestone> milestones = null;

    /** The list of all standards-based milestones. */
    private List<StandardMilestoneRec> standardMilestones = null;

    /** All campus calendar records. */
    private List<RawCampusCalendar> campusCalendars;

    /** All remote placement windows. */
    private List<RawRemoteMpe> remotePlacementWindows;

    /** All e-texts. */
    private List<RawEtext> etexts;

    /** All e-text course mappings. */
    private List<RawEtextCourse> etextCourses;

    /** A map from term key to a list of course sections for that term. */
    private Map<TermKey, List<RawCsection>> courseSections;

    /** A map from course ID to all assignments for that course. */
    private Map<String, List<AssignmentRec>> assignments;

    /** A map from course ID to all mastery exams for that course. */
    private Map<String, List<MasteryExamRec>> masteryExams;

    /** A map from course ID to all exams for that course. */
    private Map<String, List<RawExam>> exams;

    /**
     * Constructs a new {@code SystemData}.
     *
     * @param theCache the cache
     */
    public SystemData(final Cache theCache) {

        if (theCache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }

        this.cache = theCache;
    }

    /**
     * Gets the cache.
     *
     * @return the cache
     */
    public Cache getCache() {

        return this.cache;
    }

    /**
     * Gets the database descriptor for the database to which this object's cache is connected.
     *
     * @return the active term
     * @throws SQLException if there is an error accessing the database
     */
    public RawWhichDb getWhichDb() throws SQLException {

        if (this.whichDb == null) {
            this.whichDb = RawWhichDbLogic.query(this.cache);
        }

        return this.whichDb;
    }

    /**
     * Gets the active term.
     *
     * @return the active term
     * @throws SQLException if there is an error accessing the database
     */
    public TermRec getActiveTerm() throws SQLException {

        if (this.activeTerm == null) {
            this.activeTerm = TermLogic.get(this.cache).queryActive(this.cache);
        }

        return this.activeTerm;
    }

    /**
     * Gets the prior term.
     *
     * @return the prior term
     * @throws SQLException if there is an error accessing the database
     */
    public TermRec getPriorTerm() throws SQLException {

        if (this.priorTerm == null) {
            this.priorTerm = TermLogic.get(this.cache).queryPrior(this.cache);
        }

        return this.priorTerm;
    }

    /**
     * Gets the next term.
     *
     * @return the next term
     * @throws SQLException if there is an error accessing the database
     */
    public TermRec getNextTerm() throws SQLException {

        if (this.nextTerm == null) {
            this.nextTerm = TermLogic.get(this.cache).queryNext(this.cache);
        }

        return this.nextTerm;
    }

    /**
     * Gets the next term.
     *
     * @return the next term
     * @throws SQLException if there is an error accessing the database
     */
    public List<TermRec> getFutureTerms() throws SQLException {

        if (this.futureTerms == null) {
            this.futureTerms = TermLogic.get(this.cache).getFutureTerms(this.cache);
        }

        return this.futureTerms;
    }

    /**
     * Gets the list of all course milestones.
     *
     * @return the course milestones
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawMilestone> getMilestones() throws SQLException {

        if (this.milestones == null) {
            this.milestones = RawMilestoneLogic.INSTANCE.queryAll(this.cache);
        }

        return this.milestones;
    }

    /**
     * Gets the list of all course milestones in a specified term with a specified pace and pace track.
     *
     * @param term      the term whose milestones to retrieve
     * @param pace      the pace whose milestones to retrieve
     * @param paceTrack the pace track whose milestones to retrieve
     * @return the course milestones
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawMilestone> getMilestones(final TermKey term, final Integer pace, final String paceTrack)
            throws SQLException {

        final List<RawMilestone> all = getMilestones();
        final int paceInt = pace.intValue();
        final int size = Math.max(35, 7 * paceInt);
        final List<RawMilestone> result = new ArrayList<>(size);

        for (final RawMilestone test : all) {
            if (test.termKey.equals(term) && test.pace.equals(pace) && test.paceTrack.equals(paceTrack)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets the list of all standard milestones in the current term.
     *
     * @return the standard milestones
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardMilestoneRec> getStandardMilestones() throws SQLException {

        if (this.standardMilestones == null) {
            this.standardMilestones = StandardMilestoneLogic.get(this.cache).queryAll(this.cache);
        }

        return this.standardMilestones;
    }

    /**
     * Gets all course sections for a single term.
     *
     * @param term the term key
     * @return the list of course sections
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCsection> getCourseSections(final TermKey term) throws SQLException {

        List<RawCsection> result = null;

        if (this.courseSections == null) {
            this.courseSections = new HashMap<>(4);
        } else {
            result = this.courseSections.get(term);
        }

        if (result == null) {
            result = RawCsectionLogic.queryByTerm(this.cache, term);
            this.courseSections.put(term, result);
        }

        return result;
    }

    /**
     * Gets a particular course sections in a specified term.
     *
     * @param course the course
     * @param sect   the section
     * @param term   the term key
     * @return the list of course sections
     * @throws SQLException if there is an error accessing the database
     */
    public RawCsection getCourseSection(final String course, final String sect, final TermKey term)
            throws SQLException {

        final List<RawCsection> termSections = getCourseSections(term);
        RawCsection result = null;

        for (final RawCsection test : termSections) {
            if (test.course.equals(course) && test.sect.equals(sect)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all campus calendar records.
     *
     * @return the list of campus calendar records
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCampusCalendar> getCampusCalendars() throws SQLException {

        if (this.campusCalendars == null) {
            this.campusCalendars = RawCampusCalendarLogic.INSTANCE.queryAll(this.cache);
        }

        return this.campusCalendars;
    }

    /**
     * Gets all remote placement windows.
     *
     * @return the list of remote placement windows
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawRemoteMpe> getRemotePlacementWindows() throws SQLException {

        if (this.remotePlacementWindows == null) {
            this.remotePlacementWindows = RawRemoteMpeLogic.INSTANCE.queryAll(this.cache);
        }

        return this.remotePlacementWindows;
    }

    /**
     * Gets all remote placement windows for a specified course.  Windows for all application terms are included.
     *
     * @param course the course ID
     * @return the list of remote placement windows
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawRemoteMpe> getRemotePlacementWindowsForCourse(final String course) throws SQLException {

        final List<RawRemoteMpe> all = getRemotePlacementWindows();
        final List<RawRemoteMpe> match = new ArrayList<>(5);

        for (final RawRemoteMpe test : all) {
            if (test.course.equals(course)) {
                match.add(test);
            }
        }

        return match;
    }

    /**
     * Gets all e-texts.
     *
     * @return the list of e-texts
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawEtext> getETexts() throws SQLException {

        if (this.etexts == null) {
            this.etexts = RawEtextLogic.INSTANCE.queryAll(this.cache);
        }

        return this.etexts;
    }

    /**
     * Gets all e-texts.
     *
     * @return the list of e-texts
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawEtextCourse> getETextCourses() throws SQLException {

        if (this.etextCourses == null) {
            this.etextCourses = RawEtextCourseLogic.INSTANCE.queryAll(this.cache);
        }

        return this.etextCourses;
    }

    /**
     * Gets all e-texts.
     *
     * @param eTextId the e-text Id
     * @return the list of e-texts
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawEtextCourse> getETextCoursesByETextId(final String eTextId) throws SQLException {

        final List<RawEtextCourse> all = getETextCourses();
        final List<RawEtextCourse> match = new ArrayList<>(5);

        for (final RawEtextCourse test : all) {
            if (test.etextId.equals(eTextId)) {
                match.add(test);
            }
        }

        return match;
    }

    /**
     * Gets a list of all assignments for a course.
     *
     * @param course the course
     * @return the list of assignments
     * @throws SQLException if there is an error accessing the database
     */
    public List<AssignmentRec> getActiveAssignmentsByCourse(final String course) throws SQLException {

        List<AssignmentRec> result = null;

        if (this.assignments == null) {
            this.assignments = new HashMap<>(5);
        } else {
            result = this.assignments.get(course);
        }

        if (result == null) {
            result = AssignmentLogic.get(this.cache).queryActiveByCourse(this.cache, course, null);
            this.assignments.put(course, result);
        }

        return result;
    }

    /**
     * Gets a list of all assignments of a specified type for a course.
     *
     * @param course the course
     * @param type the type of assignment to retrieve
     * @return the list of assignments
     * @throws SQLException if there is an error accessing the database
     */
    public List<AssignmentRec> getActiveAssignmentsByCourse(final String course, final String type) throws SQLException {

        final List<AssignmentRec> all = getActiveAssignmentsByCourse(course);
        final int count = all.size();

        final List<AssignmentRec> match = new ArrayList<>(count);

        for (final AssignmentRec test : all) {
            if (test.assignmentType.equals(type)) {
                match.add(test);
            }
        }

        return match;
    }

    /**
     * Gets a list of all mastery exams for a course.
     *
     * @param course the course
     * @return the list of mastery exams
     * @throws SQLException if there is an error accessing the database
     */
    public List<MasteryExamRec> getActiveMasteryExamsByCourse(final String course) throws SQLException {

        List<MasteryExamRec> result = null;

        if (this.masteryExams == null) {
            this.masteryExams = new HashMap<>(5);
        } else {
            result = this.masteryExams.get(course);
        }

        if (result == null) {
            result = MasteryExamLogic.get(this.cache).queryActiveByCourse(this.cache, course);
            this.masteryExams.put(course, result);
        }

        return result;
    }

    /**
     * Gets all active exams for a course.
     *
     * @param course the course ID
     * @return the list of exams
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawExam> getActiveExams(final String course) throws SQLException {

        List<RawExam> result = null;

        if (this.exams == null) {
            this.exams = new HashMap<>(5);
        } else {
            result = this.exams.get(course);
        }

        if (result == null) {
            result = RawExamLogic.queryActiveByCourse(this.cache, course);
            this.exams.put(course, result);
        }

        return result;
    }

    /**
     * Gets the exam (if it exists) for a specified course unit of a specified type.
     *
     * @param course the course
     * @param unit the unit
     * @param type the exam type
     * @return the exam; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public RawExam getActiveExamByCourseUnitType(final String course, final Integer unit, final String type)
            throws SQLException {

        final List<RawExam> all = getActiveExams(course);
        RawExam result = null;

        for (final RawExam test : all) {
            if (test.unit.equals(unit) && test.examType.equals(type)) {
                result = test;
                break;
            }
        }

        return result;
    }
}
