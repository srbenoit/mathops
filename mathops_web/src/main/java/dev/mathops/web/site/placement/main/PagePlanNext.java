package dev.mathops.web.site.placement.main;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.logic.mathplan.data.MathPlanConstants;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.db.old.logic.mathplan.MathPlanLogic;
import dev.mathops.db.old.logic.mathplan.data.CourseInfo;
import dev.mathops.db.old.logic.mathplan.data.CourseRecommendations;
import dev.mathops.db.old.logic.mathplan.data.CourseSequence;
import dev.mathops.db.old.logic.mathplan.data.ENextStep;
import dev.mathops.db.old.logic.mathplan.data.StudentData;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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

        final MathPlanLogic logic = new MathPlanLogic(site.getDbProfile());

        final String stuId = session.getEffectiveUserId();
        final StudentData data = logic.getStudentData(cache, stuId, session.getNow(), session.loginSessionTag,
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

            if (existing.containsKey(ONE)) {
                showPlan(cache, session, htm, logic);
            } else {
                PagePlanView.doGet(cache, site, req, resp, session, logic);
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
        final StudentData data = logic.getStudentData(cache, stuId, session.getNow(), session.loginSessionTag,
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

        htm.eDiv(); // shaded2left
    }

    /**
     * Shows the next steps for the student, or informs the student that they are eligible for the course(s) they
     * currently need.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param data the student data
     * @return true if the student needs to complete placement, false if not
     */
    private static boolean showNextSteps(final HtmlBuilder htm, final StudentData data) {

        boolean needsPlacement = true;

        final CourseRecommendations recommendations = data.recommendations;
        final List<ENextStep> nextSteps = data.getNextSteps();

        htm.div("vgap");

        htm.sDiv("advice");
        if (nextSteps.isEmpty()) {
            htm.sP("center");
            htm.addln("<img class='check' src='/images/welcome/check.png' alt=''/>");

            if (recommendations.typicalSequence.hasSemester1Data()) {
                htm.add("<strong>You are eligible to register for your first-semester Mathematics courses.</strong>");
            } else {
                htm.add("<strong>You are eligible to register for your first Mathematics course.</strong>");
            }
            htm.eP();
            needsPlacement = false;
        } else {
            final TermKey termKey = data.student.aplnTerm;
            final ETermName applicationTerm = termKey == null ? null : termKey.name;

            for (final ENextStep step : nextSteps) {
                if (step == ENextStep.MSG_3A || step == ENextStep.MSG_3G || step == ENextStep.MSG_3H
                        || step == ENextStep.MSG_4A || step == ENextStep.MSG_4B
                        || step == ENextStep.MSG_4C) {
                    needsPlacement = false;
                }
                emitStep(htm, step, applicationTerm, data, false);
            }
        }
        htm.eDiv();

        return needsPlacement;
    }

    /**
     * Shows the next steps for the student in brief form (without calls to action).
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param data the student data
     */
    static void showNextStepsBrief(final HtmlBuilder htm, final StudentData data) {

        boolean needsPlacement = true;

        final CourseRecommendations recommendations = data.recommendations;
        final List<ENextStep> nextSteps = data.getNextSteps();

        htm.div("vgap");

        htm.sDiv("advice");
        if (nextSteps.isEmpty()) {
            htm.sP("center");
            htm.addln("<img class='check' src='/images/welcome/check.png' alt=''/>");

            if (recommendations.typicalSequence.hasSemester1Data()) {
                htm.add("<strong>You are eligible to register for your first-semester Mathematics course(s).</strong>");
            } else {
                htm.add("<strong>You are eligible to register for your first Mathematics course.</strong>");
            }
            htm.eP();
            needsPlacement = false;
        } else {
            final TermKey termKey = data.student.aplnTerm;
            final ETermName applicationTerm = termKey == null ? null : termKey.name;

            for (final ENextStep step : nextSteps) {
                if (step == ENextStep.MSG_3A || step == ENextStep.MSG_3G || step == ENextStep.MSG_3H
                        || step == ENextStep.MSG_4A || step == ENextStep.MSG_4B || step == ENextStep.MSG_4C) {
                    needsPlacement = false;
                }
                emitStep(htm, step, applicationTerm, data, false);
            }
        }
        htm.eDiv();
    }

    /**
     * Emits the HTML description of a step.
     *
     * @param htm             the HTML builder
     * @param applicationTerm the application term name
     * @param step            the step
     * @param data            the student data
     * @param showActions     true to include calls to action
     */
    private static void emitStep(final HtmlBuilder htm, final ENextStep step, final ETermName applicationTerm,
                                 final StudentData data, final boolean showActions) {

        final CourseSequence critical = data.recommendations.criticalSequence;
        final CourseSequence typical = data.recommendations.typicalSequence;

        final int critPrearrivPrereqs = critical.getNumPrearrivalPrereqs();
        final int typPrearrivalPrereqs = typical.getNumPrearrivalPrereqs();

        final String semester = applicationTerm == null ? "your first semester"
                : Res.fmt(Res.THE_ZERO_SEMESTER, applicationTerm.fullName);

        final String m101OrFirst;
        final String m101OrThat;
        if (critical.isCourseInSemester1("M 101")) {
            m101OrFirst = "<strong class='headercolor'>" + Res.get(Res.PLAN_M101_TITLE) + "</strong>";
            m101OrThat = Res.get(Res.PLAN_M101_LABEL);
        } else {
            m101OrFirst = Res.get(Res.PLAN_YOUR_FIRST_COURSES);
            m101OrThat = Res.get(Res.PLAN_THESE_COURSES);
        }

        final String yourFirstSemCourse;
        final HtmlBuilder builder = new HtmlBuilder(50);
        final Iterator<CourseInfo> iter = critical.getSemester1Courses().values().iterator();
        if (iter.hasNext()) {
            CourseInfo info = iter.next();
            yourFirstSemCourse = info.course.courseLabel;
            builder.add(info.course.courseLabel);

            while (iter.hasNext()) {
                info = iter.next();
                if (iter.hasNext()) {
                    builder.add(", ");
                } else {
                    builder.add(", and ");
                }
                builder.add(info.course.courseLabel);
            }
        } else {
            yourFirstSemCourse = Res.get(Res.PLAN_YOUR_FIRST_COURSE);
            builder.add(Res.get(Res.PLAN_YOUR_FIRST_COURSES));
        }
        final String yourFirstSemCourses = builder.toString();

        switch (step) {

            case MSG_1A_SINGULAR:
                htm.sP();
                htm.add(STAR);

                if (critPrearrivPrereqs == 1) {
                    htm.add(Res.fmt(Res.PLAN_1A_SINGULAR_1, yourFirstSemCourse, semester));
                } else {
                    htm.add(Res.fmt(Res.PLAN_1A_SINGULAR_2, yourFirstSemCourse, semester));
                }
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.addln(CoreConstants.SPC, Res.get(Res.PLAN_IDEALLY_SINGULAR));
                }
                htm.eP();
                break;

            case MSG_1A_PLURAL:
                htm.sP();
                htm.add(STAR);
                if (critPrearrivPrereqs == 1) {
                    htm.add(Res.fmt(Res.PLAN_1A_PLURAL_1, yourFirstSemCourses, semester));
                } else {
                    htm.add(Res.fmt(Res.PLAN_1A_PLURAL_2, yourFirstSemCourses, semester));
                }
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.addln(CoreConstants.SPC, Res.get(Res.PLAN_IDEALLY_PLURAL));
                }
                htm.eP();
                break;

            case MSG_1B_SINGULAR:
                htm.sP().add(STAR);
                if (critPrearrivPrereqs == 1) {
                    htm.add("There is a course important to your program whose prerequisite is not yet ",
                            "satisfied. You should try to satisfy this prerequisite as quickly as possible.");
                } else {
                    htm.add("There is a course important to your program whose prerequisites are not yet ",
                            "satisfied. You should try to satisfy these prerequisites as quickly as possible.");
                }
                htm.eP();
                break;

            case MSG_1B_PLURAL:
                htm.sP().add(STAR);
                if (critPrearrivPrereqs == 1) {
                    htm.add("There are courses important to your program whose prerequisite is not yet ",
                            "satisfied. You should try to satisfy this prerequisite as quickly as possible.");
                } else {
                    htm.add("There are courses important to your program whose prerequisites are not yet ",
                            "satisfied. You should try to satisfy these prerequisites as quickly as possible.");
                }
                htm.eP();
                break;

            case MSG_1C_SINGULAR:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("We recommend that you satisfy the prerequisite");
                } else {
                    htm.add("We recommend that you satisfy the prerequisites");
                }
                htm.add(" for your first-semester course so you can take that course during ", semester,
                        ", if space permits.");
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.add(" Ideally, you will register for this course during Orientation.");
                }
                htm.eP();
                break;

            case MSG_1C_PLURAL:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("We recommend that you satisfy the prerequisite");
                } else {
                    htm.add("We recommend that you satisfy the prerequisites");
                }
                htm.add(" for your first-semester courses so you can take those courses during ", semester,
                        ", if space permits.");
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.add(" Ideally, you will register for those courses during Orientation.");
                }
                htm.eP();
                break;

            case MSG_1D_SINGULAR:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("A course that is recommended for your program has a prerequisite that is not yet ",
                            "satisfied. You should try to satisfy this prerequisite as quickly as possible.");
                } else {
                    htm.add("A course that is recommended for your program has prerequisites that are not yet ",
                            "satisfied. You should try to satisfy these prerequisites as quickly as possible.");
                }
                htm.eP();
                break;

            case MSG_1D_PLURAL:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("There are courses recommended for your program whose prerequisite is not yet ",
                            "satisfied. You should try to satisfy this prerequisite as quickly as possible.");
                } else {
                    htm.add("There are courses recommended for your program whose prerequisites are not yet ",
                            "satisfied. You should try to satisfy these prerequisites as quickly as possible.");
                }
                htm.eP();
                break;

            case MSG_1E_SINGULAR:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("Satisfying the prerequisite");
                } else {
                    htm.add("Satisfying the prerequisites");
                }
                htm.add(" for your first-semester course would allow you to take that course during ", semester,
                        ", if space permits.");
                htm.eP();
                break;

            case MSG_1E_PLURAL:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("Satisfying the prerequisite");
                } else {
                    htm.add("Satisfying the prerequisites");
                }
                htm.add(" of your first-semester courses would allow you to take those courses during ", semester,
                        ", if space permits.");
                htm.eP();
                break;

            case MSG_1F_SINGULAR:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("There is a course in your program whose prerequisite is not yet ",
                            "satisfied. You should try to satisfy this prerequisite as quickly as possible.");
                } else {
                    htm.add("There is a course in your program whose prerequisites are not yet ",
                            "satisfied. You should try to satisfy these prerequisites as quickly as possible.");
                }
                htm.eP();
                break;

            case MSG_1F_PLURAL:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("There are courses in your program whose prerequisite is not yet ",
                            "satisfied. You should try to satisfy this prerequisite as quickly as possible.");
                } else {
                    htm.add("There are courses in your program whose prerequisites are not yet ",
                            "satisfied. You should try to satisfy these prerequisites as quickly as possible.");
                }
                htm.eP();
                break;

            case MSG_1G:
                htm.sP().add(DISC);
                if (critPrearrivPrereqs == 1) {
                    htm.add("The fastest way to satisfy this prerequisite");
                } else {
                    htm.add("The fastest way to satisfy these prerequisites");
                }
                htm.add(" is through <strong class='headercolor'>Math Placement</strong>. ",
                        "Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_1H:
                htm.sP().add(DISC);
                htm.add("You still have <strong class='headercolor'>Math Placement</strong> opportunities to satisfy ");
                if (critPrearrivPrereqs == 1) {
                    htm.add("this prerequisite. ");
                } else {
                    htm.add("these prerequisites. ");
                }
                htm.add("Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_1I:
                htm.sP().add(DISC);
                if (critPrearrivPrereqs == 1) {
                    htm.add("You may also complete a section of the <strong class='headercolor'>",
                            "Precalculus Tutorial</strong> to satisfy this prerequisite.");
                } else {
                    htm.add("You may also complete sections of the <strong class='headercolor'>",
                            "Precalculus Tutorial</strong> to satisfy these prerequisites.");
                }
                htm.eP();
                break;

            case MSG_1J:
                htm.sP().add(DISC);
                if (critPrearrivPrereqs == 1) {
                    htm.add("You may complete a section of the <strong class='headercolor'>",
                            "Precalculus Tutorial</strong> to satisfy this prerequisite.");
                } else {
                    htm.add("You may complete sections of the <strong class='headercolor'>",
                            "Precalculus Tutorial</strong> to satisfy these prerequisites.");
                }
                htm.eP();
                break;

            case MSG_1K:
                htm.sP().add(DISC);
                htm.add("You also have <strong class='headercolor'>Math Placement</strong> opportunities to satisfy ");
                if (critPrearrivPrereqs == 1) {
                    htm.add("this prerequisite. ");
                } else {
                    htm.add("these prerequisites. ");
                }
                htm.add("Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_1L_SINGULAR:
                htm.sP().add(DISC);
                htm.add("You may complete the <strong class='headercolor'>Entry-Level Mathematics (ELM) ",
                        "Tutorial</strong> to allow you to register for <strong class='headercolor'>MATH 117: College ",
                        "Algebra in Context I</strong>, but this will NOT satisfy the ");
                if (critPrearrivPrereqs == 1) {
                    htm.add("prerequisite");
                } else {
                    htm.add("prerequisites");
                }
                htm.add(" for your first-semester course.");
                htm.eP();
                break;

            case MSG_1L_PLURAL:
                htm.sP().add(DISC);
                htm.add("You may complete the <strong class='headercolor'>Entry-Level Mathematics (ELM) ",
                        "Tutorial</strong> to allow you to register for <strong class='headercolor'>MATH 117: College ",
                        "Algebra in Context I</strong>, but this will NOT satisfy the ");
                if (critPrearrivPrereqs == 1) {
                    htm.add("prerequisite");
                } else {
                    htm.add("prerequisites");
                }
                htm.add(" for your first-semester courses.");
                htm.eP();
                break;

            case MSG_1M_SINGULAR:
                htm.sP().add(STAR);
                htm.add("You should speak with your adviser (if you have been assigned an advisor), or to the ",
                        "Department of Mathematics (if not) about your options to satisfy the prerequisites for your ",
                        "first-semester mathematics course.");
                htm.eP();
                break;

            case MSG_1M_PLURAL:
                htm.sP().add(STAR);
                htm.add("You should speak with your adviser (if you have been assigned an advisor), or to the ",
                        "Department of Mathematics (if not) about your options to satisfy the prerequisites for your ",
                        "first-semester mathematics courses.");
                htm.eP();
                break;

            case MSG_1N:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("The fastest way to satisfy this prerequisite");
                } else {
                    htm.add("The fastest way to satisfy these prerequisites");
                }
                htm.add(" is through <strong class='headercolor'>Math Placement</strong>. ",
                        "Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_1O:
                htm.sP().add(DISC);
                htm.add("You still have <strong class='headercolor'>Math Placement</strong> opportunities to satisfy ");
                if (typPrearrivalPrereqs == 1) {
                    htm.add("this prerequisite. ");
                } else {
                    htm.add("these prerequisites. ");
                }
                htm.add("Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_1P:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("You may also complete a section of the <strong class='headercolor'>Precalculus ",
                            "Tutorial</strong> to satisfy this prerequisite.");
                } else {
                    htm.add("You may also complete sections of the <strong class='headercolor'>Precalculus ",
                            "Tutorial</strong> to satisfy these prerequisites.");
                }
                htm.eP();
                break;

            case MSG_1Q:
                htm.sP().add(DISC);
                if (typPrearrivalPrereqs == 1) {
                    htm.add("You may complete a section of the <strong class='headercolor'>Precalculus ",
                            "Tutorial</strong> to satisfy this prerequisite.");
                } else {
                    htm.add("You may complete sections of the <strong class='headercolor'>Precalculus ",
                            "Tutorial</strong> to satisfy these prerequisites.");
                }
                htm.eP();
                break;

            case MSG_1R:
                htm.sP().add(DISC);
                htm.add("You also have <strong class='headercolor'>Math Placement</strong> opportunities to satisfy ");
                if (typPrearrivalPrereqs == 1) {
                    htm.add("this prerequisite. ");
                } else {
                    htm.add("these prerequisites. ");
                }
                htm.add("Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_1S_SINGULAR:
                htm.sP().add(DISC);
                htm.add("You may complete the <strong class='headercolor'>Entry-Level Mathematics (ELM) ",
                        "Tutorial</strong> to allow you to register for <strong class='headercolor'>MATH 117: College ",
                        "Algebra in Context I</strong>, but this will NOT satisfy the ");
                if (typPrearrivalPrereqs == 1) {
                    htm.add("prerequisite");
                } else {
                    htm.add("prerequisites");
                }
                htm.add(" for your first-semester course.");
                htm.eP();
                break;

            case MSG_1S_PLURAL:
                htm.sP().add(DISC);
                htm.add("You may complete the <strong class='headercolor'>Entry-Level Mathematics (ELM) ",
                        "Tutorial</strong> to allow you to register for <strong class='headercolor'>MATH 117: College ",
                        "Algebra in Context I</strong>, but this will NOT satisfy the ");
                if (typPrearrivalPrereqs == 1) {
                    htm.add("prerequisite");
                } else {
                    htm.add("prerequisites");
                }
                htm.add(" for your first-semester courses.");
                htm.eP();
                break;

            //
            //
            //

            case MSG_2A:
                htm.sP().add(STAR);
                htm.add("It is important that you take <strong class='headercolor'>MATH 117: ",
                        "College Algebra in Context I</strong> during ", semester, CoreConstants.DOT);
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.add(" Ideally, you will register for this course during Orientation.");
                }
                htm.eP();
                break;

            case MSG_2B:
                htm.sP().add(DISC);
                htm.add("We recommend that you take <strong class='headercolor'>MATH 117: College ",
                        "Algebra in Context I</strong> during ", semester, CoreConstants.DOT);
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.add(" Ideally, you will register for this course during Orientation.");
                }
                htm.eP();
                break;

            case MSG_2C:
                htm.sP().add(DISC);
                htm.add("If you would like to take <strong class='headercolor'>MATH 117: College ",
                        "Algebra in Context I</strong> during ", semester, ", you should try ",
                        "to become eligible for that course in time to register before classes begin.");
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.add(" (ideally during Orientation).");
                }
                htm.eP();
                break;

            case MSG_2D:
                htm.sP().add(STAR);
                htm.add("It is important that you take ", m101OrFirst, " during ", semester, CoreConstants.DOT);
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.add(" Ideally, you will register for this course during Orientation.");
                }
                htm.eP();
                break;

            case MSG_2E:
                htm.sP().add(DISC);
                htm.add("We recommend that you take ", m101OrFirst, " during ", semester, CoreConstants.DOT);
                if (applicationTerm == ETermName.FALL || applicationTerm == ETermName.SPRING) {
                    htm.add(" Ideally, you will register for this course during Orientation.");
                }
                htm.eP();
                break;

            case MSG_2F:
                htm.sP().add(STAR);
                htm.add("It is important that you take <strong class='headercolor'>MATH 117: ",
                        "College Algebra in Context I</strong> as quickly as possible.");
                htm.eP();
                break;

            case MSG_2G:
                htm.sP().add(DISC);
                htm.add("We recommend that you take <strong class='headercolor'>MATH 117: College ",
                        "Algebra in Context I</strong> as quickly as possible.");
                htm.eP();
                break;

            case MSG_2H:
                htm.sP().add(DISC);
                htm.add("We recommend that you take <strong class='headercolor'>MATH 117: College Algebra in Context ",
                        "I</strong> as quickly as possible.");
                htm.eP();
                break;

            case MSG_2I:
                htm.sP().add(STAR);
                htm.add("It is important that you take ", m101OrFirst, " as quickly as possible.");
                htm.eP();
                break;

            case MSG_2J:
                htm.sP();
                htm.add(DISC);
                htm.add("We recommend that you take ", m101OrFirst, " as quickly as possible.");
                htm.eP();
                break;

            case MSG_2K:
                htm.sP().add(DISC);
                htm.add("The fastest way to become eligible for <strong>MATH 117</strong> is through ",
                        "<strong class='headercolor'>Math Placement</strong>. Examples and practice materials are ",
                        "provided if you would like to review.");
                htm.eP();
                break;

            case MSG_2L:
                htm.sP().add(DISC);
                htm.add("You only need to complete the Algebra portion of <strong class='headercolor'>Math ",
                        "Placement</strong> to become eligible for <strong>MATH 117</strong>, but completing as much ",
                        "as you can may give you more options in case you change majors.");
                htm.eP();
                break;

            case MSG_2M:
                htm.sP().add(DISC);
                htm.add("You still have <strong class='headercolor'>Math Placement</strong> opportunities to become ",
                        "eligible for <strong>MATH 117</strong>. ",
                        "Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_2N:
                htm.sP().add(DISC);
                htm.add("You can also become eligible for <strong>MATH 117</strong> by completing the ",
                        "<strong class='headercolor'>Entry Level Mathematics (ELM) Tutorial</strong>.");
                htm.eP();
                break;

            case MSG_2O:
                htm.sP().add(DISC);
                htm.add("You can become eligible for <strong>MATH 117</strong> by completing the ",
                        "<strong class='headercolor'>Entry Level Mathematics (ELM) Tutorial</strong>.");
                htm.eP();
                break;

            case MSG_2P:
                htm.sP().add(DISC);
                htm.add("You also have <strong class='headercolor'>Math Placement</strong> ",
                        "opportunities to become eligible for <strong>MATH 117</strong>. ",
                        "Examples and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_2Q:
                htm.sP().add(DISC);
                htm.add("You can become eligible for ", m101OrThat,
                        " through <strong class='headercolor'>Math Placement</strong>.");
                htm.eP();
                break;

            case MSG_2R:
                htm.sP().add(DISC);
                htm.add("You only need to complete the Algebra portion of ",
                        "<strong class='headercolor'>Math Placement</strong> to become eligible for ",
                        m101OrThat, ", but completing as much as you can may give you more options in ",
                        "case you change majors");
                htm.eP();
                break;

            //
            //
            //

            case MSG_3A:
                htm.sP(CENTER).add(CHECK);
                htm.add("No further action is needed.");
                htm.eP();
                break;

            case MSG_3B:
                htm.sP().add(DISC);
                htm.add("We recommend that you complete the required three credits of Core Curriculum mathematics ",
                        "within your first thirty credit hours.");
                htm.eP();
                break;

            case MSG_3C:
                htm.sP().add(DISC);
                htm.add("If you decide to pursue a major that requires College Algebra or other Precalculus or ",
                        "Calculus courses, you can go through <strong class='headercolor'>Math Placement</strong> or ",
                        "complete a tutorial to become eligible for those mathematics courses.");
                htm.eP();
                break;

            case MSG_3D:
                htm.sP().add(DISC);
                htm.add("You are eligible to take the <strong class='headercolor'>Math Challenge Exam</strong>. With ",
                        "this exam, you can attempt to earn credit in one or more of your required courses. Examples ",
                        "and practice materials are provided if you would like to review.");
                htm.eP();
                break;

            case MSG_3E:
                htm.sP().add(DISC);
                htm.add("You can attempt to place out of one or more courses through ",
                        "<strong class='headercolor'>Math Placement</strong>. Examples and practice materials are ",
                        "provided if you would like to review.");
                htm.eP();
                break;

            case MSG_3F:
                htm.sP().add(DISC);
                htm.add("You still have <strong class='headercolor'>Math Placement </strong> opportunities that could ",
                        "be used to place out of one or more courses. Examples and practice materials are provided if ",
                        "you would like to review.");
                htm.eP();
                break;

            case MSG_3G:
                htm.sP().add(DISC);
                htm.add("You do not need to complete the Math Placement process.");
                htm.eP();
                break;

            case MSG_3H:
                htm.sP().add(DISC);
                htm.add("You do not need to complete the Math Placement process unless you plan to take a math course ",
                        "other than MATH 101, MATH 105, STAT 100, STAT 201, or STAT 204.");
                htm.eP();
                break;

            //
            //
            //

            case MSG_4A:
                htm.sP(CENTER).add(CHECK);
                htm.add("No further action is needed.");
                htm.eP();
                break;

            case MSG_4B:
                htm.sP().add(DISC);
                htm.add("You do not need to complete the Math Placement process.");
                htm.eP();
                break;

            case MSG_4C:
                htm.sP().add(DISC);
                htm.add("Your existing course credit satisfies the requirements of your selected major(s) of ",
                        "interest.");
                htm.eP();
                break;

            //
            //
            //

            case ACT_MATH_PLACEMENT_EXAM:
                if (showActions) {
                    htm.sDiv(CENTER);
                    htm.add("<a class='btn' href='placement_process.html'>",
                            "Tell me about <span class='hideabove600'><br/></span> Math Placement...</a>");
                    htm.eDiv();
                }
                break;

            case ACT_ELM_TUTORIAL:
                if (showActions) {
                    htm.sDiv(CENTER);
                    htm.add("<a class='btn' href='elm.html'>",
                            "Tell me about <span class='hideabove600'><br/></span>the ELM Tutorial...</a>");
                    htm.eDiv();
                }
                break;

            case ACT_PRECALCULUS_TUTORIAL:
                if (showActions) {
                    htm.sDiv(CENTER);
                    htm.add("<a class='btn' href='precalc.html'>",
                            "Tell me about <span class='hideabove600'><br/></span>the Precalculus Tutorial...</a>");
                    htm.eDiv();
                }
                break;

            case ACT_CONTACT_MY_ADVISER:
                if (showActions) {
                    htm.sDiv(CENTER);
                    // If we have an adviser email address on the student's record, make the button into
                    // an email link.
                    final String email = data.student.adviserEmail;
                    if (email == null) {
                        htm.add("<a class='btn' href='contact_adviser.html'>Contact my adviser...</a>");
                    } else {
                        htm.add("<a class='btn' href='mailto:", email, "'>Contact my adviser...</a>");
                    }
                    htm.eDiv();
                }
                break;

            default:
                break;
        }
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

        final DbProfile profile = site.getDbProfile();
        final MathPlanLogic logic = new MathPlanLogic(profile);

        final boolean aff1 = req.getParameter("affirm1") != null;
        final boolean aff2 = req.getParameter("affirm2") != null;

        // Only perform updates if this is not an adviser using "Act As"
        if (session.actAsUserId == null) {
            final String effectiveId = session.getEffectiveUserId();
            final ZonedDateTime sessNow = session.getNow();
            final StudentData data = logic.getStudentData(cache, effectiveId, sessNow, session.loginSessionTag, true);

            logic.deleteMathPlanResponses(cache, data.student, MathPlanConstants.INTENTIONS_PROFILE, sessNow,
                    session.loginSessionTag);

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

                final DbContext liveCtx = profile.getDbContext(ESchemaUse.LIVE);
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
