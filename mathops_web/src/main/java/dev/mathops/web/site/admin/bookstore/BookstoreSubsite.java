package dev.mathops.web.site.admin.bookstore;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AbstractSubsite;
import dev.mathops.web.site.admin.AdminSite;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The site for use by the bookstore to handle returns of e-text keys.
 */
public final class BookstoreSubsite extends AbstractSubsite {

    /**
     * Constructs a new {@code v}.
     *
     * @param theSite the owning site
     */
    public BookstoreSubsite(final AdminSite theSite) {

        super(theSite);
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
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsiteGet(final Cache cache, final String subpath,
                           final ImmutableSessionInfo session, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        // TODO: Honor maintenance mode.

        if (session.getEffectiveRole().canActAs(ERole.BOOKSTORE)) {
            if ("home.html".equals(subpath)) {
                PageHome.doHomePage(cache, this.site, req, resp, session);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            Log.warning("GET: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
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
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsitePost(final Cache cache, final String subpath,
                            final ImmutableSessionInfo session, final HttpServletRequest req,
                            final HttpServletResponse resp) throws IOException, SQLException {

        // TODO: Honor maintenance mode.

        if (session.getEffectiveRole().canActAs(ERole.BOOKSTORE)) {
            if ("rolecontrol.html".equals(subpath)) {
                BookstorePage.processRoleControls(cache, this.site, req, resp, session);
            } else if ("check_etext_key.html".equals(subpath)) {
                PageCheckKey.checkEtextKey(cache, this.site, req, resp, session);
            } else if ("deactivate_etext_key.html".equals(subpath)) {
                PageDeactiveKey.deactivateKey(cache, this.site, req, resp, session);
            } else if ("deactivate_etext_key_yes.html".equals(subpath)) {
                PageDeactiveKey.deactivateKeyYes(cache, this.site, req, resp, session);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            Log.warning("POST: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
