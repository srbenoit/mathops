package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A page with a menu of available database servers.
 */
public enum PageDbAdmin {
    ;

    /**
     * Generates the database administration page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doDbAdminPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(site, session, true);
        htm.sH(2, "gray").add("Database Administration").eH(2);
        htm.hr("orange");

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
    static void emitNavMenu(final HtmlBuilder htm, final EAdmSubtopic selected) {

        htm.addln("<nav>");
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_CONTEXTS, null);
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_BATCH, null);
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_REPORTS, null);
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_QUERIES, null);
        htm.addln("</nav>");
    }

    /**
     * Emits the database navigation sub-menu with an optional selected item and query string to append to each button
     * link.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the subtopic to show as selected
     * @param query    the query string, which should be "driver=xxx", with 'xxx' replaced by the name of the active
     *                 driver
     */
    static void emitProdNavMenu(final HtmlBuilder htm, final EAdmSubtopic selected,
                                final String query) {

        htm.addln("<nav>");
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_META, query);
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_PROD_VIEWS, query);
        htm.addln("</nav>");
        htm.hr();
    }

    /**
     * Emits the database navigation sub-menu with an optional selected item and query string to append to each button
     * link.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the subtopic to show as selected
     * @param query    the query string, which should be "driver=xxx", with 'xxx' replaced by the name of the active
     *                 driver
     */
    static void emitLiveNavMenu(final HtmlBuilder htm, final EAdmSubtopic selected,
                                final String query) {

        htm.addln("<nav>");
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_META, query);
        htm.addln("</nav>");
        htm.hr();
    }

    /**
     * Emits the database navigation sub-menu with an optional selected item and query string to append to each button
     * link.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the subtopic to show as selected
     * @param query    the query string, which should be "driver=xxx", with 'xxx' replaced by the name of the active
     *                 driver
     */
    static void emitOdsNavMenu(final HtmlBuilder htm, final EAdmSubtopic selected, final String query) {

        htm.addln("<nav>");
        GenAdminPage.navButtonSmall(htm, selected, EAdmSubtopic.DB_META, query);
        htm.addln("</nav>");
        htm.hr();
    }

    /**
     * Emits a link to disconnect from the current database.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param query the query string, which should be "driver=xxx", with 'xxx' replaced by the name of the connected
     *              driver
     */
    static void emitDisconnectLink(final HtmlBuilder htm, final String query) {

        htm.sP().add("<a href='db_admin_server_logout.html?", query,
                "'>Disconnect</a>").eP();
    }

    /**
     * Emits a table with basic information about the database configuration.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param cfg the driver configuration
     */
    static void emitCfgInfoTable(final HtmlBuilder htm, final LoginConfig cfg) {

        htm.addln("<table class='report'>");
        htm.addln("<tr>");
        htm.addln("<td>",
                "Type:<br/><b>", cfg.db.server.type.name, //
                "</b></td>");
        htm.addln("<td>",
                "Host:<br/><b>", cfg.db.server.host, CoreConstants.COLON,
                Integer.toString(cfg.db.server.port), "</b></td>");
        if (cfg.db.id != null) {
            htm.addln("<td>",
                    "Database:<br/><b>", cfg.db.id, "</b></td>");
        }
        if (cfg.db.server.name != null) {
            htm.addln("<td>",
                    "Server:<br/><b>", cfg.db.server.name, "</b></td>");
        }
        htm.addln("<td>",
                "Use:<br/><b>", cfg.db.use, "</b></td>");
        htm.addln("</tr>");
        htm.addln("</table>");
    }
}
