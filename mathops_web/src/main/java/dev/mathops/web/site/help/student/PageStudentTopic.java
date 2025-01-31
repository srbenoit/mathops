package dev.mathops.web.site.help.student;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
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
public enum PageStudentTopic {
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

        final String id = req.getParameter("id");

        if ("117".equals(id)) {
            emit117(cache, site, req, resp, session);
        } else if ("118".equals(id)) {
            emit118(cache, site, req, resp, session);
        } else if ("124".equals(id)) {
            emit124(cache, site, req, resp, session);
        } else if ("125".equals(id)) {
            emit125(cache, site, req, resp, session);
        } else if ("126".equals(id)) {
            emit126(cache, site, req, resp, session);
        } else if ("ELM".equals(id)) {
            emitELM(cache, site, req, resp, session);
        } else if ("PCT".equals(id)) {
            emitPCT(cache, site, req, resp, session);
        } else if ("MPR".equals(id)) {
            emitMPR(cache, site, req, resp, session);
            // } else {
            // PageStudentHome.doGet(site, req, resp, session);
        }
    }

    /**
     * Emits the selection of topics in MATH 117.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emit117(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("MATH 117").eH(2);

        htm.addln("<ul>");
        htm.addln("<li><a href='lobby.html?c=117&u=0'>", //
                "Skills Review Materials</a></li>");

        htm.addln("<li>Unit 1: Linear Functions");
        htm.addln("  <ul>");
        htm.addln("  <li><a href='lobby.html?c=117&u=1&o=1'>", //
                "1.1: Generalize linear equations from tables and graphs</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=1&o=2'>",
                "1.2: Use function notation</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=1&o=3'>",
                "1.3: Determine the equation of a line given conditions</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=1&o=4'>",
                "1.4: Solve equations and inequalities</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=1&o=5'>",
                "1.5: Model with linear functions</a></li>");
        htm.addln("  </ul>");

        htm.addln("<li>Unit 2: Absolute Value and Piecewise-Defined Functions");
        htm.addln("  <ul>");
        htm.addln("  <li><a href='lobby.html?c=117&u=2&o=1'>",
                "2.1: Graph absolute functions</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=2&o=2'>",
                "2.2: Graph piecewise defined functions</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=2&o=3'>",
                "2.3: Solve absolute value equations</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=2&o=4'>",
                "2.4: Solve absolute value inequalities</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=2&o=5'>",
                "2.5: Model with absolute value and piecewise defined functions</a></li>");
        htm.addln("  </ul>");

        htm.addln("<li>Unit 3: Quadratic Relations and Functions");
        htm.addln("  <ul>");
        htm.addln("  <li><a href='lobby.html?c=117&u=3&o=1'>",
                "3.1: Graph quadratic functions</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=3&o=2'>",
                "3.2: Solve quadratic equations</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=3&o=3'>",
                "3.3: Solve quadratic inequalities</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=3&o=4'>",
                "3.4: Graph circles and ellipses</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=3&o=5'>",
                "3.5: Model with quadratic functions and relations</a></li>");
        htm.addln("  </ul>");

        htm.addln("<li>Unit 3: Systems of Equations and Inequalities");
        htm.addln("  <ul>");
        htm.addln("  <li><a href='lobby.html?c=117&u=4&o=1'>",
                "4.1: Solve systems of linear equations</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=4&o=2'>",
                "4.2: Solve systems using matrices</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=4&o=3'>",
                "4.3: Solve systems of linear inequalities</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=4&o=4'>",
                "4.4: Solve systems with other functions and relations</a></li>");
        htm.addln("  <li><a href='lobby.html?c=117&u=4&o=5'>",
                "4.5: Model with systems</a></li>");
        htm.addln("  </ul>");
        htm.addln("</ul>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the selection of topics in MATH 118.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emit118(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("MATH 118").eH(2);

        // TODO: Topics

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the selection of topics in MATH 124.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emit124(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("MATH 124").eH(2);

        // TODO: Topics

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the selection of topics in MATH 125
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emit125(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("MATH 125").eH(2);

        // TODO: Topics

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the selection of topics in MATH 126.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emit126(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("MATH 126").eH(2);

        // TODO: Topics

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the selection of topics in the ELM Tutorial.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitELM(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("Entry Level Math (ELM) Tutorial").eH(2);

        // TODO: Topics

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the selection of topics in the Precalculus Tutorials.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitPCT(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("Precalculus Tutorials").eH(2);

        // TODO: Topics

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the selection of topics in Math Placement.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitMPR(final Cache cache, final HelpSite site,
                                final ServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("Math Placement Review").eH(2);

        // TODO: Topics

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
