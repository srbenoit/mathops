package dev.mathops.web.site.ramwork;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.UserInfoBar;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/**
 * The main site.
 */
public final class RamWorkSite extends AbstractSite {

    /** Zero-length array used in construction of other arrays. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /** The data directory for proctoring sessions. */
    private final File proctoringDataDir;

    /**
     * Constructs a new {@code RamWorkSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public RamWorkSite(final WebSiteProfile theSiteProfile,
                       final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);

        TermRec active = null;

        final DbContext ctx = theSiteProfile.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(theSiteProfile.dbProfile, conn);

            try {
                active = cache.getSystemData().getActiveTerm();
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        final File dataPath = PathList.getInstance().get(EPath.CUR_DATA_PATH);
        this.proctoringDataDir = active == null ? null : new File(dataPath, //
                "proctoring" + active.term.shortString);
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

        return "Precalculus Program";
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

        Log.info("GET ", subpath);

        // TODO: Honor maintenance mode.

        switch (subpath) {
            case "basestyle.css", "secure/basestyle.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
            case "style.css", "secure/style.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
            case "favicon.ico", "secure/favicon.ico" -> serveImage(subpath, req, resp);
            case null, default -> {
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
                    } else if (subpath.endsWith(".jpg") || subpath.endsWith(".png")) {
                        serveProctoringImage(subpath, req, resp);
                    } else if ("home.html".equals(subpath)) {
                        PageHome.showPage(cache, this, req, resp, session);

                    } else if ("itembank.html".equals(subpath)) {
                        PageItemBank.showPage(cache, this, req, resp, session);
                    } else if ("qtiitembank.html".equals(subpath)) {
                        PageQTIItemBank.showPage(cache, this, req, resp, session, null);
                    } else if ("authoring.html".equals(subpath)) {
                        PageAuthoring.showPage(cache, this, req, resp);

                    } else if ("item.html".equals(subpath)) {
                        PageItem.showPage(cache, this, req, resp, session);
                    } else if ("item-edit.html".equals(subpath)) {
                        PageItemEdit.showPage(req, resp, session);
                    } else if ("mathrefresherlibrary.html".equals(subpath)) {
                        PageMathRefresherLibrary.showPage(req, resp, session);
                    } else if ("mathrefresherstudent.html".equals(subpath)) {
                        PageMathRefresherStudent.showPage(cache, req, resp, session);
                    } else if ("assessmentdev.html".equals(subpath)) {
                        PageAssessmentDev.showPage(req, resp);
                    } else if ("graphedit.html".equals(subpath)) {
                        PageGraphEditor.showPage(cache, this, req, resp);

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
     * Serves a .js file from the package containing the RamWorkSite class.
     *
     * @param name the filename, with extension
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void serveJs(final String name, final ServletRequest req,
                                final HttpServletResponse resp) throws IOException {

        final byte[] data = FileLoader.loadFileAsBytes(RamWorkSite.class, name, true);

        if (data == null) {
            Log.warning(name, " not found");
            resp.sendError(404);
        } else {
            sendReply(req, resp, "text/javascript", data);
        }
    }

    /**
     * Serves a .js file from the package containing the RamWorkSite class.
     *
     * @param name the filename, with extension
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private void serveProctoringImage(final String name, final HttpServletRequest req,
                                      final HttpServletResponse resp) throws IOException {

        final File sourceFile = new File(this.proctoringDataDir, name);

        final long total = sourceFile.length();
        long start = 0L;
        long end = total;
        boolean ranged = false;

        final String range = req.getHeader("Range");
        if ((range != null) && range.startsWith("bytes=")) {
            final String sub = range.substring(6);
            final String[] split = sub.split(CoreConstants.DASH);
            if (split.length == 2) {
                try {
                    start = (long) Integer.parseInt(split[0]);
                    end = (long) Integer.parseInt(split[1]);

                    ranged = true;
                } catch (final NumberFormatException ex) {
                    Log.warning(ex);
                }
            }
        }

        final byte[] data;

        if (ranged) {
            data = FileLoader.loadFileAsBytes(sourceFile, start, end);
        } else {
            data = FileLoader.loadFileAsBytes(sourceFile, true);
        }

        if (data == null) {
            Log.warning(sourceFile.getAbsolutePath(), " not found");
            resp.sendError(404);
        } else {
            final String lower = name.toLowerCase(Locale.ROOT);

            // final HtmlBuilder str = new HtmlBuilder(100);
            // str.add("Serving ", lower);
            // final Enumeration<String> headers = req.getHeaderNames();
            // while (headers.hasMoreElements()) {
            // final String h = headers.nextElement();
            // final String v = req.getHeader(h);
            // str.add(CoreConstants.SPC, h, "=", v);
            // }
            // Log.fine(str.toString());

            if (ranged) {
                if (lower.endsWith(".png")) {
                    sendRangedReply(req, resp, "image/png", data, start, total);
                } else if (lower.endsWith(".jpg")
                        || lower.endsWith(".jpeg")) {
                    sendRangedReply(req, resp, "image/jpeg", data, start, total);
                } else if (lower.endsWith(".webm")) {
                    sendRangedReply(req, resp, "video/webm", data, start, total);
                } else if (lower.endsWith(".mp4")) {
                    sendRangedReply(req, resp, "video/mp4", data, start, total);
                } else if (lower.endsWith(".ogv")) {
                    sendRangedReply(req, resp, "video/ogg", data, start, total);
                } else {
                    resp.sendError(404);
                }
            } else if (lower.endsWith(".png")) {
                sendReply(req, resp, "image/png", data);
            } else if (lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg")) {
                sendReply(req, resp, "image/jpeg", data);
            } else if (lower.endsWith(".webm")) {
                sendReply(req, resp, "video/webm", data);
            } else if (lower.endsWith(".mp4")) {
                sendReply(req, resp, "video/mp4", data);
            } else if (lower.endsWith(".ogv")) {
                sendReply(req, resp, "video/ogg", data);
            } else {
                resp.sendError(404);
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
                case "item-edit.html" -> PageItemEdit.processPost(req, resp, session);
                case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);
                case "qtiitembank.html" -> PageQTIItemBank.processPost(cache, this, req, resp, session);
                case "graphedit.html" -> PageGraphEditor.processPost(cache, this, req, resp);
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

        if (AbstractSite.isParamInvalid(target)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  target='", target, "'");
            PageError.doGet(cache, this, req, resp, session,
                    "No target provided with role control");
        } else if (target == null) {
            PageError.doGet(cache, this, req, resp, session,
                    "No target provided with role control");
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
}
