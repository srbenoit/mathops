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
import java.sql.SQLException;

/**
 * The home page.
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
     * @param session the user session
     * @param error   an optional error message to show
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session, final String error) throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, false);

        htm.addln("<form id='search' class='stuform' name='pick-student' method='post' action='student_pick.html'>");
        htm.sP().add("Student:").eP();
        htm.hr();

        htm.sP().add("Enter student ID or name:").eP();
        htm.sP().add("<input type='text' autocomplete='off' data-lpignore='true' ",
                "name='pick_stu' size='24' autofocus/>").eP();

        if (error != null) {
            htm.sP("center error").add(error).eP();
        }

        htm.eTable().hr();

        htm.sP("small", "style='text-align:left'")
                .add("'<code>%</code>' can be used as a wildcard in names.  For example, 'A%' will ",
                        "match all names beginning with the letter A.").eP();

        htm.div("vgap");
        htm.addln(" <button class='btn' type='submit'>Submit</button>");
        htm.addln("</form>");

        htm.sDiv("buttonstack");
        htm.addln("<form method='get' action='resource.html'>");
        htm.add("<button class='nav'>Resource Loan and Return</button>");
        htm.addln("</form>");
        htm.addln("<form method='get' action='testing.html'>");
        htm.add("<button class='nav'>Testing Center</button>");
        htm.addln("</form>");
        htm.addln("<form method='get' action='proctoring.html'>");
        htm.add("<button class='nav'>Proctoring</button>");
        htm.addln("</form>");
        htm.eDiv(); // buttonstack

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
