package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.EFunction;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.IsExactOper;
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
 * A container that can accept a Bool-valued condition argument and two additional Real-valued arguments that serve as
 * the "then" and "else" results depending on the evaluated value of the condition.
 */
public final class FEIsExact extends AbstractFEObject {

    /** The value to test. */
    private AbstractFEObject valueToTest;

    /** The number of places. */
    private AbstractFEObject numberOfPlaces;

    /** The "IsExact" box. */
    private RenderedBox isExactBox;

    /** The comma box. */
    private RenderedBox commaBox;

    /** The closing parenthesis box. */
    private RenderedBox closeParenBox;

    /**
     * Constructs a new {@code FETest}.
     *
     * @param theFontSize the font size for the component
     */
    public FEIsExact(final int theFontSize) {

        super(theFontSize);

        final EnumSet<EType> allowed = getAllowedTypes();
        allowed.add(EType.INTEGER);
        allowed.add(EType.REAL);

        getPossibleTypes().addAll(allowed);
    }

    /**
     * Sets the value to test
     *
     * @param newValue  the new value to test
     * @param storeUndo true to store an undo state; false to skip
     */
    public void setValueToTest(final AbstractFEObject newValue, final boolean storeUndo) {

        if (newValue == null) {
            if (this.valueToTest != null) {
                this.valueToTest.setParent(null);
            }
            this.valueToTest = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EType childType = newValue.getCurrentType();

            if (childType == EType.INTEGER || childType == EType.REAL) {
                if (this.valueToTest != null) {
                    this.valueToTest.setParent(null);
                }
                this.valueToTest = newValue;
                newValue.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }
    }

    /**
     * Gets the value to test.
     *
     * @return the value to test
     */
    public AbstractFEObject getValueToTest() {

        return this.valueToTest;
    }

    /**
     * Sets the number of places.
     *
     * @param newElseClause the new number of places
     * @param storeUndo     true to store an undo state; false to skip
     */
    public void setNumPlaces(final AbstractFEObject newElseClause, final boolean storeUndo) {

        if (newElseClause == null) {
            if (this.numberOfPlaces != null) {
                this.numberOfPlaces.setParent(null);
            }
            this.numberOfPlaces = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EType childType = newElseClause.getCurrentType();

            if (childType == EType.INTEGER) {
                if (this.numberOfPlaces != null) {
                    this.numberOfPlaces.setParent(null);
                }
                this.numberOfPlaces = newElseClause;
                newElseClause.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }
    }

    /**
     * Gets the number of places.
     *
     * @return the number of places
     */
    public AbstractFEObject getNumPlaces() {

        return this.numberOfPlaces;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. There is an "IS_EXACT(" token, the
     * value to test, a ", " token, the number of decimal places, and finally a ")" token.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        int count = 3;

        if (this.valueToTest != null) {
            count += this.valueToTest.getNumCursorSteps();
        }
        if (this.numberOfPlaces != null) {
            count += this.numberOfPlaces.getNumCursorSteps();
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

        boolean valid = false;

        if (this.valueToTest != null && this.numberOfPlaces != null) {
            valid = this.valueToTest.isValid() && this.numberOfPlaces.isValid();
        }

        return valid;
    }

    /**
     * Generates a {@code IsExactOper} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public AbstractFormulaObject generate() {

        IsExactOper result = null;

        if (this.valueToTest != null && this.numberOfPlaces != null) {
            final AbstractFormulaObject valueToTestObj = this.valueToTest.generate();
            final AbstractFormulaObject numPlacesObj = this.numberOfPlaces.generate();

            if (valueToTestObj != null && numPlacesObj != null) {
                result = new IsExactOper();
                result.addChild(valueToTestObj);
                result.addChild(numPlacesObj);
            }
        }

        return result;
    }

    /**
     * Recomputes the current type (does nothing for fixed-type constant values).
     */
    @Override
    public void recomputeCurrentType() {

        // Does nothing - return type is always Boolean
    }

    /**
     * Recomputes all cursor positions within the object.
     *
     * @param startPos the start position of this object
     */
    @Override
    public void recomputeCursorPositions(final int startPos) {

        setFirstCursorPosition(startPos);

        int pos = startPos + 1; // Skip "IsExact("

        if (this.valueToTest != null) {
            this.valueToTest.recomputeCursorPositions(pos);
            pos += this.valueToTest.getNumCursorSteps();
        }

        ++pos; // Skip ","

        if (this.numberOfPlaces != null) {
            this.numberOfPlaces.recomputeCursorPositions(pos);
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

        boolean result = false;

        if (newChild == null) {
            if (currentChild == this.valueToTest) {
                currentChild.setParent(null);
                setValueToTest(null, true);
                result = true;
            } else if (currentChild == this.numberOfPlaces) {
                currentChild.setParent(null);
                setNumPlaces(null, true);
                result = true;
            } else {
                Log.warning("Attempt to replace object that is not child of this object");
            }
        } else {
            final EType newType = newChild.getCurrentType();
            final EnumSet<EType> allowedArgs = getAllowedTypes();

            if (newType == null) {
                final EnumSet<EType> possible = newChild.getAllowedTypes();
                final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

                for (final EType test : possible) {
                    if (allowedArgs.contains(test)) {
                        result = true;
                        filtered.add(test);
                    }
                }

                if (result) {
                    if (currentChild == this.valueToTest) {
                        currentChild.setParent(null);
                        newChild.setParent(this);
                        setValueToTest(newChild, true);
                        possible.clear();
                        possible.addAll(filtered);
                        result = true;
                    } else if (currentChild == this.numberOfPlaces) {
                        currentChild.setParent(null);
                        newChild.setParent(this);
                        setNumPlaces(newChild, true);
                        possible.clear();
                        possible.addAll(filtered);
                        result = true;
                    } else {
                        Log.warning("Attempt to replace object that is not child of this object");
                    }
                }
            } else if (allowedArgs.contains(newType)) {
                if (currentChild == this.valueToTest) {
                    currentChild.setParent(null);
                    newChild.setParent(this);
                    setValueToTest(newChild, true);
                    result = true;
                } else if (currentChild == this.numberOfPlaces) {
                    currentChild.setParent(null);
                    newChild.setParent(this);
                    setNumPlaces(newChild, true);
                    result = true;
                } else {
                    Log.warning("Attempt to replace object that is not child of this object");
                }
            } else {
                Log.warning("Attempt to add ", newType,
                        " type child as test condition; number is required");
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

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_VALUE_SLOT:
                allowedModifications.add(EModification.TYPE);
                allowedModifications.add(EModification.INSERT_INTEGER);
                allowedModifications.add(EModification.INSERT_REAL);
                break;

            case IN_EMPTY_PLACES_SLOT:
                allowedModifications.add(EModification.TYPE);
                allowedModifications.add(EModification.INSERT_INTEGER);
                break;

            case WITHIN_VALUE:
                this.valueToTest.indicateValidModifications(fECursor, allowedModifications);
                break;

            case WITHIN_PLACES:
                this.numberOfPlaces.indicateValidModifications(fECursor, allowedModifications);
                break;

            default:
            case OUTSIDE:
                break;
        }
    }

    /**
     * Processes a typed character.
     *
     * @param fECursor the cursor position and selection range
     * @param ch     the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_VALUE_SLOT:
                processCharEmptyValueSlot(fECursor, ch);
                break;

            case IN_EMPTY_PLACES_SLOT:
                processCharEmptyPlacesSlot(fECursor, ch);
                break;

            case WITHIN_VALUE:
                this.valueToTest.processChar(fECursor, ch);
                break;

            case WITHIN_PLACES:
                this.numberOfPlaces.processChar(fECursor, ch);
                break;

            case OUTSIDE:
                final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();
                final int lastPos = getFirstCursorPosition() + getNumCursorSteps();

                if (ch == 0x08 && cursorPos == lastPos) {
                    // Backspace
                    fECursor.cursorPosition = getFirstCursorPosition();
                    getParent().replaceChild(this, null);
                } else if (ch == 0x7f && cursorPos == 0) {
                    // Delete
                    getParent().replaceChild(this, null);
                }
                break;

            default:
                break;
        }
    }

    /**
     * Processes a typed character when the cursor is in the condition slot and there is no condition present.
     *
     * @param cursor the cursor position and selection range
     * @param ch     the character typed
     */
    private void processCharEmptyValueSlot(final FECursor cursor, final char ch) {

        AbstractFEObject newObject = null;

        if (ch >= '0' && ch <= '9') {
            ++cursor.cursorPosition;
            final FEConstantInteger constInt = new FEConstantInteger(getFontSize());
            constInt.setText(Character.toString(ch), false);
            newObject = constInt;
        } else if (ch == '\u03c0' || ch == '\u0435' || ch == '.') {
            ++cursor.cursorPosition;
            final FEConstantReal constReal = new FEConstantReal(getFontSize());
            constReal.setText(Character.toString(ch), false);
            newObject = constReal;
        } else if (ch == '+' || ch == '-') {
            ++cursor.cursorPosition;
            newObject = new FEUnaryOper(getFontSize(), ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
        } else if (ch == '{') {
            ++cursor.cursorPosition;
            final FEVarRef varRef = new FEVarRef(getFontSize());
            final EnumSet<EType> varAllowed = varRef.getAllowedTypes();
            varAllowed.clear();
            varAllowed.add(EType.INTEGER);
            varAllowed.add(EType.REAL);
            newObject = varRef;
        } else if (ch == '(') {
            ++cursor.cursorPosition;
            final FEGrouping grouping = new FEGrouping(getFontSize());
            final EnumSet<EType> groupingAllowed = grouping.getAllowedTypes();
            groupingAllowed.clear();
            groupingAllowed.add(EType.INTEGER);
            groupingAllowed.add(EType.REAL);
            newObject = grouping;
        } else if (ch >= '\u2720' && ch <= '\u274F') {
            final EFunction f = EFunction.forChar(ch);
            if (f != null) {
                ++cursor.cursorPosition;
                newObject = new FEFunction(getFontSize(), f);
            }
        } else if (ch == '<') {
            ++cursor.cursorPosition;
            final FEIsExact test = new FEIsExact(getFontSize());
            final EnumSet<EType> testAllowed = test.getAllowedTypes();
            testAllowed.clear();
            testAllowed.add(EType.INTEGER);
            testAllowed.add(EType.REAL);
            newObject = test;
        } else if (ch == '*') {
            ++cursor.cursorPosition;
            newObject = new FEError(getFontSize());
        }

        if (newObject != null) {
            setValueToTest(newObject, true);
        }
    }

    /**
     * Processes a typed character when the cursor is in the "Then" or "Else" clause slot and there is no clause
     * present.
     *
     * @param cursor the cursor position and selection range
     * @param ch     the character typed
     */
    private void processCharEmptyPlacesSlot(final FECursor cursor, final char ch) {

        AbstractFEObject newObject = null;

        if (ch >= '0' && ch <= '9') {
            ++cursor.cursorPosition;
            final FEConstantInteger constInt = new FEConstantInteger(getFontSize());
            constInt.setText(Character.toString(ch), false);
            newObject = constInt;
        } else if (ch == '{') {
            ++cursor.cursorPosition;
            final FEVarRef varRef = new FEVarRef(getFontSize());
            final EnumSet<EType> varAllowed = varRef.getAllowedTypes();
            varAllowed.clear();
            varAllowed.add(EType.INTEGER);
            newObject = varRef;
        } else if (ch == '(') {
            ++cursor.cursorPosition;
            final FEGrouping grouping = new FEGrouping(getFontSize());
            final EnumSet<EType> groupingAllowed = grouping.getAllowedTypes();
            groupingAllowed.clear();
            groupingAllowed.add(EType.INTEGER);
            newObject = grouping;
        } else if (ch >= '\u2720' && ch <= '\u274F') {
            final EFunction f = EFunction.forChar(ch);
            if (f != null) {
                ++cursor.cursorPosition;
                newObject = new FEFunction(getFontSize(), f);
            }
        } else if (ch == '<') {
            ++cursor.cursorPosition;
            final FEIsExact test = new FEIsExact(getFontSize());
            final EnumSet<EType> testAllowed = test.getAllowedTypes();
            testAllowed.clear();
            testAllowed.add(EType.INTEGER);
            newObject = test;
        } else if (ch == '*') {
            ++cursor.cursorPosition;
            newObject = new FEError(getFontSize());
        }

        if (newObject != null) {
            setNumPlaces(newObject, true);
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

        final EType newType = toInsert.getCurrentType();

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_VALUE_SLOT:
                if (newType == EType.INTEGER || newType == EType.REAL) {
                    setValueToTest(toInsert, true);
                } else if (newType == null) {
                    final EnumSet<EType> newAllowed = toInsert.getAllowedTypes();
                    if (newAllowed.contains(EType.INTEGER) || newAllowed.contains(EType.REAL)) {
                        setValueToTest(toInsert, true);
                        newAllowed.clear();
                        newAllowed.add(EType.INTEGER);
                        newAllowed.add(EType.REAL);
                    }
                } else {
                    error = "Value to test must have numeric type";
                }
                break;

            case IN_EMPTY_PLACES_SLOT:
                if (newType == EType.INTEGER) {
                    setNumPlaces(toInsert, true);
                } else if (newType == null) {
                    final EnumSet<EType> newAllowed = toInsert.getAllowedTypes();
                    if (newAllowed.contains(EType.INTEGER)) {
                        setValueToTest(toInsert, true);
                        newAllowed.clear();
                        newAllowed.add(EType.INTEGER);
                    }
                } else {
                    error = "Number of places must have integer type";
                }
                break;

            case WITHIN_VALUE:
                this.valueToTest.processInsert(fECursor, toInsert);
                break;

            case WITHIN_PLACES:
                this.numberOfPlaces.processInsert(fECursor, toInsert);
                break;
            default:
            case OUTSIDE:
                break;
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

        this.isExactBox = new RenderedBox("IsExact(");
        this.isExactBox.useSans();
        this.isExactBox.setFontSize(getFontSize());
        this.isExactBox.layout(g2d);

        this.commaBox = new RenderedBox(", ");
        this.commaBox.useSans();
        this.commaBox.setFontSize(getFontSize());
        this.commaBox.layout(g2d);

        this.closeParenBox = new RenderedBox(")");
        this.closeParenBox.useSans();
        this.closeParenBox.setFontSize(getFontSize());
        this.closeParenBox.layout(g2d);

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.isExactBox.getAdvance();
        int topY = Math.min(Math.min(this.isExactBox.getBounds().y, this.commaBox.getBounds().y),
                this.closeParenBox.getBounds().y);
        int botY = 0;

        if (this.valueToTest != null) {
            this.valueToTest.layout(g2d);
            this.valueToTest.translate(x, 0);
            x += this.valueToTest.getAdvance();

            final Rectangle conditionBounds = this.valueToTest.getBounds();
            topY = Math.min(topY, conditionBounds.y);
            botY = Math.max(botY, conditionBounds.y + conditionBounds.height);
        }

        this.commaBox.translate(x, 0);
        x += this.commaBox.getAdvance();

        if (this.numberOfPlaces != null) {
            this.numberOfPlaces.layout(g2d);
            this.numberOfPlaces.translate(x, 0);
            x += this.numberOfPlaces.getAdvance();

            final Rectangle elseBounds = this.numberOfPlaces.getBounds();
            topY = Math.min(topY, elseBounds.y);
            botY = Math.max(botY, elseBounds.y + elseBounds.height);
        }

        this.closeParenBox.translate(x, 0);
        x += this.closeParenBox.getAdvance();

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

        if (this.isExactBox != null) {
            this.isExactBox.translate(dx, dy);
        }
        if (this.valueToTest != null) {
            this.valueToTest.translate(dx, dy);
        }
        if (this.commaBox != null) {
            this.commaBox.translate(dx, dy);
        }
        if (this.numberOfPlaces != null) {
            this.numberOfPlaces.translate(dx, dy);
        }
        if (this.closeParenBox != null) {
            this.closeParenBox.translate(dx, dy);
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

        if (this.isExactBox != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.isExactBox.render(g2d, selected);
        }

        pos += 1 + (this.valueToTest == null ? 0 : this.valueToTest.getNumCursorSteps());

        if (this.commaBox != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.commaBox.render(g2d, selected);
        }

        pos += 1 + (this.numberOfPlaces == null ? 0 : this.numberOfPlaces.getNumCursorSteps());

        if (this.closeParenBox != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.closeParenBox.render(g2d, selected);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.isExactBox != null) {
            target.add(this.isExactBox);
        }

        if (this.valueToTest != null) {
            this.valueToTest.gatherRenderedBoxes(target);
        }

        if (this.commaBox != null) {
            target.add(this.commaBox);
        }

        if (this.numberOfPlaces != null) {
            this.numberOfPlaces.gatherRenderedBoxes(target);
        }

        if (this.closeParenBox != null) {
            target.add(this.closeParenBox);
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
        builder.addln((getParent() == null ? "IS_EXACT* {" : "IS_EXACT {"));

        if (this.valueToTest == null) {
            indent(builder, indent + 1);
            builder.add("(No value)");
        } else {
            this.valueToTest.emitDiagnostics(builder, indent + 1);
        }

        builder.addln(", ");

        if (this.numberOfPlaces == null) {
            indent(builder, indent + 1);
            builder.add("(No number of places)");
        } else {
            this.numberOfPlaces.emitDiagnostics(builder, indent + 1);
        }

        builder.addln("}");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEIsExact duplicate() {

        final FEIsExact dup = new FEIsExact(getFontSize());

        dup.getAllowedTypes().clear();
        dup.getAllowedTypes().addAll(getAllowedTypes());
        dup.setCurrentType(getCurrentType());

        if (this.valueToTest != null) {
            dup.setValueToTest(this.valueToTest.duplicate(), false);
        }
        if (this.numberOfPlaces != null) {
            dup.setNumPlaces(this.numberOfPlaces.duplicate(), false);
        }

        return dup;
    }

    /**
     * Identifies the cursor position within the object.
     *
     * @param cursor the cursor
     * @return the cursor's position within this object
     */
    private CursorPosition getCursorPosition(final FECursor cursor) {

        final CursorPosition result;

        final int cursorPos = cursor.cursorPosition;
        final int myStart = getFirstCursorPosition();
        final int valueEnd =
                myStart + 1 + (this.valueToTest == null ? 0 : this.valueToTest.getNumCursorSteps());
        final int numPlacesEnd = valueEnd + 1
                + (this.numberOfPlaces == null ? 0 : this.numberOfPlaces.getNumCursorSteps());

        if (cursorPos < myStart + 1) {
            result = CursorPosition.OUTSIDE;
        } else if (cursorPos <= valueEnd) {
            // At start of condition
            if (this.valueToTest == null) {
                result = CursorPosition.IN_EMPTY_VALUE_SLOT;
            } else {
                result = CursorPosition.WITHIN_VALUE;
            }
        } else if (cursorPos <= numPlacesEnd) {
            // At start of condition
            if (this.numberOfPlaces == null) {
                result = CursorPosition.IN_EMPTY_PLACES_SLOT;
            } else {
                result = CursorPosition.WITHIN_PLACES;
            }
        } else {
            result = CursorPosition.OUTSIDE;
        }

        return result;
    }

    /**
     * Possible cursor positions within the object.
     */
    private enum CursorPosition {

        /** Value is not present, cursor is at the value insertion point. */
        IN_EMPTY_VALUE_SLOT,

        /** Value is present, cursor is within value. */
        WITHIN_VALUE,

        /** Number of places is not present, cursor is at the number of places insertion point. */
        IN_EMPTY_PLACES_SLOT,

        /** Number of places is present, cursor is within number of places. */
        WITHIN_PLACES,

        /** Cursor falls outside the object. */
        OUTSIDE
    }
}
