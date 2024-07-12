package dev.mathops.assessment.htmlgen;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.EFieldStyle;
import dev.mathops.assessment.document.template.AbstractDocContainer;
import dev.mathops.assessment.document.template.AbstractDocInput;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.AbstractDocSpanBase;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocDrawing;
import dev.mathops.assessment.document.template.DocFence;
import dev.mathops.assessment.document.template.DocFraction;
import dev.mathops.assessment.document.template.DocGraphXY;
import dev.mathops.assessment.document.template.DocHSpace;
import dev.mathops.assessment.document.template.DocImage;
import dev.mathops.assessment.document.template.DocInputCheckbox;
import dev.mathops.assessment.document.template.DocInputDoubleField;
import dev.mathops.assessment.document.template.DocInputLongField;
import dev.mathops.assessment.document.template.DocInputRadioButton;
import dev.mathops.assessment.document.template.DocInputStringField;
import dev.mathops.assessment.document.template.DocMathSpan;
import dev.mathops.assessment.document.template.DocNonwrappingSpan;
import dev.mathops.assessment.document.template.DocParagraph;
import dev.mathops.assessment.document.template.DocParameterReference;
import dev.mathops.assessment.document.template.DocRadical;
import dev.mathops.assessment.document.template.DocRelativeOffset;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.document.template.DocSymbolPalette;
import dev.mathops.assessment.document.template.DocTable;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.document.template.DocVSpace;
import dev.mathops.assessment.document.template.DocWhitespace;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.HtmlImage;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Queue;

/**
 * Converts document objects into HTML form.
 */
public enum DocObjectConverter {
    ;

    /**
     * Given a realized {@code DocColumn}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocColumn}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    public static String convertDocColumn(final DocColumn obj, final Deque<Style> styleStack,
                                          final boolean enabled, final int[] id, final EvalContext context) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        appendChildrenHtml(obj, obj, htm, styleStack, enabled, id, context, false);

        return htm.toString();
    }

    /**
     * Given a realized {@code DocDrawing}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocDrawing}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocDrawing(final DocDrawing obj, final Queue<Style> styleStack,
                                            final EvalContext context) {

        String result = CoreConstants.EMPTY;

        obj.buildOffscreen(false, context);

        if (obj.getOffscreen() != null) {
            final Style style = styleStack.peek();
            if (style != null) {
                final String alt = obj.getAltText();
                final String actualAlt = alt == null ? null : obj.generateStringContents(context, alt);

                result = new HtmlImage(obj.getOffscreen(), 0.0, (double) style.getSize(), actualAlt)
                        .toImg((double) obj.getScale());
            }
        }

        return result;
    }

    /**
     * Given a realized {@code DocFence}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocFence}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocFence(final DocColumn column, final DocFence obj,
                                          final Deque<Style> styleStack, final boolean enabled, final int[] id,
                                          final EvalContext context, final boolean inMath) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final String openText;
        String closeText = null;

        htm.add("<div style='display:inline-block; white-space:nowrap; "
                , "border-width:0 .5em; border-style:solid; ",
                "border-image-slice:0 20 0 20; border-image-width:0 .5em 0 .5em; ",
                "vertical-align:middle;padding-bottom:.22em;");

        switch (obj.type) {
            case DocFence.BRACKETS:
                openText = "open bracket";
                closeText = "close bracket";
                htm.add(" border-image-source: url(\"data:image/svg+xml;utf8,",
                        "&lt;svg xmlns=&apos;http://www.w3.org/2000/svg&apos; ",
                        "width=&apos;40&apos; ",
                        "height=&apos;50&apos; font-size=&apos;50&apos;&gt;",
                        "&lt;text x=&apos;2&apos; y=&apos;37&apos;&gt;[&lt;/text&gt;",
                        "&lt;text x=&apos;21&apos; y=&apos;37&apos;&gt;]&lt;/text&gt;",
                        "&lt;/svg&gt;\");");
                break;

            case DocFence.BARS:
                openText = "open vertical bar";
                closeText = "close vertical bar";
                htm.add(" border-image-source: url(\"data:image/svg+xml;utf8,",
                        "&lt;svg xmlns=&apos;http://www.w3.org/2000/svg&apos; ",
                        "width=&apos;40&apos; ",
                        "height=&apos;50&apos; font-size=&apos;50&apos;&gt;",
                        "&lt;text x=&apos;5&apos; y=&apos;37&apos;&gt;|&lt;/text&gt;",
                        "&lt;text x=&apos;24&apos; y=&apos;37&apos;&gt;|&lt;/text&gt;",
                        "&lt;/svg&gt;\");");
                break;

            case DocFence.BRACES:
                openText = "open brace";
                closeText = "close brace";
                htm.add(" border-image-source: url(\"data:image/svg+xml;utf8,",
                        "&lt;svg xmlns=&apos;http://www.w3.org/2000/svg&apos; ",
                        " width=&apos;40&apos; ",
                        "height=&apos;50&apos; font-size=&apos;50&apos;&gt;",
                        "&lt;text x=&apos;-3&apos; y=&apos;37&apos;&gt;{&lt;/text&gt;",
                        "&lt;text x=&apos;19&apos; y=&apos;37&apos;&gt;}&lt;/text&gt;",
                        "&lt;/svg&gt;\");");
                break;

            case DocFence.LBRACE:
                openText = "left brace";
                htm.add(" border-image-source: url(\"data:image/svg+xml;utf8,",
                        "&lt;svg xmlns=&apos;http://www.w3.org/2000/svg&apos; ",
                        "width=&apos;40&apos; ",
                        "height=&apos;50&apos; font-size=&apos;50&apos;&gt;",
                        "&lt;text x=&apos;-3&apos; y=&apos;37&apos;&gt;{&lt;/text&gt;",
                        "&lt;/svg&gt;\");");
                break;

            case DocFence.PARENTHESES:
            default:
                openText = "open parenthesis";
                closeText = "close parenthesis";
                htm.add(" border-image-source: url(\"data:image/svg+xml;utf8,",
                        "&lt;svg xmlns=&apos;http://www.w3.org/2000/svg&apos; ",
                        "width=&apos;40&apos; ",
                        "height=&apos;50&apos; font-size=&apos;50&apos;&gt;", //
                        "&lt;text x=&apos;2&apos; y=&apos;37&apos;&gt;(&lt;/text&gt;",
                        "&lt;text x=&apos;21&apos; y=&apos;37&apos;&gt;)&lt;/text&gt;",
                        "&lt;/svg&gt;\");");
                break;
        }
        htm.add("'>");

        htm.addln("<span class='sr-only'> ", openText, " </span>");

        appendChildrenHtml(column, obj, htm, styleStack, enabled, id, context, inMath);

        if (closeText != null) {
            htm.addln("<span class='sr-only'> ", closeText, " </span>");
        }

        htm.add("</div>");

        return htm.toString();
    }

    /**
     * Given a realized {@code DocFraction}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocFraction}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocFraction(final DocColumn column, final DocFraction obj,
                                             final Deque<Style> styleStack, final boolean enabled, final int[] id,
                                             final EvalContext context, final boolean inMath) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.addln("<table style='font-size:inherit; font-family:inherit; display:inline-table; ",
                "vertical-align:middle;'>");
        htm.sTr().add("<td style='font-size:inherit;font-family:inherit; text-align:center;",
                "border-bottom:1px solid black; padding:.2em; line-height:1em;'>");
        htm.addln("<span class='sr-only'> fraction whose numerator is </span>");
        if (obj.getNumerator() != null) {
            htm.add(convertDocNonwrappingSpan(column, obj.getNumerator(), styleStack, enabled, id, context, inMath));
        }
        htm.eTd().eTr();
        htm.sTr().add("<td style='font-size:inherit;font-family:inherit; text-align:center; padding:0 .2em; ",
                "line-height:1em;'>");
        htm.addln("<span class='sr-only'> and whose denominator is </span>");
        if (obj.getDenominator() != null) {
            htm.add(convertDocNonwrappingSpan(column, obj.getDenominator(), styleStack, enabled, id, context, inMath));
        }
        htm.eTd().eTr().eTable();
        htm.addln("<span class='sr-only'> end of fraction, </span>");

        return htm.toString();
    }

    /**
     * Given a realized {@code DocGraphXY}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocGraphXY}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocGraphXY(final DocGraphXY obj, final Queue<Style> styleStack,
                                            final EvalContext context) {

        String result = CoreConstants.EMPTY;

        obj.buildOffscreen(false, context);

        if (obj.getOffscreen() != null) {
            final Style style = styleStack.peek();
            if (style != null) {
                final String alt = obj.getAltText();
                final String actualAlt = alt == null ? null : obj.generateStringContents(context, alt);

                result = new HtmlImage(obj.getOffscreen(), 0.0, (double) style.getSize(), actualAlt)
                        .toImg((double) obj.getScale());
            }
        }

        return result;
    }

    /**
     * Given a realized {@code DocImage}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocImage}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @return the generated HTML
     */
    private static String convertDocImage(final DocImage obj, final Queue<Style> styleStack) {

        final HtmlBuilder result = new HtmlBuilder(100);

        final String alt = obj.getAltText();

        if (obj.getSource() == null) {
            if (obj.getImage() != null) {
                final Style style = styleStack.peek();
                if (style != null) {
                    result.add(new HtmlImage(obj.getImage(), 0.0, (double) style.getSize(), alt)
                            .toImg((double) obj.getScale()));
                }
            }
        } else {
            result.add("<img src='", obj.getSource(), "'");

            final NumberOrFormula scaledWidth = obj.getScaledWidth();
            if (scaledWidth != null) {
                result.addAttribute("width", scaledWidth.toString(), 0);
            }

            final NumberOrFormula scaledHeight = obj.getScaledHeight();
            if (scaledHeight != null) {
                result.addAttribute("height", scaledHeight.toString(), 0);
            }

            if (alt != null) {
                result.addAttribute("alt", alt, 0);
                result.addAttribute("title", alt, 0);
            }
            result.add("/>");
        }

        return result.toString();
    }

