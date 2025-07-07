package dev.mathops.app.database.dba;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Database;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * A panel that displays a database table when a single database is selected.
 */
final class TablePaneSingle extends JPanel {

    private final JLabel schemaName;

    private final JLabel tableName;

    private final JLabel databaseId;

    private final PaneManage manage;

    private final PaneSQL sql;

    private final PaneInformation info;

    /**
     * Constructs a new {@code TablePaneSingle}.
     */
    TablePaneSingle() {

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
        final JLabel lbl3 = new JLabel("   Database: ");
        lbl3.setFont(larger);
        topFlow.add(lbl3);
        this.databaseId = new JLabel(CoreConstants.SPC);
        this.databaseId.setFont(larger);
        this.databaseId.setForeground(labelColor);
        topFlow.add(this.databaseId);
        add(topFlow, StackedBorderLayout.NORTH);

        this.manage = new PaneManage();
        this.sql = new PaneSQL();
        this.info = new PaneInformation();

        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage", this.manage);
        tabs.addTab("SQL", this.sql);
        tabs.addTab("Information", this.info);

        add(tabs, StackedBorderLayout.CENTER);
    }

    /**
     * Updates the schema and table this panel shows and the database holding the data.
     *
     * @param schemaTable the schema and table; null if none is selected
     * @param database    the selected database
     */
    void update(final SchemaTable schemaTable, final Database database) {

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

        if (database == null) {
            this.databaseId.setText(CoreConstants.SPC);
        } else {
            this.databaseId.setText(database.id);
        }

        this.manage.update(schemaTable, database);
        this.sql.update(schemaTable, database);
        this.info.update(schemaTable, database);
    }
}
