package dev.mathops.db.old.logic.mathplan.data;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.logic.mathplan.MathPlanLogic;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.db.old.logic.HoldsStatus;
import dev.mathops.db.old.logic.PlacementLogic;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.logic.PrecalcTutorialLogic;
import dev.mathops.db.old.logic.PrecalcTutorialStatus;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.LiveCsuCredit;
import dev.mathops.db.old.rec.LiveTransferCredit;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * All data related to a student used by this site. Student data is queried once and cached for a period of time and
 * re-used on subsequent page loads, since this data is unlikely to change rapidly. If an update is made to the data by
 * the site (like the list of majors of interest), the cache is updated at the same time.
 */
public final class MPStudentData {

    /** Retain data for 60 seconds (adjust as needed). */
    private static final long RETENTION_MS = (long) (60 * 1000);

    /** SAT cut score for "remedial" student classification. */
    private static final int CDHE_SAT_CUT_SCORE = 500;

    /** ACT cut score for "remedial" student classification. */
    private static final int CDHE_ACT_CUT_SCORE = 19;

    /** The expiration timestamp of this cached record. */
    private final long expiry;

    /** The student record. */
    public final RawStudent student;

    /** The student's responses to the set of majors of interest. */
    private final Map<Integer, RawStmathplan> majorProfileResponses;

    /** True if student has viewed their existing record. */
    public final boolean viewedExisting;

    /** True if student has checked the "This is only a recommendation" checkbox. */
    public final boolean checkedOnlyRecommendation;

    /** The student's responses to their intentions. */
    private final Map<Integer, RawStmathplan> intentionsResponses;

    /**
     * Flag indicating student has not yet demonstrated (through SAT score, ACT score, course work, or secondary
     * evaluation) that they are not "remedial" according to CDHE, and requires secondary assessment.
     */
    private final boolean remedial;

    /** The courses the student has already completed at CSU. */
    private final List<LiveCsuCredit> completedCourses;

    /** The student's live transfer credit. */
    private final List<LiveTransferCredit> liveTransferCredit;

    /** The student's placement credit. */
    private final List<RawMpeCredit> placementCredit;

    /** The student's cumulative math requirements. */
    private final List<MajorMathRequirement> requirements;

    /** The course recommendations based on student major(s). */
    public final CourseRecommendations recommendations;

    /** Prerequisite logic. */
    public final PrerequisiteLogic prereqLogic;

    /** Placement status. */
    public final PlacementStatus placementStatus;

    /** ELM Tutorial Status. */
    public final ELMTutorialStatus elmTutorialStatus;

    /** Precalculus Tutorial Status. */
    public final PrecalcTutorialStatus precalcTutorialStatus;

    /** List of course IDs that the student can register for. */
    private final Set<String> canRegisterFor;

    /**
     * Number of credits of AUCC core mathematics completed (placement results do not count toward completion of these
     * credits).
     */
    private double creditsOfCoreCompleted;

    /** The list of possible next steps for the student, in order of preference. */
    private final List<ENextStep> nextSteps;

    /**
     * Constructs a new {@code StudentData}.
     *
     * @param theStudentData  the student data object
     * @param logic           the logic object to use to populate student data
     * @param now             the date/time to consider "now"
     * @param loginSessionTag the login session tag
     * @param writeChanges    {@code true} to write profile value changes (used when the student is accessing the site);
     *                        {@code false} to skip writing changes (used when an administrator or adviser is acting as
     *                        a student)
     * @throws SQLException if there is an error accessing the database
     */
    public MPStudentData(final StudentData theStudentData, final MathPlanLogic logic,
                         final ZonedDateTime now, final long loginSessionTag, final boolean writeChanges)
            throws SQLException {

        this.expiry = System.currentTimeMillis() + RETENTION_MS;

        final String studentId = theStudentData.getStudentId();
        this.student = theStudentData.getStudentRecord();

        // Log.info(Res.fmt(Res.BUILDING_STU_DATA, this.studentId));

        // Process "what admissions has told us"
        final boolean potentialRemedial = isPotentiallyRemedial();

        // FIXME: Use the student data object rather than querying with the cache where possible.
        final Cache cache = theStudentData.getCache();

        // Populate "what student has told us"
        this.majorProfileResponses =
                MathPlanLogic.getMathPlanResponses(cache, studentId, MathPlanConstants.MAJORS_PROFILE);

        // Populate "existing work on record"
        this.viewedExisting = MathPlanLogic
                .getMathPlanResponses(cache, studentId, MathPlanConstants.EXISTING_PROFILE)
                .containsKey(Integer.valueOf(1));

        this.checkedOnlyRecommendation = MathPlanLogic
                .getMathPlanResponses(cache, studentId, MathPlanConstants.ONLY_RECOM_PROFILE)
                .containsKey(Integer.valueOf(1));

        // Populate "intentions"
        this.intentionsResponses = MathPlanLogic.getMathPlanResponses(cache, studentId,
                MathPlanConstants.INTENTIONS_PROFILE);

        // Populate "what student has done"
        this.completedCourses = logic.getCompletedCourses(studentId);
        this.liveTransferCredit = logic.getStudentTransferCredit(cache, studentId, true);

        // TODO: For any transfer rows (in MATH) that are not in the local database, inset them. Also, in the course
        //  web site, if a student has no prereqs, do a secondary live check against Aries and see if new transfer rows
        //  exist (and import them).

        this.placementCredit = RawMpeCreditLogic.queryByStudent(cache, studentId);

        // Determine if student is to be treated as "remedial"
        this.remedial = potentialRemedial && this.placementCredit.isEmpty()
                && this.completedCourses.isEmpty() && this.liveTransferCredit.isEmpty();

        // Gather math courses required by all selected majors (if any)
        this.requirements = accumulateRequirements(this.student.programCode, this.majorProfileResponses, logic);

        // Compute course recommendations to satisfy requirements
        this.recommendations = new CourseRecommendations(this.requirements, this, logic);

        this.prereqLogic = new PrerequisiteLogic(theStudentData);

        this.placementStatus = new PlacementLogic(cache, studentId, this.student.aplnTerm, now).status;

        final HoldsStatus holds = HoldsStatus.of(cache, studentId);

        this.elmTutorialStatus = ELMTutorialStatus.of(cache, studentId, now, holds);

        final LocalDate today = now.toLocalDate();
        this.precalcTutorialStatus = new PrecalcTutorialLogic(theStudentData, today, this.prereqLogic).status;

        // Count the number of AUCC core mathematics course credits completed with a grade of C (2.000) or better.
        countCoreCredits(logic);

        // Build the set of courses that the student can register for right now
        this.canRegisterFor = getCoursesStudentCanRegisterFor();

        // Ready to construct plan...
        this.nextSteps = new ArrayList<>(5);
        createPlan(cache, logic);
        if (writeChanges) {
            recordPlan(theStudentData, logic, now, loginSessionTag);
        }

        recordAnalytics();
    }

