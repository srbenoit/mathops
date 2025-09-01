package dev.mathops.web.host.placement.tutorial.elm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.tutorial.ELMTutorialStatus;
import dev.mathops.db.schema.legacy.RawLessonComponent;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.servlet.CourseLesson;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of the web page that displays a lesson within a course, tailored to a student's position and
 * status in the course.
 */
enum PageLesson {
    ;

    /**
     * Generates the page with contact information.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final ELMTutorialStatus status) throws IOException, SQLException {

        final String unitStr = req.getParameter("unit");
        final String lessonStr = req.getParameter("lesson");

        if (AbstractSite.isParamInvalid(unitStr) || AbstractSite.isParamInvalid(lessonStr)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  unit='", unitStr, "'");
            Log.warning("  lesson='", lessonStr, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (unitStr == null || lessonStr == null) {
            resp.sendRedirect("home.html");
        } else {
            try {
                final int unit = Long.valueOf(unitStr).intValue();
                final int seqNum = Long.valueOf(lessonStr).intValue();

                final HtmlBuilder htm = new HtmlBuilder(1000);
                Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                        "Entry Level Mathematics Tutorial", //
                        "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

                htm.sDiv("menupanel");
                TutorialMenu.buildMenu(cache, session, status, htm);
                htm.sDiv("panel");

                buildLessonPage(cache, site, session, unit, seqNum, htm);

                htm.eDiv(); // panel
                htm.eDiv(); // menupanel
                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
            } catch (final NumberFormatException ex) {
                resp.sendRedirect("home.html");
            }
        }
    }

    /**
     * Creates the HTML of the course lesson.
     *
     * @param cache     the data cache
     * @param site      the course site
     * @param session   the user's login session information
     * @param unit      the unit
     * @param objective the objective
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void buildLessonPage(final Cache cache, final ElmTutorialSite site,
                                        final ImmutableSessionInfo session, final int unit, final int objective,
                                        final HtmlBuilder htm) throws SQLException {

        final CourseLesson less = new CourseLesson(site.site.profile);
        final String studentId = session.getEffectiveUserId();

        if (less.gatherData(cache, RawRecordConstants.M100T, Integer.valueOf(unit),
                Integer.valueOf(objective))) {

            final StudentCourseStatus status = new StudentCourseStatus(site.site.profile);
            if (status.gatherData(cache, session, studentId, RawRecordConstants.M100T, false, false)) {

                htm.sH(2, "title").add(" <a name='top'></a>", status.getCourse().courseName).eH(2);

                htm.add("<nav><a class='linkbtn' href='tutorial.html'><em>Return to the Tutorial Outline",
                        "</em></a></nav>");

                htm.div("vgap0").hr().div("vgap0");

                final int count = less.getNumComponents();
                for (int i = 0; i < count; ++i) {
                    final RawLessonComponent comp = less.getLessonComponent(i);
                    final String type = comp.type;

                    // "PREMED" types are media that appear in the course outline
                    if ("PREMED".equals(type)) {
                        continue;
                    }

                    // For examples and media objects, install the appropriate arguments
                    final String xml = comp.xmlData;
                    if ("EX".equals(type) || "MED".equals(type)) {
                        htm.add(xml.replace("%%MODE%%", "course"));
                    } else {
                        htm.add(xml);
                    }
                }

                // Button to launch practice problems
                if (status.hasHomework(unit, objective)) {
                    final AssignmentRec hw = cache.getSystemData().getActiveAssignment(RawRecordConstants.M100T,
                            Integer.valueOf(unit), Integer.valueOf(objective), "HW");

                    if (hw != null && hw.assignmentId != null) {
                        htm.div("vgap");

                        htm.addln("<form method='get' action='run_homework.html'>");
                        htm.sDiv("indent11");
                        htm.addln("  <input type='hidden' name='unit' value='", Integer.toString(unit), "'/>");
                        htm.addln("  <input type='hidden' name='lesson' value='", Integer.toString(objective), "'/>");
                        htm.addln("  <input type='hidden' name='assign' value='", hw.assignmentId, "'/>");
                        htm.addln("  <input class='btn' type='submit' value='Objective ", Integer.toString(unit),
                                CoreConstants.DOT, Integer.toString(objective), " Practice Problems'/>");
                        htm.eDiv();
                        htm.addln("</form>");
                    }
                }
            } else {
                htm.sP().add("FAILED TO GET LESSON STATUS DATA!").br();
                if (status.getErrorText() != null) {
                    htm.addln(status.getErrorText());
                }
                htm.eP();
            }
        } else {
            htm.sP().add("FAILED TO GET LESSON DATA!").br();
            if (less.getErrorText() != null) {
                htm.addln(less.getErrorText());
            }
            htm.eP();
        }
    }
}
