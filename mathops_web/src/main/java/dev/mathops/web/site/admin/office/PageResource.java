package dev.mathops.web.site.admin.office;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A page to manage resources.
 */
enum PageResource {
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

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

        htm.sDiv("center");
        htm.sH(2).add("Resource Loan and Return").eH(2);
        htm.div("vgap2");

        htm.sDiv("buttonstack");
        htm.addln("<form method='get' action='resource_loan.html'>");
        htm.add("<button class='nav'>Loan Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_return.html'>");
        htm.add("<button class='nav'>Return Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_check.html'>");
        htm.add("<button class='nav'>Check Student Loans</button>");
        htm.addln("</form>");

        htm.eDiv(); // buttonstack

        // spacer to prevent jumping when we go to a function
        htm.sDiv("buttonstack");
        htm.eDiv(); // buttonstack

        htm.eDiv(); // Center

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
