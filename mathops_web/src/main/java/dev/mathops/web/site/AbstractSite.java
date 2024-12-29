package dev.mathops.web.site;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.session.login.ShibbolethLoginProcessor;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * A base class for sites that will process some subset of the requests received by the main servlet, based on the
 * leading part of the request path.
 */
public abstract class AbstractSite {

    /** File in which to store maintenance mode data. */
    public static final String MAINT_FILE = "maintenence.properties";

    /** The MIME type text/plain. */
    public static final String MIME_TEXT_PLAIN = "text/plain";

    /** The MIME type text/html. */
    public static final String MIME_TEXT_HTML = "text/html";

    /** The MIME type text/html. */
    protected static final String MIME_TEXT_XML = "text/xml";

    /** The MIME type text/css. */
    static final String MIME_TEXT_CSS = "text/css";

    /** Characters valid in parameter strings. */
    private static final String VALID_PARAM_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.,/~()[]!@#$%^&*:;?=+|\\ \t";

    /** Characters valid in file path parameter strings. */
    private static final String VALID_FILE_PARAM_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_.~";

    /** The date and time when the site was deployed. */
    final String buildDatetime;

    /** The database profile under which this site is accessed. */
    public final WebSiteProfile siteProfile;

    /** The directory from which VTT are loaded. */
    private final File vttDir;

    /** The directory from which images are loaded. */
    private final File imgDir;

    /** The directory from which lesson files are loaded. */
    private final File lessonsDir;

    /**
     * Constructs a new {@code AbstractSite}.
     *
     * @param theSiteProfile the website profile under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    protected AbstractSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        if (theSiteProfile == null) {
            throw new IllegalArgumentException("Database profile may not be null");
        }
        if (theSessions == null) {
            throw new IllegalArgumentException("Session manager may not be null");
        }

        this.siteProfile = theSiteProfile;

        final File baseDir = PathList.getInstance().getBaseDir();

        this.vttDir = new File("/opt/public/www");
        this.imgDir = new File("/opt/public/www/images");
        this.lessonsDir = new File(baseDir, "lessons");

        String datetime = null;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("date.txt")) {
            if (in != null) {
                final byte[] data = new byte[50];
                final int size = in.read(data);
                datetime = new String(data, 0, size, StandardCharsets.UTF_8).trim();
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        this.buildDatetime = datetime;
    }

    /**
     * Gets the database profile under which this site should be accessed.
     *
     * @return the database profile
     */
    public final DbProfile getDbProfile() {

        return this.siteProfile.dbProfile;
    }

    /**
     * Gets the primary database context under which this site should be accessed.
     *
     * @return the primary database context
     */
    protected final DbContext getPrimaryDbContext() {

        return this.siteProfile.dbProfile.getDbContext(ESchemaUse.PRIMARY);
    }

    /**
     * Tests the live refresh policy for this site.
     *
     * @return the live refresh policy
     */
    protected abstract ELiveRefreshes getLiveRefreshes();

