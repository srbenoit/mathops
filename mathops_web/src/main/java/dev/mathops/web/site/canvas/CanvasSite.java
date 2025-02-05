package dev.mathops.web.site.canvas;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.UserInfoBar;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A site that delivers courses in a style similar to Canvas.  This allows testing of content for potential deep
 * integration with Canvas, and provides an alternative if that integration is not permitted.
 */
public final class CanvasSite extends AbstractSite {

    /** A page. */
    private static final String HOME_PAGE = "home.html";

    /** A page. */
    private static final String LOGIN_PAGE = "login.html";

    /** A page. */
    private static final String COURSE_PAGE = "course.html";

    /** A page. */
    private static final String COURSE_TEXT_PAGE = "course_text.html";

    /** A page. */
    private static final String COURSE_TEXT_MODULE_PAGE = "course_text_module.html";

    /** A CSS filename. */
    private static final String BASE_STYLE_CSS = "basestyle.css";

    /** A CSS filename. */
    private static final String STYLE_CSS = "style.css";

    /** A web page. */
    private static final String SHIBBOLETH_PAGE = "secure/shibboleth.html";

    /** A request parameter name. */
    private static final String TARGET_PARAM = "target";

    /** A request parameter name. */
    static final String COURSE_PARAM = "course";

    /**
     * Constructs a new {@code CanvasSite}.
     *
     * @param theSite     the website profile
     * @param theSessions the singleton user session repository
     */
    public CanvasSite(final Site theSite, final ISessionManager theSessions) {

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
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        final String path = this.site.path;

        Log.info("GET ", subpath, " within ", path);

        switch (subpath) {
            case BASE_STYLE_CSS -> serveCss(req, resp, Page.class, BASE_STYLE_CSS);
            case STYLE_CSS -> serveCss(req, resp, CanvasSite.class, STYLE_CSS);
            case "favicon.ico" -> serveImage(subpath, req, resp);

            case null -> {
                final String homePath = makePagePath(HOME_PAGE, null);
                resp.sendRedirect(homePath);
            }

            case CoreConstants.EMPTY -> {
                final String homePath = makePagePath(HOME_PAGE, null);
                resp.sendRedirect(homePath);
            }

            default -> doPageGet(cache, subpath, req, resp);
        }
    }

