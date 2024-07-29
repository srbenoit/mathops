package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.GenAdminSubsite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A page that displays metadata about a database connection.
 */
public enum PageDbAdminContextsMetadata {
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

        if (AbstractSite.isParamInvalid(driver)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  driver='", driver, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ContextMap map = ContextMap.getDefaultInstance();
            final LoginConfig cfg = map.getLogin(driver);

            if (cfg == null) {
                PageDbAdminContextsServer.doGet(cache, site, req, resp, session,
                        "Invalid database");
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

                        htm.sH(3).add(driver).eH(3);

                        final String query = "driver=" + cfg.id;

                        PageDbAdmin.emitDisconnectLink(htm, query);
                        PageDbAdmin.emitCfgInfoTable(htm, cfg);

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
                            case ARCH:
                            default:
                                PageDbAdmin.emitProdNavMenu(htm, EAdmSubtopic.DB_META, query);
                                break;
                        }

                        htm.div("vgap");

                        emitMetadata(htm, cfg, driver, meta);

                        Page.endOrdinaryPage(cache, site, htm, true);
                        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML,
                                htm.toString().getBytes(StandardCharsets.UTF_8));

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
     * @param cfg    the driver configuration
     * @param driver the driver name
     * @param meta   the database metadata object
     * @throws SQLException if there is an error querying the database
     */
    private static void emitMetadata(final HtmlBuilder htm, final LoginConfig cfg,
                                     final String driver, final DatabaseMetaData meta) throws SQLException {

        htm.addln("<h3>Database Metadata</h3>");

        htm.addln("<table class='report'>");
        htm.sTr().sTh().add("Username").eTh().sTd().add(meta.getUserName()).eTd().eTr();
        htm.sTr().sTh().add("Product Name").eTh().sTd().add(meta.getDatabaseProductName()).eTd()
                .eTr();
        htm.sTr().sTh().add("Product Version").eTh().sTd().add(meta.getDatabaseProductVersion())
                .eTd().eTr();

        htm.sTr().sTh().add("Driver Name").eTh().sTd().add(meta.getDriverName()).eTd().eTr();
        htm.sTr().sTh().add("Driver Version").eTh().sTd().add(meta.getDriverVersion()).eTd().eTr();

        htm.sTr().sTh().add("JDBC Version").eTh().sTd().add(meta.getJDBCMajorVersion()).add('.')
                .add(meta.getJDBCMinorVersion()).eTd().eTr();
        htm.eTable();

        boolean hasCatalogs = false;
        try (final ResultSet catalogs = meta.getCatalogs()) {
            if (catalogs.next()) {
                hasCatalogs = true;
                htm.addln("<h3>Catalogs (engine term is '", meta.getCatalogTerm(), "')</h3>");

                htm.addln("<table class='report'>");

                do {
                    htm.sTr().sTh().add("Name").eTh().sTd().add(catalogs.getString("TABLE_CAT")).eTd().eTr();
                } while (catalogs.next());

                htm.eTable();
            }
        }

        if (hasCatalogs) {
            hasCatalogs = false;
            try (final ResultSet schemas = meta.getSchemas()) {
                while (schemas.next()) {
                    if (schemas.getString("TABLE_CATALOG") != null) {
                        hasCatalogs = true;
                        break;
                    }
                }
            }
        }

        try (final ResultSet schemas = meta.getSchemas()) {
            if (schemas.next()) {
                htm.addln("<h3>Schemas (engine term is '", meta.getSchemaTerm(), "')</h3>");

                htm.addln("<table class='report'>");
                htm.sTr();
                if (hasCatalogs) {
                    htm.sTh().add("Catalog").eTh();
                }
                htm.sTh().add("Schema").eTh().eTr();

                do {
                    htm.sTr();
                    if (hasCatalogs) {
                        htm.sTd().add(schemas.getString("TABLE_CATALOG")).eTd();
                    }
                    htm.sTd().add(schemas.getString("TABLE_SCHEM")).eTd().eTr();
                } while (schemas.next());

                htm.eTable();
            }
        }

        try (final ResultSet tables = meta.getTables(null, null, null, null)) {

            if (tables.next()) {
                htm.addln("<h3>Tables</h3>");

                htm.addln("<table class='report'>");
                htm.sTr();
                if (hasCatalogs) {
                    htm.sTh().add("Catalog").eTh();
                }
                htm.sTh().add("Schema").eTh().sTh().add("Table").eTh().sTh().add("Type").eTh().eTr();

                do {
                    final String sch = tables.getString("TABLE_SCHEM");

                    if (cfg.db.server.type == EDbProduct.INFORMIX) {
                        if ("informix".equals(sch) || "9.55C1".equals(sch) || CoreConstants.EMPTY.equals(sch)) {
                            continue;
                        }
                    } else if (cfg.db.server.type == EDbProduct.ORACLE) {
                        if ("SYS".equals(sch) || "SYSTEM".equals(sch)
                                || "WMSYS".equals(sch) || "MDSYS".equals(sch)
                                || "MODSMGR".equals(sch) || "OLAPSYS".equals(sch)
                                || "ORDSYS".equals(sch) || "CTXSYS".equals(sch)
                                || "XDB".equals(sch) || "ORDDATA".equals(sch)
                                || "GSMADMIN_INTERNAL".equals(sch)) {
                            continue;
                        }
                    } else if ((cfg.db.server.type == EDbProduct.POSTGRESQL)
                            && ("pg_catalog".equals(sch) || "pg_toast".equals(sch)
                            || "information_schema".equals(sch))) {
                        continue;
                    }

                    htm.sTr();
                    if (hasCatalogs) {
                        htm.sTd().add(tables.getString("TABLE_CAT")).eTd();
                    }
                    htm.sTd().add(sch).eTd() //
                            .sTd().add("<a href='dbadm_cache_metadata.html?driver=", driver,
                                    "&cat=", tables.getString("TABLE_CAT"),
                                    "&schema=", sch, "&table=",
                                    tables.getString("TABLE_NAME"),
                                    "' target='_blank'>",
                                    tables.getString("TABLE_NAME"), "</a>")
                            .eTd() //
                            .sTd().add(tables.getString("TABLE_TYPE")).eTd().eTr();
                } while (tables.next());

                htm.eTable();
            }
        }

        try (final ResultSet procedures = meta.getProcedures(null, null, null)) {

            if (procedures.next()) {
                htm.addln("<h3>Stored Procedures</h3>");

                htm.addln("<table class='report'>");
                htm.sTr();
                if (hasCatalogs) {
                    htm.sTh().add("Catalog").eTh();
                }
                htm.sTh().add("Schema").eTh() //
                        .sTh().add("Name").eTh().eTr();

                do {
                    htm.sTr();
                    if (hasCatalogs) {
                        htm.sTd().add(procedures.getString("PROCEDURE_CAT")).eTd();
                    }
                    htm.sTd().add(procedures.getString("PROCEDURE_SCHEM")).eTd();
                    htm.sTd().add(procedures.getString("PROCEDURE_NAME")).eTd();
                    htm.eTr();
                } while (procedures.next());

                htm.eTable();
            }
        }
    }
}
