package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.DocPrimitiveOvalInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableFactory;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.ui.ColorNames;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * An oval primitive.
 */
final class DocPrimitiveOval extends AbstractDocPrimitive {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3190659746792873317L;

    /** The x coordinate formula. */
    private NumberOrFormula xCoord;

    /** The y coordinate formula. */
    private NumberOrFormula yCoord;

    /** The width formula. */
    private NumberOrFormula width;

    /** The height formula. */
    private NumberOrFormula height;

    /** The filled flag. */
    private Boolean filled;

    /** The color name. */
    private String colorName;

    /** The color. */
    private Color color;

    /** The alpha. */
    private Double alpha;

    /** The stroke width. */
    private Double strokeWidth;

    /** The dash lengths (must be floats for BasicStroke class). */
    private float[] dash;

    /**
     * Construct a new {@code DocPrimitiveOval}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveOval(final AbstractDocPrimitiveContainer theOwner) {

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

//    /**
//     * Gets the x coordinate.
//     *
//     * @return the x coordinate
//     */
//    public NumberOrFormula getXCoord() {
//
//        return this.xCoord;
//    }

    /**
     * Sets the y coordinate.
     *
     * @param theYCoord the y coordinate
     */
    void setYCoord(final NumberOrFormula theYCoord) {

        this.yCoord = theYCoord;
    }

//    /**
//     * Gets the y coordinate.
//     *
//     * @return the y coordinate
//     */
//    public NumberOrFormula getYCoord() {
//
//        return this.yCoord;
//    }

    /**
     * Sets the width.
     *
     * @param theWidth the width
     */
    public void setWidth(final NumberOrFormula theWidth) {

        this.width = theWidth;
    }

    /**
     * Gets the width.
     *
     * @return the width
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
     * Gets the filled flag.
     *
     * @return the flag
     */
    private boolean isFilled() {

        return Boolean.TRUE.equals(this.filled);
    }

    /**
     * Gets the color name.
     *
     * @return the color name
     */
    public String getColorName() {

        return this.colorName;
    }

    /**
     * Gets the alpha value.
     *
     * @return the alpha value
     */
    public Double getAlpha() {

        return this.alpha;
    }

//    /**
//     * Gets the stroke width.
//     *
//     * @return the stroke width
//     */
//    public Double getStrokeWidth() {
//
//        return this.strokeWidth;
//    }

//    /**
//     * Gets the dash array.
//     *
//     * @return the dash array
//     */
//    public float[] getDashArray() {
//
//        return this.dash == null ? null : this.dash.clone();
//    }

//    /**
//     * Sets the fill color.
//     *
//     * @param theColorName the name of the fill color
//     */
//    public void setFillColor(final String theColorName) {
//
//        this.colorName = theColorName;
//        this.color = ColorNames.getColor(theColorName);
//    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @param theOwner the new owner
     * @return the copy
     */
    @Override
    public DocPrimitiveOval deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitiveOval copy = new DocPrimitiveOval(theOwner);

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

        copy.filled = this.filled;
        copy.colorName = this.colorName;
        copy.color = this.color;

        copy.alpha = this.alpha;
        copy.strokeWidth = this.strokeWidth;

