package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
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
 * Page that gathers data and creates a new database server.
 */
public enum PageDbSrvAdd {
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
            emitPageContent(htm, null, null, null);
            PageDb.emitXmlFile(htm);

            SysAdminPage.endSysAdminPage(cache, htm, site, req, resp);
        } else {
            SysAdminPage.sendNotAuthorizedPage(cache, site, req, resp);
        }
    }

    /**
     * Emits the content of the page.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param name     the value with which to pre-populate the name field
     * @param hostname the value with which to pre-populate the hostname field
     * @param error    optional error message
     */
    private static void emitPageContent(final HtmlBuilder htm, final CharSequence name,
                                        final CharSequence hostname, final String error) {

        htm.sH(2).add("Add a Database Server").eH(2);

        htm.addln("<form class='form' action='db_srv_add.html' method='post'>");

        htm.sDiv();
        htm.sDiv("formfield").add("<input name='server_name' type='text'");
        if (name != null) {
            htm.add(" value='", DataDbServer.sanitize(name), "'");
        }
        htm.add("/>").eDiv();
        htm.sDiv("formlabel")
                .add("<label for='server_name'>Server Name</label>").eDiv();
        htm.eDiv();

        htm.sDiv();
        htm.sDiv("formfield")
                .add("<input name='server_hostname' type='text'");
        if (hostname != null) {
            htm.add(" value='", DataDbServer.sanitize(hostname), "'");
        }
        htm.add("/>").eDiv();
        htm.sDiv("formlabel")
                .add("<label for='server_hostname'>Fully Qualified Hostname</label>").eDiv();
        htm.eDiv();

        if (error != null) {
            htm.div("vgap");
            htm.sP(null, "style='color:red; text-align:center;'").addln(error).eP();
        }

        htm.sP("center").add("<input type='submit'/>").eP();

        htm.addln("</form>");
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

            final String name = req.getParameter("server_name");
            final String hostname = req.getParameter("server_hostname");

            final String error;

            if (name == null || name.isEmpty()) {
                error = "Name must be provided.";
            } else if (hostname == null || hostname.isEmpty()) {
                error = "Hostname must be provided.";
            } else {
                error = null;
            }

            if (error == null) {
                PageDb.addServer(new DataDbServer(name, hostname));
                final String path = site.siteProfile.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) //
                        ? "db.html" : "/db.html"));
            } else {
                // Re-display the form with an error message
                final HtmlBuilder htm = SysAdminPage.startSysAdminPage(site, session);

                SysAdminPage.emitNavBlock(ESysadminTopic.DB_SERVERS, htm);
                emitPageContent(htm, name, hostname, error);

                SysAdminPage.endSysAdminPage(cache, htm, site, req, resp);
            }

        } else {
            SysAdminPage.sendNotAuthorizedPage(cache, site, req, resp);
        }
    }
}
