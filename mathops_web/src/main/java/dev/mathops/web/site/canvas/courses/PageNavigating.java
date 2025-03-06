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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * This page shows the "How to Successfully Navigate this Course" content.
 */
public enum PageNavigating {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final Metadata metadata) throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MetadataCourse metaCourse = metadata.getCourse(registration.course);
            if (metaCourse == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentStartHere(cache, site, req, resp, session, registration, metaCourse);
            }
        }
    }

    /**
     * Presents the "Start Here" information.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @param metaCourse   the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentStartHere(final Cache cache, final CanvasSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final ImmutableSessionInfo session,
                                 final RawStcourse registration, final MetadataCourse metaCourse)
            throws IOException, SQLException {

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
            CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, metaCourse,null,  ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            // TODO: Link back to Modules

            htm.sH(1);
            htm.addln("<img class='thumb' src='/www/images/etext/navigation-thumb.png' ",
                    "alt='Man at wheel of ship at sea'/>");
            htm.addln("<div class='thumb-text'>How to Successfully Navigate this Course</div>");
            htm.eH(1);

            htm.hr();

            //

            htm.sH(2).add("Completing the Course").eH(2);

            htm.sDiv("indent0");

            htm.sP().add("Passing this course requires that you do two things:").eP();
            htm.addln("<ul>");
            htm.addln("  <li>Complete at least 18 of the 24 learning targets in the course.</li>");
            htm.addln("  <li>Complete all learning targets that are marked as <strong>essential</strong>.</li>");
            htm.addln("</ul>");

            htm.sP().add("If you achieve these two goals, you will pass the course. ",
                    "The grade you earn will then be based on the number of points you have accumulated.").eP();

            htm.sP().add("There are 120 points possible.").eP();
            htm.addln("<ul>");
            htm.addln("  <li>108 (90%) points or higher earns an A</li>");
            htm.addln("  <li>96 (80%) to 107 points earns a B</li>");
            htm.addln("</ul>");

            htm.sP().add("Any number of points less than 96, as long as you have completed the required learning ",
                    "targets, earns a C. If you do complete the required learning targets, a U grade will be ",
                    "recorded (a U grade does not affect GPA).").eP();

            htm.eDiv(); // indent0

            //
            htm.hr();
            htm.sH(2).add("Course Organization").eH(2);

            htm.sDiv("indent0");

            htm.sP().add("The course is divided into four <b>modules</b>.").eP();
            htm.sP().add("Each <b>module</b> includes a <b>skills review</b> plus two <b>topics</b>.").eP();
            htm.sP().add("Each <b>topic</b> has three <b>learning targets</b>.").eP();
            htm.sP().add("There are a total of 24 learning targets in the course.").eP();

            //

            htm.sH(3).add("Skills Review").eH(3);

            htm.sDiv("indent0");
            htm.sP().add("Each module begins with a <b>skills review</b>.  This is an optional set of review ",
                    "materials and practice questions to remind you of some background skills we will use in the ",
                    "module.").eP();

            htm.sP().add("Skills review assignments are for practice only and do not earn points.  You should use ",
                    "them if you need to refresh some skills from prior courses.").eP();
            htm.eDiv();

            //

            htm.sH(3).add("Topics and Learning Targets").eH(3);

            htm.sDiv("indent0");
            htm.sP().add("Each module has six <b>learning targets</b> (three in each of its two topics).").eP();

            htm.sP().add("Each learning target has a homework assignment you can use to practice.  You have ",
                    "unlimited tries on the homework assignments.").eP();

            htm.sP().add("The homework assignments do not earn points, but you need to complete the assignment to ",
                    "unlock the proctored exam questions that can complete the learning target.").eP();

            htm.sP().add("A few of the learning targets are marked as <strong>essential</strong>.  These have to ",
                    "be completed to pass the course.").eP();
            htm.eDiv();

            //

            htm.sH(3).add("Completing Learning Targets").eH(3);

            htm.sDiv("indent0");
            htm.sP().add("Once you have unlocked the exam questions for a learning target, you can go to the ",
                    "Precalculus Center (Weber 137) and ask for a Learning Target Exam.  You can do this any time ",
                    "the Precalculus Center is open.  There is no need to schedule an appointment.").eP();

            htm.sP().add("The exam is delivered on a computer in the Precalculus Center's testing area.  Your ",
                    "learning target exam will include all the learning target questions that you have unlocked so ",
                    "far, but once you complete a learning target, you won't see its questions any more).").eP();

            htm.sP().add("Learning targets have due dates for completion.").eP();
            htm.addln("<ul>");
            htm.addln("  <li>Completing a learning target on or before its due date earns 5 points.</li>");
            htm.addln("  <li>Completing a learning target after its due date earns 4 points.</li>");
            htm.addln("</ul>");

            htm.sP().add("You do not need to go to the Precalculus Center once for every learning target.  You can ",
                    "complete several homework assignments to unlock several questions, then go to the Precalculus ",
                    "Center and your Learning Target Exam will include all learning targets you are eligible for.").eP();

            htm.sP().add("However,if you have six or more learning targets that are unlocked but have not yet been ",
                    "completed, you will not be able to move on to the next Module until you complete some learning ",
                    "targets to get that number open below six.  This is to prevent someone from leaving all ",
                    "the learning target exams until the end of the semester.").eP();
            htm.eDiv();

            htm.eDiv(); // indent0

            //

            htm.hr();
            htm.sH(2).add(" Strategies for Success").eH(2);

            htm.sDiv("indent0");
            htm.sP().add("To succeed in this course, we recommend these strategies:").eP();
            htm.addln("<ul>");
            htm.addln("  <li><strong>Work ahead</strong> - never wait until a deadline to do work that's due that ",
                    "day.</li>");
            htm.addln("  <li><strong>Do a little work each day</strong> or every couple of days, rather than trying ",
                    "to pack a lot of work into one day each week.  Schedule a regular time to work on this ",
                    "course.</li>");
            htm.addln("  <li><strong>Give yourself time and space.</strong> Time, so you don't feel rushed or ",
                    "panicked, and a quiet study space where you can focus.</li>");
            htm.addln("  <li><strong>Use the resources provided.</strong>. Take advantage of in-person and online ",
                    "help from the Precalculus Center.  Watch course videos and read the solutions.  Use textbooks ",
                    "or Internet resources when something does not make sense.</li>");
            htm.addln("  <li>If you start to get behind, <strong>reach out quickly</strong> and get back on track. ",
                    "The Precalculus Center team wants to help you succeed! Help us to help you by bringing us in ",
                    "when you need it.</li>");
            htm.addln("</ul>");

            htm.eDiv(); // indent0

            htm.eDiv(); // flexmain
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);

        }
    }

    /**
     * Presents the list of course modules for MATH 122.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws SQLException if there is an error accessing the database
     */
    static void presentMATH122Modules(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) throws SQLException {

    }

    /**
     * Presents the list of course modules for MATH 125.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws SQLException if there is an error accessing the database
     */
    static void presentMATH125Modules(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) throws SQLException {

        final String urlCourse = URLEncoder.encode(registration.course, StandardCharsets.UTF_8);

        htm.addln("<details class='module-first'>");
        htm.addln("  <summary class='module-summary'>Introduction</summary>");
        htm.addln("  <div class='module-item'>");
        htm.addln("    <a href='start_here.html?course=", urlCourse, "'>");
        htm.addln("    <img class='module-thumb' src='/www/images/etext/start-thumb.png'/>");
        htm.addln("    <div class='module-title'>Start Here</div>");
        htm.addln("    </a>");
        htm.addln("  </div>");
        htm.addln("  <div class='module-item'>");
        htm.addln("    <a href='navigating.html?course=", urlCourse, "'>");
        htm.addln("    <img class='module-thumb' src='/www/images/etext/navigation-thumb.png'/>");
        htm.addln("    <div class='module-title'>How to Successfully Navigate this Course</div>");
        htm.addln("    </a>");
        htm.addln("  </div>");
        htm.addln("</details>");

        htm.addln("<details class='module'>");
        htm.addln("  <summary class='module-summary'>Module 1: Angles and Triangles</summary>");
        htm.addln("  <div class='module-items'>");
        htm.addln("  </div>");
        htm.addln("</details>");

        htm.addln("<details class='module'>");
        htm.addln("  <summary class='module-summary'>Module 2: The Unit Circle and Right Triangles</summary>");
        htm.addln("  <div class='module-items'>");
        htm.addln("  </div>");
        htm.addln("</details>");

        htm.addln("<details class='module'>");
        htm.addln("  <summary class='module-summary'>Module 3: The Trigonometric Functions and Modeling</summary>");
        htm.addln("  <div class='module-items'>");
        htm.addln("  </div>");
        htm.addln("</details>");

        htm.addln("<details class='module'>");
        htm.addln("  <summary class='module-summary'>Module 4: Solving Problems with Triangles</summary>");
        htm.addln("  <div class='module-items'>");
        htm.addln("  </div>");
        htm.addln("</details>");
    }

    /**
     * Presents the list of course modules for MATH 126.
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
    static void presentMATH126Modules(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) throws SQLException {

    }

    /**
     * Emits the course number, title, and section number.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param course   the course record
     * @param csection the course section record
     */
    private static void emitCourseTitleAndSection(final HtmlBuilder htm, final RawCourse course,
                                                  final RawCsection csection) {

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

}
