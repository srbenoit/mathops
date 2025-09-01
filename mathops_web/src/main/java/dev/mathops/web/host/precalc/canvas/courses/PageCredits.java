package dev.mathops.web.host.precalc.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.RawCourse;
import dev.mathops.db.schema.legacy.RawCsection;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.host.precalc.canvas.CanvasPageUtils;
import dev.mathops.web.host.precalc.canvas.CanvasSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * This page shows credits for all media assets.
 */
enum PageCredits {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CanvasSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException,
            SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        CanvasPageUtils.startPage(htm, siteTitle);

        htm.sH(1).add("Media Credits").eH(1);

        htm.sH(2).addln("Course Module Thumbnail Images").eH(2);

        htm.addln("<dl>");
        htm.addln("  <dt>'Start Here' thumbnail</dt>");
        htm.addln("  <dt>Clemens van Lay, December 20, 2020, published under the Unsplash license.</dt>");
        htm.addln("</dl>");

        htm.addln("<dl>");
        htm.addln("  <dt>'How to Successfully Navigate this Course' thumbnail</dt>");
        htm.addln("  <dt>Daniel Xavier, published May 28, 2018 under the Pexels license.</dt>");
        htm.addln("</dl>");

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
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
