package dev.mathops.web.site.canvas;

import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.RegistrationsLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.text.builder.HtmlBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param course   the course record
     * @param csection the course section record
     */
    public static void emitCourseTitleAndSection(final HtmlBuilder htm, final RawCourse course, final RawCsection csection) {

        htm.sDiv(null, "style='margin:0 24px; border-bottom:1px solid #C7CDD1;'");
        htm.sH(1, "title");
        if ("Y".equals(csection.courseLabelShown)) {
            htm.add(course.courseLabel);
            htm.add(": ");
        }
        htm.add("<span style='color:#D9782D'>", course.courseName, "</span>");
        htm.br().add("<small>Section ", csection.sect, "</small>");
        htm.eDiv();
    }

    /**
     * Emits a left-side menu with links for [Account], [Home], [Syllabus], [Announcements], [Modules], [Assignments],
     * [Getting Help], [Grades], and [Course Survey].
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    public static void emitLeftSideMenu(final HtmlBuilder htm, final String selectedCourse, final ECanvasPanel panel) {

        final String urlCourse = URLEncoder.encode(selectedCourse, StandardCharsets.UTF_8);

        htm.sDiv("flexmenu");

        htm.addln("<a class='", (panel == ECanvasPanel.ACCOUNT ? "menubtnactive" : "menubtn"),
                "' href='account.html?course=", urlCourse, "'>Account</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.HOME ? "menubtnactive" : "menubtn"),
                "' href='course.html?course=", urlCourse, "'>Home</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.SYLLABUS ? "menubtnactive" : "menubtn"),
                "' href='syllabus.html?course=", urlCourse, "'>Syllabus</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.ANNOUNCEMENTS ? "menubtnactive" : "menubtn"),
                "' href='announcements.html?course=", urlCourse, "'>Announcements</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.MODULES ? "menubtnactive" : "menubtn"),
                "' href='modules.html?course=", urlCourse, "'>Modules</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.ASSIGNMENTS ? "menubtnactive" : "menubtn"),
                "' href='assignments.html?course=", urlCourse, "'>Assignments</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.GETTING_HELP ? "menubtnactive" : "menubtn"),
                "' href='help.html?course=", urlCourse, "'>Getting Help</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.GRADES ? "menubtnactive" : "menubtn"),
                "' href='grades.html?course=", urlCourse, "'>Grades</a>");

        htm.addln("<a class='", (panel == ECanvasPanel.COURSE_SURVEY ? "menubtnactive" : "menubtn"),
                "' href='survey.html?course=", urlCourse, "'>Course Survey</a>");

        htm.eDiv(); // flexmenu
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
    public static RawStcourse confirmRegistration(final Cache cache, final String stuId, final String course) throws SQLException {

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
}
