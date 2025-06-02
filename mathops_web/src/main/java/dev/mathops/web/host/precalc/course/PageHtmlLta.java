package dev.mathops.web.host.precalc.course;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.lta.LtaSession;
import dev.mathops.web.site.html.lta.LtaSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;

/**
 * Presents a learning target assignment.
 */
enum PageHtmlLta {
    ;

    /**
     * Starts a new learning target assignment and presents its HTML representation.
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
            final LtaSessionStore store = LtaSessionStore.getInstance();
            LtaSession lta = store.getLtaSession(session.loginSessionId, assignmentId);

            if (lta == null) {
                final String redirect = "course_text_module.html?course=" + course + "&module=" + unit + "&mode="
                        + coursemode;

                final String effectiveUserId = session.getEffectiveUserId();
                lta = new LtaSession(cache, site.site, session.loginSessionId, effectiveUserId, assignmentId,
                        redirect);
                store.setLtaSession(lta);
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

            htm.addln("<form id='lta_form' action='update_lta.html' method='POST'>");
            htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
            htm.addln(" <input type='hidden' name='coursemode' value='", coursemode, "'>");
            htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
            htm.addln(" <input type='hidden' name='course' value='", course, "'>");
            htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
            htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");
            htm.addln(" <input type='hidden' id='lta_act' name='action'>");

            // Prevent POST from other thread from accessing session at the same time
            synchronized (lta) {
                final ZonedDateTime now = session.getNow();
                lta.generateHtml(cache, now, htm);
            }

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
    static void updateLta(final Cache cache, final CourseSite site, final ServletRequest req,
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
            String redirect = null;

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                    false, true);

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

            final LtaSessionStore store = LtaSessionStore.getInstance();
            final LtaSession lta = store.getLtaSession(session.loginSessionId, assignmentId);
            if (lta == null) {
                htm.sP().add("Assignment has not been started.").eP();
            } else {
                htm.addln("<form id='lta_form' action='update_lta.html' method='POST'>");
                htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
                htm.addln(" <input type='hidden' name='coursemode' value='", coursemode, "'>");
                htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
                htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");
                htm.addln(" <input type='hidden' id='lta_act' name='action'>");

                // Prevent POST from other thread from accessing session at the same time
                synchronized (lta) {
                    redirect = lta.processPost(cache, session, req, htm);
                }

                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
