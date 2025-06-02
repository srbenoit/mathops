package dev.mathops.web.host.placement.placement;

import dev.mathops.session.ImmutableSessionInfo;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generates the content with fee information.
 */
enum PageToolFees {
    ;

    /**
     * Generates the page that describes the placement fees.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final MathPlacementSite site, final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session) throws IOException {

        final HtmlBuilder htm = MPPage.startPage3(site, session);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.sP();
        htm.addln("A one-time fee of $15 will be charged to your student account the first time ",
                "you use the Math Placement Tool.");
        htm.eP();

        htm.sP();
        htm.addln("This fee also covers any future attempts on the Math Placement Tool, or use ",
                "of the ELM Tutorial or Precalculus Tutorials.");
        htm.eP();

        htm.addln("<ul>");

        htm.addln("<li>",
                "Payment of this fee is expected even if you choose not to attend Colorado State University.",
                "</li>");
        htm.div("vgap");

        htm.addln("<li>",
                "Payment is required by the due date indicated on the billing statement mailed by ",
                "the University. Charges and other penalties as specified on the billing statement ",
                "may be assessed for late payment.",
                "</li>");
        htm.div("vgap0");

        htm.addln("</ul>");

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
