package dev.mathops.web.site.canvas;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.logic.RegistrationsLogic;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

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

            RawStcourse registration = null;
            for (final RawStcourse reg : registrations.uncountedIncompletes()) {
                if (reg.course.equals(selectedCourse)) {
                    registration = reg;
                    break;
                }
            }
            for (final RawStcourse reg : registrations.inPace()) {
                if (reg.course.equals(selectedCourse)) {
                    registration = reg;
                    break;
                }
            }

            if (registration == null) {
                final String homePath = site.makePagePath("home.html", null);
                resp.sendRedirect(homePath);
            } else {
                presentCoursePage(cache, site, req, resp, session, registration);
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
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentCoursePage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                  final HttpServletResponse resp, final ImmutableSessionInfo session,
                                  final RawStcourse registration)
            throws IOException, SQLException {

        final String stuId = registration.stuId;
        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

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
            final String homePath = site.makePagePath("home.html", null);
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();
            Page.startOrdinaryPage(htm, siteTitle, session, true, Page.ADMIN_BAR, null, false, true);

            final String studentName = student.getScreenName();

            htm.sH(2, "title");
            if ("Y".equals(csection.courseLabelShown)) {
                htm.add(course.courseLabel);
                htm.add(": ");
            }
            htm.add("<span style='color:#D9782D'>", course.courseName, "</span>");
            htm.br().add("<small>Section ", csection.sect, "</small>");
            htm.eH(2).hr();

            emitLeftSideMenu(cache, htm);
            emitRightSideMenu(cache, htm);
            emitCourseAnnouncements(cache, htm);
            emitCourseImageAndWelcome(cache, htm);

            final CourseSiteLogic logic = new CourseSiteLogic(cache, site.getSite().profile, session);
            logic.gatherData();

            emitCourseStatus(cache, logic, registration, csection, htm);

            htm.addln("<a href='course_text.html?course=", course.course,
                    "&mode=course'><img style='width:210px;margin-left:10px;' ",
                    "src='/www/images/etext/textbook.png'/></a><br/>");

            htm.addln("<a style='width:202px;margin-left:10px;text-align:center;' ",
                    "class='smallbtn' href='course_text.html?course=", course.course,
                    "&mode=course'>", "Open Textbook", "</a>");

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
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

    /**
     * Emits a display of the student's status in the course.
     *
     * @param cache    the data cache
     * @param reg      the registration record
     * @param csection the course section record
     * @param htm      the {@code HtmlBuilder} to which to append
     */
    private static void emitCourseStatus(final Cache cache, final CourseSiteLogic logic, final RawStcourse reg,
                                         final RawCsection csection, final HtmlBuilder htm) {

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
                final int totalPoints = masteryStatus.score;

                htm.sDiv("hours");
                htm.addln(
                        "To pass this course, you must master at least <b>20</b> learning targets (at least <b>10</b> ",
                        "from the first half of the course, and at least <b>10</b> from the second half).  Your grade ",
                        "will then be based on total points earned.");
                htm.eDiv();

                htm.sP();
                final String targetsReachedStr = Integer.toString(targetsReachedTotal);
                htm.addln("Learning Targets Mastered: <b>", targetsReachedStr, "</b> (out of 24 total)").br();

                // Bar chart showing number of standards mastered in each half of the course
                // 3 pixel border all around, 16 pixels per standard plus 1-pixel dividing line
                // 6 + (12)(16) + 11 = 209 pixels total, 26-pixel gap, so 444 overall, second box starts at 235

                htm.addln("<svg width='445' height='62'>");
                htm.addln("<defs>");
                htm.addln(" <linearGradient id='grad1' x1='0%' y1='0%' x2='0%' y2='100%'>");
                htm.addln("  <stop offset='0%' style='stop-color:rgb(217, 120, 45)'/>");
                htm.addln("  <stop offset='35%' style='stop-color:rgb(244,218,198)'/>");
                htm.addln("  <stop offset='100%' style='stop-color:rgb(193, 104, 35)'/>");
                htm.addln(" </linearGradient>");
                htm.addln("</defs>");

                // Background
                htm.addln("  <rect x='2' y='2' width='205' height='14' ",
                        "style='fill:rgb(250,250,235);stroke-width:1px;stroke:black;'/>");
                htm.addln("  <rect x='237' y='2' width='205' height='14' ",
                        "style='fill:rgb(250,250,235);stroke-width:1px;stroke:black;'/>");

                // Fill the last two with green to show that as the target
                // 3 + (10)(16) + 10 = 173
                htm.addln(
                        "  <rect x='173' y='3' width='34' height='12' style='fill:rgb(120,255,120);stroke-width:0px;" +
                        "'/>");
                htm.addln(
                        "  <rect x='408' y='3' width='34' height='12' style='fill:rgb(120,255,120);stroke-width:0px;" +
                        "'/>");

                // Paint the number of targets achieved and pending in each half
                if (targetsFirstHalf + masteryStatus.numStandardsPendingFirstHalf > 0) {
                    int left = 3;
                    if (targetsFirstHalf > 0) {
                        final int w = 17 * targetsFirstHalf;
                        final String xStr = Integer.toString(left);
                        final String wStr = Integer.toString(w);
                        htm.addln("  <rect x='", xStr, "' y='3' width='", wStr,
                                "' height='12' style='fill:url(#grad1);stroke-width:0px;'/>");
                        left += w;
                    }
                    if (masteryStatus.numStandardsPendingFirstHalf > 0) {
                        final int w = 17 * masteryStatus.numStandardsPendingFirstHalf;
                        final String xStr = Integer.toString(left);
                        final String wStr = Integer.toString(w);
                        htm.addln("  <rect x='", xStr, "' y='3' width='", wStr,
                                "' height='12' style='fill:#F0E68C;stroke-width:0px;'/>");
                    }
                }
                if (targetsSecondHalf + masteryStatus.numStandardsPendingSecondHalf > 0) {
                    int left = 238;
                    if (targetsSecondHalf > 0) {
                        final int w = 17 * targetsSecondHalf;
                        final String xStr = Integer.toString(left);
                        final String wStr = Integer.toString(w);
                        htm.addln("  <rect x='", xStr, "' y='3' width='", wStr,
                                "' height='12' style='fill:url(#grad1);stroke-width:0px;'/>");
                        left += w;
                    }
                    if (masteryStatus.numStandardsPendingSecondHalf > 0) {
                        final int w = 17 * masteryStatus.numStandardsPendingSecondHalf;
                        final String xStr = Integer.toString(left);
                        final String wStr = Integer.toString(w);
                        htm.addln("  <rect x='", xStr, "' y='3' width='", wStr,
                                "' height='12' style='fill:#F0E68C;stroke-width:0px;'/>");
                    }
                }

                // Recessed bevel border on both boxes...
                htm.addln("  <rect x='0' y='0' width='209' height='17' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(230,230,230);'/>");
                htm.addln("  <rect x='1' y='1' width='207' height='16' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(200,200,200);'/>");

                htm.addln("  <rect x='235' y='0' width='209' height='17' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(230,230,230);'/>");
                htm.addln("  <rect x='236' y='1' width='207' height='16' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(200,200,200);'/>");

                // Dividing lines
                for (int i = 1; i < 12; ++i) {
                    final String x1 = Float.toString((float) (3.5 + 17.0 * (double) i));
                    if (i < targetsFirstHalf) {
                        htm.addln("  <line x1='", x1, "' y1='2.5' x2='", x1,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(80,80,80);'/>");
                    } else {
                        htm.addln("  <line x1='", x1, "' y1='2.5' x2='", x1,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(180,180,180);'/>");
                    }

                    final String x2 = Float.toString((float) (238.5 + 17.0 * (double) i));
                    if (i < targetsSecondHalf) {
                        htm.addln("  <line x1='", x2, "' y1='2.5' x2='", x2,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(80,80,80);'/>");
                    } else {
                        htm.addln("  <line x1='", x2, "' y1='2.5' x2='", x2,
                                "' y2='15.5' style='stroke-width:1px;stroke:rgb(180,180,180);'/>");
                    }
                }

                final String firstHalfStr = Integer.toString(targetsFirstHalf);
                htm.addln("  <text x='6' y='34' style='font-size:16px;'>", firstHalfStr,
                        " mastered in first half</text>");

                final String secondHalfStr = Integer.toString(targetsSecondHalf);
                htm.addln("  <text x='241' y='34' style='font-size:16px;'>", secondHalfStr,
                        " mastered in second half</text>");

                final String firstHalfStr2 = Integer.toString(masteryStatus.numStandardsPendingFirstHalf);
                htm.addln("  <text x='6' y='55' style='font-size:16px;'>Eligible for ", firstHalfStr2,
                        " in first half</text>");

                final String secondHalfStr2 = Integer.toString(masteryStatus.numStandardsPendingSecondHalf);
                htm.addln("  <text x='241' y='55' style='font-size:16px;'>Eligible for ", secondHalfStr2,
                        " in second half</text>");

                htm.addln("</svg>");
                htm.eP();

                htm.sP();
                final String totalPtsStr = Integer.toString(totalPoints);
                htm.addln("Current point total: <strong>", totalPtsStr, "</strong> (out of 120 possible)").br();

                htm.addln("<svg width='444' height='24'>");
                htm.addln("<defs>");
                htm.addln(" <linearGradient id='grad1' x1='0%' y1='0%' x2='0%' y2='100%'>");
                htm.addln("  <stop offset='0%' style='stop-color:rgb(190,190,233)'/>");
                htm.addln("  <stop offset='26%' style='stop-color:rgb(216,216,245)'/>");
                htm.addln("  <stop offset='100%' style='stop-color:rgb(100,100,217)'/>");
                htm.addln(" </linearGradient>");
                htm.addln("</defs>");

                // Recessed bevel border on progress bar
                htm.addln(" <rect x='0' y='0' width='444' height='24' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(230,230,230);'/>");
                htm.addln(" <rect x='1' y='1' width='442' height='22' ",
                        "style='fill:none;stroke-width:1px;stroke:rgb(200,200,200);'/>");
                htm.addln(" <rect x='2' y='2' width='440' height='20' ",
                        "style='fill:rgb(250,250,235);stroke-width:1px;stroke:black'/>");

                // Shade grade ranges

                if (csection.aMinScore != null) {
                    final int aBottom = 3 + 438 * csection.aMinScore.intValue() / 120;
                    final int aWidth = 441 - aBottom;
                    final int aTextPos = aBottom + aWidth / 2 - 4;

                    final String aBottomStr = Integer.toString(aBottom);
                    final String aWidthStr = Integer.toString(aWidth);
                    final String aTextPosStr = Integer.toString(aTextPos);
                    htm.addln("  <rect x='", aBottomStr, "' y='3' width='", aWidthStr,
                            "' height='18' style='fill:rgb(150,255,150);stroke-width:0;'/>");
                    htm.addln("  <text x='", aTextPosStr, "' y='18' style='font-size:16px;'>A</text>");

                    if (csection.bMinScore != null) {
                        final int bBottom = 3 + 438 * csection.bMinScore.intValue() / 120;
                        final int bWidth = aBottom - bBottom;
                        final int bTextPos = bBottom + bWidth / 2 - 4;

                        final String bBottomStr = Integer.toString(bBottom);
                        final String bWidthStr = Integer.toString(bWidth);
                        final String bTextPosStr = Integer.toString(bTextPos);
                        htm.addln("  <rect x='", bBottomStr, "' y='3' width='", bWidthStr,
                                "' height='18' style='fill:rgb(150,235,150);stroke-width:0;'/>");
                        htm.addln("  <text x='", bTextPosStr, "' y='18' style='font-size:16px;'>B</text>");

                        if (csection.cMinScore != null) {
                            final int cBottom = 3 + 438 * csection.cMinScore.intValue() / 120;
                            final int cWidth = bBottom - cBottom;
                            final int cTextPos = cBottom + cWidth / 2 - 4;

                            final String cBottomStr = Integer.toString(cBottom);
                            final String cWidthStr = Integer.toString(cWidth);
                            final String cTextPosStr = Integer.toString(cTextPos);
                            htm.addln("  <rect x='", cBottomStr, "' y='3' width='", cWidthStr,
                                    "' height='18' style='fill:rgb(150,215,150);stroke-width:0;'/>");
                            htm.addln("  <text x='", cTextPosStr, "' y='18' style='font-size:16px;'>C</text>");

                            if (csection.dMinScore != null) {
                                final int dBottom = 3 + 438 * csection.dMinScore.intValue() / 120;
                                final int dWidth = bBottom - cBottom;
                                final int dTextPos = cBottom + cWidth / 2 - 4;

                                final String dBottomStr = Integer.toString(dBottom);
                                final String dWidthStr = Integer.toString(dWidth);
                                final String dTextPosStr = Integer.toString(dTextPos);
                                htm.addln("  <rect x='", dBottomStr, "' y='3' width='", dWidthStr,
                                        "' height='18' style='fill:rgb(150,215,150);stroke-width:0;'/>");
                                htm.addln("  <text x='", dTextPosStr, "' y='18' style='font-size:16px;'>C</text>");
                            }
                        }
                    }
                }

                // Shade in the student's progress (interior of box is 438 x 18)
                final int progressWidth = 438 * totalPoints / 120;
                final String progressWidthStr = Integer.toString(progressWidth);
                htm.addln("  <rect x='3' y='3' width='", progressWidthStr,
                        "' height='18' style='fill:url(#grad1);fill-opacity=0.3;stroke:none;'/>");

                htm.addln("</svg>");
                htm.eP();

                final int count = masteryStatus.numStandardsPending;
                if (count > 0) {
                    final String countStr = Integer.toString(count);
                    htm.sP().addln("<strong>You are currently eligible to master ", countStr,
                            " standards in the testing center.</strong>").eP();

//                if (count >= 6 && masteryStatus.maxUnit < 8) {
//                    final String nextUnitStr = Integer.toString(masteryStatus.maxUnit + 1);
//                    htm.sP().addln("<strong>You will not be able to access unit ", nextUnitStr,
//                                    " until you master standards in the testing center to get this total below six.",
//                                    "</strong>").eP();
//                }
                }

                htm.div("vgap0");
            }
        }
    }
}
