package dev.mathops.assessment.variable;

import dev.mathops.assessment.formula.Formula;

import java.util.Collection;

/**
 * The interface for a variable that supports a list of excluded values.
 */
public interface IExcludableVariable {

    /**
     * Sets the list of excluded value formulae.
     *
     * @param theExcludes the list of excluded value formulae
     */
    void setExcludes(Formula[] theExcludes);

    /**
     * Sets the collection of excluded value formulae.
     *
     * @param theExcludes the collection of excluded value formulae
     */
    void setExcludes(Collection<Formula> theExcludes);

    /**
     * Gets the list of excluded value formulae, used to prevent values from being selected as random integers.
     *
     * @return the array of formulae for excluded values
     */
    Formula[] getExcludes();
}
