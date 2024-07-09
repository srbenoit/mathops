package dev.mathops.app.checkin;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.logic.StandardsMasteryLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawPacingRulesLogic;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.reclogic.StandardMilestoneLogic;
import dev.mathops.db.old.reclogic.StudentStandardMilestoneLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The portion of check-in logic devoted to determining the set of available course exams.
 *
 * <p>
 * The input to this logic is the set of registrations the student has.  The output is a map from the (NEW) course ID
 * for each course to a {@code StudentCheckInInfoCourseExams} object with the status of the exams for that course, which
 * is stored in a {@code StudentCheckInInfo} object.
 */
final class LogicCheckInCourseExams {

    /** A commonly used constant. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A commonly used constant. */
    private static final Integer TWO = Integer.valueOf(2);

    /** A commonly used constant. */
    private static final Integer THREE = Integer.valueOf(3);

    /** A commonly used constant. */
    private static final Integer FOUR = Integer.valueOf(4);

    /** A commonly used constant. */
    private static final Integer FIVE = Integer.valueOf(5);

    /** Infinity, in the "1, 2, 3, many" counting system. */
    private static final int INFINITY = 90;

    /** A commonly used string. */
    private static final String UNAVAILABLE = "Unavailable";

    /** A commonly used string. */
    private static final String NOT_REGISTERED = "Not Registered";

    /** A commonly used string. */
    private static final String ALL_ATTEMPTS_USED = "All Attempts Used";

    /** A commonly used string. */
    private static final String MUST_REPASS_REVIEW = "Must Repass Review";

    /** A commonly used string. */
    private static final String MUST_PASS_REVIEW = "Must Pass Review";

    /** A commonly used string. */
    private static final String PAST_UNIT_DEADLINE = "Past Unit Deadline";

    /** A commonly used string. */
    private static final String PAST_DEADLINE = "Past Deadline";

    /** A commonly used string. */
    private static final String OPENS = "Opens ";

    /** The current day number. */
    private final LocalDate today;

    /** The active term. */
    private final TermRec activeTerm;

    /** Data on the check-in attempt. */
    private final DataCheckInAttempt checkInData;

    /**
     * Constructs a new {@code CheckInLogicCourseExams}.
     *
     * @param theToday       the date/time to consider "today"
     * @param theActiveTerm  the active term
     * @param theCheckInData data on the check-in attempt
     */
    LogicCheckInCourseExams(final LocalDate theToday, final TermRec theActiveTerm,
                            final DataCheckInAttempt theCheckInData) {

        this.today = theToday;
        this.activeTerm = theActiveTerm;
        this.checkInData = theCheckInData;
    }

    /**
     * Determines exam status for all courses.
     *
     * @param cache              the data cache
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    void execute(final Cache cache, final boolean enforceEligibility) throws SQLException {

        if (isSpecial()) {
            setSpecialStudentStatus();
        } else {
            final String stuId = this.checkInData.studentData.stuId;
            final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(cache, stuId, this.activeTerm.term);

            determineAvailableChallengeExams(cache, regs, enforceEligibility);
            determineAvailableCourseExams(cache, regs, enforceEligibility);
        }
    }

    /**
     * Tests whether the student belongs to one of the "special" types for whom all exams are available all the time.
     * This includes members of the "M384", "ADMIN", and "STEVE" categories
     */
    private boolean isSpecial() {

        final List<String> specialTypes = this.checkInData.studentData.specialTypes;

        boolean isSpecial = false;

        for (final String type : specialTypes) {
            if ("M384".equals(type) || "ADMIN".equals(type) || "STEVE".equals(type)) {
                isSpecial = true;
                break;
            }
        }

        return isSpecial;
    }

    /**
     * Sets exam status for a "special" student for whom all course exams, but no challenge exams are available.
     */
    private void setSpecialStudentStatus() {

        for (final CourseNumbers numbers : CourseNumbers.COURSES) {
            final String newCourseId = numbers.newCourseId();
            final String oldCourseId = numbers.oldCourseId();
            final String challengeId = numbers.challengeId();

            final DataCourseExams data = this.checkInData.getCourseExams(numbers);
            data.registeredInOld = true;
            data.registeredInNew = true;
            data.unit1Exam = DataExamStatus.available(oldCourseId, 1);
            data.unit2Exam = DataExamStatus.available(oldCourseId, 2);
            data.unit3Exam = DataExamStatus.available(oldCourseId, 3);
            data.unit4Exam = DataExamStatus.available(oldCourseId, 4);
            data.finalExam = DataExamStatus.available(oldCourseId, 5);
            data.challengeExam = DataExamStatus.unavailable(challengeId, 0, UNAVAILABLE);
            data.masteryExam = DataExamStatus.available(newCourseId, 0);

            // FIXME: Get this from data
            data.masteryExam.numStandardsAvailable = 24;
        }
    }

