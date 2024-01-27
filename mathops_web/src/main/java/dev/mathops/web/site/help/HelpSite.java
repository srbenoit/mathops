package dev.mathops.web.site.help;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.help.admin.PageAdminHome;
import dev.mathops.web.site.help.student.PageStudentEmail;
import dev.mathops.web.site.help.student.PageStudentForum;
import dev.mathops.web.site.help.student.PageStudentHelp;
import dev.mathops.web.site.help.student.PageStudentHome;
import dev.mathops.web.site.help.student.PageStudentLiveHelp;
import dev.mathops.web.site.help.student.PageStudentLobby;
import dev.mathops.web.site.help.student.PageStudentTopic;
import dev.mathops.web.site.help.tutor.PageTutorHome;
import dev.mathops.web.site.help.tutor.PageTutorLiveHelp;
import dev.mathops.web.skin.IServletSkin;
import dev.mathops.web.skin.colostate.CSUSkin;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The help site. All users log into this single site. If a user has permission to act as a tutor, they will be given
 * the option to act as either a student or as a tutor. If the user has administrator permission, they will have the
 * option to act as student, tutor, or administrator.
 */
public final class HelpSite extends AbstractSite {

    /** The skin for this site. */
    public final IServletSkin skin;

    /**
     * Constructs a new {@code HelpSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public HelpSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);

        this.skin = CSUSkin.INSTANCE;
    }

    /**
     * Initializes the site - called when the servlet is initialized.
     *
     * @param config the servlet context in which the servlet is being in
     */
    @Override
    public void init(final ServletConfig config) {

        // No action
    }

