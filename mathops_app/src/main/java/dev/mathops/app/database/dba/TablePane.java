package dev.mathops.app.database.dba;

import dev.mathops.db.cfg.Database;

import javax.swing.JPanel;
import java.awt.CardLayout;
import java.util.List;

/**
 * A window that displays the currently selected schema/table.
 */
final class TablePane extends JPanel {

    /** The key for the blank card. */
    private static final String BLANK = "blank";

    /** The key for the card to show when a single database is selected. */
    private static final String SINGLE_DB = "single_db";

    /** The key for the card to show when a single database is selected. */
    private static final String MULTI_DB = "multi_db";

    /** The card layout. */
    private final CardLayout layout;

    /** The pane to present a table when a single database is selected. */
    private final TablePaneSingle singleDatabaseTable;

    /** The pane to present a table when multiple databases are selected. */
    private final TablePaneMulti multiDatabaseTable;

    /**
     * Constructs a new {@code TableWindow}.
     */
    TablePane() {

        super();

        this.layout = new CardLayout();
        setLayout(this.layout);

        final JPanel blank = new JPanel();
        add(blank, BLANK);

        this.singleDatabaseTable = new TablePaneSingle();
        add(this.singleDatabaseTable, SINGLE_DB);

        this.multiDatabaseTable = new TablePaneMulti();
        add(this.multiDatabaseTable, MULTI_DB);
    }

    /**
     * Selects a schema and table.
     *
     * @param schemaTable the schema and table; null if none is selected
     * @param databases   the list of selected databases
     */
    void select(final SchemaTable schemaTable, final List<Database> databases) {

        this.singleDatabaseTable.update(null, null);
        this.multiDatabaseTable.update(null, null);

        if (schemaTable == null || databases.isEmpty()) {
            this.layout.show(this, BLANK);
        } else {
            final int numDatabases = databases.size();

            if (numDatabases == 1) {
                this.layout.show(this, SINGLE_DB);
                final Database database = databases.getFirst();
                this.singleDatabaseTable.update(schemaTable, database);
            } else {
                this.layout.show(this, MULTI_DB);
                this.multiDatabaseTable.update(schemaTable, databases);
            }
        }
    }
}