    /**
     * Tests whether the student is potentially "remedial" due to missing ACT/SAT scores or scores below the remedial
     * cut scores. Remedial status can be mitigated by taking a secondary assessment or having completed courses or
     * transfer credit.
     *
     * @return {@code true} if student is potentially remedial
     */
    private boolean isPotentiallyRemedial() {

        // Process "what admissions has told us"
        final boolean potentialRemedial;
        final Integer act = this.student.actScore;
        final Integer sat = this.student.satScore;
        if (act == null) {
            if (sat == null) {
                potentialRemedial = true;
            } else {
                potentialRemedial = sat.intValue() < CDHE_SAT_CUT_SCORE;
            }
        } else if (sat == null) {
            potentialRemedial = act.intValue() < CDHE_ACT_CUT_SCORE;
        } else {
            potentialRemedial = sat.intValue() < CDHE_SAT_CUT_SCORE && act.intValue() < CDHE_ACT_CUT_SCORE;
        }

        return potentialRemedial;
    }

    /**
     * Counts the number of AUCC core mathematics credits the student has completed with a grade of C (2.000) or higher
     * (placing out of an AUCC course does not count, but earning credit for the course on the Challenge exam does
     * count).
     *
     * <p>
     * Note that we skip 550 (placement credit) sections in the completed courses list since those are also reflected as
     * rows with placed='C' in the placement credit list (where dt_cr_refused is null). So we do count such placement
     * records. This avoids the delay that the 550 section can have in getting onto the student's transcript.
     *
     * @param logic the site logic
     */
    private void countCoreCredits(final MathPlanLogic logic) {

        final CourseGroup group = logic.getCourseGroups().get("AUCC3");
        if (group != null) {
            final Map<String, RawCourse> courseMap = logic.getCourses();
            final List<String> coreCourses = group.courseNumbers;
            final Collection<String> markedAsCompleted = new ArrayList<>(3);

            for (final LiveCsuCredit completed : this.completedCourses) {
                final String courseId = completed.courseId;

                // NOTE: Section 550 still satisfies core AUCC 1B.

                if (markedAsCompleted.contains(courseId)) {
                    continue;
                }

                if (completed.getGradeGpa() != null && completed.getGradeGpa().doubleValue() > 1.9
                        && coreCourses.contains(courseId)) {

                    final RawCourse course = courseMap.get(courseId);
                    if (course != null && course.nbrCredits != null) {
                        markedAsCompleted.add(courseId);
                        this.creditsOfCoreCompleted += course.nbrCredits.doubleValue();
                    }
                }
            }

            for (final LiveTransferCredit transfer : this.liveTransferCredit) {
                final String id = transfer.courseId;
                final Float gpa = transfer.getGradeGpa();
                // Transfer IDs look like "MATH117" or "MATH1++1B"

                if (markedAsCompleted.contains(id)) {
                    continue;
                }

                if (gpa != null && gpa.doubleValue() > 1.9 && coreCourses.contains(id)) {
                    markedAsCompleted.add(id);
                    this.creditsOfCoreCompleted += transfer.credits.doubleValue();
                }
            }
        }

        // We have scanned for courses that satisfy AUCC, but students could have transfer credit
        // for "MATH 1++1B" or "MATH 2++1B" that also count toward AUCC core math
        for (final LiveTransferCredit transfer : this.liveTransferCredit) {
            final String id = transfer.transferred;
            if (id.endsWith("++1B")) {
                this.creditsOfCoreCompleted += transfer.credits.doubleValue();
            }
        }
    }

    /**
     * Creates a list of courses for which the student is currently eligible to register. Courses up through Calculus II
     * are considered, since that represents all courses that could occur as the student's first course in a degree
     * program.
     *
     * @return the set of course IDs
     */
    private Set<String> getCoursesStudentCanRegisterFor() {

        final Set<String> result = new HashSet<>(10);

        // Build a list of courses that the student can register for

        // If student has cut scores, they can register for MATH 101
        if (!this.remedial) {
            result.add("M 101");
        }

        boolean bOrBetter124 = false;
        boolean bOrBetter126 = false;
        boolean has160 = false;

        // First, get courses they can register for due to placement results
        for (final RawMpeCredit cr : this.placementCredit) {
            final String courseId = cr.course;

            if (RawRecordConstants.M100C.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add("M 101");
                result.add("M 105");
            } else if ("M 100M".equals(courseId)) {
                result.add("M 101");
                result.add("M 105");
            } else if (RawRecordConstants.M100T.equals(courseId)) {
                result.add("M 101");
            } else if ("M 100A".equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add("M 101");
            } else if (RawRecordConstants.M117.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
            } else if (RawRecordConstants.M118.equals(courseId) || "M 120".equals(courseId)
                    || "M 120A".equals(courseId)
                    || "M 121".equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add("M 141");
            } else if (RawRecordConstants.M124.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add("M 141");
                bOrBetter124 = true;
            } else if (RawRecordConstants.M125.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
            } else if (RawRecordConstants.M126.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
                result.add("M 160");
                bOrBetter126 = true;
            }
        }

        // Get courses they can register for due to transfer credit results
        for (final LiveTransferCredit cr : this.liveTransferCredit) {
            final String id = cr.courseId;
            final Float gpa = cr.getGradeGpa();

            if ("M 001".equals(id)) {
                result.add("M 101");
            } else if ("M 002".equals(id)) {
                result.add(RawRecordConstants.M117);
            } else if (RawRecordConstants.M117.equals(id)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
            } else if (RawRecordConstants.M118.equals(id) || "M 120".equals(id)
                    || "M 120A".equals(id)
                    || "M 121".equals(id)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add("M 141");
            } else if (RawRecordConstants.M124.equals(id)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add("M 141");
                if (gpa != null && gpa.floatValue() > 2.9f) {
                    bOrBetter124 = true;
                }
            } else if (RawRecordConstants.M125.equals(id)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
            } else if (RawRecordConstants.M126.equals(id)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
                if (gpa != null && gpa.floatValue() > 2.9f) {
                    bOrBetter126 = true;
                }
            } else if ("M 160".equals(id)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
                result.add("M 160");
                has160 = true;
            }
        }

        // Get courses they can register for due to completed CSU courses results
        for (final LiveCsuCredit cr : this.completedCourses) {
            final String courseId = cr.courseId;

            if (RawRecordConstants.M117.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
            } else if (RawRecordConstants.M118.equals(courseId) //
                    || "M 120".equals(courseId)
                    || "M 120A".equals(courseId)
                    || "M 121".equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add("M 141");
            } else if (RawRecordConstants.M124.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add("M 141");
                if (cr.getGradeGpa() != null && cr.getGradeGpa().floatValue() > 2.9f) {
                    bOrBetter124 = true;
                }
            } else if (RawRecordConstants.M125.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
            } else if (RawRecordConstants.M126.equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
                if (cr.getGradeGpa() != null && cr.getGradeGpa().floatValue() > 2.9f) {
                    bOrBetter126 = true;
                }
            } else if ("M 160".equals(courseId)) {
                result.add(RawRecordConstants.M117);
                result.add(RawRecordConstants.M118);
                result.add(RawRecordConstants.M124);
                result.add(RawRecordConstants.M125);
                result.add(RawRecordConstants.M126);
                result.add("M 141");
                result.add("M 155");
                result.add("M 160");
                has160 = true;
            }
        }

        if (bOrBetter124 && bOrBetter126) {
            result.add("M 160");
        }

        if (has160 && bOrBetter124) {
            result.add("M 161");
        }

        // Look for completed/transfer Precalculus courses and check grades as needed to register
        // for Calculus
        if (!result.contains("M 141")
                && testForCompletedOrTransfer(RawRecordConstants.M118, 2.0f)) {
            result.add("M 141");
        }

        if (!result.contains("M 155")
                && testForCompletedOrTransfer(RawRecordConstants.M124, 2.0f)
                && testForCompletedOrTransfer(RawRecordConstants.M125, 2.0f)) {
            result.add("M 155");
        }

        if (!result.contains("M 160")
                && testForCompletedOrTransfer(RawRecordConstants.M124, 3.0f)
                && testForCompletedOrTransfer(RawRecordConstants.M124, 3.0f)) {

            result.add("M 160");
        }

        if (!result.contains("M 161")
                && testForCompletedOrTransfer("M 160", 1.0f)
                && testForCompletedOrTransfer(RawRecordConstants.M124, 3.0f)) {

            result.add("M 161");
        }

        return result;
    }

