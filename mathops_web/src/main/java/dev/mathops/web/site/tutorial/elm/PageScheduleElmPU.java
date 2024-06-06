package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PageScheduleElmPU {
    ;

    /**
     * Generates the home page.
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

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(cache, session, status, htm);
        htm.sDiv("panel");

        doPage(htm);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates the home page HTML.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doPage(final HtmlBuilder htm) {

        htm.sH(2).add("ELM Exam Administered by ProctorU").eH(2);

        htm.sDiv("indent11");

        htm.sP("indent1");
        htm.addln(" Please review the following information before scheduling your");
        htm.addln(" <span class='green'>Entry Level Mathematics (ELM) Exam</span>:");
        htm.eP();

        htm.addln("<ul class='boxlist'>");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  <strong>You should schedule your exam with ProctorU several days in");
        htm.addln("  advance.</strong>  ProctorU may charge additional fees to administer an");
        htm.addln("  exam on short notice.  See the ProctorU web site for more information.");
        htm.addln(" </li>");
        htm.div("vgap");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  <strong>To schedule an exam through ProctorU, please visit");
        htm.addln("  <a target='_blank' href='https://go.proctoru.com/'>the ProctorU web");
        htm.addln("  site</a></strong>, register on their site, then schedule a time to take");
        htm.addln("  the exam (remember to allow 60 minutes for the exam).");
        htm.addln(" </li>");
        htm.div("vgap");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  When it is time to take the exam:");
        htm.addln("  <ul>");
        htm.addln("   <li>Return to this web site and click the \"Tell me more...\" link for");
        htm.addln("       taking the <span class='green'>ELM Exam</span> through ProctorU.</li>");
        htm.addln("   <li>Click the button labeled \"I am ready to take my scheduled exam");
        htm.addln("       now...\".</li>");
        htm.addln("   <li>Read the instructions on that page to log in to ProctorU.</li>");
        htm.addln("   <li>Work with the proctor to verify your identity (you will need photo ID");
        htm.addln("       such as a driver's license or CSU ID card)</li>");
        htm.addln("   <li>Use the button at the bottom of the page to start the exam.</li>");
        htm.addln("  </ul>");
        htm.addln("  The proctor will have to enter a password before the exam can begin.");
        htm.addln(" </li>");

        htm.addln("</ul>");

        htm.div("vgap2");

        htm.sDiv("center");

        htm.addln(" <a class='btn' target='_blank' href='https://go.proctoru.com/'>Visit the ProctorU web site</a>");

        htm.div("vgap2");

        htm.addln(" <a class='ulink' href='tutorial.html'>Return to the tutorial outline</a>");

        htm.eDiv(); // center

        htm.eDiv(); // indent11
        htm.div("vgap2");
    }
}
