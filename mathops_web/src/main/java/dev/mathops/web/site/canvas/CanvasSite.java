package dev.mathops.web.site.canvas;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A site that delivers courses in a style similar to Canvas.  This allows testing of content for potential deep
 * integration with Canvas, and provides an alternative if that integration is not permitted.
 */
public final class CanvasSite extends AbstractPageSite {

    /**
     * Constructs a new {@code CanvasSite}.
     *
     * @param theSiteProfile the website profile
     * @param theSessions    the singleton user session repository
     */
    public CanvasSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
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

        final String path = this.siteProfile.path;

        Log.info("GET ", subpath, " within ", path);

        if (CoreConstants.EMPTY.equals(subpath)) {
            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html"));
        } else if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(CanvasSite.class, "style.css", true));
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final String maintenanceMsg = isMaintenance(this.siteProfile);

            if (maintenanceMsg == null) {
                // The pages that follow require the user to be logged in
                final ImmutableSessionInfo session = validateSession(req, resp, null);

                if (session == null) {
                    switch (subpath) {
                        case "index.html", "login.html" -> PageLogin.doGet(cache, this, type, req, resp);

                        case "secure/shibboleth.html" -> doShibbolethLogin(cache, req, resp, null, "home.html");
                        default -> {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH)
                                    ? "index.html" : "/index.html"));
                        }
                    }
                } else {
                    final String userId = session.getEffectiveUserId();
                    LogBase.setSessionInfo(session.loginSessionId, userId);

                    switch (subpath) {
                        case "secure/shibboleth.html" -> doShibbolethLogin(cache, req, resp, session, "home.html");

                        case "index.html", "home.html" -> PageHome.doGet(cache, this, req, resp, session);

                        default -> {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH)
                                    ? "index.html" : "/index.html"));
                        }
                    }
                }
            } else {
                PageMaintenance.doGet(cache, this, req, resp, maintenanceMsg);
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

        final String path = this.siteProfile.path;

        Log.info("POST ", subpath, " within ", path);

        final String maintenanceMsg = isMaintenance(this.siteProfile);

        if (maintenanceMsg == null) {
            final ImmutableSessionInfo session = validateSession(req, resp, "index.html");

            if (session != null) {
                final String userId = session.getEffectiveUserId();
                LogBase.setSessionInfo(session.loginSessionId, userId);

                final CourseSiteLogic logic = new CourseSiteLogic(cache, this.siteProfile, session);
                logic.gatherData();

                switch (subpath) {
                    case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);

                    case null, default -> {
                        Log.warning("Unrecognized POST request path: ", subpath);
                        resp.sendRedirect(path + (path.endsWith(CoreConstants.SLASH) ? "index.html" : "/index.html"));
                    }
                }
            }
        } else {
            PageMaintenance.doGet(cache, this, req, resp, maintenanceMsg);
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
