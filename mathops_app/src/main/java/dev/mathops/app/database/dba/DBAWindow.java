package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.EDbUse;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main window.
 */
final class DBAWindow extends JFrame implements ActionListener, TreeSelectionListener {

    /** A map from database to its connection. */
    private final Map<Database, Login> logins;

    /** The database ribbon. */
    private final DatabaseRibbon ribbon;

    /** The tree showing schemas and their tables. */
    private final SchemaTableTree schemaTableTree;

    /** The panel that shows the currently-selected table. */
    private final TablePane tablePane;

    /** The list of currently-selected database uses. */
    private final List<DatabaseUse> selectedDatabaseUses;

    /**
     * Constructs a new {@code DBAWindow}.
     *
     * @param config the database configuration
     */
    DBAWindow(final DatabaseConfig config) {

        super("Math Database Administrator");

        this.logins = new HashMap<>(10);
        this.selectedDatabaseUses = new ArrayList<>(10);

        final JPanel content = new JPanel(new StackedBorderLayout());
        setContentPane(content);

        this.ribbon = new DatabaseRibbon(config, this, this.logins);
        content.add(this.ribbon, StackedBorderLayout.NORTH);

        this.tablePane = new TablePane(config);
        content.add(this.tablePane, StackedBorderLayout.CENTER);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int minWidth = Math.max(1024, screen.width / 2);
        final int w = Math.min(screen.width, minWidth);
        final int minHeight = Math.max(768, screen.height * 2 / 3);
        final int h = Math.min(screen.height, minHeight);
        content.setPreferredSize(new Dimension(w, h));

        this.schemaTableTree = new SchemaTableTree(this);
        content.add(this.schemaTableTree, StackedBorderLayout.WEST);
    }

    /**
     * Called when an action is invoked (a checkbox in the database ribbon is toggled).
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        if (DatabaseRibbon.CMD_GEN_FIELDS.equals(cmd)) {
            genFieldDefs();
        } else {
            this.ribbon.getSelectedDatabaseUses(this.selectedDatabaseUses);

            final SchemaTable sel = this.schemaTableTree.getSelection();
            this.tablePane.select(sel, this.selectedDatabaseUses, this.logins);
        }
    }

    /**
     * Generates field definitions from the production Informix database.
     */
    private void genFieldDefs() {

        Log.info("Generating field defs.");

        // Find the Informix PROD login
        Data ifxData = null;
        Login ifxLogin = null;
        outer:
        for (final Map.Entry<Database, Login> entry : this.logins.entrySet()) {
            final Database database = entry.getKey();
            final Login login = entry.getValue();

            if (database.server.type == EDbProduct.INFORMIX && "math".equals(login.user)) {
                for (final Data data : database.getData()) {
                    if (data.use == EDbUse.PROD) {
                        ifxData = data;
                        ifxLogin = login;
                        break outer;
                    }
                }
            }
        }

        if (ifxData == null) {
            JOptionPane.showMessageDialog(this, "Unable to identify Informix PROD database", "Generate FieldDefs",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            final DbConnection conn = ifxLogin.checkOutConnection();

            try {
                // NOTE: not in try-with-resource since we don't want the connection auto-closed
                final Connection jdbc = conn.getConnection();
                final DatabaseMetaData meta = jdbc.getMetaData();

                final List<String> cats = new ArrayList<>(200);
                final List<String> schemas = new ArrayList<>(200);
                final List<String> tables = new ArrayList<>(200);

                try (final ResultSet rs = meta.getTables(null, ifxData.prefix, null, null)) {
                    while (rs.next()) {
                        final String cat = rs.getString("TABLE_CAT");
                        final String schema = rs.getString("TABLE_SCHEM");
                        final String table = rs.getString("TABLE_NAME");
                        cats.add(cat);
                        schemas.add(schema);
                        tables.add(table);
                    }
                }

                final int size = cats.size();

                for (int i = 0; i < size; ++i) {

                    final String cat = cats.get(i);
                    final String schema = schemas.get(i);
                    final String table = tables.get(i);

                    Log.info("CAT: ", cat, ", SCHEMA: ", schema, ", TABLE: ", table);

                    try (final ResultSet rs2 = meta.getColumns(cat, schema, table, null)) {
                        while (rs2.next()) {
                            final String colName = rs2.getString("COLUMN_NAME");
                            final int colType = rs2.getInt("DATA_TYPE");
                            final int colSize = rs2.getInt("COLUMN_SIZE");
                            final int colDigits = rs2.getInt("DECIMAL_DIGITS");
                            final int nullable = rs2.getInt("NULLABLE");

                            Log.info("    Column: ", colName, ", Type: ", colType, ", Size: ", colSize, ", Digits: ",
                                    colDigits, ", Nullable: ", nullable);
                        }
                    }
                }
            } catch (final SQLException ex) {
                final String[] msg = {"Unable to access database table", ex.getLocalizedMessage()};
                JOptionPane.showMessageDialog(this, msg, "Manage Table", JOptionPane.ERROR_MESSAGE);
            } finally {
                ifxLogin.checkInConnection(conn);
            }
        }
    }

    /**
     * Called when the tree selection changes in the schema/table tree view.
     *
     * @param e the event to be processed
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        final SchemaTable sel = this.schemaTableTree.getSelection();
        this.tablePane.select(sel, this.selectedDatabaseUses, this.logins);
    }
}
