package dev.mathops.web.site;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.installation.Installation;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.SessionManager;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.front.IMidController;
import dev.mathops.web.site.admin.AdminRootSite;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.cfm.CfmSite;
import dev.mathops.web.site.course.CourseSite;
import dev.mathops.web.site.help.HelpSite;
import dev.mathops.web.site.html.challengeexam.ChallengeExamSessionStore;
import dev.mathops.web.site.html.hw.HomeworkSessionStore;
import dev.mathops.web.site.html.lta.LtaSessionStore;
import dev.mathops.web.site.html.pastexam.PastExamSessionStore;
import dev.mathops.web.site.html.pastla.PastLtaSessionStore;
import dev.mathops.web.site.html.placementexam.PlacementExamSessionStore;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;
import dev.mathops.web.site.landing.LandingSite;
import dev.mathops.web.site.lti.LtiSite;
import dev.mathops.web.site.lti.canvascourse.CanvasCourseSite;
import dev.mathops.web.site.placement.PlacementRedirector;
import dev.mathops.web.site.placement.main.MathPlacementSite;
import dev.mathops.web.site.proctoring.media.ProctoringMediaSite;
import dev.mathops.web.site.proctoring.student.ProctoringSite;
import dev.mathops.web.site.ramwork.RamWorkSite;
import dev.mathops.web.site.reporting.ReportingSite;
import dev.mathops.web.site.root.EmptyRootSite;
import dev.mathops.web.site.root.PrecalcRootSite;
import dev.mathops.web.site.scheduling.SchedulingSite;
import dev.mathops.web.site.testing.TestingCenterSite;
import dev.mathops.web.site.tutorial.elm.ElmTutorialSite;
import dev.mathops.web.site.tutorial.precalc.PrecalcTutorialSite;
import dev.mathops.web.site.txn.Site;
import dev.mathops.web.site.video.VideoSite;
import dev.mathops.web.webservice.WebServiceSite;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A mid-controller that handles requests whose request paths don't begin with "/www/", "/mgt/", "/ws/", or "/lti".
 */
public final class WebMidController implements IMidController {

    /** The servlet information string. */
    private static final String INFO = Res.get(Res.INFO);

    /** Purge interval, in milliseconds (10 minutes). */
    private static final long PURGE_INTERVAL = 10 * 60000;

    /** A commonly used character. */
    private static final int SLASH = '/';

    /** Map from host to map from site to its implementation. */
    private final Map<String, SortedMap<String, AbstractSite>> sites;

    /** A timer to track servlet timing statistics. */
    private final ServletTimer timer;

    /** The singleton user session repository. */
    private final ISessionManager sessions;

    /** Time when sessions were last purged. */
    private long lastPurge;

