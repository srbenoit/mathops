package dev.mathops.web.site.admin.genadmin.student;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Pages to let the user pick a student or group of students.
 */
public enum PageStudent {
    ;

    /**
     * Generates a page that allows the user to select a student by ID.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param error   an optional error message
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session, final String error)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

        htm.addln("<h1>Student Status</h1>");

        htm.sDiv("center");

        if (error != null) {
            htm.add("<span style='color:#CC5430;'>", error, "</span>").br();
        }

        //

        htm.addln("<form id='search' class='stuform' name='pick-student' method='post' action='student_pick.html'>");
        htm.sP().add("Search by student information:").eP();
        htm.hr();

        htm.sP().add("Enter student ID or name:").eP();
        htm.sP().add("<input type='text' autocomplete='off' data-lpignore='true' ",
                "name='pick_stu' size='24' autofocus/>").eP();
        htm.hr();

        htm.sP("small", "style='text-align:left'")
                .add("'<code>%</code>' can be used as a wildcard in names.  For example, 'A%' will ",
                        "match all names beginning with the letter A.")
                .eP();

        htm.div("vgap");
        htm.addln(" <input type='submit'/>");
        htm.addln("</form>");

        //

        htm.addln("<form id='search' class='stuform' name='pick-population' ",
                "method='post' action='population_pick.html'>");
        htm.sP().add("Search by status:").eP();
        htm.hr();

        htm.sTable();

        htm.sTr();
        htm.sTd("r").add("Course:").eTd();
        htm.sTd().add("<input type='text' autocomplete='off' data-lpignore='true' ",
                "name='pick_course' size='10'/>").eTd();
        htm.eTr();

        htm.sTr();
        htm.sTd("r").add("Section:").eTd();
        htm.sTd().add("<input type='text' autocomplete='off' data-lpignore='true' ",
                "name='pick_sect' size='10'/>").eTd();
        htm.eTr();

        htm.sTr();
        htm.sTd("r").add("Pace:").eTd();
        htm.sTd().add("<input type='text' autocomplete='off' data-lpignore='true' ",
                "name='pick_pace' size='10'/>").eTd();
        htm.eTr();

        htm.eTable().hr();

        htm.div("vgap");
        htm.addln(" <input type='submit'/>");
        htm.addln("</form>");

        htm.eDiv();

        htm.addln("</form>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
