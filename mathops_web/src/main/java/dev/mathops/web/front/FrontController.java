package dev.mathops.web.front;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EMimeType;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.installation.Installation;
import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.session.SessionManager;
import dev.mathops.web.site.WebMidController;
import dev.mathops.web.site.html.challengeexam.ChallengeExamSessionStore;
import dev.mathops.web.site.html.hw.HomeworkSessionStore;
import dev.mathops.web.site.html.pastexam.PastExamSessionStore;
import dev.mathops.web.site.html.placementexam.PlacementExamSessionStore;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.sql.SQLException;
import java.util.Locale;

/**
 * The primary front controller servlet. This servlet handles all requests for files from the "/public" path, routes
 * requests under the "/mgt" path to the management servlet handler, routes requests to the "/ws" path to the web
 * services handler, and routes all other requests to the website handler.
 *
 * <p>
 * The front controller requires that ALL connections to the server be secured. Any connection that is not secured is
 * redirected to the request path prefixed by "https://".
 */
public final class FrontController extends HttpServlet {

    /** Start of a path for a public file. */
    private static final String PUBLIC_PATH_START = "/www/";

//    /** Interval between open request report (1 hour). */
//    public static final long REPORT_INTERVAL = 60L * 60L * 1000L;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3089749430061848046L;

    /** Installation property with the path to the public file directory. */
    private static final String PUBLIC_DIR_PROPERTY = "public-dir";

    /** The default public directory, used when none specified. */
    private static final String DEFAULT_PUBLIC_DIR = "/opt/public";

    /** The servlet configuration. */
    private ServletConfig servletConfig;

    /** The servlet context. */
    private ServletContext servletContext;

    /** The server instance. */
    private ServerInstance serverInstance;

    /** The website mid-controller. */
    private IMidController webMidController;

    /** The public directory configured for the server instance. */
    private File publicDir;

    /** Count of hits from blacklisted sites. */
    private int blacklist;

    /**
     * Constructs a new {@code FrontController}.
     */
    public FrontController() {

        super();
    }

    /**
     * Initializes the servlet.
     *
     * @param config the servlet context in which the servlet is being initialized
     * @throws ServletException if the servlet could not be initialized
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {

        this.servletConfig = config;
        this.servletContext = config.getServletContext();

        final Installation installation = (Installation) this.servletContext.getAttribute("Installation");

        this.serverInstance = ServerInstance.get(installation);

        // This gets called from a thread different from the context initialization, so store the thread-local
        // installation for logging

        Log.info(Res.fmt(Res.SERVLET_INIT, this.servletContext.getServerInfo()));

        final String buildDateTime = BuildDateTime.get().value;
        if (buildDateTime != null) {
            Log.info(Res.fmt(Res.BUILD_DATETIME, buildDateTime));
        }

        this.publicDir = this.serverInstance.getInstallation().extractFileProperty(PUBLIC_DIR_PROPERTY,
                new File(DEFAULT_PUBLIC_DIR));

        final DbProfile dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.BATCH_PATH);
        if (dbProfile == null) {
            throw new ServletException("No 'batch' code profile configured");
        }

        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                this.webMidController = new WebMidController(cache, config);
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            throw new ServletException("Unable to connect to to database", ex);
        }

        Log.info(Res.get(Res.SERVLET_INITIALIZED));
    }

    /**
     * Gets the servlet configuration.
     *
     * @return the servlet configuration
     */
    @Override
    public ServletConfig getServletConfig() {

        return this.servletConfig;
    }

    /**
     * Gets the servlet context under which this servlet was initialized.
     *
     * @return the servlet context
     */
    @Override
    public ServletContext getServletContext() {

        return this.servletContext;
    }

    /**
     * Gets the installation.
     *
     * @return the installation
     */
    private Installation getInstallation() {

        return this.serverInstance.getInstallation();
    }

    /**
     * Called when the servlet container unloads the servlet.
     * <p>
     * This method reduces the reference count on the installation - when that count reaches zero, the installation's
     * JMX server is allowed to stop
     */
    @Override
    public void destroy() {

        InstructionalCache.getInstance().die();

        final Installation installation = getInstallation();
        final File dir = new File(installation.baseDir, "sessions");

        SessionManager.getInstance().persist(dir);
        ChallengeExamSessionStore.getInstance().persist(dir);
        PlacementExamSessionStore.getInstance().persist(dir);
        UnitExamSessionStore.getInstance().persist(dir);
        ReviewExamSessionStore.getInstance().persist(dir);
        HomeworkSessionStore.getInstance().persist(dir);
        PastExamSessionStore.getInstance().persist(dir);

        Log.info(Res.get(Res.SERVLET_TERMINATED));
        Log.fine(CoreConstants.EMPTY);
    }

    /**
     * Gets the servlet information string.
     *
     * @return the information string
     */
    @Override
    public String getServletInfo() {

        return Res.get(Res.SERVLET_TITLE);
    }

