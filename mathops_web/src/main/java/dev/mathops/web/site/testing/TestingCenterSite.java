package dev.mathops.web.site.testing;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The site that handles testing center applications.
 */
public final class TestingCenterSite extends AbstractSite {

    /**
     * Constructs a new {@code TestingCenterSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public TestingCenterSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

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
    public void doGet(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                      final HttpServletResponse resp) throws IOException {

        // GET https://testing.math.colostate.edu/*

        if (subpath.startsWith("images/")) {
            final String substring = subpath.substring(7);
            serveImage(substring, req, resp);
        } else if (req.isSecure()) {
            if ("assessment_item.ws".equals(subpath)) {
                AssessmentItem.doGet(cache, subpath, type, req, resp);
            } else {
                indicateNotFound(subpath, req, resp);
            }
        } else {
            indicateNotFound(subpath, req, resp);
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
                       final HttpServletResponse resp) throws IOException {

        // POST  https://testing.math.colostate.edu/*

        indicateNotFound(subpath, req, resp);
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    private void indicateNotFound(final String subpath, final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final String requestURI = req.getRequestURI();
        Log.warning(requestURI, " (", subpath, ") not found");

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
