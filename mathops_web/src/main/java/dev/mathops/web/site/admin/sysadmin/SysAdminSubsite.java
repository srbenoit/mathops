package dev.mathops.web.site.admin.sysadmin;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AbstractSubsite;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.sysadmin.db.PageDb;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrv;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvAdd;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvDel;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvPrd;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvPrdAdd;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvPrdAdm;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvPrdAdmDba;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvPrdAdmSys;
import dev.mathops.web.site.admin.sysadmin.db.PageDbSrvPrdDel;
import dev.mathops.web.site.admin.sysadmin.media.PageMediaServers;
import dev.mathops.web.site.admin.sysadmin.turn.PageTurnServers;
import dev.mathops.web.site.admin.sysadmin.web.PageWebServers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The sub-site of the administrative site available to system administrators.
 */
public final class SysAdminSubsite extends AbstractSubsite {

    /**
     * Constructs a new {@code SysAdminSubsite}.
     *
     * @param theSite the owning site
     */
    public SysAdminSubsite(final AdminSite theSite) {

        super(theSite);
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsiteGet(final Cache cache, final String subpath,
                           final ImmutableSessionInfo session, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.SYSADMIN)) {
            switch (subpath) {
                case "home.html" -> PageHome.doGet(cache, this.site, req, resp, session);
                case "db.html" -> PageDb.doGet(cache, this.site, req, resp, session);
                case "db_srv.html" -> PageDbSrv.doGet(cache, this.site, req, resp, session);
                case "db_srv_add.html" -> PageDbSrvAdd.doGet(cache, this.site, req, resp, session);
                case "db_srv_del.html" -> PageDbSrvDel.doGet(cache, this.site, req, resp, session);
                case "db_srv_prd.html" -> PageDbSrvPrd.doGet(cache, this.site, req, resp, session);
                case "db_srv_prd_add.html" -> PageDbSrvPrdAdd.doGet(cache, this.site, req, resp, session);
                case "db_srv_prd_del.html" -> PageDbSrvPrdDel.doGet(cache, this.site, req, resp, session);
                case "db_srv_prd_adm.html" -> PageDbSrvPrdAdm.doGet(cache, this.site, req, resp, session);
                case "db_srv_prd_adm_dba.html" -> PageDbSrvPrdAdmDba.doGet(cache, this.site, req, resp, session);
                case "db_srv_prd_adm_sys.html" -> PageDbSrvPrdAdmSys.doGet(cache, this.site, req, resp, session);
                case "web_servers.html" -> PageWebServers.doGet(cache, this.site, req, resp, session);
                case "media_servers.html" -> PageMediaServers.doGet(cache, this.site, req, resp, session);
                case "turn_servers.html" -> PageTurnServers.doGet(cache, this.site, req, resp, session);
                case null, default -> {
                    Log.warning("GET: unknown path '", subpath, "'");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } else {
            Log.warning("GET: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param session the login session (known not to be null)
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void subsitePost(final Cache cache, final String subpath,
                            final ImmutableSessionInfo session, final HttpServletRequest req,
                            final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.SYSADMIN)) {
            switch (subpath) {
                case "db_srv_add.html" -> PageDbSrvAdd.doPost(cache, this.site, req, resp, session);
                case "db_srv_del.html" -> PageDbSrvDel.doPost(cache, this.site, req, resp, session);
                case "db_srv_prd_add.html" -> PageDbSrvPrdAdd.doPost(cache, this.site, req, resp, session);
                case "db_srv_prd_del.html" -> PageDbSrvPrdDel.doPost(cache, this.site, req, resp, session);
                case null, default -> {
                    Log.warning("POST: unknown path '", subpath, "'");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } else {
            Log.warning("POST: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
