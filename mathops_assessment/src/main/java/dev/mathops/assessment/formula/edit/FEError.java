package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for an Error constant that can generate a {@code ErrorValue}.
 */
public final class FEError extends AbstractFEObject {

    /** The current text value (may be empty, never {@code null}). */
    private String text;

    /** The opening quote box. */
    private RenderedBox openStar;

    /** The closing quote box. */
    private RenderedBox closeStar;

    /** The rendered boxes. */
    private final List<RenderedBox> rendered;

    /**
     * Constructs a new {@code FEError}.
     *
     * @param theFontSize the font size for the component
     */
    public FEError(final int theFontSize) {

        super(theFontSize);

        getAllowedTypes().add(EType.ERROR);
        getPossibleTypes().add(EType.ERROR);
        setCurrentType(EType.ERROR);

        this.text = CoreConstants.EMPTY;
        this.rendered = new ArrayList<>(10);
    }

    /**
     * Constructs a new {@code FEError}.
     *
     * @param theFontSize the font size for the component
     * @param theText     the error text
     */
    public FEError(final int theFontSize, final String theText) {

        this(theFontSize);

        this.text = theText == null ? CoreConstants.EMPTY : theText;
    }

    /**
     * Sets the text value. If this can be parsed as a valid span, this also sets the constant value.
     *
     * @param theText   the new text value
     * @param storeUndo true to store an undo state; false to skip
     */
    private void setText(final String theText, final boolean storeUndo) {

        this.text = theText == null ? CoreConstants.EMPTY : theText;

        update(storeUndo);
    }

    /**
     * Gets the current text.
     *
     * @return the current text
     */
    public String getText() {

        return this.text;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        return this.text.length() + 2;
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
     * Generates a {@code SpanValue} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public ErrorValue generate() {

        return new ErrorValue(this.text);
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

        allowedModifications.add(EModification.TYPE);
    }

    /**
     * Processes a typed character.
     *
     * @param fECursor the cursor position and selection range
     * @param ch     the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        // Log.info("Error processing '", Character.toString(ch), "'");

        final int innerPos = fECursor.cursorPosition - getFirstCursorPosition() - 1;

        if (ch >= 0x20 && ch <= 0x7E) {
            ++fECursor.cursorPosition;
            setText(this.text.substring(0, innerPos) + ch
                    + this.text.substring(innerPos), true);
        } else if (ch == 0x08) {
            // Backspace
            if (this.text.isEmpty()) {
                if (innerPos > 0) {
                    // Cursor was after second quote
                    --fECursor.cursorPosition;
                }
                --fECursor.cursorPosition;
                getParent().replaceChild(this, null);
            } else if (innerPos > 0 && innerPos <= this.text.length()) {
                --fECursor.cursorPosition;
                setText(this.text.substring(0, innerPos - 1) + this.text.substring(innerPos), true);
            }
        } else if (ch == 0x7f) {
            // Delete
            if (this.text.isEmpty()) {
                if (innerPos >= 0) {
                    // Careful no to decrement if cursor was before first quote
                    --fECursor.cursorPosition;
                }
                getParent().replaceChild(this, null);
            } else if (innerPos >= 0 && innerPos < this.text.length()) {
                setText(this.text.substring(0, innerPos) + this.text.substring(innerPos + 1), true);
            }
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

        return "Cannot insert object into error value.";
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

        this.openStar = new RenderedBox("*");
        this.openStar.setFontSize(getFontSize());
        this.openStar.layout(g2d);

        this.closeStar = new RenderedBox("*");
        this.closeStar.setFontSize(getFontSize());
        this.closeStar.layout(g2d);

        this.rendered.clear();

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.openStar.getAdvance();
        int topY = 0;
        int botY = 0;

        final Rectangle openQuoteBounds = this.openStar.getBounds();
        topY = Math.min(topY, openQuoteBounds.y);
        botY = Math.max(botY, openQuoteBounds.y + openQuoteBounds.height);

        for (final char ch : this.text.toCharArray()) {
            final RenderedBox charBox = new RenderedBox(Character.toString(ch));
            this.rendered.add(charBox);
            charBox.setFontSize(getFontSize());
            charBox.layout(g2d);
            charBox.getOrigin().setLocation(x, 0);
            x += charBox.getAdvance();

            final Rectangle charBounds = charBox.getBounds();
            topY = Math.min(topY, charBounds.y);
            botY = Math.max(botY, charBounds.y + charBounds.height);
        }

        this.closeStar.getOrigin().setLocation(x, 0);

        final Rectangle closeQuoteBounds = this.closeStar.getBounds();
        topY = Math.min(topY, closeQuoteBounds.y);
        botY = Math.max(botY, closeQuoteBounds.y + closeQuoteBounds.height);

        x += this.closeStar.getAdvance();

        setAdvance(x);
        setCenterAscent(Math.round(lineMetrics.getBaselineOffsets()[Font.CENTER_BASELINE]));
        getOrigin().setLocation(0, 0);
        getBounds().setBounds(0, topY, x, botY - topY);
    }

    /**
     * Moves this object and all subordinate objects. Used during layout.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    @Override
    public void translate(final int dx, final int dy) {

        if (this.openStar != null) {
            this.openStar.translate(dx, dy);
        }
        if (this.closeStar != null) {
            this.closeStar.translate(dx, dy);
        }

        for (final RenderedBox box : this.rendered) {
            box.translate(dx, dy);
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

        int pos = getFirstCursorPosition();

        if (this.openStar != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.openStar.render(g2d, selected);
        }

        for (final RenderedBox box : this.rendered) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            box.render(g2d, selected);
            ++pos;
        }

        if (this.closeStar != null) {
            final boolean selected = cursor.doesSelectionInclude(pos + this.text.length());
            this.closeStar.render(g2d, selected);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.openStar != null) {
            target.add(this.openStar);
        }

        target.addAll(this.rendered);

        if (this.closeStar != null) {
            target.add(this.closeStar);
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
        builder.addln((getParent() == null ? "Error*: '" : "Error: '"), this.text, "'");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEError duplicate() {

        final FEError dup = new FEError(getFontSize());

        dup.getAllowedTypes().clear();
        dup.getAllowedTypes().addAll(getAllowedTypes());
        dup.setCurrentType(getCurrentType());

        if (this.text != null) {
            dup.setText(this.text, false);
        }

        return dup;
    }
}