    /**
     * Determines the list of challenge exams the student is eligible to take. A student can take a challenge exam if
     * <ul>
     * <li>they are not currently registered in the course (even if open status is "forfeit")
     * <li>they do not have an open Incomplete in the course from a prior term
     * <li>they do not have placement (OT) credit for the course already
     * <li>they have not already taken and completed the course
     * <li>they have not already challenged the course with the Challenge exam (earlier attempts on
     * the combined Math Challenge Exam would not affect eligibility)
     * <li>they have met the prerequisites for the course
     * <li>they do not have a fatal hold other than hold 30 (tested before this class is used)
     * </ul>
     *
     * @param cache              the data cache
     * @param activeRegs         the student's current-term registrations
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    private void determineAvailableChallengeExams(final Cache cache, final Iterable<RawStcourse> activeRegs,
                                                  final boolean enforceEligibility) throws SQLException {

        final String stuId = this.checkInData.studentData.stuId;

        final PrerequisiteLogic prereqLogic = new PrerequisiteLogic(cache, stuId);

        for (final CourseNumbers numbers : CourseNumbers.COURSES) {

            final String oldCourseId = numbers.oldCourseId();
            final String challengeId = numbers.challengeId();

            final DataCourseExams data = this.checkInData.getCourseExams(numbers);
            final DataExamStatus status = DataExamStatus.available(challengeId, 0);
            data.challengeExam = status;

            for (final RawStcourse test : activeRegs) {
                if (numbers.isMatching(test.course)) {
                    if ("OT".equals(test.instrnType)) {
                        indicateExamUnavailable(status, "Has Placement Credit", enforceEligibility);
                    } else {
                        indicateExamUnavailable(status, "Currently Enrolled", enforceEligibility);
                    }
                    break;
                }
            }

            final List<RawStchallenge> att = RawStchallengeLogic.queryByStudent(cache, stuId);

            for (final RawStchallenge test : att) {
                if (numbers.isMatching(test.course) && challengeId.equals(test.version)) {
                    indicateExamUnavailable(status, "Attempt used", enforceEligibility);
                    break;
                }
            }

            if (!prereqLogic.hasSatisfiedPrereqsFor(oldCourseId)) {
                indicateExamUnavailable(status, "Needs Prereq.", enforceEligibility);
            }
        }
    }

    /**
     * Determines the list of course exams the student is eligible to take, based on their registrations, licensed
     * status, placement history, prerequisites, incomplete status, and so on. The list of exams in those courses is
     * compiled into the {@code mAvailableExams} field in the {@code StudentCheckInInfo} object, with the availability
     * of each exam noted.
     *
     * @param cache              the data cache
     * @param activeRegs         the student's current-term registrations
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    private void determineAvailableCourseExams(final Cache cache, final Collection<RawStcourse> activeRegs,
                                               final boolean enforceEligibility) throws SQLException {

        final RawStterm studentTerm = this.checkInData.studentData.studentTerm;
        if (studentTerm == null) {
            // Set all exams as "unavailable" - student is not registered, and changes to "enforceEligibility" cannot
            // make such exams available.
            for (final CourseNumbers numbers : CourseNumbers.COURSES) {
                final DataCourseExams data = this.checkInData.getCourseExams(numbers);
                makeCourseUnavailable(data, numbers, NOT_REGISTERED);
                data.registeredInOld = false;
                data.registeredInNew = false;
            }

            // Student might not be registered this term, but still could have an Incomplete from a prior term.
            for (final RawStcourse reg : activeRegs) {
                if ("Y".equals(reg.iInProgress) && "N".equals(reg.iCounted)) {
                    CourseNumbers numbers = null;
                    for (final CourseNumbers test : CourseNumbers.COURSES) {
                        if (test.oldCourseId().equals(reg.course) || test.newCourseId().equals(reg.course)) {
                            numbers = test;
                            break;
                        }
                    }

                    if (numbers != null) {
                        final DataCourseExams data = this.checkInData.getCourseExams(numbers);

                        boolean eligible = true;
                        if (enforceEligibility) {
                            final LocalDate today = LocalDate.now();
                            if (reg.iDeadlineDt != null && reg.iDeadlineDt.isBefore(today)) {
                                makeCourseUnavailable(data, numbers, PAST_DEADLINE);
                                eligible = false;
                            }
                        }

                        if (eligible) {
                            final String newCourseId = numbers.newCourseId();
                            final String oldCourseId = numbers.oldCourseId();

                            data.registeredInOld = reg.course.equals(oldCourseId);
                            data.registeredInNew = reg.course.equals(newCourseId);

                            if (reg.openStatus == null) {
                                // "enforceEligibility" cannot make exams available in unopened courses
                                makeCourseUnavailable(data, numbers, "Not Yet Open");
                            } else if ("G".equals(reg.openStatus)) {
                                // "enforceEligibility" cannot make exams available in forfeit courses
                                makeCourseUnavailable(data, numbers, "Course is Forfeit");
                            } else if ("Y".equals(reg.openStatus)) {

                                final Map<RawStcourse, SectionData> sections = new HashMap<>(1);
                                loadSectionData(cache, activeRegs, sections);

                                final SectionData sectData = sections.get(reg);

                                testSingleIncomplete(cache, data, reg, 1, "A", sectData, enforceEligibility);
                            } else {
                                // "enforceEligibility" cannot make exams available in course that is not open
                                makeCourseUnavailable(data, numbers, "Course Not Open");
                            }
                        }
                    }
                }
            }
        } else {
            final int pace = studentTerm.pace.intValue();
            final String track = studentTerm.paceTrack;

            final int numRegs = activeRegs.size();
            final Map<RawStcourse, SectionData> sections = new HashMap<>(numRegs);
            loadSectionData(cache, activeRegs, sections);

            for (final CourseNumbers numbers : CourseNumbers.COURSES) {
                final String newCourseId = numbers.newCourseId();
                final String oldCourseId = numbers.oldCourseId();

                final DataCourseExams data = this.checkInData.getCourseExams(numbers);

                RawStcourse reg = null;
                for (final RawStcourse test : activeRegs) {
                    if (test.course.equals(oldCourseId) || test.course.equals(newCourseId)) {
                        reg = test;
                        break;
                    }
                }

                if (reg == null) {
                    makeCourseUnavailable(data, numbers, NOT_REGISTERED);
                    data.registeredInOld = false;
                    data.registeredInNew = false;
                } else {
                    data.registeredInOld = reg.course.equals(oldCourseId);
                    data.registeredInNew = reg.course.equals(newCourseId);

                    if (reg.openStatus == null) {
                        // "enforceEligibility" cannot make exams available in unopened courses
                        makeCourseUnavailable(data, numbers, "Not Yet Open");
                    } else if ("G".equals(reg.openStatus)) {
                        // "enforceEligibility" cannot make exams available in forfeit courses
                        makeCourseUnavailable(data, numbers, "Course is Forfeit");
                    } else if ("Y".equals(reg.openStatus)) {
                        final SectionData sectData = sections.get(reg);

                        if (sectData == null) {
                            // "enforceEligibility" cannot make exams available with bad configuration data
                            makeCourseUnavailable(data, numbers, "No Sect Data");
                        } else if ("OT".equals(sectData.cSection.instrnType)) {
                            // "enforceEligibility" cannot allow testing in a challenge credit section
                            makeCourseUnavailable(data, numbers, "Credit by Exam");
                        } else if (sectData.pacing == null || sectData.rules == null) {
                            // "enforceEligibility" cannot make exams available with bad configuration data
                            makeCourseUnavailable(data, numbers, "No Pacing Data");
                        } else if ("Y".equals(reg.iInProgress)) {
                            testSingleIncomplete(cache, data, reg, pace, track, sectData, enforceEligibility);
                        } else {
                            testSingleCourse(cache, data, reg, pace, track, sectData, enforceEligibility);
                        }
                    } else {
                        // "enforceEligibility" cannot make exams available in course that is not open
                        makeCourseUnavailable(data, numbers, "Course Not Open");
                    }
                }
            }
        }
    }

    /**
     * Given a list of course registrations, loads the section information for all registrations whose open status is
     * not "G" or "N".  If the course section's instruction type is "OT", the pacing structure and rules are not
     * populated, but the course section record is included.
     *
     * @param cache         the data cache
     * @param registrations the list of courses
     * @param sections      the section data objects map to populate
     * @throws SQLException if there was an error accessing the database
     */
    private void loadSectionData(final Cache cache, final Iterable<RawStcourse> registrations,
                                 final Map<? super RawStcourse, ? super SectionData> sections)
            throws SQLException {

        // NOTE: we attempt to use the rule set from the term when an incomplete was earned rather than the active term,
        // if that data is available.  This is flawed since we can't query the prior term's pacing structure data.

        final SystemData systemData = cache.getSystemData();

        final List<RawPacingStructure> allPacing = RawPacingStructureLogic.queryByTerm(cache, this.activeTerm.term);

        for (final RawStcourse reg : registrations) {

            final String openStatus = reg.openStatus;
            if ("G".equals(openStatus) || "N".equals(openStatus)) {
                continue;
            }

            CourseNumbers numbers = null;
            for (final CourseNumbers test : CourseNumbers.COURSES) {
                if (test.isMatching(reg.course)) {
                    numbers = test;
                    break;
                }
            }
            if (numbers == null) {
                continue;
            }

            TermRec effTerm = this.activeTerm;
            if (!effTerm.term.equals(reg.termKey)) {
                final TermRec incTerm = systemData.getTerm(reg.termKey);
                if (incTerm != null) {
                    effTerm = incTerm;
                }
            }

            final RawCsection cSection = systemData.getCourseSection(reg.course, reg.sect, effTerm.term);
            if (cSection == null) {
                continue;
            }

            RawPacingStructure pacing = null;
            List<RawPacingRules> rules = null;
            Map<Integer, RawCusection> cusectionMap = null;

            if (!"OT".equals(cSection.instrnType)) {
                for (final RawPacingStructure test : allPacing) {
                    if (test.pacingStructure.equals(cSection.pacingStructure)) {
                        pacing = test;
                        rules = RawPacingRulesLogic.queryByTermAndPacingStructure(cache, effTerm.term,
                                test.pacingStructure);
                        break;
                    }
                }

                final List<RawCusection> cusections = systemData.getCourseUnitSections(reg.course, reg.sect,
                        effTerm.term);
                final int count = cusections.size();
                cusectionMap = new HashMap<>(count);
                for (final RawCusection cusect : cusections) {
                    cusectionMap.put(cusect.unit, cusect);
                }
            }

            final SectionData sectData = new SectionData(numbers, cSection, pacing, rules, cusectionMap);
            sections.put(reg, sectData);
        }
    }

