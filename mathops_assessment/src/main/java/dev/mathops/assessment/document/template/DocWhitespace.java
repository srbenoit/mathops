package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocWhitespaceInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Set;

/**
 * A whitespace in the document, which can be represented as a space or a newline depending on layout constraints. The
 * width of the space may also be adjusted to perform full justification.
 */
public final class DocWhitespace extends AbstractDocObjectTemplate {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5747889303283215642L;

    /**
     * Construct a new {@code DocWhitespace} object.
     */
    public DocWhitespace() {

        super();
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocWhitespace deepCopy() {

        final DocWhitespace copy = new DocWhitespace();

        copy.copyObjectFrom(this);

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

        final FontMetrics fm = BundledFontManager.getInstance().getFontMetrics(getFont());
        final int h = fm.getAscent() + fm.getDescent();

        int w = fm.stringWidth(CoreConstants.SPC);
        setBaseLine(fm.getAscent());
        setCenterLine((fm.getAscent() << 1) / 3);

        if (isBoxed()) {
            w += 4; // Allow 2 pixels on either end for box
        }

        setWidth(w);
        setHeight(h);
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

        grx.setFont(getFont());
        final FontMetrics fm = grx.getFontMetrics();

        final int lwidth = fm.stringWidth(CoreConstants.SPC);

        if (getColorName() != null) {
            grx.setColor(ColorNames.getColor(getColorName()));
        } else {
            grx.setColor(Color.BLACK);
        }

        if (isUnderline()) {
            final int x = 0;
            final int y = getBaseLine() + 1;
            grx.drawLine(x, y, x + lwidth, y);
        }

        if (isOverline()) {
            final int x = 0;
            final int y = 1;
            grx.drawLine(x, y, x + lwidth, y);
        }

        if (isStrikethrough()) {
            final int x = 0;
            final int y = getCenterLine();
            grx.drawLine(x, y, x + lwidth, y);
        }

        if (isBoxed()) {
            final int x = 0;
            final int y = 0;
            final int y2 = getHeight() - 1;
            grx.drawLine(x, y, -1, y);
            grx.drawLine(x, y2, -1, y2);
            grx.drawLine(x, y2, -1, y2);
            grx.drawLine(x, y, x, y2);
            grx.drawLine(-1, y, -1, y2);
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

        // No action
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
    public DocWhitespaceInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        return new DocWhitespaceInst(objStyle, null);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add(' ');
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

        builder.add(' ');
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Whitespace</li>");
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

        return docObjectHashCode();
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
        } else if (obj instanceof final DocWhitespace ws) {
            equal = docObjectEquals(ws);
        } else {
            equal = false;
        }

        return equal;
    }
}
