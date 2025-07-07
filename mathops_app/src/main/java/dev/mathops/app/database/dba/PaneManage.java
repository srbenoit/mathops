package dev.mathops.app.database.dba;

import dev.mathops.db.cfg.Database;

import javax.swing.JPanel;

/**
 * A panel that lets the user perform a query of a database table, update rows, delete rows, or add new rows.
 */
final class PaneManage extends JPanel {

    /**
     * Constructs a new {@code PaneManage}.
     */
    PaneManage() {

        super();
    }

    /**
     * Updates the schema and table this panel shows and the database holding the data.
     *
     * @param schemaTable the schema and table; null if none is selected
     * @param database    the selected database
     */
    void update(final SchemaTable schemaTable, final Database database) {

    }
}

