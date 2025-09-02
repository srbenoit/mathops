package dev.mathops.web.host.nibbler.scheduling;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Contexts;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The main site.
 */
public final class SchedulingSite extends AbstractSite {

    /** An empty byte array. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * Constructs a new {@code SchedulingSite}.
     *
     * @param theSite     the site profile under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public SchedulingSite(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);
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

        return "Scheduling";
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

        switch (subpath) {
            case "basestyle.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
            case "style.css" -> sendReply(req, resp, "text/css", new byte[0]);
            case "favicon.ico" -> serveImage(subpath, req, resp);
            case CoreConstants.EMPTY, "index.html" -> PageScheduling.showPage(cache, this, type, req, resp);
            case "spursim.html" -> PageSpurSim.showPage(cache, this, type, req, resp);
            case "center_scheduler.html" -> PageCenterScheduler.showPage(cache, this, type, req, resp);
            case null, default -> {
                final ImmutableSessionInfo session = validateSession(req, resp, null);

                if (session == null) {
                    if ("secure/shibboleth.html".equals(subpath)) {
                        doShibbolethLogin(cache, req, resp, null);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                        final String path = this.site.path;
                        resp.setHeader("Location",
                                path + (path.endsWith(Contexts.ROOT_PATH) ? "index.html" : "/index.html"));
                        sendReply(req, resp, Page.MIME_TEXT_HTML, ZERO_LEN_BYTE_ARR);
                    }
                } else {
                    final String effectiveId = session.getEffectiveUserId();
                    LogBase.setSessionInfo(session.loginSessionId, effectiveId);

                    if ("home.html".equals(subpath)) {
                        PageHome.showPage(cache, this, req, resp, session);
                    } else if ("secure/shibboleth.html".equals(subpath)) {
                        doShibbolethLogin(cache, req, resp, session);
                    } else {
                        Log.warning("Unrecognized path", subpath);
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }

                    LogBase.setSessionInfo(null, null);
                }

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

        doGet(cache, subpath, type, req, resp);
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

        ImmutableSessionInfo effectiveSession = session;

        if (effectiveSession == null) {
            effectiveSession = processShibbolethLogin(cache, req);
        }

        final String path = this.site.path;
        final String redirect;
        if (effectiveSession == null) {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html");
        } else {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? "home.html" : CoreConstants.SLASH + "home.html");

            // Install the session ID cookie in the response
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, effectiveSession.loginSessionId);
            cook.setDomain(req.getServerName());
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);
    }
}
