package dev.mathops.web.site.admin;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * This servlet serves requests to the root directory of the testing hostname. It presents no pages, but provides access
 * to images, fonts, and jars whose path is relative to '/'.
 */
public final class AdminRootSite extends AbstractSite {

    /** The name of a style sheet. */
    private static final String ADMIN_CSS = "admin.css";

    /** The name of a style sheet. */
    private static final String STYLE_CSS = "style.css";

    /** A path. */
    private static final String IMAGES_PATH = "images/";

    /**
     * Constructs a new {@code AdminRootSite}.
     *
     * @param theSiteProfile the context under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public AdminRootSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

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

        if (STYLE_CSS.equals(subpath)) {
            final Class<? extends AdminRootSite> siteClass = getClass();
            final byte[] cssBytes = FileLoader.loadFileAsBytes(siteClass, STYLE_CSS, true);
            sendReply(req, resp, "text/css", cssBytes);
        } else if (ADMIN_CSS.equals(subpath)) {
            BasicCss.getInstance().serveCss(req, resp);
        } else if (subpath.startsWith(IMAGES_PATH)) {
            final String imagePath = subpath.substring(7);
            serveImage(imagePath, req, resp);
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
