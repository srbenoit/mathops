package dev.mathops.assessment.expression.editmodel;

import dev.mathops.assessment.formula.EFunction;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A glyph that represents a function name and its arguments.
 */
public final class ExprBranchFunction extends ExprObjectBranch {

    /** The function. */
    public final EFunction function;

    /** The arguments. */
    public final List<Expr> arguments;

    /**
     * Constructs a new {@code ExprBranchFunction}.
     *
     * @param theFunction the function
     */
    public ExprBranchFunction(final EFunction theFunction) {

        super();

        if (theFunction == null) {
            throw new IllegalArgumentException("Function may not be null");
        }


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

        final Expr expr = new Expr();

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

        final Expr expr = new Expr();

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

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprBranchFunction{function=");
        htm.add(this.function);
        htm.add(",arguments=[");

        final int count = this.arguments.size();
        if (count > 0) {
            final ExprObject child0 = this.arguments.getFirst();
            htm.add(child0);

            for (int i = 1; i < count; ++i) {
                htm.add(CoreConstants.COMMA);
                final ExprObject child = this.arguments.get(i);
                htm.add(child);
            }
        }

        htm.add("]}");

        return htm.toString();
    }
}

