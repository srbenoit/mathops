package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.db.Cache;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Server;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.old.rawlogic.LogicUtils;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * The "Contexts" sub-page of the Database Administration page.
 */
public enum PageDbAdminContexts {
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
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);
        htm.sH(2, "gray").add("Database Administration").eH(2);
        htm.hr("orange");

        PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_CONTEXTS);
        doPageContent(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Processes a POST from the "banner" checkbox form.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String bannerup = req.getParameter("bannerup");

        if (bannerup == null) {
            LogicUtils.indicateBannerDownIndefinitely();
        } else {
            LogicUtils.indicateBannerUp();
        }

        doGet(cache, site, req, resp, session);
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void doPageContent(final HtmlBuilder htm) {

        htm.div("vgap0").hr().div("vgap0");

        htm.sH(2).add("BANNER Access").eH(2);

        htm.addln("<form action='dbadm_update_banner.html' method='post'>");

        htm.sP();
        htm.add("<input type='checkbox' id='bannerup' name='bannerup' value='x'");
        if (!LogicUtils.isBannerDown()) {
            htm.add(" checked");
        }
        htm.add("> &nbsp; ");
        htm.add("Use BANNER for live data queries and updates");
        htm.eP();

        htm.sP().add("Uncheck this box when BANNER system is down.").eP();
        htm.addln("<input type='submit' value='Update'>");
        htm.addln("</form>");

        htm.div("vgap0").hr().div("vgap0");

        htm.sH(2).add("Contexts and Servers").eH(2);

        final DatabaseConfig dbConfig = DatabaseConfig.getDefault();

        // List contexts by host and path
        final List<String> hosts = dbConfig.getWebHosts();
        hosts.sort(null);

        for (final String host : hosts) {

            htm.sP().add(host).eP();
            htm.sTable("report");
            htm.sTr();
            htm.sTh().add("Path").eTh();
            htm.sTh().add("Profile").eTh();
            htm.sTh().add("Legacy<br/>Facet").eTh();
            htm.sTh().add("System<br/>Facet").eTh();
            htm.sTh().add("Main<br/>Facet").eTh();
            htm.sTh().add("Extern<br/>Facet").eTh();
            htm.sTh().add("Analytics<br/>Facet").eTh();
            htm.sTh().add("ODS<br/>Facet").eTh();
            htm.sTh().add("Live<br/>Facet").eTh();
            htm.eTr();

            final List<String> paths = dbConfig.getWebSites(host);
            if (paths != null) {
                paths.sort(null);

                for (final String path : paths) {
                    final Site site = dbConfig.getSite(host, path);

                    if (site != null) {
                        final Profile profile = site.profile;

                        htm.sTr();
                        htm.sTd().add(path).eTd();
                        htm.sTd().add(profile.id).eTd();
                        final Facet legacy = profile.getFacet(ESchema.LEGACY);
                        emitFacetCell(legacy, htm);
                        final Facet system = profile.getFacet(ESchema.SYSTEM);
                        emitFacetCell(system, htm);
                        final Facet main = profile.getFacet(ESchema.MAIN);
                        emitFacetCell(main, htm);
                        final Facet extern = profile.getFacet(ESchema.EXTERN);
                        emitFacetCell(extern, htm);
                        final Facet analytics = profile.getFacet(ESchema.ANALYTICS);
                        emitFacetCell(analytics, htm);
                        final Facet ods = profile.getFacet(ESchema.ODS);
                        emitFacetCell(ods, htm);
                        final Facet live = profile.getFacet(ESchema.LIVE);
                        emitFacetCell(live, htm);
                        htm.eTr();
                    }
                }
            }
            htm.eTable();
        }

        htm.sH(2).add("Available Databases").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Server Type").eTh();
        htm.sTh().add("Host").eTh();
        htm.sTh().add("Database").eTh();
        htm.sTh().add("Instance").eTh();
        htm.eTr();

        final List<Server> servers = dbConfig.getServers();
        servers.sort(null);

        for (final Server server : servers) {
            boolean startServer = true;

            final List<Database> databases = server.getDatabases();
            if (!databases.isEmpty()) {
                databases.sort(null);

                final String rowSpan = "rowSpan='" + databases.size() + "'";
                final String hostStr = server.host + ":" + server.port;

                for (final Database database : databases) {
                    htm.sTr();
                    if (startServer) {
                        htm.sTd(null, rowSpan).add(server.type).eTd();
                        htm.sTd(null, rowSpan).add(hostStr).eTd();
                        startServer = false;
                    }
                    htm.sTd().add(database.id).eTd();
                    if (database.instance == null) {
                        htm.sTd().add("-").eTd();
                    } else {
                        htm.sTd().add(database.instance).eTd();
                    }
                    htm.eTr();
                }
            }
        }
        htm.eTable();
    }

    /**
     * Emits a table cell with a link for a facet.
     *
     * @param facet the facet
     * @param htm   the {@code HtmlBuilder} to which to append
     */
    private static void emitFacetCell(final Facet facet, final HtmlBuilder htm) {

        if (facet == null) {
            htm.sTd().add("-").eTd();
        } else {
            htm.sTd().add("<a class='ulink' href='db_admin_server.html?login=", facet.login.id,
                    "&data=", facet.data.id, "'>", facet.login.id, "</a>").eTd();
        }
    }
}
