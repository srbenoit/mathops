package dev.mathops.web.host.precalc.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.host.precalc.canvas.CanvasPageUtils;
import dev.mathops.web.host.precalc.canvas.CanvasSite;
import dev.mathops.web.host.precalc.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This page shows the "Announcements" content.
 */
public enum PageAnnouncements {
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
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);
            if (course == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentAnnouncements(cache, site, req, resp, session, registration, course);
            }
        }
    }

    /**
     * Presents the "Announcements" information.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @param course       the course record
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentAnnouncements(final Cache cache, final CanvasSite site, final ServletRequest req,
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

            CanvasPageUtils.emitLeftSideMenu(htm, course, null, ECanvasPanel.ANNOUNCEMENTS);

            htm.sDiv("flexmain");

            htm.sH(2).add("Announcements").eH(2);
            htm.hr();

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }
}
