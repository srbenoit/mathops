package dev.mathops.web.site.admin.testing;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AbstractSubsite;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The subsite that provides functions to manage the testing center.
 */
public final class TestingSubsite extends AbstractSubsite {

    /**
     * Constructs a new {@code TestingSubsite}.
     *
     * @param theSite the owning site
     */
    public TestingSubsite(final AdminSite theSite) {

        super(theSite);
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param studentData the student data object
     * @param subpath     the portion of the path beyond that which was used to select this site
     * @param session     the login session (known not to be null)
     * @param req         the request
     * @param resp        the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void subsiteGet(final StudentData studentData, final String subpath,
                           final ImmutableSessionInfo session, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            if ("home.html".equals(subpath)) {
                PageHome.doGet(studentData, this.site, req, resp, session);
            } else if ("power.html".equals(subpath)) {
                PagePower.doGet(studentData, this.site, req, resp, session);
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
     * @param studentData the student data object
     * @param subpath     the portion of the path beyond that which was used to select this site
     * @param session     the login session (known not to be null)
     * @param req         the request
     * @param resp        the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void subsitePost(final StudentData studentData, final String subpath,
                            final ImmutableSessionInfo session, final HttpServletRequest req,
                            final HttpServletResponse resp) throws IOException, SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());
            if ("power.html".equals(subpath)) {
                PagePower.doPost(studentData, this.site, req, resp, session);
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
