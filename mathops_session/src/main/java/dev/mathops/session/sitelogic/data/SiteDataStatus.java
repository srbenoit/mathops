package dev.mathops.session.sitelogic.data;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.EExamStructure;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawCusectionLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawPacingRulesLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawStcuobjectiveLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStcuobjective;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A container for the score-oriented data relating to a {@code SiteData} object. All arrays in this object are indexes
 * as for the student course records in {@code CourseSiteDataRegistration}.
 */
public final class SiteDataStatus {

    /** Student IDs that do not receive hold notifications. */
    private static final List<String> NO_HOLDS_STUDENT_IDS = Arrays.asList("GUEST", "AACTUTOR", "ETEXT");

    /** The data object that owns this object. */
    private final SiteData owner;

    /** The student's status in each course, keyed on course ID. */
    private final Map<String, SiteDataCfgCourseStatus> courseStatus;

    /** The student's status in each exam, keyed on course ID, unit, exam type. */
    private final Map<String, Map<Integer, Map<String, SiteDataCfgExamStatus>>> examStatus;

    /** The student's status in each homework, keyed on course ID, unit, objective, hw type. */
    private final Map<String, Map<Integer, Map<Integer, Map<String, SiteDataCfgHwStatus>>>> hwStatus;

    /**
     * Constructs a new {@code SiteDataStatus}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataStatus(final SiteData theOwner) {

        this.owner = theOwner;

        this.courseStatus = new TreeMap<>();
        this.examStatus = new TreeMap<>();
        this.hwStatus = new TreeMap<>();
    }

    /**
     * Gets the student's status in a course.
     *
     * @param courseId the course ID
     * @return the course status container
     */
    public SiteDataCfgCourseStatus getCourseStatus(final String courseId) {

        return this.courseStatus.get(courseId);
    }

    /**
     * Gets the student's status in an exam.
     *
     * @param courseId the course ID
     * @param unit     the unit
     * @param type     the exam type
     * @return the exam status container
     */
    public SiteDataCfgExamStatus getExamStatus(final String courseId, final Integer unit, final String type) {

        final SiteDataCfgExamStatus result;

        final Map<Integer, Map<String, SiteDataCfgExamStatus>> map1 = this.examStatus.get(courseId);

        if (map1 == null) {
            result = null;
        } else {
            final Map<String, SiteDataCfgExamStatus> map2 = map1.get(unit);
            result = map2 == null ? null : map2.get(type);
        }

        return result;
    }

    /**
     * Queries all database data relevant to a session's effective user ID within the session's context.
     * <p>
     * At the time this method is called; the {@code SiteData} object will have loaded the active term, all calendar
     * records, all pace track rules, the {@code SiteDataContext} object, the {@code SiteDataStudent} object, the
     * {@code SuteDataProfile} object, and the {@code SiteDateRegistration} object, the {@code SiteDateCourse} object,
     * and the {@code SiteDataActivity} object.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    boolean loadData(final Cache cache) throws SQLException {

        final List<RawStcourse> regs = this.owner.registrationData.getRegistrations();

        for (final RawStcourse reg : regs) {
            if ("G".equals(reg.openStatus)) {
                continue;
            }

            final String courseId = reg.course;
            final String sectionNum = reg.sect;
            final SiteDataCfgCourse courseCfg = this.owner.courseData.getCourse(courseId, sectionNum);
            final RawStudent stu = this.owner.studentData.getStudent();

            final SiteDataCfgCourseStatus stat = new SiteDataCfgCourseStatus();
            this.courseStatus.put(courseId, stat);

            // TODO: Populate "blocked" field

            // Run various eligibility tests that affect the entire course
            courseEligByHolds(stu, reg, courseCfg, stat);
            courseEligByTermDates(cache, stat);
            courseEligByInstrType(reg, stat);
            courseEligByOpenStatus(reg, stat);
            courseEligByLicensed(courseCfg, stat);
            courseEligBySectDates(courseCfg, stat);
            courseEligByIncomplete(reg, stat);

            // Gather status for each exam in the course/unit
            final Map<Integer, Map<String, SiteDataCfgExamStatus>> examMap = new TreeMap<>();
            this.examStatus.put(courseId, examMap);

            // Gather status for each homework in the course/unit
            final Map<Integer, Map<Integer, Map<String, SiteDataCfgHwStatus>>> hwMap = new TreeMap<>();
            this.hwStatus.put(courseId, hwMap);

            final Integer[] units = this.owner.courseData.getUnitsForCourse(courseId);
            for (final Integer unit : units) {

                final Map<String, SiteDataCfgExamStatus> examUnitMap = new TreeMap<>();
                examMap.put(unit, examUnitMap);

                populateExamUnitMap(cache, courseId, reg.sect, unit, examUnitMap);

                final Map<Integer, Map<String, SiteDataCfgHwStatus>> hwUnitMap = new TreeMap<>();
                hwMap.put(unit, hwUnitMap);

                final Integer[] objectives = this.owner.courseData.getObjectivesForUnit(courseId, unit);

                if (objectives != null) {
                    for (final Integer objective : objectives) {
                        final Map<String, SiteDataCfgHwStatus> hwUnitObjectiveMap = new TreeMap<>();
                        hwUnitMap.put(objective, hwUnitObjectiveMap);

                        populateHwUnitObjectiveMap(cache, reg, unit, objective, hwUnitObjectiveMap);
                    }
                }
            }

            // Calculate course status based on exams and homeworks
            double totalScore = 0.0;

            final Map<Integer, Map<String, SiteDataCfgExamStatus>> exam1 = this.examStatus.get(courseId);
            if (exam1 != null) {
                for (final Map<String, SiteDataCfgExamStatus> exam2 : exam1.values()) {
                    for (final SiteDataCfgExamStatus exam : exam2.values()) {
                        totalScore = totalScore + (double) exam.countedScore + (double) exam.onTimePoints;
                    }
                }
            }

            stat.totalScore = (int) Math.round(totalScore);
        }

        return true;
    }

    /**
     * Populates the map from exam type to {@code SiteDataCfgExamStatus} for each exam type in a given course unit.
     *
     * @param cache       the data cache
     * @param courseId    the course ID
     * @param sectionNum  the section number
     * @param unit        the unit number
     * @param examUnitMap the exam unit map
     * @throws SQLException if there is an error accessing the database
     */
    private void populateExamUnitMap(final Cache cache, final String courseId,
                                     final String sectionNum, final Integer unit,
                                     final Map<? super String, ? super SiteDataCfgExamStatus> examUnitMap)
            throws SQLException {

        final SiteDataCourse courseData = this.owner.courseData;
        final SiteDataCfgCourse courseCfg = courseData.getCourse(courseId, sectionNum);
        final SiteDataCfgUnit unitCfg = courseData.getCourseUnit(courseId, unit);

        // Get the exam structure
        final EExamStructure examStruct = RawCsectionLogic.getExamStructure(courseCfg.courseSection);

        // Get the exams, then scan for the ones that apply to the student's exam structure
        if (unitCfg != null) {
            final List<RawExam> unitExams = unitCfg.getExams();
            for (final RawExam exam : unitExams) {
                final String examType = exam.examType;
                final boolean examApplies;

                if ("Q".equals(examType)) {
                    examApplies = true;
                } else if (examStruct == EExamStructure.UNIT_FINAL) {
                    examApplies = "R".equals(examType) || "U".equals(examType) || "F".equals(examType);
                } else if (examStruct == EExamStructure.UNIT_ONLY) {
                    examApplies = "R".equals(examType) || "U".equals(examType);
                } else if (examStruct == EExamStructure.FINAL_ONLY) {
                    examApplies = "R".equals(examType) || "F".equals(examType);
                } else {
                    examApplies = false;
                }

                if (examApplies) {
                    final SiteDataCfgExamStatus stat = makeExamStatus(cache, courseId, sectionNum, unit, examType,
                            exam);
                    examUnitMap.put(examType, stat);
                }
            }
        }
    }

