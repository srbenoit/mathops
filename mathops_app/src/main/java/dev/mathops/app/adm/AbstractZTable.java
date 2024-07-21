package dev.mathops.app.adm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * A base class for constructed tables used in the admin app. This class uses a GridBagLayout and components rather than
 * a JTable, so we can add controls (like view/edit buttons) to each row, and allow column headings to be clicked to
 * sort data.
 *
 * @param <T> the record type
 */
public abstract class AbstractZTable<T> extends JPanel implements ActionListener {

    /** Date formatter. */
    protected static final DateTimeFormatter FMT_WMD = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);

    /** Time formatter. */
    protected static final DateTimeFormatter FMT_HM = DateTimeFormatter.ofPattern("hh':'mm a", Locale.US);

    /** A commonly used insets object. */
    private static final Insets INSETS = new Insets(0, 0, 0, 0);

    /** A commonly used insets object. */
    private static final Insets NO_INSETS = new Insets(0, 0, 0, 0);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1160657892889154797L;

    /** The data currently in the table. */
    private final List<T> curData;

    /** The listener to receive commands from buttons pressed within rows. */
    private final IZTableCommandListener<? super T> listener;

    /**
     * Constructs a new {@code AbstractZTable}.
     *
     * @param theListener      the listener that will be notified when a button is pressed in a row
     */
    protected AbstractZTable(final IZTableCommandListener<? super T> theListener) {

        super(new GridBagLayout());
        setBackground(Skin.LIGHT);

        this.curData = new ArrayList<>(100);
        this.listener = theListener;

        addColumnHeadings();
    }

    /**
     * Adds the column headings to the table.
     */
    protected abstract void addColumnHeadings();

    /**
     * Clears the table, removing all rows.
     */
    public final void clear() {

        removeAll();
        addColumnHeadings();
        this.curData.clear();
    }

    /**
     * Gets the number of rows (not counting the header row) in the table.
     *
     * @return the number of rows
     */
    protected final int getNumRows() {

        return this.curData.size();
    }

    /**
     * Gets a row.
     *
     * @param index the index of the row to retrieve
     * @return the row record
     */
    protected final T getRow(final int index) {

        return this.curData.get(index);
    }

    /**
     * Sets table data from a list of records. This method should call {@code storeCurrentData} if any records will
     * include buttons.
     *
     * @param data the data
     */
    public abstract void setData(List<T> data);

    /**
     * Stores table records so button actions can return the record on which an action was invoked.
     *
     * @param data table data
     */
    protected final void storeCurrentData(final Collection<? extends T> data) {

        this.curData.clear();
        this.curData.addAll(data);
    }

    /**
     * Given an object value, returns the empty string if the object is null, or the string representation of the
     * object.
     *
     * @param value the value
     * @return the string representation
     */
    private static String valueToString(final Object value) {

        return value == null ? CoreConstants.EMPTY : value.toString();
    }

    /**
     * Adds a {@code JLabel} cell to the table with header style.
     *
     * @param text       the label text (if null, an empty string is substituted)
     * @param x          the x cell index (0-based)
     * @param y          the y cell index (0-based)
     * @param numColumns the number of columns (used to detect if this is the first or last
     */
    protected final void addHeaderCell(final Object text, final int x, final int y, final int numColumns) {

        final GridBagConstraints constraints = makeConstraints(x, y);
        add(new HeaderPanel(text, x == 0, x == numColumns - 1), constraints);
    }

    /**
     * Adds a non-editable {@code JTextField} cell to the table.
     *
     * @param text the label text (if null, an empty string is substituted)
     * @param x    the x cell index (0-based)
     * @param y    the y cell index (0-based)
     */
    protected final void addCell(final Object text, final int x, final int y) {

        final String valueStr = valueToString(text);
        final JTextField fld = new JTextField(valueStr);
        fld.setEditable(false);
        final Border padding = BorderFactory.createEmptyBorder(1, 3, 1, 3);
        fld.setBorder(padding);
        fld.setFont(Skin.MONO_12_FONT);
        fld.setOpaque(true);

        if ((y & 1) == 1) {
            fld.setBackground(Skin.WHITE);
        } else {
            fld.setBackground(Skin.TABLE_ROW_HIGHLIGHT);
        }

        final GridBagConstraints constraints = makeConstraints(x, y);
        add(fld, constraints);
    }

    /**
     * Adds a {@code JButton} cell to the table.
     *
     * @param label the button label
     * @param x     the x cell index (0-based)
     * @param y     the y cell index (0-based, where 0 is the header row)
     * @param cmd   the action command for the button (the actual command will be constructed with this prefix and the
     *              row index, but the action handler on this object will catch the action, parse the row index,
     *              retrieve the record, and forward the command with the row index removed to the listener)
     * @return the button that was placed in the cell
     */
    protected final JButton addButtonCell(final String label, final int x, final int y, final String cmd) {

        final int rowIndex = y - 1;

        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));

        final JButton btn = new JButton(label);
        btn.setActionCommand(cmd + "_" + rowIndex);
        btn.addActionListener(this);
        flow.add(btn);

        if ((y & 1) == 1) {
            flow.setBackground(Skin.WHITE);
        } else {
            flow.setBackground(Skin.TABLE_ROW_HIGHLIGHT);
        }

        final GridBagConstraints constraints = makeConstraints(x, y);
        add(flow, constraints);

        return btn;
    }

    /**
     * Adds a {@code JCheckBox} cell to the table.
     *
     * @param label   the checkbox label
     * @param x       the x cell index (0-based)
     * @param y       the y cell index (0-based, where 0 is the header row)
     * @param cmd     the action command for the button (the actual command will be constructed with this prefix and the
     *                row index, but the action handler on this object will catch the action, parse the row index,
     *                retrieve the record, and forward the command with the row index removed to the listener)
     * @param checked the initial state of the checkbox
     * @return the button that was placed in the cell
     */
    protected final JCheckBox makeCheckBoxCell(final String label, final int x, final int y, final String cmd,
                                               final boolean checked) {

        final int rowIndex = y - 1;

        final JCheckBox check = new JCheckBox(label, checked);
        final Border padding = BorderFactory.createEmptyBorder(1, 3, 1, 3);
        check.setBorder(padding);
        check.setActionCommand(cmd + "_" + rowIndex);
        check.addActionListener(this);
        check.setOpaque(true);

        if (y % 2 == 0) {
            check.setBackground(Skin.WHITE);
        } else {
            check.setBackground(Skin.TABLE_ROW_HIGHLIGHT);
        }

        final GridBagConstraints constraints = makeConstraints(x, y);
        add(check, constraints);

        return check;
    }

    /**
     * Adds a last row to the table that can stretch as needed to fill space. This allows the table to align itself with
     * the top of the scroll pane when it is smaller than the scroll pane client area.
     *
     * @param y          the cell index of the last row
     * @param numColumns the number of columns
     */
    protected final void addLastRow(final int y, final int numColumns) {

        final JPanel empty = new JPanel();
        final MatteBorder overline = BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.MEDIUM);
        empty.setBorder(overline);

        if (y % 2 == 0) {
            empty.setBackground(Skin.WHITE);
        } else {
            empty.setBackground(Skin.TABLE_ROW_HIGHLIGHT);
        }

        add(empty, new GridBagConstraints(0, y, numColumns, 1, 0.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, NO_INSETS, 0, 0));
    }

    /**
     * Adds a last column to the table that can stretch as needed to fill space. This allows the table to align itself
     * with the left of the scroll pane when it is smaller than the scroll pane client area.
     *
     * @param x       the cell index of the last column
     * @param numRows the number of rows
     */
    protected final void addLastCol(final int x, final int numRows) {

        final JPanel empty = new JPanel();
        empty.setBackground(Skin.LIGHTEST);
        empty.setPreferredSize(new Dimension(0, 0));

        add(empty, new GridBagConstraints(x, 0, 1, numRows, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, NO_INSETS, 0, 0));
    }

    /**
     * Makes grid bag constraints for an object.
     *
     * @param x the x cell index (0-based)
     * @param y the y cell index (0-based)
     * @return the constraints
     */
    private static GridBagConstraints makeConstraints(final int x, final int y) {

        return new GridBagConstraints(x, y, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, INSETS, 3, 1);
    }

    /**
     * Called when a "Review" button is pressed.
     */
    @Override
    public final void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        final int sep = cmd.lastIndexOf((int) CoreConstants.SPC_CHAR);
        if (sep == -1) {
            Log.warning("Unrecognized action command");
        } else if (this.listener != null) {
            final String indexStr = cmd.substring(sep + 1);

            try {
                final int index = Integer.parseInt(indexStr);

                if (index < 0 || index >= this.curData.size()) {
                    Log.warning("Invalid row index: '", indexStr, "'");
                } else {
                    final String substring = cmd.substring(0, sep);
                    final T rowData = this.curData.get(index);
                    this.listener.commandOnRow(index, rowData, substring);
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid row index: '", indexStr, "'", ex);
            }
        }
    }

    /**
     * A header panel that styles a label.
     */
    static final class HeaderPanel extends JPanel {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = -1057740420259153313L;

        /** The header text. */
        private final String text;

        /** True if this is the first column. */
        private final boolean firstColumn;

        /** True if this is the last column. */
        private final boolean lastColumn;

        /**
         * Constructs a new {@code HeaderPanel}.
         *
         * @param value            the header value (the string representation of this object is used)
         * @param isFirstColumn    true if this is the first column
         * @param isLastColumn     true if this is the last column
         */
        HeaderPanel(final Object value, final boolean isFirstColumn, final boolean isLastColumn) {

            super(new FlowLayout(FlowLayout.LEADING, 0, 0));
            setBackground(Skin.LIGHTEST);
            final Border padding = BorderFactory.createEmptyBorder(3, 6, 3, 6);
            setBorder(padding);
            setAlignmentY(0.0f);

            this.text = value == null ? CoreConstants.SPC : value.toString();
            this.firstColumn = isFirstColumn;
            this.lastColumn = isLastColumn;

            final JLabel lbl = new JLabel(this.text);
            lbl.setFont(Skin.MEDIUM_15_FONT);

            add(lbl);
        }

        /**
         * Paints the panel.
         *
         * @param g the {@code Graphics} to which to draw
         */
        @Override
        public void paintComponent(final Graphics g) {

            final Dimension size = getSize();

            if (this.text.isEmpty()) {
                super.paintComponent(g);
                g.setColor(Skin.MEDIUM);
                g.drawLine(0, size.height - 1, size.width, size.height - 1);
            } else {
                for (int y = size.height - 1; y >= 0; --y) {
                    final double sine = Math.sin(Math.PI * (double) y / (double) size.height);
                    final int value = 251 - (int) Math.round(28.0 * sine);
                    final Color color = new Color(value - 5, value - 5, value);
                    g.setColor(color);
                    g.drawLine(0, size.height - y, size.width - 1, size.height - y);
                }

                g.setColor(Skin.MEDIUM);
                if (this.firstColumn) {
                    g.drawRect(-1, -1, size.width, size.height);
                } else if (this.lastColumn) {
                    g.drawRect(0, -1, size.width - 1, size.height);
                } else {
                    g.drawRect(0, -1, size.width, size.height);
                }
            }
        }
    }
}
