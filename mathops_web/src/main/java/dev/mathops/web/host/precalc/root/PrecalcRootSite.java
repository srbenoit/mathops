package dev.mathops.web.host.precalc.root;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.session.login.ILoginProcessor;
import dev.mathops.session.login.TestStudentLoginProcessor;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A root site for precalculus courses.
 */
public final class PrecalcRootSite extends AbstractPageSite {

    /** ZERo-length array used in the construction of other arrays. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * Constructs a new {@code PrecalcRootSite}.
     *
     * @param theSite     the site
     * @param theSessions the singleton user session repository
     */
    public PrecalcRootSite(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        Log.info("GET ", subpath);

        if (CoreConstants.EMPTY.equals(subpath)) {
            resp.sendRedirect("index.html");
        } else if ("basestyle.css".equals(subpath)) {
            final byte[] baseStyleBytes = FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true);
            sendReply(req, resp, "text/css", baseStyleBytes);
        } else if ("style.css".equals(subpath)) {
            final byte[] styleBytes = FileLoader.loadFileAsBytes(PrecalcRootSite.class, "style.css", true);
            sendReply(req, resp, "text/css", styleBytes);
        } else if (subpath.startsWith("images/")) {
            final String substring = subpath.substring(7);
            serveImage(substring, req, resp);
        } else if (subpath.startsWith("media/") || subpath.startsWith("math/")) {
            serveMedia(cache, subpath, req, resp);
        } else if (subpath.endsWith(".vtt")) {
            serveVtt(subpath, req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else if ("robots.txt".equals(subpath)) {
            doRobotsTxt(req, resp);
        } else if ("courses.html".equals(subpath)) {
            PageCourses.doGet(cache, this, type, req, resp);
        } else if ("contact.html".equals(subpath)) {
            PageContact.doGet(cache, this, type, req, resp);
//        } else if ("orientation.html".equals(subpath)) {
//            PageOrientation.doGet(cache, this, type, req, resp);
        } else {
            final String maintenanceMsg = isMaintenance(this.site);

            if (maintenanceMsg == null) {
                switch (subpath) {
                    case "index.html", "login.html" -> PageIndex.doGet(cache, this, type, req, resp);
                    case "login_test_user_999.html" -> doLoginTestUserPage(cache, type, req, resp);
                    case "login_test_user_by_id.html" -> doLoginTestUserById(cache, type, req, resp);
                    default -> {
                        final ImmutableSessionInfo session = validateSession(req, resp, null);
                        if (session == null) {
                            if ("secure/shibboleth.html".equals(subpath)) {
                                doShibbolethLogin(cache, req, resp, null, "home.html");
                            } else {
                                Log.warning("Unrecognized request path: ", subpath);
                                PageIndex.doGet(cache, this, type, req, resp);
                            }
                        } else {
                            final String effectiveId = session.getEffectiveUserId();
                            LogBase.setSessionInfo(session.loginSessionId, effectiveId);

                            if ("home.html".equals(subpath)) {
                                final String path = this.site.path;
                                final boolean endsWithSlash = path.endsWith(Contexts.ROOT_PATH);
                                resp.sendRedirect(path + (endsWithSlash ? "instruction/home.html"
                                        : "/instruction/home.html"));
                            } else {
                                Log.warning("Unrecognized request path: ", subpath);
                                PageIndex.doGet(cache, this, type, req, resp);
                            }
                        }
                    }
                }
            } else {
                PageMaintenance.doGet(cache, this, type, req, resp, maintenanceMsg);
            }
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final String maintenanceMsg = isMaintenance(this.site);

        if (maintenanceMsg == null) {
            Log.warning("Unrecognized request path: ", subpath);
            final String reqUri = req.getRequestURI();
            PageError.doGet(cache, this, req, resp, null, "<p>POST " + reqUri + " (" + subpath + ")</p>");
        } else {
            PageMaintenance.doGet(cache, this, type, req, resp, maintenanceMsg);
        }
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }

    /**
     * Generates a response to a "/robots.txt" request, telling bots not to scan the site.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException of there is an error writing the response
     */
    private static void doRobotsTxt(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder data = new HtmlBuilder(30);
        data.addln("User-agent: *");
        data.addln("Disallow: /");

        sendReply(req, resp, MIME_TEXT_PLAIN, data);
    }

    /**
     * Generates a page that supports logging in as one of the test users whose ID begins with '99'.
     *
     * @param cache    the data cache
     * @param siteType the site type
     * @param req      the request
     * @param resp     the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doLoginTestUserPage(final Cache cache, final ESiteType siteType,
                                     final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = getTitle();
        Page.startOrdinaryPage(htm, title, null, true, Page.NO_BARS, null, false, true);
        htm.sDiv("menupanel");
        PrecalcRootMenu.buildMenu(this, siteType, htm);
        htm.sDiv("panel");

        htm.sH(2).add("Test Users").eH(2);

        htm.addln("<ul>");
        // TODO: Generate an appropriate list for students who can test aspects of the site
        htm.addln("<li><a href='login_test_user_by_id.html?tsid=999999999'>999999999</a> (John Doe)</li>");
        htm.addln("</ul>");

        htm.eDiv(); // panel
        htm.eDiv(); // menupanel

        Page.endOrdinaryPage(cache, this, htm, true);

        sendReply(req, resp, MIME_TEXT_HTML, htm);
    }

    /**
     * Generates a page that supports logging in as one of the test users whose ID begins with '999'.
     *
     * @param cache    the data cache
     * @param siteType the site type
     * @param req      the request
     * @param resp     the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doLoginTestUserById(final Cache cache, final ESiteType siteType, final ServletRequest req,
                                     final HttpServletResponse resp) throws IOException, SQLException {

        final String studentId = req.getParameter("tsid");

        Log.info("Logging in test student " + studentId);

        final SessionManager mgr = SessionManager.getInstance();

        final Map<String, String> fields = new HashMap<>(1);
        fields.put(TestStudentLoginProcessor.STU_ID, studentId);
        final Profile dbProfile = getSite().profile;

        final ELiveRefreshes liveRefreshes = getLiveRefreshes();
        final ILoginProcessor authMethod = mgr.identifyProcessor(TestStudentLoginProcessor.TYPE);
        final SessionResult result = mgr.login(new Cache(dbProfile), authMethod, fields, liveRefreshes);

        final ImmutableSessionInfo sess = result.session;

        if (sess == null) {
            doLoginTestUserPage(cache, siteType, req, resp);
        } else {
            final String path = this.site.path;
            final String successPath = "instruction/home.html";

            final String redirect = path + (path.endsWith(CoreConstants.SLASH) ? successPath
                    : CoreConstants.SLASH + successPath);

            // Install the session ID cookie in the response
            final String serverName = req.getServerName();
            Log.info("Adding session ID cookie ", serverName);
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(serverName);
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);

            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            resp.setHeader("Location", redirect);
            sendReply(req, resp, Page.MIME_TEXT_HTML, ZERO_LEN_BYTE_ARR);
        }
    }

    /**
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.ALL;
    }
}
