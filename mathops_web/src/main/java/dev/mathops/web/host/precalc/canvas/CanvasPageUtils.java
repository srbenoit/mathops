package dev.mathops.web.host.precalc.canvas;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.course.RegistrationsLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

import java.io.File;
import java.sql.SQLException;

/**
 * Utility methods used by many pages.
 */
public enum CanvasPageUtils {
    ;

    /**
     * Writes the start of an empty page (with no headers or footers, but with a page wrapper that sizes just like the
     * content area of an ordinary page).
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param title the page title
     */
    public static void startPage(final HtmlBuilder htm, final String title) {

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
        htm.addln(" <meta name=\"robots\" content=\"noindex\">");

        htm.addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <meta name='viewport' content='width=device-width, initial-scale=1'>")
                .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")

                .addln(" <link rel='icon' type='image/x-icon' href='/www/images/favicon.ico'>")
                .addln(" <title>", title, "</title>");

        htm.addln("</head>");

        htm.addln("<body>");
        htm.sDiv("page-wrapper-empty", "id='page_wrapper'");
        htm.sDiv(null, "id='maincontent'");
    }

    /**
     * Writes the end of an empty page, including the closing of the "maincontent" div.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    public static void endPage(final HtmlBuilder htm) {

        htm.eDiv(); // maincontent
        htm.eDiv(); // page-wrapper

        htm.addln("</body>");
        htm.addln("</html>");
    }

    /**
     * Emits the course number, title, and section number.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param course  the course object
     * @param section the course section record
     */
    public static void emitCourseTitleAndSection(final HtmlBuilder htm, final StandardsCourseRec course,
                                                 final StandardsCourseSectionRec section) {

        htm.sDiv(null, "style='margin:0 24px; border-bottom:1px solid #C7CDD1;'");
        htm.sH(1, "title");
        htm.add(course.courseId, ": ");
        htm.add("<span style='color:#D9782D'>", course.courseTitle, "</span>");
        htm.br().add("<small>Section ", section.sectionNbr, "</small>");
        htm.eDiv();
    }

    /**
     * Emits a left-side menu with links for [Account], [Home], [Syllabus], [Announcements], [Modules], [Assignments],
     * [Getting Help], [Grades], and [Course Survey].
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param course the course object
     * @param path   if not null, this is included in the URL path before the page (for example if this is "../", the
     *               page URL would be something like "../account.html" rather than just "account.html")
     * @param panel  the panel to display as selected
     */
    public static void emitLeftSideMenu(final HtmlBuilder htm, final StandardsCourseRec course,
                                        final String path, final ECanvasPanel panel) {

        htm.sDiv("flexmenu");

        emitMenuLink(htm, panel == ECanvasPanel.ACCOUNT, path, "account.html", "Account");
        emitMenuLink(htm, panel == ECanvasPanel.HOME, path, "course.html", "Home");
        emitMenuLink(htm, panel == ECanvasPanel.SYLLABUS, path, "syllabus.html", "Syllabus");
        emitMenuLink(htm, panel == ECanvasPanel.ANNOUNCEMENTS, path, "announcements.html", "Announcements");
        emitMenuLink(htm, panel == ECanvasPanel.MODULES, path, "modules.html", "Modules");
        emitMenuLink(htm, panel == ECanvasPanel.ASSIGNMENTS, path, "assignments.html", "Assignments");
        emitMenuLink(htm, panel == ECanvasPanel.GETTING_HELP, path, "help.html", "Getting Help");
        emitMenuLink(htm, panel == ECanvasPanel.GRADES, path, "grades.html", "Grades");
        emitMenuLink(htm, panel == ECanvasPanel.COURSE_SURVEY, path, "survey.html", "Course Survey");

        htm.eDiv();
    }

    /**
     * Emits a single link in the left-side menu.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param active   true if this link is "active" or currently selected
     * @param path     if not null, this is included in the URL path before the page (for example if this is "../", the
     *                 page URL would be something like "../account.html" rather than just "account.html")
     * @param pageName the page name, like "account.html"
     * @param label    the menu link label, like "Account"
     */
    private static void emitMenuLink(final HtmlBuilder htm, final boolean active, final String path,
                                     final String pageName, final String label) {

        final String href = path == null ? pageName : (path + pageName);

        htm.addln("<a class='", (active ? "menubtnactive" : "menubtn"), "' href='", href, "'>", label, "</a>");
    }

    /**
     * Retrieves the registration record for the student in a course.  This method returns both "uncounted" Incompletes
     * from a prior term and "in-pace" courses for the active term.  It does not return dropped or forfeit
     * registrations.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course ID
     * @return the registration record; null if the student is not currently enrolled
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStcourse confirmRegistration(final Cache cache, final String stuId, final String course)
            throws SQLException {

        final RegistrationsLogic.ActiveTermRegistrations registrations =
                RegistrationsLogic.gatherActiveTermRegistrations(cache, stuId);

        RawStcourse registration = null;
        for (final RawStcourse reg : registrations.uncountedIncompletes()) {
            if (reg.course.equals(course)) {
                registration = reg;
                break;
            }
        }
        for (final RawStcourse reg : registrations.inPace()) {
            if (reg.course.equals(course)) {
                registration = reg;
                break;
            }
        }

        return registration;
    }

    /**
     * Attempts to load a "metadata.json" file and parse it into a JSON object.
     *
     * @param dir the directory in which to find the "metadata.json" file
     * @return the parsed JSON object; {@code null} if the file could not be read or parsed
     */
    public static JSONObject loadMetadata(final File dir) {

        final File metadataFile = new File(dir, "metadata.json");

        JSONObject result = null;

        final String fileData = FileLoader.loadFileAsString(metadataFile, true);

        if (fileData == null) {
            final String metaPath = metadataFile.getAbsolutePath();
            Log.warning("Unable to load ", metaPath);
        } else {
            try {
                final Object parsedObj = JSONParser.parseJSON(fileData);

                if (parsedObj instanceof final JSONObject parsedJson) {
                    result = parsedJson;
                } else {
                    final String metaPath = metadataFile.getAbsolutePath();
                    Log.warning("Top-level object in ", metaPath, " is not JSON Object.");
                }
            } catch (final ParsingException ex) {
                final String metaPath = metadataFile.getAbsolutePath();
                Log.warning("Failed to parse " + metaPath, ex);
            }
        }

        return result;
    }
}