    /**
     * Builds a {@code SiteDataCfgExamStatus} object for a particular course, section, unit, and exam.
     *
     * @param cache      the data cache
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @param unit       the unit
     * @param examType   the exam type
     * @param exam       the exam model (CExam)
     * @return the constructed {@code SiteDataCfgExamStatus}
     * @throws SQLException if there is an error accessing the database
     */
    private SiteDataCfgExamStatus makeExamStatus(final Cache cache, final String courseId, final String sectionNum,
                                                 final Integer unit, final String examType, final RawExam exam)
            throws SQLException {

        final SiteDataCourse courseData = this.owner.courseData;
        final SiteDataCfgUnit unitCfg = courseData.getCourseUnit(courseId, unit);
        final SiteDataActivity activity = this.owner.activityData;

        final SiteDataCfgExamStatus stat = new SiteDataCfgExamStatus();

        // Determine the highest possible score and mastery score based on exam type
        final RawCusection cusect = unitCfg.courseSectionUnit;

        if ("Q".equals(examType)) {
            // Extended skills review exams are configured as Qualifying

            // FIXME: Make sure 17ELM is recorded as a review exam and not a Q exam
            if ("17ELM".equals(exam.version) || "7TELM".equals(exam.version)) {

                // Double scores for the extended exams
                stat.highestPossibleScore = 2 * cusect.reMasteryScore.intValue();
                stat.masteryScore = 2 * cusect.reMasteryScore.intValue();

                stat.highestPossibleScore = RawCusectionLogic.masteryToPossible(cusect.reMasteryScore).intValue();
                stat.masteryScore = cusect.reMasteryScore.intValue();
            }

        } else if ("R".equals(examType)) {
            stat.highestPossibleScore = RawCusectionLogic.masteryToPossible(cusect.reMasteryScore).intValue();
            stat.masteryScore = cusect.reMasteryScore.intValue();
        } else if ("U".equals(examType) || "F".equals(examType)) {
            stat.highestPossibleScore = RawCusectionLogic.masteryToPossible(cusect.ueMasteryScore).intValue();
            stat.masteryScore = cusect.ueMasteryScore.intValue();
        }

        // Get the student's exam record
        final List<RawStexam> stuExams = activity.getStudentExams(courseId, unit);

        // Populate first passing date/time, and accumulate the highest raw and highest passing scores
        int maxRaw = 0;
        int maxPass = 0;
        boolean synth = false;
        for (final RawStexam stexam : stuExams) {
            if (!stexam.version.equals(exam.version)) {
                continue;
            }
            final Integer score = stexam.examScore;

            if (score != null) {
                if ("G".equals(stexam.passed)) {
                    continue;
                }
                maxRaw = Math.max(maxRaw, score.intValue());
                if ("Y".equals(stexam.passed)) {
                    if (score.intValue() >= maxPass) {
                        maxPass = score.intValue();
                        synth = "SY".equals(stexam.examSource);
                    }

                    final LocalDate fin = stexam.examDt;
                    if ("Y".equals(stexam.isFirstPassed)
                            || (stat.firstPassingDate == null || stat.firstPassingDate.isAfter(fin))) {
                        stat.firstPassingDate = fin;
                    }
                }
            }
        }

        stat.highestRawScore = maxRaw;
        stat.highestPassingScore = maxPass;
        stat.synthetic = synth;

        final SiteDataRegistration regData = this.owner.registrationData;
        final RawStcourse reg = regData.getRegistration(courseId, sectionNum);
        final SiteDataCfgCourse courseCfg = courseData.getCourse(courseId, sectionNum);

        // Determine the counted points from unit exam score
        if (maxPass > 0 && ("U".equals(examType) || "F".equals(examType))) {

            int counted = maxPass;

            // If a zero unit is assigned, for a final exam, half of earned points are taken away,
            // and for all other exams, "mastery" points are taken away
            if (unit.equals(reg.zeroUnit)) {
                if ("F".equals(examType)) {
                    counted /= 2;
                } else {
                    counted = Math.max(0, counted - stat.masteryScore);
                }
            }

            stat.countedScore = counted;
        }

        // Determine all deadlines, if we have a pace order set, and see whether the student has
        // earned a "U2" deadline extension benefit.
        final Integer paceOrder = reg.paceOrder;

        if (paceOrder == null) {
            // Could be just a non-counted Incomplete

            if ("Y".equals(reg.iInProgress) && !"Y".equals(reg.iCounted) && reg.iTermKey != null) {

                final TermKey key = reg.iTermKey;

                RawStterm effStterm = null;
                final List<RawStterm> stterms = RawSttermLogic.queryByStudent(cache, reg.stuId);
                for (final RawStterm test : stterms) {
                    if (key.equals(test.termKey)) {
                        effStterm = test;
                        break;
                    }
                }

                if (effStterm != null && effStterm.pace != null && reg.paceOrder != null) {

                    final int pace = effStterm.pace.intValue();
                    final int order = reg.paceOrder.intValue();

                    final List<RawMilestone> iMilestones =
                            RawMilestoneLogic.getAllMilestones(cache, key, pace, effStterm.paceTrack);
                    final List<RawStmilestone> iStmilestones = RawStmilestoneLogic
                            .getStudentMilestones(cache, key, effStterm.paceTrack, reg.stuId);

                    for (final RawMilestone ms : iMilestones) {
                        final int num = ms.msNbr.intValue();
                        final int msCourse = num / 10 % 10;
                        final int msUnit = num % 10;

                        if (order != msCourse || unit.intValue() != msUnit) {
                            continue;
                        }

                        final String msType = ms.msType;

                        if ("R".equals(examType) && (RawMilestone.SKILLS_REVIEW.equals(msType)
                                || RawMilestone.UNIT_REVIEW_EXAM.equals(msType))) {
                            stat.deadlineDate = resolveDate(ms, iStmilestones);
                        } else if ("U".equals(examType) && RawMilestone.UNIT_EXAM.equals(msType)) {
                            stat.deadlineDate = resolveDate(ms, iStmilestones);
                        } else if ("F".equals(examType) && (RawMilestone.FINAL_EXAM.equals(msType)
                                || RawMilestone.FINAL_LAST_TRY.equals(msType))) {
                            // Replace final deadline with Incomplete deadline
                            stat.deadlineDate = reg.iDeadlineDt;
                        }
                    }
                }
            }

        } else {
            final TermKey key;

            if ("Y".equals(reg.iInProgress) && !"Y".equals(reg.iCounted)) {
                key = reg.iTermKey;
            } else {
                final TermRec term = TermLogic.get(cache).queryActive(cache);
                key = term.term;
            }

            final List<RawMilestone> milestones = this.owner.milestoneData.getMilestones(key);
            final List<RawStmilestone> stmilestones = this.owner.milestoneData.getStudentMilestones(key);

            // FIXME: If pace order is set on a course but there is another course with pace order
            // set, milestones are null but we get here...

            // All milestone records will already be for the correct pace track and pace
            for (final RawMilestone ms : milestones) {
                final int num = ms.msNbr.intValue();

                final int msCourse = num / 10 % 10;
                final int msUnit = num % 10;

                if (paceOrder.intValue() != msCourse || unit.intValue() != msUnit) {
                    continue;
                }

                final String msType = ms.msType;

                if ("R".equals(examType) && (RawMilestone.SKILLS_REVIEW.equals(msType)
                        || RawMilestone.UNIT_REVIEW_EXAM.equals(msType))) {
                    stat.deadlineDate = resolveDate(ms, stmilestones);
                } else if (("U".equals(examType) && RawMilestone.UNIT_EXAM.equals(msType))
                        || ("F".equals(examType) && RawMilestone.FINAL_EXAM.equals(msType))) {
                    stat.deadlineDate = resolveDate(ms, stmilestones);
                } else if ("F".equals(examType)
                        && RawMilestone.FINAL_LAST_TRY.equals(msType)) {
                    stat.lastTryDeadline = resolveDate(ms, stmilestones);
                    stat.lastTryAttemptsAllowed = resolveAttempts(ms, stmilestones);
                }
            }
        }

        // Now we need to determine whether the exam was passed on time or not. For the Final Exam,
        // we rely on the fact that we will be building units status records in order, so we can
        // test the prior unit's "U2" benefit to see if we get an extension on Final Exam deadline
        // dates.

        if (stat.firstPassingDate != null) {

            if ("F".equals(examType)) {
                if (stat.lastTryDeadline != null
                        && !stat.firstPassingDate.isAfter(stat.lastTryDeadline)) {
                    stat.passedOnTime = true;
                }
            } else if (stat.deadlineDate != null
                    && !stat.firstPassingDate.isAfter(stat.deadlineDate)) {
                stat.passedOnTime = true;
            }
        }

        // Store the points awarded for on-time passing of the exam
        if (("R".equals(examType) && stat.passedOnTime)
                && (cusect.rePointsOntime != null)) {
            stat.onTimePoints = cusect.rePointsOntime.intValue();
        }

        // Populate total attempts allowed and attempts allowed per passing review
        if ("U".equals(examType)) {
            if (cusect.nbrAtmptsAllow != null) {
                stat.totalAttemptsAllowed = cusect.nbrAtmptsAllow.intValue();
            }

            if (cusect.atmptsPerReview != null) {
                stat.attemptsPerPassingReview = cusect.atmptsPerReview.intValue();
            }
        }

        // Count up the total attempts so far, and for unit exams, the number of attempts since the
        // most recent passing review exam
        int total = 0;
        int since = 0;
        for (final RawStexam stexam : stuExams) {
            if ("G".equals(stexam.passed)) {
                continue;
            }

            if (stexam.version.equals(exam.version)) {
                ++total;
                ++since;
            }

            if ("Y".equals(stexam.passed)
                    && "R".equals(stexam.examType)) {
                since = 0;
            }
        }
        stat.totalAttemptsSoFar = total;
        stat.attemptsSinceLastPassingReview = since;

        final LocalDate deadline = stat.deadlineDate;

        // If the exam was not passed by the deadline, count the number of attempts after the
        // deadline date
        if (deadline != null) {
            final boolean notPassedByDeadline = (stat.firstPassingDate == null)
                    || stat.firstPassingDate.isAfter(deadline);

            if (notPassedByDeadline) {
                int lastTrySoFar = 0;
                for (final RawStexam stexam : stuExams) {
                    if (stexam.examType.equals(examType) && stexam.examDt.isAfter(deadline)) {
                        ++lastTrySoFar;
                    }
                }
                stat.lastTryAttemptsSoFar = lastTrySoFar;
            }
        }

        // Student is ineligible if a fatal hold exists on their account
        final String studentId = this.owner.studentData.getStudent().stuId;

        // Administrators, guest users and tutors need not test exam eligibility
        if ("GUEST".equals(studentId)
                || "AACTUTOR".equals(studentId)
                || "ETEXT".equals(studentId)) {
            stat.eligible = true;
        } else {
            examEligByPendingExam(cache, stat);
            examEligByTestWindow(exam, unitCfg, stat);
            examEligByRules(cache, courseId, unit, exam, courseCfg, stat);
        }

        return stat;
    }

