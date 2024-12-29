package dev.mathops.assessment.formula;

import dev.mathops.assessment.document.template.AbstractDocSpanBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The base class for all entities that exist in a formula.
 */
abstract class AbstractFormulaContainer extends AbstractFormulaObject {

    /** Holders for children of this object, if any. */
    private final ArrayList<AbstractFormulaObject> children;

    /**
     * Construct a new {@code AbstractFormulaContainer}.
     */
    AbstractFormulaContainer() {

        super();

        this.children = new ArrayList<>(2);
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    @Override
    public abstract AbstractFormulaContainer deepCopy();

    /**
     * Get the number of children currently installed in this object.
     *
     * @return the number of children installed
     */
    public final int numChildren() {

        return this.children.size();
    }

    /**
     * Retrieve a specific child.
     *
     * @param index the 0-based index of the child to retrieve
     * @return the requested child
     */
    public final AbstractFormulaObject getChild(final int index) {

        return index < 0 || index >= this.children.size() ? null : this.children.get(index);
    }

    /**
     * Sets a specific child.
     *
     * @param index    the 0-based index of the child to set
     * @param newChild the new child
     */
    final void setChild(final int index, final AbstractFormulaObject newChild) {

        this.children.set(index, newChild);
    }

    /**
     * Add a child to the object.
     *
     * @param child the child to add
     */
    public final void addChild(final AbstractFormulaObject child) {

        if (child == null) {
            throw new IllegalArgumentException(Res.get(Res.NULL_CHILD_NOT_ALLOWED));
        }

        this.children.add(child);
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final int innerHashCode() {

        return Objects.hashCode(this.children);
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param other the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    final boolean innerEquals(final AbstractFormulaContainer other) {

        return Objects.equals(this.children, other.children);
    }

    /**
     * Appends an XML representation of the formula to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    public final void appendChildrenXml(final HtmlBuilder xml) {

        // TODO: Support indentation in formula XML

        for (final AbstractFormulaObject child : this.children) {
            child.appendXml(xml);
        }
    }

    /**
     * Appends a diagnostic representation of the formula.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param indent the indent level
     */
    final void printChildrenDiagnostics(final HtmlBuilder xml, final int indent) {

        for (final AbstractFormulaObject child : this.children) {
            child.printDiagnostics(xml, indent);
        }
    }

    /**
     * Gathers all spans that are contained in the formula.
     *
     * @param spans a list to which to add gathered spans
     */
    public final void accumulateSpans(final List<AbstractDocSpanBase> spans) {

        for (final AbstractFormulaObject child : this.children) {
            if (child instanceof final ConstSpanValue spanValue) {
                spans.add(spanValue.value);
            } else if (child instanceof final AbstractFormulaContainer inner) {
                inner.accumulateSpans(spans);
            }
        }
    }
}