    /**
     * Tests whether the student has completed or transfered a course with a certain minimum grade.
     *
     * @param course   the course
     * @param minGrade the minimum grade
     * @return {@code true} if student has transfer credit for the course with the required grade or has completed the
     *         course with the required grade
     */
    private boolean testForCompletedOrTransfer(final String course, final float minGrade) {

        boolean hasCourse = false;

        // Transfer credit is more likely, so try that first
        for (final LiveTransferCredit rec : this.liveTransferCredit) {
            final String id = rec.courseId;
            final Float gpa = rec.getGradeGpa();

            if (gpa != null && course.equals(id) && gpa.floatValue() >= minGrade) {
                hasCourse = true;
                break;
            }
        }

        if (!hasCourse) {
            for (final LiveCsuCredit rec : this.completedCourses) {
                final String courseId = rec.courseId;

                if (rec.getGradeGpa() != null && course.equals(courseId)
                        && rec.getGradeGpa().floatValue() >= minGrade) {
                    hasCourse = true;
                    break;
                }
            }
        }

        return hasCourse;
    }

    /**
     * Creates the student's personalized plan.
     *
     * @param cache the data cache
     * @param logic the site logic
     * @throws SQLException if there is an error accessing the database
     */
    private void createPlan(final Cache cache, final MathPlanLogic logic) throws SQLException {

        final CourseSequence critical = this.recommendations.criticalSequence;
        final CourseSequence recommend = this.recommendations.recommendedSequence;
        final CourseSequence typical = this.recommendations.typicalSequence;

        final TermRec active = TermLogic.get(cache).queryActive(cache);

        final ETermName activeTermName = active == null ? null : active.term.name;
        final int activeTermYear = active == null ? LocalDate.now().getYear() : active.term.year.intValue();

        final int numSem1 = critical.getSemester1Courses().size() + critical.getSemester1CourseGroups().size();

        if (critical.hasPreArrivalData() > 0) {
            // CASE 1.1, critical pre-arrival requirements
            if (numSem1 > 1) {
                createPlanCase1(activeTermYear, activeTermName, critical, ENextStep.MSG_1A_PLURAL,
                        ENextStep.MSG_1B_PLURAL, numSem1);
            } else {
                createPlanCase1(activeTermYear, activeTermName, critical, ENextStep.MSG_1A_SINGULAR,
                        ENextStep.MSG_1B_SINGULAR, numSem1);
            }

        } else if (recommend.hasPreArrivalData() > 0) {

            // CASE 1.2 with RECOMMENDED requirements
            if (numSem1 > 1) {
                createPlanCase1(activeTermYear, activeTermName, recommend, ENextStep.MSG_1C_PLURAL,
                        ENextStep.MSG_1D_PLURAL, numSem1);
            } else {
                createPlanCase1(activeTermYear, activeTermName, recommend,
                        ENextStep.MSG_1C_SINGULAR, ENextStep.MSG_1D_SINGULAR, numSem1);
            }

        } else if (typical.hasPreArrivalData() > 0) {

            // CASE 1.2 with no CRITICAL or RECOMMENDED requirements
            if (numSem1 > 1) {
                createPlanCase1(activeTermYear, activeTermName, typical, ENextStep.MSG_1E_PLURAL,
                        ENextStep.MSG_1F_PLURAL, numSem1);
            } else {
                createPlanCase1(activeTermYear, activeTermName, typical, ENextStep.MSG_1E_SINGULAR,
                        ENextStep.MSG_1F_SINGULAR, numSem1);
            }

        } else if (isEligibleForSemester1(typical)) {

            // See if student is done with everything

            if (typical.hasSemester1Data() || typical.hasSemester2Data() || typical.hasAdditionalData()) {
                // CASE 3:
                createPlanCase3(typical, logic);
            } else {
                // CASE 4:
                createPlanCase4();
            }

        } else {

            // CASE 2:
            createPlanCase2(activeTermYear, activeTermName, critical, recommend, typical);
        }

        // Statistics
    }

