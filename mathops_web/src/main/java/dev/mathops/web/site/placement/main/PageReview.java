package dev.mathops.web.site.placement.main;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generates the content of the home page for the math placement review site.
 */
enum PageReview {
    ;

    /**
     * Generates the home page with the menu of courses and general information for the student.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException {

        final HtmlBuilder htm = MPPage.startReviewPage2(site, session);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");
        htm.sP().add("Our goal is to support you to be successful in the placement process. One way to be successful ",
                        "in this process is to study and review before completing the Math Placement Tool.")
                .eP().div("vgap0");
        htm.sP().add("We encourage you to study and do your best on the Math Placement Tool. Also, there may be ",
                        "questions that you do not know that are outside of your current mathematical preparation - ",
                        "that is ok! Remember, the goal is to find the course(s) that best match your mathematical ",
                        "preparation and your academic goals.")
                .eP().div("vgap0");

        htm.div("vgap");
        htm.sP("center");
        htm.add(" <a class='btn' href='review_outline.html'>Review and Practice Problems...", "</a>");
        htm.eP();
        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
