package dev.mathops.web.site.admin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.cfg.WebSiteProfile;
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
     * @param data    the web view data
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doGet(final WebViewData data, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if (CoreConstants.EMPTY.equals(subpath)) {
            final String path = this.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? "login.html" : "/login.html"));
        } else if ("login.html".equals(subpath)) {
            PageLogin.doLoginPage(data, this, req, resp);
        } else if ("basestyle.css".equals(subpath)) {
            final byte[] fileBytes = FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true);
            sendReply(req, resp, "text/css", fileBytes);
        } else if ("style.css".equals(subpath)) {
            final Class<? extends AdminSite> cls = getClass();
            final byte[] fileBytes = FileLoader.loadFileAsBytes(cls, "style.css", true);
            sendReply(req, resp, "text/css", fileBytes);
        } else if (subpath.startsWith("images/")) {
            serveImage(subpath.substring(7), req, resp);
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else {
            // The pages that follow require the user to be logged in
            final ImmutableSessionInfo session = validateSession(req, resp, null);

            if (session == null) {
                if ("secure/shibboleth.html".equals(subpath)) {
                    final Cache cache = data.getCache();
                    doShibbolethLogin(cache, req, resp, null, "home.html");
                } else {
                    final String path = this.siteProfile.path;
                    resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? "login.html" : "/login.html"));
                }
            } else {
                final String stuId = session.getEffectiveUserId();
                LogBase.setSessionInfo(session.loginSessionId, stuId);

                if ("home.html".equals(subpath)) {
                    PageHome.doGet(data, this, req, resp, session);
                } else if ("secure/shibboleth.html".equals(subpath)) {
                    final String path = this.siteProfile.path;
                    resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? "home.html" : "/home.html"));
                } else if (subpath.startsWith("sysadmin/")) {
                    this.sysadmin.doGet(data, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("genadmin/")) {
                    this.genadmin.doGet(data, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("office/")) {
                    this.office.doGet(data, subpath.substring(7), session, req, resp);
                } else if (subpath.startsWith("bookstore/")) {
                    this.bookstore.doGet(data, subpath.substring(10), session, req, resp);
                } else if (subpath.startsWith("proctor/")) {
                    this.proctor.doGet(data, subpath.substring(8), session, req, resp);
                } else if (subpath.startsWith("testing/")) {
                    this.testing.doGet(data, subpath.substring(8), session, req, resp);

                    // ... etc. ...

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
     * @param data    the web view data
     * @param subpath     the portion of the path beyond that which was used to select this site
     * @param type        the site type
     * @param req         the request
     * @param resp        the response
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void doPost(final WebViewData data, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if ("login.html".equals(subpath)) {
            PageLogin.doLoginPage(data, this, req, resp);
        } else {
            // The pages that follow require the user to be logged in
            final ImmutableSessionInfo session = validateSession(req, resp, "login.html");

            if (session == null) {
                final String msg = Res.fmt(Res.POST_NO_SESSION, subpath);
                Log.warning(msg);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                final String stuId = session.getEffectiveUserId();
                LogBase.setSessionInfo(session.loginSessionId, stuId);

                if (subpath.startsWith("sysadmin/")) {
                    this.sysadmin.doPost(data, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("genadmin/")) {
                    this.genadmin.doPost(data, subpath.substring(9), session, req, resp);
                } else if (subpath.startsWith("office/")) {
                    this.office.doPost(data, subpath.substring(7), session, req, resp);
                } else if (subpath.startsWith("bookstore/")) {
                    this.bookstore.doPost(data, subpath.substring(10), session, req, resp);
                } else if (subpath.startsWith("proctor/")) {
                    this.proctor.doPost(data, subpath.substring(8), session, req, resp);
                } else if (subpath.startsWith("testing/")) {
                    this.testing.doPost(data, subpath.substring(8), session, req, resp);

                    // ... etc. ...

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
