package dev.mathops.app.sim.rooms;

import javax.swing.DefaultComboBoxModel;
import java.io.File;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A list model for the list of all defined campus room sets.  When a change to the model is made, its data is written
 * to a JSON data file.
 */
public final class RoomSetsListModel extends DefaultComboBoxModel<RoomSet> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4615709987133825842L;
    
    /** The data directory. */
    private final File dataDir;

    /** The list of all room sets. */
    private final List<RoomSet> sets;

    /** The names of all sets. */
    private final Set<String> names;

    /**
     * Constructs a new {@code CampusRoomConfigsListModel}, attempting to populate the model from a data file.
     *
     * @param theDataDir the data directory
     */
    public RoomSetsListModel(final File theDataDir) {

        super();

        this.dataDir = theDataDir;
        this.sets = new ArrayList<>(10);
        this.names = new HashSet<>(10);

        RoomSetJson.load(theDataDir, this);

        for (final RoomSet set : this.sets) {
            final String name = set.getName();
            this.names.add(name);
        }
    }

    /**
     * Gets the number of entries in the list.
     *
     * @return the number of entries
     */
    @Override
    public int getSize() {

        return this.sets.size();
    }

    /**
     * Gets an element from the list.
     *
     * @param index the requested index
     * @return the element
     */
    @Override
    public RoomSet getElementAt(final int index) {

        return this.sets.get(index);
    }

    /**
     * Tests whether the list has a room set with a specified name.
     *
     * @param name the name
     * @return true if a room set with the specified name is present
     */
    public boolean hasName(final String name) {

        return this.names.contains(name);
    }

    /**
     * Adds the specified element to the end of this list.
     *
     * @param element the element to be added
     * @return true if the element was added; false if the element's name already existed
     */
    public boolean canAddElement(final RoomSet element) {

        final boolean added;

        final String name = element.getName();

        if (hasName(name)) {
            added = false;
        } else {
            final int index = this.sets.size();
            this.sets.add(element);
            this.names.add(name);

            RoomSetJson.store(this.dataDir, this);

            fireIntervalAdded(this, index, index);
            added = true;
        }

        return added;
    }

    /**
     * Removes the element at a specified index from this list.
     *
     * @param index the index of the element to be removed
     */
    public void removeElement(final int index) {

        final RoomSet set = this.sets.remove(index);

        if (set != null) {
            final String name = set.getName();
            this.names.remove(name);

            RoomSetJson.store(this.dataDir, this);
            fireIntervalRemoved(this, index, index);
        }
    }

    /**
     * Called when data within elements of the list changes (as opposed to when elements are added to or removed from
     * the list).  These changes require the data file be re-written, but do not change the contents of the list.
     */
    void dataChanged() {

        RoomSetJson.store(this.dataDir, this);
    }
}
