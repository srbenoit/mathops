package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that lets the user perform a query of a database table, update rows, delete rows, or add new rows.
 */
final class PaneManage extends JPanel implements ActionListener, ListSelectionListener {

    /** An action command. */
    public static final String QUERY_CMD = "QUERY";

    /** A panel to show query fields. */
    private final PaneQueryCriteria queryFields;

    /** The active login. */
    private Login activeLogin;

    /** The qualified table name of the active table. */
    private String activeTableName;

    /** The columns found in the active table; empty if there is no active table. */
    private final List<Column> activeTableColumns;

    /** The table model for the results table. */
    private final ResultsTableModel resultsTableModel;

    /** The table for results. */
    private final JTable resultsTable;

    /** A panel to display the selected record. */
    private final PaneActiveRecord activeRecord;

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

        //
        // Left pane is query criteria and a "QUERY" button.
        //

        this.queryFields = new PaneQueryCriteria(this);
        add(this.queryFields, StackedBorderLayout.WEST);

        //
        // Middle pane is a table of results - as the selected row changes, the record to the right is updated.
        //

        final JPanel recordsGrid = new JPanel(new StackedBorderLayout());
        recordsGrid.setPreferredSize(new Dimension(300, 300));
        recordsGrid.setBorder(center);
        add(recordsGrid, StackedBorderLayout.CENTER);

        final JLabel resultsHeader = new JLabel("Query Results:");
        recordsGrid.add(resultsHeader, StackedBorderLayout.NORTH);

        this.resultsTableModel = new ResultsTableModel();
        this.resultsTable = new JTable(this.resultsTableModel);
        this.resultsTable.setShowGrid(true);
        this.resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        final ListSelectionModel selectionModel = this.resultsTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(this);
        final JScrollPane resultsScroll = new JScrollPane(this.resultsTable);
        recordsGrid.add(resultsScroll, StackedBorderLayout.CENTER);

        //
        // Right pane is a record display with "UPDATE", "ADD', and "DELETE" buttons for the selected row.
        //

        this.activeRecord = new PaneActiveRecord();
        add(this.activeRecord, StackedBorderLayout.EAST);

        // Initialize storage for active table

