package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocHSpaceInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.font.BundledFontManager;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * A run of horizontal space of specified width. Width is a formula that gives a real number of widths of a decimal "0"
 * digit" (clamped to non-negative values).
 */
public final class DocHSpace extends AbstractDocObjectTemplate {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3455775449313140196L;

    /** The width. */
    private NumberOrFormula spaceWidth;

    /**
     * Construct a new {@code DocHSpace} object.
     */
    DocHSpace() {

        super();
    }

    /**
     * Sets the space width.
     *
     * @param theSpaceWidth the new width
     */
    void setSpaceWidth(final NumberOrFormula theSpaceWidth) {

        this.spaceWidth = theSpaceWidth;
    }

//    /**
//     * Gets the space width.
//     *
//     * @return the space width
//     */
//    public NumberOrFormula getSpaceWidth() {
//
//        return this.spaceWidth;
//    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocHSpace deepCopy() {

        final DocHSpace copy = new DocHSpace();

        copy.copyObjectFrom(this);
        copy.spaceWidth = this.spaceWidth == null ? null : this.spaceWidth.deepCopy();

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
     * Recompute the size of the object's bounding box.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        final double wid = calculateWidth(context);

        final FontMetrics fm = BundledFontManager.getInstance().getFontMetrics(getFont());
        final int digitWidth = fm.stringWidth("0");

        final int h = fm.getAscent() + fm.getDescent();

        int w = (int) Math.round(Math.max(0.0, (double) digitWidth * wid));
        setBaseLine(fm.getAscent());
        setCenterLine((fm.getAscent() << 1) / 3);

        if (isBoxed()) {
            w += 4; // Allow 2 pixels on either end for box
        }

        setWidth(w);
        setHeight(h);
    }

    /**
     * Calculates the width.
     *
     * @param context the evaluation context
     * @return the width
     */
    public double calculateWidth(final EvalContext context) {

        double wid = 1.0;

        if (this.spaceWidth != null) {
            final Object result = this.spaceWidth.evaluate(context);
            if (result instanceof final Number nbr) {
                wid = nbr.doubleValue();
            }
        }

        return wid;
    }

    /**
     * Draw the object.
     *
     * @param grx the {@code Graphics} to draw to
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        postPaint(grx);
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(@SuppressWarnings("BoundedWildcard") final Set<String> set) {

        if (this.spaceWidth != null && this.spaceWidth.getFormula() != null) {
            set.addAll(this.spaceWidth.getFormula().params.keySet());
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
    public DocHSpaceInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final Object widthResult = this.spaceWidth.evaluate(evalContext);

        final DocHSpaceInst instance;

        if (widthResult instanceof final Number nbr) {
            instance = new DocHSpaceInst(objStyle, null, nbr.doubleValue());
        } else  {
            instance = null;
        }

        return instance;
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<h-space");
        if (this.spaceWidth != null) {
            xml.addAttribute("width", this.spaceWidth.getNumber(), 0);
        }

        if (this.spaceWidth != null && this.spaceWidth.getFormula() != null) {
            xml.add("><width>");
            this.spaceWidth.getFormula().appendChildrenXml(xml);
            xml.add("</width></h-space>");
        } else {
            xml.add("/>");
        }
    }

    /**
     * Write the LaTeX representation of the object to a string buffer.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file; the value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time (this method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder      the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  true to show answers in any inputs embedded in the document; false if answers should not be
     *                     shown
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex,
                        final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                        final char[] mode, final EvalContext context) {

        final double wid = calculateWidth(context);

        // LATEX header must include this:
        // \newlength{\digitwidth}
        // \settowidth{\digitwidth}{6}

        builder.add("\\hspace*{" + wid + "*\\digitwidth}");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>HSpace</li>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return CoreConstants.SPC;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectHashCode() + Objects.hashCode(this.spaceWidth);
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
        } else if (obj instanceof final DocHSpace spc) {
            equal = docObjectEquals(spc)
                    && Objects.equals(this.spaceWidth, spc.spaceWidth);
        } else {
            equal = false;
        }

        return equal;
    }
}
