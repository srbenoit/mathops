package dev.mathops.app.adm.forms;

import dev.mathops.app.adm.GenericRecord;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.fields.ConstrainedNonNegIntField;
import dev.mathops.app.adm.fields.ConstrainedTextField;
import dev.mathops.app.adm.fields.DateTimeField;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.reclogic.AssignmentLogic;
import dev.mathops.db.old.reclogic.query.DateTimeCriteria;
import dev.mathops.db.old.reclogic.query.EStringComparison;
import dev.mathops.db.old.reclogic.query.IntegerCriteria;
import dev.mathops.db.old.reclogic.query.StringCriteria;

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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The "assignment" table form.
 */
final class AssignmentForm extends AbstractForm implements ListSelectionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3866489947193865895L;

    /** The data cache. */
    private final Cache cache;

    /** The assignment ID field. */
    private final ConstrainedTextField assignmentIdField;

    /** The assignment type field. */
    private final ConstrainedTextField assignmentTypeField;

    /** The course ID field. */
    private final ConstrainedTextField courseIdField;

    /** The unit field. */
    private final ConstrainedNonNegIntField unitField;

    /** The objective field. */
    private final ConstrainedNonNegIntField objectiveField;

    /** The tree reference field. */
    private final ConstrainedTextField treeRefField;

    /** The title field. */
    private final ConstrainedTextField titleField;

    /** The when-active field. */
    private final DateTimeField whenActiveField;

    /** The when-pulled field. */
    private final DateTimeField whenPulledField;

    /** Results of a query. */
    private final List<GenericRecord> queryResult;

    /** The table of results. */
    private final JTableAssignment table;

    /** Cursor pointing the current query result. */
    private int cursor;

    /**
     * Constructs a new {@code AssignmentForm}.
     *
     * @param theCache the data cache
     */
    AssignmentForm(final Cache theCache) {

        super();

        this.cache = theCache;
        this.queryResult = new ArrayList<>(10);

        final String[] fields = {AssignmentRec.FLD_ASSIGNMENT_ID, AssignmentRec.FLD_ASSIGNMENT_TYPE,
                AssignmentRec.FLD_COURSE_ID, AssignmentRec.FLD_UNIT, AssignmentRec.FLD_OBJECTIVE,
                AssignmentRec.FLD_TREE_REF, AssignmentRec.FLD_TITLE, AssignmentRec.FLD_WHEN_ACTIVE,
                AssignmentRec.FLD_WHEN_PULLED};

        final JPanel left = new JPanel(new StackedBorderLayout());
        left.setBackground(Color.WHITE);
        add(left, StackedBorderLayout.WEST);

        final JPanel center = new JPanel(new BorderLayout());
        add(center, StackedBorderLayout.CENTER);

        final JLabel[] labels = makeFieldLabels(fields);

        this.assignmentIdField = makeTextField(fields[0], 20, LETTERS_DIGITS);
        this.assignmentTypeField = makeTextField(fields[1], 2, LETTERS_DIGITS);
        this.courseIdField = makeTextField(fields[2], 10, LETTERS_DIGITS);
        this.unitField = makeIntField(fields[3], true, 99L);
        this.objectiveField = makeIntField(fields[4], true, 99L);
        this.treeRefField = makeTextField(fields[5], 50, LETTERS_DIGITS_PUNC);
        this.titleField = makeTextField(fields[6], 50, LETTERS_DIGITS_PUNC);
        this.whenActiveField = makeDateTimeField(fields[7]);
        this.whenPulledField = makeDateTimeField(fields[8]);

        this.assignmentIdField.setEnabled(false);
        this.assignmentTypeField.setEnabled(false);
        this.courseIdField.setEnabled(false);
        this.unitField.setEnabled(false);
        this.objectiveField.setEnabled(false);
        this.treeRefField.setEnabled(false);
        this.titleField.setEnabled(false);
        this.whenActiveField.setEnabled(false);
        this.whenPulledField.setEnabled(false);

        final JPanel current = new JPanel(new StackedBorderLayout(0, 0));
        current.setBackground(Skin.OFF_WHITE_GREEN);
        current.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        left.add(current, StackedBorderLayout.NORTH);

        current.add(makeFlow(labels[0], this.assignmentIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[1], this.assignmentTypeField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[2], this.courseIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[3], this.unitField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[4], this.objectiveField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[5], this.treeRefField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[6], this.titleField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[7], this.whenActiveField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[8], this.whenPulledField), StackedBorderLayout.NORTH);

        this.table = new JTableAssignment();
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
        this.assignmentIdField.setEnabled(true);
        this.assignmentTypeField.setEnabled(true);
        this.courseIdField.setEnabled(true);
        this.unitField.setEnabled(true);
        this.objectiveField.setEnabled(true);
        this.treeRefField.setEnabled(true);
        this.titleField.setEnabled(true);
        this.whenActiveField.setEnabled(true);
        this.whenPulledField.setEnabled(true);

        this.table.clear();
        showRecord(null);

        this.assignmentIdField.setEditable(true);
        this.assignmentTypeField.setEditable(true);
        this.courseIdField.setEditable(true);
        this.unitField.setEditable(true);
        this.objectiveField.setEditable(true);
        this.treeRefField.setEditable(true);
        this.titleField.setEditable(true);
        this.whenActiveField.setEditable(true);
        this.whenPulledField.setEditable(true);

        enableQuery();
        setStatus("Query: fill in fields to match, click [Execute].");

        this.assignmentIdField.requestFocus();
    }

    /**
     * Handles a click on the "Add" button, which enables the "Insert" button and all the fields.
     */
    private void doAdd() {

        // Enable fields and set focus in first field
        this.assignmentIdField.setEnabled(true);
        this.assignmentTypeField.setEnabled(true);
        this.courseIdField.setEnabled(true);
        this.unitField.setEnabled(true);
        this.objectiveField.setEnabled(true);
        this.treeRefField.setEnabled(true);
        this.titleField.setEnabled(true);
        this.whenActiveField.setEnabled(true);
        this.whenPulledField.setEnabled(true);

        this.assignmentIdField.setEditable(true);
        this.assignmentTypeField.setEditable(true);
        this.courseIdField.setEditable(true);
        this.unitField.setEditable(true);
        this.objectiveField.setEditable(true);
        this.treeRefField.setEditable(true);
        this.titleField.setEditable(true);
        this.whenActiveField.setEditable(true);
        this.whenPulledField.setEditable(true);

        enableInsert();
        setStatus("Add: fill in fields, click [Insert].");

        this.assignmentIdField.requestFocus();
    }

    /**
     * Handles a click on the "Execute" button, which performs the query and populates the table and fields.
     */
    private void doExecute() {

        this.table.clear();
        this.queryResult.clear();
        this.cursor = 0;

        try {
            // Get the logic implementation appropriate to the current cache.
            final AssignmentLogic logic = AssignmentLogic.get(this.cache);
            final AssignmentLogic.Criteria queryCriteria = extractCriteria();

            final List<AssignmentRec> result = logic.generalQuery(this.cache, queryCriteria);
            for (final AssignmentRec row : result) {
                final GenericRecord record = new GenericRecord(9);

                if (row.assignmentId != null) {
                    record.put(this.assignmentIdField.getName(), row.assignmentId);
                }
                if (row.assignmentType != null) {
                    record.put(this.assignmentTypeField.getName(), row.assignmentType);
                }
                if (row.courseId != null) {
                    record.put(this.courseIdField.getName(), row.courseId);
                }
                if (row.unit != null) {
                    record.put(this.unitField.getName(), row.unit);
                }
                if (row.objective != null) {
                    record.put(this.objectiveField.getName(), row.objective);
                }
                if (row.treeRef != null) {
                    record.put(this.treeRefField.getName(), row.treeRef);
                }
                if (row.title != null) {
                    record.put(this.titleField.getName(), row.title);
                }
                if (row.whenActive != null) {
                    record.put(this.whenActiveField.getName(), row.whenActive);
                }
                if (row.whenPulled != null) {
                    record.put(this.whenPulledField.getName(), row.whenPulled);
                }

                this.queryResult.add(record);
            }

            if (this.queryResult.isEmpty()) {
                setStatus("Query Result: (no results)");
            } else {
                disableQueryInsert();

                this.assignmentIdField.setEditable(false);
                this.assignmentTypeField.setEditable(false);
                this.courseIdField.setEditable(false);
                this.unitField.setEditable(false);
                this.objectiveField.setEditable(false);
                this.treeRefField.setEditable(false);
                this.titleField.setEditable(false);
                this.whenActiveField.setEditable(false);
                this.whenPulledField.setEditable(false);

                setStatus("Query Result: Viewing record 0 of " + this.queryResult.size());

                this.table.addData(this.queryResult);
                this.cursor = -1;
                this.table.setRowSelectionInterval(0, 0);

                setPrevEnabled(false);
                setNextEnabled(this.queryResult.size() > 1);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            setStatus("ERROR: " + ex.getMessage());
        }
    }

    /**
     * Extracts query criteria from fields.
     *
     * @return the parsed criteria
     */
    private AssignmentLogic.Criteria extractCriteria() {

        final AssignmentLogic.Criteria result = new AssignmentLogic.Criteria();

        if (this.assignmentIdField.hasValue()) {
            result.assignmentId = new StringCriteria(this.assignmentIdField.getStringValue(),
                    EStringComparison.EQUAL);
        }

        if (this.assignmentTypeField.hasValue()) {
            result.assignmentType = new StringCriteria(this.assignmentTypeField.getStringValue(),
                    EStringComparison.EQUAL);
        }

        if (this.courseIdField.hasValue()) {
            result.courseId = new StringCriteria(this.courseIdField.getStringValue(), EStringComparison.EQUAL);
        }

        if (this.unitField.hasValue()) {
            result.unit = new IntegerCriteria(this.unitField.getIntegerValue().intValue(),
                    this.unitField.getComparison());
        }

        if (this.objectiveField.hasValue()) {
            result.objective = new IntegerCriteria(this.objectiveField.getIntegerValue().intValue(),
                    this.objectiveField.getComparison());
        }

        if (this.treeRefField.hasValue()) {
            result.treeRef = new StringCriteria(this.treeRefField.getStringValue(), EStringComparison.EQUAL);
        }

        if (this.titleField.hasValue()) {
            result.title = new StringCriteria(this.titleField.getStringValue(), EStringComparison.EQUAL);
        }

        if (this.whenActiveField.hasValue()) {
            LocalDate dt = this.whenActiveField.getDateValue();
            if (dt == null) {
                dt = LocalDate.now();
            }
            LocalTime tm = this.whenActiveField.getTimeValue();
            if (tm == null) {
                tm = LocalTime.of(0, 0, 0);
            }

            result.whenActive = new DateTimeCriteria(LocalDateTime.of(dt, tm),
                    this.whenActiveField.getComparison());
        }

        if (this.whenPulledField.hasValue()) {
            LocalDate dt = this.whenPulledField.getDateValue();
            if (dt == null) {
                dt = LocalDate.now();
            }
            LocalTime tm = this.whenPulledField.getTimeValue();
            if (tm == null) {
                tm = LocalTime.of(0, 0, 0);
            }

            result.whenPulled = new DateTimeCriteria(LocalDateTime.of(dt, tm), this.whenPulledField.getComparison());
        }

        return result;
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
            this.assignmentIdField.setValue(null);
            this.assignmentTypeField.setValue(null);
            this.courseIdField.setValue(null);
            this.unitField.setValue(null);
            this.objectiveField.setValue(null);
            this.treeRefField.setValue(null);
            this.titleField.setValue(null);
            this.whenActiveField.setValue(null);
            this.whenPulledField.setValue(null);
        } else {
            this.assignmentIdField.setValue(rec.get(this.assignmentIdField.getName()));
            this.assignmentTypeField.setValue(rec.get(this.assignmentTypeField.getName()));
            this.courseIdField.setValue(rec.get(this.courseIdField.getName()));
            this.unitField.setValue(rec.get(this.unitField.getName()));
            this.objectiveField.setValue(rec.get(this.objectiveField.getName()));
            this.treeRefField.setValue(rec.get(this.treeRefField.getName()));
            this.titleField.setValue(rec.get(this.titleField.getName()));
            this.whenActiveField.setValue(rec.get(this.whenActiveField.getName()));
            this.whenPulledField.setValue(rec.get(this.whenPulledField.getName()));
        }
    }

    /**
     * A table to present admin_hold query result rows.
     */
    static final class JTableAssignment extends JTable {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 7345442790433003298L;

        /** The table model. */
        private final AssignmentTableModel model;

        /**
         * Constructs a new {@code JTableAssignment}.
         */
        JTableAssignment() {

            super(new AssignmentTableModel(), new AssignmentTableColumnModel());

            setFont(Skin.MONO_12_FONT);

            this.model = (AssignmentTableModel) getModel();

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

            final int size = data.size();

            for (final GenericRecord rec : data) {
                final String[] row = new String[9];

                row[0] = objToString(rec.get(AssignmentRec.FLD_ASSIGNMENT_ID));
                row[1] = objToString(rec.get(AssignmentRec.FLD_ASSIGNMENT_TYPE));
                row[2] = objToString(rec.get(AssignmentRec.FLD_COURSE_ID));
                row[3] = objToString(rec.get(AssignmentRec.FLD_UNIT));
                row[4] = objToString(rec.get(AssignmentRec.FLD_OBJECTIVE));
                row[5] = objToString(rec.get(AssignmentRec.FLD_TREE_REF));
                row[6] = objToString(rec.get(AssignmentRec.FLD_TITLE));
                row[7] = objToString(rec.get(AssignmentRec.FLD_WHEN_ACTIVE));
                row[8] = objToString(rec.get(AssignmentRec.FLD_WHEN_PULLED));

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
    static final class AssignmentTableModel extends DefaultTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 5170619153598011708L;

        /**
         * Constructs a new {@code AssignmentTableModel}.
         */
        AssignmentTableModel() {

            super();

            addColumn(AssignmentRec.FLD_ASSIGNMENT_ID);
            addColumn(AssignmentRec.FLD_ASSIGNMENT_TYPE);
            addColumn(AssignmentRec.FLD_COURSE_ID);
            addColumn(AssignmentRec.FLD_UNIT);
            addColumn(AssignmentRec.FLD_OBJECTIVE);
            addColumn(AssignmentRec.FLD_TREE_REF);
            addColumn(AssignmentRec.FLD_TITLE);
            addColumn(AssignmentRec.FLD_WHEN_ACTIVE);
            addColumn(AssignmentRec.FLD_WHEN_PULLED);
        }
    }

    /**
     * The column model for the table.
     */
    static final class AssignmentTableColumnModel extends DefaultTableColumnModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -6614956908765242578L;

        /**
         * Constructs a new {@code AssignmentTableColumnModel}.
         */
        AssignmentTableColumnModel() {

            super();

            final TableColumn assignmentIdCol = new TableColumn(0, 70);
            assignmentIdCol.setHeaderValue(AssignmentRec.FLD_ASSIGNMENT_ID);
            assignmentIdCol.setResizable(false);
            assignmentIdCol.setMinWidth(70);
            assignmentIdCol.setMaxWidth(210);
            addColumn(assignmentIdCol);

            final TableColumn assignmentTypeCol = new TableColumn(1, 50);
            assignmentTypeCol.setHeaderValue(AssignmentRec.FLD_ASSIGNMENT_TYPE);
            assignmentTypeCol.setResizable(false);
            assignmentTypeCol.setMinWidth(50);
            assignmentTypeCol.setMaxWidth(150);
            addColumn(assignmentTypeCol);

            final TableColumn courseIdCol = new TableColumn(2, 70);
            courseIdCol.setHeaderValue(AssignmentRec.FLD_COURSE_ID);
            courseIdCol.setResizable(false);
            courseIdCol.setMinWidth(70);
            courseIdCol.setMaxWidth(210);
            addColumn(courseIdCol);

            final TableColumn unitCol = new TableColumn(3, 70);
            unitCol.setHeaderValue(AssignmentRec.FLD_UNIT);
            unitCol.setResizable(false);
            unitCol.setMinWidth(70);
            unitCol.setMaxWidth(210);
            addColumn(unitCol);

            final TableColumn objectiveCol = new TableColumn(3, 70);
            objectiveCol.setHeaderValue(AssignmentRec.FLD_OBJECTIVE);
            objectiveCol.setResizable(false);
            objectiveCol.setMinWidth(70);
            objectiveCol.setMaxWidth(210);
            addColumn(objectiveCol);

            final TableColumn treeRefCol = new TableColumn(0, 70);
            treeRefCol.setHeaderValue(AssignmentRec.FLD_TREE_REF);
            treeRefCol.setResizable(false);
            treeRefCol.setMinWidth(70);
            treeRefCol.setMaxWidth(210);
            addColumn(treeRefCol);

            final TableColumn titleCol = new TableColumn(0, 70);
            titleCol.setHeaderValue(AssignmentRec.FLD_TITLE);
            titleCol.setResizable(false);
            titleCol.setMinWidth(70);
            titleCol.setMaxWidth(210);
            addColumn(titleCol);

            final TableColumn whenActiveCol = new TableColumn(4, 100);
            whenActiveCol.setHeaderValue(AssignmentRec.FLD_WHEN_ACTIVE);
            whenActiveCol.setResizable(false);
            whenActiveCol.setMinWidth(100);
            whenActiveCol.setMaxWidth(250);
            addColumn(whenActiveCol);

            final TableColumn whenPulledCol = new TableColumn(4, 100);
            whenPulledCol.setHeaderValue(AssignmentRec.FLD_WHEN_PULLED);
            whenPulledCol.setResizable(false);
            whenPulledCol.setMinWidth(100);
            whenPulledCol.setMaxWidth(250);
            addColumn(whenPulledCol);
        }
    }
}
