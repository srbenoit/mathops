package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocRelativeOffsetInst;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A document object that supports two items with a relative offset position, such as a superscript, subscript, over,
 * under, and so on. The type of alignment is controlled by a parameter.
 */
public final class DocRelativeOffset extends AbstractDocContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7695259420025491722L;

    /** The parent object. */
    private AbstractDocObjectTemplate base;

    /** Object that is superscripted relative to parent. */
    private AbstractDocObjectTemplate superscript;

    /** Object that is subscripted relative to parent. */
    private AbstractDocObjectTemplate subscript;

    /** Object that is over parent. */
    private AbstractDocObjectTemplate over;

    /** Object that is under parent. */
    private AbstractDocObjectTemplate under;

    /**
     * Construct a new {@code JDocRelativeOffset}.
     *
     * @param theBase        the base object
     * @param theSuperscript the object to display as a superscript to the parent
     * @param theSubscript   the object to display as a subscript to the parent
     * @param theOver        the object to display over the parent
     * @param theUnder       the object to display under the parent
     */
    DocRelativeOffset(final AbstractDocObjectTemplate theBase, final AbstractDocObjectTemplate theSuperscript,
                      final AbstractDocObjectTemplate theSubscript, final AbstractDocObjectTemplate theOver,
                      final AbstractDocObjectTemplate theUnder) {

        super();

        if (theBase != null) {
            this.base = theBase;
            add(this.base);
        }

        if (theSuperscript != null) {
            this.superscript = theSuperscript;
            add(this.superscript);
        }

        if (theSubscript != null) {
            this.subscript = theSubscript;
            add(this.subscript);
        }

        if (theOver != null) {
            this.over = theOver;
            add(this.over);
        }

        if (theUnder != null) {
            this.under = theUnder;
            add(this.under);
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
     * Gets the optional superscript expression.
     *
     * @return the superscript expression
     */
    public AbstractDocObjectTemplate getSuperscript() {

        return this.superscript;
    }

    /**
     * Gets the optional subscript expression.
     *
     * @return the subscript expression
     */
    public AbstractDocObjectTemplate getSubscript() {

        return this.subscript;
    }

    /**
     * Gets the optional over expression.
     *
     * @return the over expression
     */
    public AbstractDocObjectTemplate getOver() {

        return this.over;
    }

    /**
     * Gets the optional under expression.
     *
     * @return the under expression
     */
    public AbstractDocObjectTemplate getUnder() {

        return this.under;
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocRelativeOffset deepCopy() {

        final DocRelativeOffset copy = new DocRelativeOffset(null, null, null, null, null);

        copy.copyObjectFromContainer(this);

        if (this.base != null) {
            copy.base = this.base.deepCopy();
            copy.add(copy.base);
        }

        if (this.superscript != null) {
            copy.superscript = this.superscript.deepCopy();
            copy.add(copy.superscript);
        }

        if (this.subscript != null) {
            copy.subscript = this.subscript.deepCopy();
            copy.add(copy.subscript);
        }

        if (this.over != null) {
            copy.over = this.over.deepCopy();
            copy.add(copy.over);
        }

        if (this.under != null) {
            copy.under = this.under.deepCopy();
            copy.add(copy.under);
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
     * height will be recomputed based on current image size.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        int baseX = 0;
        int baseY = 0;
        int overX = 0;
        int underX = 0;

        // layout children if needed
        int baseWidth = 0;
        int baseHeight = 0;
        if (this.base != null) {
            this.base.doLayout(context, mathMode);
            baseWidth = this.base.getWidth();
            baseHeight = this.base.getHeight();
        }

        int supWidth = 0;
        int supHeight = 0;
        if (this.superscript != null) {
            this.superscript.doLayout(context, mathMode);
            supWidth = this.superscript.getWidth();
            supHeight = this.superscript.getHeight();
        }

        int subWidth = 0;
        int subHeight = 0;
        if (this.subscript != null) {
            this.subscript.doLayout(context, mathMode);
            subWidth = this.subscript.getWidth();
            subHeight = this.subscript.getHeight();
        }

        int overWidth = 0;
        int overHeight = 0;
        if (this.over != null) {
            this.over.doLayout(context, mathMode);
            overWidth = this.over.getWidth();
            overHeight = this.over.getHeight();
        }

        int underWidth = 0;
        int underHeight = 0;
        if (this.under != null) {
            this.under.doLayout(context, mathMode);
            underWidth = this.under.getWidth();
            underHeight = this.under.getHeight();
        }

        // Find max width of parent, over, under
        int max = Math.max(overWidth, baseWidth);
        max = Math.max(underWidth, max);

        // Determine overall width
        int w = max;
        w += Math.max(supWidth, subWidth);

        // Determine overall height
        int h = overHeight + baseHeight + underHeight;
        h += (supHeight / 2) > overHeight ? (supHeight / 2) - overHeight : 0;
        h += (subHeight / 2) > underHeight ? (subHeight / 2) - underHeight : 0;

        // Layout the parent, over and under pieces
        if (this.base != null) {
            baseX = (max - baseWidth) / 2;
            baseY = Math.max(overHeight, (supHeight / 2));
            this.base.setX(baseX);
            this.base.setY(baseY);
        }

        if (this.over != null) {
            overX = (max - overWidth) / 2;
            final int y = overHeight > (supHeight / 2) ? 0 : (supHeight / 2) - overHeight;
            this.over.setX(overX);
            this.over.setY(y);
        }

        if (this.under != null) {
            underX = (max - underWidth) / 2;
            final int y = overHeight > (supHeight / 2) ? overHeight + baseHeight
                    : (supHeight / 2) + baseHeight;
            this.under.setX(underX);
            this.under.setY(y);
        }

        if (this.base != null) {
            final int subSupShift = this.base.getFontSize() / 3;

            // Layout superscript and subscript
            if (this.superscript != null) {
                int x = subSupShift + baseWidth > overWidth ? baseX + baseWidth : overX + overWidth;
                if (this.base.isItalic()) {
                    x += this.base.getFontSize() / 6;
                } else if (this.base instanceof final AbstractDocSpanBase baseSpan) {
                    final AbstractDocObjectTemplate last = baseSpan.getLastChild();

                    if (last != null) {
                        if (last.isItalic()) {
                            x += this.base.getFontSize() / 6;
                        } else if (last instanceof final DocParameterReference baseRef) {

                            final AbstractVariable var = context.getVariable(baseRef.getVariableName());
                            if (var != null) {
                                final Object value = var.getValue();
                                if (value instanceof final DocSimpleSpan spanValue) {

                                    if (spanValue.isItalic()) {
                                        x += last.getFontSize() / 6;
                                    } else {
                                        final List<AbstractDocObjectTemplate> list = spanValue.getChildren();
                                        if (!list.isEmpty()) {
                                            final AbstractDocObjectTemplate inner = list.getLast();
                                            if (inner.isItalic()) {
                                                x += inner.getFontSize() / 6;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                final int y = overHeight > (supHeight / 2) ? overHeight - (supHeight / 3) : 0;
                this.superscript.setX(x);
                this.superscript.setY(y);
            }

            if (this.subscript != null) {
                final int x = subSupShift + baseWidth > underWidth ? baseX + baseWidth : underX + underWidth;

                final int y = overHeight > (supHeight / 2) ? overHeight + baseHeight - (2 * subHeight / 3)
                        : (supHeight / 2) + baseHeight - (2 * subHeight / 3);
                this.subscript.setX(x);
                this.subscript.setY(y);
            }
        }

        if (this.base != null) {
            setBaseLine(this.base.getBaseLine() + baseY);
            setCenterLine(this.base.getCenterLine() + baseY);
        } else {
            setBaseLine(baseY);
            setCenterLine(baseY);
        }

        setWidth(w);
        setHeight(h);
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

        if (this.base != null) {
            this.base.paintComponent(grx, mathMode);
        }

        if (this.superscript != null) {
            this.superscript.paintComponent(grx, mathMode);
        }

        if (this.subscript != null) {
            this.subscript.paintComponent(grx, mathMode);
        }

        if (this.over != null) {
            this.over.paintComponent(grx, mathMode);
        }

        if (this.under != null) {
            this.under.paintComponent(grx, mathMode);
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

        if (this.superscript != null) {
            this.superscript.accumulateParameterNames(set);
        }

        if (this.subscript != null) {
            this.subscript.accumulateParameterNames(set);
        }

        if (this.over != null) {
            this.over.accumulateParameterNames(set);
        }

        if (this.under != null) {
            this.under.accumulateParameterNames(set);
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
    public DocRelativeOffsetInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final AbstractDocObjectInst baseInst = this.base == null ? null
                : this.base.createInstance(evalContext);

        final AbstractDocObjectInst superscriptInst = this.superscript == null ? null
                : this.superscript.createInstance(evalContext);

        final AbstractDocObjectInst subscriptInst = this.subscript == null ? null
                : this.subscript.createInstance(evalContext);

        final AbstractDocObjectInst overInst = this.over == null ? null
                : this.over.createInstance(evalContext);

        final AbstractDocObjectInst underInst = this.under == null ? null
                : this.under.createInstance(evalContext);

        return new DocRelativeOffsetInst(objStyle, null, baseInst, superscriptInst, subscriptInst,
                overInst, underInst);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<rel-offset");
        printFormat(xml, 1.0f);
        xml.add('>');

        if (this.base != null) {
            this.base.toXml(xml, indent + 1);
        }

        if (this.superscript != null) {
            this.superscript.toXml(xml, indent + 1);
        }

        if (this.subscript != null) {
            this.subscript.toXml(xml, indent + 1);
        }

        if (this.over != null) {
            this.over.toXml(xml, indent + 1);
        }

        if (this.under != null) {
            this.under.toXml(xml, indent + 1);
        }

        xml.add("</rel-offset>");
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

        // TODO: Only supports superscript/subscript now.

        if (this.base != null) {
            this.base.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
        } else {
            builder.add('~'); // FIXME: Test this!
        }

        if (this.superscript != null) {
            builder.add("^{");
            this.superscript.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
            builder.add('}');
        }

        if (this.subscript != null) {
            builder.add("_{");
            this.subscript.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
            builder.add('}');
        }

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

        ps.print("<li>Relative Offset");
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

        buf.add("[RELOFF");

        if (this.base != null) {
            buf.add(" base=", this.base);
        }

        if (this.superscript != null) {
            buf.add(" sup=", this.superscript);
        }

        if (this.subscript != null) {
            buf.add(" sub=", this.subscript);
        }

        if (this.over != null) {
            buf.add(" over=", this.over);
        }

        if (this.under != null) {
            buf.add(" under=", this.under);
        }

        buf.add(']');

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
        } else if (obj instanceof final DocRelativeOffset offset) {
            equal = innerEquals(offset) //
                    && Objects.equals(this.base, offset.base)
                    && Objects.equals(this.superscript, offset.superscript)
                    && Objects.equals(this.subscript, offset.subscript)
                    && Objects.equals(this.over, offset.over)
                    && Objects.equals(this.under, offset.under);
        } else {
            equal = false;
        }

        return equal;
    }
}
