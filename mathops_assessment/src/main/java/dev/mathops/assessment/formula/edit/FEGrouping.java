package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.EFunction;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.GroupingOper;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for a single Real-valued object that can generate a {@code GroupingOper}.
 */
public final class FEGrouping extends AbstractFEObject {

    /** The opening parenthesis box. */
    private RenderedBox openParen;

    /** The closing parenthesis box. */
    private RenderedBox closeParen;

    /** The single argument. */
    private AbstractFEObject arg;

    /**
     * Constructs a new {@code FEGroupingReal}.
     *
     * @param theFontSize the font size for the component
     */
    public FEGrouping(final int theFontSize) {

        super(theFontSize);

        final EnumSet<EType> allowed = getAllowedTypes();
        allowed.add(EType.BOOLEAN);
        allowed.add(EType.INTEGER);
        allowed.add(EType.REAL);
        allowed.add(EType.INTEGER_VECTOR);
        allowed.add(EType.REAL_VECTOR);
        allowed.add(EType.STRING);
        allowed.add(EType.SPAN);

        getPossibleTypes().addAll(allowed);
    }

    /**
     * Sets the argument.
     *
     * @param newArg    the new argument
     * @param storeUndo true to store an undo state; false to skip
     */
    public void setArg(final AbstractFEObject newArg, final boolean storeUndo) {

        if (newArg == null) {
            if (this.arg != null) {
                this.arg.setParent(null);
            }
            this.arg = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EnumSet<EType> allowed = getAllowedTypes();
            final EType childType = newArg.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> filtered = EType.filter(allowed, newArg.getAllowedTypes());
                isAllowed = !filtered.isEmpty();
            } else {
                isAllowed = allowed.contains(childType);
            }

            if (isAllowed) {
                if (this.arg != null) {
                    this.arg.setParent(null);
                }
                this.arg = newArg;
                newArg.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }
    }

    /**
     * Gets the argument.
     *
     * @return the argument
     */
    public AbstractFEObject getArg1() {

        return this.arg;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. There is an opening '(' followed by
     * the argument, then a closing ')'.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        return this.arg == null ? 2 : 2 + this.arg.getNumCursorSteps();
    }

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if valid (a formula can be generated); false if not
     */
    @Override
    public boolean isValid() {

        return this.arg != null && this.arg.isValid();
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

        boolean result;

        if (this.arg == null) {
            Log.warning("Attempt to replace object when there are no child objects");
            result = false;
        } else if (currentChild == this.arg) {
            if (newChild == null) {
                setArg(null, true);
                result = true;
            } else {
                final EnumSet<EType> allowed = getAllowedTypes();
                final EType newType = newChild.getCurrentType();

                if (newType == null) {
                    result = false;
                    final EnumSet<EType> possible = newChild.getAllowedTypes();
                    final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

                    for (final EType test : possible) {
                        if (allowed.contains(test)) {
                            result = true;
                            filtered.add(test);
                        }
                    }

                    if (result) {
                        possible.clear();
                        possible.addAll(filtered);
                        setArg(newChild, true);
                    }
                } else if (allowed.contains(newType)) {
                    setArg(newChild, true);
                    result = true;
                } else {
                    Log.warning("Attempt to add ", newType,
                            " type child to grouping when not allowed");
                    result = false;
                }
            }
        } else {
            Log.warning("Attempt to replace object that is not child of this object");
            result = false;
        }

        return result;
    }

    /**
     * Recomputes the current type (does nothing for fixed-type constant values).
     */
    @Override
    public void recomputeCurrentType() {

        final EnumSet<EType> possible = getPossibleTypes();
        possible.clear();

        if (this.arg == null) {
            setCurrentType(null);
            possible.addAll(getAllowedTypes());
        } else {
            final EType argType = this.arg.getCurrentType();
            setCurrentType(argType);

            if (argType == null) {
                final EnumSet<EType> filtered =
                        EType.filter(getAllowedTypes(), this.arg.getPossibleTypes());
                possible.addAll(filtered);
            } else {
                possible.add(argType);
            }
        }
    }

    /**
     * Recomputes all cursor positions within the object.
     *
     * @param startPos the start position of this object
     */
    @Override
    public void recomputeCursorPositions(final int startPos) {

        setFirstCursorPosition(startPos);

        final int pos = startPos + 1; // Skip opening parenthesis

        if (this.arg != null) {
            this.arg.recomputeCursorPositions(pos);
        }
    }

