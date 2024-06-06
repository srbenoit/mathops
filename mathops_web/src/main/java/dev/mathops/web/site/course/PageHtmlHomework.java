package dev.mathops.web.site.course;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.hw.HomeworkSession;
import dev.mathops.web.site.html.hw.HomeworkSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZonedDateTime;

/**
 * Presents a homework assignment.
 */
enum PageHtmlHomework {
    ;

    /**
     * Starts the page that shows a lesson with student progress.
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
    static void startHomework(final Cache cache, final CourseSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session,
                              final CourseSiteLogic logic) throws IOException, SQLException {

        final String assignmentId = req.getParameter("assign");
        final String coursemode = req.getParameter("coursemode");
        final String mode = req.getParameter("mode");
        final String course = req.getParameter("course");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assignmentId) || AbstractSite.isParamInvalid(coursemode)
                || AbstractSite.isParamInvalid(mode) || AbstractSite.isParamInvalid(course)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assignmentId='", assignmentId, "'");
            Log.warning("  coursemode='", coursemode, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final boolean isNewCourse = RawRecordConstants.MATH117.equals(course)
                    || RawRecordConstants.MATH118.equals(course)
                    || RawRecordConstants.MATH124.equals(course)
                    || RawRecordConstants.MATH125.equals(course)
                    || RawRecordConstants.MATH126.equals(course);

            final HomeworkSessionStore store = HomeworkSessionStore.getInstance();
            HomeworkSession hs = store.getHomeworkSession(session.loginSessionId, assignmentId);

            if (hs == null) {
                final String redirect;
                if (isNewCourse) {
                    redirect = "course_text_module.html?course=" + course + "&module=" + unit + "&mode=" + coursemode;
                } else {
                    redirect = "course.html?course=" + course + "&mode=" + coursemode;
                }

                final String effectiveUserId = session.getEffectiveUserId();
                final boolean isPractice = "practice".equals(mode);
                hs = new HomeworkSession(cache, site.siteProfile, session.loginSessionId, effectiveUserId, assignmentId,
                        isPractice, redirect);
                store.setHomeworkSession(hs);
            }

            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();
            Page.startOrdinaryPage(htm, siteTitle, session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false,
                    true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

            htm.sDiv("nav");
            htm.sDiv("aslines");

            htm.add(" <a class='linkbtn' ");
            if (isNewCourse) {
                htm.add("href='course_text_module.html?course=", course, "&module=", unit, "&mode=", coursemode,
                        "'><em>");
                htm.add("Return to Module ", unit);
            } else {
                htm.add("href='lesson.html?course=", course, "&unit=", unit, "&lesson=", lesson, "&mode=", coursemode,
                        "'><em>");
                htm.add("Return to the Objective");
            }
            htm.addln("</em></a>");

            htm.eDiv();
            htm.eDiv();

            htm.div("clear");
            htm.sDiv("gap").add("&nbsp;").eDiv();

            htm.addln("<form action='update_homework.html' method='POST'>");
            htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
            htm.addln(" <input type='hidden' name='coursemode' value='", coursemode, "'>");
            htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
            htm.addln(" <input type='hidden' name='course' value='", course, "'>");
            htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
            htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");

            // Prevent POST from other thread from accessing session at the same time
            synchronized (hs) {
                final ZonedDateTime now = session.getNow();
                hs.generateHtml(cache, now, htm);
            }

            htm.addln("</form>");

            htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            Page.endOrdinaryPage(cache, site, htm, true);

            final String htmString = htm.toString();
            final byte[] bytes = htmString.getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
        }
    }

    /**
     * Handles a POST request.
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
    static void updateHomework(final Cache cache, final CourseSite site, final ServletRequest req,
                               final HttpServletResponse resp, final ImmutableSessionInfo session,
                               final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String assignmentId = req.getParameter("assign");
        final String coursemode = req.getParameter("coursemode");
        final String mode = req.getParameter("mode");
        final String course = req.getParameter("course");
        final String unit = req.getParameter("unit");
        final String lesson = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(assignmentId) || AbstractSite.isParamInvalid(coursemode)
                || AbstractSite.isParamInvalid(mode) || AbstractSite.isParamInvalid(course)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(lesson)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  assignmentId='", assignmentId, "'");
            Log.warning("  coursemode='", coursemode, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  course='", course, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  lesson='", lesson, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String redirect = null;

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

            htm.sDiv("nav");
            htm.sDiv("aslines");
            htm.add(" <a class='linkbtn' ");
            if (RawRecordConstants.MATH116.equals(course)
                    || RawRecordConstants.MATH117.equals(course)
                    || RawRecordConstants.MATH118.equals(course)
                    || RawRecordConstants.MATH124.equals(course)
                    || RawRecordConstants.MATH125.equals(course)
                    || RawRecordConstants.MATH126.equals(course)) {

                htm.add("href='course_text_module.html?course=", course, "&module=", unit, "&mode=", coursemode,
                        "'><em>");
                htm.add("Return to Module ", unit);
            } else {
                htm.add("href='lesson.html?course=", course, "&unit=", unit, "&lesson=", lesson, "&mode=", coursemode,
                        "'><em>");
                htm.add("Return to the Objective");
            }
            htm.addln("</em></a>");
            htm.eDiv();
            htm.eDiv();

            htm.div("clear");
            htm.sDiv("gap").add("&nbsp;").eDiv();

            final HomeworkSessionStore store = HomeworkSessionStore.getInstance();
            final HomeworkSession hs =
                    store.getHomeworkSession(session.loginSessionId, assignmentId);
            if (hs == null) {
                htm.sP().add("Assignment has not been started.").eP();
            } else {
                htm.addln("<form action='update_homework.html' method='POST'>");
                htm.addln(" <input type='hidden' name='assign' value='", assignmentId, "'>");
                htm.addln(" <input type='hidden' name='coursemode' value='", coursemode, "'>");
                htm.addln(" <input type='hidden' name='mode' value='", mode, "'>");
                htm.addln(" <input type='hidden' name='course' value='", course, "'>");
                htm.addln(" <input type='hidden' name='unit' value='", unit, "'>");
                htm.addln(" <input type='hidden' name='lesson' value='", lesson, "'>");

                // Prevent POST from other thread from accessing session at the same time
                synchronized (hs) {
                    redirect = hs.processPost(cache, session, req, htm);
                }

                htm.addln("</form>");
            }

            if (redirect == null) {
                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML,
                        htm.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirect);
            }
        }
    }
}
