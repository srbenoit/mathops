package dev.mathops.app.database.dba;

import dev.mathops.db.cfg.Database;

import javax.swing.JPanel;

/**
 * A panel that lets the user constructs an SQL statement to execute against a table.
 */
final class PaneSQL extends JPanel {

    /**
     * Constructs a new {@code PaneSQL}.
     */
    PaneSQL() {

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