    /**
     * Determines the availability of exams in an incomplete course.
     *
     * @param cache              the data cache
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update
     * @param reg                the registration (known to be open, and not an "OT" section)
     * @param pace               the student's pace
     * @param paceTrack          the student's pace track
     * @param sectData           the section data
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    private void testSingleIncomplete(final Cache cache, final DataCourseExams data, final RawStcourse reg,
                                      final int pace, final String paceTrack, final SectionData sectData,
                                      final boolean enforceEligibility) throws SQLException {

        makeCourseAvailable(data, sectData.numbers);

        if (reg.iDeadlineDt != null && reg.iDeadlineDt.isBefore(this.today)) {
            indicateCourseUnavailable(data, "Past Inc. Deadline", enforceEligibility);
        }

        if ("Y".equals(sectData.pacing.requireLicensed) && !"Y".equals(this.checkInData.studentData.student.licensed)) {
            indicateCourseUnavailable(data, "Need User's Exam", enforceEligibility);
        }

        final CourseDeadlines courseDeadlines = getCourseDeadlines(cache, reg, pace, paceTrack, sectData.numbers);

        final boolean isNew = sectData.numbers.isNew(reg.course);
        OldCourseWorkRecord oldWorkRecord = null;
        StandardsMasteryLogic standardsLogic = null;

        if (isNew) {
            standardsLogic = new StandardsMasteryLogic(cache, reg.stuId, reg.course);
        } else {
            oldWorkRecord = gatherOldCourseWorkRecord(cache, reg);
        }

        if ("Y".equals(reg.iCounted)) {
            checkCourseDeadline(reg, data, sectData, courseDeadlines, oldWorkRecord, standardsLogic,
                    enforceEligibility);
        }

        // All tests above marked the entire course as available or not.  At this point, we begin checking unit by
        // unit, and setting the availability of each.
        if (!enforceEligibility || data.unit1Exam.available) {

            // Check for "UE" deadlines from milestones (for OLD courses)
            testUnitExamDeadlines(data, courseDeadlines, oldWorkRecord, enforceEligibility);

            // Mark exams as unavailable if required prior work has not been completed
            testPacingRules(reg, data, sectData, oldWorkRecord, standardsLogic, enforceEligibility);

            // Test whether the student has N or more failed UE attempts since the last passing RE
            testPassingReviewAfterFailedUnit(reg, data, sectData, oldWorkRecord, enforceEligibility);

            // Test max attempts per unit
            testMaxAttempts(reg, data, sectData, oldWorkRecord, enforceEligibility);
        }

        if (!enforceEligibility) {
            data.unit1Exam.annotateIneligible();
            data.unit2Exam.annotateIneligible();
            data.unit3Exam.annotateIneligible();
            data.unit4Exam.annotateIneligible();
            data.finalExam.annotateIneligible();
            data.masteryExam.annotateIneligible();
            data.challengeExam.annotateIneligible();
        }
    }

    /**
     * Determines the availability of exams in a current-term (not incomplete) course.
     *
     * @param cache              the data cache
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update
     * @param reg                the registration (known to be open, and not an "OT" section)
     * @param pace               the student's pace
     * @param paceTrack          the student's pace track
     * @param sectData           the section data
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     * @throws SQLException if there is an error accessing the database
     */
    private void testSingleCourse(final Cache cache, final DataCourseExams data, final RawStcourse reg,
                                  final int pace, final String paceTrack, final SectionData sectData,
                                  final boolean enforceEligibility) throws SQLException {

        makeCourseAvailable(data, sectData.numbers);

        // If the course requires "licensing" and the student is not licensed, mark as unavailable
        if ("Y".equals(sectData.pacing.requireLicensed) &&
                !"Y".equals(this.checkInData.studentData.student.licensed)) {
            indicateCourseUnavailable(data, "Need User's Exam", enforceEligibility);
        }

        final CourseDeadlines courseDeadlines = getCourseDeadlines(cache, reg, pace, paceTrack, sectData.numbers);

        final boolean isNew = sectData.numbers.isNew(reg.course);
        OldCourseWorkRecord oldWorkRecord = null;
        StandardsMasteryLogic standardsLogic = null;

        if (isNew) {
            standardsLogic = new StandardsMasteryLogic(cache, reg.stuId, reg.course);
        } else {
            oldWorkRecord = gatherOldCourseWorkRecord(cache, reg);
        }

        checkCourseDeadline(reg, data, sectData, courseDeadlines, oldWorkRecord, standardsLogic, enforceEligibility);

        // All tests above marked the entire course as available or not.  At this point, we begin checking unit by
        // unit, and setting the availability of each.
        if (!enforceEligibility || data.unit1Exam.available) {

            // Check the "first_test_dt" and "last_test_dt" for each unit from CUSECTION
            testTestingWindows(data, sectData, enforceEligibility);

            // Check for "UE" deadlines from milestones (for OLD courses)
            testUnitExamDeadlines(data, courseDeadlines, oldWorkRecord, enforceEligibility);

            // Mark exams as unavailable if required prior work has not been completed
            testPacingRules(reg, data, sectData, oldWorkRecord, standardsLogic, enforceEligibility);

            // Test whether the student has N or more failed UE attempts since the last passing RE
            testPassingReviewAfterFailedUnit(reg, data, sectData, oldWorkRecord, enforceEligibility);

            // Test max attempts per unit
            testMaxAttempts(reg, data, sectData, oldWorkRecord, enforceEligibility);
        }

        if (!enforceEligibility) {
            data.unit1Exam.annotateIneligible();
            data.unit2Exam.annotateIneligible();
            data.unit3Exam.annotateIneligible();
            data.unit4Exam.annotateIneligible();
            data.finalExam.annotateIneligible();
            data.masteryExam.annotateIneligible();
            data.challengeExam.annotateIneligible();
        }
    }

