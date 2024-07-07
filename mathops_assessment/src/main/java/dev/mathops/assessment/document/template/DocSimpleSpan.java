package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.AbstractDocContainerInst;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocWrappingSpanInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;

import java.awt.Graphics;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple span is a collection of document objects which flow naturally in a left-to-right, top-down ordering. This
 * type of span can be embedded in a paragraph, or in another simple span, but cannot be contained in any other object.
 * When printed in XML form, it has no surrounding tag, and may not have formatting applied to it.
 */
public class DocSimpleSpan extends AbstractDocSpanBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -804918610887782652L;

    /**
     * Construct a new {@code DocSimpleSpan} object.
     */
    public DocSimpleSpan() {

        super();
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocSimpleSpan deepCopy() {

        final DocSimpleSpan copy = new DocSimpleSpan();

        copy.copyObjectFromContainer(this);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Recursively descends this span, generating a list of contained flow objects. Any parameter references are
     * expanded as they are found.
     *
     * @param objects the list to which to add accumulated flow objects
     */
    final void accumulateFlowObjects(final List<AbstractDocObjectTemplate> objects) {

        // This is called during "doLayout()" on objects that may contain DocSimpleSpans, after
        // doLayout() has been called on those objects

        for (final AbstractDocObjectTemplate child : getChildren()) {

            if (child instanceof final DocSimpleSpan innerSpan) {
                innerSpan.accumulateFlowObjects(objects);
            } else if (child instanceof final DocParameterReference paramRef) {
                final DocSimpleSpan contents = paramRef.getLaidOutContents();
                if (contents != null) {
                    contents.accumulateFlowObjects(objects);
                }
            } else {
                objects.add(child);
            }
        }
    }

    /**
     * Draw the image.
     *
     * @param grx the {@code Graphics} object to which to draw the image
     */
    @Override
    public final void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.paintComponent(grx, mathMode);
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
    public AbstractDocContainerInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        final List<AbstractDocObjectTemplate> children = getChildren();
        final List<AbstractDocObjectInst> childrenInstList = new ArrayList<>(children.size());

        for (final AbstractDocObjectTemplate child : children) {
            childrenInstList.add(child.createInstance(evalContext));
        }

        return new DocWrappingSpanInst(objStyle, null, childrenInstList);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.toXml(xml, 0);
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
     * @param context      the evaluation context
     */
    @Override
    public final void toLaTeX(final File dir, final int[] fileIndex,
                              final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                              final char[] mode, final EvalContext context) {

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>SimpleSpan");

        if (this.tag != null) {
            ps.print(" (");
            ps.print(this.tag);
            ps.print(')');
        }

        this.printTreeContents(ps);

        ps.print("</li>");
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
        } else if (obj instanceof final DocSimpleSpan spn) {
            equal = innerEquals(spn);
        } else {
            equal = false;
        }

        return equal;
    }
}
