package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A table model for the results table. */
public final class ResultsTableModel implements TableModel {

    /** Listeners to notify when the table data changes. */
    private final List<TableModelListener> listeners;

    /** The columns in the table. */
    private final List<Column> columns;

    /** The rows in the table. */
    private final List<Object[]> rows;

    /**
     * Constructs a new {@code ResultsTableModel}.
     */
    ResultsTableModel() {

        this.listeners = new ArrayList<>(2);
        this.columns = new ArrayList<>(30);
        this.rows = new ArrayList<>(200);
    }

    /**
     * Adds a listener to the list that is notified each time a change to the data model occurs.
     *
     * @param listener the TableModelListener
     */
    public void addTableModelListener(final TableModelListener listener) {

        this.listeners.add(listener);
    }

    /**
     * Removes a listener from the list that is notified each time a change to the data model occurs.
     *
     * @param listener the TableModelListener
     */
    public void removeTableModelListener(final TableModelListener listener) {

        this.listeners.remove(listener);
    }

    /**
     * Notifies all listeners that table data has changed.
     */
    private void notifyListeners(final TableModelEvent event) {

        for (final TableModelListener listener : this.listeners) {
            listener.tableChanged(event);
        }
    }

    /**
     * Deletes all rows in the table.
     */
    public void deleteAllRows() {

        final int numRows = this.rows.size();

        if (numRows > 0) {
            this.rows.clear();
            final TableModelEvent event = new TableModelEvent(this, 0, numRows - 1, TableModelEvent.ALL_COLUMNS,
                    TableModelEvent.DELETE);
            notifyListeners(event);
        }
    }

    /**
     * Sets the columns for the table and removes all existing data.
     *
     * @param newColumns the new list of columns
     */
    public void setColumns(final Collection<Column> newColumns) {

        deleteAllRows();

        this.columns.clear();
        this.columns.addAll(newColumns);

        final TableModelEvent event = new TableModelEvent(this, TableModelEvent.HEADER_ROW);
        notifyListeners(event);
    }

    /**
     * Gets the number of columns in the table.
     *
     * @return the number of columns
     */
    public int getColumnCount() {

        return this.columns.size();
    }

    /**
     * Gets the number of rows in the table.
     *
     * @return the number of rows
     */
    public int getRowCount() {

        return this.rows.size();
    }

    /**
     * Gets the name of a column.
     *
     * @param columnIndex the column index
     * @return the column name
     */
    public String getColumnName(final int columnIndex) {

        final Column col = this.columns.get(columnIndex);
        return col.name();
    }

    /**
     * Gets the most specific superclass for all the cell values in the column.
     *
     * @param columnIndex the column index
     * @return the column value class
     */
    public Class<?> getColumnClass(final int columnIndex) {

        final Column col = this.columns.get(columnIndex);
        return col.valueClass();
    }

    /**
     * Returns the value for the cell at columnIndex and rowIndex.
     *
     * @param rowIndex    the row index
     * @param columnIndex the column index
     * @return the cell value
     */
    public Object getValueAt(final int rowIndex, final int columnIndex) {

        return this.rows.get(rowIndex)[columnIndex];
    }

    /**
     * Sets the value for the cell at columnIndex and rowIndex.
     *
     * @param newValue    the new value
     * @param rowIndex    the row index
     * @param columnIndex the column index
     */
    public void setValueAt(final Object newValue, final int rowIndex, final int columnIndex) {

        final int numColumns = this.columns.size();

        if (columnIndex < numColumns) {
            int numRows = this.rows.size();
            while (rowIndex >= numRows) {
                final Object[] newRow = new Object[numColumns];
                this.rows.add(newRow);
                ++numRows;
            }

            final Column col = this.columns.get(columnIndex);
            final Class<?> cls = col.valueClass();

            final Object[] row = this.rows.get(rowIndex);
            if (newValue == null || cls.isInstance(newValue)) {
                row[columnIndex] = newValue;
            } else {
                final Class<?> newValueClass = newValue.getClass();
                final String newValueClassName = newValueClass.getName();
                final String name = cls.getName();
                Log.warning("Attempt to set table cell to ", newValueClassName, " when column type is ", name);
            }
        }
    }

    /**
     * Tests whether a cell is editable.
     *
     * @param rowIndex    the row whose value to be queried
     * @param columnIndex the column whose value to be queried
     * @return false (no cells in this table type are editable)
     */
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {

        return false;
    }

    /**
     * Adds a new row to the end of the table.  This does NOT notify listeners since this table type will probably add
     * many rows at once, and can notify after all are added.
     *
     * @param fieldValues the field value (this array is copied, not stored directly; the same array can be used for
     *                    multiple calls to this method)
     */
    public void addRow(final Object... fieldValues) {

        final int numCols = this.columns.size();
        if (fieldValues == null || fieldValues.length != numCols) {
            Log.warning("Attempting to add a row with incorrect number of field values.");
        } else {
            final Object[] newRow = new Object[numCols];
            boolean good = true;

            for (int i = 0; i < numCols; ++i) {
                final Column col = this.columns.get(i);
                final Class<?> cls = col.valueClass();
                final Object value = fieldValues[i];

                if (value == null || cls.isInstance(value)) {
                    newRow[i] = value;
                } else {
                    final Class<?> newValueClass = value.getClass();
                    final String newValueClassName = newValueClass.getName();
                    final String name = cls.getName();
                    Log.warning("Attempt to set table cell to ", newValueClassName, " when column type is ", name);
                    good = false;
                    break;
                }
            }

            if (good) {
                final int rowIndex = this.rows.size();
                this.rows.add(newRow);

                final TableModelEvent event = new TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.INSERT);
                notifyListeners(event);
            }
        }
    }

    /**
     * Gets a copy of the table data for a single row.
     *
     * @param rowIndex the row index
     * @return the table data
     */
    public Object[] getRow(final int rowIndex) {

        return this.rows.get(rowIndex).clone();
    }

    /**
     * Notifies listeners that all rows currently in the table were just added.
     */
    public void notifyAllRowsAdded() {
        final int numRows = this.rows.size();

        final TableModelEvent event = new TableModelEvent(this, 0, numRows - 1, TableModelEvent.ALL_COLUMNS,
                TableModelEvent.INSERT);
        notifyListeners(event);
    }
}
