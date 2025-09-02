package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.schema.legacy.rec.RawStcourse;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A table to present the collection of courses taken by a student in past terms.
 */
final class JTableCourseHistory extends AbstractAdmTable<RawStcourse> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8616039849185390240L;

    /**
     * Constructs a new {@code JTableCourseHistory}.
     */
    JTableCourseHistory() {

        super(makeColumns());

        setDefaultRenderer(Object.class, new HistoryCellRenderer());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(8);

        result.add(new LocalTableColumn("Term", 45));
        result.add(new LocalTableColumn("Year", 45));
        result.add(new LocalTableColumn("Course", 60));
        result.add(new LocalTableColumn("Section", 60));
        result.add(new LocalTableColumn("Grade", 60));
        result.add(new LocalTableColumn("Format", 60));

        return result;
    }

    /**
     * Installs data in the table.
     *
     * @param data    the data to install
     * @param minRows the minimum number of rows to show in the table
     * @return the number of records in the table after the insert
     */
    @Override
    protected int installData(final List<? extends RawStcourse> data, final int minRows) {

        final String[] row = new String[6];

        for (final RawStcourse reg : data) {
            row[0] = reg.termKey.termCode;
            row[1] = valueToString(reg.termKey.year);
            row[2] = valueToString(reg.course);
            row[3] = valueToString(reg.sect);
            row[4] = valueToString(reg.courseGrade);
            row[5] = reg.instrnType == null ? CoreConstants.EMPTY : reg.instrnType;

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }

    /** A renderer for the history table to shade semesters differently. */
    private static final class HistoryCellRenderer extends DefaultTableCellRenderer {

        private static final Color SP_1 = new Color(225, 255, 220);
        private static final Color SP_2 = new Color(245, 245, 245);

        private static final Color SM_1 = new Color(215, 245, 210);
        private static final Color SM_2 = new Color(235, 235, 235);

        private static final Color FA_1 = new Color(205, 235, 200);
        private static final Color FA_2 = new Color(225, 225, 225);

        /** Constructs a new {@code HistoryCellRenderer}. */
        HistoryCellRenderer() {

            super();

            setOpaque(true);
        }

        /**
         * Gets the component that will render a table cell.
         *
         * @param table      the {@code JTable}
         * @param value      the value to assign to the cell at {@code [row, column]}
         * @param isSelected true if cell is selected
         * @param hasFocus   true if cell has focus
         * @param row        the row of the cell to render
         * @param column     the column of the cell to render
         * @return the component
         */
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                       final boolean hasFocus, final int row, final int column) {

            setText(value != null ? value.toString() : "");

            if (table instanceof final JTableCourseHistory historyTable) {
                final AbstractAdmTable.LocalTableModel model = historyTable.getModel();
                final int numRows = model.getRowCount();

                if (row < numRows) {

                    // Find the set of all years represented in the data
                    final Set<Object> years = new TreeSet<>();
                    for (int i = 0; i < numRows; ++i) {
                        years.add(model.getValueAt(i, 1));
                    }

                    // Assign each year a color (alternating white and green)
                    final Map<Object, Integer> yearToggle = new HashMap<>();
                    int index = 0;
                    for (final Object year : years) {
                        yearToggle.put(year, Integer.valueOf(index));
                        index = (index + 1) % 2;
                    }

                    // See what color this row should be
                    final Object rowTerm = model.getValueAt(row, 0);
                    final Object rowYear = model.getValueAt(row, 1);

                    final Integer toggle = yearToggle.get(rowYear);
                    if (toggle != null) {
                        if (toggle.intValue() == 0) {
                            if ("SP".equals(rowTerm)) {
                                setBackground(SP_1);
                            } else if ("SM".equals(rowTerm)) {
                                setBackground(SM_1);
                            } else {
                                setBackground(FA_1);
                            }
                        } else {
                            if ("SP".equals(rowTerm)) {
                                setBackground(SP_2);
                            } else if ("SM".equals(rowTerm)) {
                                setBackground(SM_2);
                            } else {
                                setBackground(FA_2);
                            }
                        }
                    }
                }
            }

            return this;
        }
    }
}