    /**
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return false;
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        Log.info("HelpSite GET ", subpath);

        // TODO: Honor maintenance mode.

        if ("basestyle.css".equals(subpath) || "secure/basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath) || "secure/style.css".equals(subpath)) {
            sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else if ("favicon.ico".equals(subpath) || "secure/favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            final ImmutableSessionInfo session = validateSession(req, resp, null);

            if (session == null) {
                if (CoreConstants.EMPTY.equals(subpath)) {
                    redirectTo("login.html", resp);
                } else if ("index.html".equals(subpath) || "login.html".equals(subpath)) {
                    PageLanding.showPage(cache, this, req, resp);
                } else if ("secure/shibboleth.html".equals(subpath)) {
                    doShibbolethLogin(cache, req, resp, null);
                } else {
                    Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                    PageLanding.showPage(cache, this, req, resp);
                }
            } else {
                LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                if (CoreConstants.EMPTY.equals(subpath)) {
                    redirectTo("login.html", resp);
                } else if ("index.html".equals(subpath)
                        || "login.html".equals(subpath)) {
                    PageLanding.showPage(cache, this, req, resp);
                } else if ("secure/shibboleth.html".equals(subpath)) {
                    doShibbolethLogin(cache, req, resp, session);
                } else if ("helpbar.html".equals(subpath)) {
                    HelpAdminBar.processPost(req, session);
                    redirectTo("home.html", resp);
                } else {
                    Log.info("HelpSite GET[", session.getEffectiveRole().name(),
                            CoreConstants.SLASH, session.role.name(), "] ", subpath);

                    if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
                        doGetAdministrator(cache, subpath, req, resp, session);
                    } else if (session.getEffectiveRole().canActAs(ERole.TUTOR)) {
                        doGetTutor(cache, subpath, req, resp, session);
                    } else if (session.getEffectiveRole().canActAs(ERole.STUDENT)) {
                        doGetStudent(cache, subpath, req, resp, session);
                    } else {
                        Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                        PageLanding.showPage(cache, this, req, resp);
                    }
                }

                LogBase.setSessionInfo(null, null);
            }
        }
    }

    /**
     * Processes a GET request when the session is valid and is acting with ADMINISTRATOR permission.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param req     the request
     * @param resp    the response
     * @param session the session (known to have a role with ADMINISTRATOR permission)
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doGetAdministrator(final Cache cache, final String subpath,
                                    final ServletRequest req, final HttpServletResponse resp,
                                    final ImmutableSessionInfo session) throws IOException, SQLException {

        if ("home.html".equals(subpath)) {
            PageAdminHome.doGet(this, req, resp, session);
        } else if ("homemax.html".equals(subpath)) {
            PageAdminHome.doMaxGet(this, req, resp, session);
        } else {
            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            PageLanding.showPage(cache, this, req, resp);
        }
    }

    /**
     * Processes a GET request when the session is valid and is acting with TUTOR permission.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param req     the request
     * @param resp    the response
     * @param session the session (known to have a role with ADMINISTRATOR permission)
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doGetTutor(final Cache cache, final String subpath, final ServletRequest req,
                            final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if ("home.html".equals(subpath)) {
            PageTutorHome.doGet(this, req, resp, session);
        } else if ("homemax.html".equals(subpath)) {
            PageTutorHome.doMaxGet(this, req, resp, session);
        } else if ("livehelp.html".equals(subpath)) {
            PageTutorLiveHelp.doGet(this, req, resp, session);
        } else if ("livehelpmax.html".equals(subpath)) {
            PageTutorLiveHelp.doMaxGet(this, req, resp, session);
        } else {
            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            PageLanding.showPage(cache, this, req, resp);
        }
    }

    /**
     * Processes a GET request when the session is valid and is acting with STUDENT permission.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param req     the request
     * @param resp    the response
     * @param session the session (known to have a role with ADMINISTRATOR permission)
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doGetStudent(final Cache cache, final String subpath, final HttpServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if ("home.html".equals(subpath)) {
            PageStudentHome.doGet(this, req, resp, session);
        } else if ("topic.html".equals(subpath)) {
            PageStudentTopic.doGet(cache, this, req, resp, session);
        } else if ("lobby.html".equals(subpath)) {
            PageStudentLobby.doGet(cache, this, req, resp, session);
        } else if ("help.html".equals(subpath)) {
            PageStudentHelp.doGet(cache, this, req, resp, session);
        } else if ("helpmax.html".equals(subpath)) {
            PageStudentHelp.doGetMax(req, resp, session);
        } else if ("livehelp.html".equals(subpath)) {
            PageStudentLiveHelp.doGet(cache, this, req, resp, session);
        } else if ("forum.html".equals(subpath)) {
            PageStudentForum.doGet(cache, this, req, resp, session);
        } else if ("email.html".equals(subpath)) {
            PageStudentEmail.doGet(cache, this, req, resp, session);
        } else {
            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            PageLanding.showPage(cache, this, req, resp);
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        Log.info("HelpSite POST ", subpath);

        // TODO: Honor maintenance mode.

        // All posts require a session
        final ImmutableSessionInfo session = validateSession(req, resp, null);

        if (session == null) {
            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            PageLanding.showPage(cache, this, req, resp);

            LogBase.setSessionInfo(null, null);
        }
    }

    /**
     * Scans the request for Shibboleth attributes and uses them (if found) to establish a session, and then redirects
     * to either the secure page (if valid) or the login page (if not valid).
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws SQLException if there was an error accessing the database
     */
    private void doShibbolethLogin(final Cache cache, final HttpServletRequest req, final HttpServletResponse resp,
                                   final ImmutableSessionInfo session) throws SQLException {

        ImmutableSessionInfo sess = session;

        if (sess == null) {
            sess = processShibbolethLogin(cache, req);
        }

        final String relPath;

        if (sess == null) {
            relPath = "login.html";
        } else {
            relPath = "home.html";
            Log.info("Student ", sess.userId, " logged in with session ", sess.loginSessionId);

            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(req.getServerName());
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }

        redirectTo(relPath, resp);
    }

    /**
     * Redirects to a specified relative path.
     *
     * @param relativePath the relative path
     * @param resp         the response
     */
    private void redirectTo(final String relativePath, final HttpServletResponse resp) {

        final String path = this.siteProfile.path;
        final String redirect;

        redirect = path + (path.endsWith(CoreConstants.SLASH) ? relativePath
                : CoreConstants.SLASH + relativePath);

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);
    }
}
