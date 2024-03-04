package dev.mathops.assessment.expression.editmodel;

import dev.mathops.assessment.formula.EFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * An expression (or sub-expression) modeled as a sequence of expression objects.
 */
public final class Expr extends AbstractExprBranch {

    /** The list of objects in the sequence. */
    private final List<AbstractExprObject> children;

    /**
     * Constructs a new {@code Expr}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    Expr(final AbstractExprObject theParent) {

        super(theParent);

        this.children = new ArrayList<>(10);
    }

    /**
     * Called when something potentially changes the number of cursor positions in a child.  This method recalculates
     * the starting cursor positions for each child of this object, and if this results in a change to this object's
     * cursor position count, the call is  propagated upward to the parent (if any).
     *
     * @param theFirstCursorPosition the new first cursor position
     */
    void recalculate(final int theFirstCursorPosition) {

        final int origCount = getNumCursorPositions();

        innerSetFirstCursorPosition(theFirstCursorPosition);

        int pos = 0;
        for (final AbstractExprObject obj : this.children) {
            obj.innerSetFirstCursorPosition(pos);
            pos += obj.getNumCursorPositions();
        }

        if (pos != origCount) {
            innerSetNumCursorPositions(pos);
            if (getParent() instanceof final AbstractExprBranch parentBranch) {
                final int parentFirst = parentBranch.getFirstCursorPosition();
                parentBranch.recalculate(parentFirst);
            }
        }
    }

