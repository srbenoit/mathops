package dev.mathops.web.site.placement.main;

import dev.mathops.session.ImmutableSessionInfo;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generates the content of a page with instructions to start a remote exam.
 */
enum PageToolStartRe {
    ;

    /**
     * Generates the page prior to starting the remote, unproctored exam.
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
        htm.addln("<li>",
                "Clicking the button below starts your attempt, which will <strong>count as your one unproctored ",
                "attempt</strong>. Do not click the button below until you are ready to complete the entire ",
                "MathPlacement Tool.",
                "</li>");
        htm.addln("</ul>");
        htm.div("vgap");

        htm.addln("<ul>");
        htm.addln("<li>",
                "Once the tool begins, you may <strong>NOT</strong> leave and come back to your session later.  You ",
                "must complete the entire Math Placement Tool in one session.",
                "</li>");
        htm.addln("</ul>");
        htm.div("vgap");

        htm.addln("<ul>");
        htm.addln("<li>",
                "You may not use reference materials or receive assistance from others during the Math Placement ",
                "Tool. Misrepresenting your current mathematical knowledge will cause you to be placed into a course ",
                "in which you are unlikely to succeed.",
                "</li>");
        htm.addln("</ul>");

        htm.div("vgap");

        htm.sDiv("center");

        htm.addln(" <form style='display:inline;' method='get' action='tool_taking_exam_re.html'>");
        htm.addln("  <input type='submit' class='btn' value='I understand.  Begin using the tool...'/>");
        htm.addln(" </form>");

        htm.eDiv(); // center

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
