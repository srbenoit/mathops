package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * An expression (or sub-expression) modeled as a sequence of expression objects.
 */
public final class Expr extends ExprObjectBranch {

    /** The list of objects in the sequence. */
    private final List<ExprObject> children;

    /**
     * Constructs a new {@code Expr}.
     */
    public Expr() {

        super();

        this.children = new ArrayList<>(10);
    }

    /**
     * Gets the number of child objects in this expression.
     *
     * @return the number of child objects
     */
    public int size() {

        return this.children.size();
    }

    /**
     * Gets the child object at a specified index.
     *
     * @param index the index
     * @return the child object
     */
    public ExprObject get(final int index) {

        return this.children.get(index);
    }

    /**
     * Insets an object into this expression at a specified index.
     *
     * @param index the index at which to insert the object
     * @param object the object to insert
     */
    public void insert(final int index, final ExprObject object) {

        this.children.add(index, object);
    }

    /**
     * Removes the object at a specified index in this expression.
     *
     * @param index the index at which to insert the object
     */
    public void remove(final int index) {

        this.children.remove(index);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final int count = this.children.size();

        final HtmlBuilder htm = new HtmlBuilder(20 + count * 20);

        htm.add("Expr{children=[");

        if (count > 0) {
            final ExprObject child0 = this.children.getFirst();
            final String child0Str = child0.toString();
            htm.add(child0Str);

            for (int i = 1; i < count; ++i) {
                htm.add(CoreConstants.COMMA);
                final ExprObject child = this.children.get(i);
                final String childStr = child.toString();
                htm.add(childStr);
            }
        }

        htm.add("]}");

        return htm.toString();
    }
}
