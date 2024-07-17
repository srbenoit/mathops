package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocHSpaceInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.font.BundledFontManager;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * An invisible object that gets positioned during paragraph layout to place the following object at a specified
 * horizontal offset from the left edge, with the same units of measure as DocHSpace or DocAlignmentMark.
 */
public final class DocHAlign extends AbstractDocObjectTemplate {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 8488166895362286547L;

    /** The position. */
    private NumberOrFormula position;

    /**
     * Construct a new {@code DocHAlign} object.
     */
    DocHAlign() {

        super();
    }

    /**
     * Sets the position.
     *
     * @param thePosition the new position
     */
    void setPosition(final NumberOrFormula thePosition) {

        this.position = thePosition;
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public NumberOrFormula getPosition() {

        return this.position;
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocHAlign deepCopy() {

        final DocHAlign copy = new DocHAlign();

        copy.copyObjectFrom(this);
        copy.position = this.position == null ? null : this.position.deepCopy();

        return copy;
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

        setBaseLine(fm.getAscent());
        setCenterLine((fm.getAscent() << 1) / 3);

        setWidth(0);
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

        if (this.position != null) {
            final Object result = this.position.evaluate(context);
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

        if (this.position != null && this.position.getFormula() != null) {
            set.addAll(this.position.getFormula().params.keySet());
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

        final Object positionResult = this.position.evaluate(evalContext);

        final DocHSpaceInst instance;

        if (positionResult instanceof final Number nbr) {
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

        xml.add("<h-align");
        if (this.position != null) {
            xml.addAttribute("position", this.position.getNumber(), 0);
        }

        if (this.position != null && this.position.getFormula() != null) {
            xml.add("><position>");
            this.position.getFormula().appendChildrenXml(xml);
            xml.add("</position></h-align>");
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

        // TODO: How to implement in LaTeX?

//        final double wid = calculateWidth(context);

        // LATEX header must include this:
        // \newlength{\digitwidth}
        // \settowidth{\digitwidth}{6}

//        builder.add("\\hspace*{" + wid + "*\\digitwidth}");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>HAlign</li>");
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

        return docObjectHashCode() + Objects.hashCode(this.position);
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
        } else if (obj instanceof final DocHAlign spc) {
            equal = docObjectEquals(spc)
                    && Objects.equals(this.position, spc.position);
        } else {
            equal = false;
        }

        return equal;
    }
}
