package dev.mathops.web.site.course;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawlogic.RawPrereqLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Generates the content of the home page for a course site.
 */
enum PageSkillsReview {
    ;

    /**
     * Generates the page with contact information.
     *
     * @param cache    the data cache
     * @param siteType the site type
     * @param site     the owning site
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ESiteType siteType, final CourseSite site,
                      final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session, final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String course = req.getParameter("course");
        final String mode = req.getParameter("mode");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(mode)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  mode='", mode, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final StudentCourseStatus courseStatus = new StudentCourseStatus(site.getDbProfile());

            courseStatus.gatherData(cache, session, session.getEffectiveUserId(), course, false,
                    !"course".equals(mode));

            // FIXME: Hack here - if a student is in M 117, section 401/801/003, and prerequisite
            // satisfied is provisional (as set in RegistrationCache for this population), then we
            // make the gateway course M 100T to force a larger Skills Review.
            final RawStcourse stcourse = courseStatus.getStudentCourse();

            String lessonCourse = null;

            if (RawRecordConstants.M117.equals(stcourse.course)
                    && ("801".equals(stcourse.sect)
                    || "809".equals(stcourse.sect)
                    || "401".equals(stcourse.sect))
                    && "P".equals(stcourse.prereqSatis)) {

                lessonCourse = RawRecordConstants.M100T;
            }

            // FIXME: Hack here - if a student is in M 1170, and prerequisite satisfied is
            // provisional (as set in RegistrationCache for this population), then we make the
            // gateway course M 100T to force a larger Skills Review.
            if (RawRecordConstants.M1170.equals(stcourse.course)) {

                // No real student course row to use for prereq checking, so do it manually...
                final List<String> prereqs = RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M117);

                boolean prereq = prereqs.isEmpty();
                final int count = prereqs.size();
                for (int j = 0; !prereq && j < count; ++j) {
                    prereq = CourseSite.hasCourseAsPrereq(cache, courseStatus.getStudent().stuId, prereqs.get(j));
                }

                if (!prereq) {
                    // Student does NOT have prerequisite for 117, so give longer (ELM) Skills Review
                    lessonCourse = RawRecordConstants.M100T;
                }
            }

            if (lessonCourse == null) {
                PageLesson.doGet(cache, site, req, resp, session, logic);
            } else {
                final HtmlBuilder htm = new HtmlBuilder(2000);
                Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null,
                        Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

                htm.sDiv("menupanelu");
                CourseMenu.buildMenu(cache, site, session, logic, htm);
                htm.sDiv("panelu");

                final HtmlBuilder title = new HtmlBuilder(100);

                if ("Y".equals(courseStatus.getCourseSection().courseLabelShown)) {
                    title.add(courseStatus.getCourse().courseLabel, ": ");
                }

                PageOutline.doOutline(cache, siteType, site, session, logic, lessonCourse, mode,
                        null, null, htm, course);

                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                        htm.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
