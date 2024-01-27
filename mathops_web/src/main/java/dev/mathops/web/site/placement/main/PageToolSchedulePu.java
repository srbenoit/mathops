package dev.mathops.web.site.placement.main;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generates the content of a page with instructions to schedule a ProctorU exam.
 */
enum PageToolSchedulePu {
    ;

    /**
     * Generates the page with instructions on scheduling the Challenge exam through ProctorU.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException {

        final HtmlBuilder htm = MPPage.startPage3(site, session);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.addln("<ul>");

        htm.addln("<li>",
                "To schedule a session through ProctorU, please visit ",
                "<a target='_blank' href='https://go.proctoru.com/'>the ProctorU web site</a>, ",
                "register on their site, then select a start time (remember the Math Placment Tool ",
                "allows you 2 hours 20 minutes).", //
                "</li>");
        htm.div("vgap");

        htm.addln("<li>",
                "When your schduled time arrives:");
        htm.addln("<ul>");
        htm.addln(" <li>Return to this web site and click the link to complete the Math Placement ",
                "Tool through ProctorU.</li>");
        htm.addln(
                " <li>Click the button labeled \"I want to complete the Math Placement Tool now.\".</li>");
        htm.addln(" <li>Read the instructions on that page to log in to ProctorU.</li>");
        htm.addln(" <li>Work with the proctor to verify your identity (you will need photo ID ",
                "such as a driver's license or CSU ID card)</li>");
        htm.addln(" <li>Use the button at the bottom of the page to begin.</li>");
        htm.addln(" <li>The proctor will enter a password to allow the Tool to start.</li>");
        htm.addln("</ul>");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("</ul>");

        htm.sDiv("center");
        htm.addln(" <a class='btn' target='_blank' href='https://go.proctoru.com/'>Open the ProctorU web page.</a>");
        htm.eDiv(); // center

        htm.eDiv(); // indent11

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
