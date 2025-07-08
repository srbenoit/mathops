package dev.mathops.app.database.dba;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * A panel that displays a database table when multiple databases are selected.
 */
final class TablePaneMulti extends JPanel {

    private final JLabel schemaName;

    private final JLabel tableName;

    /**
     * Constructs a new {@code TablePaneMulti}.
     *
     * @param config the database configuration
     */
    TablePaneMulti(final DatabaseConfig config) {

        super(new StackedBorderLayout());

        final Color bg = getBackground();
        final boolean bgLight = InterfaceUtils.isLight(bg);
        final Color accent = InterfaceUtils.createAccentColor(bg, bgLight);
        final Color newBg = bgLight ? bg.brighter() : bg.darker();
        final Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, accent);

        final Font font = getFont();
        final int fontSize = font.getSize();
        final Font larger = font.deriveFont((float) fontSize * 1.2f);

        final Color color = getForeground();
        final boolean isLight = InterfaceUtils.isLight(color);
        final Color labelColor = isLight ? new Color(255, 255, 150) : new Color(160, 0, 0);

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
    }

    /**
     * Updates the schema and table this panel shows and the databases holding the data.
     *
     * @param schemaTable  the schema and table; null if none is selected
     * @param databaseUses the selected database uses
     */
    void update(final SchemaTable schemaTable, final List<DatabaseUse> databaseUses) {

        if (schemaTable == null) {
            this.schemaName.setText(CoreConstants.SPC);
            this.tableName.setText(CoreConstants.SPC);
        } else {
            final ESchema schema = schemaTable.schema();
            final String schemaStr = schema.name();
            this.schemaName.setText(schemaStr);
            final String tableStr = schemaTable.table();
            this.tableName.setText(tableStr);
        }
    }
}