    /**
     * Generates a {@code UnaryOper} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public GroupingOper generate() {

        GroupingOper result = null;

        if (this.arg != null) {
            final AbstractFormulaObject argObj = this.arg.generate();
            if (argObj != null) {
                result = new GroupingOper();
                result.addChild(argObj);
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

        if (this.arg == null) {
            final EnumSet<EType> allowed = getAllowedTypes();
            allowedModifications.add(EModification.TYPE);

            if (allowed.contains(EType.BOOLEAN)) {
                allowedModifications.add(EModification.INSERT_BOOLEAN);
            }
            if (allowed.contains(EType.INTEGER)) {
                allowedModifications.add(EModification.INSERT_INTEGER);
            }
            if (allowed.contains(EType.REAL)) {
                allowedModifications.add(EModification.INSERT_REAL);
                allowedModifications.add(EModification.INSERT_INTEGER);
            }
            if (allowed.contains(EType.INTEGER_VECTOR)) {
                allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
            }
            if (allowed.contains(EType.REAL_VECTOR)) {
                allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
            }
            if (allowed.contains(EType.STRING)) {
                allowedModifications.add(EModification.INSERT_STRING);
            }
            if (allowed.contains(EType.SPAN)) {
                allowedModifications.add(EModification.INSERT_SPAN);
            }
        } else {
            this.arg.indicateValidModifications(fECursor, allowedModifications);
        }
    }

    /**
     * Processes a typed character. If the argument is set, the character is simply passed to that object. Otherwise,
     * valid characters are:
     *
     * <ul>
     * <li>A decimal digit; if this object's type is null, INTEGER, or REAL, this triggers the creation of a
     * {@code FEConstantInt} child object with that digit as its text value
     * <li>A '.' (radix mark); if this object's type is null or REAL, this triggers the creation of a
     * {@code FEConstantReal} child object with that mark as its text value
     * <li>'+' or '-'; if this object's type is null, INTEGER, or REAL, this triggers the creation of a
     * {@code FEUnaryOper} child object
     * <li>'"'; if this object's type is null or SPAN, this triggers the creation of a {@code FEConstantSpan} child
     * object with empty content
     * <li>U+22A4; if this object's type is null or BOOLEAN, this triggers the creation of a {@code FEConstantBoolean}
     * child object with TRUE value
     * <li>U+22A5; if this object's type is null or BOOLEAN, this triggers the creation of a {@code FEConstantBoolean}
     * child object with FALSE value
     * <li>'{'; this triggers the creation of a {@code FEVarRef} child object with an empty variable name
     * <li>'['; this triggers the creation of a {@code FEVector} child object with no entries
     * <li>'('; this triggers the creation of a {@code FEGrouping} child object
     * <li>'U+2720' through 'U+274F'; this triggers the creation of a named function
     * <li>'<'; this triggers the creation of a {@code FETest} child object
     * <li>'*'; this triggers the creation of a {@code FEError} child object
     * </ul>
     *
     * @param fECursor the cursor position and selection range
     * @param ch     the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        // Log.info("Grouping container processing '", Character.toString(ch), "'");

        final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();

        if (cursorPos > 0 && cursorPos < getNumCursorSteps()) {
            if (this.arg == null) {
                final EnumSet<EType> allowed = getAllowedTypes();

                if (ch >= '0' && ch <= '9') {
                    if (allowed.contains(EType.REAL) || allowed.contains(EType.INTEGER)) {
                        ++fECursor.cursorPosition;
                        final FEConstantInteger constInt = new FEConstantInteger(getFontSize());
                        constInt.setText(Character.toString(ch), false);
                        setArg(constInt, true);
                    }
                } else if (ch == '\u03c0' || ch == '\u0435' || ch == '.') {
                    if (allowed.contains(EType.REAL)) {
                        ++fECursor.cursorPosition;
                        final FEConstantReal constReal = new FEConstantReal(getFontSize());
                        constReal.setText(Character.toString(ch), false);
                        setArg(constReal, true);
                    }
                } else if (ch == '+' || ch == '-') {
                    if (allowed.contains(EType.REAL) || allowed.contains(EType.INTEGER)) {
                        ++fECursor.cursorPosition;
                        final FEUnaryOper unary = new FEUnaryOper(getFontSize(),
                                ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
                        setArg(unary, true);
                    }
                } else if (ch == '{') {
                    ++fECursor.cursorPosition;
                    final FEVarRef varRef = new FEVarRef(getFontSize());
                    final EnumSet<EType> varAllowed = varRef.getAllowedTypes();
                    varAllowed.clear();
                    varAllowed.addAll(allowed);
                    setArg(varRef, true);
                } else if (ch == '"') {
                    if (allowed.contains(EType.SPAN)) {
                        ++fECursor.cursorPosition;
                        final FEConstantSpan span = new FEConstantSpan(getFontSize());
                        setArg(span, true);
                    }
                } else if (ch == '\u22A4' || ch == '\u22A5') {
                    if (allowed.contains(EType.BOOLEAN)) {
                        ++fECursor.cursorPosition;
                        final FEConstantBoolean bool =
                                new FEConstantBoolean(getFontSize(), ch == '\u22A4');
                        setArg(bool, true);
                    }
                } else if (ch == '[') {
                    if (allowed.contains(EType.REAL_VECTOR)
                            || allowed.contains(EType.INTEGER_VECTOR)) {
                        ++fECursor.cursorPosition;
                        final FEVector vec = new FEVector(getFontSize());
                        if (!allowed.contains(EType.REAL_VECTOR)) {
                            vec.getAllowedTypes().remove(EType.REAL_VECTOR);
                        }
                        setArg(vec, true);
                    }
                } else if (ch == '(') {
                    ++fECursor.cursorPosition;
                    final FEGrouping grouping = new FEGrouping(getFontSize());
                    final EnumSet<EType> groupingAllowed = grouping.getAllowedTypes();
                    groupingAllowed.clear();
                    groupingAllowed.addAll(allowed);
                    setArg(grouping, true);
                } else if (ch >= '\u2720' && ch <= '\u274F') {
                    final EFunction f = EFunction.forChar(ch);
                    if (f != null) {
                        ++fECursor.cursorPosition;
                        final FEFunction function = new FEFunction(getFontSize(), f);
                        setArg(function, true);
                    }
                } else if (ch == '<') {
                    ++fECursor.cursorPosition;
                    final FETest test = new FETest(getFontSize());
                    final EnumSet<EType> testAllowed = test.getAllowedTypes();
                    testAllowed.clear();
                    testAllowed.addAll(allowed);
                    setArg(test, true);
                } else if (ch == '*') {
                    ++fECursor.cursorPosition;
                    final FEError error = new FEError(getFontSize());
                    setArg(error, true);
                } else if (ch == 0x08 && cursorPos > 0) {
                    --fECursor.cursorPosition;
                    // Backspace
                    getParent().replaceChild(this, null);
                } else if (ch == 0x7f && cursorPos < getNumCursorSteps()) {
                    // Delete
                    getParent().replaceChild(this, null);
                }
            } else {
                Log.info("Passing character " + ch + " to " + this.arg.getClass().getSimpleName());
                this.arg.processChar(fECursor, ch);
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

        String error = null;

        if (this.arg == null) {
            this.arg = toInsert;
            this.arg.setParent(this);
            this.arg.setFirstCursorPosition(getFirstCursorPosition());
        } else {
            final int last = getFirstCursorPosition() + getNumCursorSteps();

            if (fECursor.cursorPosition > 0 && fECursor.cursorPosition < last - 1) {
                error = this.arg.processInsert(fECursor, toInsert);
            } else {
                error = "Unable to insert into grouping outside the parentheses.";
            }
        }

        return error;
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

        this.openParen = new RenderedBox("(");
        this.openParen.setFontSize(getFontSize());
        this.openParen.layout(g2d);

        this.closeParen = new RenderedBox(")");
        this.closeParen.setFontSize(getFontSize());
        this.closeParen.layout(g2d);

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.openParen.getAdvance();
        int topY = 0;
        int botY = 0;

        final Rectangle openParenBounds = this.openParen.getBounds();
        topY = Math.min(topY, openParenBounds.y);
        botY = Math.max(botY, openParenBounds.y + openParenBounds.height);

        if (this.arg != null) {
            this.arg.layout(g2d);
            this.arg.translate(x, 0);
            x += this.arg.getAdvance();

            final Rectangle charBounds = this.arg.getBounds();
            topY = Math.min(topY, charBounds.y);
            botY = Math.max(botY, charBounds.y + charBounds.height);
        }

        this.closeParen.translate(x, 0);

        final Rectangle closeParenBounds = this.closeParen.getBounds();
        topY = Math.min(topY, closeParenBounds.y);
        botY = Math.max(botY, closeParenBounds.y + closeParenBounds.height);

        x += this.closeParen.getAdvance();

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

        if (this.openParen != null) {
            this.openParen.translate(dx, dy);
        }
        if (this.closeParen != null) {
            this.closeParen.translate(dx, dy);
        }
        if (this.arg != null) {
            this.arg.translate(dx, dy);
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

        final int first = getFirstCursorPosition();
        if (this.openParen != null) {
            final boolean selected = cursor.doesSelectionInclude(first);
            this.openParen.render(g2d, selected);
        }

        if (this.arg != null) {
            this.arg.render(g2d, cursor);
        }

        final int last = 1 + (this.arg == null ? 0 : this.arg.getNumCursorSteps());
        if (this.closeParen != null) {
            final boolean selected = cursor.doesSelectionInclude(last);
            this.closeParen.render(g2d, selected);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.openParen != null) {
            target.add(this.openParen);
        }

        if (this.arg != null) {
            this.arg.gatherRenderedBoxes(target);
        }

        if (this.closeParen != null) {
            target.add(this.closeParen);
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
        builder.addln((getParent() == null ? "Grouping*:" : "Grouping:"));

        if (this.arg == null) {
            indent(builder, indent + 1);
            builder.addln("(No argument)");
        } else {
            this.arg.emitDiagnostics(builder, indent + 1);
        }
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEGrouping duplicate() {

        final FEGrouping dup = new FEGrouping(getFontSize());

        dup.getAllowedTypes().clear();
        dup.getAllowedTypes().addAll(getAllowedTypes());
        dup.setCurrentType(getCurrentType());

        if (this.arg != null) {
            dup.setArg(this.arg.duplicate(), false);
        }

        return dup;
    }
}
