package dev.mathops.web.host.precalc.course;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
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
 * Presents a unit (or midterm/final) exam.
 */
enum PageHtmlUnitExam {
    ;

    /**
     * Starts a unit exam and presents the exam instructions.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void startUnitExam(final Cache cache, final CourseSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session,
                              final CourseSiteLogic logic) throws IOException, SQLException {

        final String examId = req.getParameter("exam");
        final String course = req.getParameter("course");

        if (AbstractSite.isParamInvalid(examId) || AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  examId='", examId, "'");
            Log.warning("  course='", course, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
            UnitExamSession us = store.getUnitExamSession(session.loginSessionId, examId);

            if (us == null) {
                final String redirect = "course.html?course=" + course + "&mode=course";

                Log.info("Starting unit exam for session ", session.loginSessionId, " user ",
                        session.getEffectiveUserId(), " exam ", examId);

                us = new UnitExamSession(cache, site.site, session.loginSessionId,
                        session.getEffectiveUserId(), course, examId, redirect);
                store.setUnitExamSession(us);
            } else {
                Log.info("Found existing unit exam for session ", session.loginSessionId, " exam ", examId);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                    false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

            htm.addln("<form id='unit_exam_form' action='update_unit_exam.html'>");
            htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
            htm.addln(" <input type='hidden' name='course' value='", course, "'>");
            htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
            us.generateHtml(cache, session, htm);
            htm.addln("</form>");

            htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Handles a POST request.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateUnitExam(final Cache cache, final CourseSite site, final ServletRequest req,
                               final HttpServletResponse resp, final ImmutableSessionInfo session,
                               final CourseSiteLogic logic) throws IOException, SQLException {

        final String examId = req.getParameter("exam");
        final String course = req.getParameter("course");

        if (AbstractSite.isParamInvalid(examId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  examId='", examId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
            final UnitExamSession res = store.getUnitExamSession(session.loginSessionId, examId);

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                    false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

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
                htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
                redirect = res.processPost(cache, session, req, htm);
                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
            } else {
                Log.info("Redirect is ", redirect);

                resp.sendRedirect(redirect);
            }
        }
    }
}
