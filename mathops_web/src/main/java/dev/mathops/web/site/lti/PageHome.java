package dev.mathops.web.site.lti;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.unitexam.UnitExamSession;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the home page.
 */
enum PageHome {
    ;

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final LtiSite site, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException, SQLException {

        final String exam = req.getParameter("exam");
        final String sid = req.getParameter("sid");

        if (exam == null || exam.length() != 5) {
            Log.warning("Missing or invalid exam ID");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (AbstractSite.isParamInvalid(exam) || AbstractSite.isParamInvalid(sid)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  exam='", exam, "'");
            Log.warning("  sid='", sid, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ImmutableSessionInfo session = SessionManager.getInstance().getUserSession(sid);

            if (session == null) {
                PageIndex.generatePage(req, resp, exam, null, "Invalid student ID");
            } else {
                // Re-test eligibility in case a student navigates here somehow

                final String title;
                final String course;

                final String digits = exam.substring(0, 2);
                if ("MT".equals(digits)) {
                    title = "ELM Exam";
                    course = RawRecordConstants.M100T;
                } else {
                    final String courseLabel = "MATH 1" + digits;
                    if (exam.endsWith("FIN")) {
                        title = courseLabel + " Final Exam";
                    } else {
                        final String unitLabel = exam.substring(2, 3);
                        title = courseLabel + " Unit " + unitLabel + " Exam";
                    }
                    course = "M 1" + exam.substring(0, 2);
                }

                int unitNum = 0;
                if (exam.endsWith("FIN")) {
                    unitNum = 5;
                } else if (exam.charAt(2) == '1') {
                    unitNum = 1;
                } else if (exam.charAt(2) == '2') {
                    unitNum = 2;
                } else if (exam.charAt(2) == '3') {
                    unitNum = 3;
                } else if (exam.charAt(2) == '4') {
                    unitNum = 4;
                }

                final StudentCourseStatus courseStatus = new StudentCourseStatus(site.site.profile);
                courseStatus.gatherData(cache, session, session.getEffectiveUserId(), course, false, false);

                final boolean unitAvail = courseStatus.isProctoredExamAvailable(unitNum);

                if (unitAvail) {
                    final HtmlBuilder htm = new HtmlBuilder(2000);

                    Page.startPage(htm, title, false, false);

                    htm.addln("<body style='background:white'>");

                    final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
                    UnitExamSession us = store.getUnitExamSession(session.loginSessionId, exam);

                    if (us == null) {
                        final String redirect = "course.html";

                        Log.info("Starting unit exam for session ", session.loginSessionId, " user ",
                                session.getEffectiveUserId(), " exam ", exam);

                        us = new UnitExamSession(cache, site.site, session.loginSessionId,
                                session.getEffectiveUserId(), course, exam, redirect);
                        store.setUnitExamSession(us);
                    } else {
                        Log.info("Found existing unit exam for session ", session.loginSessionId, " exam ", exam);
                    }

                    htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' method='POST'>");
                    htm.addln(" <input type='hidden' name='exam' value='", exam, "'>");
                    htm.addln(" <input type='hidden' name='sid' value='", sid, "'>");
                    htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
                    us.generateHtml(cache, session, htm);
                    htm.addln("</form>");

                    htm.addln("</body>");
                    htm.addln("</html>");

                    AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
                } else {
                    PageIndex.generatePage(req, resp, exam, null, "You are not currently eligible for this exam");
                }
            }
        }
    }

    /**
     * Handles a POST request.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateUnitExam(final Cache cache, final ServletRequest req,
                               final HttpServletResponse resp) throws IOException, SQLException {

        final String exam = req.getParameter("exam");
        final String sid = req.getParameter("sid");

        if (exam == null) {
            Log.warning("Missing required 'exam' parameter");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (sid == null) {
            Log.warning("Missing required 'sid' parameter");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (AbstractSite.isParamInvalid(exam) || AbstractSite.isParamInvalid(sid)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  examId='", exam, "'");
            Log.warning("  sid='", sid, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ImmutableSessionInfo session = SessionManager.getInstance().getUserSession(sid);

            if (session == null) {
                Log.warning("Unit Exam POST: Session ", sid, " not found.");
                PageIndex.generatePage(req, resp, exam, null, "Login session has timed out.");
            } else {
                final HtmlBuilder htm = new HtmlBuilder(2000);

                final String courseLabel = "MATH 1" + exam.substring(0, 2);
                final String unitLabel = exam.substring(2, 3);
                final String title = courseLabel + " Unit " + unitLabel + " Exam";

                Page.startPage(htm, title, false, false);

                htm.addln("<body style='background:white'>");

                final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
                final UnitExamSession res = store.getUnitExamSession(session.loginSessionId, exam);

                String redirect = null;
                if (res == null) {
                    htm.sDiv("indent33");
                    htm.sP().add("Exam session not found.").eP();
                    htm.addln("<form action='home.html' method='GET'>");
                    htm.addln(" <input type='submit' value='Close'>");
                    htm.addln("</form>");
                    htm.eDiv();
                } else {
                    htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' method='POST'>");
                    htm.addln(" <input type='hidden' name='exam' value='", exam, "'>");
                    htm.addln(" <input type='hidden' name='sid' value='", sid, "'>");
                    htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
                    redirect = res.processPost(cache, session, req, htm);
                    htm.addln("</form>");
                }

                if (redirect == null) {
                    htm.addln("</body>");
                    htm.addln("</html>");

                    AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
                } else {
                    resp.sendRedirect(redirect);
                }
            }
        }
    }
}
