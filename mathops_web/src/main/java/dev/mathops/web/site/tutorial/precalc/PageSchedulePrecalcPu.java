package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of a page with instructions to schedule a ProctorU ELM exam.
 */
enum PageSchedulePrecalcPu {
    ;

    /**
     * Generates the page prior to starting a proctored Precalculus tutorial exam through ProctorU.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
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

            buildPage(courseId, htm);

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
     * @param courseId the course ID
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void buildPage(final String courseId, final HtmlBuilder htm) {

        htm.sH(2).add("Taking Precalculus Tutorial Exams using ProctorU").eH(2);

        htm.sDiv("indent11");
        htm.div("vgap");

        htm.sP("indent11");
        htm.addln("Please review the following information before scheduling your Precalculus Tutorial Exam:");
        htm.eP();

        htm.addln("<ul>");

        htm.addln("<li>You must complete all four units of the Precalculus Tutorial before taking the Precalculus",
                " Tutorial Exam.</li>");
        htm.div("vgap");

        htm.addln("<li>You should schedule your exam with ProctorU at least three days in advance.  ProctorU may ",
                "charge additional fees to administer an exam on short notice. See the ProctorU web site for more ",
                "information.</li>");
        htm.div("vgap");

        htm.addln("<li>To schedule an exam through ProctorU, please visit ",
                "<a target='_blank' href='https://go.proctoru.com/'>the ProctorU web site</a>, register on their ",
                "site, then schedule a time to take the exam (remember to allow 75 minutes for the exam).</li>");
        htm.div("vgap");

        htm.addln("<li>When it is time to take the exam:");
        htm.addln("  <ul>");
        htm.addln("   <li>Return to this web site and click the link for taking the Precalculus Tutorial Exam ",
                "through ProctorU.</li>");
        htm.addln("   <li>Click the button labeled \"Take the Precalculus Tutorial Exam using ProctorU\".</li>");
        htm.addln("   <li>Read the instructions on that page to log in to ProctorU.</li>");
        htm.addln("   <li>Work with the proctor to verify your identity (you will need photo ID such as a driver's ",
                "license or CSU ID card)</li>");
        htm.addln("   <li>Use the button at the bottom of the page to start the exam.</li>");
        htm.addln("   <li>The proctor will have to enter a password before the exam can begin.</li>");
        htm.addln("  </ul>");
        htm.addln(" </li>");

        htm.addln("</ul>");

        htm.div("vgap2");

        htm.sDiv("center");
        htm.addln(" <a class='btn' target='_blank' href='https://go.proctoru.com/'>",
                "Open the ProctorU web site</strong></a>");
        htm.eDiv();

        htm.div("vgap2");

        htm.sDiv("center");
        htm.addln("<a class='ulink' href='instructions_precalc_pu.html?course=", courseId,
                "'>Return to prior page</a>");
        htm.eDiv(); // center
    }
}
