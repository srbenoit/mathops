package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of a page with instructions for the Precalc exam in the testing center.
 */
enum PageInstructionsPrecalcTc {
    ;

    /**
     * Generates the page of information on a proctored precalculus tutorial exam attempt in the testing center.
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
    static void doGet(final Cache cache, final PrecalcTutorialSite site,
                      final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session,
                      final PrecalcTutorialSiteLogic logic) throws IOException, SQLException {

        final String courseId = req.getParameter("course");

        if (AbstractSite.isParamInvalid(courseId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", courseId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (courseId == null) {
            resp.sendRedirect("home.html");
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                    "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(session, logic, htm);
            htm.sDiv("panel");

            buildPage(cache, courseId, logic, htm);

            htm.eDiv(); // (end "panel" div)
            htm.eDiv(); // (end "menupanel" div)
            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Creates the HTML of the course lesson.
     *
     * @param cache    the data cache
     * @param courseId the course ID
     * @param logic    the course site logic
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void buildPage(final Cache cache, final String courseId, final PrecalcTutorialSiteLogic logic,
                                  final HtmlBuilder htm) throws SQLException {

        final RawStudent stu = RawStudentLogic.query(cache, logic.getStudentId(), false);

        htm.sH(2).add("Taking Precalculus Tutorial Exams in the Precalculus Center").eH(2);

        htm.sDiv("indent11");
        htm.div("vgap");

        htm.sP();
        htm.addln("Precalculus Tutorial Exams may be taken in the Precalculus Center (Weber 138) ",
                "after completing all four units of the corresponding Tutorial.");
        htm.eP();

        htm.div("vgap2");

        htm.sDiv("center");
        htm.sTable("plan-table", "style='display:inline-table;'");
        htm.sTr();
        htm.sTh().add("Exam Format:").eTh();
        htm.sTd().add("20 items (to pass, 14 items must be answered correctly)").eTd();
        htm.eTr();

        htm.sTr();
        htm.sTh().add("Time limit:").eTh();
        if (stu == null || stu.timelimitFactor == null) {
            htm.sTd().add("75 minutes").eTd();
        } else {
            final double factor = stu.timelimitFactor.doubleValue();

            if (factor > 1.49 && factor < 1.51) {
                htm.sTd().add("75 minutes<br/>(Adjusted to 1 hours, 53 minutes by accommodation)").eTd();
            } else if (factor > 1.99 && factor < 2.01) {
                htm.sTd().add("75 minutes<br/>(Adjusted to 2 hours, 30 minutes by accommodation)").eTd();
            } else if (factor > 2.49 && factor < 2.51) {
                htm.sTd().add("75 minutes<br/>(Adjusted to 3 hours, 8 minutes by accommodation)").eTd();
            } else if (factor > 2.99 && factor < 3.01) {
                htm.sTd().add("75 minutes<br/>(Adjusted to 5 hours by accommodation)").eTd();
            } else {
                final long minutes = Math.round(75.0 * factor);
                if (minutes <= 140L) {
                    htm.sTd().add("75 minutes").eTd();
                } else {
                    htm.sTd().add("75 minutes<br/>", "(Adjusted to ", Long.toString(minutes),
                            " minutes by accommodation)").eTd();
                }
            }
        }
        htm.eTr();

        htm.sTr();
        htm.sTh().add("Calculator:").eTh();
        htm.sTd().add("Must use provided on-screen TI-84 calculator").eTd();
        htm.eTr();
        htm.sTr();
        htm.sTh().add("References:").eTh();
        htm.sTd().add("Not allowed").eTd();
        htm.eTr();
        htm.eTable();
        htm.eDiv(); // center

        htm.div("vgap");
        AbstractPageSite.hours(cache, htm, true, true);
        htm.div("vgap2");

        htm.sDiv("center");
        htm.add("  <a class='ulink' href='course.html?course=", courseId, "'>Return to the Tutorial Outline</a>");
        htm.eDiv(); // center
    }
}