    /**
     * Determines the course deadlines for a registration.
     *
     * @param cache     the cache
     * @param reg       the registration
     * @param numbers   the course numbers for the course associated with the registration
     * @param pace      the student's pace
     * @param paceTrack the student's pace track
     * @return the course deadlines; {@code null} if they could not be determined
     * @throws SQLException if there was an error accessing the database
     */
    private CourseDeadlines getCourseDeadlines(final Cache cache, final RawStcourse reg, final int pace,
                                               final String paceTrack, final CourseNumbers numbers)
            throws SQLException {

        // Check the course deadline date based on pace track, pace, and pace order
        final CourseDeadlines courseDeadlines;

        if (reg.paceOrder == null) {
            courseDeadlines = new CourseDeadlines(null, null, null, null, null, null, 0, null);
        } else if (numbers.isNew(reg.course)) {
            // New course - look for course deadline
            final Integer paceObj = Integer.valueOf(pace);
            final List<StandardMilestoneRec> allMilestones =
                    StandardMilestoneLogic.get(cache).queryByPaceTrackPaceIndex(cache, paceTrack, paceObj,
                            reg.paceOrder);
            final List<StudentStandardMilestoneRec> allStMilestones =
                    StudentStandardMilestoneLogic.get(cache).queryByStuPaceTrackPaceIndex(cache, reg.stuId,
                            paceTrack, paceObj, reg.paceOrder);

            LocalDate deadline = null;
            for (final StandardMilestoneRec ms : allMilestones) {
                if (StandardMilestoneRec.MS_TYPE_COURSE_DEADLINE.equals(ms.msType)) {
                    deadline = ms.msDate;
                }
            }
            for (final StudentStandardMilestoneRec stms : allStMilestones) {
                if (StandardMilestoneRec.MS_TYPE_COURSE_DEADLINE.equals(stms.msType)) {
                    deadline = stms.msDate;
                }
            }

            courseDeadlines = new CourseDeadlines(null, null, null, null, null, null, 0, deadline);
        } else {
            // Old course - look for "FE" and "F1" milestones
            final List<RawMilestone> allMilestones =
                    RawMilestoneLogic.getAllMilestones(cache, this.activeTerm.term, pace, paceTrack);

            final List<RawStmilestone> allStMilestones =
                    RawStmilestoneLogic.getStudentMilestones(cache, this.activeTerm.term, paceTrack, reg.stuId);
            allStMilestones.sort(null);

            final int msNumber = pace * 100 + reg.paceOrder.intValue() * 10 + 5;
            int attempts = 0;

            LocalDate u1Deadline = null;
            LocalDate u2Deadline = null;
            LocalDate u3Deadline = null;
            LocalDate u4Deadline = null;
            LocalDate feDeadline = null;
            LocalDate f1Deadline = null;
            for (final RawMilestone ms : allMilestones) {
                if (ms.msNbr.intValue() == msNumber) {
                    if ("U1".equals(ms.msType)) {
                        u1Deadline = ms.msDate;
                    } else if ("U2".equals(ms.msType)) {
                        u2Deadline = ms.msDate;
                    } else if ("U3".equals(ms.msType)) {
                        u3Deadline = ms.msDate;
                    } else if ("U4".equals(ms.msType)) {
                        u4Deadline = ms.msDate;
                    } else if ("FE".equals(ms.msType)) {
                        feDeadline = ms.msDate;
                    } else if ("F1".equals(ms.msType)) {
                        f1Deadline = ms.msDate;
                        if (ms.nbrAtmptsAllow != null) {
                            attempts = ms.nbrAtmptsAllow.intValue();
                        }
                    }
                    // Don't break - student milestones are sorted by deadline date, and if there are multiple, we want
                    // the later date
                }
            }
            for (final RawStmilestone stms : allStMilestones) {
                if (stms.msNbr.intValue() == msNumber) {
                    if ("U1".equals(stms.msType)) {
                        u1Deadline = stms.msDate;
                    } else if ("U2".equals(stms.msType)) {
                        u2Deadline = stms.msDate;
                    } else if ("U3".equals(stms.msType)) {
                        u3Deadline = stms.msDate;
                    } else if ("U4".equals(stms.msType)) {
                        u4Deadline = stms.msDate;
                    } else if ("FE".equals(stms.msType)) {
                        feDeadline = stms.msDate;
                    } else if ("F1".equals(stms.msType)) {
                        f1Deadline = stms.msDate;
                        if (stms.nbrAtmptsAllow != null) {
                            attempts = stms.nbrAtmptsAllow.intValue();
                        }
                    }
                    // Don't break - student milestones are sorted by deadline date, and if there are multiple, we want
                    // the later date
                }
            }

            final LocalDate deadline = f1Deadline == null ? feDeadline : f1Deadline;

            courseDeadlines = new CourseDeadlines(u1Deadline, u2Deadline, u3Deadline, u4Deadline, feDeadline,
                    f1Deadline, attempts, deadline);
        }

        return courseDeadlines;
    }

    /**
     * Gathers the student's work record.
     *
     * @param cache the data cache
     * @param reg   the registration
     * @return the work record
     * @throws SQLException if there is an error accessing the database
     */
    private static OldCourseWorkRecord gatherOldCourseWorkRecord(final Cache cache, final RawStcourse reg)
            throws SQLException {

        final List<RawStexam> stexams = RawStexamLogic.getExams(cache, reg.stuId, reg.course, false,
                RawStexamLogic.ALL_EXAM_TYPES);

        final List<RawSthomework> sthws = RawSthomeworkLogic.getHomeworks(cache, reg.stuId, reg.course, false,
                RawSthomeworkLogic.ALL_HW_TYPES);

        return new OldCourseWorkRecord(sthws, stexams);
    }

