package dev.mathops.app.sim.swing;

import javax.swing.table.AbstractTableModel;

/**
 * A table model for the table of all rooms defined on campus.
 */
public abstract class ButtonColumnTableModel extends AbstractTableModel {

    /**
     * Removes a row.
     *
     * @param rowIndex the row to remove
     */
    public abstract void removeRow(final int rowIndex);
}
