package dev.mathops.web.site.root;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.session.login.TestStudentLoginProcessor;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     * @param theSiteProfile the website profile
     * @param theSessions    the singleton user session repository
     */
    public PrecalcRootSite(final WebSiteProfile theSiteProfile,
                           final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
    }

    /**
     * Initializes the site - called when the servlet is initialized.
     *
     * @param config the servlet context in which the servlet is being in
     */
    @Override
    public void init(final ServletConfig config) {

        // No action
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
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if (CoreConstants.EMPTY.equals(subpath)) {
            resp.sendRedirect("index.html");
        } else if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(PrecalcRootSite.class, "style.css", true));
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if (subpath.startsWith("media/")
                || subpath.startsWith("math/")) {
            serveMedia(subpath, req, resp);
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
        } else if ("orientation.html".equals(subpath)) {
            PageOrientation.doGet(cache, this, type, req, resp);
        } else {
            final String maintMsg = isMaintenance(this.siteProfile);

            if (maintMsg == null) {
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
                            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                            if ("home.html".equals(subpath)) {
                                final String path = this.siteProfile.path;
                                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? "instruction/home.html"
                                        : "/instruction/home.html"));
                            } else {
                                Log.warning("Unrecognized request path: ", subpath);
                                PageIndex.doGet(cache, this, type, req, resp);
                            }
                        }
                    }
                }
            } else {
                PageMaintenance.doGet(cache, this, type, req, resp, maintMsg);
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

        final String maintMsg = isMaintenance(this.siteProfile);

        if (maintMsg == null) {
            Log.warning("Unrecognized request path: ", subpath);
            PageError.doGet(cache, this, req, resp, null, "<p>POST "
                    + req.getRequestURI() + " (" + subpath + ")</p>");
        } else {
            PageMaintenance.doGet(cache, this, type, req, resp, maintMsg);
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
    private static void doRobotsTxt(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder data = new HtmlBuilder(30);
        data.addln("User-agent: *");
        data.addln("Disallow: /");

        sendReply(req, resp, MIME_TEXT_PLAIN, data.toString().getBytes(StandardCharsets.UTF_8));
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
        Page.startOrdinaryPage(htm, getTitle(), null, true, Page.NO_BARS, null, false, true);
        htm.sDiv("menupanel");
        PrecalcRootMenu.buildMenu(this, siteType, htm);
        htm.sDiv("panel");

        htm.sH(2).add("Test Users").eH(2);

        htm.addln("<ul>");
        // TODO: Generate an appropriate list for students who can test aspects of the site
        htm.addln(//
                "<li><a href='login_test_user_by_id.html?tsid=999999999'>999999999</a> ",
                "(John Doe)</li>");
        htm.addln("</ul>");

        htm.eDiv(); // panel
        htm.eDiv(); // menupanel

        Page.endOrdinaryPage(cache, this, htm, true);

        sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                htm.toString().getBytes(StandardCharsets.UTF_8));
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
        final DbProfile dbProfile = getDbProfile();

        final SessionResult result = mgr.login(new Cache(dbProfile, cache.conn),
                mgr.identifyProcessor(TestStudentLoginProcessor.TYPE), fields, doLiveRegQueries());

        final ImmutableSessionInfo sess = result.session;

        if (sess == null) {
            doLoginTestUserPage(cache, siteType, req, resp);
        } else {
            final String path = this.siteProfile.path;
            final String successPath = "instruction/home.html";

            final String redirect = path + (path.endsWith(CoreConstants.SLASH) ? successPath
                    : CoreConstants.SLASH + successPath);

            // Install the session ID cookie in the response
            Log.info("Adding session ID cookie ", req.getServerName());
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(req.getServerName());
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
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return true;
    }
}
