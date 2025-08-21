package dev.mathops.web.host.nibbler.mpsmedia;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a page whose only purpose is to edit student notes.
 */
enum PageNotes {
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
    static void showPage(final Cache cache, final ProctoringMediaSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final ERole role = session.getEffectiveRole();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = Res.get(Res.SITE_TITLE);
        Page.startOrdinaryPage(htm, title, session, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.PROCTOR)
            || role.canActAs(ERole.OFFICE_STAFF) || role.canActAs(ERole.DIRECTOR)) {

            final String term = req.getParameter("term");
            final String stuId = req.getParameter("stu");

            if (term == null || stuId == null || stuId.isBlank()) {
                Log.warning("Notes page accessed with no student ID.");
                resp.sendRedirect("home.html");
            } else {
                final File dataDir = site.getDataPath();
                final File termDir = new File(dataDir, term);
                final File studentDir = new File(termDir, stuId);
                if (studentDir.exists()) {
                    emitNotes(term, studentDir, session, htm);
                } else {
                    final String absPath = studentDir.getAbsolutePath();
                    Log.warning("Notes page accessed with nonexistent student directory: ", absPath);
                    resp.sendRedirect("home.html");
                }
            }

        } else {
            htm.sP().addln("Not authorized to access proctoring media management").eP();
        }

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the list of students, with all the sessions under each student.
     *
     * @param term       the term
     * @param studentDir the student data directory
     * @param session    the login session
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitNotes(final String term, final File studentDir, final ImmutableSessionInfo session,
                                  final HtmlBuilder htm) {

        final String heading = Res.get(Res.HOME_HEADING);
        htm.sH(2).add(heading).eH(2);
        htm.hr();

        htm.sH(3).add("Student Notes &nbsp; <a class='btnsmall' href='home.html'>Home</a>").eH(3);

        htm.sP().add("These notes are visible under all sessions for this student.  Use these notes to document ",
                "patterns over time or communications with the student.");

        final File notesFile = new File(studentDir, "notes.json");
        final String me = session.getEffectiveScreenName();
        final String stuId = studentDir.getName();

        if (notesFile.exists()) {
            final Object notesObject = loadJson(notesFile);

            if (notesObject instanceof final Object[] notesArray && notesArray.length > 0) {
                htm.addln("<dl class='indent' style='color:#229;'>");
                for (final Object note : notesArray) {
                    if (note instanceof final JSONObject jsonNote) {
                        final String dateStr = jsonNote.getStringProperty("date");
                        final String authorStr = jsonNote.getStringProperty("author");
                        final String notesStr = jsonNote.getStringProperty("notes");

                        if (dateStr == null || authorStr == null || notesStr == null) {
                            continue;
                        }

                        String formattedDate;
                        try {
                            final LocalDateTime parsedDate = LocalDateTime.parse(dateStr);
                            formattedDate = TemporalUtils.FMT_MDY.format(parsedDate);
                        } catch (final DateTimeParseException ex) {
                            formattedDate = dateStr;
                        }

                        if (me.equals(authorStr)) {
                            htm.add("<dt>", formattedDate, " (", authorStr, ")");

                            htm.addln("<form style='display:inline;font-size:smaller;' method='POST' ",
                                    "action='notesdelete.html'>");
                            htm.addln("  <input type='hidden' id='term' name='term' value='", term, "'/>");
                            htm.addln("  <input type='hidden' id='stu' name='stu' value='", stuId, "'/>");
                            htm.addln("  <input type='hidden' id='date' name='date' value='", dateStr, "'/>");
                            htm.addln("  <input type='hidden' 'id='who' name='who' value='", me, "'/>");
                            htm.addln("  <input type='submit' value='Delete'/>");
                            htm.addln("</form>");

                            htm.addln("</dt>");
                        } else {
                            htm.addln("<dt>", formattedDate, " (", authorStr, ")</dt>");
                        }
                        htm.addln("<dd>", notesStr, "</dd>");
                    }
                }
                htm.addln("</dl>");
            }
        } else {
            htm.sP("indent", "style='color:#229;'");
            htm.addln("(There are no notes on file for this student...)");
            htm.eP();
        }

        htm.sDiv("indent");
        htm.sP().add("<strong>Add a Note:</strong>").eP();

        htm.addln("<form method='POST' action='notesadd.html'>");
        htm.addln("  <input type='hidden' id='term' name='term' value='", term, "'/>");
        htm.addln("  <input type='hidden' id='stu' name='stu' value='", stuId, "'/>");
        htm.addln("  <input type='hidden' id='date' name='date' value='", LocalDateTime.now().toString(), "'/>");

        htm.addln("  Author: <input type='text' 'id='who' name='who' value='", me, "'/>").br();
        htm.addln("  <textarea id='note' name='note' rows='3' cols='60' style='margin-top:2px'></textarea>").br();
        htm.addln("  <input type='submit'/>");
        htm.addln("</form>");
        htm.eDiv();
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
            final String absPath = file.getAbsolutePath();
            Log.warning("Unable to load ", absPath);
        } else {
            try {
                result = JSONParser.parseJSON(str);
            } catch (final ParsingException ex) {
                final String absPath = file.getAbsolutePath();
                Log.warning("Failed to parse ", absPath, ex);
            }
        }

