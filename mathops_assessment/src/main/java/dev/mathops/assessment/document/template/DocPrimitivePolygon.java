package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.DocPrimitivePolygonInst;
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
import java.awt.Polygon;
import java.awt.Stroke;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A polygon primitive.
 */
final class DocPrimitivePolygon extends AbstractDocPrimitive {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5163840099621867923L;

    /** The list of x coordinate constants. */
    private List<Double> xCoordConstants;

    /** The list of x coordinate formulas. */
    private List<Formula> xCoordFormulas;

    /** The list of y coordinate constants. */
    private List<Double> yCoordConstants;

    /** The list of y coordinate formulas. */
    private List<Formula> yCoordFormulas;

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
     * Construct a new {@code DocPrimitivePolygon}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitivePolygon(final AbstractDocPrimitiveContainer theOwner) {

        super(theOwner);
    }

//    /**
//     * Gets the array of x coordinate constants.
//     *
//     * @return the coordinate value array
//     */
//    public List<Double> getXCoordConstants() {
//
//        return this.xCoordConstants == null ? null : new ArrayList<>(this.xCoordConstants);
//    }

    /**
     * Sets the array of s coordinate formulas.
     *
     * @param theXCoordFormulas the formula array
     */
    void setXCoordFormulas(final Collection<Formula> theXCoordFormulas) {

        if (theXCoordFormulas != null) {
            this.xCoordFormulas = new ArrayList<>(theXCoordFormulas);
            this.xCoordConstants = null;
        }
    }

//    /**
//     * Gets the array of x coordinate formulas.
//     *
//     * @return the formula array
//     */
//    public List<Formula> getXCoordFormulas() {
//
//        return this.xCoordFormulas == null ? null : new ArrayList<>(this.xCoordFormulas);
//    }

//    /**
//     * Gets the array of y coordinate constants.
//     *
//     * @return the coordinate value array
//     */
//    public List<Double> getYCoordConstants() {
//
//        return this.yCoordConstants == null ? null : new ArrayList<>(this.yCoordConstants);
//    }

    /**
     * Sets the array of y coordinate formulas.
     *
     * @param theYCoordFormulas the formula array
     */
    void setYCoordFormulas(final Collection<Formula> theYCoordFormulas) {

        if (theYCoordFormulas != null) {
            this.yCoordFormulas = new ArrayList<>(theYCoordFormulas);
            this.yCoordConstants = null;
        }
    }

//    /**
//     * Gets the array of y coordinate formulas.
//     *
//     * @return the formula array
//     */
//    public List<Formula> getYCoordFormulas() {
//
//        return this.yCoordFormulas == null ? null : new ArrayList<>(this.yCoordFormulas);
//    }

