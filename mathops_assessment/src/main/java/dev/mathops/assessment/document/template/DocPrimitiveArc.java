package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EArcFillStyle;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.DocPrimitiveArcInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableFactory;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.ui.ColorNames;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * An arc primitive.
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

    /** The start angle. */
    private NumberOrFormula startAngle;

    /** The arc angle. */
    private NumberOrFormula arcAngle;

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

//    /**
//     * Gets the x coordinate.
//     *
//     * @return the x coordinate
//     */
//    public NumberOrFormula getXCoordConstant() {
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
     * Sets the width.
     *
     * @param theStartAngle the width
     */
    void setStartAngle(final NumberOrFormula theStartAngle) {

        this.startAngle = theStartAngle;
    }

//    /**
//     * Gets the width.
//     *
//     * @return the width
//     */
//    public NumberOrFormula getStartAngle() {
//
//        return this.startAngle;
//    }

    /**
     * Sets the arc angle.
     *
     * @param theArcAngle the arc angle
     */
    void setArcAngle(final NumberOrFormula theArcAngle) {

        this.arcAngle = theArcAngle;
    }

//    /**
//     * Gets the arc angle.
//     *
//     * @return the arc angle
//     */
//    public NumberOrFormula getArcAngle() {
//
//        return this.arcAngle;
//    }

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

        if (this.startAngle != null) {
            copy.startAngle = this.startAngle.deepCopy();
        }

        if (this.arcAngle != null) {
            copy.arcAngle = this.arcAngle.deepCopy();
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
            if ("x".equals(name)) {
                try {
                    final Number num = NumberParser.parse(theValue);
                    this.xCoord = new NumberOrFormula(num);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    if (mode.reportDeprecated) {
                        elem.logError("Deprecated use of formula in 'x' attribute on arc primitive");
                    }
                    try {
                        final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                        this.xCoord = new NumberOrFormula(form);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'x' value (" + theValue + ") on arc primitive");
                    }
                }
            } else if ("y".equals(name)) {
                try {
                    final Number num = NumberParser.parse(theValue);
                    this.yCoord = new NumberOrFormula(num);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    if (mode.reportDeprecated) {
                        elem.logError("Deprecated use of formula in 'y' attribute on arc primitive");
                    }
                    try {
                        final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                        this.yCoord = new NumberOrFormula(form);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'y' value (" + theValue + ") on arc primitive");
                    }
                }
            } else if ("width".equals(name)) {
                try {
                    final Number num = NumberParser.parse(theValue);
                    this.width = new NumberOrFormula(num);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    if (mode.reportDeprecated) {
                        elem.logError("Deprecated use of formula in 'width' attribute on arc primitive");
                    }
                    try {
                        final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                        this.width = new NumberOrFormula(form);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'width' value (" + theValue + ") on arc primitive");
                    }
                }
            } else if ("height".equals(name)) {
                try {
                    final Number num = NumberParser.parse(theValue);
                    this.height = new NumberOrFormula(num);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    if (mode.reportDeprecated) {
                        elem.logError("Deprecated use of formula in 'height' attribute on arc primitive");
                    }
                    try {
                        final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                        this.height = new NumberOrFormula(form);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'height' value (" + theValue + ") on arc primitive");
                    }
                }
            } else if ("start-angle".equals(name)) {
                try {
                    final Number num = NumberParser.parse(theValue);
                    this.startAngle = new NumberOrFormula(num);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    if (mode.reportDeprecated) {
                        elem.logError("Deprecated use of formula in 'start-angle' attribute on arc primitive");
                    }
                    try {
                        final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                        this.startAngle = new NumberOrFormula(form);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'start-angle' value (" + theValue + ") on arc primitive");
                    }
                }
            } else if ("arc-angle".equals(name)) {
                try {
                    final Number num = NumberParser.parse(theValue);
                    this.arcAngle = new NumberOrFormula(num);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    if (mode.reportDeprecated) {
                        elem.logError("Deprecated use of formula in 'arc-angle' attribute on arc primitive");
                    }
                    try {
                        final Formula form = FormulaFactory.parseFormulaString(new EvalContext(), theValue, mode);
                        this.arcAngle = new NumberOrFormula(form);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'arc-angle' value (" + theValue + ") on arc primitive");
                    }
                }
            } else if ("filled".equals(name)) {

                try {
                    this.filled = VariableFactory.parseBooleanValue(theValue);
                    ok = true;
                } catch (final IllegalArgumentException ex) {
                    elem.logError("Invalid 'filled' value (" + theValue + ") on arc primitive");
                }
            } else if ("color".equals(name)) {

                if (ColorNames.isColorNameValid(theValue)) {
                    this.color = ColorNames.getColor(theValue);
                    this.colorName = theValue;
                    ok = true;
                } else {
                    elem.logError("Invalid 'color' value (" + theValue + ") on arc primitive");
                }
            } else if ("stroke-width".equals(name)) {
                try {
                    this.strokeWidth = Double.valueOf(theValue);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    elem.logError("Invalid 'stroke-width' value (" + theValue + ") on arc primitive");
                }
            } else if ("dash".equals(name)) {
                final String[] split = theValue.split(CoreConstants.COMMA);
                final int splitLen = split.length;

                this.dash = new float[splitLen];
                for (int i = 0; i < splitLen; ++i) {
                    try {
                        this.dash[i] = (float) Double.parseDouble(split[i]);
                        ok = true;
                    } catch (final NumberFormatException ex) {
                        // No action
                    }
                }
                if (!ok) {
                    elem.logError("Invalid 'alpha' value (" + theValue + ") on arc primitive");
                }
            } else if ("alpha".equals(name)) {
                try {
                    this.alpha = Double.valueOf(theValue);
                    ok = true;
                } catch (final NumberFormatException ex) {
                    elem.logError("Invalid 'alpha' value (" + theValue + ") on arc primitive");
                }
            } else {
                elem.logError("Unsupported attribute '" + name + "' on arc primitive");
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

        Long start = null;
        if (this.startAngle != null) {
            result = this.startAngle.evaluate(context);

            if (result instanceof final Long longResult) {
                start = longResult;
            } else if (result instanceof final Number numResult) {
                start = Long.valueOf(Math.round(numResult.doubleValue()));
            }
        }

        Long arc = null;
        if (this.arcAngle != null) {
            result = this.arcAngle.evaluate(context);

            if (result instanceof final Long longResult) {
                arc = longResult;
            } else if (result instanceof final Number numResult) {
                arc = Long.valueOf(Math.round(numResult.doubleValue()));
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

        if (x != null && y != null && w != null && h != null && start != null && arc != null) {
            if (isFilled()) {
                grx.fillArc((int) (x.floatValue() * this.scale), (int) (y.floatValue() * this.scale),
                        (int) (w.floatValue() * this.scale), (int) (h.floatValue() * this.scale), start.intValue(),
                        arc.intValue());
            } else {
                grx.drawArc((int) (x.floatValue() * this.scale), (int) (y.floatValue() * this.scale),
                        (int) (w.floatValue() * this.scale), (int) (h.floatValue() * this.scale), start.intValue(),
                        arc.intValue());
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
            final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

            final StrokeStyle stroke = strokeW <= 0.0 ? null : new StrokeStyle(strokeW, this.colorName, this.dash,
                    alphaValue, EStrokeCap.BUTT, EStrokeJoin.MITER, 10.0f);

            final FillStyle fill = Boolean.TRUE.equals(this.filled) ? new FillStyle(this.colorName, alphaValue) : null;

            result = new DocPrimitiveArcInst(rect, startNbr.doubleValue(), arcNbr.doubleValue(), stroke,
                    EArcFillStyle.CHORD, fill);
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

        if (this.startAngle != null && this.startAngle.getNumber() != null) {
            xml.add(" start-angle=\"", this.startAngle.getNumber(), CoreConstants.QUOTE);
        }

        if (this.arcAngle != null && this.arcAngle.getNumber() != null) {
            xml.add(" arc-angle=\"", this.arcAngle.getNumber(), CoreConstants.QUOTE);
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
            final int dashlen = this.dash.length;
            if (dashlen > 0) {
                xml.add(" dash=\"", Float.toString(this.dash[0]));

                for (int i = 1; i < dashlen; ++i) {
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
                && (this.height == null || this.height.getFormula() == null)
                && (this.startAngle == null || this.startAngle.getFormula() == null)
                && (this.arcAngle == null || this.arcAngle.getFormula() == null)) {
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

        if (this.startAngle != null && this.startAngle.getFormula() != null) {
            set.addAll(this.startAngle.getFormula().params.keySet());
        }

        if (this.arcAngle != null && this.arcAngle.getFormula() != null) {
            set.addAll(this.arcAngle.getFormula().params.keySet());
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return EqualityTests.objectHashCode(this.xCoord)
                + EqualityTests.objectHashCode(this.yCoord)
                + EqualityTests.objectHashCode(this.width)
                + EqualityTests.objectHashCode(this.height)
                + EqualityTests.objectHashCode(this.startAngle)
                + EqualityTests.objectHashCode(this.arcAngle)
                + EqualityTests.objectHashCode(this.filled)
                + EqualityTests.objectHashCode(this.colorName)
                + EqualityTests.objectHashCode(this.color)
                + EqualityTests.objectHashCode(this.alpha)
                + EqualityTests.objectHashCode(this.strokeWidth)
                + EqualityTests.objectHashCode(this.dash);
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
                    && Objects.equals(this.startAngle, arc.startAngle)
                    && Objects.equals(this.arcAngle, arc.arcAngle)
                    && Objects.equals(this.filled, arc.filled)
                    && Objects.equals(this.colorName, arc.colorName)
                    && Objects.equals(this.color, arc.color)
                    && Objects.equals(this.alpha, arc.alpha)
                    && Objects.equals(this.strokeWidth, arc.strokeWidth)
                    && Objects.equals(this.dash, arc.dash);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param other  the other object
     * @param indent the indent level
     */
    @Override
    public void whyNotEqual(final Object other, final int indent) {

        if (other instanceof final DocPrimitiveArc obj) {

            if (!Objects.equals(this.xCoord, obj.xCoord)) {
                if (this.xCoord == null || obj.xCoord == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveLine (xCoord: ", this.xCoord,
                            CoreConstants.SLASH, obj.xCoord, ")");
                }
            }

            if (!Objects.equals(this.yCoord, obj.yCoord)) {
                if (this.yCoord == null || obj.yCoord == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveLine (yCoord: ", this.yCoord,
                            CoreConstants.SLASH, obj.yCoord, ")");
                }
            }

            if (!Objects.equals(this.width, obj.width)) {
                if (this.width == null || obj.width == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveLine (width: ", this.width, CoreConstants.SLASH
                            , obj.width, ")");
                }
            }

            if (!Objects.equals(this.height, obj.height)) {
                if (this.height == null || obj.height == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveLine (height: ", this.height,
                            CoreConstants.SLASH, obj.height, ")");
                }
            }

            if (!Objects.equals(this.startAngle, obj.startAngle)) {
                if (this.startAngle == null || obj.startAngle == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveLine (startAngle: ", this.startAngle,
                            CoreConstants.SLASH, obj.startAngle, ")");
                }
            }

            if (!Objects.equals(this.arcAngle, obj.arcAngle)) {
                if (this.arcAngle == null || obj.arcAngle == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveLine (arcAngle: ", this.arcAngle,
                            CoreConstants.SLASH, obj.arcAngle, ")");
                }
            }

            if (!Objects.equals(this.filled, obj.filled)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (filled: ", this.filled, "!=", obj.filled, ")");
            }

            if (!Objects.equals(this.colorName, obj.colorName)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (colorName: ", this.colorName, "!=",
                        obj.colorName, ")");
            }

            if (!Objects.equals(this.color, obj.color)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (color: ", this.color, "!=", obj.color, ")");
            }

            if (!Objects.equals(this.alpha, obj.alpha)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (alpha: ", this.alpha, "!=", obj.alpha, ")");
            }

            if (!Objects.equals(this.strokeWidth, obj.strokeWidth)) {
                Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (strokeWidth: ", this.strokeWidth, "!=",
                        obj.strokeWidth, ")");
            }

            if (!Objects.equals(this.dash, obj.dash)) {
                if (this.dash == null || obj.dash == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (dash: ", this.dash, CoreConstants.SLASH,
                            obj.dash, ")");
                } else {
                    final int dashLen = this.dash.length;
                    if (dashLen == obj.dash.length) {
                        Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (dash...)");
                        for (int i = 0; i < dashLen; ++i) {
                            final float o1 = this.dash[i];
                            final float o2 = obj.dash[i];

                            if (o1 != o2) {
                                Log.info(makeIndent(indent),
                                        "UNEQUAL DocPrimitiveArc (dash " + i + ": " + o1 + CoreConstants.SLASH + o2 + ")");
                            }
                        }
                    } else {
                        Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc (dash size: " + this.dash.length
                                + CoreConstants.SLASH + obj.dash.length + ")");
                    }
                }
            }
        } else {
            Log.info(makeIndent(indent), "UNEQUAL DocPrimitiveArc because other is ", other.getClass().getName());
        }
    }
}