    /**
     * Constructs a new {@code WebSiteMidController}.
     *
     * @param cache  the data cache (not stored in this object - used only during initialization)
     * @param config the servlet context in which the servlet is being initialized
     * @throws ServletException if the servlet could not be initialized
     * @throws SQLException     if there is an error accessing the database
     */
    public WebMidController(final Cache cache, final ServletConfig config) throws ServletException, SQLException {

        final ServletContext servletContext = config.getServletContext();

        final Installation installation = (Installation) servletContext.getAttribute("Installation");

        this.sites = new TreeMap<>();
        this.timer = ServletTimer.getInstance();

        final String dir = installation.getBaseDirPath();
        final File baseDir = new File(dir);

        if (!baseDir.exists() && !baseDir.mkdirs()) {
            final String msg = Res.fmt(Res.CANT_CREATE_DIR, dir);
            throw new ServletException(msg);
        }

        if (!baseDir.isDirectory()) {
            final String msg = Res.fmt(Res.BAD_DIR, dir);
            throw new ServletException(msg);
        }

        final String startingMsg = Res.fmt(Res.STARTING, INFO, dir);
        Log.info(startingMsg);

        // Initialize paths list for data and source paths
        PathList.init(baseDir);

        // Initialize the context map
        final ContextMap map = ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        // Initialize the session manager
        this.sessions = SessionManager.getInstance();

        final String[] hosts = map.getWebHosts();
        final List<String> webHosts = Arrays.asList(hosts);

        // Create and register the sites

        if (webHosts.contains(Contexts.PACE_HOST)) {
            add(map, Contexts.PACE_HOST, Contexts.ROOT_PATH, RedirectToPrecalcSite.class);
            add(map, Contexts.PACE_HOST, Contexts.INSTRUCTION_PATH, RedirectToPrecalcSite.class);
        }

        if (webHosts.contains(Contexts.PRECALC_HOST)) {
            add(map, Contexts.PRECALC_HOST, Contexts.ROOT_PATH, PrecalcRootSite.class);
            add(map, Contexts.PRECALC_HOST, Contexts.INSTRUCTION_PATH, CourseSite.class);
            add(map, Contexts.PRECALC_HOST, Contexts.WELCOME_PATH, LandingSite.class);
            add(map, Contexts.PRECALC_HOST, Contexts.CANVAS_PATH, CanvasSite.class);
        }

        if (webHosts.contains(Contexts.PLACEMENT_HOST)) {
            add(map, Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH, PlacementRedirector.class);
            add(map, Contexts.PLACEMENT_HOST, Contexts.ELM_TUTORIAL_PATH, ElmTutorialSite.class);
            add(map, Contexts.PLACEMENT_HOST, Contexts.PRECALC_TUTORIAL_PATH, PrecalcTutorialSite.class);
            add(map, Contexts.PLACEMENT_HOST, Contexts.WELCOME_PATH, MathPlacementSite.class);
        }

        if (webHosts.contains(Contexts.COURSE_HOST)) {
            add(map, Contexts.COURSE_HOST, Contexts.ROOT_PATH, EmptyRootSite.class);
            add(map, Contexts.COURSE_HOST, Contexts.HELP_PATH, HelpSite.class);
            add(map, Contexts.COURSE_HOST, Contexts.LTI_PATH, LtiSite.class);
            add(map, Contexts.COURSE_HOST, Contexts.CSU_MATH_COURSE_MGR_PATH, CanvasCourseSite.class);
            add(map, Contexts.COURSE_HOST, Contexts.MPS_PATH, ProctoringSite.class);
            add(map, Contexts.COURSE_HOST, Contexts.VIDEO_PATH, VideoSite.class);
            add(map, Contexts.COURSE_HOST, Contexts.CFM_PATH, CfmSite.class);
        }

        if (webHosts.contains(Contexts.TESTING_HOST)) {
            add(map, Contexts.TESTING_HOST, Contexts.ROOT_PATH, AdminRootSite.class);
            add(map, Contexts.TESTING_HOST, Contexts.ADMINSYS_PATH, AdminSite.class);
            add(map, Contexts.TESTING_HOST, Contexts.TESTING_CENTER_PATH, TestingCenterSite.class);
            add(map, Contexts.TESTING_HOST, Contexts.RAMWORK_PATH, RamWorkSite.class);
            add(map, Contexts.TESTING_HOST, Contexts.REPORTING_PATH, ReportingSite.class);
            add(map, Contexts.TESTING_HOST, Contexts.SCHEDULING_PATH, SchedulingSite.class);
            add(map, Contexts.TESTING_HOST, Contexts.TXN_PATH, Site.class);
            add(map, Contexts.TESTING_HOST, Contexts.WEBSVC_PATH, WebServiceSite.class);
        }

        if (webHosts.contains(Contexts.NIBBLER_HOST)) {
            add(map, Contexts.NIBBLER_HOST, Contexts.ROOT_PATH, EmptyRootSite.class);
            add(map, Contexts.NIBBLER_HOST, Contexts.MPSMEDIA_PATH, ProctoringMediaSite.class);
        }

        // Load any sessions persisted from a prior shutdown
        final File session = new File(baseDir, "sessions");
        this.sessions.load(session);

        ChallengeExamSessionStore.getInstance().restore(cache, session);
        PlacementExamSessionStore.getInstance().restore(cache, session);
        UnitExamSessionStore.getInstance().restore(cache, session);
        ReviewExamSessionStore.getInstance().restore(cache, session);
        LtaSessionStore.getInstance().restore(cache, session);
        HomeworkSessionStore.getInstance().restore(cache, session);
        PastExamSessionStore.getInstance().restore(cache, session);
        PastLtaSessionStore.getInstance().restore(cache, session);

        this.lastPurge = System.currentTimeMillis();

        final String startedMsg = Res.fmt(Res.STARTED, INFO);
        Log.info(startedMsg);
    }

