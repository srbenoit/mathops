package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.sysadmin.ESysadminTopic;
import dev.mathops.web.site.admin.sysadmin.SysAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Page that provides administration of a database server.
 */
public enum PageDbSrvPrdAdm {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if (session.role == ERole.SYSADMIN) {

            final HtmlBuilder htm = SysAdminPage.startSysAdminPage(site, session);

            SysAdminPage.emitNavBlock(ESysadminTopic.DB_SERVERS, htm);
            emitPageContent(htm, req);

            SysAdminPage.endSysAdminPage(cache, htm, site, req, resp);
        } else {
            SysAdminPage.sendNotAuthorizedPage(cache, site, req, resp);
        }
    }

    /**
     * Emits the content of the page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param req the request
     */
    private static void emitPageContent(final HtmlBuilder htm, final ServletRequest req) {

        htm.sH(2).add("Database Server Administration").eH(2);

        final String hostname = req.getParameter("hostname");
        final EDbInstallationType product =
                EDbInstallationType.forName(req.getParameter("product"));
        final String version = DataDbInstalledProduct.sanitize(req.getParameter("version"));

        if (hostname == null || hostname.isEmpty()) {
            htm.sP().add("No server selected.").eP();
        } else if (product == null) {
            htm.sP().add("No product selected.").eP();
        } else {
            final String sanitized = DataDbServer.sanitize(hostname);
            final DataDbServer server = PageDb.getServer(sanitized);

            if (server == null) {
                htm.sP().add("No server found with hostname '", sanitized, "'").eP();
            } else {
                final List<DataDbInstalledProduct> products = server.products;
                DataDbInstalledProduct selected = null;
                for (final DataDbInstalledProduct prod : products) {
                    if (prod.product == product && prod.version.equals(version)) {
                        selected = prod;
                    }
                }

                if (selected == null) {
                    htm.sP().add("No installation of ", product.name, " version ", version,
                            " found for ", sanitized).eP();
                } else {
                    htm.sDiv("boxed");
                    htm.sP("center")
                            .add("<small><strong>", product.name,
                                    " version ", version, "</strong> installed on <strong>", sanitized,
                                    "</strong></small>")
                            .eP().hr();

                    // DBA functions
                    htm.add("<button class='nav6' ",
                            "onclick='pick(\"db_srv_prd_adm_dba.html?hostname=",
                            hostname, "&product=", selected.product.name,
                            "&version=", selected.version, "\");'>",
                            "Database Admin", "</button>");

                    // System admin functions
                    htm.add("<button class='nav6' ",
                            "onclick='pick(\"db_srv_prd_adm_sys.html?hostname=",
                            hostname, "&product=", selected.product.name,
                            "&version=", selected.version, "\");'>",
                            "System Admin", "</button>");

                    htm.eDiv();
                }

                htm.eDiv();
            }
        }
    }

}
