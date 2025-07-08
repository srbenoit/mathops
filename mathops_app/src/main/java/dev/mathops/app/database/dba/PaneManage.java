package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that lets the user perform a query of a database table, update rows, delete rows, or add new rows.
 */
final class PaneManage extends JPanel implements ActionListener {

    /** An action command. */
    private static final String QUERY_CMD = "QUERY";

    /** A panel to show query fields. */
    private final JPanel queryFields;

    /** The columns found in the active table; empty if there is no active table. */
    private final List<Column> activeTableColumns;

    /** The fields to query each active table column. */
    private final List<JTextField> activeTableQueryFields;

    /**
     * Constructs a new {@code PaneManage}.
     *
     * @param theConfig the database configuration
     */
    PaneManage(final DatabaseConfig theConfig, final Color accent) {

        super(new StackedBorderLayout());

        final Border padding = BorderFactory.createEmptyBorder(5, 8, 5, 8);
        final Border leftRightLines = BorderFactory.createMatteBorder(0, 1, 0, 1, accent);
        final Border center = BorderFactory.createCompoundBorder(leftRightLines, padding);

        // Left pane is query criteria and a "QUERY" button.

        final JPanel queryCriteria = new JPanel(new StackedBorderLayout());
        queryCriteria.setPreferredSize(new Dimension(300, 300));
        queryCriteria.setBorder(padding);
        add(queryCriteria, StackedBorderLayout.WEST);

        final JLabel queryHeader = new JLabel("Query Criteria:");
        queryCriteria.add(queryHeader, StackedBorderLayout.NORTH);

        this.queryFields = new JPanel(new StackedBorderLayout());
        queryCriteria.add(this.queryFields, StackedBorderLayout.CENTER);

        final JButton queryButton = new JButton("Query");
        queryButton.setActionCommand(QUERY_CMD);
        queryButton.addActionListener(this);
        final JPanel queryButtonFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 4));
        queryButtonFlow.add(queryButton);
        queryCriteria.add(queryButtonFlow, StackedBorderLayout.SOUTH);

        // Middle pane is a table of results - as the selected row changes, the record to the right is updated.
        final JPanel recordsGrid = new JPanel(new StackedBorderLayout());
        recordsGrid.setPreferredSize(new Dimension(300, 300));
        recordsGrid.setBorder(center);
        add(recordsGrid, StackedBorderLayout.CENTER);

        final JLabel resultsHeader = new JLabel("Query Results:");
        recordsGrid.add(resultsHeader, StackedBorderLayout.NORTH);

        // Right pane is a record display with "UPDATE", "ADD', and "DELETE" buttons for the selected row.

        final JPanel recordDisplay = new JPanel(new StackedBorderLayout());
        recordDisplay.setPreferredSize(new Dimension(300, 300));
        recordDisplay.setBorder(padding);
        add(recordDisplay, StackedBorderLayout.EAST);

        final JLabel recordHeader = new JLabel("Active Record:");
        recordDisplay.add(recordHeader, StackedBorderLayout.NORTH);

        this.activeTableColumns = new ArrayList<>(40);
        this.activeTableQueryFields = new ArrayList<>(40);
    }

    /**
     * Updates the schema and table this panel shows and the database holding the data.
     *
     * @param schemaTable the schema and table; null if none is selected
     * @param databaseUse the selected database use
     * @param login       the database login from which to obtain connections
     */
    void update(final SchemaTable schemaTable, final DatabaseUse databaseUse, final Login login) {

        this.queryFields.removeAll();
        this.activeTableColumns.clear();

        if (schemaTable == null || databaseUse == null || login == null) {
            // TODO: Clear the display
        } else {
            final Database database = databaseUse.database();

            // Find the "Data" object that represents the selected schema and use
            Data data = null;
            for (final Data test : database.getData()) {
                if (test.schema == schemaTable.schema() && test.use == databaseUse.use()) {
                    data = test;
                    break;
                }
            }

            if (data != null) {
                final DbConnection conn = login.checkOutConnection();
                try {
                    final Connection jdbc = conn.getConnection();
                    final DatabaseMetaData meta = jdbc.getMetaData();
                    final ResultSet rs = meta.getTables(null, data.prefix, schemaTable.table(), null);
                    String cat = null;
                    String schema = null;
                    String table = null;
                    while (rs.next()) {
                        cat = rs.getString("TABLE_CAT");
                        schema = rs.getString("TABLE_SCHEM");
                        table = rs.getString("TABLE_NAME");
                        break;
                    }

                    if (table != null) {

                        final ResultSet rs2 = meta.getColumns(cat, schema, table, null);
                        while (rs2.next()) {
                            final String colName = rs2.getString("COLUMN_NAME");
                            final int colType = rs2.getInt("DATA_TYPE");
                            final int colSize = rs2.getInt("COLUMN_SIZE");
                            final int colDigits = rs2.getInt("DECIMAL_DIGITS");
                            final int nullable = rs2.getInt("NULLABLE");

                            final Column col = new Column(colName, colType, colSize, colDigits, nullable);
                            this.activeTableColumns.add(col);
                        }

                        final int numColumns = this.activeTableColumns.size();
                        if (numColumns > 0) {
                            final JLabel[] fieldNames = new JLabel[numColumns];
                            for (int i = 0; i < numColumns; ++i) {
                                final Column col = this.activeTableColumns.get(i);
                                fieldNames[i] = new JLabel(col.name() + ":");
                            }
                            UIUtilities.makeLabelsSameSizeRightAligned(fieldNames);

                            for (int i = 0; i < numColumns; ++i) {
                                final Column col = this.activeTableColumns.get(i);

                                final JPanel columnFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
                                columnFlow.add(fieldNames[i]);

                                final JTextField field = new JTextField(15);
                                this.activeTableQueryFields.add(field);
                                columnFlow.add(field);

                                this.queryFields.add(columnFlow, StackedBorderLayout.NORTH);
                            }

                            this.queryFields.invalidate();
                            this.queryFields.revalidate();
                            this.queryFields.repaint();
                        }
                    }
                } catch (final SQLException ex) {
                    final String[] msg = {"Unable to access database table", ex.getLocalizedMessage()};
                    JOptionPane.showMessageDialog(this, msg, "Manage Table", JOptionPane.ERROR_MESSAGE);
                } finally {
                    login.checkInConnection(conn);
                }
            }
        }
    }

    /**
     * Called when an action in invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (QUERY_CMD.equals(cmd)) {
            performQuery();
        } else {
            Log.info("Action: ", cmd);
        }
    }

    /**
     * Performs a query and populates the results table.
     */
    private void performQuery() {

    }

    /**
     * A column in a table.
     *
     * @param name     the column name
     * @param type     the SQL type (java.sql.Types)
     * @param size     the size, for character types
     * @param digits   the number of digits after the decimal, for decimal types
     * @param nullable 1 if nullable, 0 if not
     */
    private record Column(String name, int type, int size, int digits, int nullable) {
    }
}