    /**
     * Records the plan in the profile response table so advisers can view it quickly without having to rebuilt it for
     * each advisee.
     *
     * @param theStudentData  the student data object
     * @param logic           the logic object
     * @param now             the date/time to consider "now"
     * @param loginSessionTag the login session tag
     * @throws SQLException if there is an error accessing the database
     */
    public void recordPlan(final StudentData theStudentData, final MathPlanLogic logic, final ZonedDateTime now,
                           final long loginSessionTag) throws SQLException {

        // Record only after student has checked the "only a recommendation" box
        final Map<Integer, RawStmathplan> done = theStudentData.getLatestMathPlanResponsesByPage(
                MathPlanConstants.ONLY_RECOM_PROFILE);

        if (!done.isEmpty()) {

            final Map<String, RawCourse> courseData = logic.getCourses();

            final Integer key1 = Integer.valueOf(1);
            final Integer key2 = Integer.valueOf(2);
            final Integer key3 = Integer.valueOf(3);
            final Integer key4 = Integer.valueOf(4);

            final String value1 = makePlanTextPre(courseData);
            final String value2 = makePlanTextSem1(courseData);
            final String value3 = makePlanTextSem2(courseData);
            final String value4 = makePlanTextAdd(courseData);

            final Map<Integer, RawStmathplan> existing = theStudentData.getLatestMathPlanResponsesByPage(
                    MathPlanConstants.PLAN_PROFILE);

            final RawStmathplan exist1 = existing.get(key1);
            final RawStmathplan exist2 = existing.get(key2);
            final RawStmathplan exist3 = existing.get(key3);
            final RawStmathplan exist4 = existing.get(key4);

            final boolean shouldInsertNew = exist1 == null || exist1.stuAnswer == null
                    || !exist1.stuAnswer.equals(value1) || exist2 == null || exist2.stuAnswer == null
                    || !exist2.stuAnswer.equals(value2) || exist3 == null || exist3.stuAnswer == null
                    || !exist3.stuAnswer.equals(value3) || exist4 == null || exist4.stuAnswer == null
                    || !exist4.stuAnswer.equals(value4);

            if (shouldInsertNew) {
                final List<Integer> questions = new ArrayList<>(4);
                final List<String> answers = new ArrayList<>(4);

                questions.add(key1);
                questions.add(key2);
                questions.add(key3);
                questions.add(key4);
                answers.add(value1);
                answers.add(value2);
                answers.add(value3);
                answers.add(value4);

                logic.storeMathPlanResponses(theStudentData, MathPlanConstants.PLAN_PROFILE, questions, answers, now,
                        loginSessionTag);
            }
        }
    }

    /**
     * Generates a string representation of the list of pre-arrival requirements from the student's plan to be shown in
     * the adviser's screen.
     *
     * @param courseData the course data
     * @return the string representation
     */
    private String makePlanTextPre(final Map<String, RawCourse> courseData) {

        final CourseSequence typical = this.recommendations.typicalSequence;
        final Map<String, CourseInfo> typicalCourses = typical.getPreArrivalCourses();

        final CourseSequence critical = this.recommendations.criticalSequence;
        final Map<String, CourseInfo> criticalCourses = critical.getPreArrivalCourses();

        return makePlanText(courseData, typicalCourses, null,
                criticalCourses, null, typical.hasMultipleCalc1(), typical.hasMultipleCalc2());
    }

    /**
     * Generates a string representation of the list of semester 1 requirements from the student's plan to be shown in
     * the adviser's screen.
     *
     * @param courseData the course data
     * @return the string representation
     */
    private String makePlanTextSem1(final Map<String, RawCourse> courseData) {

        final CourseSequence typical = this.recommendations.typicalSequence;
        final Map<String, CourseInfo> typicalCourses = typical.getSemester1Courses();
        final Set<CourseInfoGroup> typicalGroups = typical.getSemester1CourseGroups();

        final CourseSequence critical = this.recommendations.criticalSequence;
        final Map<String, CourseInfo> criticalCourses = critical.getSemester1Courses();
        final Set<CourseInfoGroup> criticalGroups = critical.getSemester1CourseGroups();

        return makePlanText(courseData, typicalCourses, typicalGroups,
                criticalCourses, criticalGroups, typical.hasMultipleCalc1(),
                typical.hasMultipleCalc2());
    }

    /**
     * Generates a string representation of the list of semester 2 requirements from the student's plan to be shown in
     * the adviser's screen.
     *
     * @param courseData the course data
     * @return the string representation
     */
    private String makePlanTextSem2(final Map<String, RawCourse> courseData) {

        final CourseSequence typical = this.recommendations.typicalSequence;
        final Map<String, CourseInfo> typicalCourses = typical.getSemester2Courses();
        final Set<CourseInfoGroup> typicalGroups = typical.getSemester2CourseGroups();

        final CourseSequence critical = this.recommendations.criticalSequence;
        final Map<String, CourseInfo> criticalCourses = critical.getSemester2Courses();
        final Set<CourseInfoGroup> criticalGroups = critical.getSemester2CourseGroups();

        return makePlanText(courseData, typicalCourses, typicalGroups,
                criticalCourses, criticalGroups, typical.hasMultipleCalc1(),
                typical.hasMultipleCalc2());
    }

    /**
     * Generates a string representation of the list of additional requirements from the student's plan to be shown in
     * the adviser's screen.
     *
     * @param courseData the course data
     * @return the string representation
     */
    private String makePlanTextAdd(final Map<String, RawCourse> courseData) {

        final CourseSequence typical = this.recommendations.typicalSequence;
        final Map<String, CourseInfo> typicalCourses = typical.getAdditionalCourses();
        final Set<CourseInfoGroup> typicalGroups = typical.getAdditionalCourseGroups();

        final CourseSequence critical = this.recommendations.criticalSequence;
        final Map<String, CourseInfo> criticalCourses = critical.getAdditionalCourses();
        final Set<CourseInfoGroup> criticalGroups = critical.getAdditionalCourseGroups();

        return makePlanText(courseData, typicalCourses, typicalGroups,
                criticalCourses, criticalGroups, typical.hasMultipleCalc1(),
                typical.hasMultipleCalc2());
    }

