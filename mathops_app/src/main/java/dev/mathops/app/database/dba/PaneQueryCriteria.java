package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
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
 * A panel that presents fields to enter query criteria.
 */
final class PaneQueryCriteria extends JPanel {

    /** The fields to query each active table column. */
    private final List<JTextField> activeTableQueryFields;

    /** The normal background color for a text field. */
    private final Color normalFieldBg;

    /** The "error" background color for a text field. */
    private final Color errorFieldBg;

    /** The panel with query fields. */
    private final JPanel queryFields;

    /**
     * Constructs a new {@code PaneQueryCriteria}.
     *
     * @param listener the listener to notify of QUERY actions
     */
    PaneQueryCriteria(final ActionListener listener) {

        super(new StackedBorderLayout());

        this.activeTableQueryFields = new ArrayList<>(40);

        final JTextField field = new JTextField();
        this.normalFieldBg = field.getBackground();
        final boolean isLight = InterfaceUtils.isLight(this.normalFieldBg);
        final int normalRed = this.normalFieldBg.getRed();
        final int normalGreen = this.normalFieldBg.getGreen();
        final int normalBlue = this.normalFieldBg.getBlue();
        this.errorFieldBg = isLight ? new Color(255, normalGreen - 10, normalBlue - 10)
                : new Color(normalRed + 50, normalGreen, normalBlue);

        final Border padding = BorderFactory.createEmptyBorder(5, 8, 5, 8);

        setPreferredSize(new Dimension(300, 300));
        setBorder(padding);

        final JLabel queryHeader = new JLabel("Query Criteria:");
        add(queryHeader, StackedBorderLayout.NORTH);

        this.queryFields = new JPanel(new StackedBorderLayout());
        final JScrollPane queryScroll = new JScrollPane(this.queryFields);

        final JScrollBar verticalBar = queryScroll.getVerticalScrollBar();
        verticalBar.setUnitIncrement(10);
        verticalBar.setBlockIncrement(10);

        queryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(queryScroll, StackedBorderLayout.CENTER);

        final JButton queryButton = new JButton("Query");
        queryButton.setActionCommand(PaneManage.QUERY_CMD);
        queryButton.addActionListener(listener);
        final JPanel queryButtonFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 4));
        queryButtonFlow.add(queryButton);
        add(queryButtonFlow, StackedBorderLayout.SOUTH);
    }

    /**
     * Clears tha panel.
     */
    void clear() {

        this.queryFields.removeAll();
        this.activeTableQueryFields.clear();
    }

    /**
     * Updates the panel to reflect a given set of columns.
     *
     * @param columns the list of columns
     */
    void update(final List<Column> columns) {

        final int numColumns = columns.size();
        if (numColumns > 0) {
            final JLabel[] fieldNames = new JLabel[numColumns];
            for (int i = 0; i < numColumns; ++i) {
                final Column col = columns.get(i);
                fieldNames[i] = new JLabel(col.name() + ":");
            }
            UIUtilities.makeLabelsSameSizeRightAligned(fieldNames);

            for (int i = 0; i < numColumns; ++i) {

                final JPanel columnFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
                columnFlow.add(fieldNames[i]);

                final Column col = columns.get(i);
                final int type = col.type();
                if (canBeQueryCriteria(type)) {
                    final JTextField field = new JTextField(10);
                    this.activeTableQueryFields.add(field);
                    columnFlow.add(field);
                }

                this.queryFields.add(columnFlow, StackedBorderLayout.NORTH);
            }

            invalidate();
            revalidate();
            repaint();
        }
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
     * Performs a query and populates the results table.
     *
     * @param conn      the database connection
     * @param tableName the table name (qualified with schema name if needed)
     * @param columns   the list of table columns
     * @param owner     the owning window to which to give query results
     */
    void performQuery(final DbConnection conn, final String tableName, final List<Column> columns,
                      final PaneManage owner) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(200);
        sql.add("SELECT * FROM ", tableName);

        int state = 1;
        final int numColumns = columns.size();
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
                final Column column = columns.get(i);
                final int type = column.type();

                if (PaneManage.isIntegerType(type)) {
                    state = appendIntegerCriteria(state, sql, column, field, fieldText, parameters);
                } else if (PaneManage.isNumericType(type)) {
                    state = appendNumberCriteria(state, sql, column, field, fieldText, parameters);
                } else if (PaneManage.isStringType(type)) {
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
                owner.populateResults(rs);
            }
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
}
