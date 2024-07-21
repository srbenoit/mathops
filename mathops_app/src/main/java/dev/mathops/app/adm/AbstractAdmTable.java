package dev.mathops.app.adm;

import dev.mathops.commons.CoreConstants;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * A base class for tables used in the admin app.
 *
 * @param <T> the record type
 */
public abstract class AbstractAdmTable<T> extends JTable {

    /** Date formatter. */
    protected static final DateTimeFormatter FMT_TEXT =
            DateTimeFormatter.ofPattern("LLL dd',' yyyy", Locale.US);

    /** Date formatter. */
    protected static final DateTimeFormatter FMT_WTEXT =
            DateTimeFormatter.ofPattern("EEE, LLL dd", Locale.US);

    /** Date formatter. */
    protected static final DateTimeFormatter FMT_MDY =
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);

    /** Date formatter. */
    protected static final DateTimeFormatter FMT_MDY_HM =
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh':'mm a", Locale.US);

    /** Time formatter. */
    protected static final DateTimeFormatter FMT_HM =
            DateTimeFormatter.ofPattern("hh':'mm a", Locale.US);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8079216057369843513L;

    /** The default row height. */
    private static final int DEFAULT_ROW_HEIGHT = 20;

    /** The table model. */
    private final LocalTableModel model;

    /** The table width. */
    private final int width;

    /**
     * Constructs a new {@code AbstractAdmTable}.
     *
     * @param columns the list of columns in the table
     */
    protected AbstractAdmTable(final List<LocalTableColumn> columns) {

        super();

        this.model = new LocalTableModel(columns);
        this.width = init(columns);

        updatePrefSize();
    }

    /**
     * Initialize the UI.
     */
    private int init(final List<? extends LocalTableColumn> columns) {

        setFont(Skin.MONO_14_FONT);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        getTableHeader().setFont(Skin.MEDIUM_15_FONT);
        setRowHeight(DEFAULT_ROW_HEIGHT);
        setModel(this.model);

        setColumnModel(new LocalColumnModel(columns));

        final Dimension spacing = getIntercellSpacing();

        int w = (columns.size() + 1) * spacing.width;
        for (final LocalTableColumn col : columns) {
            w += col.colWidth;
        }

        return w;
    }

    /**
     * Updates the preferred size.
     */
    public final void updatePrefSize() {

        final int numModelRows = this.model.getRowCount();
        final int numRows = Math.max(2, numModelRows);

        final int spacing = getIntercellSpacing().height;

        final int height = getTableHeader().getHeight() + spacing + numRows * (getRowHeight() + spacing);

        final Dimension newPref = new Dimension(this.width, height);

        setPreferredSize(newPref);
        setSize(newPref);
    }

    /**
     * Gets the table model.
     */
    @Override
    public final LocalTableModel getModel() {

        return this.model;
    }

    /**
     * Clears the table, removing all rows.
     */
    public final void clear() {

        int numRows = this.model.getRowCount();
        while (numRows > 0) {
            --numRows;
            this.model.removeRow(numRows);
        }
    }

    /**
     * Adds data from a list of {@code StudentCourse} records.
     *
     * @param data    the data to add
     * @param minRows the minimum number of rows to show in the table
     */
    public final void addData(final List<? extends T> data, final int minRows) {

        installData(data, minRows);

        this.model.fireTableDataChanged();
        updatePrefSize();
    }

    /**
     * Gets the preferred size for a scroll pane containing this table.
     *
     * @param scroll  the scroll pane whose insets to add to the preferred size
     * @param minRows the minimum number of rows for which to provide space
     * @return the preferred scroll pane size
     */
    public final Dimension getPreferredScrollSize(final JScrollPane scroll, final int minRows) {

        final Dimension pref = getPreferredSize();

        final int header = getTableHeader().getHeight();
        final Insets insets = scroll.getInsets();

        int allRowsHeight = pref.height;
        if (this.model.getRowCount() < minRows) {
            final Dimension spacing = getIntercellSpacing();
            allRowsHeight = minRows * getRowHeight() + (minRows + 1) * spacing.height;
        }

        return new Dimension(pref.width + insets.left + insets.right,
                allRowsHeight + header + insets.top + insets.bottom);
    }

    /**
     * Installs data in the table.
     *
     * @param data    the data to install
     * @param minRows the minimum number of rows to show in the table
     * @return the number of records in the table after the insert
     */
    protected abstract int installData(List<? extends T> data, int minRows);

    /**
     * Given an object value, returns the empty string if the object is null, or the string representation of the
     * object.
     *
     * @param value the value
     * @return the string representation
     */
    protected static String valueToString(final Object value) {

        return value == null ? CoreConstants.EMPTY : value.toString();
    }

    /**
     * Data used to configure one column in the table.
     */
    public static class LocalTableColumn {

        /** The column name. */
        final String name;

        /** The column width. */
        final int colWidth;

        /**
         * Constructs a new {@code AdminTableColumn}.
         *
         * @param theName     the column name
         * @param theColWidth the column width
         */
        public LocalTableColumn(final String theName, final int theColWidth) {

            this.name = theName;
            this.colWidth = theColWidth;
        }
    }

    /**
     * The table model for the table.
     */
    public static final class LocalTableModel extends DefaultTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -1135232376661549453L;

        /**
         * Constructs a new {@code TableModel}.
         *
         * @param columns the table columns
         */
        LocalTableModel(final Iterable<? extends LocalTableColumn> columns) {

            super();

            for (final LocalTableColumn col : columns) {
                addColumn(col.name);
            }
        }
    }

    /**
     * The column model for the table.
     */
    static final class LocalColumnModel extends DefaultTableColumnModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 6467398116324893177L;

        /**
         * Constructs a new {@code TableColumnModel}.
         *
         * @param columns the table columns
         */
        LocalColumnModel(final List<? extends LocalTableColumn> columns) {

            super();

            final int numCols = columns.size();

            for (int i = 0; i < numCols; ++i) {
                final LocalTableColumn col = columns.get(i);

                final TableColumn tableCol = new TableColumn(i, col.colWidth);
                tableCol.setHeaderValue(col.name);
                tableCol.setResizable(true);
                tableCol.setMinWidth(col.colWidth);
                tableCol.setPreferredWidth(col.colWidth);
                tableCol.setMaxWidth(col.colWidth * 3);
                addColumn(tableCol);
            }
        }
    }
}
