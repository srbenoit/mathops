package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.EFunction;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.UnaryOper;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for a single argument that can generate a {@code UnaryOper}.
 */
public final class FEUnaryOper extends AbstractFEObject {

    /** The operator. */
    private final EUnaryOp op;

    /** The operator box. */
    private RenderedBox opBox;

    /** The single argument. */
    private AbstractFEObject arg1;

    /**
     * Constructs a new {@code FEUnaryOper}.
     *
     * @param theFontSize the font size for the component
     * @param theOp       the operator
     */
    public FEUnaryOper(final int theFontSize, final EUnaryOp theOp) {

        super(theFontSize);

        if (theOp == null) {
            throw new IllegalArgumentException("Operator may not be null");
        }

        this.op = theOp;

        final EnumSet<EType> allowed = getAllowedTypes();
        final EnumSet<EType> possible = getPossibleTypes();

        switch (theOp) {
            case PLUS:
            case MINUS:
                allowed.add(EType.INTEGER);
                allowed.add(EType.REAL);
                allowed.add(EType.INTEGER_VECTOR);
                allowed.add(EType.REAL_VECTOR);
                possible.add(EType.INTEGER);
                possible.add(EType.REAL);
                possible.add(EType.INTEGER_VECTOR);
                possible.add(EType.REAL_VECTOR);
                break;

            default:
                break;
        }
    }

