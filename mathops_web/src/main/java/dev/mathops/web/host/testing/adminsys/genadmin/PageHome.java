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
 * The administrative system login page.
 */
enum PageHome {
    ;

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, false);

        GenAdminPage.emitNavBlock(null, htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Starts a navigation button.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param id         the button ID ("first" or "last" to adjust margins)
     * @param buttonName the button name
     * @param url        the URL to which the button redirects
     */
    private static void navButton(final HtmlBuilder htm, final String id, final String buttonName, final String url) {

        htm.add("<button");
        htm.add(" class='nav4'");
        if (id != null) {
            htm.add(" id='", id, "'");
        }
        htm.add(" onclick='pick(\"", url, "\");'>", buttonName, "</button>");
    }
}
