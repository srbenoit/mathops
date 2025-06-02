package dev.mathops.web.host.nibbler;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EMimeType;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.Installation;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.session.SessionManager;
import dev.mathops.web.front.BuildDateTime;
import dev.mathops.web.front.IMidController;
import dev.mathops.web.front.ServletUtils;
import dev.mathops.web.site.html.challengeexam.ChallengeExamSessionStore;
import dev.mathops.web.site.html.hw.HomeworkSessionStore;
import dev.mathops.web.site.html.lta.LtaSessionStore;
import dev.mathops.web.site.html.pastexam.PastExamSessionStore;
import dev.mathops.web.site.html.pastla.PastLtaSessionStore;
import dev.mathops.web.site.html.placementexam.PlacementExamSessionStore;
import dev.mathops.web.site.html.reviewexam.ReviewExamSessionStore;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.Locale;

/**
 * The front controller for the "nibbler.[domain]" host.
 *
 * <p>
 * The front controller requires that ALL connections to the server be secured. Any connection that is not secured is
 * redirected to the request path prefixed by "https://".
 */
public final class NibblerFrontController extends HttpServlet {

    /** Start of a path for a public file. */
    private static final String PUBLIC_PATH_START = "/www/";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3089749430061848046L;

    /** Installation property with the path to the public file directory. */
    private static final String PUBLIC_DIR_PROPERTY = "public-dir";

    /** The default public directory, used when none specified. */
    private static final String DEFAULT_PUBLIC_DIR = "/opt/public";

    /** A commonly used character. */
    private static final char DOT = '.';

    /** The servlet configuration. */
    private ServletConfig servletConfig = null;

    /** The servlet context. */
    private ServletContext servletContext = null;

    /** The installation. */
    private Installation installation = null;

    /** The website mid-controller. */
    private IMidController nibblerMidController = null;

    /** The public directory configured for the server instance. */
    private File publicDir = null;

    /** Count of hits from blacklisted sites. */
    private int blacklist = 0;

    /**
     * Constructs a new {@code PlacementFrontController}.
     */
    public NibblerFrontController() {

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

        this.installation = (Installation) this.servletContext.getAttribute("Installation");

        // This gets called from a thread different from the context initialization, so store the thread-local
        // installation for logging

        final String serverInfo = this.servletContext.getServerInfo();
        final String initMsg = Res.fmt(Res.SERVLET_INIT, serverInfo);
        Log.info(initMsg);

        final String buildDateTime = BuildDateTime.getValue();
        if (buildDateTime != null) {
            final String buildMsg = Res.fmt(Res.BUILD_DATETIME, buildDateTime);
            Log.info(buildMsg);
        }

        this.publicDir = this.installation.extractFileProperty(PUBLIC_DIR_PROPERTY, new File(DEFAULT_PUBLIC_DIR));

        final Profile dbProfile = DatabaseConfig.getDefault().getCodeProfile(Contexts.BATCH_PATH);
        if (dbProfile == null) {
            throw new ServletException("No 'batch' code profile configured");
        }

        this.nibblerMidController = new NibblerMidController(config);

        final String initializedMsg = Res.get(Res.SERVLET_INITIALIZED);
        Log.info(initializedMsg);
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

        return this.installation;
    }

    /**
     * Called when the servlet container unloads the servlet.
     * <p>
     * This method reduces the reference count on the installation - when that count reaches zero, the installation's
     * JMX server is allowed to stop
     */
    @Override
    public void destroy() {

        final File baseDir = this.installation.getBaseDir();
        final File dir = new File(baseDir, "sessions");

        SessionManager.getInstance().persist(dir);
        ChallengeExamSessionStore.getInstance().persist(dir);
        PlacementExamSessionStore.getInstance().persist(dir);
        UnitExamSessionStore.getInstance().persist(dir);
        ReviewExamSessionStore.getInstance().persist(dir);
        LtaSessionStore.getInstance().persist(dir);
        HomeworkSessionStore.getInstance().persist(dir);
        PastExamSessionStore.getInstance().persist(dir);
        PastLtaSessionStore.getInstance().persist(dir);

        final String terminatedMsg = Res.get(Res.SERVLET_TERMINATED);
        Log.info(terminatedMsg);
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
        if (remote != null && (remote.startsWith("100.27.42.") || remote.startsWith("34.237.25."))) {
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

        } else {
            try {
                final String requestHost = ServletUtils.getHost(req);
                final String requestPath = ServletUtils.getPath(req);
                LogBase.setHostPath(requestHost, requestPath, remote);

                try {
                    if (req.isSecure()) {
                        serviceSecure(requestPath, req, resp);
                    } else {
                        final String reqScheme = req.getScheme();
                        if ("http".equals(reqScheme)) {
                            serviceInsecure(requestPath, req, resp);
                        } else {
                            final String msg = Res.fmt(
                                    Res.BAD_SCHEME, reqScheme);
                            Log.warning(msg);
                            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                        }
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
     * @throws IOException if there is an error writing the response
     */
    private void serviceSecure(final String requestPath, final HttpServletRequest req,
                               final HttpServletResponse resp) throws IOException {

        // Log.info("Servicing secure request: " + requestPath);

        if (requestPath.startsWith(PUBLIC_PATH_START)) {
            servicePublic(req, resp, requestPath);
        } else {
            this.nibblerMidController.serviceSecure(req, resp, requestPath);
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
     * @throws IOException if there is an error writing the response
     */
    private void serviceInsecure(final String requestPath, final HttpServletRequest req,
                                 final HttpServletResponse resp) throws IOException {

        // Log.info("Servicing insecure request: " + requestPath);

        if (requestPath.startsWith(PUBLIC_PATH_START)) {
            // Public files may be served insecurely
            servicePublic(req, resp, requestPath);
        } else {
            this.nibblerMidController.serviceInsecure(req, resp, requestPath);
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

        final String absolutePath = file.getAbsolutePath();
        Log.info("Checking for '", absolutePath, "'");

        if (file.exists()) {
            final byte[] data = FileLoader.loadFileAsBytes(file, true);

            if (data == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                final String filename = file.getName().toLowerCase(Locale.ROOT);
                final int lastDot = filename.lastIndexOf(DOT);
                EMimeType mime = EMimeType.TEXTPLAIN;
                if (lastDot != -1) {
                    final String ext = filename.substring(lastDot + 1);
                    mime = EMimeType.forExtension(ext);
                }

                resp.setContentType(mime.mime);
                resp.setCharacterEncoding("UTF-8");
                resp.setContentLength(data.length);
                final Locale locale = req.getLocale();
                resp.setLocale(locale);

                try (final OutputStream out = resp.getOutputStream()) {
                    out.write(data);
                } catch (final IOException ex) {
                    final String className = ex.getClass().getSimpleName();
                    if (!"ClientAbortException".equals(className)) {
                        throw ex;
                    }
                }
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
