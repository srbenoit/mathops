package dev.mathops.web.site;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A minimal site that simply redirect all requests to a specified URL.
 */
public final class RedirectToPrecalcSite extends AbstractSite {

    /**
     * Constructs a new {@code RedirectToPrecalcSite}.
     *
     * @param theSiteProfile the website profile
     * @param theSessions    the singleton user session repository
     */
    public RedirectToPrecalcSite(final WebSiteProfile theSiteProfile,
                                 final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
    }

    /**
     * Initializes the site - called when the servlet is initialized.
     *
     * @param config the servlet context in which the servlet is being initialized
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

        return "The Precalculus Center";
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

        final String url =
                type == ESiteType.DEV ? "https://precalcdev.math.colostate.edu/index.html"
                        : "https://precalc.math.colostate.edu/index.html";

        resp.sendRedirect(url);
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

        final String url =
                type == ESiteType.DEV ? "https://precalcdev.math.colostate.edu/index.html"
                        : "https://precalc.math.colostate.edu/index.html";

        resp.sendRedirect(url);
    }
}
