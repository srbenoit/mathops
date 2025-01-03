package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.ConstSpanValue;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for a Span constant that can generate a {@code SpanValue}.
 */
public final class FEConstantSpan extends AbstractFEObject {

    /** The current text value (can be empty, never {@code null}). */
    private String text;

    /** The constant value as a {@code DocSimpleSpan}. */
    private DocSimpleSpan value = null;

    /** The opening quote box. */
    private RenderedBox openQuote = null;

    /** The closing quote box. */
    private RenderedBox closeQuote = null;

    /** The rendered boxes. */
    private final List<RenderedBox> rendered;

    /**
     * Constructs a new {@code FEConstantSpan}.
     *
     * @param theFontSize the font size for the component
     */
    public FEConstantSpan(final int theFontSize) {

        super(theFontSize);

        getAllowedTypes().add(EType.SPAN);
        setCurrentType(EType.SPAN);

        this.text = CoreConstants.EMPTY;
        this.rendered = new ArrayList<>(10);
    }

    /**
     * Constructs a new {@code FEConstantSpan}.
     *
     * @param theFontSize the font size for the component
     * @param span        the span value
     */
    public FEConstantSpan(final int theFontSize, final DocSimpleSpan span) {

        this(theFontSize);

        this.value = span;
        this.text = span.toXml(0);
    }

    /**
     * Sets the text value. If this can be parsed as a valid span, this also sets the constant value.
     *
     * @param theText   the new text value
     * @param storeUndo true to store an undo state; false to skip
     */
    private void setText(final String theText, final boolean storeUndo) {

        this.text = theText == null ? CoreConstants.EMPTY : theText;

        this.value = null;

        try {
            final String wrapped = "<X>" + this.text + "</X>";
            final XmlContent content = new XmlContent(wrapped, false, false);
            final IElement elem = content.getTopLevel();
            final EvalContext ctx = new EvalContext();
            if (elem instanceof final NonemptyElement nonempty) {
                this.value = DocFactory.parseSpan(ctx, nonempty, EParserMode.NORMAL);
            }
        } catch (final ParsingException ex) {
            // No action
        }

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
     * Gets the constant {@code DocSimpleSpan} value.
     *
     * @return the constant value
     */
    public DocSimpleSpan getValue() {

        return this.value;
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

        return this.value != null;
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
    public boolean replaceChild(final AbstractFEObject currentChild, final AbstractFEObject newChild) {

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
    public ConstSpanValue generate() {

        return this.value == null ? null : new ConstSpanValue(this.value);
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

        // Log.info("Span constant container processing '", Character.toString(ch), "'");

        final int innerPos = fECursor.cursorPosition - getFirstCursorPosition() - 1;

        if ((int) ch >= 0x20 && (int) ch <= 0x7E) {
            ++fECursor.cursorPosition;
            final String firstPart = this.text.substring(0, innerPos);
            final String lastPart = this.text.substring(innerPos);
            setText(firstPart + ch + lastPart, true);
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
                final String firstPart = this.text.substring(0, innerPos - 1);
                final String lastPart = this.text.substring(innerPos);
                setText(firstPart + lastPart, true);
            }
        } else if ((int) ch == 0x7f) {
            // Delete
            if (this.text.isEmpty()) {
                if (innerPos >= 0) {
                    // Careful no to decrement if cursor was before first quote
                    --fECursor.cursorPosition;
                }
                getParent().replaceChild(this, null);
            } else if (innerPos >= 0 && innerPos < this.text.length()) {
                final String firstPart = this.text.substring(0, innerPos);
                final String lastPart = this.text.substring(innerPos + 1);
                setText(firstPart + lastPart, true);
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

        return "Cannot insert object into span value.";
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

        final int fontSize = getFontSize();

        this.openQuote = new RenderedBox("\"");
        this.openQuote.setFontSize(fontSize);
        this.openQuote.layout(g2d);

        this.closeQuote = new RenderedBox("\"");
        this.closeQuote.setFontSize(fontSize);
        this.closeQuote.layout(g2d);

        this.rendered.clear();

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.openQuote.getAdvance();
        int topY = 0;
        int botY = 0;

        final Rectangle openQuoteBounds = this.openQuote.getBounds();
        topY = Math.min(topY, openQuoteBounds.y);
        botY = Math.max(botY, openQuoteBounds.y + openQuoteBounds.height);

        for (final char ch : this.text.toCharArray()) {
            final String chStr = Character.toString(ch);
            final RenderedBox charBox = new RenderedBox(chStr);
            this.rendered.add(charBox);
            charBox.setFontSize(fontSize);
            charBox.layout(g2d);
            charBox.getOrigin().setLocation(x, 0);
            x += charBox.getAdvance();

            final Rectangle charBounds = charBox.getBounds();
            topY = Math.min(topY, charBounds.y);
            botY = Math.max(botY, charBounds.y + charBounds.height);
        }

        this.closeQuote.getOrigin().setLocation(x, 0);

        final Rectangle closeQuoteBounds = this.closeQuote.getBounds();
        topY = Math.min(topY, closeQuoteBounds.y);
        botY = Math.max(botY, closeQuoteBounds.y + closeQuoteBounds.height);

        x += this.closeQuote.getAdvance();

        final float[] lineBaselines = lineMetrics.getBaselineOffsets();
        final int center = Math.round(lineBaselines[Font.CENTER_BASELINE]);

        setAdvance(x);
        setCenterAscent(center);
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

        if (this.openQuote != null) {
            this.openQuote.translate(dx, dy);
        }
        if (this.closeQuote != null) {
            this.closeQuote.translate(dx, dy);
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

        if (this.openQuote != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.openQuote.render(g2d, selected);
            ++pos;
        }

        for (final RenderedBox box : this.rendered) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            box.render(g2d, selected);
            ++pos;
        }

        if (this.closeQuote != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.closeQuote.render(g2d, selected);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.openQuote != null) {
            target.add(this.openQuote);
        }

        target.addAll(this.rendered);

        if (this.closeQuote != null) {
            target.add(this.closeQuote);
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

        final AbstractFEObject parent = getParent();
        builder.addln((parent == null ? "Span*: '" : "Span: '"), this.text, "'");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEConstantSpan duplicate() {

        final int fontSize = getFontSize();
        final FEConstantSpan dup = new FEConstantSpan(fontSize);

        dup.getAllowedTypes().clear();

        final EnumSet<EType> allowedTypes = getAllowedTypes();
        dup.getAllowedTypes().addAll(allowedTypes);

        final EType currentType = getCurrentType();
        dup.setCurrentType(currentType);

        if (this.text != null) {
            dup.setText(this.text, false);
        }

        return dup;
    }
}
