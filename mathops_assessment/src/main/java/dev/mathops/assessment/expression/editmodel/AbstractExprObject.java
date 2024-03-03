package dev.mathops.assessment.expression.editmodel;

/**
 * The base class for expression objects.
 */
public abstract class AbstractExprObject {

    /** The parent object ({@code null} only for the root node). */
    private final AbstractExprObject parent;

    /** The index of the first cursor position that belongs to this object. */
    private int firstCursorPosition = 0;

    /** The total number of cursor positions in the object. */
    private int numCursorPositions = 0;

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

    /**
     * Sets the first cursor position that is considered part of this object.
     *
     * @param theFirstCursorPosition the new first cursor position
     */
    final void innerSetFirstCursorPosition(final int theFirstCursorPosition) {

        this.firstCursorPosition = theFirstCursorPosition;
    }

    /**
     * Gets the first cursor position that is considered part of this object.
     *
     * @return the first cursor position
     */
    final int getFirstCursorPosition() {

        return this.firstCursorPosition;
    }

    /**
     * Sets the number of cursor positions this object occupies.
     *
     * @param theNumCursorPositions the number of cursor positions
     */
    final void innerSetNumCursorPositions(final int theNumCursorPositions) {

        this.numCursorPositions = theNumCursorPositions;
    }

    /**
     * Gets the number of cursor positions this object occupies.
     *
     * @return the number of cursor positions
     */
    final int getNumCursorPositions() {

        return this.numCursorPositions;
    }
}
