package dev.mathops.web.site.placement.main;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generates the content of a page with instructions to start a ProctorU placement tool.
 */
enum PageToolStartPu {
    ;

    /**
     * Generates the page prior to starting the proctored placement exam through ProctorU.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final MathPlacementSite site, final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session) throws IOException {

        final HtmlBuilder htm = MPPage.startPage3(site, session);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.addln("<ul>");

        htm.addln("<li>To complete the Math Placement Tool:");
        htm.addln("<ul>");
        htm.addln(" <li>Sign in with ProctorU using the link below (opens a new browser tab)</li>");
        htm.addln(" <li>Work with the proctor to verify your identity (you will need photo ID ",
                "such as a driver's license or CSU ID card)</li>");
        htm.addln(" <li>Return to this web page.</li>");
        htm.addln(" <li>Use the button at the bottom of the page to begin.</li>");
        htm.addln(" <li>The proctor will enter a password to allow the Tool to start.</li>");
        htm.addln("</ul>");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("<li>",
                "You may not use reference materials or receive assistance from others during the ",
                "Math Placement Tool. Misrepresenting your current mathematical knowledge will ",
                "cause you to be placed into a course in which you are unlikely to succeed.", //
                "</li>");
        htm.div("vgap");

        htm.addln("</ul>");

        htm.sDiv("center");

        htm.addln(" <a class='btn' target='_blank' href='https://go.proctoru.com/'>Sign in with ProctorU</a>");

        htm.div("vgap");

        htm.addln("<a class='btn' href='tool_proctor_login.html'>",
                "I am signed in with ProctorU. Open the Math Placement Tool...</a>");

        htm.eDiv(); // center

        htm.div("vgap0");

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