    /**
     * Given a milestone record and a list of student milestone overrides, return the correct milestone date (the date
     * from the milestone record if no student milestone overrides, or the override date from the student milestone
     * record if one matches).
     *
     * @param milestone    the milestone whose date to retrieve
     * @param stmilestones the list of student milestone overrides
     * @return the correct date
     */
    private static LocalDate resolveDate(final RawMilestone milestone,
                                         final Iterable<RawStmilestone> stmilestones) {

        final Integer number = milestone.msNbr;
        final String type = milestone.msType;
        LocalDate date = milestone.msDate;

        for (final RawStmilestone stms : stmilestones) {
            if (stms.paceTrack.equals(milestone.paceTrack) && stms.msNbr.equals(number) && stms.msType.equals(type)) {
                date = stms.msDate;
            }
        }

        return date;
    }

    /**
     * Given a milestone record and a list of student milestone overrides, return the correct milestone number of
     * attempts allowed (the value from the milestone record if no student milestone overrides, or the override value
     * from the student milestone record if one matches).
     *
     * @param milestone    the milestone whose date to retrieve
     * @param stmilestones the list of student milestone overrides
     * @return the correct value
     */
    private static Integer resolveAttempts(final RawMilestone milestone,
                                           final Iterable<RawStmilestone> stmilestones) {

        final Integer number = milestone.msNbr;
        final String type = milestone.msType;
        Integer attempts = milestone.nbrAtmptsAllow;

        for (final RawStmilestone stms : stmilestones) {
            if (stms.paceTrack.equals(milestone.paceTrack) && stms.msNbr.equals(number) && stms.msType.equals(type)) {
                attempts = stms.nbrAtmptsAllow;
                break;
            }
        }

        return attempts;
    }

