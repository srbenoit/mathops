package dev.mathops.web.site.tutorial.elm;

import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
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
enum PageStartElmPU {
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

        htm.sP().add("Please review the following information before starting your <b>",
                "Entry Level Mathematics (ELM) Exam</b>:").eP();

        htm.addln("<ul class='boxlist'>");

        htm.addln("<li class='boxlist'>");
        htm.addln("To take the exam:");
        htm.addln("<ul>");
        htm.addln(" <li>Sign in with ProctorU using the link below (opens a new browser ",
                "tab).</li>");
        htm.addln(" <li>Work with your proctor to verify your identity (you will need photo ID ",
                "such as a driver's license or CSU ID card)</li>");
        htm.addln(" <li>Return to this web page.</li>");
        htm.addln(" <li>Use the button at the bottom of the page to start the exam.</li>");
        htm.addln("</ul>");
        htm.addln("The proctor will have to enter a password before the exam can begin.");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("<li class='boxlist'>");
        htm.addln("You may use a personal graphing calculator (eg. TI-83/84) on the <b>ELM ",
                "Exam</b>, but are <b>not</b> permitted to use any reference materials or receive ",
                "assistance from others during the exam.");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("</ul>");

        htm.sDiv("center");

        htm.addln(" <a class='btn' target='_blank' href='https://go.proctoru.com/'>",
                "Sign in with ProctorU</a>");

        htm.div("vgap2");

        htm.addln(" <form style='display:inline;' method='get' ",
                "action='proctor_login_elm.html'>");
        htm.addln("  <input type='submit' class='btn' ",
                "value='I am signed on with ProctorU. Begin the exam...'/>");
        htm.addln(" </form>");

        htm.div("vgap2");

        htm.addln("<a href='tutorial.html'>Return to the tutorial outline</a>");

        htm.eDiv(); // center

        htm.div("vgap");

        htm.eDiv(); // indent11
    }
}
