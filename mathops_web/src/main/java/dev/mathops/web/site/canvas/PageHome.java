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
 * A page that presents the set of courses in which the student is enrolled, the date range in the current semester that
 * each course occupies, and all due dates associated with each course.  Students can then select a course, which
 * redirects to "course.html?course=ID".
 */
enum PageHome {
    ;

    /** The page. */
    static final String PAGE = "home.html";

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

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, true, Page.NO_BARS, null, false, true);

        htm.sH(2).add("Home").eH(2);

        htm.sP().add("TODO: Announcements like upcoming outage notifications, holds.").eP();

        htm.sP().add("TODO: Present a list of the Precalculus courses in which student is enrolled.").eP();

        htm.sP().add("TODO: Present a semester calendar showing all courses, today, due dates, ",
                "and list of upcoming things.").eP();

        htm.sP().add("TODO: Present student progress in each course.").eP();

        htm.sP().add("TODO: Present links to full textbook, help, information.").eP();

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}
