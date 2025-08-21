package dev.mathops.web.host.nibbler.root;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.BasicCss;
import dev.mathops.web.site.ESiteType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A root site that does nothing but serve the basic image/icon files.
 */
public final class NibblerRootSite extends AbstractSite {

    /** The name of a style sheet. */
    private static final String ADMIN_CSS = "admin.css";

    /** The name of a style sheet. */
    private static final String STYLE_CSS = "style.css";

    /** A path. */
    private static final String IMAGES_PATH = "images/";

    /**
     * Constructs a new {@code NibblerRootSite}.
     *
     * @param theSite     the context under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public NibblerRootSite(final Site theSite, final ISessionManager theSessions) {

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

        return CoreConstants.EMPTY;
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp) throws IOException, SQLException {

        if (STYLE_CSS.equals(subpath)) {
            final Class<? extends NibblerRootSite> myClass = getClass();
            final byte[] fileBytes = FileLoader.loadFileAsBytes(myClass, STYLE_CSS, true);
            sendReply(req, resp, "text/css", fileBytes);
        } else if (ADMIN_CSS.equals(subpath)) {
            BasicCss.getInstance().serveCss(req, resp);
        } else if (subpath.startsWith(IMAGES_PATH)) {
            final String substring = subpath.substring(7);
            serveImage(substring, req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else if (subpath.startsWith("media/") || subpath.startsWith("math/")) {
            serveMedia(cache, subpath, req, resp);
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
