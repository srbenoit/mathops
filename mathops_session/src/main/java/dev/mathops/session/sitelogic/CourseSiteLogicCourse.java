package dev.mathops.session.sitelogic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.session.sitelogic.data.SiteDataCourse;
import dev.mathops.session.sitelogic.data.SiteDataStudent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Logic and data access relating to the course information for courses in which the student is enrolled.
 * <p>
 * Web page code should be performing NO logic - just layout and formatting of the information gathered here.
 */
public final class CourseSiteLogicCourse {

    /** A course number. */
    private static final String M160R = "M 160R";

    /** A course number. */
    private static final String M161R = "M 161R";

    /** A course number. */
    private static final String GUEST = "GUEST";

    /** A course number. */
    private static final String AACTUTOR = "AACTUTOR";

    /** The set of guest student IDs. */
    private static final List<String> GUEST_IDS = Arrays.asList(GUEST, AACTUTOR);

    /** The owning site logic object. */
    private final CourseSiteLogic owner;

    /** The course site data. */
    private final SiteData data;

    /** The login session. */
    private final ImmutableSessionInfo session;

    /** The list of courses (not open yet) that the student may now start. */
    public final Set<CourseInfo> availableCourses;

    /** The list of courses that could be started if too many weren't already open. */
    public final Set<CourseInfo> unavailableCourses;

    /** The list of courses whose prerequisites have not yet been met. */
    public final Set<CourseInfo> noPrereqCourses;

    /** The list of courses that are currently open & not completed. */
    public final Set<CourseInfo> inProgressCourses;

    /** The list of courses that are completed, but remain open. */
    public final Set<CourseInfo> completedCourses;

    /** The list of courses that are not completed but past deadline date. */
    public final Set<CourseInfo> pastDeadlineCourses;

    /** The list of courses that are forfeit. */
    public final Set<CourseInfo> forfeitCourses;

    /** The list of courses that were failed. */
    public final Set<CourseInfo> notAvailableCourses;

    /** The list of courses where placement credit has been earned. */
    public final Set<CourseInfo> otCreditCourses;

    /** The list of incompletes (not open yet) that the student may now start. */
    public final Set<CourseInfo> availableIncCourses;

    /** The list of incompletes that could be started if too many weren't already open. */
    public final Set<CourseInfo> unavailableIncCourses;

    /** The list of incompletes whose prerequisites have not yet been met. */
    public final Set<CourseInfo> noPrereqIncCourses;

    /** The list of incompletes that are currently open & not completed. */
    public final Set<CourseInfo> inProgressIncCourses;

    /** The list of incompletes that are completed, but remain open. */
    public final Set<CourseInfo> completedIncCourses;

    /** The list of incompletes that are completed and past deadline date. */
    public final Set<CourseInfo> pastDeadlineIncCourses;

    /** The list of incompletes that are forfeit. */
    private final Set<CourseInfo> forfeitInc;

    /** The list of tutorials open to the student. */
    public final Set<CourseInfo> tutorials;

    /** Indicator that student has been locked out of courses by a fatal hold. */
    public boolean lockedOut;

    /** True if student has any course that requires an e-text. */
    public boolean requiresEText;

    /** Indicator that student is blocked from proceeding by deadline failures. */
    public boolean blocked = false;

    /** Labels for each course in the menu. */
    private final Map<String, String> courseLabels;

    /** The number of courses currently open. */
    private int numOpen;

    /** Flag indicating the user has an incomplete course that has not been opened. */
    private boolean incUnopened;

    /**
     * Constructs a new {@code CourseSiteLogicCourse}.
     *
     * @param cache      the data cache
     * @param theOwner   the owning course site logic
     * @param theData    the course site data from which to construct profile information
     * @param theSession the login session
     * @throws SQLException if there is an error accessing the database
     */
    CourseSiteLogicCourse(final Cache cache, final CourseSiteLogic theOwner, final SiteData theData,
                          final ImmutableSessionInfo theSession) throws SQLException {

        this.owner = theOwner;
        this.data = theData;
        this.session = theSession;

        this.availableCourses = new TreeSet<>();
        this.unavailableCourses = new TreeSet<>();
        this.noPrereqCourses = new TreeSet<>();
        this.inProgressCourses = new TreeSet<>();
        this.completedCourses = new TreeSet<>();
        this.pastDeadlineCourses = new TreeSet<>();
        this.forfeitCourses = new TreeSet<>();
        this.notAvailableCourses = new TreeSet<>();
        this.otCreditCourses = new TreeSet<>();

        this.availableIncCourses = new TreeSet<>();
        this.unavailableIncCourses = new TreeSet<>();
        this.noPrereqIncCourses = new TreeSet<>();
        this.inProgressIncCourses = new TreeSet<>();
        this.completedIncCourses = new TreeSet<>();
        this.pastDeadlineIncCourses = new TreeSet<>();
        this.forfeitInc = new TreeSet<>();

        this.tutorials = new TreeSet<>();
        this.lockedOut = false;
        this.numOpen = 0;
        this.requiresEText = false;
        this.incUnopened = false;

        this.courseLabels = new TreeMap<>();

        final ZonedDateTime now = ZonedDateTime.now();
        processData(cache, now);
    }

