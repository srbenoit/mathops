package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This page shows the "Grades" content.
 */
public enum PageGrades {
    ;

    /**
     * Starts the page that shows the status of all assignments and grades.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException,
            SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            presentGrades(cache, site, req, resp, session, registration);
        }
    }

    /**
     * Presents the "Grades" information.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentGrades(final Cache cache, final CanvasSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session,
                              final RawStcourse registration) throws IOException, SQLException {

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        final List<RawCsection> csections = RawCsectionLogic.queryByTerm(cache, active.term);
        final List<RawCourse> courses = RawCourseLogic.queryAll(cache);

        RawCsection csection = null;
        for (final RawCsection test : csections) {
            if (registration.course.equals(test.course) && registration.sect.equals(test.sect)) {
                csection = test;
                break;
            }
        }
        RawCourse course = null;
        for (final RawCourse test : courses) {
            if (registration.course.equals(test.course)) {
                course = test;
                break;
            }
        }

        if (csection == null || course == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, course, csection);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, course.course, ECanvasPanel.GRADES);

            htm.sDiv("flexmain");

            htm.sH(2).add("Grades").eH(2);
            htm.hr();

            htm.sDiv().add("The requirements to pass the course are that you:").eDiv();
            htm.addln("<ul>");
            htm.addln("  <li>Complete at least <b>18</b> of the 24 learning targets in the course, and</li>");
            htm.addln("  <li>Complete <b>all</b> learning targets that are marked as <strong>essential</strong>.</li>");
            htm.addln("</ul>");

            htm.sP().add("If you meet these two goals, you will pass the course.  Your grade will then be based on ",
                    "the number of points accumulated.").eP();

            htm.div("vgap2");
            htm.sH(2).add("Your Assignment Scores").eH(2);

            htm.sTable("grades");

            htm.sTr();
            htm.sTh().add("Learning Target").eTh();
            htm.sTh().add("Essential?").eTh();
            htm.sTh().add("Homework Assignment").eTh();
            htm.sTh().add("Learning Target Exam").eTh();
            htm.sTh().add("Points Possible").eTh();
            htm.sTh().add("Score").eTh();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("1A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("Complete").eTd();
            htm.sTd().add("Complete").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("<b>5</b>").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("1B").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("Complete").eTd();
            htm.sTd().add("Complete (late)").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("<b>4</b>").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("1C").eTd();
            htm.sTd().add("<b>Yes</b>").eTd();
            htm.sTd().add("Complete").eTd();
            htm.sTd().add("Unlocked").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("2A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("2B").eTd();
            htm.sTd().add("<b>Yes</b>").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("2C").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("3A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("3B").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("3C").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("4A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("4B").eTd();
            htm.sTd().add("<b>Yes</b>").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("4C").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("5A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("5B").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("5C").eTd();
            htm.sTd().add("<b>Yes</b>").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("6A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("6B").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("6C").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("7A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("7B").eTd();
            htm.sTd().add("<b>Yes</b>").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("7C").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("8A").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("8B").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("8C").eTd();
            htm.sTd().add("No").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd().add("-").eTd();
            htm.sTd("c").add("5").eTd();
            htm.sTd("c").add("-").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd(null, "colspan='4' style='text-align:right'").add("<b>TOTAL</b>").eTd();
            htm.sTd("c").add("<b>120</b>").eTd();
            htm.sTd("c").add("<b>9</b>").eTd();
            htm.eTr();

            htm.eTable();

            htm.sP().add("You have completed <b>2</b> learning targets (out of 24).").eP();
            htm.sP().add("You have completed <b>1</b> essential learning targets (out of the 5 required).").eP();

            htm.div("vgap2");
            htm.sH(2).add("Grading Scale").eH(2);

            htm.sTable("grades");

            htm.sTr();
            htm.sTh().add("Point Range").eTh();
            htm.sTh().add("Percentage").eTh();
            htm.sTh().add("Grade Earned").eTh();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("108 to 120").eTd();
            htm.sTd().add("90% to 100%").eTd();
            htm.sTd("c").add("A").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("96 to 107").eTd();
            htm.sTd().add("80% to 89%").eTd();
            htm.sTd("c").add("B").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd().add("Less than 96").eTd();
            htm.sTd().add("Less than 80%").eTd();
            htm.sTd("c").add("C").eTd();
            htm.eTr();

            htm.sTr();
            htm.sTd("c", "colspan='2'").add("Course not complete").eTd();
            htm.sTd("c").add("U").eTd();
            htm.eTr();

            htm.eTable();

            htm.sP().add("Any number of points less than 96, as long as you have completed the required learning ",
                    "targets, earns a C. If you do not complete the required learning targets, a U grade will be ",
                    "recorded (a U grade does not affect GPA).").eP();

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }
}
