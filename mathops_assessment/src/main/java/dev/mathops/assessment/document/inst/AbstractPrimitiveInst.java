package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.core.builder.HtmlBuilder;

/**
 * An instance of a primitive used in a drawing or graph.
 */
public abstract class AbstractPrimitiveInst {

    /**
     * Construct a new {@code AbstractPrimitiveInst}.
     */
    AbstractPrimitiveInst() {

        // No action
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent the number of spaces to indent the printout
     */
    public abstract void toXml(HtmlBuilder xml, EXmlStyle xmlStyle, int indent);

    /**
     * Create a string for a particular indentation level.
     *
     * @param indent The number of spaces to indent.
     * @return A string with the requested number of spaces.
     */
    protected static String makeIndent(final int indent) {

        final HtmlBuilder builder = new HtmlBuilder(indent);

        for (int i = 0; i < indent; ++i) {
            builder.add(' ');
        }

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public abstract int hashCode();

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public abstract boolean equals(Object obj);
}
