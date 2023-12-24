package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.AbstractPrimitiveInst;
import dev.mathops.assessment.document.inst.DocDrawingInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.XmlEscaper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A drawing in a document.
 */
public final class DocDrawing extends AbstractDocPrimitiveContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1285376936772288188L;

    /** The width constant. */
    private Long widthConstant;

    /** The width formula. */
    private Formula widthFormula;

    /** The height constant. */
    private Long heightConstant;

    /** The height formula. */
    private Formula heightFormula;

    /**
     * Construct a new {@code DocDrawing}.
     *
     * @param width  the width of the object
     * @param height the height of the object
     * @param theAltText the alternative text for the generated image for accessibility
     */
    DocDrawing(final int width, final int height, final String theAltText) {

        super(width, height, theAltText);

        this.widthConstant = Long.valueOf((long) width);
        this.heightConstant = Long.valueOf((long) height);
    }

//    /**
//     * Sets the width constant. This clears the width formula.
//     *
//     * @param theWidthConstant the constant
//     */
//    public void setWidthConstant(final Long theWidthConstant) {
//
//        this.widthConstant = theWidthConstant;
//        this.widthFormula = null;
//    }

//    /**
//     * Gets the width constant.
//     *
//     * @return the constant
//     */
//    public Long getWidthConstant() {
//
//        return this.widthConstant;
//    }

    /**
     * Sets the width formula. This clears the width constant.
     *
     * @param theWidthFormula the formula
     */
    void setWidthFormula(final Formula theWidthFormula) {

        this.widthFormula = theWidthFormula;
        this.widthConstant = null;
    }

//    /**
//     * Gets the width formula.
//     *
//     * @return the formula
//     */
//    public Formula getWidthFormula() {
//
//        return this.widthFormula;
//    }

//    /**
//     * Sets the height constant. This clears the height formula.
//     *
//     * @param theHeightConstant the constant
//     */
//    public void setHeightConstant(final Long theHeightConstant) {
//
//        this.heightConstant = theHeightConstant;
//        this.heightFormula = null;
//    }

//    /**
//     * Gets the height constant.
//     *
//     * @return the constant
//     */
//    public Long getHeightConstant() {
//
//        return this.heightConstant;
//    }

    /**
     * Sets the height formula. This clears the height constant.
     *
     * @param theHeightFormula the formula
     */
    void setHeightFormula(final Formula theHeightFormula) {

        this.heightFormula = theHeightFormula;
        this.heightConstant = null;
    }