    /**
     * Generates a string representation of the list of semester 2 requirements from the student's plan to be shown in
     * the adviser's screen.
     *
     * @param courseData       the course data
     * @param typicalCourses   the typical course list
     * @param typicalGroups    the typical group course list
     * @param criticalCourses  the critical course list
     * @param criticalGroups   the critical group course list
     * @param hasMultipleCalc1 true if course sequence allows multiple Calc 1 courses
     * @param hasMultipleCalc2 true if course sequence allows multiple Calc 2 courses
     * @return the string representation
     */
    private static String makePlanText(final Map<String, RawCourse> courseData,
                                       final Map<String, CourseInfo> typicalCourses,
                                       final Iterable<CourseInfoGroup> typicalGroups,
                                       final Map<String, CourseInfo> criticalCourses,
                                       final Collection<CourseInfoGroup> criticalGroups,
                                       final boolean hasMultipleCalc1, final boolean hasMultipleCalc2) {

        final HtmlBuilder builder = new HtmlBuilder(40);
        boolean comma = false;

        for (final Map.Entry<String, CourseInfo> entry : typicalCourses.entrySet()) {
            final CourseInfo info = entry.getValue();

            if (info.status.sufficient) {
                continue;
            }

            final RawCourse crs = courseData.get(entry.getKey());

            final String id = crs.course;
            final boolean isCalc1 = "M 141".equals(id) || "M 155".equals(id) || "M 160".equals(id);
            final boolean isCalc2 = "M 255".equals(id) || "M 161".equals(id);

            if (comma) {
                builder.add(", ");
            }

            if (isCalc1 && hasMultipleCalc1) {
                builder.add("Calc. I");
            } else if (isCalc2 && (hasMultipleCalc1 || hasMultipleCalc2)) {
                builder.add("Calc. II");
            } else if (id.startsWith("M ")) {
                builder.add(id.substring(2));
            } else {
                builder.add(id);
            }

            if (criticalCourses.containsKey(id)) {
                builder.add("*");
            }

            comma = true;
        }

        if (typicalGroups != null) {
            for (final CourseInfoGroup g : typicalGroups) {
                if (g.isSatisfied()) {
                    continue;
                }

                final String code = g.getGroupCode();
                final boolean isCalc1 = "CALC".equals(code) || "CALC1BIO".equals(code);
                final boolean isCalc2 = "CALC2BIO".equals(code);

                if (comma) {
                    builder.add(", ");
                }

                if (isCalc1 && hasMultipleCalc1) {
                    builder.add("Calc. I");
                } else if (isCalc2 && (hasMultipleCalc1 || hasMultipleCalc2)) {
                    builder.add("Calc. II");
                } else {
                    if (g.getCourseGroup().nbrCredits == null) {
                        builder.add("1 of ");
                    } else {
                        builder.add(g.getCourseGroup().nbrCredits, " cr. of ");
                    }

                    if ("AUCC3".equals(code) || "AUCC2".equals(code)) {
                        builder.add("Core Math");
                    } else if ("CALC".equals(code)) {
                        builder.add("any Calc I");
                    } else {
                        builder.add('(');
                        boolean subcomma = false;
                        for (final String courseNum : g.getCourseNumbers()) {
                            final RawCourse crs = courseData.get(courseNum);
                            if (crs != null) {
                                if (subcomma) {
                                    builder.add(", ");
                                }
                                if (courseNum.startsWith("M ")) {
                                    builder.add(courseNum.substring(2));
                                } else {
                                    builder.add(courseNum);
                                }
                                subcomma = true;
                            }
                        }
                        builder.add(')');
                    }
                }

                if (criticalGroups != null && criticalGroups.contains(g)) {
                    builder.add("*");
                }
            }
        }

        if (builder.length() == 0) {
            builder.add("(none)");
        }

        return builder.toString();
    }

    /**
     * Tests whether the student is eligible to register for a first semester course (or there are no first-semester
     * courses).
     * <p>
     * If the first semester includes both MATH 101 and MATH 117, this method returns TRUE only if the student is
     * eligible for BOTH (the intent of the method is to see if the student still needs to do something to get ready,
     * and ineligibility for MATH 117 is in that category).
     *
     * @param typical the typical (fastest) course sequence
     * @return true if the student is eligible for a first-semester courses or there are no first-semester courses
     */
    private boolean isEligibleForSemester1(final CourseSequence typical) {

        boolean eligible = false;

        if (typical.hasSemester1Data()) {
            // Student is eligible for something - check for case of MATH 101 + MATH 117
            if (typical.isEligibleToRegisterForSemester1(this.canRegisterFor)
                    && (this.canRegisterFor.contains(RawRecordConstants.M117)
                    || !typical.needs117InSem1())) {
                eligible = true;
            }
        } else {
            eligible = true;
        }

        return eligible;
    }

