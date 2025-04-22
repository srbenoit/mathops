package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
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
 * This page shows the "Start Here" content.
 */
public enum PageStartHere {
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
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);
            if (course == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentStartHere(cache, site, req, resp, session, registration, course);
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
     * @param course       the course object
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentStartHere(final Cache cache, final CanvasSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final ImmutableSessionInfo session,
                                 final RawStcourse registration, final StandardsCourseRec course)
            throws IOException, SQLException {

        final TermData termData = cache.getTermData();
        final StandardsCourseSectionRec section = termData.getStandardsCourseSection(registration.course,
                registration.sect);

        if (section == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, course, section);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, course, null, ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            // TODO: Link back to Modules

            htm.sH(1);
            htm.addln("<img class='thumb' src='/www/images/etext/start-thumb.png' alt='Starting line of race track'/>");
            htm.addln("<div class='thumb-text'>Start Here</div>");
            htm.eH(1);

            htm.div("vgap");

            htm.sH(2).add("Welcome to ", course.courseId, ": <span style='color:#D9782D'>", course.courseTitle,
                    "</span>").eH(2);

            // TODO: course-specific top-matter, general welcome message

            // TODO: Class expectations - how the class will run generally

            // TODO: How to get help, contact info

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
