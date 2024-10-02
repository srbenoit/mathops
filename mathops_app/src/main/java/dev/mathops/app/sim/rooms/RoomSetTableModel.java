package dev.mathops.app.sim.rooms;

import dev.mathops.app.sim.swing.ButtonColumnTableModel;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model for the table of all rooms defined in a campus room configuration.
 */
public final class RoomSetTableModel extends ButtonColumnTableModel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -3293960540719923842L;

    /** The column names. */
    private final String[] columnNames = {"Room Name", "Maximum Capacity", " "};

    /** The owning list to be notified when data changes. */
    private final RoomSetsListModel owningList;

    /** The list of rooms. */
    private final List<Room> rooms;

    /**
     * Constructs a new {@code CampusRoomSetTableModel}.
     *
     * @param theOwningList the owning list to be notified when data changes
     */
    RoomSetTableModel(final RoomSetsListModel theOwningList) {

        super();

        this.owningList = theOwningList;
        this.rooms = new ArrayList<>(10);
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

        return this.rooms.size();
    }

    /**
     * Gets the {@code CampusRoom} at a specified index.
     *
     * @param index the index of the row to retrieve (0 for the first row)
     * @return the row
     */
    public Room getRow(final int index) {

        return this.rooms.get(index);
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

        final Room room = this.rooms.get(rowIndex);

        Object result = null;

        if (columnIndex == 0) {
            result = room.getId();
        } else if (columnIndex == 1) {
            final int cap = room.getCapacity();
            result = Integer.toString(cap);
        } else if (columnIndex == 2) {
            result = "Delete " + room.getId();
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

        return getValueAt(0, columnIndex).getClass();
    }

    /**
     * Scans for any campus rooms with a specified name.
     *
     * @param name the name
     * @return the campus room in the table with that name; null if none
     */
    Room getByName(final String name) {

        Room result = null;

        for (final Room test : this.rooms) {
            if (test.getId().equals(name)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Tests whether a cell is editable.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     * @return true if the cell is editable
     */
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {

        return columnIndex == 2;
    }

    /**
     * Adds a room.
     *
     * @param room the room to add
     */
    void add(final Room room) {

        final int rowIndex = this.rooms.size();

        this.rooms.add(room);
        this.owningList.dataChanged();
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    /**
     * Removes a row.
     *
     * @param rowIndex the row to remove
     */
    public void removeRow(final int rowIndex) {

        this.rooms.remove(rowIndex);
        this.owningList.dataChanged();
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}