    /**
     * Given a realized {@code DocParameterReference}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocParameterReference}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocParameterRef(final DocColumn column, final DocParameterReference obj,
                                                 final Deque<Style> styleStack, final boolean enabled,
                                                 final int[] id, final EvalContext context, final boolean inMath) {

        final String result;

        final AbstractVariable var = context.getVariable(obj.getVariableName());

        if (var == null) {
            Log.warning(obj.getVariableName() + " is not defined");
            result = "null";
        } else {
            final HtmlBuilder htm = new HtmlBuilder(200);

            final Object value = var.getValue();

            if (value instanceof AbstractDocObjectTemplate) {
                appendChildHtml(column, (AbstractDocObjectTemplate) value, htm, styleStack, enabled, id, context,
                        inMath);
            } else {
                convertHtmlString(htm, obj.getFontName(), var.valueAsString(), inMath);
            }
            result = htm.toString();
        }

        return result;
    }

    /**
     * Given a realized {@code DocParagraph}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocParagraph}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocParagraph(final DocColumn column, final DocParagraph obj,
                                              final Deque<Style> styleStack, final boolean enabled, final int[] id,
                                              final EvalContext context) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        if (obj.getJustification() == DocParagraph.CENTER) {
            htm.addln("<div style='margin:.5em 0; text-align:center;'>");
        } else if (obj.getJustification() == DocParagraph.RIGHT) {
            htm.addln("<div style='margin:.5em 0; text-align:right;'>");
        } else {
            htm.addln("<div style='margin:.5em 0;'>");
        }

        appendChildrenHtml(column, obj, htm, styleStack, enabled, id, context, false);
        htm.eDiv();

        return htm.toString();
    }

    /**
     * Given a realized {@code DocRadical}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocRadical}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocRadical(final DocColumn column, final DocRadical obj,
                                            final Deque<Style> styleStack, final boolean enabled, final int[] id,
                                            final EvalContext context, final boolean inMath) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        // If there is a "root" we need to pre-pend it as a superscript
        AbstractDocObjectTemplate root = obj.getRoot();
        if (root != null && root.isVisible()) {
            if (root instanceof final DocParameterReference p) {
                final AbstractVariable var = context.getVariable(p.getVariableName());
                if (var != null) {
                    final Object value = var.getValue();
                    if (value instanceof AbstractDocObjectTemplate) {

                        if (value instanceof final AbstractDocSpanBase spanValue && spanValue.getChildren().isEmpty()) {
                            root = null;
                        } else {
                            root = (AbstractDocObjectTemplate) value;
                        }
                    }
                }
            }

            if (root != null && root.isVisible()) {
                htm.add("<span class='sr-only'> root </span>");
                htm.add("<sup style='position:relative; left:.7em; top:-.8ex; line-height:1.2em; margin-left:-.4em;'>");
                appendChildHtml(column, root, htm, styleStack, enabled, id, context, inMath);
                htm.add("</sup>");
            }
        }

        htm.add("<div style='display:inline-block; margin-left:2px;border-width:0 0 0 .8em;",
                " border-style:solid; border-image-slice:0 0 0 30; border-image-width:0 0 0 .8em;",
                " border-image-source: url(\"data:image/svg+xml;utf8,",
                "&lt;svg xmlns=&apos;http://www.w3.org/2000/svg&apos; width=&apos;40&apos; ",
                "height=&apos;50&apos; font-size=&apos;47&apos;&gt;",
                "&lt;polygon points=&apos;30,2 27,2 21,40 12,21 5,26 11,24 22,47 29,3 30,3&apos;/&gt;",
                "&lt;/svg&gt;\");'>");
        htm.add("<div style='display:inline-block;border-top:1px solid black;",
                "margin-left:-1px;margin-top:1px;padding-left:3px; padding-right:2px;'>");

        // Both 'base' and 'root' are children - just append the base...
        final AbstractDocObjectTemplate base = obj.getBase();
        if (base != null && base.isVisible()) {
            appendChildHtml(column, base, htm, styleStack, enabled, id, context, inMath);
        }

        htm.eDiv();
        htm.eDiv();

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocRelativeOffset}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocRelativeOffset}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocRelativeOffset(final DocColumn column, final DocRelativeOffset obj,
                                                   final Deque<Style> styleStack, final boolean enabled,
                                                   final int[] id, final EvalContext context, final boolean inMath) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();

        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        final AbstractDocObjectTemplate base = obj.getBase();
        final AbstractDocObjectTemplate sup = obj.getSuperscript();
        final AbstractDocObjectTemplate sub = obj.getSubscript();
        final AbstractDocObjectTemplate over = obj.getOver();
        final AbstractDocObjectTemplate under = obj.getUnder();

        final float baseSize = (float) base.getFontSize();

        htm.add("<span style='white-space:nowrap'>");
        if (over == null) {
            if (under == null) {
                appendChildHtml(column, base, htm, styleStack, enabled, id, context, inMath);
            } else {
                // Just under (like a limit construction)
                htm.add("<div style='display:inline-grid; ",
                        "grid-template-columns: 1fr; ",
                        "grid-template-rows: 1fr 1em; ",
                        "grid-template-areas: \"base\" \"under\"; ",
                        "align-content: end;'>");
                htm.add("<span style='grid-area:under; text-align:center;'>");
                appendChildHtml(column, under, htm, styleStack, enabled, id, context, inMath);
                htm.eSpan().add("<span style='grid-area:base; text-align:center;'>");
                appendChildHtml(column, base, htm, styleStack, enabled, id, context, inMath);
                htm.eSpan();
                htm.eDiv();
            }
        } else {
            if (under == null) {
                // Just over
                htm.add("<div style='display:inline-grid; grid-template-columns: 1fr; ",
                        "grid-template-rows: 1em 1fr; grid-template-areas: \"over\" \"base\"; ",
                        "align-content: end;'>");
                htm.add("<span style='grid-over; text-align:center;'>");
            } else {
                // Under and over
                htm.add("<div style='display:inline-grid; vertical-align: middle; ",
                        "grid-template-columns: 1fr; grid-template-rows: 1em 1fr 1em; ",
                        "grid-template-areas: \"over\" \"base\" \"under\";'>");
                htm.add("<span style='grid-area:under; text-align:center;'>");
                appendChildHtml(column, under, htm, styleStack, enabled, id, context, inMath);
                htm.eSpan().add("<span style='grid-area:over; text-align:center;'>");
            }
            appendChildHtml(column, over, htm, styleStack, enabled, id, context, inMath);
            htm.eSpan().add("<span style='grid-area:base; text-align:center;'>");
            appendChildHtml(column, base, htm, styleStack, enabled, id, context, inMath);
            htm.eSpan();
            htm.eDiv();
        }

        // Sub and sup should be relative to font size of base

        final Style style = styleStack.peek();
        final boolean fixBaseSize = style != null && Math.abs(baseSize - style.getSize()) > 0.01f;
        if (fixBaseSize) {
            styleStack.push(new Style(baseSize, style.getColorName()));
            htm.add("<span style='font-size:" + baseSize + "px;'>");
        }

        if (sup != null) {
            htm.add(inMath ? "<span class='sr-only'> raised to power </span>"
                    : "<span class='sr-only'> superscript </span>");
            htm.add("<sup style='position:relative; top:-.05em;line-height:1.2em;margin-left:.15em;'>");
            appendChildHtml(column, sup, htm, styleStack, enabled, id, context, inMath);
            htm.add("</sup>");
            htm.add(inMath ? "<span class='sr-only'> end of power, </span>"
                    : "<span class='sr-only'> end of superscript, </span>");
        }
        if (sub != null) {
            htm.add("<span class='sr-only'> subscript </span>");
            htm.add("<sub style='position:relative; top:.2em;line-height:1em;margin-left:.12em;'>");
            appendChildHtml(column, sub, htm, styleStack, enabled, id, context, inMath);
            htm.add("</sub>");
            htm.add("<span class='sr-only'> end of subscript, </span>");
        }

        if (fixBaseSize) {
            htm.eSpan();
            styleStack.pop();
        }

        // End fixed base font size

        htm.eSpan();

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocTable}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocTable}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocTable(final DocColumn column, final DocTable obj,
                                          final Deque<Style> styleStack, final boolean enabled, final int[] id,
                                          final EvalContext context, final boolean inMath) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        htm.add("<table style='display:inline-table; font-size:inherit; font-family:inherit;");
        if (obj.boxWidth == 0) {
            htm.add("border-width:0; ");
        } else {
            htm.add("border:", Integer.toString(obj.boxWidth), "px solid black; ");
        }
        if (obj.backgroundColorName != null) {
            htm.add("background-color:", obj.backgroundColorName, ";");
        }
        htm.addln("'>");

        final AbstractDocObjectTemplate[][] data = obj.objectData;
        final Insets cellInsets = obj.cellInsets;
        final boolean uniform = obj.getSpacing() == DocTable.UNIFORM;
        final String textAlign = obj.getJustification() == DocTable.CENTER ? "center"
                : obj.getJustification() == DocTable.RIGHT ? "right" : "left";

        final int datalen = data.length;
        for (int row = 0; row < datalen; ++row) {
            htm.sTr();
            final int rowLen = data[row].length;
            for (int col = 0; col < rowLen; ++col) {

                final AbstractDocObjectTemplate cell = data[row][col];
                final StringBuilder cellBorder = new StringBuilder(50);

                if (cell instanceof DocNonwrappingSpan) {
                    final int outlines = ((DocNonwrappingSpan) cell).outlines;

                    // outlines (1=left, 2=right, 4=top, 8=bottom)

                    if ((outlines & 1) == 1 && obj.vLineWidth > 0) {
                        cellBorder.append("border-left:").append(obj.vLineWidth).append("px solid black;");
                    }
                    if ((outlines & 2) == 2 && obj.vLineWidth > 0) {
                        cellBorder.append("border-right:").append(obj.vLineWidth).append("px solid black;");
                    }
                    if ((outlines & 4) == 4 && obj.hLineWidth > 0) {
                        cellBorder.append("border-top:").append(obj.hLineWidth).append("px solid black;");
                    }
                    if ((outlines & 8) == 8 && obj.hLineWidth > 0) {
                        cellBorder.append("border-bottom:").append(obj.hLineWidth).append("px solid black;");
                    }
                }

                htm.add("<td style='font-size:inherit; font-family:inherit; text-align:", textAlign, ";");
                htm.add(cellBorder.toString());

                if (cellInsets != null) {
                    htm.add(" padding:", Integer.toString(cellInsets.top),
                            "px ", Integer.toString(cellInsets.right),
                            "px ", Integer.toString(cellInsets.bottom),
                            "px ", Integer.toString(cellInsets.left),
                            "px;");
                }
                htm.add('\'');
                if (row == 0 && uniform) {
                    final float pct = 100.0f / (float) rowLen;
                    htm.add(" width='", Float.toString(pct), "%'");
                }
                htm.add('>');

                if (cell != null) {
                    appendChildHtml(column, data[row][col], htm, styleStack, enabled, id, context, inMath);
                }

                htm.eTd();
            }

            htm.eTr();
        }

        htm.eTable();

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocText}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocText}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocText(final DocText obj, final Deque<Style> styleStack, final boolean inMath) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final String txt = obj.getText();

        if (txt != null) {
            final float fontSize = (float) obj.getFontSize();

            final Style current = styleStack.peek();
            final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
            final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

            if (updateSize || updateColor) {
                final Style newStyle = new Style(fontSize, obj.getColorName());
                styleStack.push(newStyle);
                htm.add("<span style='");
                if (updateSize) {
                    htm.add("font-size:" + obj.getFontSize() + "px;");
                }
                if (updateColor) {
                    htm.add("color:", obj.getColorName(), ";");
                }
                htm.add("'>");
            }

            final int style = obj.getFontStyle();
            if (style != 0) {
                htm.add("<span style='");
                if ((style & AbstractDocObjectTemplate.BOLD) == AbstractDocObjectTemplate.BOLD) {
                    htm.add("font-weight:bold;");
                }
                if ((style & AbstractDocObjectTemplate.ITALIC) == AbstractDocObjectTemplate.ITALIC) {
                    htm.add("font-style:italic;");
                }
                if ((style & AbstractDocObjectTemplate.HIDDEN) == AbstractDocObjectTemplate.HIDDEN) {
                    htm.add("display:hidden;");
                }
                if ((style & AbstractDocObjectTemplate.BOXED) == AbstractDocObjectTemplate.BOXED) {
                    htm.add("border:1px solid black;");
                }

                if ((style & AbstractDocObjectTemplate.UNDERLINE) == AbstractDocObjectTemplate.UNDERLINE
                        || (style & AbstractDocObjectTemplate.OVERLINE) == AbstractDocObjectTemplate.OVERLINE
                        || (style & AbstractDocObjectTemplate.STRIKETHROUGH)
                        == AbstractDocObjectTemplate.STRIKETHROUGH) {
                    htm.add("text-decoration:");

                    if ((style & AbstractDocObjectTemplate.UNDERLINE) == AbstractDocObjectTemplate.UNDERLINE) {
                        htm.add(" underline");
                    }
                    if ((style & AbstractDocObjectTemplate.OVERLINE) == AbstractDocObjectTemplate.OVERLINE) {
                        htm.add(" overline");
                    }
                    if ((style & AbstractDocObjectTemplate.STRIKETHROUGH) == AbstractDocObjectTemplate.STRIKETHROUGH) {
                        htm.add(" line-through");
                    }
                    htm.add(';');
                }
                htm.add("'>");
            }

            final String fontName = obj.getFontName();
            convertHtmlString(htm, fontName, txt, inMath);

            if (style != 0) {
                htm.eSpan();
            }

            if (updateSize || updateColor) {
                htm.eSpan();
                styleStack.pop();
            }
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocHSpace}, generates the corresponding HTML.
     *
     * @param obj     the {@code DocHSpace}
     * @param context the evaluation context
     * @return the generated HTML
     */
    private static String convertDocHSpace(final DocHSpace obj, final EvalContext context) {

        final HtmlBuilder htm = new HtmlBuilder(50);

        final double widthDigits = obj.calculateWidth(context);

        htm.add("<span style='display:inline-block;width:" + widthDigits + "ch;'></span>");

        return htm.toString();
    }

