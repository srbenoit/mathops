package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.pastexam.PastExamSession;
import dev.mathops.web.site.html.pastexam.PastExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Presents a past exam.
 */
enum PageHtmlPastExam {
    ;

    /**
     * Starts a review exam and presents the exam instructions.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void startPastExam(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session,
                              final ELMTutorialStatus status) throws IOException, SQLException {

        final String xml = req.getParameter("xml");
        final String upd = req.getParameter("upd");
        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");

        if (AbstractSite.isFileParamInvalid(xml) || AbstractSite.isFileParamInvalid(upd)
                || AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  xml='", xml, "'");
            Log.warning("  upd='", upd, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  exam='", exam, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final PastExamSessionStore store = PastExamSessionStore.getInstance();
            PastExamSession pes = store.getPastExamSession(session.loginSessionId, xml);

            if (pes == null) {
                pes = new PastExamSession(cache, site.siteProfile, session.loginSessionId, exam,
                        xml, session.getEffectiveUserId(), "tutorial_status.html");
                store.setPastExamSession(pes);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                    "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            htm.addln("<form id='past_exam_form' action='update_past_exam.html' method='POST'>");
            htm.addln(" <input type='hidden' name='xml' value='", xml, "'>");
            htm.addln(" <input type='hidden' name='upd' value='", upd, "'>");
            htm.addln(" <input type='hidden' name='course' value='", course, "'>");
            htm.addln(" <input type='hidden' name='exam' value='", exam, "'>");
            htm.addln(" <input type='hidden' id='past_exam_act' name='action'>");
            pes.generateHtml(session.getNow(), xml, upd, htm);
            htm.addln("</form>");

            htm.eDiv(); // (end "panel" div)
            htm.eDiv(); // (end "menupanel" div)

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
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
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updatePastExam(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                               final HttpServletResponse resp, final ImmutableSessionInfo session,
                               final ELMTutorialStatus status) throws IOException, SQLException {

        final String xml = req.getParameter("xml");
        final String upd = req.getParameter("upd");
        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");

        if (AbstractSite.isFileParamInvalid(xml) || AbstractSite.isFileParamInvalid(upd)
                || AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  xml='", xml, "'");
            Log.warning("  upd='", upd, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  exam='", exam, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String redirect = null;
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            final PastExamSessionStore store = PastExamSessionStore.getInstance();
            final PastExamSession pes = store.getPastExamSession(session.loginSessionId, xml);

            if (pes == null) {
                htm.sP().add("Exam not found.").eP();
            } else {
                htm.addln("<form id='past_exam_form' action='update_past_exam.html' method='POST'>");
                htm.addln(" <input type='hidden' name='xml' value='", xml, "'>");
                htm.addln(" <input type='hidden' name='upd' value='", upd, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' name='exam' value='", exam, "'>");
                htm.addln(" <input type='hidden' id='past_exam_act' name='action'>");
                redirect = pes.processPost(session, req, htm);
                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.eDiv(); // (end "panel" div)
                htm.eDiv(); // (end "menupanel" div)

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
