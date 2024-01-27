package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocFractionInst;
import dev.mathops.assessment.document.inst.DocNonwrappingSpanInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * A container object that presents two other document objects as a fraction, with a horizontal line drawn between
 * them.
 */
public final class DocFraction extends AbstractDocContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6237449886336745518L;

    /** The object acting as the numerator. */
    private DocNonwrappingSpan numerator;

    /** The object acting as the denominator. */
    private DocNonwrappingSpan denominator;

    /** The Y offset of the horizontal line. */
    private int lineY;

    /**
     * Construct a new {@code DocFraction} object.
     *
     * @param theNumerator   the object acting as a numerator
     * @param theDenominator the object acting as a denominator
     */
    DocFraction(final AbstractDocObjectTemplate theNumerator, final AbstractDocObjectTemplate theDenominator) {

        super();

        if (theNumerator != null) {

            if (theNumerator instanceof DocNonwrappingSpan) {
                this.numerator = (DocNonwrappingSpan) theNumerator;
            } else {
                this.numerator = new DocNonwrappingSpan();
                this.numerator.add(theNumerator);
            }

            this.numerator.tag = "numerator";
            add(this.numerator);
        }

        if (theDenominator != null) {

            if (theDenominator instanceof DocNonwrappingSpan) {
                this.denominator = (DocNonwrappingSpan) theDenominator;
            } else {
                this.denominator = new DocNonwrappingSpan();
                this.denominator.add(theDenominator);
            }

            this.denominator.tag = "denominator";
            add(this.denominator);
        }
    }

    /**
     * Gets the numerator.
     *
     * @return the numerator
     */
    public DocNonwrappingSpan getNumerator() {

        return this.numerator;
    }

    /**
     * Gets the denominator.
     *
     * @return the denominator
     */
    public DocNonwrappingSpan getDenominator() {

        return this.denominator;
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocFraction deepCopy() {

        final DocFraction copy = new DocFraction(null, null);

        copy.copyObjectFromContainer(this);

        if (this.numerator != null) {
            copy.numerator = this.numerator.deepCopy();
            copy.add(copy.numerator);
        }

        if (this.denominator != null) {
            copy.denominator = this.denominator.deepCopy();
            copy.add(copy.denominator);
        }

        copy.lineY = this.lineY;

        return copy;
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        return CENTERLINE;
    }

    /**
     * Recompute the bounding box of the object.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        // layout children if needed
        this.numerator.doLayout(context, mathMode);
        this.denominator.doLayout(context, mathMode);

        final int nWidth = this.numerator.getWidth();
        final int nHeight = this.numerator.getHeight();

        final int dWidth = this.denominator.getWidth();
        final int dHeight = this.denominator.getHeight();

        int w = Math.max(nWidth, dWidth);
        w += 4; // Allow visual separation between adjacent items.

        this.lineY = nHeight + 1;

        this.numerator.setX(((w - nWidth) / 2) + 1);
        this.numerator.setY(0);
        this.denominator.setX(((w - dWidth) / 2) + 1);
        this.denominator.setY(this.lineY + 3);

        setBaseLine(nHeight + 4 + this.denominator.getBaseLine());
        setCenterLine(this.lineY);

        final int h = nHeight + dHeight + 4; // Account for line.
        setWidth(w);
        setHeight(h);
    }

    /**
     * Draw the fraction bar. The numerator and denominator will be drawn afterward by the component drawing method.
     *
     * @param grx he {@code Graphics} object to which to draw the fraction bar
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        grx.setColor(getColorName() == null ? Color.BLACK : ColorNames.getColor(getColorName()));

        final int w = getWidth();

        grx.drawLine(2, this.lineY, w - 2, this.lineY);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.paintComponent(grx, mathMode);
        }

        postPaint(grx);
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        if (this.numerator != null) {
            this.numerator.accumulateParameterNames(set);
        }

        if (this.denominator != null) {
            this.denominator.accumulateParameterNames(set);
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
    public DocFractionInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final DocNonwrappingSpanInst numeratorInst = this.numerator == null ? null
                : this.numerator.createInstance(evalContext);

        final DocNonwrappingSpanInst denominatorInst = this.denominator == null ? null
                : this.denominator.createInstance(evalContext);

        return new DocFractionInst(objStyle, null, numeratorInst, denominatorInst);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<fraction");

//        if (CENTERLINE == BASELINE) {
//            xml.add(" valign='baseline'");
//        }

        printFormat(xml, 1.0f);
        xml.add('>');

        if (this.numerator != null) {
            this.numerator.toXml(xml, indent + 1);
        }

        if (this.denominator != null) {
            this.denominator.toXml(xml, indent + 1);
        }

        xml.add("</fraction>");
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

        char oldMode = 0;

        if (mode[0] == 'T') {
            builder.add('$');
            mode[0] = '$';
            oldMode = 'T';
        }

        builder.add("\\frac{");

        if (this.numerator != null) {
            this.numerator.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
        }

        builder.add("}{");

        if (this.denominator != null) {
            this.denominator.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
        }

        builder.add('}');

        if (oldMode == 'T') {
            builder.add('$');
            mode[0] = 'T';
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Fraction");
        printTreeContents(ps);
        ps.print("</li>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String result;

        if (this.numerator == null) {
            result = "(numerator is null)";
        } else if (this.denominator == null) {
            result = "(null denominator)";
        } else {
            result = this.numerator + " / " + this.denominator;
        }

        return result;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return innerHashCode() + Objects.hashCode(this.numerator)
                + Objects.hashCode(this.denominator);
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
        } else if (obj instanceof final DocFraction frac) {
            equal = innerEquals(frac) //
                    && Objects.equals(this.numerator, frac.numerator)
                    && Objects.equals(this.denominator, frac.denominator);
        } else {
            equal = false;
        }

        return equal;
    }
}
