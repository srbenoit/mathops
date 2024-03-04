package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a symbolic real-valued constant like pi or e or the imaginary unit i.
 */
public final class ExprLeafSymbolicConstant extends AbstractExprLeaf {

    /** The symbolic value. */
    public final ESymbolicConstant value;

    /**
     * Constructs a new {@code ExprLeafSymbolicConstant}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theValue the symbolic value
     */
    public ExprLeafSymbolicConstant(final AbstractExprObject theParent, final ESymbolicConstant theValue) {

        super(theParent);

        this.value = theValue;
    }
}
