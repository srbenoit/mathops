package dev.mathops.web.site.placement.main;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawCuobjectiveLogic;
import dev.mathops.db.rawlogic.RawLessonComponentLogic;
import dev.mathops.db.rawrecord.RawCuobjective;
import dev.mathops.db.rawrecord.RawLessonComponent;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.reclogic.AssignmentLogic;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Generates the content of the web page that displays a lesson within the placement review.
 */
enum PageReviewLesson {
    ;

    /**
     * Starts the page that shows a lesson with student progress.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

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

                final HtmlBuilder htm = MPPage.startReviewPage2(site, session);

                htm.sDiv("inset2");
                htm.sDiv("shaded2left");

                doLessonPage(cache, unit, seqNum, htm);

                htm.eDiv(); // shaded2left
                htm.eDiv(); // inset2

                MPPage.emitScripts(htm);
                MPPage.endPage(htm, req, resp);
            } catch (final NumberFormatException ex) {
                resp.sendRedirect("home.html");
            }
        }
    }

    /**
     * Creates the HTML of the course lesson.
     *
     * @param cache     the data cache
     * @param unit      the unit
     * @param objective the objective
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doLessonPage(final Cache cache, final int unit, final int objective,
                                     final HtmlBuilder htm) throws SQLException {

        htm.sDiv("nav");
        htm.add("&nbsp; <a class='smallbtn' href='review_outline.html?course=M 100R'>");
        htm.add("Return to the Review Materials Outline");
        htm.addln("</a>");
        htm.eDiv();

        htm.div("clear");
        htm.div("vgap0");

        final RawLessonComponent[] components = queryComponents(cache, unit, objective);

        if (components != null) {
            for (final RawLessonComponent component : components) {
                if (component != null) {
                    final String type = component.type;

                    if ("MED".equals(type)) {
                        htm.sDiv("inset3");
                        htm.add(component.xmlData);
                        htm.eDiv();
                    } else if (!"PREMED".equals(type)) {
                        htm.add(component.xmlData);
                    }
                }
            }
        }

        // Button to launch the assignment (with status)
        final AssignmentRec hw = AssignmentLogic.get(cache).queryActive(cache, "M 100R",
                Integer.valueOf(unit), Integer.valueOf(objective), "HW");

        if (hw != null) {
            if (hw.assignmentId == null) {
                Log.warning("Null version for unit " + unit
                        + " obj " + objective);
            } else {
                doAssignment(hw.assignmentId, htm, unit, objective);
            }
        }
    }

    /**
     * Queries the lesson components for an objective.
     *
     * @param cache     the data cache
     * @param unit      the unit number
     * @param objective the objective number
     * @return an array of lesson components
     * @throws SQLException if there is an error accessing the database
     */
    private static RawLessonComponent[] queryComponents(final Cache cache, final int unit,
                                                        final int objective) throws SQLException {

        final RawLessonComponent[] result;

        final RawCuobjective obj = RawCuobjectiveLogic.query(cache, "M 100R", Integer.valueOf(unit),
                Integer.valueOf(objective), TermLogic.get(cache).queryActive(cache).term);

        if (obj == null) {
            result = null;
        } else {
            final List<RawLessonComponent> list = RawLessonComponentLogic.queryByLesson(cache, obj.lessonId);

            int max = 0;
            for (final RawLessonComponent test : list) {
                max = Math.max(max, test.seqNbr.intValue());
            }

            result = new RawLessonComponent[max + 1];

            for (final RawLessonComponent test : list) {
                result[test.seqNbr.intValue()] = test;
            }
        }

        return result;
    }

    /**
     * Generates the HTM form to launch the assignment configured for the lesson.
     *
     * @param version   the homework version
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @param unit      the unit
     * @param objective the objective
     */
    private static void doAssignment(final String version, final HtmlBuilder htm, final int unit,
                                     final int objective) {

        htm.div("vgap");
        htm.addln("<form method='get' action='review_homework.html'>");

        htm.sDiv("indent2");
        htm.addln("<input type='hidden' name='unit' value='", Integer.toString(unit), "'/>");
        htm.addln("<input type='hidden' name='unit' value='", Integer.toString(objective), "'/>");
        htm.addln("<input type='hidden' name='assign' value='", version, "'/>");
        htm.addln("<input class='btn' type='submit' value='Practice Problems'/>");
        htm.eDiv();
        htm.addln("</form>");
    }
}
