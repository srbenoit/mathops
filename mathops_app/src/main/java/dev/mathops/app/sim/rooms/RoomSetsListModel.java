package dev.mathops.app.sim.rooms;

import javax.swing.AbstractListModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A list model for the list of all defined campus room sets.  When a change to the model is made, its data is written
 * to a JSON data file.
 */
public final class RoomSetsListModel extends AbstractListModel<RoomSet> {

    /** The data directory. */
    private final File dataDir;

    /** The list of all room sets. */
    private final List<RoomSet> configs;

    /**
     * Constructs a new {@code CampusRoomConfigsListModel}, attempting to populate the model from a data file.
     *
     * @param theDataDir the data directory
     */
    public RoomSetsListModel(final File theDataDir) {

        super();

        this.dataDir = theDataDir;
        this.configs = new ArrayList<>(10);

        RoomSetJson.load(theDataDir, this);
    }

    /**
     * Gets the number of entries in the list.
     *
     * @return the number of entries
     */
    @Override
    public int getSize() {

        return this.configs.size();
    }

    /**
     * Gets an element from the list.
     *
     * @param index the requested index
     * @return the element
     */
    @Override
    public RoomSet getElementAt(final int index) {

        return this.configs.get(index);
    }

    /**
     * Adds the specified element to the end of this list.
     *
     * @param element the element to be added
     */
    public void addElement(final RoomSet element) {

        final int index = this.configs.size();
        this.configs.add(element);
        RoomSetJson.store(this.dataDir, this);

        fireIntervalAdded(this, index, index);
    }

    /**
     * Removes the element at a specified index from this list.
     *
     * @param index the index of the element to be removed
     */
    public void removeElement(final int index) {

        this.configs.remove(index);
        RoomSetJson.store(this.dataDir, this);

        fireIntervalRemoved(this, index, index);
    }

    /**
     * Called when data within elements of the list changes (as opposed to when elements are added to or removed from
     * the list).  These changes require the data file be re-written, but do not change the contents of the list.
     */
    void dataChanged() {

        RoomSetJson.store(this.dataDir, this);
    }
}
