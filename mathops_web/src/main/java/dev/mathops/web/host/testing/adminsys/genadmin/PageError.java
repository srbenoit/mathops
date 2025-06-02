package dev.mathops.web.host.testing.adminsys.genadmin;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a page that displays an error message.
 */
public enum PageError {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param message the error message
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session, final String message)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        htm.sDiv("error").br();
        htm.addln("<strong>An error has occurred:</strong>").br().br();
        htm.addln(message);
        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}
