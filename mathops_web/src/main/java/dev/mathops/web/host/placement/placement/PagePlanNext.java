package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.logic.mathplan.EEligibility;
import dev.mathops.db.logic.mathplan.ENextStep;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.logic.mathplan.MathPlanStudentData;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.type.TermKey;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates the page that shows next steps with affirmations and plans to take MPE.
 */
enum PagePlanNext {
    ;

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A commonly used integer. */
    private static final Integer TWO = Integer.valueOf(2);

    /** A class. */
    private static final String CENTER = "center";

    /** Image link to star icon. */
    private static final String STAR = "<img class='star' src='/images/welcome/orange2.png' alt=''/>";

    /** Image link to disc icon. */
    private static final String DISC = "<img class='star' src='/images/welcome/blue2.png' alt=''/>";

    /** Image link to check icon. */
    private static final String CHECK = "<img class='check' src='/images/welcome/check.png' alt=''/>";

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

        final String stuId = session.getEffectiveUserId();
        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        final MathPlanStudentData data = new MathPlanStudentData(cache, student, logic, session.getNow(),
                session.actAsUserId == null);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);
        MPPage.emitMathPlanHeader(htm);

        if (data == null) {
            MPPage.emitNoStudentDataError(htm);
        } else {
            MathPlacementSite.emitLoggedInAs2(htm, session);
            htm.sDiv("inset2");

            final Map<Integer, RawStmathplan> existing = MathPlanStudentData.getMathPlanResponses(cache,
                    session.getEffectiveUserId(), MathPlanConstants.ONLY_RECOM_PROFILE);

            if (existing.containsKey(ONE)) {
                showPlan(cache, session, htm, logic);
            } else {
                PagePlanView.doGet(cache, site, req, resp, session);
            }

            htm.eDiv(); // inset2
        }

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Displays the student's next steps.
     *
     * @param cache   the data cache
     * @param session the session
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param logic   the site logic
     * @throws SQLException if there is an error accessing the database
     */
    private static void showPlan(final Cache cache, final ImmutableSessionInfo session,
                                 final HtmlBuilder htm, final MathPlanLogic logic) throws SQLException {

        htm.sDiv("shaded2left");

        final String screenName = session.getEffectiveScreenName();
        final String stuId = session.getEffectiveUserId();
        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        final MathPlanStudentData data = new MathPlanStudentData(cache, student, logic, session.getNow(),
                session.actAsUserId == null);

        final Map<Integer, RawStmathplan> intentions = data.getIntentions();

        htm.sDiv("left welcome", "style='margin-bottom:0;'");
        if (screenName == null) {
            htm.add("Your <span class='hidebelow700'>Personalized</span> Math Plan:");
        } else {
            htm.add("<span class='hidebelow700'>Personalized</span> Math Plan for ", screenName);
        }
        htm.eDiv();
        htm.sDiv("right welcome", "style='margin-bottom:0;'");
        final LocalDate today = LocalDate.now();
        htm.add(TemporalUtils.FMT_MDY.format(today));
        htm.eDiv();
        htm.div("clear");
        htm.hr();

        htm.sP().add("Based on your math plan, these are your next steps:").eP();

        final boolean needsPlacement = showNextSteps(htm, data);
        htm.div("vgap2");

        htm.sDiv("advice");
        htm.addln("<form action='plan_next.html' method='post'>");

        htm.add("Read and affirm each statement to complete your Math Plan...");

        final boolean check1 = intentions.containsKey(ONE);
        htm.sP().add("<input type='checkbox' name='affirm1' id='affirm1'");
        if (check1) {
            htm.add(" checked");
        }

        final boolean check2 = intentions.containsKey(TWO);
        htm.add(" onclick='affirmed();'> &nbsp; <label for='affirm1'>",
                "I understand that this plan is only a recommendation.  The math requirements for each degree ",
                "program can change over time, and should be verified with the University Catalog.</label>").eP();

        if (needsPlacement) {
            htm.sP().add("<input type='checkbox' name='affirm2' id='affirm2'");
            if (check2) {
                htm.add(" checked");
            }
            htm.add(" onclick='affirmed();'> &nbsp; <label for='affirm2'>",
                    "I plan to complete the Math Placement Tool.</label>").eP();
        } else {
            htm.sP().add("<input type='hidden' name='affirm2' id='affirm2' value='Y'/>");
        }

        htm.sDiv("center");
        htm.addln("<button type='submit' id='affirmsubmit' class='btn'");
        if (!check1) {
            htm.add(" disabled");
        }
        htm.add(">Affirm</button>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln(" function affirmed() {");
        htm.addln("  document.getElementById('affirmsubmit').disabled =");
        htm.addln("  !document.getElementById('affirm1').checked;");
        htm.addln(" }");
        htm.addln("</script>");
        htm.addln("</form>");
        htm.eDiv();
        htm.div("vgap");

        htm.eDiv();
    }

    /**
     * Shows the next steps for the student, or informs the student that they are eligible for the course(s) they
     * currently need.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param data the student data
     * @return true if the student needs to complete placement, false if not
     */
    static boolean showNextSteps(final HtmlBuilder htm, final MathPlanStudentData data) {

        boolean needsPlacement = true;

        final EEligibility eligibility = data.recommendedEligibility;
        final ENextStep nextStep = data.nextStep;

        htm.div("vgap");

        htm.sDiv("advice");
        final TermKey termKey = data.student.aplnTerm;
        final ETermName applicationTerm = termKey == null ? null : termKey.name;
        final String termName = applicationTerm.fullName;

        // TODO: Check eligibility for Precalculus tutorials based on application term - if they won't be eligible after
        //  placement, change the messaging below.

        switch (nextStep) {
            case MSG_PLACEMENT_NOT_NEEDED, MSG_ALREADY_ELIGIBLE -> {
                htm.sP("center");
                htm.addln("<img class='check' src='/images/welcome/check.png' alt=''/>");

                if (eligibility == EEligibility.AUCC) {
                    htm.add("<strong>You are eligible to register for a Mathematics course appropriate for your ",
                            "program.</strong>");
                } else {
                    htm.add("<strong>You are eligible to register for your ", termName,
                            " Mathematics course(s).</strong>");
                }
                htm.eP(); // center
                needsPlacement = false;
            }

            case MSG_PLACE_INTO_117 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" section of the Math Placement Tool to try ",
                        "to become eligible for MATH 117 or MATH 120.</strong>").eP();
                htm.sP().add("If you do not place into MATH 117/MATH 120 on the Math Placement Tool, you can become ",
                        "eligible for those courses by completing the Entry Level Math Tutorial.").eP();
            }
            case MSG_PLACE_OUT_117 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" section of the Math Placement Tool to try ",
                        "to place out of MATH 117.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117 on the Math Placement Tool, you can do so by ",
                        "completing one or more tutorials.").eP();
            }
            case MSG_PLACE_INTO_118 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" section of the Math Placement Tool to try ",
                        "to place out of MATH 117 and become eligible for MATH 118.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117 on the Math Placement Tool, you can do so by ",
                        "completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_118 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" section of the Math Placement Tool to try ",
                        "to place out of MATH 118.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 118 on the Math Placement Tool, you can do so by ",
                        "completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_117_118 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" section of the Math Placement Tool to try ",
                        "to place out of MATH 117 and MATH 118.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117 and MATH 118 on the Math Placement Tool, you can do ",
                        "so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_INTO_125 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" section of the Math Placement Tool to try ",
                        "to place out of MATH 117 and MATh 118 and become eligible for MATH 125.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117 and MATh 118 on the Math Placement Tool, you can do ",
                        "so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_125 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Trigonometry\" sections of the Math ",
                        "Placement Tool to try to place out of MATH 125.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 125 on the Math Placement Tool, you can do so by ",
                        "completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_118_125 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Trigonometry\" sections of the Math ",
                        "Placement Tool to try to place out of MATH 118 and MATH 125.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 118 and MATH 125 on the Math Placement Tool, you can do ",
                        "so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_117_118_125 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Trigonometry\" sections of the Math ",
                        "Placement Tool to try to place out of MATH 117, MATH 118, and MATH 125.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117, MATH 118, and MATH 125 on the Math Placement Tool, ",
                        "you can do so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_INTO_155 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\", \"Trigonometry\", and \"Logarithmic &amp; ",
                        "Exponential Functions\" sections of the Math Placement Tool to try to become eligible for ",
                        "MATH 155.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117, MATH 118, MATH 124, and MATH 125 on the Math ",
                        "Placement Tool, you can do so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Trigonometry\" sections of the Math ",
                        "Placement Tool to try to place out of MATH 126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 126 on the Math Placement Tool, you can do so by ",
                        "completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_125_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Trigonometry\" sections of the Math ",
                        "Placement Tool to try to place out of MATH 125 and MATH 126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 125 and MATH 126 on the Math Placement Tool, you can do ",
                        "so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_118_125_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Trigonometry\" sections of the Math ",
                        "Placement Tool to try to place out of MATH 118, MATH 125, and MATH 126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 118, MATH 125, and MATH 126 on the Math Placement Tool, ",
                        "you can do so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_117_118_125_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Trigonometry\" sections of the Math ",
                        "Placement Tool to try to place out of MATH 117, MATH 118, MATH 125, and MATH ",
                        "126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117, MATH 118, MATH 125, and MATH 126 on the Math ",
                        "Placement Tool, you can do so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_124 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Logarithmic &amp; Exponential ",
                        "Functions\" sections of the Math Placement Tool to try to place out of MATH ",
                        "124.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 124 on the Math Placement Tool, you can do so by ",
                        "completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_118_124 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Logarithmic &amp; Exponential ",
                        "Functions\" sections of the Math Placement Tool to try to place out of MATH 118 and MATH ",
                        "124.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 118 and MATH 124 on the Math Placement Tool, you can do ",
                        "so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_117_118_124 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\" and \"Logarithmic &amp; Exponential ",
                        "Functions\" sections of the Math Placement Tool to try to place out of MATh 117, MATH 118, ",
                        "and MATH 124.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117, MATH 118, and MATH 124 on the Math Placement Tool, ",
                        "you can do so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_124_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\", \"Trigonometry\", and \"Logarithmic &amp; ",
                        "Exponential Functions\" sections of the Math Placement Tool to try to place out of MATH 124 ",
                        "and MATH 126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 124 and MATH 126 on the Math Placement Tool, you can do ",
                        "so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_124_125_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\", \"Trigonometry\", and \"Logarithmic &amp; ",
                        "Exponential Functions\" sections of the Math Placement Tool to try to place out of MATH 124, ",
                        "MATH 125, and MATH 126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 124, MATH 125, and MATH 126 on the Math Placement Tool, ",
                        "you can do so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_118_124_125_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\", \"Trigonometry\", and \"Logarithmic &amp; ",
                        "Exponential Functions\" sections of the Math Placement Tool to try to place out of MATH 118, ",
                        "MATH 124, MATH 125, and MATH 126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 118, MATH 124, MATH 125, and MATH 126 on the Math ",
                        "Placement Tool, you can do so by completing one or more tutorials.").eP();
            }
            case MSG_PLACE_OUT_117_118_124_125_126 -> {
                htm.sP().add("<strong>You should complete the \"Algebra\", \"Trigonometry\", and \"Logarithmic &amp; ",
                        "Exponential Functions\" sections of the Math Placement Tool to try to place out of MATH 117, ",
                        "MATH 118, MATH 124, MATH 125, and MATH 126.</strong>").eP();
                htm.sP().add("If you do not place out of MATH 117, MATH 118, MATH 124, MATH 125, and MATH 126 on the ",
                        "Math Placement Tool, you can do so by completing one or more tutorials.").eP();
            }
        }

        htm.eDiv(); // advice

        return needsPlacement;
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

        final Profile profile = site.site.profile;
        final MathPlanLogic logic = new MathPlanLogic(profile);

        final boolean aff1 = req.getParameter("affirm1") != null;
        final boolean aff2 = req.getParameter("affirm2") != null;

        // Only perform updates if this is not an adviser using "Act As"
        if (session.actAsUserId == null) {
            final String stuId = session.getEffectiveUserId();
            final ZonedDateTime sessNow = session.getNow();
            final RawStudent student = RawStudentLogic.query(cache, stuId, false);

            final MathPlanStudentData data = new MathPlanStudentData(cache, student, logic, sessNow, true);

            RawStmathplanLogic.deleteAllForPage(cache, stuId, MathPlanConstants.INTENTIONS_PROFILE);

            final List<Integer> questions = new ArrayList<>(2);
            final List<String> answers = new ArrayList<>(2);

            questions.add(ONE);
            answers.add(aff1 ? "Y" : "N");

            questions.add(TWO);
            answers.add(aff2 ? "Y" : "N");

            logic.storeMathPlanResponses(cache, data.student, MathPlanConstants.INTENTIONS_PROFILE, questions, answers,
                    sessNow, session.loginSessionTag);

            // Store MPL test score in Banner SOATEST (1 if no placement needed, 2 if placement needed). This is
            // based on a response with version='WLCM5'.  If there is a row with survey_nbr=2 and stu_answer='Y', that
            // indicates placement is needed.  If there is a row with survey_nbr=1 and stu_answer='Y', that indicates
            // the math plan has been completed and placement is not needed. The MPL test score is '1' if placement
            // is not needed, and '2' if placement is needed.

            if (aff1) {
                final String desiredMPLTestScore = aff2 ? "2" : "1";

                final Login liveCtx = profile.getLogin(ESchema.LIVE);
                final DbConnection liveConn = liveCtx.checkOutConnection();
                try {
                    // Query the test score, see if this update represents a change, and only insert a new test score
                    // row if the result has changed...  People may do the math plan several times with the same
                    // outcome, and we don't need to insert the same result each time.
                    final List<RawMpscorequeue> existing = RawMpscorequeueLogic.querySORTESTByStudent(liveConn,
                            data.student.pidm);

                    RawMpscorequeue mostRecent = null;
                    for (final RawMpscorequeue test : existing) {

                        // Log.info("Found '", test.testCode, "' test score of '", test.testScore, "' for student ",
                        //         data.student.stuId, " with PIDM ", data.student.pidm);

                        if ("MPL".equals(test.testCode)) {
                            if (mostRecent == null || mostRecent.testDate.isBefore(test.testDate)) {
                                mostRecent = test;
                            }
                        }
                    }

                    if (mostRecent == null || !desiredMPLTestScore.equals(mostRecent.testScore)) {
                        final LocalDateTime now = LocalDateTime.now();
                        final RawMpscorequeue newRow = new RawMpscorequeue(data.student.pidm, "MPL", now,
                                desiredMPLTestScore);

                        Log.info("Inserting MPL test score of ", desiredMPLTestScore, " for student ",
                                data.student.stuId, " with PIDM ", data.student.pidm);

                        if (!RawMpscorequeueLogic.insertSORTEST(liveConn, newRow)) {
                            Log.warning("Failed to insert 'MPL' test score for ", data.student.stuId);
                        }
                    }
                } finally {
                    liveCtx.checkInConnection(liveConn);
                }
            }
        }

        resp.sendRedirect("plan_start.html");
    }
}
