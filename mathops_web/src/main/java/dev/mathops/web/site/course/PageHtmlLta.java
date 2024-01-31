package dev.mathops.web.site.course;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.hw.HomeworkSession;
import dev.mathops.web.site.html.hw.HomeworkSessionStore;
import dev.mathops.web.site.html.reviewexam.ReviewExamSession;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZonedDateTime;

/**
 * Presents a learning target assignment.
 */
enum PageHtmlLta {
    ;

    /**
     * Starts a learning target assignment.
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
    static void startLta(final Cache cache, final CourseSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session,
                         final CourseSiteLogic logic) throws IOException, SQLException {

        final String assignmentId = req.getParameter("assign");
        final String coursemode = req.getParameter("coursemode");
        final String mode = req.getParameter("mode");
        final String course = req.getParameter("course");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assignmentId) || AbstractSite.isParamInvalid(coursemode)
                || AbstractSite.isParamInvalid(mode) || AbstractSite.isParamInvalid(course)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assignmentId='", assignmentId, "'");
            Log.warning("  coursemode='", coursemode, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ReviewExamSessionStore store = ReviewExamSessionStore.getInstance();
            ReviewExamSession res = store.getReviewExamSession(session.loginSessionId, assignmentId);

            if (res == null) {
                final String redirect= "course_text_module.html?course=" + course + "&module=" + unit + "&mode=" + coursemode;

                res = new ReviewExamSession(cache, site.siteProfile, session.loginSessionId,
                        session.getEffectiveUserId(), assignmentId, "practice".equals(mode),
                        redirect);
                store.setReviewExamSession(res);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();
            Page.startOrdinaryPage(htm, siteTitle, session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false,
                    true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

            htm.sDiv("nav");
            htm.sDiv("aslines");

            htm.add(" <a class='linkbtn' ");
            htm.add("href='course_text_module.html?course=", course, "&module=", unit, "&mode=", coursemode, "'><em>");
            htm.add("Return to Module ", unit);
            htm.addln("</em></a>");

            htm.eDiv();
            htm.eDiv();

            htm.div("clear");
            htm.sDiv("gap").add("&nbsp;").eDiv();

            htm.addln("<form action='update_lta.html' method='POST'>");
            htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
            htm.addln(" <input type='hidden' name='coursemode' value='", coursemode, "'>");
            htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
            htm.addln(" <input type='hidden' name='course' value='", course, "'>");
            htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
            htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");

            // Prevent POST from other thread from accessing session at the same time
            synchronized (res) {
                final ZonedDateTime now = session.getNow();
                res.generateHtml(cache, now, htm);
            }

            htm.addln("</form>");

            htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            Page.endOrdinaryPage(cache, site, htm, true);

            final String htmString = htm.toString();
            final byte[] bytes = htmString.getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
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
    static void updateLta(final Cache cache, final CourseSite site, final ServletRequest req,
                          final HttpServletResponse resp, final ImmutableSessionInfo session,
                          final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String assignmentId = req.getParameter("assign");
        final String coursemode = req.getParameter("coursemode");
        final String mode = req.getParameter("mode");
        final String course = req.getParameter("course");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assignmentId) || AbstractSite.isParamInvalid(coursemode)
                || AbstractSite.isParamInvalid(mode) || AbstractSite.isParamInvalid(course)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assignmentId='", assignmentId, "'");
            Log.warning("  coursemode='", coursemode, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String redirect = null;

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

            htm.sDiv("nav");
            htm.sDiv("aslines");
            htm.add(" <a class='linkbtn' href='course_text_module.html?course=", course, "&module=", unit, "&mode=",
                    coursemode, "'><em>");
            htm.add("Return to Module ", unit);
            htm.addln("</em></a>");
            htm.eDiv();
            htm.eDiv();

            htm.div("clear");
            htm.sDiv("gap").add("&nbsp;").eDiv();

            final ReviewExamSession res = ReviewExamSessionStore.getInstance()
                    .getReviewExamSession(session.loginSessionId, assignmentId);
            if (res == null) {
                htm.sP().add("Assignment has not been started.").eP();
            } else {
                htm.addln("<form id='lta_form' action='update_lta.html'>");
                htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
                htm.addln(" <input type='hidden' name='coursemode' value='", coursemode, "'>");
                htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
                htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");
                htm.addln("</form>");

                // Prevent POST from other thread from accessing session at the same time
                synchronized (res) {
                    redirect = res.processPost(cache, session, req, htm);
                }
            }

            if (redirect == null) {
                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
