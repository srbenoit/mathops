package dev.mathops.web.host.nibbler.mpsmedia;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.schema.legacy.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.UserInfoBar;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The proctoring media site.
 */
public final class ProctoringMediaSite extends AbstractSite {

    /** Zero-length array used in construction of other arrays. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /** Zero-length array used in construction of other arrays. */
    private static final JSONObject[] ZERO_LEN_JSON_ARR = new JSONObject[0];

    /** The path under which to find proctoring data folders. */
    private final File dataPath;

    /**
     * Constructs a new {@code ProctoringMediaSite}.
     *
     * @param theSite     the site profile under which this site is accessed
     * @param theSessions the singleton user session repository
     */
    public ProctoringMediaSite(final Site theSite, final ISessionManager theSessions) {

        super(theSite, theSessions);

        this.dataPath = PathList.getInstance().get(EPath.CUR_DATA_PATH);
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
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return Res.get(Res.SITE_TITLE);
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

        // Log.info("GET ", subpath);

        // TODO: Honor maintenance mode.

        switch (subpath) {
            case "basestyle.css", "secure/basestyle.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
            case "style.css", "secure/style.css" ->
                    sendReply(req, resp, "text/css", FileLoader.loadFileAsBytes(getClass(), "style.css", true));
            case "favicon.ico", "secure/favicon.ico" -> serveImage(subpath, req, resp);
            case null, default -> {
                final ImmutableSessionInfo session = validateSession(req, resp, null);

                final boolean showLanding = CoreConstants.EMPTY.equals(subpath) || "index.html".equals(subpath)
                                            || "login.html".equals(subpath);

                if (session == null) {
                    if (showLanding) {
                        PageLanding.showPage(cache, this, req, resp);
                    } else if ("secure/shibboleth.html".equals(subpath)) {
                        doShibbolethLogin(cache, req, resp, null);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                        final String path = this.site.path;
                        resp.setHeader("Location",
                                path + (path.endsWith(Contexts.ROOT_PATH) ? "index.html" : "/index.html"));
                        sendReply(req, resp, Page.MIME_TEXT_HTML, ZERO_LEN_BYTE_ARR);
                    }
                } else {
                    LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

                    if (session.getEffectiveRole().canActAs(ERole.PROCTOR)
                        || session.getEffectiveRole().canActAs(ERole.OFFICE_STAFF)
                        || session.getEffectiveRole().canActAs(ERole.DIRECTOR)) {
                        if (showLanding) {
                            PageLanding.showPage(cache, this, req, resp);
                        } else if (subpath.endsWith(".js")) {
                            serveJs(subpath, req, resp);
                        } else if (subpath.endsWith("png")
                                   || subpath.endsWith(".jpg")
                                   || subpath.endsWith(".jpeg")
                                   || subpath.endsWith(".gif")
                                   || subpath.endsWith(".ico")
                                   || subpath.endsWith(".webm")
                                   || subpath.endsWith(".mp4")
                                   || subpath.endsWith(".ogv")
                                   || subpath.endsWith(".pdf")) {

                            serveMedia(cache, subpath, req, resp);
                        } else if ("home.html".equals(subpath)) {
                            PageHome.showPage(cache, this, req, resp, session);
                        } else if ("details.html".equals(subpath)) {
                            PageDetails.showPage(cache, this, req, resp, session);
                        } else if ("notes.html".equals(subpath)) {
                            PageNotes.showPage(cache, this, req, resp, session);
                        } else if ("secure/shibboleth.html".equals(subpath)) {
                            doShibbolethLogin(cache, req, resp, session);
                        } else {
                            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                        }
                    } else {
                        Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }

                    LogBase.setSessionInfo(null, null);
                }
            }
        }
    }

