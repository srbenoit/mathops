package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A panel that lets the user perform a query of a database table, update rows, delete rows, or add new rows.
 */
final class PaneManage extends JPanel implements ActionListener, ListSelectionListener {

    /** An action command. */
    private static final String QUERY_CMD = "QUERY";

    /** A panel to show query fields. */
    private final JPanel queryFields;

    /** The active schema table. */
    private SchemaTable activeSchemaTable;

    /** The active database use. */
    private DatabaseUse activeDatabaseUse;

    /** The active login. */
    private Login activeLogin;

    /** The qualified table name of the active table. */
    private String activeTableName;

    /** The columns found in the active table; empty if there is no active table. */
    private final List<Column> activeTableColumns;

    /** The fields to query each active table column. */
    private final List<JTextField> activeTableQueryFields;

    /** The normal background color for a text field. */
    private final Color normalFieldBg;

    /** The "error" background color for a text field. */
    private final Color errorFieldBg;

    /** The table model for the results table. */
    private final ResultsTableModel resultsTableModel;

    /** The table for results. */
    private final JTable resultsTable;

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

        final JTextField field = new JTextField();
        this.normalFieldBg = field.getBackground();
        final boolean isLight = InterfaceUtils.isLight(this.normalFieldBg);
        final int normalRed = this.normalFieldBg.getRed();
        final int normalGreen = this.normalFieldBg.getGreen();
        final int normalBlue = this.normalFieldBg.getBlue();
        this.errorFieldBg = isLight ? new Color(255, normalGreen - 10, normalBlue - 10)
                : new Color(normalRed + 50, normalGreen, normalBlue);

        //
        // Left pane is query criteria and a "QUERY" button.
        //

        final JPanel queryCriteria = new JPanel(new StackedBorderLayout());
        queryCriteria.setPreferredSize(new Dimension(300, 300));
        queryCriteria.setBorder(padding);
        add(queryCriteria, StackedBorderLayout.WEST);

        final JLabel queryHeader = new JLabel("Query Criteria:");
        queryCriteria.add(queryHeader, StackedBorderLayout.NORTH);

        this.queryFields = new JPanel(new StackedBorderLayout());
        final JScrollPane queryScroll = new JScrollPane(this.queryFields);
        queryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        queryCriteria.add(queryScroll, StackedBorderLayout.CENTER);

