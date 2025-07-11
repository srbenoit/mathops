package dev.mathops.app.sim.swing;

import javax.swing.table.AbstractTableModel;
import java.io.Serial;

/**
 * A table model for the table of all rooms defined on campus.
 */
public abstract class ButtonColumnTableModel extends AbstractTableModel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5212267797778729356L;

    /**
     * Removes a row.
     *
     * @param rowIndex the row to remove
     */
    public abstract void removeRow(final int rowIndex);
}
