package dev.mathops.app.database.dba;

import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;

import javax.swing.JPanel;

/**
 * A panel that lets the user perform a query of a database table, update rows, delete rows, or add new rows.
 */
final class PaneManage extends JPanel {

    /** The database configuration. */
    private final DatabaseConfig config;

    /**
     * Constructs a new {@code PaneManage}.
     *
     * @param theConfig the database configuration
     */
    PaneManage(final DatabaseConfig theConfig) {

        super();

        this.config = theConfig;
    }

    /**
     * Updates the schema and table this panel shows and the database holding the data.
     *
     * @param schemaTable the schema and table; null if none is selected
     * @param databaseUse the selected database use
     */
    void update(final SchemaTable schemaTable, final DatabaseUse databaseUse) {

        // TODO:
    }
}