        final JButton queryButton = new JButton("Query");
        queryButton.setActionCommand(QUERY_CMD);
        queryButton.addActionListener(this);
        final JPanel queryButtonFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 4));
        queryButtonFlow.add(queryButton);
        queryCriteria.add(queryButtonFlow, StackedBorderLayout.SOUTH);

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
        this.resultsTable.getSelectionModel().addListSelectionListener(this);
        final JScrollPane resultsScroll = new JScrollPane(this.resultsTable);
        recordsGrid.add(resultsScroll, StackedBorderLayout.CENTER);

        //
        // Right pane is a record display with "UPDATE", "ADD', and "DELETE" buttons for the selected row.
        //

        final JPanel recordDisplay = new JPanel(new StackedBorderLayout());
        recordDisplay.setPreferredSize(new Dimension(300, 300));
        recordDisplay.setBorder(padding);
        add(recordDisplay, StackedBorderLayout.EAST);

        final JLabel recordHeader = new JLabel("Active Record:");
        recordDisplay.add(recordHeader, StackedBorderLayout.NORTH);

        // Initialize storage for active table

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
        this.activeTableQueryFields.clear();

        if (schemaTable == null || databaseUse == null || login == null) {
            this.activeSchemaTable = null;
            this.activeDatabaseUse = null;
            this.activeLogin = null;
            this.activeTableName = null;
        } else {
            this.activeSchemaTable = schemaTable;
            this.activeDatabaseUse = databaseUse;
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

                        final int numColumns = this.activeTableColumns.size();
                        if (numColumns > 0) {
                            final JLabel[] fieldNames = new JLabel[numColumns];
                            for (int i = 0; i < numColumns; ++i) {
                                final Column col = this.activeTableColumns.get(i);
                                fieldNames[i] = new JLabel(col.name() + ":");
                            }
                            UIUtilities.makeLabelsSameSizeRightAligned(fieldNames);

                            for (int i = 0; i < numColumns; ++i) {

                                final JPanel columnFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
                                columnFlow.add(fieldNames[i]);

                                final Column col = this.activeTableColumns.get(i);
                                final int type = col.type();
                                if (canBeQueryCriteria(type)) {
                                    final JTextField field = new JTextField(15);
                                    this.activeTableQueryFields.add(field);
                                    columnFlow.add(field);
                                }

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
     * Tests whether a column is of a type we can use as a query criteria.
     *
     * @param type the type (a value from {@code java.sql.Types)
     * @return true if the type can be used as a query criteria
     */
    private static boolean canBeQueryCriteria(final int type) {

        return type == Types.BIT || type == Types.TINYINT || type == Types.SMALLINT
               || type == Types.INTEGER || type == Types.BIGINT || type == Types.ROWID
               || type == Types.FLOAT || type == Types.REAL || type == Types.DOUBLE
               || type == Types.NUMERIC || type == Types.DECIMAL || type == Types.CHAR
               || type == Types.VARCHAR || type == Types.LONGVARCHAR || type == Types.CLOB
               || type == Types.NCHAR || type == Types.NVARCHAR || type == Types.LONGNVARCHAR
               || type == Types.NCLOB || type == Types.SQLXML || type == Types.DATE
               || type == Types.TIME || type == Types.TIMESTAMP || type == Types.TIME_WITH_TIMEZONE
               || type == Types.TIMESTAMP_WITH_TIMEZONE || type == Types.BOOLEAN;
    }

    /**
     * Tests whether a column is of an integer type.
     *
     * @param type the type (a value from {@code java.sql.Types)
     * @return true if the type is an integer type
     */
    private static boolean isIntegerType(final int type) {

        return type == Types.BIT || type == Types.TINYINT || type == Types.SMALLINT || type == Types.INTEGER
               || type == Types.BIGINT || type == Types.ROWID;
    }

    /**
     * Tests whether a column is of a numeric type (after we know it is not an integer type).
     *
     * @param type the type (a value from {@code java.sql.Types)
     * @return true if the type is a numeric type
     */
    private static boolean isNumericType(final int type) {

        return type == Types.FLOAT || type == Types.REAL || type == Types.DOUBLE
               || type == Types.NUMERIC || type == Types.DECIMAL;
    }

    /**
     * Tests whether a column is of a string type.
     *
     * @param type the type (a value from {@code java.sql.Types)
     * @return true if the type is a string type
     */
    private static boolean isStringType(final int type) {

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
                performQuery(conn);
            } catch (final SQLException ex) {
                final String[] msg = {"There was an error executing the query:", ex.getLocalizedMessage()};
                JOptionPane.showMessageDialog(this, msg, "Query", JOptionPane.ERROR_MESSAGE);
            } finally {
                this.activeLogin.checkInConnection(conn);
            }
        }
    }

    /**
     * Performs a query and populates the results table.
     *
     * @param conn the database connection
     */
    private void performQuery(final DbConnection conn) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(200);
        sql.add("SELECT * FROM ", this.activeTableName);

        int state = 1;
        final int numColumns = this.activeTableColumns.size();
        final int numFields = this.activeTableQueryFields.size();
        final int count = Math.min(numColumns, numFields);

        final List<Object> parameters = new ArrayList<>(count);

        for (int i = 0; i < count; ++i) {
            final JTextField field = this.activeTableQueryFields.get(i);
            if (field == null) {
                continue;
            }
            final String fieldText = field.getText();
            if (fieldText.isEmpty()) {
                field.setBackground(this.normalFieldBg);
            } else {
                final Column column = this.activeTableColumns.get(i);
                final int type = column.type();

                if (isIntegerType(type)) {
                    state = appendIntegerCriteria(state, sql, column, field, fieldText, parameters);
                } else if (isNumericType(type)) {
                    state = appendNumberCriteria(state, sql, column, field, fieldText, parameters);
                } else if (isStringType(type)) {
                    state = appendStringCriteria(state, sql, column, field, fieldText, parameters);
                } else if (type == Types.DATE) {
                    state = appendDateCriteria(state, sql, column, field, fieldText, parameters);
                } else if (type == Types.TIME) {
                    state = appendTimeCriteria(state, sql, column, field, fieldText, parameters);
                } else if (type == Types.TIMESTAMP) {
                    state = appendTimestampCriteria(state, sql, column, field, fieldText, parameters);
                } else if (type == Types.BOOLEAN) {
                    state = appendBooleanCriteria(state, sql, column, field, fieldText, parameters);
                }
            }
        }

        if (state >= 0) {
            final String sqlStr = sql.toString();

            try (final PreparedStatement prepStatement = conn.prepareStatement(sqlStr)) {
                final int numParams = parameters.size();
                for (int i = 0; i < numParams; ++i) {
                    final Object value = parameters.get(i);

                    if (value instanceof final Long longValue) {
                        final long l = longValue.longValue();
                        prepStatement.setLong(i + 1, l);
                    } else if (value instanceof final Double doubleValue) {
                        final double d = doubleValue.doubleValue();
                        prepStatement.setDouble(i + 1, d);
                    } else if (value instanceof final String stringValue) {
                        prepStatement.setString(i + 1, stringValue);
                    } else if (value instanceof final Date dateValue) {
                        prepStatement.setDate(i + 1, dateValue);
                    } else if (value instanceof final Time timeValue) {
                        prepStatement.setTime(i + 1, timeValue);
                    } else if (value instanceof final Timestamp timestampValue) {
                        prepStatement.setTimestamp(i + 1, timestampValue);
                    } else if (value instanceof final Boolean booleanValue) {
                        final boolean b = booleanValue.booleanValue();
                        prepStatement.setBoolean(i + 1, b);
                    }
                }

                final ResultSet rs = prepStatement.executeQuery();
                populateResults(rs);
            }
        }
    }

    /**
     * Populates the results table with the output of a query.
     *
     * @param rs the result set
     * @throws SQLException if there is an error reading results from the database
     */
    private void populateResults(final ResultSet rs) throws SQLException {

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
     * Appends an SQL for qn integer query criterion.  If the field is an integer or "=" followed by an integer, that
     * value is matched.  If it is "!=", ">", ">=", "<" or "<=" followed by an integer, all rows that satisfy the
     * comparison are matched.
     *
     * @param first      true if this is the first criterion (in which case " WHERE " will be appended before the
     *                   clause), false if not (in which case " AND " will be appended before the clause)
     * @param sql        the {@code HtmlBuilder} to which to add SQL
     * @param column     the column specification
     * @param field      the field (the field background is turned to an error color if the field cannot be
     *                   interpreted)
     * @param fieldText  the non-empty query text
     * @param parameters a list to which to add the field value to set in the prepared statement (if a parametrized
     *                   WHERE clause is added to the SQL, a corresponding value will be added to this list)
     * @return {@code first} if no clause was appended or if {@code first} was -1 on entry; 0 if a clause was appended;
     *         -1 if an error occurred
     */
    private int appendIntegerCriteria(final int first, final HtmlBuilder sql, final Column column,
                                      final JTextField field, final String fieldText,
                                      final Collection<Object> parameters) {

        boolean error = false;

        final Long value;
        final String op;

        try {
            if (fieldText.startsWith(">=") || fieldText.startsWith("<=") || fieldText.startsWith("!=")) {
                final String valueStr = fieldText.substring(2);
                value = Long.valueOf(valueStr);
                op = fieldText.substring(0, 2);
            } else if (fieldText.startsWith(">") || fieldText.startsWith("<") || fieldText.startsWith("=")) {
                final String valueStr = fieldText.substring(1);
                value = Long.valueOf(valueStr);
                op = fieldText.substring(0, 1);
            } else {
                value = Long.valueOf(fieldText);
                op = "=";
            }

            final String colName = column.name();
            sql.add((first == 1 ? " WHERE " : " AND "), colName, op, "?");
            parameters.add(value);
        } catch (final NumberFormatException ex) {
            error = true;
        }

        int result;

        if (error) {
            field.setBackground(this.errorFieldBg);
            result = -1;
        } else {
            result = first == -1 ? -1 : 0;
            field.setBackground(this.normalFieldBg);
        }

        return result;
    }

    /**
     * Appends an SQL for q numeric query criterion  If the field is a number or "=" followed by a number, that value is
     * matched. If it is "!=", ">", ">=", "<" or "<=" followed by a number, all rows that satisfy the comparison are
     * matched.
     *
     * @param first      true if this is the first criterion (in which case " WHERE " will be appended before the
     *                   clause), false if not (in which case " AND " will be appended before the clause)
     * @param sql        the {@code HtmlBuilder} to which to add SQL
     * @param column     the column specification
     * @param field      the field (the field background is turned to an error color if the field cannot be
     *                   interpreted)
     * @param fieldText  the non-empty query text
     * @param parameters a list to which to add the field value to set in the prepared statement (if a parametrized
     *                   WHERE clause is added to the SQL, a corresponding val
     * @return {@code first} if no clause was appended or if {@code first} was -1 on entry; 0 if a clause was appended;
     *         -1 if an error occurred
     */
    private int appendNumberCriteria(final int first, final HtmlBuilder sql, final Column column,
                                     final JTextField field, final String fieldText,
                                     final Collection<Object> parameters) {

        final double value;
        final String op;
        boolean error = false;

        try {
            if (fieldText.startsWith(">=") || fieldText.startsWith("<=") || fieldText.startsWith("!=")) {
                final String valueStr = fieldText.substring(2);
                value = Double.parseDouble(valueStr);
                op = fieldText.substring(0, 2);
            } else if (fieldText.startsWith(">") || fieldText.startsWith("<") || fieldText.startsWith("=")) {
                final String valueStr = fieldText.substring(1);
                value = Double.parseDouble(valueStr);
                op = fieldText.substring(0, 1);
            } else {
                value = Double.parseDouble(fieldText);
                op = "=";
            }

            if (Double.isFinite(value)) {
                final String colName = column.name();
                sql.add((first == 1 ? " WHERE " : " AND "), colName, op, "?");
                final Double valueObj = Double.valueOf(value);
                parameters.add(valueObj);
            } else {
                error = true;
            }
        } catch (final NumberFormatException ex) {
            error = true;
        }

        int result;

        if (error) {
            field.setBackground(this.errorFieldBg);
            result = -1;
        } else {
            result = first == -1 ? -1 : 0;
            field.setBackground(this.normalFieldBg);
        }

        return result;
    }

    /**
     * Appends an SQL for q string query criterion.  If the input string begins with "LIKE ", what remains is considered
     * m match expression (using '%' to match any sequence of characters and "_" to match a single character).
     * Otherwise, the string is matched exactly.
     *
     * @param first      true if this is the first criterion (in which case " WHERE " will be appended before the
     *                   clause), false if not (in which case " AND " will be appended before the clause)
     * @param sql        the {@code HtmlBuilder} to which to add SQL
     * @param column     the column specification
     * @param field      the field (the field background is turned to an error color if the field cannot be
     *                   interpreted)
     * @param fieldText  the non-empty query text
     * @param parameters a list to which to add the field value to set in the prepared statement (if a parametrized
     *                   WHERE clause is added to the SQL, a corresponding val
     * @return {@code first} if no clause was appended or if {@code first} was -1 on entry; 0 if a clause was appended;
     *         -1 if an error occurred
     */
    private int appendStringCriteria(final int first, final HtmlBuilder sql, final Column column,
                                     final JTextField field, final String fieldText,
                                     final Collection<Object> parameters) {

        final String colName = column.name();

        sql.add(first == 1 ? " WHERE " : " AND ");

        if (fieldText.startsWith("LIKE ")) {
            sql.add(colName, " LIKE ?");
            final String matchText = fieldText.substring(5);
            parameters.add(matchText);
        } else {
            sql.add(colName, "=?");
            parameters.add(fieldText);
        }

        final int result = first == -1 ? -1 : 0;
        field.setBackground(this.normalFieldBg);

        return result;
    }

    /**
     * Appends an SQL for q date query criterion.  If the field contains a date, records with that date are matched. If
     * it contains "=", ">", "<", ">=" or "<=" followed by a date, records with dates that satisfy the comparison are
     * matched.
     *
     * @param first      true if this is the first criterion (in which case " WHERE " will be appended before the
     *                   clause), false if not (in which case " AND " will be appended before the clause)
     * @param sql        the {@code HtmlBuilder} to which to add SQL
     * @param column     the column specification
     * @param field      the field (the field background is turned to an error color if the field cannot be
     *                   interpreted)
     * @param fieldText  the non-empty query text
     * @param parameters a list to which to add the field value to set in the prepared statement (if a parametrized
     *                   WHERE clause is added to the SQL, a corresponding val
     * @return {@code first} if no clause was appended or if {@code first} was -1 on entry; 0 if a clause was appended;
     *         -1 if an error occurred
     */
    private int appendDateCriteria(final int first, final HtmlBuilder sql, final Column column,
                                   final JTextField field, final String fieldText,
                                   final Collection<Object> parameters) {
        boolean error = false;

        final LocalDate value;
        final String op;

        try {
            if (fieldText.startsWith(">=") || fieldText.startsWith("<=")) {
                final String valueStr = fieldText.substring(2);
                value = parseDate(valueStr);
                op = fieldText.substring(0, 2);
            } else if (fieldText.startsWith(">") || fieldText.startsWith("<") || fieldText.startsWith("=")) {
                final String valueStr = fieldText.substring(1);
                value = parseDate(valueStr);
                op = fieldText.substring(0, 1);
            } else {
                value = parseDate(fieldText);
                op = "=";
            }

            if (value == null) {
                // Date could not be parsed
                error = true;
            } else {
                final String colName = column.name();
                sql.add((first == 1 ? " WHERE " : " AND "), colName, op, "?");
                final Date sqlDate = Date.valueOf(value);
                parameters.add(sqlDate);
            }
        } catch (final NumberFormatException ex) {
            error = true;
        }

        int result;

        if (error) {
            field.setBackground(this.errorFieldBg);
            result = -1;
        } else {
            result = first == -1 ? -1 : 0;
            field.setBackground(this.normalFieldBg);
        }

        return result;
    }

    /**
     * Appends an SQL for q time query criterion.  If the field contains a date, records with that date are matched. If
     * it contains "=", ">", "<", ">=" or "<=" followed by a date, records with dates that satisfy the comparison are
     * matched.
     *
     * @param first      true if this is the first criterion (in which case " WHERE " will be appended before the
     *                   clause), false if not (in which case " AND " will be appended before the clause)
     * @param sql        the {@code HtmlBuilder} to which to add SQL
     * @param column     the column specification
     * @param field      the field (the field background is turned to an error color if the field cannot be
     *                   interpreted)
     * @param fieldText  the non-empty query text
     * @param parameters a list to which to add the field value to set in the prepared statement (if a parametrized
     *                   WHERE clause is added to the SQL, a corresponding val
     * @return {@code first} if no clause was appended or if {@code first} was -1 on entry; 0 if a clause was appended;
     *         -1 if an error occurred
     */
    private int appendTimeCriteria(final int first, final HtmlBuilder sql, final Column column,
                                   final JTextField field, final String fieldText,
                                   final Collection<Object> parameters) {
        boolean error = false;

        final LocalTime value;
        final String op;

        try {
            if (fieldText.startsWith(">=") || fieldText.startsWith("<=")) {
                final String valueStr = fieldText.substring(2);
                value = parseTime(valueStr);
                op = fieldText.substring(0, 2);
            } else if (fieldText.startsWith(">") || fieldText.startsWith("<") || fieldText.startsWith("=")) {
                final String valueStr = fieldText.substring(1);
                value = parseTime(valueStr);
                op = fieldText.substring(0, 1);
            } else {
                value = parseTime(fieldText);
                op = "=";
            }

            if (value == null) {
                // Time could not be parsed
                error = true;
            } else {
                final String colName = column.name();
                sql.add((first == 1 ? " WHERE " : " AND "), colName, op, "?");
                final Time sqlTime = Time.valueOf(value);
                parameters.add(sqlTime);
            }
        } catch (final NumberFormatException ex) {
            error = true;
        }

        int result;

        if (error) {
            field.setBackground(this.errorFieldBg);
            result = -1;
        } else {
            result = first == -1 ? -1 : 0;
            field.setBackground(this.normalFieldBg);
        }

        return result;
    }

    /**
     * Appends an SQL for q timestamp query criterion.  If the field contains a date, records with that date are
     * matched. If it contains "=", ">", "<", ">=" or "<=" followed by a date, records with dates that satisfy the
     * comparison are matched.
     *
     * @param first      true if this is the first criterion (in which case " WHERE " will be appended before the
     *                   clause), false if not (in which case " AND " will be appended before the clause)
     * @param sql        the {@code HtmlBuilder} to which to add SQL
     * @param column     the column specification
     * @param field      the field (the field background is turned to an error color if the field cannot be
     *                   interpreted)
     * @param fieldText  the non-empty query text
     * @param parameters a list to which to add the field value to set in the prepared statement (if a parametrized
     *                   WHERE clause is added to the SQL, a corresponding val
     * @return {@code first} if no clause was appended or if {@code first} was -1 on entry; 0 if a clause was appended;
     *         -1 if an error occurred
     */
    private int appendTimestampCriteria(final int first, final HtmlBuilder sql, final Column column,
                                        final JTextField field, final String fieldText,
                                        final Collection<Object> parameters) {
        boolean error = false;

        final LocalDateTime value;
        final String op;

        try {
            if (fieldText.startsWith(">=") || fieldText.startsWith("<=")) {
                final String valueStr = fieldText.substring(2);
                value = parseDateTime(valueStr);
                op = fieldText.substring(0, 2);
            } else if (fieldText.startsWith(">") || fieldText.startsWith("<") || fieldText.startsWith("=")) {
                final String valueStr = fieldText.substring(1);
                value = parseDateTime(valueStr);
                op = fieldText.substring(0, 1);
            } else {
                value = parseDateTime(fieldText);
                op = "=";
            }

            if (value == null) {
                // Timestamp could not be parsed
                error = true;
            } else {
                final String colName = column.name();
                sql.add((first == 1 ? " WHERE " : " AND "), colName, op, "?");
                final Timestamp sqlTimestamp = Timestamp.valueOf(value);
                parameters.add(sqlTimestamp);
            }
        } catch (final NumberFormatException ex) {
            error = true;
        }

        int result;

        if (error) {
            field.setBackground(this.errorFieldBg);
            result = -1;
        } else {
            result = first == -1 ? -1 : 0;
            field.setBackground(this.normalFieldBg);
        }

        return result;
    }

    /**
     * Appends an SQL for qn boolean query criterion.  If the field is an integer or "=" followed by an integer, that
     * value is matched.  If it is "!=", ">", ">=", "<" or "<=" followed by an integer, all rows that satisfy the
     * comparison are matched.
     *
     * @param first      true if this is the first criterion (in which case " WHERE " will be appended before the
     *                   clause), false if not (in which case " AND " will be appended before the clause)
     * @param sql        the {@code HtmlBuilder} to which to add SQL
     * @param column     the column specification
     * @param field      the field (the field background is turned to an error color if the field cannot be
     *                   interpreted)
     * @param fieldText  the non-empty query text
     * @param parameters a list to which to add the field value to set in the prepared statement (if a parametrized
     *                   WHERE clause is added to the SQL, a corresponding value will be added to this list)
     * @return {@code first} if no clause was appended or if {@code first} was -1 on entry; 0 if a clause was appended;
     *         -1 if an error occurred
     */
    private int appendBooleanCriteria(final int first, final HtmlBuilder sql, final Column column,
                                      final JTextField field, final String fieldText,
                                      final Collection<Object> parameters) {

        boolean error = false;

        Boolean value = null;

        if ("TRUE".equalsIgnoreCase(fieldText) || "T".equalsIgnoreCase(fieldText)
            || "Y".equalsIgnoreCase(fieldText) || "1".equals(fieldText)) {
            value = Boolean.TRUE;
        } else if ("FALSE".equalsIgnoreCase(fieldText) || "F".equalsIgnoreCase(fieldText)
                   || "N".equalsIgnoreCase(fieldText) || "0".equals(fieldText)) {
            value = Boolean.FALSE;
        } else {
            error = true;
        }

        int result;

        if (error) {
            field.setBackground(this.errorFieldBg);
            result = -1;
        } else {
            final String colName = column.name();
            sql.add((first == 1 ? " WHERE " : " AND "), colName, "=?");
            parameters.add(value);

            result = first == -1 ? -1 : 0;
            field.setBackground(this.normalFieldBg);
        }

        return result;
    }

    /**
     * Parses a date from a string.
     *
     * @param str the string
     * @return the date
     */
    private LocalDate parseDate(final String str) {

        LocalDate local = null;

        final int len = str.length();

        try {
            if (len == 8) {
                // Informix syntax like "12312024"
                final int month = Integer.parseInt(str.substring(0, 2));
                final int day = Integer.parseInt(str.substring(2, 4));
                final int year = Integer.parseInt(str.substring(4));
                local = LocalDate.of(year, month, day);
            } else if (len == 10) {
                if (isDateSep(str.charAt(4)) && isDateSep(str.charAt(7))) {
                    // YYYY-MM-DD or YYYY/MM/DD or YYYY MM DD
                    final int year = Integer.parseInt(str.substring(0, 4));
                    final int month = Integer.parseInt(str.substring(5, 7));
                    final int day = Integer.parseInt(str.substring(8));
                    local = LocalDate.of(year, month, day);
                } else if (isDateSep(str.charAt(2)) && isDateSep(str.charAt(5))) {
                    // MM-DD-YYYY or MM/DD/YYYY or MM DD YYYY
                    final int month = Integer.parseInt(str.substring(0, 2));
                    final int day = Integer.parseInt(str.substring(3, 5));
                    final int year = Integer.parseInt(str.substring(6));
                    local = LocalDate.of(year, month, day);
                } else {
                    Log.warning("Invalid date: ", str);
                }
            } else {
                Log.warning("Invalid date: ", str);
            }
        } catch (final NumberFormatException | DateTimeException ex) {
            Log.warning("Invalid date: ", str, ex);
        }

        return local;
    }

    /**
     * Parses a time from a string.
     *
     * @param str the string
     * @return the time
     */
    private LocalTime parseTime(final String str) {

        LocalTime local = null;

        final int len = str.length();

        try {
            if (len == 4 && str.charAt(1) == ':') {
                // "H:MM"
                final int hour = Integer.parseInt(str.substring(0, 1));
                final int min = Integer.parseInt(str.substring(2));
                local = LocalTime.of(hour, min);
            } else if (len == 5 && str.charAt(2) == ':') {
                // "HH:MM"
                final int hour = Integer.parseInt(str.substring(0, 2));
                final int min = Integer.parseInt(str.substring(3));
                local = LocalTime.of(hour, min);
            } else if (len == 7 && str.charAt(1) == ':' && str.charAt(4) == ':') {
                // "H:MM:SS"
                final int hour = Integer.parseInt(str.substring(0, 1));
                final int min = Integer.parseInt(str.substring(2, 4));
                final int sec = Integer.parseInt(str.substring(5));
                local = LocalTime.of(hour, min, sec);
            } else if (len == 8 && str.charAt(2) == ':' && str.charAt(5) == ':') {
                // "HH:MM:SS"
                final int hour = Integer.parseInt(str.substring(0, 2));
                final int min = Integer.parseInt(str.substring(3, 5));
                final int sec = Integer.parseInt(str.substring(6));
                local = LocalTime.of(hour, min, sec);
            } else {
                Log.warning("Invalid time: ", str);
            }
        } catch (final NumberFormatException | DateTimeException ex) {
            Log.warning("Invalid time: ", str, ex);
        }

        return local;
    }

    /**
     * Parses a date/time from a string.
     *
     * @param str the string
     * @return the date/time
     */
    private LocalDateTime parseDateTime(final String str) {

        LocalDateTime local = null;

        final int len = str.length();
        final int lastSpace = str.lastIndexOf(' ');

        if (lastSpace == -1) {
            final LocalDate date = parseDate(str);
            if (date != null) {
                final LocalTime time = LocalTime.of(0, 0, 0);
                local = LocalDateTime.of(date, time);
            }
        } else {
            final LocalDate date = parseDate(str.substring(0, lastSpace));
            final LocalTime time = parseTime(str.substring(lastSpace + 1));

            if (date != null && time != null) {
                local = LocalDateTime.of(date, time);
            }
        }

        return local;
    }

    /**
     * Tests whether a character is a "date separator".
     *
     * @param ch the character to test
     * @return true of the character is a valid separator for parts of a date
     */
    private boolean isDateSep(final char ch) {

        return ch == '/' || ch == '-' || ch == ' ' || ch == '.';
    }

    /**
     * Called when the selected record in the results table changes.
     *
     * @param e the event that characterizes the change.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

    }
}

