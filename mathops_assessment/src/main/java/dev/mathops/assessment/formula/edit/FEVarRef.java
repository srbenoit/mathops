package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.VariableRef;
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
 * A reference to a variable, with an optional index that can generate a {@code VariableRef} object.
 */
public final class FEVarRef extends AbstractFEObject {

    /** Characters valid in a variable name. */
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_,.?:;~";

    /** The variable name (may be empty; never {@code null}). */
    private String varName;

    /** The index ({@code null} if none). */
    private Integer index;

    /** The rendered boxes for the variable name. */
    private final List<RenderedBox> renderedVarName;

    /** The rendered boxes for the index. */
    private final List<RenderedBox> renderedIndex;

    /** The opening brace box ("{"). */
    private RenderedBox openBrace;

    /** The mid brace box ("[" when there is an index). */
    private RenderedBox midBrace;

    /** The closing brace box ("}" when no index, or "]}" when there is an index). */
    private RenderedBox closeBrace;

    /**
     * The current index string ({@code null} if none, can be blank to indicate an index has been requested but not yet
     * typed).
     */
    private String indexString;

    /**
     * Constructs a new {@code FEVarRef}.
     *
     * @param theFontSize the font size for the component
     */
    public FEVarRef(final int theFontSize) {

        super(theFontSize);

        this.varName = CoreConstants.EMPTY;

        final EnumSet<EType> allowed = getAllowedTypes();
        allowed.add(EType.BOOLEAN);
        allowed.add(EType.INTEGER);
        allowed.add(EType.REAL);
        allowed.add(EType.INTEGER_VECTOR);
        allowed.add(EType.REAL_VECTOR);
        allowed.add(EType.STRING);
        allowed.add(EType.SPAN);

        getPossibleTypes().addAll(allowed);

        this.renderedVarName = new ArrayList<>(10);
        this.renderedIndex = new ArrayList<>(10);
    }

    /**
     * Constructs a new {@code FEVarRef}.
     *
     * @param theFontSize the font size for the component
     * @param theVarName  the variable name
     * @param theIndex    the index, null if none
     */
    public FEVarRef(final int theFontSize, final String theVarName, final Integer theIndex) {

        this(theFontSize);

        this.varName = theVarName == null ? CoreConstants.EMPTY : theVarName;
        if (theIndex != null) {
            setIndexString(theIndex.toString(), false);
        }
    }

    /**
     * Sets the variable name.
     *
     * @param theVarName the new variable name
     * @param storeUndo  true to store an undo state; false to skip
     */
    private void setVarName(final String theVarName, final boolean storeUndo) {

        this.varName = theVarName;
        recomputeCurrentType();
        update(storeUndo);
    }

    /**
     * Gets the variable name.
     *
     * @return the variable name
     */
    public String getVarName() {

        return this.varName;
    }

    /**
     * Sets the index string. If this is non-null and non-empty, an attempt is made to parse an index integer from it.
     *
     * @param theIndexString the new index string ({@code null} if none)
     * @param storeUndo      true to store an undo state; false to skip
     */
    private void setIndexString(final String theIndexString, final boolean storeUndo) {

        this.indexString = theIndexString;
        this.index = null;

        if (theIndexString != null) {
            try {
                this.index = Integer.valueOf(theIndexString);
            } catch (final NumberFormatException ex) {
                // No action
            }
        }

        update(storeUndo);
    }

    /**
     * Gets the index string.
     *
     * @return the index string ({@code null} if none)
     */
    public String getIndexString() {

        return this.indexString;
    }

