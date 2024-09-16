package dev.mathops.web.site.ramwork;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.AbstractDocPrimitiveContainer;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocParagraph;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableReal;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.ui.HtmlImage;
import dev.mathops.db.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Generates a page with utilities and tutorials for problem authoring.
 */
enum PageGraphEditor {
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

        final HtmlBuilder htm = startPage(null);

        endPage(htm, cache, site, req, resp);
    }

    /**
     * Generates the page.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processPost(final Cache cache, final RamWorkSite site, final ServletRequest req,
                            final HttpServletResponse resp) throws IOException, SQLException {

        final String xml = req.getParameter("editbox");

        final HtmlBuilder htm = startPage(xml);

        if (xml != null) {
            // XML will be "drawing" or "graphxy" element - wrap it in enough that it can be parsed as a DocColumn

            final String wrapped = SimpleBuilder.concat("<doc><p>", xml, "</p></doc>");

            try {
                final XmlContent content = new XmlContent(wrapped, false, false);
                final IElement top = content.getToplevel();

                if (top instanceof final NonemptyElement nonempty) {
                    final EvalContext evalContext = new EvalContext();

                    final DocColumn column = DocFactory.parseDocColumn(evalContext, nonempty,
                            EParserMode.ALLOW_DEPRECATED);

                    if (column == null) {
                        htm.sP("red").add("Unable to parse document.").eP();
                    } else {
                        final List<AbstractDocObjectTemplate> children = column.getChildren();

                        if (children.isEmpty()) {
                            htm.sP("red").add("Document has no paragraphs.").eP();
                        } else {
                            final AbstractDocObjectTemplate first = children.getFirst();

                            if (first instanceof final DocParagraph paragraph) {
                                drawParagraph(paragraph, htm);
                            } else {
                                htm.sP("red").add("Document's first child element is not a paragraph.").eP();
                            }
                        }
                    }
                } else {
                    htm.sP("red").add("Unable to extract top-level element.").eP();
                }
            } catch (final ParsingException ex) {
                final String msg = ex.getMessage();
                htm.sP("red").add(msg).eP();
            }
        }

        endPage(htm, cache, site, req, resp);
    }

    /**
     * Draws a paragraph.
     *
     * @param paragraph the paragraph
     * @param htm       the {@code HtmlBuilder} to which to add the rendered image
     */
    static void drawParagraph(final DocParagraph paragraph, final HtmlBuilder htm) {

        final List<AbstractDocObjectTemplate> children = paragraph.getChildren();

        if (children.isEmpty()) {
            htm.sP("red").add("Paragraph has ho child elements.").eP();
        } else {
            final AbstractDocObjectTemplate first = children.getFirst();

            if (first instanceof final AbstractDocPrimitiveContainer graphic) {
                final EvalContext context = new EvalContext();

                final VariableReal xVar = new VariableReal("x");
                context.addVariable(xVar);

                graphic.buildOffscreen(true, context);
                final BufferedImage image = graphic.getOffscreen();

                final HtmlImage htmlImage = new HtmlImage(image, 0, 16, CoreConstants.EMPTY);

                htm.sP(null, "style='padding:10px; background:#eee;'");
                htm.addln(htmlImage.toImg(1.0));
                htm.eP();
            } else {
                htm.sP("red").add("Element is not a graphic (drawing or graph).").eP();
            }
        }
    }

    /**
     * Starts the page.
     *
     * @param xml the contents of the text area
     * @return the {@code HtmlBuilder} with the start of the page HTML
     */
    private static HtmlBuilder startPage(final String xml) {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = Res.get(Res.SITE_TITLE);
        Page.startOrdinaryPage(htm, title, null, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");
        final String heading = Res.get(Res.GRAPHEDIT_HEADING);
        htm.sH(1).add(heading).eH(1);

        htm.sP().add("Type XML into the text area below, then press 'Update' to attempt to generate ",
                "a drawing or graph from that XML.").eP();

        htm.sP();
        htm.addln("<button onClick='insertDrawing();'>&lt;drawing&gt;</button>");
        htm.addln("<button onClick='insertGraphxy();'>&lt;graphxy&gt;</button>");
        htm.eP();

        htm.sP();
        htm.addln("<button onClick='insertLine();'>&lt;line&gt;</button>");
        htm.addln("<button onClick='insertArc();'>&lt;arc&gt;</button>");
        htm.addln("<button onClick='insertOval();'>&lt;oval&gt;</button>");
        htm.addln("<button onClick='insertRectangle();'>&lt;rectangle&gt;</button>");
        htm.addln("<button onClick='insertPolygon();'>&lt;polygon&gt;</button>");
        htm.addln("<button onClick='insertText();'>&lt;text&gt;</button>");
        htm.addln("<button onClick='insertSpan();'>&lt;span&gt;</button>");
        htm.addln("<button onClick='insertRaster();'>&lt;raster&gt;</button>");
        htm.addln("<button onClick='insertProtractor();'>&lt;protractor&gt;</button>");
        htm.addln("<button onClick='insertFunctionPlot();'>&lt;function-plot&gt;</button>");
        htm.eP();

        htm.sP();
        htm.addln("<button onClick='insertNumberLine();'>Number Line Example</button>");;
        htm.eP();

        htm.addln("<form action='graphedit.html' method='POST'>");
        htm.add("  <textarea rows='20' cols='100' id='editbox' name='editbox'>");
        if (xml != null) {
            htm.add(xml);
        }
        htm.add("</textarea>").br();
        htm.addln("  <input type='submit' value='Update'/>");
        htm.addln("</form>");

        return htm;
    }

    /**
     * Ends and sends the page.
     *
     * @param htm   he {@code HtmlBuilder} with the start of the page HTML
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void endPage(final HtmlBuilder htm, final Cache cache, final RamWorkSite site,
                                final ServletRequest req,
                                final HttpServletResponse resp) throws IOException, SQLException {

        htm.eDiv();

        htm.addln("""
                <script>
                function insertDrawing() {
                    var toInsert = "<drawing width='800' height='600'>\\n</drawing>";
                    insertAtCursor(toInsert);
                }
                function insertGraphxy() {
                    var toInsert = "<graphxy width='800' height='600' minx='-8' maxx='8' miny='-6' maxy='6' "
                     + "axiscolor='black' xtickinterval='1' ytickinterval='1' gridcolor='LightSkyBlue3'>\\n</graphxy>";
                    insertAtCursor(toInsert);
                }
                function insertLine() {
                    var toInsert = "<line x='' y='' width='' height='' color='' stroke-width='' dash='' alpha=''/>";
                    insertAtCursor(toInsert);
                }
                function insertArc() {
                    var toInsert = "<arc x='' y='' width='' height='' cx='' cy='' r='' rx='' ry='' start-angle=''"
                      + "arc-angle='' stroke-width='' stroke-color='' stroke-dash='' stroke-alpha='' fill-style=''"
                      + "fill-color='' fill-alpha='' rays-shown='' ray-length='' ray-width='' ray-color='' "
                      + "ray-dash ='' ray-alpha='' label='' label-color='' label-alpha='' label=offset='' "
                      +" fontname='' fontsize='' fontstyle=''/>";
                    insertAtCursor(toInsert);
                }
                function insertOval() {
                    var toInsert = "<oval x='' y='' width='' height='' filled='' color='' stroke-width='' dash='' "
                      + "alpha=''/>";
                    insertAtCursor(toInsert);
                }
                function insertRectangle() {
                    var toInsert = "<rectangle x='' y='' width='' height='' filled='' color='' stroke-width='' dash='' "
                      + "alpha=''/>";
                    insertAtCursor(toInsert);
                }
                function insertPolygon() {
                    var toInsert = "<polygon x-list=',,' y-list=',,' filled='' color='' stroke-width='' dash='' "
                      + "alpha=''/>";
                    insertAtCursor(toInsert);
                }
                function insertText() {
                    var toInsert = "<text x='' y='' anchor='' color='' highlight='' value='' fontname='' fontsize='' "
                      + "fontstyle='' alpha=''/>";
                    insertAtCursor(toInsert);
                }
                function insertSpan() {
                    var toInsert = "<span x='' y='' anchor='' color='' highlight='' value='' fontname='' fontsize='' "
                      + "fontstyle='' alpha=''>\\n<content>\\n</content>\\n</span>";
                    insertAtCursor(toInsert);
                }
                function insertRaster() {
                    var toInsert = "<raster x='' y='' width='' height='' src='' alpha=''/>";
                    insertAtCursor(toInsert);
                }
                function insertProtractor() {
                    var toInsert = "<protractor cx='' cy='' r='' orientation='' units='' quadrants='' color='' "
                      + "text-color='' alpha=''/>";
                    insertAtCursor(toInsert);
                }
                function insertFunctionPlot() {
                    var toInsert = "<function-plot color='' stroke-width='' minx='' maxx='' domain-var=''>\\n"
                      + "<expr>\\n</expr>\\n</function-plot>";
                    insertAtCursor(toInsert);
                }
                function insertNumberLine() {
                    var toInsert = "<graphxy width='800' height='50' axiswidth='0' xaxislabel='' yaxislabel='' "
                      + "minx='-5.9' maxx='5.9' miny='-3' maxy='2' gridwidth='0' ytickinterval='0' "
                      + "xtickinterval='1' tickwidth='2' ticksize='9' ticklabelfontsize='18' borderwidth='0'>\\n"
                      + "\\n"
                      + "  <line gx='-5.8' gy='0' gwidth='11.6' gheight='0' stroke-width='2'/>\\n"
                      + "  <polygon x-list='0,16,16' y-list='20,13,27' filled='TRUE'/>\\n"
                      + "  <polygon x-list='800,784,784' y-list='20,13,27' filled='TRUE'/>\\n"
                      + "\\n"
                      + "  <line gx1='-3' gy1='0' gx2='2' gy2='0' color='blue3' stroke-width='4'/>\\n"
                      + "\\n"
                      + "  <oval gcx='-3' gcy='0' r='8' filled='true' color='blue3'/>\\n"
                      + "  <oval gcx='2' gcy='0' r='8' filled='true' color='blue3'/>\\n"
                      + "  <oval gcx='2' gcy='0' r='4' filled='true' color='white'/>\\n"
                      + "\\n"
                      + "</graphxy>";
                    insertAtCursor(toInsert);
                }
                function insertAtCursor(newText) {
                    var textarea = document.getElementById("editbox");
                    if (document.selection) {
                        textarea.focus();
                        sel = document.selection.createRange();
                        sel.text = newText;
                    } else if (textarea.selectionStart || textarea.selectionStart == "0") {
                        var startPos = textarea.selectionStart;
                        var endPos = textarea.selectionEnd;
                        textarea.value = textarea.value.substring(0, startPos) + newText
                            + textarea.value.substring(endPos, textarea.value.length);
                    } else {
                        textarea.value += newText;
                    }
                }
                </script>""");

        Page.endOrdinaryPage(cache, site, htm, true);
        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }
}
