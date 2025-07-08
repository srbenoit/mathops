package dev.mathops.app.database.dba;

import dev.mathops.db.cfg.Database;

import javax.swing.JPanel;

/**
 * A panel that presents the user with information about a database table.
 */
final class PaneInformation extends JPanel {

    /**
     * Constructs a new {@code PaneInformation}.
     */
    PaneInformation() {

        super();
    }

    /**
     * Updates the schema and table this panel shows and the database holding the data.
     *
     * @param schemaTable the schema and table; null if none is selected
     * @param databaseUse the selected database use
     */
    void update(final SchemaTable schemaTable, final DatabaseUse databaseUse) {

    }
}

