package dev.mathops.web.host.placement.placement;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the page with instructions for the student who believes that some transfer credit scores are missing.
 */
enum PageMissing {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);

        MPPage.emitMathPlanHeader(htm);

        MathPlacementSite.emitLoggedInAs2(htm, session);
        htm.sDiv("inset2");

        htm.div("vgap");

        htm.sDiv("shaded2");
        htm.sH(2).add(Res.get(Res.MISSING_XFER_HEADING)).eH(2);
        htm.div("vgap0");

        htm.sP("indent");
        htm.addln("This web site shows <strong class='headercolor'>only Mathematics</strong> transfer credit and ",
                "credit for the AP, IB, or CLEP exams. Other transfer courses will not appear.");
        htm.addln("You can check your complete list of transfer credit on ",
                "<a href='https://ramweb.colostate.edu/registrar/Public/Login.aspx' ",
                "target='_blank'><strong>RAMweb</strong></a>.");
        htm.eP();

        htm.sP("indent");
        htm.addln("AP exam information is sent to the University in July, and AP credit will not appear until it ",
                "is received and processed.");
        htm.eP();

        htm.sP("indent");
        htm.add("&bull; &nbsp;");
        htm.addln("If you have sent transcripts to CSU with mathematics courses to transfer, and these courses do ",
                "not appear in <a href='https://ramweb.colostate.edu/registrar/Public/Login.aspx' target='_blank'>",
                "<strong>RAMweb</strong></a>, you can contact the ",
                "<a href='https://registrar.colostate.edu/your-transfer-coursework/' target='_blank'><strong>",
                "Registrar's office</strong><a> to see if those courses are still being evaluated for transfer.");
        htm.eP();

        htm.sP("indent");
        htm.add("&bull; &nbsp;");
        htm.addln("If you have AP/IB/CLEP exam credit that is not listed, or if you are have transfer credit that ",
                "has not been evaluated, or if you are currently taking a course that you expect to transfer to ",
                "CSU, bring supporting documentation (such as an unofficial transcript) to Orientation.");
        htm.eP();

        htm.eDiv(); // shaded2
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
