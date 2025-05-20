package dev.mathops.web.site.admin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.genadmin.GenAdminSubsite;
import dev.mathops.web.site.admin.office.OfficeSubsite;
import dev.mathops.web.site.admin.proctor.ProctorSubsite;
import dev.mathops.web.site.admin.testing.TestingSubsite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The root of the administrative site. Provides user login and buttons to enter subordinate administrative sites.
 * <p>
 * This servlet requires that all connections be secured (SSL/TLS), and uses the SSL session ID as the unique identifier
 * for the session. This prevents session hijacking as can be done in a URL-rewriting scheme.
 */
public final class AdminSite extends AbstractPageSite {

    /** The name of a style sheet. */
    private static final String BASE_STYLE_CSS = "basestyle.css";

    /** The name of a style sheet. */
    private static final String STYLE_CSS = "style.css";

    /** A path. */
    private static final String IMAGES_PATH = "images/";

    /** The sub-site for general administrators. */
    private final GenAdminSubsite general;

    /** The sub-site for office staff. */
    private final OfficeSubsite office;

    /** The sub-site for proctors. */
    private final ProctorSubsite proctor;

    /** The sub-site for testing. */
    private final TestingSubsite testing;

    /**
     * Constructs a new {@code AdminSite}.
     *
     * @param theSite     the site profile under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public AdminSite(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);

        this.general = new GenAdminSubsite(this);
        this.office = new OfficeSubsite(this);
        this.testing = new TestingSubsite(this);
        this.proctor = new ProctorSubsite(this);
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return Res.get(Res.SITE_TITLE);
    }

    /**
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.NONE;
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
    public void doGet(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        if (CoreConstants.EMPTY.equals(subpath)) {
            final String path = this.site.path;
            final boolean trailingSlash = path.endsWith(Contexts.ROOT_PATH);
            resp.sendRedirect(path + (trailingSlash ? "login.html" : "/login.html"));
        } else if ("login.html".equals(subpath)) {
            PageLogin.doLoginPage(cache, this, req, resp);
        } else if (BASE_STYLE_CSS.equals(subpath)) {
            final byte[] cssBytes = FileLoader.loadFileAsBytes(Page.class, BASE_STYLE_CSS, true);
            sendReply(req, resp, "text/css", cssBytes);
        } else if (STYLE_CSS.equals(subpath)) {
            final Class<? extends AdminSite> myClass = getClass();
            final byte[] cssBytes = FileLoader.loadFileAsBytes(myClass, STYLE_CSS, true);
            sendReply(req, resp, "text/css", cssBytes);
        } else if (subpath.startsWith(IMAGES_PATH)) {
            final String imgPath = subpath.substring(7);
            serveImage(imgPath, req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            // The pages that follow require the user to be logged in
            final ImmutableSessionInfo session = validateSession(req, resp, null);

            if (session == null) {
                if ("secure/shibboleth.html".equals(subpath)) {
                    doShibbolethLogin(cache, req, resp, null, "home.html");
                } else {
                    final String path = this.site.path;
                    final boolean trailingSlash = path.endsWith(Contexts.ROOT_PATH);
                    resp.sendRedirect(path + (trailingSlash ? "login.html" : "/login.html"));
                }
            } else {
                final String effectiveId = session.getEffectiveUserId();
                LogBase.setSessionInfo(session.loginSessionId, effectiveId);

                if ("home.html".equals(subpath)) {
                    PageHome.doGet(cache, this, req, resp, session);
                } else if ("secure/shibboleth.html".equals(subpath)) {
                    final String path = this.site.path;
                    final boolean trailingSlash = path.endsWith(Contexts.ROOT_PATH);
                    resp.sendRedirect(path + (trailingSlash ? "home.html" : "/home.html"));
                } else if (subpath.startsWith("genadmin/")) {
                    final String innerPath = subpath.substring(9);
                    this.general.doGet(cache, innerPath, session, req, resp);
                } else if (subpath.startsWith("office/")) {
                    final String innerPath = subpath.substring(7);
                    this.office.doGet(cache, innerPath, session, req, resp);
                } else if (subpath.startsWith("proctor/")) {
                    final String innerPath = subpath.substring(8);
                    this.proctor.doGet(cache, innerPath, session, req, resp);
                } else if (subpath.startsWith("testing/")) {
                    final String innerPath = subpath.substring(8);
                    this.testing.doGet(cache, innerPath, session, req, resp);
                } else {
                    final String msg = Res.fmt(Res.GET_BAD_PATH, subpath);
                    Log.warning(msg);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

                LogBase.setSessionInfo(null, null);
            }
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
    public void doPost(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                       final HttpServletResponse resp) throws IOException, SQLException {

        if ("login.html".equals(subpath)) {
            PageLogin.doLoginPage(cache, this, req, resp);
        } else {
            // The pages that follow require the user to be logged in
            final ImmutableSessionInfo session = validateSession(req, resp, "login.html");

            if (session == null) {
                final String msg = Res.fmt(Res.POST_NO_SESSION, subpath);
                Log.warning(msg);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                final String effectiveId = session.getEffectiveUserId();
                LogBase.setSessionInfo(session.loginSessionId, effectiveId);

                if (subpath.startsWith("genadmin/")) {
                    final String innerPath = subpath.substring(9);
                    this.general.doPost(cache, innerPath, session, req, resp);
                } else if (subpath.startsWith("office/")) {
                    final String innerPath = subpath.substring(7);
                    this.office.doPost(cache, innerPath, session, req, resp);
                } else if (subpath.startsWith("proctor/")) {
                    final String innerPath = subpath.substring(8);
                    this.proctor.doPost(cache, innerPath, session, req, resp);
                } else if (subpath.startsWith("testing/")) {
                    final String innerPath = subpath.substring(8);
                    this.testing.doPost(cache, innerPath, session, req, resp);
                } else {
                    final String msg = Res.fmt(Res.POST_BAD_PATH, subpath);
                    Log.warning(msg);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

                LogBase.setSessionInfo(null, null);
            }
        }
    }
}
