package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import dev.mathops.web.site.canvas.StdsMasteryStatus;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * A page that presents the top-level view of a course.
 */
public enum PageCourse {
    ;

    /**
     * Tests request parameters, shows the course page if valid or redirects to the home page if not.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param req      the request
     * @param resp     the response
     * @param session  the login session
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
            final String homePath = site.makeRootPath("home.htm");
            resp.sendRedirect(homePath);
        } else {
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);
            if (course == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentCoursePage(cache, site, req, resp, session, registration, course);
            }
        }
    }

    /**
     * Presents the top-level view of a course in which the student is enrolled
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
    static void presentCoursePage(final Cache cache, final CanvasSite site, final ServletRequest req,
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

            CanvasPageUtils.emitLeftSideMenu(htm, course, null, ECanvasPanel.HOME);

            htm.sDiv("maincontainer");
            htm.sDiv("flexmain");

            emitCourseAnnouncements(cache, htm);

            emitCourseImageAndWelcome(htm, course, section);

            final CourseSiteLogic logic = new CourseSiteLogic(cache, site.getSite().profile, session);
            logic.gatherData();

            emitCourseStatus(cache, logic, registration, section, htm);

            htm.eDiv(); // flexmain

            emitRightSideToDo(cache, htm);

            htm.eDiv(); // mainconainer
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Emits a left-side menu with links for [View Course Calendar] and a list of upcoming due dates or milestones.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitRightSideToDo(final Cache cache, final HtmlBuilder htm) throws SQLException {

        htm.sDiv("flextodo");

        htm.sDiv(null, "style='margin:0; border-bottom:1px solid #C7CDD1;'");
        htm.sP().add("<strong>To Do</strong>").eP();
        htm.eDiv();

        htm.eDiv(); // flextodo
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
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param course  the course object
     * @param section the course section record
     */
    private static void emitCourseImageAndWelcome(final HtmlBuilder htm, final StandardsCourseRec course,
                                                  final StandardsCourseSectionRec section) {

        htm.sH(2).add("Welcome to ", course.courseId, ": <span style='color:#D9782D'>", course.courseTitle,
                "</span>").eH(2);
        htm.hr();
    }

    /**
     * Emits a display of the student's status in the course.
     *
     * @param cache   the data cache
     * @param reg     the registration record
     * @param section the course section record
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    private static void emitCourseStatus(final Cache cache, final CourseSiteLogic logic, final RawStcourse reg,
                                         final StandardsCourseSectionRec section, final HtmlBuilder htm) {

        Log.info("Status reg: " + reg.course, CoreConstants.SPC, reg.sect, CoreConstants.SPC, reg.paceOrder);

        if ("Y".equals(reg.iInProgress)) {
            // TODO: Incomplete status
        } else if (reg.paceOrder == null) {
            htm.sP();
            htm.addln("Unable to determine class schedule.");
            htm.eP();
        } else {
            final SiteData data = logic.data;
            if (data == null) {
                htm.sP();
                htm.addln("Unable to gather course data: ", logic.getError());
                htm.eP();
            } else {
                final SiteDataCfgCourse courseData = data.courseData.getCourse(reg.course, reg.sect);

                final List<RawStcourse> paceRegs = data.registrationData.getPaceRegistrations();
                final int pace = paceRegs == null ? 0 : PaceTrackLogic.determinePace(paceRegs);
                final String paceTrack = paceRegs == null ? CoreConstants.EMPTY :
                        PaceTrackLogic.determinePaceTrack(paceRegs, pace);

                final ZonedDateTime now = ZonedDateTime.now();
                final boolean isTutor = data.studentData.isSpecialType(now, "TUTOR");

                final StdsMasteryStatus masteryStatus = new StdsMasteryStatus(cache, courseData, pace, paceTrack, reg,
                        isTutor);

                final int targetsFirstHalf = masteryStatus.getNbrMasteredInFirstHalf();
                final int targetsSecondHalf = masteryStatus.getNbrMasteredInSecondHalf();
                final int targetsReachedTotal = targetsFirstHalf + targetsSecondHalf;

                htm.sP();
                htm.addln("To pass this course, you must complete at least <b>18</b> (out of 24) learning targets ",
                        "(including <strong>all</strong>  of the <strong>essential</strong> learning targets).  ",
                        "Your grade will then be based on total points earned.");
                htm.eP();
                htm.div("vgap0");

                final String targetsReachedStr = Integer.toString(targetsReachedTotal);
                htm.sP();
                htm.addln("Learning Targets Completed: <b>", targetsReachedStr, "</b> (out of 24 total)");
                htm.eP();

                final int count = masteryStatus.numStandardsPending;
                if (count > 0) {
                    final String countStr = Integer.toString(count);
                    htm.sP().addln("<strong>You are currently eligible to complete ", countStr,
                            " learning targets in the testing center.</strong>").eP();
                }

                htm.div("vgap0");

                final String urlCourse = URLEncoder.encode(reg.course, StandardCharsets.UTF_8);
                htm.sP().add("To get started, go to <a class='ulink' href='modules.html'><b>Modules</b></a> ",
                        "and read the <a class='ulink' href='start_here.html'><b>Start Here</b></a> page.").eP();
            }
        }
    }
}
