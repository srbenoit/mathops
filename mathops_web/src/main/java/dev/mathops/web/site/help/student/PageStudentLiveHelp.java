package dev.mathops.web.site.help.student;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.help.HelpSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the live help page.
 */
public enum PageStudentLiveHelp {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final HelpSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String type = req.getParameter("type");

        if ("HW".equals(type)) {
            final String assign = req.getParameter("assign");
            if (assign == null) {
                Log.warning(Res.get(Res.MISSING_HW_ASSIGN));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                showHWPage(cache, site, req, resp, session, assign);
            }
        } else if ("EX".equals(type)) {

            final String xml = req.getParameter("xml");
            if (xml == null) {
                Log.warning(Res.get(Res.MISSING_EX_XML));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                showEXPage(cache, site, req, resp, session, xml);
            }

        } else if ("LE".equals(type)) {
            final String course = req.getParameter("course");
            final String unit = req.getParameter("unit");
            final String lesson = req.getParameter("lesson");

            if (course == null) {
                Log.warning(Res.get(Res.MISSING_LE_COURSE));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else if (unit == null) {
                Log.warning(Res.get(Res.MISSING_LE_UNIT));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else if (lesson == null) {
                Log.warning(Res.get(Res.MISSING_LE_LESSON));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                showLEPage(cache, site, req, resp, session, course, unit, lesson);
            }
//        } else {
            // Unqualified request - show help navigation
            // PageStudentHome.doGet(site, req, resp, session);
        }
    }

    /**
     * Generates the page content when the context is "HW", meaning the student is accessing the page after getting a
     * homework question incorrect.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @param assign  the assignment ID, used to look up the homework session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void showHWPage(final Cache cache, final HelpSite site,
                                   final ServletRequest req, final HttpServletResponse resp,
                                   final ImmutableSessionInfo session, final String assign) throws IOException,
            SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);

        htm.sP().add(assign).eP();

        // TODO: Query the database for types of help available...

        // (1) Video lectures
        // (2) Answers to common questions
        // (3) Live help (subject to hours of operation) - with est. wait time.
        // . . . With updating est wait time
        // . . . With button to cancel request
        // . . . With WebSocket polling to cancel request if student closes window/browser
        // (4) Submit question via email (main option if live help not available)
        // (5) Links to outside resources on the same topic?

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the page content when the context is "EX", meaning the student is accessing the page while reviewing a
     * past exam.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @param xml     the XML path of the exam, used to look up the exam session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void showEXPage(final Cache cache, final HelpSite site,
                                   final ServletRequest req, final HttpServletResponse resp,
                                   final ImmutableSessionInfo session, final String xml) throws IOException,
            SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);

        htm.sP().add(xml).eP();

        // TODO: Query the database for types of help available...

        // (1) Video lectures
        // (2) Answers to common questions
        // (3) Live help (subject to hours of operation) - with est. wait time.
        // . . . With updating est wait time
        // . . . With button to cancel request
        // . . . With WebSocket polling to cancel request if student closes window/browser
        // (4) Submit question via email
        // (5) Links to outside resources on the same topic?

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the page content when the context is "LE", meaning the student is accessing the lesson or lecture.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @param course  the course ID
     * @param unit    the unit number
     * @param lesson  the lesson ID
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void showLEPage(final Cache cache, final HelpSite site,
                                   final ServletRequest req, final HttpServletResponse resp,
                                   final ImmutableSessionInfo session, final String course, final String unit,
                                   final String lesson) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);

        htm.sP().add(course).eP();
        htm.sP().add(unit).eP();
        htm.sP().add(lesson).eP();

        // TODO: Query the database for types of help available...

        // (1) Video lectures
        // (2) Answers to common questions
        // (3) Live help (subject to hours of operation) - with est. wait time.
        // . . . With updating est wait time
        // . . . With button to cancel request
        // . . . With WebSocket polling to cancel request if student closes window/browser
        // (4) Submit question via email
        // (5) Links to outside resources on the same topic?

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