    /**
     * Gets the filled flag.
     *
     * @return the flag
     */
    public boolean isFilled() {

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
    public DocPrimitivePolygon
    deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitivePolygon copy = new DocPrimitivePolygon(theOwner);

        if (this.xCoordConstants != null) {
            copy.xCoordConstants = new ArrayList<>(this.xCoordConstants);
        }

        if (this.xCoordFormulas != null) {
            final int count = this.xCoordFormulas.size();
            copy.xCoordFormulas = new ArrayList<>(count);
            for (final Formula formula : this.xCoordFormulas) {
                copy.xCoordFormulas.add(formula.deepCopy());
            }
        }

        if (this.yCoordConstants != null) {
            copy.yCoordConstants = new ArrayList<>(this.yCoordConstants);
        }

        if (this.yCoordFormulas != null) {
            final int count = this.yCoordFormulas.size();
            copy.yCoordFormulas = new ArrayList<>(count);
            for (final Formula formula : this.yCoordFormulas) {
                copy.yCoordFormulas.add(formula.deepCopy());
            }
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
                case "filled" -> {
                    try {
                        this.filled = VariableFactory.parseBooleanValue(theValue);
                        ok = true;
                    } catch (final IllegalArgumentException e) {
                        elem.logError("Invalid 'filled' value (" + theValue + ") on polygon primitive");
                    }
                }
                case "color" -> {

                    if (ColorNames.isColorNameValid(theValue)) {
                        this.color = ColorNames.getColor(theValue);
                        this.colorName = theValue;
                        ok = true;
                    } else {
                        elem.logError("Invalid 'color' value (" + theValue + ") on polygon primitive");
                    }
                }
                case "x-list" -> {
                    final String[] split = theValue.split(CoreConstants.COMMA);
                    final int splitLen = split.length;

                    boolean allConstant = true;
                    for (final String s : split) {
                        try {
                            Double.parseDouble(s);
                        } catch (final NumberFormatException ex) {
                            allConstant = false;
                            break;
                        }
                    }

                    ok = true;
                    if (allConstant) {
                        this.xCoordConstants = new ArrayList<>(splitLen);
                        for (int i = 0; ok && (i < splitLen); ++i) {
                            try {
                                this.xCoordConstants.add(Double.valueOf(split[i]));
                            } catch (final NumberFormatException ex) {
                                ok = false;
                            }
                        }
                    } else {
                        if (mode.reportDeprecated) {
                            elem.logError("Deprecated use of formula in 'x-list' on polygon primitive");
                        }
                        this.xCoordFormulas = new ArrayList<>(splitLen);
                        for (int i = 0; ok && (i < splitLen); ++i) {
                            final Formula formula = FormulaFactory.parseFormulaString(new EvalContext(), split[i], mode);
                            if (formula == null) {
                                ok = false;
                            } else {
                                this.xCoordFormulas.add(formula);
                            }
                        }
                    }

                    if (!ok) {
                        elem.logError("Invalid 'x-list' value (" + theValue + ") on polygon primitive");
                    }
                }
                case "y-list" -> {
                    final String[] split = theValue.split(CoreConstants.COMMA);
                    final int splitLen = split.length;

                    boolean allConstant = true;
                    for (final String s : split) {
                        try {
                            Double.parseDouble(s);
                        } catch (final NumberFormatException ex) {
                            allConstant = false;
                            break;
                        }
                    }

                    ok = true;
                    if (allConstant) {
                        this.yCoordConstants = new ArrayList<>(splitLen);
                        for (int i = 0; ok && (i < splitLen); ++i) {
                            try {
                                this.yCoordConstants.add(Double.valueOf(split[i]));
                            } catch (final NumberFormatException ex) {
                                ok = false;
                            }
                        }
                    } else {
                        if (mode.reportDeprecated) {
                            elem.logError("Deprecated use of formula in 'y-list' on polygon primitive");
                        }

                        this.yCoordFormulas = new ArrayList<>(splitLen);
                        for (int i = 0; ok && (i < splitLen); ++i) {
                            final Formula formula = FormulaFactory.parseFormulaString(new EvalContext(), split[i], mode);
                            if (formula == null) {
                                ok = false;
                            } else {
                                this.yCoordFormulas.add(formula);
                            }
                        }
                    }

                    if (!ok) {
                        elem.logError("Invalid 'y-list' value (" + theValue + ") on polygon primitive");
                    }
                }
                case "stroke-width" -> {
                    this.strokeWidth = parseDouble(theValue, elem, name, "polygon primitive");
                    ok = this.strokeWidth != null;
                }
                case "dash" -> {
                    final String[] split = theValue.split(CoreConstants.COMMA);
                    final int count = split.length;
                    this.dash = new float[count];

                    for (int i = 0; i < count; ++i) {
                        try {
                            this.dash[i] = (float) Double.parseDouble(split[i]);
                            ok = true;
                        } catch (final NumberFormatException e) {
                            // No action
                        }
                    }
                    if (!ok) {
                        elem.logError("Invalid 'dash' value (" + theValue + ") on polygon primitive");
                    }
                }
                case "alpha" -> {
                    this.alpha = parseDouble(theValue, elem, name, "polygon primitive");
                    ok = this.alpha != null;
                }
                case null, default -> elem.logError("Unsupported attribute '" + name + "' on polygon primitive");
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

        // Evaluate formulae
        Long[] x = null;
        if (this.xCoordConstants != null) {
            final int count = this.xCoordConstants.size();
            x = new Long[count];
            for (int i = 0; i < count; ++i) {
                x[i] = Long.valueOf(Math.round(this.xCoordConstants.get(i).doubleValue()));
            }
        } else if (this.xCoordFormulas != null) {
            final int count = this.xCoordFormulas.size();
            x = new Long[count];
            for (int i = 0; i < count; ++i) {
                final Object result = this.xCoordFormulas.get(i).evaluate(context);

                if (result instanceof Long) {
                    x[i] = (Long) result;
                } else if (result instanceof Double) {
                    x[i] = Long.valueOf(Math.round(((Double) result).doubleValue()));
                }
            }
        }

        Long[] y = null;
        if (this.yCoordConstants != null) {
            final int count = this.yCoordConstants.size();
            y = new Long[count];
            for (int i = 0; i < count; ++i) {
                y[i] = Long.valueOf(Math.round(this.yCoordConstants.get(i).doubleValue()));
            }
        } else if (this.yCoordFormulas != null) {
            final int count = this.yCoordFormulas.size();
            y = new Long[count];
            for (int i = 0; i < count; ++i) {
                final Object result = this.yCoordFormulas.get(i).evaluate(context);

                if (result instanceof Long) {
                    y[i] = (Long) result;
                } else if (result instanceof Double) {
                    y[i] = Long.valueOf(Math.round(((Double) result).doubleValue()));
                }
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
                grx.setStroke(new BasicStroke(this.strokeWidth.floatValue(),
                        BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, this.dash, 0.0f));
            } else {
                grx.setStroke(new BasicStroke(this.strokeWidth.floatValue()));
            }
        } else if (this.dash != null) {
            grx.setStroke(
                    new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, this.dash, 0.0f));
        }

        if (x != null && x[0] != null && y != null && y[0] != null) {
            final Polygon poly = new Polygon();
            final int min = Math.min(x.length, y.length);

            for (int i = 0; i < min; ++i) {
                poly.addPoint((int) (x[i].floatValue() * this.scale), (int) (y[i].floatValue() * this.scale));
            }

            if (this.filled != null && this.filled.booleanValue()) {
                grx.fillPolygon(poly);
            } else {
                grx.drawPolygon(poly);
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
    public DocPrimitivePolygonInst createInstance(final EvalContext evalContext) {

        final int numX = this.xCoordConstants == null ? this.xCoordFormulas.size() : this.xCoordConstants.size();
        final int numY = this.yCoordConstants == null ? this.yCoordFormulas.size() : this.yCoordConstants.size();

        final int numPts = Math.min(numX, numY);
        final double[] x = new double[numPts];
        final double[] y = new double[numPts];

        if (this.xCoordConstants == null) {
            for (int i = 0; i < numPts; ++i) {
                final Object obj = this.xCoordFormulas.get(i).evaluate(evalContext);
                if (obj instanceof final Number nbr) {
                    x[i] = nbr.doubleValue();
                }
            }
        } else {
            for (int i = 0; i < numPts; ++i) {
                x[i] = this.xCoordConstants.get(i).doubleValue();
            }
        }

        if (this.yCoordConstants == null) {
            for (int i = 0; i < numPts; ++i) {
                final Object obj = this.yCoordFormulas.get(i).evaluate(evalContext);
                if (obj instanceof final Number nbr) {
                    y[i] = nbr.doubleValue();
                }
            }
        } else {
            for (int i = 0; i < numPts; ++i) {
                y[i] = this.yCoordConstants.get(i).doubleValue();
            }
        }

        final double strokeW = this.strokeWidth == null ? 0.0 : this.strokeWidth.doubleValue();
        final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

        final StrokeStyle stroke = strokeW <= 0.0 || alphaValue <= 0.0 ? null
                : new StrokeStyle(strokeW, this.colorName, this.dash, alphaValue, EStrokeCap.BUTT,
                                  EStrokeJoin.MITER, 10.0f);

        final FillStyle fill = Boolean.TRUE.equals(this.filled) ? new FillStyle(this.colorName, alphaValue) : null;

        return new DocPrimitivePolygonInst(x, y, stroke, fill);
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

        xml.add(ind, "<polygon");

        if (this.xCoordConstants != null) {
            xml.add(" x-list=\"", this.xCoordConstants.getFirst());
            final int count = this.xCoordConstants.size();
            for (int i = 1; i < count; ++i) {
                xml.add(CoreConstants.COMMA, this.xCoordConstants.get(i));
            }
            xml.add('"');
        }

        if (this.yCoordConstants != null) {
            xml.add(" y-list=\"", this.yCoordConstants.getFirst());
            final int count = this.yCoordConstants.size();
            for (int i = 1; i < count; ++i) {
                xml.add(CoreConstants.COMMA, this.yCoordConstants.get(i));
            }
            xml.add('"');
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

        if (this.xCoordFormulas == null && this.yCoordFormulas == null) {
            xml.addln("/>");
        } else {
            xml.addln(">");

            if (this.xCoordFormulas != null) {
                for (final Formula formula : this.xCoordFormulas) {
                    xml.add(ind2, "<x>");
                    formula.appendChildrenXml(xml);
                    xml.addln("</x>");
                }
            }

            if (this.yCoordFormulas != null) {
                for (final Formula formula : this.yCoordFormulas) {
                    xml.add(ind2, "<y>");
                    formula.appendChildrenXml(xml);
                    xml.addln("</y>");
                }
            }

            xml.addln(ind, "</polygon>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Polygon";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) { // Do NOT change to "? super String"

        if (this.xCoordFormulas != null) {
            for (final Formula formula : this.xCoordFormulas) {
                set.addAll(formula.params.keySet());
            }
        }

        if (this.yCoordFormulas != null) {
            for (final Formula formula : this.yCoordFormulas) {
                set.addAll(formula.params.keySet());
            }
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.xCoordConstants)
                + Objects.hashCode(this.xCoordFormulas)
                + Objects.hashCode(this.yCoordConstants)
                + Objects.hashCode(this.yCoordFormulas)
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
        } else if (obj instanceof final DocPrimitivePolygon poly) {
            equal = Objects.equals(this.xCoordConstants, poly.xCoordConstants)
                    && Objects.equals(this.xCoordFormulas, poly.xCoordFormulas)
                    && Objects.equals(this.yCoordConstants, poly.yCoordConstants)
                    && Objects.equals(this.yCoordFormulas, poly.yCoordFormulas)
                    && Objects.equals(this.filled, poly.filled)
                    && Objects.equals(this.colorName, poly.colorName)
                    && Objects.equals(this.color, poly.color)
                    && Objects.equals(this.alpha, poly.alpha)
                    && Objects.equals(this.strokeWidth, poly.strokeWidth)
                    && Arrays.equals(this.dash, poly.dash);
        } else {
            equal = false;
        }

        return equal;
    }
}
