package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.ConstRealVector;
import dev.mathops.assessment.formula.EBinaryOp;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.RealVectorValue;
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
 * A container for an array of constant {@code Double} values that can generate a {@code RealVectorValue}.
 */
public final class FEConstantRealVector extends AbstractFEObject {

    /** The constant value. */
    private RealVectorValue value = null;

    /**
     * The text representation of the value (comma-separated list of values, each consisting only of digits or PI or E
     * constants).
     */
    private String text;

    /** The rendered boxes. */
    private final List<RenderedBox> rendered;

    /**
     * Constructs a new {@code FEConstantRealVector}.
     *
     * @param theFontSize the font size for the component
     */
    private FEConstantRealVector(final int theFontSize) {

        super(theFontSize);

        getAllowedTypes().add(EType.REAL_VECTOR);
        getPossibleTypes().add(EType.REAL_VECTOR);
        setCurrentType(EType.REAL_VECTOR);

        this.text = CoreConstants.EMPTY;
        this.rendered = new ArrayList<>(10);
    }

    /**
     * Constructs a new {@code FEConstantRealVector}.
     *
     * @param theFontSize the font size for the component
     * @param theValue    the value
     */
    public FEConstantRealVector(final int theFontSize, final RealVectorValue theValue) {

        this(theFontSize);

        setValue(theValue, false);
    }

    /**
     * Sets the constant value.
     *
     * @param theValue  the new constant value
     * @param storeUndo true to store an undo state; false to skip
     */
    private void setValue(final RealVectorValue theValue, final boolean storeUndo) {

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
        final double[] elements = new double[len];
        boolean ok = true;
        for (int i = 0; i < len; ++i) {
            try {
                if (FEConstantReal.PI_STRING.equals(split[i])) {
                    elements[i] = Math.PI;
                } else if (FEConstantReal.E_STRING.equals(this.text)) {
                    elements[i] = Math.E;
                } else {
                    elements[i] = Double.parseDouble(this.text);
                }
            } catch (final NumberFormatException ex) {
                ok = false;
                break;
            }
        }

        if (ok) {
            this.value = new RealVectorValue(elements);
        }

        update(storeUndo);
    }

    /**
     * Gets the constant value.
     *
     * @return the constant value
     */
    public RealVectorValue getValue() {

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
     * Generates a {@code ConstRealVector} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public ConstRealVector generate() {

        return this.value == null ? null : new ConstRealVector(this.value);
    }

    /**
     * Asks the object what modifications are valid for a specified cursor position or selection range.
     *
     * @param fECursor             cursor position information
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
     * @param ch       the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        final String chStr = Character.toString(ch);
        Log.info("Real vector constant container processing '", chStr, "'");

        final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();

        if ((int) ch == '+' || (int) ch == '-') {
            final int fontSize = getFontSize();

            ++fECursor.cursorPosition;
            if (cursorPos == 0) {
                final EUnaryOp theUnary = EUnaryOp.forOp(ch);
                final FEUnaryOper unary = new FEUnaryOper(fontSize, theUnary);
                getParent().replaceChild(this, unary);
                unary.setArg1(this, true);
            } else if (cursorPos == this.text.length()) {
                final EBinaryOp theBinary = EBinaryOp.forOp(ch);
                final FEBinaryOper binary = new FEBinaryOper(fontSize, theBinary);
                getParent().replaceChild(this, binary);
                binary.setArg1(this, true);
            } else {
                final String pre = this.text.substring(0, cursorPos);
                final String post = this.text.substring(cursorPos);
                final FEConstantRealVector preReal = new FEConstantRealVector(fontSize);
                preReal.setText(pre, true);
                final FEConstantRealVector postReal = new FEConstantRealVector(fontSize);
                postReal.setText(post, true);
                final EBinaryOp theBinary = EBinaryOp.forOp(ch);
                final FEBinaryOper binary = new FEBinaryOper(fontSize, theBinary);
                getParent().replaceChild(this, binary);
                binary.setArg1(preReal, false);
                binary.setArg2(postReal, true);
            }
        } else if (((int) ch >= '0' && (int) ch <= '9') || (int) ch == '.') {
            if (!FEConstantReal.PI_STRING.equals(this.text)) {
                ++fECursor.cursorPosition;
                final String firstPart = this.text.substring(0, cursorPos);
                final String lastPart = this.text.substring(cursorPos);
                setText(firstPart + ch + lastPart, true);
            }
        } else if ((int) ch == '\u03C0' && this.text.isEmpty()) {
            ++fECursor.cursorPosition;
            setText(FEConstantReal.PI_STRING, true);
        } else if ((int) ch == '\u0435' && this.text.isEmpty()) {
            ++fECursor.cursorPosition;
            setText(FEConstantReal.E_STRING, true);
        } else if ((int) ch == 0x08 && cursorPos > 0) {
            --fECursor.cursorPosition;
            if (this.text.length() == 1) {
                getParent().replaceChild(this, null);
            } else {
                final String firstPart = this.text.substring(0, cursorPos - 1);
                final String lastPart = this.text.substring(cursorPos);
                setText(firstPart + lastPart, true);
            }
        } else if ((int) ch == 0x7f && cursorPos < getNumCursorSteps()) {
            // Delete
            if (this.text.length() == 1) {
                getParent().replaceChild(this, null);
            } else {
                final String firstPart = this.text.substring(0, cursorPos);
                final String lastPart = this.text.substring(cursorPos + 1);
                setText(firstPart + lastPart, true);
            }
        }

        // TODO: Allow "times 10 to the ___" symbol

        // TODO: support commas
    }

    /**
     * Processes an insert.
     *
     * @param fECursor the cursor position and selection range
     * @param toInsert the object to insert (never {@code null})
     * @return {@code null} on success; an error message on failure
     */
    @Override
    public String processInsert(final FECursor fECursor, final AbstractFEObject toInsert) {

        return "Cannot insert object into real vector value.";
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

            for (final char ch : this.text.toCharArray()) {
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
        builder.addln((parent == null ? "Real Vector*: '" : "Real Vector: '"), this.text, "' (", this.value, ")");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEConstantRealVector duplicate() {

        final int fontSize = getFontSize();
        final FEConstantRealVector dup = new FEConstantRealVector(fontSize);

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
