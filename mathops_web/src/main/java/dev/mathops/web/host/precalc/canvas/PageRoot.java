package dev.mathops.web.host.precalc.canvas;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.course.RegistrationsLogic;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * A "root" page that presents the set of courses in which the student is enrolled, the date range in the current
 * semester that each course occupies, and all due dates associated with each course.  Students can then select a
 * course, which redirects to "{SITE_PATH}/courses/COURSE_ID/course.html".
 */
enum PageRoot {
    ;

    /**
     * Generates the page.
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

        final String stuId = session.getEffectiveUserId();
        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, session, true, Page.ADMIN_BAR, null, false, true);

        final String studentName = student.getScreenName();
        htm.sH(2).add("Welcome ", studentName).eH(2);

        emitProgramAnnouncements(cache, htm);
        emitHolds(cache, stuId, htm);
        emitEnrolledCourses(cache, site, stuId, htm);
        emitSemesterCalendar(cache, stuId, htm);
        emitUpcoming(cache, stuId, htm);
        emitInformation(cache, stuId, htm);

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits any program-wide announcements that are currently active.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitProgramAnnouncements(final Cache cache, final HtmlBuilder htm) throws SQLException {

        // TODO:
    }

    /**
     * Emits any holds that exist on the student's record.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitHolds(final Cache cache, final String stuId, final HtmlBuilder htm) throws SQLException {

        final List<RawAdminHold> studentHolds = RawAdminHoldLogic.queryByStudent(cache, stuId);

        final int numHolds = studentHolds.size();
        if (numHolds > 0) {
            htm.sH(3).add("Account Holds").eH(3);

            htm.sP();
            if (numHolds == 1) {
                htm.add("There is an administrative hold on your account:");
            } else {
                htm.add("There are administrative holds on your account:");
            }
            htm.eP();

            for (final RawAdminHold hold : studentHolds) {
                final String holdId = hold.holdId;
                final String message = RawAdminHoldLogic.getStudentMessage(holdId);
                htm.sP().add(message).eP();
            }
        }
    }

    /**
     * Emits a block with a link button for each course in which a student is enrolled.  Below each course button is a
     * short display of student status/progress in each course.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitEnrolledCourses(final Cache cache, final CanvasSite site, final String stuId,
                                            final HtmlBuilder htm) throws SQLException {

        htm.sH(3).add("My Precalculus Courses").eH(3);

        final RegistrationsLogic.ActiveTermRegistrations registrations =
                RegistrationsLogic.gatherActiveTermRegistrations(cache, stuId);

        final List<String> warnings = registrations.warnings();
        if (!warnings.isEmpty()) {
            // TODO: Show the warnings to the student - make sure we are using student-facing language
        }

        final MainData mainData = cache.getMainData();
        final List<StandardsCourseRec> courses = mainData.getStandardsCourses();

        boolean hasCourse = false;

        final List<RawStcourse> uncountedInc = registrations.uncountedIncompletes();
        if (!uncountedInc.isEmpty()) {

            htm.sH(4).add("Incomplete Courses from a prior Semester").eH(4);
            htm.sP();

            for (final RawStcourse reg : uncountedInc) {
                boolean seeking = true;
                for (final StandardsCourseRec course : courses) {
                    if (course.courseId.equals(reg.course)) {
                        final String href = site.makeCoursePath(CanvasSite.COURSE_HOME_PAGE, reg.course);
                        htm.add("<a href='", href, "' class='smallbtn'>", reg.course, "</a> &nbsp; ");
                        seeking = false;
                        break;
                    }
                }

                if (seeking) {
                    Log.warning("Course not found: ", reg.course);
                    htm.add(reg.course, " &nbsp; ");
                }

                hasCourse = true;
            }
            htm.eP();
            // TODO: Show deadline date for Incomplete, course progress meter
        }

        final List<RawStcourse> inPace = registrations.inPace();

        if (!inPace.isEmpty()) {
            htm.sH(4).add("Current Courses").eH(4);
            htm.sP();

            for (final RawStcourse reg : inPace) {
                boolean seeking = true;
                for (final StandardsCourseRec course : courses) {
                    if (course.courseId.equals(reg.course)) {
                        final String href = site.makeCoursePath(CanvasSite.COURSE_HOME_PAGE, reg.course);
                        htm.add("<a href='", href, "' class='smallbtn'>", reg.course, "</a> &nbsp; ");
                        seeking = false;
                        break;
                    }
                }

                if (seeking) {
                    Log.warning("Metadata does not include ", reg.course);
                    htm.add(reg.course, "&nbsp; ");
                }

                hasCourse = true;
            }
            htm.eP();

            // TODO: Show course status, progress meter
        }

        if (!hasCourse) {
            htm.sP().add("You are not enrolled in any Precalculus courses this semester.").eP();
        }
    }

    /**
     * Emits a calendar display showing the semester with the date range for each enrolled course and the current date.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitSemesterCalendar(final Cache cache, final String stuId, final HtmlBuilder htm)
            throws SQLException {

        // TODO:
    }

    /**
     * Emits a list of upcoming due dates or milestones.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitUpcoming(final Cache cache, final String stuId, final HtmlBuilder htm) throws SQLException {

        // TODO:
    }

    /**
     * Emits general information, including a link to the full textbook, information on help and resources.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitInformation(final Cache cache, final String stuId, final HtmlBuilder htm)
            throws SQLException {

        // TODO:
    }
}
