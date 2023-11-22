package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.WideAddr;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.Serial;

/**
 * A panel to show disassembly of a block of memory, usually at the current PC.
 */
final class DisassemblyPanel extends LoggedPanel implements TableCellRenderer, ListSelectionListener,
        ICalcStateListener {

    /** Grid color. */
    private static final Color GRID_COLOR = new Color(220, 220, 240);

    /** Color for selected rows. */
    private static final Color SEL_COLOR = new Color(200, 200, 150);

    /** Color for address in rows with breakpoints (0x01 adds red, 0x02 green, 0x04 blue). */
    private static final Color[] BREAK_COLOR = {new Color(100, 100, 100), new Color(200, 100, 100),
            new Color(100, 200, 100), new Color(200, 200, 100), new Color(100, 100, 200),
            new Color(200, 100, 200), new Color(100, 200, 200), new Color(200, 200, 200),};

    /** Color for disassembly address. */
    private static final Color ADDR_COLOR = new Color(0, 0, 0);

    /** Color for disassembly data. */
    private static final Color DATA_COLOR = new Color(50, 50, 50);

    /** Color for disassembly instruction. */
    private static final Color INSTR_COLOR = new Color(30, 150, 50);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2561329404041733249L;

    /** The disassembly table. */
    private final JTable disassemblyTable;

    /** The data model for the table. */
    private final DisasmTableModel tableModel;

    /** A plain monospace font. */
    private final Font monoplain;

    /** A bold monospace font. */
    private final Font monobold;

    /** The action handler. */
    private final ActionHandler handler;

    /**
     * Constructs a new {@code DisassemblyPanel}.
     *
     * @param theHandler the action handler
     */
    DisassemblyPanel(final ActionHandler theHandler) {

        super(new BorderLayout());

        this.handler = theHandler;

        setBackground(Debugger.BG_COLOR);

        this.monoplain = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);
        this.monobold = Fonts.getMono().deriveFont(Font.BOLD, 11.0f);

        final JPanel disasmPane = new JPanel(new BorderLayout());
        disasmPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        disasmPane.setBackground(Color.WHITE);
        add(disasmPane, BorderLayout.CENTER);

        final JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setBackground(Color.WHITE);
        disasmPane.add(inner, BorderLayout.CENTER);

        this.tableModel = new DisasmTableModel();
        this.disassemblyTable = new JTable(this.tableModel);
        this.disassemblyTable.setBackground(Color.WHITE);
        this.disassemblyTable.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.disassemblyTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        this.disassemblyTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        this.disassemblyTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        this.disassemblyTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        this.disassemblyTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        this.disassemblyTable.getColumnModel().getColumn(0).setResizable(false);
        this.disassemblyTable.getColumnModel().getColumn(1).setResizable(false);
        this.disassemblyTable.getColumnModel().getColumn(2).setResizable(false);
        this.disassemblyTable.getColumnModel().getColumn(3).setResizable(false);
        this.disassemblyTable.getColumnModel().getColumn(4).setResizable(false);

        this.disassemblyTable.setShowHorizontalLines(false);
        this.disassemblyTable.setShowVerticalLines(false);
        this.disassemblyTable.setRowSelectionAllowed(true);
        this.disassemblyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.disassemblyTable.getSelectionModel().addListSelectionListener(this);

        this.disassemblyTable.setDefaultRenderer(Object.class, this);

        inner.add(this.disassemblyTable.getTableHeader(), BorderLayout.PAGE_START);

        final JScrollPane scroll = new JScrollPane(this.disassemblyTable);
        scroll.setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(
                60 + 60 + 350 + 40 + 60 + scroll.getVerticalScrollBar().getPreferredSize().width + 2, 200));

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

        final Z80Info[] info = Disassemble.disassembleAll(theCalc);
        this.tableModel.setData(info);

        final int pcRow = findPcRow(theCalc);
        this.disassemblyTable.getSelectionModel().setSelectionInterval(pcRow, pcRow);
        final Rectangle rect = this.disassemblyTable.getCellRect(pcRow, 0, false);
        // make rectangle and 3 lines above and below visible
        rect.y = rect.y - 3 * rect.height;
        rect.height = rect.height * 7;
        this.disassemblyTable.scrollRectToVisible(rect);
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.disassemblyTable.setEnabled(enable);
        this.disassemblyTable.setBackground(enable ? Color.WHITE : GRID_COLOR);
    }

    /**
     * Finds the row in the current data that corresponds to the CPU PC register.
     *
     * @param theCalc the calculator
     * @return the row
     */
    private int findPcRow(final Calc theCalc) {

        final WideAddr pcAddr = theCalc.getCPU().getMemoryContext().addr16ToWideAddr(theCalc.getCPU().getPC());

        final Z80Info[] data = this.tableModel.getData();
        int pcRow = 0;
        final int dataLen = data.length;
        for (int i = 0; i < dataLen; ++i) {
            if (data[i].getWaddr().getAddr() == pcAddr.getAddr()
                    && data[i].getWaddr().getPage() == pcAddr.getPage()) {
                pcRow = i;
                break;
            }
        }

        return pcRow;
    }

    /**
     * Returns the component used for drawing the cell. This method is used to configure the renderer appropriately
     * before drawing.
     *
     * @param table      the table
     * @param value      the cell value
     * @param isSelected true if the cell is selected
     * @param hasFocus   true if the table has focus
     * @param row        the row
     * @param column     the column
     * @return the component
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {

        final Z80Info info = this.tableModel.getData()[row];
        final int bp = info.getBreakpoint();

        final JLabel lbl = new JLabel(value.toString());

        if (column == 0) {
            lbl.setFont(this.monobold);
            lbl.setForeground(ADDR_COLOR);
        } else {
            lbl.setFont(this.monoplain);
            if (column == 1) {
                lbl.setForeground(DATA_COLOR);
            } else if (column == 2) {
                lbl.setForeground(INSTR_COLOR);
            }
        }

        if (bp != 0 && column == 0) {
            lbl.setBackground(BREAK_COLOR[bp & 0x07]);
            lbl.setOpaque(true);
        } else if (isSelected) {
            lbl.setBackground(SEL_COLOR);
            lbl.setOpaque(true);
        } else {
            lbl.setOpaque(false);
        }

        return lbl;
    }

    /**
     * Called when the list selection changes.
     *
     * @param e the list selection event
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        final int row = this.disassemblyTable.getSelectedRow();
        if (row == -1) {
            this.handler.setSelectedAddr(null);
        } else {
            this.handler.setSelectedAddr(this.tableModel.getData()[row].getWaddr());
        }
    }

    /**
     * A table model for disassembly.
     */
    private static final class DisasmTableModel extends AbstractTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 2762226791290903652L;

        /** A zero-length array of Z80 info blocks. */
        private static final Z80Info[] ZERO_LEN_Z80INFO_ARR = new Z80Info[0];

        /** The disassembly. */
        private Z80Info[] data;

        /**
         * Constructs a new {@code DisasmTableModel}.
         */
        DisasmTableModel() {

            super();

            this.data = ZERO_LEN_Z80INFO_ARR;
        }

        /**
         * Sets the disassembly to display.
         *
         * @param theData the disassembly
         */
        void setData(final Z80Info[] theData) {

            this.data = theData;
            fireTableDataChanged();
        }

        /**
         * Gets the data.
         *
         * @return the disassembly data
         */
        Z80Info[] getData() {

            return this.data;
        }

        /**
         * Gets the number of rows.
         *
         * @return the number of rows
         */
        @Override
        public int getRowCount() {

            return this.data == null ? 0 : this.data.length;
        }

        /**
         * Gets the number of columns.
         *
         * @return the number of columns
         */
        @Override
        public int getColumnCount() {

            return 5;
        }

        /**
         * Gets the name of a column.
         */
        @Override
        public String getColumnName(final int column) {

            final String name;

            if (column == 0) {
                name = "Address";
            } else if (column == 1) {
                name = "Data";
            } else if (column == 2) {
                name = "Disassembly";
            } else if (column == 3) {
                name = "Size";
            } else if (column == 4) {
                name = "Clocks";
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

            final StringBuilder result = new StringBuilder(50);

            switch (columnIndex) {
                case 0:
                    final int page = this.data[rowIndex].getWaddr().getPage();
                    final int addr = this.data[rowIndex].getWaddr().getAddr();

                    final String pageHex = Integer.toHexString(page);
                    final String addrHex = Integer.toHexString(addr);

                    if (pageHex.length() == 1) {
                        result.append("0");
                        result.append(pageHex);
                    } else {
                        result.append(pageHex);
                    }

                    result.append(' ');

                    if (addrHex.length() == 1) {
                        result.append("000");
                        result.append(addrHex);
                    } else if (addrHex.length() == 2) {
                        result.append("00");
                        result.append(addrHex);
                    } else if (addrHex.length() == 3) {
                        result.append("0");
                        result.append(addrHex);
                    } else if (addrHex.length() == 4) {
                        result.append(addrHex);
                    }
                    break;

                case 1:
                    final int[] opcodeData = this.data[rowIndex].getOpcodeData();
                    for (final int opcodeDatum : opcodeData) {
                        result.append(Integer.toHexString((opcodeDatum >> 4) & 0x0F));
                        result.append(Integer.toHexString(opcodeDatum & 0x0F));
                    }
                    break;
                case 2:
                    result.append(this.data[rowIndex].getExpanded());
                    break;
                case 3:
                    result.append(this.data[rowIndex].getSize());
                    break;
                case 4:
                    result.append(this.data[rowIndex].getClocks());
                    break;
                default:
                    break;
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
