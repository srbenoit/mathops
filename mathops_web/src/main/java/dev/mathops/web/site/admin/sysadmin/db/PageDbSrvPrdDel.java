package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.Contexts;
import dev.mathops.db.EDbProduct;
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
 * Page that confirms deletion of a product installed on a database server, then performs the delete on POST.
 */
public enum PageDbSrvPrdDel {
    ;

    /**
     * Generates the page.
     *
     * @param studentData the student data object
     * @param site        the owning site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final StudentData studentData, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if (session.role == ERole.SYSADMIN) {

            final HtmlBuilder htm = SysAdminPage.startSysAdminPage(studentData, site, session);

            SysAdminPage.emitNavBlock(ESysadminTopic.DB_SERVERS, htm);
            emitPageContent(htm, req);
            PageDb.emitXmlFile(htm);

            SysAdminPage.endSysAdminPage(studentData, htm, site, req, resp);
        } else {
            SysAdminPage.sendNotAuthorizedPage(studentData, site, req, resp);
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
        final EDbProduct product = EDbProduct.forName(req.getParameter("product"));
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
                    final EDbProduct type = prod.product;

                    if (prod.product == product && prod.version.equals(version)) {
                        htm.add("<button class='nav6 selected'");
                        selected = prod;
                    } else {
                        htm.add("<button class='nav6'");
                    }

                    htm.add(" onclick='pick(\"db_srv_prd.html?hostname=", hostname, "&product=", type.name, "&version=",
                            prod.version, "\");'>", type.name, "</button>");
                }

                // Button to add a new product
                htm.add("<button id='add' class='nav6'");
                htm.add(" onclick='pick(\"db_srv_prd_add.html?hostname=", sanitized, "\");'>", "Add Product",
                        "</button>");

                if (selected != null) {

                    htm.div("vgap2");
                    htm.sDiv("box");

                    htm.addln("<form action='db_srv_prd_del.html' method='post'>");
                    htm.add("<input type='hidden' name='hostname' value='", sanitized, "'/>");
                    htm.add("<input type='hidden' name='product' value='", selected.product.name, "'/>");
                    htm.add("<input type='hidden' name='version' value='", selected.version, "'/>");

                    htm.sP("center")
                            .add("<img src='/images/dialog-warning-2.png' ",
                                    "style='height:30px;position:relative; top:7px;'/>",
                                    " <strong>Delete the configuration for ", selected.product.name,
                                    " version ", selected.version, "?</strong>")
                            .eP();

                    htm.sP("center").add("<input type='submit' value='Yes, delete the installed product'/>").eP();

                    htm.addln("</form>");

                    htm.sP("center").add("<button onclick='window.location.href=\"db_srv_prd.html?hostname=",
                            sanitized, "&product=", selected.product.name, "&version=", selected.version,
                            "\"';>No, do not delete</button>").eP();

                    htm.eDiv();

                }

                htm.eDiv();
            }
        }
    }

    /**
     * Processes a confirmation of deletion of a server.
     *
     * @param studentData the student data object
     * @param site        the owning site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final StudentData studentData, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if (session.role == ERole.SYSADMIN) {

            final String hostname = req.getParameter("hostname");
            final String sanitized = DataDbServer.sanitize(hostname);
            final DataDbServer server = PageDb.getServer(sanitized);

            if (server == null) {
                // Return to the list of servers.
                final String path = site.siteProfile.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? "db.html" : "/db.html"));
            } else {
                final EDbProduct product = EDbProduct.forName(req.getParameter("product"));
                final String version = DataDbInstalledProduct.sanitize(req.getParameter("version"));
                PageDb.deleteProduct(server, product, version);

                final String path = site.siteProfile.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH)
                        ? "db_srv.html?hostname=" + sanitized
                        : "/db_srv.html?hostname=" + sanitized));
            }
        } else {
            SysAdminPage.sendNotAuthorizedPage(studentData, site, req, resp);
        }
    }
}
