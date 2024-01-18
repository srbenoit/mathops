package dev.mathops.assessment.expression.editmodel;

/**
 * The interface for listeners that wish to be notified when an expression model changes.
 */
public interface IExpressionModelListener {

    /**
     * Called when the model has changed.
     *
     * @param theModel the model that has changed
     */
    void modelChanged(ExpressionModel theModel);
}
