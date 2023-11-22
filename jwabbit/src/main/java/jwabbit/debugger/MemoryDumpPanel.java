package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A panel that displays the contents of a Memory object as hexadecimal bytes.
 */
final class MemoryDumpPanel extends LoggedPanel implements ActionListener, ICalcStateListener {

    /** Grid color. */
    private static final Color GRID_COLOR = new Color(220, 220, 240);

    /** Background color. */
    private static final char MISSING_CHAR = '\u2026';

    /** Display individual bytes of memory. */
    private static final int BYTE_MODE = 1;

    /** Display words of memory. */
    private static final int WORD_MODE = 2;

    /** Display bytes of memory as ASCII characters. */
    private static final int CHAR_MODE = 3;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8082988912756378693L;

    /** True to show RAM, false for flash. */
    private final boolean ram;

    /** The information table. */
    private final JTable table;

    /** The data model for the table. */
    private final MemoryDumpTableModel tableModel;

    /**
     * Constructs a new {@code MemoryDumpPanel}.
     *
     * @param isRam true to show RAM, false for flash
     */
    MemoryDumpPanel(final boolean isRam) {

        super(new BorderLayout());

        this.ram = isRam;

        setBackground(Debugger.BG_COLOR);

        final JPanel north = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        north.setBackground(Debugger.BG_COLOR);
        add(north, BorderLayout.PAGE_START);

        final ButtonGroup modeGroup = new ButtonGroup();
        north.add(new JLabel("Mode:"));

        final JRadioButton byteMode = new JRadioButton("byte");
        byteMode.setBackground(Debugger.BG_COLOR);
        modeGroup.add(byteMode);
        byteMode.setActionCommand("byte");
        byteMode.setSelected(true);
        byteMode.addActionListener(this);
        north.add(byteMode);

        final JRadioButton wordMode = new JRadioButton("word");
        wordMode.setBackground(Debugger.BG_COLOR);
        modeGroup.add(wordMode);
        wordMode.setActionCommand("word");
        wordMode.addActionListener(this);
        north.add(wordMode);

        final JRadioButton charMode = new JRadioButton("char");
        charMode.setBackground(Debugger.BG_COLOR);
        modeGroup.add(charMode);
        charMode.setActionCommand("char");
        charMode.addActionListener(this);
        north.add(charMode);

        final ButtonGroup baseGroup = new ButtonGroup();
        north.add(new JLabel("    Base:"));

        final JRadioButton base16 = new JRadioButton("16");
        base16.setBackground(Debugger.BG_COLOR);
        baseGroup.add(base16);
        base16.setActionCommand("16");
        base16.setSelected(true);
        base16.addActionListener(this);
        north.add(base16);

        final JRadioButton base10 = new JRadioButton("10");
        base10.setBackground(Debugger.BG_COLOR);
        baseGroup.add(base10);
        base10.setActionCommand("10");
        base10.addActionListener(this);
        north.add(base10);

        final JRadioButton base2 = new JRadioButton("2");
        base2.setBackground(Debugger.BG_COLOR);
        baseGroup.add(base2);
        base2.setActionCommand("2");
        base2.addActionListener(this);
        north.add(base2);

        final JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(Debugger.BG_COLOR);
        inner.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0),
                BorderFactory.createLineBorder(Color.GRAY)));
        add(inner, BorderLayout.CENTER);

        this.tableModel = new MemoryDumpTableModel();
        this.table = new JTable(this.tableModel);

        this.table.getColumnModel().getColumn(0).setPreferredWidth(80);
        this.table.getColumnModel().getColumn(0).setMinWidth(80);
        this.table.getColumnModel().getColumn(0).setMaxWidth(120);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(500);

        this.table.getColumnModel().getColumn(0).setResizable(false);
        this.table.getColumnModel().getColumn(1).setResizable(false);

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
        inner.add(scroll, BorderLayout.CENTER);
    }

    /**
     * Sets the mode.
     *
     * @param theMode the new mode ({@code BYTE_MODE}, {@code WORD_MODE}, or {@code CHAR_MODE})
     */
    public void setMode(final int theMode) {

        this.tableModel.setMode(theMode);
    }

    /**
     * Sets the base in which to display memory.
     *
     * @param theBase the base (16, 10, or 2)
     */
    public void setBase(final int theBase) {

        this.tableModel.setBase(theBase);
    }

    /**
     * Handles changes to the mode or base.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if ("byte".equals(cmd)) {
            this.tableModel.setMode(BYTE_MODE);
        } else if ("word".equals(cmd)) {
            this.tableModel.setMode(WORD_MODE);
        } else if ("char".equals(cmd)) {
            this.tableModel.setMode(CHAR_MODE);
        } else if ("16".equals(cmd)) {
            this.tableModel.setBase(16);
        } else if ("10".equals(cmd)) {
            this.tableModel.setBase(10);
        } else if ("2".equals(cmd)) {
            this.tableModel.setBase(2);
        }
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

        final MemoryContext memc = theCalc.getCPU().getMemoryContext();

        this.tableModel.setData(this.ram ? memc.getRam() : memc.getFlash());
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
     * A table model for a list of rows of data.
     */
    private static final class MemoryDumpTableModel extends AbstractTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -5541294348425654382L;

        /** The data. */
        private int[] data;

        /** The display mode. */
        private int mode = BYTE_MODE;

        /** The display base. */
        private int base = 16;

        /**
         * Constructs a new {@code MemoryDumpTableModel}.
         */
        MemoryDumpTableModel() {

            super();

            this.data = new int[0];
        }

        /**
         * Sets the memory data.
         *
         * @param mem the memory whose data to copy
         */
        void setData(final Memory mem) {

            this.data = mem.asArray();
            fireTableDataChanged();
        }

        /**
         * Sets the mode.
         *
         * @param theMode the new mode ({@code BYTE_MODE}, {@code WORD_MODE}, or {@code CHAR_MODE})
         */
        void setMode(final int theMode) {

            if (theMode != this.mode && (theMode == BYTE_MODE || theMode == WORD_MODE || theMode == CHAR_MODE)) {
                this.mode = theMode;
                fireTableDataChanged();
            }
        }

        /**
         * Sets the base in which to display memory.
         *
         * @param theBase the base (16, 10, or 2)
         */
        void setBase(final int theBase) {

            if (theBase != this.base && (theBase == 16 || theBase == 10 || theBase == 2)) {
                this.base = theBase;
                fireTableDataChanged();
            }
        }

        /**
         * Gets the number of rows.
         *
         * @return the number of rows
         */
        @Override
        public int getRowCount() {

            return this.data == null ? 0 : this.data.length / 16;
        }

        /**
         * Gets the number of columns.
         *
         * @return the number of columns
         */
        @Override
        public int getColumnCount() {

            return 2;
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
                name = "Memory";
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

            if (this.data == null) {
                result.append('-');
            } else {
                switch (columnIndex) {
                    case 0:
                        final int page = (rowIndex << 4) / Memory.PAGE_SIZE;
                        final int addr = (rowIndex << 4) % Memory.PAGE_SIZE;

                        final String pageHex = Integer.toHexString(page);
                        final String addrHex = Integer.toHexString(addr);

                        result.append("<html><b>");

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
                        result.append("</b></html>");
                        break;

                    case 1:
                        final int start = rowIndex << 4;

                        switch (this.mode) {
                            case BYTE_MODE:
                                appendBytes(start, result);
                                break;
                            case WORD_MODE:
                                appendWords(start, result);
                                break;
                            case CHAR_MODE:
                                appendChars(start, result);
                                break;
                            default:
                                result.append('-');
                                break;
                        }
                        break;

                    default:
                        break;
                }
            }

            return result.toString();
        }

        /**
         * Append byte-oriented display of memory.
         *
         * @param start  the start address
         * @param result the string builder to which to append
         */
        private void appendBytes(final int start, final StringBuilder result) {

            for (int i = 0; i < 16; ++i) {
                final int value = this.data[start + i];

                switch (this.base) {
                    case 16:
                        result.append(Integer.toHexString((value >> 4) & 0x0F));
                        result.append(Integer.toHexString(value & 0x0F));
                        break;
                    case 10:
                        if (value >= 100) {
                            result.append(value);
                        } else if (value >= 10) {
                            result.append(' ');
                            result.append(value);
                        } else {
                            result.append("  ");
                            result.append(value);
                        }
                        break;
                    case 2:
                        result.append((value & 0x80) == 0x80 ? '1' : '0');
                        result.append((value & 0x40) == 0x40 ? '1' : '0');
                        result.append((value & 0x20) == 0x20 ? '1' : '0');
                        result.append((value & 0x10) == 0x10 ? '1' : '0');
                        result.append((value & 0x08) == 0x08 ? '1' : '0');
                        result.append((value & 0x04) == 0x04 ? '1' : '0');
                        result.append((value & 0x02) == 0x02 ? '1' : '0');
                        result.append((value & 0x01) == 0x01 ? '1' : '0');
                        break;
                    default:
                        break;
                }

                result.append(' ');
            }
        }

        /**
         * Append byte-oriented display of memory.
         *
         * @param start  the start address
         * @param result the string builder to which to append
         */
        private void appendWords(final int start, final StringBuilder result) {

            for (int i = 0; i < 16; i += 2) {
                final int value = this.data[start + i] + this.data[start + i + 1] << 8;

                switch (this.base) {
                    case 16:
                        result.append(Integer.toHexString((value >> 12) & 0x0F));
                        result.append(Integer.toHexString((value >> 8) & 0x0F));
                        result.append(Integer.toHexString((value >> 4) & 0x0F));
                        result.append(Integer.toHexString(0));
                        break;
                    case 10:
                        if (value >= 10000) {
                            result.append(value);
                        } else if (value >= 1000) {
                            result.append(' ');
                            result.append(value);
                        } else if (value >= 100) {
                            result.append("  ");
                            result.append(value);
                        } else if (value >= 10) {
                            result.append("   ");
                            result.append(value);
                        } else {
                            result.append("    ");
                            result.append(value);
                        }
                        break;
                    case 2:
                        result.append((value & 0x8000) == 0x8000 ? '1' : '0');
                        result.append((value & 0x4000) == 0x4000 ? '1' : '0');
                        result.append((value & 0x2000) == 0x2000 ? '1' : '0');
                        result.append((value & 0x1000) == 0x1000 ? '1' : '0');
                        result.append((value & 0x0800) == 0x0800 ? '1' : '0');
                        result.append((value & 0x0400) == 0x0400 ? '1' : '0');
                        result.append((value & 0x0200) == 0x0200 ? '1' : '0');
                        result.append((value & 0x0100) == 0x0100 ? '1' : '0');
                        result.append((value & 0x0080) == 0x0080 ? '1' : '0');
                        result.append((value & 0x0040) == 0x0040 ? '1' : '0');
                        result.append('0');
                        result.append('0');
                        result.append('0');
                        result.append('0');
                        result.append('0');
                        result.append('0');
                        break;
                    default:
                        break;
                }

                result.append(' ');
            }
        }

        /**
         * Append byte-oriented display of memory.
         *
         * @param start  the start address
         * @param result the string builder to which to append
         */
        private void appendChars(final int start, final StringBuilder result) {

            // Base is ignored in character mode
            for (int i = 0; i < 16; ++i) {
                final int value = this.data[start + i];

                if (value < 0x20) {
                    result.append(MISSING_CHAR);
                } else if (value > 0x7e && value < 0xa0) {
                    result.append(MISSING_CHAR);
                } else {
                    result.append((char) value);
                }
                result.append(' ');
            }
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
