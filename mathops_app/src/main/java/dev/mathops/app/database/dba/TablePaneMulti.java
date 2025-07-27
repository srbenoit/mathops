package dev.mathops.app.database.dba;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * A panel that displays a database table when multiple databases are selected.
 */
final class TablePaneMulti extends JPanel {

    /** A label to show the schema name. */
    private final JLabel schemaName;

    /** A label to show the table name. */
    private final JLabel tableName;

    /** The pane that will show the selected databases. */
    private final JPanel selectedDatabasesRow;

    /** The panel to perform comparisons between two databases. */
    private final DatabaseCompare compare;

    /**
     * Constructs a new {@code TablePaneMulti}.
     *
     * @param config the database configuration
     */
    TablePaneMulti(final DatabaseConfig config) {

        super(new StackedBorderLayout());

        final Color background = getBackground();
        final boolean bgLight = InterfaceUtils.isLight(background);
        final Color accent = InterfaceUtils.createAccentColor(background, bgLight);

        final Color newBg = bgLight ? background.brighter() : background.darker();
        final Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, accent);

        final Font font = getFont();
        final int fontSize = font.getSize();
        final Font larger = font.deriveFont((float) fontSize * 1.2f);

        final Color foreground = getForeground();
        final boolean lightForeground = InterfaceUtils.isLight(foreground);
        final Color labelColor = lightForeground ? new Color(255, 255, 150) : new Color(160, 0, 0);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        topFlow.setBackground(newBg);
        topFlow.setBorder(bottomLine);
        final JLabel lbl1 = new JLabel("Schema: ");
        lbl1.setFont(larger);
        topFlow.add(lbl1);
        this.schemaName = new JLabel(CoreConstants.SPC);
        this.schemaName.setFont(larger);
        this.schemaName.setForeground(labelColor);
        topFlow.add(this.schemaName);
        final JLabel lbl2 = new JLabel("   Table: ");
        lbl2.setFont(larger);
        topFlow.add(lbl2);
        this.tableName = new JLabel(CoreConstants.SPC);
        this.tableName.setFont(larger);
        this.tableName.setForeground(labelColor);
        topFlow.add(this.tableName);

        add(topFlow, StackedBorderLayout.NORTH);

        this.selectedDatabasesRow = new JPanel(new StackedBorderLayout());
        add(this.selectedDatabasesRow, StackedBorderLayout.NORTH);

        this.compare = new DatabaseCompare();
        add(this.compare, StackedBorderLayout.CENTER);
    }

    /**
     * Updates the schema and table this panel shows and the databases holding the data.
     *
     * @param schemaTable  the schema and table; null if none is selected
     * @param databaseUses the selected database uses
     */
    void update(final SchemaTable schemaTable, final Iterable<DatabaseUse> databaseUses) {

        this.selectedDatabasesRow.removeAll();

        if (schemaTable == null) {
            this.schemaName.setText(CoreConstants.SPC);
            this.tableName.setText(CoreConstants.SPC);
        } else {
            final ESchema schema = schemaTable.schema();
            final String schemaStr = schema.name();
            this.schemaName.setText(schemaStr);
            final String tableStr = schemaTable.table();
            this.tableName.setText(tableStr);

            for (final DatabaseUse use : databaseUses) {
                final SelectedDatabaseHeader icon = new SelectedDatabaseHeader(use, schemaTable);
                this.selectedDatabasesRow.add(icon, StackedBorderLayout.WEST);
            }
        }

        this.compare.update(schemaTable, databaseUses);

        invalidate();
        revalidate();
        repaint();
    }
}

