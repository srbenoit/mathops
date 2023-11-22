package dev.mathops.assessment;

import dev.mathops.assessment.variable.EvalContext;

/**
 * An interface for all realizable objects.
 */
@FunctionalInterface
public interface Realizable {

    /**
     * Realizes the object, adding diagnostic messages to a message list as needed.
     *
     * @param context the evaluation context
     * @return {@code true} if realization succeeds; {@code false} otherwise
     */
    boolean realize(EvalContext context);
}
