package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.EFieldStyle;
import dev.mathops.assessment.document.EVAlign;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.formula.XmlFormulaFactory;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableInputReal;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.AbstractAttributedElementBase;
import dev.mathops.commons.parser.xml.CData;
import dev.mathops.commons.parser.xml.Comment;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import java.awt.Color;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * A class to create {@code DocColumn} or {@code DocSimpleSpan} objects from XML source.
 */
public enum DocFactory {
    ;

    /** A commonly-used string. */
    private static final String V_SPACE = "v-space";

    /** A commonly-used string. */
    private static final String P = "p";

    /** A commonly-used string. */
    private static final String SPAN = "span";

    /** A commonly-used string. */
    private static final String NONWRAP = "nonwrap";

    /** A commonly-used string. */
    private static final String MATH = "math";

    /** A commonly-used string. */
    private static final String FRACTION = "fraction";

    /** A commonly-used string. */
    private static final String RADICAL = "radical";

    /** A commonly-used string. */
    private static final String REL_OFFSET = "rel-offset";

    /** A commonly-used string. */
    private static final String FENCE = "fence";

    /** A commonly-used string. */
    private static final String ALIGN_MARK = "align-mark";

    /** A commonly-used string. */
    private static final String H_SPACE = "h-space";

    /** A commonly-used string. */
    private static final String H_ALIGN = "h-align";

    /** A commonly-used string. */
    private static final String TABLE = "table";

    /** A commonly-used string. */
    private static final String TR = "tr";

    /** A commonly-used string. */
    private static final String TD = "td";

    /** A commonly-used string. */
    private static final String DRAWING = "drawing";

    /** A commonly-used string. */
    private static final String GRAPH_XY = "graphxy";

    /** A commonly-used string. */
    private static final String INPUT = "input";

    /** A commonly-used string. */
    private static final String IMAGE = "image";

    /** A commonly-used string. */
    private static final String SYMBOL_PALETTE = "symbol-palette";

    /** A commonly-used string. */
    private static final String NUMERATOR = "numerator";

    /** A commonly-used string. */
    private static final String DENOMINATOR = "denominator";

    /** A commonly-used string. */
    private static final String BASE = "base";

    /** A commonly-used string. */
    private static final String ROOT = "root";

    /** A commonly-used string. */
    private static final String SUPER = "super";

    /** A commonly-used string. */
    private static final String SUB = "sub";

    /** A commonly-used string. */
    private static final String OVER = "over";

    /** A commonly-used string. */
    private static final String UNDER = "under";

    /** A commonly-used string. */
    private static final String FUNCTION_PLOT = "function-plot";

    /** A commonly-used string. */
    private static final String FORMULA = "formula";

    /** A commonly-used string. */
    private static final String LINE = "line";

    /** A commonly-used string. */
    private static final String ARC = "arc";

    /** A commonly-used string. */
    private static final String OVAL = "oval";

    /** A commonly-used string. */
    private static final String RECTANGLE = "rectangle";

    /** A commonly-used string. */
    private static final String POLYGON = "polygon";

    /** A commonly-used string. */
    private static final String PROTRACTOR = "protractor";

    /** A commonly-used string. */
    private static final String RASTER = "raster";

    /** A commonly-used string. */
    private static final String TEXT = "text";

    /** A commonly-used string. */
    private static final String WIDTH = "width";

    /** A commonly-used string. */
    private static final String HEIGHT = "height";

    /** A commonly-used string. */
    private static final String POSITION = "position";

    /** A commonly-used string. */
    private static final String JUSTIFICATION = "justification";

    /** A commonly-used string. */
    private static final String VALIGN = "valign";

    /** A commonly-used string. */
    private static final String LEFT = "left";

    /** A commonly-used string. */
    private static final String RIGHT = "right";

    /** A commonly-used string. */
    private static final String CENTER = "center";

    /** A commonly-used string. */
    private static final String FULL = "full";

    /** A commonly-used string. */
    private static final String LEFT_HANG = "left-hang";

    /** A commonly-used string. */
    private static final String SPACING = "spacing";

    /** A commonly-used string. */
    private static final String NONE = "none";

    /** A commonly-used string. */
    private static final String SMALL = "small";

    /** A commonly-used string. */
    private static final String NORMAL = "normal";

    /** A commonly-used string. */
    private static final String LARGE = "large";

    /** A commonly-used string. */
    private static final String INDENT = "indent";

    /** A commonly-used string. */
    private static final String BGCOLOR = "bgcolor";

    /** A commonly-used string. */
    private static final String SRC = "src";

    /** A commonly-used string. */
    private static final String ALT = "alt";

    /** A commonly-used string. */
    private static final String MIN_X = "minx";

    /** A commonly-used string. */
    private static final String MIN_Y = "miny";

    /** A commonly-used string. */
    private static final String MAX_X = "maxx";

    /** A commonly-used string. */
    private static final String MAX_Y = "maxy";

    /** A commonly-used string. */
    private static final String X_TICK_INTERVAL = "xtickinterval";

    /** A commonly-used string. */
    private static final String Y_TICK_INTERVAL = "ytickinterval";

    /** A commonly-used string. */
    private static final String BORDER_COLOR = "bordercolor";

    /** A commonly-used string. */
    private static final String GRID_COLOR = "gridcolor";

    /** A commonly-used string. */
    private static final String TICK_COLOR = "tickcolor";

    /** A commonly-used string. */
    private static final String AXIS_COLOR = "axiscolor";

    /** A commonly-used string. */
    private static final String BORDER_WIDTH = "borderwidth";

    /** A commonly-used string. */
    private static final String GRID_WIDTH = "gridwidth";

    /** A commonly-used string. */
    private static final String TICK_WIDTH = "tickwidth";

    /** A commonly-used string. */
    private static final String TICK_SIZE = "ticksize";

    /** A commonly-used string. */
    private static final String AXIS_WIDTH = "axiswidth";

    /** A commonly-used string. */
    private static final String AXIS_LABEL_FONT_SIZE = "axislabelfontsize";

    /** A commonly-used string. */
    private static final String TICK_LABEL_FONT_SIZE = "ticklabelfontsize";

    /** A commonly-used string. */
    private static final String X_AXIS_LABEL = "xaxislabel";

    /** A commonly-used string. */
    private static final String Y_AXIS_LABEL = "yaxislabel";

    /** A commonly-used string. */
    private static final String TYPE = "type";

    /** A commonly-used string. */
    private static final String PARENTHESES = "parentheses";

    /** A commonly-used string. */
    private static final String BRACKETS = "brackets";

    /** A commonly-used string. */
    private static final String BRACES = "braces";

    /** A commonly-used string. */
    private static final String BARS = "bars";

    /** A commonly-used string. */
    private static final String LBRACE = "lbrace";

    /** A commonly-used string. */
    private static final String BASELINE = "baseline";

    /** A commonly-used string. */
    private static final String BOX_WIDTH = "box-width";

    /** A commonly-used string. */
    private static final String V_LINE_WIDTH = "v-line-width";

    /** A commonly-used string. */
    private static final String H_LINE_WIDTH = "h-line-width";

    /** A commonly-used string. */
    private static final String COLUMN_WIDTH = "column-width";

    /** A commonly-used string. */
    private static final String UNIFORM = "uniform";

    /** A commonly-used string. */
    private static final String NONUNIFORM = "nonuniform";

    /** A commonly-used string. */
    private static final String CELL_MARGINS = "cell-margins";

    /** A commonly-used string. */
    private static final String LINES = "lines";

    /** A commonly-used string. */
    private static final String TOP = "top";

    /** A commonly-used string. */
    private static final String BOTTOM = "bottom";

    /** A commonly-used string. */
    private static final String DOMAIN_VAR = "domain-var";

    /** A commonly-used string. */
    private static final String COLOR = "color";

    /** A commonly-used string. */
    private static final String X = "x";

    /** A commonly-used string. */
    private static final String Y = "y";

    /** A commonly-used string. */
    private static final String STROKE_WIDTH = "stroke-width";

    /** A commonly-used string. */
    private static final String DASH = "dash";

    /** A commonly-used string. */
    private static final String ALPHA = "alpha";

    /** A commonly-used string. */
    private static final String CX = "cx";

    /** A commonly-used string. */
    private static final String CY = "cy";

    /** A commonly-used string. */
    private static final String R = "r";

    /** A commonly-used string. */
    private static final String RX = "rx";

    /** A commonly-used string. */
    private static final String RY = "ry";

    /** A commonly-used string. */
    private static final String START_ANGLE = "start-angle";

    /** A commonly-used string. */
    private static final String ARC_ANGLE = "arc-angle";

    /** A commonly-used string. */
    private static final String STROKE_COLOR = "stroke-color";

    /** A commonly-used string. */
    private static final String STROKE_DASH = "stroke-dash";

    /** A commonly-used string. */
    private static final String STROKE_ALPHA = "stroke-alpha";

    /** A commonly-used string. */
    private static final String FILL_STYLE = "fill-style";

    /** A commonly-used string. */
    private static final String FILL_COLOR = "fill-color";

    /** A commonly-used string. */
    private static final String FILL_ALPHA = "fill-alpha";

    /** A commonly-used string. */
    private static final String FILLED = "filled";

    /** A commonly-used string. */
    private static final String RAYS_SHOWN = "rays-shown";

    /** A commonly-used string. */
    private static final String RAY_WIDTH = "ray-width";

    /** A commonly-used string. */
    private static final String RAY_LENGTH = "ray-length";

    /** A commonly-used string. */
    private static final String RAY_COLOR = "ray-color";

    /** A commonly-used string. */
    private static final String RAY_DASH = "ray-dash";

    /** A commonly-used string. */
    private static final String RAY_ALPHA = "ray-alpha";

    /** A commonly-used string. */
    private static final String LABEL = "label";

    /** A commonly-used string. */
    private static final String LABEL_COLOR = "label-color";

    /** A commonly-used string. */
    private static final String LABEL_ALPHA = "label-alpha";

    /** A commonly-used string. */
    private static final String LABEL_OFFSET = "label-offset";

    /** A commonly-used string. */
    private static final String FONT_NAME = "fontname";

    /** A commonly-used string. */
    private static final String FONT_SIZE = "fontsize";

    /** A commonly-used string. */
    private static final String FONT_STYLE = "fontstyle";

    /** A commonly-used string. */
    private static final String EXPR = "expr";

    /** A commonly-used string. */
    private static final String X_LIST = "x-list";

    /** A commonly-used string. */
    private static final String Y_LIST = "y-list";

    /** A commonly-used string. */
    private static final String ORIENTATION = "orientation";

    /** A commonly-used string. */
    private static final String UNITS = "units";

    /** A commonly-used string. */
    private static final String QUADRANTS = "quadrants";

    /** A commonly-used string. */
    private static final String TEXT_COLOR = "text-color";

    /** A commonly-used string. */
    private static final String CENTER_X = "center-x";

    /** A commonly-used string. */
    private static final String CENTER_Y = "center-y";

    /** A commonly-used string. */
    private static final String RADIUS = "radius";

    /** A commonly-used string. */
    private static final String ANCHOR = "anchor";

    /** A commonly-used string. */
    private static final String HIGHLIGHT = "highlight";

    /** A commonly-used string. */
    private static final String VALUE = "value";

    /** A commonly-used string. */
    private static final String CONTENT = "content";

    /** A commonly-used string. */
    private static final String INTEGER = "integer";

    /** A commonly-used string. */
    private static final String REAL = "real";

    /** A commonly-used string. */
    private static final String STRING = "string";

    /** A commonly-used string. */
    private static final String RADIO_BUTTON = "radio-button";

    /** A commonly-used string. */
    private static final String CHECKBOX = "checkbox";

    /** A commonly-used string. */
    private static final String TEXT_VALUE = "textvalue";

    /** A commonly-used string. */
    private static final String DEFAULT = "default";

    /** A commonly-used string. */
    private static final String STYLE = "style";

    /** A commonly-used string. */
    private static final String TREAT_MINUS_AS = "treat-minus-as";

    /** A commonly-used string. */
    private static final String ENABLED_VAR_NAME = "enabled-var-name";

    /** A commonly-used string. */
    private static final String ENABLED_VAR_VALUE = "enabled-var-value";

    /** A commonly-used string. */
    private static final String BOX = "box";

    /** A commonly-used string. */
    private static final String UNDERLINE = "underline";

    /** A commonly-used string. */
    private static final String TRUE = "TRUE";

    /** A commonly-used string. */
    private static final String FALSE = "FALSE";

    /** A commonly-used string. */
    private static final String SELECTED = "selected";

    /** A commonly-used string. */
    private static final String PLAIN = "plain";

    /** A commonly-used string. */
    private static final String BOLD = "bold";

    /** A commonly-used string. */
    private static final String ITALIC = "italic";

    /** A commonly-used string. */
    private static final String OVERLINE = "overline";

    /** A commonly-used string. */
    private static final String STRIKETHROUGH = "strikethrough";

    /** A commonly-used string. */
    private static final String BOXED = "boxed";

    /** A commonly-used string. */
    private static final String ENABLED = "enabled";

    /** A commonly-used string. */
    private static final String SYMBOLS = "symbols";

    /**
     * Generate a {@code DocColumn} object from an XML element that contains a document tag (with an optional ID), and a
     * contained set of paragraph elements. Any errors encountered while generating the document object will be
     * reflected in the element's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param mode        the parser mode
     * @return the loaded {@code DocColumn} object, or null on any error
     */
    public static DocColumn parseDocColumn(final EvalContext evalContext, final NonemptyElement elem,
                                           final EParserMode mode) {

        final String tagName = elem.getTagName();

        DocColumn result = null;

        final DocColumn doc = new DocColumn();
        doc.tag = tagName;

        final boolean valid = extractFormattable(elem, doc);

        if (valid && extractParagraphs(evalContext, elem, doc, mode)) {
            doc.refreshInputs(evalContext, true);
            result = doc;
        }

        return result;
    }