        return result;
    }

    /**
     * Processes a POST to "notesadd.html" that adds a new student note.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processAddNote(final Cache cache, final ProctoringMediaSite site, final ServletRequest req,
                               final HttpServletResponse resp) throws IOException, SQLException {

        final String term = req.getParameter("term");
        final String stu = req.getParameter("stu");
        final String date = req.getParameter("date");
        final String who = req.getParameter("who");
        final String note = req.getParameter("note");

        if (AbstractSite.isFileParamInvalid(term) || AbstractSite.isFileParamInvalid(stu)
            || AbstractSite.isParamInvalid(date)) {
            Log.warning("Invalid POST parameters - possible attack");
            Log.warning("  term=", term);
            Log.warning("  stu=", stu);
            Log.warning("  date=", date);
            Log.warning("  who=", who);
            resp.sendRedirect("home.html");
        } else if (term == null || stu == null || date == null || who == null || note == null) {
            Log.warning("POST from add form form with missing parameters");
            resp.sendRedirect("home.html");
        } else {
            final File dataDir = site.getDataPath();
            final File termDir = new File(dataDir, term);
            final File studentDir = new File(termDir, stu);

            if (studentDir.exists()) {
                final JSONObject newNoteJson = new JSONObject();
                newNoteJson.setProperty("date", date);
                newNoteJson.setProperty("author", who);
                newNoteJson.setProperty("notes", note);

                final File file = new File(studentDir, "notes.json");

                if (file.exists()) {
                    final Object existing = loadJson(file);
                    if (existing instanceof final Object[] existingNotes) {

                        final HtmlBuilder builder = new HtmlBuilder(1000);
                        builder.addln("[");
                        for (final Object o : existingNotes) {
                            if (o instanceof final JSONObject oldNoteJson) {
                                final String oldJson = oldNoteJson.toJSONCompact();
                                builder.addln(oldJson, ",");
                            }
                        }
                        final String newJson = newNoteJson.toJSONCompact();
                        builder.addln(newJson);
                        builder.addln("]");

                        try (final Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                            final String jsonStr = builder.toString();
                            writer.write(jsonStr);
                        } catch (final IOException ex) {
                            final String absPath = studentDir.getAbsolutePath();
                            Log.warning("Failed to create student notes file in ", absPath, ex);
                        }
                    } else {
                        final String absPath = file.getAbsolutePath();
                        Log.warning("POST, but can't parse file: ", absPath);
                    }
                } else {
                    // Create a new "notes.json" with a single note...
                    final int length = note.length();
                    final HtmlBuilder builder = new HtmlBuilder(100 + length);
                    builder.addln("[");
                    final String json = newNoteJson.toJSONCompact();
                    builder.addln(json);
                    builder.addln("]");

                    try (final Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                        final String jsonStr = builder.toString();
                        writer.write(jsonStr);
                    } catch (final IOException ex) {
                        final String absPath = studentDir.getAbsolutePath();
                        Log.warning("Failed to create student notes file in ", absPath, ex);
                    }
                }

            } else {
                final String absPath = studentDir.getAbsolutePath();
                Log.warning("POST, but can't find student dir: ", absPath);
            }

            resp.sendRedirect("notes.html?term=" + term + "&stu=" + stu);
        }
    }

    /**
     * Processes a POST to "notesdelete.html" that deletes an existing student note.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processDeleteNote(final Cache cache, final ProctoringMediaSite site, final ServletRequest req,
                                  final HttpServletResponse resp) throws IOException, SQLException {

        final String term = req.getParameter("term");
        final String stu = req.getParameter("stu");
        final String date = req.getParameter("date");
        final String who = req.getParameter("who");

        if (AbstractSite.isFileParamInvalid(term) || AbstractSite.isFileParamInvalid(stu)
            || AbstractSite.isParamInvalid(date)) {
            Log.warning("Invalid POST parameters - possible attack");
            Log.warning("  term=", term);
            Log.warning("  stu=", stu);
            Log.warning("  date=", date);
            Log.warning("  who=", who);
            resp.sendRedirect("home.html");
        } else if (term == null || stu == null || date == null || who == null) {
            Log.warning("POST from delete note form with missing parameters");
            resp.sendRedirect("home.html");
        } else {
            final File dataDir = site.getDataPath();
            final File termDir = new File(dataDir, term);
            final File studentDir = new File(termDir, stu);

            if (studentDir.exists()) {
                final File file = new File(studentDir, "notes.json");

                if (file.exists()) {
                    final Object existing = loadJson(file);
                    if (existing instanceof final Object[] existingNotes) {
                        final List<JSONObject> retained = new ArrayList<>(existingNotes.length);

                        boolean found = false;
                        for (final Object o : existingNotes) {
                            if (o instanceof final JSONObject oldNoteJson) {
                                final String sameDate = oldNoteJson.getStringProperty("date");
                                final String samAuthor = oldNoteJson.getStringProperty("author");
                                if (sameDate.equals(date) && samAuthor.equals(who)) {
                                    found = true;
                                    continue;
                                }
                                retained.add(oldNoteJson);
                            }
                        }

                        if (found) {
                            final int count = retained.size();

                            final HtmlBuilder builder = new HtmlBuilder(1000);
                            builder.addln("[");
                            for (int i = 0; i < count; ++i) {
                                final JSONObject noteJson = retained.get(i);
                                final String json = noteJson.toJSONCompact();
                                if (i == count - 1) {
                                    builder.addln(json);
                                } else {
                                    builder.addln(json, ",");
                                }
                            }
                            builder.addln("]");

                            try (final Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                                final String jsonStr = builder.toString();
                                writer.write(jsonStr);
                            } catch (final IOException ex) {
                                final String absPath = studentDir.getAbsolutePath();
                                Log.warning("Failed to create student notes file in ", absPath, ex);
                            }
                        } else {
                            final String absPath = file.getAbsolutePath();
                            Log.warning("POST, but could not find note to delete: ", absPath);
                        }
                    } else {
                        final String absPath = file.getAbsolutePath();
                        Log.warning("POST, but can't parse file: ", absPath);
                    }
                } else {
                    final String absPath = file.getAbsolutePath();
                    Log.warning("POST, but can't find notes file: ", absPath);
                }

            } else {
                final String absPath = studentDir.getAbsolutePath();
                Log.warning("POST, but can't find student dir: ", absPath);
            }

            resp.sendRedirect("notes.html?term=" + term + "&stu=" + stu);
        }
    }
}
