package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocRadicalInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * A document object that places another object under a radical with an optional number specifying the root.
 */
public final class DocRadical extends AbstractDocContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 809527890572514676L;

    /** The argument, or item under the radical sign. */
    private AbstractDocObjectTemplate base;

    /** An optional root, or number above left end of radical sign. */
    private AbstractDocObjectTemplate root;

    /**
     * Construct a new {@code DocRadical} object.
     *
     * @param argument the object acting as an argument of the radical
     * @param theRoot  the object acting as a root for the radical
     */
    DocRadical(final AbstractDocObjectTemplate argument, final AbstractDocObjectTemplate theRoot) {

        super();

        if (argument != null) {
            this.base = argument;
            add(this.base);
        }

        if (theRoot != null) {
            this.root = theRoot;
            add(this.root);
        }
    }

    /**
     * Gets the base expression.
     *
     * @return the base expression
     */
    public AbstractDocObjectTemplate getBase() {

        return this.base;
    }

    /**
     * Gets the optional root expression.
     *
     * @return the root expression
     */
    public AbstractDocObjectTemplate getRoot() {

        return this.root;
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocRadical deepCopy() {

        final DocRadical copy = new DocRadical(null, null);

        copy.copyObjectFromContainer(this);

        if (this.base != null) {
            copy.base = this.base.deepCopy();
            copy.add(copy.base);
        }

        if (this.root != null) {
            copy.root = this.root.deepCopy();
            copy.add(copy.root);
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

        return this.base == null ? BASELINE : this.base.getLeftAlign();
    }

    /**
     * Recompute the bounding box of the object. The upper left corner of the box will not move, but the width and
     * height will be recomputed based on current image size.<br>
     * <br>
     * The radical symbol will follow the top and right bounds of the argument, and will touch the lower bound at a
     * point to the left of the argument by a distance of 1/4 the height of the argument, with a tail that extends at a
     * 45-degree angle, extending left by another 1/4 the height of the argument.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        this.base.doLayout(context, mathMode);

        final int bWidth = this.base.getWidth();
        final int bHeight = this.base.getHeight();

        int rWidth = 0;
        int rHeight = 0;
        if (this.root != null) {
            this.root.doLayout(context, mathMode);
            rWidth = this.root.getWidth();
            rHeight = this.root.getHeight();
        }

        // Compute size of left margin used for radical sign
        int left = ((bHeight - 2) / 2) + 2;

        if ((this.root != null) && (left < (rWidth + (rHeight / 4)))) {

            // Root width exceeds radical width, so margin is root width
            // plus space between root and radical sign
            left = rWidth + (rHeight / 4);
        }

        final int top = (rHeight > (bHeight / 2)) ? (rHeight - (bHeight / 2)) : 2;

        setWidth(left + bWidth + 5);
        setHeight(top + bHeight);

        this.base.setX(left);
        this.base.setY(top);

        if (this.root != null) {
            int x = left - 2 - rWidth - (rHeight / 4);

            if (x > ((left - rWidth) / 2)) {
                x = (left - rWidth) / 2;
            }

            this.root.setX(x);
            this.root.setY(0);
        }

        setBaseLine(this.base.getBaseLine() + top);
        setCenterLine(getHeight() / 2);
    }

    /**
     * Draw the radical.
     *
     * @param grx the {@code Graphics} object to which to draw the radical
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        Graphics2D g2d = null;
        Object origHints = null;

        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
            origHints = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }

        final int basey = this.base.getHeight() - 2;

        grx.setColor(getColorName() == null ? Color.BLACK : ColorNames.getColor(getColorName()));

        // Draw radical symbol
        grx.drawLine(this.base.getX() - 2, this.base.getY(), this.base.getX() + this.base.getWidth() + 1,
                this.base.getY());
        final int x1 = this.base.getX() - 2 - (basey / 4);
        final int x2 = x1 - (basey / 4);
        final int x3 = (x1 + x2) / 2;

        grx.drawLine(this.base.getX() - 2, this.base.getY(), x1, this.base.getY() + basey);
        grx.drawLine(x1, this.base.getY() + basey, x3, this.base.getY() + (basey * 3 / 4));
        grx.drawLine(x3, this.base.getY() + (basey * 3 / 4), x2, this.base.getY() + (basey * 3 / 4));
        grx.drawLine(x3, this.base.getY() + (basey * 3 / 4), x1, this.base.getY() + basey - 1);
        grx.drawLine(x1, this.base.getY() + basey - 1, this.base.getX() - 2, this.base.getY());

        // Restore state of Graphics
        if (g2d != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, origHints);
        }

        this.base.paintComponent(grx, mathMode);

        if (this.root != null) {
            this.root.paintComponent(grx, mathMode);
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

        if (this.base != null) {
            this.base.accumulateParameterNames(set);
        }

        if (this.root != null) {
            this.root.accumulateParameterNames(set);
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
    public DocRadicalInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final AbstractDocObjectInst baseInst = this.base == null ? null
                : this.base.createInstance(evalContext);

        final AbstractDocObjectInst rootInst = this.root == null ? null
                : this.root.createInstance(evalContext);

        return new DocRadicalInst(objStyle, null, baseInst, rootInst);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<radical");
        printFormat(xml, 1.0f);
        xml.add('>');

        if (this.base != null) {
            this.base.toXml(xml, indent + 1);
        }

        if (this.root != null) {
            this.root.toXml(xml, indent + 1);
        }

        xml.add("</radical>");
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

        if (this.root != null) {
            builder.add("\\sqrt[");
            this.root.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
            builder.add("]{");
        } else {
            builder.add("\\sqrt{");
        }

        if (this.base != null) {
            this.base.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
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

        ps.print("<li>Radical");
        printTreeContents(ps);
        ps.print("</li>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder buf = new HtmlBuilder(50);

        buf.add("[RADICAL");

        if (this.root != null) {
            buf.add(" root=");
            buf.add(this.root.toString());
        }

        buf.add(" (");
        buf.add(this.base.toString());
        buf.add(")]");

        return buf.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return innerHashCode();
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
        } else if (obj instanceof final DocRadical radical) {
            equal = innerEquals(radical)
                    && Objects.equals(this.base, radical.base)
                    && Objects.equals(this.root, radical.root);
        } else {
            equal = false;
        }

        return equal;
    }
}
