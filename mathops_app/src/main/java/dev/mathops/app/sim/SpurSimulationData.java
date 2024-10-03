package dev.mathops.app.sim;

import dev.mathops.app.sim.rooms.RoomSet;
import dev.mathops.app.sim.rooms.RoomSetsListModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for the various data needed to run a simulation.
 */
public final class SpurSimulationData {

    /** Listeners to be notified when data changes. */
    private final List<SpurSimulationDataListener> listeners;

    /** The list model for the list of room sets. */
    private final RoomSetsListModel roomSetListModel;

    /**
     * Constructs a new {@code SpurSimulationData}.
     *
     * @param dataDir the directory in which configuration data is stored
     */
    SpurSimulationData(final File dataDir) {

        this.listeners = new ArrayList<>(5);

        this.roomSetListModel = new RoomSetsListModel(dataDir);
    }

    /**
     * Adds a listener that will be notified when data changes.
     *
     * @param theListener the listener to add
     */
    public void addListener(final SpurSimulationDataListener theListener) {

        this.listeners.add(theListener);
    }

    /**
     * Removes a listener previously registered with {@code addListener}.
     *
     * @param theListener the listener to remove
     */
    public void removeListener(final SpurSimulationDataListener theListener) {

        this.listeners.remove(theListener);
    }

    /**
     * Notifies all registered listeners of a change.
     */
    private void fireUpdate() {

        for (final SpurSimulationDataListener listener : this.listeners) {
            listener.updateSimulationData();
        }
    }

    /**
     * Adds a room set.  This should be called on the AWT event thread since it notifies listeners.
     *
     * @param roomSet the room set to add
     */
    public void addRoomSet(final RoomSet roomSet) {

        if (this.roomSetListModel.canAddElement(roomSet)) {
            fireUpdate();
        }
    }

    /**
     * Gets the list model for the list of defined room sets.
     *
     * @return the list model
     */
    public RoomSetsListModel getRoomSetListModel() {

        return this.roomSetListModel;
    }
}
