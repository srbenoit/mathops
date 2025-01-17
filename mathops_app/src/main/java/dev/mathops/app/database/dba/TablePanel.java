package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.List;

/**
 * The panel that should be displayed when a table is selected in the tree view.
 */
final class TablePanel extends PanelBase {

    /** The caches for each login configuration provided. */
    private final List<Cache> caches;

    /** A label to show the name of the selected schema. */
    private final JLabel schemaNameLabel;

    /** A label to show the name of the selected table. */
    private final JLabel tableNameLabel;

    /**
     * Constructs a new {@code TablePanel}.
     *
     * @param theCaches the caches for each login configuration provided
     */
    TablePanel(final List<Cache> theCaches) {

        super();

        this.caches = theCaches;

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        this.schemaNameLabel = makeHeadingLabel("Schema: ", top);
        this.tableNameLabel = makeHeadingLabel("        Table: ", top);
        add(top, StackedBorderLayout.NORTH);
    }

    /**
     * Sets the table to display.
     *
     * @param schemaName the schema name
     * @param tableName  the table name
     */
    void setTable(final String schemaName, final String tableName) {

        this.schemaNameLabel.setText(schemaName);
        this.tableNameLabel.setText(tableName);

        // TODO: Go to each cache and get some table-related information, present in tabs or as a table.
    }
}