    /**
     * Generates the next steps for students in case 1.
     *
     * @param activeTermYear       the active term year
     * @param activeTermName       the active term name
     * @param sequence             the sequence of courses
     * @param openingMessageFuture the first message to present to students whose application term is in the future
     * @param openingMessagePast   the first message to present to students whose application term is in the past
     * @param numSem1              the number of semester 1 courses the student will need to take
     */
    private void createPlanCase1(final int activeTermYear, final ETermName activeTermName,
                                 final CourseSequence sequence, final ENextStep openingMessageFuture,
                                 final ENextStep openingMessagePast, final int numSem1) {

        if (this.precalcTutorialStatus.eligibleForPrecalcTutorial) {
            // Case 1.x.1:
            this.nextSteps.add(openingMessageFuture);

            if (!this.placementStatus.placementAttempted) {
                // CaseA: student has not attempted placement yet
                if (openingMessagePast == ENextStep.MSG_1A_PLURAL) {
                    // Use messages based on critical prereqs
                    this.nextSteps.add(ENextStep.MSG_1G);
                } else {
                    // Use messages based on typical prereqs
                    this.nextSteps.add(ENextStep.MSG_1N);
                }
                this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
            } else if (this.placementStatus.attemptsRemaining > 0) {

                int leftToSatisfy = 0;
                boolean needs117 = false;
                boolean needs118 = false;
                for (final CourseInfo info : sequence.getPreArrivalCourses().values()) {
                    if (!info.status.sufficient) {

                        if (RawRecordConstants.M117.equals(info.course.course)) {
                            needs117 = true;
                        } else if (RawRecordConstants.M118.equals(info.course.course)) {
                            needs118 = true;
                        }

                        ++leftToSatisfy;
                    }
                }

                // FIXME: Restore placement recommendation (and recommendation to challenge, for use in math plan
                //  displays). But store this in the STMPE record rather than forcing parse of subtest scores (whose
                //  thresholds could change). This requires retroactively populating all existing rows, then starting
                //  to store the field.

                // if ((isRecommendMPEPlace() || this.placementCredit.size() > 0)
                if (!this.placementCredit.isEmpty() && (leftToSatisfy > 1 || needs117 || needs118)) {
                    // Case B
                    if (openingMessagePast == ENextStep.MSG_1A_PLURAL) {
                        // Use messages based on critical prereqs
                        this.nextSteps.add(ENextStep.MSG_1H);
                        this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
                        this.nextSteps.add(ENextStep.MSG_1I);
                    } else {
                        // Use messages based on typical prereqs
                        this.nextSteps.add(ENextStep.MSG_1O);
                        this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
                        this.nextSteps.add(ENextStep.MSG_1P);
                    }
                    this.nextSteps.add(ENextStep.ACT_PRECALCULUS_TUTORIAL);
                } else {
                    // Case C
                    if (openingMessagePast == ENextStep.MSG_1A_PLURAL) {
                        // Use messages based on critical prereqs
                        this.nextSteps.add(ENextStep.MSG_1J);
                        this.nextSteps.add(ENextStep.ACT_PRECALCULUS_TUTORIAL);
                        this.nextSteps.add(ENextStep.MSG_1K);
                    } else {
                        // Use messages based on typical prereqs
                        this.nextSteps.add(ENextStep.MSG_1Q);
                        this.nextSteps.add(ENextStep.ACT_PRECALCULUS_TUTORIAL);
                        this.nextSteps.add(ENextStep.MSG_1R);
                    }
                    this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
                }
            } else {
                if (openingMessagePast == ENextStep.MSG_1A_PLURAL) {
                    // Use messages based on critical prereqs
                    this.nextSteps.add(ENextStep.MSG_1J);
                } else {
                    // Use messages based on typical prereqs
                    this.nextSteps.add(ENextStep.MSG_1Q);
                }
                this.nextSteps.add(ENextStep.ACT_PRECALCULUS_TUTORIAL);
            }
        } else {
            final TermKey appTerm = this.student.aplnTerm;

            final int appTermYear = appTerm == null ? 0 : appTerm.year.intValue();
            final ETermName appTermName = appTerm == null ? null : appTerm.name;

            final boolean isInFuture;

            if (appTermYear > activeTermYear) {
                isInFuture = true;
            } else if (appTermYear == activeTermYear) {
                if (appTermName == ETermName.FALL) {
                    isInFuture = activeTermName != ETermName.FALL;
                } else if (appTermName == ETermName.SUMMER) {
                    isInFuture = activeTermName == ETermName.SPRING;
                } else {
                    isInFuture = false;
                }
            } else {
                isInFuture = false;
            }

            if (isInFuture) {
                // Case 1.x.2
                this.nextSteps.add(openingMessageFuture);
            } else {
                // Case 1.x.3
                this.nextSteps.add(openingMessagePast);
            }

            if (!this.placementStatus.placementAttempted) {
                // Case A
                if (openingMessagePast == ENextStep.MSG_1A_PLURAL) {
                    // Use messages based on critical prereqs
                    this.nextSteps.add(ENextStep.MSG_1G);
                } else {
                    // Use messages based on typical prereqs
                    this.nextSteps.add(ENextStep.MSG_1N);
                }
                this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
            } else if (this.placementStatus.attemptsRemaining > 0) {
                // Case B
                if (openingMessagePast == ENextStep.MSG_1A_PLURAL) {
                    // Use messages based on critical prereqs
                    this.nextSteps.add(ENextStep.MSG_1H);
                } else {
                    // Use messages based on typical prereqs
                    this.nextSteps.add(ENextStep.MSG_1O);
                }
                this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
            } else if (sequence.getPreArrivalCourses().containsKey(RawRecordConstants.M117)
                    && !sequence.getPreArrivalCourses()
                    .get(RawRecordConstants.M117).status.sufficient) {
                // Case C
                if (openingMessagePast == ENextStep.MSG_1A_PLURAL) {
                    // Use messages based on critical prereqs
                    if (numSem1 == 1) {
                        this.nextSteps.add(ENextStep.MSG_1L_SINGULAR);
                    } else {
                        this.nextSteps.add(ENextStep.MSG_1L_PLURAL);
                    }
                } else // Use messages based on typical prereqs
                    if (numSem1 == 1) {
                        this.nextSteps.add(ENextStep.MSG_1S_SINGULAR);
                    } else {
                        this.nextSteps.add(ENextStep.MSG_1S_PLURAL);
                    }
                this.nextSteps.add(ENextStep.ACT_ELM_TUTORIAL);
            } else // Case D
                if (numSem1 == 1) {
                    this.nextSteps.add(ENextStep.MSG_1M_SINGULAR);
                } else {
                    this.nextSteps.add(ENextStep.MSG_1M_PLURAL);
                }
            // this.nextSteps.add(ENextStep.ACT_CONTACT_MY_ADVISER);
        }
    }

    /**
     * Generates the next steps for students in case 2.1.
     *
     * @param activeTermYear the active term year
     * @param activeTermName the active term name
     * @param critical       the critical sequence of courses
     * @param recommend      the recommended sequence of courses
     * @param typical        the typical sequence of courses
     */
    private void createPlanCase2(final int activeTermYear, final ETermName activeTermName,
                                 final CourseSequence critical, final CourseSequence recommend,
                                 final CourseSequence typical) {

        final TermKey appTerm = this.student.aplnTerm;
        final int appTermYear = appTerm == null ? 0 : appTerm.year.intValue();
        final ETermName appTermName = appTerm == null ? null : appTerm.name;

        final boolean isInFuture;

        if (appTermYear > activeTermYear) {
            isInFuture = true;
        } else if (appTermYear == activeTermYear) {
            if (appTermName == ETermName.FALL) {
                isInFuture = activeTermName != ETermName.FALL;
            } else if (appTermName == ETermName.SUMMER) {
                isInFuture = activeTermName == ETermName.SPRING;
            } else {
                isInFuture = false;
            }
        } else {
            isInFuture = false;
        }

        if (critical.isCourseInSemester1(RawRecordConstants.M117)
                && !critical.getSemester1Courses().get(RawRecordConstants.M117).status.sufficient
                || critical.isGroupInSemester1("AGED3")
                || critical.isGroupInSemester1("ANIM3")) {

            // Case 2.1.1 or 2.2.1
            createPlanCase211Or212(critical, isInFuture ? ENextStep.MSG_2A : ENextStep.MSG_2F);
        } else if (recommend.isCourseInSemester1(RawRecordConstants.M117)
                && !recommend.getSemester1Courses().get(RawRecordConstants.M117).status.sufficient
                || recommend.isGroupInSemester1("AGED3")
                || recommend.isGroupInSemester1("ANIM3")) {

            // Case 2.1.2A or 2.2.1A
            createPlanCase211Or212(critical, isInFuture ? ENextStep.MSG_2B : ENextStep.MSG_2G);
        } else if (typical.isCourseInSemester1(RawRecordConstants.M117)
                && !typical.getSemester1Courses().get(RawRecordConstants.M117).status.sufficient
                || typical.isGroupInSemester1("AGED3")
                || typical.isGroupInSemester1("ANIM3")) {

            // Case 2.1.2B or 2.2.1B
            createPlanCase211Or212(critical, isInFuture ? ENextStep.MSG_2C : ENextStep.MSG_2H);
        } else {
            if (critical.isCourseInSemester1("M 101")
                    && !critical.getSemester1Courses().get("M 101")
                    .status.sufficient
                    || critical.isGroupInSemester1("AUCC2")
                    || critical.isGroupInSemester1("AUCC3")) {

                // Case 2.1.3 or 2.2.3
                this.nextSteps.add(isInFuture ? ENextStep.MSG_2D : ENextStep.MSG_2I);
            } else {
                // Case 2.1.4 or 2.2.4
                this.nextSteps.add(isInFuture ? ENextStep.MSG_2E : ENextStep.MSG_2J);
            }
            this.nextSteps.add(ENextStep.MSG_2Q);
            this.nextSteps.add(ENextStep.MSG_2R);
            this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
        }
    }

