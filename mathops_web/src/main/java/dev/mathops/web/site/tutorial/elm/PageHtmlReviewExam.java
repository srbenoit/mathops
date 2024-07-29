package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.reviewexam.ReviewExamSession;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Presents a review exam.
 */
enum PageHtmlReviewExam {
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
    static void startReviewExam(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                                final HttpServletResponse resp, final ImmutableSessionInfo session,
                                final ELMTutorialStatus status) throws IOException, SQLException {

        final String examId = req.getParameter("exam");

        if (AbstractSite.isParamInvalid(examId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  examId='", examId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ReviewExamSessionStore store = ReviewExamSessionStore.getInstance();

            ReviewExamSession hs = store.getReviewExamSession(session.loginSessionId, examId);

            if (hs == null) {
                hs = new ReviewExamSession(cache, site.siteProfile, session.loginSessionId,
                        session.getEffectiveUserId(), examId, false, "tutorial.html");
                store.setReviewExamSession(hs);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                    "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            htm.addln("<form id='review_exam_form' action='update_review_exam.html' method='POST'>");
            htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
            htm.addln(" <input type='hidden' id='review_exam_act' name='action'>");
            hs.generateHtml(cache, session.getNow(), htm);
            htm.addln("</form>");

            htm.eDiv(); // panel
            htm.eDiv(); // menupanel

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
    static void updateReviewExam(final Cache cache, final ElmTutorialSite site,
                                 final ServletRequest req, final HttpServletResponse resp,
                                 final ImmutableSessionInfo session, final ELMTutorialStatus status)
            throws IOException, SQLException {

        final String examId = req.getParameter("exam");

        if (AbstractSite.isParamInvalid(examId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  examId='", examId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String redirect = null;

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                    "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            final ReviewExamSession res = ReviewExamSessionStore.getInstance()
                    .getReviewExamSession(session.loginSessionId, examId);
            if (res == null) {
                htm.sDiv("indent33");
                htm.sP().add("Exam not found.").eP();
                htm.addln("<form action='home.html' method='GET'>");
                htm.addln(" <input type='submit' value='Close'>");
                htm.addln("</form>");
                htm.eDiv();
            } else {
                htm.addln("<form id='review_exam_form' action='update_review_exam.html' method='POST'>");
                htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
                htm.addln(" <input type='hidden' id='review_exam_act' name='action'>");
                redirect = res.processPost(cache, session, req, htm);
                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.eDiv(); // panel
                htm.eDiv(); // menupanel

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
