package dev.mathops.web.site.course;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page with instructions for accessing help.
 */
enum PageGettingHelp {
    ;

    /**
     * Generates a page with information on orientation.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final WebViewData data, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = site.getTitle();
        Page.startOrdinaryPage(htm, title, session, false, null, null, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(data, site, session, logic, htm);
        htm.sDiv("panelu");

        Page.emitFile(htm, "precalc_instr_getting_help.txt");

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        final SystemData systemData = data.getSystemData();
        Page.endOrdinaryPage(systemData, site, htm, true);

        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, bytes);
    }
}
