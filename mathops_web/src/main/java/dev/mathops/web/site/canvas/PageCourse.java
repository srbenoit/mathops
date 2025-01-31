package dev.mathops.web.site.canvas;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A page that presents the top-level view of a course.
 */
enum PageCourse {
    ;

    /** The page. */
    static final String PAGE = "course.html";

    /**
     * Generates the welcome page that users see when they access the site with either the '/' or '/index.html' paths
     * and are logged in, or if they access the "/home.html" path.
     *
     * <p>
     * If this page is accessed with a "course=COURSE_ID" parameter in the request, the dashboard for that course will
     * be shown.  Otherwise, the set of all courses in which the student is enrolled is shown, and the student can
     * choose one to jump into that course.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CanvasSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException,
            SQLException {

        final String selectedCourse = req.getParameter(CanvasSite.COURSE_PARAM);

        if (selectedCourse == null) {
            final String homePath = site.makePagePath("home.html", null);
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();
            Page.startOrdinaryPage(htm, siteTitle, null, true, Page.NO_BARS, null, false, true);

            htm.sH(2).add("Course page (Course is ", selectedCourse, ")").eH(2);

            htm.sP().add("TODO: Announcements like upcoming outage notifications, holds.").eP();

            htm.sP().add("TODO: Left-side menu with [Home], [Announcements], [Assignments], [Modules], [Grades], ",
                    "[Syllabus], [Course Survey].").eP();

            htm.sP().add("TODO: right-side menu with [View Course Calendar").eP();

            htm.sP().add("TODO: Right-side TODO list.").eP();

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }
}
