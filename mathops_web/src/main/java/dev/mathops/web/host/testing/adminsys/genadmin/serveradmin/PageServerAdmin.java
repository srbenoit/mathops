package dev.mathops.web.host.testing.adminsys.genadmin.serveradmin;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdmSubtopic;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The Server Administration page.
 */
public enum PageServerAdmin {
    ;

    /**
     * Generates the server administration page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doServerAdminPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.SERVER_ADMIN, htm);
        htm.sH(1).add("Server Administration").eH(1);

        emitNavMenu(htm, null);
        htm.hr().div("vgap");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the server administration navigation sub-menu with an optional selected item and query string to append to
     * each button link.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the subtopic to show as selected
     */
    static void emitNavMenu(final HtmlBuilder htm, final EAdmSubtopic selected) {

        htm.addln("<nav>");

        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.SRV_SESSIONS, null);
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.SRV_MAINTENANCE, null);
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.SRV_CONTROL, null);
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.SRV_DIAGNOSTICS, null);

        htm.addln("</nav>");
    }
}
