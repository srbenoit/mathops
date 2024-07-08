package dev.mathops.app.adm.forms;

import dev.mathops.app.adm.GenericRecord;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.fields.ConstrainedNonNegIntField;
import dev.mathops.app.adm.fields.ConstrainedTextField;
import dev.mathops.app.adm.fields.DateField;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawAdminHold;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The "admin_hold" table form.
 */
final class AdminHoldForm extends AbstractForm implements ListSelectionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7006157832311757882L;

    /** The data cache. */
    private final Cache cache;

    /** The student ID field. */
    private final ConstrainedTextField stuIdField;

    /** The hold ID field. */
    private final ConstrainedTextField holdIdField;

    /** The severity field. */
    private final ConstrainedTextField sevAdminHoldField;

    /** The times display field. */
    private final ConstrainedNonNegIntField timesDisplayField;

    /** The creation date field. */
    private final DateField createDtField;

    /** Results of a query. */
    private final List<GenericRecord> queryResult;

    /** The table of results. */
    private final JTableAdminHold table;

    /** Cursor pointing the current query result. */
    private int cursor;

    /**
     * Constructs a new {@code AdminHoldForm}.
     *
     * @param theCache the data cache
     */
    AdminHoldForm(final Cache theCache) {

        super();

        this.cache = theCache;
        this.queryResult = new ArrayList<>(10);

        final String[] fields = {RawAdminHold.FLD_STU_ID, RawAdminHold.FLD_HOLD_ID, RawAdminHold.FLD_SEV_ADMIN_HOLD,
                RawAdminHold.FLD_TIMES_DISPLAY, RawAdminHold.FLD_CREATE_DT};

        final JPanel left = new JPanel(new StackedBorderLayout());
        left.setBackground(Color.WHITE);
        add(left, StackedBorderLayout.WEST);

        final JPanel center = new JPanel(new BorderLayout());
        add(center, StackedBorderLayout.CENTER);

        final JLabel[] labels = makeFieldLabels(fields);

        this.stuIdField = makeTextField(fields[0], 9, DIGITS);
        this.holdIdField = makeTextField(fields[1], 2, DIGITS);
        this.sevAdminHoldField = makeTextField(fields[2], 1, "FN");
        this.timesDisplayField = makeIntField(fields[3], true, 99L);
        this.createDtField = makeDateField(fields[4]);

        this.stuIdField.setEnabled(false);
        this.holdIdField.setEnabled(false);
        this.sevAdminHoldField.setEnabled(false);
        this.timesDisplayField.setEnabled(false);
        this.createDtField.setEnabled(false);

        final JPanel current = new JPanel(new StackedBorderLayout(0, 0));
        current.setBackground(Skin.OFF_WHITE_GREEN);
        current.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        left.add(current, StackedBorderLayout.NORTH);

        current.add(makeFlow(labels[0], this.stuIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[1], this.holdIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[2], this.sevAdminHoldField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[3], this.timesDisplayField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[4], this.createDtField), StackedBorderLayout.NORTH);

        this.table = new JTableAdminHold();
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.table.setFillsViewportHeight(true);
        this.table.getSelectionModel().addListSelectionListener(this);
        this.table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        final JScrollPane scroll = new JScrollPane(this.table);

        center.add(scroll, BorderLayout.LINE_START);
    }

    /**
     * Called when the form is activated. This may re-query the underlying table to (for example) refresh the list of
     * terms represented if data is segregated by term, or to populate drop-downs with results from a "select distinct"
     */
    @Override
    public void activate() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (QUERY_CMD.equals(cmd)) {
            doQuery();
        } else if (ADD_CMD.equals(cmd)) {
            doAdd();
        } else if (EXECUTE_CMD.equals(cmd)) {
            doExecute();
        } else if (INSERT_CMD.equals(cmd)) {
            // TODO:
        } else if (PREV_CMD.equals(cmd)) {
            this.table.setRowSelectionInterval(this.cursor - 1, this.cursor - 1);
        } else if (NEXT_CMD.equals(cmd)) {
            this.table.setRowSelectionInterval(this.cursor + 1, this.cursor + 1);
        }
    }

    /**
     * Handles a click on the "Query" button, which enables the "Execute" button and all the fields.
     */
    private void doQuery() {

        // Enable fields and set focus in first field
        this.stuIdField.setEnabled(true);
        this.holdIdField.setEnabled(true);
        this.sevAdminHoldField.setEnabled(true);
        this.timesDisplayField.setEnabled(true);
        this.createDtField.setEnabled(true);

        this.table.clear();
        showRecord(null);

        this.stuIdField.setEditable(true);
        this.holdIdField.setEditable(true);
        this.sevAdminHoldField.setEditable(true);
        this.timesDisplayField.setEditable(true);
        this.createDtField.setEditable(true);

        enableQuery();
        setStatus("Query: fill in fields to match, click [Execute].");

        this.stuIdField.requestFocus();
    }

    /**
     * Handles a click on the "Add" button, which enables the "Insert" button and all the fields.
     */
    private void doAdd() {

        // Enable fields and set focus in first field
        this.stuIdField.setEnabled(true);
        this.holdIdField.setEnabled(true);
        this.sevAdminHoldField.setEnabled(true);
        this.timesDisplayField.setEnabled(true);
        this.createDtField.setEnabled(true);

        this.stuIdField.setEditable(true);
        this.holdIdField.setEditable(true);
        this.sevAdminHoldField.setEditable(true);
        this.timesDisplayField.setEditable(true);
        this.createDtField.setEditable(true);

        enableInsert();
        setStatus("Add: fill in fields, click [Insert].");

        this.stuIdField.requestFocus();
    }

    /**
     * Handles a click on the "Execute" button, which performs the query and populates the table and fields.
     */
    private void doExecute() {

        this.table.clear();
        this.queryResult.clear();
        this.cursor = 0;

        try {
            final StringBuilder builder = new StringBuilder(50);

            builder.append("SELECT ").append(this.stuIdField.getName()).append(CoreConstants.COMMA_CHAR)
                    .append(this.holdIdField.getName()).append(CoreConstants.COMMA_CHAR)
                    .append(this.sevAdminHoldField.getName()).append(CoreConstants.COMMA_CHAR)
                    .append(this.timesDisplayField.getName()).append(CoreConstants.COMMA_CHAR)
                    .append(this.createDtField.getName()).append(" FROM admin_hold");

            final boolean hasWhere = this.stuIdField.hasValue() || this.holdIdField.hasValue()
                    || this.sevAdminHoldField.hasValue() || this.timesDisplayField.hasValue()
                    || this.createDtField.hasValue();

            if (hasWhere) {
                builder.append(" WHERE");

                boolean and = appendWhere(this.stuIdField, false, builder);
                and = appendWhere(this.holdIdField, and, builder);
                and = appendWhere(this.sevAdminHoldField, and, builder);
                and = appendWhere(this.timesDisplayField, and, builder);
                appendWhere(this.createDtField, and, builder);
            }

            try (final PreparedStatement ps = this.cache.conn.prepareStatement(builder.toString())) {
                if (hasWhere) {
                    int index = 1;
                    if (this.stuIdField.hasValue()) {
                        ps.setString(index, this.stuIdField.getStringValue());
                        ++index;
                    }
                    if (this.holdIdField.hasValue()) {
                        ps.setString(index, this.holdIdField.getStringValue());
                        ++index;
                    }
                    if (this.sevAdminHoldField.hasValue()) {
                        ps.setString(index, this.sevAdminHoldField.getStringValue());
                        ++index;
                    }
                    if (this.timesDisplayField.hasValue()) {
                        ps.setLong(index, this.timesDisplayField.getLongValue().longValue());
                        ++index;
                    }
                    if (this.createDtField.hasValue()) {
                        ps.setDate(index, Date.valueOf(this.createDtField.getDateTimeValue()));
                        ++index;
                    }
                }

                try (final ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final GenericRecord rec = new GenericRecord(5);
                        rec.put(this.stuIdField.getName(), rs.getString(1));
                        rec.put(this.holdIdField.getName(), rs.getString(2));
                        rec.put(this.sevAdminHoldField.getName(), rs.getString(3));
                        final long l = rs.getLong(4);
                        if (!rs.wasNull()) {
                            rec.put(this.timesDisplayField.getName(), Long.valueOf(l));
                        }
                        final Date dt = rs.getDate(5);
                        if (dt != null) {
                            rec.put(this.createDtField.getName(), dt.toLocalDate());
                        }
                        this.queryResult.add(rec);
                    }
                }

                if (this.queryResult.isEmpty()) {
                    setStatus("Query Result: (no results)");
                } else {
                    disableQueryInsert();

                    this.stuIdField.setEditable(false);
                    this.holdIdField.setEditable(false);
                    this.sevAdminHoldField.setEditable(false);
                    this.timesDisplayField.setEditable(false);
                    this.createDtField.setEditable(false);

                    setStatus("Query Result: Viewing record 0 of " + this.queryResult.size());

                    this.table.addData(this.queryResult);
                    this.cursor = -1;
                    this.table.setRowSelectionInterval(0, 0);

                    setPrevEnabled(false);
                    setNextEnabled(this.queryResult.size() > 1);
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            setStatus("ERROR: " + ex.getMessage());
        }
    }

    /**
     * Called when the selected row in the table view changes.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        final int row = this.table.getSelectedRow();

        if (row == -1) {
            // TODO:
        } else if (row < this.queryResult.size() && row != this.cursor) {
            this.cursor = row;
            showRecord(this.queryResult.get(row));

            setNextEnabled(row + 1 < this.queryResult.size());
            setPrevEnabled(row > 0);

            setStatus("Query Result: Viewing record " + (this.cursor + 1) + " of " + this.queryResult.size());
        }
    }

    /**
     * Shows a result record.
     *
     * @param rec the result record
     */
    private void showRecord(final Map<String, Object> rec) {

        if (rec == null) {
            this.stuIdField.setValue(null);
            this.holdIdField.setValue(null);
            this.sevAdminHoldField.setValue(null);
            this.timesDisplayField.setValue(null);
            this.createDtField.setValue(null);
        } else {
            this.stuIdField.setValue(rec.get(this.stuIdField.getName()));
            this.holdIdField.setValue(rec.get(this.holdIdField.getName()));
            this.sevAdminHoldField.setValue(rec.get(this.sevAdminHoldField.getName()));
            this.timesDisplayField.setValue(rec.get(this.timesDisplayField.getName()));
            this.createDtField.setValue(rec.get(this.createDtField.getName()));
        }
    }

    /**
     * A table to present admin_hold query result rows.
     */
    static final class JTableAdminHold extends JTable {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 4646053320414659264L;

        /** The table model. */
        private final AdminHoldTableModel model;

        /**
         * Constructs a new {@code JTableAdminHold}.
         */
        JTableAdminHold() {

            super(new AdminHoldTableModel(), new AdminHoldTableColumnModel());

            setFont(Skin.MONO_12_FONT);

            this.model = (AdminHoldTableModel) getModel();

            final Dimension spacing = getIntercellSpacing();
            setPreferredSize(new Dimension(360 + 6 * spacing.width, 5 * getRowHeight() + 6 * spacing.height));

            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }

        /**
         * Clears the table, removing all rows.
         */
        void clear() {

            int numRows = this.model.getRowCount();
            while (numRows > 0) {
                --numRows;
                this.model.removeRow(numRows);
            }
        }

        /**
         * Adds data from a list of {@code DeadlineRow} records.
         *
         * @param data the data to add
         */
        void addData(final Collection<? extends GenericRecord> data) {

            for (final GenericRecord rec : data) {
                final String[] row = new String[5];

                row[0] = objToString(rec.get(RawAdminHold.FLD_STU_ID));
                row[1] = objToString(rec.get(RawAdminHold.FLD_HOLD_ID));
                row[2] = objToString(rec.get(RawAdminHold.FLD_SEV_ADMIN_HOLD));
                row[3] = objToString(rec.get(RawAdminHold.FLD_TIMES_DISPLAY));
                row[4] = objToString(rec.get(RawAdminHold.FLD_CREATE_DT));

                this.model.addRow(row);
            }
        }

        /**
         * Generates the string representation of an object that could be null.
         *
         * @param obj the object
         * @return the string representation; an empty string if the object is null
         */
        private static String objToString(final Object obj) {

            return obj == null ? CoreConstants.EMPTY : obj.toString();
        }
    }

    /**
     * The table model for the table.
     */
    static final class AdminHoldTableModel extends DefaultTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 6328823842248596434L;

        /**
         * Constructs a new {@code AdminHoldTableModel}.
         */
        AdminHoldTableModel() {

            super();

            addColumn(RawAdminHold.FLD_STU_ID);
            addColumn(RawAdminHold.FLD_HOLD_ID);
            addColumn(RawAdminHold.FLD_SEV_ADMIN_HOLD);
            addColumn(RawAdminHold.FLD_TIMES_DISPLAY);
            addColumn(RawAdminHold.FLD_CREATE_DT);
        }
    }

    /**
     * The column model for the table.
     */
    static final class AdminHoldTableColumnModel extends DefaultTableColumnModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -6377484577691259836L;

        /**
         * Constructs a new {@code AdminHoldTableColumnModel}.
         */
        AdminHoldTableColumnModel() {

            super();

            final TableColumn orderCol = new TableColumn(0, 70);
            orderCol.setHeaderValue(RawAdminHold.FLD_STU_ID);
            orderCol.setResizable(false);
            orderCol.setMinWidth(70);
            orderCol.setMaxWidth(210);
            addColumn(orderCol);

            final TableColumn milestoneCol = new TableColumn(1, 50);
            milestoneCol.setHeaderValue(RawAdminHold.FLD_HOLD_ID);
            milestoneCol.setResizable(false);
            milestoneCol.setMinWidth(50);
            milestoneCol.setMaxWidth(150);
            addColumn(milestoneCol);

            final TableColumn courseCol = new TableColumn(2, 70);
            courseCol.setHeaderValue(RawAdminHold.FLD_SEV_ADMIN_HOLD);
            courseCol.setResizable(false);
            courseCol.setMinWidth(70);
            courseCol.setMaxWidth(210);
            addColumn(courseCol);

            final TableColumn unitCol = new TableColumn(3, 70);
            unitCol.setHeaderValue(RawAdminHold.FLD_TIMES_DISPLAY);
            unitCol.setResizable(false);
            unitCol.setMinWidth(70);
            unitCol.setMaxWidth(210);
            addColumn(unitCol);

            final TableColumn typeCol = new TableColumn(4, 100);
            typeCol.setHeaderValue(RawAdminHold.FLD_CREATE_DT);
            typeCol.setResizable(false);
            typeCol.setMinWidth(100);
            typeCol.setMaxWidth(250);
            addColumn(typeCol);
        }
    }
}
