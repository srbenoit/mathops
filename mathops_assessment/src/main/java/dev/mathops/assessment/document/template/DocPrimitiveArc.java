package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.EArcFillStyle;
import dev.mathops.assessment.document.EArcRaysShown;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.inst.StrokeStyleInst;
import dev.mathops.assessment.document.inst.DocPrimitiveArcInst;
import dev.mathops.assessment.document.inst.RectangleShapeInst;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableFactory;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.font.FontSpec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.xml.INode;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * An arc primitive.
 *
 * <p>
 * The dimensions of an arc can be specified by either
 * <ul>
 *     <li>Bounding box: x, y, width, height</li>
 *     <li>Center and radius: cx, cy, r</li>
 *     <li>Center and axis radii: cx, cy, rx, ry</li>
 * </ul>
 * An arc has a start-angle and arc-angle, specified in degrees, where arc angle represents counter-clockwise rotation.
 *
 * <p>
 * An arc has a stroke-width, stroke-color, stroke-dash, and stroke-alpha, and a fill-style (none, pie, or chord), with
 * associated fill-color and fill-alpha.
 *
 * <p>
 * An arc may draw initial and/or terminal rays of a based on a rays-shown attribute (none, start, end, or both), with
 * associated ray-width, ray-length (in radii), ray-color, ray-dash, and ray-alpha.
 *
 * <p>
 * An arc may have a String or Span label attached, with associated label-color, label-alpha, font-name, font-size,
 * font-weight, and font-style.  A label-size attribute can adjust how far from the center the label is positioned
 * (positive to increase distance, negative to decrease).
 *
 * <p>
 * Finally, an arc may have arrowheads (drawn in the arc's stroke color) at one or both ends, defined by arrow-type
 * (none, start, end, or both), arrow-type (a selection of preset shapes), and arrow-size.
 */
final class DocPrimitiveArc extends AbstractDocRectangleShape {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4503475270187852064L;

    /** The start angle. */
    private NumberOrFormula startAngle = null;

    /** The arc angle. */
    private NumberOrFormula arcAngle = null;

    /** The stroke width. */
    private Double strokeWidth = null;

    /** The stroke color name. */
    private String strokeColorName = null;

    /** The stroke color. */
    private Color strokeColor = null;

    /** The stroke dash lengths (must be floats for BasicStroke class). */
    private float[] strokeDash = null;

    /** The stroke alpha. */
    private Double strokeAlpha = null;

    /** The fill style. */
    private EArcFillStyle fillStyle = null;

    /** The fill color name. */
    private String fillColorName = null;

    /** The fill color. */
    private Color fillColor = null;

    /** The fill alpha. */
    private Double fillAlpha = null;

    /** The rays shown. */
    private EArcRaysShown raysShown = null;

    /** The ray width. */
    private Double rayWidth = null;

    /** The ray length, as a multiple of radius. */
    private Double rayLength = null;

    /** The ray color name. */
    private String rayColorName = null;

    /** The ray color. */
    private Color rayColor = null;

    /** The ray dash lengths (must be floats for BasicStroke class). */
    private float[] rayDash = null;

    /** The ray alpha. */
    private Double rayAlpha = null;

    /** The raw label string as provided, before substituting entities and variables. */
    private String rawLabelString = null;

    /** The label string. */
    private String labelString = null;

    /** The label span. */
    private DocNonwrappingSpan labelSpan = null;

    /** Flag indicating glyph should come from STIX Text. */
    private boolean isStixText = false;

    /** Flag indicating glyph should come from STIX Math. */
    private boolean isStixMath = false;

    /** The label color name. */
    private String labelColorName = null;

    /** The label color. */
    private Color labelColor = null;

    /** The label alpha. */
    private Double labelAlpha = null;

    /** The label offset. */
    private Double labelOffset = null;

    /** The font name. */
    private String fontName = null;

    /** The font size. */
    private Double fontSize = null;

    /** The font style. */
    private Integer fontStyle = null;

    /** The font. */
    private Font font = null;

    /**
     * Construct a new {@code DocPrimitiveArc}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveArc(final AbstractDocPrimitiveContainer theOwner) {

        super(theOwner);
    }

    /**
     * Sets the start angle.
     *
     * @param theStartAngle the start angle
     */
    void setStartAngle(final NumberOrFormula theStartAngle) {

        this.startAngle = theStartAngle;
    }

    /**
     * Sets the arc angle.
     *
     * @param theArcAngle the arc angle
     */
    void setArcAngle(final NumberOrFormula theArcAngle) {

        this.arcAngle = theArcAngle;
    }

    /**
     * Gets the fill style.
     *
     * @return the fill style
     */
    private EArcFillStyle getFillStyle() {

        return this.fillStyle;
    }

    /**
     * Gets the stroke color name.
     *
     * @return the stroke color name
     */
    public String getStrokeColorName() {

        return this.strokeColorName;
    }

    /**
     * Gets the stroke alpha value.
     *
     * @return the stroke alpha value
     */
    public Double getStrokeAlpha() {

        return this.strokeAlpha;
    }

    /**
     * Sets the label span.
     *
     * @param theLabelSpan the label span
     */
    void setLabelSpan(final DocNonwrappingSpan theLabelSpan) {

        this.labelSpan = theLabelSpan;
        this.labelSpan.tag = "label";
    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @param theOwner the new owner
     * @return the copy
     */
    @Override
    public DocPrimitiveArc deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitiveArc copy = new DocPrimitiveArc(theOwner);

        final RectangleShapeTemplate myShape = getShape();
        if (myShape != null) {
            final RectangleShapeTemplate myShapeCopy = myShape.deepCopy();
            copy.setShape(myShapeCopy);
        }

        if (this.startAngle != null) {
            copy.startAngle = this.startAngle.deepCopy();
        }
        if (this.arcAngle != null) {
            copy.arcAngle = this.arcAngle.deepCopy();
        }

        copy.strokeWidth = this.strokeWidth;
        copy.strokeColorName = this.strokeColorName;
        copy.strokeColor = this.strokeColor;
        copy.strokeAlpha = this.strokeAlpha;

        if (this.strokeDash != null) {
            copy.strokeDash = this.strokeDash.clone();
        }

        copy.fillStyle = this.fillStyle;
        copy.fillColorName = this.fillColorName;
        copy.fillColor = this.fillColor;
        copy.fillAlpha = this.fillAlpha;

        copy.raysShown = this.raysShown;
        copy.rayWidth = this.rayWidth;
        copy.rayLength = this.rayLength;
        copy.rayColorName = this.rayColorName;
        copy.rayColor = this.rayColor;
        if (this.rayDash != null) {
            copy.rayDash = this.rayDash.clone();
        }
        copy.rayAlpha = this.rayAlpha;

        copy.rawLabelString = this.rawLabelString;
        copy.labelString = this.labelString;
        if (this.labelSpan != null) {
            copy.labelSpan = this.labelSpan.deepCopy();
        }
        copy.isStixText = this.isStixText;
        copy.isStixMath = this.isStixMath;

        copy.labelColorName = this.labelColorName;
        copy.labelColor = this.labelColor;
        copy.labelAlpha = this.labelAlpha;
        copy.labelOffset = this.labelOffset;

        copy.fontName = this.fontName;
        copy.fontSize = this.fontSize;
        copy.fontStyle = this.fontStyle;
        copy.font = this.font;

        copy.scale = this.scale;

        return copy;
    }

    /**
     * Set an attribute value used in drawing.
     *
     * @param name     the name of the attribute
     * @param theValue the attribute value
     * @param elem     an element to which to log errors
     * @param mode     the parser mode
     * @return true if the attribute was valid; false otherwise
     */
    boolean setAttr(final String name, final String theValue, final INode elem, final EParserMode mode) {

        boolean ok = false;

        if (theValue == null) {
            ok = true;
        } else {
            switch (name) {
                case "start-angle" -> {
                    this.startAngle = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                    ok = this.startAngle != null;
                }
                case "arc-angle" -> {
                    this.arcAngle = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                    ok = this.arcAngle != null;
                }
                case "stroke-width" -> {
                    this.strokeWidth = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.strokeWidth != null;
                }
                case "stroke-color", "color" -> {
                    if (mode.reportDeprecated && "color".equals(name)) {
                        elem.logError("Deprecated use of 'color'' on arc primitive - use stroke-color instead.");
                    }
                    if (ColorNames.isColorNameValid(theValue)) {
                        this.strokeColor = ColorNames.getColor(theValue);
                        this.strokeColorName = theValue;
                        ok = true;
                    } else {
                        elem.logError("Invalid '" + name + "' value (" + theValue + ") on arc primitive");
                    }
                }
                case "stroke-dash", "dash" -> {
                    if (mode.reportDeprecated && "dash".equals(name)) {
                        elem.logError("Deprecated use of 'dash' on arc primitive - use stroke-dash instead.");
                    }
                    final String[] split = theValue.split(CoreConstants.COMMA);
                    final int splitLen = split.length;

                    this.strokeDash = new float[splitLen];
                    for (int i = 0; i < splitLen; ++i) {
                        try {
                            this.strokeDash[i] = (float) Double.parseDouble(split[i]);
                            ok = true;
                        } catch (final NumberFormatException ex) {
                            // No action
                        }
                    }
                    if (!ok) {
                        elem.logError("Invalid '" + name + "' value (" + theValue + ") on arc primitive");
                    }
                }
                case "stroke-alpha", "alpha" -> {
                    if (mode.reportDeprecated && "alpha".equals(name)) {
                        elem.logError("Deprecated use of 'alpha' on arc primitive - use stroke-alpha instead.");
                    }
                    this.strokeAlpha = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.strokeAlpha != null;
                }
                case "filled" -> {
                    if (mode.reportDeprecated) {
                        elem.logError("Deprecated use of 'filled'' on arc primitive - use fill-style instead.");
                    }
                    try {
                        final Boolean filledBoolean = VariableFactory.parseBooleanValue(theValue);
                        this.fillStyle = filledBoolean.booleanValue() ? EArcFillStyle.PIE : EArcFillStyle.NONE;
                        ok = true;
                    } catch (final IllegalArgumentException ex) {
                        elem.logError("Invalid 'filled' value (" + theValue + ") on arc primitive");
                    }
                }
                case "fill-style" -> {
                    try {
                        final String uppercase = theValue.toUpperCase(Locale.ROOT);
                        this.fillStyle = EArcFillStyle.valueOf(uppercase);
                        ok = true;
                    } catch (final IllegalArgumentException ex) {
                        elem.logError("Invalid 'fill-style' value (" + theValue + ") on arc primitive");
                    }
                }
                case "fill-color" -> {

                    if (ColorNames.isColorNameValid(theValue)) {
                        this.fillColor = ColorNames.getColor(theValue);
                        this.fillColorName = theValue;
                        ok = true;
                    } else {
                        elem.logError("Invalid 'fill-color' value (" + theValue + ") on arc primitive");
                    }
                }
                case "fill-alpha" -> {
                    this.fillAlpha = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.fillAlpha != null;
                }
                case "rays-shown" -> {
                    try {
                        final String uppercase = theValue.toUpperCase(Locale.ROOT);
                        this.raysShown = EArcRaysShown.valueOf(uppercase);
                        ok = true;
                    } catch (final IllegalArgumentException ex) {
                        elem.logError("Invalid 'rays-shown' value (" + theValue + ") on arc primitive");
                    }
                }
                case "ray-width" -> {
                    this.rayWidth = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.rayWidth != null;
                }
                case "ray-length" -> {
                    this.rayLength = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.rayLength != null;
                }
                case "ray-color" -> {
                    if (ColorNames.isColorNameValid(theValue)) {
                        this.rayColor = ColorNames.getColor(theValue);
                        this.rayColorName = theValue;
                        ok = true;
                    } else {
                        elem.logError("Invalid '" + name + "' value (" + theValue + ") on arc primitive");
                    }
                }
                case "ray-dash" -> {
                    final String[] split = theValue.split(CoreConstants.COMMA);
                    final int splitLen = split.length;

                    this.rayDash = new float[splitLen];
                    for (int i = 0; i < splitLen; ++i) {
                        try {
                            this.rayDash[i] = (float) Double.parseDouble(split[i]);
                            ok = true;
                        } catch (final NumberFormatException ex) {
                            // No action
                        }
                    }
                    if (!ok) {
                        elem.logError("Invalid '" + name + "' value (" + theValue + ") on arc primitive");
                    }
                }
                case "ray-alpha" -> {
                    this.rayAlpha = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.rayAlpha != null;
                }
                case "label" -> {

                    this.rawLabelString = theValue;

                    // Identify and replace {\tag} tags for special characters
                    String processed = unescape(theValue);
                    int index = processed.indexOf("{\\");
                    while (index != -1) {
                        final int endIndex = processed.indexOf('}', index + 2);
                        if (endIndex != -1) {
                            final String cp = DocFactory.parseNamedEntity(processed.substring(index + 1, endIndex));

                            if (!cp.isEmpty()) {
                                processed = processed.substring(0, index) + cp + processed.substring(endIndex + 1);
                            }
                        }
                        index = processed.indexOf("{\\", index + 1);
                    }

                    this.labelString = processed;

                    if (this.labelString.length() == 1) {
                        final char ch = this.labelString.charAt(0);

                        this.isStixText = ch == '\u03C0' || ch == '\u03D1' || ch == '\u03D5' || ch == '\u03D6'
                                          || ch == '\u03F0' || ch == '\u03F1' || ch == '\u03F5' || ch == '\u2034'
                                          || ch == '\u2057';

                        this.isStixMath = ch == '\u21D0' || ch == '\u21D1' || ch == '\u21D2' || ch == '\u21D3'
                                          || ch == '\u21D4' || ch == '\u21D5' || ch == '\u2218' || ch == '\u221D'
                                          || ch == '\u2220' || ch == '\u2221' || ch == '\u2229' || ch == '\u222A'
                                          || ch == '\u2243' || ch == '\u2266' || ch == '\u2267' || ch == '\u2268'
                                          || ch == '\u2269' || ch == '\u226A' || ch == '\u226B' || ch == '\u226C'
                                          || ch == '\u226E' || ch == '\u226F' || ch == '\u2270' || ch == '\u2271'
                                          || ch == '\u2272' || ch == '\u2273' || ch == '\u2276' || ch == '\u2277'
                                          || ch == '\u227A' || ch == '\u227B' || ch == '\u227C' || ch == '\u227D'
                                          || ch == '\u227E' || ch == '\u227F' || ch == '\u2280' || ch == '\u2281'
                                          || ch == '\u22D6' || ch == '\u22D7' || ch == '\u22DA' || ch == '\u22DB'
                                          || ch == '\u22DE' || ch == '\u22DF' || ch == '\u22E0' || ch == '\u22E1'
                                          || ch == '\u22E6' || ch == '\u22E7' || ch == '\u22E8' || ch == '\u22E9'
                                          || ch == '\u22EF' || ch == '\u2322' || ch == '\u2323' || ch == '\u2329'
                                          || ch == '\u232A' || ch == '\u25B3' || ch == '\u2713' || ch == '\u27CB'
                                          || ch == '\u27CD' || ch == '\u27F8' || ch == '\u27F9' || ch == '\u27FA'
                                          || ch == '\u2A7D' || ch == '\u2A7E' || ch == '\u2A85' || ch == '\u2A86'
                                          || ch == '\u2A87' || ch == '\u2A88' || ch == '\u2A89' || ch == '\u2A8A'
                                          || ch == '\u2A8B' || ch == '\u2A8C' || ch == '\u2A95' || ch == '\u2A96'
                                          || ch == '\u2AA1' || ch == '\u2AA2' || ch == '\u2AAF' || ch == '\u2AB0'
                                          || ch == '\u2AB5' || ch == '\u2AB6' || ch == '\u2AB7' || ch == '\u2AB8'
                                          || ch == '\u2AB9' || ch == '\u2ABA' || ch == '\u2ADB';
                    } else {
                        this.isStixText = false;
                        this.isStixMath = false;
                    }

                    ok = true;
                }
                case "label-color" -> {
                    if (ColorNames.isColorNameValid(theValue)) {
                        this.labelColor = ColorNames.getColor(theValue);
                        this.labelColorName = theValue;
                        ok = true;
                    } else {
                        elem.logError("Invalid '" + name + "' value (" + theValue + ") on arc primitive");
                    }
                }
                case "label-alpha" -> {
                    this.labelAlpha = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.labelAlpha != null;
                }
                case "label-offset" -> {
                    this.labelOffset = parseDouble(theValue, elem, name, "arc primitive");
                    ok = this.labelOffset != null;
                }
                case "fontname" -> {

                    final BundledFontManager fonts = BundledFontManager.getInstance();

                    if (fonts.isFontNameValid(theValue)) {
                        this.fontName = theValue;
                        this.font = null;
                        ok = true;
                    } else {
                        elem.logError("Invalid 'fontname' value (" + theValue + ") on arc primitive");
                    }
                }
                case "fontsize" -> {

                    try {
                        this.fontSize = Double.valueOf(theValue);
                        ok = true;
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'fontsize' value (" + theValue + ") on arc primitive");
                    }
                }
                case "fontstyle" -> {
                    final String lower = theValue.toLowerCase(Locale.ROOT);

                    if ("plain".equals(lower)) {
                        this.fontStyle = Integer.valueOf(Font.PLAIN);
                        ok = true;
                    }

                    if ("bold".equals(lower)) {
                        this.fontStyle = Integer.valueOf(Font.BOLD);
                        ok = true;
                    }

                    if ("italic".equals(lower)) {
                        this.fontStyle = Integer.valueOf(Font.ITALIC);
                        ok = true;
                    }

                    if (("bold,italic".equals(lower)) || ("italic,bold".equals(lower))) {
                        this.fontStyle = Integer.valueOf(Font.ITALIC | Font.BOLD);
                        ok = true;
                    }

                    if (!ok) {
                        elem.logError("Invalid 'fontstyle' value (" + theValue + ") on arc primitive");
                    }
                }
                case null, default -> elem.logError("Unsupported attribute '" + name + "' on arc primitive");
            }
        }

        return ok;
    }

    /**
     * Generates the string contents of the label.
     *
     * @param theContext the evaluation context
     * @return the generated string (empty if this primitive has no content)
     */
    private String generateStringContents(final EvalContext theContext) {

        String work;
        AbstractVariable var;

        if (this.labelString == null) {
            work = CoreConstants.EMPTY;
        } else {
            // Substitute parameter values into text.
            work = this.labelString;

            boolean changed = true;
            while (changed) {
                final String prior = work;

                for (final String name : theContext.getVariableNames()) {
                    var = theContext.getVariable(name);

                    if (var != null) {
                        final String theValue = var.valueAsString();
                        final String newName = "{" + name + "}";
                        work = work.replace(newName, theValue);
                    }
                }

                changed = !prior.equals(work);
            }
        }

        return work;
    }

    /**
     * Draw the primitive.
     *
     * @param grx     the graphics on which to draw
     * @param context the evaluation context
     */
    @Override
    public void draw(final Graphics2D grx, final EvalContext context) {

        Object result;

        final Rectangle2D bounds = getBoundsRect(context);

        Number start = null;
        if (this.startAngle != null) {
            result = this.startAngle.evaluate(context);
            if (result instanceof final Number numResult) {
                start = numResult;
            }
        }

        Number arc = null;
        if (this.arcAngle != null) {
            result = this.arcAngle.evaluate(context);
            if (result instanceof final Number numResult) {
                arc = numResult;
            }
        }

        if (bounds != null && start != null && arc != null) {

            double x = bounds.getX() * (double) this.scale;
            double y = bounds.getY() * (double) this.scale;
            double w = bounds.getWidth() * (double) this.scale;
            double h = bounds.getHeight() * (double) this.scale;

            if (w < 0.0) {
                x += w;
                w = -w;
            }
            if (h < 0.0) {
                y += h;
                h = -h;
            }

            final double s = start.doubleValue();
            final double a = arc.doubleValue();

            final Composite origComp = grx.getComposite();
            final Stroke origStroke = grx.getStroke();

            // Do the fill first, if specified

            if (this.fillStyle != null && this.fillStyle != EArcFillStyle.NONE) {
                final int type = this.fillStyle == EArcFillStyle.PIE ? Arc2D.PIE : Arc2D.CHORD;
                final Shape arcFill = new Arc2D.Double(x, y, w, h, s, a, type);

                if (this.fillColor == null) {
                    grx.setColor(this.strokeColor == null ? Color.BLACK : this.strokeColor);
                } else {
                    grx.setColor(this.fillColor);
                }

                if (this.fillAlpha != null) {
                    final float fillAlphaValue = this.fillAlpha.floatValue();
                    if (fillAlphaValue < 1.0f) {
                        final float actual = Math.max(0.0f, fillAlphaValue);
                        final AlphaComposite fillComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, actual);
                        grx.setComposite(fillComp);
                    }
                }

                grx.fill(arcFill);
                grx.setComposite(origComp);
            }

            // Draw the arc next...

            // TODO: Arrowheads
            // TODO: Glow? Shadows?

            grx.setColor(this.strokeColor == null ? Color.BLACK : this.strokeColor);

            if (this.strokeAlpha != null) {
                final float alphaValue = this.strokeAlpha.floatValue();
                if (alphaValue < 1.0f) {
                    final float actual = Math.max(0.0f, alphaValue);
                    final AlphaComposite fillComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, actual);
                    grx.setComposite(fillComp);
                }
            }

            if (this.strokeWidth == null) {
                if (this.strokeDash == null) {
                    grx.setStroke(new BasicStroke(1.0f));
                } else {
                    grx.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                            this.strokeDash, 0.0f));
                }
            } else {
                final float strokeW = this.strokeWidth.floatValue();
                if (this.strokeDash == null) {
                    grx.setStroke(new BasicStroke(strokeW));
                } else {
                    grx.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                            this.strokeDash, 0.0f));
                }
            }

            final Shape arcShape = new Arc2D.Double(x, y, w, h, s, a, Arc2D.OPEN);

            grx.draw(arcShape);
            grx.setComposite(origComp);
            grx.setStroke(origStroke);

            // Draw rays next...
            if (this.raysShown != null && this.raysShown != EArcRaysShown.NONE) {

                if (this.rayColor == null) {
                    grx.setColor(this.strokeColor == null ? Color.BLACK : this.strokeColor);
                } else {
                    grx.setColor(this.rayColor);
                }

                if (this.rayAlpha != null) {
                    final float rayAlphaValue = this.rayAlpha.floatValue();
                    if (rayAlphaValue < 1.0f) {
                        final float actual = Math.max(0.0f, rayAlphaValue);
                        final AlphaComposite rayComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, actual);
                        grx.setComposite(rayComp);
                    }
                }

                if (this.rayWidth == null) {
                    if (this.rayDash == null) {
                        grx.setStroke(new BasicStroke(1.0f));
                    } else {
                        grx.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                                this.rayDash, 0.0f));
                    }
                } else {
                    final float rayW = this.rayWidth.floatValue();
                    if (this.rayDash == null) {
                        grx.setStroke(new BasicStroke(rayW));
                    } else {
                        grx.setStroke(new BasicStroke(rayW, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                                this.rayDash, 0.0f));
                    }
                }

                final double lengthScale = this.rayLength == null ? 1.0 : this.rayLength.doubleValue();
                final double halfWidth = bounds.getWidth() * 0.5;
                final double halfHeight = bounds.getHeight() * 0.5;
                final double cx = (bounds.getX() + halfWidth) * (double) this.scale;
                final double cy = (bounds.getY() + halfHeight) * (double) this.scale;

                final Shape rayShape;
                if (this.raysShown == EArcRaysShown.START) {
                    final double sRadians = Math.toRadians(s);
                    final double sCos = Math.cos(sRadians);
                    final double sSin = Math.sin(sRadians);
                    final double sdx = sCos * halfWidth * lengthScale * (double) this.scale;
                    final double sdy = sSin * halfHeight * lengthScale * (double) this.scale;
                    rayShape = new Line2D.Double(cx, cy, cx + sdx, cy - sdy);
                } else if (this.raysShown == EArcRaysShown.END) {
                    final double eRadians = Math.toRadians(s + a);
                    final double eCos = Math.cos(eRadians);
                    final double eSin = Math.sin(eRadians);
                    final double edx = eCos * halfWidth * lengthScale * (double) this.scale;
                    final double edy = eSin * halfHeight * lengthScale * (double) this.scale;
                    rayShape = new Line2D.Double(cx, cy, cx + edx, cy - edy);
                } else {
                    final double sRadians = Math.toRadians(s);
                    final double eRadians = Math.toRadians(s + a);
                    final double sCos = Math.cos(sRadians);
                    final double sSin = Math.sin(sRadians);
                    final double eCos = Math.cos(eRadians);
                    final double eSin = Math.sin(eRadians);
                    final double sdx = sCos * halfWidth * lengthScale * (double) this.scale;
                    final double sdy = sSin * halfHeight * lengthScale * (double) this.scale;
                    final double edx = eCos * halfWidth * lengthScale * (double) this.scale;
                    final double edy = eSin * halfHeight * lengthScale * (double) this.scale;
                    final Path2D path = new Path2D.Double();
                    path.moveTo(cx + sdx, cy - sdy);
                    path.lineTo(cx, cy);
                    path.lineTo(cx + edx, cy - edy);
                    rayShape = path;
                }

                grx.draw(rayShape);
                grx.setComposite(origComp);
                grx.setStroke(origStroke);
            }

            // Draw the label
            if (this.labelString != null || this.labelSpan != null) {

                grx.setColor(this.labelColor == null ? Color.BLACK : this.labelColor);

                if (this.labelAlpha != null) {
                    final float labelAlphaValue = this.labelAlpha.floatValue();
                    if (labelAlphaValue < 1.0f) {
                        final float actual = Math.max(0.0f, labelAlphaValue);
                        final AlphaComposite rayComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, actual);
                        grx.setComposite(rayComp);
                    }
                }

                final FontSpec spec = new FontSpec();

                if (this.fontName != null) {
                    spec.fontName = this.fontName;
                } else if (this.owner.getFontName() != null) {
                    spec.fontName = this.owner.getFontName();
                }

                if (this.fontSize != null) {
                    spec.fontSize = this.fontSize.doubleValue() * (double) this.scale;
                } else {
                    spec.fontSize = this.owner.getFontSize();
                }

                if (this.fontStyle != null) {
                    spec.fontStyle = this.fontStyle.intValue();
                } else {
                    spec.fontStyle = this.owner.getFontStyle();
                }

                final BundledFontManager bfm = BundledFontManager.getInstance();
                this.font = bfm.getFont(spec);

                final double halfWidth = bounds.getWidth() * 0.5;
                final double halfHeight = bounds.getHeight() * 0.5;
                final double cx = (bounds.getX() + halfWidth) * this.scale;
                final double cy = (bounds.getY() + halfHeight) * this.scale;

                final double midAngleDeg = (s + a * 0.5);
                final double midAngleRad = Math.toRadians(midAngleDeg);
                final double cosMidAngle = Math.cos(midAngleRad);
                final double sinMidAngle = Math.sin(midAngleRad);

                if (this.labelSpan == null) {
                    final int style = this.font.getStyle();
                    final int size = this.font.getSize();
                    if (this.isStixText) {
                        this.font = bfm.getFont("STIX Two Text Regular", size, style);
                    } else if (this.isStixMath) {
                        this.font = bfm.getFont("STIX Two Math Regular", size, style);
                    }

                    grx.setFont(Objects.requireNonNullElseGet(this.font, this.owner::getFont));

                    final String str = generateStringContents(context);
                    final FontRenderContext frc = grx.getFontRenderContext();
                    final GlyphVector vector = grx.getFont().createGlyphVector(frc, str);
                    final Rectangle2D visBounds = vector.getVisualBounds();
                    final double visW = visBounds.getWidth();
                    final double visH = visBounds.getHeight();

                    double xRad = halfWidth + visW * (double) this.scale;
                    double yRad = halfHeight + visH * (double) this.scale;
                    if (this.labelOffset != null) {
                        xRad += this.labelOffset.doubleValue();
                        yRad += this.labelOffset.doubleValue();
                    }
                    final double dx = xRad * cosMidAngle;
                    final double dy = yRad * sinMidAngle;

                    final int actualX = (int) Math.round(cx + dx - visW * 0.5);
                    final int actualY = (int) Math.round(cy - dy + visH * 0.5);

                    grx.drawString(str, actualX, actualY);
                } else {
                    this.labelSpan.doLayout(context, ELayoutMode.TEXT);

                    final double halfVisW = (double) this.labelSpan.getWidth() * 0.5;
                    final double halfVisH = (double) this.labelSpan.getHeight() * 0.5;

                    double xRad = halfWidth + halfVisW * 1.2 * (double) this.scale;
                    double yRad = halfHeight + halfVisH * 1.2 * (double) this.scale;
                    if (this.labelOffset != null) {
                        xRad += this.labelOffset.doubleValue();
                        yRad += this.labelOffset.doubleValue();
                    }
                    final double dx = xRad * cosMidAngle;
                    final double dy = yRad * sinMidAngle;

                    final int actualX = (int) Math.round(cx + dx - halfVisW);
                    final int actualY = (int) Math.round(cy - dy - halfVisH);

                    this.labelSpan.setX(actualX);
                    this.labelSpan.setY(actualY);
                    this.labelSpan.setColorName(this.labelColorName);
                    this.labelSpan.paintComponent(grx, ELayoutMode.TEXT);

                    this.labelSpan.setFontName(null);
                    this.labelSpan.setFontSize(0.0f);
                    this.labelSpan.setFontStyle(null);
                }

                grx.setComposite(origComp);
            }
        }
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    @Override
    public void doLayout(final EvalContext context) {

        // No action
    }

    /**
     * Generates an instance of this primitive based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance primitive object; null if unable to create the instance
     */
    @Override
    public DocPrimitiveArcInst createInstance(final EvalContext evalContext) {

        final RectangleShapeTemplate shape = getShape();
        DocPrimitiveArcInst result = null;

        if (Objects.nonNull(shape)) {
            final Object start = this.startAngle == null ? null : this.startAngle.evaluate(evalContext);
            final Object arc = this.arcAngle == null ? null : this.arcAngle.evaluate(evalContext);

            if (start instanceof final Number startNbr && arc instanceof final Number arcNbr) {

                final double strokeW = this.strokeWidth == null ? 0.0 : this.strokeWidth.doubleValue();
                final double alphaValue = this.strokeAlpha == null ? 1.0 : this.strokeAlpha.doubleValue();

                final StrokeStyleInst stroke = strokeW <= 0.0 ? null : new StrokeStyleInst(strokeW,
                        this.strokeColorName, this.strokeDash, alphaValue, EStrokeCap.BUTT, EStrokeJoin.MITER, 10.0f);

                final double fillAlphaValue = this.fillAlpha == null ? 1.0 : this.fillAlpha.doubleValue();
                final FillStyle fill = new FillStyle(this.fillColorName, fillAlphaValue);

                final RectangleShapeInst shapeInst = getShape().createInstance(evalContext);
                result = new DocPrimitiveArcInst(shapeInst, startNbr.doubleValue(), arcNbr.doubleValue(), stroke,
                        this.fillStyle, fill);
            }
        }

        return result;
    }

    /**
     * Write the XML representation of the object to a {@code v}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);
        final String ind2 = makeIndent(indent + 1);

        xml.add(ind, "<arc");

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.addAttributes(xml);
        }

        if (this.startAngle != null && this.startAngle.getNumber() != null) {
            xml.add(" start-angle=\"", this.startAngle.getNumber(), CoreConstants.QUOTE);
        }

        if (this.arcAngle != null && this.arcAngle.getNumber() != null) {
            xml.add(" arc-angle=\"", this.arcAngle.getNumber(), CoreConstants.QUOTE);
        }

        if (this.strokeWidth != null) {
            xml.add(" stroke-width=\"", this.strokeWidth, CoreConstants.QUOTE);
        }

        if (this.strokeColorName != null) {
            xml.add(" stroke-color=\"", this.strokeColorName, CoreConstants.QUOTE);
        }

        if (this.strokeDash != null) {
            final int dashlen = this.strokeDash.length;
            if (dashlen > 0) {
                xml.add(" stroke-dash=\"", Float.toString(this.strokeDash[0]));
                for (int i = 1; i < dashlen; ++i) {
                    xml.add(CoreConstants.COMMA, Float.toString(this.strokeDash[i]));
                }
                xml.add('"');
            }
        }

        if (this.strokeAlpha != null) {
            xml.add(" stroke-alpha=\"", this.strokeAlpha, CoreConstants.QUOTE);
        }

        if (this.fillStyle != null) {
            xml.add(" fill-style=\"", this.fillStyle, CoreConstants.QUOTE);
        }

        if (this.fillColorName != null) {
            xml.add(" fill-color=\"", this.fillColorName, CoreConstants.QUOTE);
        }

        if (this.fillAlpha != null) {
            xml.add(" fill-alpha=\"", this.fillAlpha, CoreConstants.QUOTE);
        }

        if (this.raysShown != null) {
            xml.add(" rays-shown=\"", this.raysShown, CoreConstants.QUOTE);
        }

        if (this.rayWidth != null) {
            xml.add(" ray-width=\"", this.rayWidth, CoreConstants.QUOTE);
        }

        if (this.rayLength != null) {
            xml.add(" ray-length=\"", this.rayLength, CoreConstants.QUOTE);
        }

        if (this.rayColorName != null) {
            xml.add(" ray-color=\"", this.rayColorName, CoreConstants.QUOTE);
        }

        if (this.rayDash != null) {
            final int dashlen = this.rayDash.length;
            if (dashlen > 0) {
                xml.add(" ray-dash=\"", Float.toString(this.rayDash[0]));

                for (int i = 1; i < dashlen; ++i) {
                    xml.add(CoreConstants.COMMA, Float.toString(this.rayDash[i]));
                }

                xml.add('"');
            }
        }

        if (this.rayAlpha != null) {
            xml.add(" ray-alpha=\"", this.rayAlpha, CoreConstants.QUOTE);
        }

        xml.addAttribute("label", this.rawLabelString, 0);
        xml.addAttribute("label-color", this.labelColorName, 0);

        if (this.labelAlpha != null) {
            xml.add(" label-alpha=\"", this.labelAlpha, CoreConstants.QUOTE);
        }
        if (this.labelOffset != null) {
            xml.add(" label-offset=\"", this.labelOffset, CoreConstants.QUOTE);
        }

        xml.addAttribute("fontname", this.fontName, 0);

        if (this.fontSize != null) {
            xml.add(" fontsize=\"", this.fontSize, CoreConstants.QUOTE);
        }

        if (this.fontStyle != null) {
            xml.add(" fontstyle=\"");

            switch (this.fontStyle.intValue()) {

                case 0:
                    xml.add("plain");
                    break;

                case Font.BOLD:
                    xml.add("bold");
                    break;

                case Font.ITALIC:
                    xml.add("italic");
                    break;

                case Font.BOLD | Font.ITALIC:
                    xml.add("bold,italic");
                    break;

                default:
                    break;
            }

            xml.add('"');
        }

        // If there will be no expression child elements, make this an empty element
        if ((shape == null || shape.isConstant())
            && (this.startAngle == null || this.startAngle.getFormula() == null)
            && (this.arcAngle == null || this.arcAngle.getFormula() == null)
            && this.labelSpan == null) {
            xml.addln("/>");
        } else {
            xml.addln(">");

            if (shape != null) {
                shape.addChildElements(xml, indent + 1);
            }

            if (this.startAngle != null && this.startAngle.getFormula() != null) {
                xml.add(ind2, "<start-angle>");
                this.startAngle.getFormula().appendChildrenXml(xml);
                xml.addln("</start-angle>");
            }

            if (this.arcAngle != null && this.arcAngle.getFormula() != null) {
                xml.add(ind2, "<arc-angle>");
                this.arcAngle.getFormula().appendChildrenXml(xml);
                xml.addln("</arc-angle>");
            }

            if (this.labelSpan != null) {
                final String spanXml = this.labelSpan.toXml(0);
                xml.addln(ind2, spanXml);
            }

            xml.addln(ind, "</arc>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Arc";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.accumulateParameterNames(set);
        }

        if (this.startAngle != null && this.startAngle.getFormula() != null) {
            set.addAll(this.startAngle.getFormula().params.keySet());
        }

        if (this.arcAngle != null && this.arcAngle.getFormula() != null) {
            set.addAll(this.arcAngle.getFormula().params.keySet());
        }

        if (this.labelString != null) {
            AbstractDocObjectTemplate.scanStringForParameterReferences(this.labelString, set);
        }
        if (this.labelSpan != null) {
            this.labelSpan.accumulateParameterNames(set);
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(getShape())
               + Objects.hashCode(this.startAngle)
               + Objects.hashCode(this.arcAngle)
               + Objects.hashCode(this.strokeWidth)
               + Objects.hashCode(this.strokeColorName)
               + Objects.hashCode(this.strokeColor)
               + Objects.hashCode(this.strokeAlpha)
               + Objects.hashCode(this.strokeDash)
               + Objects.hashCode(this.fillStyle)
               + Objects.hashCode(this.fillColorName)
               + Objects.hashCode(this.fillColor)
               + Objects.hashCode(this.fillAlpha)
               + Objects.hashCode(this.raysShown)
               + Objects.hashCode(this.rayWidth)
               + Objects.hashCode(this.rayLength)
               + Objects.hashCode(this.rayColorName)
               + Objects.hashCode(this.rayColor)
               + Objects.hashCode(this.rayDash)
               + Objects.hashCode(this.rayAlpha);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocPrimitiveArc arc) {
            equal = Objects.equals(getShape(), arc.getShape())
                    && Objects.equals(this.startAngle, arc.startAngle)
                    && Objects.equals(this.arcAngle, arc.arcAngle)
                    && Objects.equals(this.strokeWidth, arc.strokeWidth)
                    && Objects.equals(this.strokeColorName, arc.strokeColorName)
                    && Objects.equals(this.strokeColor, arc.strokeColor)
                    && Objects.equals(this.strokeAlpha, arc.strokeAlpha)
                    && Arrays.equals(this.strokeDash, arc.strokeDash)
                    && this.fillStyle == arc.fillStyle
                    && Objects.equals(this.fillColorName, arc.fillColorName)
                    && Objects.equals(this.fillColor, arc.fillColor)
                    && Objects.equals(this.fillAlpha, arc.fillAlpha)
                    && this.raysShown == arc.raysShown
                    && Objects.equals(this.rayWidth, arc.rayWidth)
                    && Objects.equals(this.rayLength, arc.rayLength)
                    && Objects.equals(this.rayColorName, arc.rayColorName)
                    && Objects.equals(this.rayColor, arc.rayColor)
                    && Arrays.equals(this.rayDash, arc.rayDash)
                    && Objects.equals(this.rayAlpha, arc.rayAlpha);
        } else {
            equal = false;
        }

        return equal;
    }
}
