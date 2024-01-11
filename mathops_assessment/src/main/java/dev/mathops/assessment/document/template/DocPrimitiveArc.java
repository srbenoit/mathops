package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EArcFillStyle;
import dev.mathops.assessment.document.EArcRaysShown;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.DocPrimitiveArcInst;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableFactory;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.font.FontSpec;

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
final class DocPrimitiveArc extends AbstractDocPrimitive {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4503475270187852064L;

    /** The x coordinate. */
    private NumberOrFormula xCoord;

    /** The y coordinate. */
    private NumberOrFormula yCoord;

    /** The width. */
    private NumberOrFormula width;

    /** The height. */
    private NumberOrFormula height;

    /** The center x coordinate. */
    private NumberOrFormula centerX;

    /** The center y coordinate. */
    private NumberOrFormula centerY;

    /** The radius. */
    private NumberOrFormula radius;

    /** The x-axis radius. */
    private NumberOrFormula xRadius;

    /** The y-axis radius. */
    private NumberOrFormula yRadius;

    /** The start angle. */
    private NumberOrFormula startAngle;

    /** The arc angle. */
    private NumberOrFormula arcAngle;

    /** The stroke width. */
    private Double strokeWidth;

    /** The stroke color name. */
    private String strokeColorName;

    /** The stroke color. */
    private Color strokeColor;

    /** The stroke dash lengths (must be floats for BasicStroke class). */
    private float[] strokeDash;

    /** The stroke alpha. */
    private Double strokeAlpha;

    /** The fill style. */
    private EArcFillStyle fillStyle;

    /** The fill color name. */
    private String fillColorName;

    /** The fill color. */
    private Color fillColor;

    /** The fill alpha. */
    private Double fillAlpha;

    /** The rays shown. */
    private EArcRaysShown raysShown;

    /** The ray width. */
    private Double rayWidth;

    /** The ray length, as a multiple of radius. */
    private Double rayLength;

    /** The ray color name. */
    private String rayColorName;

    /** The ray color. */
    private Color rayColor;

    /** The ray dash lengths (must be floats for BasicStroke class). */
    private float[] rayDash;

    /** The ray alpha. */
    private Double rayAlpha;

    /** The raw label string as provided, before substituting entities and variables. */
    private String rawLabelString;

    /** The label string. */
    private String labelString;

    /** The label span. */
    private DocNonwrappingSpan labelSpan;

    /** Flag indicating glyph should come from STIX Text. */
    private boolean isStixText;

    /** Flag indicating glyph should come from STIX Math. */
    private boolean isStixMath;

    /** The label color name. */
    private String labelColorName;

    /** The label color. */
    private Color labelColor;

    /** The label alpha. */
    private Double labelAlpha;

    /** The label offset. */
    private Double labelOffset;

    /** The font name. */
    private String fontName;

    /** The font size. */
    private Double fontSize;

    /** The font style. */
    private Integer fontStyle;

    /** The font. */
    private Font font;

    /**
     * Construct a new {@code DocPrimitiveArc}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveArc(final AbstractDocPrimitiveContainer theOwner) {

        super(theOwner);
    }

    /**
     * Sets the x coordinate.
     *
     * @param theXCoord the x coordinate
     */
    void setXCoord(final NumberOrFormula theXCoord) {

        this.xCoord = theXCoord;
    }

    /**
     * Sets the y coordinate.
     *
     * @param theYCoord the y coordinate
     */
    void setYCoord(final NumberOrFormula theYCoord) {

        this.yCoord = theYCoord;
    }

    /**
     * Sets the start angle.
     *
     * @param theWidth the start angle
     */
    public void setWidth(final NumberOrFormula theWidth) {

        this.width = theWidth;
    }

    /**
     * Gets the start angle.
     *
     * @return the start angle
     */
    public NumberOrFormula getWidth() {

        return this.width;
    }

