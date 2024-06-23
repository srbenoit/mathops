package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.commons.builder.HtmlBuilder;

import java.awt.Graphics2D;
import java.util.EnumSet;
import java.util.List;

/**
 * The base class for objects that make up a formula edit tree.
 */
public abstract class AbstractFEObject extends AbstractFEDrawable {

    /** The parent object. */
    private AbstractFEObject parent = null;

    /** The allowed type of value this object can generate. */
    private final EnumSet<EType> allowedTypes;

    /**
     * The types this object could possibly generate. This should always be a subset of {@code allowedTypes}, and
     * {@code currentType} should always be a member of this set.
     */
    private final EnumSet<EType> possibleTypes;

    /** The type of value this object generates. */
    private EType currentType = null;

    /** The cursor position just before the first cursor step in this object. */
    private int firstCursorPosition = 0;

    /**
     * Constructs a new {@code AbstractFEObject}.
     *
     * @param theFontSize the font size for the component
     */
    AbstractFEObject(final int theFontSize) {

        super();

        this.allowedTypes = EnumSet.noneOf(EType.class);
        this.possibleTypes = EnumSet.noneOf(EType.class);

        setFontSize(theFontSize);
    }

    /**
     * Sets the parent object.
     *
     * @param theParent the new parent object
     */
    final void setParent(final AbstractFEObject theParent) {

        this.parent = theParent;
    }

    /**
     * Gets the parent object.
     *
     * @return the parent object
     */
    final AbstractFEObject getParent() {

        return this.parent;
    }

    /**
     * Gets the set of types this value is allowed to generate.
     *
     * @return the set of allowed types
     */
    public final EnumSet<EType> getAllowedTypes() {

        return this.allowedTypes;
    }

    /**
     * Gets the set of types this value could possibly generate.
     *
     * @return the set of possible types
     */
    public final EnumSet<EType> getPossibleTypes() {

        return this.possibleTypes;
    }

    /**
     * Sets the type of value this object currently produces.
     *
     * @param theCurrentType the new current type
     */
    final void setCurrentType(final EType theCurrentType) {

        this.currentType = theCurrentType;
    }

    /**
     * Gets the type of value this object currently produces.
     *
     * @return the current type
     */
    public final EType getCurrentType() {

        return this.currentType;
    }

    /**
     * Sets the cursor position just before the first cursor step in this object.
     *
     * @param theFirstCursorPos the new first cursor position
     */
    final void setFirstCursorPosition(final int theFirstCursorPos) {

        this.firstCursorPosition = theFirstCursorPos;
    }

    /**
     * Gets the cursor position just before the first cursor step in this object.
     *
     * @return the first cursor position
     */
    final int getFirstCursorPosition() {

        return this.firstCursorPosition;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants.
     *
     * @return the number of cursor steps
     */
    protected abstract int getNumCursorSteps();

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if valid (a formula can be generated); false if not
     */
    protected abstract boolean isValid();

    /**
     * Attempts to replace one child with another. For example, replacing an integer constant with a real constant when
     * the user types a '.' character while entering a number.
     *
     * @param currentChild the current child
     * @param newChild     the new (replacement) child
     * @return true if the replacement was allowed (and performed); false if it is not allowed
     */
    protected abstract boolean replaceChild(AbstractFEObject currentChild, AbstractFEObject newChild);

    /** Recomputes the current type based on arguments. */
    protected abstract void recomputeCurrentType();

    /**
     * Recomputes all cursor positions within the object.
     *
     * @param startPos the start position of this object
     */
    protected abstract void recomputeCursorPositions(int startPos);

    /**
     * Generates a formula object.
     *
     * @return the formula object; {@code null} if this object is invalid
     */
    protected abstract AbstractFormulaObject generate();

    /**
     * Asks the object what modifications are valid for a specified cursor position or selection range.
     *
     * @param fECursor             cursor position information
     * @param allowedModifications a set that will be populated with the set of allowed modifications at the specified
     *                             position
     */
    protected abstract void indicateValidModifications(FECursor fECursor,
                                                       EnumSet<EModification> allowedModifications);

    /**
     * Processes a typed character.
     *
     * @param fECursor the cursor position and selection range
     * @param ch       the character typed
     */
    protected abstract void processChar(FECursor fECursor, char ch);

    /**
     * Processes an insert.
     *
     * @param fECursor the cursor position and selection range
     * @param toInsert the object to insert (never {@code null})
     * @return {@code null} on success; an error message on failure
     */
    protected abstract String processInsert(FECursor fECursor, AbstractFEObject toInsert);

    /**
     * Called on an object whenever any change is made. On child objects, this simply calls the same method on the
     * parent to propagate the notification to the root object. On the root object, this should perform a global update,
     * which performs layout of all objects in the tree and recomputes all cursor positions.
     *
     * <p>
     * This default implementation simply calls the parent {@code update} method. The root object class should override
     * to perform actual update operations.
     *
     * @param storeUndo true to store an undo state; false to skip
     */
    void update(final boolean storeUndo) {

        if (this.parent != null) {
            this.parent.update(storeUndo);
        }
    }

    /**
     * Performs layout when something changes. This method clears and re-generates the sequence of rendered boxes that
     * this component represents. This is part of the processing when the root object receives an "update"
     * notification.
     *
     * <p>
     * This method should lay out all child objects and rendered boxes relative to its own origin, but this method does
     * not
     *
     * @param g2d the {@code Graphics2D} from which a font render context can be obtained
     */
    protected abstract void layout(final Graphics2D g2d);

    /**
     * Moves this object and all subordinate objects. Used during layout.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    protected abstract void translate(int dx, int dy);

    /**
     * Renders the component. This renders all rendered boxes in this component or its descendants.
     *
     * @param g2d    the {@code Graphics2D} to which to render
     * @param cursor the cursor position and selection range
     */
    protected abstract void render(Graphics2D g2d, FECursor cursor);

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    protected abstract void gatherRenderedBoxes(List<? super RenderedBox> target);

    /**
     * Adds indentation spaces to a {@code HtmlBuilder}.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param indent  the indentation level
     */
    static void indent(final HtmlBuilder builder, final int indent) {

        for (int i = 0; i < indent; ++i) {
            builder.add("  ");
        }
    }

    /**
     * Emits a diagnostic representation of this object.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param indent  the indentation level
     */
    protected abstract void emitDiagnostics(HtmlBuilder builder, int indent);

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    protected abstract AbstractFEObject duplicate();
}
