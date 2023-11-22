package dev.mathops.web.site.admin.office;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A page to manage testing center activities (makeup exams, calculator loans).
 */
enum PageTesting {
    ;

    /**
     * Generates the page that prompts the user to log in.
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

        final HtmlBuilder htm = OfficePage.startOfficePage(site, session, true);

        htm.sDiv("center");
        htm.sH(2).add("Testing Center").eH(2);

        htm.sDiv("buttonstack");

        htm.hr().div("vgap");
        htm.sH(3).add("Testing Center Calculators").eH(3);

        htm.addln("<form method='get' action='testing_issue_calc.html' style='display:inline-block; width:150px;'>",
                "<button class='nav'>Issue</button></form><form method='get' action='testing_collect_calc.html' ",
                "style='display:inline-block; width:150px;'><button class='nav'>Collect</button></form>");

        htm.hr().div("vgap");
        htm.sH(3).add("Testing Center & Quiet Testing").eH(3);

        htm.addln("<form method='get' action='testing_issue_exam.html'>");
        htm.add("<button class='nav'>Issue Exam</button>");
        htm.addln("</form>");

        htm.hr().div("vgap");

        htm.eDiv(); // buttonstack

        // spacer to prevent jumping when we go to a function
        htm.sDiv("buttonstack");
        htm.eDiv(); // buttonstack

        htm.eDiv(); // Center

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
