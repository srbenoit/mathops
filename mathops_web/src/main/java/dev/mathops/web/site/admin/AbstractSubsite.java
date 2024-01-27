package dev.mathops.web.site.admin;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.old.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The base class for sub-sites of the administrative site.
 */
public abstract class AbstractSubsite {

    /** The owning site. */
    public final AdminSite site;

    /**
     * Constructs a new {@code AbstractSubsite}.
     *
     * @param theSite the owning site
     */
    protected AbstractSubsite(final AdminSite theSite) {

        this.site = theSite;
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public final void doGet(final Cache cache, final String subpath, final ImmutableSessionInfo session,
                            final HttpServletRequest req, final HttpServletResponse resp) throws IOException, SQLException {

        if ("basestyle.css".equals(subpath)) {
            AbstractSite.sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css",
                    true));
        } else if ("style.css".equals(subpath)) {
            AbstractSite.sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(this.site.getClass(),
                    "style.css", true));
        } else if (subpath.startsWith("images/")) {
            this.site.serveImage(subpath.substring(7), req, resp);
        } else {
            subsiteGet(cache, subpath, session, req, resp);
        }
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    protected abstract void subsiteGet(final Cache cache, final String subpath,
                                       final ImmutableSessionInfo session, final HttpServletRequest req,
                                       final HttpServletResponse resp) throws IOException, SQLException;

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public final void doPost(final Cache cache, final String subpath,
                             final ImmutableSessionInfo session, final HttpServletRequest req,
                             final HttpServletResponse resp) throws IOException, SQLException {

        // We could intercept common POST requests here, such as "act as" requests

        subsitePost(cache, subpath, session, req, resp);
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    protected abstract void subsitePost(final Cache cache, final String subpath,
                                        final ImmutableSessionInfo session, final HttpServletRequest req,
                                        final HttpServletResponse resp) throws IOException, SQLException;
}
