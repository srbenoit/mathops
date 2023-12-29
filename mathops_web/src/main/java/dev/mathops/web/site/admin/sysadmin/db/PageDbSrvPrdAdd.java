package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
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

/**
 * Page that gathers data and adds a database product to a database server.
 */
public enum PageDbSrvPrdAdd {
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
            final HtmlBuilder htm = SysAdminPage.startSysAdminPage(cache, site, session);

            SysAdminPage.emitNavBlock(ESysadminTopic.DB_SERVERS, htm);
            emitPageContent(htm, req, null);
            PageDb.emitXmlFile(htm);

            SysAdminPage.endSysAdminPage(cache, htm, site, req, resp);
        } else {
            SysAdminPage.sendNotAuthorizedPage(cache, site, req, resp);
        }
    }

    /**
     * Emits the content of the page.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param req   the request
     * @param error optional error message
     */
    private static void emitPageContent(final HtmlBuilder htm, final ServletRequest req,
                                        final String error) {

        htm.sH(2).add("Database Servers").eH(2);

        final String hostname = req.getParameter("hostname");
        if (hostname == null || hostname.isEmpty()) {
            PageDb.emitServersNav(htm, null, true);
            htm.sP().add("No server selected.").eP();
        } else {
            final String sanitized = DataDbServer.sanitize(hostname);
            final DataDbServer server = PageDb.getServer(sanitized);

            if (server == null) {
                PageDb.emitServersNav(htm, null, true);
                htm.sP().add("No server found with hostname '", sanitized, "'").eP();
            } else {
                PageDb.emitServersNav(htm, server, true);

                htm.sDiv("boxed");
                htm.sP("center").add("<small><strong>",
                        sanitized, "</strong><small>").eP();

                htm.sH(3).add("Add Database Product").eH(3);

                final EDbProduct product =
                        EDbProduct.forName(req.getParameter("product"));
                final String version = req.getParameter("version");

                htm.addln("<form class='form' action='db_servers_add_product.html' method='post'>");
                htm.addln("<input type='hidden' name='hostname' value='",
                        sanitized, "'/>");

                htm.sDiv();
                htm.sDiv("formfield").add("<select name='product'>");
                for (final EDbProduct item : EDbProduct.values()) {
                    htm.add("<option value='", item.name, "'");
                    if (product == item) {
                        htm.add(" selected");
                    }
                    htm.add(">", item.name, "</option>");

                }
                htm.add("</select>").eDiv();
                htm.sDiv("formlabel")
                        .add("<label for='product'>Database Product</label>").eDiv();
                htm.eDiv();

                htm.sDiv();
                htm.sDiv("formfield")
                        .add("<input name='version' type='text'");
                if (version != null) {
                    htm.add(" value='", DataDbInstalledProduct.sanitize(version),
                            "'");
                }
                htm.add("/>").eDiv();
                htm.sDiv("formlabel")
                        .add("<label for='version'>Version</label>").eDiv();
                htm.eDiv();

                if (error != null) {
                    htm.div("vgap");
                    htm.sP(null, "style='color:red; text-align:center;'")
                            .addln(error).eP();
                }

                htm.sP("center").add("<input type='submit'/>").eP();

                htm.addln("</form>");

                htm.eDiv();

                // Button to delete server
                htm.add("<button id='add' class='nav6'");
                htm.add(" onclick='pick(\"db_srv_del.html?hostname=",
                        sanitized, "\");'>",
                        "Delete Server", "</button>");
            }
        }
    }

    /**
     * Processes a POST from the create form.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if (session.role == ERole.SYSADMIN) {

            final String hostname = req.getParameter("hostname");
            final String sanitized = DataDbServer.sanitize(hostname);

            DataDbServer server = null;
            String error = null;

            if (hostname == null || hostname.isEmpty()) {
                error = "Hostname must be provided.";
            } else {
                server = PageDb.getServer(sanitized);
                if (server == null) {
                    error = "Hostname must be provided.";
                }
            }

            if (server != null) {
                final EDbProduct product =
                        EDbProduct.forName(req.getParameter("product"));
                final String version = req.getParameter("version");

                if (product == null) {
                } else {
                }

                PageDb.addProductServer(server, new DataDbInstalledProduct(product, version));

                final String path = site.siteProfile.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH)
                        ? "db_srv.html?hostname=" + sanitized
                        : "/db_srv.html?hostname=" + sanitized));
            } else {
                // Re-display the form with an error message
                final HtmlBuilder htm = SysAdminPage.startSysAdminPage(cache, site, session);

                SysAdminPage.emitNavBlock(ESysadminTopic.DB_SERVERS, htm);
                emitPageContent(htm, req, error);

                SysAdminPage.endSysAdminPage(cache, htm, site, req, resp);
            }

        } else {
            SysAdminPage.sendNotAuthorizedPage(cache, site, req, resp);
        }
    }
}
