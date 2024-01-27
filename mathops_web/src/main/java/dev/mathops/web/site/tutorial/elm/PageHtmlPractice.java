package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.hw.HomeworkSession;
import dev.mathops.web.site.html.hw.HomeworkSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Presents practice problems.
 */
enum PageHtmlPractice {
    ;

    /**
     * Starts the page that shows a lesson with student progress.
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
    static void startHomework(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session,
                              final ELMTutorialStatus status) throws IOException, SQLException {

        final String assignmentId = req.getParameter("assign");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assignmentId) || AbstractSite.isParamInvalid(unit)
                || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assignmentId='", assignmentId, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HomeworkSessionStore store = HomeworkSessionStore.getInstance();
            HomeworkSession hs = store.getHomeworkSession(session.loginSessionId, assignmentId);

            if (hs == null) {
                hs = new HomeworkSession(cache, site.siteProfile, session.loginSessionId,
                        session.getEffectiveUserId(), assignmentId, true, "tutorial.html");
                store.setHomeworkSession(hs);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                    "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            htm.sDiv("nav");
            htm.sDiv("aslines");
            htm.add(" <a class='linkbtn' href='lesson.html?unit=", unit, "&lesson=", lesson, "'><em>");
            htm.add("Return to the Objective");
            htm.addln("</em></a>");
            htm.eDiv();
            htm.eDiv();

            htm.div("clear");
            htm.sDiv("gap").add("&nbsp;").eDiv();

            htm.addln("<form action='update_homework.html' method='POST'>");
            htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
            htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
            htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");

            // Prevent POST from other thread from accessing session at the same time
            synchronized (hs) {
                hs.generateHtml(cache, session.getNow(), htm);
            }

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
    static void updateHomework(final Cache cache, final ElmTutorialSite site,
                               final ServletRequest req, final HttpServletResponse resp,
                               final ImmutableSessionInfo session, final ELMTutorialStatus status)
            throws IOException, SQLException {

        final String assignmentId = req.getParameter("assign");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assignmentId) || AbstractSite.isParamInvalid(unit)
                || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assignmentId='", assignmentId, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(cache, session, status, htm);
            htm.sDiv("panel");

            htm.sDiv("nav");
            htm.sDiv("aslines");
            htm.add(" <a class='linkbtn' href='lesson.html?unit=", unit,
                    "&lesson=", lesson, "'><em>");
            htm.add("Return to the Objective");
            htm.addln("</em></a>");
            htm.eDiv();
            htm.eDiv();

            htm.div("clear");
            htm.sDiv("gap").add("&nbsp;").eDiv();

            String redirect = null;

            final HomeworkSessionStore store = HomeworkSessionStore.getInstance();
            final HomeworkSession hs = store.getHomeworkSession(session.loginSessionId, assignmentId);
            if (hs == null) {
                htm.sP().add("Assignment has not been started.").eP();
            } else {
                htm.addln("<form action='update_homework.html' method='POST'>");
                htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
                htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
                htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");

                // Prevent POST from other thread from accessing session at the same time
                synchronized (hs) {
                    redirect = hs.processPost(cache, session, req, htm);
                }

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
