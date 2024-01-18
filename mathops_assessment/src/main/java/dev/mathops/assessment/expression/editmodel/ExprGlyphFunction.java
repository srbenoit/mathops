package dev.mathops.assessment.expression.editmodel;

import dev.mathops.assessment.formula.EFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * A glyph that represents a function name and its arguments.
 */
public class ExprGlyphFunction extends AbstractExprGlyph {

    /** The function. */
    public final EFunction function;

    /** The arguments. */
    public final List<ExprGlyphSequence> arguments;

    /**
     * Constructs a new {@code ExprGlyphFunction}.
     *
     * @param theFunction the function
     */
    public ExprGlyphFunction(final EFunction theFunction) {

        super();

        this.function = theFunction;
        this.arguments = new ArrayList<>(3);
    }
}
