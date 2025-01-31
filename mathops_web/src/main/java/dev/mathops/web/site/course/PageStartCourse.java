package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.servlet.StartCourse;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of the home page for a course site.
 */
enum PageStartCourse {
    ;

    /**
     * Generates the page with contact information.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final String course = req.getParameter("course");

        if (AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final StartCourse start = new StartCourse(site.getDbProfile());

            if (start.startCourse(cache, session.getNow(), session.getEffectiveUserId(), course)) {
                resp.sendRedirect("course.html?course=" + course.replace(CoreConstants.SPC, "+") + "&mode=course");
            } else {
                final HtmlBuilder htm = new HtmlBuilder(2000);
                Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                        Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

                htm.sDiv("menupanelu");
                CourseMenu.buildMenu(cache, site, session, logic, htm);
                htm.sDiv("panelu");

                htm.sDiv("error").add("Failed to start ", course, CoreConstants.DOT).eDiv();

                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
            }
        }
    }
}