    /**
     * Tests course deadlines.  If the final exam has already been passed, or the FE deadline is not in the past,
     * nothing is done.  Otherwise, if the F1 deadline has passed, the course is made unavailable.  If not, but the FE
     * deadline has passed, and the student has passed Unit 4, the number of final exam tries since the FE deadline is
     * counted and compared to the "attempts allowed" field.
     *
     * @param reg                the course registration
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update (exams in this
     *                           object may be marked as unavailable already, or as available but with eligibility
     *                           override notes.
     * @param sectData           the section data
     * @param courseDeadlines    the course deadlines
     * @param workRecord         the work record
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private void checkCourseDeadline(final RawStcourse reg, final DataCourseExams data, final SectionData sectData,
                                     final CourseDeadlines courseDeadlines, final OldCourseWorkRecord workRecord,
                                     final StandardsMasteryLogic standardsLogic, final boolean enforceEligibility) {

        final boolean isNew = sectData.numbers.isNew(reg.course);

        if (isNew) {
            // If enough standards have been mastered to complete the course, do nothing, since the student can
            // continue to master standards until the section's last testing date.
            if (!standardsLogic.areEnoughStandardsMasteredToPassCourse()) {
                // New courses have only the "course deadline" with no "last try" concept
                final LocalDate courseDeadline = courseDeadlines.courseDeadline();
                if (Objects.nonNull(courseDeadline) && this.today.isAfter(courseDeadline)) {
                    indicateCourseUnavailable(data, PAST_DEADLINE, enforceEligibility);
                }
            }
        } else if (!workRecord.isFinalExamPassed()) {
            // If the final exam has already been passed, we disregard the course deadline.  The student can re-test
            // to improve grade until the section's last test date.

            final LocalDate f1Deadline = courseDeadlines.f1Deadline();

            if (Objects.nonNull(f1Deadline) && f1Deadline.isBefore(this.today)) {

                Log.info("F1 deadline is in the past...");

                // It could be that the FE deadline has been moved but not the F1
                final LocalDate feDeadline = courseDeadlines.feDeadline();
                if (feDeadline == null || feDeadline.isBefore(this.today)) {
                    Log.info("FE deadline is also in the past...");

                    indicateCourseUnavailable(data, PAST_DEADLINE, enforceEligibility);
                }
            } else {
                final LocalDate feDeadline = courseDeadlines.feDeadline();

                if (Objects.nonNull(feDeadline) && feDeadline.isBefore(this.today)) {
                    Log.info("FE deadline is in the past, checking tries allowed");

                    final int triesAllowed = courseDeadlines.f1AttemptsAllowed();
                    if (triesAllowed == 0) {
                        Log.info("There are 0 tries allowed!");
                        indicateCourseUnavailable(data, PAST_DEADLINE, enforceEligibility);
                    } else {
                        final int attemptsSoFar = workRecord.countFinalAttemptsAfter(feDeadline);
                        Log.info("There are " + triesAllowed + " tries allowed " + " and " + attemptsSoFar + " " +
                                "attempts so far");

                        if (attemptsSoFar >= triesAllowed) {
                            indicateCourseUnavailable(data, PAST_DEADLINE, enforceEligibility);
                        }
                    }
                }
            }
        }
    }

    /**
     * Makes all exams in a course (except possibly the challenge exam) as available.
     *
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update
     * @param sectData           the section data
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private void testTestingWindows(final DataCourseExams data, final SectionData sectData,
                                    final boolean enforceEligibility) {

        final RawCusection unit1 = sectData.cuSections.get(ONE);
        testUnitTestingWindow(unit1, data.unit1Exam, enforceEligibility);

        final RawCusection unit2 = sectData.cuSections.get(TWO);
        testUnitTestingWindow(unit2, data.unit2Exam, enforceEligibility);

        final RawCusection unit3 = sectData.cuSections.get(THREE);
        testUnitTestingWindow(unit3, data.unit3Exam, enforceEligibility);

        final RawCusection unit4 = sectData.cuSections.get(FOUR);
        testUnitTestingWindow(unit4, data.unit4Exam, enforceEligibility);

        final RawCusection unit5 = sectData.cuSections.get(FIVE);
        testUnitTestingWindow(unit5, data.finalExam, enforceEligibility);
    }

    /**
     * Checks whether the current date is outside the testing window for a unit, and marks the exam status accordingly.
     *
     * @param unit               the unit section configuration
     * @param exam               the exam object to update
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private void testUnitTestingWindow(final RawCusection unit, final DataExamStatus exam,
                                       final boolean enforceEligibility) {

        if (Objects.nonNull(unit.firstTestDt) && unit.firstTestDt.isAfter(this.today)) {
            final String firstTestDtStr = TemporalUtils.FMT_MDY_COMPACT.format(unit.firstTestDt);
            final String msg = SimpleBuilder.concat(OPENS, firstTestDtStr);
            indicateExamUnavailable(exam, msg, enforceEligibility);
        } else if (Objects.nonNull(unit.lastTestDt) && unit.lastTestDt.isBefore(this.today)) {
            final String lastTestDtStr = TemporalUtils.FMT_MDY_COMPACT.format(unit.lastTestDt);
            final String msg = SimpleBuilder.concat(OPENS, lastTestDtStr);
            indicateExamUnavailable(exam, msg, enforceEligibility);
        }
    }

    /**
     * Tests for "UE" deadline types in the milestone table.  If a unit exam has been passed, these deadlines do not
     * apply, and the student can re-test.  If it has not been passed and this deadline is in the past, the exam is not
     * available.
     *
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update
     * @param courseDeadlines    the course deadlines
     * @param workRecord         the student's work record
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private void testUnitExamDeadlines(final DataCourseExams data, final CourseDeadlines courseDeadlines,
                                       final OldCourseWorkRecord workRecord, final boolean enforceEligibility) {

        final LocalDate u1Due = courseDeadlines.u1Deadline();
        if (Objects.nonNull(u1Due) && u1Due.isBefore(this.today)) {
            final RawStexam firstPassingU1 = workRecord.getFirstPassingUnitExam(1);
            if (firstPassingU1 == null) {
                indicateExamUnavailable(data.unit1Exam, PAST_UNIT_DEADLINE, enforceEligibility);
            }
        }

        final LocalDate u2Due = courseDeadlines.u2Deadline();
        if (Objects.nonNull(u2Due) && u2Due.isBefore(this.today)) {
            final RawStexam firstPassingU2 = workRecord.getFirstPassingUnitExam(2);
            if (firstPassingU2 == null) {
                indicateExamUnavailable(data.unit2Exam, PAST_UNIT_DEADLINE, enforceEligibility);
            }
        }

        final LocalDate u3Due = courseDeadlines.u3Deadline();
        if (Objects.nonNull(u3Due) && u3Due.isBefore(this.today)) {
            final RawStexam firstPassingU3 = workRecord.getFirstPassingUnitExam(3);
            if (firstPassingU3 == null) {
                indicateExamUnavailable(data.unit3Exam, PAST_UNIT_DEADLINE, enforceEligibility);
            }
        }

        final LocalDate u4Due = courseDeadlines.u4Deadline();
        if (Objects.nonNull(u4Due) && u4Due.isBefore(this.today)) {
            final RawStexam firstPassingU4 = workRecord.getFirstPassingUnitExam(4);
            if (firstPassingU4 == null) {
                indicateExamUnavailable(data.unit4Exam, PAST_UNIT_DEADLINE, enforceEligibility);
            }
        }
    }

    /**
     * Ensures that requirements specified in the pacing rules for the section are satisfied.
     *
     * @param reg                the course registration
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update
     * @param sectData           the section data
     * @param workRecord         the student's work record
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private static void testPacingRules(final RawStcourse reg, final DataCourseExams data,
                                        final SectionData sectData, final OldCourseWorkRecord workRecord,
                                        final StandardsMasteryLogic standardsLogic,
                                        final boolean enforceEligibility) {

        final boolean isOld = sectData.numbers.isOld(reg.course);

        if (isOld) {
            final RawStexam passedU1 = workRecord.getFirstPassingUnitExam(1);
            final RawStexam passedU2 = workRecord.getFirstPassingUnitExam(2);
            final RawStexam passedU3 = workRecord.getFirstPassingUnitExam(3);
            final RawStexam passedU4 = workRecord.getFirstPassingUnitExam(4);

            // See if the prior unit exam must be passed before accessing a Unit exam
            if (sectData.isRequired(RawPacingRulesLogic.ACTIVITY_UNIT_EXAM, RawPacingRulesLogic.UE_MSTR)
                    || sectData.isRequired(RawPacingRulesLogic.ACTIVITY_UNIT_EXAM, RawPacingRulesLogic.UE_PASS)) {

                if (passedU1 == null) {
                    indicateExamUnavailable(data.unit2Exam, "Must Pass Unit 1", enforceEligibility);
                }

                if (passedU2 == null) {
                    indicateExamUnavailable(data.unit3Exam, "Must Pass Unit 2", enforceEligibility);
                }

                if (passedU3 == null) {
                    indicateExamUnavailable(data.unit4Exam, "Must Pass Unit 3", enforceEligibility);
                }
            }

            // See if the unit review exam must be passed before accessing a Unit exam
            if (sectData.isRequired(RawPacingRulesLogic.ACTIVITY_UNIT_EXAM, RawPacingRulesLogic.UR_MSTR)
                    || sectData.isRequired(RawPacingRulesLogic.ACTIVITY_UNIT_EXAM, RawPacingRulesLogic.UR_PASS)) {

                final RawStexam passedR1 = workRecord.getFirstPassingReviewExam(1);
                if (passedR1 == null && passedU1 == null) {
                    indicateExamUnavailable(data.unit1Exam, MUST_PASS_REVIEW, enforceEligibility);
                }

                final RawStexam passedR2 = workRecord.getFirstPassingReviewExam(2);
                if (passedR2 == null && passedU2 == null) {
                    indicateExamUnavailable(data.unit2Exam, MUST_PASS_REVIEW, enforceEligibility);
                }

                final RawStexam passedR3 = workRecord.getFirstPassingReviewExam(3);
                if (passedR3 == null && passedU3 == null) {
                    indicateExamUnavailable(data.unit3Exam, MUST_PASS_REVIEW, enforceEligibility);
                }

                final RawStexam passedR4 = workRecord.getFirstPassingReviewExam(4);
                if (passedR4 == null && passedU4 == null) {
                    indicateExamUnavailable(data.unit4Exam, MUST_PASS_REVIEW, enforceEligibility);
                }
            }

            // See if the last unit exam must be passed before accessing Final exam
            if (sectData.isRequired(RawPacingRulesLogic.ACTIVITY_FINAL_EXAM, RawPacingRulesLogic.UE_MSTR)
                    || sectData.isRequired(RawPacingRulesLogic.ACTIVITY_FINAL_EXAM, RawPacingRulesLogic.UE_PASS)) {

                if (passedU4 == null) {
                    indicateExamUnavailable(data.finalExam, "Must Pass Unit 4", enforceEligibility);
                }
            }
        } else {
            // FIXME: Move these rules into data - student needs to have at least one standard assignment passed for
            // FIXME: which the corresponding standard has not yet been mastered.

            if (data.masteryExam.available) {
                final int numAvailableStandards = standardsLogic.countAvailableStandards();
                if (numAvailableStandards == 0) {
                    // It could be zero because all have been mastered, or because student needs to pass homework

                    final int numCompleteStandards = standardsLogic.countCompleteStandards();
                    final int totalStandards = standardsLogic.countTotalStandards();

                    if (numCompleteStandards == totalStandards) {
                        indicateExamUnavailable(data.masteryExam, "All Targets Mastered", enforceEligibility);
                    } else {
                        indicateExamUnavailable(data.masteryExam, "No Targets Avail.", enforceEligibility);
                    }
                } else {
                    data.masteryExam.note = numAvailableStandards == 1 ? "(1 standard)"
                            : ("(" + numAvailableStandards + " standards)");
                }
            }
        }
    }

    /**
     * Ensures that the student has passed the review test enough times to warrant taking the proctored unit exam, and
     * enforce limits on the number of times the student may take the proctored exam after each successful attempt on
     * the review exam.
     *
     * @param reg                the course registration
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update
     * @param sectData           the section data
     * @param workRecord         the student's work record
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private static void testPassingReviewAfterFailedUnit(final RawStcourse reg, final DataCourseExams data,
                                                         final SectionData sectData,
                                                         final OldCourseWorkRecord workRecord,
                                                         final boolean enforceEligibility) {

        final boolean isOld = sectData.numbers.isOld(reg.course);

        if (isOld) {
            final RawStexam passedU1 = workRecord.getFirstPassingUnitExam(1);
            final RawStexam passedU2 = workRecord.getFirstPassingUnitExam(2);
            final RawStexam passedU3 = workRecord.getFirstPassingUnitExam(3);
            final RawStexam passedU4 = workRecord.getFirstPassingUnitExam(4);

            if (passedU1 == null) {
                final Integer allowed1 = sectData.cuSections.get(ONE).atmptsPerReview;
                if (Objects.nonNull(allowed1) && allowed1.intValue() < INFINITY) {
                    final int numFailed = workRecord.countFailedExamSincePassingReview(1);
                    if (numFailed >= allowed1.intValue()) {
                        indicateExamUnavailable(data.unit1Exam, MUST_REPASS_REVIEW, enforceEligibility);
                    }
                }
            }

            if (passedU2 == null) {
                final Integer allowed2 = sectData.cuSections.get(TWO).atmptsPerReview;
                if (Objects.nonNull(allowed2) && allowed2.intValue() < INFINITY) {
                    final int numFailed = workRecord.countFailedExamSincePassingReview(2);
                    if (numFailed >= allowed2.intValue()) {
                        indicateExamUnavailable(data.unit2Exam, MUST_REPASS_REVIEW, enforceEligibility);
                    }
                }
            }

            if (passedU3 == null) {
                final Integer allowed3 = sectData.cuSections.get(THREE).atmptsPerReview;
                if (Objects.nonNull(allowed3) && allowed3.intValue() < INFINITY) {
                    final int numFailed = workRecord.countFailedExamSincePassingReview(3);
                    if (numFailed >= allowed3.intValue()) {
                        indicateExamUnavailable(data.unit3Exam, MUST_REPASS_REVIEW, enforceEligibility);
                    }
                }
            }

            if (passedU4 == null) {
                final Integer allowed4 = sectData.cuSections.get(FOUR).atmptsPerReview;
                if (Objects.nonNull(allowed4) && allowed4.intValue() < INFINITY) {
                    final int numFailed = workRecord.countFailedExamSincePassingReview(4);
                    if (numFailed >= allowed4.intValue()) {
                        indicateExamUnavailable(data.unit4Exam, MUST_REPASS_REVIEW, enforceEligibility);
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
     * @param reg                the course registration
     * @param data               the {@code CheckInDataCourseExams} whose exam status values to update
     * @param sectData           the section data
     * @param workRecord         the student's work record
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private static void testMaxAttempts(final RawStcourse reg, final DataCourseExams data,
                                        final SectionData sectData, final OldCourseWorkRecord workRecord,
                                        final boolean enforceEligibility) {

        final boolean isOld = sectData.numbers.isOld(reg.course);

        if (isOld) {
            final Integer maxAttempts1 = sectData.cuSections.get(ONE).nbrAtmptsAllow;
            if (Objects.nonNull(maxAttempts1) && maxAttempts1.intValue() < INFINITY) {
                final int attemptsSoFar = workRecord.countUnitExams(1);
                if (attemptsSoFar >= maxAttempts1.intValue()) {
                    indicateExamUnavailable(data.unit1Exam, ALL_ATTEMPTS_USED, enforceEligibility);
                }
            }

            final Integer maxAttempts2 = sectData.cuSections.get(TWO).nbrAtmptsAllow;
            if (Objects.nonNull(maxAttempts2) && maxAttempts2.intValue() < INFINITY) {
                final int attemptsSoFar = workRecord.countUnitExams(2);
                if (attemptsSoFar >= maxAttempts2.intValue()) {
                    indicateExamUnavailable(data.unit2Exam, ALL_ATTEMPTS_USED, enforceEligibility);
                }
            }

            final Integer maxAttempts3 = sectData.cuSections.get(THREE).nbrAtmptsAllow;
            if (Objects.nonNull(maxAttempts3) && maxAttempts3.intValue() < INFINITY) {
                final int attemptsSoFar = workRecord.countUnitExams(3);
                if (attemptsSoFar >= maxAttempts3.intValue()) {
                    indicateExamUnavailable(data.unit3Exam, ALL_ATTEMPTS_USED, enforceEligibility);
                }
            }

            final Integer maxAttempts4 = sectData.cuSections.get(FOUR).nbrAtmptsAllow;
            if (Objects.nonNull(maxAttempts4) && maxAttempts4.intValue() < INFINITY) {
                final int attemptsSoFar = workRecord.countUnitExams(4);
                if (attemptsSoFar >= maxAttempts4.intValue()) {
                    indicateExamUnavailable(data.unit4Exam, ALL_ATTEMPTS_USED, enforceEligibility);
                }
            }

            final Integer maxAttempts5 = sectData.cuSections.get(FIVE).nbrAtmptsAllow;
            if (Objects.nonNull(maxAttempts5) && maxAttempts5.intValue() < INFINITY) {
                final int attemptsSoFar = workRecord.countFinalExams();
                if (attemptsSoFar >= maxAttempts5.intValue()) {
                    indicateExamUnavailable(data.finalExam, ALL_ATTEMPTS_USED, enforceEligibility);
                }
            }
        }

        // TODO: Limit on NEW course mastery exams?
    }

    /**
     * Makes all exams in a course (except possibly the challenge exam) as unavailable.
     *
     * @param data    the {@code CheckInDataCourseExams} whose exam status values to update
     * @param numbers the course numbers
     * @param reason  the reason the exams are not available
     */
    private static void makeCourseUnavailable(final DataCourseExams data, final CourseNumbers numbers,
                                              final String reason) {

        final String oldCourseId = numbers.oldCourseId();
        final String newCourseId = numbers.newCourseId();

        data.unit1Exam = DataExamStatus.unavailable(oldCourseId, 1, reason);
        data.unit2Exam = DataExamStatus.unavailable(oldCourseId, 2, reason);
        data.unit3Exam = DataExamStatus.unavailable(oldCourseId, 3, reason);
        data.unit4Exam = DataExamStatus.unavailable(oldCourseId, 4, reason);
        data.finalExam = DataExamStatus.unavailable(oldCourseId, 5, reason);
        data.masteryExam = DataExamStatus.unavailable(newCourseId, 0, reason);
    }

