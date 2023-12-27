package dev.mathops.web.site.placement.main;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
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
 * Presents a homework assignment.
 */
enum PageReviewHomework {
    ;

    /**
     * Starts the page that shows a lesson with student progress.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String assign = req.getParameter("assign");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assign) || AbstractSite.isParamInvalid(assign)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assign='", assign, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (assign == null) {
            Log.info("Review homework: unit=" + unit + " lesson=" + lesson + " assign=null");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HomeworkSessionStore store = HomeworkSessionStore.getInstance();
            HomeworkSession hs = store.getHomeworkSession(session.loginSessionId, assign);

            if (hs == null) {
                final String redirect = "review-lesson.html?courseM%20100R&unit=" + unit + "&lesson=" + lesson;

                hs = new HomeworkSession(cache, site.siteProfile, session.loginSessionId,
                        session.getEffectiveUserId(), assign, true, false, redirect);
                store.setHomeworkSession(hs);
            }

            final HtmlBuilder htm = MPPage.startReviewPage2(site, session);

            htm.sDiv("inset2");
            htm.sDiv("shaded2left");

            htm.addln("<form action='review_homework.html' method='POST'>");
            htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
            htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");
            htm.addln(" <input type='hidden' name='assign' value='", assign, "'>");

            // Prevent POST from other thread from accessing session at the same time
            synchronized (hs) {
                hs.generateHtml(cache, session.getNow(), htm);
            }
            htm.addln("</form>");

            htm.div("vgap");
            htm.sDiv("center");
            htm.add("<a class='btn' href='review_outline.html?course=M 100R'>Return to outline</a></td>");
            htm.eDiv();

            htm.eDiv(); // shaded2left
            htm.eDiv(); // inset2

            MPPage.emitScripts(htm);
            MPPage.endPage(htm, req, resp);
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
    static void doPost(final Cache cache, final MathPlacementSite site,
                       final ServletRequest req, final HttpServletResponse resp,
                       final ImmutableSessionInfo session) throws IOException, SQLException {

        final String assign = req.getParameter("assign");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assign) || AbstractSite.isParamInvalid(assign)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assign='", assign, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String redirect = null;

            final HtmlBuilder htm = new HtmlBuilder(8192);
            Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);

            // Show "Welcome to Math Placement", and "Logged in as "NAME".
            htm.sDiv("center");
            htm.sH(1, "shaded").add("Welcome to Math Placement Review").eH(1);
            htm.eDiv(); // center

            htm.div("vgap");

            MathPlacementSite.emitLoggedInAs1(htm, session);

            htm.sDiv("inset2");
            htm.sDiv("shaded2left");

            final HomeworkSessionStore store = HomeworkSessionStore.getInstance();
            final HomeworkSession hs = store.getHomeworkSession(session.loginSessionId, assign);
            if (hs == null) {
                htm.sP().add("Assignment has not been started.").eP();
            } else {
                htm.addln("<form action='review_homework.html' method='POST'>");
                htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
                htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");
                htm.addln(" <input type='hidden' name='assign' value='", assign, "'>");

                // Prevent POST from other thread from accessing session at the same time
                synchronized (hs) {
                    redirect = hs.processPost(cache, session, req, htm);
                }

                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.div("vgap");
                htm.sDiv("center");
                htm.add("<a class='btn' href='review_outline.html?course=M 100R'>Return to outline</a></td>");
                htm.eDiv();

                htm.eDiv(); // shaded2left
                htm.eDiv(); // inset2

                MPPage.emitScripts(htm);

                Page.endNoFooterPage(htm, false);
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
