package dev.mathops.web.site.tutorial.elm;

import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractPageSite;
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
enum PageInstructionsElmTC {
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

        doPage(cache, htm);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates the home page HTML.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doPage(final Cache cache, final HtmlBuilder htm) throws SQLException {

        htm.sH(2).add("ELM Exam Administered in the Precalculus Center").eH(2);

        htm.sDiv("indent22");

        htm.sP();
        htm.addln("To complete the <b>ELM Tutorial</b>, a proctored <b>ELM Exam</b> must be ",
                "passed.  This exam can be taken in the Precalculus Center, Weber 138.");
        htm.eP();

        AbstractPageSite.hours(cache, htm, true, true);
        htm.div("vgap2");

        htm.addln("<ul class='boxlist'>");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  The time limit for the exam is 60 minutes. The exam will automatically");
        htm.addln("  be submitted for grading when the time limit expires.");
        htm.addln(" </li>");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  An on-screen TI-84 calculator will be provided on the exam, but you");
        htm.addln("  are <strong>not</strong> permitted <strong>any</strong> reference");
        htm.addln("  materials or outside assistance.");
        htm.addln(" </li>");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  The exam consists of 20 questions. There is no penalty for guessing. To");
        htm.addln("  pass the <span class='green'>ELM Exam</span>, you must answer at least");
        htm.addln("  14 questions correctly.");
        htm.addln(" </li>");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  Report any problems via email to");
        htm.addln("  <a class='ulink2' href='mailto:precalc_math@colostate.edu'>",
                "precalc_math@colostate.edu</a>.");
        htm.addln(" </li>");

        htm.addln("</ul>");

        htm.div("vgap2");

        htm.sDiv("center");
        htm.addln(" <a class='btn' href='tutorial.html'>Return to the tutorial outline</a>").br();
        htm.eDiv();

        htm.div("vgap");
    }
}
