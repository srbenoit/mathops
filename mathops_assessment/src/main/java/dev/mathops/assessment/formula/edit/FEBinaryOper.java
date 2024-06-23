package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.BinaryOper;
import dev.mathops.assessment.formula.EBinaryOp;
import dev.mathops.assessment.formula.EFunction;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for two Real-valued objects that can generate a {@code BinaryOper}.
 */
public final class FEBinaryOper extends AbstractFEObject {

    /** The first argument. */
    private AbstractFEObject arg1 = null;

    /** The operator. */
    private final EBinaryOp op;

    /** The operator box. */
    private RenderedBox opBox = null;

    /** The second argument. */
    private AbstractFEObject arg2 = null;

    /**
     * Constructs a new {@code FEBinaryOper} and sets the default set of allowed types. The set of allowed types can be
     * restricted from this default by calling {@code getAllowedTypes} and removing disallowed types from the returned
     * set. Types should not be added to the resulting set since it already includes the set of possible types generated
     * by the operation.
     *
     * @param theFontSize the font size for the component
     * @param theOp       the operator
     */
    public FEBinaryOper(final int theFontSize, final EBinaryOp theOp) {

        super(theFontSize);

        if (theOp == null) {
            throw new IllegalArgumentException("Operator may not be null");
        }

        this.op = theOp;

        final EnumSet<EType> allowed = getAllowedTypes();
        final EnumSet<EType> possible = getPossibleTypes();

        switch (theOp) {
            case ADD, SUBTRACT:
                allowed.add(EType.INTEGER);
                allowed.add(EType.REAL);
                allowed.add(EType.INTEGER_VECTOR);
                allowed.add(EType.REAL_VECTOR);
                possible.add(EType.INTEGER);
                possible.add(EType.REAL);
                possible.add(EType.INTEGER_VECTOR);
                possible.add(EType.REAL_VECTOR);
                break;

            case EQ, APPROX, NE, GT, GE, LT, LE, AND, OR:
                allowed.add(EType.BOOLEAN);
                possible.add(EType.BOOLEAN);
                break;

            case DIVIDE:
                allowed.add(EType.REAL);
                possible.add(EType.REAL);
                break;

            case MULTIPLY, POWER:
                allowed.add(EType.INTEGER);
                allowed.add(EType.REAL);
                possible.add(EType.INTEGER);
                possible.add(EType.REAL);
                break;

            case REMAINDER:
                allowed.add(EType.INTEGER);
                possible.add(EType.INTEGER);
                break;

            default:
                break;
        }
    }

    /**
     * Determines the argument types allowed based on the operator.
     *
     * @return the allowed argument types
     */
    private EnumSet<EType> getAllowedArgumentTypes() {

        final EnumSet<EType> allowed = EnumSet.noneOf(EType.class);

        switch (this.op) {
            case ADD, SUBTRACT:
                allowed.add(EType.INTEGER);
                allowed.add(EType.REAL);
                allowed.add(EType.INTEGER_VECTOR);
                allowed.add(EType.REAL_VECTOR);
                break;

            case EQ, NE:
                allowed.add(EType.BOOLEAN);
                allowed.add(EType.INTEGER);
                allowed.add(EType.REAL);
                allowed.add(EType.INTEGER_VECTOR);
                allowed.add(EType.REAL_VECTOR);
                allowed.add(EType.STRING);
                break;

            case AND, OR:
                allowed.add(EType.BOOLEAN);
                break;

            case APPROX, GT, GE, LT, LE, DIVIDE, MULTIPLY, POWER:
                allowed.add(EType.INTEGER);
                allowed.add(EType.REAL);
                break;

            case REMAINDER:
                allowed.add(EType.INTEGER);
                break;

            default:
                break;
        }

        return allowed;
    }

    /**
     * Sets the first argument.
     *
     * @param newArg1   the new first argument
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
            final EnumSet<EType> allowed = getAllowedArgumentTypes();
            final EType childType = newArg1.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> possibleArg1 = newArg1.getPossibleTypes();
                final EnumSet<EType> filtered = EType.filter(allowed, possibleArg1);
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
            } else {
                Log.warning("Arg 1 (", childType, ") not an allowed type for '", this.op, "'");

                final HtmlBuilder diag = new HtmlBuilder(150);
                newArg1.emitDiagnostics(diag, 0);
                final String diagStr = diag.toString();
                Log.warning(diagStr);

                AbstractFEObject par = newArg1.getParent();
                while (par != null) {
                    final String simpleName = par.getClass().getSimpleName();
                    Log.warning("    Parent is ", simpleName);
                    par = par.getParent();
                }
            }
        }
    }

    /**
     * Gets the first argument.
     *
     * @return the first argument
     */
    public AbstractFEObject getArg1() {

        return this.arg1;
    }

