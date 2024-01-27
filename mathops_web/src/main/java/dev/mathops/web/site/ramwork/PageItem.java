package dev.mathops.web.site.ramwork;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page that presents a selected item (in randomized form) to the user.
 */
enum PageItem {
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
    static void showPage(final Cache cache, final RamWorkSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String itemId = req.getParameter("id");
        if (AbstractSite.isParamInvalid(itemId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  itemId='", itemId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null,
                    false, true);

            htm.add("<h1 style='display:inline-block;float:left;'>").add(itemId).eH(1);
            htm.sDiv(null, "style='display:inline-block;float:right;'")
                    .add("<a class='btn' href='item-edit.html?id=", itemId,
                            "'>Edit</a>")
                    .eDiv();
            htm.div("clear");

            InstructionalCache.getInstance().forgetProblem(itemId);
            final AbstractProblemTemplate p = InstructionalCache.getProblem(itemId);

            if (p == null) {
                htm.sP().add("Unable to load problem").eP();
            } else {
                // Generate...
                p.evalContext.generate(itemId);
                p.realize(p.evalContext);

                // Emit the question...
                if (p.questionHtml == null) {
                    ProblemConverter.populateProblemHtml(p, new int[]{1});
                }

                htm.div("vgap");
                htm.add("<div style='height:.75rem;border-bottom:1px dotted #2c723f; ",
                        "margin-bottom:.75rem;text-align:center'>");
                htm.add("<p style='display:inline; background-color:white; padding:0 10px;'>");
                htm.add("Question");
                htm.eP();
                htm.eDiv();

                htm.addln(p.insertAnswers(p.questionHtml));

                // Emit the answer...
                if (p.answerHtml != null) {

                    htm.div("vgap");
                    htm.add("<div style='height:.75rem;border-bottom:1px dotted #2c723f; ",
                            "margin-bottom:.75rem;text-align:center'>");
                    htm.add("<p style='display:inline; background-color:white; padding:0 10px;'>");
                    htm.add("Completed Question with Simple Answer");
                    htm.eP();
                    htm.eDiv();

                    htm.addln(p.insertAnswers(p.answerHtml));
                }

                // Emit the detailed solution...
                if (p.solutionHtml != null) {

                    htm.div("vgap");
                    htm.add("<div style='height:.75rem;border-bottom:1px dotted #2c723f; ",
                            "margin-bottom:.75rem;text-align:center'>");
                    htm.add("<p style='display:inline; background-color:white; padding:0 10px;'>");
                    htm.add("Completed Question with Detailed Explanation");
                    htm.eP();
                    htm.eDiv();

                    htm.addln(p.insertAnswers(p.solutionHtml));
                }
            }

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
