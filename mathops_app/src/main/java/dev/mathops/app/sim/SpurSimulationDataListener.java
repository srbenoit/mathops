package dev.mathops.app.sim;

/**
 * A listener that can be notified when simulation data changes.
 */
public interface SpurSimulationDataListener {

    /**
     * Called after simulation data is updated.  This should be called on the AWT event thread.  The callee should not
     * invoke time-consuming tasks, but can update the UI.
     */
    void updateSimulationData();
}