    /**
     * Populates the map from exam type to {@code SiteDataCfgExamStatus} for each exam type in a given course unit.
     *
     * @param cache              the data cache
     * @param reg                the student's registration record (could be synthetic)
     * @param unit               the unit number
     * @param objective          the objective number
     * @param hwUnitObjectiveMap the homework unit map
     * @throws SQLException if there is an error accessing the database
     */
    private void populateHwUnitObjectiveMap(final Cache cache, final RawStcourse reg,
                                            final Integer unit, final Integer objective,
                                            final Map<? super String, ? super SiteDataCfgHwStatus> hwUnitObjectiveMap)
            throws SQLException {

        final SiteDataCfgObjective objectiveCfg =
                this.owner.courseData.getCourseUnitObjective(reg.course, unit, objective);

        final List<AssignmentRec> homeworks = objectiveCfg.getHomeworks();

        for (final AssignmentRec hw : homeworks) {
            final SiteDataCfgHwStatus stat = makeHwStatus(cache, reg, unit, objective, hw);
            hwUnitObjectiveMap.put(hw.assignmentType, stat);
        }
    }

    /**
     * Builds a {@code SiteDataCfgHwStatus} object for a particular course, section, unit, objective, and homework.
     *
     * @param cache     the data cache
     * @param reg       the student's registration record (could be synthetic)
     * @param unit      the unit
     * @param objective the objective
     * @param homework  the homework model (CHomework)
     * @return the constructed {@code SiteDataCfgHwStatus}
     * @throws SQLException if there is an error accessing the database
     */
    private SiteDataCfgHwStatus makeHwStatus(final Cache cache, final RawStcourse reg, final Integer unit,
                                             final Integer objective, final AssignmentRec homework)
            throws SQLException {

        final SiteDataCfgHwStatus stat = new SiteDataCfgHwStatus();
        final SiteDataActivity actData = this.owner.activityData;
        final String courseId = reg.course;

        final String type = homework.assignmentType;
        final List<RawSthomework> sthws = actData.getStudentHomework(courseId, unit, objective);

        // See when the move-on and mastery scores were first achieved
        LocalDateTime earliestMoveon = null;
        LocalDateTime earliestMastered = null;
        int highestMoveon = 0;
        int highestMastery = 0;

        for (final RawSthomework sthw : sthws) {
            if (type.equals(sthw.hwType)) {

                final LocalDateTime when = sthw.getFinishDateTime();

                final int score = sthw.hwScore.intValue();

                highestMoveon = Math.max(score, highestMoveon);

                if (earliestMoveon == null || earliestMoveon.isAfter(when)) {
                    earliestMoveon = when;
                }

                if ("Y".equals(sthw.passed)) {
                    if (earliestMastered == null || earliestMastered.isAfter(when)) {
                        earliestMastered = when;
                    }

                    highestMastery = Math.max(score, highestMastery);
                }
            }
        }

        // Test homework eligibility
        stat.eligible = true;
        final LocalDate today = this.owner.now.toLocalDate();

        final TermRec active = TermLogic.get(cache).queryActive(cache);

        // Ineligible outside the current term
        final LocalDate termStart = active.startDate;
        if (today.isBefore(termStart)) {
            stat.eligible = false;
        } else {
            final LocalDate termEnd = active.endDate;

            if (today.isAfter(termEnd)) {
                stat.eligible = false;
            }
        }

        testHwEligibility(cache, reg, unit, objective, stat);

        return stat;
    }

