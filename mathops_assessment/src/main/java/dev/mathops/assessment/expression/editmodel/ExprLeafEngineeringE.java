package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An expression object that represents the "E" that precedes a power of 10 in engineering notation.
 */
public final class ExprLeafEngineeringE extends ExprObjectLeaf {

    /**
     * Constructs a new {@code ExprLeafEngineeringE}.
     */
    public ExprLeafEngineeringE() {

        super();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "ExprLeafEngineeringE";
    }
}