    /**
     * Populates data for all current courses for a student, once the active term has been loaded.
     *
     * @param cache the data cache
     * @param now   the current date/time
     * @throws SQLException if there is an error accessing the database
     */
    private void processData(final Cache cache, final ZonedDateTime now) throws SQLException {

        final SiteDataStudent stuData = this.data.studentData;

        this.lockedOut = stuData.hasHold("30");
        loadCourseLabels();

        final RawStudent student = stuData.getStudent();

        if (GUEST_IDS.contains(student.stuId)) {
            loadGuestData(student.stuId);
        } else {
            loadStudentData(cache, now);
        }
    }

    /**
     * Loads the map from course ID to course label.
     */
    private void loadCourseLabels() {

        this.courseLabels.put("M 116", "MATH 116");
        this.courseLabels.put("M 117", "MATH 117");
        this.courseLabels.put("M 118", "MATH 118");
        this.courseLabels.put("M 124", "MATH 124");
        this.courseLabels.put("M 125", "MATH 125");
        this.courseLabels.put("M 126", "MATH 126");

        this.courseLabels.put("MATH 116", "MATH 116");
        this.courseLabels.put("MATH 117", "MATH 117");
        this.courseLabels.put("MATH 118", "MATH 118");
        this.courseLabels.put("MATH 124", "MATH 124");
        this.courseLabels.put("MATH 125", "MATH 125");
        this.courseLabels.put("MATH 126", "MATH 126");

        this.courseLabels.put("M 100R", "Math Placement Review");
        this.courseLabels.put("M 100T", "ELM Tutorial");
        this.courseLabels.put("M 100P", "Math Placement Tool");
        this.courseLabels.put("M 100U", "User's Exam");
        this.courseLabels.put("M 170T", "Precalculus Tutorial");

        this.courseLabels.put("M 1170", "College Algebra I");
        this.courseLabels.put("M 1180", "College Algebra II");
        this.courseLabels.put("M 1240", "Functions");
        this.courseLabels.put("M 1250", "College Trig I");
        this.courseLabels.put("M 1260", "College Trig II");

        this.courseLabels.put("M 384", "MATH 384");
        this.courseLabels.put("M 495", "MATH 495");

        this.courseLabels.put("M 160R", "MATH 160 Review");
        this.courseLabels.put("M 161R", "MATH 161 Review");

        this.courseLabels.put("M 117C", "MATH 117 Challenge Exam");
        this.courseLabels.put("M 118C", "MATH 118 Challenge Exam");
        this.courseLabels.put("M 124C", "MATH 124 Challenge Exam");
        this.courseLabels.put("M 125C", "MATH 125 Challenge Exam");
        this.courseLabels.put("M 126C", "MATH 126 Challenge Exam");
    }

    /**
     * Loads the available course data for pre-configured guest IDs.
     *
     * @param studentId the student ID
     */
    private void loadGuestData(final String studentId) {

        // TODO: Make this data driven, and also context-specific

        if (GUEST.equals(studentId)) {
            final String lblm100t = this.courseLabels.get(RawRecordConstants.M100T);
            this.tutorials.add(new CourseInfo(RawRecordConstants.M100T, lblm100t));

            final String lblm117 = this.courseLabels.get(RawRecordConstants.M117);
            this.otCreditCourses.add(new CourseInfo(RawRecordConstants.M117, lblm117));

            final String lblm118 = this.courseLabels.get(RawRecordConstants.M118);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M118, lblm118));

            final String lblm124 = this.courseLabels.get(RawRecordConstants.M124);
            this.noPrereqCourses.add(new CourseInfo(RawRecordConstants.M124, lblm124));

            final String lblm125 = this.courseLabels.get(RawRecordConstants.M125);
            this.noPrereqCourses.add(new CourseInfo(RawRecordConstants.M125, lblm125));

