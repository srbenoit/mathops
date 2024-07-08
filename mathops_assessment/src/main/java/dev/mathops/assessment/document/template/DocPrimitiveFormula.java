package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.DocPrimitiveFormulaInst;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * A formula primitive (rendered as a graph of the function).
 */
final class DocPrimitiveFormula extends AbstractDocPrimitive {

    /** A smooth curve, computing function at each pixel. */
    static final int CURVE = 0;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -375439145801857218L;

    /** The formula to graph. */
    private final Formula formula;

    /** The name of the domain variable that will be used for graphing. */
    private String domainVarName;

    /** The color to use when plotting the formula. */
    private final String formulaColorName;

    /** The color to use when plotting the formula. */
    private final Color formulaColor;

    /** The style of plot to use for the formula. */
    private final int formulaStyle;

    /** The minimum X value from which to draw the function. */
    private final NumberOrFormula formulaMinX;

    /** The maximum X value to which to draw the function. */
    private final NumberOrFormula formulaMaxX;

    /** The min x coordinate of the window that the graph shows. */
    private Number windowMinX = null;

    /** The max x coordinate of the window that the graph shows. */
    private Number windowMaxX = null;

    /** The min y coordinate of the window that the graph shows. */
    private Number windowMinY = null;

    /** The max y coordinate of the window that the graph shows. */
    private Number windowMaxY = null;

    /** Graph bounds within drawing surface. */
    private transient Rectangle bounds = null;

    /**
     * Construct a new {@code DocPrimitiveFormula}.
     *
     * @param theOwner     the object that owns this primitive
     * @param theFormula   the formula to graph
     * @param theColorName the color to use when plotting the formula
     * @param theColor     the color to use when plotting the formula
     * @param theStyle     the style of plot to use for the formula
     * @param theMinX      the minimum X from which to draw the function
     * @param theMaxX      the maximum X to which to draw the function
     */
    DocPrimitiveFormula(final AbstractDocPrimitiveContainer theOwner, final Formula theFormula,
                        final String theColorName, final Color theColor, final int theStyle,
                        final NumberOrFormula theMinX, final NumberOrFormula theMaxX) {

        super(theOwner);

        this.formula = theFormula;
        this.formulaColorName = theColorName;
        this.formulaColor = theColor;
        this.formulaStyle = theStyle;
        this.formulaMinX = theMinX;
        this.formulaMaxX = theMaxX;

        this.domainVarName = "x";
    }

    /**
     * Set the graph window.
     *
     * @param minX the left edge of the window
     * @param maxX the right edge of the window
     * @param minY the top edge of the window
     * @param maxY the bottom edge of the window
     */
    void setWindow(final Number minX, final Number maxX, final Number minY, final Number maxY) {

        if (minX == null || minY == null || maxX == null || maxY == null) {
            Log.warning("Missing window parameter - ignoring.");
        } else {
            this.windowMinX = minX;
            this.windowMinY = minY;
            this.windowMaxX = maxX;
            this.windowMaxY = maxY;
        }
    }

    /**
     * Sets the domain variable name.
     *
     * @param theDomainVarName the domain variable name
     */
    void setDomainVarName(final String theDomainVarName) {

        if (theDomainVarName != null && !theDomainVarName.isEmpty()) {
            this.domainVarName = theDomainVarName;
        }
    }

    /**
     * Gets the domain variable name.
     *
     * @return the domain variable name
     */
    String getDomainVarName() {

        return this.domainVarName;
    }

    /**
     * Sets the graph bounds (used for drawing).
     *
     * @param theBounds the bounds
     */
    public void setBounds(final Rectangle theBounds) {

        this.bounds = theBounds;
    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @param theOwner the new owner
     * @return the copy
     */
    @Override
    public DocPrimitiveFormula
    deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final Formula newFormula = this.formula == null ? null : this.formula.deepCopy();
        final NumberOrFormula newMinX = this.formulaMinX == null ? null : this.formulaMinX.deepCopy();
        final NumberOrFormula newMaxX = this.formulaMaxX == null ? null : this.formulaMaxX.deepCopy();

        return new DocPrimitiveFormula(theOwner, newFormula, this.formulaColorName,
                this.formulaColor, this.formulaStyle, newMinX, newMaxX);
    }

