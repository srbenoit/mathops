package dev.mathops.app.database.dba;

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
     * @param databaseUse    the selected database use
     */
    void update(final SchemaTable schemaTable, final DatabaseUse databaseUse) {

    }
}