    /**
     * Sets the second argument.
     *
     * @param newArg2   the new second argument
     * @param storeUndo true to store an undo state; false to skip
     */
    public void setArg2(final AbstractFEObject newArg2, final boolean storeUndo) {

        if (newArg2 == null) {
            if (this.arg2 != null) {
                this.arg2.setParent(null);
            }
            this.arg2 = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EnumSet<EType> allowed = getAllowedArgumentTypes();
            final EType childType = newArg2.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> possibleArg2 = newArg2.getPossibleTypes();
                final EnumSet<EType> filtered = EType.filter(allowed, possibleArg2);
                isAllowed = !filtered.isEmpty();
            } else {
                isAllowed = allowed.contains(childType);
            }

            if (isAllowed) {
                if (this.arg2 != null) {
                    this.arg2.setParent(null);
                }
                this.arg2 = newArg2;
                newArg2.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            } else {
                Log.warning("Arg 2 (", childType, ") not an allowed type for '", this.op, "'");

                final HtmlBuilder diag = new HtmlBuilder(150);
                newArg2.emitDiagnostics(diag, 0);
                final String diagStr = diag.toString();
                Log.warning(diagStr);

                AbstractFEObject par = newArg2.getParent();
                while (par != null) {
                    final String simpleName = par.getClass().getSimpleName();
                    Log.warning("    Parent is ", simpleName);
                    par = par.getParent();
                }
            }
        }
    }

    /**
     * Gets the second argument.
     *
     * @return the second argument
     */
    public AbstractFEObject getArg2() {

        return this.arg2;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. This is the steps in the first
     * argument, one step for the "+", then the steps in the second argument.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        int steps = 1;

        if (this.arg1 != null) {
            steps += this.arg1.getNumCursorSteps();
        }
        if (this.arg2 != null) {
            steps += this.arg2.getNumCursorSteps();
        }

        return steps;
    }

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if valid (a formula can be generated); false if not
     */
    @Override
    public boolean isValid() {

        return this.arg1 != null && this.arg2 != null && this.arg1.isValid() && this.arg2.isValid();
    }

    /**
     * Generates a {@code BinaryOper} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public AbstractFormulaObject generate() {

        BinaryOper result = null;

        if (this.arg1 != null && this.arg2 != null) {
            final AbstractFormulaObject arg1Obj = this.arg1.generate();
            final AbstractFormulaObject arg2Obj = this.arg2.generate();
            if (arg1Obj != null && arg2Obj != null) {
                result = new BinaryOper(this.op);
                result.addChild(arg1Obj);
                result.addChild(arg2Obj);
            }
        }

        return result;
    }

    /** Recomputes the current type based on arguments. */
    @Override
    public void recomputeCurrentType() {

        final EType oldType = getCurrentType();
        EType newType = null;
        final EType arg1Type = this.arg1 == null ? null : this.arg1.getCurrentType();
        final EType arg2Type = this.arg2 == null ? null : this.arg2.getCurrentType();

        final EnumSet<EType> possible = getPossibleTypes();
        possible.clear();

        final boolean bothInteger = arg1Type == EType.INTEGER && arg2Type == EType.INTEGER;
        final boolean bothNumbers = (arg1Type == EType.INTEGER || arg1Type == EType.REAL)
                && (arg2Type == EType.INTEGER || arg2Type == EType.REAL);

        switch (this.op) {
            case ADD, SUBTRACT, POWER:
                if (bothInteger) {
                    newType = EType.INTEGER;
                    possible.add(EType.INTEGER);
                } else if (bothNumbers) {
                    newType = EType.REAL;
                    possible.add(EType.REAL);
                } else {
                    possible.add(EType.INTEGER);
                    possible.add(EType.REAL);
                }
                break;

            case MULTIPLY:
                if (bothInteger) {
                    newType = EType.INTEGER;
                    possible.add(EType.INTEGER);
                } else if (bothNumbers) {
                    newType = EType.REAL;
                    possible.add(EType.REAL);
                } else if ((arg1Type == EType.INTEGER && arg2Type == EType.INTEGER_VECTOR)
                        || (arg1Type == EType.INTEGER_VECTOR && arg2Type == EType.INTEGER)) {
                    newType = EType.INTEGER_VECTOR;
                    possible.add(EType.INTEGER_VECTOR);
                } else if ((arg1Type == EType.INTEGER && arg2Type == EType.REAL_VECTOR)
                        || (arg1Type == EType.REAL_VECTOR && arg2Type == EType.INTEGER)
                        || (arg1Type == EType.REAL && arg2Type == EType.INTEGER_VECTOR)
                        || (arg1Type == EType.INTEGER_VECTOR && arg2Type == EType.REAL)
                        || (arg1Type == EType.REAL && arg2Type == EType.REAL_VECTOR)
                        || (arg1Type == EType.REAL_VECTOR && arg2Type == EType.REAL)) {
                    newType = EType.REAL_VECTOR;
                    possible.add(EType.REAL_VECTOR);
                } else {
                    possible.add(EType.INTEGER);
                    possible.add(EType.REAL);
                    possible.add(EType.INTEGER_VECTOR);
                    possible.add(EType.REAL_VECTOR);
                }
                break;

            case EQ, APPROX, NE, GT, GE, LT, LE, AND, OR:
                newType = EType.BOOLEAN;
                possible.add(EType.BOOLEAN);
                break;

            case DIVIDE:
                newType = EType.REAL;
                possible.add(EType.REAL);
                break;

            case REMAINDER:
                newType = EType.INTEGER;
                possible.add(EType.INTEGER);
                break;

            default:
                break;
        }

        if (newType != oldType) {
            setCurrentType(newType);
            final AbstractFEObject parent = getParent();
            if (parent != null) {
                parent.recomputeCurrentType();
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

        int pos = startPos;
        if (this.arg1 != null) {
            this.arg1.recomputeCursorPositions(pos);
            pos += this.arg1.getNumCursorSteps();
        }

        ++pos; // Skip operator position

        if (this.arg2 != null) {
            this.arg2.recomputeCursorPositions(pos);
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

        final boolean result;

        final boolean isArg1 = currentChild == this.arg1;
        final boolean isArg2 = currentChild == this.arg2;

        if (isArg1 || isArg2) {
            if (newChild == null) {
                if (isArg1) {
                    this.arg1 = null;
                } else {
                    this.arg2 = null;
                }
                recomputeCurrentType();
                update(true);
                result = true;
            } else if ((isArg1 && newChild == this.arg2) || (isArg2 && newChild == this.arg1)) {
                Log.warning("Cannot have same object as both arguments to operator");
                result = false;
            } else {
                final EType newType = newChild.getCurrentType();
                final EnumSet<EType> allowed = getAllowedTypes();
                final String opStr = Character.toString(this.op.op);

                switch (this.op) {
                    case ADD:
                    case SUBTRACT:
                        if (allowed.contains(newType)) {
                            if (isArg1) {
                                this.arg1 = null;
                            } else {
                                this.arg2 = null;
                            }
                            result = true;
                            recomputeCurrentType();
                            update(true);
                        } else {
                            Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                            result = false;
                        }
                        break;

                    case EQ:
                    case APPROX:
                    case NE:
                        if (newType == EType.BOOLEAN || newType == EType.INTEGER || newType == EType.REAL
                                || newType == EType.INTEGER_VECTOR || newType == EType.REAL_VECTOR) {
                            if (isArg1) {
                                this.arg1 = null;
                            } else {
                                this.arg2 = null;
                            }
                            recomputeCurrentType();
                            update(true);
                            result = true;
                        } else {
                            Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                            result = false;
                        }
                        break;

                    case GT:
                    case GE:
                    case LT:
                    case LE, DIVIDE:
                        if (newType == EType.INTEGER || newType == EType.REAL) {
                            if (isArg1) {
                                this.arg1 = null;
                            } else {
                                this.arg2 = null;
                            }
                            recomputeCurrentType();
                            update(true);
                            result = true;
                        } else {
                            Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                            result = false;
                        }
                        break;

                    case AND:
                    case OR:
                        if (newType == EType.BOOLEAN) {
                            if (isArg1) {
                                this.arg1 = null;
                            } else {
                                this.arg2 = null;
                            }
                            recomputeCurrentType();
                            update(true);
                            result = true;
                        } else {
                            Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                            result = false;
                        }
                        break;

                    case MULTIPLY, POWER:
                        if (allowed.contains(EType.REAL)) {
                            if (newType == EType.INTEGER || newType == EType.REAL) {
                                if (isArg1) {
                                    this.arg1 = null;
                                } else {
                                    this.arg2 = null;
                                }
                                recomputeCurrentType();
                                update(true);
                                result = true;
                            } else {
                                Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                                result = false;
                            }
                        } else if (newType == EType.INTEGER) {
                            if (isArg1) {
                                this.arg1 = null;
                            } else {
                                this.arg2 = null;
                            }
                            recomputeCurrentType();
                            update(true);
                            result = true;
                        } else {
                            Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                            result = false;
                        }
                        break;

                    case REMAINDER:
                        if (newType == EType.INTEGER) {
                            if (isArg1) {
                                this.arg1 = null;
                            } else {
                                this.arg2 = null;
                            }
                            recomputeCurrentType();
                            update(true);
                            result = true;
                        } else {
                            Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                            result = false;
                        }
                        break;

                    default:
                        Log.warning("Attempt to add argument of type ", newType, " to binary ", opStr);
                        result = false;
                        break;
                }
            }
        } else {
            Log.warning("Attempt to replace child that was not actually a child");
            result = false;
        }

        return result;
    }

    /**
     * Asks the object what modifications are valid for a specified cursor position or selection range.
     *
     * <p>
     * There are 4 scenarios that may allow a modification:
     * <ul>
     * <li>Case (A): First argument is empty and cursor is in that argument's slot
     * <li>Case (B): First argument is present and cursor falls within that argument
     * <li>Case (C): Second argument is empty and cursor is in that argument's slot
     * <li>Case (D): Second argument is present and cursor falls within that argument
     * </ul>
     *
     * @param fECursor               cursor position information
     * @param allowedModifications a set that will be populated with the allowed modifications at the specified
     *                             position
     */
    @Override
    public void indicateValidModifications(final FECursor fECursor,
                                           final EnumSet<EModification> allowedModifications) {

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_ARG1_SLOT:
                indicateValidModificationsEmptyArg1Slot(allowedModifications);
                break;
            case IN_EMPTY_ARG2_SLOT:
                indicateValidModificationsEmptyArg2Slot(allowedModifications);
                break;
            case WITHIN_ARG1:
                this.arg1.indicateValidModifications(fECursor, allowedModifications);
                break;
            case WITHIN_ARG2:
                this.arg2.indicateValidModifications(fECursor, allowedModifications);
                break;
            case OUTSIDE:
            default:
                break;
        }
    }

    /**
     * Indicates the valid modifications in "Case A", where the first argument is not present, and the cursor falls at
     * the first argument's position.
     *
     * @param allowedModifications a set that will be populated with the allowed modifications at the specified
     *                             position
     */
    private void
    indicateValidModificationsEmptyArg1Slot(final Collection<? super EModification> allowedModifications) {

        final EnumSet<EType> allowed = getAllowedTypes();

        switch (this.op) {
            case ADD, SUBTRACT:
                if (allowed.contains(EType.INTEGER)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                }
                if (allowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                break;

            case MULTIPLY:
                if (allowed.contains(EType.INTEGER)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                }
                if (allowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                if (allowed.contains(EType.INTEGER_VECTOR)) {
                    if (this.arg2 == null || this.arg2.getCurrentType() == null) {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                    } else if (this.arg2.getCurrentType() == EType.INTEGER) {
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                    } else {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                    }
                }
                if (allowed.contains(EType.REAL_VECTOR)) {
                    if (this.arg2 == null || this.arg2.getCurrentType() == null) {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                        allowedModifications.add(EModification.INSERT_REAL);
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                        allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                    } else if (this.arg2.getCurrentType() == EType.INTEGER
                            || this.arg2.getCurrentType() == EType.REAL) {
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                        allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                    } else {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                        allowedModifications.add(EModification.INSERT_REAL);
                    }
                }
                break;

            case EQ, NE:
                allowedModifications.add(EModification.INSERT_BOOLEAN);
                allowedModifications.add(EModification.INSERT_INTEGER);
                allowedModifications.add(EModification.INSERT_REAL);
                allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                break;

            case APPROX, GT, GE, LT, LE, DIVIDE:
                allowedModifications.add(EModification.INSERT_INTEGER);
                allowedModifications.add(EModification.INSERT_REAL);
                break;

            case AND, OR:
                allowedModifications.add(EModification.INSERT_BOOLEAN);
                break;

            case POWER:
                allowedModifications.add(EModification.INSERT_INTEGER);
                if (allowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                break;

            case REMAINDER:
                allowedModifications.add(EModification.INSERT_INTEGER);
                break;

            default:
                break;
        }
    }

    /**
     * Indicates the valid modifications in "Case C", where the second argument is not present, and the cursor falls at
     * the second argument's position.
     *
     * @param allowedModifications a set that will be populated with the allowed modifications at the specified
     *                             position
     */
    private void
    indicateValidModificationsEmptyArg2Slot(final Collection<? super EModification> allowedModifications) {

        final EnumSet<EType> allowed = getAllowedTypes();

        switch (this.op) {
            case ADD, SUBTRACT:
                if (allowed.contains(EType.INTEGER)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                }
                if (allowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                break;

            case MULTIPLY:
                if (allowed.contains(EType.INTEGER)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                }
                if (allowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_INTEGER);
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                if (allowed.contains(EType.INTEGER_VECTOR)) {
                    if (this.arg1 == null || this.arg1.getCurrentType() == null) {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                    } else if (this.arg1.getCurrentType() == EType.INTEGER) {
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                    } else {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                    }
                }
                if (allowed.contains(EType.REAL_VECTOR)) {
                    if (this.arg1 == null || this.arg1.getCurrentType() == null) {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                        allowedModifications.add(EModification.INSERT_REAL);
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                        allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                    } else if (this.arg1.getCurrentType() == EType.INTEGER
                            || this.arg1.getCurrentType() == EType.REAL) {
                        allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                        allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                    } else {
                        allowedModifications.add(EModification.INSERT_INTEGER);
                        allowedModifications.add(EModification.INSERT_REAL);
                    }
                }
                break;

            case EQ, NE:
                allowedModifications.add(EModification.INSERT_BOOLEAN);
                allowedModifications.add(EModification.INSERT_INTEGER);
                allowedModifications.add(EModification.INSERT_REAL);
                allowedModifications.add(EModification.INSERT_INTEGER_VECTOR);
                allowedModifications.add(EModification.INSERT_REAL_VECTOR);
                break;

            case APPROX, GT, GE, LT, LE, DIVIDE:
                allowedModifications.add(EModification.INSERT_INTEGER);
                allowedModifications.add(EModification.INSERT_REAL);
                break;

            case AND, OR:
                allowedModifications.add(EModification.INSERT_BOOLEAN);
                break;

            case POWER:
                allowedModifications.add(EModification.INSERT_INTEGER);
                if (allowed.contains(EType.REAL)) {
                    allowedModifications.add(EModification.INSERT_REAL);
                }
                break;

            case REMAINDER:
                allowedModifications.add(EModification.INSERT_INTEGER);
                break;

            default:
                break;
        }
    }

    /**
     * Processes a typed character.
     *
     * <p>
     * There are 4 scenarios:
     * <ul>
     * <li>Case (A): First argument is empty and cursor is in that argument's slot
     * <li>Case (B): First argument is present and cursor falls within that argument
     * <li>Case (C): Second argument is empty and cursor is in that argument's slot
     * <li>Case (D): Second argument is present and cursor falls within that argument
     * </ul>
     *
     * @param fECursor the cursor position and selection range
     * @param ch       the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        final EnumSet<EModification> allowedModifications = EnumSet.noneOf(EModification.class);

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_ARG1_SLOT:
                indicateValidModificationsEmptyArg1Slot(allowedModifications);
                processCharEmptyArg1Slot(fECursor, allowedModifications, ch);
                break;
            case IN_EMPTY_ARG2_SLOT:
                indicateValidModificationsEmptyArg2Slot(allowedModifications);
                processCharEmptyArg2Slot(fECursor, allowedModifications, ch);
                break;
            case WITHIN_ARG1:
                this.arg1.processChar(fECursor, ch);
                break;
            case WITHIN_ARG2:
                this.arg2.processChar(fECursor, ch);
                break;
            case OUTSIDE:
            default:
                break;
        }
    }

    /**
     * Processes a typed character when the cursor is in the first argument slot and there is no first argument
     * present.
     *
     * @param cursor               the cursor position and selection range
     * @param allowedModifications the allowed modifications
     * @param ch                   the character typed
     */
    private void processCharEmptyArg1Slot(final FECursor cursor, final Collection<EModification> allowedModifications,
                                          final char ch) {

        final int fontSize = getFontSize();

        if ((int) ch >= '0' && (int) ch <= '9') {
            final String chStr = Character.toString(ch);

            if (allowedModifications.contains(EModification.INSERT_INTEGER)) {
                ++cursor.cursorPosition;
                final FEConstantInteger constInt = new FEConstantInteger(fontSize);
                constInt.setText(chStr, false);
                setArg1(constInt, true);
            } else if (allowedModifications.contains(EModification.INSERT_REAL)) {
                ++cursor.cursorPosition;
                final FEConstantReal constReal = new FEConstantReal(fontSize);
                constReal.setText(chStr, false);
                setArg1(constReal, true);
            }
        } else if ((int) ch == '.') {
            if (allowedModifications.contains(EModification.INSERT_REAL)) {
                final FEConstantReal constReal = new FEConstantReal(fontSize);
                constReal.setText(".", false);
                setArg1(constReal, true);
            }
        } else if ((int) ch == '+' || (int) ch == '-') {
            if (allowedModifications.contains(EModification.INSERT_INTEGER)
                    || allowedModifications.contains(EModification.INSERT_REAL)) {
                ++cursor.cursorPosition;
                final FEUnaryOper unary = new FEUnaryOper(fontSize, (int) ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
                setArg1(unary, true);
            }
        } else if ((int) ch == '{') {
            ++cursor.cursorPosition;
            final FEVarRef varRef = new FEVarRef(fontSize);
            varRef.getAllowedTypes().clear();
            final EnumSet<EType> allowedTypes = getAllowedTypes();
            varRef.getAllowedTypes().addAll(allowedTypes);
            setArg1(varRef, true);
        } else if ((int) ch == '"') {
            if (allowedModifications.contains(EModification.INSERT_SPAN)) {
                ++cursor.cursorPosition;
                final FEConstantSpan span = new FEConstantSpan(fontSize);
                setArg1(span, true);
            }
        } else if ((int) ch == '\u22A4' || (int) ch == '\u22A5') {
            if (allowedModifications.contains(EModification.INSERT_BOOLEAN)) {
                ++cursor.cursorPosition;
                final FEConstantBoolean boolValue = new FEConstantBoolean(fontSize, (int) ch == '\u22A4');
                setArg1(boolValue, true);
            }
        } else if ((int) ch == '[') {
            if (allowedModifications.contains(EModification.INSERT_INTEGER_VECTOR)
                    || allowedModifications.contains(EModification.INSERT_REAL_VECTOR)) {
                ++cursor.cursorPosition;
                final FEVector vec = new FEVector(fontSize);
                setArg1(vec, true);
            }
        } else if ((int) ch == '(') {
            ++cursor.cursorPosition;
            final FEGrouping grouping = new FEGrouping(fontSize);
            setArg1(grouping, true);
        } else if ((int) ch >= '\u2720' && (int) ch <= '\u274F') {
            final EFunction fxn = EFunction.forChar(ch);
            if (fxn != null) {
                ++cursor.cursorPosition;
                final FEFunction function = new FEFunction(fontSize, fxn);
                setArg1(function, true);
            }
        } else if ((int) ch == '<') {
            ++cursor.cursorPosition;
            final FETest test = new FETest(fontSize);
            setArg1(test, true);
        } else if ((int) ch == '*') {
            ++cursor.cursorPosition;
            final FEConstantError error = new FEConstantError(fontSize);
            setArg2(error, true);
        }
    }

    /**
     * Processes a typed character when the cursor is in the second argument slot and there is no first argument
     * present.
     *
     * @param cursor               the cursor position and selection range
     * @param allowedModifications the allowed modifications
     * @param ch                   the character typed
     */
    private void processCharEmptyArg2Slot(final FECursor cursor, final Collection<EModification> allowedModifications,
                                          final char ch) {

        final int fontSize = getFontSize();

        if ((int) ch >= '0' && (int) ch <= '9') {
            final String chStr = Character.toString(ch);

            if (allowedModifications.contains(EModification.INSERT_INTEGER)) {
                ++cursor.cursorPosition;
                final FEConstantInteger constInt = new FEConstantInteger(fontSize);
                constInt.setText(chStr, false);
                setArg2(constInt, true);
            } else if (allowedModifications.contains(EModification.INSERT_REAL)) {
                ++cursor.cursorPosition;
                final FEConstantReal constReal = new FEConstantReal(fontSize);
                constReal.setText(chStr, false);
                setArg2(constReal, true);
            }
        } else if ((int) ch == '.') {
            if (allowedModifications.contains(EModification.INSERT_REAL)) {
                ++cursor.cursorPosition;
                final FEConstantReal constReal = new FEConstantReal(fontSize);
                constReal.setText(".", false);
                setArg2(constReal, true);
            }
        } else if ((int) ch == '+' || (int) ch == '-') {
            if (allowedModifications.contains(EModification.INSERT_INTEGER)
                    || allowedModifications.contains(EModification.INSERT_REAL)) {
                ++cursor.cursorPosition;
                final FEUnaryOper unary = new FEUnaryOper(fontSize, (int) ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
                setArg2(unary, true);
            }
        } else if ((int) ch == '{') {
            ++cursor.cursorPosition;
            final FEVarRef varRef = new FEVarRef(fontSize);
            varRef.getAllowedTypes().clear();
            final EnumSet<EType> allowedTypes = getAllowedTypes();
            varRef.getAllowedTypes().addAll(allowedTypes);
            setArg2(varRef, true);
        } else if ((int) ch == '"') {
            if (allowedModifications.contains(EModification.INSERT_SPAN)) {
                ++cursor.cursorPosition;
                final FEConstantSpan span = new FEConstantSpan(fontSize);
                setArg2(span, true);
            }
        } else if ((int) ch == '\u22A4' || (int) ch == '\u22A5') {
            if (allowedModifications.contains(EModification.INSERT_BOOLEAN)) {
                ++cursor.cursorPosition;
                final FEConstantBoolean boolValue = new FEConstantBoolean(fontSize, (int) ch == '\u22A4');
                setArg2(boolValue, true);
            }
        } else if ((int) ch == '[') {
            if (allowedModifications.contains(EModification.INSERT_INTEGER_VECTOR)
                    || allowedModifications.contains(EModification.INSERT_REAL_VECTOR)) {
                ++cursor.cursorPosition;
                final FEVector vec = new FEVector(fontSize);
                setArg2(vec, true);
            }
        } else if ((int) ch == '(') {
            ++cursor.cursorPosition;
            final FEGrouping grouping = new FEGrouping(fontSize);
            setArg2(grouping, true);
        } else if ((int) ch >= '\u2720' && (int) ch <= '\u274F') {
            final EFunction fxn = EFunction.forChar(ch);
            if (fxn != null) {
                ++cursor.cursorPosition;
                final FEFunction function = new FEFunction(fontSize, fxn);
                setArg2(function, true);
            }
        } else if ((int) ch == '<') {
            ++cursor.cursorPosition;
            final FETest test = new FETest(fontSize);
            setArg2(test, true);
        } else if ((int) ch == '*') {
            ++cursor.cursorPosition;
            final FEConstantError error = new FEConstantError(fontSize);
            setArg2(error, true);
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

        final String error = null;

        final EnumSet<EModification> allowedModifications = EnumSet.noneOf(EModification.class);

        switch (getCursorPosition(fECursor)) {
            case IN_EMPTY_ARG1_SLOT:
                indicateValidModificationsEmptyArg1Slot(allowedModifications);
                if (isInsertAllowed(allowedModifications, toInsert)) {
                    setArg1(toInsert, true);
                }
                break;
            case IN_EMPTY_ARG2_SLOT:
                indicateValidModificationsEmptyArg2Slot(allowedModifications);
                if (isInsertAllowed(allowedModifications, toInsert)) {
                    setArg2(toInsert, true);
                }
                break;
            case WITHIN_ARG1:
                this.arg1.processInsert(fECursor, toInsert);
                break;
            case WITHIN_ARG2:
                this.arg2.processInsert(fECursor, toInsert);
                break;
            case OUTSIDE:
            default:
                break;
        }

        return error;
    }

    /**
     * Tests whether a specific object is compatible with a set of allowed inserts.
     *
     * @param allowedModifications the allowed modifications
     * @param toInsert             the object requesting to be inserted
     * @return true if the object is allowed to be inserted
     */
    private static boolean isInsertAllowed(final Collection<EModification> allowedModifications,
                                           final AbstractFEObject toInsert) {

        boolean result = false;
        final EType insertType = toInsert.getCurrentType();

        if (insertType == null) {
            final EnumSet<EType> allowed = toInsert.getAllowedTypes();
            final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

            if (allowedModifications.contains(EModification.INSERT_BOOLEAN)
                    && allowed.contains(EType.BOOLEAN)) {
                filtered.add(EType.BOOLEAN);
                result = true;
            }

            if (allowedModifications.contains(EModification.INSERT_INTEGER)
                    && allowed.contains(EType.INTEGER)) {
                filtered.add(EType.INTEGER);
                result = true;
            }

            if (allowedModifications.contains(EModification.INSERT_REAL)
                    && allowed.contains(EType.REAL)) {
                filtered.add(EType.REAL);
                result = true;
            }

            if (allowedModifications.contains(EModification.INSERT_INTEGER_VECTOR)
                    && allowed.contains(EType.INTEGER_VECTOR)) {
                filtered.add(EType.INTEGER_VECTOR);
                result = true;
            }

            if (allowedModifications.contains(EModification.INSERT_REAL_VECTOR)
                    && allowed.contains(EType.REAL_VECTOR)) {
                filtered.add(EType.REAL_VECTOR);
                result = true;
            }

            if (allowedModifications.contains(EModification.INSERT_SPAN)
                    && allowed.contains(EType.SPAN)) {
                filtered.add(EType.SPAN);
                result = true;
            }

            if (result) {
                allowed.clear();
                allowed.addAll(filtered);
            }
        } else {
            switch (insertType) {
                case BOOLEAN:
                    result = allowedModifications.contains(EModification.INSERT_BOOLEAN);
                    break;
                case INTEGER:
                    result = allowedModifications.contains(EModification.INSERT_INTEGER);
                    break;
                case INTEGER_VECTOR:
                    result = allowedModifications.contains(EModification.INSERT_INTEGER_VECTOR);
                    break;
                case REAL:
                    result = allowedModifications.contains(EModification.INSERT_REAL);
                    break;
                case REAL_VECTOR:
                    result = allowedModifications.contains(EModification.INSERT_REAL_VECTOR);
                    break;
                case SPAN:
                    result = allowedModifications.contains(EModification.INSERT_SPAN);
                    break;
                case ERROR:
                default:
                    break;
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

        final String opStr = Character.toString(this.op.op);

        this.opBox = new RenderedBox(opStr);
        final int fontSize = getFontSize();
        this.opBox.setFontSize(fontSize);
        this.opBox.layout(g2d);

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = 0;
        int topY = 0;
        int botY = 0;

        if (this.arg1 != null) {
            this.arg1.layout(g2d);
            x += this.arg1.getAdvance();

            final Rectangle arg1Bounds = this.arg1.getBounds();
            topY = Math.min(topY, arg1Bounds.y);
            botY = Math.max(botY, arg1Bounds.y + arg1Bounds.height);
        }

        this.opBox.translate(x, 0);
        x += this.opBox.getAdvance();

        final Rectangle opBoxBounds = this.opBox.getBounds();
        topY = Math.min(topY, opBoxBounds.y);
        botY = Math.max(botY, opBoxBounds.y + opBoxBounds.height);

        if (this.arg2 != null) {
            this.arg2.layout(g2d);
            this.arg2.translate(x, 0);
            x += this.arg2.getAdvance();

            final Rectangle arg2Bounds = this.arg2.getBounds();
            topY = Math.min(topY, arg2Bounds.y);
            botY = Math.max(botY, arg2Bounds.y + arg2Bounds.height);
        }

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

        if (this.arg1 != null) {
            this.arg1.translate(dx, dy);
        }
        if (this.opBox != null) {
            this.opBox.translate(dx, dy);
        }
        if (this.arg2 != null) {
            this.arg2.translate(dx, dy);
        }

        getOrigin().move(dx, dy);
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
        if (this.arg1 != null) {
            this.arg1.render(g2d, cursor);
        }

        if (this.opBox != null) {
            final boolean selected = cursor.doesSelectionInclude(first);
            this.opBox.render(g2d, selected);
        }

        if (this.arg2 != null) {
            this.arg2.render(g2d, cursor);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.arg1 != null) {
            this.arg1.gatherRenderedBoxes(target);
        }

        if (this.opBox != null) {
            target.add(this.opBox);
        }

        if (this.arg2 != null) {
            this.arg2.gatherRenderedBoxes(target);
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
        final String opStr = Character.toString(this.op.op);
        final EnumSet<EType> possibleTypes = getPossibleTypes();

        builder.addln((parent == null ? "Binary Operator*: (" : "Binary Operator: ("), opStr, ") Possible=",
                possibleTypes);

        if (this.arg1 == null) {
            indent(builder, indent + 1);
            builder.addln("(No first argument)");
        } else {
            this.arg1.emitDiagnostics(builder, indent + 1);
        }

        if (this.arg2 == null) {
            indent(builder, indent + 1);
            builder.addln("(No second argument)");
        } else {
            this.arg2.emitDiagnostics(builder, indent + 1);
        }
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEBinaryOper duplicate() {

        final int fontSize = getFontSize();
        final FEBinaryOper dup = new FEBinaryOper(fontSize, this.op);

        dup.getAllowedTypes().clear();

        final EnumSet<EType> allowedTypes = getAllowedTypes();
        dup.getAllowedTypes().addAll(allowedTypes);

        final EType currentType = getCurrentType();
        dup.setCurrentType(currentType);

        // (The following sets parent on the duplicate arguments
        if (this.arg1 != null) {
            final AbstractFEObject arg1Dup = this.arg1.duplicate();
            dup.setArg1(arg1Dup, false);
        }
        if (this.arg2 != null) {
            final AbstractFEObject arg2Dup = this.arg2.duplicate();
            dup.setArg2(arg2Dup, false);
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

        final int myStart = getFirstCursorPosition();
        final int pos = cursor.cursorPosition;

        if (pos < myStart) {
            result = CursorPosition.OUTSIDE;
        } else if (pos == myStart) {
            if (this.arg1 == null) {
                result = CursorPosition.IN_EMPTY_ARG1_SLOT;
            } else {
                result = CursorPosition.WITHIN_ARG1;
            }
        } else if (this.arg1 == null) {
            if (pos == myStart + 1) {
                if (this.arg1 == null) {
                    result = CursorPosition.IN_EMPTY_ARG2_SLOT;
                } else {
                    result = CursorPosition.WITHIN_ARG2;
                }
            } else if (this.arg2 == null) {
                result = CursorPosition.OUTSIDE;
            } else {
                final int arg2End =
                        this.arg2.getFirstCursorPosition() + this.arg2.getNumCursorSteps();
                if (pos <= arg2End) {
                    result = CursorPosition.WITHIN_ARG2;
                } else {
                    result = CursorPosition.OUTSIDE;
                }
            }
        } else {
            // There is an "arg1" present
            final int arg1End = myStart + this.arg1.getNumCursorSteps();
            if (pos <= arg1End) {
                result = CursorPosition.WITHIN_ARG1;
            } else if (pos == arg1End + 1) {
                if (this.arg2 == null) {
                    result = CursorPosition.IN_EMPTY_ARG2_SLOT;
                } else {
                    result = CursorPosition.WITHIN_ARG2;
                }
            } else if (this.arg2 == null) {
                result = CursorPosition.OUTSIDE;
            } else {
                final int arg2End =
                        this.arg2.getFirstCursorPosition() + this.arg2.getNumCursorSteps();
                if (pos <= arg2End) {
                    result = CursorPosition.WITHIN_ARG2;
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
    private enum CursorPosition {

        /** Argument 1 is not present, cursor is at the argument 1 insertion point. */
        IN_EMPTY_ARG1_SLOT,

        /** Argument 1 is present, cursor is within argument 1. */
        WITHIN_ARG1,

        /** Argument 2 is not present, cursor is at the argument 2 insertion point. */
        IN_EMPTY_ARG2_SLOT,

        /** Argument 2 is present, cursor is within argument 2. */
        WITHIN_ARG2,

        /** Cursor falls outside the object. */
        OUTSIDE
    }
}
