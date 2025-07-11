package dev.mathops.web.host.placement.root;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.placement.placement.MathPlacementSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The Math Placement website.
 */
public final class PlacementRedirector extends AbstractPageSite {

    /**
     * Constructs a new {@code PlacementSite}.
     *
     * @param theSite the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public PlacementRedirector(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Math Placement Tool";
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
                      final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(MathPlacementSite.class, "style.css", true));
        } else if ("placement.css".equals(subpath)) {
            doPlacementCss(req, resp);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final String url = type == ESiteType.DEV
                    ? "https://placementdev.math.colostate.edu/welcome/welcome.html"
                    : "https://placement.math.colostate.edu/welcome/welcome.html";

            resp.sendRedirect(url);
        }
    }

    /**
     * Generates a basic CSS stylesheet for use in utility pages.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void doPlacementCss(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        BasicCss.getInstance().serveCss(req, resp);
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
                       final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        final String url = type == ESiteType.DEV
                ? "https://placementdev.math.colostate.edu/welcome/welcome.html"
                : "https://placement.math.colostate.edu/welcome/welcome.html";

        resp.sendRedirect(url);
    }
}