    /**
     * Gets the index.
     *
     * @return the index ({@code null} if none)
     */
    public Integer getIndex() {

        return this.index;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. There is an opening "{", the variable
     * name, and a closing "}". If there is an index, the format will be "{name[index]}", where the cursor steps over
     * the '[' as well, but treats the closing "]}" as a single step.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        int count = 2 + this.varName.length();

        if (this.indexString != null) {
            count += 1 + this.indexString.length();
        }

        return count;
    }

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if valid (a formula can be generated); false if not
     */
    @Override
    public boolean isValid() {

        boolean valid = !this.varName.isBlank();

        if (valid && this.indexString != null) {
            valid = this.index != null;
        }

        // TODO: If we had an EvalContext, we could check that the variable name is valid, and if
        // there is an index, if the variable has vector type

        return valid;
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

        // TODO: If we had an EvalContext, we could check the type by variable name
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
     * Generates a {@code VariableRef} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public AbstractFormulaObject generate() {

        VariableRef result = null;

        if (!this.varName.isBlank()) {
            if (this.indexString == null) {
                result = new VariableRef(this.varName);
            } else if (this.index != null) {
                result = new VariableRef(this.varName);
                result.index = this.index;
            }
        }

        return result;
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

        // Log.info("Variable reference processing '", Character.toString(ch), "'");

        final int innerPos = fECursor.cursorPosition - getFirstCursorPosition() - 1;

        if (VALID_CHARS.indexOf(ch) >= 0) {
            ++fECursor.cursorPosition;
            setVarName(this.varName.substring(0, innerPos) + ch + this.varName.substring(innerPos), true);
        } else if (ch == 0x08) {
            // Backspace
            if (this.varName.isEmpty()) {
                if (innerPos > 0) {
                    // Cursor was after second quote
                    --fECursor.cursorPosition;
                }
                --fECursor.cursorPosition;
                getParent().replaceChild(this, null);
            } else if (innerPos > 0 && innerPos <= this.varName.length()) {
                --fECursor.cursorPosition;
                setVarName(this.varName.substring(0, innerPos - 1) + this.varName.substring(innerPos), true);
            }
        } else if (ch == 0x7f) {
            // Delete
            if (this.varName.isEmpty()) {
                if (innerPos >= 0) {
                    // Careful no to decrement if cursor was before first quote
                    --fECursor.cursorPosition;
                }
                getParent().replaceChild(this, null);
            } else if (innerPos >= 0 && innerPos < this.varName.length()) {
                setVarName(this.varName.substring(0, innerPos) + this.varName.substring(innerPos + 1), true);
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

        return "Cannot insert object into variable reference.";
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

        this.openBrace = new RenderedBox("{");
        this.openBrace.setFontSize(getFontSize());
        this.openBrace.layout(g2d);

        if (this.indexString == null) {
            this.midBrace = null;
            this.closeBrace = new RenderedBox("}");
        } else {
            this.midBrace = new RenderedBox("[");
            this.midBrace.setFontSize(getFontSize());
            this.midBrace.layout(g2d);
            this.closeBrace = new RenderedBox("]}");
        }
        this.closeBrace.setFontSize(getFontSize());
        this.closeBrace.layout(g2d);

        this.renderedVarName.clear();
        this.renderedIndex.clear();

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.openBrace.getAdvance();
        int topY = 0;
        int botY = 0;

        for (final char ch : this.varName.toCharArray()) {
            final RenderedBox charBox = new RenderedBox(Character.toString(ch));
            this.renderedVarName.add(charBox);
            charBox.setFontSize(getFontSize());
            charBox.layout(g2d);
            charBox.getOrigin().setLocation(x, 0);
            x += charBox.getAdvance();

            final Rectangle charBounds = charBox.getBounds();
            topY = Math.min(topY, charBounds.y);
            botY = Math.max(botY, charBounds.y + charBounds.height);
        }

        if (this.indexString != null && this.midBrace != null) {
            this.midBrace.getOrigin().setLocation(x, 0);
            x += this.midBrace.getAdvance();

            final Rectangle midBraceBounds = this.midBrace.getBounds();
            topY = Math.min(topY, midBraceBounds.y);
            botY = Math.max(botY, midBraceBounds.y + midBraceBounds.height);

            for (final char ch : this.indexString.toCharArray()) {
                final RenderedBox charBox = new RenderedBox(Character.toString(ch));
                this.renderedIndex.add(charBox);
                charBox.setFontSize(getFontSize());
                charBox.layout(g2d);
                charBox.getOrigin().setLocation(x, 0);
                x += charBox.getAdvance();

                final Rectangle charBounds = charBox.getBounds();
                topY = Math.min(topY, charBounds.y);
                botY = Math.max(botY, charBounds.y + charBounds.height);
            }
        }

        this.closeBrace.getOrigin().setLocation(x, 0);
        x += this.closeBrace.getAdvance();

        final Rectangle closeBraceBounds = this.closeBrace.getBounds();
        topY = Math.min(topY, closeBraceBounds.y);
        botY = Math.max(botY, closeBraceBounds.y + closeBraceBounds.height);

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

        if (this.openBrace != null) {
            this.openBrace.translate(dx, dy);
        }
        for (final RenderedBox box : this.renderedVarName) {
            box.translate(dx, dy);
        }
        if (this.midBrace != null) {
            this.midBrace.translate(dx, dy);
        }
        for (final RenderedBox box : this.renderedIndex) {
            box.translate(dx, dy);
        }
        if (this.closeBrace != null) {
            this.closeBrace.translate(dx, dy);
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

        if (this.openBrace != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.openBrace.render(g2d, selected);
            ++pos;
        }

        for (final RenderedBox box : this.renderedVarName) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            box.render(g2d, selected);
            ++pos;
        }

        if (this.midBrace != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.midBrace.render(g2d, selected);
            ++pos;
        }

        for (final RenderedBox box : this.renderedIndex) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            box.render(g2d, selected);
            ++pos;
        }

        if (this.closeBrace != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.closeBrace.render(g2d, selected);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.openBrace != null) {
            target.add(this.openBrace);
        }

        target.addAll(this.renderedVarName);

        if (this.midBrace != null) {
            target.add(this.midBrace);
        }

        target.addAll(this.renderedIndex);

        if (this.closeBrace != null) {
            target.add(this.closeBrace);
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
        builder.add((getParent() == null ? "Variable Reference*: {" : "Variable Reference: {"),
                this.varName);
        if (this.index == null) {
            builder.addln("}");
        } else {
            builder.addln("[", this.index, "]}");
        }

        if (getCurrentType() != null) {
            builder.addln(" of type ", getCurrentType());
        }
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEVarRef duplicate() {

        final FEVarRef dup = new FEVarRef(getFontSize());

        dup.getAllowedTypes().clear();
        dup.getAllowedTypes().addAll(getAllowedTypes());
        dup.setCurrentType(getCurrentType());

        if (this.varName != null) {
            dup.setVarName(this.varName, false);
        }
        if (this.indexString != null) {
            dup.setIndexString(this.indexString, false);
        }

        return dup;
    }

    /**
     * Identifies the cursor position within the object.
     *
     * @param cursor the cursor
     * @return the cursor's position within this object
     */
    public CursorPosition getCursorPosition(final FECursor cursor) {

        final CursorPosition result;

        final int myStart = getFirstCursorPosition();
        final int pos = cursor.cursorPosition;

        // {name[123]}

        if (pos <= myStart) {
            result = CursorPosition.OUTSIDE;
        } else if (pos == myStart + 1) {
            result = CursorPosition.WITHIN_VARNAME;
        } else if (this.varName.isEmpty()) {
            if (this.indexString == null) {
                // No index is indicated
                result = CursorPosition.OUTSIDE;
            } else {
                // An index is indicated
                final int indexEnd = myStart + 2 + this.indexString.length();
                if (pos <= indexEnd) {
                    result = CursorPosition.WITHIN_INDEX;
                } else {
                    result = CursorPosition.OUTSIDE;
                }
            }
        } else {
            // Var name has data
            final int varNameEnd = myStart + 1 + this.varName.length();
            if (pos <= varNameEnd) {
                result = CursorPosition.WITHIN_VARNAME;
            } else if (this.indexString == null) {
                // No index is indicated
                result = CursorPosition.OUTSIDE;
            } else {
                // An index is indicated
                final int indexEnd = varNameEnd + 1 + this.indexString.length();
                if (pos <= indexEnd) {
                    result = CursorPosition.WITHIN_INDEX;
                } else {
                    result = CursorPosition.OUTSIDE;
                }
            }
        }

        return result;
    }

    /**
     * Possible cursor positions within the object.
     */
    public enum CursorPosition {

        /** Variable name is present, cursor is within variable name. */
        WITHIN_VARNAME,

        /** Index is present, cursor is within index. */
        WITHIN_INDEX,

        /** Cursor falls outside the object. */
        OUTSIDE
    }
}