    /**
     * Tests whether the student is eligible for a homework assignment.
     *
     * @param cache     the data cache
     * @param reg       the student's registration record (could be synthetic)
     * @param unit      the unit
     * @param objective the objective
     * @param stat      the {@code SiteDataCfgHwStatus} being populated
     * @throws SQLException if there is an error accessing the database
     */
    private void testHwEligibility(final Cache cache, final RawStcourse reg, final Integer unit,
                                   final Integer objective, final SiteDataCfgHwStatus stat) throws SQLException {

        if (stat.eligible) {
            final String courseId = reg.course;
            final String sectionNum = reg.sect;

            if (this.owner.courseData == null) {
                Log.warning("NO CUSECT (2) for ", courseId, " SECT ", sectionNum, " UNIT ", unit);
            } else {
                final SiteDataCfgCourse courseCfg = this.owner.courseData.getCourse(courseId, sectionNum);
                final RawPacingStructure pacingStructure = courseCfg.pacingStructure;

                if (pacingStructure == null) {
                    if (!"550".equals(sectionNum)) {
                        Log.warning("NO RULE SET for ", courseId, " SECT ", sectionNum, " UNIT ", unit);
                    }
                } else {
                    doTestHwEligibility(cache, reg, unit, objective, stat, pacingStructure);
                }
            }
        }
    }

