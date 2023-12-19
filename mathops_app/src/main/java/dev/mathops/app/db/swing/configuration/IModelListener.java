package dev.mathops.app.db.swing.configuration;

/**
 * A class that can receive notifications when the model changes.
 */
@FunctionalInterface
public interface IModelListener {

    /**
     * Called when the model has changed.
     *
     * @param updatedModel the updated model
     */
    void modelChanged(Model updatedModel);
}
