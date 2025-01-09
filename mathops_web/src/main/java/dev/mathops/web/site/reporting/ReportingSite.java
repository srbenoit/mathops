package dev.mathops.web.site.reporting;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.UserInfoBar;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.NONE;
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

        switch (subpath) {
            case "basestyle.css", "secure/basestyle.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
            case "style.css", "secure/style.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
            case "favicon.ico", "secure/favicon.ico" -> serveImage(subpath, req, resp);
            case null -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);

            default -> {
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
                    } else if ("reports_admin.html".equals(subpath)) {
                        PageAdmin.showPage(cache, this, req, resp, session);
                    } else if ("placement_by_category.html".equals(subpath)) {
                        PagePlacementByCategory.showPage(cache, this, req, resp, session);
                    } else if ("placement_by_students.html".equals(subpath)) {
                        PagePlacementByStudents.showPage(cache, this, req, resp, session);
                    } else if ("precalc_by_course.html".equals(subpath)) {
                        PagePrecalcStatusBySections.showPage(cache, this, req, resp, session);
                    } else if ("precalc_by_students.html".equals(subpath)) {
                        PagePrecalcStatusByStudents.showPage(cache, this, req, resp, session);
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

        final byte[] data = FileLoader.loadFileAsBytes(ReportingSite.class, name, true);

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

            switch (subpath) {
                case "placement_by_category.html" -> PagePlacementByCategory.showPage(cache, this, req, resp, session);
                case "placement_by_students.html" -> PagePlacementByStudents.showPage(cache, this, req, resp, session);
                case "precalc_by_course.html" -> PagePrecalcStatusBySections.showPage(cache, this, req, resp,session);
                case "precalc_by_students.html" -> PagePrecalcStatusByStudents.showPage(cache, this, req, resp,
                        session);

                case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);
                case "reports_admin.html" -> PageAdmin.processPost(cache, req, resp);
                case null, default -> {
                    Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }

            LogBase.setSessionInfo(null, null);
        }
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
    private void processRoleControls(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                                     final ImmutableSessionInfo session) throws IOException, SQLException {

        UserInfoBar.processRoleControls(cache, req, session);

        final String target = req.getParameter("target");

        if (isParamInvalid(target)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  target='", target, "'");
            resp.sendError(400, "Invalid target provided with role control");
        } else if (target == null) {
            resp.sendError(400, "No target provided with role control");
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


    /**
     * Attempts to extract student IDs from a submitted text list.
     *
     * @param idlist the ID list text
     * @return the list of extracted IDs
     */
    public static List<String> extractIds(final String idlist) {

        final List<String> studentIds = new ArrayList<>(30);

        if (idlist != null && !idlist.isBlank()) {

            final int len = idlist.length();
            int start = 0;
            int index = 0;
            while (index < len) {
                final char ch = idlist.charAt(index);

                if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f' || ch == ',' || ch == ';'
                        || ch == '.' || ch == '|' || ch == ':' || ch == '"' || ch == '\'') {
                    if (start < index) {
                        final String id = idlist.substring(start, index).trim();
                        if (!id.isBlank()) {
                            Log.info("Adding '", id, "'");
                            studentIds.add(id);
                        }
                    }
                    start = index + 1;
                }
                ++index;
            }
            if (start < len) {
                final String id = idlist.substring(start, len).trim();
                if (!id.isBlank()) {
                    Log.info("Adding '", id, "'");
                    studentIds.add(id);
                }
            }
        }
        return studentIds;
    }

    /**
     * Cleans a student ID that could have been entered by a user.  This removes any characters that are not digits.
     *
     * @param stuId the student ID to be cleaned
     * @return the cleaned ID
     */
    public static String cleanId(final String stuId) {

        final int len = stuId.length();
        final HtmlBuilder cleaned = new HtmlBuilder(len);

        for (int i= 0; i < len; ++i) {
            final char ch = stuId.charAt(i);
            if (ch >= '0' && ch <= '9') {
                cleaned.add(ch);
            }
        }

        return cleaned.toString();
    }
}