        if (this.dash != null) {
            copy.dash = this.dash.clone();
        }

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
                case "x" -> {
                    this.xCoord = parseNumberOrFormula(theValue, elem, mode, "x", "oval primitive");
                    ok = this.xCoord != null;
                }
                case "y" -> {
                    this.yCoord = parseNumberOrFormula(theValue, elem, mode, "y", "oval primitive");
                    ok = this.yCoord != null;
                }
                case "width" -> {
                    this.width = parseNumberOrFormula(theValue, elem, mode, "width", "oval primitive");
                    ok = this.width != null;
                }
                case "height" -> {
                    this.height = parseNumberOrFormula(theValue, elem, mode, "height", "oval primitive");
                    ok = this.height != null;
                }
                case "filled" -> {
                    try {
                        this.filled = VariableFactory.parseBooleanValue(theValue);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'filled' value (" + theValue + ") on oval primitive");
                    }
                }
                case "color" -> {
                    if (ColorNames.isColorNameValid(theValue)) {
                        this.color = ColorNames.getColor(theValue);
                        this.colorName = theValue;
                        ok = true;
                    } else {
                        elem.logError("Invalid 'color' value (" + theValue + ") on oval primitive");
                    }
                }
                case "stroke-width" -> {
                    this.strokeWidth = parseDouble(theValue, elem, name, "oval primitive");
                    ok = this.strokeWidth != null;
                }
                case "dash" -> {
                    final String[] split = theValue.split(CoreConstants.COMMA);
                    final int len = split.length;
                    this.dash = new float[len];

                    for (int i = 0; i < len; ++i) {
                        try {
                            this.dash[i] = (float) Double.parseDouble(split[i]);
                            ok = true;
                        } catch (final NumberFormatException e) {
                            // No action
                        }
                    }
                    if (!ok) {
                        elem.logError("Invalid 'dash' value (" + theValue + ") on oval primitive");
                    }
                }
                case "alpha" -> {
                    this.alpha = parseDouble(theValue, elem, name, "oval primitive");
                    ok = this.alpha != null;
                }
                case null, default -> elem.logError("Unsupported attribute '" + name + "' on oval primitive");
            }
        }

        return ok;
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

        // Evaluate formulae
        Long x = null;
        if (this.xCoord != null) {
            result = this.xCoord.evaluate(context);

            if (result instanceof final Long longResult) {
                x = longResult;
            } else if (result instanceof final Number numResult) {
                x = Long.valueOf(Math.round(numResult.doubleValue()));
            }
        }

        Long y = null;
        if (this.yCoord != null) {
            result = this.yCoord.evaluate(context);

            if (result instanceof final Long longResult) {
                y = longResult;
            } else if (result instanceof final Number numResult) {
                y = Long.valueOf(Math.round(numResult.doubleValue()));
            }
        }

        Long w = null;
        if (this.width != null) {
            result = this.width.evaluate(context);

            if (result instanceof final Long longResult) {
                w = longResult;
            } else if (result instanceof final Number numResult) {
                w = Long.valueOf(Math.round(numResult.doubleValue()));
            }
        }

        Long h = null;
        if (this.height != null) {
            result = this.height.evaluate(context);

            if (result instanceof final Long longResult) {
                h = longResult;
            } else if (result instanceof final Number numResult) {
                h = Long.valueOf(Math.round(numResult.doubleValue()));
            }
        }

        if (this.color != null) {
            grx.setColor(this.color);
        } else {
            grx.setColor(Color.BLACK);
        }

        Composite origComp = null;
        final Stroke origStroke;

        if (this.alpha != null) {
            origComp = grx.getComposite();
            grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) this.alpha.doubleValue()));
        }

        origStroke = grx.getStroke();

        if (this.strokeWidth != null) {

            if (this.dash != null) {
                grx.setStroke(new BasicStroke((float) this.strokeWidth.doubleValue(),
                        BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, this.dash, 0.0f));
            } else {
                grx.setStroke(new BasicStroke((float) this.strokeWidth.doubleValue()));
            }
        } else if (this.dash != null) {
            grx.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                    10.0f, this.dash, 0.0f));
        }

        if (x != null && y != null && w != null && h != null) {

            if (isFilled()) {
                grx.fillOval((int) (x.floatValue() * this.scale),
                        (int) (y.floatValue() * this.scale), (int) (w.floatValue() * this.scale),
                        (int) (h.floatValue() * this.scale));
            } else {
                grx.drawOval((int) (x.floatValue() * this.scale),
                        (int) (y.floatValue() * this.scale), (int) (w.floatValue() * this.scale),
                        (int) (h.floatValue() * this.scale));
            }
        }

        if (origComp != null) {
            grx.setComposite(origComp);
        }

        grx.setStroke(origStroke);
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
    public DocPrimitiveOvalInst createInstance(final EvalContext evalContext) {

        final Object xVal = this.xCoord == null ? null : this.xCoord.evaluate(evalContext);
        final Object yVal = this.yCoord == null ? null : this.yCoord.evaluate(evalContext);
        final Object wVal = this.width == null ? null : this.width.evaluate(evalContext);
        final Object hVal = this.height == null ? null : this.height.evaluate(evalContext);

        final DocPrimitiveOvalInst result;

        if (xVal instanceof final Number xNbr && yVal instanceof final Number yNbr
                && wVal instanceof final Number wNbr && hVal instanceof final Number hNbr) {

            final BoundingRect rect = new BoundingRect(xNbr.doubleValue(), yNbr.doubleValue(),
                    wNbr.doubleValue(), hNbr.doubleValue());

            final double strokeW = this.strokeWidth == null ? 0.0 : this.strokeWidth.doubleValue();
            final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

            final StrokeStyle stroke = strokeW <= 0.0 ? null : new StrokeStyle(strokeW,
                    this.colorName, this.dash, alphaValue, EStrokeCap.BUTT, EStrokeJoin.MITER,
                    10.0f);

            final FillStyle fill = Boolean.TRUE.equals(this.filled) ? new FillStyle(this.colorName, alphaValue) : null;

            result = new DocPrimitiveOvalInst(rect, stroke, fill);
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

        xml.add(ind, "<oval");

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

        if (this.filled != null) {
            xml.add(" filled=\"", this.filled, CoreConstants.QUOTE);
        }

        if (this.colorName != null) {
            xml.add(" color=\"", this.colorName, CoreConstants.QUOTE);
        }

        if (this.alpha != null) {
            xml.add(" alpha=\"", this.alpha.toString(), CoreConstants.QUOTE);
        }

        if (this.dash != null) {
            final int len = this.dash.length;
            if (len > 0) {
                xml.add(" dash=\"", Float.toString(this.dash[0]));

                for (int i = 1; i < len; ++i) {
                    xml.add(CoreConstants.COMMA, Float.toString(this.dash[i]));
                }

                xml.add('"');
            }
        }

        if (this.strokeWidth != null) {
            xml.add(" stroke-width=\"", this.strokeWidth, CoreConstants.QUOTE);
        }

        if ((this.xCoord == null || this.xCoord.getFormula() == null)
                && (this.yCoord == null || this.yCoord.getFormula() == null)
                && (this.width == null || this.width.getFormula() == null)
                && (this.height == null || this.height.getFormula() == null)) {
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

            xml.addln(ind, "</oval>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Oval";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) { // Do NOT change to "? super String"

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
                + Objects.hashCode(this.filled)
                + Objects.hashCode(this.colorName)
                + Objects.hashCode(this.color)
                + Objects.hashCode(this.alpha)
                + Objects.hashCode(this.strokeWidth)
                + Objects.hashCode(this.dash);
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
        } else if (obj instanceof final DocPrimitiveOval oval) {
            equal = Objects.equals(this.xCoord, oval.xCoord)
                    && Objects.equals(this.yCoord, oval.yCoord)
                    && Objects.equals(this.width, oval.width)
                    && Objects.equals(this.height, oval.height)
                    && Objects.equals(this.filled, oval.filled)
                    && Objects.equals(this.colorName, oval.colorName)
                    && Objects.equals(this.color, oval.color)
                    && Objects.equals(this.alpha, oval.alpha)
                    && Objects.equals(this.strokeWidth, oval.strokeWidth)
                    && Arrays.equals(this.dash, oval.dash);
        } else {
            equal = false;
        }

        return equal;
    }
}