    /**
     * Makes all exams in a course (except possibly the challenge exam) as available.
     *
     * @param data    the {@code CheckInDataCourseExams} whose exam status values to update
     * @param numbers the course numbers
     */
    private static void makeCourseAvailable(final DataCourseExams data, final CourseNumbers numbers) {

        final String oldCourseId = numbers.oldCourseId();
        final String newCourseId = numbers.newCourseId();

        data.unit1Exam = DataExamStatus.available(oldCourseId, 1);
        data.unit2Exam = DataExamStatus.available(oldCourseId, 2);
        data.unit3Exam = DataExamStatus.available(oldCourseId, 3);
        data.unit4Exam = DataExamStatus.available(oldCourseId, 4);
        data.finalExam = DataExamStatus.available(oldCourseId, 5);
        data.masteryExam = DataExamStatus.available(newCourseId, 0);
    }

    /**
     * Marks all course exams in a course as unavailable with a specified message, if unit 1's exam is not already
     * marked as unavailable.  If eligibility is not being enforced, exams are not marked as unavailable, but the
     * message is added to all exams' lists of eligibility conditions that were not met.
     *
     * @param data               the course exams data object to update
     * @param msg                the reason message
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private static void indicateCourseUnavailable(final DataCourseExams data, final String msg,
                                                  final boolean enforceEligibility) {

        if (enforceEligibility) {
            if (data.unit1Exam.available) {
                data.unit1Exam.indicateUnavailable(msg);
                data.unit2Exam.indicateUnavailable(msg);
                data.unit3Exam.indicateUnavailable(msg);
                data.unit4Exam.indicateUnavailable(msg);
                data.finalExam.indicateUnavailable(msg);
                data.masteryExam.indicateUnavailable(msg);
            }
        } else {
            data.unit1Exam.eligibilityOverrides.add(msg);
            data.unit2Exam.eligibilityOverrides.add(msg);
            data.unit3Exam.eligibilityOverrides.add(msg);
            data.unit4Exam.eligibilityOverrides.add(msg);
            data.finalExam.eligibilityOverrides.add(msg);
            data.masteryExam.eligibilityOverrides.add(msg);
        }
    }

    /**
     * Marks one exam in a course as unavailable with a specified message.  If eligibility is not being enforced, the
     * exam is not marked as unavailable, but the message is added to that exam's list of eligibility conditions that
     * were not met.
     *
     * @param exam               the exam data object to update
     * @param msg                the reason message
     * @param enforceEligibility true to enforce all eligibility checks; false to override these checks to allow a
     *                           student to take an exam in special situations where an eligibility condition should be
     *                           waived
     */
    private static void indicateExamUnavailable(final DataExamStatus exam, final String msg,
                                                final boolean enforceEligibility) {

        if (enforceEligibility) {
            if (exam.available) {
                exam.indicateUnavailable(msg);
            }
        } else {
            exam.eligibilityOverrides.add(msg);
        }
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CheckInLogicCourseExams{today=", this.today, ", activeTerm=", this.activeTerm,
                ", checkInData=", this.checkInData, "}");
    }

