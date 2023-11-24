package dev.mathops.web.site.lti;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.challengeexam.ChallengeExamSession;
import dev.mathops.web.site.html.challengeexam.ChallengeExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the home page.
 */
enum PageChallenge {
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
        final String stu = req.getParameter("stu");
        final String pwd = req.getParameter("pwd");

        if (exam == null || exam.length() != 5) {
            Log.warning("Missing or invalid exam ID");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (AbstractSite.isParamInvalid(exam) || AbstractSite.isParamInvalid(stu)
                || AbstractSite.isParamInvalid(pwd)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  exam='", exam, "'");
            Log.warning("  stu='", stu, "'");
            Log.warning("  pwd='", pwd, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startPage(htm, site.getTitle(), false, false);
            htm.addln("<body style='background:white'>");

            final boolean valid = ChallengeExamSessionStore.getInstance()
                    .validateAndDeleteOneTimeChallengeCode(stu, pwd);

            if (valid) {
                final ChallengeExamSessionStore store = ChallengeExamSessionStore.getInstance();
                ChallengeExamSession cs = store.getChallengeExamSessionForStudent(stu);

                if (cs == null) {
                    final String redirect = "course.html";

                    Log.info("Starting challenge exam for user ", stu, " exam ", exam);

                    cs = new ChallengeExamSession(cache, site.siteProfile, pwd, stu, exam, redirect);
                    store.setChallengeExamSession(cs);
                } else {
                    Log.info("Found existing unit exam for student ", stu, " exam ", exam);
                }

                htm.addln("<form id='challenge_exam_form' action='update_challenge_exam.html' method='POST'>");
                htm.addln(" <input type='hidden' name='stu' value='", stu, "'>");
                htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");

                // Ensure we don't try to generate HTML while a POST from another thread is
                // accessing the session
                synchronized (cs) {
                    cs.generateHtml(cache, req, htm);
                }

                htm.addln("</form>");

            } else {
                htm.sP("red").add("Password is not valid").eP();
            }

            htm.addln("</body>");
            htm.addln("</html>");

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
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
    static void updateChallengeExam(final Cache cache, final ServletRequest req,
                                    final HttpServletResponse resp) throws IOException, SQLException {

        final String stu = req.getParameter("stu");

        if (stu == null) {
            Log.warning("Missing required 'stu' parameter");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (AbstractSite.isParamInvalid(stu)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  stu='", stu, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ChallengeExamSessionStore store = ChallengeExamSessionStore.getInstance();
            final ChallengeExamSession cs = store.getChallengeExamSessionForStudent(stu);

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startPage(htm, "Course Challenge Exam", false, false);
            htm.addln("<body style='background:white'>");

            String redirect = null;
            if (cs == null) {
                htm.sP("red").add("There is no challenge exam in progress.").eP();
            } else {
                htm.addln("<form id='challenge_exam_form' action='update_challenge_exam.html' method='POST'>");
                htm.addln(" <input type='hidden' name='stu' value='", stu, "'>");
                htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");

                // Try to ensure multiple POSTS don't try to do POST processing at the same time
                synchronized (cs) {
                    redirect = cs.processPost(cache, req, htm);
                }

                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.addln("</body>");
                htm.addln("</html>");

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
