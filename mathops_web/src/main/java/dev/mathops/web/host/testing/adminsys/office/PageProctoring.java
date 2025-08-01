package dev.mathops.web.host.testing.adminsys.office;

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
 * A page to manage proctoring activities.
 */
enum PageProctoring {
    ;

    /**
     * Generates the page that shows proctoring options.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

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
}
