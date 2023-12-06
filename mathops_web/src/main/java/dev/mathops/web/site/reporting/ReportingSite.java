package dev.mathops.web.site.reporting;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.web.file.WebFileLoader;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The reporting site.
 */
public final class ReportingSite extends AbstractSite {

    /** Zero-length array used in construction of other arrays. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * Constructs a new {@code ReportingSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public ReportingSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

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
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return false;
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Reporting Site";
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
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

//        Log.info("GET ", subpath);

        // TODO: Honor maintenance mode.

        if ("basestyle.css".equals(subpath) || "secure/basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", WebFileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath) || "secure/style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", WebFileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else if ("favicon.ico".equals(subpath) || "secure/favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final ImmutableSessionInfo session = validateSession(req, resp, null);

            final boolean showLanding = CoreConstants.EMPTY.equals(subpath) || "index.html".equals(subpath)
                    || "login.html".equals(subpath);

            if (session == null) {
                if (showLanding) {
                    PageLanding.showPage(cache, this, req, resp);
                } else if ("secure/shibboleth.html".equals(subpath)) {
                    doShibbolethLogin(cache, req, resp, null);
                } else {
                    resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    final String path = this.siteProfile.path;
                    resp.setHeader("Location",
                            path + (path.endsWith(Contexts.ROOT_PATH) ? "index.html" : "/index.html"));
                    sendReply(req, resp, Page.MIME_TEXT_HTML, ZERO_LEN_BYTE_ARR);
                }
            } else {
                LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                if (showLanding) {
                    PageLanding.showPage(cache, this, req, resp);
                } else if (subpath.endsWith(".js")) {
                    serveJs(subpath, req, resp);
                } else if ("home.html".equals(subpath)) {
                    PageHome.showPage(cache, this, req, resp, session);
                } else if ("placement_by_category.html".equals(subpath)) {
                    PagePlacementByCategory.showPage(cache, this, req, resp, session);
                } else if ("placement_by_students.html".equals(subpath)) {
                    PagePlacementByStudents.showPage(cache, this, req, resp, session);





                } else if ("secure/shibboleth.html".equals(subpath)) {
                    doShibbolethLogin(cache, req, resp, session);
                } else {
                    Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

                LogBase.setSessionInfo(null, null);
            }
        }
    }

    /**
     * Serves a .js file from the package containing the ReportingSite class.
     *
     * @param name the filename, with extension
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void serveJs(final String name, final ServletRequest req,
                                final HttpServletResponse resp) throws IOException {

        final byte[] data = WebFileLoader.loadFileAsBytes(ReportingSite.class, name, true);

        if (data == null) {
            Log.warning(name, " not found");
            resp.sendError(404);
        } else {
            sendReply(req, resp, "text/javascript", data);
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

        // TODO: Honor maintenance mode.

        // All posts require a session
        final ImmutableSessionInfo session = validateSession(req, resp, null);

        // Log.info("POST ", subpath, " session is ", session);

        if (session == null) {
            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

            if ("placement_by_category.html".equals(subpath)) {
                PagePlacementByCategory.showPage(cache, this, req, resp, session);
            } else if ("placement_by_students.html".equals(subpath)) {
                PagePlacementByStudents.showPage(cache, this, req, resp, session);
            } else {
                Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

            LogBase.setSessionInfo(null, null);
        }
    }

    /**
     * Scans the request for Shibboleth attributes and uses them (if found) to establish a session, and then redirects
     * to either the secure page (if valid) or the login page (if not valid).
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws SQLException if there was an error accessing the database
     */
    private void doShibbolethLogin(final Cache cache, final HttpServletRequest req, final HttpServletResponse resp,
                                   final ImmutableSessionInfo session) throws SQLException {

        Log.info("Shibboleth login attempt");

        ImmutableSessionInfo sess = session;

        if (sess == null) {
            sess = processShibbolethLogin(cache, req);
        }

        final String path = this.siteProfile.path;
        final String redirect;
        if (sess == null) {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? "login.html" : "/login.html");
        } else {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? "home.html" : CoreConstants.SLASH + "home.html");

            // Install the session ID cookie in the response
            Log.info("Adding session ID cookie ", req.getServerName());
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(req.getServerName());
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }

        Log.info("Redirecting to ", redirect);

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);
    }
}