    /**
     * Processes a GET request for a page. Before this method is called, the request will have been verified to be
     * secure and have a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    private void doPageGet(final Cache cache, final String subpath, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        final String path = this.site.path;

        final String maintenanceMsg = isMaintenance(this.site);

        if (maintenanceMsg == null) {
            // The pages that follow require the user to be logged in
            final ImmutableSessionInfo session = validateSession(req, resp, null);

            if (session == null) {
                switch (subpath) {
                    case LOGIN_PAGE -> PageLogin.doGet(cache, this, req, resp);
                    case SHIBBOLETH_PAGE -> doShibbolethLogin(cache, req, resp, null);

                    case null, default -> {
                        Log.warning("Unrecognized GET request path: ", subpath);
                        final String selectedCourse = req.getParameter(COURSE_PARAM);
                        final String homePath = makePagePath(LOGIN_PAGE, selectedCourse);
                        resp.sendRedirect(homePath);
                    }
                }
            } else {
                final String userId = session.getEffectiveUserId();
                LogBase.setSessionInfo(session.loginSessionId, userId);

                switch (subpath) {
                    case LOGIN_PAGE -> PageLogin.doGet(cache, this, req, resp);
                    case SHIBBOLETH_PAGE -> doShibbolethLogin(cache, req, resp, session);
                    case HOME_PAGE -> PageHome.doGet(cache, this, req, resp, session);
                    case COURSE_PAGE -> PageCourse.doGet(cache, this, req, resp, session);
                    case COURSE_TEXT_PAGE -> PageCourseText.doGet(cache, this, req, resp, session);
                    case COURSE_TEXT_MODULE_PAGE -> PageCourseModule.doGet(cache, this, req, resp, session);

                    case null, default -> {
                        Log.warning("Unrecognized GET request path: ", subpath);
                        final String selectedCourse = req.getParameter(COURSE_PARAM);
                        final String homePath = selectedCourse == null ? makePagePath(HOME_PAGE, null)
                                : makePagePath(COURSE_PAGE, selectedCourse);
                        resp.sendRedirect(homePath);
                    }
                }
            }
        } else {
            PageMaintenance.doGet(cache, this, req, resp, maintenanceMsg);
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
    public void doPost(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                       final HttpServletResponse resp) throws IOException, SQLException {

        final String path = this.site.path;

        Log.info("POST ", subpath, " within ", path);

        final String maintenanceMsg = isMaintenance(this.site);

        if (maintenanceMsg == null) {
            final ImmutableSessionInfo session = validateSession(req, resp, LOGIN_PAGE);

            if (session != null) {
                final String userId = session.getEffectiveUserId();
                LogBase.setSessionInfo(session.loginSessionId, userId);

                switch (subpath) {
                    case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);

                    case null, default -> {
                        Log.warning("Unrecognized POST request path: ", subpath);
                        final String selectedCourse = req.getParameter(COURSE_PAGE);
                        final String homePath = selectedCourse == null ? makePagePath(HOME_PAGE, null)
                                : makePagePath(COURSE_PAGE, selectedCourse);
                        resp.sendRedirect(homePath);
                    }
                }
            }
        } else {
            PageMaintenance.doGet(cache, this, req, resp, maintenanceMsg);
        }
    }

    /**
     * Serves a CSS file.
     *
     * @param req      the request
     * @param resp     the response
     * @param cls      the class in whose package the CSS file is found
     * @param filename the CSS file name
     * @throws IOException if there is an error writing the response
     */
    private static void serveCss(final HttpServletRequest req, final HttpServletResponse resp, final Class<?> cls,
                                 final String filename) throws IOException {

        final byte[] cssBytes = FileLoader.loadFileAsBytes(cls, filename, true);

        sendReply(req, resp, MIME_TEXT_CSS, cssBytes);
    }

    /**
     * Processes any submissions by the role controls (call on POST).
     *
     * @param cache   the data cache
     * @param req     the HTTP request
     * @param resp    the HTTP response
     * @param session the session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void processRoleControls(final Cache cache, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        UserInfoBar.processRoleControls(cache, req, session);

        final String target = req.getParameter(TARGET_PARAM);

        if (isParamInvalid(target)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  target='", target, "'");
//            PageError.doGet(cache, this, req, resp, session, "No target provided with role control");
        } else if (target == null) {
//            PageError.doGet(cache, this, req, resp, session, "No target provided with role control");
        } else {
            resp.sendRedirect(target);
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

        final String selectedCourse = req.getParameter(COURSE_PARAM);

        final String redirect;
        if (sess == null) {
            // Login failed - return to login page
            redirect = makePagePath(LOGIN_PAGE, selectedCourse);
        } else {
            redirect = selectedCourse == null ? makePagePath(HOME_PAGE, null)
                    : makePagePath(COURSE_PAGE, selectedCourse);

            // Install the session ID cookie in the response
            final String serverName = req.getServerName();
            Log.info("Adding session ID cookie ", serverName);
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(serverName);
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);
        Log.info("Redirecting to ", redirect);
    }

    /**
     * Given the path of this site, generates the path of a page.
     *
     * @param page           the page, like "home.html"
     * @param selectedCourse the course ID to add as a parameter (null to skip adding parameter)
     * @return the index file path
     */
    String makePagePath(final String page, final String selectedCourse) {

        final String result;

        final String path = this.site.path;
        final boolean endsWithSlash = path.endsWith(CoreConstants.SLASH);
        final String fixedPage = endsWithSlash ? page : ("/" + page);

        if (selectedCourse == null) {
            result = SimpleBuilder.concat(path, fixedPage);
        } else {
            final String encoded = URLEncoder.encode(selectedCourse, StandardCharsets.UTF_8);
            result = SimpleBuilder.concat(path, fixedPage, "?", COURSE_PARAM, "=", encoded);
        }

        return result;
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

    /**
     * Generates the site title based on the context.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }
}
