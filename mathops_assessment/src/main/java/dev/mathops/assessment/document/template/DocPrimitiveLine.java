package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.inst.StrokeStyleInst;
import dev.mathops.assessment.document.inst.DocPrimitiveLineInst;
import dev.mathops.assessment.document.inst.RectangleShapeInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.xml.INode;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * A line primitive.
 */
final class DocPrimitiveLine extends AbstractDocRectangleShape {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5269770064156882137L;

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
     * Construct a new {@code DocPrimitiveLine}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveLine(final AbstractDocPrimitiveContainer theOwner) {

        super(theOwner);
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
    public DocPrimitiveLine deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitiveLine copy = new DocPrimitiveLine(theOwner);

        final RectangleShapeTemplate myShape = getShape();
        if (myShape != null) {
            final RectangleShapeTemplate myShapeCopy = myShape.deepCopy();
            copy.setShape(myShapeCopy);
        }

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
                case "color" -> {
                    if (ColorNames.isColorNameValid(theValue)) {
                        this.color = ColorNames.getColor(theValue);
                        this.colorName = theValue;
                        ok = true;
                    } else {
                        elem.logError("Invalid 'color' value (" + theValue + ") on line primitive");
                    }
                }
                case "stroke-width" -> {
                    this.strokeWidth = parseDouble(theValue, elem, name, "line primitive");
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
                        elem.logError("Invalid 'dash' value (" + theValue + ") on line primitive");
                    }
                }
                case "alpha" -> {
                    this.alpha = parseDouble(theValue, elem, name, "line primitive");
                    ok = this.alpha != null;
                }
                case null, default -> elem.logError("Unsupported attribute '" + name + "' on line primitive");
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

        final Rectangle2D bounds = getBoundsRect(context);

        if (bounds != null) {
            if (this.color != null) {
                grx.setColor(this.color);
            } else {
                grx.setColor(Color.BLACK);
            }

            Composite origComp = null;
            final Stroke origStroke;

            if (this.alpha != null) {
                origComp = grx.getComposite();
                grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha.floatValue()));
            }

            origStroke = grx.getStroke();

            if (this.strokeWidth != null) {

                if (this.dash != null) {
                    grx.setStroke(new BasicStroke(this.strokeWidth.floatValue(), BasicStroke.CAP_SQUARE,
                            BasicStroke.JOIN_MITER, 10.0f, this.dash, 0.0f));
                } else {
                    grx.setStroke(new BasicStroke(this.strokeWidth.floatValue()));
                }
            } else if (this.dash != null) {
                grx.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, this.dash,
                        0.0f));
            }

            final double x = bounds.getX();
            final double y = bounds.getY();
            final double width = bounds.getWidth();
            final double height = bounds.getHeight();

            final Shape scaled = new Line2D.Double(x * (double) this.scale, y * (double) this.scale,
                    (x + width) * (double) this.scale, (y + height) * (double) this.scale);

            grx.draw(scaled);

            if (origComp != null) {
                grx.setComposite(origComp);
            }

            grx.setStroke(origStroke);
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
    public DocPrimitiveLineInst createInstance(final EvalContext evalContext) {

        final RectangleShapeTemplate shape = getShape();

        DocPrimitiveLineInst result = null;

        if (Objects.nonNull(shape)) {
            final double strokeW = this.strokeWidth == null ? 0.0 : this.strokeWidth.doubleValue();
            final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

            final StrokeStyleInst stroke = strokeW <= 0.0 ? null : new StrokeStyleInst(strokeW, this.colorName,
                    this.dash, alphaValue, EStrokeCap.BUTT, EStrokeJoin.MITER, 10.0f);

            final RectangleShapeInst shapeInst = getShape().createInstance(evalContext);
            result = new DocPrimitiveLineInst(shapeInst, stroke);
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

        xml.add(ind, "<line");

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.addAttributes(xml);
        }

        if (this.colorName != null) {
            xml.add(" color=\"", this.colorName, CoreConstants.QUOTE);
        }

        if (this.alpha != null) {
            xml.add(" alpha=\"", this.alpha.toString(), CoreConstants.QUOTE);
        }

        if (this.dash != null) {
            final int dashLen = this.dash.length;
            if (dashLen > 0) {
                xml.add(" dash=\"", Float.toString(this.dash[0]));

                for (int i = 1; i < dashLen; ++i) {
                    xml.add(CoreConstants.COMMA, Float.toString(this.dash[i]));
                }

                xml.add('"');
            }
        }

        if (this.strokeWidth != null) {
            xml.add(" stroke-width=\"", this.strokeWidth, CoreConstants.QUOTE);
        }

        if (shape == null || shape.isConstant()) {
            xml.addln("/>");
        } else {
            xml.addln(">");
            shape.addChildElements(xml, indent + 1);
            xml.addln(ind, "</line>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Line";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) { // Do NOT change to "? super String"

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.accumulateParameterNames(set);
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
        } else if (obj instanceof final DocPrimitiveLine line) {
            equal = Objects.equals(getShape(), line.getShape())
                    && Objects.equals(this.colorName, line.colorName)
                    && Objects.equals(this.color, line.color)
                    && Objects.equals(this.alpha, line.alpha)
                    && Objects.equals(this.strokeWidth, line.strokeWidth)
                    && Arrays.equals(this.dash, line.dash);
        } else {
            equal = false;
        }

        return equal;
    }
}