    /** A record to store deadlines associated with a course. */
    private record CourseDeadlines(LocalDate u1Deadline, LocalDate u2Deadline, LocalDate u3Deadline,
                                   LocalDate u4Deadline, LocalDate feDeadline, LocalDate f1Deadline,
                                   int f1AttemptsAllowed, LocalDate courseDeadline) {
    }

    /**
     * A container for data related to a single course section.
     */
    private static final class SectionData {

        /** The course numbers. */
        final CourseNumbers numbers;

        /** The course section. */
        final RawCsection cSection;

        /** The pacing structure. */
        final RawPacingStructure pacing;

        /** The pacing rules. */
        final List<RawPacingRules> rules;

        /** The course unit sections. */
        final Map<Integer, RawCusection> cuSections;

        /**
         * Constructs a new {@code SectionData}.
         *
         * @param theNumbers    the course numbers
         * @param theCSection   the course section
         * @param thePacing     the pacing structure
         * @param theRules      the pacing rules
         * @param theCUSections a map from unit number to the course unit section configuration
         */
        SectionData(final CourseNumbers theNumbers, final RawCsection theCSection, final RawPacingStructure thePacing,
                    final List<RawPacingRules> theRules, final Map<Integer, RawCusection> theCUSections) {

            this.numbers = theNumbers;
            this.cSection = theCSection;
            this.pacing = thePacing;
            this.rules = theRules;
            this.cuSections = theCUSections;
        }

