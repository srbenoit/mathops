package dev.mathops.web.host.precalc.course;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a page with orientation videos.
 */
enum PageOrientation {
    ;

    /**
     * Generates a page with information on orientation.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null,
                Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        if (session != null && logic != null) {
            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
        }
        htm.sDiv("panelu");

        Page.emitFile(htm, "precalc_root_orientation_content.txt");

        htm.eDiv(); // panelu
        if (session != null && logic != null) {
            htm.eDiv(); // menupanelu
        }

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}
