package dev.mathops.web.host.precalc.canvas;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.UserInfoBar;
import dev.mathops.web.host.precalc.canvas.courses.PageAccount;
import dev.mathops.web.host.precalc.canvas.courses.PageAnnouncements;
import dev.mathops.web.host.precalc.canvas.courses.PageAssignments;
import dev.mathops.web.host.precalc.canvas.courses.PageCourse;
import dev.mathops.web.host.precalc.canvas.courses.PageGrades;
import dev.mathops.web.host.precalc.canvas.courses.PageHelp;
import dev.mathops.web.host.precalc.canvas.courses.PageModules;
import dev.mathops.web.host.precalc.canvas.courses.PageNavigating;
import dev.mathops.web.host.precalc.canvas.courses.PageStartHere;
import dev.mathops.web.host.precalc.canvas.courses.PageSurvey;
import dev.mathops.web.host.precalc.canvas.courses.PageSyllabus;
import dev.mathops.web.host.precalc.canvas.courses.PageTopicAssignments;
import dev.mathops.web.host.precalc.canvas.courses.PageTopicModule;
import dev.mathops.web.host.precalc.canvas.courses.PageTopicSkillsReview;
import dev.mathops.web.host.precalc.canvas.courses.PageTopicTargets;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A site that delivers courses in a style similar to Canvas.  This allows testing of content for potential deep
 * integration with Canvas, and provides an alternative if that integration is not permitted.
 */
public final class CanvasSite extends AbstractSite {

    /** A page. */
    private static final String ROOT_PAGE = "home.html";

    /** A page. */
    private static final String LOGIN_PAGE = "login.html";

    /** A page. */
    private static final String ACCOUNT_PAGE = "account.html";

    /** A page. */
    static final String COURSE_HOME_PAGE = "course.html";

    /** A page. */
    private static final String SYLLABUS_PAGE = "syllabus.html";

    /** A page. */
    private static final String ANNOUNCEMENTS_PAGE = "announcements.html";

    /** A page. */
    private static final String MODULES_PAGE = "modules.html";

    /** A page. */
    private static final String ASSIGNMENTS_PAGE = "assignments.html";

    /** A page. */
    private static final String HELP_PAGE = "help.html";

    /** A page. */
    private static final String GRADES_PAGE = "grades.html";

    /** A page. */
    private static final String SURVEY_PAGE = "survey.html";

    /** A page. */
    private static final String START_HERE_PAGE = "start_here.html";

    /** A page. */
    private static final String NAVIGATING_PAGE = "navigating.html";

    /** A CSS filename. */
    private static final String BASE_STYLE_CSS = "basestyle.css";

    /** A CSS filename. */
    private static final String STYLE_CSS = "style.css";

    /** A CSS filename. */
    private static final String FAVICON_ICO = "favicon.ico";

    /** A web page. */
    private static final String SHIBBOLETH_PAGE = "secure/shibboleth.html";

    /** A request parameter name. */
    private static final String TARGET_PARAM = "target";

    /** The interval (ms) between checks of the metadata file. */
    private static final long FILE_SCAN_INTERVAL = 10000L;

    /** A request parameter name. */
    static final String COURSE_PARAM = "course";

    /**
     * Constructs a new {@code CanvasSite}.
     *
     * @param theSite     the website profile
     * @param theSessions the singleton user session repository
     */
    public CanvasSite(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);

        final File www = PathList.getInstance().get(EPath.WWW_PATH);
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

        Log.info("GET ", subpath);

        final String path = this.site.path;

