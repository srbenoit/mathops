package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.core.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The base class for document object iterations that may contain child objects.
 */
public abstract class AbstractDocContainerInst extends AbstractDocObjectInst {

    /** The set of contained children components. */
    private final List<AbstractDocObjectInst> children;

    /**
     * Construct a new {@code AbstractDocContainerInst}.
     *
     * @param theStyle    the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theChildren the list of child objects
     */
    AbstractDocContainerInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                             final List<? extends AbstractDocObjectInst> theChildren) {

        super(theStyle, theBgColorName);

        this.children = new ArrayList<>(theChildren);
    }

    /**
     * Get the number of children this object contains.
     *
     * @return the number of children
     */
    public final int numChildren() {

        return this.children.size();
    }

    /**
     * Get an unmodifiable view of the list of children.
     *
     * @return the list of children
     */
    public final List<AbstractDocObjectInst> getChildren() {

        return Collections.unmodifiableList(this.children);
    }

    /**
     * Adds the XML representations for all children to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent the number of spaces to indent each child
     */
    final void appendChildrenXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        for (final AbstractDocObjectInst child : getChildren()) {
            child.toXml(xml, xmlStyle, indent);
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    final int docContainerInstHashCode() {

        return docObjectInstHashCode() + Objects.hashCode(this.children);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    final boolean checkDocContainerInstEquals(final AbstractDocContainerInst obj) {

        return checkDocObjectInstEquals(obj) && Objects.equals(this.children, obj.children);
    }
}
