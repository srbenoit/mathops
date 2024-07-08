package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.EFunction;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.TestOper;
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
 * A container that can accept a Bool-valued condition argument and two additional Real-valued arguments that serve as
 * the "then" and "else" results depending on the evaluated value of the condition.
 */
public final class FETest extends AbstractFEObject {

    /** The condition. */
    private AbstractFEObject condition = null;

    /** The "then" clause. */
    private AbstractFEObject thenClause = null;

    /** The "else" clause. */
    private AbstractFEObject elseClause = null;

    /** The "If" box. */
    private RenderedBox ifBox = null;

    /** The "Then" box. */
    private RenderedBox thenBox = null;

    /** The "Else" box. */
    private RenderedBox elseBox = null;

    /** The "End If" box. */
    private RenderedBox endifBox = null;

    /**
     * Constructs a new {@code FETest}.
     *
     * @param theFontSize the font size for the component
     */
    public FETest(final int theFontSize) {

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
     * Sets the condition.
     *
     * @param newCondition the new condition
     * @param storeUndo    true to store an undo state; false to skip
     */
    public void setCondition(final AbstractFEObject newCondition, final boolean storeUndo) {

        if (newCondition == null) {
            if (this.condition != null) {
                this.condition.setParent(null);
            }
            this.condition = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EType childType = newCondition.getCurrentType();
            boolean isAllowed = false;

            if (childType == null) {
                final EnumSet<EType> possible = newCondition.getAllowedTypes();
                if (possible.contains(EType.BOOLEAN)) {
                    possible.clear();
                    possible.add(EType.BOOLEAN);
                    isAllowed = true;
                }
            } else {
                isAllowed = childType == EType.BOOLEAN;
            }

            if (isAllowed) {
                if (this.condition != null) {
                    this.condition.setParent(null);
                }
                this.condition = newCondition;
                newCondition.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }
    }

    /**
     * Gets the condition.
     *
     * @return the condition
     */
    public AbstractFEObject getCondition() {

        return this.condition;
    }

    /**
     * Sets the "then" clause.
     *
     * @param newThenClause the new "then" clause
     * @param storeUndo     true to store an undo state; false to skip
     */
    public void setThenClause(final AbstractFEObject newThenClause, final boolean storeUndo) {

        if (newThenClause == null) {
            if (this.thenClause != null) {
                this.thenClause.setParent(null);
            }
            this.thenClause = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EnumSet<EType> allowed = getTypesAllowedForThen();
            final EType childType = newThenClause.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> allowedTypes = newThenClause.getAllowedTypes();
                final EnumSet<EType> filtered = EType.filter(allowed, allowedTypes);
                isAllowed = !filtered.isEmpty();
            } else {
                isAllowed = allowed.contains(childType);
            }

            if (isAllowed) {
                if (this.thenClause != null) {
                    this.thenClause.setParent(null);
                }
                this.thenClause = newThenClause;
                newThenClause.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }
    }

    /**
     * Gets the "then" clause.
     *
     * @return the "then" clause
     */
    public AbstractFEObject getThenClause() {

        return this.thenClause;
    }

    /**
     * Sets the "else" clause.
     *
     * @param newElseClause the new "else" clause
     * @param storeUndo     true to store an undo state; false to skip
     */
    public void setElseClause(final AbstractFEObject newElseClause, final boolean storeUndo) {

        if (newElseClause == null) {
            if (this.elseClause != null) {
                this.elseClause.setParent(null);
            }
            this.elseClause = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EnumSet<EType> allowed = getTypesAllowedForElse();
            final EType childType = newElseClause.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> allowedTypes = newElseClause.getAllowedTypes();
                final EnumSet<EType> filtered = EType.filter(allowed, allowedTypes);
                isAllowed = !filtered.isEmpty();
            } else {
                isAllowed = allowed.contains(childType);
            }

            if (isAllowed) {
                if (this.elseClause != null) {
                    this.elseClause.setParent(null);
                }
                this.elseClause = newElseClause;
                newElseClause.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }
    }

    /**
     * Gets the "else" clause.
     *
     * @return the "else" clause
     */
    public AbstractFEObject getElseClause() {

        return this.elseClause;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. There is an "IF (" token, the
     * condition, a ") THEN (" token, the "then" clause, an ") ELSE (" token, then the "else" clause, and finally a ")"
     * token.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        int count = 4;

        if (this.condition != null) {
            count += this.condition.getNumCursorSteps();
        }
        if (this.thenClause != null) {
            count += this.thenClause.getNumCursorSteps();
        }
        if (this.elseClause != null) {
            count += this.elseClause.getNumCursorSteps();
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

        if (this.condition != null && this.thenClause != null && this.elseClause != null) {
            valid = this.condition.isValid() && this.thenClause.isValid() && this.elseClause.isValid();
        }

        return valid;
    }

    /**
     * Generates a {@code TestOperation} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public AbstractFormulaObject generate() {

        TestOper result = null;

        if (this.condition != null && this.thenClause != null && this.elseClause != null) {
            final AbstractFormulaObject conditionObj = this.condition.generate();
            final AbstractFormulaObject thenObj = this.thenClause.generate();
            final AbstractFormulaObject elseObj = this.elseClause.generate();

            if (conditionObj != null && thenObj != null && elseObj != null) {
                result = new TestOper();
                result.addChild(conditionObj);
                result.addChild(thenObj);
                result.addChild(elseObj);
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

        final EnumSet<EType> allowedTypes = getAllowedTypes();

        if (this.thenClause == null) {
            if (this.elseClause == null) {
                setCurrentType(null);
                possible.addAll(allowedTypes);
            } else {
                final EType elseType = this.elseClause.getCurrentType();
                setCurrentType(elseType);

                if (elseType == null) {
                    final EnumSet<EType> possibleElse = this.elseClause.getPossibleTypes();
                    final EnumSet<EType> filtered = EType.filter(allowedTypes, possibleElse);
                    possible.addAll(filtered);
                } else {
                    possible.add(elseType);
                }
            }
        } else if (this.elseClause == null) {
            final EType thenType = this.thenClause.getCurrentType();
            setCurrentType(thenType);

            if (thenType == null) {
                final EnumSet<EType> possibleThen = this.thenClause.getPossibleTypes();
                final EnumSet<EType> filtered = EType.filter(allowedTypes, possibleThen);
                possible.addAll(filtered);
            } else {
                possible.add(thenType);
            }
        } else {
            // We have both "then" and "else" clauses

            final EType thenType = this.thenClause.getCurrentType();
            final EType elseType = this.elseClause.getCurrentType();

            if (thenType == null || elseType == null) {
                setCurrentType(null);
                final EnumSet<EType> possibleThen = this.thenClause.getPossibleTypes();
                final EnumSet<EType> possibleElse = this.elseClause.getPossibleTypes();
                final EnumSet<EType> filtered1 = EType.filter(allowedTypes, possibleThen);
                final EnumSet<EType> filtered2 = EType.filter(filtered1, possibleElse);
                possible.addAll(filtered2);
            } else if (thenType == elseType) {
                setCurrentType(thenType);
                possible.add(thenType);
            } else if ((thenType == EType.INTEGER && elseType == EType.REAL)
                    || (thenType == EType.REAL && elseType == EType.INTEGER)) {
                setCurrentType(EType.REAL);
                possible.add(EType.REAL);
            } else {
                // "then" and "else" are present but not compatible. Shouldn't be allowed to happen.
                Log.warning("Then and Else clauses have incompatible types.");
                setCurrentType(null);
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

        int pos = startPos + 1; // Skip "IF ("

        if (this.condition != null) {
            this.condition.recomputeCursorPositions(pos);
            pos += this.condition.getNumCursorSteps();
        }

        ++pos; // Skip ") THEN ("

        if (this.thenClause != null) {
            this.thenClause.recomputeCursorPositions(pos);
            pos += this.thenClause.getNumCursorSteps();
        }

        ++pos; // Skip ") ELSE ("

        if (this.elseClause != null) {
            this.elseClause.recomputeCursorPositions(pos);
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
    public boolean replaceChild(final AbstractFEObject currentChild, final AbstractFEObject newChild) {

        boolean result = false;

        if (newChild == null) {
            if (currentChild == this.condition) {
                currentChild.setParent(null);
                setCondition(null, true);
                result = true;
            } else if (currentChild == this.thenClause) {
                currentChild.setParent(null);
                setThenClause(null, true);
                result = true;
            } else if (currentChild == this.elseClause) {
                currentChild.setParent(null);
                setElseClause(null, true);
                result = true;
            } else {
                Log.warning("Attempt to replace object that is not child of this object");
            }
        } else if (currentChild == this.condition) {
            final EType newType = newChild.getCurrentType();

            if (newType == null) {
                final EnumSet<EType> possible = newChild.getAllowedTypes();

                if (possible.contains(EType.BOOLEAN)) {
                    currentChild.setParent(null);
                    newChild.setParent(this);
                    possible.clear();
                    possible.add(EType.BOOLEAN);
                    setCondition(newChild, true);
                    result = true;
                } else {
                    Log.warning("Attempt to add 'null' type child as test condition; Boolean is required");
                }
            } else if (newType == EType.BOOLEAN) {
                currentChild.setParent(null);
                newChild.setParent(this);
                setCondition(newChild, true);
                result = true;
            } else {
                Log.warning("Attempt to add ", newType, " type child as test condition; Boolean is required");
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
                    if (currentChild == this.thenClause) {
                        currentChild.setParent(null);
                        newChild.setParent(this);
                        setThenClause(newChild, true);
                        possible.clear();
                        possible.addAll(filtered);
                    } else if (currentChild == this.elseClause) {
                        currentChild.setParent(null);
                        newChild.setParent(this);
                        setElseClause(newChild, true);
                        possible.clear();
                        possible.addAll(filtered);
                    } else {
                        Log.warning("Attempt to replace object that is not child of this object");
                    }
                }
            } else if (allowedArgs.contains(newType)) {
                if (currentChild == this.thenClause) {
                    currentChild.setParent(null);
                    newChild.setParent(this);
                    setThenClause(newChild, true);
                    result = true;
                } else if (currentChild == this.elseClause) {
                    currentChild.setParent(null);
                    newChild.setParent(this);
                    setElseClause(newChild, true);
                    result = true;
                } else {
                    Log.warning("Attempt to replace object that is not child of this object");
                }
            } else {
                Log.warning("Attempt to add ", newType, " type child as test condition; Boolean is required");
            }
        }

        return result;
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

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_CONDITION_SLOT:
                allowedModifications.add(EModification.TYPE);
                allowedModifications.add(EModification.INSERT_BOOLEAN);
                break;

            case IN_EMPTY_THEN_SLOT:
                allowedModifications.add(EModification.TYPE);
                final EnumSet<EType> thenAllowed = getTypesAllowedForThen();

                if (thenAllowed.contains(EType.BOOLEAN)) {
                    allowedModifications.add(EModification.INSERT_BOOLEAN);
                }
                if (thenAllowed.contains(EType.INTEGER)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                }
                if (thenAllowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                if (thenAllowed.contains(EType.INTEGER_VECTOR)) {
                    allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                }
                if (thenAllowed.contains(EType.REAL_VECTOR)) {
                    allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                }
                if (thenAllowed.contains(EType.STRING)) {
                    allowedModifications.add(EModification.INSERT_STRING);
                }
                if (thenAllowed.contains(EType.SPAN)) {
                    allowedModifications.add(EModification.INSERT_SPAN);
                }
                break;

            case IN_EMPTY_ELSE_SLOT:
                allowedModifications.add(EModification.TYPE);
                final EnumSet<EType> elseAllowed = getTypesAllowedForElse();

                if (elseAllowed.contains(EType.BOOLEAN)) {
                    allowedModifications.add(EModification.INSERT_BOOLEAN);
                }
                if (elseAllowed.contains(EType.INTEGER)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                }
                if (elseAllowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                if (elseAllowed.contains(EType.INTEGER_VECTOR)) {
                    allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                }
                if (elseAllowed.contains(EType.REAL_VECTOR)) {
                    allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                }
                if (elseAllowed.contains(EType.STRING)) {
                    allowedModifications.add(EModification.INSERT_STRING);
                }
                if (elseAllowed.contains(EType.SPAN)) {
                    allowedModifications.add(EModification.INSERT_SPAN);
                }
                break;

            case WITHIN_CONDITION:
                this.condition.indicateValidModifications(fECursor, allowedModifications);
                break;

            case WITHIN_THEN:
                this.thenClause.indicateValidModifications(fECursor, allowedModifications);
                break;

            case WITHIN_ELSE:
                this.elseClause.indicateValidModifications(fECursor, allowedModifications);
                break;

            case OUTSIDE:
            default:
                break;
        }
    }

    /**
     * Processes a typed character.
     *
     * @param fECursor the cursor position and selection range
     * @param ch       the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_CONDITION_SLOT:
                processCharEmptyConditionSlot(fECursor, ch);
                break;

            case IN_EMPTY_THEN_SLOT:
                processCharEmptyThenElseSlot(fECursor, ch, true);
                break;

            case IN_EMPTY_ELSE_SLOT:
                processCharEmptyThenElseSlot(fECursor, ch, false);
                break;

            case WITHIN_CONDITION:
                this.condition.processChar(fECursor, ch);
                break;

            case WITHIN_THEN:
                this.thenClause.processChar(fECursor, ch);
                break;

            case WITHIN_ELSE:
                this.elseClause.processChar(fECursor, ch);
                break;

            case OUTSIDE:
                final int cursorPos = fECursor.cursorPosition - getFirstCursorPosition();
                final int lastPos = getFirstCursorPosition() + getNumCursorSteps();

                if ((int) ch == 0x08 && cursorPos == lastPos) {
                    // Backspace
                    fECursor.cursorPosition = getFirstCursorPosition();
                    getParent().replaceChild(this, null);
                } else if ((int) ch == 0x7f && cursorPos == 0) {
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
    private void processCharEmptyConditionSlot(final FECursor cursor, final char ch) {

        final int fontSize = getFontSize();

        if ((int) ch == '{') {
            ++cursor.cursorPosition;
            final FEVarRef varRef = new FEVarRef(fontSize);
            final EnumSet<EType> varAllowed = varRef.getAllowedTypes();
            varAllowed.clear();
            varAllowed.add(EType.BOOLEAN);
            setCondition(varRef, true);
        } else if ((int) ch == '\u22A4' || (int) ch == '\u22A5') {
            ++cursor.cursorPosition;
            final FEConstantBoolean boolValue = new FEConstantBoolean(fontSize, (int) ch == '\u22A4');
            setCondition(boolValue, true);
        } else if ((int) ch == '(') {
            ++cursor.cursorPosition;
            final FEGrouping grouping = new FEGrouping(fontSize);
            final EnumSet<EType> groupingAllowed = grouping.getAllowedTypes();
            groupingAllowed.clear();
            groupingAllowed.add(EType.BOOLEAN);
            setCondition(grouping, true);
        } else if ((int) ch >= '\u2720' && (int) ch <= '\u274F') {
            final EFunction function1 = EFunction.forChar(ch);
            if (function1 == EFunction.NOT) {
                ++cursor.cursorPosition;
                final FEFunction function = new FEFunction(fontSize, EFunction.NOT);
                setCondition(function, true);
            }
        } else if ((int) ch == '<') {
            ++cursor.cursorPosition;
            final FETest test = new FETest(fontSize);
            final EnumSet<EType> testAllowed = test.getAllowedTypes();
            testAllowed.clear();
            testAllowed.add(EType.BOOLEAN);
            setCondition(test, true);
        } else if ((int) ch == '*') {
            ++cursor.cursorPosition;
            final FEConstantError error = new FEConstantError(fontSize);
            setCondition(error, true);
        }
    }

    /**
     * Processes a typed character when the cursor is in the "Then" or "Else" clause slot and there is no clause
     * present.
     *
     * @param cursor the cursor position and selection range
     * @param ch     the character typed
     * @param isThen true for "then", false for "else"
     */
    private void processCharEmptyThenElseSlot(final FECursor cursor, final char ch, final boolean isThen) {

        final EnumSet<EType> actualAllowed =
                isThen ? getTypesAllowedForThen() : getTypesAllowedForElse();

        AbstractFEObject newObject = null;

        final int fontSize = getFontSize();

        if ((int) ch >= '0' && (int) ch <= '9') {
            if (actualAllowed.contains(EType.REAL) || actualAllowed.contains(EType.INTEGER)) {
                ++cursor.cursorPosition;
                final FEConstantInteger constInt = new FEConstantInteger(fontSize);
                final String txt = Character.toString(ch);
                constInt.setText(txt, false);
                newObject = constInt;
            }
        } else if ((int) ch == '\u03c0' || (int) ch == '\u0435' || (int) ch == '.') {
            if (actualAllowed.contains(EType.REAL)) {
                ++cursor.cursorPosition;
                final FEConstantReal constReal = new FEConstantReal(fontSize);
                final String txt = Character.toString(ch);
                constReal.setText(txt, false);
                newObject = constReal;
            }
        } else if ((int) ch == '+' || (int) ch == '-') {
            if (actualAllowed.contains(EType.REAL) || actualAllowed.contains(EType.INTEGER)) {
                ++cursor.cursorPosition;
                newObject = new FEUnaryOper(fontSize, (int) ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
            }
        } else if ((int) ch == '{') {
            ++cursor.cursorPosition;
            final FEVarRef varRef = new FEVarRef(fontSize);
            final EnumSet<EType> varAllowed = varRef.getAllowedTypes();
            varAllowed.clear();
            varAllowed.addAll(actualAllowed);
            newObject = varRef;
        } else if ((int) ch == '"') {
            if (actualAllowed.contains(EType.SPAN)) {
                ++cursor.cursorPosition;
                newObject = new FEConstantSpan(fontSize);
            }
        } else if ((int) ch == '\u22A4' || (int) ch == '\u22A5') {
            if (actualAllowed.contains(EType.BOOLEAN)) {
                ++cursor.cursorPosition;
                newObject = new FEConstantBoolean(fontSize, (int) ch == '\u22A4');
            }
        } else if ((int) ch == '[') {
            if (actualAllowed.contains(EType.REAL_VECTOR) || actualAllowed.contains(EType.INTEGER_VECTOR)) {
                ++cursor.cursorPosition;
                final FEVector vec = new FEVector(fontSize);
                if (!actualAllowed.contains(EType.REAL_VECTOR)) {
                    vec.getAllowedTypes().remove(EType.REAL_VECTOR);
                }
                newObject = vec;
            }
        } else if ((int) ch == '(') {
            ++cursor.cursorPosition;
            final FEGrouping grouping = new FEGrouping(fontSize);
            final EnumSet<EType> groupingAllowed = grouping.getAllowedTypes();
            groupingAllowed.clear();
            groupingAllowed.addAll(actualAllowed);
            newObject = grouping;
        } else if ((int) ch >= '\u2720' && (int) ch <= '\u274F') {
            final EFunction function = EFunction.forChar(ch);
            if (function != null) {
                ++cursor.cursorPosition;
                newObject = new FEFunction(fontSize, function);
            }
        } else if ((int) ch == '<') {
            ++cursor.cursorPosition;
            final FETest test = new FETest(fontSize);
            final EnumSet<EType> testAllowed = test.getAllowedTypes();
            testAllowed.clear();
            testAllowed.addAll(actualAllowed);
            newObject = test;
        } else if ((int) ch == '*') {
            ++cursor.cursorPosition;
            newObject = new FEConstantError(fontSize);
        }

        if (newObject != null) {
            if (isThen) {
                setThenClause(newObject, true);
            } else {
                setElseClause(newObject, true);
            }
        }
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

        String error = null;

        final EType newType = toInsert.getCurrentType();

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_CONDITION_SLOT:
                if (newType == EType.BOOLEAN) {
                    setCondition(toInsert, true);
                } else if (newType == null) {
                    final EnumSet<EType> newAllowed = toInsert.getAllowedTypes();
                    if (newAllowed.contains(EType.BOOLEAN)) {
                        setCondition(toInsert, true);
                        newAllowed.clear();
                        newAllowed.add(EType.BOOLEAN);
                    }
                } else {
                    error = "Condition must have Boolean type";
                }
                break;

            case IN_EMPTY_THEN_SLOT:
                final EnumSet<EType> thenAllowed = getTypesAllowedForThen();
                if (newType == null) {
                    final EnumSet<EType> newAllowed = toInsert.getAllowedTypes();
                    final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

                    for (final EType test : newAllowed) {
                        if (thenAllowed.contains(test)) {
                            filtered.add(test);
                        }
                    }

                    if (!filtered.isEmpty()) {
                        newAllowed.clear();
                        newAllowed.addAll(filtered);
                        setThenClause(toInsert, true);
                    }
                } else if (thenAllowed.contains(newType)) {
                    setThenClause(toInsert, true);
                } else {
                    error = "Invalid type for 'Then' clause";
                }
                break;

            case IN_EMPTY_ELSE_SLOT:
                final EnumSet<EType> elseAllowed = getTypesAllowedForElse();
                if (newType == null) {
                    final EnumSet<EType> newAllowed = toInsert.getAllowedTypes();
                    final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

                    for (final EType test : newAllowed) {
                        if (elseAllowed.contains(test)) {
                            filtered.add(test);
                        }
                    }

                    if (!filtered.isEmpty()) {
                        newAllowed.clear();
                        newAllowed.addAll(filtered);
                        setElseClause(toInsert, true);
                    }
                } else if (elseAllowed.contains(newType)) {
                    setElseClause(toInsert, true);
                } else {
                    error = "Invalid type for 'Else' clause";
                }
                break;

            case WITHIN_CONDITION:
                this.condition.processInsert(fECursor, toInsert);
                break;

            case WITHIN_THEN:
                this.thenClause.processInsert(fECursor, toInsert);
                break;

            case WITHIN_ELSE:
                this.elseClause.processInsert(fECursor, toInsert);
                break;

            case OUTSIDE:
            default:
                break;
        }

        return error;
    }

    /**
     * Gets the types allowed in the "Then" clause slot. This will be this object's allowed types if there is on "Else"
     * clause present, or that list filtered to types compatible with the "Else" clause otherwise.
     *
     * @return the allowed "Then" types
     */
    private EnumSet<EType> getTypesAllowedForThen() {

        final EnumSet<EType> result = EnumSet.noneOf(EType.class);
        final EnumSet<EType> allowed = getAllowedTypes();

        if (this.elseClause == null) {
            result.addAll(allowed);
        } else {
            final EType elseType = this.elseClause.getCurrentType();

            if (elseType == null) {
                final EnumSet<EType> otherAllowed = this.elseClause.getAllowedTypes();
                for (final EType test : allowed) {
                    if (otherAllowed.contains(test)) {
                        result.add(test);
                    }
                }
            } else if (elseType == EType.INTEGER) {
                if (allowed.contains(EType.REAL)) {
                    result.add(EType.INTEGER);
                    result.add(EType.REAL);
                }
            } else {
                result.add(elseType);
            }
        }

        return result;
    }

    /**
     * Gets the types allowed in the "Else" clause slot. This will be this object's allowed types if there is on "Then"
     * clause present, or that list filtered to types compatible with the "Then" clause otherwise.
     *
     * @return the allowed "Else" types
     */
    private EnumSet<EType> getTypesAllowedForElse() {

        final EnumSet<EType> result = EnumSet.noneOf(EType.class);
        final EnumSet<EType> allowed = getAllowedTypes();

        if (this.thenClause == null) {
            result.addAll(allowed);
        } else {
            final EType thenType = this.thenClause.getCurrentType();

            if (thenType == null) {
                final EnumSet<EType> otherAllowed = this.thenClause.getAllowedTypes();
                for (final EType test : allowed) {
                    if (otherAllowed.contains(test)) {
                        result.add(test);
                    }
                }
            } else if (thenType == EType.INTEGER) {
                if (allowed.contains(EType.REAL)) {
                    result.add(EType.INTEGER);
                    result.add(EType.REAL);
                }
            } else {
                result.add(thenType);
            }
        }

        return result;
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

        this.ifBox = new RenderedBox("IF (");
        this.ifBox.useSans();
        this.ifBox.setFontSize(fontSize);
        this.ifBox.layout(g2d);

        this.thenBox = new RenderedBox(") THEN (");
        this.thenBox.useSans();
        this.thenBox.setFontSize(fontSize);
        this.thenBox.layout(g2d);

        this.elseBox = new RenderedBox(") ELSE (");
        this.elseBox.useSans();
        this.elseBox.setFontSize(fontSize);
        this.elseBox.layout(g2d);

        this.endifBox = new RenderedBox(")");
        this.endifBox.useSans();
        this.endifBox.setFontSize(fontSize);
        this.endifBox.layout(g2d);

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.ifBox.getAdvance();

        final Rectangle ifBounds = this.ifBox.getBounds();
        final Rectangle thenBounds = this.thenBox.getBounds();
        final Rectangle elseBounds = this.elseBox.getBounds();
        final int minY = Math.min(ifBounds.y, thenBounds.y);

        int topY = Math.min(minY, elseBounds.y);
        int botY = 0;

        if (this.condition != null) {
            this.condition.layout(g2d);
            this.condition.translate(x, 0);
            x += this.condition.getAdvance();

            final Rectangle conditionBounds = this.condition.getBounds();
            topY = Math.min(topY, conditionBounds.y);
            botY = Math.max(botY, conditionBounds.y + conditionBounds.height);
        }

        this.thenBox.translate(x, 0);
        x += this.thenBox.getAdvance();

        if (this.thenClause != null) {
            this.thenClause.layout(g2d);
            this.thenClause.translate(x, 0);
            x += this.thenClause.getAdvance();

            topY = Math.min(topY, thenBounds.y);
            botY = Math.max(botY, thenBounds.y + thenBounds.height);
        }

        this.elseBox.translate(x, 0);
        x += this.elseBox.getAdvance();

        if (this.elseClause != null) {
            this.elseClause.layout(g2d);
            this.elseClause.translate(x, 0);
            x += this.elseClause.getAdvance();

            topY = Math.min(topY, elseBounds.y);
            botY = Math.max(botY, elseBounds.y + elseBounds.height);
        }

        this.endifBox.translate(x, 0);
        x += this.endifBox.getAdvance();

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

        if (this.ifBox != null) {
            this.ifBox.translate(dx, dy);
        }
        if (this.condition != null) {
            this.condition.translate(dx, dy);
        }
        if (this.thenBox != null) {
            this.thenBox.translate(dx, dy);
        }
        if (this.thenClause != null) {
            this.thenClause.translate(dx, dy);
        }
        if (this.elseBox != null) {
            this.elseBox.translate(dx, dy);
        }
        if (this.elseClause != null) {
            this.elseClause.translate(dx, dy);
        }
        if (this.endifBox != null) {
            this.endifBox.translate(dx, dy);
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

        if (this.ifBox != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.ifBox.render(g2d, selected);
        }

        pos += 1 + (this.condition == null ? 0 : this.condition.getNumCursorSteps());

        if (this.thenBox != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.thenBox.render(g2d, selected);
        }

        pos += 1 + (this.thenClause == null ? 0 : this.thenClause.getNumCursorSteps());

        if (this.elseBox != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.elseBox.render(g2d, selected);
        }

        pos += 1 + (this.elseClause == null ? 0 : this.elseClause.getNumCursorSteps());

        if (this.endifBox != null) {
            final boolean selected = cursor.doesSelectionInclude(pos);
            this.endifBox.render(g2d, selected);
        }

        if (this.condition != null) {
            this.condition.render(g2d, cursor);
        }

        if (this.thenClause != null) {
            this.thenClause.render(g2d, cursor);
        }

        if (this.elseClause != null) {
            this.elseClause.render(g2d, cursor);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.ifBox != null) {
            target.add(this.ifBox);
        }

        if (this.condition != null) {
            this.condition.gatherRenderedBoxes(target);
        }

        if (this.thenBox != null) {
            target.add(this.thenBox);
        }

        if (this.thenClause != null) {
            this.thenClause.gatherRenderedBoxes(target);
        }

        if (this.elseBox != null) {
            target.add(this.elseBox);
        }

        if (this.elseClause != null) {
            this.elseClause.gatherRenderedBoxes(target);
        }

        if (this.endifBox != null) {
            target.add(this.endifBox);
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
        builder.addln((parent == null ? "IF* {" : "IF {"));

        if (this.condition == null) {
            indent(builder, indent + 1);
            builder.addln("(No test condition)");
        } else {
            this.condition.emitDiagnostics(builder, indent + 1);
        }

        indent(builder, indent);
        builder.addln("} THEN {");

        if (this.thenClause == null) {
            indent(builder, indent + 1);
            builder.addln("(No 'Then' clause)");
        } else {
            this.thenClause.emitDiagnostics(builder, indent + 1);
        }

        indent(builder, indent);
        builder.addln("} ELSE {");

        if (this.elseClause == null) {
            indent(builder, indent + 1);
            builder.addln("(No 'Else' clause)");
        } else {
            this.elseClause.emitDiagnostics(builder, indent + 1);
        }

        indent(builder, indent);
        builder.addln("}");
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FETest duplicate() {

        final int fontSize = getFontSize();
        final FETest dup = new FETest(fontSize);

        dup.getAllowedTypes().clear();

        final EnumSet<EType> allowedTypes = getAllowedTypes();
        dup.getAllowedTypes().addAll(allowedTypes);

        final EType currentType = getCurrentType();
        dup.setCurrentType(currentType);

        if (this.condition != null) {
            final AbstractFEObject conditionDup = this.condition.duplicate();
            dup.setCondition(conditionDup, false);
        }
        if (this.thenClause != null) {
            final AbstractFEObject thenDup = this.thenClause.duplicate();
            dup.setThenClause(thenDup, false);
        }
        if (this.elseClause != null) {
            final AbstractFEObject elseDup = this.elseClause.duplicate();
            dup.setElseClause(elseDup, false);
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
        final int conditionEnd = myStart + 1 + (this.condition == null ? 0 : this.condition.getNumCursorSteps());
        final int thenEnd = conditionEnd + 1 + (this.thenClause == null ? 0 : this.thenClause.getNumCursorSteps());
        final int elseEnd = thenEnd + 1 + (this.elseClause == null ? 0 : this.elseClause.getNumCursorSteps());

        if (cursorPos < myStart + 1) {
            result = CursorPosition.OUTSIDE;
        } else if (cursorPos <= conditionEnd) {
            if (this.condition == null) {
                result = CursorPosition.IN_EMPTY_CONDITION_SLOT;
            } else {
                result = CursorPosition.WITHIN_CONDITION;
            }
        } else if (cursorPos <= thenEnd) {
            // At start of condition
            if (this.thenClause == null) {
                result = CursorPosition.IN_EMPTY_THEN_SLOT;
            } else {
                result = CursorPosition.WITHIN_THEN;
            }
        } else if (cursorPos <= elseEnd) {
            // At start of condition
            if (this.elseClause == null) {
                result = CursorPosition.IN_EMPTY_ELSE_SLOT;
            } else {
                result = CursorPosition.WITHIN_ELSE;
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

        /** Condition is not present, cursor is at the condition insertion point. */
        IN_EMPTY_CONDITION_SLOT,

        /** Condition is present, cursor is within condition. */
        WITHIN_CONDITION,

        /** Then clause is not present, cursor is at the then clause insertion point. */
        IN_EMPTY_THEN_SLOT,

        /** Then clause is present, cursor is within then clause. */
        WITHIN_THEN,

        /** Else clause is not present, cursor is at the else clause insertion point. */
        IN_EMPTY_ELSE_SLOT,

        /** Else clause is present, cursor is within else clause. */
        WITHIN_ELSE,

        /** Cursor falls outside the object. */
        OUTSIDE
    }
}
