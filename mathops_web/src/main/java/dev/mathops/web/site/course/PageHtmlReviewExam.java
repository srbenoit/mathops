package dev.mathops.web.site.course;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.reviewexam.ReviewExamSession;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
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
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void startReviewExam(final Cache cache, final CourseSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session, final CourseSiteLogic logic)
            throws IOException, SQLException {

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
                final String redirect;

                if ("UOOOO".equals(examId) || "UOPOP".equals(examId)) {
                    redirect = "users_exam.html";
                } else if (RawRecordConstants.MATH117.equals(course)
                        || RawRecordConstants.MATH118.equals(course)
                        || RawRecordConstants.MATH124.equals(course)
                        || RawRecordConstants.MATH125.equals(course)
                        || RawRecordConstants.MATH126.equals(course)) {
                    redirect = "course_text_module.html?course=" + course
                            + "&module=" + unit
                            + "&mode=" + mode;
                } else {
                    redirect = "course.html?course=" + course
                            + "&mode=" + mode;
                }

                hs = new ReviewExamSession(cache, site.siteProfile, session.loginSessionId,
                        session.getEffectiveUserId(), examId, "practice".equals(mode),
                        redirect);
                store.setReviewExamSession(hs);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

            htm.addln("<form id='review_exam_form' action='update_review_exam.html'>");
            htm.addln(" <input type='hidden' name='exam' value='", examId,
                    "'>");
            htm.addln(" <input type='hidden' name='mode' value='", mode,
                    "'>");
            htm.addln(" <input type='hidden' name='course' value='", course,
                    "'>");
            htm.addln(" <input type='hidden' name='unit' value='", unit,
                    "'>");
            htm.addln(" <input type='hidden' id='review_exam_act' name='action'>");
            hs.generateHtml(cache, session.getNow(), htm);
            htm.addln("</form>");

            htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
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
    static void updateReviewExam(final Cache cache, final CourseSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final ImmutableSessionInfo session,
                                 final CourseSiteLogic logic) throws IOException, SQLException {

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
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

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
                htm.addln("<form id='review_exam_form' action='update_review_exam.html'>");
                htm.addln(" <input type='hidden' name='exam' value='", examId,
                        "'>");
                htm.addln(" <input type='hidden' name='mode' value='", mode,
                        "'>");
                htm.addln(" <input type='hidden' name='course' value='", course,
                        "'>");
                htm.addln(" <input type='hidden' name='unit' value='", unit,
                        "'>");
                htm.addln(" <input type='hidden' id='review_exam_act' name='action'>");
                redirect = res.processPost(cache, session, req, htm);
                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML,
                        htm.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
