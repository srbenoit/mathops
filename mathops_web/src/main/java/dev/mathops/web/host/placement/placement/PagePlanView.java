package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.mathplan.Major;
import dev.mathops.db.logic.mathplan.Majors;
import dev.mathops.db.logic.mathplan.MajorsCurrent;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.StudentMathPlan;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
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

        final String stuId = session.getEffectiveUserId();
        final StudentData studentData = cache.getStudent(stuId);
        final StudentMathPlan plan = MathPlanLogic.queryPlan(cache, stuId);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        final String title = site.getTitle();
        Page.startNofooterPage(htm, title, session, true, Page.NO_BARS, null, false, false);
        MPPage.emitMathPlanHeader(htm);

        MathPlacementSite.emitLoggedInAs2(htm, session);
        htm.sDiv("inset2");

        final Map<Integer, RawStmathplan> existing =
                studentData.getLatestMathPlanResponsesByPage(MathPlanConstants.ONLY_RECOM_PROFILE);

        if (existing.containsKey(Integer.valueOf(1))) {
            showPlan(cache, session, htm, plan);
        } else {
            showAffirmations(htm);
        }

        htm.eDiv(); // inset2

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
        htm.eDiv();

        htm.div("vgap");

        htm.sDiv("advice");
        htm.addln("<form action='plan_view.html' method='post'>");
        htm.addln("<input type='hidden' name='cmd' value='", MathPlanConstants.ONLY_RECOM_PROFILE, "'/>");

        htm.add("Read and affirm each statement to access your Math Plan...");

        htm.sP().add("<input type='checkbox' id='affirm1' onclick='affirmed();'> &nbsp; <label for='affirm1'>",
                "I understand that I must satisfy the All-University Core Curriculum requirement ",
                "(Category 1B) to graduate from CSU, regardless of the major I choose.</label>").eP();
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
     * @param plan    the student math plan
     * @throws SQLException if there is an error accessing the database
     */
    private static void showPlan(final Cache cache, final ImmutableSessionInfo session, final HtmlBuilder htm,
                                 final StudentMathPlan plan) throws SQLException {

        final String stuId = session.getEffectiveUserId();
        final StudentData studentData = cache.getStudent(stuId);

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

        final int numSelected = emitMajors(htm, studentData, plan);

        // Now display the computed mathematics recommendations
        if (plan.recommendedEligibility != null) {

            htm.div("vgap");
            htm.hr();
            htm.div("vgap");

            emitResultsHeader(htm, numSelected);

            // Now display the computed mathematics recommendations

            htm.hr();
            if (plan.checkedOnlyRecommendation) {
                PagePlanNext.showNextSteps(cache, htm, plan);

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

            htm.div(null, "id='end'");
        }

        htm.eDiv();
    }

    /**
     * Displays the finished plan in a brief form.
     *
     * @param cache   the data cache
     * @param session the session
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param plan    the student math plan
     * @throws SQLException if there is an error accessing the database
     */
    static void showBriefPlan(final Cache cache, final ImmutableSessionInfo session,
                              final HtmlBuilder htm, final StudentMathPlan plan) throws SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        htm.sDiv("indent");

        emitMajors(htm, plan);

        if (plan.recommendedEligibility != null) {
            if (plan.checkedOnlyRecommendation) {
                PagePlanNext.showNextSteps(cache, htm, plan);
            } else {
                htm.div("vgap");
                htm.sDiv("center");
                htm.addln("<b>To see your next steps, read the statement above and check the box to agree.</b>");
                htm.eDiv();
            }

            htm.div(null, "id='end'");
        }

        htm.eDiv(); // indent
    }

    /**
     * Emits the student's majors of interest and declared major.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param studentData the student data
     * @param plan        the student math plan
     * @return the number of selected majors of interest
     * @throws SQLException if there is an error accessing the database
     */
    private static int emitMajors(final HtmlBuilder htm, final StudentData studentData, final StudentMathPlan plan) throws SQLException {

        // See if the student has already submitted a list of majors
        final Map<Integer, RawStmathplan> profileResponses =
                studentData.getLatestMathPlanResponsesByPage(MathPlanConstants.MAJORS_PROFILE);

        htm.sP().add("<strong>I am interested in majoring in...</strong>").eP();

        htm.sDiv();
        final int numSelected = emitCurrentMajorSelections(htm, profileResponses, studentData, plan);
        htm.eDiv();

        final RawStudent student = studentData.getStudentRecord();
        final Major declared = Majors.getMajorByProgramCode(student.programCode);

        htm.sP("advice");
        htm.add("Current declared major: <strong>");
        if (declared == null) {
            htm.add("none");
        } else {
            htm.add(declared.programName);
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
     * Emits the user's current major selections.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param curResponses the current student responses (empty if not yet responded)
     * @param studentData  the student data
     * @param plan         the student math plan
     * @return the number of majors selected
     * @throws SQLException if there is an error accessing the database
     */
    private static int emitCurrentMajorSelections(final HtmlBuilder htm, final Map<Integer, RawStmathplan> curResponses,
                                                  final StudentData studentData, final StudentMathPlan plan)
            throws SQLException {

        int count = 0;

        final RawStudent student = studentData.getStudentRecord();
        final Major declared = Majors.getMajorByProgramCode(student.programCode);

        final List<Major> majors = MajorsCurrent.INSTANCE.getMajors();
        final Collection<String> printedMajors = new ArrayList<>(10);

        htm.addln("<ul style='margin:0 0 10px 0;'>");

        for (final Major major : majors) {
            boolean selected = major.equals(declared);
            if (!selected) {
                for (final int q : major.questionNumbers) {
                    final Integer qObj = Integer.valueOf(major.questionNumbers[0]);
                    if (curResponses.containsKey(qObj)) {
                        selected = true;
                        break;
                    }
                }
            }

            if (selected) {
                printedMajors.add(major.programName);

                htm.add("<li style='margin-bottom:3px;'>");
                if (major.catalogPageUrl == null) {
                    htm.add(major.programName);
                } else {
                    htm.add("<a target='_blank' href='", major.catalogPageUrl, "'>");
                    htm.add(major.programName);
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

        final String cmd = req.getParameter("cmd");

        // Only perform updates if this is not an adviser using "Act As"
        if (session.actAsUserId == null && (MathPlanConstants.EXISTING_PROFILE.equals(cmd)
                                            || MathPlanConstants.ONLY_RECOM_PROFILE.equals(cmd))) {

            final String stuId = session.getEffectiveUserId();
            final StudentData studentData = cache.getStudent(stuId);

            final ZonedDateTime now = session.getNow();
            final Integer key = Integer.valueOf(1);

            final Map<Integer, RawStmathplan> existing = studentData.getLatestMathPlanResponsesByPage(cmd);

            if (!existing.containsKey(key)) {
                final List<Integer> questions = new ArrayList<>(1);
                final List<String> answers = new ArrayList<>(1);

                questions.add(key);
                answers.add("Y");
                final RawStudent student = studentData.getStudentRecord();
                MathPlanLogic.storeMathPlanResponses(cache, student, cmd, questions, answers, now,
                        session.loginSessionTag);

                data.recordPlan(cache, logic, now, stuId, session.loginSessionTag);
            }
        }

        doGet(cache, site, req, resp, session);
    }
}
