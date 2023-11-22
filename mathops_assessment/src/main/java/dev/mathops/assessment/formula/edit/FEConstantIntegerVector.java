package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.ConstIntegerVector;
import dev.mathops.assessment.formula.EBinaryOp;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.IntegerVectorValue;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for an array of constant {@code Long} values that can generate an {@code IntegerVectorValue}.
 */
public final class FEConstantIntegerVector extends AbstractFEObject {

    /** The constant value. */
    private IntegerVectorValue value;

    /**
     * The text representation of the value (comma-separated list of values, each consisting only of digits or PI or E
     * constants).
     */
    private String text;

    /** The rendered boxes. */
    private final List<RenderedBox> rendered;

    /**
     * Constructs a new {@code FEConstantIntegerVector}.
     *
     * @param theFontSize the font size for the component
     */
    private FEConstantIntegerVector(final int theFontSize) {

        super(theFontSize);

        getAllowedTypes().add(EType.REAL_VECTOR);
        getPossibleTypes().add(EType.REAL_VECTOR);
        setCurrentType(EType.REAL_VECTOR);

        this.text = CoreConstants.EMPTY;
        this.rendered = new ArrayList<>(10);
    }

    /**
     * Constructs a new {@code FEConstantIntegerVector}.
     *
     * @param theFontSize the font size for the component
     * @param theValue    the value
     */
    public FEConstantIntegerVector(final int theFontSize, final IntegerVectorValue theValue) {

        this(theFontSize);

        setValue(theValue, false);
    }

    /**
     * Sets the constant value.
     *
     * @param theValue  the new constant value
     * @param storeUndo true to store an undo state; false to skip
     */
    private void setValue(final IntegerVectorValue theValue, final boolean storeUndo) {

        this.value = theValue;
        this.text = theValue == null ? "null" : theValue.toString();
        update(storeUndo);
    }

    /**
     * Sets the text value. If this can be parsed as a valid integer this also sets the constant integer value.
     *
     * @param theText   the new text value
     * @param storeUndo true to store an undo state; false to skip
     */
    private void setText(final String theText, final boolean storeUndo) {

        this.text = theText == null ? CoreConstants.EMPTY : theText;
        this.value = null;

        final String[] split = this.text.split(",");
        final int len = split.length;
        final long[] elements = new long[len];
        boolean ok = true;
        for (int i = 0; i < len; ++i) {
            try {
                elements[i] = Long.parseLong(this.text);
            } catch (final NumberFormatException ex) {
                ok = false;
                break;
            }
        }

        if (ok) {
            this.value = new IntegerVectorValue(elements);
        }

        update(storeUndo);
    }

    /**
     * Gets the constant value.
     *
     * @return the constant value
     */
    public IntegerVectorValue getValue() {

        return this.value;
    }

    /**
     * Gets the text representation of the value.
     *
     * @return the text representation
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

        return this.text.length();
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
     * Generates a {@code ConstIntegerVector} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public ConstIntegerVector generate() {

        return this.value == null ? null : new ConstIntegerVector(this.value);
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

        Log.info("Integer vector constant container processing '", Character.toString(ch), "'");

        final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();

        if (ch == '+' || ch == '-') {
            ++fECursor.cursorPosition;
            if (cursorPos == 0) {
                final FEUnaryOper unary = new FEUnaryOper(getFontSize(), EUnaryOp.forOp(ch));
                getParent().replaceChild(this, unary);
                unary.setArg1(this, true);
            } else if (cursorPos == this.text.length()) {
                final FEBinaryOper binary = new FEBinaryOper(getFontSize(), EBinaryOp.forOp(ch));
                getParent().replaceChild(this, binary);
                binary.setArg1(this, true);
            } else {
                final String pre = this.text.substring(0, cursorPos);
                final String post = this.text.substring(cursorPos);
                final FEConstantIntegerVector preReal = new FEConstantIntegerVector(getFontSize());
                preReal.setText(pre, true);
                final FEConstantIntegerVector postReal = new FEConstantIntegerVector(getFontSize());
                postReal.setText(post, true);
                final FEBinaryOper binary = new FEBinaryOper(getFontSize(), EBinaryOp.forOp(ch));
                getParent().replaceChild(this, binary);
                binary.setArg1(preReal, false);
                binary.setArg2(postReal, true);
            }
        } else if ((ch >= '0' && ch <= '9') || ch == '.') {
            ++fECursor.cursorPosition;
            setText(this.text.substring(0, cursorPos) + ch
                    + this.text.substring(cursorPos), true);
        } else if (ch == 0x08 && cursorPos > 0) {
            --fECursor.cursorPosition;
            if (this.text.length() == 1) {
                getParent().replaceChild(this, null);
            } else {
                setText(this.text.substring(0, cursorPos - 1) + this.text.substring(cursorPos),
                        true);
            }
        } else if (ch == 0x7f && cursorPos < getNumCursorSteps()) {
            // Delete
            if (this.text.length() == 1) {
                getParent().replaceChild(this, null);
            } else {
                setText(this.text.substring(0, cursorPos) + this.text.substring(cursorPos + 1),
                        true);
            }
        }

        // TODO: Allow "times 10 to the ___" symbol

        // TODO: support commas
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

        return "Cannot insert object into integer vector value.";
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

        if (this.text.isEmpty()) {
            // Set our bounds to zero width, but a reasonable height/ascent for our font
            final int ascent = Math.round(lineMetrics.getAscent());
            final int descent = Math.round(lineMetrics.getDescent());

            setAdvance(0);
            getOrigin().setLocation(0, 0);
            getBounds().setBounds(0, -ascent, 0, ascent + descent);
        } else {
            int x = 0;
            int topY = 0;
            int botY = 0;

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

            setAdvance(x);
            setCenterAscent(Math.round(lineMetrics.getBaselineOffsets()[Font.CENTER_BASELINE]));
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
        builder.addln((getParent() == null ? "Integer Vector*: '" : "Integer Vector: '"), this.text,
                "' (", this.value, ")");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEConstantIntegerVector duplicate() {

        final FEConstantIntegerVector dup = new FEConstantIntegerVector(getFontSize());

        dup.getAllowedTypes().clear();
        dup.getAllowedTypes().addAll(getAllowedTypes());
        dup.setCurrentType(getCurrentType());

        if (this.text != null) {
            dup.setText(this.text, false);
        }

        return dup;
    }
}
