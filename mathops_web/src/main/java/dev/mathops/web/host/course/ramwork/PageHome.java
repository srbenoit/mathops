package dev.mathops.web.host.course.ramwork;

import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the home page.
 */
enum PageHome {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final RamWorkSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final ERole role = session.getEffectiveRole();
        final String siteTitle = Res.get(Res.SITE_TITLE);
        final HtmlBuilder htm = new HtmlBuilder(2000);

        if (role.canActAs(ERole.ADMINISTRATOR)) {
            Page.startOrdinaryPage(htm, siteTitle, session, false, Page.ADMIN_BAR, null, false, true);

            htm.hr();

            htm.sP().add("<a href='itembank.html'>Item Bank</a>").eP();
            htm.sP().add("<a href='authoring.html'>Item Authoring Resources</a>").eP();

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        } else if (role.canActAs(ERole.OFFICE_STAFF)) {
            Page.startOrdinaryPage(htm, siteTitle, session, false, Page.ADMIN_BAR, null, false, true);

            htm.sP().add("<a href='authoring.html'>Item Authoring Resources</a>").eP();

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        } else {
            Page.startOrdinaryPage(htm, siteTitle, session, false, Page.ADMIN_BAR, null, false, true);

            htm.sP().add("<a href='authoring.html'>Item Authoring Resources</a>").eP();

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }
}
