package dev.mathops.app.sim.rooms;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model for the table of all rooms defined on campus.
 */
public final class CampusRoomsTableModel extends AbstractTableModel {

    /** The column names. */
    private final String[] columnNames = {"Room Name", "Maximum Capacity"};

    /** The data directory. */
    private final File dataDir;

    /** The list of campus rooms. */
    private final List<CampusRoom> allCampusRooms;

    /**
     * Constructs a new {@code CampusRoomsTableModel}, attempting to populate the model from a data file.
     *
     * @param theDataDir the data directory
     */
    public CampusRoomsTableModel(final File theDataDir) {

        super();

        this.dataDir = theDataDir;
        this.allCampusRooms = new ArrayList<>(10);

        CampusRoomJson.load(theDataDir, this.allCampusRooms);
    }

    /**
     * Gets the column count.
     *
     * @return the column count
     */
    public int getColumnCount() {

        return this.columnNames.length;
    }

    /**
     * Gets the number of rows in the table.
     *
     * @return the number of rows
     */
    public int getRowCount() {

        return this.allCampusRooms.size();
    }

    /**
     * Gets the name of a column.
     *
     * @param column the column being queried
     * @return the name
     */
    public String getColumnName(final int column) {

        return this.columnNames[column];
    }

    /**
     * Gets the value of a table cell.
     *
     * @param rowIndex    the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the cell value
     */
    public Object getValueAt(final int rowIndex, final int columnIndex) {

        final CampusRoom room = this.allCampusRooms.get(rowIndex);

        Object result = null;

        if (columnIndex == 0) {
            result = room.getId();
        } else if (columnIndex == 1) {
            result = Integer.valueOf(room.getCapacity());
        }

        return result;
    }

    /**
     * Gets the class of a column.
     *
     * @param columnIndex the column being queried
     * @return the class
     */
    public Class<?> getColumnClass(final int columnIndex) {

        return columnIndex == 1 ? Integer.class : String.class;
    }

    /**
     * Tests whether a cell is editable.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     * @return true if editable
     */
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {

        return false;
    }

    /**
     * Scans for any campus rooms with a specified name.
     *
     * @param name the name
     * @return the campus room in the table with that name; null if none
     */
    public CampusRoom getByName(final String name) {

        CampusRoom result = null;

        for (final CampusRoom test : this.allCampusRooms) {

            if (test.getId().equals(name)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Adds a room.
     *
     * @param room the room to add
     */
    public void add(final CampusRoom room) {

        final int rowIndex = this.allCampusRooms.size();

        this.allCampusRooms.add(room);
        CampusRoomJson.store(this.dataDir, this.allCampusRooms);
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    /**
     * Removes a room.
     *
     * @param room the room to remove
     */
    public void remove(final CampusRoom room) {

        final int rowIndex = this.allCampusRooms.indexOf(room);

        if (rowIndex >= 0) {
            this.allCampusRooms.remove(rowIndex);
            CampusRoomJson.store(this.dataDir, this.allCampusRooms);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
}
