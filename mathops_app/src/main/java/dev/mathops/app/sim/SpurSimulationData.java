package dev.mathops.app.sim;

import dev.mathops.app.sim.rooms.CampusRoom;
import dev.mathops.app.sim.rooms.CampusRoomsTableModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for the various data needed to run a simulation.
 */
public final class SpurSimulationData {

    /** Listeners to be notified when data changes. */
    private final List<SpurSimulationDataListener> listeners;

    /** The table model for the list of defined campus rooms. */
    private final CampusRoomsTableModel campusRoomsTableModel;

    /**
     * Constructs a new {@code SpurSimulationData}.
     *
     * @param dataDir the directory in which configuration data is stored
     */
    public SpurSimulationData(final File dataDir) {

        this.listeners = new ArrayList<>(5);

        this.campusRoomsTableModel = new CampusRoomsTableModel(dataDir);
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
     * Adds a campus room.  This should be called on the AWT event thread since it notifies listeners.
     *
     * @param room the room to add
     */
    public void addCampusRoom(final CampusRoom room) {

        this.campusRoomsTableModel.add(room);
        fireUpdate();
    }

    /**
     * Gets the data model for the table of campus rooms.
     *
     * @return the table model
     */
    public CampusRoomsTableModel getCampusRoomsTableModel() {

        return this.campusRoomsTableModel;
    }
}