        /**
         * Tests whether the pacing rules configured for this section specify some particular requirement.
         *
         * @param activity    the activity
         * @param requirement the requirement that must be completed to perform the activity
         * @return true if this requirement is specified by the pacing rules
         */
        boolean isRequired(final String activity, final String requirement) {

            boolean required = false;

            if (this.rules != null) {
                for (final RawPacingRules rule : this.rules) {
                    if (activity.equals(rule.activityType) && requirement.equals(rule.requirement)) {
                        required = true;
                        break;
                    }
                }
            }

            return required;
        }

        /**
         * Generates a diagnostic string representation of the object.
         *
         * @return the string representation
         */
        @Override
        public String toString() {

            return SimpleBuilder.concat("SectionData{numbers=", this.numbers, ", cSection=", this.cSection,
                    ", pacing=", this.pacing, ", rules=", this.rules, ", cuSections=", this.cuSections, "}");
        }
    }

    /**
     * A container for the student's work record in a course.
     */
    private static final class OldCourseWorkRecord {

        /** Homeworks the student has taken. */
        final List<RawSthomework> stHomeworks;

        /** Exams the student has taken. */
        final List<RawStexam> stExams;

        /**
         * Constructs a new {@code WorkRecord}.
         *
         * @param theStHomeworks homeworks the student has taken
         * @param theStExams     exams the student has taken
         */
        OldCourseWorkRecord(final List<RawSthomework> theStHomeworks, final List<RawStexam> theStExams) {

            this.stHomeworks = theStHomeworks;
            this.stExams = theStExams;
        }

        /**
         * Tests whether the student has a passing score on the final exam (used for OLD courses).
         *
         * @return true if the final exam has been passed
         */
        boolean isFinalExamPassed() {

            boolean passedFinal = false;

            for (final RawStexam exam : this.stExams) {
                if (RawStexam.FINAL_EXAM.equals(exam.examType) && "Y".equals(exam.passed)) {
                    passedFinal = true;
                    break;
                }
            }

            return passedFinal;
        }

        /**
         * Gets the user's first passing submission for a specified unit exam.
         *
         * @param theUnit the unit
         * @return the earliest passing Unit exam in that unit
         */
        RawStexam getFirstPassingUnitExam(final int theUnit) {

            RawStexam firstPassing = null;

            for (final RawStexam rec : this.stExams) {

                if (RawStexam.UNIT_EXAM.equals(rec.examType) && "Y".equals(rec.passed)
                        && rec.unit.intValue() == theUnit) {
                    if (firstPassing == null || firstPassing.examDt.isAfter(rec.examDt)) {
                        firstPassing = rec;
                    }
                }
            }

            return firstPassing;
        }

        /**
         * Gets the user's first passing submission for a specified review exam.
         *
         * @param theUnit the unit
         * @return the earliest passing Unit exam in that unit
         */
        RawStexam getFirstPassingReviewExam(final int theUnit) {

            RawStexam firstPassing = null;

            for (final RawStexam rec : this.stExams) {
                if (RawStexam.REVIEW_EXAM.equals(rec.examType) && "Y".equals(rec.passed)
                        && rec.unit.intValue() == theUnit) {
                    if (firstPassing == null || firstPassing.examDt.isAfter(rec.examDt)) {
                        firstPassing = rec;
                    }
                }
            }

            return firstPassing;
        }

        /**
         * Counts the number of final exam attempts after a specified date.
         *
         * @param date the date
         * @return the number of attempts
         */
        int countFinalAttemptsAfter(final ChronoLocalDate date) {

            int count = 0;

            for (final RawStexam rec : this.stExams) {
                if (RawStexam.FINAL_EXAM.equals(rec.examType) && rec.examDt.isAfter(date)) {
                    final String passed = rec.passed;
                    // An attempt can be ignored by setting passed to something other than "Y" or "N"
                    if ("Y".equals(passed) || "N".equals(passed)) {
                        ++count;
                    }
                }
            }

            return count;
        }

        /**
         * Counts the number of failed attempts on the Unit exam in a unit since the most recent passing Review exam.
         *
         * @return the number of failed attempts
         */
        int countFailedExamSincePassingReview(final int unit) {

            LocalDate mostRecentPassingReview = null;
            for (final RawStexam exam : this.stExams) {
                if (exam.unit.intValue() == unit && "Y".equals(exam.passed)
                        && RawStexam.REVIEW_EXAM.equals(exam.examType)) {

                    if (mostRecentPassingReview == null || mostRecentPassingReview.isBefore(exam.examDt)) {
                        mostRecentPassingReview = exam.examDt;
                    }
                }
            }

            int count = 0;

            for (final RawStexam exam : this.stExams) {
                if (exam.unit.intValue() == unit && "N".equals(exam.passed)
                        && RawStexam.UNIT_EXAM.equals(exam.examType)) {

                    if (mostRecentPassingReview == null || mostRecentPassingReview.isBefore(exam.examDt)) {
                        ++count;
                    }
                }
            }

            return count;
        }

        /**
         * Counts the total number of unit exams submitted for a student in a unit, regardless of pass/fail status.
         *
         * @param unit the unit
         * @return the number of exams
         */
        int countUnitExams(final int unit) {

            int count = 0;

            for (final RawStexam exam : this.stExams) {
                if (exam.unit.intValue() == unit && RawStexam.UNIT_EXAM.equals(exam.examType)) {
                    ++count;
                }
            }

            return count;
        }

        /**
         * Counts the total number of final exams submitted for a student in a unit, regardless of pass/fail status.
         *
         * @return the number of exams
         */
        int countFinalExams() {

            int count = 0;

            for (final RawStexam exam : this.stExams) {
                if (exam.unit.intValue() == 5 && RawStexam.FINAL_EXAM.equals(exam.examType)) {
                    ++count;
                }
            }

            return count;
        }

        /**
         * Generates a diagnostic string representation of the object.
         *
         * @return the string representation
         */
        @Override
        public String toString() {

            return SimpleBuilder.concat("OldCourseWorkRecord{stHomeworks=", this.stHomeworks, ", stExams=",
                    this.stExams, "}");
        }
    }
}