            final String lblm126 = this.courseLabels.get(RawRecordConstants.M126);
            this.noPrereqCourses.add(new CourseInfo(RawRecordConstants.M126, lblm126));
        } else if (AACTUTOR.equals(studentId)) {
            final String lblm100t = this.courseLabels.get(RawRecordConstants.M100T);
            this.tutorials.add(new CourseInfo(RawRecordConstants.M100T, lblm100t));

            final String lblm117 = this.courseLabels.get(RawRecordConstants.M117);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M117, lblm117));

            final String lblm118 = this.courseLabels.get(RawRecordConstants.M118);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M118, lblm118));

            final String lblm124 = this.courseLabels.get(RawRecordConstants.M124);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M124, lblm124));

            final String lblm125 = this.courseLabels.get(RawRecordConstants.M125);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M125, lblm125));

            final String lblm126 = this.courseLabels.get(RawRecordConstants.M126);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M126, lblm126));
        }
    }

    /**
     * Loads the available course data for a genuine student.
     *
     * @param cache the data cache
     * @param now   the current date/time
     * @throws SQLException if there is an error accessing the database
     */
    private void loadStudentData(final Cache cache, final ZonedDateTime now) throws SQLException {

        if (this.session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            final String lblm117 = this.courseLabels.get(RawRecordConstants.M117);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M117, lblm117));

            final String lblm118 = this.courseLabels.get(RawRecordConstants.M118);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M118, lblm118));

            final String lblm124 = this.courseLabels.get(RawRecordConstants.M124);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M124, lblm124));

            final String lblm125 = this.courseLabels.get(RawRecordConstants.M125);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M125, lblm125));

            final String lblm126 = this.courseLabels.get(RawRecordConstants.M126);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.M126, lblm126));

//            final String lblmath117 = this.courseLabels.get(RawRecordConstants.MATH117);
//            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.MATH117, lblmath117));

//            final String lblmath118 = this.courseLabels.get(RawRecordConstants.MATH118);
//            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.MATH118, lblmath118));

