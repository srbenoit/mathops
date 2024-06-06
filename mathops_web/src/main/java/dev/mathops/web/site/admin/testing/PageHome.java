package dev.mathops.web.site.admin.testing;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
     * @param studentData the student data object
     * @param site        the site
     * @param req         the request
     * @param resp        the response
     * @param session     the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final StudentData studentData, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = TestingPage.startTestingPage(studentData, site, session);

        TestingPage.emitNavBlock(null, htm);

        TestingPage.endTestingPage(studentData, htm, site, req, resp);
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
