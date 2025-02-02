package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.GenAdminSubsite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A page that displays metadata about a database connection.
 */
public enum PageDbAdminContextsTableMetadata {
    ;

    /**
     * Generates the database metadata page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String driver = req.getParameter("driver");
        final String cat = req.getParameter("cat");
        final String schema = req.getParameter("schema");
        final String table = req.getParameter("table");

        if (AbstractSite.isParamInvalid(driver) || AbstractSite.isParamInvalid(cat)
                || AbstractSite.isParamInvalid(schema) || AbstractSite.isParamInvalid(table)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  driver='", driver, "'");
            Log.warning("  cat='", cat, "'");
            Log.warning("  schema='", schema, "'");
            Log.warning("  table='", table, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ContextMap map = ContextMap.getDefaultInstance();
            final LoginConfig cfg = map.getLogin(driver);

            if (cfg == null) {
                PageDbAdminContextsServer.doGet(cache, site, req, resp, session, "Invalid database");
            } else {
                final Connection jdbc = GenAdminSubsite.getConnection(session.loginSessionId, driver);

                if (jdbc == null) {
                    PageDbAdminContextsServer.doGet(cache, site, req, resp, session, null);
                } else {
                    try {
                        final DatabaseMetaData meta = jdbc.getMetaData();

                        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);
                        htm.sH(2, "gray").add("Database Administration").eH(2);
                        htm.hr("orange");

                        final String query = "driver=" + cfg.id;

                        switch (cfg.db.use) {
                            case LIVE:
                                PageDbAdmin.emitLiveNavMenu(htm, EAdmSubtopic.DB_META, query);
                                break;

                            case ODS:
                                PageDbAdmin.emitOdsNavMenu(htm, EAdmSubtopic.DB_META, query);
                                break;

                            case PROD:
                            case DEV:
                            case TEST:
                            default:
                                PageDbAdmin.emitProdNavMenu(htm, EAdmSubtopic.DB_META, query);
                                break;
                        }

                        htm.div("vgap");

                        emitMetadata(htm, cat, schema, table, meta);

                        Page.endOrdinaryPage(cache, site, htm, true);
                        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                        GenAdminSubsite.removeConnection(session.loginSessionId, driver);
                        PageDbAdminContextsServer.doGet(cache, site, req, resp, session, null);
                    }
                }
            }
        }
    }

    /**
     * Emits a table of database metadata.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param cat    the catalog name
     * @param schema the schema name
     * @param table  the table name
     * @param meta   the database metadata object
     * @throws SQLException if there is an error querying the database
     */
    private static void emitMetadata(final HtmlBuilder htm, final String cat, final String schema,
                                     final String table, final DatabaseMetaData meta) throws SQLException {

        htm.addln("<h3>Table Metadata</h3>");

        htm.add("<h3>");
        if (!"null".equals(cat)) {
            htm.add(cat, CoreConstants.DOT);
        }
        if (!"null".equals(schema)) {
            htm.add(schema, CoreConstants.DOT);
        }
        htm.add(table, " Table</h3>");

        try (final ResultSet columns = meta.getColumns(null, null, table, null)) {
            if (columns.next()) {
                htm.addln("<table class='report'>");
                htm.sTr().sTh().add("Column").eTh()//
                        .sTh().add("Type").eTh()//
                        .sTh().add("Size").eTh()//
                        .sTh().add("Nulls").eTh()//
                        .eTr();

                do {
                    htm.sTr();
                    htm.sTd().add(columns.getString("COLUMN_NAME")).eTd();
                    htm.sTd().add(columns.getString("TYPE_NAME")).eTd();
                    htm.sTd().add(columns.getString("COLUMN_SIZE")).eTd();
                    htm.sTd().add(columns.getString("NULLABLE")).eTd();
                    htm.eTr();
                } while (columns.next());
            }
        }

        htm.eTable();
    }
}
