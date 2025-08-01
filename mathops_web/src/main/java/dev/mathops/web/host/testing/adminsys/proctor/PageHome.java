package dev.mathops.web.host.testing.adminsys.proctor;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminPage;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Pages that allow the user to choose a population of students.
 */
enum PageHome {
    ;

    /**
     * Handles a POST from the page.
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

        final RawWhichDb whichDb = cache.getSystemData().getWhichDb();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, false, null, "home.html", Page.NO_BARS, null, false, true);
        AdminPage.emitPageHeader(htm, session, whichDb, false);

        htm.sDiv("buttonstack");

        htm.sH(2).add("Online Proctoring").eH(2).hr();

        htm.sH(3).add("Course Exams, ELM Exam, Precalc Tutorial Exams").eH(2);

        htm.addln("<form method='get' action='proctoring_teams.html'>");
        htm.add("<button class='nav'>Using Microsoft Teams</button>");
        htm.addln("</form>");

        htm.div("vgap2");

        htm.sH(3).add("Proctoring For Challenge Exams").eH(3);

        htm.addln("<form method='get' action='proctoring_challenge_teams.html'>");
        htm.add("<button class='nav'>Using Microsoft Teams</button>");
        htm.addln("</form>");

        htm.div("vgap2");

        htm.eDiv(); // buttonstack

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Starts a small navigation button.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected true if button is currently selected
     * @param label    the button label
     * @param url      the URL to which to link
     * @param query    optional query string (not including leading '?')
     */
    static void navButtonSmall(final HtmlBuilder htm, final boolean selected, final String label,
                               final String url, final String query) {

        htm.add("<button");
        if (selected) {
            htm.add(" class='nav8 selected'");
        } else {
            htm.add(" class='nav8'");
        }
        htm.add(" onclick='pick(\"", url);
        if (query != null) {
            htm.add('?').add(query);
        }
        htm.add("\");'>", label, "</button>");
    }
}
