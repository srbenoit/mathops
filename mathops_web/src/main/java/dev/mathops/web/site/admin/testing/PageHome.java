package dev.mathops.web.site.admin.testing;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AdminSite;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Page that presents actions related to testing.
 */
enum PageHome {
    ;

    /**
     * Handles a GET request for the page.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = TestingPage.startTestingPage(site, session);

        TestingPage.emitNavBlock(null, htm);

        TestingPage.endTestingPage(cache, htm, site, req, resp);
    }

//    /**
//     * Starts a small navigation button.
//     *
//     * @param htm      the {@code HtmlBuilder} to which to append
//     * @param selected true if button is currently selected
//     * @param label    the button label
//     * @param url      the URL to which to link
//     * @param query    optional query string (not including leading '?')
//     */
//    static void navButtonSmall(final HtmlBuilder htm, final boolean selected, final String label,
//                               final String url, final String query) {
//
//        htm.add("<button");
//        if (selected) {
//            htm.add(" class='nav8 selected'");
//        } else {
//            htm.add(" class='nav8'");
//        }
//        htm.add(" onclick='pick(\"", url);
//        if (query != null) {
//            htm.add('?').add(query);
//        }
//        htm.add("\");'>", label, "</button>");
//    }
}
