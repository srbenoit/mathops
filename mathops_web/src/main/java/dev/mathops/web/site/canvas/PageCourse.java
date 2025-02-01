package dev.mathops.web.site.canvas;

import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.RegistrationsLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
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

    /**
     * Tests request parameters, shows the course page if valid or redirects to the home page if not.
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
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String selectedCourse = req.getParameter(CanvasSite.COURSE_PARAM);

        if (selectedCourse == null) {
            final String homePath = site.makePagePath("home.html", null);
            resp.sendRedirect(homePath);
        } else {
            // Make sure student is actually enrolled in the selected course

            final String stuId = session.getEffectiveUserId();
            final RegistrationsLogic.ActiveTermRegistrations registrations =
                    RegistrationsLogic.gatherActiveTermRegistrations(cache, stuId);

            boolean enrolled = false;
            for (final RawStcourse reg : registrations.uncountedIncompletes()) {
                if (reg.course.equals(selectedCourse)) {
                    enrolled = true;
                    break;
                }
            }
            for (final RawStcourse reg : registrations.inPace()) {
                if (reg.course.equals(selectedCourse)) {
                    enrolled = true;
                    break;
                }
            }

            if (enrolled) {
                presentCoursePage(cache, site, req, resp, session);
            } else {
                final String homePath = site.makePagePath("home.html", null);
                resp.sendRedirect(homePath);
            }
        }
    }

    /**
     * Presents the top-level view of a course in which the student is enrolled
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentCoursePage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                  final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException,
            SQLException {

        final String stuId = session.getEffectiveUserId();
        final String selectedCourse = req.getParameter(CanvasSite.COURSE_PARAM);
        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, session, true, Page.ADMIN_BAR, null, false, true);

        final String studentName = student.getScreenName();

        htm.sH(2).add(selectedCourse, " (logged in as ", studentName, ")").eH(2);

        emitLeftSideMenu(cache, htm);
        emitRightSideMenu(cache, htm);
        emitCourseAnnouncements(cache, htm);
        emitCourseImageAndWelcome(cache, htm);

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits a left-side menu with links for [Home], [Announcements], [Assignments], [Modules], [Grades], [Syllabus],
     * [Course Survey].
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitLeftSideMenu(final Cache cache, final HtmlBuilder htm) throws SQLException {

        // TODO:
    }

    /**
     * Emits a left-side menu with links for [View Course Calendar] and a list of upcoming due dates or milestones.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitRightSideMenu(final Cache cache, final HtmlBuilder htm) throws SQLException {

        // TODO:
    }

    /**
     * Emits any course-level announcements that are currently active.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseAnnouncements(final Cache cache, final HtmlBuilder htm) throws SQLException {

        // TODO:
    }

    /**
     * Emits the course image and welcome content.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseImageAndWelcome(final Cache cache, final HtmlBuilder htm) throws SQLException {

        // TODO:
    }
}
