package dev.mathops.web.host.testing.adminsys.genadmin.dbadmin;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Server;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdmSubtopic;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A page that displays information on a database server and its tables.
 */
public enum PageDbAdminContextsServer {
    ;

    /** Suffix for fields containing usernames. */
    private static final String USERNAME_SUFFIX = "pOjFsohF4z";

    /** Suffix for fields containing passwords. */
    private static final String PASSWORD_SUFFIX = "WikDBnwkAs";

    /**
     * Generates the database administration server page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @param error   an optional error message
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site,
                             final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session, final String error) throws IOException, SQLException {

        final String loginId = req.getParameter("login");
        final String dataId = req.getParameter("data");

        if (AbstractSite.isParamInvalid(loginId) || AbstractSite.isParamInvalid(dataId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  login='", loginId, "'");
            Log.warning("  data='", dataId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final DatabaseConfig dbConfig = DatabaseConfig.getDefault();
            final Login loginObj = dbConfig.getLogin(loginId);
            final Data dataObj = dbConfig.getData(dataId);

            if (loginObj == null || dataObj == null) {
                PageDbAdminContexts.doGet(cache, site, req, resp, session);
            } else {
                final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

                GenAdminPage.emitNavBlock(EAdminTopic.DB_ADMIN, htm);
                htm.sH(1).add("Database Administration").eH(1);

                PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_CONTEXTS);
                doPageContent(htm, loginObj, dataObj);

                Page.endOrdinaryPage(cache, site, htm, true);
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
            }
        }
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm      the {@code HtmlBuilder} to which to write
     * @param loginObj the login object
     * @param dataObj  the data object
     */
    private static void doPageContent(final HtmlBuilder htm, final Login loginObj, final Data dataObj) {

        final Database database = loginObj.database;
        final Server server = database.server;

        htm.div("vgap0").hr();

        htm.sP().add("Server and Database Configuration").eP();
        htm.sTable("report");
        htm.sTr().sTh().add("Product Type").eTh().sTh().add("Host").eTh().sTh().add("Port").eTh()
                .sTh().add("Database ID").eTh().sTh().add("Instance").eTh().sTh().add("DBA").eTh().eTr();
        htm.sTr().sTd().add(server.type.name).eTd().sTd().add(server.host).eTd().sTd().add(server.port).eTd()
                .sTd().add(database.id).eTd().sTd().add(database.instance).eTd().sTd().add(database.dba).eTd().eTr();
        htm.eTable();

        htm.sP().add("Login and Data Facet Configuration").eP();
        htm.sTable("report");
        htm.sTr().sTh().add("Login ID").eTh().sTh().add("Username").eTh().sTh().add("Data ID").eTh()
                .sTh().add("Schema").eTh().sTh().add("Use").eTh().sTh().add("Prefix").eTh().eTr();
        htm.sTr().sTd().add(loginObj.id).eTd().sTd().add(loginObj.user).eTd().sTd().add(dataObj.id).eTd()
                .sTd().add(dataObj.schema).eTd().sTd().add(dataObj.use).eTd().sTd().add(dataObj.prefix).eTd().eTr();
        htm.eTable();

        htm.sP().add("Available Tables:").eP();
        htm.sTable("report");
        htm.sTr().sTh().add("Schema Name").eTh().sTh().add("Table Name").eTh().eTr();
        final DbConnection conn = loginObj.checkOutConnection();
        try {
            final DatabaseMetaData meta = conn.getConnection().getMetaData();
            final ResultSet rs = meta.getTables(null, null, null, null);
            while (rs.next()) {
                final String schema = rs.getString("TABLE_SCHEM");
                final String name = rs.getString("TABLE_NAME");
                if (dataObj.prefix == null || dataObj.prefix.equals(schema)) {
                    htm.sTr().sTd().add(schema).eTd().sTd().add(name).eTd().eTr();
                }
            }
        } catch (final SQLException ex) {
            htm.sP().add("ERROR accessing tables: ", ex.getMessage()).eP();
        } finally {
            loginObj.checkInConnection(conn);
        }
        htm.eTable();
    }
}
