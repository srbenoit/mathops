package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.GenAdminSubsite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A page that provides a menu of pages with friendly formatted views of data in PROD schema tables.
 */
public enum PageDbAdminContextsProdViews {
    ;

    /**
     * Generates the page with the menu of view page.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final WebViewData data, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String driver = req.getParameter("driver");
        final String view = req.getParameter("view");

        if (AbstractSite.isParamInvalid(driver) || AbstractSite.isParamInvalid(view)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  driver='", driver, "'");
            Log.warning("  view='", view, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ContextMap map = ContextMap.getDefaultInstance();
            final LoginConfig cfg = map.getLogin(driver);

            if (cfg == null) {
                PageDbAdminContextsServer.doGet(data, site, req, resp, session, "Invalid database");
            } else {
                final Connection jdbc = GenAdminSubsite.getConnection(session.loginSessionId, driver);

                if (jdbc == null) {
                    PageDbAdminContextsServer.doGet(data, site, req, resp, session, null);
                } else {
                    final HtmlBuilder htm = GenAdminPage.startGenAdminPage(data, site, session, true);
                    htm.sH(2, "gray").add("Database Administration").eH(2);
                    htm.hr("orange");

                    htm.sH(3).add(driver).eH(3);

                    final String query = "driver=" + cfg.id;
                    PageDbAdmin.emitDisconnectLink(htm, query);
                    PageDbAdmin.emitCfgInfoTable(htm, cfg);
                    PageDbAdmin.emitProdNavMenu(htm, EAdmSubtopic.DB_PROD_VIEWS, query);

                    if (cfg.db.use == EDbUse.PROD) {
                        htm.addln("<nav>");
                        htm.add("<button");
                        if ("remote_mpe".equals(view)) {
                            htm.add(" class='nav8 selected'");
                        } else {
                            htm.add(" class='nav8'");
                        }
                        htm.addln("</nav>");

                        htm.hr().div("vgap");
                    } else {
                        htm.addln("<nav>");
                        // TODO: DEV views
                        htm.addln("</nav>");
                    }

                    final SystemData systemData = data.getSystemData();
                    Page.endOrdinaryPage(systemData, site, htm, true);

                    final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
                    AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
                }
            }
        }
    }
}
