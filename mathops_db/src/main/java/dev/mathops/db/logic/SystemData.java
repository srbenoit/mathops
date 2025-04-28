package dev.mathops.db.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCampusCalendarLogic;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawCunitLogic;
import dev.mathops.db.old.rawlogic.RawCuobjectiveLogic;
import dev.mathops.db.old.rawlogic.RawCusectionLogic;
import dev.mathops.db.old.rawlogic.RawEtextCourseLogic;
import dev.mathops.db.old.rawlogic.RawEtextLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawHoldTypeLogic;
import dev.mathops.db.old.rawlogic.RawLessonComponentLogic;
import dev.mathops.db.old.rawlogic.RawLessonLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawPacingRulesLogic;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawlogic.RawPrereqLogic;
import dev.mathops.db.old.rawlogic.RawRemoteMpeLogic;
import dev.mathops.db.old.rawlogic.RawSurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawTestingCenterLogic;
import dev.mathops.db.old.rawlogic.RawWhichDbLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawEtext;
import dev.mathops.db.old.rawrecord.RawEtextCourse;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawHoldType;
import dev.mathops.db.old.rawrecord.RawLesson;
import dev.mathops.db.old.rawrecord.RawLessonComponent;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawPrereq;
import dev.mathops.db.old.rawrecord.RawRemoteMpe;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.db.old.rawrecord.RawTestingCenter;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.rec.MasteryExamRec;
import dev.mathops.db.rec.StandardMilestoneRec;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.rec.TermWeekRec;
import dev.mathops.db.reclogic.AssignmentLogic;
import dev.mathops.db.reclogic.MasteryExamLogic;
import dev.mathops.db.reclogic.StandardMilestoneLogic;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.db.reclogic.TermWeekLogic;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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

    /** All hold types. */
    private List<RawHoldType> holdTypes = null;

    /** All campus calendar records. */
    private List<RawCampusCalendar> campusCalendars = null;

    /** All term week records. */
    private List<TermWeekRec> termWeeks = null;

    /** All courses. */
    private List<RawCourse> courses = null;

    /** A map from term key to a list of course sections for that term. */
    private Map<TermKey, List<RawCsection>> courseSections = null;

    /** A map from term key to a list of course units for that term. */
    private Map<TermKey, List<RawCunit>> courseUnits = null;

    /** A map from term key to a list of course unit sections for that term. */
    private Map<TermKey, List<RawCusection>> courseUnitSections = null;

    /** A map from term key to a list of course unit objectives for that term. */
    private Map<TermKey, List<RawCuobjective>> courseUnitObjectives = null;

    /** A map from course ID to all assignments for that course. */
    private Map<String, List<AssignmentRec>> assignments = null;

    /** A map from course ID to all exams for that course. */
    private Map<String, List<RawExam>> exams = null;

    /** A map from course ID to all mastery exams for that course. */
    private Map<String, List<MasteryExamRec>> masteryExams = null;

    /** The list of all course milestones. */
    private List<RawMilestone> milestones = null;

    /** The list of all standards-based milestones. */
    private List<StandardMilestoneRec> standardMilestones = null;

    /** All remote placement windows. */
    private List<RawRemoteMpe> remotePlacementWindows = null;

    /** All e-texts. */
    private List<RawEtext> etexts = null;

    /** All e-text course mappings. */
    private List<RawEtextCourse> etextCourses = null;

    /** A map from term key to a list of pacing structures for that term. */
    private Map<TermKey, List<RawPacingStructure>> pacingStructures = null;

    /** A map from term key to a list of pacing rules for that term. */
    private Map<TermKey, List<RawPacingRules>> pacingRules = null;

    /** All survey questions for the active term. */
    private List<RawSurveyqa> surveyQuestions = null;

    /** Prerequisites (map from course to the list of prerequisites for that course). */
    private Map<String, List<RawPrereq>> prerequisites = null;

    /** Testing Centers. */
    private List<RawTestingCenter> testingCenters = null;

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
     * Gets the database descriptor for the database to which this object's cache is connected.
     *
     * @return the database descriptor
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
     * Gets the list of future terms.
     *
     * @return the list of future terms
     * @throws SQLException if there is an error accessing the database
     */
    public List<TermRec> getFutureTerms() throws SQLException {

        if (this.futureTerms == null) {
            this.futureTerms = TermLogic.get(this.cache).getFutureTerms(this.cache);
        }

        return this.futureTerms;
    }

    /**
     * Gets a specified term.
     *
     * @param term the term key
     * @return the term
     * @throws SQLException if there is an error accessing the database
     */
    public TermRec getTerm(final TermKey term) throws SQLException {

        // TODO: Should we cache all terms, and convert the queries for active/prior/next to lookups?

        return TermLogic.get(this.cache).query(this.cache, term);
    }

    /**
     * Inserts a new Term record.
     *
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public boolean insertTerm(final TermRec record) throws SQLException {

        final boolean ok = TermLogic.get(this.cache).insert(this.cache, record);

        if (ok) {
            final Integer index = record.activeIndex;
            if (index != null) {
                final int indexValue = index.intValue();

                if (indexValue == 0) {
                    this.activeTerm = null;
                } else if (indexValue > 0) {
                    this.futureTerms.clear();
                    if (indexValue == 1) {
                        this.nextTerm = null;
                    }
                } else {
                    if (indexValue == -1) {
                        this.priorTerm = null;
                    }
                }
            }
        }

        return ok;
    }

    /**
     * Gets all hold types.
     *
     * @return the list of hold types
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawHoldType> getHoldTypes() throws SQLException {

        if (this.holdTypes == null) {
            this.holdTypes = RawHoldTypeLogic.queryAll(this.cache);
        }

        return this.holdTypes;
    }

    /**
     * Gets a hold types.
     *
     * @param holdId the hold ID
     * @return the list of hold types
     * @throws SQLException if there is an error accessing the database
     */
    public RawHoldType getHoldType(final String holdId) throws SQLException {

        final List<RawHoldType> allHoldTypes = getHoldTypes();

        RawHoldType result = null;
        for (final RawHoldType test : allHoldTypes) {
            if (test.holdId.equals(holdId)) {
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
            this.campusCalendars = RawCampusCalendarLogic.queryAll(this.cache);
        }

        return this.campusCalendars;
    }

    /**
     * Gets all campus calendar records of a specified type.
     *
     * @param type the type
     * @return the list of campus calendar records
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCampusCalendar> getCampusCalendarsByType(final String type) throws SQLException {

        final List<RawCampusCalendar> all = getCampusCalendars();
        final List<RawCampusCalendar> result = new ArrayList<>(4);

        for (final RawCampusCalendar test : all) {
            if (test.dtDesc.equals(type)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Tests whether a date is a holiday.
     *
     * @param date the date to test
     * @return true if the given date is a holiday
     * @throws SQLException if there is an error accessing the database
     */
    public boolean isHoliday(final LocalDate date) throws SQLException {

        final List<RawCampusCalendar> holidays = getCampusCalendarsByType(RawCampusCalendar.DT_DESC_HOLIDAY);

        boolean holiday = false;

        for (final RawCampusCalendar test : holidays) {
            if (test.campusDt.equals(date)) {
                holiday = true;
                break;
            }
        }

        return holiday;
    }

    /**
     * Queries for the first day when students may work on classes.
     *
     * @return the first day, or {@code null} if no end dates are configured
     * @throws SQLException if there is an error performing the query
     */
    public LocalDate getFirstClassDay() throws SQLException {

        final List<RawCampusCalendar> allOfType = getCampusCalendarsByType(RawCampusCalendar.DT_DESC_START_DATE_1);

        final LocalDate result;

        if (allOfType.isEmpty()) {
            result = getActiveTerm().startDate;
        } else {
            result = allOfType.getFirst().campusDt;
        }

        return result;
    }

    /**
     * Queries for the last day when students may work on classes.
     *
     * @return the first day, or {@code null} if no end dates are configured
     * @throws SQLException if there is an error performing the query
     */
    public LocalDate getLastClassDay() throws SQLException {

        final List<RawCampusCalendar> allOfType = getCampusCalendarsByType(RawCampusCalendar.DT_DESC_END_DATE_1);

        final LocalDate result;

        if (allOfType.isEmpty()) {
            result = getActiveTerm().endDate;
        } else {
            result = allOfType.getFirst().campusDt;
        }

        return result;
    }

    /**
     * Gets all term week records.
     *
     * @return the list of term week records
     * @throws SQLException if there is an error accessing the database
     */
    public List<TermWeekRec> getTermWeeks() throws SQLException {

        if (this.termWeeks == null) {
            this.termWeeks = TermWeekLogic.get(this.cache).queryAll(this.cache);
            Collections.sort(this.termWeeks);
        }

        return this.termWeeks;
    }

    /**
     * Gets the list of all courses.
     *
     * @return the courses
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCourse> getCourses() throws SQLException {

        if (this.courses == null) {
            this.courses = RawCourseLogic.queryAll(this.cache);
            this.courses.sort(null);
        }

        return this.courses;
    }

    /**
     * Gets the single course.
     *
     * @param course the course ID
     * @return the course; null if not found
     * @throws SQLException if there is an error accessing the database
     */
    public RawCourse getCourse(final String course) throws SQLException {

        final List<RawCourse> all = getCourses();
        RawCourse result = null;

        for (final RawCourse test : all) {
            if (test.course.equals(course)) {
                result = test;
            }
        }

        return result;
    }

    /**
     * Tests whether a course requires an e-text.
     *
     * @param course the ID of the course to retrieve
     * @return TRUE if the course was found an is marked as being a tutorial; FALSE if the course was found as is not
     *         marked as a tutorial; null if the course was not found
     * @throws SQLException if there is an error performing the query
     */
    public Boolean isETextRequired(final String course) throws SQLException {

        final RawCourse rec = getCourse(course);

        if (rec == null) {
            return null;
        } else {
            final boolean requiresEText = "Y".equals(rec.requireEtext);
            return Boolean.valueOf(requiresEText);
        }
    }

    /**
     * Tests whether a course is a tutorial.
     *
     * @param course the ID of the course to retrieve
     * @return TRUE if the course was found an is marked as being a tutorial; FALSE if the course was found and is not
     *         marked as a tutorial; null if the course was not found
     * @throws SQLException if there is an error performing the query
     */
    public Boolean isCourseTutorial(final String course) throws SQLException {

        final RawCourse rec = getCourse(course);

        if (rec == null) {
            return null;
        } else {
            final boolean isTutorial = "Y".equals(rec.isTutorial);
            return Boolean.valueOf(isTutorial);
        }
    }

    /**
     * Gets the label for a course.
     *
     * @param course the ID of the course to query
     * @return the label; null if the course was not found or had no label
     * @throws SQLException if there is an error performing the query
     */
    public String getCourseLabel(final String course) throws SQLException {

        final RawCourse rec = getCourse(course);

        return rec == null ? null : rec.courseLabel;
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
     * Gets all course sections for a single course in a single term.
     *
     * @param course the course ID
     * @param term   the term key
     * @return the list of course sections
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCsection> getCourseSectionsByCourse(final String course, final TermKey term) throws SQLException {

        final List<RawCsection> all = getCourseSections(term);
        final List<RawCsection> result = new ArrayList<>(10);

        for (final RawCsection test : all) {
            if (test.course.equals(course)) {
                result.add(test);
                break;
            }
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
     * Attempts to query the course section object for a registration.
     *
     * @param reg the registration
     * @return the course section record
     * @throws SQLException if there is an error accessing the database or the course section cannot be found
     */
    public RawCsection getCourseSection(final RawStcourse reg) throws SQLException {

        RawCsection csection = null;

        if ("Y".equals(reg.iInProgress)) {
            csection = getCourseSection(reg.course, reg.sect, reg.iTermKey);
        }

        if (csection == null) {
            csection = getCourseSection(reg.course, reg.sect, reg.termKey);
        }

        if (csection == null) {
            final List<RawCsection> all = getCourseSections(reg.termKey);
            final boolean isDistance = reg.sect.startsWith("8") || reg.sect.startsWith("4");

            for (final RawCsection test : all) {
                final boolean testIsDistance = test.sect.startsWith("8") || test.sect.startsWith("4");
                if (isDistance == testIsDistance && test.course.equals(reg.course)) {
                    csection = test;
                    break;
                }
            }
        }

        if (csection == null) {
            final String msg = SimpleBuilder.concat("Failed to query course section for ", reg.course, " sect ",
                    reg.sect, " in ", reg.termKey.shortString);
            throw new SQLException(msg);
        }

        return csection;
    }

    /**
     * Retrieves the exam delete date for a particular course section.
     *
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the exam delete date; {@code null} if the exam delete date was null or if no course exists with the
     *         specified ID
     * @throws SQLException if there is an error accessing the database
     */
    public LocalDate getExamDeleteDate(final String course, final String sect, final TermKey termKey)
            throws SQLException {

        final RawCsection rec = getCourseSection(course, sect, termKey);

        return rec == null ? null : rec.examDeleteDt;
    }

    /**
     * Retrieves the instruction type for a particular course section.
     *
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the instruction type; {@code null} on any error or if no course exists with the specified ID
     * @throws SQLException if there is an error accessing the database
     */
    public String getInstructionType(final String course, final String sect, final TermKey termKey)
            throws SQLException {

        final RawCsection rec = getCourseSection(course, sect, termKey);

        return rec == null ? null : rec.instrnType;
    }

    /**
     * Retrieves the rule set ID for a particular course section.
     *
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the rule set ID; {@code null} on any error or if no course exists with the specified ID
     * @throws SQLException if there is an error accessing the database
     */
    public String getRuleSetId(final String course, final String sect, final TermKey termKey) throws SQLException {

        final RawCsection rec = getCourseSection(course, sect, termKey);

        return rec == null ? null : rec.pacingStructure;
    }

    /**
     * Gets all course units for a single term.
     *
     * @param term the term key
     * @return the list of course units
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawCunit> getCourseUnits(final TermKey term) throws SQLException {

        List<RawCunit> result = null;

        if (this.courseUnits == null) {
            this.courseUnits = new HashMap<>(4);
        } else {
            result = this.courseUnits.get(term);
        }

        if (result == null) {
            result = RawCunitLogic.queryByTerm(this.cache, term);
            this.courseUnits.put(term, result);
        }

        return result;
    }

    /**
     * Gets all course units in a single course for a single term.
     *
     * @param course the course ID
     * @param term   the term key
     * @return the list of course sections
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCunit> getCourseUnits(final String course, final TermKey term) throws SQLException {

        final List<RawCunit> all = getCourseUnits(term);
        final List<RawCunit> result = new ArrayList<>(8);

        for (final RawCunit test : all) {
            if (test.course.equals(course)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets a particular course units in a specified term.
     *
     * @param course the course
     * @param unit   the unit
     * @param term   the term key
     * @return the course unit
     * @throws SQLException if there is an error accessing the database
     */
    public RawCunit getCourseUnit(final String course, final Integer unit, final TermKey term) throws SQLException {

        final List<RawCunit> termSections = getCourseUnits(term);
        RawCunit result = null;

        for (final RawCunit test : termSections) {
            if (test.course.equals(course) && test.unit.equals(unit)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all course sections for a single term.
     *
     * @param term the term key
     * @return the list of course unit sections
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawCusection> getCourseUnitSections(final TermKey term) throws SQLException {

        List<RawCusection> result = null;

        if (this.courseUnitSections == null) {
            this.courseUnitSections = new HashMap<>(4);
        } else {
            result = this.courseUnitSections.get(term);
        }

        if (result == null) {
            result = RawCusectionLogic.queryByTerm(this.cache, term);
            this.courseUnitSections.put(term, result);
        }

        return result;
    }

    /**
     * Gets a particular course unit sections for a specified course and section in a specified term.
     *
     * @param course the course
     * @param sect   the section
     * @param term   the term key
     * @return the list of course unit sections
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCusection> getCourseUnitSections(final String course, final String sect, final TermKey term)
            throws SQLException {

        final List<RawCusection> all = getCourseUnitSections(term);
        final List<RawCusection> result = new ArrayList<>(10);

        for (final RawCusection test : all) {
            if (test.course.equals(course) && test.sect.equals(sect)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets a particular course unit section in a specified term.
     *
     * @param course the course
     * @param unit   the unit
     * @param sect   the section
     * @param term   the term key
     * @return the list of course sections
     * @throws SQLException if there is an error accessing the database
     */
    public RawCusection getCourseUnitSection(final String course, final String sect, final Integer unit,
                                             final TermKey term) throws SQLException {

        final List<RawCusection> termSections = getCourseUnitSections(term);
        RawCusection result = null;

        for (final RawCusection test : termSections) {
            if (test.course.equals(course) && test.unit.equals(unit) && test.sect.equals(sect)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all course unit objectives for a single term.
     *
     * @param term the term key
     * @return the list of course unit objectives
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawCuobjective> getCourseUnitObjectives(final TermKey term) throws SQLException {

        List<RawCuobjective> result = null;

        if (this.courseUnitObjectives == null) {
            this.courseUnitObjectives = new HashMap<>(4);
        } else {
            result = this.courseUnitObjectives.get(term);
        }

        if (result == null) {
            result = RawCuobjectiveLogic.queryByTerm(this.cache, term);
            this.courseUnitObjectives.put(term, result);
        }

        return result;
    }

    /**
     * Gets the course unit objectives in a specified course and unit in a specified term.
     *
     * @param course the course
     * @param unit   the unit
     * @param term   the term key
     * @return the list of course sections
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawCuobjective> getCourseUnitObjectives(final String course, final Integer unit, final TermKey term)
            throws SQLException {

        final List<RawCuobjective> termObjectives = getCourseUnitObjectives(term);
        final List<RawCuobjective> result = new ArrayList<>(10);

        for (final RawCuobjective test : termObjectives) {
            if (test.course.equals(course) && test.unit.equals(unit)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets a particular course unit objective in a specified term.
     *
     * @param course    the course
     * @param unit      the unit
     * @param objective the objective
     * @param term      the term key
     * @return the list of course sections
     * @throws SQLException if there is an error accessing the database
     */
    public RawCuobjective getCourseUnitObjective(final String course, final Integer unit, final Integer objective,
                                                 final TermKey term) throws SQLException {

        final List<RawCuobjective> termObjectives = getCourseUnitObjectives(term);
        RawCuobjective result = null;

        for (final RawCuobjective test : termObjectives) {
            if (test.course.equals(course) && test.unit.equals(unit) && test.objective.equals(objective)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets a single lesson.
     *
     * @param lessonId the ID of the lesson to retrieve
     * @return the lesson; null if not found
     */
    public RawLesson getLesson(final String lessonId) {

        return RawLessonLogic.query(lessonId);
    }

    /**
     * Gets all lesson components for a single lesson.
     *
     * @param lessonId the ID of the lesson whose components to retrieve
     * @return the lesson components
     */
    public List<RawLessonComponent> getLessonComponentsByLesson(final String lessonId) {

        return RawLessonComponentLogic.queryByLesson(lessonId);
    }

    /**
     * Gets a list of all assignments for a course.
     *
     * @param course the course
     * @return the list of assignments
     * @throws SQLException if there is an error accessing the database
     */
    private List<AssignmentRec> getActiveAssignmentsByCourse(final String course) throws SQLException {

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
     * Gets a list of all assignments of a specified type for a course and type.
     *
     * @param course the course
     * @param type   the type of assignment to retrieve
     * @return the list of assignments
     * @throws SQLException if there is an error accessing the database
     */
    public List<AssignmentRec> getActiveAssignmentsByCourseType(final String course, final String type)
            throws SQLException {

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
     * Gets a list of all assignments of a specified type for a course, unit, and type.
     *
     * @param course the course
     * @param unit   the unit
     * @param types  the type(s) of exams to retrieve (null or empty to retrieve all types)
     * @return the list of assignments
     * @throws SQLException if there is an error accessing the database
     */
    public List<AssignmentRec> getActiveAssignmentsByCourseUnitType(final String course, final Integer unit,
                                                                    final String... types) throws SQLException {

        final List<AssignmentRec> all = getActiveAssignmentsByCourse(course);
        final int count = all.size();

        final List<AssignmentRec> match = new ArrayList<>(count);

        for (final AssignmentRec test : all) {
            if (test.unit.equals(unit)) {

                if (types == null || types.length == 0) {
                    match.add(test);
                } else {
                    for (final String type : types) {
                        if (test.assignmentType.equals(type)) {
                            match.add(test);
                            break;
                        }
                    }
                }
            }
        }

        return match;
    }

    /**
     * Queries for all active (having null pull date) assignments in a course unit objective.
     *
     * @param course    the course for which to query
     * @param unit      the unit for which to query
     * @param objective the objective for which to query
     * @param types     the type(s) of exams to retrieve (null or empty to retrieve all types)
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public List<AssignmentRec> getActiveAssignmentsByCourseUnitObjectiveType(final String course, final Integer unit,
                                                                             final Integer objective,
                                                                             final String... types)
            throws SQLException {

        final List<AssignmentRec> all = getActiveAssignmentsByCourse(course);
        final int count = all.size();

        final List<AssignmentRec> match = new ArrayList<>(count);

        for (final AssignmentRec test : all) {
            if (test.unit.equals(unit) && test.objective.equals(objective)) {

                if (types == null || types.length == 0) {
                    match.add(test);
                } else {
                    for (final String type : types) {
                        if (test.assignmentType.equals(type)) {
                            match.add(test);
                            break;
                        }
                    }
                }
            }
        }

        return match;
    }

    /**
     * Gets the unique assignment of a specified type for a course unit objective.
     *
     * @param course    the course
     * @param unit      the unit
     * @param objective the objective
     * @param type      the type of assignment to retrieve
     * @return the list of assignments
     * @throws SQLException if there is an error accessing the database
     */
    public AssignmentRec getActiveAssignment(final String course, final Integer unit, final Integer objective,
                                             final String type) throws SQLException {

        final List<AssignmentRec> all = getActiveAssignmentsByCourse(course);

        AssignmentRec match = null;

        for (final AssignmentRec test : all) {
            if (test.unit.equals(unit) && test.objective.equals(objective) && test.assignmentType.equals(type)) {
                match = test;
                break;
            }
        }

        return match;
    }

    /**
     * Gets the unique assignment with a specified ID.
     *
     * @param assignmentId the assignment ID
     * @return the list of assignments
     * @throws SQLException if there is an error accessing the database
     */
    public AssignmentRec getActiveAssignment(final String assignmentId) throws SQLException {

        AssignmentRec result = null;

        if (this.assignments == null) {
            result = AssignmentLogic.get(this.cache).query(this.cache, assignmentId);
        } else {
            outer:
            for (final List<AssignmentRec> list : this.assignments.values()) {
                for (final AssignmentRec test : list) {
                    if (test.assignmentId.equals(assignmentId)) {
                        result = test;
                        break outer;
                    }
                }
            }

            if (result == null) {
                result = AssignmentLogic.get(this.cache).query(this.cache, assignmentId);
            }
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
     * Gets a single active exam by its version
     *
     * @param version the version
     * @return the matching exam; null if not found
     * @throws SQLException if there is an error accessing the database
     */
    public RawExam getActiveExam(final String version) throws SQLException {

        RawExam result = null;

        if (this.exams == null) {
            result = RawExamLogic.query(this.cache, version);
        } else {
            outer:
            for (final List<RawExam> list : this.exams.values()) {
                for (final RawExam test : list) {
                    if (test.version.equals(version)) {
                        result = test;
                        break outer;
                    }
                }
            }
            if (result == null) {
                result = RawExamLogic.query(this.cache, version);
            }
        }

        return result;
    }

    /**
     * Gets all exams for a specified course unit.
     *
     * @param course the course
     * @param unit   the unit
     * @return the list of exams
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawExam> getActiveExamByCourseUnit(final String course, final Integer unit) throws SQLException {

        final List<RawExam> all = getActiveExams(course);
        final List<RawExam> result = new ArrayList<>(10);

        for (final RawExam test : all) {
            if (test.unit.equals(unit)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets the exam (if it exists) for a specified course unit of a specified type.
     *
     * @param course the course
     * @param unit   the unit
     * @param type   the exam type
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
     * Gets a list of all mastery exams for a course.
     *
     * @param course    the course
     * @param unit      the unit
     * @param objective the objective
     * @return the list of mastery exams
     * @throws SQLException if there is an error accessing the database
     */
    public List<MasteryExamRec> getActiveMasteryExamsByCourseUnitObjective(final String course, final Integer unit,
                                                                           final Integer objective)
            throws SQLException {

        final List<MasteryExamRec> all = getActiveMasteryExamsByCourse(course);
        final List<MasteryExamRec> result = new ArrayList<>(5);

        for (final MasteryExamRec test : all) {
            if (unit.equals(test.unit) && objective.equals(test.objective)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets the list of all course milestones.
     *
     * @return the course milestones
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawMilestone> getMilestones() throws SQLException {

        if (this.milestones == null) {
            this.milestones = RawMilestoneLogic.queryAll(this.cache);
            Collections.sort(this.milestones);
        }

        return this.milestones;
    }

    /**
     * Gets the list of all course milestones in a specified term.
     *
     * @param term the term whose milestones to retrieve
     * @return the course milestones
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawMilestone> getMilestones(final TermKey term) throws SQLException {

        final List<RawMilestone> all = getMilestones();
        final int size = all.size();
        final List<RawMilestone> result = new ArrayList<>(size);

        for (final RawMilestone test : all) {
            if (test.termKey.equals(term)) {
                result.add(test);
            }
        }

        return result;
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
     * Gets the list of all standard milestones in the current term for a specified pace track and pace.
     *
     * @param paceTrack the pace track
     * @param pace      the pace
     * @return the standard milestones
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardMilestoneRec> getStandardMilestonesForPaceTrack(final String paceTrack,
                                                                        final Integer pace) throws SQLException {

        final List<StandardMilestoneRec> all = getStandardMilestones();
        final List<StandardMilestoneRec> result = new ArrayList<>(20);

        for (final StandardMilestoneRec test : all) {
            if (test.paceTrack.equals(paceTrack) && test.pace.equals(pace)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets the list of all standard milestones in the current term for a specified pace track, pace and pace index.
     *
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param paceIndex the pace index
     * @return the standard milestones
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardMilestoneRec> getStandardMilestonesForPaceTrackIndex(final String paceTrack, final Integer pace,
                                                                             final Integer paceIndex)
            throws SQLException {

        final List<StandardMilestoneRec> all = getStandardMilestones();
        final List<StandardMilestoneRec> result = new ArrayList<>(20);

        for (final StandardMilestoneRec test : all) {
            if (test.paceTrack.equals(paceTrack) && test.pace.equals(pace) && test.paceIndex.equals(paceIndex)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets all remote placement windows.
     *
     * @return the list of remote placement windows
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawRemoteMpe> getRemotePlacementWindows() throws SQLException {

        if (this.remotePlacementWindows == null) {
            this.remotePlacementWindows = RawRemoteMpeLogic.queryAll(this.cache);
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
            this.etexts = RawEtextLogic.queryAll(this.cache);
        }

        return this.etexts;
    }

    /**
     * Gets a single e-text.
     *
     * @param eTextId the e-text ID
     * @return the e-text
     * @throws SQLException if there is an error accessing the database
     */
    public RawEtext getEText(final String eTextId) throws SQLException {

        final List<RawEtext> all = getETexts();
        RawEtext result = null;

        for (final RawEtext test : all) {
            if (test.etextId.equals(eTextId)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all e-texts.
     *
     * @return the list of e-texts
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawEtextCourse> getETextCourses() throws SQLException {

        if (this.etextCourses == null) {
            this.etextCourses = RawEtextCourseLogic.queryAll(this.cache);
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
     * Gets all e-texts for a course.
     *
     * @param courseId the course ID
     * @return the list of e-texts
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawEtextCourse> getETextCoursesByCourseId(final String courseId) throws SQLException {

        final List<RawEtextCourse> all = getETextCourses();
        final List<RawEtextCourse> match = new ArrayList<>(5);

        for (final RawEtextCourse test : all) {
            if (test.course.equals(courseId)) {
                match.add(test);
            }
        }

        return match;
    }

    /**
     * Gets all pacing structures for a single term.
     *
     * @param term the term key
     * @return the list of pacing structures
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawPacingStructure> getPacingStructures(final TermKey term) throws SQLException {

        List<RawPacingStructure> result = null;

        if (this.pacingStructures == null) {
            this.pacingStructures = new HashMap<>(4);
        } else {
            result = this.pacingStructures.get(term);
        }

        if (result == null) {
            result = RawPacingStructureLogic.queryByTerm(this.cache, term);
            this.pacingStructures.put(term, result);
        }

        return result;
    }

    /**
     * Gets a single pacing structure for a single term.
     *
     * @param pacingStructureId the ID of the pacing structure to retrieve
     * @param term              the term key
     * @return the  pacing structure; null if not found
     * @throws SQLException if there is an error accessing the database
     */
    public RawPacingStructure getPacingStructure(final String pacingStructureId, final TermKey term)
            throws SQLException {

        final List<RawPacingStructure> all = getPacingStructures(term);
        RawPacingStructure result = null;

        for (final RawPacingStructure test : all) {
            if (test.pacingStructure.equals(pacingStructureId)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all pacing rules for a single term.
     *
     * @param term the term key
     * @return the list of pacing rules
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawPacingRules> getPacingRules(final TermKey term) throws SQLException {

        List<RawPacingRules> result = null;

        if (this.pacingRules == null) {
            this.pacingRules = new HashMap<>(4);
        } else {
            result = this.pacingRules.get(term);
        }

        if (result == null) {
            result = RawPacingRulesLogic.queryByTerm(this.cache, term);
            this.pacingRules.put(term, result);
        }

        return result;
    }

    /**
     * Gets all pacing rules in a specified pacing structure for a single term.
     *
     * @param term            the term key
     * @param pacingStructure the pacing structure
     * @return the list of pacing rules
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawPacingRules> getPacingRulesByTermAndPacing(final TermKey term, final String pacingStructure)
            throws SQLException {

        final List<RawPacingRules> all = getPacingRules(term);
        final List<RawPacingRules> result = new ArrayList<>(10);

        for (final RawPacingRules test : all) {
            if (test.pacingStructure.equals(pacingStructure)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Tests whether the pacing rules for a term and pacing structure require some prerequisite condition before
     * allowing some activity.
     *
     * @param term            the term key
     * @param pacingStructure the pacing structure
     * @param activity        the activity
     * @param prerequisite    the condition
     * @return true if the condition must be met before the activity can take place
     * @throws SQLException if there is an error accessing the database
     */
    public boolean isRequiredByPacingRules(final TermKey term, final String pacingStructure,
                                           final String activity, final String prerequisite)
            throws SQLException {

        final List<RawPacingRules> all = getPacingRulesByTermAndPacing(term, pacingStructure);
        boolean result = false;

        for (final RawPacingRules test : all) {
            if (test.activityType.equals(activity) && test.requirement.equals(prerequisite)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all question records for the active term.
     *
     * @return the list of survey records
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawSurveyqa> getSurveyQuestions() throws SQLException {

        if (this.surveyQuestions == null) {
            final TermRec active = getActiveTerm();
            if (active != null) {
                this.surveyQuestions = RawSurveyqaLogic.queryByTerm(this.cache, active.term);
            }
        }

        return this.surveyQuestions;
    }

    /**
     * Gets all question records for a single survey version.
     *
     * @param version the survey version
     * @return the list of survey records
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawSurveyqa> getSurveyQuestions(final String version) throws SQLException {

        final List<RawSurveyqa> all = getSurveyQuestions();
        final List<RawSurveyqa> result = new ArrayList<>(10);

        for (final RawSurveyqa test : all) {
            if (test.version.equals(version)) {
                result.add(test);
            }
        }

        return result;
    }

    /**
     * Gets all question records for a single item on a survey version.
     *
     * @param version the survey version
     * @param number  the item number
     * @return the list of survey records
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawSurveyqa> getSurveyQuestions(final String version, final Integer number) throws SQLException {

        final List<RawSurveyqa> all = getSurveyQuestions();
        final List<RawSurveyqa> result = new ArrayList<>(10);

        for (final RawSurveyqa test : all) {
            if (test.version.equals(version) && test.surveyNbr.equals(number)) {
                result.add(test);
            }
        }

        return result;
    }

//    /**
//     * Gets all questions for a survey, with one record per question (that record will have answer-related fields from
//     * an arbitrary record). Results are ordered by question number.
//     *
//     * @param theVersion the profile ID whose questions to retrieve
//     * @return the list of models that matched the criteria, a zero-length array if none matched
//     * @throws SQLException if there is an error accessing the database
//     */
//    public List<RawSurveyqa> getUniqueQuestionsByVersion(final String theVersion) throws SQLException {
//
//        final List<RawSurveyqa> all = getSurveyQuestions(theVersion);
//
//        final Map<Integer, RawSurveyqa> map = new TreeMap<>();
//        for (final RawSurveyqa record : all) {
//            map.put(record.surveyNbr, record);
//        }
//
//        final Collection<RawSurveyqa> values = map.values();
//        return new ArrayList<>(values);
//    }

    /**
     * Gets the list of courses that can satisfy the prerequisites of a specified course.
     *
     * @param course the course
     * @return the list of courses
     * @throws SQLException if there is an error performing the query
     */
    public List<String> getPrerequisitesByCourse(final String course) throws SQLException {

        if (this.prerequisites == null) {
            this.prerequisites = new HashMap<>(5);
        }

        List<RawPrereq> records = this.prerequisites.get(course);

        if (records == null) {
            final TermRec active = getActiveTerm();
            if (active == null) {
                records = new ArrayList<>(0);
            } else {
                records = RawPrereqLogic.queryByTermAndCourse(this.cache, active.term, course);
            }
            this.prerequisites.put(course, records);
        }

        final int count = records.size();
        final List<String> result = new ArrayList<>(count);
        for (final RawPrereq rec : records) {
            result.add(rec.prerequisite);
        }

        return result;
    }

    /**
     * Gets all client PC records.
     *
     * @return the list of client PCs
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawClientPc> getClientPcs() throws SQLException {

        return RawClientPcLogic.queryAll(this.cache);
    }

    /**
     * Gets all client PC records in a specified testing center.
     *
     * @param testingCenterId the testing center ID
     * @return the list of client PCs
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawClientPc> getClientPcsByTestingCenter(final String testingCenterId) throws SQLException {

        return RawClientPcLogic.queryByTestingCenter(this.cache, testingCenterId);
    }

    /**
     * Gets a specific client PC.
     *
     * @param computerId the ID of the PC to retrieve
     * @return the matching client PC; {@code null} if none found
     * @throws SQLException if there is an error accessing the database
     */
    public RawClientPc getClientPc(final String computerId) throws SQLException {

        return RawClientPcLogic.query(this.cache, computerId);
    }

    /**
     * Gets all testing center records.
     *
     * @return the list of testing centers
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawTestingCenter> getTestingCenters() throws SQLException {

        if (this.testingCenters == null) {
            this.testingCenters = RawTestingCenterLogic.queryAll(this.cache);
        }

        return this.testingCenters;
    }

    /**
     * Gets a specific client PC.
     *
     * @param testingCenterId the ID of the testing center to retrieve
     * @return the matching testing center; {@code null} if none found
     * @throws SQLException if there is an error accessing the database
     */
    public RawTestingCenter getTestingCenter(final String testingCenterId) throws SQLException {

        final List<RawTestingCenter> all = getTestingCenters();

        RawTestingCenter result = null;
        for (final RawTestingCenter test : all) {
            if (test.testingCenterId.equals(testingCenterId)) {
                result = test;
                break;
            }
        }

        return result;
    }
}
