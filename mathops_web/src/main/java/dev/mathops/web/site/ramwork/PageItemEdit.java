package dev.mathops.web.site.ramwork;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates a page that presents a selected item (in randomized form) to the user.
 */
enum PageItemEdit {
    ;

    /**
     * Generates the page.
     *
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp,
                         final ImmutableSessionInfo session) throws IOException {

        final String itemId = req.getParameter("id");
        if (AbstractSite.isParamInvalid(itemId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  itemId='", itemId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryMaxPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null,
                    false, true);

            htm.add("<h1 style='display:inline-block;float:left;'>").add(itemId).eH(1);
            htm.sDiv(null, "style='display:inline-block;float:right;'")
                    .add("<a class='btn' href='item.html?id=", itemId,
                            "'>Close Editor</a>")
                    .eDiv();
            htm.div("clear");

            final InstructionalCache ic = InstructionalCache.getInstance();

            ic.forgetProblem(itemId);
            final AbstractProblemTemplate p = InstructionalCache.getProblem(itemId);

            if (p == null) {
                htm.sP().add("Unable to load problem").eP();
            } else {
                // Left pane with editor

                htm.addln("<div style='display:inline-block; vertical-align:top; width:50%;",
                        "padding:0; border-right: 2px solid #1e4d2b; min-height:400px;",
                        "height:calc(100vh - 100px);'>");

                final File src = InstructionalCache.getProblemSource(itemId);

                if (src == null) {
                    htm.sP().add("Unable to load item definition file!").eP();
                } else {
                    final String xml = FileLoader.loadFileAsString(src, true);
                    if (xml == null) {
                        htm.sP().add("Unable to loread item definition data!").eP();
                    } else {
                        htm.addln("<form action='item-edit.html' method='POST'/>");

                        htm.addln("<input type='hidden' name='id' value='", itemId,
                                "'/>");

                        htm.addln("<textarea id='newsrc' name='newsrc' ",
                                "style='width:100%;height:calc(100vh - 130px);background:GhostWhite;'>",
                                xml, "</textarea>");

                        htm.addln("<div style='width:100%;height:24px;text-align:right;",
                                "padding:3px 10px 0 0;'>");
                        htm.addln("<input type='submit' value='Update'>");
                        htm.addln("</div>");

                        htm.addln("</form>");
                    }
                }

                htm.eDiv();

                // Right pane with problem display

                htm.addln(
                        "<div style='display:inline-block; vertical-align:top; width:calc(50% - 2px);",
                        "padding:10px; min-height:400px; height:calc(100vh - 100px);'>");

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

                htm.eDiv();
            }

            Page.endOrdinaryMaxPage(htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Processes a POST request, which should perform an update to the item.
     * <p>
     * For the moment, it simply repaints the form and logs a message.
     *
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    static void processPost(final ServletRequest req, final HttpServletResponse resp,
                            final ImmutableSessionInfo session) throws IOException {

        Log.info("Processing edit post.");

        final String itemId = req.getParameter("id");
        final String newsrc = req.getParameter("newsrc");

        if (AbstractSite.isParamInvalid(itemId) || newsrc == null || newsrc.isEmpty()) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  itemId='", itemId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final File src = InstructionalCache.getProblemSource(itemId);

            if (src != null) {
                final String xml = FileLoader.loadFileAsString(src, true);
                if (xml != null) {

                    final String oldFixed = xml.replace("\n", CoreConstants.CRLF)
                            .replace("\r\r\n", CoreConstants.CRLF);

                    final String newFixed = newsrc.replace("\n", CoreConstants.CRLF)
                            .replace("\r\r\n", CoreConstants.CRLF);

                    Log.info(src.getAbsolutePath() + " : "
                            + oldFixed.length() + " -> " + newFixed.length());

                    // final int delta = Math.abs(newFixed.length() - oldFixed.length());

                    final int min = Math.min(oldFixed.length(), newFixed.length());
                    int firstDiff = -1;
                    for (int i = 0; i < min; ++i) {
                        if (oldFixed.charAt(i) != newFixed.charAt(i)) {
                            firstDiff = i;
                            break;
                        }
                    }
                    if (firstDiff != -1) {
                        final int start = Math.max(0, firstDiff - 20);
                        final int end = Math.min(min, firstDiff + 20);
                        Log.info("DIFF: ", oldFixed.substring(start, end), " -> ", newFixed.substring(start, end));
                    }
                }
            }

            showPage(req, resp, session);
        }
    }
}
