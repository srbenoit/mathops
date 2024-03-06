package dev.mathops.assessment.expression.editmodel;

/**
 * The base class for expression objects that are "branch" nodes in an expression model tree.
 */
abstract class AbstractExprBranch extends AbstractExprObject {

    /**
     * Constructs a new {@code AbstractExprBranch}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    AbstractExprBranch(final AbstractExprObject theParent) {

        super(theParent);
    }
}
