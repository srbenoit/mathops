package dev.mathops.web.host.placement.tutorial.precalc;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.reviewexam.ReviewExamSession;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void startReviewExam(final Cache cache, final PrecalcTutorialSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final String examId = req.getParameter("exam");
        final String mode = req.getParameter("mode");
        final String course = req.getParameter("course");
        final String unit = req.getParameter("unit");

        if (AbstractSite.isParamInvalid(examId) || AbstractSite.isParamInvalid(mode)
                || AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(unit)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  examId='", examId, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  unit='", unit, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ReviewExamSessionStore store = ReviewExamSessionStore.getInstance();

            ReviewExamSession hs = store.getReviewExamSession(session.loginSessionId, examId);
            if (hs == null) {
                final String redirect = "course.html?course=" + course;

                hs = new ReviewExamSession(cache, site.site, session.loginSessionId,
                        session.getEffectiveUserId(), examId, "practice".equals(mode), redirect);
                store.setReviewExamSession(hs);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                    "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("panel");

            htm.addln("<form id='review_exam_form' action='update_review_exam.html' method='POST' onkeydown='if(event.keyCode === 13) {return false;}'>");
            htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
            htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
            htm.addln(" <input type='hidden' name='course' value='", course, "'>");
            htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
            htm.addln(" <input type='hidden' id='review_exam_act' name='action'>");
            hs.generateHtml(cache, session.getNow(), htm);
            htm.addln("</form>");

            htm.eDiv(); // (end "menupanel" div)

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateReviewExam(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String examId = req.getParameter("exam");
        final String mode = req.getParameter("mode");
        final String course = req.getParameter("course");
        final String unit = req.getParameter("unit");

        if (AbstractSite.isParamInvalid(examId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  examId='", examId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String redirect = null;

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                    "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

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
                htm.addln("<form id='review_exam_form' action='update_review_exam.html' method='POST' onkeydown='if(event.keyCode === 13) {return false;}'>");
                htm.addln(" <input type='hidden' name='exam' value='", examId, "'>");
                htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
                htm.addln(" <input type='hidden' id='review_exam_act' name='action'>");
                redirect = res.processPost(cache, session, req, htm);
                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.eDiv(); // (end "menupanel" div)

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
