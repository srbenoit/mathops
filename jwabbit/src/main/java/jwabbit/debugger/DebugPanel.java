package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.Serial;

/**
 * A panel that shows disassembly, RAM/FLash dumps, and a summary of CPU status.
 */
final class DebugPanel extends LoggedPanel implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2407433965428228285L;

    /** The pane that summarizes register status. */
    private final DebugRegistersPane regPane;

    /** The pane that summarizes flags status. */
    private final DebugFlagsPane flagsPane;

    /** The pane that summarizes CPU status. */
    private final DebugCpuPane cpuPane;

    /** The pane that summarizes memory map. */
    private final DebugMemoryMapPane memMapPane;

    /** The pane showing the keypad status. */
    private final DebugKeyboardPane keyPane;

    /** The pane showing the interrupt status. */
    private final DebugInterruptPane intPane;

    /** The pane showing the display status. */
    private final DebugDisplayPane dispPane;

    /** The memory dump. */
    private final MemDumpPanel dump;

    /** The flash memory dump. */
    private final MemoryDumpPanel flash;

    /** The RAM memory dump. */
    private final MemoryDumpPanel ram;

    /** The disassembly dump. */
    private final DisassemblyPanel disasm;

    /** The port monitor. */
    private final PortMonitorPanel portmon;

    /**
     * Constructs a new {@code DebugPanel}.
     *
     * @param theHandler the action handler
     */
    DebugPanel(final ActionHandler theHandler) {

        super(new BorderLayout());
        setBackground(Debugger.BG_COLOR);

        this.regPane = new DebugRegistersPane();
        this.flagsPane = new DebugFlagsPane();
        this.cpuPane = new DebugCpuPane();
        this.memMapPane = new DebugMemoryMapPane();
        this.keyPane = new DebugKeyboardPane();
        this.intPane = new DebugInterruptPane();
        this.dispPane = new DebugDisplayPane();

        // Build the east pane, a column of expandable panes summarizing CPU and memory status
        final JPanel east = new JPanel(new BorderLayout());
        east.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        east.setBackground(Color.WHITE);

        final JScrollPane scroll = new JScrollPane(east);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.LINE_END);

        final JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(Color.WHITE);
        row1.add(this.regPane, BorderLayout.PAGE_START);
        east.add(row1, BorderLayout.PAGE_START);

        final JPanel row2 = new JPanel(new BorderLayout());
        row2.setBackground(Color.WHITE);
        row2.add(this.flagsPane, BorderLayout.PAGE_START);
        row1.add(row2, BorderLayout.CENTER);

        final JPanel row3 = new JPanel(new BorderLayout());
        row3.setBackground(Color.WHITE);
        row3.add(this.cpuPane, BorderLayout.PAGE_START);
        row2.add(row3, BorderLayout.CENTER);

        final JPanel row4 = new JPanel(new BorderLayout());
        row4.setBackground(Color.WHITE);
        row4.add(this.memMapPane, BorderLayout.PAGE_START);
        row3.add(row4, BorderLayout.CENTER);

        final JPanel row5 = new JPanel(new BorderLayout());
        row5.setBackground(Color.WHITE);
        row5.add(this.keyPane, BorderLayout.PAGE_START);
        row4.add(row5, BorderLayout.CENTER);

        final JPanel row6 = new JPanel(new BorderLayout());
        row6.setBackground(Color.WHITE);
        row6.add(this.intPane, BorderLayout.PAGE_START);
        row5.add(row6, BorderLayout.CENTER);

        final JPanel row7 = new JPanel(new BorderLayout());
        row7.setBackground(Color.WHITE);
        row7.add(this.dispPane, BorderLayout.PAGE_START);
        row6.add(row7, BorderLayout.CENTER);

        // The center pane is a split pane - disassembly on top, memory dump and stack on bottom

        this.dump = new MemDumpPanel();
        this.flash = new MemoryDumpPanel(false);
        this.ram = new MemoryDumpPanel(true);
        this.disasm = new DisassemblyPanel(theHandler);
        this.portmon = new PortMonitorPanel();

        // Top panel is a tabbed pane
        final JTabbedPane pane1 = new JTabbedPane();
        pane1.setBackground(Debugger.BG_COLOR);

        pane1.addTab("Disasm 1", this.disasm);
        pane1.addTab("Port Monitor", this.portmon);

        // Bottom is also a tabbed pane on the left, stack on the right
        final JTabbedPane pane2 = new JTabbedPane();
        pane2.setBackground(Debugger.BG_COLOR);

        pane2.addTab("Mem 1", this.dump);
        pane2.addTab("Flash 1", this.flash);
        pane2.addTab("RAM 1", this.ram);

        final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane1, pane2);
        split.setBackground(Debugger.BG_COLOR);
        split.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(split, BorderLayout.CENTER);

        split.setDividerLocation(0.5);
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

        if (this.isShowing()) {
            this.regPane.calcState(theCalc);
            this.flagsPane.calcState(theCalc);
            this.cpuPane.calcState(theCalc);
            this.memMapPane.calcState(theCalc);
            this.keyPane.calcState(theCalc);
            this.intPane.calcState(theCalc);
            this.dispPane.calcState(theCalc);

            this.dump.calcState(theCalc);
            this.disasm.calcState(theCalc);
            this.flash.calcState(theCalc);
            this.ram.calcState(theCalc);
            this.portmon.calcState(theCalc);
        }
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.regPane.enableControls(enable);
        this.flagsPane.enableControls(enable);
        this.cpuPane.enableControls(enable);
        this.memMapPane.enableControls(enable);
        this.keyPane.enableControls(enable);
        this.intPane.enableControls(enable);
        this.dispPane.enableControls(enable);

        this.dump.enableControls(enable);
        this.disasm.enableControls(enable);
        this.flash.enableControls(enable);
        this.ram.enableControls(enable);
        this.portmon.enableControls(enable);
    }
}
