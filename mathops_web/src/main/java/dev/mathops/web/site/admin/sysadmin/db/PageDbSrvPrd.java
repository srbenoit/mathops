package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.EDbInstallationType;
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
 * Page that displays a single product installed on a database server, with management options.
 */
public enum PageDbSrvPrd {
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
            PageDb.emitXmlFile(htm);

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

        htm.sH(2).add("Database Servers").eH(2);

        final String hostname = req.getParameter("hostname");
        final EDbInstallationType product =
                EDbInstallationType.forName(req.getParameter("product"));
        final String version = DataDbInstalledProduct.sanitize(req.getParameter("version"));

        if (hostname == null || hostname.isEmpty()) {
            PageDb.emitServersNav(htm, null, true);
            htm.sP().add("No server selected.").eP();
        } else if (product == null) {
            PageDb.emitServersNav(htm, null, true);
            htm.sP().add("No product selected.").eP();
        } else {
            final String sanitized = DataDbServer.sanitize(hostname);
            final DataDbServer server = PageDb.getServer(sanitized);

            if (server == null) {
                PageDb.emitServersNav(htm, null, true);
                htm.sP().add("No server found with hostname '", sanitized, "'").eP();
            } else {
                PageDb.emitServersNav(htm, server, true);

                htm.sDiv("boxed");
                htm.sP("center").add("<small><strong>", sanitized, "</strong></small>").eP();

                htm.sH(3).add("Database Products Installed").eH(3);

                // List the installed database products

                final List<DataDbInstalledProduct> products = server.products;
                DataDbInstalledProduct selected = null;

                for (final DataDbInstalledProduct prod : products) {
                    final EDbInstallationType type = prod.product;

                    if (prod.product == product && prod.version.equals(version)) {
                        htm.add("<button class='nav6 selected'");
                        selected = prod;
                    } else {
                        htm.add("<button class='nav6'");
                    }

                    htm.add(" onclick='pick(\"db_srv_prd.html?hostname=", hostname, "&product=", type.name,
                            "&version=", prod.version, "\");'>", type.name, "</button>");
                }

                // Button to add a new product
                htm.add("<button id='add' class='nav6'");
                htm.add(" onclick='pick(\"db_srv_prd_add.html?hostname=", sanitized, "\");'>Add Product</button>");

                if (selected != null) {

                    htm.sDiv("boxed2");

                    htm.sP("center").add("<small><strong>", selected.product.name, " version ",
                            selected.version, "</strong></small>").eP();

                    // Button to administer
                    htm.hr().add("<button id='add' class='nav6'");
                    htm.add(" onclick='pick(\"db_srv_prd_adm.html?hostname=", sanitized, "&product=",
                            selected.product.name, "&version=", selected.version, "\");'>Administer</button>");

                    // Button to delete product
                    htm.add("<button id='add' class='nav6'");
                    htm.add(" onclick='pick(\"db_srv_prd_del.html?hostname=", sanitized, "&product=",
                            selected.product.name, "&version=", selected.version, "\");'>Delete Product</button>");

                    htm.eDiv();

                    // Button to delete server
                    htm.hr().add("<button id='add' class='nav6'");
                    htm.add(" onclick='pick(\"db_srv_del.html?hostname=", sanitized, "\");'>Delete Server</button>");
                }

                htm.eDiv();
            }
        }
    }
}
