package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawLessonComponent;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Generates the content of the web page that displays the lessons within one unit of the ELM Tutorial.  This is used as
 * a Skills Review for students who have not placed into MATH 117.
 */
enum PageElmUnit {
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
    public static void doGet(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final PrecalcTutorialSiteLogic logic) throws IOException, SQLException {

        final String unitStr = req.getParameter("unit");

        if (AbstractSite.isParamInvalid(unitStr)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  unit='", unitStr, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (unitStr == null) {
            resp.sendRedirect("home.html");
        } else {
            try {
                final int unit = Long.valueOf(unitStr).intValue();

                final HtmlBuilder htm = new HtmlBuilder(2000);
                Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                        "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

                htm.sDiv("menupanel");
                TutorialMenu.buildMenu(session, logic, htm);
                htm.sDiv("panel");

                buildElmUnitPage(cache, unit, logic, htm);

                htm.eDiv(); // (end "panel" div)
                htm.eDiv(); // (end "menupanel" div)
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
     * @param cache the data cache
     * @param unit  the unit
     * @param logic the course site logic
     * @param htm   the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void buildElmUnitPage(final Cache cache, final int unit, final PrecalcTutorialSiteLogic logic,
                                         final HtmlBuilder htm) throws SQLException {

        final RawStudent student = logic.getStudent();
        final PrecalcTutorialCourseStatus tutStatus = new PrecalcTutorialCourseStatus(cache, student,
                RawRecordConstants.M1170);

        final String associatedCourse = PrecalcTutorialSiteLogic.getAssociatedCourse(tutStatus.getCourse());
        htm.add("<h2 class='title' style='margin-bottom:3px;'>");
        htm.add("Precalculus Tutorial to place out of ", associatedCourse);
        htm.eH(2);

        htm.sDiv("nav");
        htm.sDiv("aslines");
        htm.add("  <a class='linkbtn' href='course.html?course=M%201170'><em>");
        htm.add("Return to the Tutorial Outline");
        htm.addln("</em></a>");
        htm.eDiv(); // aslines
        htm.eDiv(); // nav
        htm.hr();
        htm.div("vgap");

        final SystemData systemData = cache.getSystemData();

        final List<RawCuobjective> objectives = systemData.getCourseUnitObjectives(RawRecordConstants.M100T,
                Integer.valueOf(unit), logic.getActiveTerm().term);

        for (final RawCuobjective objective : objectives) {
            if (objective.lessonId != null) {
                final List<RawLessonComponent> components = systemData.getLessonComponentsByLesson(objective.lessonId);

                // Show any "LH"
                final Iterator<RawLessonComponent> iter1 = components.iterator();
                while (iter1.hasNext()) {
                    final RawLessonComponent comp = iter1.next();
                    if ("LH".equals(comp.type)) {
                        htm.sDiv().add(comp.xmlData).eDiv();
                        iter1.remove();
                    }
                }

                // Show any "PREMED" media components next
                final Iterator<RawLessonComponent> iter2 = components.iterator();
                while (iter2.hasNext()) {
                    final RawLessonComponent comp = iter2.next();
                    if ("PREMED".equals(comp.type)) {
                        htm.sDiv("premed").add(comp.xmlData).eDiv();
                        iter2.remove();
                    }
                }

                // Then show all the remaining components, with mode specified
                for (final RawLessonComponent comp : components) {
                    htm.sDiv().add(comp.xmlData.replace("%%MODE%%", "course")).eDiv();
                }

                // Button to launch the assignment (with status)
                final AssignmentRec hw = systemData.getActiveAssignment(RawRecordConstants.M100T, Integer.valueOf(unit),
                        objective.objective, "HW");
                if (hw != null) {
                    final String assign = hw.assignmentId;
                    if (assign != null) {
                        doAssignment(unit, objective.objective.intValue(), assign, htm);
                    }
                }
            }
        }
    }

    /**
     * Generates the HTM form to launch the assignment configured for the lesson.
     *
     * @param unit      the unit
     * @param objective the objective of the lesson to display
     * @param assignId  the assignment ID
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doAssignment(final int unit, final int objective, final String assignId,
                                     final HtmlBuilder htm) {

        htm.div("gap2");
        htm.addln("<form method='get' action='run_homework.html'>");
        htm.addln("  <input type='hidden' name='course' value='", RawRecordConstants.M100T, "'/>");
        htm.addln("  <input type='hidden' name='assign' value='", assignId, "'/>");
        htm.addln("  <input type='hidden' name='unit' value='", Integer.toString(unit), "'/>");
        htm.addln("  <input type='hidden' name='lesson' value='", Integer.toString(objective), "'/>");
        htm.sDiv("indent11");
        htm.addln("  <input class='btn' type='submit' value='Objective ", Integer.toString(unit),
                CoreConstants.DOT, Integer.toString(objective), " Practice Problems'/>");
        htm.eDiv();
        htm.addln("</form>");
    }
}
