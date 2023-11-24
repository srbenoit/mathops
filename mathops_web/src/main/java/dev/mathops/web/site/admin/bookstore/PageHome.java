package dev.mathops.web.site.admin.bookstore;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The bookstore site home page.
 */
enum PageHome {
    ;

    /**
     * Generates a page that shows the user's dashboard. If the user is not an administrator, this simply shows the user
     * information. For administrators, there is a menu of functions.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doHomePage(final Cache cache, final AdminSite site, final ServletRequest req,
                           final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = BookstorePage.startBookstorePage(site, session);

        BookstorePage.emitKeyForm(htm, null, null);

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