    /**
     * Processes a request. The first part of the request path (between the first and second '/') is used to determine
     * the site, then if the site is valid, the request is dispatched to the site processor.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException      if there is an error writing the response
     * @throws ServletException if there is an exception processing the request
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");

        final String remote = req.getRemoteAddr();
        if (remote != null && remote.startsWith("100.27.42.")) {
            ++this.blacklist;
            if (this.blacklist % 100 == 1) {
                Log.warning("Connection from blacklisted Amazon AWS site: ", remote);
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

        } else if (remote != null && remote.startsWith("116.206.196.")) {
            ++this.blacklist;
            if (this.blacklist % 100 == 1) {
                Log.warning("Connection from blacklisted Biznet site: ", remote);
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

        } else if (remote != null && remote.startsWith("34.237.25.")) {
            ++this.blacklist;
            if (this.blacklist % 100 == 1) {
                Log.warning("Connection from blacklisted Amazon AWS site: ", remote);
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

        } else {
            try {
                final String requestHost = ServletUtils.getHost(req);
                final String requestPath = ServletUtils.getPath(req);
                LogBase.setHostPath(requestHost, requestPath, req.getRemoteAddr());

                try {
                    if (req.isSecure()) {
                        serviceSecure(requestPath, req, resp);
                    } else if ("http".equals(req.getScheme())) {
                        serviceInsecure(requestPath, req, resp);
                    } else {
                        Log.warning(Res.fmt(Res.BAD_SCHEME, req.getScheme()));
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                } finally {
                    LogBase.setHostPath(null, null, null);
                }
            } catch (final IOException ex) {
                // Make sure unexpected exceptions get logged rather than silently failing
                Log.severe(ex);
                throw ex;
            }
        }
    }

    /**
     * Processes a request when it is known the connection was secured. The first part of the request path is used to
     * determine whether the request is for a public file, or if not, to determine the mid-controller to which to
     * forward the request.
     *
     * @param requestPath the request path
     * @param req         the HTTP servlet request
     * @param resp        the HTTP servlet response
     * @throws IOException      if there is an error writing the response
     * @throws ServletException if there is an exception processing the request
     */
    private void serviceSecure(final String requestPath, final HttpServletRequest req,
                               final HttpServletResponse resp) throws IOException, ServletException {

        // Log.info("Servicing secure request: " + requestPath);

        if (requestPath.startsWith(PUBLIC_PATH_START)) {
            servicePublic(req, resp, requestPath);
        } else {
            this.webMidController.serviceSecure(req, resp, requestPath);
        }
    }

    /**
     * Processes a request when it is known the connection is not secure. The first part of the request path is used to
     * determine whether the request is for a public file, or if not, to determine the mid-controller to which to
     * forward the request.
     *
     * @param requestPath the request path
     * @param req         the HTTP servlet request
     * @param resp        the HTTP servlet response
     * @throws IOException      if there is an error writing the response
     * @throws ServletException if there is an exception processing the request
     */
    private void serviceInsecure(final String requestPath, final HttpServletRequest req,
                                 final HttpServletResponse resp) throws IOException, ServletException {

        // Log.info("Servicing insecure request: " + requestPath);

        if (requestPath.startsWith(PUBLIC_PATH_START)) {
            // Public files may be served insecurely
            servicePublic(req, resp, requestPath);
        } else {
            this.webMidController.serviceInsecure(req, resp, requestPath);
        }
    }

    /**
     * Services a request to the "/www" path - only GET requests are supported.
     *
     * @param req         the HTTP servlet request
     * @param resp        the HTTP servlet response
     * @param requestPath the full request path
     * @throws IOException if there is an error writing the response
     */
    private void servicePublic(final ServletRequest req, final HttpServletResponse resp,
                               final String requestPath) throws IOException {

        Log.warning("SERVICING PUBLIC - APACHE SHOULD HAVE GOTTEN THIS: ", requestPath);

        // final String subpath = requestPath.substring(PUBLIC_PATH_START.length());

        final File file = new File(this.publicDir, requestPath);

        Log.info("Checking for '", file.getAbsolutePath(), "'");

        if (file.exists()) {
            final byte[] data = FileLoader.loadFileAsBytes(file, true);

            if (data == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                final String fname = file.getName().toLowerCase(Locale.ROOT);
                final int lastDot = fname.lastIndexOf('.');
                EMimeType mime = EMimeType.TEXTPLAIN;
                if (lastDot != -1) {
                    final String ext = fname.substring(lastDot + 1);
                    mime = EMimeType.forExtension(ext);
                }

                resp.setContentType(mime.mime);
                resp.setCharacterEncoding("UTF-8");
                resp.setContentLength(data.length);
                resp.setLocale(req.getLocale());

                try (final OutputStream out = resp.getOutputStream()) {
                    out.write(data);
                } catch (final IOException ex) {
                    if (!"ClientAbortException".equals(ex.getClass().getSimpleName())) {
                        throw ex;
                    }
                }
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