    /**
     * Given a realized {@code DocVSpace}, generates the corresponding HTML.
     *
     * @param obj     the {@code DocVSpace}
     * @param context the evaluation context
     * @return the generated HTML
     */
    private static String convertDocVSpace(final DocVSpace obj, final EvalContext context) {

        final HtmlBuilder htm = new HtmlBuilder(50);

        final double heightLines = obj.calculateHeight(context);

        htm.add("<div style='height:" + heightLines + "em;'></div>");

        return htm.toString();
    }

    /**
     * Converts an HTML string to appropriate HTML codes.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param fontName the font name
     * @param txt      the text to convert
     * @param inMath   {@code true} if in math mode
     */
    private static void convertHtmlString(final HtmlBuilder htm, final String fontName,
                                          final String txt, final boolean inMath) {

        final char[] chars = txt.toCharArray();
        final int len = chars.length;

        for (int i = 0; i < len; ++i) {

            final char ch = chars[i];

            if (ch == '<') {
                htm.add("&lt;");
            } else if (ch == '>') {
                htm.add("&gt;");
            } else if (ch == '"') {
                htm.add("&quot;");
            } else if (ch == '\'') {
                if (inMath) {
                    // Look for sequences of primes
                    if (i + 1 < len && chars[i + 1] == '\'') {
                        // At least two
                        if (i + 2 < len && chars[i + 2] == '\'') {
                            // At least three
                            if (i + 3 < len && chars[i + 3] == '\'') {
                                htm.add("<span class='sr-only'> quadruple prime </span>",
                                        "<span class='no-sr'>&qprime;</span>");
                            } else {
                                // Three primes
                                htm.add("<span class='sr-only'> triple prime </span>",
                                        "<span class='no-sr'>&tprime;</span>");
                            }
                        } else {
                            // Two primes
                            htm.add("<span class='sr-only'> double prime </span>",
                                    "<span class='no-sr'>&Prime;</span>");
                        }
                    } else {
                        // Just one prime
                        htm.add("<span class='sr-only'> prime </span>",
                                "<span class='no-sr'>&prime;</span>");
                    }
                } else {
                    htm.add("&apos;");
                }
            } else if (ch == '&') {
                htm.add("&amp;");
            } else if (fontName == null || fontName.contains("Times") || fontName.contains("Arial")) {

                if (ch == '\u00a0') {
                    htm.add("&nbsp;");
                } else if (ch == '\u00b0') {
                    htm.add("<span class='sr-only'> degrees </span><span class='no-sr'>&deg;</span>");
                } else if (ch == '\u00b1') {
                    htm.add("<span class='sr-only'> plus or minus </span><span class='no-sr'>&plusmn;</span>");
                } else if (ch == '\u00b7') {
                    htm.add("<span class='sr-only'> dot </span><span class='no-sr'>&middot;</span>");
                } else if (ch == '\u00d7') {
                    htm.add("<span class='sr-only'> times </span><span class='no-sr'>&times;</span>");
                } else if (ch == '\u00f7') {
                    htm.add("<span class='sr-only'> divided by </span><span class='no-sr'>&div;</span>");
                } else if (ch == '\u0192') {
                    htm.add("<span class='sr-only'> f </span><span class='no-sr'>&fnof;</span>");
                } else if (ch == '\u0393') {
                    htm.add("<span class='sr-only'> Gamma </span><span class='no-sr'>&Gamma;</span>");
                } else if (ch == '\u0394') {
                    htm.add("<span class='sr-only'> Delta </span><span class='no-sr'>&Delta;</span>");
                } else if (ch == '\u0398') {
                    htm.add("<span class='sr-only'> Theta </span><span class='no-sr'>&Theta;</span>");
                } else if (ch == '\u039B') {
                    htm.add("<span class='sr-only'> Lamda </span><span class='no-sr'>&Lamda;</span>");
                } else if (ch == '\u039E') {
                    htm.add("<span class='sr-only'> Xi </span><span class='no-sr'>&Xi;</span>");
                } else if (ch == '\u03A0') {
                    htm.add("<span class='sr-only'> Pi </span><span class='no-sr'>&Pi;</span>");
                } else if (ch == '\u03A3') {
                    htm.add("<span class='sr-only'> Sigma </span><span class='no-sr'>&Sigma;</span>");
                } else if (ch == '\u03A5') {
                    htm.add("<span class='sr-only'> Upsilon </span><span class='no-sr'>&Upsilon;</span>");
                } else if (ch == '\u03A6') {
                    htm.add("<span class='sr-only'> Phi </span><span class='no-sr'>&Phi;</span>");
                } else if (ch == '\u03A8') {
                    htm.add("<span class='sr-only'> Psi </span><span class='no-sr'>&Psi;</span>");
                } else if (ch == '\u03A9') {
                    htm.add("<span class='sr-only'> Omega </span><span class='no-sr'>&Omega;</span>");
                } else if (ch == '\u03B1') {
                    htm.add("<span class='sr-only'> alpha </span><span class='no-sr'>&alpha;</span>");
                } else if (ch == '\u03B2') {
                    htm.add("<span class='sr-only'> beta </span><span class='no-sr'>&beta;</span>");
                } else if (ch == '\u03B3') {
                    htm.add("<span class='sr-only'> gamma </span><span class='no-sr'>&gamma;</span>");
                } else if (ch == '\u03B4') {
                    htm.add("<span class='sr-only'> delta </span><span class='no-sr'>&delta;</span>");
                } else if (ch == '\u03B5') {
                    htm.add("<span class='sr-only'> epsilon </span><span class='no-sr'>&epsilon;</span>");
                } else if (ch == '\u03B6') {
                    htm.add("<span class='sr-only'> zeta </span><span class='no-sr'>&zeta;</span>");
                } else if (ch == '\u03B7') {
                    htm.add("<span class='sr-only'> eta </span><span class='no-sr'>&eta;</span>");
                } else if (ch == '\u03B8') {
                    htm.add("<span class='sr-only'> theta </span><span class='no-sr'>&theta;</span>");
                } else if (ch == '\u03B9') {
                    htm.add("<span class='sr-only'> iota </span><span class='no-sr'>&iota;</span>");
                } else if (ch == '\u03BA') {
                    htm.add("<span class='sr-only'> kappa </span><span class='no-sr'>&kappa;</span>");
                } else if (ch == '\u03BB') {
                    htm.add("<span class='sr-only'> lamda </span><span class='no-sr'>&lamda;</span>");
                } else if (ch == '\u03BC') {
                    htm.add("<span class='sr-only'> mu </span><span class='no-sr'>&mu;</span>");
                } else if (ch == '\u03BD') {
                    htm.add("<span class='sr-only'> nu </span><span class='no-sr'>&nu;</span>");
                } else if (ch == '\u03BE') {
                    htm.add("<span class='sr-only'> xi </span><span class='no-sr'>&xi;</span>");
                } else if (ch == '\u03BF') {
                    htm.add("<span class='sr-only'> omicron </span><span class='no-sr'>&omicron;</span>");
                } else if (ch == '\u03C0') {
                    htm.add("<span class='sr-only'> pi </span><span class='no-sr'>&pi;</span>");
                } else if (ch == '\u03C1') {
                    htm.add("<span class='sr-only'> rho </span><span class='no-sr'>&rho;</span>");
                } else if (ch == '\u03C2') {
                    htm.add("<span class='sr-only'> stigma </span><span class='no-sr'>&stigmaf;</span>");
                } else if (ch == '\u03C3') {
                    htm.add("<span class='sr-only'> sigma </span><span class='no-sr'>&sigma;</span>");
                } else if (ch == '\u03C4') {
                    htm.add("<span class='sr-only'> tau </span><span class='no-sr'>&tau;</span>");
                } else if (ch == '\u03C5') {
                    htm.add("<span class='sr-only'> upsilon </span><span class='no-sr'>&upsilon;</span>");
                } else if (ch == '\u03C6') {
                    htm.add("<span class='sr-only'> phi </span><span class='no-sr'>&phi;</span>");
                } else if (ch == '\u03C7') {
                    htm.add("<span class='sr-only'> chi </span><span class='no-sr'>&chi;</span>");
                } else if (ch == '\u03C8') {
                    htm.add("<span class='sr-only'> psi </span><span class='no-sr'>&psi;</span>");
                } else if (ch == '\u03C9') {
                    htm.add("<span class='sr-only'> omega </span><span class='no-sr'>&omega;</span>");
                } else if (ch == '\u03D1') {
                    htm.add("<span class='sr-only'> theta </span><span class='no-sr'>&thetasym;</span>");
                } else if (ch == '\u03D5') {
                    htm.add("<span class='sr-only'> phi </span><span class='no-sr'>&phi;</span>");
                } else if (ch == '\u03D6') {
                    htm.add("<span class='sr-only'> pi </span><span class='no-sr'>&piv;</span>");
                } else if (ch == '\u03F0') {
                    htm.add("<span class='sr-only'> kappa </span><span class='no-sr'>&varkappa;</span>");
                } else if (ch == '\u03F1') {
                    htm.add("<span class='sr-only'> rho </span><span class='no-sr'>&rho;</span>");
                } else if (ch == '\u03F5') {
                    htm.add("<span class='sr-only'> epsilon </span><span class='no-sr'>&epsilon;</span>");
                } else if (ch == '\u2013') {
                    htm.add("<span class='sr-only'> dash </span><span class='no-sr'>&ndash;</span>");
                } else if (ch == '-') {
                    htm.add(inMath ? "<span class='sr-only'> minus </span><span class='no-sr'>&minus;</span>"
                            : CoreConstants.DASH);
                } else if (ch == '\u2014') {
                    htm.add("<span class='sr-only'> dash </span><span class='no-sr'>&mdash;</span>");
                } else if (ch == '\u2018') {
                    htm.add("<span class='sr-only'> open quote </span><span class='no-sr'>&lsquo;</span>");
                } else if (ch == '\u2019') {
                    htm.add("<span class='sr-only'> close quote </span><span class='no-sr'>&rsquo;</span>");
                } else if (ch == '\u201C') {
                    htm.add("<span class='sr-only'> open quote </span><span class='no-sr'>&ldquo;</span>");
                } else if (ch == '\u201D') {
                    htm.add("<span class='sr-only'> close quote </span><span class='no-sr'>&rdquo;</span>");
                } else if (ch == '\u2022') {
                    htm.add("<span class='sr-only'> bullet </span><span class='no-sr'>&bull;</span>");
                } else if (ch == '\u2032') {
                    htm.add("<span class='sr-only'> prime </span><span class='no-sr'>&prime;</span>");
                } else if (ch == '\u2033') {
                    htm.add("<span class='sr-only'> double prime </span><span class='no-sr'>&Prime;</span>");
                } else if (ch == '\u2034') {
                    htm.add("<span class='sr-only'> triple prime </span><span class='no-sr'>&tprime;</span>");
                } else if (ch == '\u2057') {
                    htm.add("<span class='sr-only'> quadruple prime </span><span class='no-sr'>&qprime;</span>");
                } else if (ch == '\u2147') {
                    htm.add("<i><b>e</b></i>");
                } else if (ch == '\u2148') {
                    htm.add("<i><b>i</b></i>");
                } else if (ch == '\u2190') {
                    htm.add("<span class='sr-only'> left arrow </span><span class='no-sr'>&larr;</span>");
                } else if (ch == '\u2191') {
                    htm.add("<span class='sr-only'> up arrow </span><span class='no-sr'>&uarr;</span>");
                } else if (ch == '\u2192') {
                    htm.add("<span class='sr-only'> right arrow </span><span class='no-sr'>&rarr;</span>");
                } else if (ch == '\u2193') {
                    htm.add("<span class='sr-only'> down arrow </span><span class='no-sr'>&darr;</span>");
                } else if (ch == '\u2194') {
                    htm.add("<span class='sr-only'> left right arrow </span><span class='no-sr'>&harr;</span>");
                } else if (ch == '\u2195') {
                    htm.add("<span class='sr-only'> up down arrow </span><span class='no-sr'>&varr;</span>");
                } else if (ch == '\u2206') {
                    htm.add("<span class='sr-only'> Delta </span><span class='no-sr'>&xutri;</span>");
                } else if (ch == '\u2212') {
                    htm.add("<span class='sr-only'> minus </span><span class='no-sr'>&minus;</span>");
                } else if (ch == '\u2218') {
                    htm.add("<span class='sr-only'> composed with </span><span class='no-sr'>&compfn;</span>");
                } else if (ch == '\u221D') {
                    htm.add("<span class='sr-only'> proportional to </span><span class='no-sr'>&varpropto;</span>");
                    htm.add("&;");
                } else if (ch == '\u221E') {
                    htm.add("<span class='sr-only'> infinity </span><span class='no-sr'>&infin;</span>");
                } else if (ch == '\u2220') {
                    htm.add("<span class='sr-only'> angle </span><span class='no-sr'>&angle;</span>");
                } else if (ch == '\u2221') {
                    htm.add("<span class='sr-only'> measured angle </span><span class='no-sr'>&measuredangle;</span>");
                } else if (ch == '\u222B') {
                    htm.add("<span class='sr-only'> integral of </span>",
                            "<span class='no-sr' style='position:relative; top:.2em;'>&int;</span>");
                } else if (ch == '\u2243') {
                    htm.add("<span class='sr-only'> similar to </span><span class='no-sr'>&sime;</span>");
                } else if (ch == '\u2248') {
                    htm.add("<span class='sr-only'> approximately equal to </span><span class='no-sr'>&asymp;</span>");
                } else if (ch == '\u2260') {
                    htm.add("<span class='sr-only'> not equal to </span><span class='no-sr'>&ne;</span>");
                } else if (ch == '\u2264') {
                    htm.add("<span class='sr-only'> less than or equal to </span><span class='no-sr'>&le;</span>");
                } else if (ch == '\u2265') {
                    htm.add("<span class='sr-only'> greater than or equal to </span><span class='no-sr'>&ge;</span>");
                } else if (ch == '\u2266') {
                    htm.add("<span class='sr-only'> less than or equal to </span><span class='no-sr'>&leqq;</span>");
                } else if (ch == '\u2267') {
                    htm.add("<span class='sr-only'> greater than or equal to </span><span class='no-sr'>&geqq;</span>");
                } else if (ch == '\u2268') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'>&lneqq;</span>");
                } else if (ch == '\u2269') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'>&gneqq;</span>");
                } else if (ch == '\u226A') {
                    htm.add("<span class='sr-only'> much less than or equal to </span><span class='no-sr'>&ll;</span>");
                } else if (ch == '\u226B') {
                    htm.add("<span class='sr-only'> much greater than or equal to </span>",
                            "<span class='no-sr'>&gg;</span>");
                } else if (ch == '\u226C') {
                    htm.add("<span class='sr-only'> between </span><span class='no-sr'>&between;</span>");
                } else if (ch == '\u226E') {
                    htm.add("<span class='sr-only'> not less than </span><span class='no-sr'>&nless;</span>");
                } else if (ch == '\u226F') {
                    htm.add("<span class='sr-only'> not greater than </span><span class='no-sr'>&ngt;</span>");
                } else if (ch == '\u2270') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'>&nleq;</span>");
                } else if (ch == '\u2271') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'>&ngeq;</span>");
                } else if (ch == '\u2272') {
                    htm.add("<span class='sr-only'> less than or similar to </span>",
                            "<span class='no-sr'>&lesssim;</span>");
                } else if (ch == '\u2273') {
                    htm.add("<span class='sr-only'> greater than or similar to </span>",
                            "<span class='no-sr'>&gtrsim;</span>");
                } else if (ch == '\u2276') {
                    htm.add("<span class='sr-only'> less than greater than </span>",
                            "<span class='no-sr'>&lessgtr;</span>");
                } else if (ch == '\u2277') {
                    htm.add("<span class='sr-only'> greater than less than </span>",
                            "<span class='no-sr'>&gtrless;</span>");
                } else if (ch == '\u227A') {
                    htm.add("<span class='sr-only'> precedes </span><span class='no-sr'>&prec;</span>");
                } else if (ch == '\u227B') {
                    htm.add("<span class='sr-only'> succeeds </span><span class='no-sr'>&succ;</span>");
                } else if (ch == '\u227C') {
                    htm.add("&preccurlyeq;");
                } else if (ch == '\u227D') {
                    htm.add("&succcurlyeq;");
                } else if (ch == '\u227E') {
                    htm.add("&precsim;");
                } else if (ch == '\u227F') {
                    htm.add("&succsim;");
                } else if (ch == '\u2280') {
                    htm.add("&nprec;");
                } else if (ch == '\u2281') {
                    htm.add("&nsucc;");
                } else if (ch == '\u22D6') {
                    htm.add("&lessdot;");
                } else if (ch == '\u22D7') {
                    htm.add("&gtrdot;");
                } else if (ch == '\u22DA') {
                    htm.add("&lesseqgtr;");
                } else if (ch == '\u22DB') {
                    htm.add("&gtreqless;");
                } else if (ch == '\u22DE') {
                    htm.add("&curlyeqprec;");
                } else if (ch == '\u22DF') {
                    htm.add("&curlyeqsucc;");
                } else if (ch == '\u22E0') {
                    htm.add("&npreceq;");
                } else if (ch == '\u22E1') {
                    htm.add("&nsucceq;");
                } else if (ch == '\u22E6') {
                    htm.add("&lnsim;");
                } else if (ch == '\u22E7') {
                    htm.add("&gnsim;");
                } else if (ch == '\u22E8') {
                    htm.add("&precnsim;");
                } else if (ch == '\u22E9') {
                    htm.add("&succnsim;");
                } else if (ch == '\u22EF') {
                    htm.add("<span class='sr-only'> three middle dots </span>",
                            "<span class='no-sr'>&middot;&middot;&middot;</span>");
                } else if (ch == '\u2322') {
                    htm.add("<span class='sr-only'> frown </span><span class='no-sr'>&smallfrown;</span>");
                } else if (ch == '\u2323') {
                    htm.add("<span class='sr-only'> smile </span><span class='no-sr'>&smallsmile;</span>");
                } else if (ch == '\u2329') {
                    htm.add("<span class='sr-only'> left angle brackets </span><span class='no-sr'>&lang;</span>");
                } else if (ch == '\u232A') {
                    htm.add("<span class='sr-only'> right angle brackets </span><span class='no-sr'>&rang;</span>");
                } else if (ch == '\u25A0') {
                    htm.add("<span class='sr-only'> filled square </span>",
                            "<span class='no-sr'>&FilledSmallSquare;</span>");
                } else if (ch == '\u25B2') {
                    htm.add("<span class='sr-only'> up-pointing triangle </span>",
                            "<span class='no-sr'>&blacktriangle;</span>");
                } else if (ch == '\u25B3') {
                    htm.add("<span class='sr-only'> up-pointing triangle </span><span class='no-sr'>&xutri;</span>");
                } else if (ch == '\u25BA') {
                    htm.add("<span class='sr-only'> right-pointing triangle </span>",
                            "<span class='no-sr'>&blacktriangleright;</span>");
                } else if (ch == '\u25BC') {
                    htm.add("<span class='sr-only'> down-pointing triangle </span>",
                            "<span class='no-sr'>&blacktriangledown;</span>");
                } else if (ch == '\u25C4') {
                    htm.add("<span class='sr-only'> left-pointing triangle </span>",
                            "<span class='no-sr'>&blacktriangleleft;</span>");
                } else if (ch == '\u2660') {
                    htm.add("<span class='sr-only'> class </span><span class='no-sr'>&class;</span>");
                } else if (ch == '\u2663') {
                    htm.add("<span class='sr-only'> clubs </span><span class='no-sr'>&clubs;</span>");
                } else if (ch == '\u2665') {
                    htm.add("<span class='sr-only'> hearts </span><span class='no-sr'>&hearts;</span>");
                } else if (ch == '\u2666') {
                    htm.add("<span class='sr-only'> diamonds </span><span class='no-sr'>&diams;</span>");
                } else if (ch == '\u2713') {
                    htm.add("<span class='sr-only'> checkmark </span><span class='no-sr'>&check;</span>");
                } else if (ch == '\u27CB') {
                    htm.add("<span class='sr-only'> diagonal up arrow </span><span class='no-sr'>&diagup;</span>");
                } else if (ch == '\u27CD') {
                    htm.add("<span class='sr-only'> diagonal down arrow </span><span class='no-sr'>&diagdown;</span>");
                } else if (ch == '\u27F8') {
                    htm.add("<span class='sr-only'> left arrow </span><span class='no-sr'>&Longleftarrow;</span>");
                } else if (ch == '\u27F9') {
                    htm.add("<span class='sr-only'> right arrow </span><span class='no-sr'>&Longrightarrow;</span>");
                } else if (ch == '\u2A7D') {
                    htm.add("<span class='sr-only'> less than or equal to </span>",
                            "<span class='no-sr'>&leqslant;</span>");
                } else if (ch == '\u2A7E') {
                    htm.add("<span class='sr-only'> greater than or equal to </span>",
                            "<span class='no-sr'>&geqslant;</span>");
                } else if (ch == '\u2A85') {
                    htm.add("&lessapprox;");
                } else if (ch == '\u2A86') {
                    htm.add("&gtrapprox;");
                } else if (ch == '\u2A87') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'>&lneq;</span>");
                } else if (ch == '\u2A88') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'>&gneq;</span>");
                } else if (ch == '\u2A89') {
                    htm.add("&lnapprox;");
                } else if (ch == '\u2A8A') {
                    htm.add("&gnapprox;");
                } else if (ch == '\u2A8B') {
                    htm.add("&lesseqqgtr;");
                } else if (ch == '\u2A8C') {
                    htm.add("&gtreqqless;");
                } else if (ch == '\u2A95') {
                    htm.add("<span class='sr-only'> less than or equal to </span>",
                            "<span class='no-sr'>&eqslantless;</span>");
                } else if (ch == '\u2A96') {
                    htm.add("<span class='sr-only'> greater than or equal to </span>",
                            "<span class='no-sr'>&eqslantgtr;</span>");
                } else if (ch == '\u2AA1') {
                    htm.add("&lll;");
                } else if (ch == '\u2AA2') {
                    htm.add("&ggg;");
                } else if (ch == '\u2AAF') {
                    htm.add("&preceq;");
                } else if (ch == '\u2AB0') {
                    htm.add("&succeq;");
                } else if (ch == '\u2AB5') {
                    htm.add("&precneqq;");
                } else if (ch == '\u2AB6') {
                    htm.add("&succneqq;");
                } else if (ch == '\u2AB7') {
                    htm.add("&precapprox;");
                } else if (ch == '\u2AB8') {
                    htm.add("&succapprox;");
                } else if (ch == '\u2AB9') {
                    htm.add("&precnapprox;");
                } else if (ch == '\u2ABA') {
                    htm.add("&succnapprox;");
                } else if (ch == '\u2ADB') {
                    htm.add("&pitchfork;");
                } else {
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXOne")) {

                if (ch == '\u0029') {
                    htm.add("<span class='sr-only'> left arrow </span><span class='no-sr'>&larr;</span>");
                } else if (ch == '\u002A') {
                    htm.add("<span class='sr-only'> left arrow </span><span class='no-sr'>&Longleftarrow;</span>");
                } else if (ch == '\u002f') {
                    htm.add("<span class='sr-only'> right arrow </span><span class='no-sr'>&rarr;</span>");
                } else if (ch == '\u0030') {
                    htm.add("<span class='sr-only'> right arrow </span><span class='no-sr'>&Longrightarrow;</span>");
                } else if (ch == '\u0035') {
                    htm.add("<span class='sr-only'> left right arrow </span><span class='no-sr'>&hArr;</span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXOne: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXTwo")) {

                if (ch == '\u0023') {
                    htm.add("<span class='sr-only'> checkmark </span><span class='no-sr'>&check;</span>");
                } else if (ch == '\u0036') {
                    htm.add("<span class='sr-only'> triangle </span><span class='no-sr'>&xutri;</span>");
                    htm.add("&xutri;");
                } else if (ch == '\u004f') {
                    htm.add("<span class='sr-only'> triangle </span><span class='no-sr'>&xutri;</span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXTwo: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXThree")) {

                if (ch == '\u0021') {
                    htm.add("<span class='sr-only'> less than </span><span class='no-sr'> &lt; </span>");
                } else if (ch == '\u0023') {
                    htm.add(" &leqslant; ");
                } else if (ch == '\u0024') {
                    htm.add(" &eqslantless; ");
                } else if (ch == '\u0025') {
                    htm.add("<span class='sr-only'> less than or equal to </span><span class='no-sr'> &leq; </span>");
                } else if (ch == '\u0026') {
                    htm.add("<span class='sr-only'> less than or equal to </span><span class='no-sr'> &leqq; </span>");
                } else if (ch == '\u0028') {
                    htm.add(" &lesssim; ");
                } else if (ch == '\u0029') {
                    htm.add(" &lessapprox; ");
                } else if (ch == '\u002b') {
                    htm.add(" &lessgtr; ");
                } else if (ch == '\u002c') {
                    htm.add(" &lesseqgtr; ");
                } else if (ch == '\u002d') {
                    htm.add(" &lesseqqgtr; ");
                } else if (ch == '\u002f') {
                    htm.add(" &ll; ");
                } else if (ch == '\u0030') {
                    htm.add(" &lll; ");
                } else if (ch == '\u0031') {
                    htm.add(" &lessdot; ");
                } else if (ch == '\u0033') {
                    htm.add(" &prec; ");
                } else if (ch == '\u0034') {
                    htm.add(" &precsim; ");
                } else if (ch == '\u0035') {
                    htm.add(" &precapprox; ");
                } else if (ch == '\u0036') {
                    htm.add(" &preceq; ");
                } else if (ch == '\u0037') {
                    htm.add(" &preccurlyeq; ");
                } else if (ch == '\u0038') {
                    htm.add(" &curlyeqprec; ");
                } else if (ch == '\u003a') {
                    htm.add("<span class='sr-only'> angle </span><span class='no-sr'> &angle; </span>");
                } else if (ch == '\u003b') {
                    htm.add("<span class='sr-only'> measured angle </span>", //
                            "<span class='no-sr'> &measuredangle; </span>");
                } else if (ch == '\u003e') {
                    htm.add("<span class='sr-only'> not less than </span><span class='no-sr'> &nless; </span>");
                } else if (ch == '\u003f') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'> &nleq; </span>");
                } else if (ch == '\u0040') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'> &lneq; </span>");
                } else if (ch == '\u0041') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'> &lneqq; </span>");
                } else if (ch == '\u0042') {
                    htm.add(" &lnsim; ");
                } else if (ch == '\u0043') {
                    htm.add(" &lnapprox; ");
                } else if (ch == '\u0046') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'> &nleq; </span>");
                } else if (ch == '\u0047') {
                    htm.add("<span class='sr-only'> not less than or equal to </span>",
                            "<span class='no-sr'> &nleqq; </span>");
                } else if (ch == '\u0048') {
                    htm.add("<span class='sr-only'> between </span><span class='no-sr'> &between; </span>");
                } else if (ch == '\u0049') {
                    htm.add(" &nprec; ");
                } else if (ch == '\u004a') {
                    htm.add(" &precnsim; ");
                } else if (ch == '\u004b') {
                    htm.add(" &precnapprox; ");
                } else if (ch == '\u004c') {
                    htm.add(" &precneqq; ");
                } else if (ch == '\u004d') {
                    htm.add(" &npreceq; ");
                } else if (ch == '\u004e') {
                    htm.add("<span class='sr-only'> infinity </span><span class='no-sr'>&infin;</span>");
                } else if (ch == '\u004f') {
                    htm.add("<span class='sr-only'> greater than </span><span class='no-sr'> &gt; </span>");
                } else if (ch == '\u0050') {
                    htm.add("<span class='sr-only'> greater than or equal to </span>",
                            "<span class='no-sr'> &geqslant; </span>");
                    htm.add(" &geqslant; ");
                } else if (ch == '\u0051') {
                    htm.add("<span class='sr-only'> greater than or equal to </span>",
                            "<span class='no-sr'> &eqslantgtr; </span>");
                } else if (ch == '\u0052') {
                    htm.add("<span class='sr-only'> greater than or equal to </span>",
                            "<span class='no-sr'> &geq; </span>");
                } else if (ch == '\u0053') {
                    htm.add("<span class='sr-only'> greater than or equal to </span>",
                            "<span class='no-sr'> &geqq; </span>");
                } else if (ch == '\u0054') {
                    htm.add(" &gtrsim; ");
                } else if (ch == '\u0055') {
                    htm.add(" &gtrapprox; ");
                } else if (ch == '\u0057') {
                    htm.add(" &gtrless; ");
                } else if (ch == '\u0058') {
                    htm.add(" &gtreqless; ");
                } else if (ch == '\u0059') {
                    htm.add(" &gtreqqless; ");
                } else if (ch == '\u005b') {
                    htm.add(" &gg; ");
                } else if (ch == '&') {
                    htm.add(" &ggg; ");
                } else if (ch == '\u005d') {
                    htm.add(" &gtrdot; ");
                } else if (ch == '\u005f') {
                    htm.add(" &succ; ");
                } else if (ch == '\u0061') {
                    htm.add(" &succsim; ");
                } else if (ch == '\u0062') {
                    htm.add(" &succapprox; ");
                } else if (ch == '\u0063') {
                    htm.add(" &succeq; ");
                } else if (ch == '\u0064') {
                    htm.add(" &succcurlyeq; ");
                } else if (ch == '\u0065') {
                    htm.add(" &curlyeqsucc; ");
                } else if (ch == '\u0066') {
                    htm.add(" &varpropto; ");
                } else if (ch == '\u0067') {
                    htm.add(" &smallsmile; ");
                } else if (ch == '\u0068') {
                    htm.add(" &smallfrown; ");
                } else if (ch == '\u0069') {
                    htm.add(" &pitchfork; ");
                } else if (ch == '\u006a') {
                    htm.add("<span class='sr-only'> not greater than </span>",
                            "<span class='no-sr'> &ngt; </span>");
                } else if (ch == '\u006b') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'> &ngeq; </span>");
                } else if (ch == '\u006c') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'> &gneq; </span>");
                } else if (ch == '\u006d') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'> &gneqq; </span>");
                } else if (ch == '\u006e') {
                    htm.add(" &gnsim; ");
                } else if ((ch == '\u006f') || (ch == '\u0070')) {
                    htm.add(" &gnapprox; ");
                } else if (ch == '\u0072') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'> &ngeq; </span>");
                } else if (ch == '\u0073') {
                    htm.add("<span class='sr-only'> not greater than or equal to </span>",
                            "<span class='no-sr'> &ngeqq; </span>");
                } else if (ch == '\u0074') {
                    htm.add(" &nsucc; ");
                } else if (ch == '\u0075') {
                    htm.add(" &succnsim; ");
                } else if (ch == '\u0076') {
                    htm.add(" &succnapprox; ");
                } else if (ch == '\u0077') {
                    htm.add(" &succneqq; ");
                } else if (ch == '\u0078') {
                    htm.add(" &nsucceq; ");
                } else if (ch == '\u0079') {
                    htm.add("<span class='sr-only'> diagonal up arrow </span>",
                            "<span class='no-sr'> &diagup; </span>");
                } else if (ch == '\u007a') {
                    htm.add("<span class='sr-only'> diagonal down arrow </span>",
                            "<span class='no-sr'> &diagdown; </span>");
                } else if (ch == '\u2010') {
                    htm.add(" &lesseqqgtr; ");
                } else {
                    Log.warning("Unmatched character from ESSTIXThree: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXFour")) {

                if (ch == '\u0021') {
                    htm.add("<span class='sr-only'> grave </span><span class='no-sr'>grave</span>");
                } else if (ch == '\u0023') {
                    htm.add("<span class='sr-only'> prime </span><span class='no-sr'>prime</span>");
                } else if (ch == '\u0024') {
                    htm.add("<span class='sr-only'> double prime </span><span class='no-sr'>Prime</span>");
                } else if (ch == '\u0025') {
                    htm.add("<span class='sr-only'> triple prime </span><span class='no-sr'>tprime</span>");
                } else if (ch == '\u0026') {
                    htm.add("<span class='sr-only'> quadruple prime </span><span class='no-sr'>&Prime;&Prime;</span>");
                } else if (ch == '\u0028') {
                    htm.add("<span class='sr-only'> degrees </span><span class='no-sr'>&deg;</span>");
                } else if (ch == '\u002b') {
                    htm.add("<span class='sr-only'> composed with </span><span class='no-sr'>&compfn;</span>");
                } else if (ch == '\u002f') {
                    htm.add("<span class='sr-only'> three middle dots </span>",
                            "<span class='no-sr'>&middot;&middot;&middot;</span>");
                } else if (ch == '\u007a') {
                    htm.add("<span class='sr-only'> approximately equal to </span><span class='no-sr'>&asymp;</span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXFour: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXFive")) {

                if ((ch == '\u0021') || (ch == '\u0023')) {
                    htm.add("<span class='sr-only'> times </span><span class='no-sr'>&times;</span>");
                } else if (ch == '\u0025') {
                    htm.add("<span class='sr-only'> bullet </span><span class='no-sr'>&bull;</span>");
                } else if (ch == '\u004f') {
                    htm.add("<span class='sr-only'> divided by </span><span class='no-sr'>&div;</span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXFive: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXSix")) {

                if (ch == '\u0021' || ch == '\u0045') {

                    // Integral signs are large - we really want to center it on the line
                    // rather than align it with the baseline
                    htm.add("<span class='sr-only'> integral of </span>",
                            "<span class='no-sr' style='position:relative; top:.2em;'>&int;</span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXSix: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXSeven")) {

                if (ch == '\u0030') {
                    htm.add('(');
                } else if (ch == '\u0031') {
                    htm.add(')');
                } else if (ch == '\u0034') {
                    htm.add("<span style='font-size:larger'>{").eSpan();
                } else if (ch == '\u0035') {
                    htm.add("<span style='font-size:larger'>}").eSpan();
                } else if (ch == '\u0041') {
                    htm.add('{');
                } else if (ch == '\u0042') {
                    htm.add('}');
                } else if (ch == '\u0043') {
                    htm.add("<span class='sr-only'> left angle bracket </span><span class='no-sr'>&lang;</span>");
                } else if (ch == '\u0044') {
                    htm.add("<span class='sr-only'> right angle bracket </span><span class='no-sr'>&rang;</span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXSeven: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXNine")) {

                if (ch == '\u0061') {
                    htm.add("<span class='sr-only'> alpha </span><span class='no-sr'><b>&alpha;</b></span>");
                } else if (ch == '\u0062') {
                    htm.add("<span class='sr-only'> beta </span><span class='no-sr'><b>&beta;</b></span>");
                } else if (ch == '\u0064') {
                    htm.add("<span class='sr-only'> delta </span><span class='no-sr'><b>&delta;</b></span>");
                } else if (ch == '\u0070') {
                    htm.add("<span class='sr-only'> pi </span><span class='no-sr'><b>&pi;</b></span>");
                } else if (ch == '\u0071') {
                    htm.add("<span class='sr-only'> theta </span><span class='no-sr'><b>&theta;</b></span>");
                } else if (ch == '\u0072') {
                    htm.add("<span class='sr-only'> rho </span><span class='no-sr'><b>&rho;</b></span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXNine: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXEleven")) {

                if (ch == '\u0061') {
                    htm.add("<span class='sr-only'> alpha </span><span class='no-sr'><b>&alpha;</b></span>");
                } else if (ch == '\u0062') {
                    htm.add("<span class='sr-only'> beta </span><span class='no-sr'><b>&beta;</b></span>");
                } else if (ch == '\u0064') {
                    htm.add("<span class='sr-only'> delta </span><span class='no-sr'><b>&delta;</b></span>");
                } else if (ch == '\u0065') {
                    htm.add("<span class='sr-only'> epsilon </span><span class='no-sr'><b>&epsilon;</b></span>");
                } else if (ch == '\u0066') {
                    htm.add("<span class='sr-only'> phi </span><span class='no-sr'><b>&phi;</b></span>");
                } else if (ch == '\u0067') {
                    htm.add("<span class='sr-only'> gamma </span><span class='no-sr'><b>&gamma;</b></span>");
                } else if (ch == '\u006d') {
                    htm.add("<span class='sr-only'> mu </span><span class='no-sr'><b>&mu;</b></span>");
                } else if (ch == '\u0070') {
                    htm.add("<span class='sr-only'> pi </span><span class='no-sr'><b>&pi;</b></span>");
                } else if (ch == '\u0072') {
                    htm.add("<span class='sr-only'> rho </span><span class='no-sr'><b>&rho;</b></span>");
                } else if (ch == '\u0074') {
                    htm.add("<span class='sr-only'> tau </span><span class='no-sr'><b>&tau;</b></span>");
                } else if (ch == '\u0075') {
                    htm.add("<span class='sr-only'> omega </span><span class='no-sr'><b>&omega;</b></span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXEleven: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else if (fontName.contains("ESSTIXThirteen")) {

                if (ch == '\u0065') {
                    htm.add("<span class='sr-only'> script E </span><span class='no-sr'>&escr;</span>");
                } else if (ch == '\u0066') {
                    htm.add("<span class='sr-only'> f </span><span class='no-sr'>&fnof;</span>");
                } else if (ch == '\u0069') {
                    htm.add("<i><b>i</b></i>");
                } else if (ch == '\u006c') {
                    htm.add("<span class='sr-only'> script L </span><span class='no-sr'>&lscr;</span>");
                } else if (ch == '\u0078') {
                    htm.add("<span class='sr-only'> script X </span><span class='no-sr'>&xscr;</span>");
                } else {
                    Log.warning("Unmatched character from ESSTIXThirteen: 0x", Integer.toHexString(ch), " in ", txt);
                    htm.add(ch);
                }
            } else {
                Log.warning("Unmatched character from ", fontName, ": 0x", Integer.toHexString(ch), " in ", txt);
                htm.add(ch);
            }
        }
    }

    /**
     * Given a realized {@code DocWhitespace}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocWhitespace}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @return the generated HTML
     */
    private static String convertDocWhitespace(final DocWhitespace obj, final Deque<Style> styleStack) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        htm.add(CoreConstants.SPC);

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocInputDoubleField}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocInputDoubleField}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocInputDoubleField(final DocInputDoubleField obj, final Deque<Style> styleStack,
                                                     final boolean enabled, final EvalContext context) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        // An input may set its enabled status based on a radio button.
        String data = null;
        boolean actualDisabled = !enabled;

        if (enabled) {
            final String varName = obj.getEnabledVarName();
            final Object varValue = obj.getEnabledVarValue();

            if (varName == null || varValue == null) {
                // An input may set its enabled status based on a radio button.
                final Formula formula = obj.getEnabledFormula();

                if (formula != null) {
                    final String formstr = formula.toString();
                    if (formstr.startsWith("{which}=")) {
                        data = " data-choice='INP_which_" + formstr.substring(8) + "'";
                    } else if (formstr.startsWith("{WHICH}=")) {
                        data = " data-choice='INP_WHICH_" + formstr.substring(8) + "'";
                    } else if (formstr.startsWith("{which2}=")) {
                        data = " data-choice='INP_which2_" + formstr.substring(8) + "'";
                    }

                    final Object result = formula.evaluate(context);
                    if (!Boolean.TRUE.equals(result)) {
                        actualDisabled = true;
                    }
                }
            } else {
                data = " data-choice='INP_" + varName + "_" + varValue + "'";

                final Object result = context.getVariable(varName);
                if (!varValue.equals(result)) {
                    actualDisabled = true;
                }
            }
        }

        // Valid characters in the field: "-1234567890./"

        final Style style = styleStack.peek();
        final float styleSize = style == null ? 16.0f : style.getSize();
        htm.add("<input type='text' data-lpignore='true' autocomplete='off' ",
                "oninput=\"this.value = this.value.replace(/[^0-9./\\-\u03c0]/g, '');\" ",
                "style='font-family:serif;font-size:", Float.toString(styleSize), "px;");
        if (obj.style == EFieldStyle.UNDERLINE) {
            htm.add("border:0;outline:0;background:transparent;border-bottom:1px solid black;");
        }
        htm.add("'");

        if (obj.width == null) {
            htm.add(" size='4'");
        } else {
            htm.add(" size='", obj.width, "'");
        }

        if (actualDisabled) {
            htm.add(" disabled");
        }
        if (data != null) {
            htm.add(data);
        }
        htm.add(" id='INP_", obj.getName(), "' name='INP_", obj.getName(), "'/> ");

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocInputLongField}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocInputLongField}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocInputLongField(final DocInputLongField obj, final Deque<Style> styleStack,
                                                   final boolean enabled, final EvalContext context) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        String data = null;
        boolean actualDisabled = !enabled;

        if (enabled) {
            final String varName = obj.getEnabledVarName();
            final Object varValue = obj.getEnabledVarValue();

            if (varName == null || varValue == null) {
                // An input may set its enabled status based on a radio button.
                final Formula formula = obj.getEnabledFormula();

                if (formula != null) {
                    final String formstr = formula.toString();
                    if (formstr.startsWith("{which}=")) {
                        data = " data-choice='INP_which_" + formstr.substring(8) + "'";
                    } else if (formstr.startsWith("{WHICH}=")) {
                        data = " data-choice='INP_WHICH_" + formstr.substring(8) + "'";
                    } else if (formstr.startsWith("{which2}=")) {
                        data = " data-choice='INP_which2_" + formstr.substring(8) + "'";
                    }

                    final Object result = formula.evaluate(context);
                    if (!Boolean.TRUE.equals(result)) {
                        actualDisabled = true;
                    }
                }
            } else {
                data = " data-choice='INP_" + varName + "_" + varValue + "'";

                final Object result = context.getVariable(varName);
                if (!varValue.equals(result)) {
                    actualDisabled = true;
                }
            }
        }

        final Style style = styleStack.peek();
        final float styleSize = style == null ? 16.0f : style.getSize();
        htm.add("<input type='text' data-lpignore='true' autocomplete='off' ",
                "oninput=\"this.value = this.value.replace(/[^0-9\\-]/g, '');\" ",
                "style='font-family:serif;font-size:", Float.toString(styleSize), "px;");
        if (obj.style == EFieldStyle.UNDERLINE) {
            htm.add("border:0;outline:0;background:transparent;border-bottom:1px solid black;");
        }
        htm.add("'");

        if (obj.width == null) {
            htm.add(" size='4'");
        } else {
            htm.add(" size='", obj.width, "'");
        }

        if (actualDisabled) {
            htm.add(" disabled");
        }
        if (data != null) {
            htm.add(data);
        }
        htm.add(" id='INP_", obj.getName(), "' name='INP_", obj.getName(), "'/> ");

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocInputStringField}, generates the corresponding HTML.
     *
     * @param obj        the {@code DocInputStringField}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocInputStringField(final DocInputStringField obj, final Deque<Style> styleStack,
                                                     final boolean enabled, final EvalContext context) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        // An input may set its enabled status based on a radio button.
        String data = null;
        boolean actualDisabled = !enabled;

        if (enabled) {
            final String varName = obj.getEnabledVarName();
            final Object varValue = obj.getEnabledVarValue();

            if (varName == null || varValue == null) {
                // An input may set its enabled status based on a radio button.
                final Formula formula = obj.getEnabledFormula();

                if (formula != null) {
                    final String formstr = formula.toString();
                    if (formstr.startsWith("{which}=")) {
                        data = " data-choice='INP_which_" + formstr.substring(8) + "'";
                    } else if (formstr.startsWith("{WHICH}=")) {
                        data = " data-choice='INP_WHICH_" + formstr.substring(8) + "'";
                    } else if (formstr.startsWith("{which2}=")) {
                        data = " data-choice='INP_which2_" + formstr.substring(8) + "'";
                    }

                    final Object result = formula.evaluate(context);
                    if (!Boolean.TRUE.equals(result)) {
                        actualDisabled = true;
                    }
                }
            } else {
                data = " data-choice='INP_" + varName + "_" + varValue + "'";

                final Object result = context.getVariable(varName);
                if (!varValue.equals(result)) {
                    actualDisabled = true;
                }
            }
        }

        final Style style = styleStack.peek();
        final float styleSize = style == null ? 16.0f : style.getSize();

        htm.add("<input type='text' data-lpignore='true' autocomplete='off' style='font-family:serif;font-size:",
                Float.toString(styleSize), "px;");
        if (obj.style == EFieldStyle.UNDERLINE) {
            htm.add("border:0;outline:0;background:transparent;border-bottom:1px solid black;");
        }
        htm.add("'");

        if (obj.width == null) {
            htm.add(" size='4'");
        } else {
            htm.add(" size='", obj.width, "'");
        }

        if (actualDisabled) {
            htm.add(" disabled");
        }
        if (data != null) {
            htm.add(data);
        }
        htm.add(" id='INP_", obj.getName(), "' name='INP_", obj.getName(), "'/> ");

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocInputRadioButton}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocInputRadioButton}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocInputRadioButton(final DocColumn column,
                                                     final DocInputRadioButton obj, final Deque<Style> styleStack,
                                                     final boolean enabled, final EvalContext context) {

        final float fontSize = (float) obj.getFontSize();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + obj.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        // An input may set its enabled status based on a radio button.
        final String data = null;
        boolean actualDisabled = !enabled;

        if (enabled) {
            final String varName = obj.getEnabledVarName();
            final Object varValue = obj.getEnabledVarValue();

            if (varName == null || varValue == null) {
                // An input may set its enabled status based on a radio button.
                final Formula formula = obj.getEnabledFormula();

                if (formula != null) {
                    final String formstr = formula.toString();
                    if (formstr.startsWith("{which}=")) {
                    } else if (formstr.startsWith("{WHICH}=")) {
                    } else if (formstr.startsWith("{which2}=")) {
                    }

                    final Object result = formula.evaluate(context);
                    if (!Boolean.TRUE.equals(result)) {
                        actualDisabled = true;
                    }
                }
            } else {

                final Object result = context.getVariable(varName);
                if (!varValue.equals(result)) {
                    actualDisabled = true;
                }
            }
        }

        // Other inputs may set their enabled status based on this radio button.

        // Such inputs will have an "enabledFormula" of the form "{WHICH}=1" where WHICH
        // must match this input's name, and 1 must match this input's value.

        // So we add an "onClick" handler to the radio button, and for all other inputs whose
        // enabled formula is of the form {THIS-INPUT'S-ID}=number, the handler will set the
        // disabled state of that input accordingly. The click event will always occur on the
        // button that is becoming the selected radio button

        final String name = obj.getName();
        final String value = Integer.toString(obj.value);

        final Collection<String> toEnable = new ArrayList<>(10);
        final Collection<String> toDisable = new ArrayList<>(10);

        final String start = "{" + name + "}=";

        if (column.getInputs() == null) {
            Log.warning("Doc column has no inputs, but we are presenting a radio button!");
        } else {
            for (final AbstractDocInput input : column.getInputs()) {

                final String varName = input.getEnabledVarName();
                final Object varValue = input.getEnabledVarValue();

                if (varName == null || varValue == null) {
                    final Formula enabledFormula = input.getEnabledFormula();
                    if (enabledFormula == null) {
                        continue;
                    }
                    final String str = enabledFormula.toString();
                    if (str.startsWith(start)) {
                        // The input's enabled state depends on this radio button
                        if (value.equals(str.substring(start.length()))) {
                            // Input is enabled if this radio button is selected, so the "onClick"
                            // for this button should enable it.
                            toEnable.add(input.getName());
                        } else {
                            toDisable.add(input.getName());
                        }
                    }
                } else if (varName.equals(name) && varValue instanceof final Long longValue) {
                    if (longValue.intValue() == obj.value) {
                        toEnable.add(input.getName());
                    } else {
                        toDisable.add(input.getName());
                    }
                }
            }
        }

        final Style style = styleStack.peek();
        final float floatFontSize = style == null ? 16.0f : style.getSize();
        final String boxSizeStr = Integer.toString(Math.round(floatFontSize * 0.8f));
        final String fontSizeStr = Float.toString(floatFontSize);

        htm.add("<input type='radio' style='width:", boxSizeStr, "px;height:", boxSizeStr, "px;font-size:",
                fontSizeStr, "px;'");

        if (obj.isChoiceSelected()) {
            htm.add(" checked");
        }
        if (actualDisabled) {
            htm.add(" disabled");
        }

        if ((!toEnable.isEmpty() || !toDisable.isEmpty())) {
            htm.add(" onClick='function go(){");
            for (final String en : toEnable) {
                htm.add("document.getElementById(\"INP_", en, "\").disabled = false;");
            }
            for (final String dis : toDisable) {
                htm.add("document.getElementById(\"INP_", dis, "\").disabled = true;");
            }
            htm.add("} go();'");
        }

        htm.add(" id='INP_", name, "_", value, "' name='INP_", name, "' value='", value, "'/> ");

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocInputCheckbox}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocInputCheckbox}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocInputCheckbox(final DocColumn column, final DocInputCheckbox obj,
                                                  final Deque<Style> styleStack, final boolean enabled,
                                                  final EvalContext context) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Style current = styleStack.peek();
        final float fontSize = (float) obj.getFontSize();

        final boolean updateSize = current != null && Math.abs(fontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !obj.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(fontSize, obj.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + fontSize + "px;");
            }
            if (updateColor) {
                htm.add("color:", obj.getColorName(), ";");
            }
            htm.add("'>");
        }

        // An input may set its enabled status based on a radio button.
        final String data = null;
        boolean actualDisabled = !enabled;

        if (enabled) {
            final String varName = obj.getEnabledVarName();
            final Object varValue = obj.getEnabledVarValue();

            if (varName == null || varValue == null) {
                // An input may set its enabled status based on a radio button.
                final Formula formula = obj.getEnabledFormula();

                if (formula != null) {
                    final String formstr = formula.toString();
                    if (formstr.startsWith("{which}=")) {
                    } else if (formstr.startsWith("{WHICH}=")) {
                    } else if (formstr.startsWith("{which2}=")) {
                    }

                    final Object result = formula.evaluate(context);
                    if (!Boolean.TRUE.equals(result)) {
                        actualDisabled = true;
                    }
                }
            } else {

                final Object result = context.getVariable(varName);
                if (!varValue.equals(result)) {
                    actualDisabled = true;
                }
            }
        }

        // Other inputs may set their enabled status based on this radio button.

        // Such inputs will have an "enabledFormula" of the form "{WHICH}=1" where WHICH
        // must match this input's name, and 1 must match this input's value.

        // So we add an "onClick" handler to the radio button, and for all other inputs whose
        // enabled formula is of the form {THIS-INPUT'S-ID}=number, the handler will set the
        // disabled state of that input accordingly. The click event will always occur on the
        // button that is becoming the selected radio button

        final String name = obj.getName();
        final String value = Long.toString(obj.value);

        final Collection<String> toEnable = new ArrayList<>(10);
        final Collection<String> toDisable = new ArrayList<>(10);

        final String start = "{" + name + "}=";

        if (column.getInputs() == null) {
            Log.warning("Doc column has no inputs, but we are presenting a checkbox!");
        } else {
            for (final AbstractDocInput input : column.getInputs()) {
                final Formula enabledFormula = input.getEnabledFormula();
                if (enabledFormula == null) {
                    continue;
                }
                final String str = enabledFormula.toString();
                if (str.startsWith(start)) {
                    // The input's enabled state depends on this radio button
                    if (value.equals(str.substring(start.length()))) {
                        // Input is enabled if this radio button is selected, so the "onClick" for
                        // this button should enable it.
                        toEnable.add(input.getName());
                    } else {
                        toDisable.add(input.getName());
                    }
                }
            }
        }

        final Style style = styleStack.peek();
        final float floatFontSize = style == null ? 16.0f : style.getSize();
        final String boxSizeStr = Integer.toString(Math.round(floatFontSize * 0.7f));
        final String fontSizeStr = Float.toString(floatFontSize);

        htm.add("<input type='checkbox' style='width:", boxSizeStr, "px;height:", boxSizeStr, "px;font-size:",
                fontSizeStr, "px;'");

        if (obj.isChoiceSelected()) {
            htm.add(" checked");
        }
        if (actualDisabled) {
            htm.add(" disabled");
        }

        if ((!toEnable.isEmpty() || !toDisable.isEmpty())) {
            htm.add(" onClick='function go(){");
            for (final String en : toEnable) {
                htm.add("document.getElementById(\"INP_", en, "\").disabled = false;");
            }
            for (final String dis : toDisable) {
                htm.add("document.getElementById(\"INP_", dis, "\").disabled = true;");
            }
            htm.add("} go();'");
        }

        htm.add(" id='INP_", name, "_", value, "' name='INP_", name, "' value='", value, "'/> ");

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }

        return htm.toString();
    }

    /**
     * Given a realized {@code DocNonwrappingSpan}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocNonwrappingSpan}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocNonwrappingSpan(final DocColumn column, final DocNonwrappingSpan obj,
                                                    final Deque<Style> styleStack, final boolean enabled,
                                                    final int[] id, final EvalContext context, final boolean inMath) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.add("<span style='white-space:nowrap;'>");
        appendChildrenHtml(column, obj, htm, styleStack, enabled, id, context, inMath);
        htm.eSpan();

        return htm.toString();
    }

    /**
     * Given a realized {@code DocSimpleSpan}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocSimpleSpan}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @param inMath     {@code true} if content is within a math span
     * @return the generated HTML
     */
    private static String convertDocSimpleSpan(final DocColumn column, final DocSimpleSpan obj,
                                               final Deque<Style> styleStack, final boolean enabled, final int[] id,
                                               final EvalContext context, final boolean inMath) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        appendChildrenHtml(column, obj, htm, styleStack, enabled, id, context, inMath);

        return htm.toString();
    }

    /**
     * Given a realized {@code DocMathSpan}, generates the corresponding HTML.
     *
     * @param column     the owning column
     * @param obj        the {@code DocMathSpan}
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param context    the evaluation context
     * @return the generated HTML
     */
    private static String convertDocMathSpan(final DocColumn column, final DocMathSpan obj,
                                             final Deque<Style> styleStack, final boolean enabled, final int[] id,
                                             final EvalContext context) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        appendChildrenHtml(column, obj, htm, styleStack, enabled, id, context, true);

        return htm.toString();
    }

    /**
     * Given a realized {@code DocSymbolPalette}, generates the corresponding HTML.
     *
     * @param obj the {@code DocWrappingSpan}
     * @return the generated HTML
     */
    private static String convertDocSymbolPalette(final DocSymbolPalette obj) {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        for (final DocSymbolPalette.Key key : obj.keys) {

            final String fname = "typeSymbol" + key.label;

            htm.addln("<script>");
            htm.addln("  function " + fname + "() {");
            htm.addln("    if (document.activeElement) {");
            htm.addln("      document.activeElement.value = document.activeElement.value + \""
                    + key.symbol.character + "\";");
            htm.addln("    }");
            htm.addln("  }");
            htm.addln("</script>");

            htm.add("<div style='display:inline-block;border-radius:6px;border-width:0px;",
                    "color:#fff;line-height:1.2em;background:#1e4d2b;padding:4px 12px;",
                    "margin:6px 0;white-space:nowrap;' onMouseDown='" + fname + "(); return false;'>");
            htm.add(key.symbol.character);
            htm.eDiv();
        }

        return htm.toString();
    }

    /**
     * Appends the HTML for all children of an object to an {@code HtmlBuilder}.
     *
     * @param column     the owning column
     * @param parent     the parent whose children to append
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param styleStack the font size stack - top Integer is current HTML font size
     * @param enabled    true to disable inputs (used when showing answers or solutions)
     * @param id         a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                   incremented each time a unique ID is called for)
     * @param inMath     {@code true} if content is within a math span
     * @param context    the evaluation context
     */
    private static void appendChildrenHtml(final DocColumn column, final AbstractDocContainer parent,
                                           final HtmlBuilder htm, final Deque<Style> styleStack,
                                           final boolean enabled, final int[] id, final EvalContext context,
                                           final boolean inMath) {

        final float parentFontSize = (float) parent.getFontSize();

        final Style current = styleStack.peek();
        final boolean updateSize = current != null && Math.abs(parentFontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !parent.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(parentFontSize, parent.getColorName());
            styleStack.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + parent.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", parent.getColorName(), ";");
            }
            htm.add("'>");
        }

        for (final AbstractDocObjectTemplate child : parent.getChildren()) {
            if (child.isVisible()) {
                appendChildHtml(column, child, htm, styleStack, enabled, id, context, inMath);
            }
        }

        if (updateSize || updateColor) {
            htm.eSpan();
            styleStack.pop();
        }
    }

    /**
     * Appends the HTML for a single child to an {@code HtmlBuilder}.
     *
     * @param column  the owning column
     * @param child   the child to append
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param styles  the font size stack - top Integer is current HTML font size
     * @param enabled true to disable inputs (used when showing answers or solutions)
     * @param id      a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                incremented each time a unique ID is called for)
     * @param inMath  {@code true} if content is within a math span
     * @param context the evaluation context
     */
    private static void appendChildHtml(final DocColumn column, final AbstractDocObjectTemplate child,
                                        final HtmlBuilder htm, final Deque<Style> styles, final boolean enabled,
                                        final int[] id, final EvalContext context, final boolean inMath) {

        final float childFontSize = (float) child.getFontSize();

        final Style current = styles.peek();

        final boolean updateSize = current != null && Math.abs(childFontSize - current.getSize()) > 0.01f;
        final boolean updateColor = current != null && !child.getColorName().equals(current.getColorName());

        if (updateSize || updateColor) {
            final Style newStyle = new Style(childFontSize, child.getColorName());
            styles.push(newStyle);
            htm.add("<span style='");
            if (updateSize) {
                htm.add("font-size:" + child.getFontSize() + "px;");
            }
            if (updateColor) {
                htm.add("color:", child.getColorName(), ";");
            }
            htm.add("'>");
        }

        switch (child) {
            case final DocColumn col -> htm.add(convertDocColumn(col, styles, enabled, id, context));
            case final DocDrawing drawing -> htm.add(convertDocDrawing(drawing, styles, context));
            case final DocFence fence -> htm.add(convertDocFence(column, fence, styles, enabled, id, context, inMath));
            case final DocFraction fraction ->
                    htm.add(convertDocFraction(column, fraction, styles, enabled, id, context, inMath));
            case final DocGraphXY graph -> htm.add(convertDocGraphXY(graph, styles, context));
            case final DocImage image -> htm.add(convertDocImage(image, styles));
            case final DocParameterReference ref ->
                    htm.add(convertDocParameterRef(column, ref, styles, enabled, id, context, inMath));
            case final DocParagraph p -> htm.add(convertDocParagraph(column, p, styles, enabled, id, context));
            case final DocRadical radical ->
                    htm.add(convertDocRadical(column, radical, styles, enabled, id, context, inMath));
            case final DocRelativeOffset rel ->
                    htm.add(convertDocRelativeOffset(column, rel, styles, enabled, id, context, inMath));
            case final DocTable table -> htm.add(convertDocTable(column, table, styles, enabled, id, context, inMath));
            case final DocText text -> htm.add(convertDocText(text, styles, inMath));
            case final DocHSpace hspace -> htm.add(convertDocHSpace(hspace, context));
            case final DocVSpace vspace -> htm.add(convertDocVSpace(vspace, context));
            case final DocWhitespace ws -> htm.add(convertDocWhitespace(ws, styles));
            case final DocInputDoubleField field ->
                    htm.add(convertDocInputDoubleField(field, styles, enabled, context));
            case final DocInputLongField field -> htm.add(convertDocInputLongField(field, styles, enabled, context));
            case final DocInputStringField field ->
                    htm.add(convertDocInputStringField(field, styles, enabled, context));
            case final DocInputRadioButton radio ->
                    htm.add(convertDocInputRadioButton(column, radio, styles, enabled, context));
            case final DocInputCheckbox checkbox ->
                    htm.add(convertDocInputCheckbox(column, checkbox, styles, enabled, context));
            case final DocNonwrappingSpan span ->
                    htm.add(convertDocNonwrappingSpan(column, span, styles, enabled, id, context, inMath));
            case final DocSimpleSpan span ->
                    htm.add(convertDocSimpleSpan(column, span, styles, enabled, id, context, inMath));
            case final DocMathSpan span -> htm.add(convertDocMathSpan(column, span, styles, enabled, id, context));
            case final DocSymbolPalette span -> htm.add(convertDocSymbolPalette(span));
            default -> {
            }
        }

        if (updateSize || updateColor) {
            htm.eSpan();
            styles.pop();
        }
    }
}
