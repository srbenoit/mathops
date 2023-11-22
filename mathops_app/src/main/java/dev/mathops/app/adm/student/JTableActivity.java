package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractAdminTable;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.db.rawrecord.RawStexam;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present student activity in each week.
 */
final class JTableActivity extends AbstractAdminTable<ActivityRow> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 9131945356398234487L;

    /** Row indexes that represent the last activity in a week. */
    private final List<Integer> weekEndRows;

    /** The installed rows. */
    private final List<ActivityRow> rows;

    /**
     * Constructs a new {@code JTableActivity}.
     */
    JTableActivity() {

        super(makeColumns());

        this.weekEndRows = new ArrayList<>(20);
        this.rows = new ArrayList<>(20);

        setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        setRowHeight(21);
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<AdminTableColumn> makeColumns() {

        final List<AdminTableColumn> result = new ArrayList<>(12);

        result.add(new AdminTableColumn("Week", 50));
        result.add(new AdminTableColumn("Course", 60));
        result.add(new AdminTableColumn("Unit", 40));
        result.add(new AdminTableColumn("Activity", 90));
        result.add(new AdminTableColumn("ID", 60));
        result.add(new AdminTableColumn("Date", 90));
        result.add(new AdminTableColumn("Finish", 70));
        result.add(new AdminTableColumn("Dur (min)", 90));
        result.add(new AdminTableColumn("Score", 75));
        result.add(new AdminTableColumn("Passed", 70));

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
    protected int installData(final List<? extends ActivityRow> data, final int minRows) {

        this.weekEndRows.clear();
        this.rows.clear();
        this.rows.addAll(data);

        final String[] row = new String[10];
        final int size = data.size();

        for (int i = 0; i < size; ++i) {
            final ActivityRow record = data.get(i);

            final String durStr;
            if (record.start == null || record.finish == null) {
                durStr = CoreConstants.EMPTY;
            } else {
                final int startSec = record.start.toSecondOfDay();
                final int finSec = record.finish.toSecondOfDay();
                int durSec = finSec - startSec;
                if (durSec < -1000) {
                    durSec += 86400;
                }
                if (durSec < 0) {
                    durStr = "(neg)";
                } else if (durSec < 10) {
                    durStr = "0:0" + durSec;
                } else if (durSec < 60) {
                    durStr = "0:" + durSec;
                } else if (durSec < 120) {
                    durStr = "1:" + (durSec - 60);
                } else {
                    durStr = Integer.toString((durSec + 30) / 60);
                }
            }

            row[0] = record.week < 1 ? "Before" : Integer.toString(record.week);
            row[1] = valueToString(record.course);
            row[2] = valueToString(record.unit);
            if ("LE".equals(record.activity)) {
                row[3] = "User's Exam";
            } else if ("R".equals(record.activity)) {
                if (Integer.valueOf(0).equals(record.unit)) {
                    row[3] = "Skills Rev.";
                } else {
                    row[3] = "Unit Rev.";
                }
            } else if (RawStexam.UNIT_EXAM.equals(record.activity)) {
                row[3] = "Unit Exam";
            } else if (RawStexam.FINAL_EXAM.equals(record.activity)) {
                row[3] = "Final Exam";
            } else {
                row[3] = valueToString(record.activity);
            }
            row[4] = valueToString(record.id);
            row[5] = record.date == null ? CoreConstants.EMPTY
                    : record.week == 0 ? FMT_TEXT.format(record.date) : FMT_WTEXT.format(record.date);
            row[6] = record.finish == null ? CoreConstants.EMPTY : FMT_HM.format(record.finish);
            row[7] = durStr;
            row[8] = valueToString(record.score);

            if (record.first) {
                row[9] = record.passed + " (first)";
            } else {
                row[9] = record.passed;
            }
            if (i + 1 >= size) {
                // Border below last row
                this.weekEndRows.add(Integer.valueOf(i));
            } else {
                final ActivityRow next = data.get(i + 1);
                if (next.week != record.week) {
                    this.weekEndRows.add(Integer.valueOf(i));
                }
            }

            getModel().addRow(row);
        }

        // Adds an empty row...
        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }

    /**
     * Prepares the component that will render a cell of the table.
     *
     * @param renderer the cell renderer
     * @param row      the row
     * @param column   the column
     * @return the component
     */
    @Override
    public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {

        final Object value = getValueAt(row, column);
        final JLabel inner = new JLabel(value == null ? CoreConstants.EMPTY : value.toString());

        final JPanel result = new JPanel(new BorderLayout());
        result.setBackground(inner.getBackground());
        result.add(inner, BorderLayout.CENTER);

        int bottomBorder = 0;
        int rightBorder = 0;

        if (row < this.rows.size()) {
            final ActivityRow r = this.rows.get(row);
            if (column == 0) {
                if ((r.week & 1) == 1) {
                    result.setBackground(Skin.LIGHTER_GREEN);
                } else {
                    result.setBackground(Skin.LIGHTER_YELLOW);
                }

                if (this.weekEndRows.contains(Integer.valueOf(row))) {
                    bottomBorder = 2;
                } else {
                    bottomBorder = 1;
                }
                rightBorder = 1;

            } else if ("(none)".equals(r.activity)) {
                result.setBackground(Skin.LIGHTER_GRAY);
                bottomBorder = 2;
            } else {
                if (column == 3 && r.activity != null) {
                    if ("Placement".equals(r.activity)) {
                        result.setBackground(Skin.LIGHTER_RED);
                    } else if ("User's Exam".equals(r.activity)) {
                        result.setBackground(Skin.LIGHTER_MAGENTA);
                    } else if ("R".equals(r.activity)) {
                        if (Integer.valueOf(0).equals(r.unit)) {
                            result.setBackground(Skin.LIGHTER_CYAN);
                        } else {
                            result.setBackground(Skin.LIGHTER_YELLOW);
                        }
                    } else if ("U".equals(r.activity)) {
                        result.setBackground(Skin.LIGHTER_GREEN);
                    } else if ("F".equals(r.activity)) {
                        result.setBackground(Skin.LT_GREEN);
                    } else if (r.activity.startsWith("HW ")) {
                        result.setBackground(Skin.LIGHTER_GRAY);
                    } else {
                        result.setBackground(Skin.WHITE);
                    }
                } else {
                    result.setBackground(Skin.WHITE);
                }

                if (this.weekEndRows.contains(Integer.valueOf(row))) {
                    bottomBorder = 2;
                } else {
                    bottomBorder = 1;
                }
                rightBorder = 1;
            }
        } else {
            // Empty row
            result.setBackground(Skin.WHITE);
        }

        if (bottomBorder == 0) {
            result.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        } else {
            result.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, bottomBorder, rightBorder, Color.gray),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        }

        return result;
    }
}