    /**
     * Serves a .js file from the package containing the ProctoringMediaSite class.
     *
     * @param name the filename, with extension
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void serveJs(final String name, final ServletRequest req,
                                final HttpServletResponse resp) throws IOException {

        final byte[] data = FileLoader.loadFileAsBytes(ProctoringMediaSite.class, name, true);

        if (data == null) {
            Log.warning(name, " not found");
            resp.sendError(404);
        } else {
            sendReply(req, resp, "text/javascript", data);
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

        // TODO: Honor maintenance mode.

        final ImmutableSessionInfo session = validateSession(req, resp, null);

        if (session == null) {
            if ("upload.html".equals(subpath)) {
                processUpload(cache, req, resp);
            } else {
                Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            LogBase.setSessionInfo(session.loginSessionId, session.getEffectiveUserId());

            switch (subpath) {
                case "rolecontrol.html" -> processRoleControls(cache, req, resp, session);
                case "upload.html" -> processUpload(cache, req, resp);
                case "details.html" -> PageDetails.processPost(cache, this, req, resp);
                case "elevated.html" -> PageDetails.processElevated(cache, this, req, resp);
                case "studentnote.html" -> PageDetails.processStudentNote(cache, this, req, resp);
                case "deletestudentnote.html" -> PageDetails.processDeleteStudentNote(cache, this, req, resp);
                case "notesadd.html" -> PageNotes.processAddNote(cache, this, req, resp);
                case "notesdelete.html" -> PageNotes.processDeleteNote(cache, this, req, resp);
                case null, default -> {
                    Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }

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

        Log.info("Shibboleth login attempt");

        ImmutableSessionInfo sess = session;

        if (sess == null) {
            sess = processShibbolethLogin(cache, req);
        }

        final String path = this.site.path;
        final String redirect;
        if (sess == null) {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? "login.html" : "/login.html");
        } else {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? "home.html" : CoreConstants.SLASH + "home.html");

            // Install the session ID cookie in the response
            Log.info("Adding session ID cookie ", req.getServerName());
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(req.getServerName());
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }

        Log.info("Redirecting to ", redirect);

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);
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
    private void processRoleControls(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                                     final ImmutableSessionInfo session) throws IOException, SQLException {

        UserInfoBar.processRoleControls(cache, req, session);

        final String target = req.getParameter("target");

        if (isParamInvalid(target)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  target='", target, "'");
            PageError.doGet(cache, this, req, resp, session, Res.get(Res.ERR_NO_ROLE_TARGET));
        } else if (target == null) {
            PageError.doGet(cache, this, req, resp, session, Res.get(Res.ERR_NO_ROLE_TARGET));
        } else {
            resp.sendRedirect(target);
        }
    }

    /**
     * Processes a file upload.
     *
     * @param cache the data cache
     * @param req   the HTTP request
     * @param resp  the HTTP response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void processUpload(final Cache cache, final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final String psid = req.getParameter("psid");
        final String stuid = req.getParameter("stuid");
        final String type = req.getParameter("type");
        final String when = req.getParameter("when");

        // Log.info(" PSID=", psid, " STUID=", stuid, " TYPE=", type, " WHEN=", when);

        if (psid == null || psid.isEmpty()) {
            Log.warning("Upload with no PSID field - ignorning");
        } else if (stuid == null || stuid.isEmpty()) {
            Log.warning("Upload with no STUID field - ignorning");
        } else if (type == null || type.isEmpty()) {
            Log.warning("Upload with no TYPE field - ignorning");
        } else if (when == null || when.isEmpty()) {
            Log.warning("Upload with no WHEN field - ignorning");
        } else // PSID and STUID will be used as parts of a file path - ensure they are safe!
            if (isParamInvalid(psid) || isParamInvalid(stuid) || isParamInvalid(when)) {
                Log.warning("Invalid request parameters - possible attack:");
                Log.warning("  psid='", psid, "'");
                Log.warning("  stuid='", stuid, "'");
                Log.warning("  when='", when, "'");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else if ("P".equals(type)) {
                final String fname = "photo.jpg";
                storeImage(cache, req, resp, psid, stuid, fname);
            } else if ("I".equals(type)) {
                final String fname = "id.jpg";
                storeImage(cache, req, resp, psid, stuid, fname);
            } else if ("V".equals(type)) {
                final String fname = "webcam.webm";
                storeVideo(cache, req, resp, psid, stuid, fname);
            } else if ("S".equals(type)) {
                final String fname = "screen.webm";
                storeVideo(cache, req, resp, psid, stuid, fname);
            } else if ("M".equals(type)) {
                storeMeta(cache, req, resp, psid, stuid);
            } else if ("E".equals(type)) {
                storeEvent(cache, req, resp, psid, stuid);
            } else {
                Log.warning("Invalid file type: ", type);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
    }

    /**
     * Retrieves the data directory.
     *
     * @return the  data directory
     */
    File getDataPath() {

        return this.dataPath;
    }