    /**
     * Draw the primitive.
     *
     * @param grx     the graphics on which to draw
     * @param context the evaluation context
     */
    @Override
    public void draw(final Graphics2D grx, final EvalContext context) {

        if (this.formula != null && this.formulaStyle == CURVE && this.windowMinX != null
                && this.windowMaxX != null && this.windowMinY != null && this.windowMaxY != null) {

            final AbstractVariable xValue = context.getVariable(this.domainVarName);

            double ddx = Double.NaN;

            final double minX = this.windowMinX.doubleValue();
            final double maxX = this.windowMaxX.doubleValue();
            final double minY = this.windowMinY.doubleValue();
            final double maxY = this.windowMaxY.doubleValue();

            boolean started = false;
            boolean enteredDomain = false;
            int prior = -Integer.MAX_VALUE;
            grx.setColor(this.formulaColor);
            final double per = (maxX - minX) / (double) this.bounds.width;

            // Safety check to prevent infinite loops
            if (per <= 0.0) {
                Log.warning("Bad per-pixel when drawing graph");
            } else {
                int x = this.bounds.x;

                double domainMin = minX;
                if (this.formulaMinX != null) {
                    final Object obj = this.formulaMinX.evaluate(context);

                    if (obj instanceof final Number numberObj) {
                        domainMin = numberObj.doubleValue();
                    } else {
                        // Bad output of 'minx' formula - don't draw
                        Log.warning(new IllegalArgumentException("Bad output from 'minx' when drawing graph ("
                                + this.formulaMinX + " = " + obj + ")"));
                        domainMin = maxX;
                    }
                }

                double domainMax = maxX;
                if (this.formulaMaxX != null) {
                    final Object obj = this.formulaMaxX.evaluate(context);

                    if (obj instanceof final Number numberObj) {
                        domainMax = numberObj.doubleValue();
                    } else {
                        // Bad output of 'minx' formula - don't draw
                        Log.warning(new IllegalArgumentException("Bad output from 'maxx' when drawing graph ("
                                + this.formulaMaxX + " = " + obj + ")"));
                        domainMax = minX;
                    }
                }

                for (double dx = minX; dx < maxX; dx += per) {

                    Object obj = null;

                    // Check against formula domain
                    if (dx < domainMin) {
                        // Still outside domain, don't draw yet
                        ++x;
                        continue;
                    }

                    if (!enteredDomain) {
                        // Snap x to domain start the first instant we enter domain
                        ddx = domainMin;
                        enteredDomain = true;
                    }

                    if (dx > domainMax) {
                        // Beyond the domain, stop drawing
                        ++x;
                        continue;
                    }

                    if ((dx + per) > domainMax) {
                        // Next full step will go outside domain - snap to domain end
                        dx = domainMax;
                    }

                    // We're inside the visible domain of the function...

                    // "ddx" is an override "dx" value to snap to domain start
                    if (Double.isNaN(ddx)) {
                        xValue.setValue(Double.valueOf(dx));
                    } else {
                        xValue.setValue(Double.valueOf(ddx));
                        ddx = Double.NaN;
                    }

                    // Compute the formula value
                    final Object newObj = this.formula.evaluate(context);

                    // Log.info("(x,y) = (" + dx + "," + obj + ")");

                    double dy;
                    if (newObj instanceof final Number numObj) {
                        dy = numObj.doubleValue();

                        // Compute Y position of point and draw a line to it.
                        dy = (dy - minY) / (maxY - minY);
                        final int y = this.bounds.y + this.bounds.height - (int) (dy * (double) this.bounds.height);

                        if (!started) {
                            grx.drawLine(x, y, x, y);
                            started = true;
                        } else // Detect wild swings like asymptotes, avoid drawing
                            if (Math.abs(y - prior) < 500) {
                                grx.drawLine(x - 1, prior, x, y);
                            }

                        prior = y;
                    }
                    ++x;
                }
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
    public DocPrimitiveFormulaInst createInstance(final EvalContext evalContext) {

        final Object minX = this.formulaMinX.evaluate(evalContext);
        final Object maxX = this.formulaMaxX.evaluate(evalContext);

        final DocPrimitiveFormulaInst result;

        if (minX instanceof final Number minNbr && maxX instanceof final Number maxNbr) {

            final AbstractFormulaObject simplified = this.formula.simplify(evalContext);
            final Formula simpleFormula;
            if (simplified instanceof final Formula sf) {
                simpleFormula = sf;
            } else {
                simpleFormula = new Formula(simplified);
            }

            final StrokeStyle stroke = new StrokeStyle(1.0, this.formulaColorName, null, 1.0, EStrokeCap.BUTT,
                    EStrokeJoin.MITER, 10.0f);

            result = new DocPrimitiveFormulaInst(simpleFormula, this.domainVarName, stroke, minNbr.doubleValue(),
                    maxNbr.doubleValue());
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

        xml.add(ind, "<formula");

        if (!"x".equals(this.domainVarName)) {
            xml.add(" domain-var=\"", this.domainVarName, CoreConstants.QUOTE);
        }

        if (this.formulaColorName != null) {
            xml.add(" color=\"", this.formulaColorName, CoreConstants.QUOTE);
        }

        if (this.formulaStyle == CURVE) {
            xml.add(" style=\"curve\"");
        }

        if (this.formulaMinX != null && this.formulaMinX.getNumber() != null) {
            xml.add(" minx=\"", this.formulaMinX.getNumber(), CoreConstants.QUOTE);
        }

        if (this.formulaMaxX != null && this.formulaMaxX.getNumber() != null) {
            xml.add(" maxx=\"", this.formulaMaxX.getNumber(), CoreConstants.QUOTE);
        }
        xml.add('>');

        if (this.formulaMinX != null && this.formulaMinX.getFormula() != null) {
            xml.add("<minx>");
            this.formulaMinX.getFormula().appendChildrenXml(xml);
            xml.add("</minx>");
        }

        if (this.formulaMaxX != null && this.formulaMaxX.getFormula() != null) {
            xml.add("<maxx>");
            this.formulaMaxX.getFormula().appendChildrenXml(xml);
            xml.add("</maxx>");
        }

        if (this.formula != null) {
            xml.add("<expr>");
            this.formula.appendChildrenXml(xml);
            xml.add("</expr>");
        }

        xml.addln("</formula>");
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Formula";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(@SuppressWarnings("BoundedWildcard") final Set<String> set) {

        set.add(this.domainVarName);
        if (this.formula != null) {
            set.addAll(this.formula.params.keySet());
        }
        if (this.formulaMinX != null && this.formulaMinX.getFormula() != null) {
            set.addAll(this.formulaMinX.getFormula().params.keySet());
        }
        if (this.formulaMaxX != null && this.formulaMaxX.getFormula() != null) {
            set.addAll(this.formulaMaxX.getFormula().params.keySet());
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.formula)
                + Objects.hashCode(this.formulaColorName)
                + Objects.hashCode(this.formulaColor)
                + this.formulaStyle
                + Objects.hashCode(this.formulaMinX)
                + Objects.hashCode(this.formulaMaxX);
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
        } else if (obj instanceof final DocPrimitiveFormula form) {
            equal = Objects.equals(this.formula, form.formula)
                    && Objects.equals(this.formulaColorName, form.formulaColorName)
                    && Objects.equals(this.formulaColor, form.formulaColor)
                    && this.formulaStyle == form.formulaStyle
                    && Objects.equals(this.formulaMinX, form.formulaMinX)
                    && Objects.equals(this.formulaMaxX, form.formulaMaxX);
        } else {
            equal = false;
        }

        return equal;
    }
}
