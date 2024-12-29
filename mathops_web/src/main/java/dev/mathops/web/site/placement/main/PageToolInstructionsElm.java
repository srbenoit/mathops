package dev.mathops.web.site.placement.main;

import dev.mathops.session.ImmutableSessionInfo;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generates the content of a page with instructions for the ELM tutorial.
 */
enum PageToolInstructionsElm {
    ;

    /**
     * Generates the page of information about the ELM Tutorial with the student's testing options.
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

        htm.sP();
        htm.addln("You are eligible for the Entry Level Mathematics (ELM) Tutorial.");
        htm.eP();

        htm.sP();
        htm.addln("The ELM Tutorial is an alternate method to achieve some of the possible outcomes of the ",
                "Math Placement process. The ELM Tutorial can be used to satisfy the prerequisite for the entry-level ",
                "mathematics courses:");
        htm.eP();

        htm.addln(" <ul>");
        htm.addln("  <li>MATH 117, College Algebra in Context I</li>");
        htm.addln("  <li>MATH 120, College Algebra</li>");
        htm.addln("  <li>MATH 127, Precalculus</li>");
        htm.addln(" </ul>");

        htm.sP();
        htm.addln("The ELM Tutorial cannot be used to place out of or earn credit for any courses.");
        htm.eP();

        htm.sDiv("center");
        htm.addln(" <form style='display:inline;margin:20pt;' method='get' target='_blank' ",
                "action='/www/media/ELM_information.pdf'>");
        htm.add("  <input type='submit' class='btn' value='Tell me more about the ELM Tutorial'/>");
        htm.addln(" </form>");
        htm.eDiv(); // center

        htm.div("vgap");

        htm.sP();
        htm.addln("After completing the four units of the ELM Tutorial, you may take the ELM Exam. ",
                "Passing this exam satisfies the prerequisite for MATH 117, MATH 120, and MATH 127.");
        htm.eP();

        htm.div("vgap");

        htm.sDiv("center");
        htm.addln(" <form style='display:inline;margin:20pt;' method='get' ",
                "target='_blank' action='/elm-tutorial/home.html'>");
        htm.add("  <input type='submit' class='btn' value='Go to the ELM Tutorial web site'/>");
        htm.addln(" </form>");
        htm.eDiv(); // center

        htm.div("vgap0");

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
