package dev.mathops.web.site.admin.sysadmin;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            if ("home.html".equals(subpath)) {
                PageHome.doGet(cache, this.site, req, resp, session);

            } else if ("db.html".equals(subpath)) {
                PageDb.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv.html".equals(subpath)) {
                PageDbSrv.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_add.html".equals(subpath)) {
                PageDbSrvAdd.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_del.html".equals(subpath)) {
                PageDbSrvDel.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_prd.html".equals(subpath)) {
                PageDbSrvPrd.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_prd_add.html".equals(subpath)) {
                PageDbSrvPrdAdd.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_prd_del.html".equals(subpath)) {
                PageDbSrvPrdDel.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_prd_adm.html".equals(subpath)) {
                PageDbSrvPrdAdm.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_prd_adm_dba.html".equals(subpath)) {
                PageDbSrvPrdAdmDba.doGet(cache, this.site, req, resp, session);
            } else if ("db_srv_prd_adm_sys.html".equals(subpath)) {
                PageDbSrvPrdAdmSys.doGet(cache, this.site, req, resp, session);

            } else if ("web_servers.html".equals(subpath)) {
                PageWebServers.doGet(cache, this.site, req, resp, session);
            } else if ("media_servers.html".equals(subpath)) {
                PageMediaServers.doGet(cache, this.site, req, resp, session);
            } else if ("turn_servers.html".equals(subpath)) {
                PageTurnServers.doGet(cache, this.site, req, resp, session);
            } else {
                Log.warning("GET: unknown path '", subpath, "'");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
            if ("db_srv_add.html".equals(subpath)) {
                PageDbSrvAdd.doPost(cache, this.site, req, resp, session);
            } else if ("db_srv_del.html".equals(subpath)) {
                PageDbSrvDel.doPost(cache, this.site, req, resp, session);
            } else if ("db_srv_prd_add.html".equals(subpath)) {
                PageDbSrvPrdAdd.doPost(cache, this.site, req, resp, session);
            } else if ("db_srv_prd_del.html".equals(subpath)) {
                PageDbSrvPrdDel.doPost(cache, this.site, req, resp, session);
            } else {
                Log.warning("POST: unknown path '", subpath, "'");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            Log.warning("POST: invalid role");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
