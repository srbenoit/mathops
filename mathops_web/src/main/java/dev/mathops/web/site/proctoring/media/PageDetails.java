package dev.mathops.web.site.proctoring.media;

import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.json.JSONObject;
import dev.mathops.core.parser.json.JSONParser;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Generates a detailed view of one proctoring session, allowing review.
 */
enum PageDetails {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final ProctoringMediaSite site,
                         final ServletRequest req, final HttpServletResponse resp,
                         final ImmutableSessionInfo session) throws IOException, SQLException {

        final ERole role = session.getEffectiveRole();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.PROCTOR)
                || role.canActAs(ERole.OFFICE_STAFF) || role.canActAs(ERole.DIRECTOR)) {

            htm.sH(2).add(Res.get(Res.HOME_HEADING)).eH(2);

            emitDetails(site.dataDir, req, session, htm);
        } else {
            htm.sP().addln("Not authorized to access proctoring media management").eP();
        }

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the list of students, with all the sessions under each student.
     *
     * @param dataDir the data directory
     * @param req     the request
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    private static void emitDetails(final File dataDir, final ServletRequest req,
                                    final ImmutableSessionInfo session, final HtmlBuilder htm) {

        htm.hr();

        htm.sDiv(null, "id='vw-by-date'");

        htm.sH(3).add("Session Details &nbsp; <a class='btnsmall' href='home.html'>Home</a>").eH(3);
        htm.sDiv("indent");

        final String stuid = req.getParameter("stu");
        final String psid = req.getParameter("psid");

        if (AbstractSite.isFileParamInvalid(stuid) || AbstractSite.isFileParamInvalid(psid)) {
            Log.warning("Invalid POST parameters - possible attack");
            Log.warning("  stu=", stuid);
            Log.warning("  psid=", psid);
            htm.sP().add("Invalid request parameters").eP();
        } else if (stuid == null || psid == null) {
            htm.sP().add("Missing request parameters").eP();
        } else {
            final File sessionDir = new File(new File(dataDir, stuid), psid);
            if (sessionDir.exists()) {
                emitDetails(sessionDir, stuid, psid, session, htm);
            } else {
                Log.warning(sessionDir.getAbsolutePath() + " not found");
                htm.sP().add("Unable to locate proctoring session files").eP();
            }
        }

        htm.eDiv();
    }

    /**
     * Emits the list of students, with all the sessions under each student.
     *
     * @param sessionDir the proctoring session directory
     * @param stuid      the student ID
     * @param psid       the proctoring session ID
     * @param session    the login session
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitDetails(final File sessionDir, final String stuid, final String psid,
                                    final ImmutableSessionInfo session, final HtmlBuilder htm) {

        htm.sP().add("<strong>");
        emitStudentMeta(sessionDir, stuid, htm);
        htm.add(" &nbsp; ");
        emitSessionMeta(sessionDir, htm);
        htm.add("</strong>").eP();
        htm.div("vgap0");

        emitMedia(sessionDir, stuid, psid, htm);
        emitTags(sessionDir, htm);
        emitReviews(stuid, psid, session, sessionDir, htm);
        emitReviewForm(stuid, psid, session, htm);
    }

    /**
     * Emits a line with student metadata.
     *
     * @param sessionDir the session directory (student metadata is in the parent directory)
     * @param stuid      the student ID
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitStudentMeta(final File sessionDir, final String stuid, final HtmlBuilder htm) {

        final Object stuMeta = loadJson(new File(sessionDir.getParentFile(), "meta.json"));

        String first = null;
        String last = null;
        if (stuMeta instanceof JSONObject) {
            first = ((JSONObject) stuMeta).getStringProperty("first");
            last = ((JSONObject) stuMeta).getStringProperty("last");
        }
        if (first == null || last == null) {
            htm.add(stuid);
        } else {
            htm.add(last, ", ", first, " (", stuid, ")");
        }
    }

    /**
     * Emits a line with session metadata.
     *
     * @param sessionDir the session directory (student metadata is in the parent directory)
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitSessionMeta(final File sessionDir, final HtmlBuilder htm) {

        final Object sessMeta = loadJson(new File(sessionDir, "meta.json"));

        String course = null;
        String exam = null;
        if (sessMeta instanceof JSONObject) {
            course = ((JSONObject) sessMeta).getStringProperty("course");
            exam = ((JSONObject) sessMeta).getStringProperty("exam");
        }
        if (course == null) {
            if (exam == null) {
                htm.addln("(No course/exam information found)");
            } else {
                htm.addln("Exam: ", exam);
            }
        } else if (exam == null) {
            htm.addln("Course: ", course);
        } else {
            htm.addln("Course: ", course, ", Exam: ", exam);
        }
    }

    /**
     * Emits media (photo and ID card image capture, webcam video, and screen video).
     *
     * @param dir   the session directory
     * @param stuid the student ID
     * @param psid  the proctoring session ID
     * @param htm   the {@code HtmlBuilder} to which to append
     */
    private static void emitMedia(final File dir, final String stuid, final String psid, final HtmlBuilder htm) {

        final String subpath = stuid + "/" + psid + "/";

        htm.sDiv();
        final File photo = new File(dir, "photo.jpg");
        if (photo.exists()) {
            htm.addln("<img width='160' src='", subpath, "photo.jpg'/>");
        } else {
            htm.addln("<img width='120' src='missing_image.png'/>");
        }

        final File id = new File(dir, "id.jpg");
        if (id.exists()) {
            htm.addln("<img width='160' src='", subpath, "id.jpg'/>");
        } else {
            htm.addln("<img width='120' src='missing_image.png'/>");
        }
        htm.eDiv();

        htm.sDiv();
        htm.sDiv(null, "style='display:inline-block;width:160px;text-align:center;'").add("Photo").eDiv();
        htm.sDiv(null, "style='display:inline-block;width:160px;text-align:center;'").add("ID Card").eDiv();
        htm.eDiv();
        htm.div("vgap");

        // Find the largest numbered files that exist
        int largest = 0;
        for (int i = 1; i < 100; ++i) {
            if (new File(dir, "wemcam" + i + ".webm").exists() || new File(dir, "screen" + i + ".webm").exists()) {
                largest = i;
            } else {
                break;
            }
        }

        if (largest > 0) {
            for (int i = largest; i >= 1; --i) {
                htm.sDiv();
                final File webcam = new File(dir, "webcam" + i + ".webm");
                if (webcam.exists()) {
                    htm.addln("<video id='webcam-vid' width='320' controls>");
                    htm.addln("<source src='", subpath, "webcam" + i + ".webm' type='video/webm'/>");
                    htm.addln("</video>");
                } else {
                    htm.addln("<img width='120' src='missing_image.png'/>");
                }

                final File screen = new File(dir, "screen" + i + ".webm");
                if (screen.exists()) {
                    htm.addln("<video id='screen-vid' width='420' controls>");
                    htm.addln("<source src='", subpath, "screen" + i + ".webm' type='video/webm'/>");
                    htm.addln("</video>");

                } else {
                    htm.addln("<img width='120' src='missing_image.png'/>");
                }
                htm.eDiv();
            }
        }

        htm.sDiv();
        final File webcam = new File(dir, "webcam.webm");
        if (webcam.exists()) {
            htm.addln("<video id='webcam-vid' width='320' controls>");
            htm.addln("<source src='", subpath, "webcam.webm' type='video/webm'/>");
            htm.addln("</video>");
        } else {
            htm.addln("<img width='120' src='missing_image.png'/>");
        }

        final File screen = new File(dir, "screen.webm");
        if (screen.exists()) {
            htm.addln("<video id='screen-vid' width='420' controls>");
            htm.addln("<source src='", subpath, "screen.webm' type='video/webm'/>");
            htm.addln("</video>");

        } else {
            htm.addln("<img width='120' src='missing_image.png'/>");
        }
        htm.eDiv();

        htm.sDiv();
        htm.sDiv(null, "style='display:inline-block;width:320px;text-align:center;'").add("Webcam Capture").eDiv();
        htm.sDiv(null, "style='display:inline-block;width:420px;text-align:center;'").add("Screen Capture").eDiv();
        htm.eDiv();
        htm.div("vgap");

        htm.sDiv("indent");
        htm.add("<button class='btnsmall' onclick='playboth()'>Play</button> &nbsp; ",
                "<button class='btnsmall' onclick='stopboth()'>Stop</button> &nbsp; ",
                "<input type='range' min='1' max='1000' value='0' id='playback-slider' ",
                "oninput='playbackSliderUpdate()'/>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln("function playboth() {");
        htm.addln("  document.getElementById('webcam-vid').play();");
        htm.addln("  document.getElementById('screen-vid').play();");
        htm.addln("}");
        htm.addln("function stopboth() {");
        htm.addln("  document.getElementById('webcam-vid').pause();");
        htm.addln("  document.getElementById('screen-vid').pause();");
        htm.addln("}");
        htm.addln("function playbackSliderUpdate() {");
        htm.addln("  let dur1 = document.getElementById('webcam-vid').duration;");
        htm.addln("  let dur2 = document.getElementById('screen-vid').duration;");
        htm.addln("  let dur = dur1 > dur2 ? dur1 : dur2;");
        htm.addln("  let value = document.getElementById('playback-slider').value;");
        htm.addln("  let time = dur * value / 1000;");
        htm.addln("  document.getElementById('webcam-vid').currentTime = time;");
        htm.addln("  document.getElementById('screen-vid').currentTime = time;");
        htm.addln("}");
        htm.addln("</script>");

        htm.div("vgap");
    }

    /**
     * Emits a list of identified tags in the media.
     *
     * @param sessionDir the session directory
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitTags(final File sessionDir, final HtmlBuilder htm) {

        final Object tags = loadJson(new File(sessionDir, "tags.json"));

        htm.hr();
        htm.sP().add("<strong>Tagged timestamps").add("</strong>").eP();

        if (tags instanceof Object[]) {
            htm.addln("<ul>");
            for (final Object o : (Object[]) tags) {
                htm.add("<li>");
                if (o instanceof final JSONObject tagsObj) {

                    final Double sec = tagsObj.getNumberProperty("sec");
                    final String note = tagsObj.getStringProperty("note");
                    final String src = tagsObj.getStringProperty("src");
                    final Double sev = tagsObj.getNumberProperty("severity");

                    htm.add("<span style='color:");
                    if (sev == null || sev.doubleValue() < 1.5) {
                        // "Low" or severity not marked - yellow
                        htm.add("Moccasin");
                    } else if (sev.doubleValue() < 2.5) {
                        // "Medium" - orange
                        htm.add("Orange");
                    } else {
                        // "High" - red
                        htm.add("OrangeRed");
                    }
                    htm.add("'>&bull;</span> ");

                    if (sec != null) {
                        final int total = sec.intValue();
                        final int mm = total / 60;
                        final int ss = total % 60;

                        htm.add(mm).add(':');
                        if (ss < 10) {
                            htm.add('0');
                        }
                        htm.add(ss);
                        htm.add("  ");
                    }

                    if (src == null) {
                        htm.add("[anonymous] ");
                    } else {
                        htm.add("[", src, "] ");
                    }

                    if (note != null) {
                        htm.add("<i>", note, "</i>");
                    }
                } else {
                    htm.add("(Invalid tag object)");
                }
                htm.addln("</li>");
            }
            htm.addln("</ul>");
        } else {
            htm.sP().add("(No tags found)").eP();
        }
    }

    /**
     * Emits a list of identified tags in the media.
     *
     * @param stuid      the student ID
     * @param psid       the proctoring session ID
     * @param session    the login session
     * @param sessionDir the session directory
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitReviews(final String stuid, final String psid, final ImmutableSessionInfo session,
                                    final File sessionDir, final HtmlBuilder htm) {

        htm.hr();
        htm.sP().add("<strong>Reviews</strong>").eP();

        for (int i = 1; i < 100; ++i) {

            final File elevFile = new File(sessionDir, "elevated" + i + ".json");

            final File reviewFile = new File(sessionDir, "review" + i + ".json");

            if (elevFile.exists()) {
                final Object elevated = loadJson(elevFile);
                Log.info(elevated.getClass().getName());
                if (elevated instanceof JSONObject) {
                    emitSingleReview(elevFile, (JSONObject) elevated, true, htm);
                    emitProcessForm(i, stuid, psid, session, htm);
                }
            } else if (reviewFile.exists()) {
                final Object review = loadJson(reviewFile);
                Log.info(review.getClass().getName());
                if (review instanceof JSONObject) {
                    emitSingleReview(reviewFile, (JSONObject) review, false, htm);
                }
            } else {
                break;
            }
        }
    }

    /**
     * Emits a single review.
     *
     * @param srcFile  the source file
     * @param obj      the JSON object
     * @param elevated true if the review is elevated
     * @param htm      the {@code HtmlBuilder} to which to append
     */
    private static void emitSingleReview(final File srcFile, final JSONObject obj,
                                         final boolean elevated, final HtmlBuilder htm) {

        htm.sP();

        if (elevated) {
            htm.add("<strong class='elevated'>");
        }

        final String date = obj.getStringProperty("date");
        if (date == null) {
            Log.warning("No date in ", srcFile.getAbsolutePath());
            htm.add("[No date]: &nbsp; ");
        } else {
            try {
                htm.add(TemporalUtils.FMT_MDY_HMS.format(LocalDateTime.parse(date)), ": &nbsp; ");
            } catch (final DateTimeParseException ex) {
                Log.warning("Bad date in ", srcFile.getAbsolutePath(), ex);
                htm.add(date);
            }
        }

        final String reviewer = obj.getStringProperty("reviewer");
        if (reviewer == null) {
            Log.warning("No reviewer in ", srcFile.getAbsolutePath());
            htm.add("(No reviewer) &nbsp; ");
        } else {
            htm.add("(", reviewer, ") &nbsp; ");
        }

        final String notes = obj.getStringProperty("notes");
        if (notes == null) {
            Log.warning("No notes in ", srcFile.getAbsolutePath());
            htm.add("<i>No notes.</i>");
        } else {
            htm.add("<i>", notes, "</i>");
        }

        if (elevated) {
            htm.add("</strong>");
        }

        String proc = obj.getStringProperty("processor");
        if (proc != null) {
            htm.br().add(" &nbsp; Processed by ", proc);
            String procdt = obj.getStringProperty("procdate");
            if (procdt != null) {
                htm.add(" on ");
                try {
                    htm.add(TemporalUtils.FMT_MDY_HMS.format(LocalDateTime.parse(procdt)));
                } catch (final DateTimeParseException ex) {
                    Log.warning("Failed to parse date: ", procdt, ex);
                    htm.add(date);
                }
            }
            String procnotes = obj.getStringProperty("procnotes");
            if (procnotes != null) {
                htm.add(": <i>", procnotes, "</i>");
            }

            for (int j = 1; j < 100; ++j) {
                proc = obj.getStringProperty("processor" + j);
                if (proc == null) {
                    break;
                }
                htm.br().add(" &nbsp; Processed by ", proc);
                procdt = obj.getStringProperty("procdate" + j);
                if (procdt != null) {
                    htm.add(" on ");
                    try {
                        htm.add(TemporalUtils.FMT_MDY_HMS.format(LocalDateTime.parse(procdt)));
                    } catch (final DateTimeParseException ex) {
                        Log.warning("Failed to parse date: ", procdt, ex);
                        htm.add(date);
                    }
                }
                procnotes = obj.getStringProperty("procnotes" + j);
                if (procnotes != null) {
                    htm.add(": <i>", procnotes, "</i>");
                }
            }
        }
        htm.eP();
    }

    /**
     * Emits a form used to add a review to this session.
     *
     * @param i       the index (1-based) of the elevated review being processed
     * @param stuid   the student ID
     * @param psid    the proctoring session ID
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    private static void emitProcessForm(final int i, final String stuid, final String psid,
                                        final ImmutableSessionInfo session, final HtmlBuilder htm) {

        htm.sDiv("indent");
        htm.sH(4).add("Process").eH(4);

        htm.addln("<form method='POST' action='elevated.html'>");
        htm.addln("  <input type='hidden' id='stu' name='stu' value='", stuid, "'/>");
        htm.addln("  <input type='hidden' id='psid' name='psid' value='", psid, "'/>");
        htm.addln("  <input type='hidden' id='date' name='date' value='", LocalDateTime.now().toString(), "'/>");
        htm.addln("  <input type='hidden' id='index' name='index' value='", Integer.toString(i), "'/>");

        htm.addln("Reviewer: <input type='text' 'id='who' name='who' value='", session.getEffectiveScreenName(),
                "'/>").br();
        htm.addln("Resolve: <input type='checkbox' id='resolve' name='resolve' value='resolve'/>").br();
        htm.addln("<textarea id='notes' name='notes' rows='3' cols='60'></textarea>").br();
        htm.addln("<input type='submit'/>");
        htm.addln("</form>");
        htm.eDiv(); // indent
    }

    /**
     * Emits a form used to add a review to this session.
     *
     * @param stuid   the student ID
     * @param psid    the proctoring session ID
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    private static void emitReviewForm(final String stuid, final String psid,
                                       final ImmutableSessionInfo session, final HtmlBuilder htm) {

        htm.hr();

        htm.sH(4).add("Add New Review").eH(4);
        htm.sDiv("indent");

        htm.addln("<form method='POST' action='details.html'>");
        htm.addln("  <input type='hidden' id='stu' name='stu' value='", stuid, "'/>");
        htm.addln("  <input type='hidden' id='psid' name='psid' value='", psid, "'/>");
        htm.addln("  <input type='hidden' id='date' name='date' value='", LocalDateTime.now().toString(), "'/>");

        htm.addln("Reviewer: <input type='text' 'id='who' name='who' value='", session.getEffectiveScreenName(),
                "'/>").br();
        htm.addln("Elevate: <input type='checkbox' 'id='elevate' name='elevate' value='elevate'/>").br();
        htm.addln("<textarea id='notes' name='notes' rows='3' cols='60'></textarea>").br();
        htm.addln("<input type='submit'/>");
        htm.addln("</form>");
        htm.eDiv(); // indent
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
     * Processes a POST to 'details.html' from the review submission form, which adds a new review (which may or may not
     * be elevated).
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void processPost(final ProctoringMediaSite site, final ServletRequest req,
                            final HttpServletResponse resp) throws IOException {

        final String stu = req.getParameter("stu");
        final String psid = req.getParameter("psid");
        final String date = req.getParameter("date");
        final String who = req.getParameter("who");
        final String elevate = req.getParameter("elevate");
        final String notes = req.getParameter("notes");

        if (AbstractSite.isFileParamInvalid(stu) || AbstractSite.isFileParamInvalid(psid)
                || AbstractSite.isParamInvalid(date)) {
            Log.warning("Invalid POST parameters - possible attack");
            Log.warning("  stu=", stu);
            Log.warning("  psid=", psid);
            Log.warning("  date=", date);

            resp.sendRedirect("home.html");
        } else if (stu == null || psid == null || date == null) {
            Log.warning("POST from review form with missing parameters");

            resp.sendRedirect("home.html");
        } else {
            final File sessionDir = new File(new File(site.dataDir, stu), psid);

            if (sessionDir.exists()) {
                if (elevate == null) {
                    addReview(sessionDir, who, date, notes);
                } else {
                    addElevatedReview(sessionDir, who, date, notes);
                }
            } else {
                Log.warning("POST, but can't find session dir: ", sessionDir.getAbsolutePath());
            }

            resp.sendRedirect("details.html?stu=" + stu + "&psid=" + psid);
        }
    }

    /**
     * Adds a review to the JSON data in the proctoring session directory.
     *
     * @param dir   the session directory
     * @param who   the reviewer
     * @param date  the date of the review
     * @param notes the review notes
     */
    private static void addReview(final File dir, final String who, final String date, final String notes) {

        // See what number we are on...
        int index = 1;
        while (new File(dir, "review" + index + ".json").exists()
                || new File(dir, "elevated" + index + ".json").exists()) {
            ++index;
        }

        final JSONObject toAdd = new JSONObject();
        toAdd.setProperty("reviewer", who);
        toAdd.setProperty("date", date);
        toAdd.setProperty("notes", notes);

        final File file = new File(dir, "review" + index + ".json");
        try (final FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write(toAdd.toJSONCompact());
        } catch (final IOException ex) {
            Log.warning("Failed to write ", file.getAbsolutePath(), ex);
        }
    }

    /**
     * Adds an elevated review to the JSON data in the proctoring session directory.
     *
     * @param dir   the session directory
     * @param who   the reviewer
     * @param date  the date of the review
     * @param notes the review notes
     */
    private static void addElevatedReview(final File dir, final String who, final String date, final String notes) {

        // See what number we are on...
        int index = 1;
        while (new File(dir, "review" + index + ".json").exists()
                || new File(dir, "elevated" + index + ".json").exists()) {
            ++index;
        }

        final JSONObject toAdd = new JSONObject();
        toAdd.setProperty("reviewer", who);
        toAdd.setProperty("date", date);
        toAdd.setProperty("notes", notes);

        final File file = new File(dir, "elevated" + index + ".json");
        try (final FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write(toAdd.toJSONCompact());
        } catch (final IOException ex) {
            Log.warning("Failed to write ", file.getAbsolutePath(), ex);
        }
    }

    /**
     * Processes a POST to "elevated.html" from the review submission form.
     *
     * @param site the owning site
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void processElevated(final ProctoringMediaSite site, final ServletRequest req,
                                final HttpServletResponse resp) throws IOException {

        final String index = req.getParameter("index");
        final String stu = req.getParameter("stu");
        final String psid = req.getParameter("psid");
        final String date = req.getParameter("date");
        final String who = req.getParameter("who");
        final String resolve = req.getParameter("resolve");
        final String notes = req.getParameter("notes");

        if (AbstractSite.isFileParamInvalid(index) || AbstractSite.isFileParamInvalid(stu)
                || AbstractSite.isFileParamInvalid(psid) || AbstractSite.isParamInvalid(date)) {
            Log.warning("Invalid POST parameters - possible attack");
            Log.warning("  index=", index);
            Log.warning("  stu=", stu);
            Log.warning("  psid=", psid);
            Log.warning("  date=", date);

            resp.sendRedirect("home.html");
        } else if (stu == null || psid == null || date == null) {
            Log.warning("POST from process form with missing parameters");

            resp.sendRedirect("home.html");
        } else {
            final File dir = new File(new File(site.dataDir, stu), psid);

            if (dir.exists()) {
                final File file = new File(dir, "elevated" + index + ".json");

                if (file.exists()) {
                    final Object existing = loadJson(file);
                    if (existing instanceof final JSONObject obj) {

                        // Add data on the processor and re-write
                        if (obj.getStringProperty("processor") != null) {
                            int i = 1;
                            while (obj.getStringProperty("processor" + i) != null) {
                                ++i;
                            }
                            obj.setProperty("processor" + i, who);
                            obj.setProperty("procdate" + i, date);
                            obj.setProperty("procnotes" + i, notes);
                        } else {
                            obj.setProperty("processor", who);
                            obj.setProperty("procdate", date);
                            obj.setProperty("procnotes", notes);
                        }

                        try (final FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
                            w.write(obj.toJSONCompact());
                        } catch (final IOException ex) {
                            Log.warning("Failed to update file", file.getAbsolutePath(), ex);
                        }

                    } else {
                        Log.warning("POST, but can't parse file: ", file.getAbsolutePath());
                    }

                    if (resolve != null) {
                        file.renameTo(new File(dir, "review" + index + ".json"));
                    }
                } else {
                    Log.warning("POST, but can't find elevated file: ", file.getAbsolutePath());
                }

            } else {
                Log.warning("POST, but can't find session dir: ", dir.getAbsolutePath());
            }

            resp.sendRedirect("details.html?stu=" + stu + "&psid=" + psid);
        }
    }
}