    /**
     * Creates the student plan for cases 2.1.1 or 2.1.2.
     *
     * @param sequence     a course sequence
     * @param firstMessage the first advice message to include
     */
    private void createPlanCase211Or212(final CourseSequence sequence,
                                        final ENextStep firstMessage) {

        this.nextSteps.add(firstMessage);

        if (!this.placementStatus.placementAttempted) {
            // 2.1.1A and 2.1.1B (or 2.2.1A and 2.2.1B)
            this.nextSteps.add(ENextStep.MSG_2K);

            final Collection<String> allCourses = new ArrayList<>(10);
            allCourses.addAll(sequence.getSemester1Courses().keySet());
            allCourses.addAll(sequence.getSemester2Courses().keySet());
            allCourses.addAll(sequence.getAdditionalCourses().keySet());

            final boolean needsBeyond118 = allCourses.contains(RawRecordConstants.M124)
                    || allCourses.contains(RawRecordConstants.M125)
                    || allCourses.contains(RawRecordConstants.M126);

            if (!needsBeyond118) {
                this.nextSteps.add(ENextStep.MSG_2L);
            }
            this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
            // FIXME: Restore this once we can recommend retrying placement
            //  } else if (isRecommendMPEPlace()) { // 2.1.1C (or 2.2.1C)
            //  this.nextSteps.add(ENextStep.MSG_2M);
            //  this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
            //  this.nextSteps.add(ENextStep.MSG_2N);
            //  this.nextSteps.add(ENextStep.ACT_ELM_TUTORIAL);
        } else if (this.placementStatus.attemptsRemaining > 0) {
            // 2.1.1D (or 2.2.1D)
            this.nextSteps.add(ENextStep.MSG_2O);
            this.nextSteps.add(ENextStep.ACT_ELM_TUTORIAL);
            this.nextSteps.add(ENextStep.MSG_2P);
            this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
        } else {
            // 2.1.1E (or 2.2.1E)
            this.nextSteps.add(ENextStep.MSG_2N);
            this.nextSteps.add(ENextStep.ACT_ELM_TUTORIAL);
        }
    }

    /**
     * Generates the next steps for students in case 3.
     *
     * @param typical the typical course sequence
     * @param logic   the site logic
     */
    private void createPlanCase3(final CourseSequence typical, final MathPlanLogic logic) {

        // [Check] No further action is needed.
        this.nextSteps.add(ENextStep.MSG_3A);

        if (typical.hasSemester1Data() || typical.hasSemester2Data()) {

            if (typical.isPrecalcCourseInSemester1() || typical.isPrecalcCourseInSemester2()) {

                // Case 3.3
                this.nextSteps.add(ENextStep.MSG_3G);

                if (needsOnly3CreditsMath(typical, logic) || isAllPrecalcCoursesNamed(typical)) {

                    // 3.3.1

                    if (this.placementStatus.attemptsRemaining > 0) {
                        this.nextSteps.add(ENextStep.MSG_3D);
                    }

                } else {
                    final Collection<String> allNeeded = new ArrayList<>(10);
                    for (final CourseInfo test : typical.getSemester1Courses().values()) {
                        if (!test.status.sufficient) {
                            allNeeded.add(test.course.course);
                        }
                    }
                    for (final CourseInfo test : typical.getSemester2Courses().values()) {
                        if (!test.status.sufficient) {
                            allNeeded.add(test.course.course);
                        }
                    }
                    for (final CourseInfo test : typical.getAdditionalCourses().values()) {
                        if (!test.status.sufficient) {
                            allNeeded.add(test.course.course);
                        }
                    }

                    final boolean needsPrecalc = allNeeded.contains(RawRecordConstants.M117)
                            || allNeeded.contains(RawRecordConstants.M118)
                            || allNeeded.contains(RawRecordConstants.M124)
                            || allNeeded.contains(RawRecordConstants.M125)
                            || allNeeded.contains(RawRecordConstants.M126);

                    // 3.3.2
                    if (needsPrecalc) {
                        if (!this.placementStatus.placementAttempted) {
                            this.nextSteps.add(ENextStep.MSG_3E);
                            this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
                        } else if (this.placementStatus.attemptsRemaining > 0) {
                            this.nextSteps.add(ENextStep.MSG_3F);
                            this.nextSteps.add(ENextStep.ACT_MATH_PLACEMENT_EXAM);
                        }
                    }
                }

            } else if (typical.needs101InSem1() || typical.needs101InSem2()) {

                // Case 3.2
                this.nextSteps.add(ENextStep.MSG_3H);
                this.nextSteps.add(ENextStep.MSG_3B);
                this.nextSteps.add(ENextStep.MSG_3C);
            }

            // ELSE Case 3.4, no additional messages needed

        } else // CASE 3.1
            if (this.canRegisterFor.contains(RawRecordConstants.M117)) {
                this.nextSteps.add(ENextStep.MSG_3G);
                this.nextSteps.add(ENextStep.MSG_3B);
            } else {
                this.nextSteps.add(ENextStep.MSG_3H);
                this.nextSteps.add(ENextStep.MSG_3B);
                this.nextSteps.add(ENextStep.MSG_3C);
            }
    }

    /**
     * Generates the next steps for students in case 4.
     */
    private void createPlanCase4() {

        this.nextSteps.add(ENextStep.MSG_4A);
        this.nextSteps.add(ENextStep.MSG_4B);
        this.nextSteps.add(ENextStep.MSG_4C);
    }