    /**
     * Tests whether the student is eligible for a homework assignment.
     *
     * @param cache           the data cache
     * @param reg             the student's registration record (could be synthetic)
     * @param unit            the unit
     * @param objective       the objective
     * @param stat            the {@code SiteDataCfgHwStatus} being populated
     * @param pacingStructure the pacing structure under which the student is operating
     * @throws SQLException if there is an error accessing the database
     */
    private void doTestHwEligibility(final Cache cache, final RawStcourse reg, final Integer unit,
                                     final Integer objective, final SiteDataCfgHwStatus stat,
                                     final RawPacingStructure pacingStructure) throws SQLException {

        boolean reqLect = false;
        boolean reqHwComp = false;
        boolean reqHwMstr = false;
        boolean reqReComp = false;
        boolean reqReMstr = false;
        boolean reqUeComp = false;
        boolean reqUeMstr = false;

        final List<RawPacingRules> rsRules =
                RawPacingRulesLogic.queryByTermAndPacingStructure(cache,
                        TermLogic.get(cache).queryActive(cache).term, pacingStructure.pacingStructure);

        for (final RawPacingRules rule : rsRules) {
            if (RawPacingRulesLogic.ACTIVITY_HOMEWORK.equals(rule.activityType)) {
                final String req = rule.requirement;

                if (RawPacingRulesLogic.LECT_VIEWED.equals(req)) {
                    reqLect = true;
                } else if (RawPacingRulesLogic.HW_PASS.equals(req)) {
                    reqHwComp = true;
                } else if (RawPacingRulesLogic.HW_MSTR.equals(req)) {
                    reqHwMstr = true;
                } else if (RawPacingRulesLogic.UR_PASS.equals(req)) {
                    reqReComp = true;
                } else if (RawPacingRulesLogic.UR_MSTR.equals(req)) {
                    reqReMstr = true;
                } else if (RawPacingRulesLogic.UE_PASS.equals(req)) {
                    reqUeComp = true;
                } else if (RawPacingRulesLogic.UE_MSTR.equals(req)) {
                    reqUeMstr = true;
                }
            }
        }

        final String courseId = reg.course;
        final SiteDataActivity actData = this.owner.activityData;

        if (reqLect) {
            final RawStcuobjective stuLesson =
                    RawStcuobjectiveLogic.query(cache, reg.stuId, courseId, unit, objective);

            if (stuLesson == null || stuLesson.lectureViewedDt == null) {
                stat.eligible = false;
            }
        }

        final Integer priorUnit = Integer.valueOf(unit.intValue() - 1);
        final Integer priorObj = Integer.valueOf(objective.intValue() - 1);
        final SiteDataCfgUnit unitData = this.owner.courseData.getCourseUnit(courseId, priorUnit);

        if (stat.eligible && objective.intValue() > 1 && (reqHwComp || reqHwMstr)) {

            final List<RawSthomework> priorHw = actData.getStudentHomework(courseId, unit, priorObj);
            boolean needToComplete = true;
            boolean needToMaster = true;
            for (final RawSthomework hw : priorHw) {
                if ("Y".equals(hw.passed)) {
                    needToMaster = false;
                }
                needToComplete = false;
            }
            if ((reqHwMstr && needToMaster) || (reqHwComp && needToComplete)) {
                stat.eligible = false;
            }
        }

        if (stat.eligible && unit.intValue() > 0 && (reqReComp || reqReMstr)) {

            // If there is a Unit review exam for the prior unit, it must have been
            // completed/mastered
            RawExam reqRe = null;
            if (unitData != null) {
                final List<RawExam> exams = unitData.getExams();
                for (final RawExam exam : exams) {
                    if ("R".equals(exam.examType)) {
                        reqRe = exam;
                    }
                }
            }

            if (reqRe != null) {
                final List<RawStexam> priorExams = actData.getStudentExams(courseId, priorUnit);
                boolean needToComplete = true;
                boolean needToMaster = true;
                for (final RawStexam exam : priorExams) {
                    if (reqRe.version.equals(exam.version)) {
                        if ("Y".equals(exam.passed)) {
                            needToMaster = false;
                        }
                        needToComplete = false;
                    }
                }

                if ((reqReMstr && needToMaster) || (reqReComp && needToComplete)) {
                    stat.eligible = false;
                }
            }
        }

        if (stat.eligible && unit.intValue() > 0 && (reqUeComp || reqUeMstr)) {

            // Find the prior unit's Unit exam
            RawExam reqUE = null;
            if (unitData != null) {
                final List<RawExam> exams = unitData.getExams();
                for (final RawExam exam : exams) {
                    if ("U".equals(exam.examType)) {
                        reqUE = exam;
                    }
                }
            }

            if (reqUE != null) {
                final List<RawStexam> priorExams = actData.getStudentExams(courseId, priorUnit);
                boolean needToComplete = true;
                boolean needToMaster = true;
                for (final RawStexam exam : priorExams) {
                    if (reqUE.version.equals(exam.version)) {
                        if ("Y".equals(exam.passed)) {
                            needToMaster = false;
                        }
                        needToComplete = false;
                    }
                }

                if ((reqReMstr && needToMaster) || (reqReComp && needToComplete)) {
                    stat.eligible = false;
                }
            }
        }
    }

    /**
     * Tests whether the student is ineligible to work in a course due to holds.
     *
     * @param student   the student record, used to check hold severity
     * @param reg       the registration record (used to check pace order - a lockout hold allows work in non-paced
     *                  courses)
     * @param courseCfg the course configuration, used to check for practice mode or open access (which allow access
     *                  even in the presence of a hold)
     * @param stat      the status object being populated
     */
    private void courseEligByHolds(final RawStudent student, final RawStcourse reg,
                                   final SiteDataCfgCourse courseCfg, final SiteDataCfgStatusBase stat) {

        final String studentId = student.stuId;

        if (stat.eligible && (!NO_HOLDS_STUDENT_IDS.contains(studentId) && !courseCfg.practiceMode
                && !courseCfg.openAccess)) {

            if (this.owner.registrationData.isLockedOut()) {
                if (reg.paceOrder != null) {
                    stat.eligible = false;
                }
            } else if ("F".equals(student.sevAdminHold)) {
                stat.eligible = false;
            }
        }
    }

    /**
     * Tests whether the student is ineligible to work in a course because the current date is outside the term's
     * start-end date range.
     *
     * @param cache the data cache
     * @param stat  the status object being populated
     * @throws SQLException if there is an error accessing the database
     */
    private void courseEligByTermDates(final Cache cache, final SiteDataCfgStatusBase stat) throws SQLException {

        if (stat.eligible) {
            final TermRec active = TermLogic.get(cache).queryActive(cache);

            final LocalDate today = this.owner.now.toLocalDate();

            // Ineligible outside the current term
            final LocalDate termStart = active.startDate;
            if (today.isBefore(termStart)) {
                stat.eligible = false;
            } else {
                final LocalDate termEnd = active.endDate;

                if (today.isAfter(termEnd)) {
                    stat.eligible = false;
                }
            }
        }
    }

    /**
     * Tests whether the instruction type of the course registration is a type that does not allow work (like advance
     * placement).
     *
     * @param reg  the student registration
     * @param stat the status object being populated
     */
    private static void courseEligByInstrType(final RawStcourse reg, final SiteDataCfgStatusBase stat) {

        // FIXME remove once getRegistrationData omits AP credit records
        if (stat.eligible && "OT".equals(reg.instrnType)) {
            stat.eligible = false;
        }
    }

    /**
     * Tests whether the course is in the "open" state. If not, no work may be submitted in the course.
     *
     * @param reg  the student registration
     * @param stat the status object being populated
     */
    private static void courseEligByOpenStatus(final RawStcourse reg, final SiteDataCfgStatusBase stat) {

        // If the registration indicates "advance placement", not eligible
        if (stat.eligible && !"Y".equals(reg.openStatus)) {
            stat.eligible = false;
        }
    }

