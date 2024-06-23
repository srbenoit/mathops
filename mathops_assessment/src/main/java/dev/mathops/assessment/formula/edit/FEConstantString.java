package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.ConstStringValue;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for a String constant .
 */
public final class FEConstantString extends AbstractFEObject {

    /** The string value. */
    private String value;

    /** The rendered boxes. */
    private final List<RenderedBox> rendered;

    /**
     * Constructs a new {@code FEConstantString}.
     *
     * @param theFontSize the font size for the component
     */
    private FEConstantString(final int theFontSize) {

        super(theFontSize);

        getAllowedTypes().add(EType.REAL);
        getPossibleTypes().add(EType.REAL);
        setCurrentType(EType.REAL);

        this.value = CoreConstants.EMPTY;
        this.rendered = new ArrayList<>(10);
    }

    /**
     * Constructs a new {@code FEConstantString}.
     *
     * @param theFontSize the font size for the component
     * @param theValue    the value
     */
    public FEConstantString(final int theFontSize, final String theValue) {

        this(theFontSize);

        setValue(theValue, false);
    }

    /**
     * Sets the constant value.
     *
     * @param theValue  the new constant value
     * @param storeUndo true to store an undo state; false to skip
     */
    private void setValue(final String theValue, final boolean storeUndo) {

        this.value = theValue == null ? CoreConstants.EMPTY : theValue;
        update(storeUndo);
    }

    /**
     * Gets the constant value.
     *
     * @return the constant value
     */
    public String getValue() {

        return this.value;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        return this.value.length();
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
     * Generates a {@code ConstStringValue} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public ConstStringValue generate() {

        return this.value == null ? null : new ConstStringValue(this.value);
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

        final String chStr = Character.toString(ch);
        Log.info("String constant container processing '", chStr, "'");

        final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();

        if ((int) ch >= 0x20 && (int) ch <= 0x7E) {
            final String pre = this.value.substring(0, cursorPos);
            final String post = this.value.substring(cursorPos);
            final String newValue = pre + ch + post;
            setValue(newValue, true);
        } else if ((int) ch == 0x08 && cursorPos > 0) {
            --fECursor.cursorPosition;
            if (this.value.length() == 1) {
                getParent().replaceChild(this, null);
            } else {
                final String firstPart = this.value.substring(0, cursorPos - 1);
                final String substring = this.value.substring(cursorPos);
                setValue(firstPart + substring, true);
            }
        } else if ((int) ch == 0x7f && cursorPos < getNumCursorSteps()) {
            // Delete
            if (this.value.length() == 1) {
                getParent().replaceChild(this, null);
            } else {
                final String firstPart = this.value.substring(0, cursorPos);
                final String lastPart = this.value.substring(cursorPos + 1);
                setValue(firstPart + lastPart, true);
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

        return "Cannot insert object into string value.";
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

        this.rendered.clear();

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        if (this.value.isEmpty()) {
            // Set our bounds to zero width, but a reasonable height/ascent for our font
            final float lineAscent = lineMetrics.getAscent();
            final float lineDescent = lineMetrics.getDescent();
            final int ascent = Math.round(lineAscent);
            final int descent = Math.round(lineDescent);

            setAdvance(0);
            getOrigin().setLocation(0, 0);
            getBounds().setBounds(0, -ascent, 0, ascent + descent);
        } else {
            int x = 0;
            int topY = 0;
            int botY = 0;

            for (final char ch : this.value.toCharArray()) {
                final String chStr = Character.toString(ch);
                final RenderedBox charBox = new RenderedBox(chStr);
                this.rendered.add(charBox);
                final int fontSize = getFontSize();
                charBox.setFontSize(fontSize);
                charBox.layout(g2d);
                charBox.getOrigin().setLocation(x, 0);
                x += charBox.getAdvance();

                final Rectangle charBounds = charBox.getBounds();
                topY = Math.min(topY, charBounds.y);
                botY = Math.max(botY, charBounds.y + charBounds.height);
            }

            final float[] lineBaselines = lineMetrics.getBaselineOffsets();
            final int center = Math.round(lineBaselines[Font.CENTER_BASELINE]);

            setAdvance(x);
            setCenterAscent(center);
            getOrigin().setLocation(0, 0);
            getBounds().setBounds(0, topY, x, botY - topY);
        }
    }

    /**
     * Moves this object and all subordinate objects. Used during layout.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    @Override
    public void translate(final int dx, final int dy) {

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

        for (final RenderedBox box : this.rendered) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            box.render(g2d, selected);
            ++pos;
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        target.addAll(this.rendered);
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
        final AbstractFEObject parent = getParent();
        builder.addln((parent == null ? "String*: '" : "String: '"), this.value, "' (", this.value, ")");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEConstantString duplicate() {

        final int fontSize = getFontSize();
        final FEConstantString dup = new FEConstantString(fontSize);

        dup.getAllowedTypes().clear();

        final EnumSet<EType> allowedTypes = getAllowedTypes();
                dup.getAllowedTypes().addAll(allowedTypes);

        final EType currentType = getCurrentType();
        dup.setCurrentType(currentType);

        if (this.value != null) {
            dup.setValue(this.value, false);
        }

        return dup;
    }
}
