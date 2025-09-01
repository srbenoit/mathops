package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.RecommendedFirstTerm;
import dev.mathops.db.logic.mathplan.StudentMathPlan;
import dev.mathops.db.logic.mathplan.StudentStatus;
import dev.mathops.db.logic.mathplan.types.ECourse;
import dev.mathops.db.logic.mathplan.types.EIdealFirstTermType;
import dev.mathops.db.logic.mathplan.types.ENextStep;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.schema.legacy.RawMpscorequeue;
import dev.mathops.db.schema.legacy.RawStmathplan;
import dev.mathops.db.schema.legacy.RawStudent;
import dev.mathops.db.rec.TermRec;
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

    /** A string used when describing "ideal" eligibility. */
    private static final String IT_IS_IDEAL = "it is ideal if you are eligible for ";

    /** A string used when describing "ideal" eligibility. */
    private static final String OR = " or ";

    /** A string used when describing "ideal" eligibility. */
    private static final String AND = " and ";

    /** A string used when describing "ideal" eligibility. */
    private static final String C = ", ";

    /** A string used when describing "ideal" eligibility. */
    private static final String C_AND = ", and ";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_A =
            "You should complete the \"Algebra\" section of the Math Placement Tool to try to ";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_AT = "You should complete the \"Algebra\" and \"Trigonometry\" "
                                                         + "sections of the Math Placement Tool to try to ";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_AL = "You should complete the \"Algebra\" and \"Logarithmic &amp; "
                                                         + "Exponential Functions\" sections of the Math Placement "
                                                         + "Tool to try to ";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_3 = "You should complete the \"Algebra\", \"Trigonometry\", and "
                                                        + "\"Logarithmic &amp; Exponential Functions\" sections of "
                                                        + "the Math Placement Tool to try to ";

    /** A course name for describing next steps. */
    private static final String IF_NOT_PLACED = "If you do not place out of ";

    /** A course name for describing next steps. */
    private static final String YOU_CAN_DO_TUTORIAL =
            " on the Math Placement Tool, you can do so by completing one or more tutorials.";

    /** A string to start a "strong" span. */
    private static final String SSTRONG = "<strong>";

    /** A string to end a "strong" span. */
    private static final String ESTRONG = "</strong>";

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

        final HtmlBuilder htm = new HtmlBuilder(8192);
        final String title = site.getTitle();
        Page.startNofooterPage(htm, title, session, true, Page.NO_BARS, null, false, false);
        MPPage.emitMathPlanHeader(htm);

        MathPlacementSite.emitLoggedInAs2(htm, session);
        htm.sDiv("inset2");

        final String stuId = session.getEffectiveUserId();
        final StudentData studentData = cache.getStudent(stuId);
        final RawStudent student = studentData.getStudentRecord();

        final Map<Integer, RawStmathplan> existing = studentData.getLatestMathPlanResponsesByPage(
                MathPlanConstants.ONLY_RECOM_PROFILE);

        if (existing.containsKey(ONE)) {
            showPlan(cache, session, htm, studentData);
        } else {
            PagePlanView.doGet(cache, site, req, resp, session);
        }

        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Displays the student's next steps.
     *
     * @param cache       the data cache
     * @param session     the session
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param studentData the student data object
     * @throws SQLException if there is an error accessing the database
     */
    private static void showPlan(final Cache cache, final ImmutableSessionInfo session,
                                 final HtmlBuilder htm, final StudentData studentData) throws SQLException {

        htm.sDiv("shaded2left");

        final String screenName = session.getEffectiveScreenName();
        final String stuId = session.getEffectiveUserId();
        final StudentMathPlan plan = MathPlanLogic.queryPlan(cache, stuId);

        final Map<Integer, RawStmathplan> intentions = studentData.getLatestMathPlanResponsesByPage(
                MathPlanConstants.INTENTIONS_PROFILE);

        htm.sDiv("left welcome", "style='margin-bottom:0;'");
        if (screenName == null) {
            htm.add("Your <span class='hidebelow700'>Personalized</span> Math Plan:");
        } else {
            htm.add("<span class='hidebelow700'>Personalized</span> Math Plan for ", screenName);
        }
        htm.eDiv();
        htm.sDiv("right welcome", "style='margin-bottom:0;'");
        final LocalDate today = LocalDate.now();
        final String todayStr = TemporalUtils.FMT_MDY.format(today);
        htm.add(todayStr);
        htm.eDiv();
        htm.div("clear");
        htm.hr();

        final boolean needsPlacement = showNextSteps(cache, htm, plan);
        htm.div("vgap2");

        // If the user has already affirmed, don't make them do it again...
        final RawStmathplan affirm1 = intentions.get(ONE);
        final RawStmathplan affirm2 = intentions.get(TWO);

        if (affirm1 == null || affirm2 == null) {

            htm.sDiv("advice");
            htm.addln("<form action='plan_next.html' method='post'>");

            htm.add("Read and affirm each statement to complete your Math Plan...");

            final boolean check1 = affirm1 != null && "Y".equals(affirm1.stuAnswer);
            htm.sP().add("<input type='checkbox' name='affirm1' id='affirm1'");
            if (check1) {
                htm.add(" checked");
            }

            final boolean check2 = affirm2 != null && "Y".equals(affirm2.stuAnswer);
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

            htm.sDiv(CENTER);
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
        } else {
            htm.addln("<form action='secure_landing.html' method='get'>");
            htm.sDiv("center");
            htm.addln("<button type='submit' class='btn'>Go to the next step...</button>");
            htm.eDiv();
            htm.addln("</form>");
        }
    }

    /**
     * Shows the next steps for the student, or informs the student that they are eligible for the course(s) they
     * currently need.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param plan  the student math plan
     * @return true if the student needs to complete placement, false if not
     */
    static boolean showNextSteps(final Cache cache, final HtmlBuilder htm, final StudentMathPlan plan) {

        TermKey active = null;
        try {
            final SystemData systemData = cache.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();
            if (activeTerm != null) {
                active = activeTerm.term;
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        final RawStudent student = plan.stuStatus.student;
        final TermKey termKey = student.aplnTerm;
        final boolean isIncoming = testForIncoming(active, termKey);

        final Map<Integer, RawStmathplan> profileResponses = plan.stuStatus.majorsResponses;
        final String basedOn = profileResponses.size() == 1 ? "Based on the major you selected, "
                : "Based on the list of majors you selected, ";
        final String inTerm = " in your first semester.";

        htm.sDiv("indent");

        // Show the student's "ideal first term eligibility" based on their selected major(s)
        final RecommendedFirstTerm firstTerm = plan.requirements.firstTerm;

        if (firstTerm.firstTermNamed.type == EIdealFirstTermType.CORE_ONLY) {
            htm.sP().add(basedOn, "you just need to complete the All-University Core Curriculum requirement of 3 ",
                    "credits of Quantitative Reasoning.").eP();

            final int completed = plan.stuStatus.getCreditsOfCoreCompleted();

            if (completed >= 3) {
                htm.sP().add("You have already satisfied this core requirement.").eP();
            } else if (completed > 0) {
                final String str = Integer.toString(completed);
                htm.sP().add("You have already completed ", str, " of these credits.").eP();
            }
        } else {
            final String idealEligibilityText = firstTerm.getText();
            htm.sP().add(basedOn, IT_IS_IDEAL, idealEligibilityText, inTerm).eP();
        }

        htm.eDiv();

        htm.div("vgap");
        htm.sDiv("advice");
        final boolean needsPlacement = showNextSteps(htm, plan, isIncoming);
        htm.eDiv(); // advice

        return needsPlacement;
    }

    /**
     * Tests whether a student is "incoming".
     *
     * @param active  the active term
     * @param termKey the student's application term
     * @return true if the student is "incoming" and can access the Precalculus tutorials
     */
    private static boolean testForIncoming(final TermKey active, final TermKey termKey) {

        boolean isIncoming = false;
        if (active != null && termKey != null && termKey.name == ETermName.FALL
            && (active.name == ETermName.SUMMER || active.name == ETermName.FALL)) {
            isIncoming = active.year.equals(termKey.year);
        }

        return isIncoming;
    }

    /**
     * Shows the student's next step(s), or a message telling them nothing more is needed.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param plan       the student math plan
     * @param isIncoming true if the student is "incoming" and can use Precalculus Tutorials
     */
    private static boolean showNextSteps(final HtmlBuilder htm, final StudentMathPlan plan, final boolean isIncoming) {

        boolean needsPlacement = true;

        final RecommendedFirstTerm firstTerm = plan.requirements.firstTerm;
        final ENextStep nextStep = plan.nextSteps.nextStep;

        final String existing = buildExistingEligibility(plan);

        switch (nextStep) {
            case MSG_PLACEMENT_NOT_NEEDED, MSG_ALREADY_ELIGIBLE -> {
                htm.sP(CENTER);
                htm.addln("<img class='check' src='/images/welcome/check.png' alt=''/>");
                htm.add(SSTRONG,
                        "You are eligible to register for a Mathematics course appropriate for your program.",
                        ESTRONG);
                if (firstTerm.firstTermNamed.type != EIdealFirstTermType.CORE_ONLY) {
                    emitExistingIfNotBlank(htm, existing);
                }
                htm.eP(); // center
                needsPlacement = false;
            }

            case MSG_PLACEMENT_NOT_NEEDED_FOR_DECLARED -> {
                htm.sP(CENTER);
                htm.addln("<img class='check' src='/images/welcome/check.png' alt=''/>");
                htm.add(SSTRONG,
                        "You are eligible to register for a Mathematics course appropriate for your declared major.",
                        ESTRONG);
                if (firstTerm.firstTermNamed.type != EIdealFirstTermType.CORE_ONLY) {
                    emitExistingIfNotBlank(htm, existing);
                }
                htm.eP(); // center

                htm.sP();
                htm.add("NOTE: You selected majors in the Math Plan that need additional MATH courses.  If you switch ",
                        "to one of these majors, you would need to satisfy the MATH requirements for that major.");
                htm.eP();
                needsPlacement = false;
            }

            case MSG_ALREADY_COMPLETE -> {
                htm.sP(CENTER);
                htm.addln("<img class='check' src='/images/welcome/check.png' alt=''/>");
                htm.add(SSTRONG,
                        "You have already completed the recommended MATH courses for your first semester.",
                        ESTRONG);
                htm.eP(); // center
                needsPlacement = false;
            }

            case MSG_PLACE_INTO_117 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "become eligible for MATH 117 or MATH 120.", ESTRONG).eP();
                htm.sP().add("If you do not place into MATH 117 or MATH 120 on the Math Placement Tool, you can ",
                        "become eligible for those courses by completing the Entry Level Math Tutorial.").eP();
            }
            case MSG_PLACE_OUT_117 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "place out of MATH 117.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_INTO_118 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A,
                        "place out of MATH 117 and become eligible for MATH 118.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "place out of MATH 118.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "place out of MATH 117 and MATH 118.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117 and MATH 118", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_INTO_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A,
                        "place out of MATH 117 and MATh 118 and become eligible for MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117 and MATh 118", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 118 and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118 and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT,
                        "place out of MATH 117, MATH 118, and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_INTO_155 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3, "become eligible for MATH 155.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 124, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 125 and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 125 and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT,
                        "place out of MATH 118, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT,
                        "place out of MATH 117, MATH 118, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_124 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AL, "place out of MATH 124.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_124 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AL, "place out of MATH 118 and MATH 124.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118 and MATH 124", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_124 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AL,
                        "place out of MATh 117, MATH 118, and MATH 124.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, and MATH 124", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_124_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3, "place out of MATH 124 and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124 and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_124_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 124, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_124_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 118, MATH 124, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118, MATH 124, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_124_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 117, MATH 118, MATH 124, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 124, MATH 125, and MATH 126",
                            YOU_CAN_DO_TUTORIAL).eP();
                }
            }

            case MSG_PLACE_OUT_118_124_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 118, MATH 124, and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118, MATH 124, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }

            case MSG_PLACE_OUT_117_118_124_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 117, MATH 118, MATH 124, and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 124, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }

            case MSG_PLACE_OUT_124_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3, "place out of MATH 124 and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124 and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
        }

        return needsPlacement;
    }

    /**
     * Emits a paragraph with the "existing eligibility and credit" string if that string is not blank.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param existing the "existing eligibility" string
     */
    private static void emitExistingIfNotBlank(final HtmlBuilder htm, final String existing) {

        if (!existing.isBlank()) {
            htm.sP().add(existing).eP();
        }
    }

    /**
     * Displays the set of courses for which the student is already eligible or already has credit.
     *
     * @param plan the student math plan
     * @return the eligibility string; an empty string if the student is not eligible for any courses and has no credit
     */
    private static String buildExistingEligibility(final StudentMathPlan plan) {

        final StudentStatus stuStatus = plan.stuStatus;

        final HtmlBuilder htm = new HtmlBuilder(50);

        // Display the relevant courses for which the student is eligible

        final List<String> isEligibleFor = new ArrayList<>(10);
        final List<String> alreadyHas = new ArrayList<>(10);
        final boolean okFor120 = stuStatus.isEligible(ECourse.M_117);

        if (stuStatus.isEligible(ECourse.M_117)) {
            if (stuStatus.hasCompleted(ECourse.M_117)) {
                alreadyHas.add("MATH 117");
            } else {
                isEligibleFor.add("MATH 117");
            }
        }

        if (stuStatus.isEligible(ECourse.M_118)) {
            if (stuStatus.hasCompleted(ECourse.M_118)) {
                alreadyHas.add("MATH 118");
            } else {
                isEligibleFor.add("MATH 118");
            }
        }

        if (stuStatus.isEligible(ECourse.M_124)) {
            if (stuStatus.hasCompleted(ECourse.M_124)) {
                alreadyHas.add("MATH 124");
            } else {
                isEligibleFor.add("MATH 124");
            }
        }

        if (stuStatus.isEligible(ECourse.M_125)) {
            if (stuStatus.hasCompleted(ECourse.M_125)) {
                alreadyHas.add("MATH 125");
            } else {
                isEligibleFor.add("MATH 125");
            }
        }

        if (stuStatus.isEligible(ECourse.M_126)) {
            if (stuStatus.hasCompleted(ECourse.M_126)) {
                alreadyHas.add("MATH 126");
            } else {
                isEligibleFor.add("MATH 126");
            }
        }

        if (stuStatus.isEligible(ECourse.M_141)) {
            if (stuStatus.hasCompleted(ECourse.M_141)) {
                alreadyHas.add("MATH 141");
            } else {
                isEligibleFor.add("MATH 141");
            }
        }

        if (stuStatus.isEligible(ECourse.M_155)) {
            if (stuStatus.hasCompleted(ECourse.M_155)) {
                alreadyHas.add("MATH 155");
            } else {
                isEligibleFor.add("MATH 155");
            }
        }

        if (stuStatus.isEligible(ECourse.M_156)) {
            if (stuStatus.hasCompleted(ECourse.M_156)) {
                alreadyHas.add("MATH 156");
            } else {
                isEligibleFor.add("MATH 156");
            }
        }

        if (stuStatus.isEligible(ECourse.M_160)) {
            if (stuStatus.hasCompleted(ECourse.M_160)) {
                alreadyHas.add("MATH 160");
            } else {
                isEligibleFor.add("MATH 160");
            }
        }

        final int numEligible = isEligibleFor.size();
        if (numEligible > 0) {
            htm.add(SSTRONG, "You are eligible to register for ");
            final String first = isEligibleFor.getFirst();
            if (numEligible == 1) {
                htm.add(first);
            } else if (numEligible == 2) {
                final String second = isEligibleFor.get(1);
                htm.add(first, AND, second);
            } else {
                htm.add(first);
                for (int i = 1; i < numEligible - 1; ++i) {
                    final String item = isEligibleFor.get(i);
                    htm.add(C, item);
                }
                final String last = isEligibleFor.get(numEligible - 1);
                htm.add(C_AND, last);
            }

            if (okFor120) {
                htm.add(" (or MATH 120)");
            }
            htm.add(" in the upcoming semester. ", ESTRONG);
        }

        final int numAlready = alreadyHas.size();
        if (!alreadyHas.isEmpty()) {
            htm.add("You already have credit for ");
            final String first = alreadyHas.get(0);
            if (numAlready == 1) {
                htm.add(first);
            } else if (numAlready == 2) {
                final String second = alreadyHas.get(1);
                htm.add(first, AND, second);
            } else {
                htm.add(first);
                for (int i = 1; i < numAlready - 1; ++i) {
                    final String item = alreadyHas.get(i);
                    htm.add(C, item);
                }
                final String last = alreadyHas.get(numAlready - 1);
                htm.add(C_AND, last);
            }
            htm.add(".");
        }

        return htm.toString();
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

        final boolean aff1 = req.getParameter("affirm1") != null;
        final boolean aff2 = req.getParameter("affirm2") != null;

        // Only perform updates if this is not an adviser using "Act As"
        if (session.actAsUserId == null) {
            final String stuId = session.getEffectiveUserId();
            final StudentData studentData = cache.getStudent(stuId);
            final RawStudent student = studentData.getStudentRecord();

            final ZonedDateTime sessionNow = session.getNow();

            RawStmathplanLogic.deleteAllForPage(cache, stuId, MathPlanConstants.INTENTIONS_PROFILE);

            final List<Integer> questions = new ArrayList<>(2);
            final List<String> answers = new ArrayList<>(2);

            questions.add(ONE);
            answers.add(aff1 ? "Y" : "N");

            questions.add(TWO);
            answers.add(aff2 ? "Y" : "N");

            MathPlanLogic.storeMathPlanResponses(cache, student, MathPlanConstants.INTENTIONS_PROFILE, questions,
                    answers, sessionNow, session.loginSessionTag);

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
                            student.pidm);

                    final RawMpscorequeue mostRecent = getMostRecentMPLScore(existing);

                    if (mostRecent == null || !desiredMPLTestScore.equals(mostRecent.testScore)) {
                        final LocalDateTime now = LocalDateTime.now();
                        final RawMpscorequeue newRow = new RawMpscorequeue(student.pidm, "MPL", now,
                                desiredMPLTestScore);

                        Log.info("Inserting MPL test score of ", desiredMPLTestScore, " for student ", stuId,
                                " with PIDM ", student.pidm);

                        if (!RawMpscorequeueLogic.insertSORTEST(liveConn, newRow)) {
                            Log.warning("Failed to insert 'MPL' test score for ", student.stuId);
                        }
                    }
                } finally {
                    liveCtx.checkInConnection(liveConn);
                }
            }
        }

        resp.sendRedirect("plan_start.html");
    }

    /**
     * Scans for the most recent MPL test score for the student.
     *
     * @param existing the existing records to scan
     * @return the most recent record
     */
    private static RawMpscorequeue getMostRecentMPLScore(final Iterable<RawMpscorequeue> existing) {

        RawMpscorequeue mostRecent = null;

        for (final RawMpscorequeue test : existing) {
            if ("MPL".equals(test.testCode)) {
                if (mostRecent == null || mostRecent.testDate.isBefore(test.testDate)) {
                    mostRecent = test;
                }
            }
        }
        return mostRecent;
    }
}
