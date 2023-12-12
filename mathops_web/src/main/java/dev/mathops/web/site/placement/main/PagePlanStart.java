package dev.mathops.web.site.placement.main;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.mathplan.MathPlanLogic;
import dev.mathops.session.sitelogic.mathplan.data.StudentData;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates the page that presents the student's plan.
 */
enum PagePlanStart {
    ;

    /** The input name for the "only a recommendation" form checkbox. */
    private static final String INPUT_NAME = "only-rec";

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

        final MathPlanLogic logic = new MathPlanLogic(site.getDbProfile());

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
    private static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session,
                              final MathPlanLogic logic) throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final StudentData data = logic.getStudentData(cache, stuId, session);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);

        MPPage.emitMathPlanHeader(htm);

        if (data == null) {
            MPPage.emitNoStudentDataError(htm);
        } else {
            MathPlacementSite.emitLoggedInAs2(htm, session);

            htm.sDiv("inset2");
            emitPlanSteps(htm, data);
            htm.eDiv(); // inset2
        }

        MPPage.emitScripts(htm, //
                "function pick(target) {",
                "  window.location.assign(target);",
                "}");
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Emits content that describes the steps involved in making a math plan.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param data student math plan data
     */
    private static void emitPlanSteps(final HtmlBuilder htm, final StudentData data) {

        final Map<Integer, RawStmathplan> majorResponses = data.getMajorProfileResponses();
        final boolean viewedExisting = data.viewedExisting;
        final boolean onlyRecommendation = data.checkedOnlyRecommendation;
        final Map<Integer, RawStmathplan> intentionsResponses = data.getIntentions();

        final boolean doneStep1 = !majorResponses.isEmpty();
        final boolean doneStep2 = doneStep1 && viewedExisting;
        final boolean doneStep3 = doneStep2 && onlyRecommendation;
        final boolean doneStep4 = doneStep3 && !intentionsResponses.isEmpty();

        htm.sDiv("shaded2left");

        htm.sP("grow")
                .add("Creating your personalized math plan will help you identify the ",
                        "mathematics course(s) that best match your mathematical preparation with ",
                        "your academic goals, and will help determine if the Math Placement ",
                        "process is right for you.")
                .eP();

        htm.sP("grow")
                .add("Creating your plan takes four simple steps.  You can come back ",
                        "and change your selections at any time to update your plan.")
                .eP();

        htm.sDiv("center", "style='min-width:256px;'");

        htm.add("<ul class='stepnum' id='bluesteps' aria-hidden='true'>");
        htm.add(" <li class='", doneStep1 ? "lit" : "dim",
                "' aria-hidden='true'>1</li>");
        htm.add(" <li class='", doneStep2 ? "lit" : "dim",
                "' aria-hidden='true'>2</li>");
        htm.add(" <li class='", doneStep3 ? "lit" : "dim",
                "' aria-hidden='true'>3</li>");
        htm.add(" <li class='", doneStep4 ? "lit" : "dim",
                "' aria-hidden='true'>4</li>");
        htm.add("</ul>");

        htm.add("<nav class='foursteps'>");
        htm.add("<button class='foursteps' id='first' ",
                "onclick='pick(\"plan_majors1.html\");'>",
                "<span class='sr-only'>Step 1:</span>", //
                "Tell us your<br/>preferred<br/>major(s).</button>");
        htm.add("<button class='foursteps' ");
        if (!doneStep1) {
            htm.add(" disabled");
        }
        htm.add(" onclick='pick(\"plan_record.html\");'>",
                "<span class='sr-only'>Step 2:</span>",
                "Tell us about<br/>transfer credit,<br/>test scores.</button>");

        htm.add("<button class='foursteps' ");
        if (!doneStep2) {
            htm.add(" disabled");
        }
        htm.add(" onclick='pick(\"plan_view.html\");'>",
                "<span class='sr-only'>Step 3:</span>", //
                "Generate your<br/>personalized<br/>math plan.</button>");

        htm.add("<button class='foursteps' id='last' ");
        if (!doneStep3) {
            htm.add(" disabled");
        }
        htm.add(" onclick='pick(\"plan_next.html\");'>",
                "<span class='sr-only'>Step 4:</span>", //
                "Review your<br/>recommended<br/>next steps.</button>");
        htm.add("</nav>");

        // Prompts below the four steps
        htm.sDiv("stepprompts", "aria-hidden='true'");

        if (doneStep1) {
            htm.sDiv("stepprompt check", "id='first'");
        } else {
            htm.sDiv("stepprompt arrow", "id='first'");
            htm.add("<span class='hidebelow500'>Start<br class='hideabove800'>Here...</span>");
        }
        htm.eDiv();

        if (doneStep2) {
            htm.sDiv("stepprompt check");
        } else if (doneStep1) {
            htm.sDiv("stepprompt arrow");
            htm.add("<span class='hidebelow500'>Next<br class='hideabove800'>Step...</span>");
        } else {
            htm.sDiv("stepprompt");
        }
        htm.eDiv();

        if (doneStep3) {
            htm.sDiv("stepprompt check");
        } else if (doneStep2) {
            htm.sDiv("stepprompt arrow");
            htm.add("<span class='hidebelow500'>Next<br class='hideabove800'>Step...</span>");
        } else {
            htm.sDiv("stepprompt");
        }
        htm.eDiv();

        if (doneStep4) {
            htm.sDiv("stepprompt check", "id='last'");
        } else if (doneStep3) {
            htm.sDiv("stepprompt arrow", "id='last'");
            htm.add("<span class='hidebelow500'>Last<br class='hideabove800'>Step...</span>");
        } else {
            htm.sDiv("stepprompt", "id='last'");
        }
        htm.eDiv();

        htm.eDiv(); // stepprompts

        htm.eDiv(); // center

        htm.eDiv(); // shaded2left
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

        final MathPlanLogic logic = new MathPlanLogic(site.getDbProfile());

        // Only perform updates if this is not an adviser using "Act As"
        if (session.actAsUserId == null) {
            final String studentId = session.getEffectiveUserId();

            final StudentData data = logic.getStudentData(cache, studentId, session);

            if (req.getParameter(INPUT_NAME) != null) {

                final Integer key = Integer.valueOf(1);

                final Map<Integer, RawStmathplan> existing = MathPlanLogic
                        .getMathPlanResponses(cache, studentId, MathPlanLogic.ONLY_RECOM_PROFILE);

                if (!existing.containsKey(key)) {

                    final List<Integer> questions = new ArrayList<>(1);
                    final List<String> answers = new ArrayList<>(1);

                    questions.add(key);
                    answers.add("Y");
                    logic.storeMathPlanResponses(cache, data.student,
                            MathPlanLogic.ONLY_RECOM_PROFILE, questions, answers, session);

                    data.recordPlan(cache, logic, session);
                }
            }
        }

        doGet(cache, site, req, resp, session, logic);
    }
}
