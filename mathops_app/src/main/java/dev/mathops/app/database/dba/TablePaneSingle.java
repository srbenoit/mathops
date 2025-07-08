package dev.mathops.app.database.dba;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Server;

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

    /** A label that will receive the name of the selected schema. */
    private final JLabel schemaName;

    /** A label that will receive the name of the selected table. */
    private final JLabel tableName;

    /** A label that will receive the name of the selected server. */
    private final JLabel serverId;

    /** A label that will receive the name of the selected database. */
    private final JLabel databaseId;

    /** A label that will receive the name of the selected use. */
    private final JLabel useName;

    private final PaneManage manage;

    private final PaneSQL sql;

    private final PaneInformation info;

    /**
     * Constructs a new {@code TablePaneSingle}.
     *
     * @param config the database configuration
     */
    TablePaneSingle(final DatabaseConfig config) {

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

        final JLabel lbl3 = new JLabel("   Server: ");
        lbl3.setFont(larger);
        topFlow.add(lbl3);
        this.serverId = new JLabel(CoreConstants.SPC);
        this.serverId.setFont(larger);
        this.serverId.setForeground(labelColor);
        topFlow.add(this.serverId);

        final JLabel lbl4 = new JLabel("   Database: ");
        lbl4.setFont(larger);
        topFlow.add(lbl4);
        this.databaseId = new JLabel(CoreConstants.SPC);
        this.databaseId.setFont(larger);
        this.databaseId.setForeground(labelColor);
        topFlow.add(this.databaseId);
        this.useName = new JLabel(CoreConstants.SPC);
        this.useName.setFont(larger);
        this.useName.setForeground(labelColor);
        topFlow.add(this.useName);

        add(topFlow, StackedBorderLayout.NORTH);

        this.manage = new PaneManage(config, accent);
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
     * @param databaseUse the selected database use
     * @param login       the database login from which to obtain connections
     */
    void update(final SchemaTable schemaTable, final DatabaseUse databaseUse, final Login login) {

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

        if (databaseUse == null) {
            this.serverId.setText(CoreConstants.SPC);
            this.databaseId.setText(CoreConstants.SPC);
            this.useName.setText(CoreConstants.SPC);
        } else {
            final Database database = databaseUse.database();
            final Server server = database.server;
            final String txt = server.host.replace(".colostate.edu", "") + ":" + server.port;
            this.serverId.setText(txt);

            this.databaseId.setText(database.id);
            final EDbUse use = databaseUse.use();
            final String useStr = use.name();
            this.useName.setText(" (" + useStr + ")");
        }

        this.manage.update(schemaTable, databaseUse, login);
        this.sql.update(schemaTable, databaseUse);
        this.info.update(schemaTable, databaseUse);
    }
}