//    /**
//     * Gets the height formula.
//     *
//     * @return the formula
//     */
//    public Formula getHeightFormula() {
//
//        return this.heightFormula;
//    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocDrawing deepCopy() {

        final String alt = getAltText();
        final DocDrawing copy = new DocDrawing(this.origWidth, this.origHeight, alt);

        copy.copyObjectFromContainer(this);

        copy.widthConstant = this.widthConstant;
        if (this.widthFormula != null) {
            copy.widthFormula = this.widthFormula.deepCopy();
        }

        copy.heightConstant = this.heightConstant;
        if (this.heightFormula != null) {
            copy.heightFormula = this.heightFormula.deepCopy();
        }

        for (final AbstractDocPrimitive prim : getPrimitives()) {
            copy.addPrimitive(prim.deepCopy(copy));
        }

        return copy;
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        return BASELINE;
    }

    /**
     * Draw the graph to an offscreen image.
     *
     * @param forceWhite true to force background rectangle to be white if it is the first primitive in the drawing
     *                   and it is filled
     * @param context    the evaluation context
     */
    @Override
    public void buildOffscreen(final boolean forceWhite, final EvalContext context) {

        // Calculate width and height
        int w = getWidth();
        if (this.widthConstant != null) {
            w = this.widthConstant.intValue();
        } else if (this.widthFormula != null) {
            final Object o = this.widthFormula.evaluate(context);
            if (o instanceof final Number nbr) {
                w = nbr.intValue();
            }
        }

        int h = getHeight();
        if (this.heightConstant != null) {
            h = this.heightConstant.intValue();
        } else if (this.heightFormula != null) {
            final Object o = this.heightFormula.evaluate(context);
            if (o instanceof final Number nbr) {
                h = nbr.intValue();
            }
        }

        w = Math.round((float) w * getScale());
        h = Math.round((float) h * getScale());

        setWidth(w);
        setHeight(h);

        innerCreateOffscreen(w, h);

        final Graphics2D g2d = (Graphics2D) (getOffscreen().getGraphics());
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw primitives
        final int count = getPrimitives().size();
        for (int i = 0; i < count; ++i) {
            final AbstractDocPrimitive prim = getPrimitives().get(i);

            // If the first primitive is a rectangle, and we are not to fill an outer rect,
            // then make sure its fill is off.
            if (prim instanceof final DocPrimitiveRectangle rect && i == 0 && forceWhite) {
                rect.setFillColor("white");
            }

            prim.draw(g2d, context);
        }
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        // Calculate width and height
        int w = getWidth();
        if (this.widthConstant != null) {
            w = this.widthConstant.intValue();
        } else if (this.widthFormula != null) {
            final Object o = this.widthFormula.evaluate(context);
            if (o instanceof final Number nbr) {
                w = nbr.intValue();
            }
        }

        int h = getHeight();
        if (this.heightConstant != null) {
            h = this.heightConstant.intValue();
        } else if (this.heightFormula != null) {
            final Object o = this.heightFormula.evaluate(context);
            if (o instanceof final Number nbr) {
                h = nbr.intValue();
            }
        }

        setOrigSize(w, h);

        super.doLayout(context, mathMode);
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(@SuppressWarnings("BoundedWildcard") final Set<String> set) {

        super.accumulateParameterNames(set);

        if (this.widthFormula != null) {
            set.addAll(this.widthFormula.params.keySet());
        }
        if (this.heightFormula != null) {
            set.addAll(this.heightFormula.params.keySet());
        }
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object; null if unable to create the instance
     */
    @Override
    public DocDrawingInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final List<AbstractDocPrimitive> primitives = getPrimitives();
        final List<AbstractPrimitiveInst> primitivesInstList = new ArrayList<>(primitives.size());

        for (final AbstractDocPrimitive primitive : primitives) {
            primitivesInstList.add(primitive.createInstance(evalContext));
        }

        final String alt = getAltText();
        final String actualAlt = alt == null ? null : generateStringContents(evalContext, alt);

        return new DocDrawingInst(objStyle, null, getWidth(), getHeight(), actualAlt, null, primitivesInstList);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);
        final String ind2 = makeIndent(indent + 1);

        xml.add(ind, "<drawing");

        if (this.widthConstant != null) {
            xml.add(" width='", Integer.toString(getWidth()), "'");
        }
        if (this.heightConstant != null) {
            xml.add(" height='", Integer.toString(getHeight()), "'");
        }

        printFormat(xml, 1.0f);

        final String alt = getAltText();
        if (alt != null) {
            xml.add(" alt='", XmlEscaper.escape(alt), "'");
        }

        xml.add('>');

        if (this.widthFormula != null) {
            xml.add(ind2, "<width>");
            this.widthFormula.appendChildrenXml(xml);
            xml.addln("</width>");
        }

        if (this.heightFormula != null) {
            xml.add(ind2, "<height>");
            this.heightFormula.appendChildrenXml(xml);
            xml.addln("</height>");
        }

        appendPrimitivesXml(xml, indent + 1);

        xml.add(ind, "</drawing>");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Drawing");
        printTreeContents(ps);
        ps.print("</li>");
    }

    /**
     * Generate a {@code String} representation of the image (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "[DRAWING]";
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return primitiveContainerHashCode() + Objects.hashCode(getPrimitives());
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
        } else if (obj instanceof final DocDrawing drw) {
            equal = primitiveContainerEquals(drw) && Objects.equals(getPrimitives(), drw.getPrimitives());
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

        if (other instanceof final DocDrawing obj) {
            primitiveContainerWhyNotEqual(obj, indent);

            if (!Objects.equals(getPrimitives(), obj.getPrimitives())) {
                if (getPrimitives() == null || obj.getPrimitives() == null) {
                    Log.info(makeIndent(indent), "UNEQUAL DocDrawing (primitives: ", getPrimitives(), "!=",
                            obj.getPrimitives(), ")");
                } else {
                    final int numPriv = getPrimitives().size();
                    if (numPriv == obj.getPrimitives().size()) {
                        for (int i = 0; i < numPriv; ++i) {
                            final AbstractDocPrimitive o1 = getPrimitives().get(i);
                            final AbstractDocPrimitive o2 = obj.getPrimitives().get(i);

                            if (!Objects.equals(o1, o2)) {
                                if (o1 == null || o2 == null) {
                                    Log.info(makeIndent(indent), "UNEQUAL DocDrawing (primitive " + i + ": ", o1, "!=",
                                            o2, ")");
                                } else {
                                    Log.info(makeIndent(indent), "UNEQUAL DocDrawing (primitive " + i + "...) ",
                                            o1.getClass().getName());
                                    o1.whyNotEqual(o2, indent + 1);
                                }
                            }
                        }
                    } else {
                        Log.info(makeIndent(indent), "UNEQUAL DocDrawing (primitives size: "
                                + getPrimitives().size() + "!=" + obj.getPrimitives().size() + ")");
                    }
                }
            }
        } else {
            Log.info(makeIndent(indent), "UNEQUAL DocDrawing because other is ", other.getClass().getName());
        }
    }
}