    /**
     * Tests whether the course section requires the student to be "licensed", and if so, makes the student ineligible
     * if they are not yet licensed.
     *
     * @param courseCfg the course configuration, used to test whether the course requires licensing but the student
     *                  is not yet licensed
     * @param stat      the status object being populated
     */
    private static void courseEligByLicensed(final SiteDataCfgCourse courseCfg, final SiteDataCfgStatusBase stat) {

        // If the registration indicates "advance placement", not eligible
        if (stat.eligible && courseCfg.mustTakeUsersExam) {
            stat.eligible = false;
        }
    }

    /**
     * Tests whether the current date falls within the start-end date range of the course section.
     *
     * @param courseCfg the course configuration, used to retrieve the section start and end dates
     * @param stat      the status object being populated
     */
    private void courseEligBySectDates(final SiteDataCfgCourse courseCfg,
                                       final SiteDataCfgStatusBase stat) {

        // Test the section start and end date
        if (stat.eligible) {
            final LocalDate today = this.owner.now.toLocalDate();
            if (courseCfg.courseSection == null) {
                stat.eligible = false;
            } else {
                final LocalDate start = courseCfg.courseSection.startDt;
                final LocalDate end = courseCfg.courseSection.ariesEndDt;

                if ((start != null && today.isBefore(start)) || (end != null && today.isAfter(end))) {
                    stat.eligible = false;
                }
            }
        }
    }

    /**
     * Tests whether the registration is an incomplete in progress - if not, but there are incompletes pending, then the
     * student is ineligible until the pending incompletes are completed (unless this is a synthetic registration). If
     * so, then the incomplete is tested for inclusion in the pace, in which case the existence of pending non-paced
     * incompletes also makes the student ineligible. Finally, if the incomplete deadline date is in the past, the
     * student is ineligible to work in an incomplete course.
     *
     * @param reg  the student registration
     * @param stat the status object being populated
     */
    private void courseEligByIncomplete(final RawStcourse reg, final SiteDataCfgStatusBase stat) {

        if (stat.eligible) {
            final LocalDate today = this.owner.now.toLocalDate();

            if ("Y".equals(reg.iInProgress)) {
                if ((this.owner.registrationData.isNonPacedIncompletePending()
                        && "Y".equals(reg.iCounted)) || (reg.iDeadlineDt == null)
                        || today.isAfter(reg.iDeadlineDt)) {
                    stat.eligible = false;
                }
            } else if (this.owner.registrationData.isNonPacedIncompletePending() && !reg.synthetic) {
                // This course is not incomplete, but there are incompletes pending
                stat.eligible = false;
            }
        }
    }

    /**
     * Tests whether there are open pending exam records for the student, which prevents them from starting any
     * proctored exams (does not apply to homework).
     *
     * @param cache the data cache
     * @param stat  the status object being populated
     * @throws SQLException if there is an error accessing the database
     */
    private void examEligByPendingExam(final Cache cache, final SiteDataCfgStatusBase stat) throws SQLException {

        if (stat.eligible) {
            final String studentId = this.owner.studentData.getStudent().stuId;

            final List<RawPendingExam> pendings = RawPendingExamLogic.queryByStudent(cache, studentId);

            if (!pendings.isEmpty()) {
                stat.eligible = false;
            }
        }
    }

    /**
     * Tests whether the testing window is open for a course and unit, or if not, if the "with coupon" window is still
     * open and the student has an unused homework coupon.
     *
     * @param exam    the exam being considered
     * @param unitCfg the unit configuration, used to retrieve the testing window
     * @param stat    the status object being populated
     */
    private void examEligByTestWindow(final RawExam exam, final SiteDataCfgUnit unitCfg,
                                      final SiteDataCfgStatusBase stat) {

        if (stat.eligible) {
            final String examType = exam.examType;
            final LocalDate today = this.owner.now.toLocalDate();

            if ("U".equals(examType) || "F".equals(examType)) {

                final RawCusection cusect = unitCfg.courseSectionUnit;

                // Proctored exams must honor test date and time windows
                if ((cusect.firstTestDt != null && today.isBefore(cusect.firstTestDt))
                        || (cusect.lastTestDt != null && today.isAfter(cusect.lastTestDt))) {
                    stat.eligible = false;
                }
            }
        }
    }