//            final String lblmath124 = this.courseLabels.get(RawRecordConstants.MATH124);
//            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.MATH124, lblmath124));

            final String lblmath125 = this.courseLabels.get(RawRecordConstants.MATH125);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.MATH125, lblmath125));

            final String lblmath126 = this.courseLabels.get(RawRecordConstants.MATH126);
            this.inProgressCourses.add(new CourseInfo(RawRecordConstants.MATH126, lblmath126));
        } else {
            addSpecialStudentCourses();
            final List<RawStcourse> studentCourses = this.data.registrationData.getRegistrations();

            // Filter courses to only those this website supports
            final Collection<RawStcourse> stCoursesInContext = new ArrayList<>(10);
            for (final RawStcourse stcourse : studentCourses) {
                if (CourseSiteLogic.COURSE_IDS.contains(stcourse.course)) {
                    stCoursesInContext.add(stcourse);
                }
            }

            processCourses(cache, now, stCoursesInContext);
        }
    }

    /**
     * Checks for any special student configurations and adds the relevant courses to the list of courses currently in
     * progress.
     */
    private void addSpecialStudentCourses() {

        final SiteDataStudent stuData = this.data.studentData;
        final ZonedDateTime now = this.owner.sessionInfo.getNow();

        if (stuData.isSpecialType(now, "TUTOR", "M384", "ADMIN")) {

            final String[] toAdd = {RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                    RawRecordConstants.M125, RawRecordConstants.M126, RawRecordConstants.MATH125,
                    RawRecordConstants.MATH126};

            for (final String s : toAdd) {
                if (this.courseLabels.containsKey(s)) {
                    this.inProgressCourses.add(new CourseInfo(s, this.courseLabels.get(s)));
                }
            }
        }
    }

    /**
     * Processes the list of student course registrations and constructs the appropriate lists for each category.
     *
     * @param cache              the data cache
     * @param now                the current date/time
     * @param stCoursesInContext an array of {@code RawStcourse}, representing student course registrations active in
     *                           the current context
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean processCourses(final Cache cache, final ZonedDateTime now,
                                   final Iterable<RawStcourse> stCoursesInContext) throws SQLException {

        // Determine the student's pace (assume pace order is correct - may be wrong, but it will not report a
        // false-positive blocked result this way, since there should be no more records with a pace order set than the
        // student's actual pace).
        final int pace = PaceTrackLogic.determinePace(stCoursesInContext);

        // TODO: This is the place to store the STTERM info for pace and pace track, but we need to calculate pace track
        // from the pace track rules. If the track is changed from the default, we may need to re-query milestones!

        final SiteDataCourse courseData = this.data.courseData;

        // Count up the open courses based strictly on open status and the "counts toward max open"
        // field in the section record
        this.numOpen = 0;
        for (final RawStcourse stcourse : stCoursesInContext) {

            final String openStatus = stcourse.openStatus;

            if ("Y".equals(openStatus) && !"Y".equals(stcourse.completed)) {

                final SiteDataCfgCourse cfg = courseData.getCourse(stcourse.course, stcourse.sect);

                if (cfg != null) {
                    final RawCsection sect = cfg.courseSection;
                    if ("Y".equals(sect.countInMaxCourses)) {
                        ++this.numOpen;
                    }
                }
            }
        }

        final TermRec active = cache.getSystemData().getActiveTerm();

        // Load the OT credit courses that occurred this term
        final List<RawStcourse> otCredit = this.data.studentData.getStudentOTCredit();
        for (final RawStcourse credit : otCredit) {
            if (credit.termKey.equals(active.term) && this.courseLabels.containsKey(credit.course)) {
                this.otCreditCourses.add(new CourseInfo(credit.course, this.courseLabels.get(credit.course)));
            }
        }

        this.requiresEText = false;
        outer:
        for (final RawStcourse stcourse : stCoursesInContext) {

            final TermRec regTerm = this.data.registrationData.getRegistrationTerm(stcourse.course, stcourse.sect);
            final List<RawMilestone> allMilestones = this.data.milestoneData.getMilestones(regTerm.term);
            final List<RawStmilestone> stuMilestones = this.data.milestoneData.getStudentMilestones(regTerm.term);

            final SiteDataCfgCourse cfg = courseData.getCourse(stcourse.course, stcourse.sect);
            if (cfg == null) {
                Log.warning("No config found for ", stcourse.course, " section ", stcourse.sect);
                continue;
            }
            if ("Y".equals(cfg.course.requireEtext)) {
                this.requiresEText = true;
            }

            final RawCsection sect = cfg.courseSection;
            if (("OT".equals(sect.instrnType)) || "Y".equals(cfg.course.isTutorial)) {
                continue;
            }

            final String courseId = stcourse.course;
            final Integer paceOrder = stcourse.paceOrder;
            LocalDate paceDeadlineDay = null;
            LocalDate lastTryDeadlineDay = null;
            int lastTryAttempts = -1;
            final LocalDate today = this.owner.sessionInfo.getNow().toLocalDate();

            if (paceOrder != null) {
                for (final RawMilestone msRec : allMilestones) {

                    final int msNumber = msRec.msNbr.intValue();
                    final int msPace = msNumber < 1000 ? msNumber / 100 : msNumber / 1000;
                    final int msWhich = msNumber < 1000 ? msNumber / 10 % 10 : msNumber / 100 % 10;

                    if (msPace != pace || msWhich != paceOrder.intValue()) {
                        continue;
                    }

                    final String msType = msRec.msType;

                    if (paceDeadlineDay == null) {
                        paceDeadlineDay = msRec.msDate;
                    } else if ("FE".equals(msType)) {
                        if (msRec.msDate.isAfter(paceDeadlineDay)) {
                            paceDeadlineDay = msRec.msDate;
                        }

                        // See if date is overridden
                        for (final RawStmilestone stuMs : stuMilestones) {
                            if (stuMs.msNbr.equals(msRec.msNbr) && stuMs.msType.equals(msRec.msType)) {
                                paceDeadlineDay = stuMs.msDate;
                            }
                        }
                    } else if ("F1".equals(msType)) {
                        if (lastTryDeadlineDay == null || msRec.msDate.isAfter(paceDeadlineDay)) {
                            lastTryDeadlineDay = msRec.msDate;
                        }

                        lastTryAttempts = msRec.nbrAtmptsAllow == null ? 1 : msRec.nbrAtmptsAllow.intValue();

                        // See if date is overridden
                        for (final RawStmilestone stuMs : stuMilestones) {
                            if (stuMs.msNbr.equals(msRec.msNbr) && stuMs.msType.equals(msRec.msType)) {
                                lastTryDeadlineDay = stuMs.msDate;
                                lastTryAttempts = stuMs.nbrAtmptsAllow == null ? 1 : stuMs.nbrAtmptsAllow.intValue();
                            }
                        }
                    }
                }
            }

            boolean hasTut = false;
            for (final CourseInfo test : this.tutorials) {
                if (test.course.equals(courseId)) {
                    hasTut = true;
                    break;
                }
            }

            if (hasTut || checkForIncomplete(cache, paceDeadlineDay, stcourse)) {
                continue;
            }

            // If we get here, course is NOT an incomplete
            final String openStatus = stcourse.openStatus;
            final String prereq = stcourse.prereqSatis;

            if ("G".equals(openStatus) && this.courseLabels.containsKey(courseId)) {
                // Forfeit courses do NOT count toward pace (this is point of forfeiting)
                this.forfeitCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                continue;
            }

            if ("N".equals(openStatus) && this.courseLabels.containsKey(courseId)) {
                // Unopened courses do NOT count toward pace
                this.notAvailableCourses
                        .add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                continue;
            }

            if ("Y".equals(stcourse.completed) && this.courseLabels.containsKey(courseId)) {
                this.completedCourses
                        .add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                continue;
            }

            if (paceDeadlineDay != null) {
                // See if the student has passed the final, which leaves the course in the
                // "in progress" mode, even if we're past the final exam deadline date
                final Collection<RawStexam> passedFinals = new ArrayList<>(3);
                final List<RawStexam> allExams = this.data.activityData.getStudentExams(courseId);
                for (final RawStexam exam : allExams) {
                    if ("F".equals(exam.examType) && "Y".equals(exam.passed)) {
                        passedFinals.add(exam);
                    }
                }

                if (passedFinals.isEmpty()) {
                    // If we're beyond the last deadline date, shut off the course...
                    if (lastTryDeadlineDay == null) {
                        if (paceDeadlineDay.isBefore(today) && this.courseLabels.containsKey(courseId)) {
                            this.pastDeadlineCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                            continue;
                        }
                    } else if (lastTryDeadlineDay.isBefore(today) && paceDeadlineDay.isBefore(today)
                            && this.courseLabels.containsKey(courseId)) {
                        this.pastDeadlineCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                        continue;
                    } else if (paceDeadlineDay.isBefore(today)) {
                        // If there are at least N failed finals after the paceDeadlineDay, STOP
                        final Collection<RawStexam> failedFinals = new ArrayList<>(3);
                        final Collection<RawStexam> passedUnit4 = new ArrayList<>(3);
                        final List<RawStexam> allExams2 = this.data.activityData.getStudentExams(courseId);
                        for (final RawStexam exam : allExams2) {
                            if ("F".equals(exam.examType) && "N".equals(exam.passed)) {
                                failedFinals.add(exam);
                            }
                            if ("U".equals(exam.examType) && exam.unit.intValue() == 4 && "Y".equals(exam.passed)) {
                                passedUnit4.add(exam);
                            }
                        }

                        // If student did not have a passing Unit 4 exam on the pace deadline
                        // they cannot use the last try deadline day
                        if (passedUnit4.isEmpty() && this.courseLabels.containsKey(courseId)) {
                            this.pastDeadlineCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                            continue;
                        }

                        int attemptsUsed = 0;
                        for (final RawStexam failedFinal : failedFinals) {
                            final LocalDate failedOnDay = failedFinal.examDt;

                            if (failedOnDay.isAfter(paceDeadlineDay)) {
                                ++attemptsUsed;
                                if (attemptsUsed >= lastTryAttempts && this.courseLabels.containsKey(courseId)) {
                                    this.pastDeadlineCourses.add(new CourseInfo(courseId,
                                            this.courseLabels.get(courseId)));
                                    continue outer;
                                }
                            }
                        }
                    }
                }
            }

            if ("Y".equals(openStatus) && ("Y".equals(prereq) || "P".equals(prereq))
                    && this.courseLabels.containsKey(courseId)) {
                this.inProgressCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
            }
        }

        final RawPacingStructure ruleSet = getPacingStructure(cache, now, stCoursesInContext);
        final boolean ok = ruleSet != null;

        if (ok) {
            categorizeCourses(cache, stCoursesInContext, ruleSet);
            testForBlocked();
        }

        return ok;
    }

    /**
     * Tests whether a particular course registration is an incomplete from a prior term, and if so, categorizes that
     * registration appropriately.
     *
     * @param cache           the data cache
     * @param paceDeadlineDay a deadline day for the incomplete if it is participating in pace
     * @param studentCourse   a {@code RawStcourse} representing the student course registration
     * @return {@code true} if the course was found to be an incomplete; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkForIncomplete(final Cache cache, final LocalDate paceDeadlineDay,
                                       final RawStcourse studentCourse) throws SQLException {

        final boolean isIncomplete = studentCourse.iDeadlineDt != null;

        if (isIncomplete) {
            final LocalDate today = this.owner.sessionInfo.getNow().toLocalDate();

            final LocalDate dline;

            if ("N".equals(studentCourse.iCounted)) {
                dline = studentCourse.iDeadlineDt;
            } else {
                dline = paceDeadlineDay == null ? studentCourse.iDeadlineDt : paceDeadlineDay;
            }

            final String courseId = studentCourse.course;
            if (this.courseLabels.containsKey(courseId)) {

                if ("Y".equals(studentCourse.completed)) {
                    this.completedIncCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                } else if (dline.isBefore(today)) {
                    this.pastDeadlineIncCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                } else if ("G".equals(studentCourse.openStatus)) {
                    this.forfeitInc.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                } else if ("Y".equals(studentCourse.openStatus)) {

                    this.inProgressIncCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                    final SiteDataCfgCourse cfg =
                            this.data.courseData.getCourse(studentCourse.course, studentCourse.sect);
                    if (cfg != null) {
                        final RawCsection courseSect = cfg.courseSection;
                        if ("Y".equals(courseSect.countInMaxCourses)) {
                            ++this.numOpen;
                        }
                    }
                } else if ("Y".equals(studentCourse.prereqSatis)
                        || "P".equals(studentCourse.prereqSatis)) {
                    this.incUnopened = true;
                } else if (checkPrerequisites(cache, studentCourse)) {
                    this.availableIncCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                    this.incUnopened = true;
                } else {
                    this.noPrereqIncCourses.add(new CourseInfo(courseId, this.courseLabels.get(courseId)));
                }
            }
        }

        return isIncomplete;
    }

    /**
     * Tests the prerequisite on a course registration which was not already flagged as having its prerequisites
     * satisfied.
     *
     * @param cache         the data cache
     * @param studentCourse a {@code RawStcourse} representing the student course registration
     * @return {@code true} if prerequisites were satisfied; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkPrerequisites(final Cache cache, final RawStcourse studentCourse)
            throws SQLException {

        final List<String> prereqs = this.data.registrationData.getPrerequisites(studentCourse.course);
        final int numPrereq = prereqs.size();

        boolean prereq = numPrereq == 0;
        for (int j = 0; !prereq && j < numPrereq; ++j) {
            prereq = hasCourseAsPrereq(prereqs.get(j));
        }

        if (prereq && RawStcourseLogic.updatePrereqSatisfied(cache, studentCourse.stuId,
                studentCourse.course, studentCourse.sect, studentCourse.termKey, "Y")) {

            studentCourse.prereqSatis = "Y";
        }

        return prereq;
    }

    /**
     * Checks whether the student has "credit" for a course from the perspective of testing prerequisites.
     *
     * @param courseId the course to test
     * @return true if student has the course
     */
    private boolean hasCourseAsPrereq(final String courseId) {

        boolean hasCourse = false;

        // See if student has completed the course at any time in the past
        final List<RawStcourse> complete = this.data.registrationData.getAllCompletedCourses();
        if (complete != null) {
            for (final RawStcourse test : complete) {
                if (courseId.equals(test.course)) {
                    hasCourse = true;
                    break;
                }
            }
        }

        if (!hasCourse) {
            // See if there is a placement result satisfying prerequisite
            final List<RawMpeCredit> placeCred = this.data.studentData.getStudentPlacementCredit();

            for (final RawMpeCredit test : placeCred) {
                if (courseId.equals(test.course)) {
                    hasCourse = true;
                    break;
                }
            }
        }

        if (!hasCourse) {
            // See if there are OT credits satisfying the prerequisite
            final List<RawStcourse> otCredit = this.data.studentData.getStudentOTCredit();

            for (final RawStcourse test : otCredit) {
                if (courseId.equals(test.course)) {
                    hasCourse = true;
                    break;
                }
            }
        }

        if (!hasCourse) {
            // See if there are transfer credits satisfying the prerequisite
            final List<RawFfrTrns> trans = this.data.registrationData.getTransferCredit();
            if (trans != null) {
                for (final RawFfrTrns test : trans) {
                    if (courseId.equals(test.course)) {
                        hasCourse = true;
                        break;
                    }
                }
            }
        }

        return hasCourse;
    }

    /**
     * Determines the pacing structure under which the student is operating. Within a particular context, a student may
     * operate under only one pacing structure in a term. In different contexts, a student may operate under different
     * pacing structures.
     *
     * @param cache          the data cache
     * @param now            the current date/time
     * @param studentCourses an array of models of type {@code CRawStcourse} representing all student course
     *                       registrations
     * @return the pacing structure appropriate for the courses available in this context; {@code null} if no such
     *         pacing structure exists
     * @throws SQLException if there is an error accessing the database
     */
    private RawPacingStructure getPacingStructure(final Cache cache, final ZonedDateTime now,
                                                  final Iterable<RawStcourse> studentCourses) throws SQLException {

        final SiteDataStudent stuData = this.data.studentData;
        final SiteDataCourse courseData = this.data.courseData;

        final boolean isTutor = stuData.isSpecialType(now, "TUTOR", "M384", "ADMIN");

        // Find the pacing structure ID for the student's course registrations, and verify that
        // there is only one
        String pacingStructure = null;

        if (isTutor) {
            pacingStructure = RawPacingStructure.DEF_PACING_STRUCTURE;
        } else {
            final Collection<String> pacingStructures = new HashSet<>(4);

            for (final RawStcourse stcourse : studentCourses) {
                if (stcourse == null) {
                    continue;
                }

                // Pacing structure is not relevant for a non-counted Incomplete
                if ("Y".equals(stcourse.iInProgress) && "N".equals(stcourse.iCounted)) {
                    continue;
                }

                final SiteDataCfgCourse cfg = courseData.getCourse(stcourse.course, stcourse.sect);
                if ((cfg == null) || "Y".equals(cfg.course.isTutorial)) {
                    continue;
                }

                final RawCsection section = cfg.courseSection;
                if ("OT".equals(section.instrnType)) {
                    continue;
                }
                pacingStructures.add(section.pacingStructure);
            }

            boolean mixed = false;
            if (pacingStructures.size() > 1) {
                boolean hasS = false;
                boolean hasM = false;
                boolean hasO = false;
                for (final String test : pacingStructures) {
                    if ("S".equals(test)) {
                        hasS = true;
                    }
                    if ("M".equals(test)) {
                        hasM = true;
                    }
                    if ("O".equals(test)) {
                        hasO = true;
                    }
                }

                if (hasS) {
                    if (hasO) {
                        mixed = true;
                    } else if (hasM) {
                        // FIXME: We choose M since new courses don't use online assignments yet...
                        pacingStructure = "M";
                    } else {
                        pacingStructure = "S";
                    }
                } else {
                    mixed = hasM && hasO;
                }
            }

            if (mixed) {
                final RawStudent student = stuData.getStudent();
                Log.warning("Student ", student.stuId, " registered for sections with inconsistent pacing structures.");
            }

            // If no pacing structure is stored for the student, store the one we found for reference
            final RawStudent student = this.data.studentData.getStudent();
            final String stuRuleSetId = student.pacingStructure;
            if (pacingStructure == null) {
                // No pacing structure found based on course registrations, check student record
                pacingStructure = stuRuleSetId;
            } else if (stuRuleSetId == null) {
                // Student record has no pacing structure, but we found some from courses, so store it
                Log.info("Updating student pacing structure as part of scanning available courses");
                RawStudentLogic.updatePacingStructure(cache, student.stuId, pacingStructure);
            }

            // If no course registrations and no student pacing structure, so use most restrictive
            // pacing structure
            if (pacingStructure == null) {
                pacingStructure = RawPacingStructure.DEF_PACING_STRUCTURE;
            }
        }

        return RawPacingStructureLogic.query(cache, pacingStructure);
    }

    /**
     * Organizes courses into categories.
     *
     * @param cache           the data cache
     * @param studentCourses  an array of models of type {@code CRawStcourse}, representing student course registrations
     *                        (may have null entries for invalid configurations)
     * @param pacingStructure a{@code RuleSet} with the pacing structure under which the student works in this context
     * @throws SQLException if there is an error accessing the database
     */
    private void categorizeCourses(final Cache cache, final Iterable<RawStcourse> studentCourses,
                                   final RawPacingStructure pacingStructure) throws SQLException {

        final Integer max = pacingStructure.nbrOpenAllowed;
        final int maxOpen = max == null ? Integer.MAX_VALUE : max.intValue();

        // Now, we loop through the list of unopened courses and test each one for prerequisites.
        for (final RawStcourse stcourse : studentCourses) {

            if (stcourse == null) {
                continue;
            }

            final SiteDataCfgCourse cfg = this.data.courseData.getCourse(stcourse.course, stcourse.sect);
            if (cfg == null) {
                continue;
            }

            // Skip tutorials - they have already been handled
            final RawCourse course = cfg.course;
            if ("Y".equals(course.isTutorial)) {
                continue;
            }

            final RawCsection section = cfg.courseSection;
            if ("OT".equals(section.instrnType)) {
                continue;
            }

            final String courseId = stcourse.course;
            final CourseInfo courseInfo = new CourseInfo(courseId, CoreConstants.EMPTY);

            // Skip any courses already categorized
            // NOTE: Equality comparison of CourseInfo is only based on course ID...

            if (this.availableCourses.contains(courseInfo)
                    || this.unavailableCourses.contains(courseInfo)
                    || this.noPrereqCourses.contains(courseInfo)
                    || this.inProgressCourses.contains(courseInfo)
                    || this.pastDeadlineCourses.contains(courseInfo)
                    || this.completedCourses.contains(courseInfo)
                    || this.forfeitCourses.contains(courseInfo)
                    || this.notAvailableCourses.contains(courseInfo)
                    || this.otCreditCourses.contains(courseInfo)
                    || this.availableIncCourses.contains(courseInfo)
                    || this.unavailableIncCourses.contains(courseInfo)
                    || this.noPrereqIncCourses.contains(courseInfo)
                    || this.inProgressIncCourses.contains(courseInfo)
                    || this.completedIncCourses.contains(courseInfo)
                    || this.pastDeadlineIncCourses.contains(courseInfo)
                    || this.forfeitInc.contains(courseInfo)) {
                continue;
            }

            boolean prereq;

            // If course indicates prerequisites are satisfied, believe that; if not, check
            final String satis = stcourse.prereqSatis;
            if ("Y".equals(satis) || "P".equals(satis)) {
                prereq = true;
            } else {
                prereq = checkPrerequisites(cache, stcourse);
            }

            // CSU Online students can take MATH 117 without prereq, but get ELM as unit 0 if prereq is not satisfied
            if (!prereq && RawRecordConstants.M117.equals(courseId)
                    && (!stcourse.sect.isEmpty() && (int) stcourse.sect.charAt(0) == (int) '8')) {
                prereq = true;
            }

            final String theLabel = this.courseLabels.get(courseId);
            if (theLabel == null) {
                Log.warning("No course label configured for ", courseId);
            } else {
                final CourseInfo infoWithLabel = new CourseInfo(courseId, theLabel);

                if ("Y".equals(stcourse.iInProgress)) {
                    if ("G".equals(stcourse.openStatus)) {
                        this.forfeitInc.add(infoWithLabel);
                    } else if ("Y".equals(stcourse.completed)) {
                        this.completedIncCourses.add(infoWithLabel);
                    } else if ("N".equals(stcourse.openStatus)) {
                        // Incomplete was not completed, but has been closed, so deadline must have expired - this
                        // should not happen, but warn if it does
                        Log.warning("Incomplete in ", courseId, " for ", stcourse.stuId,
                                " has open_status='N' but is not completed - investigate...");
                        this.pastDeadlineIncCourses.add(infoWithLabel);
                    } else if ("Y".equals(stcourse.openStatus)) {
                        this.inProgressIncCourses.add(infoWithLabel);
                    } else if (prereq) {
                        if (this.numOpen >= maxOpen) {
                            this.unavailableIncCourses.add(infoWithLabel);
                        } else {
                            this.availableIncCourses.add(infoWithLabel);
                        }
                    } else {
                        this.noPrereqIncCourses.add(infoWithLabel);
                    }
                } else {
                    if ("G".equals(stcourse.openStatus)) {
                        this.forfeitCourses.add(infoWithLabel);
                    } else if ("Y".equals(stcourse.completed)) {
                        this.completedCourses.add(infoWithLabel);
                    } else if ("N".equals(stcourse.openStatus)) {
                        this.notAvailableCourses.add(infoWithLabel);
                    } else if ("Y".equals(stcourse.openStatus)) {
                        this.inProgressCourses.add(infoWithLabel);
                    } else if (prereq) {
                        if (this.numOpen >= maxOpen || this.incUnopened) {
                            this.unavailableCourses.add(infoWithLabel);
                        } else {
                            this.availableCourses.add(infoWithLabel);
                        }
                    } else {
                        this.noPrereqCourses.add(infoWithLabel);
                    }
                }
            }
        }
    }

    /**
     * Tests whether the student is blocked because they have courses left to do, but have no courses that are in a
     * condition where work can be done.
     */
    private void testForBlocked() {

        // If there exist unavailable or no-prerequisite courses, but there are no in-progress or
        // available courses (regular or incomplete), the student is stuck.

        this.blocked = this.inProgressCourses.isEmpty() && this.availableCourses.isEmpty()
                && this.inProgressIncCourses.isEmpty() && this.availableIncCourses.isEmpty() &&
                !(this.unavailableCourses.isEmpty() && this.noPrereqCourses.isEmpty()
                        && this.unavailableIncCourses.isEmpty() && this.noPrereqIncCourses.isEmpty());
    }

    /**
     * Tests whether the student is using the new "standards-based" courses.
     *
     * @return 0 if the student is not taking any standards-based courses; 1 if they have a mixture, and 2 if they are
     *         using exclusively standards-based courses
     */
    public int getUseOfStandardsBased() {

        int numOld = 0;
        int numNew = 0;

        final Collection<CourseInfo> allCourses = new HashSet<>(10);
        allCourses.addAll(this.availableCourses);
        allCourses.addAll(this.unavailableCourses);
        allCourses.addAll(this.noPrereqCourses);
        allCourses.addAll(this.inProgressCourses);
        allCourses.addAll(this.completedCourses);
        allCourses.addAll(this.pastDeadlineCourses);

        for (final CourseInfo info : allCourses) {
            if (info.course.startsWith("MATH ")) {
                ++numNew;
            } else if (info.course.startsWith("M 1")) {
                ++numOld;
            }
        }

        return numNew == 0 ? 0 : (numOld == 0 ? 2 : 1);
    }
}
