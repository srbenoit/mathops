package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.commons.builder.HtmlBuilder;
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
 * Page that confirms deletion of a database server, then performs the delete on POST.
 */
public enum PageDbSrvDel {
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
                PageDb.emitServersNav(htm, server, false);

                htm.div("vgap2");
                htm.sDiv("box");

                htm.addln("<form action='db_srv_del.html' method='post'>");
                htm.add("<input type='hidden' name='hostname' value='",
                        sanitized, "'/>");

                htm.sP("center")
                        .add("<img src='/images/dialog-warning-2.png' ",
                                "style='height:30px;position:relative; top:7px;'/>",
                                " <strong>Delete the configuration for database server ", sanitized,
                                "?</strong>")
                        .eP();

                htm.sP("center")
                        .add("<input type='submit' value='Yes, delete the database server'/>").eP();

                htm.addln("</form>");

                htm.sP("center")
                        .add("<button onclick='window.location.href=\"db_srv.html?hostname=",
                                sanitized, "\"';>", "No, do not delete</button>")
                        .eP();

                htm.eDiv();
            }
        }
    }

    /**
     * Processes a confirmation of deletion of a server.
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
            if (hostname != null && !hostname.isEmpty()) {
                final String sanitized = DataDbServer.sanitize(hostname);
                PageDb.deleteServer(sanitized);
            }

            // Return to the list of servers.
            final String path = site.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) //
                    ? "db.html" : "/db.html"));
        } else {
            SysAdminPage.sendNotAuthorizedPage(cache, site, req, resp);
        }
    }
}
