package dev.mathops.app.sim.campus;

import dev.mathops.app.sim.swing.ButtonColumnTableModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model for the table of all semester schedules defined on campus.
 */
public final class SemesterSchedulesTableModel extends ButtonColumnTableModel {

    /** The column names. */
    private final String[] columnNames = {"Schedule Name", "Weeks of Class", " "};

    /** The data directory. */
    private final File dataDir;

    /** The list of campus rooms. */
    private final List<SemesterSchedule> allSemesterSchedules;

    /**
     * Constructs a new {@code SemesterSchedulesTableModel}, attempting to populate the model from a data file.
     *
     * @param theDataDir the data directory
     */
    public SemesterSchedulesTableModel(final File theDataDir) {

        super();

        this.dataDir = theDataDir;
        this.allSemesterSchedules = new ArrayList<>(10);

        SemesterSchedulesJson.load(theDataDir, this.allSemesterSchedules);
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

        return this.allSemesterSchedules.size();
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

        final SemesterSchedule schedule = this.allSemesterSchedules.get(rowIndex);

        Object result = null;

        if (columnIndex == 0) {
            result = schedule.getId();
        } else if (columnIndex == 1) {
            final int weeks = schedule.getWeeksOfClass();
            result = Integer.toString(weeks);
        } else if (columnIndex == 2) {
            result = "Delete " + schedule.getId();
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
    public SemesterSchedule getByName(final String name) {

        SemesterSchedule result = null;

        for (final SemesterSchedule test : this.allSemesterSchedules) {

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
     * Adds a schedule.
     *
     * @param schedule the schedule to add
     */
    public void add(final SemesterSchedule schedule) {

        final int rowIndex = this.allSemesterSchedules.size();

        this.allSemesterSchedules.add(schedule);
        SemesterSchedulesJson.store(this.dataDir, this.allSemesterSchedules);
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    /**
     * Removes a row.
     *
     * @param rowIndex the row to remove
     */
    public void removeRow(final int rowIndex) {

        this.allSemesterSchedules.remove(rowIndex);
        SemesterSchedulesJson.store(this.dataDir, this.allSemesterSchedules);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}
