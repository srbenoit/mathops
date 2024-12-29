package dev.mathops.web.site.admin.genadmin.siteadmin;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The Website Administration page.
 */
public enum PageSiteAdmin {
    ;

    /**
     * Generates the website administration page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.SITE_ADMIN, htm);
        htm.sH(1).add("Web Site Administration").eH(1);

        emitNavMenu(htm, null);
        htm.hr().div("vgap");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the server administration navigation sub-menu with an optional selected item and query string to append to
     * each button link.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the subtopic to show as selected
     */
    private static void emitNavMenu(final HtmlBuilder htm, final EAdmSubtopic selected) {

        htm.addln("<nav>");

        // TODO:
        // navButtonSmall(htm, selected, EAdmSubtopic.SRV_SESSIONS, null);
        // navButtonSmall(htm, selected, EAdmSubtopic.SRV_DIAGNOSTICS, null);

        htm.addln("</nav>");
    }
}
