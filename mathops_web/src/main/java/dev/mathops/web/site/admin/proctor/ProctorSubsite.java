package dev.mathops.web.site.admin.proctor;

import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AbstractSubsite;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The subsite that provides functions for proctors.
 */
public final class ProctorSubsite extends AbstractSubsite {

    /**
     * Constructs a new {@code ProctorSubsite}.
     *
     * @param theSite the owning site
     */
    public ProctorSubsite(final AdminSite theSite) {

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

        if (session.getEffectiveRole().canActAs(ERole.PROCTOR)) {
            if ("home.html".equals(subpath)) {
                PageHome.doGet(cache, this.site, req, resp, session);
            } else if ("proctoring_teams.html".equals(subpath)) {
                PageProctorTeams.doPage(cache, this.site, req, resp, session);
            } else if ("proctoring_challenge_teams.html".equals(subpath)) {
                PageProctoringChallengeTeams.doGet(cache, this.site, req, resp, session);
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

        if (session.getEffectiveRole().canActAs(ERole.PROCTOR)) {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

            if ("proctoring_teams.html".equals(subpath)) {
                PageProctorTeams.doPage(cache, this.site, req, resp, session);
            } else if ("proctoring_challenge_teams.html".equals(subpath)) {
                PageProctoringChallengeTeams.doPost(cache, this.site, req, resp, session);
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
