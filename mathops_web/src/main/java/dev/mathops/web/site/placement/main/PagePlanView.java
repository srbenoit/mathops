package dev.mathops.web.site.placement.main;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.logic.mathplan.MathPlanLogic;
import dev.mathops.db.old.logic.mathplan.data.CourseInfo;
import dev.mathops.db.old.logic.mathplan.data.CourseInfoGroup;
import dev.mathops.db.old.logic.mathplan.data.CourseRecommendations;
import dev.mathops.db.old.logic.mathplan.data.CourseSequence;
import dev.mathops.db.old.logic.mathplan.data.ENextStep;
import dev.mathops.db.old.logic.mathplan.data.Major;
import dev.mathops.db.old.logic.mathplan.data.MajorMathRequirement;
import dev.mathops.db.old.logic.mathplan.data.MathPlanConstants;
import dev.mathops.db.old.logic.mathplan.data.MathPlanStudentData;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Generates the page that allows the user to affirm some statements then view their plan.
 */
enum PagePlanView {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final MathPlanLogic logic = new MathPlanLogic(site.site.profile);

        doGet(cache, site, req, resp, session, logic);
    }

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @param logic   the site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final MathPlanLogic logic) throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final MathPlanStudentData data = logic.getStudentData(cache, stuId, session.getNow(), session.loginSessionTag,
                session.actAsUserId == null);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);
        MPPage.emitMathPlanHeader(htm);

        if (data == null) {
            MPPage.emitNoStudentDataError(htm);
        } else {
            MathPlacementSite.emitLoggedInAs2(htm, session);
            htm.sDiv("inset2");

            final Map<Integer, RawStmathplan> existing = MathPlanLogic.getMathPlanResponses(cache,
                    session.getEffectiveUserId(), MathPlanConstants.ONLY_RECOM_PROFILE);

            if (existing.containsKey(Integer.valueOf(1))) {
                showPlan(cache, session, htm, logic);
            } else {
                showAffirmations(htm);
            }

            htm.eDiv(); // inset2
        }

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Displays a box with affirmations that must be acknowledged before viewing the plan.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void showAffirmations(final HtmlBuilder htm) {

        htm.sDiv("shaded2left");
        htm.sP().add("We have created a personalized Mathematics Plan for you, based on the mathematics courses ",
                "required for any of the majors you selected. Following this plan keeps your options open to choose ",
                "any of these majors.").eP();
        htm.eDiv(); // shaded2left

        htm.div("vgap");

        htm.sDiv("advice");
        htm.addln("<form action='plan_view.html' method='post'>");
        htm.addln("<input type='hidden' name='cmd' value='", MathPlanConstants.ONLY_RECOM_PROFILE, "'/>");

        htm.add("Read and affirm each statement to access your Math Plan...");

        htm.sP().add("<input type='checkbox' id='affirm1' onclick='affirmed();'> &nbsp; <label for='affirm1'>",
                "I understand that I must satisfy the All-University Core Curriculum requirement in ",
                "Quantitative Reasoning to graduate from CSU, regardless of the major I choose.</label>").eP();
        htm.sP().add("<input type='checkbox' id='affirm2' onclick='affirmed();'> &nbsp; <label for='affirm2'>",
                "I understand that this Math Plan is only a recommendation.  The math requirements ",
                "for each degree program can change over time, and should be verified with the ",
                "University Catalog.</label>").eP();
        htm.sP().add("<input type='checkbox' id='affirm3' onclick='affirmed();'> &nbsp; <label for='affirm3'>",
                "I understand that if Math Placement is required based on my plan, it should be ",
                "completed before Orientation or course registration, even if AP, IB, or transfer ",
                "credit is pending or expected.</label>").eP();
        htm.sP().add("<input type='checkbox' id='affirm4' onclick='affirmed();'> &nbsp; <label for='affirm4'>",
                "I understand that I will not be able to register for math or science courses ",
                "until I have satisfied their prerequisites, which may require completing Math ",
                "Placement.</label>").eP();

        htm.sDiv("center");
        htm.addln("<button type='submit' id='affirmsubmit' class='btn' disabled>View my completed plan...</button>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln(" function affirmed() {");
        htm.addln("  document.getElementById('affirmsubmit').disabled =");
        htm.addln("  !(   document.getElementById('affirm1').checked");
        htm.addln("    && document.getElementById('affirm2').checked");
        htm.addln("    && document.getElementById('affirm3').checked");
        htm.addln("    && document.getElementById('affirm4').checked);");
        htm.addln(" }");
        htm.addln("</script>");
        htm.addln("</form>");
        htm.eDiv();
        htm.div("vgap");
    }

    /**
     * Displays the finished plan.
     *
     * @param cache   the data cache
     * @param session the session
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param logic   the site logic
     * @throws SQLException if there is an error accessing the database
     */
    private static void showPlan(final Cache cache, final ImmutableSessionInfo session, final HtmlBuilder htm,
                                 final MathPlanLogic logic) throws SQLException {

        final String stuId = session.getEffectiveUserId();
        final MathPlanStudentData data = logic.getStudentData(cache, stuId, session.getNow(), session.loginSessionTag,
                session.actAsUserId == null);

        htm.sDiv("shaded2left");

        htm.sDiv("left welcome", "style='margin-bottom:0;'");
        htm.add("Your <span class='hidebelow700'>Personalized</span> Math Plan:");
        htm.eDiv();
        htm.sDiv("right welcome", "style='margin-bottom:0;'");
        final LocalDate today = LocalDate.now();
        htm.add(TemporalUtils.FMT_MDY.format(today));
        htm.eDiv();
        htm.div("clear");
        htm.hr();

        final int numSelected = emitMajors(htm, data, logic);

        final List<MajorMathRequirement> requirements = data.getRequirements();

        // Now display the computed mathematics recommendations
        if (!requirements.isEmpty()) {

            htm.div("vgap");
            htm.hr();
            htm.div("vgap");

            htm.sDiv("planbox");

            emitResultsHeader(htm, numSelected);

            // Now display the computed mathematics recommendations

            htm.hr();
            if (data.checkedOnlyRecommendation) {
                emitPlan(htm, data, logic, numSelected);

                htm.div("clear");
                htm.div("vgap");

                htm.addln("<form action='plan_next.html' method='get'>");
                htm.sDiv("center");
                htm.addln("<input type='hidden' name='cmd' value='", MathPlanConstants.EXISTING_PROFILE, "'/>");

                htm.addln("<button type='submit' class='btn'>Go to the next step...</button>");
                htm.eDiv();
                htm.addln("</form>");
                htm.eDiv(); // shaded2
            } else {
                htm.sDiv("center");
                htm.addln("<b>To see your next steps, read the statement above and check the box to agree.</b>");
                htm.eDiv();
                htm.div("vgap");
            }

            htm.eDiv();

            htm.div(null, "id='end'");
        }

        htm.eDiv(); // shaded2left
    }

    /**
     * Displays the finished plan in a brief form.
     *
     * @param cache   the data cache
     * @param session the session
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param logic   the site logic
     * @throws SQLException if there is an error accessing the database
     */
    static void showBriefPlan(final Cache cache, final ImmutableSessionInfo session,
                              final HtmlBuilder htm, final MathPlanLogic logic) throws SQLException {

        final String stuId = session.getEffectiveUserId();
        final MathPlanStudentData data = logic.getStudentData(cache, stuId, session.getNow(), session.loginSessionTag,
                session.actAsUserId == null);

        htm.sDiv("indent");

        final int numSelected = emitMajors(htm, data, logic);

        final List<MajorMathRequirement> requirements = data.getRequirements();
        if (!requirements.isEmpty()) {

            htm.div("vgap");
            htm.sDiv("planbox");

            if (data.checkedOnlyRecommendation) {
                emitPlan(htm, data, logic, numSelected);
                PagePlanNext.showNextStepsBrief(htm, data);
            } else {
                htm.sDiv("center");
                htm.addln("<b>To see your next steps, read the statement above and check the box to agree.</b>");
                htm.eDiv();
            }

            htm.eDiv(); // planbox

            htm.div(null, "id='end'");
        }

        htm.eDiv(); // indent
    }

    /**
     * Emits the student's majors of interest and declared major.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param data  the student data
     * @param logic the site logic
     * @return the number of selected majors of interest
     */
    private static int emitMajors(final HtmlBuilder htm, final MathPlanStudentData data, final MathPlanLogic logic) {

        // See if the student has already submitted a list of majors
        final Map<Integer, RawStmathplan> profileResponses = data.getMajorProfileResponses();

        htm.sP().add("<strong>I am interested in majoring in...</strong>").eP();

        htm.sDiv();
        final int numSelected = emitCurrentMajorSelections(htm, profileResponses, data, logic);
        htm.eDiv();

        final String declaredProgram = data.student.programCode;
        final Major declaredMajor = logic.getMajor(declaredProgram);
        htm.sP("advice");
        htm.add("Current declared major: <strong>");
        if (declaredMajor == null) {
            htm.add("none");
        } else {
            htm.add(declaredMajor.majorName);
            if (declaredMajor.concentrationName != null) {
                htm.add(", ", declaredMajor.concentrationName, " Concentration");
            }
        }
        htm.addln("</strong>").eP();

        return numSelected;
    }

    /**
     * Emits the header of the results box.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param numSelected the number of selected majors of interest
     */
    private static void emitResultsHeader(final HtmlBuilder htm, final int numSelected) {

        htm.sP();
        if (numSelected == 1) {
            htm.add("This plan is based on the mathematics courses required by the major you selected.");
        } else {
            htm.add("This plan is based on the mathematics courses required for <b>any</b> of the majors you ",
                    "selected.  Following this plan keeps your options open to choose any of the majors listed above.");
        }
        htm.eP();
    }

    /**
     * Emits the student's complete plan.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param data        the student data
     * @param logic       the site logic
     * @param numSelected the number of selected majors of interest
     */
    private static void emitPlan(final HtmlBuilder htm, final MathPlanStudentData data, final MathPlanLogic logic,
                                 final int numSelected) {

        final Map<String, RawCourse> courses = logic.getCourses();
        final CourseRecommendations recommendations = data.recommendations;
        final CourseSequence typical = recommendations.typicalSequence;

        if (typical.hasPreArrivalData() > 0) {
            htm.div("vgap0");

            htm.sP().add("<strong>Prior to your first semester</strong>, your goal should be to ",
                    "have these prerequisites satisfied by ");

            if (data.getNextSteps().contains(ENextStep.ACT_PRECALCULUS_TUTORIAL)) {
                if (data.getNextSteps().contains(ENextStep.ACT_MATH_PLACEMENT_EXAM)) {
                    htm.add("Math Placement, the Precalculus Tutorial, or by transfer or exam credit:");
                } else {
                    htm.add("the Precalculus Tutorial or by transfer or exam credit:");
                }
            } else if (data.getNextSteps().contains(ENextStep.ACT_MATH_PLACEMENT_EXAM)) {
                htm.add("Math Placement or by transfer or exam credit:");
            } else {
                htm.add("transfer or exam credit:");
            }
            htm.eP();

            emitCourseList(htm, typical.hasMultipleCalc1(), typical.hasMultipleCalc2(), typical.getPreArrivalCourses(),
                    null, courses);
        }

        if (typical.hasSemester1Data()) {
            if (typical.hasPreArrivalData() > 0) {
                htm.div("vgap0");
            }

            htm.sP();
            if (typical.isPrecalcCourseInSemester1() && data.placementStatus.attemptsRemaining > 0) {
                htm.add("<strong>During your first semester</strong>, your goal should be to take ",
                        "these courses, or to place out of them through Math Placement:");
            } else {
                htm.add("<strong>During your first semester</strong>, your goal should be to take:");
            }
            htm.eP();

            emitCourseList(htm, typical.hasMultipleCalc1(), typical.hasMultipleCalc2(),
                    typical.getSemester1Courses(), typical.getSemester1CourseGroups(), courses);
        }

        if (typical.hasSemester2Data()) {
            if (typical.hasPreArrivalData() > 0 || typical.hasSemester1Data()) {
                htm.div("vgap0");
            }
            htm.sP().add("<strong>During your second semester</strong>, your goal should be to take:").eP();

            emitCourseList(htm, typical.hasMultipleCalc1(), typical.hasMultipleCalc2(),
                    typical.getSemester2Courses(), typical.getSemester2CourseGroups(), courses);
        }

        if (typical.hasAdditionalData()) {
            if (typical.hasPreArrivalData() > 0 || typical.hasSemester1Data() || typical.hasSemester2Data()) {
                htm.div("vgap0");
            }
            htm.sP();
            if (numSelected == 1) {
                htm.add("Other mathematics courses required by your selected major:");
            } else {
                htm.add("Other mathematics courses required by one or more of your selected majors:");
            }
            htm.eP();

            emitCourseList(htm, typical.hasMultipleCalc1(), typical.hasMultipleCalc2(),
                    typical.getAdditionalCourses(), typical.getAdditionalCourseGroups(), courses);
        }
    }

    /**
     * Emits the user's current major selections.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param curResponses the current student responses (empty if not yet responded)
     * @param data         the student data
     * @param logic        the site logic
     * @return the number of majors selected
     */
    private static int emitCurrentMajorSelections(final HtmlBuilder htm, final Map<Integer, RawStmathplan> curResponses,
                                                  final MathPlanStudentData data, final MathPlanLogic logic) {

        int count = 0;
        final Map<Major, MajorMathRequirement> majors = logic.getMajors();

        final Major declared = logic.getMajor(data.student.programCode);

        final Collection<String> printedMajors = new ArrayList<>(10);

        htm.addln("<ul style='margin:0 0 10px 0;'>");

        boolean inConcentration = false;
        for (final Map.Entry<Major, MajorMathRequirement> entry : majors.entrySet()) {
            final Major major = entry.getKey();

            if (major.equals(declared) || curResponses.containsKey(major.questionNumber)) {

                if (major.concentrationName == null) {
                    // This is a top-level major - record the fact we're printing it
                    printedMajors.add(major.majorName);
                } else if (!printedMajors.contains(major.majorName)) {
                    // We're printing a concentration whose top-level major has not been printed,
                    // so print the top-level major first

                    for (final Major inner : majors.keySet()) {
                        if (inner.concentrationName == null && inner.majorName.equals(major.majorName)) {

                            htm.add("<li>");
                            if (inner.catalogUrl == null) {
                                htm.add(inner.majorName);
                            } else {
                                htm.add("<a target='_blank' href='", inner.catalogUrl, "'>");
                                htm.add(inner.majorName);
                                htm.addln("</a>");
                            }
                            htm.addln("</li>");
                            break;
                        }
                    }
                }

                if (inConcentration && major.concentrationName == null) {
                    htm.add("</ul>");
                    inConcentration = false;
                } else if (!inConcentration && major.concentrationName != null) {
                    htm.add("<ul>");
                    inConcentration = true;
                }

                htm.add("<li style='margin-bottom:3px;'>");
                if (major.catalogUrl == null) {
                    if (major.concentrationName == null) {
                        htm.add(major.majorName);
                    } else {
                        htm.add(major.concentrationName, " Concentration");
                    }
                } else {
                    htm.add("<a target='_blank' href='", major.catalogUrl, "'>");
                    if (major.concentrationName == null) {
                        htm.add(major.majorName);
                    } else {
                        htm.add(major.concentrationName, " Concentration");
                    }
                    htm.addln("</a>");
                }

                htm.addln("</li>");

                ++count;
            }
        }
        htm.addln("</ul>");

        return count;
    }

    /**
     * Emits a list of course recommendations.
     *
     * @param htm              the {@code HtmlBuilder} to which to append
     * @param hasMultipleCalc1 {@code true} if course sequence includes more than one calculus 1 course
     * @param hasMultipleCalc2 {@code true} if course sequence includes more than one calculus 2 course
     * @param typicalCourses   the set of courses for a typical student
     * @param typicalGroups    the set of course groups for a typical student
     * @param courseData       a map from course ID to course data
     */
    private static void emitCourseList(final HtmlBuilder htm, final boolean hasMultipleCalc1,
                                       final boolean hasMultipleCalc2, final Map<String, CourseInfo> typicalCourses,
                                       final Iterable<CourseInfoGroup> typicalGroups,
                                       final Map<String, RawCourse> courseData) {

        htm.addln("<ul>");
        for (final Map.Entry<String, CourseInfo> entry : typicalCourses.entrySet()) {
            final CourseInfo info = entry.getValue();
            if (info.status.sufficient) {
                continue;
            }

            final RawCourse crs = courseData.get(entry.getKey());

            final String id = crs.course;
            final boolean isCalc1 = "M 141".equals(id) || "M 155".equals(id) || "M 156".equals(id)
                    || "M 160".equals(id);
            final boolean isCalc2 = "M 255".equals(id) || "M 256".equals(id) || "M 161".equals(id);

            htm.add("<li>");
            if (isCalc1 && hasMultipleCalc1) {
                htm.add("The <strong>Calculus I</strong> course appropriate for your major");
            } else if (isCalc2 && (hasMultipleCalc1 || hasMultipleCalc2)) {
                htm.add("The <strong>Calculus II</strong> course appropriate for your major");
            } else if (crs.getCatalogUrl() == null) {
                htm.add(crs.courseLabel, "<span class='hidebelow500'>: ", crs.courseName, "</span>");
            } else {
                htm.add("<a target='_blank' href='", crs.getCatalogUrl(), "'>", crs.courseLabel,
                        "</a><span class='hidebelow500'>: ", crs.courseName, "</span>");
            }

            htm.addln("</li>");
        }

        if (typicalGroups != null) {
            for (final CourseInfoGroup g : typicalGroups) {
                if (g.isSatisfied()) {
                    continue;
                }

                final String code = g.getGroupCode();
                final boolean isCalc1 = "CALC".equals(code) || "CALC1BIO".equals(code);
                final boolean isCalc2 = "CALC2BIO".equals(code);

                htm.add("<li>");
                if (isCalc1 && hasMultipleCalc1) {
                    htm.add("The <strong>Calculus I</strong> course appropriate for your major");
                } else if (isCalc2 && (hasMultipleCalc1 || hasMultipleCalc2)) {
                    htm.add("The <strong>Calculus II</strong> course appropriate for your major");
                } else {
                    htm.add(g.getCourseGroup().toString(courseData));
                }

                htm.addln("</li>");
            }
        }
        htm.addln("</ul>");
    }

    /**
     * Called when a POST is received to the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final Profile dbProfile = site.site.profile;
        final MathPlanLogic logic = new MathPlanLogic(dbProfile);

        final String cmd = req.getParameter("cmd");

        // Only perform updates if this is not an adviser using "Act As"
        if (session.actAsUserId == null && (MathPlanConstants.EXISTING_PROFILE.equals(cmd)
                || MathPlanConstants.ONLY_RECOM_PROFILE.equals(cmd))) {

            final String studentId = session.getEffectiveUserId();
            final ZonedDateTime sessNow = session.getNow();
            final MathPlanStudentData data = logic.getStudentData(cache, studentId, sessNow, session.loginSessionTag, true);
            final Integer key = Integer.valueOf(1);

            final Map<Integer, RawStmathplan> existing = MathPlanLogic.getMathPlanResponses(cache, studentId, cmd);

            if (!existing.containsKey(key)) {

                final List<Integer> questions = new ArrayList<>(1);
                final List<String> answers = new ArrayList<>(1);

                questions.add(key);
                answers.add("Y");
                logic.storeMathPlanResponses(cache, data.student, cmd, questions, answers, sessNow,
                        session.loginSessionTag);

                data.recordPlan(cache, logic, sessNow, studentId, session.loginSessionTag);
            }
        }

        doGet(cache, site, req, resp, session, logic);
    }
}