    /**
     * Tests whether the student only needs 3 credits of (core) mathematics.
     *
     * @param seq   the course sequence
     * @param logic the site logic
     * @return {@code true} if all credits needed belong to the AUCC core group, and only three are needed
     */
    private static boolean needsOnly3CreditsMath(final CourseSequence seq,
                                                 final MathPlanLogic logic) {

        final Map<String, RawCourse> courseData = logic.getCourses();
        final List<String> aucc = logic.getCourseGroups().get("AUCC3").courseNumbers;

        int num = 0;
        boolean allAucc = true;

        if (seq.getPreArrivalCourses().isEmpty()) {

            final Collection<String> courses = new ArrayList<>(10);
            courses.addAll(seq.getSemester1Courses().keySet());
            courses.addAll(seq.getSemester2Courses().keySet());
            courses.addAll(seq.getAdditionalCourses().keySet());

            for (final String crs : courses) {
                if (aucc.contains(crs)) {
                    final RawCourse course = courseData.get(crs);
                    if (course != null) {
                        num += course.nbrCredits.intValue();
                    }
                } else {
                    allAucc = false;
                    break;
                }
            }

            if (allAucc && num <= 3) {
                final Collection<CourseInfoGroup> groups = new ArrayList<>(5);
                groups.addAll(seq.getSemester1CourseGroups());
                groups.addAll(seq.getSemester2CourseGroups());

                for (final CourseInfoGroup grp : groups) {
                    if (grp.getGroupCode().startsWith("AUCC")) {
                        if (grp.getNumCredits() != null) {
                            num += grp.getNumCredits().intValue();
                        }
                    } else {
                        allAucc = false;
                        break;
                    }
                }
            }

        } else {
            allAucc = false;
        }

        return allAucc && num <= 3;
    }

    /**
     * Tests whether all Precalculus courses present were named in a major, not added as a prerequisite for a named
     * course.
     *
     * @param sequence the course sequence
     * @return {@code true} if all Precalculus courses are named (none are prereqs)
     */
    private static boolean isAllPrecalcCoursesNamed(final CourseSequence sequence) {

        boolean allNamed = false;

        final Collection<CourseInfo> all = new ArrayList<>(10);
        all.addAll(sequence.getPreArrivalCourses().values());
        all.addAll(sequence.getSemester1Courses().values());
        all.addAll(sequence.getSemester2Courses().values());
        all.addAll(sequence.getAdditionalCourses().values());

        for (final CourseInfo info : all) {
            final String id = info.course.course;

            if ((RawRecordConstants.M117.equals(id) || RawRecordConstants.M118.equals(id)
                    || RawRecordConstants.M124.equals(id) || RawRecordConstants.M125.equals(id)
                    || RawRecordConstants.M126.equals(id)) && info.addedAsPrerequisite) {

                allNamed = false;
                break;
            }

            // Only set this if there is at least one actual Precalculus course in the list
            allNamed = true;
        }

        return allNamed;
    }

    /**
     * Record statistics of this session for analysis.
     */
    private void recordAnalytics() {

        // Statistics Tracking...

        // Active term
        // Date/time of access
        // Student id
        // Application Term
        // Whether this is a rebuild due to change of settings or a new "site visit"
        // Selected majors
        // ACT/SAT
        // Placement attempts and results
        // Recommend retake MPE
        // Recommend MCE credit
        // Transfer credit
        // Eligible for courses
        // Eligible for tutorials
        // Requirements
        // Recommendations
        // Advice code

    }

    /**
     * Tests whether this entry has expired.
     *
     * @return {@code true} if the entry has expired
     */
    public boolean isExpired() {

        return this.expiry < System.currentTimeMillis();
    }

    /**
     * Gets the student's current profile responses.
     *
     * @return the profile responses
     */
    public Map<Integer, RawStmathplan> getMajorProfileResponses() {

        return this.majorProfileResponses;
    }

    /**
     * Gets the intentions profile responses in the student's math plan.
     *
     * @return the profile responses
     */
    public Map<Integer, RawStmathplan> getIntentions() {

        return this.intentionsResponses;
    }

    /**
     * Gets the student's completed course list.
     *
     * @return the completed course list
     */
    public List<LiveCsuCredit> getCompletedCourses() {

        return this.completedCourses;
    }

    /**
     * Gets the student's transfer credit list.
     *
     * @return the transfer credit list
     */
    public List<LiveTransferCredit> getLiveTransferCredit() {

        return this.liveTransferCredit;
    }

    /**
     * Counts transfer credits.
     *
     * @return the total number of transfer credits.
     */
    double countTransferCredits() {

        double total = 0.0;

        for (final LiveTransferCredit rec : this.liveTransferCredit) {
            if (rec.credits != null) {
                total += rec.credits.doubleValue();
            }
        }

        return total;
    }

    /**
     * Gets the student's placement credit list.
     *
     * @return the placement credit list
     */
    public List<RawMpeCredit> getPlacementCredit() {

        return this.placementCredit;
    }

    /**
     * Gets the computed course math requirements.
     *
     * @return the requirements; {@code null} if never computed
     */
    public List<MajorMathRequirement> getRequirements() {

        return this.requirements;
    }

    /**
     * Shows the major selections when the student has not yet selected any majors.
     *
     * @param declaredProgramCode the student's declared program code
     * @param curResponses        the current student responses (empty if not yet responded)
     * @param logic               the site logic
     * @return a list containing all math requirements for all selected majors
     */
    private static List<MajorMathRequirement> accumulateRequirements(
            final String declaredProgramCode, final Map<Integer, RawStmathplan> curResponses,
            final MathPlanLogic logic) {

        final Map<Major, MajorMathRequirement> allMajors = logic.getMajors();

        final Major declaredMajor = logic.getMajor(declaredProgramCode);
        final List<MajorMathRequirement> requirements = new ArrayList<>(10);

        if (declaredMajor != null) {
            requirements.add(allMajors.get(declaredMajor));
        }

        for (final Map.Entry<Major, MajorMathRequirement> entry : allMajors.entrySet()) {
            final RawStmathplan curResp = curResponses.get(entry.getKey().questionNumber);
            final boolean selected = curResp != null && "Y".equals(curResp.stuAnswer);
            if (selected && !entry.getKey().equals(declaredMajor)) {
                requirements.add(entry.getValue());
            }
        }

        return requirements;
    }

    /**
     * Gets the set of courses (through Calculus I) that the student is eligible to register for right now.
     *
     * @return the set of course IDs ({@code null} if none)
     */
    public Set<String> getCanRegisterFor() {

        return this.canRegisterFor;
    }

    /**
     * Gets the number of core credits the student has completed.
     *
     * @return the number of completed credits
     */
    double getCreditsOfCoreCompleted() {

        return this.creditsOfCoreCompleted;
    }

    /**
     * Gets the list of next steps for the student.
     *
     * @return the list of next steps
     */
    public List<ENextStep> getNextSteps() {

        return this.nextSteps;
    }
}
