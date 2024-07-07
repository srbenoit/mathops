package dev.mathops.web.site.landing;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The main site.
 */
public final class LandingSite extends AbstractSite {

    /** An empty byte array. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * Constructs a new {@code LandingSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public LandingSite(final WebSiteProfile theSiteProfile,
                       final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
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

        // Log.info("GET ", subpath);

        // TODO: Honor maintenance mode.

        switch (subpath) {
            case "basestyle.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
            case "style.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
            case "favicon.ico" -> serveImage(subpath, req, resp);
            case CoreConstants.EMPTY, "index.html" -> PageLanding.showPage(cache, this, type, req, resp);
            case null, default -> {
                resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                final String path = this.siteProfile.path;
                resp.setHeader("Location",
                        path + (path.endsWith(Contexts.ROOT_PATH) ? "index.html" : "/index.html"));
                sendReply(req, resp, Page.MIME_TEXT_HTML, ZERO_LEN_BYTE_ARR);
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
}