    /**
     * Determines the directory name under which to store "active term" proctoring videos.
     *
     * @param cache the data cache
     * @return the proctoring data directory
     * @throws SQLException if there is an error accessing the database
     */
    private File getActiveTermDir(final Cache cache) throws SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec term = systemData.getActiveTerm();
        final String dirName = term.term.shortString;

        return new File(this.dataPath, dirName);
    }

    /**
     * Stores an image and sends an appropriate response.
     *
     * @param cache    the data cache
     * @param req      the HTTP request
     * @param resp     the HTTP response
     * @param psId     the proctoring session ID
     * @param stuId    the student ID
     * @param filename the filename
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void storeImage(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                            final String psId, final String stuId, final String filename)
            throws IOException, SQLException {

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream(2 << 16);
        final byte[] buffer = new byte[65536];

        try (final ServletInputStream in = req.getInputStream()) {
            int numRead = in.read(buffer);
            while (numRead > 0) {
                outStream.write(buffer, 0, numRead);
                numRead = in.read(buffer);
            }

            final File dataDir = getActiveTermDir(cache);
            final File stuPath = new File(dataDir, stuId);
            final File sessionPath = new File(stuPath, psId);

            if (sessionPath.exists() || sessionPath.mkdirs()) {
                final File file = new File(sessionPath, filename);
                try (final FileOutputStream fos = new FileOutputStream(file)) {
                    final byte[] bytes = outStream.toByteArray();
                    fos.write(bytes);

                    final HtmlBuilder htm = new HtmlBuilder(200);
                    final String title = Res.get(Res.SITE_TITLE);
                    Page.startEmptyPage(htm, title, false);
                    Page.endEmptyPage(htm, false);
                    sendReply(req, resp, MIME_TEXT_HTML, htm);
                } catch (final IOException ex) {
                    final String absPath = sessionPath.getAbsolutePath();
                    Log.warning("Unable to write image file: ", absPath, ex);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                final String absPath = sessionPath.getAbsolutePath();
                Log.warning("Unable to create image directory: ", absPath);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (final IOException ex) {
            Log.warning("Failed to read upload image file data for student ", stuId, ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Stores a block of video and sends an appropriate response.
     *
     * @param cache    the data cache
     * @param req      the HTTP request
     * @param resp     the HTTP response
     * @param psId     the proctoring session ID
     * @param stuId    the student ID
     * @param filename the filename
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void storeVideo(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                            final String psId, final String stuId, final String filename)
            throws IOException, SQLException {

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream(2 << 16);
        final byte[] buffer = new byte[65536];

        try (final ServletInputStream in = req.getInputStream()) {
            int numRead = in.read(buffer);
            while (numRead > 0) {
                outStream.write(buffer, 0, numRead);
                try {
                    numRead = in.read(buffer);
                } catch (final IOException ex) {
                    Log.warning(ex);
                    numRead = 0;
                }
            }

            final File dataDir = getActiveTermDir(cache);
            final File stuPath = new File(dataDir, stuId);
            final File sessionPath = new File(stuPath, psId);

            if (sessionPath.exists() || sessionPath.mkdirs()) {
                final File file = new File(sessionPath, filename);
                try (final FileOutputStream fos = new FileOutputStream(file, true)) {
                    final byte[] byteArray = outStream.toByteArray();
                    fos.write(byteArray);

                    final HtmlBuilder htm = new HtmlBuilder(200);
                    final String title = Res.get(Res.SITE_TITLE);
                    Page.startEmptyPage(htm, title, false);
                    Page.endEmptyPage(htm, false);
                    sendReply(req, resp, MIME_TEXT_HTML, htm);

                } catch (final IOException ex) {
                    final String absolutePath = sessionPath.getAbsolutePath();
                    Log.warning("Unable to write video file ", absolutePath, ex);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                final String absolutePath = sessionPath.getAbsolutePath();
                Log.warning("Unable to create video directory: ", absolutePath);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (final IOException ex) {
            Log.warning("Failed to read upload video data for student ", stuId, ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Stores a block of metadata and sends an appropriate response.
     *
     * @param cache the data cache
     * @param req   the HTTP request
     * @param resp  the HTTP response
     * @param psId  the proctoring session ID
     * @param stuId the student ID
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void storeMeta(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                           final String psId, final String stuId) throws IOException, SQLException {

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
        final byte[] buffer = new byte[1024];

        try (final ServletInputStream in = req.getInputStream()) {
            int numRead = in.read(buffer);
            while (numRead > 0) {
                outStream.write(buffer, 0, numRead);
                numRead = in.read(buffer);
            }

            final File dataDir = getActiveTermDir(cache);
            final File stuPath = new File(dataDir, stuId);

            if (!stuPath.exists() && stuPath.mkdirs()) {
                // Just created the student directory - populate metadata
                final RawStudent stuRec = RawStudentLogic.query(cache, stuId, false);

                final HtmlBuilder meta = new HtmlBuilder(100);
                if (stuRec == null) {
                    meta.add("{\"first\":\"Record not found\",\"last\":\"* ERROR\"}");
                } else {
                    meta.add("{\"first\":\"", stuRec.firstName, "\",\"last\":\"", stuRec.lastName, "\"}");
                }
                try (final FileWriter w = new FileWriter(new File(stuPath, "meta.json"), StandardCharsets.UTF_8)) {
                    final String metaStr = meta.toString();
                    w.write(metaStr);
                } catch (final IOException ex) {
                    Log.warning(ex);
                }
            }

            final File sessionPath = new File(stuPath, psId);
            if (sessionPath.exists() || sessionPath.mkdirs()) {
                final File file = new File(sessionPath, "meta.json");
                try (final FileOutputStream fos = new FileOutputStream(file, false)) {
                    final byte[] bytes = outStream.toByteArray();
                    fos.write(bytes);

                    final HtmlBuilder htm = new HtmlBuilder(200);
                    final String title = Res.get(Res.SITE_TITLE);
                    Page.startEmptyPage(htm, title, false);
                    Page.endEmptyPage(htm, false);
                    sendReply(req, resp, MIME_TEXT_HTML, htm);

                } catch (final IOException ex) {
                    final String absolutePath = sessionPath.getAbsolutePath();
                    Log.warning("Unable to write file: ", absolutePath, ex);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                final String absolutePath = sessionPath.getAbsolutePath();
                Log.warning("Unable to create directory: ", absolutePath);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (final IOException ex) {
            Log.warning("Failed to read upload file data", ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Stores an event message by adding it to the "tags" file.
     *
     * @param cache the data cache
     * @param req   the HTTP request
     * @param resp  the HTTP response
     * @param psId  the proctoring session ID
     * @param stuId the student ID
     * @throws IOException  if there is an error writing the
     * @throws SQLException if there is an error accessing the database
     */
    private void storeEvent(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                            final String psId, final String stuId) throws IOException, SQLException {

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
        final byte[] buffer = new byte[128];

        try (final ServletInputStream in = req.getInputStream()) {
            int numRead = in.read(buffer);
            while (numRead > 0) {
                outStream.write(buffer, 0, numRead);
                numRead = in.read(buffer);
            }
            final String body = outStream.toString();

            final File dataDir = getActiveTermDir(cache);
            final File stuPath = new File(dataDir, stuId);
            if (stuPath.exists()) {

                final File sessionDir = new File(stuPath, psId);
                if (sessionDir.exists()) {

                    final File meta = new File(sessionDir, "meta.json");
                    final Path metaPath = meta.toPath();
                    final FileTime metaTime = (FileTime) Files.getAttribute(metaPath, "creationTime");
                    final long start = metaTime == null ? 0L : metaTime.toMillis();

                    final File tagsFile = new File(sessionDir, "tags.json");

                    final List<JSONObject> tags = new ArrayList<>(10);

                    if (tagsFile.exists()) {
                        final Object json = loadJson(tagsFile);
                        if (json instanceof final Object[] array) {
                            for (final Object o : array) {
                                if (o instanceof JSONObject) {
                                    tags.add((JSONObject) o);
                                }
                            }
                        }
                    }

                    final double sev;
                    if ("PAGE-CLOSED".equals(body) || "EXAM-ENDED".equals(body)) {
                        sev = 1.0;
                    } else if ("START-STREAMING".equals(body)) {
                        // Need to start a new video stream if one already existed
                        bumpVideoStreams(sessionDir);
                        sev = 1.0;
                    } else {
                        // Unrecognized - elevate severity
                        sev = 2.0;
                    }

                    final long duration = start == 0L ? 0L : System.currentTimeMillis() - start;

                    final JSONObject newEvent = new JSONObject();
                    final Double durationObj = Double.valueOf((double) duration);
                    newEvent.setProperty("sec", durationObj);
                    newEvent.setProperty("note", body);
                    newEvent.setProperty("src", "system");
                    final Double severityObj = Double.valueOf(sev);
                    newEvent.setProperty("severity", severityObj);

                    tags.add(newEvent);

                    final JSONObject[] newTags = tags.toArray(ZERO_LEN_JSON_ARR);

                    final int count = newTags.length;
                    try (final FileWriter w = new FileWriter(new File(sessionDir, "tags.json"),
                            StandardCharsets.UTF_8)) {
                        w.write('[');
                        final String json = newTags[0].toJSONFriendly(0);
                        w.write(json);
                        for (int i = 1; i < count; ++i) {
                            w.write(CoreConstants.COMMA);
                            final String nextJson = newTags[i].toJSONFriendly(0);
                            w.write(nextJson);
                        }
                        w.write(']');
                    } catch (final IOException ex) {
                        Log.warning(ex);
                    }
                }
            }
        } catch (final IOException ex) {
            Log.warning("Failed to read event upload file data", ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Renames all video stream files so new uploads will start new streams.
     *
     * @param dir the session directory
     */
    private static void bumpVideoStreams(final File dir) {

        // Find the largest numbered files that exist
        int largest = 0;
        for (int i = 1; i < 100; ++i) {
            if (new File(dir, "webcam" + i + ".webm").exists()
                || new File(dir, "screen" + i + ".webm").exists()) {
                largest = i;
            } else {
                break;
            }
        }

        // Move all files to a larger index
        for (int i = largest; i >= 1; --i) {
            new File(dir, "webcam" + i + ".webm").renameTo(new File(dir, "webcam" + (i + 1) + ".webm"));
            new File(dir, "screen" + i + ".webm").renameTo(new File(dir, "screen" + (i + 1) + ".webm"));
        }

        new File(dir, "webcam.webm").renameTo(new File(dir, "webcam1.webm"));
        new File(dir, "screen.webm").renameTo(new File(dir, "screen1.webm"));
    }

    /**
     * Attempts to load and parse a JSON file.
     *
     * @param file the file to load
     * @return the parsed object; {@code null} if unable to load or parse
     */
    private static Object loadJson(final File file) {

        Object result = null;

        final String str = FileLoader.loadFileAsString(file, false);

        if (str == null) {
            Log.warning("Unable to load ", file.getAbsolutePath());
        } else {
            try {
                result = JSONParser.parseJSON(str);
            } catch (final ParsingException ex) {
                Log.warning("Failed to parse ", file.getAbsolutePath(), ex);
            }
        }

        return result;
    }

    /**
     * Serves an image file from the proctoring media directory.
     *
     * @param cache    the data cache
     * @param filename the image filename
     * @param req      the request
     * @param resp     the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    protected void serveMedia(final Cache cache, final String filename, final HttpServletRequest req,
                              final HttpServletResponse resp) throws IOException, SQLException {

        final File file = new File(this.dataPath, filename);
        final long total = file.length();
        long start = 0L;
        long end = total;
        boolean ranged = false;

        final String range = req.getHeader("Range");
        if ((range != null) && range.startsWith("bytes=")) {
            final String sub = range.substring(6);
            if (!sub.isEmpty() && sub.charAt(sub.length() - 1) == '-') {
                // something like '100-', use total size as end
                try {
                    start = Long.parseLong(sub.substring(0, sub.length() - 1));
                    ranged = true;
                } catch (final NumberFormatException ex) {
                    Log.warning(ex);
                }
            } else {
                final String[] split = sub.split(CoreConstants.DASH);
                if (split.length == 2) {
                    try {
                        start = Integer.parseInt(split[0]);
                        end = Integer.parseInt(split[1]);
                        ranged = true;
                    } catch (final NumberFormatException ex) {
                        Log.warning(ex);
                    }
                } else {
                    Log.warning("Split size was ", Integer.toString(split.length), " rather than 2");
                }
            }
        }

        final byte[] data;

        if (ranged) {
            data = FileLoader.loadFileAsBytes(new File(this.dataPath, filename), start, end);
        } else {
            data = FileLoader.loadFileAsBytes(new File(this.dataPath, filename), true);
        }

        if (data == null) {
            final String absPath = file.getAbsolutePath();
            Log.warning(absPath, " not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final String lower = filename.toLowerCase(Locale.ROOT);

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
                // Log.info(" GET ", lower, " with range from ", Long.toString(start), " to ",
                // Long.valueOf(end), " out of ", Long.toString(total));

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
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
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
}
