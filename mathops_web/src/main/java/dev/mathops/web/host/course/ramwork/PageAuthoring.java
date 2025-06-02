package dev.mathops.web.host.course.ramwork;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a page with utilities and tutorials for problem authoring.
 */
enum PageAuthoring {
    ;

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final RamWorkSite site, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = Res.get(Res.SITE_TITLE);
        Page.startOrdinaryPage(htm, title, null, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");
        htm.sH(1).add(Res.get(Res.AUTHORING_HEADING)).eH(1);

        htm.sH(3).add("Third-Party Software:").eH(3);

        htm.addln("<ul class='indent0' style='margin-top:0;'>")
                .addln("<li><a href='https://www.oracle.com/java/technologies/' target='_blank'>",
                        "Java SE</a> (version 21 or greater is needed)</li>")
                .addln("<li><a href='https://notepad-plus-plus.org/' target='_blank'>",
                        "Notepad++</a> (a free editor that works well with XML)</li>")
                .addln("</ul>");

        htm.sH(3).add("Authoring Software:").eH(3);
        htm.sP("indent0").add("Download these to a directory on your local system, then edit 'paths.properties' with ",
                "the path of your item bank (the directory that contains the 'math' directory)").eP();

        htm.addln("<ul class='indent0' style='margin-top:0;'>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/ADMIN.jar'>",
                        "ADMIN.jar</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/flatlaf-3.4.jar'>",
                        "flatlaf-3.4.jar</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/mathops_commons.jar'>",
                        "mathops_commons.jar</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/mathops_persistence.jar'>",
                        "mathops_persistence.jar</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/jwabbit.jar'>",
                        "jwabbit.jar</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/paths.properties'>",
                        "paths.properties</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/TI-84PCSE.rom'>",
                        "TI-84PCSE.rom</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/ProblemAuthor.bat'>",
                        "ProblemAuthor.bat</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/QualityControl.bat'>",
                        "QualityControl.bat</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/Software/ExamTester.bat'>",
                        "ExamTester.bat</a></li>")
                .addln("<li><a href='graphedit.html'>Graph Editor</a></li>")
                .addln("</ul>");

        htm.sH(3).add("Documentation:").eH(3);

        htm.addln("<ul class='indent0' style='margin-top:0;'>")
                .addln("<li><a href='/www/media/ItemAuthoring/Problem Authoring Reference.pdf'>",
                        "Problem Authoring Reference</a> (Updated July 18, 2024)</li>")
                .addln("</ul>");

        htm.sH(3).add("Tutorials:").eH(3);

        htm.addln("<ul class='indent0' style='margin-top:0;'>")
                .addln("<li><a href='/www/media/ItemAuthoring/01-Getting Started.mp4'>",
                        "01: Getting Started</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/01-Getting Started.pdf'>(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/02-Problem Types.mp4'>",
                        "02: Problem Types</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/02-Problem Types.pdf'>(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/03-Multiple-Choice and Multiple Selection Problems.mp4'>",
                        "03: Multiple-Choice and Multiple Selection Problems</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/3-Multiple-Choice and Multiple Selection Problems.pdf'>",
                        "(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/04-Numeric Problems.mp4'>",
                        "04: Numeric Problems</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/04-Numeric Problems.pdf'>(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/05-Embedded Input Problems.mp4'>",
                        "05: Embedded Input Problems</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/05-Embedded Input Problems.pdf'>(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/06-Variables.mp4'>",
                        "06: Variables</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/06-Variables.pdf'>(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/07-Expressions.mp4'>",
                        "07: Expressions</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/07-Expressions.pdf'>(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/08-Document Content Part 1.mp4'>",
                        "08: Document Content, Part 1: Overall Structure and Block-Level Elements</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/08-Document Content Part 1.pdf'>(slides)</a></li>")
                .addln("<li><a href='/www/media/ItemAuthoring/09-Document Content Part 2.mp4'>",
                        "09: Document Content, Part 2: Flow-Level Elements</a> &nbsp; ",
                        "<a href='/www/media/ItemAuthoring/09-Document Content Part 2.pdf'>(slides)</a></li>")
                .addln("<li>... more to come soon ...</li>")
                .addln("</ul>");

        // TODO: Current library (read-only)

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