    /**
     * Adds a site by creating the website profile for a specified host and port, then creating an instance of a site
     * from its class and registering it with the controller.
     *
     * @param map  the context map
     * @param host the host
     * @param path the path
     * @param cls  the site class
     */
    private void add(final ContextMap map, final String host, final String path,
                     final Class<? extends AbstractSite> cls) {

        final WebSiteProfile profile = map.getWebSiteProfile(host, path);

        if (profile == null) {
            Log.warning("No web profile for ", host, CoreConstants.COLON, path);
        } else {
            final Constructor<? extends AbstractSite> constr;
            try {
                constr = cls.getConstructor(WebSiteProfile.class, ISessionManager.class);
                final AbstractSite site = constr.newInstance(profile, this.sessions);
                regSite(site);
            } catch (final NoSuchMethodException | SecurityException | InstantiationException
                           | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Log.warning("Failed to create web site for ", host, CoreConstants.COLON, path, ex);
            }
        }
    }

    /**
     * Registers a site to handle requests directed at a particular host, and to a host with "_dev" appended to the
     * given hostname.
     *
     * @param site the site
     */
    private void regSite(final AbstractSite site) {

        final String host = site.siteProfile.host;
        final SortedMap<String, AbstractSite> map = this.sites.computeIfAbsent(host, s -> new TreeMap<>());
        final String path = site.siteProfile.path;

//        Log.info("Registering site for host '", host, "' path '", path, "'");

        map.put(path, site);
    }

