package dev.mathops.web.site.cfm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
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
public final class CfmSite extends AbstractSite {

    /** An empty byte array. */
    static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * Constructs a new {@code CfmSite}.
     *
     * @param theSiteProfile     the site profile under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public CfmSite(final Site theSiteProfile, final ISessionManager theSessions) {

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

        return "The Center for Foundational Mathematics";
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
            case "basestyle.css", "secure/basestyle.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
            case "style.css", "secure/style.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
            case "favicon.ico" -> serveImage(subpath, req, resp);

            case CoreConstants.EMPTY, "index.html" -> PageCfmIndex.showPage(cache, this, type, req, resp);
            case "info.html" -> PageInformation.showPage(cache, this, req, resp);
            case "contact.html" -> PageContact.showPage(cache, this, req, resp);
            case "analytics.html" -> PageAnalytics.showPage(cache, this, req, resp);
            case "strategy.html" -> PageStrategy.showPage(cache, this, req, resp);

            case null, default -> {
                resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                final String path = this.site.path;
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