    /**
     * Generate a {@code DocSimpleSpan} object from XML element that contains a set of arbitrary document objects. Any
     * errors encountered while generating the span object will be reflected in the element's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param mode        the parser mode
     * @return the loaded {@code DocSimpleSpan} object, or null on any error
     */
    public static DocSimpleSpan parseSpan(final EvalContext evalContext, final NonemptyElement elem,
                                          final EParserMode mode) {

        final DocSimpleSpan span = new DocSimpleSpan();

        boolean valid = true;
        for (final INode child : elem.getChildrenAsList()) {

            if (child instanceof final CData cdata) {
                valid = valid && extractText(cdata, span, mode);
            } else if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (SPAN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractWrappingSpan(evalContext, childTag, nonempty, span, mode);
                    } else if (NONWRAP.equalsIgnoreCase(childTag)) {
                        valid = valid && extractNonwrap(evalContext, childTag, nonempty, span, false, mode);
                    } else if (MATH.equalsIgnoreCase(childTag)) {
                        valid = valid && extractMath(evalContext, childTag, nonempty, span, false, mode);
                    } else if (FRACTION.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFraction(evalContext, nonempty, span, mode);
                    } else if (RADICAL.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRadical(evalContext, nonempty, span, mode);
                    } else if (REL_OFFSET.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRelOffset(evalContext, nonempty, span, mode);
                    } else if (FENCE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFence(evalContext, nonempty, span, mode);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(evalContext, nonempty, span, mode);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(evalContext, nonempty, span, mode);
                    } else if (TABLE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTable(evalContext, nonempty, span, mode);
                    } else if (DRAWING.equalsIgnoreCase(childTag)) {
                        valid = valid && extractDrawing(evalContext, nonempty, span, mode);
                    } else if (GRAPH_XY.equalsIgnoreCase(childTag)) {
                        valid = valid && extractGraphxy(evalContext, nonempty, span, mode);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, nonempty, span, mode);
                    } else if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(evalContext, nonempty, span, mode);
                    } else {
                        elem.logError("The " + childTag + " tag is not valid within a span.");
                        valid = false;
                    }
                } else if (child instanceof final EmptyElement empty) {
                    if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(empty, span);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, empty, span, mode);
                    } else if (ALIGN_MARK.equalsIgnoreCase(childTag)) {
                        valid = valid && extractAlignMark(span);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(empty, span);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(empty, span);
                    } else if (SYMBOL_PALETTE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractSymbolPalette(empty, span);
                    } else {
                        elem.logError("An empty " + childTag + " element is not valid within a span.");
                        valid = false;
                    }
                }
            }
        }

        return valid ? span : null;
    }

    /**
     * Scan a range of XML for paragraph tags, adding each to a document. Any other tags encountered will generate an
     * error. Any errors encountered will be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param doc         the {@code DocModel} object that paragraphs will be added to
     * @param mode        the parser mode
     * @return true on success; false on any error
     */
    private static boolean extractParagraphs(final EvalContext evalContext, final NonemptyElement elem,
                                             final DocColumn doc, final EParserMode mode) {

        boolean valid = true;

        for (final IElement child : elem.getElementChildrenAsList()) {
            final String tagName = child.getTagName();

            if (child instanceof final NonemptyElement nonempty) {

                if (V_SPACE.equalsIgnoreCase(tagName)) {
                    valid = valid && extractVSpace(evalContext, nonempty, doc, mode);
                } else if (P.equalsIgnoreCase(tagName)) {
                    valid = valid && extractParagraph(evalContext, nonempty, doc, mode);
                } else {
                    elem.logError("All items in this context must be within &lt;p&gt; tags - found &lt;" + tagName
                            + "&gt;.");
                    valid = false;
                }
            } else if (child instanceof final EmptyElement empty) {
                if (V_SPACE.equalsIgnoreCase(tagName)) {
                    valid = valid && extractVSpace(empty, doc);
                } else {
                    elem.logError("All items in this context must be within &lt;p&gt; tags - found &lt;" + tagName
                            + "&gt;.");
                    valid = false;
                }
            } else {
                elem.logError("Unexpected empty &lt;" + tagName + "&gt; element when expecting non-empty &lt;p&gt;.");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Generate a {@code DocHSpace} object from XML source. Any errors encountered while generating the input object
     * will be reflected in the source file's error context.
     *
     * @param elem      the element
     * @param container the span to which to add this input
     * @return true if loading successful; false otherwise
     */
    private static boolean extractVSpace(final EmptyElement elem, final DocColumn container) {

        boolean valid = true;

        final String heightStr = elem.getStringAttr(HEIGHT);
        Number heightC = null;

        if (heightStr != null) {
            try {
                heightC = NumberParser.parse(heightStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'height' attribute value (must be a valid number).");
                valid = false;
            }
        }

        if (valid) {
            final DocVSpace vSpace = new DocVSpace();

            NumberOrFormula height = null;
            if (heightC != null) {
                height = new NumberOrFormula(heightC);
            }

            vSpace.setSpaceHeight(height);
            container.add(vSpace);
        }

        return valid;
    }

    /**
     * Generate a {@code DocHSpace} object from XML source. Any errors encountered while generating the input object
     * will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractVSpace(final EvalContext evalContext, final NonemptyElement elem,
                                         final DocColumn container, final EParserMode mode) {

        boolean valid = true;

        final String heightStr = elem.getStringAttr(HEIGHT);
        Number heightC = null;
        Formula heightF = null;

        if (heightStr != null) {
            try {
                heightC = NumberParser.parse(heightStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'height' attribute value (must be a valid number).");
                valid = false;
            }
        }

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (HEIGHT.equalsIgnoreCase(childTag)) {
                    if (heightC == null) {
                        if (heightF == null) {
                            heightF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (heightF == null) {
                                elem.logError("Invalid 'height' formula.");
                                valid = false;
                            }
                        } else {
                            elem.logError("Cannot have multiple height formulas.");
                            valid = false;
                        }
                    } else {
                        elem.logError("Cannot have both height attribute and height formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within v-space.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in v-space.");
                valid = false;
            }
        }

        if (valid) {
            final DocVSpace vSpace = new DocVSpace();

            NumberOrFormula height = null;
            if (heightC != null) {
                height = new NumberOrFormula(heightC);
            }
            if (heightF != null) {
                height = new NumberOrFormula(heightF);
            }

            vSpace.setSpaceHeight(height);
            container.add(vSpace);
        }

        return valid;
    }

    /**
     * Generate a {@code DocParagraph} object from XML source. Any errors encountered while generating the paragraph
     * object will be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param doc         the document to which to add the loaded paragraph
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractParagraph(final EvalContext evalContext, final NonemptyElement elem,
                                            final DocColumn doc, final EParserMode mode) {

        final DocParagraph par = new DocParagraph();

        boolean valid = extractFormattable(elem, par);

        final String justificationStr = elem.getStringAttr(JUSTIFICATION);
        if (justificationStr != null) {

            switch (justificationStr) {
                case LEFT -> par.setJustification(DocParagraph.LEFT);
                case RIGHT -> par.setJustification(DocParagraph.RIGHT);
                case CENTER -> par.setJustification(DocParagraph.CENTER);
                case FULL -> par.setJustification(DocParagraph.FULL);
                case LEFT_HANG -> par.setJustification(DocParagraph.LEFT_HANG);
                default -> {
                    elem.logError(
                            "Invalid justification (should be 'left', 'right', 'center', 'full', or 'left-hang').");
                    valid = false;
                }
            }
        }

        final String spacingStr = elem.getStringAttr(SPACING);
        if (spacingStr != null) {
            switch (spacingStr) {
                case NONE -> par.setSpacing(DocParagraph.NONE);
                case SMALL -> par.setSpacing(DocParagraph.SMALL);
                case NORMAL -> par.setSpacing(DocParagraph.NORMAL);
                case LARGE -> par.setSpacing(DocParagraph.LARGE);
                default -> {
                    elem.logError("Invalid paragraph spacing (should be 'none', 'small', 'normal' or 'large').");
                    valid = false;
                }
            }
        }

        final String indentStr = elem.getStringAttr(INDENT);
        if (indentStr != null) {
            try {
                int parsed = Integer.parseInt(indentStr);
                par.setIndent(parsed);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid paragraph indent.");
                valid = false;
            }
        }

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final CData cdata) {
                valid = valid && extractText(cdata, par, mode);
            } else if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (SPAN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractWrappingSpan(evalContext, childTag, nonempty, par, mode);
                    } else if (NONWRAP.equalsIgnoreCase(childTag)) {
                        valid = valid && extractNonwrap(evalContext, childTag, nonempty, par, false, mode);
                    } else if (MATH.equalsIgnoreCase(childTag)) {
                        valid = valid && extractMath(evalContext, childTag, nonempty, par, false, mode);
                    } else if (FRACTION.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFraction(evalContext, nonempty, par, mode);
                    } else if (RADICAL.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRadical(evalContext, nonempty, par, mode);
                    } else if (REL_OFFSET.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRelOffset(evalContext, nonempty, par, mode);
                    } else if (FENCE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFence(evalContext, nonempty, par, mode);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(evalContext, nonempty, par, mode);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(evalContext, nonempty, par, mode);
                    } else if (TABLE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTable(evalContext, nonempty, par, mode);
                    } else if (DRAWING.equalsIgnoreCase(childTag)) {
                        valid = valid && extractDrawing(evalContext, nonempty, par, mode);
                    } else if (GRAPH_XY.equalsIgnoreCase(childTag)) {
                        valid = valid && extractGraphxy(evalContext, nonempty, par, mode);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, nonempty, par, mode);
                    } else if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(evalContext, nonempty, par, mode);
                    } else {
                        elem.logError("The " + childTag + " element is not valid within a paragraph.");
                        valid = false;
                    }
                } else if (child instanceof final EmptyElement empty) {
                    if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(empty, par);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, empty, par, mode);
                    } else if (SYMBOL_PALETTE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractSymbolPalette(empty, par);
                    } else if (ALIGN_MARK.equalsIgnoreCase(childTag)) {
                        valid = valid && extractAlignMark(par);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(empty, par);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(empty, par);
                    } else {
                        elem.logError("An empty " + childTag + " element is not valid within a paragraph.");
                        valid = false;
                    }
                } else {
                    elem.logError("Found " + child.getClass().getSimpleName() + " in paragraph.");
                    valid = false;
                }
            }
        }

        if (valid) {
            doc.add(par);
        }

        return valid;
    }

    /**
     * Generate a {@code DocWrappingSpan} object from XML source. Any errors encountered while generating the span
     * object will be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param name        the tag name
     * @param elem        the element
     * @param container   the span to which to add this wrapping span
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractWrappingSpan(final EvalContext evalContext, final String name,
                                               final NonemptyElement elem, final AbstractDocSpanBase container,
                                               final EParserMode mode) {

        final DocWrappingSpan span = new DocWrappingSpan();
        span.tag = name;

        boolean valid = extractFormattable(elem, span);

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final CData cdata) {
                valid = valid && extractText(cdata, span, mode);
            } else if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (childElem instanceof final NonemptyElement nonempty) {
                    if (SPAN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractWrappingSpan(evalContext, childTag, nonempty, span, mode);
                    } else if (NONWRAP.equalsIgnoreCase(childTag)) {
                        valid = valid && extractNonwrap(evalContext, childTag, nonempty, span, false, mode);
                    } else if (MATH.equalsIgnoreCase(childTag)) {
                        valid = valid && extractMath(evalContext, childTag, nonempty, span, false, mode);
                    } else if (FRACTION.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFraction(evalContext, nonempty, span, mode);
                    } else if (RADICAL.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRadical(evalContext, nonempty, span, mode);
                    } else if (REL_OFFSET.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRelOffset(evalContext, nonempty, span, mode);
                    } else if (FENCE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFence(evalContext, nonempty, span, mode);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(evalContext, nonempty, span, mode);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(evalContext, nonempty, span, mode);
                    } else if (TABLE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTable(evalContext, nonempty, span, mode);
                    } else if (DRAWING.equalsIgnoreCase(childTag)) {
                        valid = valid && extractDrawing(evalContext, nonempty, span, mode);
                    } else if (GRAPH_XY.equalsIgnoreCase(childTag)) {
                        valid = valid && extractGraphxy(evalContext, nonempty, span, mode);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, nonempty, span, mode);
                    } else if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(evalContext, nonempty, span, mode);
                    } else {
                        elem.logError("The " + childTag + " element is not valid within nonwrap span.");
                        valid = false;
                    }
                } else if (childElem instanceof final EmptyElement empty) {
                    if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(empty, span);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, empty, span, mode);
                    } else if (SYMBOL_PALETTE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractSymbolPalette(empty, span);
                    } else if (ALIGN_MARK.equalsIgnoreCase(childTag)) {
                        valid = valid && extractAlignMark(span);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(empty, span);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(empty, span);
                    } else {
                        elem.logError("An empty " + childTag + " element is not valid within nonwrap span.");
                        valid = false;
                    }
                } else {
                    elem.logError("Found " + child.getClass().getSimpleName() + " in nonwrap span.");
                    valid = false;
                }
            }
        }

        if (valid) {
            container.add(span);
        }

        return valid;
    }

    /**
     * Generate a {@code DocNonwrappingSpan} object from XML source. Any errors encountered while generating the span
     * object will be reflected in the source file's error list.
     *
     * @param evalContext        the evaluation context
     * @param name               the name of the span
     * @param elem               the element
     * @param container          the span to which to add this non-wrapping span
     * @param containerIsNonwrap true if the container is the non-wrapping span and the contents should be added to it;
     *                           false to create a new non-wrapping span and load it, then add it to {@code container}
     * @param mode               the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractNonwrap(final EvalContext evalContext, final String name,
                                          final NonemptyElement elem, final AbstractDocSpanBase container,
                                          final boolean containerIsNonwrap, final EParserMode mode) {

        final DocNonwrappingSpan span = containerIsNonwrap ? (DocNonwrappingSpan) container : new DocNonwrappingSpan();
        if (!containerIsNonwrap) {
            span.tag = name;
        }

        boolean valid = extractFormattable(elem, span);

        final Color bg;
        final String bgColorStr = elem.getStringAttr(BGCOLOR);
        if (bgColorStr != null) {
            span.backgroundColorName = bgColorStr;

            if (ColorNames.isColorNameValid(bgColorStr)) {
                bg = ColorNames.getColor(bgColorStr);
                span.backgroundColor = bg;
            } else {
                elem.logError("Invalid color specified for bgcolor.");
                valid = false;
            }
        }

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final CData cdata) {
                valid = valid && extractNonwrapText(cdata, span, mode);
            } else if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (NONWRAP.equalsIgnoreCase(childTag)) {
                        valid = valid && extractNonwrap(evalContext, childTag, nonempty, span, false, mode);
                    } else if (MATH.equalsIgnoreCase(childTag)) {
                        valid = valid && extractMath(evalContext, childTag, nonempty, span, false, mode);
                    } else if (FRACTION.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFraction(evalContext, nonempty, span, mode);
                    } else if (RADICAL.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRadical(evalContext, nonempty, span, mode);
                    } else if (REL_OFFSET.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRelOffset(evalContext, nonempty, span, mode);
                    } else if (FENCE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFence(evalContext, nonempty, span, mode);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(evalContext, nonempty, span, mode);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(evalContext, nonempty, span, mode);
                    } else if (TABLE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTable(evalContext, nonempty, span, mode);
                    } else if (DRAWING.equalsIgnoreCase(childTag)) {
                        valid = valid && extractDrawing(evalContext, nonempty, span, mode);
                    } else if (GRAPH_XY.equalsIgnoreCase(childTag)) {
                        valid = valid && extractGraphxy(evalContext, nonempty, span, mode);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, nonempty, span, mode);
                    } else if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(evalContext, nonempty, span, mode);
                    } else {
                        elem.logError("The " + childTag + " element is not valid within nonwrap span.");
                        valid = false;
                    }
                } else if (child instanceof final EmptyElement empty) {
                    if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(empty, span);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, empty, span, mode);
                    } else if (SYMBOL_PALETTE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractSymbolPalette(empty, span);
                    } else if (ALIGN_MARK.equalsIgnoreCase(childTag)) {
                        valid = valid && extractAlignMark(span);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(empty, span);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(empty, span);
                    } else {
                        elem.logError("An empty " + childTag + " element is not valid within nonwrap span.");
                        valid = false;
                    }
                } else {
                    elem.logError("Found " + child.getClass().getSimpleName() + " in nonwrap span.");
                    valid = false;
                }
            }
        }

        if (valid && !containerIsNonwrap) {
            container.add(span);
        }

        return valid;
    }

    /**
     * Generate a {@code DocMathSpan} object from XML source. Any errors encountered while generating the span object
     * will be reflected in the source file's error list.
     *
     * @param evalContext        the evaluation context
     * @param name               the name of the span
     * @param elem               the element
     * @param container          the span to which to add this non-wrapping span
     * @param containerIsNonwrap true if the container is the non-wrapping span and the contents should be added to it;
     *                           false to create a new non-wrapping span and load it, then add it to {@code container}
     * @param mode               the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractMath(final EvalContext evalContext, final String name,
                                       final NonemptyElement elem, final AbstractDocSpanBase container,
                                       final boolean containerIsNonwrap, final EParserMode mode) {

        final DocMathSpan span = containerIsNonwrap ? (DocMathSpan) container : new DocMathSpan();
        span.setColorName(DocMathSpan.MATH_COLOR_NAME);

        span.tag = name;

        boolean valid = extractFormattable(elem, span);

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final CData cdata) {
                valid = valid && extractNonwrapText(cdata, span, mode);
            } else if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (NONWRAP.equalsIgnoreCase(childTag)) {
                        valid = valid && extractNonwrap(evalContext, childTag, nonempty, span, false, mode);
                    } else if (MATH.equalsIgnoreCase(childTag)) {
                        valid = valid && extractMath(evalContext, childTag, nonempty, span, false, mode);
                    } else if (FRACTION.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFraction(evalContext, nonempty, span, mode);
                    } else if (RADICAL.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRadical(evalContext, nonempty, span, mode);
                    } else if (REL_OFFSET.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRelOffset(evalContext, nonempty, span, mode);
                    } else if (FENCE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFence(evalContext, nonempty, span, mode);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(evalContext, nonempty, span, mode);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(evalContext, nonempty, span, mode);
                    } else if (TABLE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTable(evalContext, nonempty, span, mode);
                    } else if (DRAWING.equalsIgnoreCase(childTag)) {
                        valid = valid && extractDrawing(evalContext, nonempty, span, mode);
                    } else if (GRAPH_XY.equalsIgnoreCase(childTag)) {
                        valid = valid && extractGraphxy(evalContext, nonempty, span, mode);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, nonempty, span, mode);
                    } else if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(evalContext, nonempty, span, mode);
                    } else {
                        elem.logError("The " + childTag + " element is not valid within math.");
                        valid = false;
                    }
                } else if (child instanceof final EmptyElement empty) {
                    if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(empty, span);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, empty, span, mode);
                    } else if (ALIGN_MARK.equalsIgnoreCase(childTag)) {
                        valid = valid && extractAlignMark(span);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(empty, span);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(empty, span);
                    } else {
                        elem.logError("An empty " + childTag + " element is not valid within math.");
                        valid = false;
                    }
                } else {
                    elem.logError("Found " + child.getClass().getSimpleName() + " in math.");
                    valid = false;
                }
            }
        }

        if (valid && !containerIsNonwrap) {
            container.add(span);
        }

        return valid;
    }

    /**
     * Generate a {@code DocFraction} object from XML source. Any errors encountered while generating the fraction
     * object will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this fraction
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractFraction(final EvalContext evalContext, final NonemptyElement elem,
                                           final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        DocNonwrappingSpan num = null;
        DocNonwrappingSpan den = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (NUMERATOR.equalsIgnoreCase(childTag)) {
                        if (num == null) {
                            num = new DocNonwrappingSpan();
                            valid = valid && extractNonwrap(evalContext, childTag, nonempty, num, true, mode);
                        } else {
                            elem.logError("Multiple <numerator> tags in fraction.");
                            valid = false;
                        }
                    } else if (DENOMINATOR.equalsIgnoreCase(childTag)) {
                        if (den == null) {
                            den = new DocNonwrappingSpan();
                            valid = valid && extractNonwrap(evalContext, childTag, nonempty, den, true, mode);
                        } else {
                            elem.logError("Multiple <denominator> tags in fraction.");
                            valid = false;
                        }
                    } else {
                        elem.logError("The " + childTag + " tag is not valid within fraction.");
                        valid = false;
                    }
                } else {
                    elem.logError("Found " + child.getClass().getSimpleName() + " in fraction.");
                    valid = false;
                }
            }
        }

        if (num == null || den == null) {
            elem.logError("&lt;fraction&gt; must have both &lt;numerator&gt; and &lt;denominator&gt; child.");
            valid = false;
        } else if (valid) {
            final DocFraction fraction = new DocFraction(num, den);

            valid = extractFormattable(elem, fraction);

            if (valid) {
                container.add(fraction);
            }
        }

        return valid;
    }

    /**
     * Generate a {@code DocRadical} object from XML source. Any errors encountered while generating the radical object
     * will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this radical
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractRadical(final EvalContext evalContext, final NonemptyElement elem,
                                          final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        DocNonwrappingSpan base = null;
        DocNonwrappingSpan root = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (BASE.equalsIgnoreCase(childTag)) {
                        if (base == null) {
                            base = new DocNonwrappingSpan();
                            base.tag = childTag;
                            valid = valid && extractNonwrap(evalContext, childTag, nonempty, base, true, mode);
                        } else {
                            elem.logError("Multiple <base> tags in radical.");
                            valid = false;
                        }
                    } else if (ROOT.equalsIgnoreCase(childTag)) {
                        if (root == null) {
                            root = new DocNonwrappingSpan();
                            root.tag = childTag;
                            valid = valid && extractNonwrap(evalContext, childTag, nonempty, root, true, mode);

                            if (valid && (base == null || (root.getFontSize() == base.getFontSize()
                                    && base.getFontSize() > 8))) {
                                root.setFontScale(0.75f);
                            }
                        } else {
                            elem.logError("Multiple &lt;root&gt; tags in radical.");
                            valid = false;
                        }
                    } else {
                        elem.logError("The " + childTag + " tag is not valid within radical.");
                        valid = false;
                    }
                } else {
                    elem.logError("Found " + child.getClass().getSimpleName() + " in radical.");
                    valid = false;
                }
            }
        }

        if (base == null) {
            elem.logError("&lt;radical&gt; must have &lt;base&gt; child.");
            valid = false;
        }

        if (valid) {
            final DocRadical rad = new DocRadical(base, root);

            valid = extractFormattable(elem, rad);

            if (valid) {
                container.add(rad);
            }
        }

        return valid;
    }

    /**
     * Generate a {@code DocRelativeOffset} object from XML source. Any errors encountered while generating the relative
     * offset object will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this relative offset
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractRelOffset(final EvalContext evalContext, final NonemptyElement elem,
                                            final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        AbstractDocObjectTemplate base = null;
        AbstractDocObjectTemplate sup = null;
        AbstractDocObjectTemplate sub = null;
        AbstractDocObjectTemplate over = null;
        AbstractDocObjectTemplate under = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (BASE.equalsIgnoreCase(childTag)) {
                        if (base == null) {
                            final DocNonwrappingSpan span = new DocNonwrappingSpan();
                            valid = extractNonwrap(evalContext, childTag, nonempty, span, false, mode);

                            if (valid && span.getChildren() != null && !span.getChildren().isEmpty()) {
                                base = span.getChildren().getFirst();
                            } else {
                                elem.logError("Failed to parse &lt;base&gt; content from '" + nonempty.print(0) + "'");
                            }
                        } else {
                            elem.logError("Multiple &lt;base&gt; tags in &lt;rel-offset&gt; element.");
                            valid = false;
                        }
                    } else if (SUPER.equalsIgnoreCase(childTag)) {
                        if (sup == null) {
                            final DocNonwrappingSpan span = new DocNonwrappingSpan();

                            sup = new DocNonwrappingSpan();
                            valid = extractNonwrap(evalContext, childTag, nonempty, span, false, mode);

                            if (valid) {
                                if (span.getChildren() != null) {
                                    sup = span.getChildren().getFirst();
                                }

                                if (base != null && sup.getFontSize() == base.getFontSize() && base.getFontSize() > 8) {
                                    sup.setFontScale(0.75f);
                                }
                            }
                        } else {
                            elem.logError("Multiple &lt;super&gt; tags in &lt;rel-offset&gt; element.");
                            valid = false;
                        }
                    } else if (SUB.equalsIgnoreCase(childTag)) {
                        if (sub == null) {
                            final DocNonwrappingSpan span = new DocNonwrappingSpan();

                            sub = new DocNonwrappingSpan();
                            valid = extractNonwrap(evalContext, childTag, nonempty, span, false, mode);

                            if (valid) {
                                if (span.getChildren() != null) {
                                    sub = span.getChildren().getFirst();
                                }

                                if (base != null && sub.getFontSize() == base.getFontSize() && base.getFontSize() > 8) {
                                    sub.setFontScale(0.75f);
                                }
                            }
                        } else {
                            elem.logError("Multiple &lt;sub&gt; tags in &lt;rel-offset&gt; element.");
                            valid = false;
                        }
                    } else if (OVER.equalsIgnoreCase(childTag)) {
                        if (over == null) {
                            final DocNonwrappingSpan span = new DocNonwrappingSpan();

                            over = new DocNonwrappingSpan();
                            valid = extractNonwrap(evalContext, childTag, nonempty, span, false, mode);

                            if (valid) {
                                if (span.getChildren() != null) {
                                    over = span.getChildren().getFirst();
                                }

                                if (base != null && over.getFontSize() == base.getFontSize()
                                        && base.getFontSize() > 8) {
                                    over.setFontScale(0.75f);
                                }
                            }
                        } else {
                            elem.logError("Multiple &lt;over&gt; tags in &lt;rel-offset&gt; element.");
                            valid = false;
                        }
                    } else if (UNDER.equalsIgnoreCase(childTag)) {
                        if (under == null) {
                            final DocNonwrappingSpan span = new DocNonwrappingSpan();

                            under = new DocNonwrappingSpan();
                            valid = extractNonwrap(evalContext, childTag, nonempty, span, false, mode);

                            if (valid) {
                                if (span.getChildren() != null) {
                                    under = span.getChildren().getFirst();
                                }

                                if (base != null && under.getFontSize() == base.getFontSize()
                                        && base.getFontSize() > 8) {
                                    under.setFontScale(0.75f);
                                }
                            }
                        } else {
                            elem.logError("Multiple &lt;under&gt; tags in &lt;rel-offset&gt; element.");
                            valid = false;
                        }
                    } else {
                        elem.logError("The " + childTag + " tag is not valid within rel-offset.");
                        valid = false;
                    }
                }
            }
        }

        if (base == null) {
            elem.logError("&lt;rel-offset&gt; must have &lt;base&gt; child.");
            valid = false;
        }

        if (valid) {
            final DocRelativeOffset rel = new DocRelativeOffset(base, sup, sub, over, under);

            valid = extractFormattable(elem, rel);

            if (valid) {
                container.add(rel);
            }
        }

        return valid;
    }

    /**
     * Generate a {@code DocFence} object from XML source. Any errors encountered while generating the span object will
     * be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this fence
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractFence(final EvalContext evalContext, final NonemptyElement elem,
                                        final AbstractDocSpanBase container, final EParserMode mode) {

        final DocFence fence = new DocFence();

        boolean valid = extractFormattable(elem, fence);

        final String typeStr = elem.getStringAttr(TYPE);
        if (typeStr != null) {
            if (PARENTHESES.equalsIgnoreCase(typeStr)) {
                fence.type = DocFence.PARENTHESES;
            } else if (BRACKETS.equalsIgnoreCase(typeStr)) {
                fence.type = DocFence.BRACKETS;
            } else if (BRACES.equalsIgnoreCase(typeStr)) {
                fence.type = DocFence.BRACES;
            } else if (BARS.equalsIgnoreCase(typeStr)) {
                fence.type = DocFence.BARS;
            } else if (LBRACE.equalsIgnoreCase(typeStr)) {
                fence.type = DocFence.LBRACE;
            } else {
                elem.logError(
                        "Invalid fence type (should be 'parentheses', 'brackets', 'bars', 'braces', or 'lbrace').");
                valid = false;
            }
        }

        final String valignStr = elem.getStringAttr(VALIGN);
        if (valignStr != null) {
            if (CENTER.equals(valignStr)) {
                fence.setLeftAlign(EVAlign.CENTER);
            } else if (BASELINE.equals(valignStr)) {
                fence.setLeftAlign(EVAlign.BASELINE);
            } else {
                elem.logError("Invalid fence valign setting (should be 'center', 'baseline').");
                valid = false;
            }
        }

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final CData cdata) {
                extractText(cdata, fence, mode);
            } else if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (SPAN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractWrappingSpan(evalContext, childTag, nonempty, fence, mode);
                    } else if (NONWRAP.equalsIgnoreCase(childTag)) {
                        valid = valid && extractNonwrap(evalContext, childTag, nonempty, fence, false, mode);
                    } else if (MATH.equalsIgnoreCase(childTag)) {
                        valid = valid && extractMath(evalContext, childTag, nonempty, fence, false, mode);
                    } else if (FRACTION.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFraction(evalContext, nonempty, fence, mode);
                    } else if (RADICAL.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRadical(evalContext, nonempty, fence, mode);
                    } else if (REL_OFFSET.equalsIgnoreCase(childTag)) {
                        valid = valid && extractRelOffset(evalContext, nonempty, fence, mode);
                    } else if (FENCE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractFence(evalContext, nonempty, fence, mode);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(evalContext, nonempty, fence, mode);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(evalContext, nonempty, fence, mode);
                    } else if (TABLE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTable(evalContext, nonempty, fence, mode);
                    } else if (DRAWING.equalsIgnoreCase(childTag)) {
                        valid = valid && extractDrawing(evalContext, nonempty, fence, mode);
                    } else if (GRAPH_XY.equalsIgnoreCase(childTag)) {
                        valid = valid && extractGraphxy(evalContext, nonempty, fence, mode);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, nonempty, fence, mode);
                    } else if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(evalContext, nonempty, fence, mode);
                    } else {
                        elem.logError("The " + childTag + " element is not valid within fence.");
                        valid = false;
                    }
                } else if (child instanceof final EmptyElement empty) {
                    if (IMAGE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractImage(empty, fence);
                    } else if (INPUT.equalsIgnoreCase(childTag)) {
                        valid = valid && extractInput(evalContext, empty, fence, mode);
                    } else if (ALIGN_MARK.equalsIgnoreCase(childTag)) {
                        valid = valid && extractAlignMark(fence);
                    } else if (H_SPACE.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHSpace(empty, fence);
                    } else if (H_ALIGN.equalsIgnoreCase(childTag)) {
                        valid = valid && extractHAlign(empty, fence);
                    } else {
                        elem.logError("An empty " + childTag + " element is not valid within fence.");
                        valid = false;
                    }
                }
            }
        }

        if (valid) {
            container.add(fence);
        }

        return valid;
    }

    /**
     * Generate a {@code DocHSpace} object from XML source. Any errors encountered while generating the input object
     * will be reflected in the source file's error context.
     *
     * @param elem      the element
     * @param container the span to which to add this input
     * @return true if loading successful; false otherwise
     */
    private static boolean extractHSpace(final EmptyElement elem, final AbstractDocSpanBase container) {

        boolean valid = true;

        final String widthStr = elem.getStringAttr(WIDTH);
        Number widthC = null;

        if (widthStr != null) {
            try {
                widthC = NumberParser.parse(widthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'width' attribute value (must be a valid number).");
                valid = false;
            }
        }

        if (valid) {
            final DocHSpace hSpace = new DocHSpace();

            NumberOrFormula width = null;
            if (widthC != null) {
                width = new NumberOrFormula(widthC);
            }
            hSpace.setSpaceWidth(width);

            container.add(hSpace);
        }

        return valid;
    }

    /**
     * Generate a {@code DocHSpace} object from XML source. Any errors encountered while generating the input object
     * will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractHSpace(final EvalContext evalContext, final NonemptyElement elem,
                                         final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        final String widthStr = elem.getStringAttr(WIDTH);
        Number widthC = null;
        Formula widthF = null;

        if (widthStr != null) {
            try {
                widthC = NumberParser.parse(widthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'width' attribute value (must be a valid number).");
                valid = false;
            }
        }

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (WIDTH.equalsIgnoreCase(childTag)) {
                    if (widthC == null) {
                        if (widthF == null) {
                            widthF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (widthF == null) {
                                elem.logError("Invalid 'width' formula.");
                                valid = false;
                            }
                        } else {
                            elem.logError("Cannot have multiple width formulas.");
                            valid = false;
                        }
                    } else {
                        elem.logError("Cannot have both width attribute and width formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within h-space.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in h-space.");
                valid = false;
            }
        }

        if (valid) {
            final DocHSpace hSpace = new DocHSpace();

            NumberOrFormula width = null;
            if (widthC != null) {
                width = new NumberOrFormula(widthC);
            } else if (widthF != null) {
                width = new NumberOrFormula(widthF);
            }

            hSpace.setSpaceWidth(width);
            container.add(hSpace);
        }

        return valid;
    }

    /**
     * Generate a {@code DocHAlign} object from XML source. Any errors encountered while generating the input object
     * will be reflected in the source file's error context.
     *
     * @param elem      the element
     * @param container the span to which to add this input
     * @return true if loading successful; false otherwise
     */
    private static boolean extractHAlign(final EmptyElement elem, final AbstractDocSpanBase container) {

        boolean valid = true;

        final String positionStr = elem.getStringAttr(POSITION);
        Number positionC = null;

        if (positionStr != null) {
            try {
                positionC = NumberParser.parse(positionStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'position' attribute value (must be a valid number).");
                valid = false;
            }
        }

        if (valid) {
            final DocHAlign hAlign = new DocHAlign();

            NumberOrFormula position = null;
            if (positionC != null) {
                position = new NumberOrFormula(positionC);
            }
            hAlign.setPosition(position);

            container.add(hAlign);
        }

        return valid;
    }

    /**
     * Generate a {@code DocHAlign} object from XML source. Any errors encountered while generating the input object
     * will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractHAlign(final EvalContext evalContext, final NonemptyElement elem,
                                         final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        final String positionStr = elem.getStringAttr(POSITION);
        Number positionC = null;
        Formula positionF = null;

        if (positionStr != null) {
            try {
                positionC = NumberParser.parse(positionStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'position' attribute value (must be a valid number).");
                valid = false;
            }
        }

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (POSITION.equalsIgnoreCase(childTag)) {
                    if (positionC == null) {
                        if (positionF == null) {
                            positionF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (positionF == null) {
                                elem.logError("Invalid 'position' formula.");
                                valid = false;
                            }
                        } else {
                            elem.logError("Cannot have multiple position formulas.");
                            valid = false;
                        }
                    } else {
                        elem.logError("Cannot have both position attribute and position formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within h-align.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in h-align.");
                valid = false;
            }
        }

        if (valid) {
            final DocHAlign hAlign = new DocHAlign();

            NumberOrFormula position = null;
            if (positionC != null) {
                position = new NumberOrFormula(positionC);
            } else if (positionF != null) {
                position = new NumberOrFormula(positionF);
            }

            hAlign.setPosition(position);
            container.add(hAlign);
        }

        return valid;
    }

    /**
     * Generate a {@code DocAlignMark} object from XML source. Any errors encountered while generating the input object
     * will be reflected in the source file's error context.
     *
     * @param container the span to which to add this input
     * @return true if loading successful; false otherwise
     */
    private static boolean extractAlignMark(final AbstractDocSpanBase container) {

        container.add(new DocAlignmentMark());

        return true;
    }

    /**
     * Generate a {@code DocTable} object from XML source. Any errors encountered while generating the table object will
     * be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this table
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractTable(final EvalContext evalContext, final NonemptyElement elem,
                                        final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        // First, we have to build the table data. This can be either a set of strings, or a set of
        // <code>DocObjects</code>. If at least one item in the table is not a string, then it will
        // be composed of <code>DocObject</code>s.

        // Extract rows
        final List<List<DocNonwrappingSpan>> rows = new ArrayList<>(5);
        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (TR.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTableRow(evalContext, nonempty, rows, mode);
                    } else {
                        elem.logError("The " + childTag + " tag is not valid within table.");
                        valid = false;
                    }
                }
            }
        }

        // Now construct the contents arrays and build the DocTable object
        if (valid) {
            final DocNonwrappingSpan[][] objects = new DocNonwrappingSpan[rows.size()][];

            final int numObjects = objects.length;
            for (int i = 0; i < numObjects; ++i) {
                final List<DocNonwrappingSpan> cells = rows.get(i);
                objects[i] = new DocNonwrappingSpan[cells.size()];

                final int innerLen = objects[i].length;
                for (int j = 0; j < innerLen; ++j) {
                    objects[i][j] = cells.get(j);
                }
            }

            final DocTable table = new DocTable(objects);

            final String boxWidthStr = elem.getStringAttr(BOX_WIDTH);
            if (boxWidthStr != null) {
                try {
                    table.boxWidth = Integer.parseInt(boxWidthStr);
                } catch (final NumberFormatException e) {
                    elem.logError("Box width must be integer");
                    valid = false;
                }
            }

            final String vLineWidthStr = elem.getStringAttr(V_LINE_WIDTH);
            if (vLineWidthStr != null) {
                try {
                    table.vLineWidth = Integer.parseInt(vLineWidthStr);
                } catch (final NumberFormatException e) {
                    elem.logError("Vertical Line width must be integer");
                    valid = false;
                }
            }

            final String hLineWidthStr = elem.getStringAttr(H_LINE_WIDTH);
            if (hLineWidthStr != null) {
                try {
                    table.hLineWidth = Integer.parseInt(hLineWidthStr);
                } catch (final NumberFormatException e) {
                    elem.logError("Horizontal Line width must be integer");
                    valid = false;
                }
            }

            final String columnWidthStr = elem.getStringAttr(COLUMN_WIDTH);
            if (columnWidthStr != null) {
                if (UNIFORM.equalsIgnoreCase(columnWidthStr)) {
                    table.setSpacing(DocTable.UNIFORM);
                } else if (NONUNIFORM.equalsIgnoreCase(columnWidthStr)) {
                    table.setSpacing(DocTable.NONUNIFORM);
                } else {
                    elem.logError("Invalid column width, use 'uniform' or 'nonuniform'");
                    valid = false;
                }
            }

            final String justificationStr = elem.getStringAttr(JUSTIFICATION);
            if (justificationStr != null) {
                if (LEFT.equalsIgnoreCase(justificationStr)) {
                    table.setJustification(DocTable.LEFT);
                } else if (RIGHT.equalsIgnoreCase(justificationStr)) {
                    table.setJustification(DocTable.RIGHT);
                } else if (CENTER.equalsIgnoreCase(justificationStr)) {
                    table.setJustification(DocTable.CENTER);
                } else {
                    elem.logError("Invalid justification, use 'left', 'right', or 'center'");
                    valid = false;
                }
            }

            final String bgColorStr = elem.getStringAttr(BGCOLOR);
            if (bgColorStr != null) {
                if (ColorNames.isColorNameValid(bgColorStr)) {
                    table.setBackgroundColor(bgColorStr, ColorNames.getColor(bgColorStr));
                } else {
                    elem.logError("Invalid color specified for bgcolor.");
                    valid = false;
                }
            }

            final String cellMarginsStr = elem.getStringAttr(CELL_MARGINS);
            if (cellMarginsStr != null) {
                if (cellMarginsStr.indexOf((int) CoreConstants.COMMA_CHAR) == -1) {
                    try {
                        final int size = Integer.parseInt(cellMarginsStr);
                        table.cellInsets = new Insets(size, size, size, size);
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid cell margin specification.");
                        valid = false;
                    }
                } else {
                    final String[] splits = cellMarginsStr.split(CoreConstants.COMMA);

                    if (splits.length == 4) {
                        final int[] sizes = new int[4];

                        for (int i = 0; i < 4; ++i) {
                            try {
                                sizes[i] = Integer.parseInt(splits[i].trim());
                            } catch (final NumberFormatException e) {
                                elem.logError("Invalid cell margin specification.");
                                valid = false;
                            }
                        }
                        if (valid) {
                            table.cellInsets = new Insets(sizes[0], sizes[1], sizes[2], sizes[3]);
                        }
                    } else {
                        elem.logError("Invalid cell margin specification.");
                        valid = false;
                    }
                }
            }

            valid = valid && extractFormattable(elem, table);

            if (valid) {
                container.add(table);
            }
        }

        return valid;
    }

    /**
     * Parse a row element within a table to extract its cell contents. Any errors encountered while generating the
     * table object will be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param rows        the {@code ArrayList} to which to append the rows of data. Each row will be an
     *                    {@code ArrayList} of cell data
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractTableRow(final EvalContext evalContext, final NonemptyElement elem,
                                           final Collection<? super List<DocNonwrappingSpan>> rows,
                                           final EParserMode mode) {

        boolean valid = true;

        // Extract all <td> tags in the table
        final List<DocNonwrappingSpan> cells = new ArrayList<>(5);

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final IElement childElem) {
                final String childTag = childElem.getTagName();

                if (child instanceof final NonemptyElement nonempty) {
                    if (TD.equalsIgnoreCase(childTag)) {
                        valid = valid && extractTableCell(evalContext, nonempty, cells, mode);
                    } else {
                        elem.logError("The " + childTag + " tag is not valid within table row.");
                        valid = false;
                    }
                }
            }
        }

        if (valid) {
            rows.add(cells);
        }

        return valid;
    }

    /**
     * Parse a table cell element within a table to extract its cell contents. Any errors encountered while generating
     * the table object will be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param cells       the {@code ArrayList} to which to append the cells of data
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractTableCell(final EvalContext evalContext, final NonemptyElement elem,
                                            final Collection<? super DocNonwrappingSpan> cells,
                                            final EParserMode mode) {

        boolean valid = true;

        int which = 0xFFFF;

        final String linesStr = elem.getStringAttr(LINES);
        if (linesStr != null) {
            which = 0;

            if (!linesStr.isBlank()) {
                final String[] values = linesStr.split(CoreConstants.COMMA);
                for (final String value : values) {
                    if (value.isBlank()) {
                        continue;
                    }

                    if (LEFT.equalsIgnoreCase(value)) {
                        which |= DocTable.LEFTLINE;
                    } else if (RIGHT.equalsIgnoreCase(value)) {
                        which |= DocTable.RIGHTLINE;
                    } else if (TOP.equalsIgnoreCase(value)) {
                        which |= DocTable.TOPLINE;
                    } else if (BOTTOM.equalsIgnoreCase(value)) {
                        which |= DocTable.BOTTOMLINE;
                    } else {
                        elem.logError("Invalid table cell line position, use a comma-separated list of 'left', " +
                                "'right', 'top' and 'bottom'");
                        valid = false;
                    }
                }
            }
        }

        final DocNonwrappingSpan obj = new DocNonwrappingSpan();
        obj.tag = TD;
        obj.outlines = which;

        if (valid) {
            valid = extractNonwrap(evalContext, TD, elem, obj, true, mode);
            if (valid) {
                cells.add(obj);
            }
        }

        return valid;
    }

    /**
     * Generate a {@code DocDrawing} object from XML source. Any errors encountered while generating the drawing object
     * will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this radical
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractDrawing(final EvalContext evalContext, final NonemptyElement elem,
                                          final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        final String widthStr = elem.getStringAttr(WIDTH);
        int width = 0;
        if (widthStr != null) {
            try {
                width = Integer.parseInt(widthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'width' attribute value (must be an integer).");
                valid = false;
            }
        }

        final String heightStr = elem.getStringAttr(HEIGHT);
        int height = 0;
        if (heightStr != null) {
            try {
                height = Integer.parseInt(heightStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'height' attribute value (must be an integer).");
                valid = false;
            }
        }

        final String valignStr = elem.getStringAttr(VALIGN);
        EVAlign valign = null;
        if (valignStr != null) {
            if (BASELINE.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.BASELINE;
            } else if (CENTER.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.CENTER;
            } else if (TOP.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.TOP;
            } else {
                elem.logError("<image> element has invalid value in '' attribute.");
            }
        }


        final String altStr = elem.getStringAttr(ALT);

        final DocDrawing drawing = new DocDrawing(width, height, altStr);

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (LINE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveLine(evalContext, nonempty, drawing, mode);
                } else if (ARC.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveArc(evalContext, nonempty, drawing, mode);
                } else if (OVAL.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveOval(evalContext, nonempty, drawing, mode);
                } else if (RECTANGLE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRectangle(evalContext, nonempty, drawing, mode);
                } else if (POLYGON.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitivePolygon(evalContext, nonempty, drawing, mode);
                } else if (PROTRACTOR.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveProtractor(evalContext, nonempty, drawing, mode);
                } else if (RASTER.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRaster(evalContext, nonempty, drawing, mode);
                } else if (TEXT.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveText(evalContext, nonempty, drawing, mode);
                } else if (SPAN.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveSpan(evalContext, nonempty, drawing, mode);
                } else if (WIDTH.equals(childTag)) {
                    final Formula theWidth = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (theWidth == null) {
                        elem.logError("Invalid 'width' formula.");
                        valid = false;
                    } else {
                        drawing.setWidthFormula(theWidth);
                    }
                } else if (HEIGHT.equals(childTag)) {
                    final Formula theHeight = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (theHeight == null) {
                        elem.logError("Invalid 'height' formula.");
                        valid = false;
                    } else {
                        drawing.setHeightFormula(theHeight);
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within drawing.");
                    valid = false;
                }
            } else if (child instanceof final EmptyElement empty) {
                final String childTag = empty.getTagName();

                if (LINE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveLine(evalContext, empty, drawing, mode);
                } else if (ARC.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveArc(evalContext, empty, drawing, mode);
                } else if (OVAL.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveOval(evalContext, empty, drawing, mode);
                } else if (RECTANGLE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRectangle(evalContext, empty, drawing, mode);
                } else if (POLYGON.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitivePolygon(evalContext, empty, drawing, mode);
                } else if (PROTRACTOR.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveProtractor(evalContext, empty, drawing, mode);
                } else if (RASTER.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRaster(evalContext, empty, drawing, mode);
                } else if (TEXT.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveText(evalContext, empty, drawing, mode);
                } else if (SPAN.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveSpan(evalContext, empty, drawing, mode);
                } else {
                    elem.logError("Empty " + childTag + " tag is not valid within drawing.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in Drawing.");
                valid = false;
            }
        }

        valid = valid && extractFormattable(elem, drawing);

        if (valid) {
            drawing.setLeftAlign(valign);
            container.add(drawing);
        }

        return valid;
    }

    /**
     * Generate a {@code DocGraphXY} object from XML source. Any errors encountered while generating the graph object
     * will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this graph
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractGraphxy(final EvalContext evalContext, final NonemptyElement elem,
                                          final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        final String widthStr = elem.getStringAttr(WIDTH);
        final String heightStr = elem.getStringAttr(HEIGHT);
        final String altStr = elem.getStringAttr(ALT);
        final String minxStr = elem.getStringAttr(MIN_X);
        final String minyStr = elem.getStringAttr(MIN_Y);
        final String maxxStr = elem.getStringAttr(MAX_X);
        final String maxyStr = elem.getStringAttr(MAX_Y);
        final String xtickintervalStr = elem.getStringAttr(X_TICK_INTERVAL);
        final String ytickintervalStr = elem.getStringAttr(Y_TICK_INTERVAL);
        final String bgcolorStr = elem.getStringAttr(BGCOLOR);
        final String bordercolorStr = elem.getStringAttr(BORDER_COLOR);
        final String gridcolorStr = elem.getStringAttr(GRID_COLOR);
        final String tickcolorStr = elem.getStringAttr(TICK_COLOR);
        final String axiscolorStr = elem.getStringAttr(AXIS_COLOR);
        final String borderwidthStr = elem.getStringAttr(BORDER_WIDTH);
        final String gridwidthStr = elem.getStringAttr(GRID_WIDTH);
        final String tickwidthStr = elem.getStringAttr(TICK_WIDTH);
        final String ticksizeStr = elem.getStringAttr(TICK_SIZE);
        final String axiswidthStr = elem.getStringAttr(AXIS_WIDTH);
        final String axislabelfontsizeStr = elem.getStringAttr(AXIS_LABEL_FONT_SIZE);
        final String ticklabelfontsizeStr = elem.getStringAttr(TICK_LABEL_FONT_SIZE);
        final String xaxislabelStr = elem.getStringAttr(X_AXIS_LABEL);
        final String yaxislabelStr = elem.getStringAttr(Y_AXIS_LABEL);
        final String valignStr = elem.getStringAttr(VALIGN);

        int width = 0;
        if (widthStr == null) {
            elem.logError("&lt;graphxy&gt; element missing required 'width' attribute.");
            valid = false;
        } else {
            try {
                width = Integer.parseInt(widthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'width' attribute value (must be an integer).");
                valid = false;
            }
        }

        int height = 0;
        if (heightStr == null) {
            elem.logError("&lt;graphxy&gt; element missing required 'height' attribute.");
            valid = false;
        } else {
            try {
                height = Integer.parseInt(heightStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'height' attribute value (must be an integer).");
                valid = false;
            }
        }

        final DocGraphXY graph = new DocGraphXY(width, height, altStr);

        valid = valid && extractFormattable(elem, graph);

        if (minxStr != null && minyStr != null && maxxStr != null && maxyStr != null) {
            try {
                final Number minx = NumberParser.parse(minxStr);
                final Number maxx = NumberParser.parse(maxxStr);
                final Number miny = NumberParser.parse(minyStr);
                final Number maxy = NumberParser.parse(maxyStr);
                graph.setWindow(minx, maxx, miny, maxy);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid window min or max attribute value (must be an integer).");
                valid = false;
            }
        } else if (minxStr != null || minyStr != null || maxxStr != null || maxyStr != null) {
            elem.logError("Incomplete window specification.");
            valid = false;
        }

        if (xtickintervalStr != null) {
            try {
                graph.xTickInterval = NumberParser.parse(xtickintervalStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'xtickinterval' attribute value (must be a number).");
                valid = false;
            }
        }

        if (ytickintervalStr != null) {
            try {
                graph.yTickInterval = NumberParser.parse(ytickintervalStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'ytickinterval' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (bgcolorStr != null) {
            if (ColorNames.isColorNameValid(bgcolorStr)) {
                final Color color = ColorNames.getColor(bgcolorStr);
                graph.setBackgroundColor(bgcolorStr, color);
            } else {
                elem.logError("Invalid 'bgcolor' color name.");
                valid = false;
            }
        }

        if (bordercolorStr != null) {
            if (ColorNames.isColorNameValid(bordercolorStr)) {
                final Color color = ColorNames.getColor(bordercolorStr);
                graph.setBorderColor(bordercolorStr, color);
            } else {
                elem.logError("Invalid 'bordercolor' color name.");
                valid = false;
            }
        }

        if (gridcolorStr != null) {
            if (ColorNames.isColorNameValid(gridcolorStr)) {
                final Color color = ColorNames.getColor(gridcolorStr);
                graph.setGridColor(gridcolorStr, color);
            } else {
                elem.logError("Invalid 'gridcolor' color name.");
                valid = false;
            }
        }

        if (tickcolorStr != null) {
            if (ColorNames.isColorNameValid(tickcolorStr)) {
                final Color color = ColorNames.getColor(tickcolorStr);
                graph.setTickColor(tickcolorStr, color);
            } else {
                elem.logError("Invalid 'tickcolor' color name.");
                valid = false;
            }
        }

        if (axiscolorStr != null) {
            if (ColorNames.isColorNameValid(axiscolorStr)) {
                final Color color = ColorNames.getColor(axiscolorStr);
                graph.setAxisColor(axiscolorStr, color);
            } else {
                elem.logError("Invalid 'axiscolor' color name.");
                valid = false;
            }
        }

        if (borderwidthStr != null) {
            try {
                graph.borderWidth = Integer.parseInt(borderwidthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'borderwidth' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (gridwidthStr != null) {
            try {
                graph.gridWidth = Integer.parseInt(gridwidthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'gridwidth' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (tickwidthStr != null) {
            try {
                graph.tickWidth = Integer.parseInt(tickwidthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'tickwidth' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (ticksizeStr != null) {
            try {
                graph.tickSize = Integer.parseInt(ticksizeStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'ticksize' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (axiswidthStr != null) {
            try {
                graph.axisWidth = Integer.parseInt(axiswidthStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'axiswidth' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (axislabelfontsizeStr != null) {
            try {
                graph.axisLabelSize = Integer.parseInt(axislabelfontsizeStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'axislabelfontsize' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (ticklabelfontsizeStr != null) {
            try {
                graph.tickLabelSize = Integer.parseInt(ticklabelfontsizeStr);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'ticklabelfontsize' attribute value (must be an integer).");
                valid = false;
            }
        }

        if (xaxislabelStr != null) {
            graph.xAxisLabel = xaxislabelStr;
        }

        if (yaxislabelStr != null) {
            graph.yAxisLabel = yaxislabelStr;
        }

        EVAlign valign = null;
        if (valignStr != null) {
            if (BASELINE.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.BASELINE;
            } else if (CENTER.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.CENTER;
            } else if (TOP.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.TOP;
            } else {
                elem.logError("<image> element has invalid value in '' attribute.");
            }
        }


        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (FORMULA.equalsIgnoreCase(childTag) || FUNCTION_PLOT.equalsIgnoreCase(childTag)) {
                    valid = valid && extractGraphFormula(evalContext, nonempty, graph, mode);
                } else if (LINE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveLine(evalContext, nonempty, graph, mode);
                } else if (ARC.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveArc(evalContext, nonempty, graph, mode);
                } else if (OVAL.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveOval(evalContext, nonempty, graph, mode);
                } else if (RECTANGLE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRectangle(evalContext, nonempty, graph, mode);
                } else if (POLYGON.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitivePolygon(evalContext, nonempty, graph, mode);
                } else if (PROTRACTOR.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveProtractor(evalContext, nonempty, graph, mode);
                } else if (RASTER.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRaster(evalContext, nonempty, graph, mode);
                } else if (TEXT.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveText(evalContext, nonempty, graph, mode);
                } else if (SPAN.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveSpan(evalContext, nonempty, graph, mode);
                } else {
                    elem.logError("The " + childTag + " tag is not valid within GraphXY.");
                    valid = false;
                }
            } else if (child instanceof final EmptyElement empty) {
                final String childTag = empty.getTagName();

                if (LINE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveLine(evalContext, empty, graph, mode);
                } else if (ARC.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveArc(evalContext, empty, graph, mode);
                } else if (OVAL.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveOval(evalContext, empty, graph, mode);
                } else if (RECTANGLE.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRectangle(evalContext, empty, graph, mode);
                } else if (POLYGON.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitivePolygon(evalContext, empty, graph, mode);
                } else if (PROTRACTOR.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveProtractor(evalContext, empty, graph, mode);
                } else if (RASTER.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveRaster(evalContext, empty, graph, mode);
                } else if (TEXT.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveText(evalContext, empty, graph, mode);
                } else if (SPAN.equalsIgnoreCase(childTag)) {
                    valid = valid && extractPrimitiveSpan(evalContext, empty, graph, mode);
                } else {
                    elem.logError("Empty " + childTag + " tag is not valid within graphxy.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in graphxy.");
                valid = false;
            }
        }

        if (valid) {
            graph.setLeftAlign(valign);
            container.add(graph);
        }

        return valid;
    }

    /**
     * Parse a formula specification from XML source and add the formula to an XY graph. Any errors encountered while
     * generating the graph object will be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param graph       the graph to which to add this formula
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractGraphFormula(final EvalContext evalContext, final NonemptyElement elem,
                                               final DocGraphXY graph, final EParserMode mode) {

        boolean valid = true;

        final String domainVarStr = elem.getStringAttr(DOMAIN_VAR);
        final String colorStr = elem.getStringAttr(COLOR);
        final String minxStr = elem.getStringAttr(MIN_X);
        final String maxxStr = elem.getStringAttr(MAX_X);

        Color color = Color.BLACK;
        if (colorStr != null) {
            if (ColorNames.isColorNameValid(colorStr)) {
                color = ColorNames.getColor(colorStr);
            } else {
                elem.logError("Unrecognized color value.");
                valid = false;
            }
        }

        final int style = DocPrimitiveFormula.CURVE;

        Formula minXF = null;
        Number minXC = null;
        if (minxStr != null) {
            try {
                minXC = NumberParser.parse(minxStr);
            } catch (final NumberFormatException ex) {
                minXF = FormulaFactory.parseFormulaString(evalContext, minxStr, mode);

                if (minXF == null) {
                    elem.logError("Failed to parse 'minx' attribute");
                    valid = false;
                } else if (mode.reportDeprecated) {
                    elem.logError("Deprecated formula in 'minx' in graph formula");
                }
            }
        }

        Formula maxXF = null;
        Number maxXC = null;
        if (maxxStr != null) {
            try {
                maxXC = NumberParser.parse(maxxStr);
            } catch (final NumberFormatException ex) {
                maxXF = FormulaFactory.parseFormulaString(evalContext, maxxStr, mode);

                if (maxXF == null) {
                    elem.logError("Failed to parse 'maxx' attribute");
                    valid = false;
                } else if (mode.reportDeprecated) {
                    elem.logError("Deprecated formula in 'maxx' in graph formula");
                }
            }
        }

        // The content of this element has evolved...

        // There are two old (deprecated) formats: (1) the content is a CDATA with the formula in text format, and
        // (2) the content is the XML contents of a single formula

        // The new format defines three child elements: <minx> (with an XML formula for domain lower bound), <maxx>
        // (with an XML formula for domain upper bound), and <expr> with the XML formula defining the function to be
        // graphed.

        Formula form = null;
        final int count = elem.getNumChildren();

        if (count == 1 && elem.getChild(0) instanceof final CData cdata) {
            // Deprecated format (1)
            if (mode.reportDeprecated) {
                elem.logError("Deprecated text-format expression in graph formula");
            }
            form = FormulaFactory.parseFormulaString(evalContext, cdata.content, mode);
            if (form == null) {
                elem.logError("Failed to parse inline formula");
                valid = false;
            }
        } else {
            // See of we're in the new format, signaled by the presence of an <expr> child element
            boolean newFormat = false;
            for (int i = 0; i < count; ++i) {
                final INode child = elem.getChild(i);
                if (child instanceof final IElement childElement) {
                    final String tagName = childElement.getTagName();
                    if (EXPR.equals(tagName)) {
                        newFormat = true;
                        break;
                    }
                }
            }

            if (newFormat) {
                for (final IElement child : elem.getElementChildrenAsList()) {
                    final String tag = child.getTagName();

                    if (child instanceof final NonemptyElement nonempty) {

                        switch (tag) {
                            case MIN_X -> {
                                minXF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                                if (minXF == null) {
                                    elem.logError("Invalid 'minx' formula");
                                    valid = false;
                                }
                            }
                            case MAX_X -> {
                                maxXF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                                if (maxXF == null) {
                                    elem.logError("Invalid 'maxx' formula");
                                    valid = false;
                                }
                            }
                            case EXPR -> {
                                form = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                                if (form == null) {
                                    elem.logError("Invalid 'expr' formula");
                                    valid = false;
                                }
                            }
                            case null, default -> {
                                elem.logError("Unsupported '" + tag + "' child of graph formula");
                                valid = false;
                            }
                        }
                    } else {
                        elem.logError("Unsupported empty '" + tag + "' child of graph formula");
                        valid = false;
                    }
                }
            } else {
                if (mode.reportDeprecated) {
                    elem.logError("Deprecated wrapperless XML expression in graph formula");
                }
                form = XmlFormulaFactory.extractFormula(evalContext, elem, mode);
            }
        }

        if (form == null) {
            elem.logError("Unable to parse formula.");
            valid = false;
        } else if (valid) {
            NumberOrFormula min = null;
            if (minXC != null) {
                min = new NumberOrFormula(minXC);
            } else if (minXF != null) {
                min = new NumberOrFormula(minXF);
            }
            NumberOrFormula max = null;
            if (maxXC != null) {
                max = new NumberOrFormula(maxXC);
            } else if (maxXF != null) {
                max = new NumberOrFormula(maxXF);
            }

            final DocPrimitiveFormula primitive = new DocPrimitiveFormula(graph, form, null, color, style, min, max);
            if (domainVarStr != null) {
                primitive.setDomainVarName(domainVarStr);
            }
            final String name = primitive.getDomainVarName();
            final AbstractVariable variable = evalContext.getVariable(name);
            if (variable == null) {
                final VariableInputReal domainVar = new VariableInputReal(name);
                evalContext.addVariable(domainVar);
            }

            graph.addPrimitive(primitive);
        }

        return valid;
    }

    /**
     * Parse a line primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveLine(final EvalContext evalContext, final AbstractAttributedElementBase e,
                                                final AbstractDocPrimitiveContainer container, final EParserMode mode) {

        final DocPrimitiveLine p = new DocPrimitiveLine(container);

        boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(WIDTH, e.getStringAttr(WIDTH), e, mode)
                && p.setAttr(HEIGHT, e.getStringAttr(HEIGHT), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(STROKE_WIDTH, e.getStringAttr(STROKE_WIDTH), e, mode)
                && p.setAttr(DASH, e.getStringAttr(DASH), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        if (valid && e instanceof final NonemptyElement nonempty) {
            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement formula) {
                    final String tag = child.getTagName();

                    if (X.equals(tag)) {
                        final Formula theXCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theXCoord == null) {
                            e.logError("Invalid 'x' formula.");
                            valid = false;
                        } else {
                            p.setXCoord(new NumberOrFormula(theXCoord));
                        }
                    } else if (Y.equals(tag)) {
                        final Formula theYCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theYCoord == null) {
                            e.logError("Invalid 'y' formula.");
                            valid = false;
                        } else {
                            p.setYCoord(new NumberOrFormula(theYCoord));
                        }
                    } else if (WIDTH.equals(tag)) {
                        final Formula theWidth = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theWidth == null) {
                            e.logError("Invalid 'width' formula.");
                            valid = false;
                        } else {
                            p.setWidth(new NumberOrFormula(theWidth));
                        }
                    } else if (HEIGHT.equals(tag)) {
                        final Formula theHeight = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theHeight == null) {
                            e.logError("Invalid 'height' formula.");
                            valid = false;
                        } else {
                            p.setHeight(new NumberOrFormula(theHeight));
                        }
                    }
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse an arc primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveArc(final EvalContext evalContext, final AbstractAttributedElementBase e,
                                               final AbstractDocPrimitiveContainer container, final EParserMode mode) {

        final DocPrimitiveArc p = new DocPrimitiveArc(container);

        boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(WIDTH, e.getStringAttr(WIDTH), e, mode)
                && p.setAttr(HEIGHT, e.getStringAttr(HEIGHT), e, mode)
                && p.setAttr(CX, e.getStringAttr(CX), e, mode)
                && p.setAttr(CY, e.getStringAttr(CY), e, mode)
                && p.setAttr(R, e.getStringAttr(R), e, mode)
                && p.setAttr(RX, e.getStringAttr(RX), e, mode)
                && p.setAttr(RY, e.getStringAttr(RY), e, mode)
                && p.setAttr(START_ANGLE, e.getStringAttr(START_ANGLE), e, mode)
                && p.setAttr(ARC_ANGLE, e.getStringAttr(ARC_ANGLE), e, mode)
                && p.setAttr(STROKE_WIDTH, e.getStringAttr(STROKE_WIDTH), e, mode)
                && p.setAttr(STROKE_COLOR, e.getStringAttr(STROKE_COLOR), e, mode)
                && p.setAttr(STROKE_DASH, e.getStringAttr(STROKE_DASH), e, mode)
                && p.setAttr(STROKE_ALPHA, e.getStringAttr(STROKE_ALPHA), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode) // *** Deprecated
                && p.setAttr(DASH, e.getStringAttr(DASH), e, mode) // *** Deprecated
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode) // *** Deprecated
                && p.setAttr(FILL_STYLE, e.getStringAttr(FILL_STYLE), e, mode)
                && p.setAttr(FILL_COLOR, e.getStringAttr(FILL_COLOR), e, mode)
                && p.setAttr(FILL_ALPHA, e.getStringAttr(FILL_ALPHA), e, mode)
                && p.setAttr(FILLED, e.getStringAttr(FILLED), e, mode) // *** Deprecated
                && p.setAttr(RAYS_SHOWN, e.getStringAttr(RAYS_SHOWN), e, mode)
                && p.setAttr(RAY_WIDTH, e.getStringAttr(RAY_WIDTH), e, mode)
                && p.setAttr(RAY_LENGTH, e.getStringAttr(RAY_LENGTH), e, mode)
                && p.setAttr(RAY_COLOR, e.getStringAttr(RAY_COLOR), e, mode)
                && p.setAttr(RAY_DASH, e.getStringAttr(RAY_DASH), e, mode)
                && p.setAttr(RAY_ALPHA, e.getStringAttr(RAY_ALPHA), e, mode)
                && p.setAttr(LABEL, e.getStringAttr(LABEL), e, mode)
                && p.setAttr(LABEL_COLOR, e.getStringAttr(LABEL_COLOR), e, mode)
                && p.setAttr(LABEL_ALPHA, e.getStringAttr(LABEL_ALPHA), e, mode)
                && p.setAttr(LABEL_OFFSET, e.getStringAttr(LABEL_OFFSET), e, mode)
                && p.setAttr(FONT_NAME, e.getStringAttr(FONT_NAME), e, mode)
                && p.setAttr(FONT_SIZE, e.getStringAttr(FONT_SIZE), e, mode)
                && p.setAttr(FONT_STYLE, e.getStringAttr(FONT_STYLE), e, mode);

        if (valid && e instanceof final NonemptyElement nonempty) {
            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement inner) {
                    final String tag = child.getTagName();

                    if (X.equals(tag)) {
                        final Formula theX = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theX == null) {
                            e.logError("Invalid 'x' formula.");
                            valid = false;
                        } else {
                            p.setXCoord(new NumberOrFormula(theX));
                        }
                    } else if (Y.equals(tag)) {
                        final Formula theY = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theY == null) {
                            e.logError("Invalid 'y' formula.");
                            valid = false;
                        } else {
                            p.setYCoord(new NumberOrFormula(theY));
                        }
                    } else if (WIDTH.equals(tag)) {
                        final Formula theWidth = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theWidth == null) {
                            e.logError("Invalid 'width' formula.");
                            valid = false;
                        } else {
                            p.setWidth(new NumberOrFormula(theWidth));
                        }
                    } else if (HEIGHT.equals(tag)) {
                        final Formula theHeight = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theHeight == null) {
                            e.logError("Invalid 'height' formula.");
                            valid = false;
                        } else {
                            p.setHeight(new NumberOrFormula(theHeight));
                        }
                    } else if (CX.equals(tag)) {
                        final Formula theCx = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theCx == null) {
                            e.logError("Invalid 'cx' formula.");
                            valid = false;
                        } else {
                            p.setCenterX(new NumberOrFormula(theCx));
                        }
                    } else if (CY.equals(tag)) {
                        final Formula theCy = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theCy == null) {
                            e.logError("Invalid 'cy' formula.");
                            valid = false;
                        } else {
                            p.setCenterY(new NumberOrFormula(theCy));
                        }
                    } else if (R.equals(tag)) {
                        final Formula theR = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theR == null) {
                            e.logError("Invalid 'r' formula.");
                            valid = false;
                        } else {
                            p.setRadius(new NumberOrFormula(theR));
                        }
                    } else if (RX.equals(tag)) {
                        final Formula theRx = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theRx == null) {
                            e.logError("Invalid 'rx' formula.");
                            valid = false;
                        } else {
                            p.setXRadius(new NumberOrFormula(theRx));
                        }
                    } else if (RY.equals(tag)) {
                        final Formula theRy = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theRy == null) {
                            e.logError("Invalid 'ry' formula.");
                            valid = false;
                        } else {
                            p.setYRadius(new NumberOrFormula(theRy));
                        }
                    } else if (START_ANGLE.equals(tag)) {
                        final Formula theStartAngle = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theStartAngle == null) {
                            e.logError("Invalid 'start-angle' formula.");
                            valid = false;
                        } else {
                            p.setStartAngle(new NumberOrFormula(theStartAngle));
                        }
                    } else if (ARC_ANGLE.equals(tag)) {
                        final Formula theArcAngle = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theArcAngle == null) {
                            e.logError("Invalid 'arc-angle' formula.");
                            valid = false;
                        } else {
                            p.setArcAngle(new NumberOrFormula(theArcAngle));
                        }
                    } else if (LABEL.equals(tag)) {
                        final DocSimpleSpan innerSpan = parseSpan(evalContext, inner, mode);
                        if (innerSpan == null) {
                            e.logError("Failed to parse <label> in span primitive.");
                        } else {
                            final DocNonwrappingSpan nonwrap = new DocNonwrappingSpan();
                            for (final AbstractDocObjectTemplate obj : innerSpan.getChildren()) {
                                nonwrap.add(obj);
                            }
                            p.setLabelSpan(nonwrap);
                        }
                    }
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse an oval primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveOval(final EvalContext evalContext, final AbstractAttributedElementBase e,
                                                final AbstractDocPrimitiveContainer container, final EParserMode mode) {

        final DocPrimitiveOval p = new DocPrimitiveOval(container);

        boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(WIDTH, e.getStringAttr(WIDTH), e, mode)
                && p.setAttr(HEIGHT, e.getStringAttr(HEIGHT), e, mode)
                && p.setAttr(FILLED, e.getStringAttr(FILLED), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(STROKE_WIDTH, e.getStringAttr(STROKE_WIDTH), e, mode)
                && p.setAttr(DASH, e.getStringAttr(DASH), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        if (valid && e instanceof final NonemptyElement nonempty) {
            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement formula) {
                    final String tag = child.getTagName();

                    if (X.equals(tag)) {
                        final Formula theXCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theXCoord == null) {
                            e.logError("Invalid 'x' formula.");
                            valid = false;
                        } else {
                            p.setXCoord(new NumberOrFormula(theXCoord));
                        }
                    } else if (Y.equals(tag)) {
                        final Formula theYCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theYCoord == null) {
                            e.logError("Invalid 'y' formula.");
                            valid = false;
                        } else {
                            p.setYCoord(new NumberOrFormula(theYCoord));
                        }
                    } else if (WIDTH.equals(tag)) {
                        final Formula theWidth = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theWidth == null) {
                            e.logError("Invalid 'width' formula.");
                            valid = false;
                        } else {
                            p.setWidth(new NumberOrFormula(theWidth));
                        }
                    } else if (HEIGHT.equals(tag)) {
                        final Formula theHeight = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theHeight == null) {
                            e.logError("Invalid 'height' formula.");
                            valid = false;
                        } else {
                            p.setHeight(new NumberOrFormula(theHeight));
                        }
                    }
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse a rectangle primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveRectangle(final EvalContext evalContext,
                                                     final AbstractAttributedElementBase e,
                                                     final AbstractDocPrimitiveContainer container,
                                                     final EParserMode mode) {

        final DocPrimitiveRectangle p = new DocPrimitiveRectangle(container);

        boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(WIDTH, e.getStringAttr(WIDTH), e, mode)
                && p.setAttr(HEIGHT, e.getStringAttr(HEIGHT), e, mode)
                && p.setAttr(FILLED, e.getStringAttr(FILLED), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(STROKE_WIDTH, e.getStringAttr(STROKE_WIDTH), e, mode)
                && p.setAttr(DASH, e.getStringAttr(DASH), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        if (valid && e instanceof final NonemptyElement nonempty) {
            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement formula) {
                    final String tag = child.getTagName();

                    if (X.equals(tag)) {
                        final Formula theXCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theXCoord == null) {
                            e.logError("Invalid 'x' formula.");
                            valid = false;
                        } else {
                            p.setXCoord(new NumberOrFormula(theXCoord));
                        }
                    } else if (Y.equals(tag)) {
                        final Formula theYCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theYCoord == null) {
                            e.logError("Invalid 'y' formula.");
                            valid = false;
                        } else {
                            p.setYCoord(new NumberOrFormula(theYCoord));
                        }
                    } else if (WIDTH.equals(tag)) {
                        final Formula theWidth = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theWidth == null) {
                            e.logError("Invalid 'width' formula.");
                            valid = false;
                        } else {
                            p.setWidth(new NumberOrFormula(theWidth));
                        }
                    } else if (HEIGHT.equals(tag)) {
                        final Formula theHeight = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theHeight == null) {
                            e.logError("Invalid 'height' formula.");
                            valid = false;
                        } else {
                            p.setHeight(new NumberOrFormula(theHeight));
                        }
                    }
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse a polygon primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitivePolygon(final EvalContext evalContext, final AbstractAttributedElementBase e,
                                                   final AbstractDocPrimitiveContainer container,
                                                   final EParserMode mode) {

        final DocPrimitivePolygon p = new DocPrimitivePolygon(container);

        boolean valid = p.setAttr(X_LIST, e.getStringAttr(X_LIST), e, mode)
                && p.setAttr(Y_LIST, e.getStringAttr(Y_LIST), e, mode)
                && p.setAttr(FILLED, e.getStringAttr(FILLED), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(STROKE_WIDTH, e.getStringAttr(STROKE_WIDTH), e, mode)
                && p.setAttr(DASH, e.getStringAttr(DASH), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        if (valid && e instanceof final NonemptyElement nonempty) {

            final Collection<Formula> xList = new ArrayList<>(10);
            final Collection<Formula> yList = new ArrayList<>(10);

            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement formula) {
                    final String tag = child.getTagName();

                    if (X.equals(tag)) {
                        final Formula theXCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theXCoord == null) {
                            e.logError("Invalid 'x' formula in child element.");
                            valid = false;
                        } else {
                            xList.add(theXCoord);
                        }
                    } else if (Y.equals(tag)) {
                        final Formula theYCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theYCoord == null) {
                            e.logError("Invalid 'y' formula in child element.");
                            valid = false;
                        } else {
                            yList.add(theYCoord);
                        }
                    }
                }
            }

            if (!xList.isEmpty()) {
                p.setXCoordFormulas(xList);
            }

            if (!yList.isEmpty()) {
                p.setYCoordFormulas(yList);
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse a protractor primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveProtractor(final EvalContext evalContext,
                                                      final AbstractAttributedElementBase e,
                                                      final AbstractDocPrimitiveContainer container,
                                                      final EParserMode mode) {

        final DocPrimitiveProtractor p = new DocPrimitiveProtractor(container);

        boolean valid = p.setAttr(CX, e.getStringAttr(CX), e, mode)
                && p.setAttr(CY, e.getStringAttr(CY), e, mode)
                && p.setAttr(R, e.getStringAttr(R), e, mode)
                && p.setAttr(ORIENTATION, e.getStringAttr(ORIENTATION), e, mode)
                && p.setAttr(UNITS, e.getStringAttr(UNITS), e, mode)
                && p.setAttr(QUADRANTS, e.getStringAttr(QUADRANTS), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(TEXT_COLOR, e.getStringAttr(TEXT_COLOR), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        if (valid && e instanceof final NonemptyElement nonempty) {

            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement formula) {
                    final String tag = child.getTagName();

                    if (CENTER_X.equals(tag)) {
                        final Formula theCenterX = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theCenterX == null) {
                            e.logError("Invalid 'center-x' formula in child element.");
                            valid = false;
                        } else {
                            p.setCenterX(new NumberOrFormula(theCenterX));
                        }
                    } else if (CENTER_Y.equals(tag)) {
                        final Formula theCenterY = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theCenterY == null) {
                            e.logError("Invalid 'center-y' formula in child element.");
                            valid = false;
                        } else {
                            p.setCenterY(new NumberOrFormula(theCenterY));
                        }
                    } else if (RADIUS.equals(tag)) {
                        final Formula theRadius = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theRadius == null) {
                            e.logError("Invalid 'radius' formula in child element.");
                            valid = false;
                        } else {
                            p.setRadius(new NumberOrFormula(theRadius));
                        }
                    } else if (ORIENTATION.equals(tag)) {
                        final Formula theOrientation = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theOrientation == null) {
                            e.logError("Invalid 'orientation' formula in child element.");
                            valid = false;
                        } else {
                            p.setOrientation(new NumberOrFormula(theOrientation));
                        }
                    }
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse a raster primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveRaster(final EvalContext evalContext, final AbstractAttributedElementBase e,
                                                  final AbstractDocPrimitiveContainer container,
                                                  final EParserMode mode) {

        final DocPrimitiveRaster p = new DocPrimitiveRaster(container);

        boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(WIDTH, e.getStringAttr(WIDTH), e, mode)
                && p.setAttr(HEIGHT, e.getStringAttr(HEIGHT), e, mode)
                && p.setAttr(SRC, e.getStringAttr(SRC), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        if (valid && e instanceof final NonemptyElement nonempty) {
            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement formula) {
                    final String tag = child.getTagName();

                    if (X.equals(tag)) {
                        final Formula theXCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theXCoord == null) {
                            e.logError("Invalid 'x' formula.");
                            valid = false;
                        } else {
                            p.setXCoord(new NumberOrFormula(theXCoord));
                        }
                    } else if (Y.equals(tag)) {
                        final Formula theYCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theYCoord == null) {
                            e.logError("Invalid 'y' formula.");
                            valid = false;
                        } else {
                            p.setYCoord(new NumberOrFormula(theYCoord));
                        }
                    } else if (WIDTH.equals(tag)) {
                        final Formula theWidth = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theWidth == null) {
                            e.logError("Invalid 'width' formula.");
                            valid = false;
                        } else {
                            p.setWidth(new NumberOrFormula(theWidth));
                        }
                    } else if (HEIGHT.equals(tag)) {
                        final Formula theHeight = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theHeight == null) {
                            e.logError("Invalid 'height' formula.");
                            valid = false;
                        } else {
                            p.setHeight(new NumberOrFormula(theHeight));
                        }
                    }
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse a text primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveText(final EvalContext evalContext, final AbstractAttributedElementBase e,
                                                final AbstractDocPrimitiveContainer container, final EParserMode mode) {

        final DocPrimitiveText p = new DocPrimitiveText(container);

        boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(ANCHOR, e.getStringAttr(ANCHOR), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(HIGHLIGHT, e.getStringAttr(HIGHLIGHT), e, mode)
                && p.setAttr(FONT_NAME, e.getStringAttr(FONT_NAME), e, mode)
                && p.setAttr(FONT_SIZE, e.getStringAttr(FONT_SIZE), e, mode)
                && p.setAttr(FONT_STYLE, e.getStringAttr(FONT_STYLE), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        String valueStr = e.getStringAttr(VALUE);
        if (valueStr != null) {
            // Identify and replace {\tag} tags for special characters
            int index = valueStr.indexOf("{\\");
            while (index != -1) {
                final int endIndex = valueStr.indexOf('}', index + 2);
                if (endIndex != -1) {
                    final String substring = valueStr.substring(index + 1, endIndex);
                    final String cp = parseNamedEntity(substring);

                    if (!cp.isEmpty()) {
                        valueStr = valueStr.substring(0, index) + cp + valueStr.substring(endIndex + 1);
                    }
                }
                index = valueStr.indexOf("{\\", index + 1);
            }

            valid = p.setAttr(VALUE, valueStr, e, mode);
            if (!valid) {
                e.logError("Invalid value for 'value' attribute for drawing primitive (" + valueStr + ").");
            }
        }

        if (valid && e instanceof final NonemptyElement nonempty) {
            for (final IElement child : nonempty.getElementChildrenAsList()) {
                if (child instanceof final NonemptyElement formula) {
                    final String tag = child.getTagName();

                    if (X.equals(tag)) {
                        final Formula theXCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theXCoord == null) {
                            e.logError("Invalid 'x' formula.");
                            valid = false;
                        } else {
                            p.setXCoord(new NumberOrFormula(theXCoord));
                        }
                    } else if (Y.equals(tag)) {
                        final Formula theYCoord = XmlFormulaFactory.extractFormula(evalContext, formula, mode);
                        if (theYCoord == null) {
                            e.logError("Invalid 'y' formula.");
                            valid = false;
                        } else {
                            p.setYCoord(new NumberOrFormula(theYCoord));
                        }
                    }
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse a span primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveSpan(final EvalContext evalContext, final EmptyElement e,
                                                final AbstractDocPrimitiveContainer container, final EParserMode mode) {

        final DocPrimitiveSpan p = new DocPrimitiveSpan(container);

        final boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(ANCHOR, e.getStringAttr(ANCHOR), e, mode)
                && p.setAttr(FILLED, e.getStringAttr(FILLED), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(FONT_NAME, e.getStringAttr(FONT_NAME), e, mode)
                && p.setAttr(FONT_SIZE, e.getStringAttr(FONT_SIZE), e, mode)
                && p.setAttr(FONT_STYLE, e.getStringAttr(FONT_STYLE), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        // <span x="((30+{lb})/2)+10" y="35+{la}" fontsize="20.0">{db2}</span>
        if (valid) {
            String value = e.getStringAttr(VALUE);
            if (value == null) {
                value = e.getStringAttr(SPAN);
            }

            final String xml = "<A>" + value + "</A>";
            try {
                final XmlContent content = new XmlContent(xml, false, false);
                final IElement top = content.getToplevel();
                if (top instanceof final NonemptyElement nonempty) {
                    final DocSimpleSpan innerSpan = parseSpan(evalContext, nonempty, mode);
                    if (innerSpan == null) {
                        e.logError("Failed to parsed 'value' attribute in span primitive.");
                    } else {
                        final DocNonwrappingSpan nonwrap = new DocNonwrappingSpan();
                        nonwrap.tag = NONWRAP;
                        nonwrap.add(innerSpan);
                        p.setSpan(nonwrap);
                    }
                }
            } catch (final ParsingException ex) {
                e.logError("Failed to parsed contents of 'value' attribute in span primitive.");
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Parse a span primitive and add it to a container.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param container   the container to which to add this primitive
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractPrimitiveSpan(final EvalContext evalContext, final NonemptyElement e,
                                                final AbstractDocPrimitiveContainer container, final EParserMode mode) {

        final DocPrimitiveSpan p = new DocPrimitiveSpan(container);

        boolean valid = p.setAttr(X, e.getStringAttr(X), e, mode)
                && p.setAttr(Y, e.getStringAttr(Y), e, mode)
                && p.setAttr(ANCHOR, e.getStringAttr(ANCHOR), e, mode)
                && p.setAttr(FILLED, e.getStringAttr(FILLED), e, mode)
                && p.setAttr(COLOR, e.getStringAttr(COLOR), e, mode)
                && p.setAttr(FONT_NAME, e.getStringAttr(FONT_NAME), e, mode)
                && p.setAttr(FONT_SIZE, e.getStringAttr(FONT_SIZE), e, mode)
                && p.setAttr(FONT_STYLE, e.getStringAttr(FONT_STYLE), e, mode)
                && p.setAttr(ALPHA, e.getStringAttr(ALPHA), e, mode);

        boolean newFormat = false;
        for (final IElement grandchild : e.getElementChildrenAsList()) {
            final String tag = grandchild.getTagName();
            if (X.equals(tag) || Y.equals(tag) || CONTENT.equals(tag)) {
                newFormat = true;
            }
        }

        if (!newFormat && mode.reportDeprecated) {
            e.logError("Deprecated format for &lt;span&gt; primitive");
        }

        if (valid) {
            if (newFormat) {
                for (final IElement child : e.getElementChildrenAsList()) {
                    if (child instanceof final NonemptyElement nonemptyChild) {
                        final String tag = child.getTagName();

                        if (X.equals(tag)) {
                            final Formula theXCoord = XmlFormulaFactory.extractFormula(evalContext, nonemptyChild,
                                    mode);
                            if (theXCoord == null) {
                                e.logError("Invalid 'x' formula.");
                                valid = false;
                            } else {
                                p.setXCoord(new NumberOrFormula(theXCoord));
                            }
                        } else if (Y.equals(tag)) {
                            final Formula theYCoord = XmlFormulaFactory.extractFormula(evalContext, nonemptyChild,
                                    mode);
                            if (theYCoord == null) {
                                e.logError("Invalid 'y' formula.");
                                valid = false;
                            } else {
                                p.setYCoord(new NumberOrFormula(theYCoord));
                            }
                        } else if (CONTENT.equals(tag)) {
                            final DocSimpleSpan innerSpan = parseSpan(evalContext, nonemptyChild, mode);
                            if (innerSpan == null) {
                                e.logError("Failed to parse <content> in span primitive.");
                            } else {
                                final DocNonwrappingSpan nonwrap = new DocNonwrappingSpan();
                                for (final AbstractDocObjectTemplate obj : innerSpan.getChildren()) {
                                    nonwrap.add(obj);
                                }
                                p.setSpan(nonwrap);
                            }
                        }
                    }
                }
            } else {
                final DocSimpleSpan innerSpan = parseSpan(evalContext, e, mode);
                if (innerSpan == null) {
                    e.logError("Failed to parse contents of span primitive.");
                } else {
                    final DocNonwrappingSpan nonwrap = new DocNonwrappingSpan();
                    for (final AbstractDocObjectTemplate obj : innerSpan.getChildren()) {
                        nonwrap.add(obj);
                    }
                    p.setSpan(nonwrap);
                }
            }
        }

        if (valid) {
            container.addPrimitive(p);
        }

        return valid;
    }

    /**
     * Generate a {@code DocImage} object from XML source. Any errors encountered while generating the image object will
     * be reflected in the source file's error list.
     *
     * @param elem      the element
     * @param container the span to which to add this image
     * @return true if loading successful; false otherwise
     */
    private static boolean extractImage(final EmptyElement elem, final AbstractDocSpanBase container) {

        boolean valid = true;

        final String widthStr = elem.getStringAttr(WIDTH);
        final String heightStr = elem.getStringAttr(HEIGHT);
        final String srcStr = elem.getStringAttr(SRC);
        final String altStr = elem.getStringAttr(ALT);

        NumberOrFormula width = null;
        if (widthStr != null) {
            try {
                final Number parsed = NumberParser.parse(widthStr);
                width = new NumberOrFormula(parsed);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'width' attribute value (must be a number).");
                valid = false;
            }
        }

        NumberOrFormula height = null;
        if (heightStr != null) {
            try {
                final Number parsed = NumberParser.parse(heightStr);
                height = new NumberOrFormula(parsed);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'height' attribute value (must be a number).");
                valid = false;
            }
        }

        URL url = null;
        if (srcStr == null) {
            elem.logError("<image> element missing required 'src' attribute.");
            valid = false;
        } else {
            try {
                final URI uri = new URI(srcStr);
                url = uri.toURL();
            } catch (final MalformedURLException | URISyntaxException e) {
                elem.logError("<image> element has invalid URL in 'src' attribute.");
                valid = false;
            }
        }

        if (valid) {
            final DocImage image = new DocImage(altStr);

            image.setScaledWidth(width);
            image.setScaledHeight(height);
            image.setSource(url);
            container.add(image);
        }

        return valid;
    }

    /**
     * Generate a {@code DocImage} object from XML source. Any errors encountered while generating the image object will
     * be reflected in the source file's error list.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this image
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractImage(final EvalContext evalContext, final NonemptyElement elem,
                                        final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        final String widthStr = elem.getStringAttr(WIDTH);
        final String heightStr = elem.getStringAttr(HEIGHT);
        final String srcStr = elem.getStringAttr(SRC);
        final String altStr = elem.getStringAttr(ALT);
        final String valignStr = elem.getStringAttr(VALIGN);

        NumberOrFormula width = null;
        if (widthStr != null) {
            try {
                final Number parsed = NumberParser.parse(widthStr);
                width = new NumberOrFormula(parsed);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'width' attribute value (must be a number).");
                valid = false;
            }
        }

        NumberOrFormula height = null;
        if (heightStr != null) {
            try {
                final Number parsed = NumberParser.parse(heightStr);
                height = new NumberOrFormula(parsed);
            } catch (final NumberFormatException e) {
                elem.logError("Invalid 'height' attribute value (must be a number).");
                valid = false;
            }
        }

        URL url = null;
        if (srcStr == null) {
            elem.logError("<image> element missing required 'src' attribute.");
            valid = false;
        } else {
            try {
                final URI uri = new URI(srcStr);
                url = uri.toURL();

            } catch (final MalformedURLException | URISyntaxException e) {
                elem.logError("<image> element has invalid URL in 'src' attribute.");
                valid = false;
            }
        }

        EVAlign valign = null;
        if (valignStr != null) {
            if (BASELINE.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.BASELINE;
            } else if (CENTER.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.CENTER;
            } else if (TOP.equalsIgnoreCase(valignStr)) {
                valign = EVAlign.TOP;
            } else {
                elem.logError("<image> element has invalid value in '' attribute.");
            }
        }

        // TODO: Scan children for width/height formulas

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();
                if (WIDTH.equals(childTag)) {
                    final Formula theWidth = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (theWidth == null) {
                        elem.logError("Invalid 'width' formula.");
                        valid = false;
                    } else {
                        width = new NumberOrFormula(theWidth);
                    }
                } else if (HEIGHT.equals(childTag)) {
                    final Formula theHeight = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (theHeight == null) {
                        elem.logError("Invalid 'height' formula.");
                        valid = false;
                    } else {
                        height = new NumberOrFormula(theHeight);
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within image.");
                    valid = false;
                }
            } else if (child instanceof final EmptyElement empty) {
                final String childTag = empty.getTagName();
                elem.logError("Empty " + childTag + " tag is not valid within image.");
                valid = false;
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in Image.");
                valid = false;
            }
        }

        if (valid) {
            final DocImage image = new DocImage(altStr);

            image.setScaledWidth(width);
            image.setScaledHeight(height);
            image.setSource(url);
            image.setLeftAlign(valign);
            container.add(image);
        }

        return valid;
    }

    /**
     * Generate a series of {@code DocText} or {@code DocParameterReference} objects from an XML CData object. Symbol
     * references of the form {\name} will be replaced by the corresponding Unicode code point. Any errors encountered
     * will be reflected in the source file's error context.
     *
     * @param cdata     the element
     * @param container the span to which to add this text
     * @param mode      the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractText(final CData cdata, final AbstractDocSpanBase container,
                                       final EParserMode mode) {

        final String content = cdata.content;

        boolean inWhitespace = false;
        boolean inText = false;
        boolean inParameter = false;
        int start = 0;
        int end;

        // Now break into runs of whitespace (each of which is stored as a single DocWhitespace
        // object), and runs of non-whitespace, each of which is stored as a DocText or a
        // DocParameterReference object.

        final int len = content.length();
        for (end = 0; end < len; ++end) {
            final int currentChar = (int) content.charAt(end);

            if (isXmlWhitespace(currentChar)) {

                if (inParameter) {
                    continue; // Whitespace: can be part of parameter name
                }

                if (inText) {
                    // End of run of text
                    final String str = content.substring(start, end);
                    final String unescaped = unescape(cdata, str, mode);
                    final AbstractDocObjectTemplate obj = new DocText(unescaped);
                    container.add(obj);
                    inText = false;
                }

                inWhitespace = true;
            } else {

                if (inWhitespace) {
                    // End of a run of whitespace
                    final AbstractDocObjectTemplate obj = new DocWhitespace();
                    container.add(obj);
                    inWhitespace = false;
                }

                if (currentChar == '{') {

                    if (inText) {
                        // End the text, start the parameter
                        final String str = content.substring(start, end);
                        final String unescaped = unescape(cdata, str, mode);
                        final AbstractDocObjectTemplate obj = new DocText(unescaped);
                        container.add(obj);
                        inText = false;
                    } else if (inParameter) {
                        cdata.logError("Unexpected '{' within parameter or entity name.");
                        return false;
                    }

                    inParameter = true;
                    start = end;

                } else if (currentChar == '}') {

                    if (inText) {
                        // Misplaced '}' in text: error
                        cdata.logError("Unexpected '}' found, no matching '{'.");
                        return false;
                    }

                    if (inParameter) {
                        // End a parameter reference or entity
                        final String name = content.substring(start + 1, end);

                        if (name.startsWith("\\")) {
                            // This is a named entity - install the proper character entity.
                            final String entity = parseNamedEntity(name);
                            container.add(new DocText(entity));
                        } else {
                            final AbstractDocObjectTemplate obj = new DocParameterReference(name);
                            container.add(obj);
                        }

                        inParameter = false;
                    }
                } else if (!inText && !inParameter) {
                    // Start of run of text
                    start = end;
                    inText = true;
                }
            }
        }

        // Finish up any open text or whitespace after loop (we should not be in a parameter at the
        // end since closing '}' should be found)

        if (inParameter) {
            cdata.logError("No matching '}' found.");
            return false;
        }

        if (inText) {
            // End of run of text
            final String str = content.substring(start, end);
            final String unescaped = unescape(cdata, str, mode);
            final DocText obj = new DocText(unescaped);
            container.add(obj);
        } else if (inWhitespace) {
            // End of a run of whitespace
            container.add(new DocWhitespace());
        }

        return true;
    }

    /**
     * Generate a series of {@code DocText} or {@code DocParameterReference} objects from an XML CData object. Symbol
     * references of the form {\name} will be replaced by the corresponding Unicode code point. Any errors encountered
     * will be reflected in the source file's error context.
     *
     * <p>
     * This differs from {@code extractText} in that whitespace found is not emitted as {@code DocWhitespace}, but is
     * simply included as part of text objects.
     *
     * @param cdata     the element
     * @param container the span to which to add this text
     * @param mode      the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractNonwrapText(final CData cdata, final AbstractDocSpanBase container,
                                              final EParserMode mode) {

        final String content = cdata.content;

        boolean inText = false;
        boolean inParameter = false;
        int start = 0;
        int end;

        // Logic is the same as for <code>extractText</code> except that contiguous whitespace and
        // text are merged into DocText objects
        final int len = content.length();
        for (end = 0; end < len; ++end) {
            final int currentChar = (int) content.charAt(end);

            if (currentChar == '{') {

                if (inText) {
                    // End the text, start the parameter
                    final String str = content.substring(start, end);
                    final AbstractDocObjectTemplate obj = new DocText(unescape(cdata, collapseWhitespace(str), mode));
                    container.add(obj);
                    inText = false;
                } else if (inParameter) {
                    cdata.logError("Unexpected '{' within parameter or entity name.");
                    return false;
                }

                inParameter = true;
                start = end;

            } else if (currentChar == '}') {

                if (inText) {
                    // Misplaced '}' in text: error
                    cdata.logError("Unexpected '}' found, no matching '{'");
                    return false;
                } else if (inParameter) {
                    // End a parameter reference or entity
                    final String name = content.substring(start + 1, end);

                    if (name.startsWith("\\")) {
                        // This is a named entity - install the proper character entity.
                        final String entity = parseNamedEntity(name);
                        container.add(new DocText(entity));
                    } else {
                        final AbstractDocObjectTemplate obj = new DocParameterReference(name);
                        container.add(obj);
                    }

                    inParameter = false;
                }
            } else if (!inText && !inParameter) {
                // Start of run of text
                start = end;
                inText = true;
            }
        }

        // Finish up any open text or whitespace after loop (we should not be in a parameter at the
        // end since closing '}' should be found)
        if (inText) {
            // End of run of text
            final String str = content.substring(start, end);
            final String collapsed = collapseWhitespace(str);
            final String unescaped = unescape(cdata, collapsed, mode);
            final AbstractDocObjectTemplate obj = new DocText(unescaped);
            container.add(obj);
        } else if (inParameter) {
            cdata.logError("No matching '}' found.");
            return false;
        }

        return true;
    }

    /**
     * Parse a symbol palette and add it to a container.
     *
     * @param elem      the element
     * @param container the span to which to add this fraction
     * @return true if loading successful; false otherwise
     */
    private static boolean extractSymbolPalette(final IElement elem, final AbstractDocSpanBase container) {

        final DocSymbolPalette palette = new DocSymbolPalette();

        final String stringAttr = elem.getStringAttr(SYMBOLS);
        final boolean valid = palette.setAttribute(SYMBOLS, stringAttr, elem);

        if (valid) {
            container.add(palette);
        }

        return valid;
    }

    /**
     * Parses a named entity into a string.
     *
     * @param name the entity name, such as "\lll"
     * @return the entity string (a single-character string)
     */
    static String parseNamedEntity(final String name) {

        char ch = (char) 0;

        // TODO: Extract first character and test, then group codes by first char to make search more efficient

        if ("\\nbsp".equals(name)) {
            ch = '\u00A0';
        } else if ("\\degree".equals(name)) {
            ch = '\u00B0';
        } else if ("\\pm".equals(name)) {
            ch = '\u00B1';
        } else if ("\\cdot".equals(name)) {
            ch = '\u00B7';
        } else if ("\\times".equals(name)) {
            ch = '\u00D7';
        } else if ("\\div".equals(name)) {
            ch = '\u00F7';
        } else if ("\\fnof".equals(name)) {
            ch = '\u0192';
        } else if ("\\Gamma".equals(name)) {
            ch = '\u0393';
        } else if ("\\Delta".equals(name)) {
            ch = '\u0394';
        } else if ("\\Theta".equals(name)) {
            ch = '\u0398';
        } else if ("\\Lamda".equals(name)) {
            ch = '\u039B';
        } else if ("\\Xi".equals(name)) {
            ch = '\u039E';
        } else if ("\\Pi".equals(name)) {
            ch = '\u03A0';
        } else if ("\\Sigma".equals(name)) {
            ch = '\u03A3';
        } else if ("\\Upsilon".equals(name)) {
            ch = '\u03A5';
        } else if ("\\Phi".equals(name)) {
            ch = '\u03A6';
        } else if ("\\Psi".equals(name)) {
            ch = '\u03A8';
        } else if ("\\Omega".equals(name)) {
            ch = '\u03A9';
        } else if ("\\alpha".equals(name)) {
            ch = '\u03B1';
        } else if ("\\beta".equals(name)) {
            ch = '\u03B2';
        } else if ("\\gamma".equals(name)) {
            ch = '\u03B3';
        } else if ("\\delta".equals(name)) {
            ch = '\u03B4';
        } else if ("\\varepsilon".equals(name)) {
            ch = '\u03B5';
        } else if ("\\zeta".equals(name)) {
            ch = '\u03B6';
        } else if ("\\eta".equals(name)) {
            ch = '\u03B7';
        } else if ("\\theta".equals(name)) {
            ch = '\u03B8';
        } else if ("\\iota".equals(name)) {
            ch = '\u03B9';
        } else if ("\\kappa".equals(name)) {
            ch = '\u03BA';
        } else if ("\\lamda".equals(name)) {
            ch = '\u03BB';
        } else if ("\\mu".equals(name)) {
            ch = '\u03BC';
        } else if ("\\nu".equals(name)) {
            ch = '\u03BD';
        } else if ("\\xi".equals(name)) {
            ch = '\u03BE';
        } else if ("\\omicron".equals(name)) {
            ch = '\u03BF';
        } else if ("\\pi".equals(name)) {
            ch = '\u03C0';
        } else if ("\\rho".equals(name)) {
            ch = '\u03C1';
        } else if ("\\varsigma".equals(name)) {
            ch = '\u03C2';
        } else if ("\\sigma".equals(name)) {
            ch = '\u03C3';
        } else if ("\\tau".equals(name)) {
            ch = '\u03C4';
        } else if ("\\upsilon".equals(name)) {
            ch = '\u03C5';
        } else if ("\\varphi".equals(name)) {
            ch = '\u03C6';
        } else if ("\\chi".equals(name)) {
            ch = '\u03C7';
        } else if ("\\psi".equals(name)) {
            ch = '\u03C8';
        } else if ("\\omega".equals(name)) {
            ch = '\u03C9';
        } else if ("\\vartheta".equals(name)) {
            ch = '\u03D1';
        } else if ("\\phi".equals(name)) {
            ch = '\u03D5';
        } else if ("\\varpi".equals(name)) {
            ch = '\u03D6';
        } else if ("\\varkappa".equals(name)) {
            ch = '\u03F0';
        } else if ("\\varrho".equals(name)) {
            ch = '\u03F1';
        } else if ("\\epsilon".equals(name)) {
            ch = '\u03F5';
        } else if ("\\minus".equals(name)) {
            ch = '\u2212';
        } else if ("\\textendash".equals(name)) {
            ch = '\u2013';
        } else if ("\\textemdash".equals(name)) {
            ch = '\u2014';
        } else if ("\\textquoteleft".equals(name)) {
            ch = '\u2018';
        } else if ("\\textquoteright".equals(name)) {
            ch = '\u2019';
        } else if ("\\textquotedblleft".equals(name)) {
            ch = '\u201C';
        } else if ("\\textquotedblright".equals(name)) {
            ch = '\u201D';
        } else if ("\\bullet".equals(name)) {
            ch = '\u2022';
        } else if ("\\prime".equals(name)) {
            ch = '\u2032';
        } else if ("\\dprime".equals(name)) {
            ch = '\u2033';
        } else if ("\\tprime".equals(name)) {
            ch = '\u2034';
        } else if ("\\qprime".equals(name)) {
            ch = '\u2057';
        } else if ("\\e".equals(name)) {
            ch = '\u2147';
        } else if ("\\i".equals(name)) {
            ch = '\u2148';
        } else if ("\\leftarrow".equals(name)) {
            ch = '\u2190';
        } else if ("\\uparrow".equals(name)) {
            ch = '\u2191';
        } else if ("\\rightarrow".equals(name)) {
            ch = '\u2192';
        } else if ("\\downarrow".equals(name)) {
            ch = '\u2193';
        } else if ("\\leftrightarrow".equals(name)) {
            ch = '\u2194';
        } else if ("\\updownarrow".equals(name)) {
            ch = '\u2195';
        } else if ("\\Leftarrow".equals(name)) {
            ch = '\u21D0';
        } else if ("\\Uparrow".equals(name)) {
            ch = '\u21D1';
        } else if ("\\Rightarrow".equals(name)) {
            ch = '\u21D2';
        } else if ("\\Downarrow".equals(name)) {
            ch = '\u21D3';
        } else if ("\\Leftrightarrow".equals(name)) {
            ch = '\u21D4';
        } else if ("\\Updownarrow".equals(name)) {
            ch = '\u21D5';
        } else if ("\\circ".equals(name)) {
            ch = '\u2218';
        } else if ("\\varpropto".equals(name)) {
            ch = '\u221D';
        } else if ("\\infty".equals(name)) {
            ch = '\u221E';
        } else if ("\\angle".equals(name)) {
            ch = '\u2220';
        } else if ("\\measuredangle".equals(name)) {
            ch = '\u2221';
        } else if ("\\cap".equals(name)) {
            ch = '\u2229';
        } else if ("\\cup".equals(name)) {
            ch = '\u222A';
        } else if ("\\int".equals(name)) {
            ch = '\u222B';
        } else if ("\\simeq".equals(name)) {
            ch = '\u2243';
        } else if ("\\approx".equals(name)) {
            ch = '\u2248';
        } else if ("\\neq".equals(name)) {
            ch = '\u2260';
        } else if ("\\leq".equals(name)) {
            ch = '\u2264';
        } else if ("\\geq".equals(name)) {
            ch = '\u2265';
        } else if ("\\leqq".equals(name)) {
            ch = '\u2266';
        } else if ("\\geqq".equals(name)) {
            ch = '\u2267';
        } else if ("\\lneqq".equals(name)) {
            ch = '\u2268';
        } else if ("\\gneqq".equals(name)) {
            ch = '\u2269';
        } else if ("\\ll".equals(name)) {
            ch = '\u226A';
        } else if ("\\gg".equals(name)) {
            ch = '\u226B';
        } else if ("\\between".equals(name)) {
            ch = '\u226C';
        } else if ("\\nless".equals(name)) {
            ch = '\u226E';
        } else if ("\\ngtr".equals(name)) {
            ch = '\u226F';
        } else if ("\\nleq".equals(name)) {
            ch = '\u2270';
        } else if ("\\ngeq".equals(name)) {
            ch = '\u2271';
        } else if ("\\lesssim".equals(name)) {
            ch = '\u2272';
        } else if ("\\gtrsim".equals(name)) {
            ch = '\u2273';
        } else if ("\\lessgtr".equals(name)) {
            ch = '\u2276';
        } else if ("\\gtrless".equals(name)) {
            ch = '\u2277';
        } else if ("\\prec".equals(name)) {
            ch = '\u227A';
        } else if ("\\succ".equals(name)) {
            ch = '\u227B';
        } else if ("\\preccurlyeq".equals(name)) {
            ch = '\u227C';
        } else if ("\\succcurlyeq".equals(name)) {
            ch = '\u227D';
        } else if ("\\precsim".equals(name)) {
            ch = '\u227E';
        } else if ("\\succsim".equals(name)) {
            ch = '\u227F';
        } else if ("\\nprec".equals(name)) {
            ch = '\u2280';
        } else if ("\\nsucc".equals(name)) {
            ch = '\u2281';
        } else if ("\\lessdot".equals(name)) {
            ch = '\u22D6';
        } else if ("\\gtrdot".equals(name)) {
            ch = '\u22D7';
        } else if ("\\lesseqgtr".equals(name)) {
            ch = '\u22DA';
        } else if ("\\gtreqless".equals(name)) {
            ch = '\u22DB';
        } else if ("\\curlyeqprec".equals(name)) {
            ch = '\u22DE';
        } else if ("\\curlyeqsucc".equals(name)) {
            ch = '\u22DF';
        } else if ("\\npreceq".equals(name)) {
            ch = '\u22E0';
        } else if ("\\nsucceq".equals(name)) {
            ch = '\u22E1';
        } else if ("\\lnsim".equals(name)) {
            ch = '\u22E6';
        } else if ("\\gnsim".equals(name)) {
            ch = '\u22E7';
        } else if ("\\precnsim".equals(name)) {
            ch = '\u22E8';
        } else if ("\\succnsim".equals(name)) {
            ch = '\u22E9';
        } else if ("\\cdots".equals(name)) {
            ch = '\u22EF';
        } else if ("\\smallfrown".equals(name)) {
            ch = '\u2322';
        } else if ("\\smallsmile".equals(name)) {
            ch = '\u2323';
        } else if ("\\langle".equals(name)) {
            ch = '\u2329';
        } else if ("\\rangle".equals(name)) {
            ch = '\u232A';
        } else if ("\\blacksquare".equals(name)) {
            ch = '\u25A0';
        } else if ("\\blacktriangle".equals(name)) {
            ch = '\u25B2';
        } else if ("\\triangle".equals(name)) {
            ch = '\u25B3';
        } else if ("\\blacktriangleright".equals(name)) {
            ch = '\u25BA';
        } else if ("\\blacktriangledown".equals(name)) {
            ch = '\u25BC';
        } else if ("\\blacktriangleleft".equals(name)) {
            ch = '\u25C4';
        } else if ("\\spadesuit".equals(name)) {
            ch = '\u2660';
        } else if ("\\clubsuit".equals(name)) {
            ch = '\u2663';
        } else if ("\\heartsuit".equals(name)) {
            ch = '\u2665';
        } else if ("\\diamondsuit".equals(name)) {
            ch = '\u2666';
        } else if ("\\checkmark".equals(name)) {
            ch = '\u2713';
        } else if ("\\diagup".equals(name)) {
            ch = '\u27CB';
        } else if ("\\diagdown".equals(name)) {
            ch = '\u27CD';
        } else if ("\\Longleftarrow".equals(name)) {
            ch = '\u27F8';
        } else if ("\\Longrightarrow".equals(name)) {
            ch = '\u27F9';
        } else if ("\\Longleftrightarrow".equals(name)) {
            ch = '\u27FA';
        } else if ("\\leqslant".equals(name)) {
            ch = '\u2A7D';
        } else if ("\\geqslant".equals(name)) {
            ch = '\u2A7E';
        } else if ("\\lessapprox".equals(name)) {
            ch = '\u2A85';
        } else if ("\\gtrapprox".equals(name)) {
            ch = '\u2A86';
        } else if ("\\lneq".equals(name)) {
            ch = '\u2A87';
        } else if ("\\gneq".equals(name)) {
            ch = '\u2A88';
        } else if ("\\lnapprox".equals(name)) {
            ch = '\u2A89';
        } else if ("\\gnapprox".equals(name)) {
            ch = '\u2A8A';
        } else if ("\\lesseqqgtr".equals(name)) {
            ch = '\u2A8B';
        } else if ("\\gtreqqless".equals(name)) {
            ch = '\u2A8C';
        } else if ("\\eqslantless".equals(name)) {
            ch = '\u2A95';
        } else if ("\\eqslantgtr".equals(name)) {
            ch = '\u2A96';
        } else if ("\\lll".equals(name)) {
            ch = '\u2AA1';
        } else if ("\\ggg".equals(name)) {
            ch = '\u2AA2';
        } else if ("\\preceq".equals(name)) {
            ch = '\u2AAF';
        } else if ("\\succeq".equals(name)) {
            ch = '\u2AB0';
        } else if ("\\precneqq".equals(name)) {
            ch = '\u2AB5';
        } else if ("\\succneqq".equals(name)) {
            ch = '\u2AB6';
        } else if ("\\precapprox".equals(name)) {
            ch = '\u2AB7';
        } else if ("\\succapprox".equals(name)) {
            ch = '\u2AB8';
        } else if ("\\precnapprox".equals(name)) {
            ch = '\u2AB9';
        } else if ("\\succnapprox".equals(name)) {
            ch = '\u2ABA';
        } else if ("\\pitchfork".equals(name)) {
            ch = '\u2ADB';
        }

        return (int) ch == 0 ? CoreConstants.EMPTY : Character.toString(ch);
    }

    /**
     * Given a string, collapses runs of multiple whitespace characters into a single space.
     *
     * @param chars the string to collapse
     * @return the collapsed string
     */
    private static String collapseWhitespace(final CharSequence chars) {

        final int len = chars.length();

        final StringBuilder cleaned = new StringBuilder(len);
        boolean regular = true;
        for (int i = 0; i < len; ++i) {
            final char ch = chars.charAt(i);

            if (Character.isWhitespace(ch)) {
                if (regular) {
                    cleaned.append(' ');
                    regular = false;
                }
            } else {
                regular = true;
                cleaned.append(ch);
            }
        }

        return cleaned.toString();
    }

    /**
     * Generate a {@code DocInput} object from XML source. Any errors encountered while generating the input object will
     * be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractInput(final EvalContext evalContext, final NonemptyElement elem,
                                        final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        final String type = elem.getStringAttr(TYPE);
        if (type == null) {
            elem.logError("<input> element missing required 'type' attribute");
            valid = false;
        }

        final String name = elem.getStringAttr("name");
        if (name == null) {
            elem.logError("<input> element missing required 'name' attribute");
            valid = false;
        }

        if (valid) {
            switch (type) {
                case INTEGER -> valid = extractInputInteger(evalContext, name, elem, container, mode);
                case REAL -> valid = extractInputReal(evalContext, name, elem, container, mode);
                case STRING -> valid = extractInputString(evalContext, name, elem, container, mode);
                case RADIO_BUTTON -> valid = extractInputRadioButton(evalContext, name, elem, container, mode);
                case CHECKBOX -> valid = extractInputCheckbox(evalContext, name, elem, container, mode);
                default -> {
                    elem.logError("Unrecognized type of input: " + type);
                    valid = false;
                }
            }
        }

        return valid;
    }

    /**
     * Generate a {@code DocInputLongField} object from XML source. Any errors encountered while generating the input
     * object will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param name        the input name
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractInputInteger(final EvalContext evalContext, final String name,
                                               final NonemptyElement elem, final AbstractDocSpanBase container,
                                               final EParserMode mode) {

        boolean valid = true;

        final String textValue = elem.getStringAttr(TEXT_VALUE);
        final String value = elem.getStringAttr(VALUE);
        final String widthStr = elem.getStringAttr(WIDTH);
        final String defaultStr = elem.getStringAttr(DEFAULT);
        final String styleStr = elem.getStringAttr(STYLE);
        final String treatMinusAsStr = elem.getStringAttr(TREAT_MINUS_AS);
        final String enabledVarNameStr = elem.getStringAttr(ENABLED_VAR_NAME);
        final String enabledVarValueStr = elem.getStringAttr(ENABLED_VAR_VALUE);

        Formula enabledF = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (ENABLED.equalsIgnoreCase(childTag)) {
                    if (mode == EParserMode.NORMAL) {
                        elem.logError("Deprecated 'enabled' formula on input");
                    }
                    enabledF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (enabledF == null) {
                        elem.logError("Invalid 'enabled' formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within input.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in Input.");
                valid = false;
            }
        }

        final DocInputLongField input = new DocInputLongField(name);
        input.setEnabledFormula(enabledF);

        if (defaultStr != null) {
            try {
                input.defaultValue = Long.valueOf(defaultStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'default' attribute value");
                valid = false;
            }
        }

        if (styleStr != null) {
            if (BOX.equalsIgnoreCase(styleStr)) {
                input.style = EFieldStyle.BOX;
            } else if (UNDERLINE.equalsIgnoreCase(styleStr)) {
                input.style = EFieldStyle.UNDERLINE;
            } else {
                elem.logError("Invalid 'style' attribute value");
                valid = false;
            }
        }

        if (treatMinusAsStr != null) {
            try {
                input.minusAs = Long.valueOf(treatMinusAsStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'treat-minus-as' attribute value");
                valid = false;
            }
        }

        if (widthStr != null) {
            try {
                input.width = Integer.valueOf(widthStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'width' attribute value");
                valid = false;
            }
        }

        if (textValue != null) {
            input.setTextValue(textValue);
        }

        if (value != null) {
            try {
                final Long longValue = Long.valueOf(value);
                input.setOnlyLongValue(longValue);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'value' attribute value");
                valid = false;
            }
        }

        if (enabledVarNameStr != null) {
            if (enabledVarValueStr == null) {
                elem.logError("'enabled-var-name' present but 'enabled-var-value' absent");
                valid = false;
            } else {
                input.setEnabledVarName(enabledVarNameStr);

                if (TRUE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.TRUE);
                } else if (FALSE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.FALSE);
                } else {
                    try {
                        final Long varValue = Long.valueOf(enabledVarValueStr);
                        input.setEnabledVarValue(varValue);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'enabled-var-value' attribute value");
                        valid = false;
                    }
                }
            }
        } else if (enabledVarValueStr != null) {
            elem.logError("'enabled-var-value' present but 'enabled-var-name' absent");
            valid = false;
        }

        valid = valid && extractFormattable(elem, input);

        if (valid) {
            container.add(input);
        }

        return valid;
    }

    /**
     * Generate a {@code DocInputDoubleField} object from XML source. Any errors encountered while generating the input
     * object will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param name        the input name
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractInputReal(final EvalContext evalContext, final String name,
                                            final NonemptyElement elem, final AbstractDocSpanBase container,
                                            final EParserMode mode) {

        boolean valid = true;

        final String textValue = elem.getStringAttr(TEXT_VALUE);
        final String value = elem.getStringAttr(VALUE);
        final String widthStr = elem.getStringAttr(WIDTH);
        final String defaultStr = elem.getStringAttr(DEFAULT);
        final String styleStr = elem.getStringAttr(STYLE);
        final String treatMinusAsStr = elem.getStringAttr(TREAT_MINUS_AS);
        final String enabledVarNameStr = elem.getStringAttr(ENABLED_VAR_NAME);
        final String enabledVarValueStr = elem.getStringAttr(ENABLED_VAR_VALUE);

        Formula enabledF = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (ENABLED.equalsIgnoreCase(childTag)) {
                    if (mode == EParserMode.NORMAL) {
                        elem.logError("Deprecated 'enabled' formula on input");
                    }
                    enabledF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (enabledF == null) {
                        elem.logError("Invalid 'enabled' formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within input.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in Input.");
                valid = false;
            }
        }

        final DocInputDoubleField input = new DocInputDoubleField(name);
        input.setEnabledFormula(enabledF);

        if (defaultStr != null) {
            try {
                input.defaultValue = Double.valueOf(defaultStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'default' attribute value");
                valid = false;
            }
        }

        if (styleStr != null) {
            if (BOX.equalsIgnoreCase(styleStr)) {
                input.style = EFieldStyle.BOX;
            } else if (UNDERLINE.equalsIgnoreCase(styleStr)) {
                input.style = EFieldStyle.UNDERLINE;
            } else {
                elem.logError("Invalid 'style' attribute value");
                valid = false;
            }
        }

        if (treatMinusAsStr != null) {
            try {
                input.minusAs = Double.valueOf(treatMinusAsStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'treat-minus-as' attribute value");
                valid = false;
            }
        }

        if (widthStr != null) {
            try {
                input.width = Integer.valueOf(widthStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'width' attribute value");
                valid = false;
            }
        }

        if (textValue != null && !textValue.isEmpty()) {
            input.setTextValue(textValue);
        }

        if (value != null) {
            try {
                final Double dblValue = Double.valueOf(value);
                input.setOnlyDoubleValue(dblValue);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'value' attribute value");
                valid = false;
            }
        }

        if (enabledVarNameStr != null) {
            if (enabledVarValueStr == null) {
                elem.logError("'enabled-var-name' present but 'enabled-var-value' absent");
                valid = false;
            } else {
                input.setEnabledVarName(enabledVarNameStr);

                if (TRUE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.TRUE);
                } else if (FALSE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.FALSE);
                } else {
                    try {
                        final Long varValue = Long.valueOf(enabledVarValueStr);
                        input.setEnabledVarValue(varValue);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'enabled-var-value' attribute value");
                        valid = false;
                    }
                }
            }
        } else if (enabledVarValueStr != null) {
            elem.logError("'enabled-var-value' present but 'enabled-var-name' absent");
            valid = false;
        }

        valid = valid && extractFormattable(elem, input);

        if (valid) {
            container.add(input);
        }

        return valid;
    }

    /**
     * Generate a {@code DocInputStringField} object from XML source. Any errors encountered while generating the input
     * object will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param name        the input name
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractInputString(final EvalContext evalContext, final String name,
                                              final NonemptyElement elem, final AbstractDocSpanBase container,
                                              final EParserMode mode) {

        boolean valid = true;

        final String textValue = elem.getStringAttr(TEXT_VALUE);
        final String widthStr = elem.getStringAttr(WIDTH);
        final String styleStr = elem.getStringAttr(STYLE);
        final String enabledVarNameStr = elem.getStringAttr(ENABLED_VAR_NAME);
        final String enabledVarValueStr = elem.getStringAttr(ENABLED_VAR_VALUE);

        Formula enabledF = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (ENABLED.equalsIgnoreCase(childTag)) {
                    if (mode == EParserMode.NORMAL) {
                        elem.logError("Deprecated 'enabled' formula on input");
                    }
                    enabledF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (enabledF == null) {
                        elem.logError("Invalid 'enabled' formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within input.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in Input.");
                valid = false;
            }
        }

        final DocInputStringField input = new DocInputStringField(name);
        input.setEnabledFormula(enabledF);

        if (styleStr != null) {
            if (BOX.equalsIgnoreCase(styleStr)) {
                input.style = EFieldStyle.BOX;
            } else if (UNDERLINE.equalsIgnoreCase(styleStr)) {
                input.style = EFieldStyle.UNDERLINE;
            } else {
                elem.logError("Invalid 'style' attribute value");
                valid = false;
            }
        }

        if (widthStr != null) {
            try {
                input.width = Integer.valueOf(widthStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'width' attribute value");
                valid = false;
            }
        }

        if (textValue != null && !textValue.isEmpty()) {
            input.setTextValue(textValue);
        }

        if (enabledVarNameStr != null) {
            if (enabledVarValueStr == null) {
                elem.logError("'enabled-var-name' present but 'enabled-var-value' absent");
                valid = false;
            } else {
                input.setEnabledVarName(enabledVarNameStr);

                if (TRUE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.TRUE);
                } else if (FALSE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.FALSE);
                } else {
                    try {
                        final Long varValue = Long.valueOf(enabledVarValueStr);
                        input.setEnabledVarValue(varValue);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'enabled-var-value' attribute value");
                        valid = false;
                    }
                }
            }
        } else if (enabledVarValueStr != null) {
            elem.logError("'enabled-var-value' present but 'enabled-var-name' absent");
            valid = false;
        }

        valid = valid && extractFormattable(elem, input);

        if (valid) {
            container.add(input);
        }

        return valid;
    }

    /**
     * Generate a {@code DocInputRadioButton} object from XML source. Any errors encountered while generating the input
     * object will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param name        the input name
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractInputRadioButton(final EvalContext evalContext, final String name,
                                                   final NonemptyElement elem, final AbstractDocSpanBase container,
                                                   final EParserMode mode) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr(VALUE);
        final String enabledVarNameStr = elem.getStringAttr(ENABLED_VAR_NAME);
        final String enabledVarValueStr = elem.getStringAttr(ENABLED_VAR_VALUE);

        Formula enabledF = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (ENABLED.equalsIgnoreCase(childTag)) {
                    if (mode == EParserMode.NORMAL) {
                        elem.logError("Deprecated 'enabled' formula on input");
                    }
                    enabledF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (enabledF == null) {
                        elem.logError("Invalid 'enabled' formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within input.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in Input.");
                valid = false;
            }
        }

        try {
            final int val = Long.valueOf(valueStr).intValue();
            final DocInputRadioButton input = new DocInputRadioButton(name, val);

            input.setEnabledFormula(enabledF);

            final String selectedStr = elem.getStringAttr(SELECTED);
            if (selectedStr != null) {
                if (TRUE.equalsIgnoreCase(selectedStr)) {
                    input.selectChoice();
                } else if (!FALSE.equalsIgnoreCase(selectedStr)) {
                    elem.logError("Invalid radio button selected value (must be TRUE or FALSE).");
                    valid = false;
                }
            }

            if (enabledVarNameStr != null) {
                if (enabledVarValueStr == null) {
                    elem.logError("'enabled-var-name' present but 'enabled-var-value' absent");
                    valid = false;
                } else {
                    input.setEnabledVarName(enabledVarNameStr);

                    if (TRUE.equalsIgnoreCase(enabledVarValueStr)) {
                        input.setEnabledVarValue(Boolean.TRUE);
                    } else if (FALSE.equalsIgnoreCase(enabledVarValueStr)) {
                        input.setEnabledVarValue(Boolean.FALSE);
                    } else {
                        try {
                            input.setEnabledVarValue(Long.valueOf(enabledVarValueStr));
                        } catch (final NumberFormatException ex) {
                            elem.logError("Invalid 'enabled-var-value' attribute value");
                            valid = false;
                        }
                    }
                }
            } else if (enabledVarValueStr != null) {
                elem.logError("'enabled-var-value' present but 'enabled-var-name' absent");
                valid = false;
            }

            valid = valid && extractFormattable(elem, input);

            if (valid) {
                container.add(input);
            }
        } catch (final NumberFormatException e) {
            elem.logError("Invalid radio button value.");
            valid = false;
        }

        return valid;
    }

    /**
     * Generate a {@code DocInputCheckbox} object from XML source. Any errors encountered while generating the input
     * object will be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param name        the input name
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractInputCheckbox(final EvalContext evalContext, final String name,
                                                final NonemptyElement elem, final AbstractDocSpanBase container,
                                                final EParserMode mode) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr(VALUE);
        final String enabledVarNameStr = elem.getStringAttr(ENABLED_VAR_NAME);
        final String enabledVarValueStr = elem.getStringAttr(ENABLED_VAR_VALUE);

        Formula enabledF = null;

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = nonempty.getTagName();

                if (ENABLED.equalsIgnoreCase(childTag)) {
                    if (mode == EParserMode.NORMAL) {
                        elem.logError("Deprecated 'enabled' formula on input");
                    }
                    enabledF = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (enabledF == null) {
                        elem.logError("Invalid 'enabled' formula.");
                        valid = false;
                    }
                } else {
                    elem.logError("The " + childTag + " tag is not valid within input.");
                    valid = false;
                }
            } else if ((!(child instanceof CData) && !(child instanceof Comment))) {
                elem.logError("Found " + child.getClass().getSimpleName() + " in Input.");
                valid = false;
            }
        }

        try {
            final long val = Long.parseLong(valueStr);
            final DocInputCheckbox input = new DocInputCheckbox(name, val);

            input.setEnabledFormula(enabledF);

            final String selectedStr = elem.getStringAttr(SELECTED);
            if (selectedStr != null) {
                if (TRUE.equalsIgnoreCase(selectedStr)) {
                    input.selectChoice();
                } else if (!FALSE.equalsIgnoreCase(selectedStr)) {
                    elem.logError("Invalid checkbox selected value (must be TRUE or FALSE).");
                    valid = false;
                }
            }

            if (enabledVarNameStr != null) {
                if (enabledVarValueStr == null) {
                    elem.logError("'enabled-var-name' present but 'enabled-var-value' absent");
                    valid = false;
                } else {
                    input.setEnabledVarName(enabledVarNameStr);

                    if (TRUE.equalsIgnoreCase(enabledVarValueStr)) {
                        input.setEnabledVarValue(Boolean.TRUE);
                    } else if (FALSE.equalsIgnoreCase(enabledVarValueStr)) {
                        input.setEnabledVarValue(Boolean.FALSE);
                    } else {
                        try {
                            input.setEnabledVarValue(Long.valueOf(enabledVarValueStr));
                        } catch (final NumberFormatException ex) {
                            elem.logError("Invalid 'enabled-var-value' attribute value");
                            valid = false;
                        }
                    }
                }
            } else if (enabledVarValueStr != null) {
                elem.logError("'enabled-var-value' present but 'enabled-var-name' absent");
                valid = false;
            }

            valid = valid && extractFormattable(elem, input);

            if (valid) {
                container.add(input);
            }
        } catch (final NumberFormatException e) {
            elem.logError("Invalid checkbox value.");
            valid = false;
        }

        return valid;
    }

    /**
     * Generate a {@code DocInput} object from XML source. Any errors encountered while generating the input object will
     * be reflected in the source file's error context.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param container   the span to which to add this input
     * @param mode        the parser mode
     * @return true if loading successful; false otherwise
     */
    private static boolean extractInput(final EvalContext evalContext, final EmptyElement elem,
                                        final AbstractDocSpanBase container, final EParserMode mode) {

        boolean valid = true;

        final String type = elem.getStringAttr(TYPE);
        if (type == null) {
            elem.logError("<input> element missing required 'type' attribute");
            valid = false;
        }

        final String name = elem.getStringAttr("name");
        if (name == null) {
            elem.logError("<input> element missing required 'name' attribute");
            valid = false;
        }

        Integer width = null;
        final String widthStr = elem.getStringAttr(WIDTH);
        if (widthStr != null) {
            try {
                width = Integer.valueOf(widthStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'width' value.");
                valid = false;
            }
        }

        final String enabledStr = elem.getStringAttr(ENABLED);
        Formula enabledF = null;
        if (enabledStr != null) {
            enabledF = FormulaFactory.parseFormulaString(evalContext, enabledStr, mode);
            if (enabledF == null) {
                elem.logError("Invalid 'enabled' formula.");
                valid = false;
            }
        }

        final String textValue = elem.getStringAttr(TEXT_VALUE);
        final String value = elem.getStringAttr(VALUE);
        final String defaultStr = elem.getStringAttr(DEFAULT);
        final String treatMinusAsStr = elem.getStringAttr(TREAT_MINUS_AS);
        final String styleStr = elem.getStringAttr(STYLE);
        final String enabledVarNameStr = elem.getStringAttr(ENABLED_VAR_NAME);
        final String enabledVarValueStr = elem.getStringAttr(ENABLED_VAR_VALUE);

        AbstractDocInput input = null;
        switch (type) {
            case INTEGER -> {
                final DocInputLongField longInput = new DocInputLongField(name);
                input = longInput;
                input.setEnabledFormula(enabledF);

                if (defaultStr != null) {
                    try {
                        longInput.defaultValue = Long.valueOf(defaultStr);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'default' attribute value");
                        valid = false;
                    }
                }

                if (treatMinusAsStr != null) {
                    try {
                        longInput.minusAs = Long.valueOf(treatMinusAsStr);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'treat-minus-as' attribute value");
                        valid = false;
                    }
                }

                if (textValue != null) {
                    longInput.setTextValue(textValue);
                }

                if (styleStr != null) {
                    if (BOX.equalsIgnoreCase(styleStr)) {
                        longInput.style = EFieldStyle.BOX;
                    } else if (UNDERLINE.equalsIgnoreCase(styleStr)) {
                        longInput.style = EFieldStyle.UNDERLINE;
                    } else {
                        elem.logError("Invalid 'style' attribute value");
                        valid = false;
                    }
                }

                longInput.width = width;

                if (value != null) {
                    try {
                        final Long longValue = Long.valueOf(value);
                        longInput.setOnlyLongValue(longValue);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'value' attribute value");
                        valid = false;
                    }
                }
            }
            case REAL -> {
                final DocInputDoubleField doubleInput = new DocInputDoubleField(name);
                input = doubleInput;
                input.setEnabledFormula(enabledF);

                if (defaultStr != null) {
                    try {
                        doubleInput.defaultValue = Double.valueOf(defaultStr);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'default' attribute value");
                        valid = false;
                    }
                }

                if (treatMinusAsStr != null) {
                    try {
                        doubleInput.minusAs = Double.valueOf(treatMinusAsStr);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'treat-minus-as' attribute value");
                        valid = false;
                    }
                }

                if (textValue != null && !textValue.isEmpty()) {
                    doubleInput.setTextValue(textValue);
                }

                if (styleStr != null) {
                    if (BOX.equalsIgnoreCase(styleStr)) {
                        doubleInput.style = EFieldStyle.BOX;
                    } else if (UNDERLINE.equalsIgnoreCase(styleStr)) {
                        doubleInput.style = EFieldStyle.UNDERLINE;
                    } else {
                        elem.logError("Invalid 'style' attribute value");
                        valid = false;
                    }
                }

                doubleInput.width = width;

                if (value != null) {
                    try {
                        final Double dblValue = Double.valueOf(value);
                        doubleInput.setOnlyDoubleValue(dblValue);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'value' attribute value");
                        valid = false;
                    }
                }
            }
            case STRING -> {
                final DocInputStringField stringInput = new DocInputStringField(name);
                input = stringInput;
                input.setEnabledFormula(enabledF);

                if (textValue != null && !textValue.isEmpty()) {
                    stringInput.setTextValue(textValue);
                }

                if (styleStr != null) {
                    if (BOX.equalsIgnoreCase(styleStr)) {
                        stringInput.style = EFieldStyle.BOX;
                    } else if (UNDERLINE.equalsIgnoreCase(styleStr)) {
                        stringInput.style = EFieldStyle.UNDERLINE;
                    } else {
                        elem.logError("Invalid 'style' attribute value");
                        valid = false;
                    }
                }

                stringInput.width = width;

                if (value != null) {
                    stringInput.setTextValue(value);
                }
            }
            case RADIO_BUTTON -> {

                try {
                    final int val = Long.valueOf(value).intValue();
                    final DocInputRadioButton radioInput = new DocInputRadioButton(name, val);
                    input = radioInput;
                    input.setEnabledFormula(enabledF);

                    final String selectedStr = elem.getStringAttr(SELECTED);
                    if (selectedStr != null) {
                        if (TRUE.equalsIgnoreCase(selectedStr)) {
                            radioInput.selectChoice();
                        } else if (!FALSE.equalsIgnoreCase(selectedStr)) {
                            elem.logError("Invalid radio button selected value (must be TRUE or FALSE).");
                            valid = false;
                        }
                    }
                } catch (final NumberFormatException e) {
                    elem.logError("Invalid radio button value.");
                    valid = false;
                }
            }
            case CHECKBOX -> {

                try {
                    final int val = Long.valueOf(value).intValue();
                    final DocInputCheckbox checkboxInput = new DocInputCheckbox(name, (long) val);
                    input = checkboxInput;
                    input.setEnabledFormula(enabledF);

                    final String selectedStr = elem.getStringAttr(SELECTED);
                    if (selectedStr != null) {
                        if (TRUE.equalsIgnoreCase(selectedStr)) {
                            checkboxInput.selectChoice();
                        } else if (!FALSE.equalsIgnoreCase(selectedStr)) {
                            elem.logError("Invalid checkbox selected value (must be TRUE or FALSE).");
                            valid = false;
                        }
                    }
                } catch (final NumberFormatException e) {
                    elem.logError("Invalid checkbox value.");
                    valid = false;
                }
            }
            case null, default -> {
                elem.logError("Unrecognized type of input.");
                valid = false;
            }
        }

        if (enabledVarNameStr != null) {
            if (enabledVarValueStr == null) {
                elem.logError("'enabled-var-name' present but 'enabled-var-value' absent");
                valid = false;
            } else if (input != null) {
                input.setEnabledVarName(enabledVarNameStr);

                if (TRUE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.TRUE);
                } else if (FALSE.equalsIgnoreCase(enabledVarValueStr)) {
                    input.setEnabledVarValue(Boolean.FALSE);
                } else {
                    try {
                        final Long varValue = Long.valueOf(enabledVarValueStr);
                        input.setEnabledVarValue(varValue);
                    } catch (final NumberFormatException ex) {
                        elem.logError("Invalid 'enabled-var-value' attribute value");
                        valid = false;
                    }
                }
            } else {
                elem.logError("input was not found");
            }
        } else if (enabledVarValueStr != null) {
            elem.logError("'enabled-var-value' present but 'enabled-var-name' absent");
            valid = false;
        }

        valid = valid && extractFormattable(elem, input);

        if (valid) {
            container.add(input);
        }

        return valid;
    }

    /**
     * Extract all formatting attributes from the XML tag and apply them to a {@code DocFormattable} object.
     *
     * @param elem the element
     * @param obj  the object to apply formatting to
     * @return true if successful; false if any error occurred
     */
    private static boolean extractFormattable(final IElement elem, final AbstractDocObjectTemplate obj) {

        boolean valid = true;

        final String colorStr = elem.getStringAttr(COLOR);
        if (colorStr != null) {
            if (ColorNames.isColorNameValid(colorStr)) {
                obj.setColorName(colorStr);
            } else {
                elem.logError("Unrecognized color: " + colorStr);
                valid = false;
            }
        }

        final String fontnameStr = elem.getStringAttr(FONT_NAME);
        if (fontnameStr != null) {
            if (BundledFontManager.getInstance().isFontNameValid(fontnameStr)) {
                obj.setFontName(fontnameStr);
            } else {
                elem.logError("Unrecognized font name: " + fontnameStr);
                valid = false;
            }
        }

        final String fontsizeStr = elem.getStringAttr(FONT_SIZE);
        if (fontsizeStr != null) {
            final int len = fontsizeStr.length();
            if ((int) fontsizeStr.charAt(len - 1) == '%') {
                try {
                    final String substring = fontsizeStr.substring(0, len - 1);
                    final int scale = Integer.parseInt(substring);

                    if (scale < 1) {
                        elem.logError("Font scale factor must be greater than zero.");
                        valid = false;
                    } else {
                        obj.setFontScale(((float) scale + 0.01f) / 100.0f);
                    }
                } catch (final NumberFormatException e) {
                    elem.logError("Unrecognized font scale factor: " + fontsizeStr);
                    valid = false;
                }
            } else {
                try {
                    final float size = Float.parseFloat(fontsizeStr);

                    if (size <= 0.0f) {
                        elem.logError("Font size must be greater than zero.");
                        valid = false;
                    } else {
                        obj.setFontSize(size);
                    }
                } catch (final NumberFormatException e) {
                    elem.logError("Unrecognized font size: " + fontsizeStr);
                    valid = false;
                }
            }
        }

        final String fontstyleStr = elem.getStringAttr(FONT_STYLE);

        if (fontstyleStr != null) {
            int style = AbstractDocObjectTemplate.PLAIN;

            if (!PLAIN.equalsIgnoreCase(fontstyleStr)) {
                final String[] split = fontstyleStr.split(CoreConstants.COMMA);

                for (final String entry : split) {
                    final String trimmed = entry.trim().toLowerCase(Locale.ROOT);

                    switch (trimmed) {
                        case BOLD -> style |= AbstractDocObjectTemplate.BOLD;
                        case ITALIC -> style |= AbstractDocObjectTemplate.ITALIC;
                        case UNDERLINE -> style |= AbstractDocObjectTemplate.UNDERLINE;
                        case OVERLINE -> style |= AbstractDocObjectTemplate.OVERLINE;
                        case STRIKETHROUGH -> style |= AbstractDocObjectTemplate.STRIKETHROUGH;
                        case BOXED -> style |= AbstractDocObjectTemplate.BOXED;
                        default -> {
                            elem.logError("Invalid font style.");
                            valid = false;
                        }
                    }
                }
            }

            if (valid) {
                final Integer styleInt = Integer.valueOf(style);
                obj.setFontStyle(styleInt);
            }
        }

        return valid;
    }

    /**
     * Convert any escape sequences found in a string into their corresponding special characters.
     *
     * @param node  the node to which to log any warnings encountered
     * @param value the string to convert
     * @param mode  the parser mode
     * @return the converted string, or null on any error
     */
    private static String unescape(final INode node, final String value, final EParserMode mode) {

        final int len = value.length();
        final HtmlBuilder htm = new HtmlBuilder(len);

        for (int i = 0; i < len; ++i) {
            final char ch = value.charAt(i);

            if ((int) ch == '&') {

                if (len > i + 3) {
                    final int ch1 = (int) value.charAt(i + 1);
                    final int ch2 = (int) value.charAt(i + 2);
                    final int ch3 = (int) value.charAt(i + 3);

                    if (ch1 == 'g' && ch2 == 't' && ch3 == ';') {
                        htm.add('>');
                        i += 3;
                    } else if (ch1 == 'l' && ch2 == 't' && ch3 == ';') {
                        htm.add('<');
                        i += 3;
                    } else if (len > i + 4) {
                        final int ch4 = (int) value.charAt(i + 4);

                        if (ch1 == 'a' && ch2 == 'm' && ch3 == 'p' && ch4 == ';') {
                            htm.add('&');
                            i += 4;
                        } else if (len > i + 5) {
                            final int ch5 = (int) value.charAt(i + 5);

                            if (ch1 == 'a' && ch2 == 'p' && ch3 == 'o' && ch4 == 's' && ch5 == ';') {
                                htm.add('\'');
                                i += 5;
                            } else if (ch1 == 'q' && ch2 == 'u' && ch3 == 'o' && ch4 == 't' && ch5 == ';') {
                                htm.add('\"');
                                i += 5;
                            } else {
                                htm.add('&');
                            }
                        } else {
                            htm.add('&');
                        }
                    } else {
                        htm.add('&');
                    }
                } else {
                    htm.add('&');
                }
            } else if ((int) ch == '\\' && len > i + 1 && value.charAt(i + 1) == 'u') {

                if (len > i + 5) {
                    final String valueStr = value.substring(i + 2, i + 6);
                    try {
                        if (mode.reportDeprecated) {
                            node.logError("Deprecated escape: \\u" + valueStr);
                        }
                        htm.add((char) Integer.parseInt(valueStr, 16));
                        i += 5;
                    } catch (final NumberFormatException e) {
                        node.logError("Invalid escape: \\u" + valueStr);
                    }
                }
            } else {
                htm.add(ch);
            }
        }

        return htm.toString();
    }

    /**
     * A utility function to test whether a character is XML whitespace, which is defined as space, tab, carriage return
     * or line-feed. This differs from the Java definition of whitespace, which includes several other characters (such
     * as vertical tab).
     *
     * @param codePoint the character to be tested
     * @return true if the character is whitespace; false otherwise
     */
    private static boolean isXmlWhitespace(final int codePoint) {

        return codePoint == ' ' || codePoint == '\t' || codePoint == '\n' || codePoint == '\r';
    }
}
