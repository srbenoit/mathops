package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.IDevice;
import jwabbit.core.PIOContext;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.Serial;

/**
 * A panel that monitors the last data read from or written to each port.
 */
final class PortMonitorPanel extends LoggedPanel implements ICalcStateListener {

    /** Grid color. */
    private static final Color GRID_COLOR = new Color(220, 220, 240);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3160537985221140191L;

    /** The information table. */
    private final JTable table;

    /** The data model for the table. */
    private final PortMonitorTableModel tableModel;

    /**
     * Constructs a new {@code PortMonitorPanel}.
     */
    PortMonitorPanel() {

        super(new BorderLayout());
        setBackground(Debugger.BG_COLOR);

        final JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(Debugger.BG_COLOR);
        inner.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0),
                BorderFactory.createLineBorder(Color.GRAY)));
        add(inner, BorderLayout.CENTER);

        this.tableModel = new PortMonitorTableModel();
        this.table = new JTable(this.tableModel);

        this.table.getColumnModel().getColumn(0).setPreferredWidth(40);
        this.table.getColumnModel().getColumn(0).setMaxWidth(60);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(100);
        this.table.getColumnModel().getColumn(1).setMinWidth(100);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(120);
        this.table.getColumnModel().getColumn(2).setMinWidth(100);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(120);
        this.table.getColumnModel().getColumn(3).setMinWidth(100);

        this.table.getColumnModel().getColumn(0).setResizable(false);
        this.table.getColumnModel().getColumn(1).setResizable(false);
        this.table.getColumnModel().getColumn(2).setResizable(false);
        this.table.getColumnModel().getColumn(3).setResizable(false);

        this.table.setShowHorizontalLines(true);
        this.table.setShowVerticalLines(true);
        this.table.setRowSelectionAllowed(false);
        this.table.setColumnSelectionAllowed(false);
        this.table.setGridColor(GRID_COLOR);
        this.table.setBackground(Color.WHITE);

        final Font sans = Fonts.getSans().deriveFont(Font.BOLD, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        this.table.setFont(mono);
        this.table.getTableHeader().setFont(sans);

        inner.add(this.table.getTableHeader(), BorderLayout.PAGE_START);
        final JScrollPane scroll = new JScrollPane(this.table);
        scroll.setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(
                40 + 100 + 120 + 120 + scroll.getVerticalScrollBar().getPreferredSize().width + 2, 200));

        inner.add(scroll, BorderLayout.CENTER);
    }

    /**
     * Called from the calculator thread to allow a client to retrieve data values from a running or stopped calculator
     * without fear of thread conflicts. The receiver should try to minimize time in the function, but will have
     * exclusive access to the calculator data while this method executes.
     *
     * @param theCalc the calculator
     */
    @Override
    public void calcState(final Calc theCalc) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.tableModel.update(theCalc.getCPU().getPIOContext());
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.table.setEnabled(enable);
        this.table.setBackground(enable ? Color.WHITE : GRID_COLOR);
    }

    /**
     * A table model for a list of ports and their data.
     */
    private static final class PortMonitorTableModel extends AbstractTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 6698748870623994440L;

        /** The list of device indexes. */
        private int[] devList;

        /** The list of device names. */
        private String[] devNames;

        /** The list of last input values. */
        private int[] lastInput;

        /** The list of last output values. */
        private int[] lastOutput;

        /**
         * Constructs a new {@code PortMonitorTableModel}.
         */
        PortMonitorTableModel() {

            super();

            this.devList = new int[0];
            this.devNames = new String[0];
            this.lastInput = this.devList;
            this.lastOutput = this.devList;
        }

        /**
         * Updates the table data.
         *
         * @param pio the PIO context from which to update
         */
        void update(final PIOContext pio) {

            this.devList = pio.getDeviceIndexes();
            final int numDevs = this.devList.length;

            this.devNames = new String[numDevs];
            this.lastInput = new int[numDevs];
            this.lastOutput = new int[numDevs];

            for (int i = 0; i < numDevs; ++i) {
                final IDevice dev = pio.getDevice(this.devList[i]);
                this.devNames[i] = dev.getClass().getSimpleName();
                this.lastInput[i] = pio.getMostRecentInput(this.devList[i]);
                this.lastOutput[i] = pio.getMostRecentOutput(this.devList[i]);
            }
            fireTableDataChanged();
        }

        /**
         * Gets the number of rows.
         *
         * @return the number of rows
         */
        @Override
        public int getRowCount() {

            return this.devList.length;
        }

        /**
         * Gets the number of columns.
         *
         * @return the number of columns
         */
        @Override
        public int getColumnCount() {

            return 4;
        }

        /**
         * Gets the name of a column.
         */
        @Override
        public String getColumnName(final int column) {

            final String name;

            if (column == 0) {
                name = "Port";
            } else if (column == 1) {
                name = "Name";
            } else if (column == 2) {
                name = "Last Input";
            } else if (column == 3) {
                name = "Last Output";
            } else {
                name = "?";
            }

            return name;
        }

        /**
         * Gets the value at a cell.
         *
         * @param rowIndex    the row
         * @param columnIndex the column
         * @return the value
         */
        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {

            final StringBuilder result = new StringBuilder(10);

            if (rowIndex >= 0 && rowIndex < this.devList.length) {

                final int index = this.devList[rowIndex];
                if (index < 0) {
                    result.append("ERR");
                } else if (columnIndex == 0) {
                    result.append(Integer.toHexString((index >> 4) & 0x0F));
                    result.append(Integer.toHexString(index & 0x0F));
                } else if (columnIndex == 1) {
                    result.append(this.devNames[rowIndex]);
                } else if (columnIndex == 2) {
                    final int input = this.lastInput[rowIndex];
                    if (input == -1) {
                        result.append('-');
                    } else {
                        result.append('$');
                        result.append(Integer.toHexString((input >> 4) & 0x0F));
                        result.append(Integer.toHexString(input & 0x0F));
                        result.append("/");
                        result.append(input);
                        result.append("/%");
                        result.append((input & 0x80) == 0 ? '0' : '1');
                        result.append((input & 0x40) == 0 ? '0' : '1');
                        result.append((input & 0x20) == 0 ? '0' : '1');
                        result.append((input & 0x10) == 0 ? '0' : '1');
                        result.append((input & 0x08) == 0 ? '0' : '1');
                        result.append((input & 0x04) == 0 ? '0' : '1');
                        result.append((input & 0x02) == 0 ? '0' : '1');
                        result.append((input & 0x01) == 0 ? '0' : '1');
                    }
                } else if (columnIndex == 3) {
                    final int output = this.lastOutput[rowIndex];
                    if (output == -1) {
                        result.append('-');
                    } else {
                        result.append('$');
                        result.append(Integer.toHexString((output >> 4) & 0x0F));
                        result.append(Integer.toHexString(output & 0x0F));
                        result.append("/");
                        result.append(output);
                        result.append("/%");
                        result.append((output & 0x80) == 0 ? '0' : '1');
                        result.append((output & 0x40) == 0 ? '0' : '1');
                        result.append((output & 0x20) == 0 ? '0' : '1');
                        result.append((output & 0x10) == 0 ? '0' : '1');
                        result.append((output & 0x08) == 0 ? '0' : '1');
                        result.append((output & 0x04) == 0 ? '0' : '1');
                        result.append((output & 0x02) == 0 ? '0' : '1');
                        result.append((output & 0x01) == 0 ? '0' : '1');
                    }
                }
            }

            return result.toString();
        }

        /**
         * Checks whether a cell is editable.
         *
         * @param rowIndex    the row
         * @param columnIndex the column
         * @return true if editable, false if not
         */
        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {

            return false;
        }
    }
}
