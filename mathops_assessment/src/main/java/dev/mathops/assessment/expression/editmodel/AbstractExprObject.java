package dev.mathops.assessment.expression.editmodel;

/**
 * The base class for expression objects.
 */
public abstract class AbstractExprObject {

    /** The parent object ({@code null} only for the root node). */
    private final AbstractExprObject parent;

    /**
     * Constructs a new {@code AbstractExprObject}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    AbstractExprObject(final AbstractExprObject theParent) {

        this.parent = theParent;
    }

    /**
     * Gets the parent node.
     * @return the parent ({@code null} only for the root node)
     */
    public final AbstractExprObject getParent() {

        return this.parent;
    }
}
