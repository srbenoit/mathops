package dev.mathops.web.proxy;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.installation.Installation;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.web.ServletUtils;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * The web service proxy servlet.
 */
public final class WebServiceProxy extends HttpServlet {

    /** Filename to which to persist registration information. */
    private static final String PERSIST_FILENAME = "proxy_registrations.xml";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8099178062605555425L;

    /** The servlet configuration. */
    private ServletConfig servletConfig = null;

    /** The servlet context. */
    private ServletContext servletContext = null;

    /** The installation. */
    private Installation installation = null;

    /** A map from host name to the registry for that host name. */
    private final Map<String, HostRegistry> hosts;

    /**
     * Constructs a new {@code WebServiceProxy}.
     */
    public WebServiceProxy() {

        super();

        this.hosts = new HashMap<>(10);
    }

    /**
     * Adds a new service registration.
     *
     * @param host the host
     * @param path the path
     * @param reg the service registration (replaces any existing registration under the specified host and path)
     */
    void registerService(final String host, final String path, final ServiceRegistration reg) {

        final HostRegistry hostReg = this.hosts.computeIfAbsent(host, s -> new HostRegistry());

        hostReg.registerService(path, reg);
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

        if (this.installation == null) {
            throw new ServletException("Installation is not configured in servlet context.");
        }

        final String serverInfo = this.servletContext.getServerInfo();
        final String initMsg = Res.fmt(Res.SERVLET_INIT, serverInfo);
        Log.info(initMsg);

        final File persist = new File(this.installation.baseDir, PERSIST_FILENAME);
        if (persist.exists() && !persist.isDirectory()) {
            // TODO: Load persisted registrations and delete the persist file.
        }

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
     * Called when the servlet container unloads the servlet.
     * <p>
     * This method reduces the reference count on the installation - when that count reaches zero, the installation's
     * JMX server is allowed to stop
     */
    @Override
    public void destroy() {

        final File persist = new File(this.installation.baseDir, PERSIST_FILENAME);

        // TODO: Persist registrations if any exist

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

        // This can be called from many threads simultaneously

        req.setCharacterEncoding("UTF-8");

        try {
            final String requestHost = ServletUtils.getHost(req);
            final String requestPath = ServletUtils.getPath(req);

            final String remoteAddress = req.getRemoteAddr();
            LogBase.setHostPath(requestHost, requestPath, remoteAddress);

            try {
                synchronized (this.hosts) {
                    final HostRegistry hostReg = this.hosts.get(requestHost);

                    if (hostReg == null) {
                        // TODO: Record an unrecognized host event
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        final ServiceRegistration reg = hostReg.findService(requestPath);

                        if (reg == null) {
                            // TODO: Record an unrecognized path event
                            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                        } else {
                            reg.serve(req, resp);
                        }
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

    /**
     * Generates the string representation of the registration.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "WebServiceProxy{}";
    }
}
