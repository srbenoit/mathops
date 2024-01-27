package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.rawlogic.AbstractLogicModule;
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
import java.util.Arrays;

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
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
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
            AbstractLogicModule.indicateBannerDownIndefinitely();
        } else {
            AbstractLogicModule.indicateBannerUp();
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
        if (!AbstractLogicModule.isBannerDown()) {
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

        final ContextMap map = ContextMap.getDefaultInstance();

        // List contexts by host and path
        final String[] hosts = map.getWebHosts();
        Arrays.sort(hosts, null);

        for (final String host : hosts) {
            htm.sP().add(host).eP();
            htm.sTable("report");
            htm.sTr();
            htm.sTh().add("Path").eTh();
            htm.sTh().add("Primary Server").eTh();
            htm.sTh().add("ODS Server").eTh();
            htm.sTh().add("Live Server").eTh();
            htm.eTr();

            final String[] paths = map.getWebSites(host);
            if (paths != null) {
                Arrays.sort(paths, null);
                for (final String path : paths) {

                    final WebSiteProfile siteProfile = map.getWebSiteProfile(host, path);
                    if (siteProfile != null) {
                        htm.sTr();
                        htm.sTd().add(path).eTd();

                        final DbContext pri = siteProfile.dbProfile.getDbContext(ESchemaUse.PRIMARY);
                        final LoginConfig pd = pri.loginConfig;
                        htm.sTd().add("<a class='ulink' href='db_admin_server.html?driver=",
                                pd.id.replace(' ', '+'), "'>", pd.id, "</a>").eTd();

                        final DbContext ods = siteProfile.dbProfile.getDbContext(ESchemaUse.ODS);
                        final LoginConfig od = ods.loginConfig;
                        htm.sTd().add("<a class='ulink' href='db_admin_server.html?driver=",
                                od.id.replace(' ', '+'), "'>", od.id, "</a>").eTd();

                        final DbContext live = siteProfile.dbProfile.getDbContext(ESchemaUse.LIVE);
                        final LoginConfig ld = live.loginConfig;
                        htm.sTd().add("<a class='ulink' href='db_admin_server.html?driver=",
                                ld.id.replace(' ', '+'), "'>", ld.id, "</a>").eTd();

                        htm.eTr();
                    }
                }
            }
            htm.eTable();
        }

        htm.sH(2).add("Available Databases").eH(2);

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("Server").eTh();
        htm.sTh().add("Type").eTh();
        htm.sTh().add("Host").eTh();
        htm.sTh().add("Database").eTh();
        htm.eTr();

        final String[] names = map.getLoginIDs();
        Arrays.sort(names);
        for (final String name : names) {
            final LoginConfig cfg = map.getLogin(name);

            htm.sTd().add("<a class='ulink' href='db_admin_server.html?driver=",
                    name.replace(' ', '+'), "'>", name, "</a>").eTd();
            htm.sTd().add(cfg.db.server.type.name()).eTd();
            htm.sTd().add(cfg.db.server.host, CoreConstants.COLON, //
                    Integer.toString(cfg.db.server.port)).eTd();
            htm.sTd().add(cfg.db.id).eTd();
            htm.eTr();
        }
        htm.eTable();
    }
}
