package dev.mathops.web.site.admin;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.file.WebFileLoader;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet serves requests to the root directory of the testing hostname. It presents no pages, but provides access
 * to images, fonts, and jars whose path is relative to '/'.
 */
public final class AdminRootSite extends AbstractSite {

    /**
     * Constructs a new {@code AdminRootSite}.
     *
     * @param theSiteProfile  the context under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public AdminRootSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

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

        return Res.get(Res.SITE_TITLE);
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

        if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", WebFileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else if ("admin.css".equals(subpath)) {
            BasicCss.getInstance().serveCss(req, resp);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
                       final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