    /**
     * Sets the height.
     *
     * @param theHeight the height
     */
    public void setHeight(final NumberOrFormula theHeight) {

        this.height = theHeight;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public NumberOrFormula getHeight() {

        return this.height;
    }

    /**
     * Sets the center x coordinate.
     *
     * @param theCenterX the center x coordinate
     */
    void setCenterX(final NumberOrFormula theCenterX) {

        this.centerX = theCenterX;
    }

    /**
     * Sets the center y coordinate.
     *
     * @param theCenterY the center y coordinate
     */
    void setCenterY(final NumberOrFormula theCenterY) {

        this.centerY = theCenterY;
    }

    /**
     * Sets the radius.
     *
     * @param theRadius the radius
     */
    void setRadius(final NumberOrFormula theRadius) {

        this.radius = theRadius;
    }

    /**
     * Sets the X radius.
     *
     * @param theXRadius the X radius
     */
    void setXRadius(final NumberOrFormula theXRadius) {

        this.xRadius = theXRadius;
    }

    /**
     * Sets the Y radius.
     *
     * @param theYRadius the Y radius
     */
    void setYRadius(final NumberOrFormula theYRadius) {

        this.yRadius = theYRadius;
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

        if (this.xCoord != null) {
            copy.xCoord = this.xCoord.deepCopy();
        }
        if (this.yCoord != null) {
            copy.yCoord = this.yCoord.deepCopy();
        }
        if (this.width != null) {
            copy.width = this.width.deepCopy();
        }
        if (this.height != null) {
            copy.height = this.height.deepCopy();
        }
        if (this.centerX != null) {
            copy.centerX = this.centerX.deepCopy();
        }
        if (this.centerY != null) {
            copy.centerY = this.centerY.deepCopy();
        }
        if (this.radius != null) {
            copy.radius = this.radius.deepCopy();
        }
        if (this.xRadius != null) {
            copy.xRadius = this.xRadius.deepCopy();
        }
        if (this.yRadius != null) {
            copy.yRadius = this.yRadius.deepCopy();
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
            if ("x".equals(name)) {
                this.xCoord = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.xCoord != null;
            } else if ("y".equals(name)) {
                this.yCoord = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.yCoord != null;
            } else if ("width".equals(name)) {
                this.width = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.width != null;
            } else if ("height".equals(name)) {
                this.height = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.height != null;
            } else if ("cx".equals(name)) {
                this.centerX = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.centerX != null;
            } else if ("cy".equals(name)) {
                this.centerY = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.centerY != null;
            } else if ("r".equals(name)) {
                this.radius = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.radius != null;
            } else if ("rx".equals(name)) {
                this.xRadius = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.xRadius != null;
            } else if ("ry".equals(name)) {
                this.yRadius = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.yRadius != null;
            } else if ("start-angle".equals(name)) {
                this.startAngle = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.startAngle != null;
            } else if ("arc-angle".equals(name)) {
                this.arcAngle = parseNumberOrFormula(theValue, elem, mode, name, "arc primitive");
                ok = this.arcAngle != null;
            } else if ("stroke-width".equals(name)) {
                this.strokeWidth = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.strokeWidth != null;
            } else if ("stroke-color".equals(name) || "color".equals(name)) {
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
            } else if ("stroke-dash".equals(name) || "dash".equals(name)) {
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
            } else if ("stroke-alpha".equals(name) || "alpha".equals(name)) {
                if (mode.reportDeprecated && "alpha".equals(name)) {
                    elem.logError("Deprecated use of 'alpha' on arc primitive - use stroke-alpha instead.");
                }
                this.strokeAlpha = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.strokeAlpha != null;
            } else if ("filled".equals(name)) {
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
            } else if ("fill-style".equals(name)) {
                try {
                    final String uppercase = theValue.toUpperCase(Locale.ROOT);
                    this.fillStyle = EArcFillStyle.valueOf(uppercase);
                    ok = true;
                } catch (final IllegalArgumentException ex) {
                    elem.logError("Invalid 'fill-style' value (" + theValue + ") on arc primitive");
                }
            } else if ("fill-color".equals(name)) {

                if (ColorNames.isColorNameValid(theValue)) {
                    this.fillColor = ColorNames.getColor(theValue);
                    this.fillColorName = theValue;
                    ok = true;
                } else {
                    elem.logError("Invalid 'fill-color' value (" + theValue + ") on arc primitive");
                }
            } else if ("fill-alpha".equals(name)) {
                this.fillAlpha = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.fillAlpha != null;
            } else if ("rays-shown".equals(name)) {
                try {
                    final String uppercase = theValue.toUpperCase(Locale.ROOT);
                    this.raysShown = EArcRaysShown.valueOf(uppercase);
                    ok = true;
                } catch (final IllegalArgumentException ex) {
                    elem.logError("Invalid 'rays-shown' value (" + theValue + ") on arc primitive");
                }

            } else if ("ray-width".equals(name)) {
                this.rayWidth = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.rayWidth != null;
            } else if ("ray-length".equals(name)) {
                this.rayLength = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.rayLength != null;
            } else if ("ray-color".equals(name)) {
                if (ColorNames.isColorNameValid(theValue)) {
                    this.rayColor = ColorNames.getColor(theValue);
                    this.rayColorName = theValue;
                    ok = true;
                } else {
                    elem.logError("Invalid '" + name + "' value (" + theValue + ") on arc primitive");
                }
            } else if ("ray-dash".equals(name)) {
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
            } else if ("ray-alpha".equals(name)) {
                this.rayAlpha = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.rayAlpha != null;
            } else if ("label".equals(name)) {

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

                    this.isStixMath = ch == '\u2218' || ch == '\u221D' || ch == '\u2220' || ch == '\u2221'
                            || ch == '\u2229' || ch == '\u222A' || ch == '\u2243' || ch == '\u2266'
                            || ch == '\u2267' || ch == '\u2268' || ch == '\u2269' || ch == '\u226A'
                            || ch == '\u226B' || ch == '\u226C' || ch == '\u226E' || ch == '\u226F'
                            || ch == '\u2270' || ch == '\u2271' || ch == '\u2272' || ch == '\u2273'
                            || ch == '\u2276' || ch == '\u2277' || ch == '\u227A' || ch == '\u227B'
                            || ch == '\u227C' || ch == '\u227D' || ch == '\u227E' || ch == '\u227F'
                            || ch == '\u2280' || ch == '\u2281' || ch == '\u22D6' || ch == '\u22D7'
                            || ch == '\u22DA' || ch == '\u22DB' || ch == '\u22DE' || ch == '\u22DF'
                            || ch == '\u22E0' || ch == '\u22E1' || ch == '\u22E6' || ch == '\u22E7'
                            || ch == '\u22E8' || ch == '\u22E9' || ch == '\u22EF' || ch == '\u2322'
                            || ch == '\u2323' || ch == '\u2329' || ch == '\u232A' || ch == '\u25B3'
                            || ch == '\u2713' || ch == '\u27CB' || ch == '\u27CD' || ch == '\u27F8'
                            || ch == '\u27F9' || ch == '\u2A7D' || ch == '\u2A7E' || ch == '\u2A85'
                            || ch == '\u2A86' || ch == '\u2A87' || ch == '\u2A88' || ch == '\u2A89'
                            || ch == '\u2A8A' || ch == '\u2A8B' || ch == '\u2A8C' || ch == '\u2A95'
                            || ch == '\u2A96' || ch == '\u2AA1' || ch == '\u2AA2' || ch == '\u2AAF'
                            || ch == '\u2AB0' || ch == '\u2AB5' || ch == '\u2AB6' || ch == '\u2AB7'
                            || ch == '\u2AB8' || ch == '\u2AB9' || ch == '\u2ABA' || ch == '\u2ADB';
                } else {
                    this.isStixText = false;
                    this.isStixMath = false;
                }

                ok = true;
            } else if ("label-color".equals(name)) {
                if (ColorNames.isColorNameValid(theValue)) {
                    this.labelColor = ColorNames.getColor(theValue);
                    this.labelColorName = theValue;
                    ok = true;
                } else {
                    elem.logError("Invalid '" + name + "' value (" + theValue + ") on arc primitive");
                }
            } else if ("label-alpha".equals(name)) {
                this.labelAlpha = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.labelAlpha != null;
            } else if ("label-offset".equals(name)) {
                this.labelOffset = parseDouble(theValue, elem, name, "arc primitive");
                ok = this.labelOffset != null;
            } else if ("fontname".equals(name)) {

                final BundledFontManager fonts = BundledFontManager.getInstance();

                if (fonts.isFontNameValid(theValue)) {
                    this.fontName = theValue;
                    this.font = null;
                    ok = true;
                } else {
                    elem.logError("Invalid 'fontname' value (" + theValue + ") on arc primitive");
                }
            } else if ("fontsize".equals(name)) {

                try {
                    this.fontSize = Double.valueOf(theValue);
                    ok = true;
                } catch (final NumberFormatException e) {
                    elem.logError("Invalid 'fontsize' value (" + theValue + ") on arc primitive");
                }
            } else if ("fontstyle".equals(name)) {
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
            } else {
                elem.logError("Unsupported attribute '" + name + "' on arc primitive");
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

            final double x = bounds.getX() * (double) this.scale;
            final double y = bounds.getY() * (double) this.scale;
            final double w = bounds.getWidth() * (double) this.scale;
            final double h = bounds.getHeight() * (double) this.scale;
            final double s = start.doubleValue();
            final double a = arc.doubleValue();

            final Composite origComp = grx.getComposite();
            final Stroke origStroke = grx.getStroke();

            // Do the fill first, if specified

            if (this.fillStyle != null && this.fillStyle != EArcFillStyle.NONE) {
                final int type = this.fillStyle == EArcFillStyle.PIE ? Arc2D.PIE :Arc2D.CHORD;
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
                final double cx = bounds.getX() + halfWidth;
                final double cy = bounds.getY() + halfHeight;

                final Shape rayShape;
                if (this.raysShown == EArcRaysShown.START) {
                    final double sRadians = Math.toRadians(s);
                    final double sCos = Math.cos(sRadians);
                    final double sSin = Math.sin(sRadians);
                    final double sdx = sCos * halfWidth * lengthScale;
                    final double sdy = sSin * halfHeight * lengthScale;
                    rayShape = new Line2D.Double(cx, cy, cx + sdx, cy - sdy);
                } else if (this.raysShown == EArcRaysShown.END) {
                    final double eRadians = Math.toRadians(s + a);
                    final double eCos = Math.cos(eRadians);
                    final double eSin = Math.sin(eRadians);
                    final double edx = eCos * halfWidth * lengthScale;
                    final double edy = eSin * halfHeight * lengthScale;
                    rayShape = new Line2D.Double(cx, cy, cx + edx, cy - edy);
                } else {
                    final double sRadians = Math.toRadians(s);
                    final double eRadians = Math.toRadians(s + a);
                    final double sCos = Math.cos(sRadians);
                    final double sSin = Math.sin(sRadians);
                    final double eCos = Math.cos(eRadians);
                    final double eSin = Math.sin(eRadians);
                    final double sdx = sCos * halfWidth * lengthScale;
                    final double sdy = sSin * halfHeight * lengthScale;
                    final double edx = eCos * halfWidth * lengthScale;
                    final double edy = eSin * halfHeight * lengthScale;
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
                    spec.fontSize = (double) this.owner.getFontSize();
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
                final double cx = bounds.getX() + halfWidth;
                final double cy = bounds.getY() + halfHeight;

                final double midAngleDeg = (s + a * 0.5);
                final double midAngleRad = Math.toRadians(midAngleDeg);
                final double cosMidAngle = Math.cos(midAngleRad);
                final double sinMidAngle = Math.sin(midAngleRad);

                if (this.labelSpan == null) {
                    final int style = this.font.getStyle();
                    final int size = this.font.getSize();
                    if (this.isStixText) {
                        this.font = bfm.getFont("STIX Two Text Regular", (double) size, style);
                    } else if (this.isStixMath) {
                        this.font = bfm.getFont("STIX Two Math Regular", (double) size, style);
                    }

                    grx.setFont(Objects.requireNonNullElseGet(this.font, this.owner::getFont));

                    final String str = generateStringContents(context);
                    final FontRenderContext frc = grx.getFontRenderContext();
                    final GlyphVector vector = grx.getFont().createGlyphVector(frc, str);
                    final Rectangle2D visBounds = vector.getVisualBounds();
                    final double visW = visBounds.getWidth();
                    final double visH = visBounds.getHeight();

                    double xRad = halfWidth + visW;
                    double yRad = halfHeight + visH;
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

                    double xRad = halfWidth + halfVisW * 1.2;
                    double yRad = halfHeight + halfVisH * 1.2;
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
     * Determines the bounding rectangle from attribute settings.
     *
     * @param context the evaluation context
     * @return the bounding rectangle; {@code null} if that rectangle could not be determined
     */
    private Rectangle2D getBoundsRect(final EvalContext context) {

        Number x = null;
        if (this.xCoord != null) {
            final Object result = this.xCoord.evaluate(context);
            if (result instanceof final Number numResult) {
                x = numResult;
            }
        }

        Number y = null;
        if (this.yCoord != null) {
            final Object result = this.yCoord.evaluate(context);
            if (result instanceof final Number numResult) {
                y = numResult;
            }
        }

        Number w = null;
        if (this.width != null) {
            final Object result = this.width.evaluate(context);
            if (result instanceof final Number numResult) {
                w = numResult;
            }
        }

        Number h = null;
        if (this.height != null) {
            final Object result = this.height.evaluate(context);
            if (result instanceof final Number numResult) {
                h = numResult;
            }
        }

        Rectangle2D bounds = null;

        // TODO: Allow the {cx, cy, width, height} combination or perhaps even {[x|cx], [y|cy], width, height}

        if (x == null || y == null || w == null || h == null) {

            Number cx = null;
            if (this.centerX != null) {
                final Object result = this.centerX.evaluate(context);
                if (result instanceof final Number numResult) {
                    cx = numResult;
                }
            }

            Number cy = null;
            if (this.centerY != null) {
                final Object result = this.centerY.evaluate(context);
                if (result instanceof final Number numResult) {
                    cy = numResult;
                }
            }

            if (cx == null || cy == null) {
                Log.warning("Arc bounding rectangle is not sufficiently specified (cx=", cx, ",cy=", cy, ")");
            } else {
                Number r = null;
                if (this.radius != null) {
                    final Object result = this.radius.evaluate(context);
                    if (result instanceof final Number numResult) {
                        r = numResult;
                    }
                }

                if (r == null) {
                    Number rx = null;
                    if (this.xRadius != null) {
                        final Object result = this.xRadius.evaluate(context);
                        if (result instanceof final Number numResult) {
                            rx = numResult;
                        }
                    }

                    Number ry = null;
                    if (this.yRadius != null) {
                        final Object result = this.yRadius.evaluate(context);
                        if (result instanceof final Number numResult) {
                            ry = numResult;
                        }
                    }

                    if (rx == null || ry == null) {
                        Log.warning("Arc bounding rectangle is not sufficiently specified.");
                    } else {
                        final double cxd = cx.doubleValue();
                        final double cyd = cy.doubleValue();
                        final double rxd = rx.doubleValue();
                        final double wd = rxd * 2.0;
                        final double ryd = ry.doubleValue();
                        final double hd = ryd * 2.0;
                        bounds = new Rectangle2D.Double(cxd - rxd, cyd - ryd, wd, hd);
                    }
                } else {
                    final double cxd = cx.doubleValue();
                    final double cyd = cy.doubleValue();
                    final double rd = r.doubleValue();
                    final double wd = rd * 2.0;
                    bounds = new Rectangle2D.Double(cxd - rd, cyd - rd, wd, wd);
                }
            }
        } else {
            final double xd = x.doubleValue();
            final double yd = y.doubleValue();
            final double wd = w.doubleValue();
            final double hd = h.doubleValue();
            bounds = new Rectangle2D.Double(xd, yd, wd, hd);
        }

        return bounds;
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

        final Object xVal = this.xCoord == null ? null : this.xCoord.evaluate(evalContext);
        final Object yVal = this.yCoord == null ? null : this.yCoord.evaluate(evalContext);
        final Object wVal = this.width == null ? null : this.width.evaluate(evalContext);
        final Object hVal = this.height == null ? null : this.height.evaluate(evalContext);
        final Object start = this.startAngle == null ? null : this.startAngle.evaluate(evalContext);
        final Object arc = this.arcAngle == null ? null : this.arcAngle.evaluate(evalContext);

        final DocPrimitiveArcInst result;

        if (xVal instanceof final Number xNbr && yVal instanceof final Number yNbr
                && wVal instanceof final Number wNbr && hVal instanceof final Number hNbr
                && start instanceof final Number startNbr && arc instanceof final Number arcNbr) {

            final BoundingRect rect = new BoundingRect(xNbr.doubleValue(), yNbr.doubleValue(),
                    wNbr.doubleValue(), hNbr.doubleValue());

            final double strokeW = this.strokeWidth == null ? 0.0 : this.strokeWidth.doubleValue();
            final double alphaValue = this.strokeAlpha == null ? 1.0 : this.strokeAlpha.doubleValue();

            final StrokeStyle stroke = strokeW <= 0.0 ? null : new StrokeStyle(strokeW, this.strokeColorName,
                    this.strokeDash, alphaValue, EStrokeCap.BUTT, EStrokeJoin.MITER, 10.0f);

            final double fillAlphaValue = this.fillAlpha == null ? 1.0 : this.fillAlpha.doubleValue();
            final FillStyle fill = new FillStyle(this.fillColorName, fillAlphaValue);

            result = new DocPrimitiveArcInst(rect, startNbr.doubleValue(), arcNbr.doubleValue(), stroke,
                    this.fillStyle, fill);
        } else {
            result = null;
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

        if (this.xCoord != null && this.xCoord.getNumber() != null) {
            xml.add(" x=\"", this.xCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.yCoord != null && this.yCoord.getNumber() != null) {
            xml.add(" y=\"", this.yCoord.getNumber(), CoreConstants.QUOTE);
        }

        if (this.width != null && this.width.getNumber() != null) {
            xml.add(" width=\"", this.width.getNumber(), CoreConstants.QUOTE);
        }

        if (this.height != null && this.height.getNumber() != null) {
            xml.add(" height=\"", this.height.getNumber(), CoreConstants.QUOTE);
        }

        if (this.centerX != null && this.centerX.getNumber() != null) {
            xml.add(" cx=\"", this.centerX.getNumber(), CoreConstants.QUOTE);
        }

        if (this.centerY != null && this.centerY.getNumber() != null) {
            xml.add(" cy=\"", this.centerY.getNumber(), CoreConstants.QUOTE);
        }

        if (this.radius != null && this.radius.getNumber() != null) {
            xml.add(" r=\"", this.radius.getNumber(), CoreConstants.QUOTE);
        }

        if (this.xRadius != null && this.xRadius.getNumber() != null) {
            xml.add(" rx=\"", this.xRadius.getNumber(), CoreConstants.QUOTE);
        }

        if (this.yRadius != null && this.yRadius.getNumber() != null) {
            xml.add(" ry=\"", this.yRadius.getNumber(), CoreConstants.QUOTE);
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

        if ((this.xCoord == null || this.xCoord.getFormula() == null)
                && (this.yCoord == null || this.yCoord.getFormula() == null)
                && (this.width == null || this.width.getFormula() == null)
                && (this.height == null || this.height.getFormula() == null)
                && (this.centerX == null || this.centerX.getFormula() == null)
                && (this.centerY == null || this.centerY.getFormula() == null)
                && (this.radius == null || this.radius.getFormula() == null)
                && (this.xRadius == null || this.xRadius.getFormula() == null)
                && (this.yRadius == null || this.yRadius.getFormula() == null)
                && (this.startAngle == null || this.startAngle.getFormula() == null)
                && (this.arcAngle == null || this.arcAngle.getFormula() == null)
                && this.labelSpan == null) {
            xml.addln("/>");
        } else {
            xml.addln(">");

            if (this.xCoord != null && this.xCoord.getFormula() != null) {
                xml.add(ind2, "<x>");
                this.xCoord.getFormula().appendChildrenXml(xml);
                xml.addln("</x>");
            }

            if (this.yCoord != null && this.yCoord.getFormula() != null) {
                xml.add(ind2, "<y>");
                this.yCoord.getFormula().appendChildrenXml(xml);
                xml.addln("</y>");
            }

            if (this.width != null && this.width.getFormula() != null) {
                xml.add(ind2, "<width>");
                this.width.getFormula().appendChildrenXml(xml);
                xml.addln("</width>");
            }

            if (this.height != null && this.height.getFormula() != null) {
                xml.add(ind2, "<height>");
                this.height.getFormula().appendChildrenXml(xml);
                xml.addln("</height>");
            }

            if (this.centerX != null && this.centerX.getFormula() != null) {
                xml.add(ind2, "<cx>");
                this.centerX.getFormula().appendChildrenXml(xml);
                xml.addln("</cx>");
            }

            if (this.centerY != null && this.centerY.getFormula() != null) {
                xml.add(ind2, "<cy>");
                this.centerY.getFormula().appendChildrenXml(xml);
                xml.addln("</cy>");
            }

            if (this.radius != null && this.radius.getFormula() != null) {
                xml.add(ind2, "<r>");
                this.radius.getFormula().appendChildrenXml(xml);
                xml.addln("</r>");
            }

            if (this.xRadius != null && this.xRadius.getFormula() != null) {
                xml.add(ind2, "<rx>");
                this.xRadius.getFormula().appendChildrenXml(xml);
                xml.addln("</rx>");
            }

            if (this.yRadius != null && this.yRadius.getFormula() != null) {
                xml.add(ind2, "<ry>");
                this.yRadius.getFormula().appendChildrenXml(xml);
                xml.addln("</ry>");
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
                xml.addln(ind2, "<label>", spanXml, "</label>");
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
    public void accumulateParameterNames(@SuppressWarnings("BoundedWildcard") final Set<String> set) {

        if (this.xCoord != null && this.xCoord.getFormula() != null) {
            set.addAll(this.xCoord.getFormula().params.keySet());
        }

        if (this.yCoord != null && this.yCoord.getFormula() != null) {
            set.addAll(this.yCoord.getFormula().params.keySet());
        }

        if (this.width != null && this.width.getFormula() != null) {
            set.addAll(this.width.getFormula().params.keySet());
        }

        if (this.height != null && this.height.getFormula() != null) {
            set.addAll(this.height.getFormula().params.keySet());
        }

        if (this.centerX != null && this.centerX.getFormula() != null) {
            set.addAll(this.centerX.getFormula().params.keySet());
        }

        if (this.centerY != null && this.centerY.getFormula() != null) {
            set.addAll(this.centerY.getFormula().params.keySet());
        }

        if (this.radius != null && this.radius.getFormula() != null) {
            set.addAll(this.radius.getFormula().params.keySet());
        }

        if (this.xRadius != null && this.xRadius.getFormula() != null) {
            set.addAll(this.xRadius.getFormula().params.keySet());
        }

        if (this.yRadius != null && this.yRadius.getFormula() != null) {
            set.addAll(this.yRadius.getFormula().params.keySet());
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

        return Objects.hashCode(this.xCoord)
                + Objects.hashCode(this.yCoord)
                + Objects.hashCode(this.width)
                + Objects.hashCode(this.height)
                + Objects.hashCode(this.centerX)
                + Objects.hashCode(this.centerY)
                + Objects.hashCode(this.radius)
                + Objects.hashCode(this.xRadius)
                + Objects.hashCode(this.yRadius)
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
            equal = Objects.equals(this.xCoord, arc.xCoord)
                    && Objects.equals(this.yCoord, arc.yCoord)
                    && Objects.equals(this.width, arc.width)
                    && Objects.equals(this.height, arc.height)
                    && Objects.equals(this.centerX, arc.centerX)
                    && Objects.equals(this.centerY, arc.centerY)
                    && Objects.equals(this.radius, arc.radius)
                    && Objects.equals(this.xRadius, arc.xRadius)
                    && Objects.equals(this.yRadius, arc.yRadius)
                    && Objects.equals(this.startAngle, arc.startAngle)
                    && Objects.equals(this.arcAngle, arc.arcAngle)
                    && Objects.equals(this.strokeWidth, arc.strokeWidth)
                    && Objects.equals(this.strokeColorName, arc.strokeColorName)
                    && Objects.equals(this.strokeColor, arc.strokeColor)
                    && Objects.equals(this.strokeAlpha, arc.strokeAlpha)
                    && Objects.equals(this.strokeDash, arc.strokeDash)
                    && Objects.equals(this.fillStyle, arc.fillStyle)
                    && Objects.equals(this.fillColorName, arc.fillColorName)
                    && Objects.equals(this.fillColor, arc.fillColor)
                    && Objects.equals(this.fillAlpha, arc.fillAlpha)
                    && Objects.equals(this.raysShown, arc.raysShown)
                    && Objects.equals(this.rayWidth, arc.rayWidth)
                    && Objects.equals(this.rayLength, arc.rayLength)
                    && Objects.equals(this.rayColorName, arc.rayColorName)
                    && Objects.equals(this.rayColor, arc.rayColor)
                    && Objects.equals(this.rayDash, arc.rayDash)
                    && Objects.equals(this.rayAlpha, arc.rayAlpha)
            ;
        } else {
            equal = false;
        }

        return equal;
    }
}
