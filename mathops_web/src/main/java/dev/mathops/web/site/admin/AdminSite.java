package dev.mathops.web.site.admin;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.bookstore.BookstoreSubsite;
import dev.mathops.web.site.admin.genadmin.GenAdminSubsite;
import dev.mathops.web.site.admin.office.OfficeSubsite;
import dev.mathops.web.site.admin.proctor.ProctorSubsite;
import dev.mathops.web.site.admin.sysadmin.SysAdminSubsite;
import dev.mathops.web.site.admin.testing.TestingSubsite;

import jakarta.servlet.ServletConfig;
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

    /** The sub-site for system administrators. */
    private final SysAdminSubsite sysadmin;

    /** The sub-site for general administrators. */
    private final GenAdminSubsite genadmin;

    /** The sub-site for office staff. */
    private final OfficeSubsite office;

    /** The sub-site for bookstore staff. */
    private final BookstoreSubsite bookstore;

    /** The sub-site for proctors. */
    private final ProctorSubsite proctor;

    /** The sub-site for testing. */
    private final TestingSubsite testing;

    /**
     * Constructs a new {@code AdminSite}.
     *
     * @param theSiteProfile the site profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public AdminSite(final WebSiteProfile theSiteProfile,
                     final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);

        this.sysadmin = new SysAdminSubsite(this);
        this.genadmin = new GenAdminSubsite(this);
        this.office = new OfficeSubsite(this);
        this.bookstore = new BookstoreSubsite(this);
        this.proctor = new ProctorSubsite(this);
        this.testing = new TestingSubsite(this);
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
     * Initializes the site - called when the servlet is initialized.
     *
     * @param config the servlet context in which the servlet is being initialized
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

        if (CoreConstants.EMPTY.equals(subpath)) {
            final String path = this.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) //
                    ? "login.html" : "/login.html"));
        } else if ("login.html".equals(subpath)) {
            PageLogin.doLoginPage(cache, this, req, resp);
        } else if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css",
                    FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css",
                    FileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            // The pages that follow require the user to be logged in
            final ImmutableSessionInfo session = validateSession(req, resp, null);

            if (session == null) {
                if ("secure/shibboleth.html".equals(subpath)) {
                    doShibbolethLogin(cache, req, resp, null, "home.html");
                } else {
                    final String path = this.siteProfile.path;
                    resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? "login.html" : "/login.html"));
                }
            } else {
                LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                if ("home.html".equals(subpath)) {
                    PageHome.doGet(cache, this, req, resp, session);
                } else if ("secure/shibboleth.html".equals(subpath)) {
                    final String path = this.siteProfile.path;
                    resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? "home.html" : "/home.html"));
                } else if (subpath.startsWith("sysadmin/")) {
                    this.sysadmin.doGet(cache, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("genadmin/")) {
                    this.genadmin.doGet(cache, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("office/")) {
                    this.office.doGet(cache, subpath.substring(7), session, req, resp);
                } else if (subpath.startsWith("bookstore/")) {
                    this.bookstore.doGet(cache, subpath.substring(10), session, req, resp);
                } else if (subpath.startsWith("proctor/")) {
                    this.proctor.doGet(cache, subpath.substring(8), session, req, resp);
                } else if (subpath.startsWith("testing/")) {
                    this.testing.doGet(cache, subpath.substring(8), session, req, resp);

                    // ... etc. ...

                } else {
                    Log.warning(Res.fmt(Res.GET_BAD_PATH, subpath));
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
     * @param cache    the data cache
     * @param subpath  the portion of the path beyond that which was used to select this site
     * @param type the site type
     * @param req      the request
     * @param resp     the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if ("login.html".equals(subpath)) {
            PageLogin.doLoginPage(cache, this, req, resp);
        } else {
            // The pages that follow require the user to be logged in
            final ImmutableSessionInfo session = validateSession(req, resp, "login.html");

            if (session == null) {
                Log.warning(Res.fmt(Res.POST_NO_SESSION, subpath));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                if (subpath.startsWith("sysadmin/")) {
                    this.sysadmin.doPost(cache, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("genadmin/")) {
                    this.genadmin.doPost(cache, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("office/")) {
                    this.office.doPost(cache, subpath.substring(7), session, req, resp);
                } else if (subpath.startsWith("bookstore/")) {
                    this.bookstore.doPost(cache, subpath.substring(10), session, req, resp);
                } else if (subpath.startsWith("proctor/")) {
                    this.proctor.doPost(cache, subpath.substring(8), session, req, resp);
                } else if (subpath.startsWith("testing/")) {
                    this.testing.doPost(cache, subpath.substring(8), session, req, resp);

                    // ... etc. ...

                } else {
                    Log.warning(Res.fmt(Res.POST_BAD_PATH, subpath));
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

                LogBase.setSessionInfo(null, null);
            }
        }
    }
}
