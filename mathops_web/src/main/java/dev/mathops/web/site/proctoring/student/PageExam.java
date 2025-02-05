package dev.mathops.web.site.proctoring.student;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.unitexam.EUnitExamState;
import dev.mathops.web.site.html.unitexam.UnitExamSession;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;
import dev.mathops.web.websocket.proctor.MPSEndpoint;
import dev.mathops.web.websocket.proctor.MPSSession;
import dev.mathops.web.websocket.proctor.MPSSessionManager;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;

/**
 * A page that will display in an IFrame to present the exam within a proctoring session.
 */
enum PageExam {
    ;

    /** A pre-compiled regular expression pattern. */
    private static final Pattern PATTERN = Pattern.compile(CoreConstants.SPC);

    /**
     * Generates an empty page.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void doEmpty(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:calc(100% - 50px);'>");
        htm.addln("</body>");
        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

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
    static void doGet(final Cache cache, final ProctoringSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final MPSSession ps = MPSSessionManager.getInstance().getSessionForStudent(session.getEffectiveUserId());

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:calc(100% - 50px);'>");

        if (ps == null) {
            htm.sP().add("Logged in as: ", session.getEffectiveScreenName()).eP();
            htm.sP().add("No active proctoring session found for student ", session.getEffectiveUserId(),
                    CoreConstants.DOT).eP();
        } else {
            ps.timeout = System.currentTimeMillis() + MPSEndpoint.SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(ps.timeout), ZoneId.systemDefault());
            Log.info("Updating timeout on session ", ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            htm.sDiv().add(
                    "You are welcome to use scratch paper, and you can use either your own personal ",
                    "graphing calculator, or the <strong>Desmos calculator</strong> during your exam. ",
                    "&nbsp; <a href='https://www.desmos.com/calculator' target='_blank' rel='noopener'>",
                    "Open Desmos...</a>").eDiv();
            htm.div("vgap0");
            htm.hr();

            final String examId = ps.examId;

            switch (examId) {
                case "MT4UE" -> emitELMExam(cache, ps, site, session, htm);
                case "7T4UE" -> emitUnitExam(cache, ps, site, session, htm, RawRecordConstants.M1170, examId);
                case "8T4UE" -> emitUnitExam(cache, ps, site, session, htm, RawRecordConstants.M1180, examId);
                case "4T4UE" -> emitUnitExam(cache, ps, site, session, htm, RawRecordConstants.M1240, examId);
                case "5T4UE" -> emitUnitExam(cache, ps, site, session, htm, RawRecordConstants.M1250, examId);
                case "6T4UE" -> emitUnitExam(cache, ps, site, session, htm, RawRecordConstants.M1260, examId);
                case null, default -> {
                    final String courseNumDigits = examId.substring(0, 2);
                    final boolean isCourse = "17".equals(courseNumDigits) || "18".equals(courseNumDigits)
                            || "24".equals(courseNumDigits) || "25".equals(courseNumDigits)
                            || "26".equals(courseNumDigits);

                    if (examId.endsWith("FIN")) {
                        if (isCourse) {
                            emitFinalExam(cache, ps, site, session, htm, "M 1" + courseNumDigits, examId);
                        } else {
                            htm.sP().addln("Unrecognized final exam ID").eP();
                        }
                    } else if (examId.endsWith("UE")) {
                        final String unit = examId.substring(2, 3);

                        if (isCourse) {
                            if ("1".equals(unit) || "2".equals(unit) || "3".equals(unit) || "4".equals(unit)) {
                                emitUnitExam(cache, ps, site, session, htm, "M 1" + courseNumDigits, examId);
                            } else {
                                htm.sP().addln("Unrecognized unit exam ID").eP();
                            }
                        } else {
                            htm.sP().addln("Unrecognized unit exam ID").eP();
                        }
                    }
                }
            }
        }

        htm.addln("</body>");
        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

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
    static void doPost(final Cache cache, final ProctoringSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final MPSSession ps = MPSSessionManager.getInstance().getSessionForStudent(session.getEffectiveUserId());

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:calc(100% - 50px);'>");

        if (ps == null) {
            htm.sP().add("Logged in as: ", session.getEffectiveScreenName()).eP();
            htm.sP().add("No active proctoring session found for student ", session.getEffectiveUserId(), ".").eP();
        } else {
            ps.timeout = System.currentTimeMillis() + MPSEndpoint.SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(ps.timeout), ZoneId.systemDefault());
            Log.info("Updating timeout on session ", ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            htm.sDiv().add(
                    "You are welcome to use scratch paper, and you can use either your own personal ",
                    "graphing calculator, or the <strong>Desmos calculator</strong> during your exam. ",
                    "&nbsp; <a href='https://www.desmos.com/calculator' target='_blank' rel='noopener'>",
                    "Open Desmos...</a>").eDiv();
            htm.div("vgap0");

            htm.hr();

            final String examId = ps.examId;

            if ("MT4UE".equals(examId)) {
                emitELMExam(cache, ps, site, session, htm);
            } else {
                final String courseNumDigits = examId.substring(0, 2);
                final boolean isCourse = "17".equals(courseNumDigits) || "18".equals(courseNumDigits)
                        || "24".equals(courseNumDigits) || "25".equals(courseNumDigits)
                        || "26".equals(courseNumDigits);

                if (examId.endsWith("FIN")) {
                    if (isCourse) {
                        emitFinalExam(cache, ps, site, session, htm, "M 1" + courseNumDigits, examId);
                    } else {
                        htm.sP().addln("Unrecognized final exam ID").eP();
                    }
                } else if (examId.endsWith("UE")) {
                    final String unit = examId.substring(2, 3);

                    if (isCourse) {
                        if ("1".equals(unit) || "2".equals(unit) || "3".equals(unit) || "4".equals(unit)) {
                            emitUnitExam(cache, ps, site, session, htm, "M 1" + courseNumDigits, examId);
                        } else {
                            htm.sP().addln("Unrecognized unit exam ID").eP();
                        }
                    } else {
                        htm.sP().addln("Unrecognized unit exam ID").eP();
                    }
                }
            }
        }

        htm.addln("</body>");
        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits an ELM Exam session (exam ID "MT4UE").
     *
     * @param cache   the data cache
     * @param ps      the proctoring session
     * @param site    the owning site
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitELMExam(final Cache cache, final MPSSession ps, final ProctoringSite site,
                                    final ImmutableSessionInfo session, final HtmlBuilder htm) throws SQLException {

        final String examId = "MT4UE";

        final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
        UnitExamSession us = store.getUnitExamSession(session.loginSessionId, examId);

        if (us == null) {
            final String redirect = "unit_done.html?course=M%20100T";

            Log.info("Starting unit exam for session ", session.loginSessionId, " user ",
                    session.getEffectiveUserId(), " exam ", examId);

            us = new UnitExamSession(cache, site.site, session.loginSessionId,
                    session.getEffectiveUserId(), RawRecordConstants.M100T, examId, redirect);
            store.setUnitExamSession(us);
        } else {
            Log.info("Found existing unit exam for session ", session.loginSessionId, " exam ", examId);

            if (ps.justStarted) {
                // Student is trying to start a new exam - if the old exam is in some post-exam
                // state, close it and start a new one
                if (us.getState() == EUnitExamState.COMPLETED || us.getState() == EUnitExamState.SOLUTION_NN) {
                    us.closeSession(session);

                    final String redirect = "unit_done.html?course=M%20100T";
                    us = new UnitExamSession(cache, site.site, session.loginSessionId,
                            session.getEffectiveUserId(), RawRecordConstants.M100T, examId, redirect);
                    store.setUnitExamSession(us);
                }
            }
        }

        htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' method='POST'>");
        htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
        htm.addln(" <input type='hidden' name='sid' value='", session.loginSessionId, "'>");
        htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
        us.generateHtml(cache, session, htm);
        htm.addln("</form>");
    }

    /**
     * Emits a course final exam session (exam ID "##FIN").
     *
     * @param cache    the data cache
     * @param ps       the proctoring session
     * @param site     the owning site
     * @param session  the login session
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param courseId the course ID
     * @param examId   the exam ID
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitFinalExam(final Cache cache, final MPSSession ps, final ProctoringSite site,
                                      final ImmutableSessionInfo session, final HtmlBuilder htm, final String courseId,
                                      final String examId) throws SQLException {

        final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
        UnitExamSession us = store.getUnitExamSession(session.loginSessionId, examId);

        if (us == null) {
            final String redirect = "unit_done.html?course=" + courseId;

            Log.info("Starting unit exam for session ", session.loginSessionId, " user ",
                    session.getEffectiveUserId(), " exam ", examId);

            us = new UnitExamSession(cache, site.site, session.loginSessionId,
                    session.getEffectiveUserId(), courseId, examId, redirect);
            store.setUnitExamSession(us);
        } else {
            Log.info("Found existing unit exam for session ", session.loginSessionId, " exam ", examId);

            if (ps.justStarted) {
                // Student is trying to start a new exam - if the old exam is in some post-exam
                // state, close it and start a new one

                if (us.getState() == EUnitExamState.COMPLETED || us.getState() == EUnitExamState.SOLUTION_NN) {
                    us.closeSession(session);

                    final String redirect = "unit_done.html?course=" + courseId;
                    us = new UnitExamSession(cache, site.site, session.loginSessionId,
                            session.getEffectiveUserId(), courseId, examId, redirect);
                    store.setUnitExamSession(us);
                }
            }
        }

        htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' ", "method='POST'>");
        htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
        htm.addln(" <input type='hidden' name='sid' value='", session.loginSessionId, "'>");
        htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
        us.generateHtml(cache, session, htm);
        htm.addln("</form>");
    }

    /**
     * Emits a course unit exam session (exam ID "###UE").
     *
     * @param cache    the data cache
     * @param ps       the proctoring session
     * @param site     the owning site
     * @param session  the login session
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param courseId the course ID
     * @param examId   the exam ID
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitUnitExam(final Cache cache, final MPSSession ps, final ProctoringSite site,
                                     final ImmutableSessionInfo session, final HtmlBuilder htm, final String courseId,
                                     final String examId) throws SQLException {

        final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
        UnitExamSession us = store.getUnitExamSession(session.loginSessionId, examId);

        if (us == null) {
            final String redirect = "unit_done.html?course=" + courseId;

            Log.info("Starting unit exam for session ", session.loginSessionId, " user ",
                    session.getEffectiveUserId(), " exam ", examId);

            us = new UnitExamSession(cache, site.site, session.loginSessionId,
                    session.getEffectiveUserId(), courseId, examId, redirect);
            store.setUnitExamSession(us);
        } else {
            Log.info("Found existing unit exam for session ", session.loginSessionId, " exam ", examId);

            if (ps.justStarted) {
                // Student is trying to start a new exam - if the old exam is in some post-exam
                // state, close it and start a new one

                if (us.getState() == EUnitExamState.COMPLETED || us.getState() == EUnitExamState.SOLUTION_NN) {
                    us.closeSession(session);

                    final String redirect = "unit_done.html?course=" + courseId;
                    us = new UnitExamSession(cache, site.site, session.loginSessionId,
                            session.getEffectiveUserId(), courseId, examId, redirect);
                    store.setUnitExamSession(us);
                }
            }
        }

        htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' ", "method='POST'>");
        htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
        htm.addln(" <input type='hidden' name='sid' value='", session.loginSessionId, "'>");
        htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
        us.generateHtml(cache, session, htm);
        htm.addln("</form>");
    }

    /**
     * Handles a POST request to 'update_unit_exam.html'.
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateUnitExam(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                               final ImmutableSessionInfo session) throws IOException, SQLException {

        final MPSSession ps = MPSSessionManager.getInstance().getSessionForStudent(session.getEffectiveUserId());

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:calc(100% - 50px);'>");

        if (ps == null) {
            htm.sP().add("Logged in as: ", session.getEffectiveScreenName()).eP();
            htm.sP().add("No active proctoring session found for student ",
                    session.getEffectiveUserId(), CoreConstants.DOT).eP();
        } else {
            htm.sDiv().add(
                    "You are welcome to use scratch paper, and you can use either your own personal ",
                    "graphing calculator, or the <strong>Desmos calculator</strong> during your exam. ",
                    "&nbsp; <a href='https://www.desmos.com/calculator' target='_blank' rel='noopener'>",
                    "Open Desmos...</a>").eDiv();
            htm.div("vgap0");

            htm.hr();

            final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
            final UnitExamSession res = store.getUnitExamSession(session.loginSessionId, ps.examId);

            String redirect = null;
            if (res == null) {
                htm.sDiv("indent33");
                htm.sP().add("Exam not found.").eP();
                htm.addln("<form action='home.html' method='GET'>");
                htm.addln(" <input type='submit' value='Close'>");
                htm.addln("</form>");
                htm.eDiv();
            } else {
                htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' method='POST'>");
                htm.addln(" <input type='hidden' name='exam' value='", ps.examId, "'>");
                htm.addln(" <input type='hidden' name='course' value='", ps.courseId, "'>");
                htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
                redirect = res.processPost(cache, session, req, htm);
                htm.addln("</form>");
            }

            htm.addln("</body>");
            Page.endPage(htm);

            if (redirect == null) {
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
            } else {
                Log.info("Redirect is ", redirect);
                resp.sendRedirect(redirect);
            }
        }
    }

    /**
     * Generates the page.
     *
     * @param type the site type
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void showDone(final ESiteType type, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException {

        final String course = req.getParameter("course");
        final String urlCourse = PATTERN.matcher(course).replaceAll("%20");

        final boolean isElmTutorial = RawRecordConstants.M100T.equals(course);

        final boolean isPrecalcTutorial = RawRecordConstants.M1170.equals(course)
                || RawRecordConstants.M1180.equals(course) || RawRecordConstants.M1240.equals(course)
                || RawRecordConstants.M1250.equals(course) || RawRecordConstants.M1260.equals(course);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:calc(100% - 50px);'>");

        htm.div("vgap");
        htm.sH(3).add("The exam has been completed").eH(3);
        htm.sP("indent");

        htm.sDiv("inset2");
        if (isElmTutorial) {
            htm.add("<button class='btn' id='back_to_course'>",
                    "Return to ELM Tutorial web site</button>");
        } else if (isPrecalcTutorial) {
            htm.add("<button class='btn' id='back_to_course'>",
                    "Return to Precalculus Tutorial web site</button>");
        } else {
            htm.add("<button class='btn' id='back_to_course'>",
                    "Return to course web site</button>");
        }
        htm.eDiv(); // inset2
        htm.eP();

        htm.addln("<script>");
        htm.addln("document.getElementById('back_to_course').onclick = function(ev) {");
        htm.add("  window.top.location.href = \"https://");

        if (isElmTutorial) {
            if (type == ESiteType.PROD) {
                htm.add("placement.math.colostate.edu");
            } else {
                htm.add("placementdev.math.colostate.edu");
            }
            htm.add("/elm-tutorial/tutorial.html\";");
        } else if (isPrecalcTutorial) {

            if (type == ESiteType.PROD) {
                htm.add("placement.math.colostate.edu");
            } else {
                htm.add("placementdev.math.colostate.edu");
            }
            htm.add("/precalc-tutorial/course.html?course=", urlCourse,
                    "&mode=course\";");
        } else {
            if (type == ESiteType.PROD) {
                htm.add("precalc.math.colostate.edu");
            } else {
                htm.add("precalcdev.math.colostate.edu");
            }
            htm.add("/instruction/course.html?course=", urlCourse,
                    "&mode=course\";");
        }

        htm.addln();
        htm.addln("}");
        htm.addln("let event = new CustomEvent('examEnded')");
        htm.addln("window.parent.document.dispatchEvent(event)");
        htm.addln("</script>");

        htm.addln("</body>");

        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