        if (subpath == null || subpath.isEmpty()) {
            final String homePath = makeRootPath(ROOT_PAGE);
            resp.sendRedirect(homePath);
        } else {
            final String maintenanceMsg = isMaintenance(this.site);

            if (maintenanceMsg == null) {
                Log.info("GET ", subpath, " within ", path);

                switch (subpath) {
                    case BASE_STYLE_CSS -> serveCss(req, resp, Page.class, BASE_STYLE_CSS);
                    case STYLE_CSS -> serveCss(req, resp, CanvasSite.class, STYLE_CSS);
                    case FAVICON_ICO -> serveImage(subpath, req, resp);
                    default -> doPageGet(cache, subpath, req, resp);
                }
            } else {
                PageMaintenance.doGet(cache, this, req, resp, maintenanceMsg);
            }
        }
    }

    /**
     * Processes a GET request for a page. Before this method is called, the request will have been verified to be
     * secure and have a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site (not null or empty)
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    private void doPageGet(final Cache cache, final String subpath, final HttpServletRequest req,
                           final HttpServletResponse resp) throws IOException, SQLException {

        final ImmutableSessionInfo session = validateSession(req, resp, null);

        if (session == null) {
            switch (subpath) {
                case LOGIN_PAGE -> PageLogin.doGet(cache, this, req, resp);
                case SHIBBOLETH_PAGE -> doShibbolethLogin(cache, req, resp, null);

                default -> {
                    Log.warning("Unrecognized GET request path: ", subpath);
                    // TODO: How to make login jump to a specified course once completed?
                    final String homePath = makeRootPath(LOGIN_PAGE);
                    resp.sendRedirect(homePath);
                }
            }
        } else {
            final String userId = session.getEffectiveUserId();
            LogBase.setSessionInfo(session.loginSessionId, userId);

            if (subpath.startsWith("courses/")) {
                final int nextSlash = subpath.indexOf('/', 8);
                if (nextSlash == -1) {
                    final String homePath = makeRootPath(ROOT_PAGE);
                    resp.sendRedirect(homePath);
                } else {
                    final String courseId = subpath.substring(8, nextSlash).replace('_', ' ');
                    final String pathWithinCourse = subpath.substring(nextSlash + 1);

                    Log.info("GET '", pathWithinCourse, " within ", courseId, " course");

                    serveCoursePage(cache, courseId, pathWithinCourse, req, resp, session);
                }
            } else {
                // TODO: Other top-level domains before user has selected a course

                switch (subpath) {
                    case ROOT_PAGE -> PageRoot.doGet(cache, this, req, resp, session);

                    default -> {
                        Log.warning("Unrecognized GET request path: ", subpath);
                        final String selectedCourse = req.getParameter(COURSE_PARAM);
                        final String homePath = selectedCourse == null ? makeRootPath(ROOT_PAGE)
                                : makeCoursePath(COURSE_HOME_PAGE, selectedCourse);
                        resp.sendRedirect(homePath);
                    }
                }
            }
        }
    }

    /**
     * Serves a page below "/courses/COURSE_ID/".
     *
     * @param cache    the data cache
     * @param courseId the course ID
     * @param subpath  the subpath (after "/courses/COURSE_ID/")
     * @param req      the request
     * @param resp     the response
     * @param session  the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void serveCoursePage(final Cache cache, final String courseId, final String subpath,
                                 final HttpServletRequest req, final HttpServletResponse resp,
                                 final ImmutableSessionInfo session) throws IOException, SQLException {

        if (subpath.endsWith(BASE_STYLE_CSS)) {
            serveCss(req, resp, Page.class, BASE_STYLE_CSS);
        } else if (subpath.endsWith(STYLE_CSS)) {
            serveCss(req, resp, CanvasSite.class, STYLE_CSS);
        } else if (subpath.endsWith(FAVICON_ICO)) {
            serveImage(FAVICON_ICO, req, resp);
        } else {
            switch (subpath) {
                case ACCOUNT_PAGE -> PageAccount.doGet(cache, this, courseId, req, resp, session);
                case COURSE_HOME_PAGE -> PageCourse.doGet(cache, this, courseId, req, resp, session);
                case SYLLABUS_PAGE -> PageSyllabus.doGet(cache, this, courseId, req, resp, session);
                case ANNOUNCEMENTS_PAGE -> PageAnnouncements.doGet(cache, this, courseId, req, resp, session);
                case MODULES_PAGE -> PageModules.doGet(cache, this, courseId, req, resp, session);
                case ASSIGNMENTS_PAGE -> PageAssignments.doGet(cache, this, courseId, req, resp, session);
                case HELP_PAGE -> PageHelp.doGet(cache, this, courseId, req, resp, session);
                case GRADES_PAGE -> PageGrades.doGet(cache, this, courseId, req, resp, session);
                case SURVEY_PAGE -> PageSurvey.doGet(cache, this, courseId, req, resp, session);

                case START_HERE_PAGE -> PageStartHere.doGet(cache, this, courseId, req, resp, session);
                case NAVIGATING_PAGE -> PageNavigating.doGet(cache, this, courseId, req, resp, session);

                default -> {
                    final Integer moduleNbr = getModuleNbr(subpath);

                    if (moduleNbr == null) {
                        Log.warning("Unrecognized GET request path: ", subpath);
                        final String selectedCourse = req.getParameter(COURSE_PARAM);
                        final String homePath = selectedCourse == null ? makeRootPath(ROOT_PAGE)
                                : makeCoursePath(COURSE_HOME_PAGE, selectedCourse);
                        resp.sendRedirect(homePath);
                    } else {
                        final int slash = subpath.indexOf('/');
                        final String modulePath = subpath.substring(slash + 1);

                        Log.info("Module subpath is ", modulePath);

                        if ("module.html".equals(modulePath)) {
                            PageTopicModule.doGet(cache, this, courseId, moduleNbr, req, resp, session);
                        } else if ("review.html".equals(modulePath)) {
                            PageTopicSkillsReview.doGet(cache, this, courseId, moduleNbr, req, resp, session);
                        } else if ("assignments.html".equals(modulePath)) {
                            PageTopicAssignments.doGet(cache, this, courseId, moduleNbr, req, resp, session);
                        } else if ("targets.html".equals(modulePath)) {
                            PageTopicTargets.doGet(cache, this, courseId, moduleNbr, req, resp, session);
                        } else {
                            Log.warning("Unrecognized GET request path: ", subpath);
                            final String selectedCourse = req.getParameter(COURSE_PARAM);
                            final String homePath = selectedCourse == null ? makeRootPath(ROOT_PAGE)
                                    : makeCoursePath(COURSE_HOME_PAGE, selectedCourse);
                            resp.sendRedirect(homePath);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tests whether the page represents a topic module page.
     *
     * @return the topic module ID if the subpath is a topic module page; null if not
     */
    private static String getTopicModuleId(final String subpath) {

        String result = null;

        if (subpath.startsWith("M") && subpath.endsWith("/module.html")) {
            final int slash = subpath.indexOf('/');
            if (slash > 0) {
                result = subpath.substring(0, slash);
            }
        }

        return result;
    }

    /**
     * Tests whether the page represents a topic module Skills Review page.
     *
     * @return the topic module ID if the subpath is a topic module Skills Review page; null if not
     */
    private static String getReviewModuleId(final String subpath) {

        String result = null;

        if (subpath.startsWith("M") && subpath.endsWith("/review.html")) {
            final int slash = subpath.indexOf('/');
            if (slash > 0) {
                result = subpath.substring(0, slash);
            }
        }

        return result;
    }

    /**
     * Tests whether the page starts with a module number designator like "M5/"
     *
     * @return the module number if so; null if not.
     */
    private static Integer getModuleNbr(final String subpath) {

        Integer result = null;

        if (subpath.startsWith("M")) {
            final int slash = subpath.indexOf('/');
            if (slash > 1) {
                final String str = subpath.substring(0, slash);
                try {
                    result = Integer.parseInt(str);
                } catch (final NumberFormatException ex) {
                    // No action
                }
            }
        }

        return result;
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

        final String path = this.site.path;

        if (subpath == null || subpath.isEmpty()) {
            final String homePath = makeRootPath(ROOT_PAGE);
            resp.sendRedirect(homePath);
        } else {
            Log.info("POST ", subpath, " within ", path);

            final String maintenanceMsg = isMaintenance(this.site);

            if (maintenanceMsg == null) {
                final ImmutableSessionInfo session = validateSession(req, resp, LOGIN_PAGE);

                if (session != null) {
                    final String userId = session.getEffectiveUserId();
                    LogBase.setSessionInfo(session.loginSessionId, userId);

                    switch (subpath) {
                        case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);

                        default -> {
                            Log.warning("Unrecognized POST request path: ", subpath);
                            final String selectedCourse = req.getParameter(COURSE_HOME_PAGE);
                            final String homePath = selectedCourse == null ? makeRootPath(ROOT_PAGE)
                                    : makeCoursePath(COURSE_HOME_PAGE, selectedCourse);
                            resp.sendRedirect(homePath);
                        }
                    }
                }
            } else {
                PageMaintenance.doGet(cache, this, req, resp, maintenanceMsg);
            }
        }
    }

    /**
     * Serves a CSS file.
     *
     * @param req      the request
     * @param resp     the response
     * @param cls      the class in whose package the CSS file is found
     * @param filename the CSS file name
     * @throws IOException if there is an error writing the response
     */
    private static void serveCss(final ServletRequest req, final HttpServletResponse resp, final Class<?> cls,
                                 final String filename) throws IOException {

        final byte[] cssBytes = FileLoader.loadFileAsBytes(cls, filename, true);

        sendReply(req, resp, MIME_TEXT_CSS, cssBytes);
    }

    /**
     * Processes any submissions by the role controls (call on POST).
     *
     * @param cache   the data cache
     * @param req     the HTTP request
     * @param resp    the HTTP response
     * @param session the session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void processRoleControls(final Cache cache, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        UserInfoBar.processRoleControls(cache, req, session);

        final String target = req.getParameter(TARGET_PARAM);

        if (isParamInvalid(target)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  target='", target, "'");
//            PageError.doGet(cache, this, req, resp, session, "No target provided with role control");
        } else if (target == null) {
//            PageError.doGet(cache, this, req, resp, session, "No target provided with role control");
        } else {
            resp.sendRedirect(target);
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

        Log.info("Shibboleth login attempt");

        ImmutableSessionInfo sess = session;

        if (sess == null) {
            sess = processShibbolethLogin(cache, req);
        }

        final String selectedCourse = req.getParameter(COURSE_PARAM);

        final String redirect;
        if (sess == null) {
            // Login failed - return to login page
            // TODO: How to make login jump to a specified course once completed?
            redirect = makeRootPath(LOGIN_PAGE);
        } else {
            redirect = selectedCourse == null ? makeRootPath(ROOT_PAGE) :
                    makeCoursePath(COURSE_HOME_PAGE, selectedCourse);

            // Install the session ID cookie in the response
            final String serverName = req.getServerName();
            Log.info("Adding session ID cookie ", serverName);
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(serverName);
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);
        Log.info("Redirecting to ", redirect);
    }

    /**
     * Given the path of this site, generates the path of a "root-level" page.
     *
     * @param page the page, like "something.html"
     * @return the path
     */
    public String makeRootPath(final String page) {

        final String result;

        final String path = this.site.path;
        final boolean endsWithSlash = path.endsWith(CoreConstants.SLASH);

        if (endsWithSlash) {
            result = SimpleBuilder.concat(path, page);
        } else {
            result = SimpleBuilder.concat(path, "/", page);
        }

        return result;
    }

    /**
     * Given the path of this site, generates the path of a page.
     *
     * @param page     the page, like "something.html"
     * @param courseId the course ID
     * @return the path
     */
    public String makeCoursePath(final String page, final String courseId) {

        final String result;

        final String path = this.site.path;
        final boolean endsWithSlash = path.endsWith(CoreConstants.SLASH);

        final String urlCourse = courseId.replace(' ', '_');
        if (endsWithSlash) {
            result = SimpleBuilder.concat(path, "courses/", urlCourse, "/", page);
        } else {
            result = SimpleBuilder.concat(path, "/courses/", urlCourse, "/", page);
        }

        return result;
    }

    /**
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    @Override
    protected ELiveRefreshes getLiveRefreshes() {

        return ELiveRefreshes.ALL;
    }

    /**
     * Generates the site title based on the context.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Precalculus Program";
    }
}