    /**
     * Processes an action represented by an integer.  If the action code is 0xFFFF or smaller, it is interpreted
     * as a Unicode character;  otherwise, it is interpreted as an enumerated code.
     *
     * <p>
     * If there is a selection and an action is performed that would result in the deletion of that selection, the
     * deletion is done before this method is called.  Actions on objects are called only when there is no selection
     * region.  Actions like CUT/COPY/PASTE/DELETE (and undo/redo) are handled by other mechanisms.
     *
     * @param action the action code
     * @param cursorPosition the cursor position
     */
    void processAction(final int action, final int cursorPosition) {

        // See if the action should be forwarded to a child, and at the same time, find the index of the first child
        // that starts at or after the cursor position.
        int beforeIndex = -1;

        final int count = this.children.size();
        for (int i = 0; i < count; ++i) {
            final AbstractExprObject child = this.children.get(i);
            final int childStart = child.getFirstCursorPosition();

            if (childStart >= cursorPosition) {
                beforeIndex = i;
                break;
            }

            if (child instanceof final AbstractExprBranch branch) {
                final int childEnd = childStart + child.getNumCursorPositions();
                if (childEnd - 1 > cursorPosition) {
                    branch.processAction(action, cursorPosition);
                    break;
                }
            }
        }

        if (beforeIndex >= 0) {

            AbstractExprObject toAdd = null;

            if (action < 0x00010000) {
                // A character...

                if (action == (int) '.') {
                    toAdd = new ExprLeafDelimiter(this, EDelimiter.DECIMAL_POINT);
                } else if (action == (int) '(') {
                    toAdd = new ExprLeafDelimiter(this, EDelimiter.LEFT_PAREN);
                } else if (action == (int) ')') {
                    toAdd = new ExprLeafDelimiter(this, EDelimiter.RIGHT_PAREN);
                } else if (action == (int) '[') {
                    toAdd = new ExprLeafDelimiter(this, EDelimiter.LEFT_BRACKET);
                } else if (action == (int) ']') {
                    toAdd = new ExprLeafDelimiter(this, EDelimiter.RIGHT_BRACKET);
                } else if (action == (int) '\"') {
                    toAdd = new ExprBranchString(this);
                } else if (action == (int) '+') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.PLUS);
                } else if (action == (int) '-') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.MINUS);
                } else if (action == (int) '*') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.TIMES);
                } else if (action == (int) '/') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.DIVIDED_BY);
                } else if (action == (int) '%') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.REMAINDER);
                } else if (action == (int) '|') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.OR);
                } else if (action == (int) '&') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.AND);
                } else if (action == (int) '!') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.NOT);
                } else if (action == (int) '=') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.EQUALS);
                } else if (action == (int) '<') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.LESS_THAN);
                } else if (action == (int) '>') {
                    toAdd = new ExprLeafOperator(this, EOperatorSymbol.GREATER_THAN);
                } else if (action >= (int) '0' && action <= (int) '9') {
                    toAdd = new ExprLeafDigit(this, action - (int) '0');
                } else if (action == (int) '\u03C0') {
                    toAdd = new ExprLeafSymbolicConstant(this, ESymbolicConstant.PI);
                } else if (action == (int) '\u2147') {
                    toAdd = new ExprLeafSymbolicConstant(this, ESymbolicConstant.E);
                } else if (action == (int) '\u2148') {
                    toAdd = new ExprLeafSymbolicConstant(this, ESymbolicConstant.I);
                } else {
                    final char character = (char)action;

                    if (Character.isLetter(character)) {
                        toAdd = new ExprLeafLetter(this, character);
                    }
                }
            } else if (action < EExprAction.INSERT_FXN_ABS.value) {
                if (action == EExprAction.INSERT_BOOLEAN_TRUE.value) {
                    toAdd = new ExprLeafBoolean(this, true);
                } else if (action == EExprAction.INSERT_BOOLEAN_FALSE.value) {
                    toAdd = new ExprLeafBoolean(this, false);
                } else if (action == EExprAction.INSERT_ENGINEERING_E.value) {
                    toAdd = new ExprLeafEngineeringE(this);
                } else if (action == EExprAction.INSERT_VECTOR_PARENS.value) {
                    toAdd = new ExprBranchVector(this, EVectorMatrixBrackets.PARENTHESES, 1);
                } else if (action == EExprAction.INSERT_VECTOR_BRACKETS.value) {
                    toAdd = new ExprBranchVector(this, EVectorMatrixBrackets.SQUARE, 1);
                } else if (action == EExprAction.INSERT_MATRIX_PARENS.value) {
                    toAdd = new ExprBranchMatrix(this, EVectorMatrixBrackets.PARENTHESES, 1, 1);
                } else if (action == EExprAction.INSERT_MATRIX_BRACKETS.value) {
                    toAdd = new ExprBranchMatrix(this, EVectorMatrixBrackets.SQUARE, 1, 1);
                } else if (action == EExprAction.INSERT_FRACTION_VERTICAL.value) {
                    toAdd = new ExprBranchFraction(this, EFractionShape.VERTICAL);
                } else if (action == EExprAction.INSERT_FRACTION_SLANT.value) {
                    toAdd = new ExprBranchFraction(this, EFractionShape.SLANT);
                } else if (action == EExprAction.INSERT_FRACTION_HORIZONTAL.value) {
                    toAdd = new ExprBranchFraction(this, EFractionShape.HORIZONTAL);
                } else if (action == EExprAction.INSERT_RADICAL.value) {
                    toAdd = new ExprBranchRadical(this);
                } else if (action == EExprAction.INSERT_RADICAL_WITH_ROOT.value) {
                    toAdd = new ExprBranchRadicalWithRoot(this);
                }
            } else {
                if (action == EExprAction.INSERT_FXN_ABS.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.ABS);
                } else if (action == EExprAction.INSERT_FXN_COS.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.COS);
                } else if (action == EExprAction.INSERT_FXN_SIN.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.SIN);
                } else if (action == EExprAction.INSERT_FXN_TAN.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.TAN);
                } else if (action == EExprAction.INSERT_FXN_SEC.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.SEC);
                } else if (action == EExprAction.INSERT_FXN_CSC.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.CSC);
                } else if (action == EExprAction.INSERT_FXN_COT.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.COT);
                } else if (action == EExprAction.INSERT_FXN_ACOS.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.ACOS);
                } else if (action == EExprAction.INSERT_FXN_ASIN.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.ASIN);
                } else if (action == EExprAction.INSERT_FXN_ATAN.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.ATAN);
                } else if (action == EExprAction.INSERT_FXN_EXP.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.EXP);
                } else if (action == EExprAction.INSERT_FXN_LOG.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.LOG);
                } else if (action == EExprAction.INSERT_FXN_CEIL.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.CEIL);
                } else if (action == EExprAction.INSERT_FXN_FLOOR.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.FLOOR);
                } else if (action == EExprAction.INSERT_FXN_ROUND.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.ROUND);
                } else if (action == EExprAction.INSERT_FXN_SQRT.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.SQRT);
                } else if (action == EExprAction.INSERT_FXN_CBRT.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.CBRT);
                } else if (action == EExprAction.INSERT_FXN_TO_DEG.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.TO_DEG);
                } else if (action == EExprAction.INSERT_FXN_TO_RAD.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.TO_RAD);
                } else if (action == EExprAction.INSERT_FXN_NOT.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.NOT);
                } else if (action == EExprAction.INSERT_FXN_GCD.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.GCD);
                } else if (action == EExprAction.INSERT_FXN_LCM.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.LCM);
                } else if (action == EExprAction.INSERT_FXN_SRAD2.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.SRAD2);
                } else if (action == EExprAction.INSERT_FXN_SRAD3.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.SRAD3);
                } else if (action == EExprAction.INSERT_FXN_LCASE.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.LCASE);
                } else if (action == EExprAction.INSERT_FXN_UCASE.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.UCASE);
                } else if (action == EExprAction.INSERT_FXN_RAD_NUM.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.RAD_NUM);
                } else if (action == EExprAction.INSERT_FXN_RAD_DEN.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.RAD_DEN);
                } else if (action == EExprAction.INSERT_FXN_APPROX_EQUAL.value) {
                    toAdd = new ExprBranchFunction(this, EFunction.APPROX);
                }
            }

            if (toAdd != null) {
                this.children.add(beforeIndex, toAdd);
                final int firstPos = getFirstCursorPosition();
                recalculate(firstPos);
            }
        }
    }
}