        this.activeTableColumns = new ArrayList<>(40);
    }

    /**
     * Updates the schema and table this panel shows and the database holding the data.
     *
     * @param schemaTable the schema and table; null if none is selected
     * @param databaseUse the selected database use
     * @param login       the database login from which to obtain connections
     */
    void update(final SchemaTable schemaTable, final DatabaseUse databaseUse, final Login login) {

        this.queryFields.clear();
        this.activeTableColumns.clear();

        if (schemaTable == null || databaseUse == null || login == null) {
            this.activeLogin = null;
            this.activeTableName = null;
        } else {
            this.activeLogin = login;

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
                        this.activeTableName = schema == null || schema.isBlank() ? table : (schema + "." + table);

                        final ResultSet rs2 = meta.getColumns(cat, schema, table, null);
                        while (rs2.next()) {
                            final String colName = rs2.getString("COLUMN_NAME");
                            final int colType = rs2.getInt("DATA_TYPE");
                            final int colSize = rs2.getInt("COLUMN_SIZE");
                            final int colDigits = rs2.getInt("DECIMAL_DIGITS");
                            final int nullable = rs2.getInt("NULLABLE");

                            final Class<?> cls = determineValueClass(colType);

                            final Column col = new Column(colName, colType, cls, colSize, colDigits, nullable);
                            this.activeTableColumns.add(col);
                        }

                        this.queryFields.update(this.activeTableColumns);
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
     * Determines the value class based on a column type code.
     *
     * @param colType the column type code
     * @return the value class
     */
    private static Class<?> determineValueClass(final int colType) {

        final Class<?> cls;
        if (isIntegerType(colType)) {
            cls = Long.class;
        } else if (isNumericType(colType)) {
            cls = Double.class;
        } else if (isStringType(colType)) {
            cls = String.class;
        } else if (colType == Types.DATE) {
            cls = Date.class;
        } else if (colType == Types.TIME) {
            cls = Time.class;
        } else if (colType == Types.TIMESTAMP) {
            cls = Timestamp.class;
        } else if (colType == Types.BOOLEAN) {
            cls = Boolean.class;
        } else {
            cls = Object.class;
        }
        return cls;
    }

    /**
     * Tests whether a column is of an integer type.
     *
     * @param type the type (a value from {@code java.sql.Types)
     * @return true if the type is an integer type
     */
    static boolean isIntegerType(final int type) {

        return type == Types.BIT || type == Types.TINYINT || type == Types.SMALLINT || type == Types.INTEGER
               || type == Types.BIGINT || type == Types.ROWID;
    }

    /**
     * Tests whether a column is of a numeric type (after we know it is not an integer type).
     *
     * @param type the type (a value from {@code java.sql.Types)
     * @return true if the type is a numeric type
     */
    static boolean isNumericType(final int type) {

        return type == Types.FLOAT || type == Types.REAL || type == Types.DOUBLE
               || type == Types.NUMERIC || type == Types.DECIMAL;
    }

    /**
     * Tests whether a column is of a string type.
     *
     * @param type the type (a value from {@code java.sql.Types)
     * @return true if the type is a string type
     */
    static boolean isStringType(final int type) {

        return type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARCHAR
               || type == Types.CLOB || type == Types.NCHAR || type == Types.NVARCHAR
               || type == Types.LONGNVARCHAR || type == Types.NCLOB || type == Types.SQLXML;
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

        if (this.activeLogin == null || this.activeTableColumns.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Query attempted with no active table", "Query",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            this.resultsTableModel.deleteAllRows();

            final DbConnection conn = this.activeLogin.checkOutConnection();

            try {
                this.queryFields.performQuery(conn, this.activeTableName, this.activeTableColumns, this);
            } catch (final SQLException ex) {
                final String[] msg = {"There was an error executing the query:", ex.getLocalizedMessage()};
                JOptionPane.showMessageDialog(this, msg, "Query", JOptionPane.ERROR_MESSAGE);
            } finally {
                this.activeLogin.checkInConnection(conn);
            }
        }
    }

    /**
     * Populates the results table with the output of a query.
     *
     * @param rs the result set
     * @throws SQLException if there is an error reading results from the database
     */
    void populateResults(final ResultSet rs) throws SQLException {

        final int numColumns = this.activeTableColumns.size();
        if (numColumns > 0) {
            this.resultsTableModel.setColumns(this.activeTableColumns);

            final Object[] data = new Object[numColumns];

            while (rs.next()) {
                for (int i = 0; i < numColumns; ++i) {
                    final Column col = this.activeTableColumns.get(i);
                    final String colName = col.name();
                    final int type = col.type();

                    if (isIntegerType(type)) {
                        final long value = rs.getLong(colName);
                        data[i] = Long.valueOf(value);
                    } else if (isNumericType(type)) {
                        final double value = rs.getDouble(colName);
                        data[i] = Double.valueOf(value);
                    } else if (isStringType(type)) {
                        data[i] = rs.getString(colName);
                    } else if (type == Types.DATE) {
                        data[i] = rs.getDate(colName);
                    } else if (type == Types.TIME) {
                        data[i] = rs.getTime(colName);
                    } else if (type == Types.TIMESTAMP) {
                        data[i] = rs.getTimestamp(colName);
                    } else if (type == Types.BOOLEAN) {
                        final boolean b = rs.getBoolean(colName);
                        data[i] = Boolean.valueOf(b);
                    } else {
                        data[i] = rs.getObject(colName);
                    }
                }

                this.resultsTableModel.addRow(data);
            }

            this.resultsTableModel.notifyAllRowsAdded();
        }
    }

    /**
     * Called when the selected record in the results table changes.
     *
     * @param e the event that characterizes the change.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        if (!e.getValueIsAdjusting()) {
            final int index = e.getFirstIndex();
            final int numRows = this.resultsTableModel.getRowCount();
            if (index < numRows) {
                final Object[] data = this.resultsTableModel.getRow(index);
                this.activeRecord.update(this.activeTableColumns, data);
            }
        }
    }
}

