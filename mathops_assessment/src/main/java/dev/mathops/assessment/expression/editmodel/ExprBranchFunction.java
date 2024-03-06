package dev.mathops.assessment.expression.editmodel;

import dev.mathops.assessment.formula.EFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * A glyph that represents a function name and its arguments.
 */
public final class ExprBranchFunction extends AbstractExprBranch {

    /** The function. */
    public final EFunction function;

    /** The arguments. */
    public final List<Expr> arguments;

    /**
     * Constructs a new {@code ExprBranchFunction}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theFunction the function
     */
    public ExprBranchFunction(final AbstractExprObject theParent, final EFunction theFunction) {

        super(theParent);

        this.function = theFunction;
        this.arguments = new ArrayList<>(10);
    }

    /**
     * Gets the number of child objects in this expression.
     *
     * @return the number of child objects
     */
    public int numArguments() {

        return this.arguments.size();
    }

    /**
     * Gets an argument.
     *
     * @param index the index
     * @return the argument expression
     */
    public Expr getArgument(final int index) {

        return this.arguments.get(index);
    }

    /**
     * Adds a new argument to the end of the argument list.
     *
     * @return the newly added argument expression
     */
    public Expr addArgument() {

        final Expr expr = new Expr(this);

        this.arguments.add(expr);

        return expr;
    }

    /**
     * Adds a new argument at a specified position in the argument list.
     *
     * @param index the index at which to add the new argument
     * @return the newly added argument expression
     */
    public Expr addArgument(final int index) {

        final Expr expr = new Expr(this);

        this.arguments.add(index, expr);

        return expr;
    }

    /**
     * Removes the object at a specified index in this expression.
     *
     * @param index the index at which to insert the object
     */
    public void removeArgument(final int index) {

        this.arguments.remove(index);
    }
}

