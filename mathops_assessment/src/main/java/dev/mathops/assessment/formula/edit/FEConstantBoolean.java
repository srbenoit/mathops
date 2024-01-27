package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.ConstBooleanValue;
import dev.mathops.commons.builder.HtmlBuilder;

import java.awt.Graphics2D;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for a Boolean constant that can generate a {@code BooleanValue}.
 */
public final class FEConstantBoolean extends AbstractFEObject {

    /** Text to display for a TRUE value. */
    private static final String TRUE = "TRUE";

    /** Text to display for a FALSE value. */
    private static final String FALSE = "FALSE";

    /** The constant value. */
    private boolean value;

    /** The rendered box. */
    private RenderedBox rendered;

    /**
     * Constructs a new {@code FEConstantBoolean}.
     *
     * @param theFontSize the font size for the component
     * @param theValue    the initial value
     */
    public FEConstantBoolean(final int theFontSize, final boolean theValue) {

        super(theFontSize * 4 / 5);

        getAllowedTypes().add(EType.BOOLEAN);
        getPossibleTypes().add(EType.BOOLEAN);
        setCurrentType(EType.BOOLEAN);

        setValue(theValue, false);
    }

    /**
     * Sets the constant value.
     *
     * @param theValue  the new constant value
     * @param storeUndo true to store an undo state; false to skip
     */
    private void setValue(final boolean theValue, final boolean storeUndo) {

        this.value = theValue;
        update(storeUndo);
    }

    /**
     * Gets the constant value.
     *
     * @return the constant value
     */
    public boolean isValue() {

        return this.value;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        return 1;
    }

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if valid (a formula can be generated); false if not
     */
    @Override
    public boolean isValid() {

        return true;
    }

    /**
     * Attempts to replace one child with another. For example, replacing an integer constant with a real constant when
     * the user types a '.' character while entering a number.
     *
     * @param currentChild the current child
     * @param newChild     the new (replacement) child
     * @return true if the replacement was allowed (and performed); false if it is not allowed
     */
    @Override
    public boolean replaceChild(final AbstractFEObject currentChild,
                                final AbstractFEObject newChild) {

        return false;
    }

    /**
     * Recomputes the current type (does nothing for fixed-type constant values).
     */
    @Override
    public void recomputeCurrentType() {

        // No action
    }

    /**
     * Recomputes all cursor positions within the object.
     *
     * @param startPos the start position of this object
     */
    @Override
    public void recomputeCursorPositions(final int startPos) {

        setFirstCursorPosition(startPos);
    }

    /**
     * Generates a formula object.
     *
     * @return the formula object; {@code null} if this object is invalid
     */
    @Override
    public ConstBooleanValue generate() {

        return new ConstBooleanValue(this.value);
    }

    /**
     * Asks the object what modifications are valid for a specified cursor position or selection range.
     *
     * @param fECursor               cursor position information
     * @param allowedModifications a set that will be populated with the set of allowed modifications at the specified
     *                             position
     */
    @Override
    public void indicateValidModifications(final FECursor fECursor,
                                           final EnumSet<EModification> allowedModifications) {

        // There are no valid modifications, other than "delete" which is always allowed
    }

    /**
     * Processes a typed character.
     *
     * @param fECursor the cursor position and selection range
     * @param ch     the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();

        if (ch == 0x08 && cursorPos > 0) {
            // Backspace
            --fECursor.cursorPosition;
            getParent().replaceChild(this, null);
        } else if (ch == 0x7f && cursorPos < getNumCursorSteps()) {
            // Delete
            getParent().replaceChild(this, null);
        }
    }

    /**
     * Processes an insert.
     *
     * @param fECursor   the cursor position and selection range
     * @param toInsert the object to insert (never {@code null})
     * @return {@code null} on success; an error message on failure
     */
    @Override
    public String processInsert(final FECursor fECursor, final AbstractFEObject toInsert) {

        return "Cannot insert object into boolean value.";
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
    @Override
    public void layout(final Graphics2D g2d) {

        this.rendered = new RenderedBox(this.value ? TRUE : FALSE);
        this.rendered.useSans();
        this.rendered.setFontSize(getFontSize());
        this.rendered.layout(g2d);

        getBounds().setBounds(this.rendered.getBounds());
        getOrigin().setLocation(0, 0);
        setAdvance(this.rendered.getAdvance());
        setCenterAscent(this.rendered.getCenterAscent());
    }

    /**
     * Moves this object and all subordinate objects. Used during layout.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    @Override
    public void translate(final int dx, final int dy) {

        if (this.rendered != null) {
            this.rendered.translate(dx, dy);
        }
        getOrigin().translate(dx, dy);
    }

    /**
     * Renders the component. This renders all rendered boxes in this component or its descendants.
     *
     * @param g2d    the {@code Graphics2D} to which to render
     * @param cursor the cursor position and selection range
     */
    @Override
    public void render(final Graphics2D g2d, final FECursor cursor) {

        if (this.rendered != null) {
            final int firstPos = getFirstCursorPosition();
            final boolean selected = cursor.doesSelectionInclude(firstPos);
            this.rendered.render(g2d, selected);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.rendered != null) {
            target.add(this.rendered);
        }
    }

    /**
     * Emits a diagnostic representation of this object.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param indent  the indentation level
     */
    @Override
    public void emitDiagnostics(final HtmlBuilder builder, final int indent) {

        indent(builder, indent);
        builder.addln((getParent() == null ? "Boolean*: '" : "Boolean: '"),
                (this.value ? TRUE : FALSE), "' (", Boolean.toString(this.value), ")");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEConstantBoolean duplicate() {

        final FEConstantBoolean dup = new FEConstantBoolean(getFontSize(), this.value);

        dup.getAllowedTypes().clear();
        dup.getAllowedTypes().addAll(getAllowedTypes());
        dup.setCurrentType(getCurrentType());

        return dup;
    }
}
