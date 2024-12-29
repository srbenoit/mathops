package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocWrappingSpanInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapping span is a collection of document objects which flow naturally in a left-to-right, top-down ordering. This
 * type of span can be embedded in a paragraph, or in another wrapping span, but cannot be contained in any other
 * object.
 */
public final class DocWrappingSpan extends DocSimpleSpan {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6270125082117577790L;

    /**
     * Construct a new {@code DocWrappingSpan}.
     */
    public DocWrappingSpan() {

        super();
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocWrappingSpan deepCopy() {

        final DocWrappingSpan copy = new DocWrappingSpan();

        copy.copyObjectFromContainer(this);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object
     */
    @Override
    public DocWrappingSpanInst createInstance(final EvalContext evalContext) {

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

        xml.add("<span");
        printFormat(xml, 1.0f);
        xml.add('>');

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.toXml(xml, 0);
        }

        xml.add("</span>");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>WrappingSpan");

        if (this.tag != null) {
            ps.print(" (");
            ps.print(this.tag);
            ps.print(')');
        }

        printTreeContents(ps);

        ps.print("</li>");
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return super.hashCode();
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
        } else if (obj instanceof final DocWrappingSpan span) {
            equal = innerEquals(span);
        } else {
            equal = false;
        }

        return equal;
    }
}