    /**
     * Generates the site title.
     *
     * @return the title
     */
    public abstract String getTitle();

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public abstract void doGet(final Cache cache, String subpath, ESiteType type, HttpServletRequest req,
                               HttpServletResponse resp) throws IOException, SQLException;

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public abstract void doPost(final Cache cache, String subpath, ESiteType type,
                                HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException;

    /**
     * Sends a response with a particular content type and content.
     *
     * @param req         the request
     * @param resp        the response
     * @param contentType the content type
     * @param reply       the reply content
     * @throws IOException if there was an exception writing the response
     */
    public static void sendReply(final ServletRequest req, final HttpServletResponse resp, final String contentType,
                                 final byte[] reply) throws IOException {

        resp.setContentType(contentType);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(reply.length);
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setLocale(req.getLocale());

        try (final OutputStream out = resp.getOutputStream()) {
            out.write(reply);
        } catch (final IOException ex) {
            if (!"ClientAbortException".equals(ex.getClass().getSimpleName())) {
                throw ex;
            }
        }
    }

    /**
     * Sends a response with a particular content type and content.
     *
     * @param req         the request
     * @param resp        the response
     * @param contentType the content type
     * @param reply       the reply content
     * @param start       the index of the first byte being sent
     * @param total       the total size of the file
     * @throws IOException if there was an exception writing the response
     */
    protected static void sendRangedReply(final ServletRequest req, final HttpServletResponse resp,
                                          final String contentType, final byte[] reply, final long start,
                                          final long total) throws IOException {

        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        resp.setContentType(contentType);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(reply.length);
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("Content-Range", "bytes " + start + CoreConstants.DASH + (start + (long) reply.length - 1L)
                                        + CoreConstants.SLASH + total);
        resp.setLocale(req.getLocale());

        // Log.info(" Sending ranged reply: bytes " + start + CoreConstants.DASH
        // + (start + reply.length - 1) + CoreConstants.SLASH + total);

        try (final OutputStream out = resp.getOutputStream()) {
            out.write(reply);
        } catch (final IOException ex) {
            if (!"ClientAbortException".equals(ex.getClass().getSimpleName())) {
                throw ex;
            }
        }
    }

    /**
     * Validates the user session. If the session is invalid, an error is logged and the user is redirected to the
     * index.html page.
     *
     * @param req      the request
     * @param resp     the response
     * @param failPage the page to which to redirect the user on a failed validation
     * @return the {@code ImmutableSessionInfo} if the session is valid; {@code null} if not
     * @throws IOException if there is an error writing the response
     */
    protected ImmutableSessionInfo validateSession(final HttpServletRequest req, final HttpServletResponse resp,
                                                   final String failPage) throws IOException {

        final String sess = extractSessionId(req);

        final SessionResult session = SessionManager.getInstance().validate(sess);
        final ImmutableSessionInfo result = session.session;

        if (result == null) {
            if (sess != null) {
                Log.warning("Session validation error: ", session.error);

                // Tell the client to delete the cookie that provided the session ID
                final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess);
                cook.setDomain(req.getServerName());
                cook.setPath(CoreConstants.SLASH);
                cook.setMaxAge(0);
                resp.addCookie(cook);
            }

            if (failPage != null) {
                final String path = this.siteProfile.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) ? failPage
                        : CoreConstants.SLASH + failPage));
            }
        }

        return result;
    }

    /**
     * Extracts the session ID sent in a secure cookie in a client request. If such a session ID is not found, a new
     * session ID is generated.
     *
     * @param req the request
     * @return the session ID
     */
    protected static String extractSessionId(final HttpServletRequest req) {

        final Cookie[] cookies = req.getCookies();
        String sessionId = null;

        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (SessionManager.SESSION_ID_COOKIE.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    if (sessionId != null && sessionId.length() != ISessionManager.SESSION_ID_LEN) {
                        sessionId = null;
                    }
                }
            }
        }

        return sessionId;
    }

    /**
     * Extracts the session ID sent in a secure cookie in a client request. If such a session ID is not found, a new
     * session ID is generated.
     *
     * @param cache the data cache
     * @param req   the request
     * @return the created session
     * @throws SQLException if there was an error accessing the database
     */
    protected final ImmutableSessionInfo processShibbolethLogin(final Cache cache, final HttpServletRequest req)
            throws SQLException {

        ImmutableSessionInfo session = null;

        final SessionManager mgr = SessionManager.getInstance();

        // See if there is a Shibboleth session that has been logged out
        boolean stillLoggedIn = true;
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().startsWith("_shibsession_")) {
                    stillLoggedIn = !mgr.isLoggedOutSession(cookie.getValue());
                    // Log.info("Session " + c.getValue() + " logged out: " + alreadyLoggedOut);
                }
            }
        }

        if (stillLoggedIn) {
            // If we have Shibboleth attributes, use them to establish a new session
            final Object a1 = req.getAttribute("colostateEduPersonCSUID");
            if (a1 == null) {
                Log.info("No Shibboleth credentials");
            } else {
                // Log.info("colostateEduPersonCSUID = ", a1);

                final Map<String, String> fieldValues = new HashMap<>(10);
                fieldValues.put(ShibbolethLoginProcessor.CSUID, a1.toString());

                final SessionResult res = mgr.login(cache, new ShibbolethLoginProcessor(), fieldValues,
                        getLiveRefreshes());

                if (res.error != null) {
                    Log.info("Error establishing session: ", res.error);
                } else {
                    session = res.session;
                    Log.info("Established session ", session.loginSessionId, " for user ", session.userId,
                            " via Shibboleth");
                }
            }
        }

        return session;
    }

    /**
     * Serves an image file from the images subdirectory of the base directory.
     *
     * @param imgName the image filename
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    public final void serveImage(final String imgName, final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final long total = new File(this.imgDir, imgName).length();
        long start = 0L;
        long end = total;
        boolean ranged = false;

        final String range = req.getHeader("Range");
        if ((range != null) && range.startsWith("bytes=")) {
            final String sub = range.substring(6);
            final String[] split = sub.split(CoreConstants.DASH);
            if (split.length == 2) {
                try {
                    start = Long.parseLong(split[0]);
                    end = Long.parseLong(split[1]);

                    ranged = true;
                } catch (final NumberFormatException ex) {
                    Log.warning(ex);
                }
            }
        }

        final byte[] data;

        if (ranged) {
            data = FileLoader.loadFileAsBytes(new File(this.imgDir, imgName), start, end);
        } else {
            data = FileLoader.loadFileAsBytes(new File(this.imgDir, imgName), true);
        }

        if (data == null) {
            Log.warning(new File(this.imgDir, imgName).getAbsolutePath(), " not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final String lower = imgName.toLowerCase(Locale.ROOT);

            // final HtmlBuilder str = new HtmlBuilder(100);
            // str.add("Serving ", lower);
            // final Enumeration<String> headers = req.getHeaderNames();
            // while (headers.hasMoreElements()) {
            // final String h = headers.nextElement();
            // final String v = req.getHeader(h);
            // str.add(CoreConstants.SPC, h, "=", v);
            // }
            // Log.fine(str.toString());

            if (ranged) {
                if (lower.endsWith(".png")) {
                    sendRangedReply(req, resp, "image/png", data, start, total);
                } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                    sendRangedReply(req, resp, "image/jpeg", data, start, total);
                } else if (lower.endsWith(".gif")) {
                    sendRangedReply(req, resp, "image/gif", data, start, total);
                } else if (lower.endsWith(".ico")) {
                    sendRangedReply(req, resp, "image/x-icon", data, start, total);
                } else if (lower.endsWith(".webm")) {
                    sendRangedReply(req, resp, "video/webm", data, start, total);
                } else if (lower.endsWith(".mp4")) {
                    sendRangedReply(req, resp, "video/mp4", data, start, total);
                } else if (lower.endsWith(".ogv")) {
                    sendRangedReply(req, resp, "video/ogg", data, start, total);
                } else if (lower.endsWith(".pdf")) {
                    sendRangedReply(req, resp, "application/pdf", data, start, total);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (lower.endsWith(".png")) {
                sendReply(req, resp, "image/png", data);
            } else if (lower.endsWith(".jpg")
                       || lower.endsWith(".jpeg")) {
                sendReply(req, resp, "image/jpeg", data);
            } else if (lower.endsWith(".gif")) {
                sendReply(req, resp, "image/gif", data);
            } else if (lower.endsWith(".ico")) {
                sendReply(req, resp, "image/x-icon", data);
            } else if (lower.endsWith(".webm")) {
                sendReply(req, resp, "video/webm", data);
            } else if (lower.endsWith(".mp4")) {
                sendReply(req, resp, "video/mp4", data);
            } else if (lower.endsWith(".ogv")) {
                sendReply(req, resp, "video/ogg", data);
            } else if (lower.endsWith(".pdf")) {
                sendReply(req, resp, "application/pdf", data);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Serves a VTT file from the /opt/public subdirectory of the base directory.
     *
     * @param vttName the VTT filename
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    protected final void serveVtt(final String vttName, final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final File vttFile = new File(this.vttDir, vttName);
        final byte[] data = FileLoader.loadFileAsBytes(vttFile, true);

        if (data == null) {
            Log.warning(vttFile.getAbsolutePath(), " not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            sendReply(req, resp, "text/vtt", data);
        }
    }

    /**
     * Serves a media file from the /opt/public subdirectory of the base directory.
     *
     * @param filename the filename
     * @param req      the request
     * @param resp     the response
     * @throws IOException if there is an error writing the response
     */
    protected void serveMedia(final String filename, final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final byte[] data = FileLoader.loadFileAsBytes(new File(this.vttDir, filename), true);

        if (data == null) {
            Log.warning(new File(this.imgDir, filename).getAbsolutePath(), " not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            sendReply(req, resp, "application/pdf", data);
        }
    }

    /**
     * Serves an HTML lesson file from the lessons subdirectory of the base directory.
     *
     * @param file the lesson filename
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    protected final void serveLesson(final String file, final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final byte[] data = FileLoader.loadFileAsBytes(new File(this.lessonsDir, file), true);

        if (data == null) {
            Log.warning(new File(this.lessonsDir, file).getAbsolutePath(), " not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (file.endsWith(".css")) {
            sendReply(req, resp, "text/css", data);
        } else if (file.endsWith(".png")) {
            sendReply(req, resp, "image/png", data);
        } else if (file.endsWith(".jpg")
                   || file.endsWith(".jpeg")) {
            sendReply(req, resp, "image/jpeg", data);
        } else if (file.endsWith(".gif")) {
            sendReply(req, resp, "image/gif", data);
        } else if (file.endsWith(".ico")) {
            sendReply(req, resp, "image/x-icon", data);
        } else {
            sendReply(req, resp, "text/html", data);
        }
    }

    /**
     * Ensures that a parameter contains only alphanumerics, '.', '_', '-', or space, AND that it forms a reasonable
     * file path without ".." path components. Path components must be separated by forward slash characters
     *
     * @param param the parameter to test
     * @return {@code true} if the parameter has invalid characters
     */
    public static boolean isFileParamInvalid(final String param) {

        boolean invalid = false;

        if (param != null) {
            final String[] components = param.split(CoreConstants.SLASH);
            for (final String component : components) {
                for (final char ch : component.toCharArray()) {
                    if (VALID_FILE_PARAM_CHARS.indexOf(ch) == -1) {
                        invalid = true;
                        break;
                    }
                }
            }
        }

        return invalid;
    }

    /**
     * Ensures that a parameter contains only alphanumerics, '.', '_', '-', or space.
     *
     * @param param the parameter to test
     * @return {@code true} if the parameter has invalid characters
     */
    public static boolean isParamInvalid(final String param) {

        boolean invalid = false;

        if (param != null) {
            for (final char ch : param.toCharArray()) {
                if (VALID_PARAM_CHARS.indexOf(ch) == -1) {
                    invalid = true;
                    break;
                }
            }
        }

        return invalid;
    }

    /**
     * Loads the properties file with maintenance settings.
     *
     * @return the properties file contents
     */
    public static Properties loadMaintenanceFile() {

        final Properties props = new Properties();

        final File cfg = PathList.getInstance().get(EPath.CFG_PATH);
        final File file = new File(cfg, MAINT_FILE);
        if (file.exists()) {
            try (final FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }

        return props;
    }

    /**
     * Tests whether a single context is in maintenance mode.
     *
     * @param theSiteProfile the site profile to test
     * @return the maintenance message if the context is in maintenance mode; {@code null} if not
     */
    public static String isMaintenance(final WebSiteProfile theSiteProfile) {

        final Properties props = new Properties();

        final File cfg = PathList.getInstance().get(EPath.CFG_PATH);
        final File file = new File(cfg, MAINT_FILE);
        if (file.exists()) {
            try (final FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }

        final String key = theSiteProfile.host + theSiteProfile.path.replace(CoreConstants.SLASH, "~");

        final String maint = props.getProperty(key + "_maintenance");

        String result = null;

        if ("true".equalsIgnoreCase(maint)) {
            result = props.getProperty(key + "_msg");
            if (result == null || result.trim().isEmpty()) {
                result = props.getProperty(theSiteProfile.host + "_default_msg");

                if (result == null) {
                    // Still should be in maintenance mode, so return non-null result
                    result = CoreConstants.EMPTY;
                }
            }
        }

        return result;
    }
}