    /**
     * Sets the argument.
     *
     * @param newArg1   the new argument
     * @param storeUndo true to store an undo state; false to skip
     */
    public void setArg1(final AbstractFEObject newArg1, final boolean storeUndo) {

        if (newArg1 == null) {
            if (this.arg1 != null) {
                this.arg1.setParent(null);
            }
            this.arg1 = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            // FIXME: This should be "getAllowedArgumentTypes()" for arg1 based on operator
            final EnumSet<EType> allowed = getAllowedTypes();
            final EType childType = newArg1.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> filtered = EType.filter(allowed, newArg1.getAllowedTypes());
                isAllowed = !filtered.isEmpty();
            } else {
                isAllowed = allowed.contains(childType);
            }

            if (isAllowed) {
                if (this.arg1 != null) {
                    this.arg1.setParent(null);
                }
                this.arg1 = newArg1;
                newArg1.setParent(this);
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

        return this.arg1;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. There is a "+" followed by the
     * argument
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        return this.arg1 == null ? 1 : 1 + this.arg1.getNumCursorSteps();
    }

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if valid (a formula can be generated); false if not
     */
    @Override
    public boolean isValid() {

        return this.arg1 != null && this.arg1.isValid();
    }

    /**
     * Generates a {@code UnaryOper} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public UnaryOper generate() {

        UnaryOper result = null;

        if (this.arg1 != null) {
            final AbstractFormulaObject argObj = this.arg1.generate();
            if (argObj != null) {
                result = new UnaryOper(this.op);
                result.addChild(argObj);
            }
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

        if (this.arg1 == null) {
            setCurrentType(null);
            possible.add(EType.INTEGER);
            possible.add(EType.REAL);
        } else {
            final EType argType = this.arg1.getCurrentType();
            setCurrentType(argType);

            if (argType == null) {
                possible.add(EType.INTEGER);
                possible.add(EType.REAL);
            } else {
                final EnumSet<EType> arg1Possible = this.arg1.getPossibleTypes();
                if (arg1Possible.contains(EType.INTEGER)) {
                    possible.add(EType.INTEGER);
                }
                if (arg1Possible.contains(EType.REAL)) {
                    possible.add(EType.REAL);
                }
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

        final int pos = startPos + 1; // Skip operator position

        if (this.arg1 != null) {
            this.arg1.recomputeCursorPositions(pos);
        }
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

        if (this.arg1 == null) {
            Log.warning("Attempt to replace object when there are no child objects");
            result = false;
        } else if (currentChild == this.arg1) {
            if (newChild == null) {
                setArg1(null, true);
                result = true;
            } else {
                // At this time, all unary operators return the same type as their argument, so
                // allowed argument types is identical to allowed types
                final EnumSet<EType> allowedArgs = getAllowedTypes();

                final EType newType = newChild.getCurrentType();

                if (newType == null) {
                    result = false;
                    final EnumSet<EType> possible = newChild.getAllowedTypes();
                    final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

                    for (final EType test : possible) {
                        if (allowedArgs.contains(test)) {
                            result = true;
                            filtered.add(test);
                        }
                    }

                    if (result) {
                        possible.clear();
                        possible.addAll(filtered);
                        setArg1(newChild, true);
                    }
                } else if (allowedArgs.contains(newType)) {
                    setArg1(newChild, true);
                    result = true;
                } else {
                    Log.warning("Attempt to add ", newType,
                            " type child to unary operator when not allowed");
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
     * Asks the object what modifications are valid for a specified cursor position or selection range.
     *
     * @param fECursor               cursor position information
     * @param allowedModifications a set that will be populated with the set of allowed modifications at the specified
     *                             position
     */
    @Override
    public void indicateValidModifications(final FECursor fECursor,
                                           final EnumSet<EModification> allowedModifications) {

        if (this.arg1 == null) {
            final EnumSet<EType> allowedArgs = getAllowedTypes();

            allowedModifications.add(EModification.TYPE);

            if (allowedArgs.contains(EType.BOOLEAN)) {
                allowedModifications.add(EModification.INSERT_BOOLEAN);
            }
            if (allowedArgs.contains(EType.INTEGER)) {
                allowedModifications.add(EModification.INSERT_INTEGER);
            }
            if (allowedArgs.contains(EType.REAL)) {
                allowedModifications.add(EModification.INSERT_REAL);
                allowedModifications.add(EModification.INSERT_INTEGER);
            }
            if (allowedArgs.contains(EType.INTEGER_VECTOR)) {
                allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
            }
            if (allowedArgs.contains(EType.REAL_VECTOR)) {
                allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
            }
            if (allowedArgs.contains(EType.SPAN)) {
                allowedModifications.add(EModification.INSERT_SPAN);
            }
        } else {
            this.arg1.indicateValidModifications(fECursor, allowedModifications);
        }
    }

    /**
     * Processes a typed character. If the argument is set, the character is simply passed to that object. Otherwise,
     * valid characters are:
     *
     * <ul>
     * <li>A decimal digit; if this object's type is null, INTEGER, or REAL, this triggers the creation of a
     * {@code FEConstantInt} child object with that digit as its text value     *
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

        // Log.info("Unary operator processing '", Character.toString(ch), "'");

        final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();

        if (this.arg1 == null) {
            final EnumSet<EType> allowed = getAllowedTypes();

            if (ch >= '0' && ch <= '9') {
                if (allowed.contains(EType.REAL) || allowed.contains(EType.INTEGER)) {
                    ++fECursor.cursorPosition;
                    final FEConstantInteger constInt = new FEConstantInteger(getFontSize());
                    constInt.setText(Character.toString(ch), false);
                    setArg1(constInt, true);
                }
            } else if (ch == '\u03c0' || ch == '\u0435' || ch == '.') {
                if (allowed.contains(EType.REAL)) {
                    ++fECursor.cursorPosition;
                    final FEConstantReal constReal = new FEConstantReal(getFontSize());
                    constReal.setText(Character.toString(ch), false);
                    setArg1(constReal, true);
                }
            } else if (ch == '+' || ch == '-') {
                if (allowed.contains(EType.REAL) || allowed.contains(EType.INTEGER)) {
                    ++fECursor.cursorPosition;
                    final FEUnaryOper unary =
                            new FEUnaryOper(getFontSize(), ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
                    setArg1(unary, true);
                }
            } else if (ch == '{') {
                ++fECursor.cursorPosition;
                final FEVarRef varRef = new FEVarRef(getFontSize());
                final EnumSet<EType> varAllowed = varRef.getAllowedTypes();
                varAllowed.clear();
                varAllowed.addAll(allowed);
                setArg1(varRef, true);
            } else if (ch == '[') {
                if (allowed.contains(EType.REAL_VECTOR) || allowed.contains(EType.INTEGER_VECTOR)) {
                    ++fECursor.cursorPosition;
                    final FEVector vec = new FEVector(getFontSize());
                    if (!allowed.contains(EType.REAL_VECTOR)) {
                        vec.getAllowedTypes().remove(EType.REAL_VECTOR);
                    }
                    setArg1(vec, true);
                }
            } else if (ch == '(') {
                ++fECursor.cursorPosition;
                final FEGrouping grouping = new FEGrouping(getFontSize());
                final EnumSet<EType> groupingAllowed = grouping.getAllowedTypes();
                groupingAllowed.clear();
                groupingAllowed.addAll(allowed);
                setArg1(grouping, true);
            } else if (ch >= '\u2720' && ch <= '\u274F') {
                ++fECursor.cursorPosition;
                final EFunction f = EFunction.forChar(ch);
                if (f != null) {
                    final FEFunction function = new FEFunction(getFontSize(), f);
                    setArg1(function, true);
                }
            } else if (ch == '<') {
                ++fECursor.cursorPosition;
                final FETest test = new FETest(getFontSize());
                final EnumSet<EType> testAllowed = test.getAllowedTypes();
                testAllowed.clear();
                testAllowed.addAll(allowed);
                setArg1(test, true);
            } else if (ch == 0x08 && cursorPos > 0) {
                --fECursor.cursorPosition;
                // Backspace
                if (this.arg1 == null) {
                    getParent().replaceChild(this, null);
                }
            } else if (ch == 0x7f && cursorPos < getNumCursorSteps()) {
                // Delete
                getParent().replaceChild(this, null);
            }
        } else if (cursorPos == 0) {
            if (ch == 0x7f) {
                // Delete with cursor before operator - replace this operator with its argument
                getParent().replaceChild(this, this.arg1);
            }
        } else if (cursorPos == 1 && ch == 0x08) {
            // Backspace with cursor after operator - replace this operator with its argument
            --fECursor.cursorPosition;
            getParent().replaceChild(this, this.arg1);
        } else {
            // Log.info("Passing character " + ch + " to " + this.arg1.getClass().getSimpleName());
            this.arg1.processChar(fECursor, ch);
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

        if (this.arg1 == null) {
            this.arg1 = toInsert;
            this.arg1.setParent(this);
            this.arg1.setFirstCursorPosition(getFirstCursorPosition());
        } else {
            final int first = getFirstCursorPosition();
            final int last = first + getNumCursorSteps();

            if (fECursor.cursorPosition > first && fECursor.cursorPosition < last) {
                error = this.arg1.processInsert(fECursor, toInsert);
            } else {
                error = "Unable to insert into unary operator outside the parentheses.";
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

        final String opStr = switch (this.op) {
            case MINUS -> "\u2212";
            case PLUS -> "+";
        };

        this.opBox = new RenderedBox(opStr);
        this.opBox.setFontSize(getFontSize());
        this.opBox.layout(g2d);

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.opBox.getAdvance();
        int topY = 0;
        int botY = 0;

        final Rectangle opBoxBounds = this.opBox.getBounds();
        topY = Math.min(topY, opBoxBounds.y);
        botY = Math.max(botY, opBoxBounds.y + opBoxBounds.height);

        if (this.arg1 != null) {
            this.arg1.layout(g2d);
            this.arg1.translate(x, 0);
            x += this.arg1.getAdvance();

            final Rectangle argBounds = this.arg1.getBounds();
            topY = Math.min(topY, argBounds.y);
            botY = Math.max(botY, argBounds.y + argBounds.height);
        }

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

        if (this.opBox != null) {
            this.opBox.translate(dx, dy);
        }
        if (this.arg1 != null) {
            this.arg1.translate(dx, dy);
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
        if (this.opBox != null) {
            final boolean selected = cursor.doesSelectionInclude(first);
            this.opBox.render(g2d, selected);
        }

        if (this.arg1 != null) {
            this.arg1.render(g2d, cursor);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.opBox != null) {
            target.add(this.opBox);
        }

        if (this.arg1 != null) {
            this.arg1.gatherRenderedBoxes(target);
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
        builder.addln((getParent() == null ? "Unary Operator*: (" : "Unary Operator: ("),
                Character.toString(this.op.op), ")");

        if (this.arg1 == null) {
            indent(builder, indent + 1);
            builder.addln("(No argument)");
        } else {
            this.arg1.emitDiagnostics(builder, indent + 1);
        }
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEUnaryOper duplicate() {

        final FEUnaryOper dup = new FEUnaryOper(getFontSize(), this.op);

        dup.getAllowedTypes().clear();
        dup.getAllowedTypes().addAll(getAllowedTypes());
        dup.setCurrentType(getCurrentType());

        if (this.arg1 != null) {
            dup.setArg1(this.arg1.duplicate(), false);
        }

        return dup;
    }
}