    /**
     * Tests whether the student is ineligible for an exam because prerequisites specified in the rule set rules have
     * not been satisfied.
     *
     * @param cache     the data cache
     * @param courseId  the ID of the course
     * @param unit      the unit
     * @param exam      the exam being considered
     * @param courseCfg the course configuration, used to retrieve the rule set
     * @param stat      the status object being populated
     * @throws SQLException if there is an error accessing the database
     */
    private void examEligByRules(final Cache cache, final String courseId, final Integer unit,
                                 final RawExam exam, final SiteDataCfgCourse courseCfg,
                                 final SiteDataCfgStatusBase stat)
            throws SQLException {

        if (stat.eligible) {

            // From the exam being considered, determine the target activity type
            final String targetActivity;
            final String examType = exam.examType;

            if ("R".equals(examType)) {
                if (Integer.valueOf(0).equals(exam.unit)) {
                    targetActivity = RawPacingRulesLogic.ACTIVITY_SR_EXAM;
                } else {
                    targetActivity = RawPacingRulesLogic.ACTIVITY_UNIT_REV_EXAM;
                }
            } else if ("U".equals(examType)) {
                targetActivity = RawPacingRulesLogic.ACTIVITY_UNIT_EXAM;
            } else if ("F".equals(examType)) {
                targetActivity = RawPacingRulesLogic.ACTIVITY_FINAL_EXAM;
            } else {
                targetActivity = null;
            }

            final RawPacingStructure pacingStructure = courseCfg.pacingStructure;
            if (targetActivity != null && pacingStructure != null) {

                // Find the set of prerequisites for the target activity
                final SiteDataCourse courseData = this.owner.courseData;

                final List<RawPacingRules> rsRules =
                        RawPacingRulesLogic.queryByTermAndPacingStructure(cache,
                                TermLogic.get(cache).queryActive(cache).term, pacingStructure.pacingStructure);

                boolean reqHwComp = false;
                boolean reqHwMstr = false;
                boolean reqReComp = false;
                boolean reqReMstr = false;
                boolean reqUeComp = false;
                boolean reqUeMstr = false;

                for (final RawPacingRules rule : rsRules) {
                    if (targetActivity.equals(rule.activityType)) {
                        final String req = rule.requirement;

                        if (RawPacingRulesLogic.HW_PASS.equals(req)) {
                            reqHwComp = true;
                        } else if (RawPacingRulesLogic.HW_MSTR.equals(req)) {
                            reqHwMstr = true;
                        } else if (RawPacingRulesLogic.UR_PASS.equals(req)) {
                            reqReComp = true;
                        } else if (RawPacingRulesLogic.UR_MSTR.equals(req)) {
                            reqReMstr = true;
                        } else if (RawPacingRulesLogic.UE_PASS.equals(req)) {
                            reqUeComp = true;
                        } else if (RawPacingRulesLogic.UE_MSTR.equals(req)) {
                            reqUeMstr = true;
                        }
                    }
                }

                final SiteDataActivity actData = this.owner.activityData;

                if (stat.eligible && (reqHwComp || reqHwMstr)) {
                    // See if all homework in the unit has been completed/mastered
                    final Integer[] objectives = courseData.getObjectivesForUnit(courseId, unit);
                    Arrays.sort(objectives);
                    boolean allComplete = true;
                    boolean allMastered = true;
                    Integer firstNotMastered = null;

                    for (final Integer objective : objectives) {
                        final List<AssignmentRec> homeworks =
                                courseData.getCourseUnitObjective(courseId, unit, objective).getHomeworks();
                        if (!homeworks.isEmpty()) {
                            final List<RawSthomework> stuHw = actData.getStudentHomework(courseId, unit, objective);
                            boolean needToComplete = true;
                            boolean needToMaster = true;
                            for (final RawSthomework hw : stuHw) {
                                needToComplete = false;
                                if ("Y".equals(hw.passed)) {
                                    needToMaster = false;
                                    break;
                                }
                            }
                            if (needToMaster) {
                                allMastered = false;
                                if (firstNotMastered == null) {
                                    firstNotMastered = objective;
                                }
                                // No break - still testing for all complete
                            }
                            if (needToComplete) {
                                allComplete = false;
                                // Not complete implies not mastered, so no need to keep looking
                                break;
                            }
                        }
                    }

                    if ((reqHwMstr && !allMastered) || (reqHwComp && !allComplete)) {
                        stat.eligible = false;
                    }
                }

                final SiteDataCfgUnit unitData = this.owner.courseData.getCourseUnit(courseId, unit);
                final Integer priorUnit = Integer.valueOf(unit.intValue() - 1);
                final SiteDataCfgUnit priorUnitData = this.owner.courseData.getCourseUnit(courseId, priorUnit);

                if (stat.eligible && (reqReComp || reqReMstr)) {

                    // If the exam in question is a unit review exam, this prereq refers to the
                    // prior unit's review exam. If it is a unit/midterm/final, this refers to
                    // the current term's unit review exam, if any.
                    List<RawExam> exams = null;
                    if ("R".equals(exam.examType)) {
                        if (unit.intValue() > 0 && priorUnitData != null) {
                            exams = priorUnitData.getExams();
                        }
                    } else if (unitData != null) {
                        exams = unitData.getExams();
                    }

                    // Given the list of exams for the unit in question (if any), find the
                    // prerequisite review exam
                    RawExam reqRe = null;
                    if (exams != null) {
                        for (final RawExam test : exams) {
                            if ("R".equals(test.examType)) {
                                reqRe = test;
                            }
                        }
                    }

                    if (reqRe != null) {
                        // If there is a prerequisite exam, see if it was completed/mastered
                        final List<RawStexam> priorExams = actData.getStudentExams(courseId, priorUnit);
                        boolean needToComplete = true;
                        boolean needToMaster = true;
                        for (final RawStexam test : priorExams) {
                            if (reqRe.version.equals(test.version)) {
                                if ("Y".equals(test.passed)) {
                                    needToMaster = false;
                                }
                                needToComplete = false;
                            }
                        }

                        if ((reqReMstr && needToMaster) || (reqReComp && needToComplete)) {
                            stat.eligible = false;
                        }
                    }
                }

                if (stat.eligible && unit.intValue() > 0 && (reqUeComp || reqUeMstr)) {

                    // Find the prior unit's Unit exam
                    RawExam reqUE = null;
                    if (unitData != null) {
                        final List<RawExam> exams = unitData.getExams();
                        for (final RawExam test : exams) {
                            if ("U".equals(test.examType)) {
                                reqUE = test;
                            }
                        }
                    }

                    if (reqUE != null) {
                        final List<RawStexam> priorExams = actData.getStudentExams(courseId, priorUnit);
                        boolean needToComplete = true;
                        boolean needToMaster = true;
                        for (final RawStexam test : priorExams) {
                            if (reqUE.version.equals(test.version)) {
                                if ("Y".equals(test.passed)) {
                                    needToMaster = false;
                                }
                                needToComplete = false;
                            }
                        }

                        if ((reqReMstr && needToMaster) || (reqReComp && needToComplete)) {
                            stat.eligible = false;
                        }
                    }
                }
            }
        }
    }
}
