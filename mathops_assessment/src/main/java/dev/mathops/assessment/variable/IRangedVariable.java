package dev.mathops.assessment.variable;

import dev.mathops.assessment.NumberOrFormula;

/**
 * The interface for a variable that supports min/max limits.
 */
public interface IRangedVariable {

    /**
     * Sets the minimum value.
     *
     * @param theMin the minimum value
     */
    void setMin(NumberOrFormula theMin);

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    NumberOrFormula getMin();

    /**
     * Sets the maximum value.
     *
     * @param theMax the maximum value
     */
    void setMax(NumberOrFormula theMax);

    /**
     * Gets the maximum value.
     *
     * @return the maximum value
     */
    NumberOrFormula getMax();
}
