package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import dev.mathops.web.site.admin.genadmin.GenAdminSubsite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * A page that displays information on a database server and its tables.
 */
public enum PageDbAdminContextsServer {
    ;

    /** Suffix for fields containing user names. */
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

        final String driver = req.getParameter("driver");

        if (AbstractSite.isParamInvalid(driver)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  driver='", driver, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final ContextMap map = ContextMap.getDefaultInstance();
            final LoginConfig cfg = map.getLogin(driver);

            if (cfg == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                final HtmlBuilder htm = GenAdminPage.startGenAdminPage(site, session, true);
                htm.sH(2, "gray").add("Database Administration").eH(2);
                htm.hr("orange");

                Connection jdbc = GenAdminSubsite.getConnection(session.loginSessionId, driver);

                if (jdbc == null) {
                    final String username = cfg.user;
                    final String password = cfg.password;

                    if (username != null && password != null) {
                        try {
                            jdbc = cfg.openConnection(username, password);
                            GenAdminSubsite.addConnection(session.loginSessionId, driver, jdbc);
                        } catch (final SQLException ex) {
                            Log.warning(ex);
                        }
                    }
                }

                if (jdbc == null) {
                    emitLoginForm(htm, cfg, error);
                } else {
                    try {
                        if (jdbc.isClosed()) {
                            GenAdminSubsite.removeConnection(session.loginSessionId, driver);
                            emitLoginForm(htm, cfg, error);
                        } else {
                            emitServerAdminPage(htm, cfg);
                        }
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                        GenAdminSubsite.removeConnection(session.loginSessionId, driver);
                        emitLoginForm(htm, cfg, error);
                    }
                }

                Page.endOrdinaryPage(cache, site, htm, true);
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * Emits the login form to create a new connection to the database.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param cfg   the driver configuration
     * @param error an optional error message
     */
    private static void emitLoginForm(final HtmlBuilder htm, final LoginConfig cfg,
                                      final String error) {

        final String uid = CoreConstants.newId(10) + USERNAME_SUFFIX;
        final String pid = CoreConstants.newId(10) + PASSWORD_SUFFIX;

        htm.div("vgap2");
        htm.sDiv("dbloginpane");

        htm.sP("dbloginprompt").add("Connect to Database").eP();

        final String usr = cfg.user == null ? CoreConstants.EMPTY : cfg.user;

        htm.sDiv("center");
        htm.addln("<form action='db_admin_server_login.html' method='post'>");
        htm.addln("  <input type='hidden' name='driver', value='", cfg.id,
                "'/>");
        htm.sDiv("loginfield")
                .add("<label for='", uid, "'>Username:</label> ",
                        "<input data-lpignore='true' autocomplete='new-password' type='text' size='16' id='",
                        uid, "' name='", uid, "' value='", usr,
                        "'/>")
                .eDiv();
        htm.sDiv("loginfield")
                .add("<label for='", pid, "'>Password:</label> ",
                        "<input data-lpignore='true' autocomplete='new-password' type='text' size='16' id='",
                        pid, "' name='", pid, "'/>")
                .eDiv();
        htm.add("<button class='btn' type='submit'>", "Submit",
                "</button>");
        htm.addln("</form>");
        htm.eDiv();

        if (error != null) {
            htm.sP("dbloginerror").add(error).eP();
        }

        htm.eDiv();
    }

    /**
     * Emits the administration page when connected to a database.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param cfg the driver configuration
     */
    private static void emitServerAdminPage(final HtmlBuilder htm, final LoginConfig cfg) {

        final String query = "driver=" + cfg.id;

        PageDbAdmin.emitDisconnectLink(htm, query);
        PageDbAdmin.emitCfgInfoTable(htm, cfg);

        switch (cfg.db.use) {
            case LIVE:
                PageDbAdmin.emitLiveNavMenu(htm, null, query);
                break;

            case PROD:
            case DEV:
            case TEST:
            case ARCH:
            case ODS:
            default:
                PageDbAdmin.emitProdNavMenu(htm, null, query);
                break;
        }

        htm.div("vgap");
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        String username = null;
        String password = null;

        for (final Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            if (entry.getKey().endsWith(USERNAME_SUFFIX)) {
                username = entry.getValue()[0];
            } else if (entry.getKey().endsWith(PASSWORD_SUFFIX)) {
                password = entry.getValue()[0];
            }
        }

        final String driver = req.getParameter("driver");

        if (AbstractSite.isParamInvalid(driver)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  driver='", driver, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (driver == null || username == null || password == null) {
            doGet(cache, site, req, resp, session, "Invalid login");
        } else {
            final ContextMap map = ContextMap.getDefaultInstance();
            final LoginConfig cfg = map.getLogin(driver);

            if (cfg == null) {
                doGet(cache, site, req, resp, session, "Invalid login");
            } else {
                try {
                    final Connection jdbc = cfg.openConnection(username, password);

                    GenAdminSubsite.addConnection(session.loginSessionId, driver, jdbc);
                    doGet(cache, site, req, resp, session, null);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    doGet(cache, site, req, resp, session, "Invalid login");
                }
            }
        }
    }

    /**
     * Processes a logout request.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user login session
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
            GenAdminSubsite.removeConnection(session.loginSessionId, driver);
            doGet(cache, site, req, resp, session, null);
        }
    }
}