    /**
     * Services a request. The connection is known to be secure (HTTPS) at this point.
     *
     * @param req         the request
     * @param resp        the response
     * @param requestPath the complete request path
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void serviceSecure(final HttpServletRequest req, final HttpServletResponse resp, final String requestPath)
            throws IOException {

        final ESiteType type;
        final String reqHost;
        if (req.getServerName().contains("dev.")) {
            reqHost = req.getServerName().replace("dev.", CoreConstants.DOT);
            type = ESiteType.DEV;
        } else if (req.getServerName().contains("test")) {
            reqHost = req.getServerName().replace("test.", CoreConstants.DOT);
            type = ESiteType.TEST;
        } else {
            reqHost = req.getServerName();
            type = ESiteType.PROD;
        }

        final String reqPath = req.getServletPath();

//         Log.info(req.getMethod() + " request for ", reqHost, reqPath);

        final AbstractSite site = findSite(reqHost, reqPath);

        if (site == null) {
            if ("/ShibbolethError.html".equals(reqPath)) {
                showShibbolethError(req, resp);
            } else {
                Log.warning("Unrecognized path: ", reqPath);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
//            Log.info(site.getClass().getSimpleName() + " handling " + req.getMethod());

            final DbContext ctx = site.getPrimaryDbContext();

            try {
                final long timerStart = System.currentTimeMillis();

                final DbConnection conn = ctx.checkOutConnection();
                final DbProfile siteProfile = site.getDbProfile();

                // The lifetime of this cache should be the page request only.
                final Cache cache = new Cache(siteProfile, conn);

                try {
                    if (timerStart > this.lastPurge + PURGE_INTERVAL) {

                        Log.info("Purging expired HTML sessions");

                        ChallengeExamSessionStore.getInstance().purgeExpired(cache);
                        PlacementExamSessionStore.getInstance().purgeExpired(cache);
                        UnitExamSessionStore.getInstance().purgeExpired(cache);
                        ReviewExamSessionStore.getInstance().purgeExpired(cache);
                        HomeworkSessionStore.getInstance().purgeExpired();
                        PastExamSessionStore.getInstance().purgeExpired();

                        this.lastPurge = timerStart;
                    }

                    final int pathLen = site.siteProfile.path.length();
                    String subpath = reqPath.substring(pathLen);

                    if (req.isSecure()) {

                        if (!subpath.isEmpty() && (int) subpath.charAt(0) == SLASH) {
                            subpath = subpath.substring(1);
                        }

                        final long start = System.currentTimeMillis();
                        final String reqMethod = req.getMethod();

                        if ("GET".equals(reqMethod)) {
                            site.doGet(cache, subpath, type, req, resp);
                        } else if ("POST".equals(reqMethod)) {
                            site.doPost(cache, subpath, type, req, resp);
                        } else {
                            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        }
                        final long elapsed = System.currentTimeMillis() - start;
                        this.timer.recordAccess(siteProfile, subpath, elapsed);
                    } else if (Contexts.TESTING_HOST.equals(reqHost) || Contexts.ONLINE_HOST.equals(reqHost)
                                                                        && reqPath.startsWith(Contexts.TESTING_CENTER_PATH)) {

                        if (!subpath.isEmpty() && (int) subpath.charAt(0) == SLASH) {
                            subpath = subpath.substring(1);
                        }

                        final long start = System.currentTimeMillis();
                        site.doPost(cache, subpath, type, req, resp);
                        final long elapsed = System.currentTimeMillis() - start;
                        this.timer.recordAccess(siteProfile, subpath, elapsed);
                    } else {
                        // Site requires secure connections
                        final String loc = "https://" + req.getServerName() + req.getServletPath();
                        resp.setHeader("Location", loc);
                        resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    }
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (final IOException | RuntimeException ex) {
                Log.warning(ex);
                throw ex;
            }
        }
    }

    /**
     * Services a request. The connection is known to be insecure (HTTP) at this point. If the request maps to a real
     * website that requires a secure connection, the client is redirected to a secure (https) URL. If the site permits
     * insecure connections, it can process this request directly. If no site maps to the URL, a "404" response is
     * sent.
     *
     * @param req         the request
     * @param resp        the response
     * @param requestPath the complete request path
     * @throws IOException if there is an error writing the response
     */
    @Override
    public void serviceInsecure(final HttpServletRequest req, final HttpServletResponse resp, final String requestPath)
            throws IOException {

        final ESiteType type;
        final String reqHost;
        if (req.getServerName().contains("dev.")) {
            reqHost = req.getServerName().replace("dev.", CoreConstants.DOT);
            type = ESiteType.DEV;
        } else if (req.getServerName().contains("test")) {
            reqHost = req.getServerName().replace("test.", CoreConstants.DOT);
            type = ESiteType.TEST;
        } else {
            reqHost = req.getServerName();
            type = ESiteType.PROD;
        }

        final String reqPath = req.getServletPath();

        // Log.info("Request for ", reqHost, reqPath);

        final AbstractSite site = findSite(reqHost, reqPath);

        if (site == null) {
            Log.warning("Unrecognized path: ", reqPath);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final DbContext ctx = site.getPrimaryDbContext();

            try {
                final DbConnection conn = ctx.checkOutConnection();
                final DbProfile siteProfile = site.getDbProfile();

                // The lifetime of this cache should be the page request only.
                final Cache cache = new Cache(siteProfile, conn);

                try {
                    if (Contexts.TESTING_HOST.equals(reqHost)) {

                        final int pathLen = site.siteProfile.path.length();
                        String subpath = reqPath.substring(pathLen);

                        if (!subpath.isEmpty() && (int) subpath.charAt(0) == SLASH) {
                            subpath = subpath.substring(1);
                        }

                        final long start = System.currentTimeMillis();
                        final String reqMethod = req.getMethod();

                        if ("GET".equals(reqMethod)) {
                            site.doGet(cache, subpath, type, req, resp);
                        } else if ("POST".equals(reqMethod)) {
                            site.doPost(cache, subpath, type, req, resp);
                        } else {
                            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        }
                        final long elapsed = System.currentTimeMillis() - start;
                        this.timer.recordAccess(siteProfile, subpath, elapsed);
                    } else {
                        // Site requires secure connections
                        final String loc = "https://" + req.getServerName() + req.getServletPath();
                        resp.setHeader("Location", loc);
                        resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    }
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (final IOException | RuntimeException ex) {
                Log.warning(ex);
                throw ex;
            }
        }
    }

    /**
     * Looks up the site based on the leading part of the path info.
     *
     * @param host the host of the site to retrieve
     * @param path the path info (the portion of the URL after that part that was used to determine the servlet)
     * @return the matching site, or {@code null} if no site maps to the given path
     */
    private AbstractSite findSite(final String host, final String path) {

        AbstractSite site = null;
        int len = 0;

//        Log.info("Finding site for host '", host, "' path '", path, "'");

        final SortedMap<String, AbstractSite> siteList = this.sites.get(host);

        if (siteList != null) {

            final int pathLen = path.length();

            for (final AbstractSite test : siteList.values()) {

                final String testPath = test.siteProfile.path;
                final int testPathLen = testPath.length();

                if (testPathLen > len) {
                    if ((pathLen > testPathLen && path.startsWith(testPath)
                         && (int) path.charAt(testPathLen) == SLASH) || path.equals(testPath)) {
                        site = test;
                        len = testPathLen;
                    }
                }
            }

            if (site == null) {
                // Attempt to find a default root site to handle unknown paths
                for (final AbstractSite test : siteList.values()) {
                    if (Contexts.ROOT_PATH.equals(test.siteProfile.path)) {
                        site = test;
                        break;
                    }
                }
            }
        }

        return site;
    }

    /**
     * Displays a Shibboleth error.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void showShibbolethError(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final String now = req.getParameter("now");
        final String requestURL = req.getParameter("requestURL");
        final String errorType = req.getParameter("errorType");
        final String errorText = req.getParameter("errorText");
        final String relayState = req.getParameter("RelayState");
        final String contactEmail = req.getParameter("contactEmail");
        final String contactName = req.getParameter("contactName");
        final String entityID = req.getParameter("entityID");
        final String eventType = req.getParameter("eventType");

        final HtmlBuilder htm = new HtmlBuilder(2000);

        Page.startPage(htm, "Shibboleth Error", false, false);

        htm.addln("<body>");

        htm.sH(3).add("A login error has occurred.").eH(3);

        htm.sP().add(errorText).eP();

        htm.sTable();
        htm.sTr().sTh().add("Date/Time:").eTh().sTd().add(now).eTd().eTr();
        htm.sTr().sTh().add("Request URL").eTh().sTd().add(requestURL).eTd().eTr();
        htm.sTr().sTh().add("Error type:").eTh().sTd().add(errorType).eTd().eTr();
        htm.sTr().sTh().add("Relay State:").eTh().sTd().add(relayState).eTd().eTr();
        htm.sTr().sTh().add("Contact Name:").eTh().sTd().add(contactName).eTd().eTr();
        htm.sTr().sTh().add("Contact Email:").eTh().sTd().add(contactEmail).eTd().eTr();
        htm.sTr().sTh().add("Entity ID:").eTh().sTd().add(entityID).eTd().eTr();
        htm.sTr().sTh().add("Event Type:").eTh().sTd().add(eventType).eTd().eTr();
        htm.eTable();

        htm.addln("</body>");
        Page.endPage(htm);
        final String htmStr = htm.toString();
        final byte[] htmBytes = htmStr.getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htmBytes);
    }
}
