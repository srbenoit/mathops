package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocVSpaceInst;
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
 * A run of vertical space of specified height. Height is a formula that gives a real number of "ems" (clamped to
 * non-negative values).
 */
public final class DocVSpace extends AbstractDocObjectTemplate {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3364179087193536278L;

    /** The height. */
    private NumberOrFormula spaceHeight;

    /**
     * Construct a new {@code DocVSpace} object.
     */
    DocVSpace() {

        super();
    }

    /**
     * Sets the space height.
     *
     * @param theSpaceHeight the new height
     */
    void setSpaceHeight(final NumberOrFormula theSpaceHeight) {

        this.spaceHeight = theSpaceHeight;
    }

    /**
     * Gets the space height.
     *
     * @return the space height
     */
    public NumberOrFormula getSpaceHeight() {

        return this.spaceHeight;
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocVSpace deepCopy() {

        final DocVSpace copy = new DocVSpace();

        copy.copyObjectFrom(this);
        copy.spaceHeight = this.spaceHeight == null ? null : this.spaceHeight.deepCopy();

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

        final double hgt = calculateHeight(context);

        final FontMetrics fm = BundledFontManager.getInstance().getFontMetrics(getFont());
        final double heightDbl = hgt * (double) (fm.getAscent() + fm.getDescent());
        final int h = (int) Math.round(Math.max(0.0, heightDbl));

        setBaseLine(fm.getAscent());
        setCenterLine((fm.getAscent() << 1) / 3);

        int w = 1;
        if (isBoxed()) {
            w += 4; // Allow 2 pixels on either end for box
        }

        setWidth(w);
        setHeight(h);
    }

    /**
     * Calculates the height.
     *
     * @param context the evaluation context
     * @return the height
     */
    public double calculateHeight(final EvalContext context) {

        double hgt = 1.0;

        if (this.spaceHeight != null) {
            final Object result = this.spaceHeight.evaluate(context);
            if (result instanceof final Number nbr) {
                hgt = nbr.doubleValue();
            }
        }

        return hgt;
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
    public void accumulateParameterNames(final Set<String> set) { // Do NOT change to "? super String"

        if (this.spaceHeight != null && this.spaceHeight.getFormula() != null) {
            set.addAll(this.spaceHeight.getFormula().params.keySet());
        }
    }

    /**
     * Generates an iteration of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the iteration document object; null if unable to create the iteration
     */
    @Override
    public DocVSpaceInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle style = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final Object heightResult = this.spaceHeight.evaluate(evalContext);

        final DocVSpaceInst instance;

        if (heightResult instanceof final Number nbr) {
            instance = new DocVSpaceInst(style, null, nbr.doubleValue());
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

        final String ind = AbstractDocObjectTemplate.makeIndent(indent);

        xml.add(ind, "<v-space");
        if (this.spaceHeight != null) {
            xml.addAttribute("height", this.spaceHeight.getNumber(), 0);
        }

        if (this.spaceHeight != null && this.spaceHeight.getFormula() != null) {
            xml.add("><height>");
            this.spaceHeight.getFormula().appendChildrenXml(xml);
            xml.add("</height></v-space>");
        } else {
            xml.addln("/>");
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

        final double hgt = calculateHeight(context);

        builder.addln("\\vspace*{" + hgt + "em}");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>VSpace</li>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return CoreConstants.CRLF;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectHashCode() + EqualityTests.objectHashCode(this.spaceHeight);
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
        } else if (obj instanceof final DocVSpace spc) {
            equal = docObjectEquals(spc)
                    && Objects.equals(this.spaceHeight, spc.spaceHeight);
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

        if (other instanceof final DocVSpace obj) {
            docObjectWhyNotEqual(obj, indent);
        } else {
            Log.info(makeIndent(indent), "UNEQUAL DocVSpace because other is ",
                    other.getClass().getName());
        }
    }
}
