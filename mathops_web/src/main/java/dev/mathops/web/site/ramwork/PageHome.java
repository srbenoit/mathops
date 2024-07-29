package dev.mathops.web.site.ramwork;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        if (role.canActAs(ERole.ADMINISTRATOR)) {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, true);

            htm.hr();

            htm.sP().add("<a href='itembank.html'>Item Bank</a>").eP();
            htm.sP().add("<a href='qtiitembank.html'>QTI Item Bank</a>").eP();
            htm.sP().add("<a href='mathrefresherlibrary.html'>CSU Math Refresher Library</a>",
                    " (used by course owners)").eP();
            htm.sP().add("<a href='mathrefresherstudent.html'>CSU Math Refresher Student View</a>",
                    " (used by students)").eP();
            htm.sP().add("<a href='assessmentdev.html'>Assessment System Development</a>").eP();
            htm.sP().add("<a href='authoring.html'>Item Authoring Resources</a>").eP();

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        } else if (role.canActAs(ERole.OFFICE_STAFF)) {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, true);

            htm.sP().add("<a href='authoring.html'>Item Authoring Resources</a>").eP();

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, true);

            htm.sP().add("<a href='authoring.html'>Item Authoring Resources</a>").eP();

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
